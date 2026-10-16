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
import java.util.Map;
import java.util.Set;

import lucee.commons.digest.MD5;
import lucee.runtime.PageContext;
import lucee.runtime.db.QoQ;
import lucee.runtime.db.SQL;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.IllegalQoQException;
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
import lucee.runtime.type.QueryColumnImpl;
import lucee.runtime.type.QueryImpl;

public final class QueryPartitions {
	// Select expressions for target query
	private Expression[] columns;
	// Array of keys for fast lookup
	private Collection.Key[] columnKeys;
	// Needed for functions and aggregates but not explicitly part of the final select
	private Set<Collection.Key> additionalColumns;
	// Ordered version of additionalColumns for stable index alignment between buildPartition and addRow.
	private Collection.Key[] additionalColumnsArr;
	// Group by expressions
	private Expression[] groupbys;
	// Target query for column references
	private QueryImpl target;
	// Map of partitioned query data. Plain HashMap — build is single-threaded.
	private HashMap<String, Partition> partitions = new HashMap<String, Partition>();
	// Reference to QoQ instance
	private QoQ qoQ;
	// SQL instance
	private SQL sql;

	/**
	 * Carries pre-resolved column refs alongside the partition's QueryImpl so addRow() can write
	 * via column.add() without looking columns up by name. Indices align with the parent's columns
	 * and additionalColumnsArr; null = skip (literals, aggregates, source-missing columns).
	 */
	public static final class Partition {
		public final QueryImpl query;
		public final QueryColumnImpl[] selectColRefs;
		public final QueryColumnImpl[] additionalColRefs;
		private int size;

		Partition(QueryImpl query, QueryColumnImpl[] selectColRefs, QueryColumnImpl[] additionalColRefs) {
			this.query = query;
			this.selectColRefs = selectColRefs;
			this.additionalColRefs = additionalColRefs;
		}

		void bumpSize() { size++; }
		int getSize() { return size; }
	}

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
	public QueryPartitions(SQL sql, Expression[] columns, Expression[] groupbys, QueryImpl target, Set<Key> additionalColumns, QoQ qoQ, boolean hasAggregateSelect)
			throws PageException {
		this.sql = sql;
		this.qoQ = qoQ;
		this.columns = columns;
		this.groupbys = groupbys;
		// This happens when using distinct with no group by
		// Just assume we're grouping on the entire select list
		if (this.groupbys.length == 0 && !hasAggregateSelect) {
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
		for (Key col: additionalColumns) {
			this.additionalColumns.add(col);
		}
		this.additionalColumnsArr = this.additionalColumns.toArray(new Collection.Key[0]);
		// Convert these Expression aliases to Keys now so we don't do it over and over later
		this.columnKeys = new Collection.Key[columns.length];
		for (int cell = 0; cell < columns.length; cell++) {
			this.columnKeys[cell] = (Caster.toKey(columns[cell].getAlias()));
		}
	}

	/**
	 * Adds empty partition for aggregating empty results
	 *
	 * @param source Source query to get data from
	 * @param target target query (for column reference)
	 * @throws PageException
	 */
	public void addEmptyPartition(QueryImpl source, QueryImpl target) throws PageException {
		partitions.put("default", buildPartition(target, source, false));
	}

	/**
	 * Call this to add a single row to the proper partition finaizedColumnVals is true when all data in
	 * the source Query is fully realized and there are no expressions left to evaluate
	 *
	 * @param pc                  PageContext
	 * @param source              Source query to get data from
	 * @param row                 Row to get data from
	 * @param finalizedColumnVals If we're adding finalized data, just copy it across. Easy. This
	 *                                applies when distincting a result set after it's already been
	 *                                processed
	 * @throws PageException
	 */
	public void addRow(PageContext pc, QueryImpl source, int row, boolean finalizedColumnVals) throws PageException {
		// Generate unique key based on row data
		String partitionKey = buildPartitionKey(pc, source, row, finalizedColumnVals);
		// Create partition if necessary — manual get+put avoids the per-row computeIfAbsent lambda.
		Partition partition = partitions.get(partitionKey);
		if (partition == null) {
			partition = buildPartition(target, source, finalizedColumnVals);
			partitions.put(partitionKey, partition);
		}
		partition.bumpSize();

		// If we're adding finalized data, just copy it across. Easy. This applies when distincting
		// a result set after it's already been processed
		if (finalizedColumnVals) {
			Collection.Key[] sourceColKeys = source.getColumnNames();
			QueryColumnImpl[] selectColRefs = partition.selectColRefs;
			for (int col = 0; col < selectColRefs.length; col++) {
				if (selectColRefs[col] != null) {
					selectColRefs[col].add(source.getColumn(sourceColKeys[col]).get(row, null));
				}
			}
		}
		// For normal group by operations, we ONLY put real data in the partition. Operations will
		// be added later, but there's no use filling up the partition with place holders
		else {
			QueryColumnImpl[] selectColRefs = partition.selectColRefs;
			for (int cell = 0; cell < columns.length; cell++) {
				Object value;
				// Literal values
				if (columns[cell] instanceof Value) {
					value = ((Value) columns[cell]).getValue();
				}
				// A column expression is read by column Key (ColumnExpression caches its source col internally)
				else if (columns[cell] instanceof ColumnExpression) {
					try {
						value = ((ColumnExpression) columns[cell]).getValue(pc, source, row, null);
					}
					catch (DatabaseException e) {
						// Wrap as IllegalQoQException to prevent fallback to HSQLDB
						IllegalQoQException iqe = new IllegalQoQException(e.getMessage(), e.getDetail(), sql, null);
						ExceptionUtil.initCauseEL(iqe, e);
						throw iqe;
					}
				}
				else {
					// Aggregates and other operations — computed in phase 3, never read from this column.
					continue;
				}
				selectColRefs[cell].add(value);
			}
			// Additional columns needed for aggregates/operations but not in the select list.
			// Refs are null where the source didn't contain that column at partition-creation time.
			QueryColumnImpl[] additionalColRefs = partition.additionalColRefs;
			for (int i = 0; i < additionalColumnsArr.length; i++) {
				QueryColumnImpl ref = additionalColRefs[i];
				if (ref != null) {
					ref.add(source.getColumn(additionalColumnsArr[i]).get(row, null));
				}
			}
		}
	}

	/**
	 * Publish per-partition row counts. column.add(...) bumps column size but not
	 * query.recordcount, so it has to be set once at the end of the build.
	 */
	public void finalizePartitionRecordCounts() {
		for (Partition p : partitions.values()) {
			p.query.setRecordcount(p.getSize());
		}
	}

	/**
	 * Generate a unique string that represents the column data being grouped on
	 *
	 * @param pc                  PageContext
	 * @param source              QueryImpl to get data from. Note, operations have not yet been
	 *                                processed
	 * @param row                 Row to get data from
	 * @param finalizedColumnVals If we're adding finalized data, just copy it across. Easy. This
	 *                                applies when distincting a result set after it's already been
	 *                                processed
	 * @return unique string
	 * @throws PageException
	 */
	public String buildPartitionKey(PageContext pc, QueryImpl source, int row, boolean finalizedColumnVals) throws PageException {
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
	public Map<String, Partition> getPartitions() {
		return partitions;
	}

	/**
	 * Get array of grouped Query objects
	 *
	 * @return
	 */
	public Query[] getPartitionArray() {
		Query[] arr = new Query[partitions.size()];
		int i = 0;
		for (Partition p : partitions.values()) arr[i++] = p.query;
		return arr;
	}

	/**
	 * Create new Query for a partition. Needs to have all ColumnExpressions in the final select as well
	 * as any additional columns required for operation expressions
	 *
	 * @param target              Query for target data (for column refernces)
	 * @param source              source query we're getting data from
	 * @param finalizedColumnVals If we're adding finalized data, just copy it across. Easy. This
	 *                                applies when distincting a result set after it's already been
	 *                                processed
	 * @return Empty Query with all the needed columns
	 * @throws PageException
	 */
	private Partition buildPartition(QueryImpl target, QueryImpl source, boolean finalizedColumnVals) throws PageException {
		QueryImpl newTarget = new QueryImpl(new Collection.Key[0], 0, "query", sql);
		QueryColumnImpl[] selectColRefs = new QueryColumnImpl[columns.length];
		QueryColumnImpl[] additionalColRefs = new QueryColumnImpl[additionalColumnsArr.length];

		// If we're just distincting fully-realized data, this is just a simple lookup
		if (finalizedColumnVals) {
			for (int i = 0; i < columns.length; i++) {
				ColumnExpression ce = (ColumnExpression) columns[i];
				newTarget.addColumn(ce.getColumn(), new ArrayImpl(), target.getColumn(target.getColumnNames()[i]).getType());
				selectColRefs[i] = (QueryColumnImpl) newTarget.getColumn(ce.getColumn());
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
					selectColRefs[i] = (QueryColumnImpl) newTarget.getColumn(alias);
				}
				else if (expSelect instanceof Literal) {
					newTarget.addColumn(alias, new ArrayImpl(), Types.OTHER);
					selectColRefs[i] = (QueryColumnImpl) newTarget.getColumn(alias);
				}
				// other expression types: selectColRefs[i] stays null; addRow's loop skips them
			}

			// As well as any additional columns that need to be used for expressions and aggregates
			// but don't appear in the final select.
			for (int i = 0; i < additionalColumnsArr.length; i++) {
				Collection.Key col = additionalColumnsArr[i];
				// This check is here because it seems the SelectsParser also lists table names as
				// ColumnExpressions
				if (source.containsKey(col)) {
					newTarget.addColumn(col, new ArrayImpl(), source.getColumn(col).getType());
					additionalColRefs[i] = (QueryColumnImpl) newTarget.getColumn(col);
				}
				// else: additionalColRefs[i] stays null; addRow's loop skips
			}
		}
		return new Partition(newTarget, selectColRefs, additionalColRefs);
	}

}