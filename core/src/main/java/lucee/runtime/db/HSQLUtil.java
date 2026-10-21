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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import lucee.runtime.sql.old.ParseException;
import lucee.runtime.sql.old.ZExp;
import lucee.runtime.sql.old.ZExpression;
import lucee.runtime.sql.old.ZFromItem;
import lucee.runtime.sql.old.ZQuery;
import lucee.runtime.sql.old.ZStatement;
import lucee.runtime.sql.old.ZqlParser;

/**
 * utilities for sql statements
 */
public final class HSQLUtil {

	private ZqlParser parser;
	private String sql;
	private boolean isUnion;

	/**
	 * or of the class construct
	 * 
	 * @param sql SQl Statement as String
	 */
	public HSQLUtil(String sql) {
		this.sql = SQLPrettyfier.prettyfie(stripCastTypes(sql), true);// sqlToZQL(sql,true);
		parser = new ZqlParser(new ByteArrayInputStream(this.sql.getBytes()));
	}

	/*
	 * * transalte SQL syntax to a ZQL combatible form
	 * 
	 * @param sql sql to transalte
	 * 
	 * @param changePlaceHolder
	 * 
	 * @return translated sql / private static String sqlToZQL(String sql,boolean changePlaceHolder) {
	 * sql=sql.trim(); char c=' ';//,last=' '; int len=sql.length(); boolean insideString=false;
	 * StringBuilder sb=new StringBuilder(len);
	 * 
	 * 
	 * 
	 * for(int i=0;i<len;i++) { c=sql.charAt(i); if(insideString) { if(c=='\'') { if(i+1>=len ||
	 * sql.charAt(i+1)!='\'')insideString=false; } } else { if(c=='\'')insideString=true; else
	 * if(changePlaceHolder && c=='?') { sb.append("QUESTION_MARK_SIGN"); //last=c; continue; } else
	 * if(c=='a'|| c=='A') { if( (i!=0 && isWhiteSpace(sql.charAt(i-1))) && (i+1<len &&
	 * (sql.charAt(i+1)=='s' || sql.charAt(i+1)=='S')) && (i+2<len && isWhiteSpace(sql.charAt(i+2))) ) {
	 * i++; //last=c; continue; } } else if(c=='*') {
	 * 
	 * } } //last=c; sb.append(c); }
	 * 
	 * if(c!=';')sb.append(';');
	 * 
	 * return sb.toString();
	 * 
	 * }
	 */

	/*
	 * private static boolean isWhiteSpace(char c) { return (c==' ' || c=='\t' || c=='\b' || c=='\r' ||
	 * c=='\n'); }
	 */

	/**
	 * @return return the sql state inside
	 */
	public String getSQL() {
		return sql;
	}

	/**
	 * return all invoked tables by a sql statement
	 * 
	 * @return invoked tables in an ArrayList
	 * @throws ParseException
	 */
	public Set<String> getInvokedTables() throws ParseException {

		// Read all SQL statements from input
		ZStatement st;
		Set<String> tables = new HashSet<String>();

		while ((st = parser.readStatement()) != null) {
			this.sql = st.toString();
			if (st instanceof ZQuery) { // An SQL query: query the DB
				getInvokedTables((ZQuery) st, tables);
			}
			break;
		}
		return tables;
	}

	private void getInvokedTables(ZQuery query, Set<String> tablesNames) {
		// print.out("qry:"+query.getSet());
		Vector tables = query.getFrom();
		Enumeration e = tables.elements();

		// from
		while (e.hasMoreElements()) {
			ZFromItem fromItem = (ZFromItem) e.nextElement();
			tablesNames.add(fromItem.getFullName());
		}
		// where
		ZExp where = query.getWhere();
		if (where instanceof ZExpression) {
			parseZExpression((ZExpression) where, tablesNames);
		}
		// set
		ZExpression set = query.getSet();
		if (set != null) {
			isUnion = true;
			ZExp op = set.getOperand(0);
			if (op instanceof ZQuery) getInvokedTables((ZQuery) op, tablesNames);
		}
	}

	public boolean isUnion() {
		return isUnion;
	}

	private void parseZExpression(ZExpression expression, Set tablesNames) {
		Vector operands = expression.getOperands();
		Enumeration e = operands.elements();
		while (e.hasMoreElements()) {
			Object el = e.nextElement();
			if (el instanceof ZExpression) parseZExpression((ZExpression) el, tablesNames);
			else if (el instanceof ZQuery) getInvokedTables((ZQuery) el, tablesNames);
		}
	}

	/**
	 * the ZQL parser used to find the invoked tables does not understand "cast(expr AS type)", so it is
	 * reduced to "(expr)" for that parsing only, the SQL executed by HSQLDB is not changed
	 */
	static String stripCastTypes(String sql) {
		if (sql == null || sql.toLowerCase().indexOf("cast") == -1) return sql;
		StringBuilder sb = new StringBuilder(sql.length());
		int len = sql.length();
		int i = 0;
		while (i < len) {
			char c = sql.charAt(i);
			if (c == '\'' || c == '"') {
				int end = skipString(sql, i);
				sb.append(sql, i, end);
				i = end;
				continue;
			}
			if ((c == 'c' || c == 'C') && sql.regionMatches(true, i, "cast", 0, 4) && (i == 0 || !isIdentifierPart(sql.charAt(i - 1)))) {
				int open = i + 4;
				while (open < len && Character.isWhitespace(sql.charAt(open))) open++;
				if (open < len && sql.charAt(open) == '(') {
					int close = findClose(sql, open);
					int as = close == -1 ? -1 : findTopLevelAs(sql, open + 1, close);
					if (as != -1) {
						sb.append('(');
						sb.append(stripCastTypes(sql.substring(open + 1, as)));
						sb.append(')');
						i = close + 1;
						continue;
					}
				}
			}
			sb.append(c);
			i++;
		}
		return sb.toString();
	}

	private static int skipString(String sql, int start) {
		char q = sql.charAt(start);
		int i = start + 1;
		while (i < sql.length()) {
			if (sql.charAt(i) == q) {
				if (i + 1 < sql.length() && sql.charAt(i + 1) == q) {
					i += 2;
					continue;
				}
				return i + 1;
			}
			i++;
		}
		return sql.length();
	}

	private static int findClose(String sql, int open) {
		int depth = 0;
		int i = open;
		while (i < sql.length()) {
			char c = sql.charAt(i);
			if (c == '\'' || c == '"') {
				i = skipString(sql, i);
				continue;
			}
			if (c == '(') depth++;
			else if (c == ')' && --depth == 0) return i;
			i++;
		}
		return -1;
	}

	private static int findTopLevelAs(String sql, int start, int end) {
		int depth = 0;
		int last = -1;
		int i = start;
		while (i < end) {
			char c = sql.charAt(i);
			if (c == '\'' || c == '"') {
				i = skipString(sql, i);
				continue;
			}
			if (c == '(') depth++;
			else if (c == ')') depth--;
			else if (depth == 0 && (c == 'a' || c == 'A') && i + 2 < end && sql.regionMatches(true, i, "as", 0, 2) && i > start && Character.isWhitespace(sql.charAt(i - 1))
					&& Character.isWhitespace(sql.charAt(i + 2))) {
				last = i;
			}
			i++;
		}
		return last;
	}

	private static boolean isIdentifierPart(char c) {
		return Character.isLetterOrDigit(c) || c == '_' || c == '.';
	}
}
