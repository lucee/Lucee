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
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.ExprNumber;
import lucee.transformer.expression.Expression;

public final class OpNumber extends ExpressionBase implements ExprNumber {
	// exponent
	private static final Method EXP_REF = new Method("exponentRef", Types.NUMBER, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT });
	private static final Method EXP = new Method("exponent", Types.DOUBLE_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT });

	// divide
	private static final Method DIV_REF = new Method("divideRef", Types.NUMBER, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT });
	private static final Method DIV = new Method("divide", Types.DOUBLE_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT });

	// divide int
	private static final Method INTDIV_REF = new Method("intdivRef", Types.NUMBER, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT });
	private static final Method INTDIV = new Method("intdiv", Types.DOUBLE_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT });

	// plus
	private static final Method PLUS_REF = new Method("plusRef", Types.NUMBER, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT });
	private static final Method PLUS = new Method("plus", Types.DOUBLE_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT });

	// minus
	private static final Method MINUS_REF = new Method("minusRef", Types.NUMBER, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT });
	private static final Method MINUS = new Method("minus", Types.DOUBLE_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT });

	// modulus
	private static final Method MODULUS_REF = new Method("modulusRef", Types.NUMBER, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT });
	private static final Method MODULUS = new Method("modulus", Types.DOUBLE_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT });

	// multiply
	private static final Method MULTIPLY_REF = new Method("multiplyRef", Types.NUMBER, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT });
	private static final Method MULTIPLY = new Method("multiply", Types.DOUBLE_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT });

	private int op;
	private Expression left;
	private Expression right;

	OpNumber(Expression left, Expression right, int operation) {
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
	public static ExprNumber toExprNumber(Expression left, Expression right, int operation) {
		return new OpNumber(left, right, operation);
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		return writeOutNumber(bc, mode);
	}

	public Type writeOutNumber(BytecodeContext bc, int mode) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();

		adapter.loadArg(0);
		left.writeOut(bc, MODE_REF);
		right.writeOut(bc, MODE_REF);

		if (op == Factory.OP_DBL_EXP) {
			if (mode == MODE_VALUE) adapter.invokeStatic(Types.OP_UTIL, EXP);
			else adapter.invokeStatic(Types.OP_UTIL, EXP_REF);
		}
		else if (op == Factory.OP_DBL_DIVIDE) {
			if (mode == MODE_VALUE) adapter.invokeStatic(Types.OP_UTIL, DIV);
			else adapter.invokeStatic(Types.OP_UTIL, DIV_REF);
		}
		else if (op == Factory.OP_DBL_INTDIV) {
			if (mode == MODE_VALUE) adapter.invokeStatic(Types.OP_UTIL, INTDIV);
			else adapter.invokeStatic(Types.OP_UTIL, INTDIV_REF);
		}
		else if (op == Factory.OP_DBL_PLUS) {
			if (mode == MODE_VALUE) adapter.invokeStatic(Types.OP_UTIL, PLUS);
			else adapter.invokeStatic(Types.OP_UTIL, PLUS_REF);
		}
		else if (op == Factory.OP_DBL_MINUS) {
			if (mode == MODE_VALUE) adapter.invokeStatic(Types.OP_UTIL, MINUS);
			else adapter.invokeStatic(Types.OP_UTIL, MINUS_REF);
		}
		else if (op == Factory.OP_DBL_MODULUS) {
			if (mode == MODE_VALUE) adapter.invokeStatic(Types.OP_UTIL, MODULUS);
			else adapter.invokeStatic(Types.OP_UTIL, MODULUS_REF);
		}
		else if (op == Factory.OP_DBL_MULTIPLY) {
			if (mode == MODE_VALUE) adapter.invokeStatic(Types.OP_UTIL, MULTIPLY);
			else adapter.invokeStatic(Types.OP_UTIL, MULTIPLY_REF);
		}
		if (mode == MODE_VALUE) return Types.DOUBLE_VALUE;
		return Types.NUMBER;
	}

}