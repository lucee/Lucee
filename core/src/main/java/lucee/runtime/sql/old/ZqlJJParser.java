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

package lucee.runtime.sql.old;

import java.io.InputStream;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Vector;

// Referenced classes of package Zql:
//            ParseException, ZTransactStmt, ZLockTable, ZUpdate, 
//            ZInsert, ZExpression, ZDelete, ZQuery, 
//            ZSelectItem, ZFromItem, ZGroupBy, ZOrderBy, 
//            ZConstant, SimpleCharStream, ZqlJJParserTokenManager, Token, 
//            ZqlJJParserConstants, ZUtils, ZStatement, ZExp

public final class ZqlJJParser implements ZqlJJParserConstants {
	static final class JJCalls {

		int gen;
		Token first;
		int arg;
		JJCalls next;

		JJCalls() {
		}
	}

	/*
	 * public static void main(String args[]) throws ParseException { ZqlJJParser zqljjparser = null; if
	 * (args.length < 1) { //System.out.println("Reading from stdin (exit; to finish)"); zqljjparser =
	 * new ZqlJJParser(System.in); } else { try { zqljjparser = new ZqlJJParser(new DataInputStream(new
	 * FileInputStream(args[0]))); } catch (FileNotFoundException filenotfoundexception) {
	 * //System.out.println("File " + args[0] + " not found. Reading from stdin"); zqljjparser = new
	 * ZqlJJParser(System.in); } } if (args.length > 0) System.out.println(args[0]); for (ZStatement
	 * zstatement = null; (zstatement = zqljjparser.SQLStatement()) != null;)
	 * //System.out.println(zstatement.toString());
	 * 
	 * System.out.println("Parse Successful"); }
	 */

	public final void BasicDataTypeDeclaration() throws ParseException {
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 15: // '\017'
		case 27: // '\033'
		case 34: // '"'
		case 44: // ','
		case 48: // '0'
		case 56: // '8'
		case 70: // 'F'
		case 71: // 'G'
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			case 15: // '\017'
				jj_consume_token(15);
				break;

			case 71: // 'G'
				jj_consume_token(71);
				break;

			case 70: // 'F'
				jj_consume_token(70);
				break;

			case 34: // '"'
				jj_consume_token(34);
				break;

			case 48: // '0'
				jj_consume_token(48);
				break;

			case 44: // ','
				jj_consume_token(44);
				break;

			case 56: // '8'
				jj_consume_token(56);
				break;

			case 27: // '\033'
				jj_consume_token(27);
				break;

			default:
				jj_la1[0] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			case 88: // 'X'
				jj_consume_token(88);
				jj_consume_token(76);
				switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
				case 89: // 'Y'
					jj_consume_token(89);
					jj_consume_token(76);
					break;

				default:
					jj_la1[1] = jj_gen;
					break;
				}
				jj_consume_token(90);
				break;

			default:
				jj_la1[2] = jj_gen;
				break;
			}
			break;

		case 20: // '\024'
			jj_consume_token(20);
			break;

		case 12: // '\f'
			jj_consume_token(12);
			break;

		case 13: // '\r'
			jj_consume_token(13);
			break;

		default:
			jj_la1[3] = jj_gen;
			jj_consume_token(-1);
			throw new ParseException();
		}
	}

	public final Vector SQLStatements() throws ParseException {
		Vector vector = new Vector();
		label0: do {
			ZStatement zstatement = SQLStatement();
			if (zstatement == null) return vector;
			vector.addElement(zstatement);
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			case 17: // '\021'
			case 21: // '\025'
			case 26: // '\032'
			case 33: // '!'
			case 39: // '\''
			case 54: // '6'
			case 57: // '9'
			case 59: // ';'
			case 60: // '<'
			case 68: // 'D'
				break;

			default:
				jj_la1[4] = jj_gen;
				break label0;
			}
		}
		while (true);
		return vector;
	}

	public final ZStatement SQLStatement() throws ParseException {
		// Object obj = null;
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 17: // '\021'
			ZTransactStmt ztransactstmt = CommitStatement();
			return ztransactstmt;

		case 21: // '\025'
			ZDelete zdelete = DeleteStatement();
			return zdelete;

		case 33: // '!'
			ZInsert zinsert = InsertStatement();
			return zinsert;

		case 39: // '\''
			ZLockTable zlocktable = LockTableStatement();
			return zlocktable;

		case 57: // '9'
			ZTransactStmt ztransactstmt1 = RollbackStatement();
			return ztransactstmt1;

		case 59: // ';'
			ZQuery zquery = QueryStatement();
			return zquery;

		case 60: // '<'
			ZTransactStmt ztransactstmt2 = SetTransactionStatement();
			return ztransactstmt2;

		case 68: // 'D'
			ZUpdate zupdate = UpdateStatement();
			return zupdate;

		case 26: // '\032'
		case 54: // '6'
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			case 26: // '\032'
				jj_consume_token(26);
				break;

			case 54: // '6'
				jj_consume_token(54);
				break;

			default:
				jj_la1[5] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			jj_consume_token(91);
			return null;
		}
		jj_la1[6] = jj_gen;
		jj_consume_token(-1);
		throw new ParseException();
	}

	public final ZTransactStmt CommitStatement() throws ParseException {
		ZTransactStmt ztransactstmt = new ZTransactStmt("COMMIT");
		jj_consume_token(17);
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 74: // 'J'
			jj_consume_token(74);
			break;

		default:
			jj_la1[7] = jj_gen;
			break;
		}
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 16: // '\020'
			jj_consume_token(16);
			Token token1 = jj_consume_token(86);
			ztransactstmt.setComment(token1.toString());
			break;

		default:
			jj_la1[8] = jj_gen;
			break;
		}
		jj_consume_token(91);
		return ztransactstmt;
	}

	public final ZLockTable LockTableStatement() throws ParseException {
		ZLockTable zlocktable = new ZLockTable();
		Vector vector = new Vector();
		jj_consume_token(39);
		jj_consume_token(65);
		String s = TableReference();
		vector.addElement(s);
		label0: do
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			default:
				jj_la1[9] = jj_gen;
				break label0;

			case 89: // 'Y'
				jj_consume_token(89);
				s = TableReference();
				vector.addElement(s);
				break;
			}
		while (true);
		jj_consume_token(32);
		s = LockMode();
		zlocktable.setLockMode(s);
		jj_consume_token(43);
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 46: // '.'
			jj_consume_token(46);
			zlocktable.nowait_ = true;
			break;

		default:
			jj_la1[10] = jj_gen;
			break;
		}
		jj_consume_token(91);
		zlocktable.addTables(vector);
		return zlocktable;
	}

	public final ZTransactStmt RollbackStatement() throws ParseException {
		ZTransactStmt ztransactstmt = new ZTransactStmt("ROLLBACK");
		jj_consume_token(57);
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 74: // 'J'
			jj_consume_token(74);
			break;

		default:
			jj_la1[11] = jj_gen;
			break;
		}
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 16: // '\020'
			jj_consume_token(16);
			Token token1 = jj_consume_token(86);
			ztransactstmt.setComment(token1.toString());
			break;

		default:
			jj_la1[12] = jj_gen;
			break;
		}
		jj_consume_token(91);
		return ztransactstmt;
	}

	public final ZTransactStmt SetTransactionStatement() throws ParseException {
		ZTransactStmt ztransactstmt = new ZTransactStmt("SET TRANSACTION");
		boolean flag = false;
		jj_consume_token(60);
		jj_consume_token(66);
		jj_consume_token(55);
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 50: // '2'
			jj_consume_token(50);
			flag = true;
			break;

		case 75: // 'K'
			jj_consume_token(75);
			break;

		default:
			jj_la1[13] = jj_gen;
			jj_consume_token(-1);
			throw new ParseException();
		}
		jj_consume_token(91);
		ztransactstmt.readOnly_ = flag;
		return ztransactstmt;
	}

	public final String LockMode() throws ParseException {
		StringBuffer stringbuffer = new StringBuffer();
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 58: // ':'
			jj_consume_token(58);
			stringbuffer.append("ROW ");
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			case 61: // '='
				jj_consume_token(61);
				stringbuffer.append("SHARE");
				break;

			case 24: // '\030'
				jj_consume_token(24);
				stringbuffer.append("EXCLUSIVE");
				break;

			default:
				jj_la1[14] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			return stringbuffer.toString();

		case 61: // '='
			jj_consume_token(61);
			stringbuffer.append("SHARE");
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			case 58: // ':'
			case 68: // 'D'
				switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
				case 68: // 'D'
					jj_consume_token(68);
					stringbuffer.append(" UPDATE");
					break;

				case 58: // ':'
					jj_consume_token(58);
					jj_consume_token(24);
					stringbuffer.append(" ROW EXCLUSIVE");
					break;

				default:
					jj_la1[15] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				break;

			default:
				jj_la1[16] = jj_gen;
				break;
			}
			return stringbuffer.toString();

		case 24: // '\030'
			jj_consume_token(24);
			return new String("EXCLUSIVE");
		}
		jj_la1[17] = jj_gen;
		jj_consume_token(-1);
		throw new ParseException();
	}

	public final ZUpdate UpdateStatement() throws ParseException {
		jj_consume_token(68);
		String s = TableReference();
		ZUpdate zupdate = new ZUpdate(s);
		jj_consume_token(60);
		ColumnValues(zupdate);
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 72: // 'H'
			jj_consume_token(72);
			ZExp zexp = SQLExpression();
			zupdate.addWhere(zexp);
			break;

		default:
			jj_la1[18] = jj_gen;
			break;
		}
		jj_consume_token(91);
		return zupdate;
	}

	public final void ColumnValues(ZUpdate zupdate) throws ParseException {
		String s = TableColumn();
		jj_consume_token(92);
		ZExp zexp = UpdatedValue();
		zupdate.addColumnUpdate(s, zexp);
		label0: do
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			default:
				jj_la1[19] = jj_gen;
				break label0;

			case 89: // 'Y'
				jj_consume_token(89);
				String s1 = TableColumn();
				jj_consume_token(92);
				ZExp zexp1 = UpdatedValue();
				zupdate.addColumnUpdate(s1, zexp1);
				break;
			}
		while (true);
	}

	public final ZExp UpdatedValue() throws ParseException {
		if (jj_2_1(0x7fffffff)) {
			jj_consume_token(88);
			ZQuery zquery = SelectStatement();
			jj_consume_token(90);
			return zquery;
		}
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 10: // '\n'
		case 19: // '\023'
		case 25: // '\031'
		case 40: // '('
		case 41: // ')'
		case 45: // '-'
		case 47: // '/'
		case 53: // '5'
		case 64: // '@'
		case 76: // 'L'
		case 82: // 'R'
		case 85: // 'U'
		case 86: // 'V'
		case 87: // 'W'
		case 88: // 'X'
		case 101: // 'e'
		case 102: // 'f'
			ZExp zexp = SQLExpression();
			return zexp;

		case 105: // 'i'
			ZExp zexp1 = PreparedCol();
			return zexp1;
		}
		jj_la1[20] = jj_gen;
		jj_consume_token(-1);
		throw new ParseException();
	}

	public final ZInsert InsertStatement() throws ParseException {
		jj_consume_token(33);
		jj_consume_token(36);
		String s = TableReference();
		ZInsert zinsert = new ZInsert(s);
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 88: // 'X'
			jj_consume_token(88);
			String s1 = TableColumn();
			Vector vector = new Vector();
			vector.addElement(s1);
			label0: do
				switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
				default:
					jj_la1[21] = jj_gen;
					break label0;

				case 89: // 'Y'
					jj_consume_token(89);
					String s2 = TableColumn();
					vector.addElement(s2);
					break;
				}
			while (true);
			jj_consume_token(90);
			zinsert.addColumns(vector);
			break;

		default:
			jj_la1[22] = jj_gen;
			break;
		}
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 69: // 'E'
			jj_consume_token(69);
			jj_consume_token(88);
			Vector vector1 = SQLExpressionList();
			jj_consume_token(90);
			ZExpression zexpression = new ZExpression(",");
			zexpression.setOperands(vector1);
			zinsert.addValueSpec(zexpression);
			break;

		case 59: // ';'
			ZQuery zquery = SelectStatement();
			zinsert.addValueSpec(zquery);
			break;

		default:
			jj_la1[23] = jj_gen;
			jj_consume_token(-1);
			throw new ParseException();
		}
		jj_consume_token(91);
		return zinsert;
	}

	public final ZDelete DeleteStatement() throws ParseException {
		jj_consume_token(21);
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 29: // '\035'
			jj_consume_token(29);
			break;

		default:
			jj_la1[24] = jj_gen;
			break;
		}
		String s = TableReference();
		ZDelete zdelete = new ZDelete(s);
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 72: // 'H'
			jj_consume_token(72);
			ZExp zexp = SQLExpression();
			zdelete.addWhere(zexp);
			break;

		default:
			jj_la1[25] = jj_gen;
			break;
		}
		jj_consume_token(91);
		return zdelete;
	}

	public final ZQuery QueryStatement() throws ParseException {
		ZQuery zquery = SelectStatement();
		jj_consume_token(91);
		return zquery;
	}

	public final String TableColumn() throws ParseException {
		StringBuffer stringbuffer = new StringBuffer();
		String s = OracleObjectName();
		stringbuffer.append(s);

		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 93: // ']'
			jj_consume_token(93);
			String s1 = OracleObjectName();

			stringbuffer.append("." + s1);
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			case 93: // ']'
				jj_consume_token(93);
				String s2 = OracleObjectName();
				stringbuffer.append("." + s2);
				break;

			default:
				jj_la1[26] = jj_gen;
				break;
			}
			break;

		default:
			jj_la1[27] = jj_gen;
			break;
		}
		return stringbuffer.toString();
	}

	public final String OracleObjectName() throws ParseException {
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 82: // 'R'
			Token token1 = jj_consume_token(82);
			return token1.toString();

		case 87: // 'W'
			Token token2 = jj_consume_token(87);
			return token2.toString();
		}

		Token token1 = jj_consume_token(82);
		return token1.toString();

		// MOD jj_la1[28] = jj_gen;
		// MOD jj_consume_token(-1);
		// MOD throw new ParseException();
	}

	public final String Relop() throws ParseException {
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 92: // '\\'
			Token token1 = jj_consume_token(92);
			return token1.toString();

		case 94: // '^'
			Token token2 = jj_consume_token(94);
			return token2.toString();

		case 95: // '_'
			Token token3 = jj_consume_token(95);
			return token3.toString();

		case 96: // '`'
			Token token4 = jj_consume_token(96);
			return token4.toString();

		case 97: // 'a'
			Token token5 = jj_consume_token(97);
			return token5.toString();

		case 98: // 'b'
			Token token6 = jj_consume_token(98);
			return token6.toString();

		case 99: // 'c'
			Token token7 = jj_consume_token(99);
			return token7.toString();

		case 100: // 'd'
			Token token8 = jj_consume_token(100);
			return token8.toString();

		case 93: // ']'
		default:
			jj_la1[29] = jj_gen;
			jj_consume_token(-1);
			throw new ParseException();
		}
	}

	public final String TableReference() throws ParseException {
		StringBuffer stringbuffer = new StringBuffer();
		String s = OracleObjectName();

		stringbuffer.append(s);
		/*
		 * changed by mic switch(jj_ntk != -1 ? jj_ntk : jj_ntk()) { case 93: // ']' jj_consume_token(93);
		 * String s1 = OracleObjectName(); stringbuffer.append("." + s1); break;
		 * 
		 * default: jj_la1[30] = jj_gen; break; }
		 */
		while (true) {
			if ((jj_ntk != -1 ? jj_ntk : jj_ntk()) == 93) {
				jj_consume_token(93);
				String s1 = OracleObjectName();
				stringbuffer.append("." + s1);
			}
			else {
				jj_la1[30] = jj_gen;
				break;
			}
		}

		return stringbuffer.toString();
	}

	public final void NumOrID() throws ParseException {
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 82: // 'R'
			jj_consume_token(82);
			break;

		case 76: // 'L'
		case 101: // 'e'
		case 102: // 'f'
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			case 101: // 'e'
			case 102: // 'f'
				switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
				case 101: // 'e'
					jj_consume_token(101);
					break;

				case 102: // 'f'
					jj_consume_token(102);
					break;

				default:
					jj_la1[31] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				break;

			default:
				jj_la1[32] = jj_gen;
				break;
			}
			jj_consume_token(76);
			break;

		default:
			jj_la1[33] = jj_gen;
			jj_consume_token(-1);
			throw new ParseException();
		}
	}

	public final ZQuery SelectStatement() throws ParseException {
		ZQuery zquery = SelectWithoutOrder();
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 52: // '4'
			Vector vector = OrderByClause();
			zquery.addOrderBy(vector);
			break;

		default:
			jj_la1[34] = jj_gen;
			break;
		}
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 28: // '\034'
			ForUpdateClause();
			zquery.forupdate_ = true;
			break;

		default:
			jj_la1[35] = jj_gen;
			break;
		}
		return zquery;
	}

	public final ZQuery SelectWithoutOrder() throws ParseException {
		ZQuery zquery = new ZQuery();
		ZExp zexp = null;
		ZGroupBy zgroupby = null;
		ZExpression zexpression = null;
		jj_consume_token(59);
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 5: // '\005'
		case 23: // '\027'
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			case 5: // '\005'
				jj_consume_token(5);
				break;

			case 23: // '\027'
				jj_consume_token(23);
				zquery.distinct_ = true;
				break;

			default:
				jj_la1[36] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			break;

		default:
			jj_la1[37] = jj_gen;
			break;
		}
		Vector vector = SelectList();
		Vector vector1 = FromClause();
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 72: // 'H'
			zexp = WhereClause();
			break;

		default:
			jj_la1[38] = jj_gen;
			break;
		}
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 18: // '\022'
		case 63: // '?'
			ConnectClause();
			break;

		default:
			jj_la1[39] = jj_gen;
			break;
		}
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 30: // '\036'
			zgroupby = GroupByClause();
			break;

		default:
			jj_la1[40] = jj_gen;
			break;
		}
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 35: // '#'
		case 42: // '*'
		case 67: // 'C'
			zexpression = SetClause();
			break;

		default:
			jj_la1[41] = jj_gen;
			break;
		}
		zquery.addSelect(vector);
		zquery.addFrom(vector1);
		zquery.addWhere(zexp);
		zquery.addGroupBy(zgroupby);
		zquery.addSet(zexpression);
		return zquery;
	}

	public final Vector SelectList() throws ParseException {
		Vector vector = new Vector(8);
		int i = jj_ntk != -1 ? jj_ntk : jj_ntk();
		// print.out("i:"+i);

		switch (i) {

		case 103: // 'g'
			jj_consume_token(103);
			vector.addElement(new ZSelectItem("*"));
			return vector;

		// case 15: token.kind=82;

		case 10: // '\n'
		case 19: // '\023'
		case 40: // '('
		case 41: // ')'
		case 47: // '/'
		case 64: // '@'
		case 76: // 'L'
		case 82: // 'R'
		case 85: // 'U'
		case 86: // 'V'
		case 87: // 'W'
		case 88: // 'X'
		case 101: // 'e'
		case 102: // 'f'
			// default:
			ZSelectItem zselectitem = SelectItem();
			vector.addElement(zselectitem);
			// print.out("sel:"+zselectitem.column_+"::"+zselectitem.alias_);
			label0: do
				switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
				default:
					jj_la1[42] = jj_gen;
					break label0;

				case 89: // 'Y'
					jj_consume_token(89);
					ZSelectItem zselectitem1 = SelectItem();
					vector.addElement(zselectitem1);
					break;
				}
			while (true);

			return vector;
		}
		jj_la1[43] = jj_gen;
		jj_consume_token(-1);
		throw new ParseException();
	}

	public final ZSelectItem SelectItem() throws ParseException {
		if (jj_2_2(0x7fffffff)) {
			String s = SelectStar();
			return new ZSelectItem(s);
		}
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		// case 15: token.kind=82;
		case 10: // '\n'
		case 19: // '\023'
		case 40: // '('
		case 41: // ')'
		case 47: // '/'
		case 64: // '@'
		case 76: // 'L'
		case 82: // 'R'
		case 85: // 'U'
		case 86: // 'V'
		case 87: // 'W'
		case 88: // 'X'
		case 101: // 'e'
		case 102: // 'f'
			// default:
			ZExp zexp = SQLSimpleExpression();
			ZSelectItem zselectitem = new ZSelectItem(zexp.toString());
			zselectitem.setExpression(zexp);
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			case 8: // '\b'
			case 82: // 'R'
				String s1 = SelectAlias();
				zselectitem.setAlias(s1);
				break;

			default:
				jj_la1[44] = jj_gen;
				break;
			}
			return zselectitem;
		}
		jj_la1[45] = jj_gen;
		jj_consume_token(-1);
		throw new ParseException();
	}

	public final String SelectAlias() throws ParseException {
		StringBuffer stringbuffer = new StringBuffer();
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 8: // '\b'
			jj_consume_token(8);
			break;

		default:
			jj_la1[46] = jj_gen;
			break;
		}
		label0: do {
			Token token1 = jj_consume_token(82);
			stringbuffer.append(token1.toString().trim() + " ");
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			case 82: // 'R'
				break;

			default:
				jj_la1[47] = jj_gen;
				break label0;
			}
		}
		while (true);
		return stringbuffer.toString().trim();
	}

	public final String SelectStar() throws ParseException {
		if (jj_2_3(2)) {
			String s = OracleObjectName();
			jj_consume_token(104);
			return new String(s + ".*");
		}
		if (jj_2_4(4)) {
			String s1 = OracleObjectName();
			jj_consume_token(93);
			String s2 = OracleObjectName();
			jj_consume_token(104);
			return new String(s1 + "." + s2 + ".*");
		}
		jj_consume_token(-1);
		throw new ParseException();

	}

	public final Vector FromClause() throws ParseException {
		Vector vector = new Vector(8);
		jj_consume_token(29);
		ZFromItem zfromitem = FromItem();
		vector.addElement(zfromitem);
		label0: do
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			default:
				jj_la1[48] = jj_gen;
				break label0;

			case 89: // 'Y'
				jj_consume_token(89);
				ZFromItem zfromitem1 = FromItem();
				vector.addElement(zfromitem1);
				break;
			}
		while (true);
		return vector;
	}

	public final ZFromItem FromItem() throws ParseException {
		String s = TableReference();

		ZFromItem zfromitem = new ZFromItem(s);
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 82: // 'R'
			Token token1 = jj_consume_token(82);
			zfromitem.setAlias(token1.toString());
			break;

		default:
			jj_la1[49] = jj_gen;
			break;
		}
		return zfromitem;
	}

	public final ZExp WhereClause() throws ParseException {
		jj_consume_token(72);
		ZExp zexp = SQLExpression();
		return zexp;
	}

	public final void ConnectClause() throws ParseException {
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 63: // '?'
			jj_consume_token(63);
			jj_consume_token(73);
			SQLExpression();
			break;

		default:
			jj_la1[50] = jj_gen;
			break;
		}
		jj_consume_token(18);
		jj_consume_token(14);
		SQLExpression();
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 63: // '?'
			jj_consume_token(63);
			jj_consume_token(73);
			SQLExpression();
			break;

		default:
			jj_la1[51] = jj_gen;
			break;
		}
	}

	public final ZGroupBy GroupByClause() throws ParseException {
		ZGroupBy zgroupby = null;
		jj_consume_token(30);
		jj_consume_token(14);
		Vector vector = SQLExpressionList();
		zgroupby = new ZGroupBy(vector);
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 31: // '\037'
			jj_consume_token(31);
			ZExp zexp = SQLExpression();
			zgroupby.setHaving(zexp);
			break;

		default:
			jj_la1[52] = jj_gen;
			break;
		}
		return zgroupby;
	}

	public final ZExpression SetClause() throws ParseException {
		Token token1;
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 67: // 'C'
			token1 = jj_consume_token(67);
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			case 5: // '\005'
				jj_consume_token(5);
				break;

			default:
				jj_la1[53] = jj_gen;
				break;
			}
			break;

		case 35: // '#'
			token1 = jj_consume_token(35);
			break;

		case 42: // '*'
			token1 = jj_consume_token(42);
			break;

		default:
			jj_la1[54] = jj_gen;
			jj_consume_token(-1);
			throw new ParseException();
		}
		ZExpression zexpression = new ZExpression(token1.toString());
		if (jj_2_5(0x7fffffff)) {
			jj_consume_token(88);
			ZQuery zquery = SelectWithoutOrder();
			zexpression.addOperand(zquery);
			jj_consume_token(90);
		}
		else {
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			case 59: // ';'
				ZQuery zquery1 = SelectWithoutOrder();
				zexpression.addOperand(zquery1);
				break;

			default:
				jj_la1[55] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
		}
		return zexpression;
	}

	public final Vector OrderByClause() throws ParseException {
		Vector vector = new Vector();
		jj_consume_token(52);
		jj_consume_token(14);
		ZExp zexp = SQLSimpleExpression();
		ZOrderBy zorderby = new ZOrderBy(zexp);
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 9: // '\t'
		case 22: // '\026'
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			case 9: // '\t'
				jj_consume_token(9);
				break;

			case 22: // '\026'
				jj_consume_token(22);
				zorderby.setAscOrder(false);
				break;

			default:
				jj_la1[56] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			break;

		default:
			jj_la1[57] = jj_gen;
			break;
		}
		vector.addElement(zorderby);
		label0: do
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			default:
				jj_la1[58] = jj_gen;
				break label0;

			case 89: // 'Y'
				jj_consume_token(89);
				ZExp zexp1 = SQLSimpleExpression();
				ZOrderBy zorderby1 = new ZOrderBy(zexp1);
				switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
				case 9: // '\t'
				case 22: // '\026'
					switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
					case 9: // '\t'
						jj_consume_token(9);
						break;

					case 22: // '\026'
						jj_consume_token(22);
						zorderby1.setAscOrder(false);
						break;

					default:
						jj_la1[59] = jj_gen;
						jj_consume_token(-1);
						throw new ParseException();
					}
					break;

				default:
					jj_la1[60] = jj_gen;
					break;
				}
				vector.addElement(zorderby1);
				break;
			}
		while (true);
		return vector;
	}

	public final void ForUpdateClause() throws ParseException {
		jj_consume_token(28);
		jj_consume_token(68);
		label0: switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 49: // '1'
			jj_consume_token(49);
			TableColumn();
			do
				switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
				default:
					jj_la1[61] = jj_gen;
					break label0;

				case 89: // 'Y'
					jj_consume_token(89);
					TableColumn();
					break;
				}
			while (true);

		default:
			jj_la1[62] = jj_gen;
			break;
		}
	}

	public final ZExp SQLExpression() throws ParseException {
		ZExpression zexpression = null;
		boolean flag = true;
		ZExp zexp = SQLAndExpression();
		label0: do
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			default:
				jj_la1[63] = jj_gen;
				break label0;

			case 51: // '3'
				jj_consume_token(51);
				ZExp zexp1 = SQLAndExpression();
				if (flag) zexpression = new ZExpression("OR", zexp);
				flag = false;
				zexpression.addOperand(zexp1);
				break;
			}
		while (true);
		return ((flag ? zexp : zexpression));
	}

	public final ZExp SQLAndExpression() throws ParseException {
		ZExpression zexpression = null;
		boolean flag = true;
		ZExp zexp = SQLUnaryLogicalExpression();
		label0: do
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			default:
				jj_la1[64] = jj_gen;
				break label0;

			case 6: // '\006'
				jj_consume_token(6);
				ZExp zexp1 = SQLUnaryLogicalExpression();
				if (flag) zexpression = new ZExpression("AND", zexp);
				flag = false;
				zexpression.addOperand(zexp1);
				break;
			}
		while (true);
		return ((flag ? zexp : zexpression));
	}

	public final ZExp SQLUnaryLogicalExpression() throws ParseException {
		boolean flag = false;
		if (jj_2_6(2)) {
			ZExpression zexpression = ExistsClause();
			return zexpression;
		}
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 10: // '\n'
		case 19: // '\023'
		case 40: // '('
		case 41: // ')'
		case 45: // '-'
		case 47: // '/'
		case 53: // '5'
		case 64: // '@'
		case 76: // 'L'
		case 82: // 'R'
		case 85: // 'U'
		case 86: // 'V'
		case 87: // 'W'
		case 88: // 'X'
		case 101: // 'e'
		case 102: // 'f'
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			case 45: // '-'
				jj_consume_token(45);
				flag = true;
				break;

			default:
				jj_la1[65] = jj_gen;
				break;
			}
			ZExp zexp = SQLRelationalExpression();
			Object obj;
			if (flag) obj = new ZExpression("NOT", zexp);
			else obj = zexp;
			return ((ZExp) (obj));
		}
		jj_la1[66] = jj_gen;
		jj_consume_token(-1);
		throw new ParseException();
	}

	public final ZExpression ExistsClause() throws ParseException {
		boolean flag = false;
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 45: // '-'
			jj_consume_token(45);
			flag = true;
			break;

		default:
			jj_la1[67] = jj_gen;
			break;
		}
		jj_consume_token(25);
		jj_consume_token(88);
		ZQuery zquery = SubQuery();
		jj_consume_token(90);
		ZExpression zexpression1 = new ZExpression("EXISTS", zquery);
		ZExpression zexpression;
		if (flag) zexpression = new ZExpression("NOT", zexpression1);
		else zexpression = zexpression1;
		return zexpression;
	}

	public final ZExp SQLRelationalExpression() throws ParseException {
		ZExpression zexpression = null;
		boolean flag = false;
		Object obj;
		if (jj_2_7(0x7fffffff)) {
			jj_consume_token(88);
			Vector vector = SQLExpressionList();
			jj_consume_token(90);
			obj = new ZExpression(",");
			((ZExpression) obj).setOperands(vector);
		}
		else {
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			case 10: // '\n'
			case 19: // '\023'
			case 40: // '('
			case 41: // ')'
			case 47: // '/'
			case 53: // '5'
			case 64: // '@'
			case 76: // 'L'
			case 82: // 'R'
			case 85: // 'U'
			case 86: // 'V'
			case 87: // 'W'
			case 88: // 'X'
			case 101: // 'e'
			case 102: // 'f'
				switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
				case 53: // '5'
					jj_consume_token(53);
					flag = true;
					break;

				default:
					jj_la1[68] = jj_gen;
					break;
				}
				ZExp zexp = SQLSimpleExpression();
				if (flag) obj = new ZExpression("PRIOR", zexp);
				else obj = zexp;
				break;

			default:
				jj_la1[69] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
		}
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 11: // '\013'
		case 32: // ' '
		case 37: // '%'
		case 38: // '&'
		case 45: // '-'
		case 92: // '\\'
		case 94: // '^'
		case 95: // '_'
		case 96: // '`'
		case 97: // 'a'
		case 98: // 'b'
		case 99: // 'c'
		case 100: // 'd'
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			case 92: // '\\'
			case 94: // '^'
			case 95: // '_'
			case 96: // '`'
			case 97: // 'a'
			case 98: // 'b'
			case 99: // 'c'
			case 100: // 'd'
				zexpression = SQLRelationalOperatorExpression();
				break;

			case 93: // ']'
			default:
				jj_la1[70] = jj_gen;
				if (jj_2_8(2)) zexpression = SQLInClause();
				else if (jj_2_9(2)) zexpression = SQLBetweenClause();
				else if (jj_2_10(2)) zexpression = SQLLikeClause();
				else switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
				case 37: // '%'
					zexpression = IsNullClause();
					break;

				default:
					jj_la1[71] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				break;
			}
			break;

		default:
			jj_la1[72] = jj_gen;
			break;
		}
		if (zexpression == null) return ((ZExp) (obj));
		Vector vector1 = zexpression.getOperands();
		if (vector1 == null) vector1 = new Vector();
		vector1.insertElementAt(obj, 0);
		zexpression.setOperands(vector1);
		return zexpression;
	}

	public final Vector SQLExpressionList() throws ParseException {
		Vector vector = new Vector(8);
		ZExp zexp = SQLSimpleExpressionOrPreparedCol();
		vector.addElement(zexp);
		label0: do
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			default:
				jj_la1[73] = jj_gen;
				break label0;

			case 89: // 'Y'
				jj_consume_token(89);
				ZExp zexp1 = SQLSimpleExpressionOrPreparedCol();
				vector.addElement(zexp1);
				break;
			}
		while (true);
		return vector;
	}

	public final ZExpression SQLRelationalOperatorExpression() throws ParseException {
		String s1 = null;
		String s = Relop();
		ZExpression zexpression = new ZExpression(s);
		Object obj;
		if (jj_2_11(0x7fffffff)) {
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			case 5: // '\005'
			case 7: // '\007'
				switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
				case 5: // '\005'
					jj_consume_token(5);
					s1 = "ALL";
					break;

				case 7: // '\007'
					jj_consume_token(7);
					s1 = "ANY";
					break;

				default:
					jj_la1[74] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				break;

			default:
				jj_la1[75] = jj_gen;
				break;
			}
			jj_consume_token(88);
			ZQuery zquery = SubQuery();
			jj_consume_token(90);
			if (s1 == null) obj = zquery;
			else obj = new ZExpression(s1, zquery);
		}
		else {
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			case 10: // '\n'
			case 19: // '\023'
			case 40: // '('
			case 41: // ')'
			case 47: // '/'
			case 53: // '5'
			case 64: // '@'
			case 76: // 'L'
			case 82: // 'R'
			case 85: // 'U'
			case 86: // 'V'
			case 87: // 'W'
			case 88: // 'X'
			case 101: // 'e'
			case 102: // 'f'
			case 105: // 'i'
				switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
				case 53: // '5'
					jj_consume_token(53);
					s1 = "PRIOR";
					break;

				default:
					jj_la1[76] = jj_gen;
					break;
				}
				ZExp zexp = SQLSimpleExpressionOrPreparedCol();
				if (s1 == null) obj = zexp;
				else obj = new ZExpression(s1, zexp);
				break;

			default:
				jj_la1[77] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
		}
		zexpression.addOperand(((ZExp) (obj)));
		return zexpression;
	}

	public final ZExp SQLSimpleExpressionOrPreparedCol() throws ParseException {
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 10: // '\n'
		case 19: // '\023'
		case 40: // '('
		case 41: // ')'
		case 47: // '/'
		case 64: // '@'
		case 76: // 'L'
		case 82: // 'R'
		case 85: // 'U'
		case 86: // 'V'
		case 87: // 'W'
		case 88: // 'X'
		case 101: // 'e'
		case 102: // 'f'
			ZExp zexp = SQLSimpleExpression();
			return zexp;

		case 105: // 'i'
			ZExp zexp1 = PreparedCol();
			return zexp1;
		}
		jj_la1[78] = jj_gen;
		jj_consume_token(-1);
		throw new ParseException();
	}

	public final ZExp PreparedCol() throws ParseException {
		jj_consume_token(105);
		return new ZExpression("?");
	}

	public final ZExpression SQLInClause() throws ParseException {
		boolean flag = false;
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 45: // '-'
			jj_consume_token(45);
			flag = true;
			break;

		default:
			jj_la1[79] = jj_gen;
			break;
		}
		jj_consume_token(32);
		ZExpression zexpression = new ZExpression(flag ? "NOT IN" : "IN");
		jj_consume_token(88);
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 10: // '\n'
		case 19: // '\023'
		case 40: // '('
		case 41: // ')'
		case 47: // '/'
		case 64: // '@'
		case 76: // 'L'
		case 82: // 'R'
		case 85: // 'U'
		case 86: // 'V'
		case 87: // 'W'
		case 88: // 'X'
		case 101: // 'e'
		case 102: // 'f'
		case 105: // 'i'
			Vector vector = SQLExpressionList();
			zexpression.setOperands(vector);
			break;

		case 59: // ';'
			ZQuery zquery = SubQuery();
			zexpression.addOperand(zquery);
			break;

		default:
			jj_la1[80] = jj_gen;
			jj_consume_token(-1);
			throw new ParseException();
		}
		jj_consume_token(90);
		return zexpression;
	}

	public final ZExpression SQLBetweenClause() throws ParseException {
		boolean flag = false;
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 45: // '-'
			jj_consume_token(45);
			flag = true;
			break;

		default:
			jj_la1[81] = jj_gen;
			break;
		}
		jj_consume_token(11);
		ZExp zexp = SQLSimpleExpression();
		jj_consume_token(6);
		ZExp zexp1 = SQLSimpleExpression();
		ZExpression zexpression;
		if (flag) zexpression = new ZExpression("NOT BETWEEN", zexp, zexp1);
		else zexpression = new ZExpression("BETWEEN", zexp, zexp1);
		return zexpression;
	}

	public final ZExpression SQLLikeClause() throws ParseException {
		boolean flag = false;
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 45: // '-'
			jj_consume_token(45);
			flag = true;
			break;

		default:
			jj_la1[82] = jj_gen;
			break;
		}
		jj_consume_token(38);
		ZExp zexp = SQLSimpleExpression();
		ZExpression zexpression;
		if (flag) zexpression = new ZExpression("NOT LIKE", zexp);
		else zexpression = new ZExpression("LIKE", zexp);
		return zexpression;
	}

	public final ZExpression IsNullClause() throws ParseException {
		boolean flag = false;
		jj_consume_token(37);
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 45: // '-'
			jj_consume_token(45);
			flag = true;
			break;

		default:
			jj_la1[83] = jj_gen;
			break;
		}
		jj_consume_token(47);
		return flag ? new ZExpression("IS NOT NULL") : new ZExpression("IS NULL");
	}

	public final ZExp SQLSimpleExpression() throws ParseException {
		// Object obj1 = null;
		Object obj = SQLMultiplicativeExpression();
		label0: do
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			default:
				jj_la1[84] = jj_gen;
				break label0;

			case 101: // 'e'
			case 102: // 'f'
			case 106: // 'j'
				Token token1;
				switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
				case 101: // 'e'
					token1 = jj_consume_token(101);
					break;

				case 102: // 'f'
					token1 = jj_consume_token(102);
					break;

				case 106: // 'j'
					token1 = jj_consume_token(106);
					break;

				default:
					jj_la1[85] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				ZExp zexp = SQLMultiplicativeExpression();
				ZExpression zexpression = new ZExpression(token1.toString(), ((ZExp) (obj)));
				zexpression.addOperand(zexp);
				obj = zexpression;
				break;
			}
		while (true);
		return ((ZExp) (obj));
	}

	public final ZExp SQLMultiplicativeExpression() throws ParseException {
		// Object obj1 = null;
		Object obj = SQLExpotentExpression();
		label0: do
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			default:
				jj_la1[86] = jj_gen;
				break label0;

			case 103: // 'g'
			case 107: // 'k'
				Token token1;
				switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
				case 103: // 'g'
					token1 = jj_consume_token(103);
					break;

				case 107: // 'k'
					token1 = jj_consume_token(107);
					break;

				default:
					jj_la1[87] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				ZExp zexp = SQLExpotentExpression();
				ZExpression zexpression = new ZExpression(token1.toString(), ((ZExp) (obj)));
				zexpression.addOperand(zexp);
				obj = zexpression;
				break;
			}
		while (true);
		return ((ZExp) (obj));
	}

	public final ZExp SQLExpotentExpression() throws ParseException {
		ZExpression zexpression = null;
		boolean flag = true;
		ZExp zexp = SQLUnaryExpression();
		label0: do
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			default:
				jj_la1[88] = jj_gen;
				break label0;

			case 108: // 'l'
				Token token1 = jj_consume_token(108);
				ZExp zexp1 = SQLUnaryExpression();
				if (flag) zexpression = new ZExpression(token1.toString(), zexp);
				flag = false;
				zexpression.addOperand(zexp1);
				break;
			}
		while (true);
		return ((flag ? zexp : zexpression));
	}

	public final ZExp SQLUnaryExpression() throws ParseException {
		Token token1 = null;
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 101: // 'e'
		case 102: // 'f'
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			case 101: // 'e'
				token1 = jj_consume_token(101);
				break;

			case 102: // 'f'
				token1 = jj_consume_token(102);
				break;

			default:
				jj_la1[89] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			break;

		default:
			jj_la1[90] = jj_gen;
			break;
		}
		ZExp zexp = SQLPrimaryExpression();
		Object obj;
		if (token1 == null) obj = zexp;
		else obj = new ZExpression(token1.toString(), zexp);
		return ((ZExp) (obj));
	}

	public final ZExp SQLPrimaryExpression() throws ParseException {
		String s4 = "";
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 47: // '/'
			jj_consume_token(47);
			return new ZConstant("NULL", 1);
		}
		jj_la1[93] = jj_gen;
		if (jj_2_12(0x7fffffff)) {
			OuterJoinExpression();
			return new ZExpression("_NOT_SUPPORTED");
		}
		if (jj_2_13(3)) {
			jj_consume_token(19);
			jj_consume_token(88);
			jj_consume_token(103);
			jj_consume_token(90);
			return new ZExpression("COUNT", new ZConstant("*", 0));
		}
		if (jj_2_14(2)) {
			String s = AggregateFunc();
			jj_consume_token(88);
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			case 5: // '\005'
			case 23: // '\027'
				switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
				case 5: // '\005'
					jj_consume_token(5);
					s4 = "all ";
					break;

				case 23: // '\027'
					jj_consume_token(23);
					s4 = "distinct ";
					break;

				default:
					jj_la1[91] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				break;

			default:
				jj_la1[92] = jj_gen;
				break;
			}
			String s3 = TableColumn();
			jj_consume_token(90);
			return new ZExpression(s, new ZConstant(s4 + s3, 0));
		}
		if (jj_2_15(0x7fffffff)) {
			ZExpression zexpression = FunctionCall();
			return zexpression;
		}
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 82: // 'R'
		case 87: // 'W'
			String s1 = TableColumn();
			return new ZConstant(s1, 0);

		case 76: // 'L'
			Token token1 = jj_consume_token(76);
			return new ZConstant(token1.toString(), 2);

		case 86: // 'V'
			Token token2 = jj_consume_token(86);
			String s2 = token2.toString();
			if (s2.startsWith("'")) s2 = s2.substring(1);
			if (s2.endsWith("'")) s2 = s2.substring(0, s2.length() - 1);
			return new ZConstant(s2, 3);

		case 85: // 'U'
			Token token3 = jj_consume_token(85);
			return new ZConstant(token3.toString(), 3);

		case 88: // 'X'
			jj_consume_token(88);
			ZExp zexp = SQLExpression();
			jj_consume_token(90);
			return zexp;

		case 77: // 'M'
		case 78: // 'N'
		case 79: // 'O'
		case 80: // 'P'
		case 81: // 'Q'
		case 83: // 'S'
		case 84: // 'T'
		default:
			jj_la1[94] = jj_gen;
			jj_consume_token(-1);
			throw new ParseException();
		}
	}

	public final String AggregateFunc() throws ParseException {
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 64: // '@'
			Token token1 = jj_consume_token(64);
			return token1.toString();

		case 10: // '\n'
			Token token2 = jj_consume_token(10);
			return token2.toString();

		case 40: // '('
			Token token3 = jj_consume_token(40);
			return token3.toString();

		case 41: // ')'
			Token token4 = jj_consume_token(41);
			return token4.toString();

		case 19: // '\023'
			Token token5 = jj_consume_token(19);
			return token5.toString();
		}
		jj_la1[95] = jj_gen;
		jj_consume_token(-1);
		throw new ParseException();
	}

	public final ZExpression FunctionCall() throws ParseException {
		Token token1 = jj_consume_token(82);
		jj_consume_token(88);
		Vector vector = SQLExpressionList();
		jj_consume_token(90);
		int i = ZUtils.isCustomFunction(token1.toString());
		if (i <= 0) throw new ParseException("Undefined function: " + token1.toString());
		if (false && vector.size() != i) {
			throw new ParseException("Function " + token1.toString() + " should have " + i + " parameter(s)");
		}
		// else {
		ZExpression zexpression = new ZExpression(token1.toString());
		zexpression.setOperands(vector);
		return zexpression;
		// }
	}

	public final void OuterJoinExpression() throws ParseException {
		OracleObjectName();
		switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
		case 93: // ']'
			jj_consume_token(93);
			OracleObjectName();
			switch (jj_ntk != -1 ? jj_ntk : jj_ntk()) {
			case 93: // ']'
				jj_consume_token(93);
				OracleObjectName();
				break;

			default:
				jj_la1[96] = jj_gen;
				break;
			}
			break;

		default:
			jj_la1[97] = jj_gen;
			break;
		}
		jj_consume_token(88);
		jj_consume_token(101);
		jj_consume_token(90);
	}

	public final ZQuery SubQuery() throws ParseException {
		ZQuery zquery = SelectWithoutOrder();
		return zquery;
	}

	private final boolean jj_2_1(int i) {
		jj_la = i;
		jj_lastpos = jj_scanpos = token;
		boolean flag = !jj_3_1();
		jj_save(0, i);
		return flag;
	}

	private final boolean jj_2_2(int i) {
		jj_la = i;
		jj_lastpos = jj_scanpos = token;
		boolean flag = !jj_3_2();
		jj_save(1, i);
		return flag;
	}

	private final boolean jj_2_3(int i) {
		jj_la = i;
		jj_lastpos = jj_scanpos = token;
		boolean flag = !jj_3_3();
		jj_save(2, i);
		return flag;
	}

	private final boolean jj_2_4(int i) {
		jj_la = i;
		jj_lastpos = jj_scanpos = token;
		boolean flag = !jj_3_4();
		jj_save(3, i);
		return flag;
	}

	private final boolean jj_2_5(int i) {
		jj_la = i;
		jj_lastpos = jj_scanpos = token;
		boolean flag = !jj_3_5();
		jj_save(4, i);
		return flag;
	}

	private final boolean jj_2_6(int i) {
		jj_la = i;
		jj_lastpos = jj_scanpos = token;
		boolean flag = !jj_3_6();
		jj_save(5, i);
		return flag;
	}

	private final boolean jj_2_7(int i) {
		jj_la = i;
		jj_lastpos = jj_scanpos = token;
		boolean flag = !jj_3_7();
		jj_save(6, i);
		return flag;
	}

	private final boolean jj_2_8(int i) {
		jj_la = i;
		jj_lastpos = jj_scanpos = token;
		boolean flag = !jj_3_8();
		jj_save(7, i);
		return flag;
	}

	private final boolean jj_2_9(int i) {
		jj_la = i;
		jj_lastpos = jj_scanpos = token;
		boolean flag = !jj_3_9();
		jj_save(8, i);
		return flag;
	}

	private final boolean jj_2_10(int i) {
		jj_la = i;
		jj_lastpos = jj_scanpos = token;
		boolean flag = !jj_3_10();
		jj_save(9, i);
		return flag;
	}

	private final boolean jj_2_11(int i) {
		jj_la = i;
		jj_lastpos = jj_scanpos = token;
		boolean flag = !jj_3_11();
		jj_save(10, i);
		return flag;
	}

	private final boolean jj_2_12(int i) {
		jj_la = i;
		jj_lastpos = jj_scanpos = token;
		boolean flag = !jj_3_12();
		jj_save(11, i);
		return flag;
	}

	private final boolean jj_2_13(int i) {
		jj_la = i;
		jj_lastpos = jj_scanpos = token;
		boolean flag = !jj_3_13();
		jj_save(12, i);
		return flag;
	}

	private final boolean jj_2_14(int i) {
		jj_la = i;
		jj_lastpos = jj_scanpos = token;
		boolean flag = !jj_3_14();
		jj_save(13, i);
		return flag;
	}

	private final boolean jj_2_15(int i) {
		jj_la = i;
		jj_lastpos = jj_scanpos = token;
		boolean flag = !jj_3_15();
		jj_save(14, i);
		return flag;
	}

	private final boolean jj_3_7() {
		if (jj_scan_token(88)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_20()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(89)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_91() {
		if (jj_scan_token(53)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_88() {
		Token token1 = jj_scanpos;
		if (jj_3R_91()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_20()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_87() {
		if (jj_scan_token(88)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_72()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(90)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_85() {
		Token token1 = jj_scanpos;
		if (jj_3R_87()) {
			jj_scanpos = token1;
			if (jj_3R_88()) return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		token1 = jj_scanpos;
		if (jj_3R_89()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3R_16() {
		if (jj_scan_token(88)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3_1() {
		if (jj_3R_16()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		do {
			Token token1 = jj_scanpos;
			if (jj_3R_16()) {
				jj_scanpos = token1;
				break;
			}
			if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		while (true);
		if (jj_scan_token(59)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_31() {
		if (jj_scan_token(45)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_19() {
		Token token1 = jj_scanpos;
		if (jj_3R_31()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(25)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(88)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_86()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(90)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_84() {
		if (jj_scan_token(45)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_82() {
		Token token1 = jj_scanpos;
		if (jj_3R_84()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_85()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3_6() {
		if (jj_3R_19()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_78() {
		Token token1 = jj_scanpos;
		if (jj_3_6()) {
			jj_scanpos = token1;
			if (jj_3R_82()) return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3R_48() {
		if (jj_scan_token(93)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_18()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_79() {
		if (jj_scan_token(6)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_78()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_73() {
		if (jj_3R_78()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		do {
			Token token1 = jj_scanpos;
			if (jj_3R_79()) {
				jj_scanpos = token1;
				break;
			}
			if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		while (true);
		return false;
	}

	private final boolean jj_3R_128() {
		if (jj_scan_token(42)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_37() {
		if (jj_scan_token(93)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_18()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		Token token1 = jj_scanpos;
		if (jj_3R_48()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3R_74() {
		if (jj_scan_token(51)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_73()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_68() {
		if (jj_3R_73()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		do {
			Token token1 = jj_scanpos;
			if (jj_3R_74()) {
				jj_scanpos = token1;
				break;
			}
			if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		while (true);
		return false;
	}

	private final boolean jj_3R_86() {
		if (jj_3R_90()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_127() {
		if (jj_scan_token(35)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_27() {
		if (jj_3R_18()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		Token token1 = jj_scanpos;
		if (jj_3R_37()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(88)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(101)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(90)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_144() {
		if (jj_scan_token(5)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3_5() {
		if (jj_scan_token(88)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_130() {
		if (jj_3R_90()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_129() {
		if (jj_scan_token(88)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_90()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(90)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_67() {
		if (jj_scan_token(82)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(88)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_72()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(90)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_126() {
		if (jj_scan_token(67)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		Token token1 = jj_scanpos;
		if (jj_3R_144()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3R_112() {
		Token token1 = jj_scanpos;
		if (jj_3R_126()) {
			jj_scanpos = token1;
			if (jj_3R_127()) {
				jj_scanpos = token1;
				if (jj_3R_128()) return true;
				if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
			}
			else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		token1 = jj_scanpos;
		if (jj_3R_129()) {
			jj_scanpos = token1;
			if (jj_3R_130()) return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3R_70() {
		if (jj_scan_token(23)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_42() {
		if (jj_scan_token(19)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_41() {
		if (jj_scan_token(41)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_40() {
		if (jj_scan_token(40)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_39() {
		if (jj_scan_token(10)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_125() {
		if (jj_scan_token(31)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_68()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_38() {
		if (jj_scan_token(64)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_28() {
		Token token1 = jj_scanpos;
		if (jj_3R_38()) {
			jj_scanpos = token1;
			if (jj_3R_39()) {
				jj_scanpos = token1;
				if (jj_3R_40()) {
					jj_scanpos = token1;
					if (jj_3R_41()) {
						jj_scanpos = token1;
						if (jj_3R_42()) return true;
						if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
					}
					else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
				}
				else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
			}
			else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3R_111() {
		if (jj_scan_token(30)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(14)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_72()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		Token token1 = jj_scanpos;
		if (jj_3R_125()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3_15() {
		if (jj_scan_token(82)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(88)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_124() {
		if (jj_scan_token(63)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(73)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_68()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_64() {
		if (jj_scan_token(88)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_68()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(90)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_123() {
		if (jj_scan_token(63)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(73)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_68()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_63() {
		if (jj_scan_token(85)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_110() {
		Token token1 = jj_scanpos;
		if (jj_3R_123()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(18)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(14)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_68()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		token1 = jj_scanpos;
		if (jj_3R_124()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3R_62() {
		if (jj_scan_token(86)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_61() {
		if (jj_scan_token(76)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_60() {
		if (jj_3R_66()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_69() {
		if (jj_scan_token(5)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_65() {
		Token token1 = jj_scanpos;
		if (jj_3R_69()) {
			jj_scanpos = token1;
			if (jj_3R_70()) return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3R_59() {
		if (jj_3R_67()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3_12() {
		if (jj_3R_27()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_109() {
		if (jj_scan_token(72)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_68()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3_14() {
		if (jj_3R_28()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(88)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		Token token1 = jj_scanpos;
		if (jj_3R_65()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_66()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(90)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3_13() {
		if (jj_scan_token(19)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(88)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(103)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(90)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_58() {
		if (jj_3R_27()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_122() {
		if (jj_scan_token(82)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_57() {
		if (jj_scan_token(47)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_54() {
		Token token1 = jj_scanpos;
		if (jj_3R_57()) {
			jj_scanpos = token1;
			if (jj_3R_58()) {
				jj_scanpos = token1;
				if (jj_3_13()) {
					jj_scanpos = token1;
					if (jj_3_14()) {
						jj_scanpos = token1;
						if (jj_3R_59()) {
							jj_scanpos = token1;
							if (jj_3R_60()) {
								jj_scanpos = token1;
								if (jj_3R_61()) {
									jj_scanpos = token1;
									if (jj_3R_62()) {
										jj_scanpos = token1;
										if (jj_3R_63()) {
											jj_scanpos = token1;
											if (jj_3R_64()) return true;
											if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
										}
										else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
									}
									else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
								}
								else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
							}
							else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
						}
						else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
					}
					else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
				}
				else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
			}
			else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3R_107() {
		if (jj_3R_121()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		Token token1 = jj_scanpos;
		if (jj_3R_122()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3R_108() {
		if (jj_scan_token(89)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_107()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_56() {
		if (jj_scan_token(102)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_96() {
		if (jj_scan_token(29)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_107()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		do {
			Token token1 = jj_scanpos;
			if (jj_3R_108()) {
				jj_scanpos = token1;
				break;
			}
			if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		while (true);
		return false;
	}

	private final boolean jj_3R_55() {
		if (jj_scan_token(101)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_53() {
		Token token1 = jj_scanpos;
		if (jj_3R_55()) {
			jj_scanpos = token1;
			if (jj_3R_56()) return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3R_49() {
		Token token1 = jj_scanpos;
		if (jj_3R_53()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_54()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3_4() {
		if (jj_3R_18()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(93)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_18()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(104)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_17() {
		Token token1 = jj_scanpos;
		if (jj_3_3()) {
			jj_scanpos = token1;
			if (jj_3_4()) return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3_3() {
		if (jj_3R_18()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(104)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_150() {
		if (jj_scan_token(82)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_50() {
		if (jj_scan_token(108)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_49()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_149() {
		if (jj_scan_token(8)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_43() {
		if (jj_3R_49()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		do {
			Token token1 = jj_scanpos;
			if (jj_3R_50()) {
				jj_scanpos = token1;
				break;
			}
			if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		while (true);
		return false;
	}

	private final boolean jj_3R_148() {
		Token token1 = jj_scanpos;
		if (jj_3R_149()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_150()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		do {
			Token token2 = jj_scanpos;
			if (jj_3R_150()) {
				jj_scanpos = token2;
				break;
			}
			if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		while (true);
		return false;
	}

	private final boolean jj_3R_52() {
		if (jj_scan_token(107)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_147() {
		if (jj_3R_148()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3_2() {
		if (jj_3R_17()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_47() {
		if (jj_scan_token(106)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_51() {
		if (jj_scan_token(103)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_44() {
		Token token1 = jj_scanpos;
		if (jj_3R_51()) {
			jj_scanpos = token1;
			if (jj_3R_52()) return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_43()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_142() {
		if (jj_3R_20()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		Token token1 = jj_scanpos;
		if (jj_3R_147()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3R_32() {
		if (jj_3R_43()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		do {
			Token token1 = jj_scanpos;
			if (jj_3R_44()) {
				jj_scanpos = token1;
				break;
			}
			if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		while (true);
		return false;
	}

	private final boolean jj_3R_119() {
		Token token1 = jj_scanpos;
		if (jj_3R_141()) {
			jj_scanpos = token1;
			if (jj_3R_142()) return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3R_141() {
		if (jj_3R_17()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_46() {
		if (jj_scan_token(102)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_45() {
		if (jj_scan_token(101)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_120() {
		if (jj_scan_token(89)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_119()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_33() {
		Token token1 = jj_scanpos;
		if (jj_3R_45()) {
			jj_scanpos = token1;
			if (jj_3R_46()) {
				jj_scanpos = token1;
				if (jj_3R_47()) return true;
				if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
			}
			else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_32()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_106() {
		if (jj_3R_119()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		do {
			Token token1 = jj_scanpos;
			if (jj_3R_120()) {
				jj_scanpos = token1;
				break;
			}
			if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		while (true);
		return false;
	}

	private final boolean jj_3R_20() {
		if (jj_3R_32()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		do {
			Token token1 = jj_scanpos;
			if (jj_3R_33()) {
				jj_scanpos = token1;
				break;
			}
			if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		while (true);
		return false;
	}

	private final boolean jj_3R_95() {
		Token token1 = jj_scanpos;
		if (jj_3R_105()) {
			jj_scanpos = token1;
			if (jj_3R_106()) return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3R_105() {
		if (jj_scan_token(103)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_118() {
		if (jj_scan_token(45)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_104() {
		if (jj_scan_token(23)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_102() {
		if (jj_scan_token(37)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		Token token1 = jj_scanpos;
		if (jj_3R_118()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(47)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_94() {
		Token token1 = jj_scanpos;
		if (jj_3R_103()) {
			jj_scanpos = token1;
			if (jj_3R_104()) return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3R_103() {
		if (jj_scan_token(5)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_100() {
		if (jj_3R_112()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_99() {
		if (jj_3R_111()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_98() {
		if (jj_3R_110()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_97() {
		if (jj_3R_109()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_36() {
		if (jj_scan_token(45)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_23() {
		Token token1 = jj_scanpos;
		if (jj_3R_36()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(38)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_20()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_90() {
		if (jj_scan_token(59)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		Token token1 = jj_scanpos;
		if (jj_3R_94()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_95()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_96()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		token1 = jj_scanpos;
		if (jj_3R_97()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		token1 = jj_scanpos;
		if (jj_3R_98()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		token1 = jj_scanpos;
		if (jj_3R_99()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		token1 = jj_scanpos;
		if (jj_3R_100()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3R_35() {
		if (jj_scan_token(45)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_22() {
		Token token1 = jj_scanpos;
		if (jj_3R_35()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(11)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_20()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(6)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_20()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_30() {
		if (jj_scan_token(87)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_116() {
		if (jj_3R_72()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_117() {
		if (jj_3R_86()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_34() {
		if (jj_scan_token(45)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_21() {
		Token token1 = jj_scanpos;
		if (jj_3R_34()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(32)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(88)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		token1 = jj_scanpos;
		if (jj_3R_116()) {
			jj_scanpos = token1;
			if (jj_3R_117()) return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(90)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_146() {
		if (jj_scan_token(7)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_143() {
		if (jj_scan_token(93)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_18()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_26() {
		if (jj_scan_token(88)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(59)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_121() {
		if (jj_3R_18()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		Token token1 = jj_scanpos;
		if (jj_3R_143()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3R_83() {
		if (jj_scan_token(105)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_25() {
		if (jj_scan_token(5)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_81() {
		if (jj_3R_83()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_76() {
		Token token1 = jj_scanpos;
		if (jj_3R_80()) {
			jj_scanpos = token1;
			if (jj_3R_81()) return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3R_80() {
		if (jj_3R_20()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_138() {
		if (jj_scan_token(100)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_137() {
		if (jj_scan_token(99)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_136() {
		if (jj_scan_token(98)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_135() {
		if (jj_scan_token(97)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_134() {
		if (jj_scan_token(96)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3_11() {
		Token token1 = jj_scanpos;
		if (jj_3R_24()) {
			jj_scanpos = token1;
			if (jj_3R_25()) {
				jj_scanpos = token1;
				if (jj_3R_26()) return true;
				if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
			}
			else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3R_24() {
		if (jj_scan_token(7)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_133() {
		if (jj_scan_token(95)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_140() {
		if (jj_scan_token(53)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_132() {
		if (jj_scan_token(94)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_115() {
		Token token1 = jj_scanpos;
		if (jj_3R_140()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_76()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_113() {
		Token token1 = jj_scanpos;
		if (jj_3R_131()) {
			jj_scanpos = token1;
			if (jj_3R_132()) {
				jj_scanpos = token1;
				if (jj_3R_133()) {
					jj_scanpos = token1;
					if (jj_3R_134()) {
						jj_scanpos = token1;
						if (jj_3R_135()) {
							jj_scanpos = token1;
							if (jj_3R_136()) {
								jj_scanpos = token1;
								if (jj_3R_137()) {
									jj_scanpos = token1;
									if (jj_3R_138()) return true;
									if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
								}
								else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
							}
							else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
						}
						else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
					}
					else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
				}
				else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
			}
			else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3R_131() {
		if (jj_scan_token(92)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_145() {
		if (jj_scan_token(5)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_139() {
		Token token1 = jj_scanpos;
		if (jj_3R_145()) {
			jj_scanpos = token1;
			if (jj_3R_146()) return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3R_18() {
		Token token1 = jj_scanpos;
		if (jj_3R_29()) {
			jj_scanpos = token1;
			if (jj_3R_30()) return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3R_29() {
		if (jj_scan_token(82)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_114() {
		Token token1 = jj_scanpos;
		if (jj_3R_139()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(88)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_86()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_scan_token(90)) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_75() {
		if (jj_scan_token(93)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_18()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_71() {
		if (jj_scan_token(93)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_18()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		Token token1 = jj_scanpos;
		if (jj_3R_75()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3R_101() {
		if (jj_3R_113()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		Token token1 = jj_scanpos;
		if (jj_3R_114()) {
			jj_scanpos = token1;
			if (jj_3R_115()) return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3R_66() {
		if (jj_3R_18()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		Token token1 = jj_scanpos;
		if (jj_3R_71()) jj_scanpos = token1;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3R_77() {
		if (jj_scan_token(89)) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		if (jj_3R_76()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_72() {
		if (jj_3R_76()) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		do {
			Token token1 = jj_scanpos;
			if (jj_3R_77()) {
				jj_scanpos = token1;
				break;
			}
			if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		while (true);
		return false;
	}

	private final boolean jj_3R_93() {
		if (jj_3R_102()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3_10() {
		if (jj_3R_23()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3_9() {
		if (jj_3R_22()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3_8() {
		if (jj_3R_21()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	private final boolean jj_3R_89() {
		Token token1 = jj_scanpos;
		if (jj_3R_92()) {
			jj_scanpos = token1;
			if (jj_3_8()) {
				jj_scanpos = token1;
				if (jj_3_9()) {
					jj_scanpos = token1;
					if (jj_3_10()) {
						jj_scanpos = token1;
						if (jj_3R_93()) return true;
						if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
					}
					else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
				}
				else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
			}
			else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		}
		else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
		return false;
	}

	private final boolean jj_3R_92() {
		if (jj_3R_101()) return true;
		return jj_la != 0 || jj_scanpos != jj_lastpos ? false : false;
	}

	public ZqlJJParser(InputStream inputstream) {
		lookingAhead = false;
		jj_la1 = new int[98];
		jj_2_rtns = new JJCalls[15];
		jj_rescan = false;
		jj_gc = 0;
		jj_expentries = new Vector();
		jj_kind = -1;
		jj_lasttokens = new int[100];
		jj_input_stream = new SimpleCharStream(inputstream, 1, 1);
		token_source = new ZqlJJParserTokenManager(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 98; i++)
			jj_la1[i] = -1;

		for (int j = 0; j < jj_2_rtns.length; j++)
			jj_2_rtns[j] = new JJCalls();

	}

	public void ReInit(InputStream inputstream) {
		jj_input_stream.ReInit(inputstream, 1, 1);
		token_source.ReInit(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 98; i++)
			jj_la1[i] = -1;

		for (int j = 0; j < jj_2_rtns.length; j++)
			jj_2_rtns[j] = new JJCalls();

	}

	public ZqlJJParser(Reader reader) {
		lookingAhead = false;
		jj_la1 = new int[98];
		jj_2_rtns = new JJCalls[15];
		jj_rescan = false;
		jj_gc = 0;
		jj_expentries = new Vector();
		jj_kind = -1;
		jj_lasttokens = new int[100];
		jj_input_stream = new SimpleCharStream(reader, 1, 1);
		token_source = new ZqlJJParserTokenManager(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 98; i++)
			jj_la1[i] = -1;

		for (int j = 0; j < jj_2_rtns.length; j++)
			jj_2_rtns[j] = new JJCalls();

	}

	public void ReInit(Reader reader) {
		jj_input_stream.ReInit(reader, 1, 1);
		token_source.ReInit(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 98; i++)
			jj_la1[i] = -1;

		for (int j = 0; j < jj_2_rtns.length; j++)
			jj_2_rtns[j] = new JJCalls();

	}

	public ZqlJJParser(ZqlJJParserTokenManager zqljjparsertokenmanager) {
		lookingAhead = false;
		jj_la1 = new int[98];
		jj_2_rtns = new JJCalls[15];
		jj_rescan = false;
		jj_gc = 0;
		jj_expentries = new Vector();
		jj_kind = -1;
		jj_lasttokens = new int[100];
		token_source = zqljjparsertokenmanager;
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 98; i++)
			jj_la1[i] = -1;

		for (int j = 0; j < jj_2_rtns.length; j++)
			jj_2_rtns[j] = new JJCalls();

	}

	public void ReInit(ZqlJJParserTokenManager zqljjparsertokenmanager) {
		token_source = zqljjparsertokenmanager;
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 98; i++)
			jj_la1[i] = -1;

		for (int j = 0; j < jj_2_rtns.length; j++)
			jj_2_rtns[j] = new JJCalls();

	}

	private final Token jj_consume_token(int i) throws ParseException {

		// System. out.println("char:"+((char)i));
		Token token1;
		if ((token1 = token).next != null) token = token.next;
		else token = token.next = token_source.getNextToken();
		jj_ntk = -1;
		// print.out("img:"+token.image+"+"+token.kind);
		if (token.kind == i) {
			jj_gen++;
			if (++jj_gc > 100) {
				jj_gc = 0;
				for (int j = 0; j < jj_2_rtns.length; j++) {
					for (JJCalls jjcalls = jj_2_rtns[j]; jjcalls != null; jjcalls = jjcalls.next)
						if (jjcalls.gen < jj_gen) jjcalls.first = null;

				}

			}
			return token;
		}
		// else{
		token = token1;
		jj_kind = i;
		throw generateParseException();
		// }
	}

	private final boolean jj_scan_token(int i) {
		if (jj_scanpos == jj_lastpos) {
			jj_la--;
			if (jj_scanpos.next == null) jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
			else jj_lastpos = jj_scanpos = jj_scanpos.next;
		}
		else {
			jj_scanpos = jj_scanpos.next;
		}
		if (jj_rescan) {
			int j = 0;
			Token token1;
			for (token1 = token; token1 != null && token1 != jj_scanpos; token1 = token1.next)
				j++;

			if (token1 != null) jj_add_error_token(i, j);
		}
		return jj_scanpos.kind != i;
	}

	public final Token getNextToken() {
		if (token.next != null) token = token.next;
		else token = token.next = token_source.getNextToken();
		jj_ntk = -1;
		jj_gen++;
		return token;
	}

	public final Token getToken(int i) {
		Token token1 = lookingAhead ? jj_scanpos : token;
		for (int j = 0; j < i; j++)
			if (token1.next != null) token1 = token1.next;
			else token1 = token1.next = token_source.getNextToken();

		return token1;
	}

	private final int jj_ntk() {
		if ((jj_nt = token.next) == null) {
			token.next = token_source.getNextToken();
			return jj_ntk = (token.next).kind;
		}
		return jj_ntk = jj_nt.kind;
	}

	private void jj_add_error_token(int i, int j) {
		if (j >= 100) return;
		if (j == jj_endpos + 1) jj_lasttokens[jj_endpos++] = i;
		else if (jj_endpos != 0) {
			jj_expentry = new int[jj_endpos];
			for (int k = 0; k < jj_endpos; k++)
				jj_expentry[k] = jj_lasttokens[k];

			boolean flag = false;
			Enumeration enumeration = jj_expentries.elements();
			while (enumeration.hasMoreElements()) {
				int ai[] = (int[]) enumeration.nextElement();
				if (ai.length != jj_expentry.length) continue;
				flag = true;
				for (int l = 0; l < jj_expentry.length; l++) {
					if (ai[l] == jj_expentry[l]) continue;
					flag = false;
					break;
				}

				if (flag) break;
			}
			if (!flag) jj_expentries.addElement(jj_expentry);
			if (j != 0) jj_lasttokens[(jj_endpos = j) - 1] = i;
		}
	}

	public final ParseException generateParseException() {
		jj_expentries.removeAllElements();
		boolean aflag[] = new boolean[109];
		for (int i = 0; i < 109; i++)
			aflag[i] = false;

		if (jj_kind >= 0) {
			aflag[jj_kind] = true;
			jj_kind = -1;
		}
		for (int j = 0; j < 98; j++)
			if (jj_la1[j] == jj_gen) {
				for (int k = 0; k < 32; k++) {
					if ((jj_la1_0[j] & 1 << k) != 0) aflag[k] = true;
					if ((jj_la1_1[j] & 1 << k) != 0) aflag[32 + k] = true;
					if ((jj_la1_2[j] & 1 << k) != 0) aflag[64 + k] = true;
					if ((jj_la1_3[j] & 1 << k) != 0) aflag[96 + k] = true;
				}

			}

		for (int l = 0; l < 109; l++)
			if (aflag[l]) {
				jj_expentry = new int[1];
				jj_expentry[0] = l;
				jj_expentries.addElement(jj_expentry);
			}

		jj_endpos = 0;
		jj_rescan_token();
		jj_add_error_token(0, 0);
		int ai[][] = new int[jj_expentries.size()][];
		for (int i1 = 0; i1 < jj_expentries.size(); i1++)
			ai[i1] = (int[]) jj_expentries.elementAt(i1);

		return new ParseException(token, ai, ZqlJJParserConstants.tokenImage);
	}

	public final void enable_tracing() {
	}

	public final void disable_tracing() {
	}

	private final void jj_rescan_token() {
		jj_rescan = true;
		for (int i = 0; i < 15; i++) {
			JJCalls jjcalls = jj_2_rtns[i];
			do {
				if (jjcalls.gen > jj_gen) {
					jj_la = jjcalls.arg;
					jj_lastpos = jj_scanpos = jjcalls.first;
					switch (i) {
					case 0: // '\0'
						jj_3_1();
						break;

					case 1: // '\001'
						jj_3_2();
						break;

					case 2: // '\002'
						jj_3_3();
						break;

					case 3: // '\003'
						jj_3_4();
						break;

					case 4: // '\004'
						jj_3_5();
						break;

					case 5: // '\005'
						jj_3_6();
						break;

					case 6: // '\006'
						jj_3_7();
						break;

					case 7: // '\007'
						jj_3_8();
						break;

					case 8: // '\b'
						jj_3_9();
						break;

					case 9: // '\t'
						jj_3_10();
						break;

					case 10: // '\n'
						jj_3_11();
						break;

					case 11: // '\013'
						jj_3_12();
						break;

					case 12: // '\f'
						jj_3_13();
						break;

					case 13: // '\r'
						jj_3_14();
						break;

					case 14: // '\016'
						jj_3_15();
						break;
					}
				}
				jjcalls = jjcalls.next;
			}
			while (jjcalls != null);
		}

		jj_rescan = false;
	}

	private final void jj_save(int i, int j) {
		JJCalls jjcalls;
		for (jjcalls = jj_2_rtns[i]; jjcalls.gen > jj_gen; jjcalls = jjcalls.next) {
			if (jjcalls.next != null) continue;
			jjcalls = jjcalls.next = new JJCalls();
			break;
		}

		jjcalls.gen = (jj_gen + j) - jj_la;
		jjcalls.first = token;
		jjcalls.arg = j;
	}

	public ZqlJJParserTokenManager token_source;
	SimpleCharStream jj_input_stream;
	public Token token;
	public Token jj_nt;
	private int jj_ntk;
	private Token jj_scanpos;
	private Token jj_lastpos;
	private int jj_la;
	public boolean lookingAhead;
	// private boolean jj_semLA;
	private int jj_gen;
	private final int jj_la1[];
	private final int jj_la1_0[] = { 0x8008000, 0, 0, 0x810b000, 0x4220000, 0x4000000, 0x4220000, 0, 0x10000, 0, 0, 0, 0x10000, 0, 0x1000000, 0, 0, 0x1000000, 0, 0, 0x2080400, 0,
			0, 0, 0x20000000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0x10000000, 0x800020, 0x800020, 0, 0x40000, 0x40000000, 0, 0, 0x80400, 256, 0x80400, 256, 0, 0, 0, 0, 0, 0x80000000, 32,
			0, 0, 0x400200, 0x400200, 0, 0x400200, 0x400200, 0, 0, 0, 64, 0, 0x80400, 0, 0, 0x80400, 0, 0, 2048, 0, 160, 160, 0, 0x80400, 0x80400, 0, 0x80400, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0x800020, 0x800020, 0, 0, 0x80400, 0, 0 };
	private final int jj_la1_1[] = { 0x1011004, 0, 0, 0x1011004, 0x1a400082, 0x400000, 0x1a400082, 0, 0, 0, 16384, 0, 0, 0x40000, 0x20000000, 0x4000000, 0x4000000, 0x24000000, 0,
			0, 0x20a300, 0, 0, 0x8000000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0x100000, 0, 0, 0, 0, 0x80000000, 0, 1032, 0, 33536, 0, 33536, 0, 0, 0, 0, 0x80000000, 0x80000000, 0, 0,
			1032, 0x8000000, 0, 0, 0, 0, 0, 0, 0x20000, 0x80000, 0, 8192, 0x20a300, 8192, 0x200000, 0x208300, 0, 32, 8289, 0, 0, 0, 0x200000, 0x208300, 33536, 8192, 0x8008300,
			8192, 8192, 8192, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32768, 0, 768, 0, 0 };
	private final int jj_la1_2[] = { 192, 0x2000000, 0x1000000, 192, 16, 0, 16, 1024, 0, 0x2000000, 0, 1024, 0, 2048, 0, 16, 16, 0, 256, 0x2000000, 0x1e41001, 0x2000000, 0x1000000,
			32, 0, 256, 0x20000000, 0x20000000, 0x840000, 0xd0000000, 0x20000000, 0, 0, 0x41000, 0, 0, 0, 0, 256, 0, 0, 8, 0x2000000, 0x1e41001, 0x40000, 0x1e41001, 0, 0x40000,
			0x2000000, 0x40000, 0, 0, 0, 0, 8, 0, 0, 0, 0x2000000, 0, 0, 0x2000000, 0, 0, 0, 0, 0x1e41001, 0, 0, 0x1e41001, 0xd0000000, 0, 0xd0000000, 0x2000000, 0, 0, 0,
			0x1e41001, 0x1e41001, 0, 0x1e41001, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0x1e41000, 1, 0x20000000, 0x20000000 };
	private final int jj_la1_3[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 608, 0, 0, 0, 0, 0, 0, 0, 0, 31, 0, 96, 96, 96, 0, 0, 0, 0, 0, 0, 0, 0, 0, 224, 0,
			96, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 96, 0, 0, 96, 31, 0, 31, 0, 0, 0, 0, 608, 608, 0, 608, 0, 0, 0, 1120, 1120, 2176, 2176, 4096, 96, 96, 0,
			0, 0, 0, 0, 0, 0 };
	private final JJCalls jj_2_rtns[];
	private boolean jj_rescan;
	private int jj_gc;
	private Vector jj_expentries;
	private int jj_expentry[];
	private int jj_kind;
	private int jj_lasttokens[];
	private int jj_endpos;
}