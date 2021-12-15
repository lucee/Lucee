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

import java.sql.SQLException;
import java.util.Vector;

// Referenced classes of package Zql:
//            ZExpression, ZConstant, ZExp, ZTuple, 
//            ZqlParser

public final class ZEval {

	public ZEval() {
	}

	public boolean eval(ZTuple ztuple, ZExp zexp) throws SQLException {
		if (ztuple == null || zexp == null) throw new SQLException("ZEval.eval(): null argument or operator");
		if (!(zexp instanceof ZExpression)) throw new SQLException("ZEval.eval(): only expressions are supported");
		ZExpression zexpression = (ZExpression) zexp;
		String s = zexpression.getOperator();
		if (s.equals("AND")) {
			boolean flag = true;
			for (int i = 0; i < zexpression.nbOperands(); i++)
				flag &= eval(ztuple, zexpression.getOperand(i));

			return flag;
		}
		if (s.equals("OR")) {
			boolean flag1 = false;
			for (int j = 0; j < zexpression.nbOperands(); j++)
				flag1 |= eval(ztuple, zexpression.getOperand(j));

			return flag1;
		}
		if (s.equals("NOT")) return !eval(ztuple, zexpression.getOperand(0));
		if (s.equals("=")) return evalCmp(ztuple, zexpression.getOperands()) == 0.0D;
		if (s.equals("!=")) return evalCmp(ztuple, zexpression.getOperands()) != 0.0D;
		if (s.equals("<>")) return evalCmp(ztuple, zexpression.getOperands()) != 0.0D;
		if (s.equals("#")) throw new SQLException("ZEval.eval(): Operator # not supported");
		if (s.equals(">")) return evalCmp(ztuple, zexpression.getOperands()) > 0.0D;
		if (s.equals(">=")) return evalCmp(ztuple, zexpression.getOperands()) >= 0.0D;
		if (s.equals("<")) return evalCmp(ztuple, zexpression.getOperands()) < 0.0D;
		if (s.equals("<=")) return evalCmp(ztuple, zexpression.getOperands()) <= 0.0D;
		if (s.equals("BETWEEN") || s.equals("NOT BETWEEN")) {
			ZExpression zexpression1 = new ZExpression("AND", new ZExpression(">=", zexpression.getOperand(0), zexpression.getOperand(1)),
					new ZExpression("<=", zexpression.getOperand(0), zexpression.getOperand(2)));
			if (s.equals("NOT BETWEEN")) return !eval(ztuple, ((zexpression1)));
			return eval(ztuple, ((zexpression1)));
		}
		if (s.equals("LIKE") || s.equals("NOT LIKE")) throw new SQLException("ZEval.eval(): Operator (NOT) LIKE not supported");
		if (s.equals("IN") || s.equals("NOT IN")) {
			ZExpression zexpression2 = new ZExpression("OR");
			for (int k = 1; k < zexpression.nbOperands(); k++)
				zexpression2.addOperand(new ZExpression("=", zexpression.getOperand(0), zexpression.getOperand(k)));

			if (s.equals("NOT IN")) return !eval(ztuple, ((zexpression2)));
			return eval(ztuple, ((zexpression2)));
		}
		if (s.equals("IS NULL")) {
			if (zexpression.nbOperands() <= 0 || zexpression.getOperand(0) == null) return true;
			ZExp zexp1 = zexpression.getOperand(0);
			if (zexp1 instanceof ZConstant) return ((ZConstant) zexp1).getType() == 1;
			throw new SQLException("ZEval.eval(): can't eval IS (NOT) NULL");
		}
		if (s.equals("IS NOT NULL")) {
			ZExpression zexpression3 = new ZExpression("IS NULL");
			zexpression3.setOperands(zexpression.getOperands());
			return !eval(ztuple, ((zexpression3)));
		}
		throw new SQLException("ZEval.eval(): Unknown operator " + s);

	}

	double evalCmp(ZTuple ztuple, Vector vector) throws SQLException {
		if (vector.size() < 2) throw new SQLException("ZEval.evalCmp(): Trying to compare less than two values");
		if (vector.size() > 2) throw new SQLException("ZEval.evalCmp(): Trying to compare more than two values");
		Object obj = null;
		Object obj1 = null;
		obj = evalExpValue(ztuple, (ZExp) vector.elementAt(0));
		obj1 = evalExpValue(ztuple, (ZExp) vector.elementAt(1));
		if ((obj instanceof String) || (obj1 instanceof String)) return (obj.equals(obj1) ? 0 : -1);
		if ((obj instanceof Number) && (obj1 instanceof Number)) return ((Number) obj).doubleValue() - ((Number) obj1).doubleValue();
		throw new SQLException("ZEval.evalCmp(): can't compare (" + obj.toString() + ") with (" + obj1.toString() + ")");
	}

	double evalNumericExp(ZTuple ztuple, ZExpression zexpression) throws SQLException {
		if (ztuple == null || zexpression == null || zexpression.getOperator() == null) throw new SQLException("ZEval.eval(): null argument or operator");
		String s = zexpression.getOperator();
		Object obj = evalExpValue(ztuple, zexpression.getOperand(0));
		if (!(obj instanceof Double)) throw new SQLException("ZEval.evalNumericExp(): expression not numeric");
		Double double1 = (Double) obj;
		if (s.equals("+")) {
			double d = double1.doubleValue();
			for (int i = 1; i < zexpression.nbOperands(); i++) {
				Object obj1 = evalExpValue(ztuple, zexpression.getOperand(i));
				d += ((Number) obj1).doubleValue();
			}

			return d;
		}
		if (s.equals("-")) {
			double d1 = double1.doubleValue();
			if (zexpression.nbOperands() == 1) return -d1;
			for (int j = 1; j < zexpression.nbOperands(); j++) {
				Object obj2 = evalExpValue(ztuple, zexpression.getOperand(j));
				d1 -= ((Number) obj2).doubleValue();
			}

			return d1;
		}
		if (s.equals("*")) {
			double d2 = double1.doubleValue();
			for (int k = 1; k < zexpression.nbOperands(); k++) {
				Object obj3 = evalExpValue(ztuple, zexpression.getOperand(k));
				d2 *= ((Number) obj3).doubleValue();
			}

			return d2;
		}
		if (s.equals("/")) {
			double d3 = double1.doubleValue();
			for (int l = 1; l < zexpression.nbOperands(); l++) {
				Object obj4 = evalExpValue(ztuple, zexpression.getOperand(l));
				d3 /= ((Number) obj4).doubleValue();
			}

			return d3;
		}
		if (s.equals("**")) {
			double d4 = double1.doubleValue();
			for (int i1 = 1; i1 < zexpression.nbOperands(); i1++) {
				Object obj5 = evalExpValue(ztuple, zexpression.getOperand(i1));
				d4 = Math.pow(d4, ((Number) obj5).doubleValue());
			}

			return d4;
		}
		throw new SQLException("ZEval.evalNumericExp(): Unknown operator " + s);

	}

	public Object evalExpValue(ZTuple ztuple, ZExp zexp) throws SQLException {
		Object obj = null;
		if (zexp instanceof ZConstant) {
			ZConstant zconstant = (ZConstant) zexp;
			switch (zconstant.getType()) {
			case 0: // '\0'
				Object obj1 = ztuple.getAttValue(zconstant.getValue());
				if (obj1 == null) throw new SQLException("ZEval.evalExpValue(): unknown column " + zconstant.getValue());
				try {
					obj = new Double(obj1.toString());
				}
				catch (NumberFormatException numberformatexception) {
					obj = obj1;
				}
				break;

			case 2: // '\002'
				obj = new Double(zconstant.getValue());
				break;

			case 1: // '\001'
			case 3: // '\003'
			default:
				obj = zconstant.getValue();
				break;
			}
		}
		else if (zexp instanceof ZExpression) obj = new Double(evalNumericExp(ztuple, (ZExpression) zexp));
		return obj;
	}

	/*
	 * public static void main(String args[]) { try { BufferedReader bufferedreader = new
	 * BufferedReader(new FileReader("test.db")); String s = bufferedreader.readLine(); ZTuple ztuple =
	 * new ZTuple(s); ZqlParser zqlparser = new ZqlParser(); ZEval zeval = new ZEval(); while ((s =
	 * bufferedreader.readLine()) != null) { ztuple.setRow(s); BufferedReader bufferedreader1 = new
	 * BufferedReader(new FileReader("test.sql")); String s1; while ((s1 = bufferedreader1.readLine())
	 * != null) { zqlparser.initParser(new ByteArrayInputStream(s1.getBytes())); ZExp zexp =
	 * zqlparser.readExpression(); System.out.print(s + ", " + s1 + ", ");
	 * System.out.println(zeval.eval(ztuple, zexp)); } bufferedreader1.close(); }
	 * bufferedreader.close(); } catch (Exception exception) {
	 * 
	 * } }
	 */
}