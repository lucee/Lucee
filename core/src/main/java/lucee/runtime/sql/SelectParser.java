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

import java.util.ArrayList;
import java.util.List;

import lucee.commons.lang.ParserString;
import lucee.commons.lang.types.RefBoolean;
import lucee.commons.lang.types.RefBooleanImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.sql.exp.Column;
import lucee.runtime.sql.exp.ColumnExpression;
import lucee.runtime.sql.exp.Expression;
import lucee.runtime.sql.exp.op.Operation;
import lucee.runtime.sql.exp.op.Operation1;
import lucee.runtime.sql.exp.op.Operation2;
import lucee.runtime.sql.exp.op.Operation3;
import lucee.runtime.sql.exp.op.OperationN;
import lucee.runtime.sql.exp.value.ValueBoolean;
import lucee.runtime.sql.exp.value.ValueDate;
import lucee.runtime.sql.exp.value.ValueNull;
import lucee.runtime.sql.exp.value.ValueNumber;
import lucee.runtime.sql.exp.value.ValueString;

public class SelectParser {

	/*
	 * SELECT [{LIMIT <offset> <limit> | TOP <limit>}[1]][ALL | DISTINCT] { <selectExpression> | table.*
	 * | * } [, ...] [INTO [CACHED | TEMP | TEXT][1] newTable] FROM tableList [WHERE Expression] [GROUP
	 * BY Expression [, ...]] [HAVING Expression] [{ UNION [ALL | DISTINCT] | {MINUS [DISTINCT] | EXCEPT
	 * [DISTINCT] } | INTERSECT [DISTINCT] } selectStatement] [ORDER BY orderExpression [, ...]] [LIMIT
	 * <limit> [OFFSET <offset>]];
	 */

	private int columnIndex = 0;

	// select <select-statement> from <tables> where <where-statement>
	public Selects parse(String sql) throws SQLParserException {
		columnIndex = 0;
		ParserString raw = new ParserString(sql.trim());
		Selects selects = new Selects();
		Select select = new Select();

		boolean runAgain = false;
		do {

			// select
			if (!raw.forwardIfCurrentAndNoWordNumberAfter("select")) throw new SQLParserException("missing select");
			raw.removeSpace();

			// top
			if (raw.forwardIfCurrentAndNoWordNumberAfter("top")) {
				raw.removeSpace();
				ValueNumber number = number(raw);
				if (number == null) throw new SQLParserException("missing top number");
				select.setTop(number);
				raw.removeSpace();
			}

			// distinct
			if (raw.forwardIfCurrentAndNoWordNumberAfter("distinct")) {
				select.setDistinct(true);
				raw.removeSpace();
			}

			// all
			if (raw.forwardIfCurrentAndNoWordNumberAfter("all")) {
				select.setDistinct(false);
				raw.removeSpace();
			}

			// select expression
			selectExpressions(raw, select);
			raw.removeSpace();

			// from
			if (!raw.forwardIfCurrentAndNoWordNumberAfter("from")) throw new SQLParserException("missing from");
			tableList(raw, select);
			raw.removeSpace();

			// where
			if (raw.forwardIfCurrentAndNoWordNumberAfter("where")) whereExpressions(raw, select);
			raw.removeSpace();

			// group by
			if (raw.forwardIfCurrentAndNoWordNumberAfter("group by")) {
				groupByExpressions(raw, select);
				raw.removeSpace();

				// having
				if (raw.forwardIfCurrentAndNoWordNumberAfter("having")) havingExpressions(raw, select);
				raw.removeSpace();
			}
			selects.addSelect(select);

			runAgain = false;
			// union
			if (raw.forwardIfCurrentAndNoWordNumberAfter("union")) {
				select = new Select();
				raw.removeSpace();
				if (raw.forwardIfCurrentAndNoWordNumberAfter("all")) {
					raw.removeSpace();
					select.setUnionDistinct(false);
				}
				else if (raw.forwardIfCurrentAndNoWordNumberAfter("distinct")) {
					raw.removeSpace();
					select.setUnionDistinct(true);
				}
				else select.setDistinct(true);
				raw.removeSpace();
				runAgain = true;
			}

		}
		while (runAgain);

		// order by
		if (raw.forwardIfCurrentAndNoWordNumberAfter("order by")) orderByExpressions(raw, selects);
		raw.removeSpace();

		if (raw.forwardIfCurrent(';')) raw.removeSpace();

		if (!raw.isAfterLast()) throw new SQLParserException("can not read the full sql statement (stop at:" + raw.getCurrent() + ")");
		return selects;
	}

	private void orderByExpressions(ParserString raw, Selects selects) throws SQLParserException {
		Expression exp = null;
		do {
			raw.removeSpace();
			// print.out(raw.getCurrent());
			exp = expression(raw);
			if (!(exp instanceof Column)) throw new SQLParserException("invalid order by part of query");
			Column col = (Column) exp;

			raw.removeSpace();
			if (raw.forwardIfCurrent("desc")) col.setDirectionBackward(true);
			if (raw.forwardIfCurrent("asc")) col.setDirectionBackward(false);
			selects.addOrderByExpression(col);
			raw.removeSpace();
		}
		while (raw.forwardIfCurrent(','));
		raw.removeSpace();
	}

	private void whereExpressions(ParserString raw, Select select) throws SQLParserException {
		raw.removeSpace();
		Expression exp = expression(raw);
		if (exp == null) throw new SQLParserException("missing where expression");
		if (!(exp instanceof Operation)) throw new SQLParserException("invalid where expression (" + Caster.toClassName(exp) + ")");
		select.setWhereExpression((Operation) exp);
		raw.removeSpace();
	}

	private void havingExpressions(ParserString raw, Select select) throws SQLParserException {
		raw.removeSpace();
		Expression exp = expression(raw);
		if (exp == null) throw new SQLParserException("missing having expression");
		if (!(exp instanceof Operation)) throw new SQLParserException("invalid having expression");
		select.setHaving((Operation) exp);
		raw.removeSpace();
	}

	private void groupByExpressions(ParserString raw, Select select) throws SQLParserException {
		Expression exp = null;
		do {
			raw.removeSpace();
			// print.out(raw.getCurrent());
			exp = expression(raw);
			if (!(exp instanceof Column)) throw new SQLParserException("invalid group by part of query");
			Column col = (Column) exp;

			select.addGroupByExpression(col);
			raw.removeSpace();
		}
		while (raw.forwardIfCurrent(','));
		raw.removeSpace();
	}

	private void tableList(ParserString raw, Select select) throws SQLParserException {
		Column column = null;
		Expression exp = null;
		do {
			raw.removeSpace();

			exp = column(raw);
			if (!(exp instanceof Column)) throw new SQLParserException("invalid table definition");
			column = (Column) exp;
			raw.removeSpace();
			if (raw.forwardIfCurrent("as ")) {
				String alias = identifier(raw, new RefBooleanImpl(false));
				if (alias == null) throw new SQLParserException("missing alias in select part");
				column.setAlias(alias);
			}
			else {
				int start = raw.getPos();
				RefBoolean hasBracked = new RefBooleanImpl(false);
				String alias = identifier(raw, hasBracked);// TODO having usw
				if (!hasBracked.toBooleanValue()) {
					if ("where".equalsIgnoreCase(alias)) raw.setPos(start);
					else if ("group".equalsIgnoreCase(alias)) raw.setPos(start);
					else if ("having".equalsIgnoreCase(alias)) raw.setPos(start);
					else if ("union".equalsIgnoreCase(alias)) raw.setPos(start);
					else if ("order".equalsIgnoreCase(alias)) raw.setPos(start);
					else if ("limit".equalsIgnoreCase(alias)) raw.setPos(start);
					else if (alias != null) column.setAlias(alias);
				}
				else {
					if (alias != null) column.setAlias(alias);
				}

			}
			select.addFromExpression(column);
			raw.removeSpace();
		}
		while (raw.forwardIfCurrent(','));

	}

	// { (selectStatement) [AS] label | tableName [AS] label}
	private void selectExpressions(ParserString raw, Select select) throws SQLParserException {
		Expression exp = null;
		do {
			raw.removeSpace();

			exp = expression(raw);
			if (exp == null) throw new SQLParserException("missing expression in select part of query");
			raw.removeSpace();
			if (raw.forwardIfCurrent("as ")) {
				String alias = identifier(raw, new RefBooleanImpl(false));
				if (alias == null) throw new SQLParserException("missing alias in select part");
				exp.setAlias(alias);
			}
			else {
				int start = raw.getPos();
				RefBoolean hb = new RefBooleanImpl(false);
				String alias = identifier(raw, hb);
				if (!hb.toBooleanValue() && "from".equalsIgnoreCase(alias)) raw.setPos(start);
				else if (alias != null) exp.setAlias(alias);
			}
			select.addSelectExpression(exp);
			raw.removeSpace();
		}
		while (raw.forwardIfCurrent(','));

	}

	private Expression expression(ParserString raw) throws SQLParserException {
		return xorOp(raw);
	}

	private Expression xorOp(ParserString raw) throws SQLParserException {
		Expression expr = orOp(raw);

		while (raw.forwardIfCurrentAndNoWordNumberAfter("xor")) {
			raw.removeSpace();
			expr = new Operation2(expr, orOp(raw), Operation.OPERATION2_XOR);
		}
		return expr;
	}

	private Expression orOp(ParserString raw) throws SQLParserException {
		Expression expr = andOp(raw);

		while (raw.forwardIfCurrentAndNoWordNumberAfter("or")) {
			raw.removeSpace();
			expr = new Operation2(expr, andOp(raw), Operation.OPERATION2_OR);
		}
		return expr;
	}

	private Expression andOp(ParserString raw) throws SQLParserException {
		Expression expr = notOp(raw);

		while (raw.forwardIfCurrentAndNoWordNumberAfter("and")) {
			raw.removeSpace();
			expr = new Operation2(expr, notOp(raw), Operation.OPERATION2_AND);
		}
		return expr;
	}

	private Expression notOp(ParserString raw) throws SQLParserException {
		// NOT
		if (raw.forwardIfCurrentAndNoWordNumberAfter("not")) {
			raw.removeSpace();
			return new Operation1(decsionOp(raw), Operation.OPERATION1_NOT);
		}
		return decsionOp(raw);
	}

	private Expression decsionOp(ParserString raw) throws SQLParserException {

		Expression expr = plusMinusOp(raw);
		boolean hasChanged = false;
		do {
			hasChanged = false;

			// value BETWEEN value AND value
			if (raw.forwardIfCurrent("between ")) {
				raw.removeSpace();
				Expression left = plusMinusOp(raw);
				raw.removeSpace();
				if (!raw.forwardIfCurrent("and ")) throw new SQLParserException("invalid operation (between) missing operator and");
				raw.removeSpace();
				Expression right = plusMinusOp(raw);
				raw.removeSpace();
				expr = new Operation3(expr, left, right, Operation.OPERATION3_BETWEEN);
				hasChanged = true;
			}

			// value like value [escape value]
			else if (raw.forwardIfCurrentAndNoWordNumberAfter("like")) {
				raw.removeSpace();
				Expression left = plusMinusOp(raw);
				raw.removeSpace();
				if (raw.forwardIfCurrentAndNoWordNumberAfter("escape ")) {
					raw.removeSpace();
					Expression right = plusMinusOp(raw);
					raw.removeSpace();
					expr = new Operation3(expr, left, right, Operation.OPERATION3_LIKE);
				}
				else {
					raw.removeSpace();
					expr = new Operation2(expr, left, Operation.OPERATION2_LIKE);
				}
				hasChanged = true;
			}

			// IS [NOT] NULL
			else if (raw.isCurrent("is ")) {
				int start = raw.getPos();
				if (raw.forwardIfCurrentAndNoWordNumberAfter("is null")) {
					raw.removeSpace();
					return new Operation1(expr, Operation.OPERATION1_IS_NULL);

				}
				else if (raw.forwardIfCurrentAndNoWordNumberAfter("is not null")) {
					raw.removeSpace();
					return new Operation1(expr, Operation.OPERATION1_IS_NOT_NULL);

				}
				else {
					raw.setPos(start);
					raw.removeSpace();
				}
			}

			// not in
			else if (raw.forwardIfCurrent("not in", '(')) {
				expr = new OperationN("not_in", readArguments(raw, expr));
				hasChanged = true;
			}
			// in
			else if (raw.forwardIfCurrent("in", '(')) {
				expr = new OperationN("in", readArguments(raw, expr));
				hasChanged = true;
			}
			// not like
			if (raw.forwardIfCurrentAndNoWordNumberAfter("not like")) {
				expr = decisionOpCreate(raw, Operation.OPERATION2_NOT_LIKE, expr);
				hasChanged = true;
			}
			/*
			 * / like else if (raw.forwardIfCurrentAndNoWordNumberAfter("like")) { expr =
			 * decisionOpCreate(raw,Operation.OPERATION2_LIKE,expr); hasChanged=true; }
			 */
			// =
			else if (raw.forwardIfCurrent('=')) {
				expr = decisionOpCreate(raw, Operation.OPERATION2_EQ, expr);
				hasChanged = true;
			}
			// !=
			else if (raw.forwardIfCurrent("!=")) {
				expr = decisionOpCreate(raw, Operation.OPERATION2_NEQ, expr);
				hasChanged = true;
			}
			// <>
			else if (raw.forwardIfCurrent("<>")) {
				expr = decisionOpCreate(raw, Operation.OPERATION2_LTGT, expr);
				hasChanged = true;
			}
			// <, <=
			else if (raw.isCurrent('<')) {
				if (raw.forwardIfCurrent("<=")) {
					expr = decisionOpCreate(raw, Operation.OPERATION2_LTE, expr);
					hasChanged = true;
				}
				else {
					raw.next();
					expr = decisionOpCreate(raw, Operation.OPERATION2_LT, expr);
					hasChanged = true;
				}
			}
			// >, =>
			else if (raw.isCurrent('>')) {
				if (raw.forwardIfCurrent("=>")) {
					expr = decisionOpCreate(raw, Operation.OPERATION2_GTE, expr);
					hasChanged = true;
				}
				if (raw.forwardIfCurrent(">=")) {
					expr = decisionOpCreate(raw, Operation.OPERATION2_GTE, expr);
					hasChanged = true;
				}
				else {
					raw.next();
					expr = decisionOpCreate(raw, Operation.OPERATION2_GT, expr);
					hasChanged = true;
				}
			}
		}
		while (hasChanged);
		return expr;
	}

	private Expression decisionOpCreate(ParserString raw, int operation, Expression left) throws SQLParserException {
		raw.removeSpace();
		return new Operation2(left, plusMinusOp(raw), operation);
	}

	private Expression plusMinusOp(ParserString raw) throws SQLParserException {
		Expression expr = modOp(raw);

		while (!raw.isLast()) {

			// Plus Operation
			if (raw.forwardIfCurrent('+')) {
				raw.removeSpace();
				expr = new Operation2(expr, modOp(raw), Operation.OPERATION2_PLUS);
			}
			// Minus Operation
			else if (raw.forwardIfCurrent('-')) {
				raw.removeSpace();
				expr = new Operation2(expr, modOp(raw), Operation.OPERATION2_MINUS);
			}
			else break;
		}
		return expr;
	}

	private Expression modOp(ParserString raw) throws SQLParserException {
		Expression expr = divMultiOp(raw);

		// Modulus Operation
		while (raw.forwardIfCurrent('%')) {
			raw.removeSpace();
			expr = new Operation2(expr, divMultiOp(raw), Operation.OPERATION2_MOD);
		}
		return expr;
	}

	private Expression divMultiOp(ParserString raw) throws SQLParserException {
		Expression expr = expoOp(raw);
		while (!raw.isLast()) {
			// Multiply Operation
			if (raw.forwardIfCurrent('*')) {
				raw.removeSpace();
				expr = new Operation2(expr, expoOp(raw), Operation.OPERATION2_MULTIPLY);
			}
			// Divide Operation
			else if (raw.forwardIfCurrent('/')) {
				raw.removeSpace();
				expr = new Operation2(expr, expoOp(raw), Operation.OPERATION2_DIVIDE);
			}
			else {
				break;
			}

		}
		return expr;
	}

	private Expression expoOp(ParserString raw) throws SQLParserException {
		Expression exp = negateMinusOp(raw);

		// Modulus Operation
		while (raw.forwardIfCurrent('^')) {
			raw.removeSpace();
			exp = new Operation2(exp, negateMinusOp(raw), Operation.OPERATION2_EXP);
		}
		return exp;
	}

	private Expression negateMinusOp(ParserString raw) throws SQLParserException {
		// And Operation
		if (raw.forwardIfCurrent('-')) {
			raw.removeSpace();
			return new Operation1(clip(raw), Operation.OPERATION1_MINUS);
		}
		else if (raw.forwardIfCurrent('+')) {
			raw.removeSpace();
			return new Operation1(clip(raw), Operation.OPERATION1_PLUS);
		}
		return clip(raw);
	}

	// { Expression | COUNT(*) | {COUNT | MIN | MAX | SUM | AVG | SOME | EVERY | VAR_POP | VAR_SAMP |
	// STDDEV_POP | STDDEV_SAMP} ([ALL | DISTINCT][1]] Expression) } [[AS] label]
	private Expression clip(ParserString raw) throws SQLParserException {
		Expression exp = column(raw);
		// if(exp==null)exp=brackedColumn(raw);
		if (exp == null) exp = date(raw);
		if (exp == null) exp = bracked(raw);
		if (exp == null) exp = number(raw);
		if (exp == null) exp = string(raw);
		return exp;
	}

	private Expression bracked(ParserString raw) throws SQLParserException {
		if (!raw.forwardIfCurrent('(')) return null;
		raw.removeSpace();
		Expression exp = expression(raw);
		raw.removeSpace();
		if (!raw.forwardIfCurrent(')')) throw new SQLParserException("missing closing )");
		raw.removeSpace();
		return exp;// new BracketExpression(exp);
	}

	private Expression column(ParserString raw) throws SQLParserException {
		RefBoolean hb = new RefBooleanImpl(false);
		String name = identifier(raw, hb);
		if (name == null) return null;
		if (!hb.toBooleanValue()) {
			if ("true".equalsIgnoreCase(name)) return ValueBoolean.TRUE;
			if ("false".equalsIgnoreCase(name)) return ValueBoolean.FALSE;
			if ("null".equalsIgnoreCase(name)) return ValueNull.NULL;
		}

		ColumnExpression column = new ColumnExpression(name, name.equals("?") ? columnIndex++ : 0);
		raw.removeSpace();
		while (raw.forwardIfCurrent(".")) {
			raw.removeSpace();
			String sub = identifier(raw, hb);
			if (sub == null) throw new SQLParserException("invalid column definition");
			column.setSub(sub);
		}
		raw.removeSpace();
		if (raw.forwardIfCurrent('(')) {
			return new OperationN(column.getFullName(), readArguments(raw));
		}
		return column;
	}

	private List readArguments(ParserString raw) throws SQLParserException {
		return readArguments(raw, null);
	}

	private List readArguments(ParserString raw, Expression exp) throws SQLParserException {
		List args = new ArrayList();
		Expression arg;
		if (exp != null) args.add(exp);
		do {
			raw.removeSpace();
			if (raw.isCurrent(')')) break;

			args.add(arg = expression(raw));
			raw.removeSpace();
			// check for alias
			if (raw.forwardIfCurrent("as ")) {
				raw.removeSpace();
				arg.setAlias(identifier(raw, null));
				raw.removeSpace();
			}

		}
		while (raw.forwardIfCurrent(','));
		if (!raw.forwardIfCurrent(')')) throw new SQLParserException("missing closing )");
		raw.removeSpace();
		return args;
	}

	private ValueNumber number(ParserString raw) throws SQLParserException {
		// check first character is a number literal representation
		if (!(raw.isCurrentBetween('0', '9') || raw.isCurrent('.'))) return null;

		StringBuffer rtn = new StringBuffer();

		// get digit on the left site of the dot
		if (raw.isCurrent('.')) rtn.append('0');
		else rtn.append(digit(raw));
		// read dot if exist
		if (raw.forwardIfCurrent('.')) {
			rtn.append('.');
			String rightSite = digit(raw);
			if (rightSite.length() > 0 && raw.forwardIfCurrent('e')) {
				if (raw.isCurrentBetween('0', '9')) {
					rightSite += 'e' + digit(raw);
				}
				else {
					raw.previous();
				}
			}
			// read right side of the dot
			if (rightSite.length() == 0) throw new SQLParserException("Number can't end with [.]");
			rtn.append(rightSite);
		}
		raw.removeSpace();
		return new ValueNumber(rtn.toString());

	}

	private String digit(ParserString raw) {
		String rtn = "";
		while (raw.isValidIndex()) {
			if (!raw.isCurrentBetween('0', '9')) break;
			rtn += raw.getCurrentLower();
			raw.next();
		}
		return rtn;
	}

	private ValueString string(ParserString raw) throws SQLParserException {

		// check starting character for a string literal
		if (!raw.isCurrent('\'')) return null;

		// Init Parameter
		StringBuffer str = new StringBuffer();

		while (raw.hasNext()) {
			raw.next();

			// check quoter
			if (raw.isCurrent('\'')) {
				// Ecaped sharp
				if (raw.isNext('\'')) {
					raw.next();
					str.append('\'');
				}
				// finsish
				else {
					break;
				}
			}
			// all other character
			else {
				str.append(raw.getCurrent());
			}
		}
		if (!raw.forwardIfCurrent('\'')) throw new SQLParserException("Invalid Syntax Closing ['] not found");

		raw.removeSpace();
		return new ValueString(str.toString());
	}

	private ValueDate date(ParserString raw) throws SQLParserException {

		if (!raw.isCurrent('{')) return null;

		// Init Parameter
		StringBuilder str = new StringBuilder();

		while (raw.hasNext()) {
			raw.next();
			if (raw.isCurrent('}')) break;
			str.append(raw.getCurrent());

		}
		if (!raw.forwardIfCurrent('}')) throw new SQLParserException("Invalid Syntax Closing [}] not found");

		raw.removeSpace();
		try {
			return new ValueDate("{" + str.toString() + "}");
		}
		catch (PageException e) {
			throw new SQLParserException("can't cast value [{" + str.toString() + "}] to date object");
		}
	}

	private String identifier(ParserString raw, RefBoolean hasBracked) throws SQLParserException {

		if (hasBracked != null && raw.forwardIfCurrent('[')) {
			hasBracked.setValue(true);
			return identifierBracked(raw);
		}
		else if (!(raw.isCurrentLetter() || raw.isCurrent('*') || raw.isCurrent('?') || raw.isCurrent('_'))) return null;

		int start = raw.getPos();
		do {
			raw.next();
			if (!(raw.isCurrentLetter() || raw.isCurrentBetween('0', '9') || raw.isCurrent('*') || raw.isCurrent('?') || raw.isCurrent('_'))) {
				break;
			}
		}
		while (raw.isValidIndex());
		String str = raw.substring(start, raw.getPos() - start);
		raw.removeSpace();

		return str;
	}

	private String identifierBracked(ParserString raw) throws SQLParserException {
		int start = raw.getPos();
		do {
			raw.next();
		}
		while (raw.isValidIndex() && !raw.isCurrent(']'));

		String str = raw.substring(start, raw.getPos() - start);
		if (!raw.forwardIfCurrent(']')) throw new SQLParserException("missing ending ] of identifier");
		return str;
	}

	public static void main(String[] args) {

		// print.out(new SelectParser().parse("select a, b as c, d e from test limit 3").toString());

		// String sql="select cast(susi as integer) as y,a=b as x from source";//WHERE (lft BETWEEN 1 AND 4
		// AND ID = 111)
		// print.out(new SelectParser().parse(sql).toString());

		/*
		 * print.out(new SelectParser().
		 * parse("select (a and b) s,'abc'<b as c,'abc'+b as c,'abc'%b as c,'abc'*b as c,'susi''s lustige dings', 'sss' as a, 'xxx' b, 1.1,1.2 as c, 1.3 d, e, f as g, h i, a.b.c.d as j, 1^1 as k  "
		 * + "from test, a as x, b y,a.b.c as y " +
		 * " where not t=d and x>y or x=0 xor '1'>'2' ").toString());
		 */
		// print.out("*****************************");
		/*
		 * print.out(new SelectParser().parse("select a, b as c, d e from test").toString()); print.out(new
		 * SelectParser().parse("select c is not null,c is null as x from test").toString()); print.out(new
		 * SelectParser().parse("select c between 'a' and 1,c between 'a' and 1 as x,a from test").toString(
		 * )); print.out(new SelectParser().parse("select c from test where a like b and c=1").toString());
		 * print.out(new SelectParser().
		 * parse("select true as x,b in(1,a,'x'), count(c),count(c) as x from test where s not in(1,2,3) and a like b and c=count(c)"
		 * ).toString());
		 */
		// print.out(new SelectParser().parse("select c from test where x=y order by test,a.b desc , a.b.c
		// asc ").toString());
		// print.out(new SelectParser().parse("select *, *.lastname from test").toString());
		// Selects select = new SelectParser().parse("SELECT * FROM qTest WHERE (ID = 7 AND data_ID = 1) OR
		// (data_ID = 110422)");
		// select = new SelectParser().parse("\nSELECT *\nFROM qAllTodos\nWHERE (\n (\n (\n toeditor_ID = ?
		// OR\n workflowtaskseditor_ID = ? OR\n (editorgroup_ID IN (?) AND\n workflowmethod_ID = 1) OR\n
		// (editorgroup_ID IN (?) AND\n workflowmethod_ID = 2 AND status <> 6) OR\n (editorgroup_ID IN (?,?)
		// AND\n workflowmethod_ID = 3) OR\n (editorgroup_ID IN (?) AND\n workflowmethod_ID = 4) OR\n
		// (editorgroup_ID IN (?) AND\n ? <> editor_ID AND\n workflowmethod_ID = 5) OR\n
		// (acceptedbyeditor_ID = ?) OR\n (passedtoeditor_ID = ?)\n ) AND\n status IN (1,2,6) AND\n
		// (acceptedbyeditor_ID = ? OR\n acceptedbyeditor_ID = 0) AND\n\n (site_ID = ? OR\n
		// workflowprocessessite_ID = ? OR\n workflowprocessessite_ID = 0) AND\n\n startdate <= '2007-09-14
		// 17:09:48' AND\n\n istimeouttask = 0\n )\n\n\n OR\n\n (\n\n confirm_needed <> 0 AND\n\n
		// (confirm_editor_ID = ? OR\n\n (confirm_editorgroup_ID IN (?) AND\n confirm_method_ID = 1) OR\n\n
		// (confirm_editorgroup_ID IN (?) AND\n confirm_method_ID = 2) OR\n\n (confirm_editorgroup_ID IN
		// (?,?) AND\n confirm_method_ID = 3) OR\n\n (confirm_editorgroup_ID IN (?) AND\n confirm_method_ID
		// = 4) OR\n\n (confirm_editorgroup_ID IN (?) AND\n donebyeditor_ID <> ? AND\n confirm_method_ID =
		// 5)\n ) AND\n\n confirmedbyeditor_ID = 0 AND\n\n status = 3 AND\n\n (workflowprocessessite_ID = ?
		// OR\n workflowprocessessite_ID = 0)\n )\n\n\n OR\n\n (\n\n istimeouttask <> 0 AND\n\n
		// (timeout_editor_ID = ? OR\n\n (timeout_editorgroup_ID IN (?) AND\n timeout_method_ID = 1) OR\n\n
		// (timeout_editorgroup_ID IN (?) AND\n timeout_method_ID = 2 AND\n status <> 6) OR\n\n
		// (timeout_editorgroup_ID IN (?,?) AND\n timeout_method_ID = 3) OR\n\n (timeout_editorgroup_ID IN
		// (?) AND\n timeout_method_ID = 4) OR\n\n (timeout_editorgroup_ID IN (?) AND\n ? <> deputyeditor_ID
		// AND\n timeout_method_ID = 5) OR\n\n (acceptedbyeditor_ID = ?) OR\n\n (passedtoeditor_ID = ?)\n )
		// AND\n\n status IN (1,2,6) AND\n\n (acceptedbyeditor_ID = ? OR\n acceptedbyeditor_ID = 0) AND\n\n
		// (workflowprocessessite_ID = ? OR\n workflowprocessessite_ID = 0)\n\n )\n )\n");
		// print.out(select.toString());
		// print.out(select.getTables().length);
		// lucee.print.out(new SelectParser().parse("select * from qryData where tableName like '%@_array'
		// or x=1").toString());
		// lucee.print.out(new SelectParser().parse("select * from qryData where tableName like '%@_array'
		// escape '@' or x=1").toString());

	}
}