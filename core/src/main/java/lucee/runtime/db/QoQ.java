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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.IntStream;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.Random;

import lucee.runtime.util.DBUtilImpl;
import lucee.runtime.tag.util.QueryParamConverter.NamedSQLItem;
import lucee.commons.lang.CFTypes;
import lucee.commons.lang.StringUtil;
import lucee.commons.math.MathUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.IllegalQoQException;
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
import lucee.runtime.functions.other.Dump;
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
import lucee.runtime.type.comparator.QueryComparator;
import lucee.commons.io.SystemUtil;

/**
 *
 */
public final class QoQ {
	final static private Collection.Key paramKey = new KeyImpl("?");
	private static int qoqParallelism;

	static {
		qoqParallelism = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("lucee.qoq.parallelism", "50"), 50);
	}

	/**
	 * Execute a QofQ against a SQL object
	 *
	 * @param pc
	 * @param sql
	 * @param maxrows
	 * @return
	 * @throws PageException
	 */
	public QueryImpl execute(PageContext pc, SQL sql, int maxrows) throws PageException {
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
	public QueryImpl execute(PageContext pc, SQL sql, Selects selects, int maxrows) throws PageException {
		Select[] arrSelects = selects.getSelects();
		boolean isUnion = (arrSelects.length > 1);

		QueryImpl target = new QueryImpl(new Collection.Key[0], 0, "query", sql);

		// For each select (more than one when using union)
		for (int i = 0; i < arrSelects.length; i++) {
			arrSelects[i].getFroms();
			Column[] froms = arrSelects[i].getFroms();

			if (froms.length > 1) throw new DatabaseException("QoQ can only select from a single tables at a time.", null, sql, null);

			// Lookup actual Query variable on page
			QueryImpl source = getSingleTable(pc, froms[0]);
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
			order(pc, target, orders, isUnion, sql);
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
	private static void order(PageContext pc, QueryImpl target, Expression[] columns, boolean isUnion, SQL sql) throws PageException {
		Expression col;
		// Build up a int[] that represents where each row needs to be in the final query
		int[] sortedIndexes = getStream(target).boxed().sorted(new QueryComparator(pc, target, columns, isUnion, sql)).mapToInt(i -> ((Integer) i).intValue()).toArray();

		// Move the data around to match
		target.sort(sortedIndexes);

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
	private QueryImpl executeSingle(PageContext pc, Select select, QueryImpl source, QueryImpl previous, int maxrows, SQL sql, boolean hasOrders, boolean isUnion)
			throws PageException {

		// Our records will be placed here to return
		QueryImpl target = new QueryImpl(new Collection.Key[0], 0, "query", sql);

		// Make max rows the smaller of the two
		ValueNumber oTop = select.getTop();
		int top = -1;
		if (oTop != null) {
			top = (int) oTop.getValueAsDouble();
			if (maxrows == -1 || maxrows > top) maxrows = top;
		}

		Expression[] expSelects = select.getSelects();
		int selCount = expSelects.length;

		Collection.Key[] headers = new Collection.Key[selCount];
		QueryColumnImpl[] trgColumns = new QueryColumnImpl[selCount];
		Object[] trgValues = new Object[selCount];
		// Build up the final columns we need in our target query
		for (int i = 0; i < selCount; i++) {
			Expression expSelect = expSelects[i];
			Key alias = Caster.toKey(expSelect.getAlias());
			headers[i] = alias;
			trgValues[i] = expSelect;
			int type = Types.OTHER;
			if (expSelect instanceof ColumnExpression) {
				ColumnExpression ce = (ColumnExpression) expSelect;
				// A query param being selected back out uses the type other. We should probably use
				// the query param type, but we don't actually know what param we'll bind to at this
				// point.
				if (!ce.isParam()) type = source.getColumn(Caster.toKey(ce.getColumnName())).getType();
			}
			queryAddColumn(target, alias, type);
			trgColumns[i] = (QueryColumnImpl) target.getColumn(alias);
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
	private QueryImpl doUnionAll(QueryImpl previous, QueryImpl target, SQL sql) throws PageException {
		// If this is the first select in a series of unions, just return it directly. It's column
		// names now get set in stone as the column names the next union(s) will use!
		if (previous.getRecordcount() == 0) {
			return target;
		}
		Collection.Key[] previousColKeys = previous.getColumnNames();
		Collection.Key[] targetColKeys = target.getColumnNames();

		if (previousColKeys.length != targetColKeys.length) {
			throw new IllegalQoQException("Cannot perform union as number of columns in selects do not match.", null, sql, null);
		}

		// Queries being joined need to have the same number of columns and the data is fully
		// realized, so just copy it over positionally. The column names may not match, but that's
		// fine.
		getStream(target).forEach(throwingIntConsumer(row -> {
			int newRow = previous.addRow();
			for (int col = 0; col < targetColKeys.length; col++) {
				previous.setAt(previousColKeys[col], newRow, target.getColumn(targetColKeys[col]).get(row, null), true);
			}
		}));

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
	private QueryImpl doUnionDistinct(PageContext pc, QueryImpl previous, QueryImpl target, SQL sql) throws PageException {
		Collection.Key[] previousColKeys = previous.getColumnNames();
		Collection.Key[] targetColKeys = target.getColumnNames();

		if (previousColKeys.length != targetColKeys.length) {
			throw new IllegalQoQException("Cannot perform union as number of columns in selects do not match.", null, sql, null);
		}

		Expression[] selectExpressions = new Expression[previousColKeys.length];
		// We want the exact columns from the previous query, but not necessarily all the data. Make
		// a new target and copy the columns
		QueryImpl newTarget = new QueryImpl(new Collection.Key[0], 0, "query", sql);
		for (int col = 0; col < previousColKeys.length; col++) {
			newTarget.addColumn(previousColKeys[col], new ArrayImpl(), previous.getColumn(previousColKeys[col]).getType());
			// While we're looping, build up a handy array of expressions from the previous query.
			selectExpressions[col] = new ColumnExpression(previousColKeys[col].getString(), 0);
		}

		// Initialize our object to track the partitions
		QueryPartitions queryPartitions = new QueryPartitions(sql, selectExpressions, new Expression[0], newTarget, new HashSet<String>(), this);

		// Add in all the rows from our previous work
		getStream(previous).forEach(throwingIntConsumer(row -> {
			queryPartitions.addRow(pc, previous, row, true);
		}));

		// ...and all of the new rows
		getStream(target).forEach(throwingIntConsumer(row -> {
			queryPartitions.addRow(pc, target, row, true);
		}));

		// Loop over the partitions and take one from each and add to our new target question for a
		// distinct result
		getStream(queryPartitions).forEach(throwingConsumer(sourcePartition -> {
			int newRow = newTarget.addRow();

			for (int col = 0; col < targetColKeys.length; col++) {
				newTarget.setAt(previousColKeys[col], newRow, sourcePartition.getValue().getColumn(previousColKeys[col]).get(1, null), true);
			}
		}));

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
	private void executeSingleNonPartitioned(PageContext pc, Select select, QueryImpl source, QueryImpl target, int maxrows, SQL sql, boolean hasOrders, boolean isUnion,
			QueryColumnImpl[] trgColumns, Object[] trgValues, Collection.Key[] headers) throws PageException {
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
			target.addRow();
			for (int cell = 0; cell < headers.length; cell++) {
				trgColumns[cell].set(1, getValue(pc, sql, source, 1, headers[cell], trgValues[cell], null), true);
			}
			return;
		}

		IntStream stream = getStream(source);
		if (where != null) {
			stream = stream.filter(throwingFilter(row -> {
				// The where clause is a single Operation expression that returns true or false.
				return Caster.toBooleanValue(executeExp(pc, sql, source, where, row));
			}));
		}

		// If this was a non-grouped select with only aggregates like select "count(1) from
		// table" than bail after a single row
		if (hasAggregateSelect) {
			stream = stream.limit(1);
			// If we can, optimize the max rows exit strategy
			// This won't fire if there is an ORDER BY since we can't limit the rows until we sort (later)
		}
		else if (hasMaxrow) {
			stream = stream.limit(maxrows);
		}

		stream.forEach(throwingIntConsumer(row -> {
			int newRow = target.addRow();
			for (int cell = 0; cell < headers.length; cell++) {
				trgColumns[cell].set(newRow, getValue(pc, sql, source, row, headers[cell], trgValues[cell], null), true);
			}
		}));
	}

	public static IntStream getStream(QueryImpl qry) {
		if (qry.getRecordcount() > 0) {
			IntStream qStream = IntStream.range(1, qry.getRecordcount() + 1);
			if (qry.getRecordcount() >= qoqParallelism) {
				return qStream.parallel();
			}
			return qStream;
		}
		else {
			return IntStream.empty();
		}
	}

	public static Stream<Map.Entry<String, QueryImpl>> getStream(QueryPartitions queryPartitions) {
		if (queryPartitions.getPartitions().size() > 0) {
			Stream<Map.Entry<String, QueryImpl>> qStream = queryPartitions.getPartitions().entrySet().stream();
			if (queryPartitions.getPartitions().size() >= qoqParallelism) {
				qStream = qStream.parallel();
			}
			return qStream;
		}
		else {
			return Stream.empty();
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
	private void executeSinglePartitioned(PageContext pc, Select select, QueryImpl source, QueryImpl target, int maxrows, SQL sql, boolean hasOrders, boolean isUnion,
			QueryColumnImpl[] trgColumns, Object[] trgValues, Collection.Key[] headers) throws PageException {

		// Is there at least on aggregate expression in the select list
		boolean hasAggregateSelect = select.hasAggregateSelect();

		// For a non-grouping query with aggregates in the select such as
		// SELECT count(1) FROM qry WHERE col='foo'
		// then we need to return a single row
		if (hasAggregateSelect && select.getGroupbys().length == 0 && source.getRecordcount() == 0) {
			int newRow = target.addRow();
			for (int cell = 0; cell < headers.length; cell++) {
				trgColumns[cell].set(1, getValue(pc, sql, source, 1, headers[cell], trgValues[cell], null), true);
			}
			return;
		}

		Operation where = select.getWhere();
		// Initialize object to track our partitioned data
		QueryPartitions queryPartitions = new QueryPartitions(sql, select.getSelects(), select.getGroupbys(), target, select.getAdditionalColumns(), this);

		IntStream stream = getStream(source);
		if (where != null) {
			stream = stream.filter(throwingFilter(row -> {
				// The where clause is a single Operation expression that returns true or false.
				return Caster.toBooleanValue(executeExp(pc, sql, source, where, row));
			}));
		}

		stream.forEach(throwingIntConsumer(row -> {
			// ... add this row to our partitioned data
			queryPartitions.addRow(pc, source, row, false);
		}));

		// For a non-grouping query with aggregates where no records matched the where clause
		// SELECT count(1) FROM qry WHERE 1=0
		// then we need to add a single empty partition so our final select will have a single row.
		if (hasAggregateSelect && select.getGroupbys().length == 0 && queryPartitions.getPartitions().size() == 0) {
			queryPartitions.addEmptyPartition(source, target);
		}

		// Now that all rows are partitioned, eliminate partitions we don't need via the having
		// clause
		if (select.getHaving() != null) {

			// Loop over the partitions and take one from each and add to our new target question for a
			// distinct result
			getStream(queryPartitions).forEach(throwingConsumer(sourcePartition -> {
				// Eval the having clause on it
				if (!Caster.toBooleanValue(executeExp(pc, sql, sourcePartition.getValue(), select.getHaving(), 1))) {
					// Voted off the island :/
					queryPartitions.getPartitions().remove(sourcePartition.getKey());
				}
			}));

		}

		// Turn off query caching for our column references
		// Sharing columnExpressions across different query objects will have issues
		for (int cell = 0; cell < headers.length; cell++) {
			if (trgValues[cell] instanceof Expression) {
				((Expression) trgValues[cell]).setCacheColumn(false);
			}
		}

		getStream(queryPartitions).forEach(throwingConsumer(sourcePartition -> {
			int newRow = target.addRow();
			for (int cell = 0; cell < headers.length; cell++) {

				// If this is a column
				if (trgValues[cell] instanceof ColumnExpression) {
					ColumnExpression ce = (ColumnExpression) trgValues[cell];
					if (ce.getColumn().equals(paramKey)) {
						target.setAt(headers[cell], newRow, getValue(pc, sql, sourcePartition.getValue(), 1, null, trgValues[cell], null), true);
					}
					else {
						// Then make sure to use the alias now to reference it since it changed
						// names after going into the partition
						target.setAt(headers[cell], newRow, getValue(pc, sql, sourcePartition.getValue(), 1, ce.getColumnAlias(), null, null), true);
					}
				}
				// For Operations, just execute them normally
				else {
					target.setAt(headers[cell], newRow, getValue(pc, sql, sourcePartition.getValue(), 1, null, trgValues[cell], null), true);
				}
			}
		}));

	}

	/**
	 * Helper for adding a column to a query
	 *
	 * @param query
	 * @param column
	 * @param type
	 * @throws PageException
	 */
	private void queryAddColumn(QueryImpl query, Collection.Key column, int type) throws PageException {
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
	public Object getValue(PageContext pc, SQL sql, QueryImpl querySource, int row, Collection.Key key, Object value) throws PageException {
		if (value instanceof Expression) return executeExp(pc, sql, querySource, ((Expression) value), row);
		return querySource.getColumn(key).get(row, null);
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
	public Object getValue(PageContext pc, SQL sql, QueryImpl querySource, int row, Collection.Key key, Object value, Object defaultValue) throws PageException {
		if (value instanceof Expression) return executeExp(pc, sql, querySource, ((Expression) value), row, defaultValue);
		return querySource.getColumn(key).get(row, null);
	}

	/**
	 * @param pc Page Context of the Request
	 * @param table ZQLQuery
	 * @return Query
	 * @throws PageException
	 */
	private QueryImpl getSingleTable(PageContext pc, Column table) throws PageException {
		return (QueryImpl)Caster.toQuery(pc.getVariable(StringUtil.removeQuotes(table.getFullName(), true)));
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
	private Object executeExp(PageContext pc, SQL sql, QueryImpl source, Expression exp, int row) throws PageException {
		if (exp instanceof Value) return ((Value) exp).getValue();
		if (exp instanceof Column) return executeColumn(pc, sql, source, (Column) exp, row);
		if (exp instanceof Operation) return executeOperation(pc, sql, source, (Operation) exp, row);
		if (exp instanceof BracketExpression) return executeBracked(pc, sql, source, (BracketExpression) exp, row);
		throw new DatabaseException("unsupported sql statement [" + exp + "]", null, sql, null);
	}

	private Object executeExp(PageContext pc, SQL sql, QueryImpl source, Expression exp, int row, Object columnDefault) throws PageException {
		if (exp instanceof Value) return ((Value) exp).getValue();
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
	private Object[] executeAggregateExp(PageContext pc, SQL sql, QueryImpl source, Expression exp, boolean includeNull, boolean returnDistinct) throws PageException {
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

	private Object executeOperation(PageContext pc, SQL sql, QueryImpl source, Operation operation, int row) throws PageException {

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
			case Operation.OPERATION2_BITWISE:
				return executeBitwise(pc, sql, source, op2, row);
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
				if (op.equals("abs")) return Double.valueOf(MathUtil.abs(Caster.toDoubleValue(value)));
				if (op.equals("acos")) return Double.valueOf(Math.acos(Caster.toDoubleValue(value)));
				if (op.equals("asin")) return Double.valueOf(Math.asin(Caster.toDoubleValue(value)));
				if (op.equals("atan")) return Double.valueOf(Math.atan(Caster.toDoubleValue(value)));
				if (op.equals("avg")) {
					// If there are no non-null values, return empty
					if (aggregateValues.length == 0) {
						return null;
					}
					return ArrayUtil.avg(Caster.toArray(aggregateValues));
				}
				break;
			case 'c':
				if (op.equals("ceiling")) return Double.valueOf(Math.ceil(Caster.toDoubleValue(value)));
				if (op.equals("cos")) return Double.valueOf(Math.cos(Caster.toDoubleValue(value)));
				if (op.equals("count")) return executeCount(pc, sql, source, operators);
				if (op.equals("cast")) {
					// Cast is a single operand operator, but it gets the type from the alias of the single operand
					// i.e. cast( col1 as date )
					// If there is no alias, throw an exception.
					if (!operators[0].hasAlias()) {
						throw new IllegalQoQException("No type provided to cast to. [" + opn.toString(true) + "] ", null, sql, null);
					}
					return executeCast(pc, value, Caster.toString(operators[0].getAlias()));
				}
				if (op.equals("coalesce")) return executeCoalesce(pc, sql, source, operators, row);
				break;
			case 'e':
				if (op.equals("exp")) return Double.valueOf(Math.exp(Caster.toDoubleValue(value)));
				break;
			case 'f':
				if (op.equals("floor")) return Double.valueOf(Math.floor(Caster.toDoubleValue(value)));
				break;
			case 'u':
				if (op.equals("upper") || op.equals("ucase")) return Caster.toString(value).toUpperCase();
				break;

			case 'l':
				if (op.equals("lower") || op.equals("lcase")) return Caster.toString(value).toLowerCase();
				if (op.equals("ltrim")) return StringUtil.ltrim(Caster.toString(value), null);
				if (op.equals("length")) return Double.valueOf(Caster.toString(value).length());
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
					Comparator comp = ArrayUtil.toComparator(pc, sortType, sortDir, false);
					// Sort the array with proper type and direction
					colData.sortIt(comp);

					// If there are no non-null values, return empty
					if (colData.size() == 0) {
						return null;
					}
					// The first item in the array is our "max" or "min"
					return colData.getE(1);
				}
				break;
			case 'r':
				if (op.equals("rtrim")) return StringUtil.rtrim(Caster.toString(value), null);
				if (op.equals("rand")) return executeRand(pc, value, sql, operation);
				break;
			case 's':
				if (op.equals("sign")) return Double.valueOf(MathUtil.sgn(Caster.toDoubleValue(value)));
				if (op.equals("sin")) return Double.valueOf(Math.sin(Caster.toDoubleValue(value)));
				if (op.equals("soundex")) return StringUtil.soundex(Caster.toString(value));
				if (op.equals("sin")) return Double.valueOf(Math.sqrt(Caster.toDoubleValue(value)));
				if (op.equals("sum")) {
					// If there are no non-null values, return empty
					if (aggregateValues.length == 0) {
						return null;
					}
					return ArrayUtil.sum(Caster.toArray(aggregateValues));
				}
				break;
			case 't':
				if (op.equals("tan")) return Double.valueOf(Math.tan(Caster.toDoubleValue(value)));
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
				if (op.equals("atan2")) return Double.valueOf(Math.atan2(Caster.toDoubleValue(left), Caster.toDoubleValue(right)));
				break;
			case 'b':
				if (op.equals("bitand")) return OpUtil.bitand(pc, Caster.toDoubleValue(left), Caster.toDoubleValue(right));
				if (op.equals("bitor")) return OpUtil.bitor(pc, Caster.toDoubleValue(left), Caster.toDoubleValue(right));
				break;
			case 'c':
				if (op.equals("concat")) return Caster.toString(left).concat(Caster.toString(right));
				if (op.equals("count")) return executeCount(pc, sql, source, operators);
				if (op.equals("coalesce")) return executeCoalesce(pc, sql, source, operators, row);
				if (op.equals("convert")) {
					// If the user does convert( col1, 'string' ) it will be a ValueExpression and we can use it
					// directly;
					// If the user does convert( col1, string ) it will be a ColumnExpressin and we just want to use the
					// column name ("string" in this case).
					// convert() is the binary version of the unary operator cast()
					// i.e. convert( col1, string ) is the same as cast( col1 as string )
					if (operators[1] instanceof ColumnExpression) {
						right = ((ColumnExpression) operators[1]).getColumnName();
					}
					return executeCast(pc, left, Caster.toString(right));
				}
				break;
			case 'i':
				if (op.equals("isnull")) return executeCoalesce(pc, sql, source, operators, row);
				break;
			case 'm':
				if (op.equals("mod")) {
					// The result of any mathmatical operation involving a null is null
					if (left == null || right == null) {
						return null;
					}
					return Double.valueOf(castForMathDouble(left) % castForMathDouble(right));
				}
				break;
			case 'p':
				if (op.equals("power")) {
					// The result of any mathmatical operation involving a null is null
					if (left == null || right == null) {
						return null;
					}
					return Math.pow(castForMathDouble(left), castForMathDouble(right));
				}
				break;

			}

		}
		// 3333333333333333333333333333333333333333333333333333333333333333333

		if (op.equals("in")) return executeIn(pc, sql, source, opn, row, false);
		if (op.equals("not_in")) return executeIn(pc, sql, source, opn, row, true);

		if (op.equals("coalesce")) return executeCoalesce(pc, sql, source, operators, row);

		if (op.equals("count")) return executeCount(pc, sql, source, operators);

		if (op.equals("rand")) return executeRand(pc, null, sql, operation);

		throw new DatabaseException("unsupported sql statement (" + op + ") ", null, sql, null);

	}

	private Integer executeCount(PageContext pc, SQL sql, QueryImpl source, Expression[] inputs) throws PageException {
		boolean isDistinct = false;
		List<Expression> inputList = new ArrayList<Expression>(Arrays.asList(inputs));
		Expression first = inputList.get(0);
		if (inputList.size() > 1 && first instanceof Value && ((Value) first).getString().equals("all")) {
			inputList.remove(0);
		}
		else if (inputList.size() > 1 && first instanceof Value && ((Value) first).getString().equals("distinct")) {
			isDistinct = true;
			inputList.remove(0);
			// This would be count( DISTINCT col1, col2 )
			// HSQLDB doesn't support this either
			if (inputList.size() > 1) {
				throw new IllegalQoQException("count( DISTINCT ... ) doesn't support more than one expression at this time", null, sql, null);
			}

		}
		if (inputList.size() > 1) {
			// HSQLDB doesn't support this either
			throw new IllegalQoQException("count() only accepts one expression, but you provided " + inputList.size() + ".", null, sql, null);
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
			throw new IllegalQoQException("count() function can only accept [*], a literal value, a column name, or an expression.", null, sql, null);
		}
	}

	private Object executeCoalesce(PageContext pc, SQL sql, QueryImpl source, Expression[] inputs, Integer row) throws PageException {

		for (Expression thisOp: inputs) {
			Object thisValue = executeExp(pc, sql, source, thisOp, row, null);
			if (thisValue != null) {
				return thisValue;
			}
		}
		return null;
	}

	private Double executeRand(PageContext pc, Object seed, SQL sql, Operation operation) throws PageException {
		SQLImpl sqlImpl = (SQLImpl) sql;
		Random rand = sqlImpl.getRand();
		// rand() always returns a new random number unless a seed is used like rand(123), in which case
		// all subsequent calls to rand() will use that seed for the duration of this query
		if (seed != null) {
			try {
				rand.setSeed(Caster.toLong(seed));
			}
			catch (PageException e) {
				throw new IllegalQoQException("rand() seed cannot be cast to a Long.  Encountered while evaluating [" + operation.toString(true) + "]", null, sql, null);
			}
		}
		return rand.nextDouble();
	}

	private Object executeCast(PageContext pc, Object value, String type) throws PageException {
		return Caster.castTo(pc, CFTypes.toShort(type, true, CFTypes.TYPE_UNKNOW), type, value);
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
	private Object executeAnd(PageContext pc, SQL sql, QueryImpl source, Operation2 expression, int row) throws PageException {
		// print.out("("+expression.getLeft().toString(true)+" AND
		// "+expression.getRight().toString(true)+")");
		boolean rtn = Caster.toBooleanValue(executeExp(pc, sql, source, expression.getLeft(), row));
		if (!rtn) return Boolean.FALSE;
		return Caster.toBoolean(executeExp(pc, sql, source, expression.getRight(), row));
	}

	private Object executeBracked(PageContext pc, SQL sql, QueryImpl source, BracketExpression expression, int row) throws PageException {
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
	private Object executeOr(PageContext pc, SQL sql, QueryImpl source, Operation2 expression, int row) throws PageException {
		// print.out("("+expression.getLeft().toString(true)+" OR
		// "+expression.getRight().toString(true)+")");
		boolean rtn = Caster.toBooleanValue(executeExp(pc, sql, source, expression.getLeft(), row));
		if (rtn) return Boolean.TRUE;
		Boolean rtn2 = Caster.toBoolean(executeExp(pc, sql, source, expression.getRight(), row));

		// print.out(rtn+ " or "+rtn2);

		return rtn2;

	}

	private Object executeXor(PageContext pc, SQL sql, QueryImpl source, Operation2 expression, int row) throws PageException {
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
	private Object executeEQ(PageContext pc, SQL sql, QueryImpl source, Operation2 expression, int row) throws PageException {
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
	private Object executeNEQ(PageContext pc, SQL sql, QueryImpl source, Operation2 expression, int row) throws PageException {
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
	private Object executeLT(PageContext pc, SQL sql, QueryImpl source, Operation2 expression, int row) throws PageException {
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
	private Object executeLTE(PageContext pc, SQL sql, QueryImpl source, Operation2 expression, int row) throws PageException {
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
	private Object executeGT(PageContext pc, SQL sql, QueryImpl source, Operation2 expression, int row) throws PageException {
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
	private Object executeGTE(PageContext pc, SQL sql, QueryImpl source, Operation2 expression, int row) throws PageException {
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
	private int executeCompare(PageContext pc, SQL sql, QueryImpl source, Operation2 op, int row) throws PageException {
		// print.e(op.getLeft().getClass().getName());
		return OpUtil.compare(pc, executeExp(pc, sql, source, op.getLeft(), row), executeExp(pc, sql, source, op.getRight(), row));
	}

	private Object executeMod(PageContext pc, SQL sql, QueryImpl source, Operation2 expression, int row) throws PageException {
		Object left = executeExp(pc, sql, source, expression.getLeft(), row);
		Object right = executeExp(pc, sql, source, expression.getRight(), row);

		// The result of any mathmatical operation involving a null is null
		if (left == null || right == null) {
			return null;
		}

		Double rightDouble = castForMathDouble(right);
		if (rightDouble == 0) {
			throw new IllegalQoQException("Divide by zero not allowed.  Encountered while evaluating [" + expression.toString(true) + "] in row " + row, null, sql, null);
		}

		return Double.valueOf(castForMathDouble(left) % rightDouble);
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
	private Boolean executeIn(PageContext pc, SQL sql, QueryImpl source, OperationN expression, int row, boolean isNot) throws PageException {
		Expression[] operators = expression.getOperants();
		Object left = executeExp(pc, sql, source, operators[0], row);

		for (int i = 1; i < operators.length; i++) {
			if (OpUtil.compare(pc, left, executeExp(pc, sql, source, operators[i], row)) == 0) return isNot ? Boolean.FALSE : Boolean.TRUE;
		}
		return isNot ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * Cast value to Double, accounting for logic such as turning empty strings into zero.
	 *
	 * @param value Value for casting. Must be non-null
	 * @return Value cast to a Double
	 */
	private Double castForMathDouble(Object value) throws PageException {
		if (Caster.toString(value).equals("")) {
			return Double.valueOf(0);
		}
		return Caster.toDoubleValue(value);
	}

	/**
	 * Cast value to Int, accounting for logic such as turning empty strings into zero.
	 *
	 * @param value Value for casting. Must be non-null
	 * @return Value cast to a Int
	 */
	private Integer castForMathInt(Object value) throws PageException {
		if (Caster.toString(value).equals("")) {
			return Integer.valueOf(0);
		}
		return Caster.toIntValue(value);
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
	private Object executeMinus(PageContext pc, SQL sql, QueryImpl source, Operation2 expression, int row) throws PageException {
		Object left = executeExp(pc, sql, source, expression.getLeft(), row);
		Object right = executeExp(pc, sql, source, expression.getRight(), row);

		// The result of any mathmatical operation involving a null is null
		if (left == null || right == null) {
			return null;
		}

		return Double.valueOf(castForMathDouble(left) - castForMathDouble(right));
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
	private Object executeDivide(PageContext pc, SQL sql, QueryImpl source, Operation2 expression, int row) throws PageException {
		Object left = executeExp(pc, sql, source, expression.getLeft(), row);
		Object right = executeExp(pc, sql, source, expression.getRight(), row);

		// The result of any mathmatical operation involving a null is null
		if (left == null || right == null) {
			return null;
		}

		Double rightDouble = castForMathDouble(right);
		if (rightDouble == 0) {
			throw new IllegalQoQException("Divide by zero not allowed.  Encountered while evaluating [" + expression.toString(true) + "] in row " + row, null, sql, null);
		}

		return Double.valueOf(castForMathDouble(left) / rightDouble);
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
	private Object executeMultiply(PageContext pc, SQL sql, QueryImpl source, Operation2 expression, int row) throws PageException {
		Object left = executeExp(pc, sql, source, expression.getLeft(), row);
		Object right = executeExp(pc, sql, source, expression.getRight(), row);

		// The result of any mathmatical operation involving a null is null
		if (left == null || right == null) {
			return null;
		}

		return Double.valueOf(castForMathDouble(left) * castForMathDouble(right));
	}

	/**
	 *
	 * execute a bitwise operation
	 *
	 * @param sql
	 * @param source QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executeBitwise(PageContext pc, SQL sql, QueryImpl source, Operation2 expression, int row) throws PageException {
		Object left = executeExp(pc, sql, source, expression.getLeft(), row);
		Object right = executeExp(pc, sql, source, expression.getRight(), row);

		// The result of any mathmatical operation involving a null is null
		if (left == null || right == null) {
			return null;
		}

		return Integer.valueOf(castForMathInt(left) ^ castForMathInt(right));
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
	private Object executePlus(PageContext pc, SQL sql, QueryImpl source, Operation2 expression, int row) throws PageException {
		Object left = executeExp(pc, sql, source, expression.getLeft(), row);
		Object right = executeExp(pc, sql, source, expression.getRight(), row);
		boolean leftIsNumber = Decision.isNumber(left);
		boolean rightIsNumber = Decision.isNumber(right);

		// Short circuit to string concat if both are not numbers and one isn't a number and the other null.
		// Note, if both are null, we treat the operations as arethmatic and return null.
		// If at least one is a string, we concat turn any nulls to empty strings
		if ((!leftIsNumber || !rightIsNumber) && !(leftIsNumber && right == null) && !(rightIsNumber && left == null) && !(right == null && left == null)) {
			return Caster.toString(left) + Caster.toString(right);
		}

		try {
			Double dLeft = Caster.toDoubleValue(left);
			Double dRight = Caster.toDoubleValue(right);

			// The result of any mathmatical operation involving a null is null
			if (left == null || right == null) {
				return null;
			}

			return Double.valueOf(dLeft + dRight);
			// If casting fails, we assume the inputs are strings and concat instead
			// Unlike SQL, we're not going to return null for a null string concat
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
	private Object executeBetween(PageContext pc, SQL sql, QueryImpl source, Operation3 expression, int row) throws PageException {
		Object left = executeExp(pc, sql, source, expression.getExp(), row);
		Object right1 = executeExp(pc, sql, source, expression.getLeft(), row);
		Object right2 = executeExp(pc, sql, source, expression.getRight(), row);
		// print.out(left+" between "+right1+" and "+right2
		// +" = "+((Operator.compare(left,right1)>=0)+" && "+(Operator.compare(left,right2)<=0)));

		return ((OpUtil.compare(pc, left, right1) >= 0) && (OpUtil.compare(pc, left, right2) <= 0)) ? Boolean.TRUE : Boolean.FALSE;
	}

	private Object executeLike(PageContext pc, SQL sql, QueryImpl source, Operation3 expression, int row) throws PageException {
		return LikeCompare.like(sql, Caster.toString(executeExp(pc, sql, source, expression.getExp(), row)),
				Caster.toString(executeExp(pc, sql, source, expression.getLeft(), row)), Caster.toString(executeExp(pc, sql, source, expression.getRight(), row))) ? Boolean.TRUE
						: Boolean.FALSE;
	}

	private boolean executeLike(PageContext pc, SQL sql, QueryImpl source, Operation2 expression, int row) throws PageException {
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
	private Object executeColumn(PageContext pc, SQL sql, QueryImpl source, Column column, int row) throws PageException {
		return executeColumn(pc, sql, source, column, row, null);
	}

	private Object executeColumn(PageContext pc, SQL sql, QueryImpl source, Column column, int row, Object defaultValue) throws PageException {
		if (column.isParam()) {
			int pos = column.getColumnIndex();
			if (sql.getItems().length <= pos) throw new IllegalQoQException("Invalid SQL Statement. Not enough parameters provided.", null, sql, null);
			SQLItem param = sql.getItems()[pos];
			// If null=true is used with query param
			if (param.isNulls()) return null;
			try {
				return param.getValueForCF();
			}
			catch (PageException e) {
				// Create best error message based on whether param was defined as ? or :name
				if (param instanceof NamedSQLItem) {
					throw (IllegalQoQException) (new IllegalQoQException("Parameter [:" + ((NamedSQLItem) param).getName() + "] is invalid.", e.getMessage(), sql, null)
							.initCause(e));
				}
				else {
					throw (IllegalQoQException) (new IllegalQoQException(new DBUtilImpl().toStringType(param.getType()) + " parameter in position " + (pos + 1) + " is invalid.",
							e.getMessage(), sql, null).initCause(e));
				}
			}
		}
		return column.getValue(pc, source, row, defaultValue);
	}

	// Helpers for exceptions in Lambdas
	@FunctionalInterface
	public interface ThrowingIntConsumer {
		/**
		 * Applies this function to the given argument.
		 *
		 * @param t the Consumer argument
		 */
		void accept(int t) throws Exception;
	}

	public static IntConsumer throwingIntConsumer(ThrowingIntConsumer throwingIntConsumer) {
		return new IntConsumer() {
			@Override
			public void accept(int t) {
				try {
					throwingIntConsumer.accept(t);
				}
				catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		};
	}

	@FunctionalInterface
	public interface ThrowingConsumer {
		/**
		 * Applies this function to the given argument.
		 *
		 * @param t the Consumer argument
		 */
		void accept(Map.Entry<String, QueryImpl> t) throws Exception;
	}

	public static Consumer<Map.Entry<String, QueryImpl>> throwingConsumer(ThrowingConsumer throwingConsumer) {
		return new Consumer<Map.Entry<String, QueryImpl>>() {
			@Override
			public void accept(Map.Entry<String, QueryImpl> t) {
				try {
					throwingConsumer.accept(t);
				}
				catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		};
	}

	@FunctionalInterface
	public interface ThrowingFilter {
		/**
		 * Applies this function to the given argument.
		 *
		 * @param t the Consumer argument
		 */
		boolean test(int t) throws Exception;
	}

	public static IntPredicate throwingFilter(ThrowingFilter throwingFilter) {
		return new IntPredicate() {
			@Override
			public boolean test(int t) {
				try {
					return throwingFilter.test(t);
				}
				catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		};
	}

}