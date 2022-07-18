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

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import lucee.commons.collection.MapFactory;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.math.MathUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.OpUtil;
import lucee.runtime.sql.old.ZConstant;
import lucee.runtime.sql.old.ZExp;
import lucee.runtime.sql.old.ZExpression;
import lucee.runtime.sql.old.ZFromItem;
import lucee.runtime.sql.old.ZOrderBy;
import lucee.runtime.sql.old.ZQuery;
import lucee.runtime.sql.old.ZSelectItem;
import lucee.runtime.sql.old.ZqlParser;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryColumn;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.type.util.QueryUtil;

/**
 * 
 */
public final class Executer {

	/**
	 * execute a SQL Statement against CFML Scopes
	 * 
	 * @param pc PageContext of the Request
	 * @param sql
	 * @param maxrows
	 * @return result
	 * @throws PageException
	 */
	public QueryImpl execute(Vector statements, PageContext pc, SQL sql, int maxrows) throws PageException {
		// parse sql
		if (statements.size() != 1) throw new DatabaseException("only one SQL Statement allowed at time", null, null, null);
		ZQuery query = (ZQuery) statements.get(0);

		// single table
		if (query.getFrom().size() == 1) {
			return testExecute(pc, sql, getSingleTable(pc, query), query, maxrows);

		}
		// multiple table
		throw new DatabaseException("can only work with single tables yet", null, null, null);

	}

	public QueryImpl execute(PageContext pc, SQL sql, String prettySQL, int maxrows) throws PageException {
		if (StringUtil.isEmpty(prettySQL)) prettySQL = SQLPrettyfier.prettyfie(sql.getSQLString());

		ZqlParser parser = new ZqlParser(new ByteArrayInputStream(prettySQL.getBytes()));
		Vector statements;
		try {
			statements = parser.readStatements();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}
		return execute(statements, pc, sql, maxrows);

	}

	private QueryImpl testExecute(PageContext pc, SQL sql, Query qr, ZQuery query, int maxrows) throws PageException {

		int recCount = qr.getRecordcount();
		Vector vSelects = query.getSelect();
		int selCount = vSelects.size();

		Map<Collection.Key, Object> selects = MapFactory.<Collection.Key, Object>getConcurrentMap();
		boolean isSMS = false;
		// headers
		for (int i = 0; i < selCount; i++) {
			ZSelectItem select = (ZSelectItem) vSelects.get(i);

			if (select.isWildcard() || (isSMS = select.getColumn().equals(SQLPrettyfier.PLACEHOLDER_ASTERIX))) {

				if (!isSMS && !select.getColumn().equals("*")) throw new DatabaseException("can't execute this type of query at the moment", null, sql, null);
				// Collection.Key[] keys = qr.keys();
				Iterator<Key> it = qr.keyIterator();
				Key k;
				while (it.hasNext()) {
					k = it.next();
					selects.put(k, k.getString());
				}
				isSMS = false;
			}
			else {
				// if(SQLPrettyfier.PLACEHOLDER_COUNT.equals(select.getAlias())) select.setAlias("count");
				// if(SQLPrettyfier.PLACEHOLDER_COUNT.equals(select.getColumn())) select.setExpression(new
				// ZConstant("count",ZConstant.COLUMNNAME));

				String alias = select.getAlias();
				String column = select.getColumn();
				if (alias == null) alias = column;
				alias = alias.toLowerCase();

				selects.put(KeyImpl.init(alias), select);
			}
		}
		Key[] headers = selects.keySet().toArray(new Collection.Key[selects.size()]);

		// aHeaders.toArray(new String[aHeaders.size()]);
		QueryImpl rtn = new QueryImpl(headers, 0, "query", sql);

		// loop records
		Vector orders = query.getOrderBy();
		ZExp where = query.getWhere();
		// print.out(headers);
		// int newRecCount=0;
		boolean hasMaxrow = maxrows > -1 && (orders == null || orders.size() == 0);
		for (int row = 1; row <= recCount; row++) {
			sql.setPosition(0);
			if (hasMaxrow && maxrows <= rtn.getRecordcount()) break;
			boolean useRow = where == null || Caster.toBooleanValue(executeExp(pc, sql, qr, where, row));
			if (useRow) {

				rtn.addRow(1);
				for (int cell = 0; cell < headers.length; cell++) {
					Object value = selects.get(headers[cell]);

					rtn.setAt(headers[cell], rtn.getRecordcount(), getValue(pc, sql, qr, row, headers[cell], value));
				}
			}
		}

		// Group By
		if (query.getGroupBy() != null) throw new DatabaseException("group by are not supported at the moment", null, sql, null);

		// Order By
		if (orders != null && orders.size() > 0) {

			int len = orders.size();
			for (int i = len - 1; i >= 0; i--) {
				ZOrderBy order = (ZOrderBy) orders.get(i);
				ZConstant name = (ZConstant) order.getExpression();
				rtn.sort(name.getValue().toLowerCase(), order.getAscOrder() ? Query.ORDER_ASC : Query.ORDER_DESC);
			}
			if (maxrows > -1) {
				rtn.cutRowsTo(maxrows);
			}
		}
		// Distinct
		if (query.isDistinct()) {
			String[] keys = rtn.getColumns();
			QueryColumn[] columns = new QueryColumn[keys.length];
			for (int i = 0; i < columns.length; i++) {
				columns[i] = rtn.getColumn(keys[i]);
			}

			int i;
			outer: for (int row = rtn.getRecordcount(); row > 1; row--) {
				for (i = 0; i < columns.length; i++) {
					if (!OpUtil.equals(pc, QueryUtil.getValue(columns[i], row), QueryUtil.getValue(columns[i], row - 1), true)) continue outer;
				}
				rtn.removeRow(row);
			}

		}
		// UNION // TODO support it
		ZExpression set = query.getSet();
		if (set != null) {
			ZExp op = set.getOperand(0);
			if (op instanceof ZQuery) throw new DatabaseException("union is not supported at the moment", null, sql, null);
			// getInvokedTables((ZQuery)op, tablesNames);
		}

		return rtn;
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
	private Object getValue(PageContext pc, SQL sql, Query querySource, int row, Collection.Key key, Object value) throws PageException {
		if (value instanceof ZSelectItem) return executeExp(pc, sql, querySource, ((ZSelectItem) value).getExpression(), row);
		return querySource.getAt(key, row);
	}

	/**
	 * @param pc Page Context of the Request
	 * @param query ZQLQuery
	 * @return Query
	 * @throws PageException
	 */
	private Query getSingleTable(PageContext pc, ZQuery query) throws PageException {
		return Caster.toQuery(pc.getVariable(((ZFromItem) query.getFrom().get(0)).getFullName()));
	}

	/**
	 * Executes a ZEXp
	 * 
	 * @param sql
	 * @param qr Query Result
	 * @param exp expression to execute
	 * @param row current row of resultset
	 * @return result
	 * @throws PageException
	 */
	private Object executeExp(PageContext pc, SQL sql, Query qr, ZExp exp, int row) throws PageException {
		if (exp instanceof ZConstant) return executeConstant(sql, qr, (ZConstant) exp, row);
		else if (exp instanceof ZExpression) return executeExpression(pc, sql, qr, (ZExpression) exp, row);
		throw new DatabaseException("unsupported sql statement [" + exp + "]", null, sql, null);

	}

	/**
	 * Executes an Expression
	 * 
	 * @param sql
	 * @param qr
	 * @param expression
	 * @param row
	 * @return result
	 * @throws PageException
	 */
	private Object executeExpression(PageContext pc, SQL sql, Query qr, ZExpression expression, int row) throws PageException {
		String op = StringUtil.toLowerCase(expression.getOperator());
		int count = expression.nbOperands();

		if (op.equals("and")) return executeAnd(pc, sql, qr, expression, row);
		else if (op.equals("or")) return executeOr(pc, sql, qr, expression, row);
		if (count == 0 && op.equals("?")) {
			int pos = sql.getPosition();
			if (sql.getItems().length <= pos) throw new DatabaseException("invalid syntax for SQL Statement", null, sql, null);
			sql.setPosition(pos + 1);
			return sql.getItems()[pos].getValueForCF();
		}
		// 11111111111111111111111111111111111111111111111111111
		else if (count == 1) {
			Object value = executeExp(pc, sql, qr, expression.getOperand(0), row);

			// Functions
			switch (op.charAt(0)) {
			case 'a':
				if (op.equals("abs")) return new Double(MathUtil.abs(Caster.toDoubleValue(value)));
				if (op.equals("acos")) return new Double(Math.acos(Caster.toDoubleValue(value)));
				if (op.equals("asin")) return new Double(Math.asin(Caster.toDoubleValue(value)));
				if (op.equals("atan")) return new Double(Math.atan(Caster.toDoubleValue(value)));
				break;
			case 'c':
				if (op.equals("ceiling")) return new Double(Math.ceil(Caster.toDoubleValue(value)));
				if (op.equals("cos")) return new Double(Math.cos(Caster.toDoubleValue(value)));
				break;
			case 'e':
				if (op.equals("exp")) return new Double(Math.exp(Caster.toDoubleValue(value)));
				break;
			case 'f':
				if (op.equals("floor")) return new Double(Math.floor(Caster.toDoubleValue(value)));
				break;
			case 'i':
				if (op.equals("is not null")) return Boolean.valueOf(value != null);
				if (op.equals("is null")) return Boolean.valueOf(value == null);
				break;
			case 'u':
				if (op.equals("upper") || op.equals("ucase")) return Caster.toString(value).toUpperCase();
				break;

			case 'l':
				if (op.equals("lower") || op.equals("lcase")) return Caster.toString(value).toLowerCase();
				if (op.equals("ltrim")) return StringUtil.ltrim(Caster.toString(value), null);
				if (op.equals("length")) return new Double(Caster.toString(value).length());
				break;
			case 'r':
				if (op.equals("rtrim")) return StringUtil.rtrim(Caster.toString(value), null);
				break;
			case 's':
				if (op.equals("sign")) return new Double(MathUtil.sgn(Caster.toDoubleValue(value)));
				if (op.equals("sin")) return new Double(Math.sin(Caster.toDoubleValue(value)));
				if (op.equals("soundex")) return StringUtil.soundex(Caster.toString(value));
				if (op.equals("sin")) return new Double(Math.sqrt(Caster.toDoubleValue(value)));
				break;
			case 't':
				if (op.equals("tan")) return new Double(Math.tan(Caster.toDoubleValue(value)));
				if (op.equals("trim")) return Caster.toString(value).trim();
				break;
			}

		}

		// 22222222222222222222222222222222222222222222222222222
		else if (count == 2) {

			if (op.equals("=") || op.equals("in")) return executeEQ(pc, sql, qr, expression, row);
			else if (op.equals("!=") || op.equals("<>")) return executeNEQ(pc, sql, qr, expression, row);
			else if (op.equals("<")) return executeLT(pc, sql, qr, expression, row);
			else if (op.equals("<=")) return executeLTE(pc, sql, qr, expression, row);
			else if (op.equals(">")) return executeGT(pc, sql, qr, expression, row);
			else if (op.equals(">=")) return executeGTE(pc, sql, qr, expression, row);
			else if (op.equals("-")) return executeMinus(pc, sql, qr, expression, row);
			else if (op.equals("+")) return executePlus(pc, sql, qr, expression, row);
			else if (op.equals("/")) return executeDivide(pc, sql, qr, expression, row);
			else if (op.equals("*")) return executeMultiply(pc, sql, qr, expression, row);
			else if (op.equals("^")) return executeExponent(pc, sql, qr, expression, row);

			Object left = executeExp(pc, sql, qr, expression.getOperand(0), row);
			Object right = executeExp(pc, sql, qr, expression.getOperand(1), row);

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
				break;
			case 'l':
				if (op.equals("like")) return executeLike(pc, sql, qr, expression, row);
				break;
			case 'm':
				if (op.equals("mod")) return OpUtil.modulusRef(pc, Caster.toDoubleValue(left), Caster.toDoubleValue(right));
				break;
			}

			throw new DatabaseException("unsopprted sql statement [" + op + "]", null, sql, null);
		}
		// 3333333333333333333333333333333333333333333333333333333333333333333
		else if (count == 3) {
			if (op.equals("between")) return executeBetween(pc, sql, qr, expression, row);
		}

		if (op.equals("in")) return executeIn(pc, sql, qr, expression, row);

		/*
		 * 
		 * addCustomFunction("cot",1); addCustomFunction("degrees",1); addCustomFunction("log",1);
		 * addCustomFunction("log10",1);
		 * 
		 * addCustomFunction("pi",0); addCustomFunction("power",2); addCustomFunction("radians",1);
		 * addCustomFunction("rand",0); addCustomFunction("round",2); addCustomFunction("roundmagic",1);
		 * addCustomFunction("truncate",2); addCustomFunction("ascii",1); addCustomFunction("bit_length",1);
		 * addCustomFunction("char",1); addCustomFunction("char_length",1);
		 * addCustomFunction("difference",2); addCustomFunction("hextoraw",1);
		 * addCustomFunction("insert",4); addCustomFunction("left",2); addCustomFunction("locate",3);
		 * addCustomFunction("octet_length",1); addCustomFunction("rawtohex",1);
		 * addCustomFunction("repeat",2); addCustomFunction("replace",3); addCustomFunction("right",2);
		 * addCustomFunction("space",1); addCustomFunction("substr",3); addCustomFunction("substring",3);
		 * addCustomFunction("curdate",0); addCustomFunction("curtime",0); addCustomFunction("datediff",3);
		 * addCustomFunction("dayname",1); addCustomFunction("dayofmonth",1);
		 * addCustomFunction("dayofweek",1); addCustomFunction("dayofyear",1); addCustomFunction("hour",1);
		 * addCustomFunction("minute",1); addCustomFunction("month",1); addCustomFunction("monthname",1);
		 * addCustomFunction("now",0); addCustomFunction("quarter",1); addCustomFunction("second",1);
		 * addCustomFunction("week",1); addCustomFunction("year",1); addCustomFunction("current_date",1);
		 * addCustomFunction("current_time",1); addCustomFunction("current_timestamp",1);
		 * addCustomFunction("database",0); addCustomFunction("user",0);
		 * addCustomFunction("current_user",0); addCustomFunction("identity",0);
		 * addCustomFunction("ifnull",2); addCustomFunction("casewhen",3); addCustomFunction("convert",2);
		 * //addCustomFunction("cast",1); addCustomFunction("coalesce",1000); addCustomFunction("nullif",2);
		 * addCustomFunction("extract",1); addCustomFunction("position",1);
		 */

		// print(expression);
		throw new DatabaseException("unsopprted sql statement (op-count:" + expression.nbOperands() + ";operator:" + op + ") ", null, sql, null);

	}

	/*
	 * *
	 * 
	 * @param expression / private void print(ZExpression expression) {
	 * print.ln("Operator:"+expression.getOperator().toLowerCase()); int len=expression.nbOperands();
	 * for(int i=0;i<len;i++) { print.ln("	["+i+"]=	"+expression.getOperand(i)); } }/*
	 * 
	 * /**
	 * 
	 * execute an and operation
	 * 
	 * @param qr QueryResult to execute on it
	 * 
	 * @param expression
	 * 
	 * @param row row of resultset to execute
	 * 
	 * @return
	 * 
	 * @throws PageException
	 */
	private Object executeAnd(PageContext pc, SQL sql, Query qr, ZExpression expression, int row) throws PageException {
		int len = expression.nbOperands();

		// boolean rtn=Caster.toBooleanValue(executeExp(pc,sql,qr,expression.getOperand(0),row));
		for (int i = 0; i < len; i++) {
			// if(!rtn)break;
			// rtn=rtn && Caster.toBooleanValue(executeExp(pc,sql,qr,expression.getOperand(i),row));
			if (!Caster.toBooleanValue(executeExp(pc, sql, qr, expression.getOperand(i), row))) return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	/**
	 * 
	 * execute an and operation
	 * 
	 * @param sql
	 * @param qr QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executeOr(PageContext pc, SQL sql, Query qr, ZExpression expression, int row) throws PageException {
		int len = expression.nbOperands();

		// boolean rtn=Caster.toBooleanValue(executeExp(pc,sql,qr,expression.getOperand(0),row));
		for (int i = 0; i < len; i++) {
			if (Caster.toBooleanValue(executeExp(pc, sql, qr, expression.getOperand(i), row))) return Boolean.TRUE;
			// if(rtn)break;
			// rtn=rtn || Caster.toBooleanValue(executeExp(pc,sql,qr,expression.getOperand(i),row));
		}
		return Boolean.FALSE;
	}

	/**
	 * 
	 * execute an equal operation
	 * 
	 * @param sql
	 * @param qr QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executeEQ(PageContext pc, SQL sql, Query qr, ZExpression expression, int row) throws PageException {
		return (executeCompare(pc, sql, qr, expression, row) == 0) ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * 
	 * execute a not equal operation
	 * 
	 * @param sql
	 * @param qr QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executeNEQ(PageContext pc, SQL sql, Query qr, ZExpression expression, int row) throws PageException {
		return (executeCompare(pc, sql, qr, expression, row) != 0) ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * 
	 * execute a less than operation
	 * 
	 * @param sql
	 * @param qr QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executeLT(PageContext pc, SQL sql, Query qr, ZExpression expression, int row) throws PageException {
		return (executeCompare(pc, sql, qr, expression, row) < 0) ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * 
	 * execute a less than or equal operation
	 * 
	 * @param sql
	 * @param qr QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executeLTE(PageContext pc, SQL sql, Query qr, ZExpression expression, int row) throws PageException {
		return (executeCompare(pc, sql, qr, expression, row) <= 0) ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * 
	 * execute a greater than operation
	 * 
	 * @param sql
	 * @param qr QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executeGT(PageContext pc, SQL sql, Query qr, ZExpression expression, int row) throws PageException {
		return (executeCompare(pc, sql, qr, expression, row) > 0) ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * 
	 * execute a greater than or equal operation
	 * 
	 * @param sql
	 * @param qr QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executeGTE(PageContext pc, SQL sql, Query qr, ZExpression expression, int row) throws PageException {
		return (executeCompare(pc, sql, qr, expression, row) >= 0) ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * 
	 * execute an equal operation
	 * 
	 * @param sql
	 * @param qr QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private int executeCompare(PageContext pc, SQL sql, Query qr, ZExpression expression, int row) throws PageException {
		return OpUtil.compare(pc, executeExp(pc, sql, qr, expression.getOperand(0), row), executeExp(pc, sql, qr, expression.getOperand(1), row));
	}

	private Object executeLike(PageContext pc, SQL sql, Query qr, ZExpression expression, int row) throws PageException {
		return Caster.toBoolean(
				like(sql, Caster.toString(executeExp(pc, sql, qr, expression.getOperand(0), row)), Caster.toString(executeExp(pc, sql, qr, expression.getOperand(1), row))));
	}

	private boolean like(SQL sql, String haystack, String needle) throws PageException {
		return LikeCompare.like(sql, haystack, needle);
	}

	/**
	 * 
	 * execute a greater than or equal operation
	 * 
	 * @param sql
	 * @param qr QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executeIn(PageContext pc, SQL sql, Query qr, ZExpression expression, int row) throws PageException {
		int len = expression.nbOperands();
		Object left = executeExp(pc, sql, qr, expression.getOperand(0), row);

		for (int i = 1; i < len; i++) {
			if (OpUtil.compare(pc, left, executeExp(pc, sql, qr, expression.getOperand(i), row)) == 0) return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	/**
	 * 
	 * execute a minus operation
	 * 
	 * @param sql
	 * @param qr QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executeMinus(PageContext pc, SQL sql, Query qr, ZExpression expression, int row) throws PageException {
		return Caster.toDouble(executeExp(pc, sql, qr, expression.getOperand(0), row)) - Caster.toDoubleValue(executeExp(pc, sql, qr, expression.getOperand(1), row));
	}

	/**
	 * 
	 * execute a divide operation
	 * 
	 * @param sql
	 * @param qr QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executeDivide(PageContext pc, SQL sql, Query qr, ZExpression expression, int row) throws PageException {
		return Caster.toDouble(executeExp(pc, sql, qr, expression.getOperand(0), row)) / Caster.toDoubleValue(executeExp(pc, sql, qr, expression.getOperand(1), row));
	}

	/**
	 * 
	 * execute a multiply operation
	 * 
	 * @param sql
	 * @param qr QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executeMultiply(PageContext pc, SQL sql, Query qr, ZExpression expression, int row) throws PageException {
		return Caster.toDouble(executeExp(pc, sql, qr, expression.getOperand(0), row)) * Caster.toDoubleValue(executeExp(pc, sql, qr, expression.getOperand(1), row));
	}

	/**
	 * 
	 * execute a multiply operation
	 * 
	 * @param sql
	 * @param qr QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executeExponent(PageContext pc, SQL sql, Query qr, ZExpression expression, int row) throws PageException {
		return Integer
				.valueOf(Caster.toIntValue(executeExp(pc, sql, qr, expression.getOperand(0), row)) ^ Caster.toIntValue(executeExp(pc, sql, qr, expression.getOperand(1), row)));
	}

	/**
	 * 
	 * execute a plus operation
	 * 
	 * @param sql
	 * @param qr QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executePlus(PageContext pc, SQL sql, Query qr, ZExpression expression, int row) throws PageException {
		Object left = executeExp(pc, sql, qr, expression.getOperand(0), row);
		Object right = executeExp(pc, sql, qr, expression.getOperand(1), row);

		try {
			return OpUtil.plusRef(pc, Caster.toNumber(left), Caster.toNumber(right));
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
	 * @param qr QueryResult to execute on it
	 * @param expression
	 * @param row row of resultset to execute
	 * @return result
	 * @throws PageException
	 */
	private Object executeBetween(PageContext pc, SQL sql, Query qr, ZExpression expression, int row) throws PageException {
		Object left = executeExp(pc, sql, qr, expression.getOperand(0), row);
		Object right1 = executeExp(pc, sql, qr, expression.getOperand(1), row);
		Object right2 = executeExp(pc, sql, qr, expression.getOperand(2), row);
		return ((OpUtil.compare(pc, left, right1) <= 0) && (OpUtil.compare(pc, left, right2) >= 0)) ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * Executes a constant value
	 * 
	 * @param sql
	 * @param qr
	 * @param constant
	 * @param row
	 * @return result
	 * @throws PageException
	 */
	private Object executeConstant(SQL sql, Query qr, ZConstant constant, int row) throws PageException {
		switch (constant.getType()) {
		case ZConstant.COLUMNNAME: {
			if (constant.getValue().equals(SQLPrettyfier.PLACEHOLDER_QUESTION)) {
				int pos = sql.getPosition();
				sql.setPosition(pos + 1);
				if (sql.getItems().length <= pos) throw new DatabaseException("invalid syntax for SQL Statement", null, sql, null);
				return sql.getItems()[pos].getValueForCF();
			}
			return qr.getAt(ListUtil.last(constant.getValue(), ".", true), row);
		}
		case ZConstant.NULL:
			return null;
		case ZConstant.NUMBER:
			return Caster.toDouble(constant.getValue());
		case ZConstant.STRING:
			return constant.getValue();
		case ZConstant.UNKNOWN:
		default:
			throw new DatabaseException("invalid constant value", null, sql, null);
		}
	}
}