/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.transformer.bytecode.op;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import lucee.runtime.exp.TemplateException;
import lucee.transformer.Factory;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.Methods;
import lucee.transformer.bytecode.util.Methods_Operator;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.ExprBoolean;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.Literal;

public final class OpBool extends ExpressionBase implements ExprBoolean {

	private ExprBoolean left;
	private ExprBoolean right;
	private int operation;

	/**
	 *
	 * @see lucee.transformer.bytecode.expression.ExpressionBase#_writeOut(org.objectweb.asm.commons.GeneratorAdapter,
	 *      int)
	 */
	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();

		if (mode == MODE_REF) {
			_writeOut(bc, MODE_VALUE);
			adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_BOOLEAN_FROM_BOOLEAN);
			return Types.BOOLEAN;
		}

		Label doFalse = new Label();
		Label end = new Label();

		if (operation == Factory.OP_BOOL_AND) {
			left.writeOut(bc, MODE_VALUE);
			adapter.ifZCmp(Opcodes.IFEQ, doFalse);

			right.writeOut(bc, MODE_VALUE);
			adapter.ifZCmp(Opcodes.IFEQ, doFalse);
			adapter.push(true);

			adapter.visitJumpInsn(Opcodes.GOTO, end);
			adapter.visitLabel(doFalse);

			adapter.push(false);
			adapter.visitLabel(end);
		}
		if (operation == Factory.OP_BOOL_OR) {
			left.writeOut(bc, MODE_VALUE);
			adapter.ifZCmp(Opcodes.IFNE, doFalse);

			right.writeOut(bc, MODE_VALUE);
			adapter.ifZCmp(Opcodes.IFNE, doFalse);

			adapter.push(false);
			adapter.visitJumpInsn(Opcodes.GOTO, end);
			adapter.visitLabel(doFalse);

			adapter.push(true);
			adapter.visitLabel(end);
		}
		else if (operation == Factory.OP_BOOL_XOR) {
			left.writeOut(bc, MODE_VALUE);
			right.writeOut(bc, MODE_VALUE);
			adapter.visitInsn(Opcodes.IXOR);
		}
		else if (operation == Factory.OP_BOOL_EQV) {

			adapter.loadArg(0);
			left.writeOut(bc, MODE_REF);
			right.writeOut(bc, MODE_REF);
			adapter.invokeStatic(Types.OP_UTIL, Methods_Operator.OPERATOR_EQV_PC_B_B);
		}
		else if (operation == Factory.OP_BOOL_IMP) {

			adapter.loadArg(0);
			left.writeOut(bc, MODE_REF);
			right.writeOut(bc, MODE_REF);
			adapter.invokeStatic(Types.OP_UTIL, Methods_Operator.OPERATOR_IMP_PC_B_B);
		}
		return Types.BOOLEAN_VALUE;

	}

	private OpBool(Expression left, Expression right, int operation) {
		super(left.getFactory(), left.getStart(), right.getEnd());
		this.left = left.getFactory().toExprBoolean(left);
		this.right = left.getFactory().toExprBoolean(right);
		this.operation = operation;
	}

	/**
	 * Create a String expression from an Expression
	 * 
	 * @param left
	 * @param right
	 * 
	 * @return String expression
	 * @throws TemplateException
	 */
	public static ExprBoolean toExprBoolean(Expression left, Expression right, int operation) {
		if (left instanceof Literal && right instanceof Literal) {
			Boolean l = ((Literal) left).getBoolean(null);
			Boolean r = ((Literal) right).getBoolean(null);

			if (l != null && r != null) {
				switch (operation) {
				case Factory.OP_BOOL_AND:
					return left.getFactory().createLitBoolean(l.booleanValue() && r.booleanValue(), left.getStart(), right.getEnd());
				case Factory.OP_BOOL_OR:
					return left.getFactory().createLitBoolean(l.booleanValue() || r.booleanValue(), left.getStart(), right.getEnd());
				case Factory.OP_BOOL_XOR:
					return left.getFactory().createLitBoolean(l.booleanValue() ^ r.booleanValue(), left.getStart(), right.getEnd());
				}
			}
		}
		return new OpBool(left, right, operation);
	}

	@Override
	public String toString() {
		return left + " " + toStringOperation() + " " + right;
	}

	private String toStringOperation() {
		if (Factory.OP_BOOL_AND == operation) return "and";
		if (Factory.OP_BOOL_OR == operation) return "or";
		if (Factory.OP_BOOL_XOR == operation) return "xor";
		if (Factory.OP_BOOL_EQV == operation) return "eqv";
		if (Factory.OP_BOOL_IMP == operation) return "imp";
		return operation + "";
	}
}