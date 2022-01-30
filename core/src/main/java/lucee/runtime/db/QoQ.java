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
package lucee.runtime.db;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import lucee.commons.lang.CFTypes;
import lucee.commons.lang.StringUtil;
import lucee.commons.math.MathUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.op.OpUtil;
import lucee.runtime.sql.QueryPartitions;
import lucee.runtime.sql.Select;
import lucee.runtime.sql.SelectParser;
import lucee.runtime.sql.Selects;
import lucee.runtime.sql.exp.BracketExpression;
import lucee.runtime.sql.exp.Column;
import lucee.runtime.sql.exp.ColumnExpression;
import lucee.runtime.sql.exp.Expression;
import lucee.runtime.sql.exp.Literal;
import lucee.runtime.sql.exp.op.Operation;
import lucee.runtime.sql.exp.op.Operation1;
import lucee.runtime.sql.exp.op.Operation2;
import lucee.runtime.sql.exp.op.Operation3;
import lucee.runtime.sql.exp.op.OperationAggregate;
import lucee.runtime.sql.exp.op.OperationN;
import lucee.runtime.sql.exp.value.Value;
import lucee.runtime.sql.exp.value.ValueNumber;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryColumn;
import lucee.runtime.type.QueryColumnImpl;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.util.ArrayUtil;

/**
 * 
 */
public final class QoQ {
	final static private Collection.Key paramKey = new KeyImpl("?");

	/**
	 * Execute a QofQ against a SQL object
	 * 
	 * @param pc
	 * @param sql
	 * @param maxrows
	 * @return
	 * @throws PageException
	 */
	public Query execute(PageContext pc, SQL sql, int maxrows) throws PageException {
		try {
			SelectParser parser = new SelectParser();
			Selects selects = parser.parse(sql.getSQLString());

			return execute(pc, sql, selects, maxrows);
		}
		catch (Throwable t) {
			throw Caster.toPageException(t);
		}
	}

	/**
	 * execute a SQL Statement against CFML Scopes
	 */
	public Query execute(PageContext pc, SQL sql, Selects selects, int maxrows) throws PageException {
		Select[] arrSelects = selects.getSelects();
		boolean isUnion = (arrSelects.length > 1);

		Query target = new QueryImpl(new Collection.Key[0], 0, "query", sql);

		// For each select (more than one when using union)
		for (int i = 0; i < arrSelects.length; i++) {
			arrSelects[i].getFroms();
			Column[] froms = arrSelects[i].getFroms();

			if (froms.length > 1) throw new DatabaseException("Native QoQ can only select from a single tables at a time, falling back to HSQLDB.", sql.toString(), sql, null);

			// Lookup actual Query variable on page
			Query source = getSingleTable(pc, froms[0]);
			arrSelects[i].expandAsterisks(source);

			// Unions don't allow operations in the order by
			if (!isUnion) {
				selects.calcOrderByExpressions();
			}
			// Run a select statement. If we have a union, we run this once per select being unioned
			target = executeSingle(pc, arrSelects[i], getSingleTable(pc, froms[0]), target, isUnion ? -1 : maxrows, sql, selects.getOrderbys().length > 0, isUnion);
		}

		// DON'T GET THIS SOONER! We recalculate the order bys above based on the columns in the
		// first select
		Expression[] orders = selects.getOrderbys();

		// Order By
		if (orders.length > 0) {
			order(target, orders, isUnion, sql);
			// Clean up extra columns that we added in just for the sorting
			for (Collection.Key col: target.getColumnNames()) {
				if (col.getLowerString().startsWith("__order_by_expression__")) {
					target.removeColumn(col);
				}
			}
		}

		// If we only had a single select, but couldn't apply the top earlier, apply it now
		if (!isUnion) {
			ValueNumber oTop = arrSelects[0].getTop();
			int top = -1;
			if (oTop != null) {
				top = (int) oTop.getValueAsDouble();
				if (maxrows == -1 || maxrows > top) maxrows = top;
			}
		}
		// Choppy chop
		if (maxrows > -1) {
			((QueryImpl) target).cutRowsTo(maxrows);
		}

		// New query is populated and ready to go!
		return target;
	}

	/**
	 * Order the rows in a query
	 * 
	 * @param target Query to order
	 * @param columns Column expressions to order on
	 * @param isUnion Is this a union
	 * @param sql
	 * @throws PageException
	 */
	private static void order(Query target, Expression[] columns, boolean isUnion, SQL sql) throws PageException {
		Expression col;
		// Looping backwards over columns so they order correctly
		for (int i = columns.length - 1; i >= 0; i--) {
			col = columns[i];
			if (!isUnion) {
				// order by 'test' -- just ignore this
				if (col instanceof Literal) return;
				// order by ? -- ignore this as well
				if (col instanceof Column && ((Column) col).getColumn().equals(paramKey)) return;

				// Lookup column in query based on the index stored in the order by expression
				target.sort(target.getColumnNames()[col.getIndex() - 1], col.isDirectionBackward() ? Query.ORDER_DESC : Query.ORDER_ASC);
			}
			else if (col instanceof Column) {
				Column c = (Column) col;
				// Lookup column in query based on name of column. unions don't allow operations in
				// the order by
				target.sort(c.getColumn(), col.isDirectionBackward() ? Query.ORDER_DESC : Query.ORDER_ASC);
			}
			else {
				throw new DatabaseException("ORDER BY items must be a column name/alias from the first select list if the statement contains a UNION operator", sql.toString(), sql,
						null);
			}
		}
	}

	/**
	 * Process a single select statement. If this is a union, append it to the incoming "previous" Query
	 * and return the new, combined query with all rows
	 * 
	 * @param pc PageContext
	 * @param select Select instance
	 * @param source Source query to pull data from
	 * @param previous Previous query in case of union. May be empty if this is the first select in the
	 *            union
	 * @param maxrows max rows from cfquery tag. Not necessarily the same as TOP
	 * @param sql SQL object
	 * @param hasOrders Is this overall Selects instance ordered? This affects whether we can optimize
	 *            maxrows or not
	 * @param isUnion Is this select part of a union of several selects
	 * @return
	 * @throws PageException
	 */
	private Query executeSingle(PageContext pc, Select select, Query source, Query previous, int maxrows, SQL sql, boolean hasOrders, boolean isUnion) throws PageException {

		// Our records will be placed here to return
		Query target = new QueryImpl(new Collection.Key[0], 0, "query", sql);

		// Make max rows the smaller of the two
		ValueNumber oTop = select.getTop();
		int top = -1;
		if (oTop != null) {
			top = (int) oTop.getValueAsDouble();
			if (maxrows == -1 || maxrows > top) maxrows = top;
		}

		Expression[] expSelects = select.getSelects();
		int selCount = expSelects.length;

		Map<Collection.Key, Object> expSelectsMap = new HashMap<Collection.Key, Object>();
		// Build up the final columns we need in our target query
		for (int i = 0; i < selCount; i++) {
			Expression expSelect = expSelects[i];
			Key alias = Caster.toKey(expSelect.getAlias());
			expSelectsMap.put(alias, expSelect);
			int type = Types.OTHER;
			if (expSelect instanceof ColumnExpression) {
				ColumnExpression ce = (ColumnExpression) expSelect;
				// A query param being selected back out uses the type other. We should probably use
				// the query param type, but we don't actually know what param we'll bind to at this
				// point.
				if (!ce.isParam()) type = source.getColumn(Caster.toKey(ce.getColumnName())).getType();
			}
			queryAddColumn(target, alias, type);
		}

		Collection.Key[] headers = expSelectsMap.keySet().toArray(new Collection.Key[expSelectsMap.size()]);

		// get target columns
		QueryColumn[] trgColumns = new QueryColumn[headers.length];
		Object[] trgValues = new Object[headers.length];
		for (int cell = 0; cell < headers.length; cell++) {
			trgColumns[cell] = target.getColumn(headers[cell]);
			trgValues[cell] = expSelectsMap.get(headers[cell]);
		}

		// If have a group by, a distinct, or this is part of a "union", or has aggregates in the
		// select list then we partition
		if (select.getGroupbys().length > 0 || select.isDistinct() || (select.hasAggregateSelect() && select.getWhere() != null)) {
			executeSinglePartitioned(pc, select, source, target, maxrows, sql, hasOrders, isUnion, trgColumns, trgValues, headers);
		}
		// This is a "normal" select with no partitioning
		else {
			executeSingleNonPartitioned(pc, select, source, target, maxrows, sql, hasOrders, isUnion, trgColumns, trgValues, headers);
		}

		// Top is applied to a union regardless of order. This is because you can't order the
		// individual selects of a union. You can only order the final result. So any top on an
		// individual select is just blindly applied to whatever order the records may be in
		if (isUnion && top > -1) {
			((QueryImpl) target).cutRowsTo(top);
		}

		// For a union all, we just slam all the rows together, keeping any duplicate record
		if (isUnion && !select.isUnionDistinct()) {
			return doUnionAll(previous, target, sql);
		}
		// If this is a select following a "union" or "union distinct", then everything gets
		// distincted. Load up the partitions with all the existing rows in the target thus far
		else if (isUnion && select.isUnionDistinct()) {
			return doUnionDistinct(pc, previous, target, sql);
		}

		return target;
	}

	/**
	 * Combine two queries while retaining all rows.
	 * 
	 * @param previous Query from previous select to union
	 * @param target New query to add into the previous
	 * @return Combined Query with potential duplicate rows
	 * @throws PageException
	 */
	private Query doUnionAll(Query previous, Query target, SQL sql) throws PageException {
		// If this is the first select in a series of unions, just return it directly. It's column
		// names now get set in stone as the column names the next union(s) will use!
		if (previous.getRecordcount() == 0) {
			return target;
		}
		Collection.Key[] previousColKeys = previous.getColumnNames();
		Collection.Key[] targetColKeys = target.getColumnNames();

		if( previousColKeys.length != targetColKeys.length ) {
			throw new DatabaseException("Cannot perform union as number of columns in selects do not match.", null, sql, null);
		}

		// Queries being joined need to have the same number of columns and the data is full
		// realized, so just copy it over positionally. The column names may not match, but that's
		// fine.
		for (int row = 1; row <= target.getRecordcount(); row++) {
			previous.addRow(1);
			for (int col = 0; col < targetColKeys.length; col++) {
				previous.setAt(previousColKeys[col], previous.getRecordcount(), target.getAt(targetColKeys[col], row));
			}
		}
		return previous;
	}

	/**
	 * Combine two queries while removing duplicate rows
	 * 
	 * @param pc PageContext
	 * @param previous Query from previous select to union
	 * @param target New query to add into the previous
	 * @param sql SQL instance
	 * @return Combined Query with no duplicate rows
	 * @throws PageException
	 */
	private Query doUnionDistinct(PageContext pc, Query previous, Query target, SQL sql) throws PageException {
		// If this is the first select in a series of unions, just return it directly. It's column
		// names now get set in stone as the column names the next union(s) will use!
		if (previous.getRecordcount() == 0) {
			return target;
		}
		Collection.Key[] previousColKeys = previous.getColumnNames();
		Collection.Key[] targetColKeys = target.getColumnNames();
		
		if( previousColKeys.length != targetColKeys.length ) {
			throw new DatabaseException("Cannot perform union as number of columns in selects do not match.", null, sql, null);
		}
		
		Expression[] selectExpressions = new Expression[previousColKeys.length];
		// We want the exact columns from the previous query, but not necessarily all the data. Make
		// a new target and copy the columns
		Query newTarget = new QueryImpl(new Collection.Key[0], 0, "query", sql);
		for (int col = 0; col < previousColKeys.length; col++) {
			newTarget.addColumn(previousColKeys[col], new ArrayImpl(), previous.getColumn(previousColKeys[col]).getType());
			// While we're looping, build up a handy array of expressions from the previous query.
			selectExpressions[col] = new ColumnExpression(previousColKeys[col].getString(), 0);
		}

		// Initialize our object to track the partitions
		QueryPartitions queryPartitions = new QueryPartitions(sql, selectExpressions, new Expression[0], newTarget, new HashSet<String>(), this);

		// Add in all the rows from our previous work
		for (int row = 1; row <= previous.getRecordcount(); row++) {
			queryPartitions.addRow(pc, previous, row, true);
		}
		// ...and all of the new rows
		for (int row = 1; row <= target.getRecordcount(); row++) {
			queryPartitions.addRow(pc, target, row, true);
		}

		// Loop over the partitions and take one from each and add to our new target question for a
		// distinct result
		for (Query sourcePartition: queryPartitions.getPartitions().values()) {
			newTarget.addRow(1);

			for (int col = 0; col < targetColKeys.length; col++) {
				newTarget.setAt(previousColKeys[col], newTarget.getRecordcount(), sourcePartition.getAt(previousColKeys[col], 1));
			}

		}
		return newTarget;
	}

	/**
	 * Process a single select that is not partitioned (grouped or distinct)
	 * 
	 * @param pc PageContext
	 * @param select Select instance
	 * @param source Query we're select from
	 * @param target Query object we're adding rows into. (passed back by reference)
	 * @param maxrows Max rows from cfquery.
	 * @param sql
	 * @param hasOrders Is this overall query ordered?
	 * @param isUnion Is this part of a union?
	 * @param trgColumns Lookup array of column
	 * @param trgValues Lookup array of expressions
	 * @param headers Select lists
	 * @throws PageException
	 */
	private void executeSingleNonPartitioned(PageContext pc, Select select, Query source, Query target, int maxrows, SQL sql, boolean hasOrders, boolean isUnion,
			QueryColumn[] trgColumns, Object[] trgValues, Collection.Key[] headers) throws PageException {
		Operation where = select.getWhere();

		// If we are ordering or distincting the result, we can't enforce the maxrows until after
		// we've built the entire query
		boolean hasMaxrow = maxrows > -1 && !hasOrders;
		// Is there at least on aggregate expression in the select list
		boolean hasAggregateSelect = select.hasAggregateSelect();

		// For a non-grouping query with aggregates in the select such as
		// SELECT count(1) FROM qry
		// then we need to return a single row
		if (hasAggregateSelect && source.getRecordcount() == 0) {
			target.addRow(1);
			for (int cell = 0; cell < headers.length; cell++) {
				trgColumns[cell].set(1, getValue(pc, sql, source, 1, headers[cell], trgValues[cell]));
			}
			return;
		}

		// Loop over all rows in the source query
		for (int row = 1; row <= source.getRecordcount(); row++) {
			// Does this do anything??
			sql.setPosition(0);
			// If we can, optimize the max rows exit strategy
			if (hasMaxrow && maxrows <= target.getRecordcount()) break;

			// The where clause is a single Operation expression that returns try or false. Does
			// this row match the where clause, if any?
			if (where == null || Caster.toBooleanValue(executeExp(pc, sql, source, where, row))) {
				target.addRow(1);
				// If we have a match, add this row into the target query
				for (int cell = 0; cell < headers.length; cell++) {
					trgColumns[cell].set(target.getRecordcount(), getValue(pc, sql, source, row, headers[cell], trgValues[cell]));
				}
			}

			// If this was a non-grouped select with only aggregates like select "count(1) from
			// table" than bail after a single row
			if (hasAggregateSelect) {
				break;
			}
		}

	}

	/**
	 * Process a single select that is partitioned (grouped)
	 * 
	 * @param pc PageContext
	 * @param select Select instance
	 * @param source Query we're selecting from
	 * @param target Query object we're adding rows into. (passed back by reference)
	 * @param maxrows
	 * @param sql
	 * @param hasOrders Is this overall query ordered?
	 * @param isUnion Is this part of a union?
	 * @param trgColumns Lookup array of column
	 * @param trgValues Lookup array of expressions
	 * @param headers select columns
	 * @throws PageException
	 */
	private void executeSinglePartitioned(PageContext pc, Select select, Query source, Query target, int maxrows, SQL sql, boolean hasOrders, boolean isUnion,
			QueryColumn[] trgColumns, Object[] trgValues, Collection.Key[] headers) throws PageException {

		// Is there at least on aggregate expression in the select list
		boolean hasAggregateSelect = select.hasAggregateSelect();

		// For a non-grouping query with aggregates in the select such as
		// SELECT count(1) FROM qry WHERE col='foo'
		// then we need to return a single row
		if (hasAggregateSelect && select.getGroupbys().length == 0 && source.getRecordcount() == 0) {
			target.addRow(1);
			for (int cell = 0; cell < headers.length; cell++) {
				trgColumns[cell].set(1, getValue(pc, sql, source, 1, headers[cell], trgValues[cell]));
			}
			return;
		}

		Operation where = select.getWhere();
		// Initialize object to track our partitioned data
		QueryPartitions queryPartitions = new QueryPartitions(sql, select.getSelects(), select.getGroupbys(), target, select.getAdditionalColumns(), this);

		// For all records in the source query
		for (int row = 1; row <= source.getRecordcount(); row++) {
			sql.setPosition(0);
			// If where operation is matched (or doesn't exist) ....
			if (where == null || Caster.toBooleanValue(executeExp(pc, sql, source, where, row))) {
				// ... add this row to our partitioned data
				queryPartitions.addRow(pc, source, row, false);
			}
		}

		// Now that all rows are partitioned, eliminate partitions we don't need via the having
		// clause
		if (select.getHaving() != null) {
			// Loop over each partition
			Set<Entry<String, Query>> set = queryPartitions.getPartitions().entrySet();
			for (Entry<String, Query> entry: set.toArray(new Entry[set.size()])) {
				// Eval the having clause on it
				if (!Caster.toBooleanValue(executeExp(pc, sql, entry.getValue(), select.getHaving(), 1))) {
					// Voted off the island :/
					queryPartitions.getPartitions().remove(entry.getKey());
				}
				// ColumnExpressions cache the actual query column they use internally
				// need to reset any cache data in the having Expression
				// since each iteration is on a diff Query instance
				select.getHaving().reset();
			}
		}

		// For a non-grouping query with aggregates where no records matched the where clause
		// SELECT count(1) FROM qry WHERE 1=0
		// then we need to add a single empty partition so our final select will have a single row.
		if (hasAggregateSelect && select.getGroupbys().length == 0 && queryPartitions.getPartitions().size() == 0) {
			queryPartitions.addEmptyPartition(source, target);
		}

		// Add first row of each group of partitioned data into final result
		for (Query sourcePartition: queryPartitions.getPartitions().values()) {
			target.addRow(1);
			for (int cell = 0; cell < headers.length; cell++) {

				// finally processing column expressions and aggregates
				if (trgValues[cell] instanceof Expression) {
					Expression exp = (Expression) trgValues[cell];
					// Sharing columnExpressions across different query objects
					// Can't have them caching those columns
					exp.reset();
				}
				// If this is a column
				if (trgValues[cell] instanceof ColumnExpression) {
					ColumnExpression ce = (ColumnExpression) trgValues[cell];
					if (ce.getColumn().equals(paramKey)) {
						target.setAt(headers[cell], target.getRecordcount(), getValue(pc, sql, sourcePartition, 1, null, trgValues[cell]));
					}
					else {
						// Then make sure to use the alias now to reference it since it changed
						// names after going into the partition
						target.setAt(headers[cell], target.getRecordcount(), getValue(pc, sql, sourcePartition, 1, ce.getColumnAlias(), null));
					}
				}
				// For Operations, just execute them normally
				else {
					target.setAt(headers[cell], target.getRecordcount(), getValue(pc, sql, sourcePartition, 1, null, trgValues[cell]));
				}
			}

		}

	}

	/**
	 * Helper for adding a column to a query
	 * 
	 * @param query
	 * @param column
	 * @param type
	 * @throws PageException
	 */
	private void queryAddColumn(Query query, Collection.Key column, int type) throws PageException {
		if (!query.containsKey(column)) {
			query.addColumn(column, new ArrayImpl(), type);
		}
	}

	/**
	 * return value
	 * 
	 * @param sql
	 * @param querySource
	 * @param row
	 * @param key
	 * @param value
	 * @return value
	 * @throws PageException
	 */
	public Object getValue(PageContext pc, SQL sql, Query querySource, int row, Collection.Key key, Object value) throws PageException {
		if (value instanceof Expression) return executeExp(pc, sql, querySource, ((Expression) value), row);
		return querySource.getAt(key, row, null);
	}

	/**
	 * @param pc Page Context of the Request
	 * @param table ZQLQuery
	 * @return Query
	 * @throws PageException
	 */
	private Query getSingleTable(PageContext pc, Column table) throws PageException {
		return Caster.toQuery(pc.getVariable(table.getFullName()));
	}

	/**
	 * Executes a ZEXp
	 * 
	 * @param sql
	 * @param source Query Result
	 * @param exp expression to execute
	 * @param row current row of resultset
	 * @return result
	 * @throws PageException
	 */
	private Object executeExp(PageContext pc, SQL sql, Query source, Expression exp, int row) throws PageException {
		if (exp instanceof Value) return ((Value) exp).getValue();// executeConstant(sql,qr,
		if (exp instanceof Column) return executeColumn(pc, sql, source, (Column) exp, row);
		if (exp instanceof Operation) return executeOperation(pc, sql, source, (Operation) exp, row);
		if (exp instanceof BracketExpression) return executeBracked(pc, sql, source, (BracketExpression) exp, row);
		throw new DatabaseException("unsupported sql statement [" + exp + "]", null, sql, null);
	}

	private Object executeExp(PageContext pc, SQL sql, Query source, Expression exp, int row, Object columnDefault) throws PageException {
		if (exp instanceof Value) return ((Value) exp).getValue();// executeConstant(sql,qr,
		if (exp instanceof Column) return executeColumn(pc, sql, source, (Column) exp, row, columnDefault);
		if (exp instanceof Operation) return executeOperation(pc, sql, source, (Operation) exp, row);
		if (exp instanceof BracketExpression) return executeBracked(pc, sql, source, (BracketExpression) exp, row);
		throw new DatabaseException("unsupported sql statement [" + exp + "]", null, sql, null);
	}

	/**
	 * Accepts an expression which is the input to an aggregate operation.
	 * 
	 * @param pc
	 * @param sql
	 * @param source
	 * @param exp
	 * @param row
	 * @param includeNull
	 * @return an array with as many items as rows in the source query containing the corresponding
	 *         expression result for each matching row
	 * @throws PageException
	 */
	private Object[] executeAggregateExp(PageContext pc, SQL sql, Query source, Expression exp, boolean includeNull, boolean returnDistinct) throws PageException {
		Object[] result = new Object[source.getRecordcount()];

		// For a literal value, just fill an array with that value
		if (exp instanceof Value) {
			Object value = ((Value) exp).getValue();
			for (int i = 0; i < source.getRecordcount(); i++) {
				result[i] = value;
			}
			return result;
		}

		// For a column, return the data in that column as an array
		if (exp instanceof Column) {
			result = ((QueryColumnImpl) source.getColumn(((Column) exp).getColumn())).toArray();
			// Simple return if we want all values
			if (!returnDistinct && includeNull) {
				return result;
			}
			// if we want to filter nulls or distinct the array
			else {
				Stream<Object> resultStream = Arrays.stream(result);
				if (!includeNull) resultStream = resultStream.filter(s -> (s != null));
				if (returnDistinct) resultStream = resultStream.distinct();
				return resultStream.toArray();
			}
		}
		// For an operation, we need to execute the operation once for each row and capture the
		// results as our final array
		if (exp instanceof Operation) {
			for (int i = 0; i < source.getRecordcount(); i++) {
				result[i] = executeOperation(pc, sql, source, (Operation) exp, i + 1);
			}

			// Simple return if we want all values
			if (!returnDistinct && includeNull) {
				return result;
			}
			// if we want to filter nulls or distinct the array
			else {
				Stream<Object> resultStream = Arrays.stream(result);
				if (!includeNull) resultStream = resultStream.filter(s -> (s != null));
				if (returnDistinct) resultStream = resultStream.distinct();
				return resultStream.toArray();
			}
		}
		throw new DatabaseException("unsupported sql statement [" + exp + "]", null, sql, null);
	}

	private Object executeOperation(PageContext pc, SQL sql, Query source, Operation operation, int row) throws PageException {

		if (operation instanceof Operation2) {
			Operation2 op2 = (Operation2) operation;

			switch (op2.getOperator()) {
			case Operation.OPERATION2_AND:
				return executeAnd(pc, sql, source, op2, row);
			case Operation.OPERATION2_OR:
				return executeOr(pc, sql, source, op2, row);
			case Operation.OPERATION2_XOR:
				return executeXor(pc, sql, source, op2, row);
			case Operation.OPERATION2_EQ:
				return executeEQ(pc, sql, source, op2, row);
			case Operation.OPERATION2_NEQ:
				return executeNEQ(pc, sql, source, op2, row);
			case Operation.OPERATION2_LTGT:
				return executeNEQ(pc, sql, source, op2, row);
			case Operation.OPERATION2_LT:
				return executeLT(pc, sql, source, op2, row);
			case Operation.OPERATION2_LTE:
				return executeLTE(pc, sql, source, op2, row);
			case Operation.OPERATION2_GT:
				return executeGT(pc, sql, source, op2, row);
			case Operation.OPERATION2_GTE:
				return executeGTE(pc, sql, source, op2, row);
			case Operation.OPERATION2_MINUS:
				return executeMinus(pc, sql, source, op2, row);
			case Operation.OPERATION2_PLUS:
				return executePlus(pc, sql, source, op2, row);
			case Operation.OPERATION2_DIVIDE:
				return executeDivide(pc, sql, source, op2, row);
			case Operation.OPERATION2_MULTIPLY:
				return executeMultiply(pc, sql, source, op2, row);
			case Operation.OPERATION2_EXP:
				return executeExponent(pc, sql, source, op2, row);
			case Operation.OPERATION2_LIKE:
				return Caster.toBoolean(executeLike(pc, sql, source, op2, row));
			case Operation.OPERATION2_NOT_LIKE:
				return Caster.toBoolean(!executeLike(pc, sql, source, op2, row));
			case Operation.OPERATION2_MOD:
				return executeMod(pc, sql, source, op2, row);
			}

		}

		if (operation instanceof Operation1) {
			Operation1 op1 = (Operation1) operation;
			int o = op1.getOperator();

			if (o == Operation.OPERATION1_IS_NULL) {
				Object value = executeExp(pc, sql, source, op1.getExp(), row, null);
				return Caster.toBoolean(value == null);
			}
			if (o == Operation.OPERATION1_IS_NOT_NULL) {
				Object value = executeExp(pc, sql, source, op1.getExp(), row, null);
				return Caster.toBoolean(value != null);
			}

			Object value = executeExp(pc, sql, source, op1.getExp(), row);

			if (o == Operation.OPERATION1_MINUS) return Caster.toDouble(-Caster.toDoubleValue(value));
			if (o == Operation.OPERATION1_PLUS) return Caster.toDouble(value);
			if (o == Operation.OPERATION1_NOT) return Caster.toBoolean(!Caster.toBooleanValue(value));

		}

		if (operation instanceof Operation3) {
			Operation3 op3 = (Operation3) operation;
			int o = op3.getOperator();
			if (o == Operation.OPERATION3_BETWEEN) return executeBetween(pc, sql, source, op3, row);
			if (o == Operation.OPERATION3_LIKE) return executeLike(pc, sql, source, op3, row);
		}

		if (!(operation instanceof OperationN)) throw new DatabaseException("invalid syntax for SQL Statement", null, sql, null);

		OperationN opn = (OperationN) operation;

		String op = opn.getOperator();
		Expression[] operators = opn.getOperants();

		// 11111111111111111111111111111111111111111111111111111
		if (operators.length == 1) {

			Object value = null;
			Object[] aggregateValues = null;

			// Aggregate operations use the entire array of values for the column instead
			// of a single value at a given row
			if (operation instanceof OperationAggregate) {
				// count() has special handling below
				if (!op.equals("count")) {
					aggregateValues = executeAggregateExp(pc, sql, source, operators[0], false, false);
				}
			}
			else {
				value = executeExp(pc, sql, source, operators[0], row);
			}

			// Functions
			switch (op.charAt(0)) {
			case 'a':
				if (op.equals("abs")) return new Double(MathUtil.abs(Caster.toDoubleValue(value)));
				if (op.equals("acos")) return new Double(Math.acos(Caster.toDoubleValue(value)));
				if (op.equals("asin")) return new Double(Math.asin(Caster.toDoubleValue(value)));
				if (op.equals("atan")) return new Double(Math.atan(Caster.toDoubleValue(value)));
				if (op.equals("avg")) {
					// If there are no non-null values, return empty
					if (aggregateValues.length == 0) {
						return (NullSupportHelper.full(pc) ? null : "");
					}
					return ArrayUtil.avg(Caster.toArray(aggregateValues));
				}
				break;
			case 'c':
				if (op.equals("ceiling")) return new Double(Math.ceil(Caster.toDoubleValue(value)));
				if (op.equals("cos")) return new Double(Math.cos(Caster.toDoubleValue(value)));
				if (op.equals("cast")) return Caster.castTo(pc, CFTypes.toShort(operators[0].getAlias(), true, CFTypes.TYPE_UNKNOW), operators[0].getAlias(), value);
				if (op.equals("count")) return executeCount(pc, sql, source, operators);
				if (op.equals("coalesce")) return executeCoalesce(pc, sql, source, operators, row);
				break;
			case 'e':
				if (op.equals("exp")) return new Double(Math.exp(Caster.toDoubleValue(value)));
				break;
			case 'f':
				if (op.equals("floor")) return new Double(Math.floor(Caster.toDoubleValue(value)));
				break;
			case 'u':
				if (op.equals("upper") || op.equals("ucase")) return Caster.toString(value).toUpperCase();
				break;

			case 'l':
				if (op.equals("lower") || op.equals("lcase")) return Caster.toString(value).toLowerCase();
				if (op.equals("ltrim")) return StringUtil.ltrim(Caster.toString(value), null);
				if (op.equals("length")) return new Double(Caster.toString(value).length());
				break;
			case 'm':
				if (op.equals("max") || op.equals("min")) {
					// Get column data as array
					Array colData = Caster.toArray(aggregateValues);
					// Get column type
					String colType = QueryImpl.getColumTypeName(Types.OTHER);

					// If we're passing a column directly, get the type from it
					if ((operators[0] instanceof ColumnExpression)) {
						ColumnExpression ce = (ColumnExpression) operators[0];
						colType = source.getColumn(ce.getColumn()).getTypeAsString();
					}
					// If we're wrapping another scalar function, guess the type based on the
					// first value
					else if (operators[0] instanceof Operation && aggregateValues.length > 0) {
						if (Decision.isNumber(aggregateValues[0])) {
							colType = "NUMERIC";
						}
					}

					String sortDir = "desc";
					String sortType = "text";
					if (op.equals("min")) {
						sortDir = "asc";
					}
					// Numeric-based sort
					if (colType.equals("NUMERIC") || colType.equals("INTEGER") || colType.equals("DOUBLE") || colType.equals("DECIMAL") || colType.equals("BIGINT")
							|| colType.equals("TINYINT") || colType.equals("SMALLINT") || colType.equals("REAL")) {
						sortType = "numeric";
					}

					// text-based sort
					java.util.Comparator comp = ArrayUtil.toComparator(pc, sortType, sortDir, false);
					// Sort the array with proper type and direction
					colData.sortIt(comp);

					// If there are no non-null values, return empty
					if (colData.size() == 0) {
						return (NullSupportHelper.full(pc) ? null : "");
					}
					// The first item in the array is our "max" or "min"
					return colData.getE(1);
				}
				break;
			case 'r':
				if (op.equals("rtrim")) return StringUtil.rtrim(Caster.toString(value), null);
				break;
			case 's':
				if (op.equals("sign")) return new Double(MathUtil.sgn(Caster.toDoubleValue(value)));
				if (op.equals("sin")) return new Double(Math.sin(Caster.toDoubleValue(value)));
				if (op.equals("soundex")) return StringUtil.soundex(Caster.toString(value));
				if (op.equals("sin")) return new Double(Math.sqrt(Caster.toDoubleValue(value)));
				if (op.equals("sum")) {
					// If there are no non-null values, return empty
					if (aggregateValues.length == 0) {
						return (NullSupportHelper.full(pc) ? null : "");
					}
					return ArrayUtil.sum(Caster.toArray(aggregateValues));
				}
				break;
			case 't':
				if (op.equals("tan")) return new Double(Math.tan(Caster.toDoubleValue(value)));
				if (op.equals("trim")) return Caster.toString(value).trim();
				break;
			}

		}

		// 22222222222222222222222222222222222222222222222222222
		else if (operators.length == 2) {

			// if(op.equals("=") || op.equals("in")) return executeEQ(pc,sql,qr,expression,row);

			Object left = executeExp(pc, sql, source, operators[0], row);
			Object right = executeExp(pc, sql, source, operators[1], row);

			// Functions
			switch (op.charAt(0)) {
			case 'a':
				if (op.equals("atan2")) return new Double(Math.atan2(Caster.toDoubleValue(left), Caster.toDoubleValue(right)));
				break;
			case 'b':
				if (op.equals("bitand")) return OpUtil.bitand(pc, Caster.toDoubleValue(left), Caster.toDoubleValue(right));
				if (op.equals("bitor")) return OpUtil.bitor(pc, Caster.toDoubleValue(left), Caster.toDoubleValue(right));
				break;
			case 'c':
				if (op.equals("concat")) return Caster.toString(left).concat(Caster.toString(right));
				if (op.equals("count")) return executeCount(pc, sql, source, operators);
				if (op.equals("coalesce")) return executeCoalesce(pc, sql, source, operators, row);
				break;
			case 'i':
				if (op.equals("isnull")) return executeCoalesce(pc, sql, source, operators, row);
				break;
			case 'm':
				if (op.equals("mod")) return OpUtil.modulusRef(pc, Caster.toDoubleValue(left), Caster.toDoubleValue(right));
				break;

			}

		}
		// 3333333333333333333333333333333333333333333333333333333333333333333

		if (op.equals("in")) return executeIn(pc, sql, source, opn, row, false);
		if (op.equals("not_in")) return executeIn(pc, sql, source, opn, row, true);

		if (op.equals("coalesce")) return executeCoalesce(pc, sql, source, operators, row);

		if (op.equals("count")) return executeCount(pc, sql, source, operators);

		throw new DatabaseException("unsupported sql statement (" + op + ") ", null, sql, null);

	}

	private Integer executeCount(PageContext pc, SQL sql, Query source, Expression[] inputs) throws PageException {
		boolean isDistinct = false;
		List<Expression> inputList = new ArrayList<Expression>(Arrays.asList(inputs));
		Expression first = inputList.get(0);
		if (inputList.size() > 1 && first instanceof Value && ((Value) first).getString().equals("all")) {
			inputList.remove(0);
		}
		else if (inputList.size() > 1 && first instanceof Value && ((Value) first).getString().equals("distinct")) {
			isDistinct = true;
			inputList.remove(0);
		}
		if (inputList.size() > 1) {
			throw new DatabaseException("count() aggregate doesn't support more than one expression at this time", null, sql, null);
		}
		Expression input = inputList.get(0);
		// count(*), count(1), or count('asdf') just count the rows
		if ((input instanceof Column && ((Column) input).getAlias().equals("*")) || input instanceof Value) {
			return Caster.toIntValue(source.getRecordcount());
		}
		// count( columnName ) returns count of non-null values
		else if (input instanceof Column || input instanceof Operation) {
			return Caster.toIntValue(executeAggregateExp(pc, sql, source, input, false, isDistinct).length);
		}
		else {
			// I'm not sure if this would ever get hit.
			throw new DatabaseException("count() function can only accept [*], a literal value, a column name, or an expression.", null, sql, null);
		}
	}

	private Object executeCoalesce(PageContext pc, SQL sql, Query source, Expression[] inputs, Integer row) throws PageException {
		boolean nullSupport = NullSupportHelper.full(pc);

		for (Expression thisOp: inputs) {
			Object thisValue = executeExp(pc, sql, source, thisOp, row);
			// If full null support is enabled, do actual null check
			if (nullSupport) {
				if (thisValue != null) {
					return thisValue;
				}
			}
			// If full null support is NOT enabled, check for empty string
			else {
				if (!Caster.toString(thisValue).equals("")) {
					return thisValue;
				}
			}

		}
		// Default value depends on full null support
		return (nullSupport ? null : "");
	}

	/*
	 * *
	 * 
	 * @param expression / private void print(ZExpression expression) {
	 * print.ln("Operator:"+expression.getOperator().toLowerCase()); int len=expression.nbOperands();
	 * for(int i=0;i<len;i++) { print.ln("	["+i+"]=	" +expression.getOperand(i)); } }/*
	 * 
	 * 
	 * 
	 * /**
	 * 
	 * execute an and operation
	 * 
	 * @param source QueryResult to execute on it
	 * 
	 * @param expression
	 * 
	 * @param row row of resultset to execute
	 * 
	 * @return
	 * 
	 * @throws PageException
	 */
	private Object executeAnd(PageContext pc, SQL sql, Query source, Operation2 expression, int row) throws PageException {
		// print.out("("+expression.getLeft().toString(true)+" AND
		// "+expression.getRight().toString(true)+")");
		boolean rtn = Caster.toBooleanValue(executeExp(pc, sql, source, expression.getLeft(), row));
		if (!rtn) return Boolean.FALSE;
		return Caster.toBoolean(executeExp(pc, sql, source, expression.getRight(), row));
	}

	private Object executeBracked(PageContext pc, SQL sql, Query source, BracketExpression expression, int row) throws PageException {
		return executeExp(pc, sql, source, expression.getExp(), row);
	}

	/**
	 * 
	 * execute an and operation
	 * 
	 * @param sql
	 * @param source QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executeOr(PageContext pc, SQL sql, Query source, Operation2 expression, int row) throws PageException {
		// print.out("("+expression.getLeft().toString(true)+" OR
		// "+expression.getRight().toString(true)+")");
		boolean rtn = Caster.toBooleanValue(executeExp(pc, sql, source, expression.getLeft(), row));
		if (rtn) return Boolean.TRUE;
		Boolean rtn2 = Caster.toBoolean(executeExp(pc, sql, source, expression.getRight(), row));

		// print.out(rtn+ " or "+rtn2);

		return rtn2;

	}

	private Object executeXor(PageContext pc, SQL sql, Query source, Operation2 expression, int row) throws PageException {
		return Caster.toBooleanValue(executeExp(pc, sql, source, expression.getLeft(), row)) ^ Caster.toBooleanValue(executeExp(pc, sql, source, expression.getRight(), row))
				? Boolean.TRUE
				: Boolean.FALSE;
	}

	/**
	 * 
	 * execute an equal operation
	 * 
	 * @param sql
	 * @param source QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executeEQ(PageContext pc, SQL sql, Query source, Operation2 expression, int row) throws PageException {
		return (executeCompare(pc, sql, source, expression, row) == 0) ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * 
	 * execute a not equal operation
	 * 
	 * @param sql
	 * @param source QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executeNEQ(PageContext pc, SQL sql, Query source, Operation2 expression, int row) throws PageException {
		return (executeCompare(pc, sql, source, expression, row) != 0) ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * 
	 * execute a less than operation
	 * 
	 * @param sql
	 * @param source QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executeLT(PageContext pc, SQL sql, Query source, Operation2 expression, int row) throws PageException {
		return (executeCompare(pc, sql, source, expression, row) < 0) ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * 
	 * execute a less than or equal operation
	 * 
	 * @param sql
	 * @param source QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executeLTE(PageContext pc, SQL sql, Query source, Operation2 expression, int row) throws PageException {
		return (executeCompare(pc, sql, source, expression, row) <= 0) ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * 
	 * execute a greater than operation
	 * 
	 * @param sql
	 * @param source QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executeGT(PageContext pc, SQL sql, Query source, Operation2 expression, int row) throws PageException {
		return (executeCompare(pc, sql, source, expression, row) > 0) ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * 
	 * execute a greater than or equal operation
	 * 
	 * @param sql
	 * @param source QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executeGTE(PageContext pc, SQL sql, Query source, Operation2 expression, int row) throws PageException {
		return (executeCompare(pc, sql, source, expression, row) >= 0) ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * 
	 * execute an equal operation
	 * 
	 * @param sql
	 * @param source QueryResult to execute on it
	 * @param op
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private int executeCompare(PageContext pc, SQL sql, Query source, Operation2 op, int row) throws PageException {
		// print.e(op.getLeft().getClass().getName());
		return OpUtil.compare(pc, executeExp(pc, sql, source, op.getLeft(), row), executeExp(pc, sql, source, op.getRight(), row));
	}

	private Object executeMod(PageContext pc, SQL sql, Query source, Operation2 expression, int row) throws PageException {

		return Caster.toDouble(
				Caster.toDoubleValue(executeExp(pc, sql, source, expression.getLeft(), row)) % Caster.toDoubleValue(executeExp(pc, sql, source, expression.getRight(), row)));
	}

	/**
	 * 
	 * execute a greater than or equal operation
	 * 
	 * @param sql
	 * @param source QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Boolean executeIn(PageContext pc, SQL sql, Query source, OperationN expression, int row, boolean isNot) throws PageException {
		Expression[] operators = expression.getOperants();
		Object left = executeExp(pc, sql, source, operators[0], row);

		for (int i = 1; i < operators.length; i++) {
			if (OpUtil.compare(pc, left, executeExp(pc, sql, source, operators[i], row)) == 0) return isNot ? Boolean.FALSE : Boolean.TRUE;
		}
		return isNot ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * 
	 * execute a minus operation
	 * 
	 * @param sql
	 * @param source QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executeMinus(PageContext pc, SQL sql, Query source, Operation2 expression, int row) throws PageException {
		return new Double(
				Caster.toDoubleValue(executeExp(pc, sql, source, expression.getLeft(), row)) - Caster.toDoubleValue(executeExp(pc, sql, source, expression.getRight(), row)));
	}

	/**
	 * 
	 * execute a divide operation
	 * 
	 * @param sql
	 * @param source QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executeDivide(PageContext pc, SQL sql, Query source, Operation2 expression, int row) throws PageException {
		return new Double(
				Caster.toDoubleValue(executeExp(pc, sql, source, expression.getLeft(), row)) / Caster.toDoubleValue(executeExp(pc, sql, source, expression.getRight(), row)));
	}

	/**
	 * 
	 * execute a multiply operation
	 * 
	 * @param sql
	 * @param source QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executeMultiply(PageContext pc, SQL sql, Query source, Operation2 expression, int row) throws PageException {
		return new Double(
				Caster.toDoubleValue(executeExp(pc, sql, source, expression.getLeft(), row)) * Caster.toDoubleValue(executeExp(pc, sql, source, expression.getRight(), row)));
	}

	/**
	 * 
	 * execute a multiply operation
	 * 
	 * @param sql
	 * @param source QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executeExponent(PageContext pc, SQL sql, Query source, Operation2 expression, int row) throws PageException {
		return Integer
				.valueOf(Caster.toIntValue(executeExp(pc, sql, source, expression.getLeft(), row)) ^ Caster.toIntValue(executeExp(pc, sql, source, expression.getRight(), row)));
	}

	/**
	 * 
	 * execute a plus operation
	 * 
	 * @param sql
	 * @param source QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executePlus(PageContext pc, SQL sql, Query source, Operation2 expression, int row) throws PageException {
		Object left = executeExp(pc, sql, source, expression.getLeft(), row);
		Object right = executeExp(pc, sql, source, expression.getRight(), row);

		try {
			return new Double(Caster.toDoubleValue(left) + Caster.toDoubleValue(right));
		}
		catch (PageException e) {
			return Caster.toString(left) + Caster.toString(right);
		}
	}

	/**
	 * 
	 * execute a between operation
	 * 
	 * @param sql
	 * @param source QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executeBetween(PageContext pc, SQL sql, Query source, Operation3 expression, int row) throws PageException {
		Object left = executeExp(pc, sql, source, expression.getExp(), row);
		Object right1 = executeExp(pc, sql, source, expression.getLeft(), row);
		Object right2 = executeExp(pc, sql, source, expression.getRight(), row);
		// print.out(left+" between "+right1+" and "+right2
		// +" = "+((Operator.compare(left,right1)>=0)+" && "+(Operator.compare(left,right2)<=0)));

		return ((OpUtil.compare(pc, left, right1) >= 0) && (OpUtil.compare(pc, left, right2) <= 0)) ? Boolean.TRUE : Boolean.FALSE;
	}

	private Object executeLike(PageContext pc, SQL sql, Query source, Operation3 expression, int row) throws PageException {
		return LikeCompare.like(sql, Caster.toString(executeExp(pc, sql, source, expression.getExp(), row)),
				Caster.toString(executeExp(pc, sql, source, expression.getLeft(), row)), Caster.toString(executeExp(pc, sql, source, expression.getRight(), row))) ? Boolean.TRUE
						: Boolean.FALSE;
	}

	private boolean executeLike(PageContext pc, SQL sql, Query source, Operation2 expression, int row) throws PageException {
		return LikeCompare.like(sql, Caster.toString(executeExp(pc, sql, source, expression.getLeft(), row)),
				Caster.toString(executeExp(pc, sql, source, expression.getRight(), row)));
	}

	/**
	 * Executes a constant value
	 * 
	 * @param sql
	 * @param source
	 * @param column
	 * @param row
	 * @return result
	 * @throws PageException
	 */
	private Object executeColumn(PageContext pc, SQL sql, Query source, Column column, int row) throws PageException {
		if (column.isParam()) {
			int pos = column.getColumnIndex();
			if (sql.getItems().length <= pos) throw new DatabaseException("invalid syntax for SQL Statement", null, sql, null);
			// If null=true is used with query param
			if (sql.getItems()[pos].isNulls()) return null;
			return sql.getItems()[pos].getValueForCF();
		}
		return column.getValue(pc, source, row);
		// return source.getAt(column.getColumn(),row);
	}

	private Object executeColumn(PageContext pc, SQL sql, Query source, Column column, int row, Object defaultValue) throws PageException {
		if (column.isParam()) {
			int pos = column.getColumnIndex();
			if (sql.getItems().length <= pos) throw new DatabaseException("invalid syntax for SQL Statement", null, sql, null);
			// If null=true is used with query param
			if (sql.getItems()[pos].isNulls()) return null;
			return sql.getItems()[pos].getValueForCF();
		}
		return column.getValue(pc, source, row, defaultValue);
	}
}