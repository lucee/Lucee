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

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.transformer.Factory;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.Methods;
import lucee.transformer.bytecode.util.Methods_Operator;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.ExprBoolean;
import lucee.transformer.expression.Expression;

public final class OpDecision extends ExpressionBase implements ExprBoolean {
	/*
	 * public static final int LT=GeneratorAdapter.LT; public static final int LTE=GeneratorAdapter.LE;
	 * public static final int GTE=GeneratorAdapter.GE; public static final int GT=GeneratorAdapter.GT;
	 * public static final int EQ=GeneratorAdapter.EQ; public static final int NEQ=GeneratorAdapter.NE;
	 * public static final int CT = 1000; public static final int NCT = 1001; public static final int
	 * EEQ = 1002; public static final int NEEQ = 1003;
	 */
	private final Expression left;
	private final Expression right;
	private final int op;

	// int compare (Object, Object)
	final public static Method METHOD_COMPARE = new Method("compare", Types.INT_VALUE, new Type[] { Types.OBJECT, Types.OBJECT });

	private OpDecision(Expression left, Expression right, int operation) {
		super(left.getFactory(), left.getStart(), right.getEnd());
		this.left = left;
		this.right = right;
		this.op = operation;
	}

	/**
	 * Create a String expression from an operation
	 * 
	 * @param left
	 * @param right
	 * 
	 * @return String expression
	 */
	public static ExprBoolean toExprBoolean(Expression left, Expression right, int operation) {
		return new OpDecision(left, right, operation);
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		if (mode == MODE_REF) {
			_writeOut(bc, MODE_VALUE);
			adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_BOOLEAN_FROM_BOOLEAN);
			return Types.BOOLEAN;
		}

		if (op == Factory.OP_DEC_CT) {
			left.writeOut(bc, MODE_REF);
			right.writeOut(bc, MODE_REF);
			adapter.invokeStatic(Types.OPERATOR, Methods_Operator.OPERATOR_CT);
		}
		else if (op == Factory.OP_DEC_NCT) {
			left.writeOut(bc, MODE_REF);
			right.writeOut(bc, MODE_REF);
			adapter.invokeStatic(Types.OPERATOR, Methods_Operator.OPERATOR_NCT);
		}
		else if (op == Factory.OP_DEC_EEQ) {
			left.writeOut(bc, MODE_REF);
			right.writeOut(bc, MODE_REF);
			adapter.invokeStatic(Types.OPERATOR, Methods_Operator.OPERATOR_EEQ);
		}
		else if (op == Factory.OP_DEC_NEEQ) {
			left.writeOut(bc, MODE_REF);
			right.writeOut(bc, MODE_REF);
			adapter.invokeStatic(Types.OPERATOR, Methods_Operator.OPERATOR_NEEQ);
		}
		else {
			int iLeft = Types.getType(((ExpressionBase) left).writeOutAsType(bc, MODE_VALUE));
			int iRight = Types.getType(((ExpressionBase) right).writeOutAsType(bc, MODE_VALUE));

			adapter.invokeStatic(Types.OPERATOR, Methods_Operator.OPERATORS[iLeft][iRight]);

			adapter.visitInsn(Opcodes.ICONST_0);

			Label l1 = new Label();
			Label l2 = new Label();
			adapter.ifCmp(Type.INT_TYPE, toASMOperation(op), l1);
			// adapter.visitJumpInsn(Opcodes.IF_ICMPEQ, l1);
			adapter.visitInsn(Opcodes.ICONST_0);
			adapter.visitJumpInsn(Opcodes.GOTO, l2);
			adapter.visitLabel(l1);
			adapter.visitInsn(Opcodes.ICONST_1);
			adapter.visitLabel(l2);
		}
		return Types.BOOLEAN_VALUE;
	}

	private int toASMOperation(int op) throws TransformerException {
		if (Factory.OP_DEC_LT == op) return GeneratorAdapter.LT;
		if (Factory.OP_DEC_LTE == op) return GeneratorAdapter.LE;
		if (Factory.OP_DEC_GT == op) return GeneratorAdapter.GT;
		if (Factory.OP_DEC_GTE == op) return GeneratorAdapter.GE;
		if (Factory.OP_DEC_EQ == op) return GeneratorAdapter.EQ;
		if (Factory.OP_DEC_NEQ == op) return GeneratorAdapter.NE;

		throw new TransformerException("cannot convert operation [" + op + "] to an ASM Operation", left.getStart());
	}

	public Expression getLeft() {
		return left;
	}

	public Expression getRight() {
		return right;
	}

	public int getOperation() {
		return op;
	}
}