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
package lucee.transformer.bytecode.op;

import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.bytecode.expression.var.Assign;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.var.Variable;

public final class OpVariable extends Assign {

	public OpVariable(Variable variable, Expression value, Position end) {
		super(variable, value, end);
	}

	@Override
	public void dump(Struct sct) {
		// Check if this is a compound assignment (value is OpNumber with left side matching our variable)
		Expression value = getValue();
		if (value instanceof OpNumber) {
			OpNumber opNum = (OpNumber) value;
			int op = opNum.getOperation();
			String compoundOp = getCompoundOperator(op);
			if (compoundOp != null) {
				// This is a compound assignment - output with compound operator
				// Dump position info from grandparent (ExpressionBase)
				Position start = getStart();
				Position end = getEnd();
				if (start != null) {
					Struct sctStart = new StructImpl(Struct.TYPE_LINKED);
					sctStart.setEL(KeyConstants._line, start.line);
					sctStart.setEL(KeyConstants._column, start.displayColumn());
					sctStart.setEL(KeyConstants._offset, start.displayPosition());
					sct.setEL(KeyConstants._start, sctStart);
				}
				if (end != null) {
					Struct sctEnd = new StructImpl(Struct.TYPE_LINKED);
					sctEnd.setEL(KeyConstants._line, end.line);
					sctEnd.setEL(KeyConstants._column, end.displayColumn());
					sctEnd.setEL(KeyConstants._offset, end.displayPosition());
					sct.setEL(KeyConstants._end, sctEnd);
				}

				sct.setEL(KeyConstants._type, "AssignmentExpression");
				sct.setEL(KeyConstants._operator, compoundOp);

				Struct left = new StructImpl(Struct.TYPE_LINKED);
				sct.setEL(KeyConstants._left, left);
				getVariable().dump(left);

				Struct right = new StructImpl(Struct.TYPE_LINKED);
				sct.setEL(KeyConstants._right, right);
				opNum.getRight().dump(right);
				return;
			}
		}
		// Fall back to default Assign dump
		super.dump(sct);
	}

	private static String getCompoundOperator(int op) {
		if (op == Factory.OP_DBL_PLUS) return "PLUS_ASSIGN";
		if (op == Factory.OP_DBL_MINUS) return "MINUS_ASSIGN";
		if (op == Factory.OP_DBL_MULTIPLY) return "MULTIPLY_ASSIGN";
		if (op == Factory.OP_DBL_DIVIDE) return "DIVIDE_ASSIGN";
		if (op == Factory.OP_DBL_MODULUS) return "MODULUS_ASSIGN";
		return null;
	}
}