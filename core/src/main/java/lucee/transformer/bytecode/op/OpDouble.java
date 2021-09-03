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

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.runtime.exp.TemplateException;
import lucee.transformer.Factory;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.Methods;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.ExprDouble;
import lucee.transformer.expression.Expression;

public final class OpDouble extends ExpressionBase implements ExprDouble {

	private static final Method DIV_REF = new Method("divRef", Types.DOUBLE, new Type[] { Types.OBJECT, Types.OBJECT });
	private static final Method INTDIV_REF = new Method("intdivRef", Types.DOUBLE, new Type[] { Types.OBJECT, Types.OBJECT });
	private static final Method DIVIDE_REF = new Method("divideRef", Types.DOUBLE, new Type[] { Types.OBJECT, Types.OBJECT });
	private static final Method EXP_REF = new Method("exponentRef", Types.DOUBLE, new Type[] { Types.OBJECT, Types.OBJECT });

	private static final Method PLUS_REF = new Method("plusRef", Types.DOUBLE, new Type[] { Types.OBJECT, Types.OBJECT });
	private static final Method MINUS_REF = new Method("minusRef", Types.DOUBLE, new Type[] { Types.OBJECT, Types.OBJECT });
	private static final Method MODULUS_REF = new Method("modulusRef", Types.DOUBLE, new Type[] { Types.OBJECT, Types.OBJECT });
	private static final Method MULTIPLY_REF = new Method("multiplyRef", Types.DOUBLE, new Type[] { Types.OBJECT, Types.OBJECT });

	/*
	 * public static final int PLUS=GeneratorAdapter.ADD; public static final int
	 * MINUS=GeneratorAdapter.SUB; public static final int MODULUS=GeneratorAdapter.REM; public static
	 * final int DIVIDE=GeneratorAdapter.DIV; public static final int MULTIPLY=GeneratorAdapter.MUL;
	 * public static final int EXP = 2000; public static final int INTDIV = 2001;
	 */

	private int op;
	private Expression left;
	private Expression right;

	OpDouble(Expression left, Expression right, int operation) {
		super(left.getFactory(), left.getStart(), right.getEnd());
		this.left = left;
		this.right = right;
		this.op = operation;
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

	/**
	 * Create a String expression from an Expression
	 * 
	 * @param left
	 * @param right
	 * @param operation
	 * 
	 * @return String expression
	 * @throws TemplateException
	 */
	public static ExprDouble toExprDouble(Expression left, Expression right, int operation) {
		return new OpDouble(left, right, operation);
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.expression.ExpressionBase#_writeOut(org.objectweb.asm.commons.GeneratorAdapter,
	 *      int)
	 */
	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		return writeOutDouble(bc, mode);
	}

	public Type writeOutDouble(BytecodeContext bc, int mode) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();

		left.writeOut(bc, MODE_REF);
		right.writeOut(bc, MODE_REF);

		if (op == Factory.OP_DBL_EXP) {
			adapter.invokeStatic(Types.OPERATOR, EXP_REF);
		}
		else if (op == Factory.OP_DBL_DIVIDE) {
			adapter.invokeStatic(Types.OPERATOR, DIV_REF);
		}
		else if (op == Factory.OP_DBL_INTDIV) {
			adapter.invokeStatic(Types.OPERATOR, INTDIV_REF);
		}
		else if (op == Factory.OP_DBL_PLUS) {
			adapter.invokeStatic(Types.OPERATOR, PLUS_REF);
		}
		else if (op == Factory.OP_DBL_MINUS) {
			adapter.invokeStatic(Types.OPERATOR, MINUS_REF);
		}
		else if (op == Factory.OP_DBL_MODULUS) {
			adapter.invokeStatic(Types.OPERATOR, MODULUS_REF);
		}
		else if (op == Factory.OP_DBL_DIVIDE) {
			adapter.invokeStatic(Types.OPERATOR, DIVIDE_REF);
		}
		else if (op == Factory.OP_DBL_MULTIPLY) {
			adapter.invokeStatic(Types.OPERATOR, MULTIPLY_REF);
		}

		if (mode == MODE_VALUE) {
			adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_DOUBLE_VALUE_FROM_DOUBLE);
			return Types.DOUBLE_VALUE;
		}
		return Types.DOUBLE;
	}

}