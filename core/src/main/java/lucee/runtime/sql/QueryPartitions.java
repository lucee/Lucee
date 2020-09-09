/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.runtime.sql;

import java.io.IOException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import lucee.commons.digest.MD5;
import lucee.runtime.PageContext;
import lucee.runtime.db.QoQ;
import lucee.runtime.db.SQL;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.sql.exp.ColumnExpression;
import lucee.runtime.sql.exp.Expression;
import lucee.runtime.sql.exp.Literal;
import lucee.runtime.sql.exp.op.OperationAggregate;
import lucee.runtime.sql.exp.value.Value;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;

public class QueryPartitions {
	// Select expressions for target query
	private Expression[] columns;
	// Array of keys for fast lookup
	private Collection.Key[] columnKeys;
	// Needed for functions and aggregates but not explicitly part of the final select
	private Set<Collection.Key> additionalColumns;
	// Group by expressions
	private Expression[] groupbys;
	// Target query for column references
	private QueryImpl target;
	// Mapof partitioned query data. Key is unique string representing grouped data, value is a
	// Query object representing the matching rows in that group/partition
	private HashMap<String, QueryImpl> partitions = new HashMap<String, QueryImpl>();
	// Reference to QoQ instance
	private QoQ qoQ;
	// SQL instance
	private SQL sql;

	/**
	 * Constructor
	 * 
	 * @param sql
	 * @param columns
	 * @param groupbys
	 * @param target
	 * @param additionalColumns
	 * @param qoQ
	 * @throws PageException
	 */
	public QueryPartitions(SQL sql, Expression[] columns, Expression[] groupbys, QueryImpl target, Set<String> additionalColumns, QoQ qoQ) throws PageException {
		this.sql = sql;
		this.qoQ = qoQ;
		this.columns = columns;
		this.groupbys = groupbys;
		// This happens when using distinct with no group by
		// Just assume we're grouping on the entire select list
		if (this.groupbys.length == 0) {
			ArrayList<Expression> temp = new ArrayList<Expression>();
			for (Expression col: columns) {
				if (!(col instanceof OperationAggregate)) {
					temp.add(col);
				}
			}
			this.groupbys = temp.toArray(new Expression[0]);
		}
		this.target = target;

		// Convert these strings to Keys now so we don't do it over and over later
		this.additionalColumns = new HashSet<Collection.Key>();
		for (String col: additionalColumns) {
			this.additionalColumns.add(Caster.toKey(col));
		}
		// Convert these Expression aliases to Keys now so we don't do it over and over later
		this.columnKeys = new Collection.Key[columns.length];
		for (int cell = 0; cell < columns.length; cell++) {
			this.columnKeys[cell] = (Caster.toKey(columns[cell].getAlias()));
		}
	}

	/**
	 * Call this to add a single row to the proper partition finaizedColumnVals is true when all
	 * data in the source Query is fully realized and there are no expressions left to evaluate
	 * 
	 * @param pc PageContext
	 * @param source Source query to get data from
	 * @param row Row to get data from
	 * @param finalizedColumnVals If we're adding finalized data, just copy it across. Easy. This
	 *            applies when distincting a result set after it's already been processed
	 * @throws PageException
	 */
	public void addRow(PageContext pc, Query source, int row, boolean finalizedColumnVals) throws PageException {
		// Generate unique key based on row data
		String partitionKey = buildPartitionKey(pc, source, row, finalizedColumnVals);
		// Create partition if necessary
		if (!partitions.containsKey(partitionKey)) {
			partitions.put(partitionKey, createPartition(target, source, finalizedColumnVals));
		}
		QueryImpl targetPartition = partitions.get(partitionKey);

		targetPartition.addRow(1);

		// If we're adding finalized data, just copy it across. Easy. This applies when distincting
		// a result set after it's already been processed
		if (finalizedColumnVals) {
			Collection.Key[] sourceColKeys = source.getColumnNames();
			Collection.Key[] targetColKeys = targetPartition.getColumnNames();
			for (int col = 0; col < targetColKeys.length; col++) {
				targetPartition.setAt(targetColKeys[col], targetPartition.getRecordcount(), source.getAt(sourceColKeys[col], row), true);
			}

		}
		// For normal group by operations, we ONLY put real data in the partition. Operations will
		// be added later, but there's no use filling up the partition with place holders
		else {
			for (int cell = 0; cell < columns.length; cell++) {
				// Literal values get by alias
				if (columns[cell] instanceof Value) {
					Value v = (Value) columns[cell];

					targetPartition.setAt(columnKeys[cell], targetPartition.getRecordcount(), source.getAt(Caster.toKey(v.getAlias()), row, null), true);
				}
				// A column expressions is set by column Key
				else if (columns[cell] instanceof ColumnExpression) {
					ColumnExpression ce = (ColumnExpression) columns[cell];

					targetPartition.setAt(columnKeys[cell], targetPartition.getRecordcount(), source.getAt(ce.getColumn(), row, null), true);
				}

			}
			// Populate additional columns needed for operations but are not found in the select
			// list above
			for (Collection.Key col: additionalColumns) {
				if (source.containsKey(col)) {
					targetPartition.setAt(col, targetPartition.getRecordcount(), source.getAt(col, row, null), true);
				}
			}
		}
	}

	/**
	 * Generate a unique string that represents the column data being grouped on
	 * 
	 * @param pc PageContext
	 * @param source QueryImpl to get data from. Note, operations have not yet been processed
	 * @param row Row to get data from
	 * @param finalizedColumnVals If we're adding finalized data, just copy it across. Easy. This
	 *            applies when distincting a result set after it's already been processed
	 * @return unique string
	 * @throws PageException
	 */
	public String buildPartitionKey(PageContext pc, Query source, int row, boolean finalizedColumnVals) throws PageException {
		String partitionKey = "";
		for (int cell = 0; cell < groupbys.length; cell++) {
			String value;
			// This is when reading columns out of a previous union query that doesn't have any
			// expressions in it, just literal values It's important that we are just getting this
			// value by index since the group by expressions may be a reference to the select
			// expressions from another query object
			if (finalizedColumnVals) {
				value = Caster.toString(source.getAt(source.getColumnNames()[cell], row));
			}

			else {
				value = Caster.toString(qoQ.getValue(pc, sql, source, row, null, groupbys[cell]));
			}
			// Internally Java uses a StringBuilder for this concatenation
			partitionKey += createUniqueValue(value, groupbys[cell].toString(false));
		}
		return partitionKey;
	}

	/**
	 * Helper function to turn column data into string
	 * 
	 * @param value
	 * @param col
	 * @return
	 * @throws PageException
	 */
	private String createUniqueValue(String value, String col) throws PageException {

		// There doesn't seem to be a key length on a HashMap, but it seems like a good
		// idea to hash long values. Not hashing everything, because that is slower.
		if (value.length() > 255) {
			try {
				return MD5.getDigestAsString(value);
			}
			catch (IOException e) {
				throw new DatabaseException("Unable to hash query value for column [" + col + "] for partitioning.", e.getMessage(), null, null);
			}
		}
		else {
			// Inject some characters to prevent accidental overlap of data been nearby columns
			return "______________" + value;
		}
	}

	/**
	 * Get number of partitions
	 * 
	 * @return
	 */
	public int getPartitionCount() {
		return partitions.size();
	}

	/**
	 * Get partition Map
	 * 
	 * @return
	 */
	public HashMap<String, QueryImpl> getPartitions() {
		return partitions;
	}

	/**
	 * Get array of grouped QueryImpl object
	 * 
	 * @return
	 */
	public QueryImpl[] getPartitionArray() {
		return (QueryImpl[]) partitions.values().toArray();
	}

	/**
	 * Create new QueryImpl for a partition. Needs to have all ColumnExpressions in the final select
	 * as well as any additional columns required for operation expressions
	 * 
	 * @param target Query for target data (for column refernces)
	 * @param source source query we're getting data from
	 * @param finalizedColumnValsfinalizedColumnVals If we're adding finalized data, just copy it
	 *            across. Easy. This applies when distincting a result set after it's already been
	 *            processed
	 * @return Empty QueryImpl with all the needed columns
	 * @throws PageException
	 */
	private QueryImpl createPartition(Query target, Query source, boolean finalizedColumnVals) throws PageException {
		QueryImpl newTarget = new QueryImpl(new Collection.Key[0], 0, "query", sql);

		// If we're just distincting fully-realized data, this is just a simple lookup
		if (finalizedColumnVals) {
			for (int i = 0; i < columns.length; i++) {
				ColumnExpression ce = (ColumnExpression) columns[i];
				newTarget.addColumn(ce.getColumn(), new ArrayImpl(), target.getColumn(target.getColumnNames()[i]).getType());
			}
		}
		// Standard group by
		else {

			Expression[] expSelects = columns;
			int selCount = expSelects.length;

			// Loop over all select expressions and add column to new query for every column
			// expression and literal
			for (int i = 0; i < selCount; i++) {
				Expression expSelect = expSelects[i];
				Key alias = Caster.toKey(expSelect.getAlias());

				if (expSelect instanceof ColumnExpression) {
					ColumnExpression ce = (ColumnExpression) expSelect;

					int type = Types.OTHER;
					if (!"?".equals(ce.getColumnName())) type = source.getColumn(Caster.toKey(ce.getColumnName())).getType();

					newTarget.addColumn(alias, new ArrayImpl(), type);
				}
				else if (expSelect instanceof Literal) {
					newTarget.addColumn(alias, new ArrayImpl(), Types.OTHER);
				}
			}

			// As well as any additional columns that need to be used for expressions and aggregates
			// but don't appear in the final select.
			for (Collection.Key col: additionalColumns) {
				// This check is here because it seems the SelectsParser also lists table names as
				// ColumnExpressions
				if (source.containsKey(col)) {
					newTarget.addColumn(col, new ArrayImpl(), source.getColumn(col).getType());
				}
			}
		}
		return newTarget;
	}

}