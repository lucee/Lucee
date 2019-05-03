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

import java.util.Vector;

// Referenced classes of package Zql:
//            ZExp, ZQuery, ZConstant, ZUtils

public final class ZExpression implements ZExp {

	public ZExpression(String s) {
		op_ = null;
		operands_ = null;
		op_ = new String(s);
	}

	public ZExpression(String s, ZExp zexp) {
		op_ = null;
		operands_ = null;
		op_ = new String(s);
		addOperand(zexp);
	}

	public ZExpression(String s, ZExp zexp, ZExp zexp1) {
		op_ = null;
		operands_ = null;
		op_ = new String(s);
		addOperand(zexp);
		addOperand(zexp1);
	}

	public String getOperator() {
		return op_;
	}

	public void setOperands(Vector vector) {
		operands_ = vector;
	}

	public Vector getOperands() {
		return operands_;
	}

	public void addOperand(ZExp zexp) {
		if (operands_ == null) operands_ = new Vector();
		operands_.addElement(zexp);
	}

	public ZExp getOperand(int i) {
		if (operands_ == null || i >= operands_.size()) return null;
		return (ZExp) operands_.elementAt(i);
	}

	public int nbOperands() {
		if (operands_ == null) return 0;
		return operands_.size();
	}

	public String toReversePolish() {
		StringBuffer stringbuffer = new StringBuffer("(");
		stringbuffer.append(op_);
		for (int i = 0; i < nbOperands(); i++) {
			ZExp zexp = getOperand(i);
			if (zexp instanceof ZExpression) stringbuffer.append(" " + ((ZExpression) zexp).toReversePolish());
			else if (zexp instanceof ZQuery) stringbuffer.append(" (" + zexp.toString() + ")");
			else stringbuffer.append(" " + zexp.toString());
		}

		stringbuffer.append(")");
		return stringbuffer.toString();
	}

	@Override
	public String toString() {
		if (op_.equals("?")) return op_;
		if (ZUtils.isCustomFunction(op_) > 0) return formatFunction();
		StringBuffer stringbuffer = new StringBuffer();
		if (needPar(op_)) stringbuffer.append("(");
		switch (nbOperands()) {
		case 1: // '\001'
			ZExp zexp = getOperand(0);
			if (zexp instanceof ZConstant) {
				if (ZUtils.isAggregate(op_)) stringbuffer.append(op_ + "(" + zexp.toString() + ")");
				else stringbuffer.append(op_ + " " + zexp.toString());
			}
			else if (zexp instanceof ZQuery) stringbuffer.append(op_ + " (" + zexp.toString() + ")");
			else stringbuffer.append(op_ + " " + zexp.toString());
			break;

		case 3: // '\003'
			if (op_.toUpperCase().endsWith("BETWEEN")) {
				stringbuffer.append(getOperand(0).toString() + " " + op_ + " " + getOperand(1).toString() + " AND " + getOperand(2).toString());
				break;
			}
			// fall through

		default:
			boolean flag = op_.equals("IN") || op_.equals("NOT IN");
			int i = nbOperands();
			for (int j = 0; j < i; j++) {
				if (flag && j == 1) stringbuffer.append(" " + op_ + " (");
				ZExp zexp1 = getOperand(j);
				if ((zexp1 instanceof ZQuery) && !flag) stringbuffer.append("(" + zexp1.toString() + ")");
				else stringbuffer.append(zexp1.toString());
				if (j < i - 1) if (op_.equals(",") || flag && j > 0) stringbuffer.append(", ");
				else if (!flag) stringbuffer.append(" " + op_ + " ");
			}

			if (flag) stringbuffer.append(")");
			break;
		}
		if (needPar(op_)) stringbuffer.append(")");
		return stringbuffer.toString();
	}

	private boolean needPar(String s) {
		s = s.toUpperCase();
		return !s.equals("ANY") && !s.equals("ALL") && !s.equals("UNION") && !ZUtils.isAggregate(s);
	}

	private String formatFunction() {
		StringBuffer stringbuffer = new StringBuffer(op_ + "(");
		int i = nbOperands();
		for (int j = 0; j < i; j++)
			stringbuffer.append(getOperand(j).toString() + (j >= i - 1 ? "" : ","));

		stringbuffer.append(")");
		return stringbuffer.toString();
	}

	String op_;
	Vector operands_;
}