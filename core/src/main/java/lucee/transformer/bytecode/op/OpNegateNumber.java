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

import java.math.BigDecimal;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import lucee.runtime.exp.TemplateException;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.Methods;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.ExprDouble;
import lucee.transformer.expression.ExprNumber;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.Literal;

public final class OpNegateNumber extends ExpressionBase implements ExprNumber {

	private ExprDouble expr;

	// public static final int PLUS = 0;
	// public static final int MINUS = 1;

	private OpNegateNumber(Expression expr, Position start, Position end) {
		super(expr.getFactory(), start, end);
		this.expr = expr.getFactory().toExprDouble(expr);// TODOX allow other types than double
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
	public static ExprNumber toExprNumber(Expression expr, Position start, Position end) {
		if (expr instanceof Literal) {
			Number n = ((Literal) expr).getNumber(null);
			if (n != null) {
				if (n instanceof BigDecimal) return expr.getFactory().createLitNumber(((BigDecimal) n).negate(), start, end);
				return expr.getFactory().createLitDouble(-n.doubleValue(), start, end);
			}
		}
		return new OpNegateNumber(expr, start, end);
	}

	public static ExprNumber toExprNumber(Expression expr, int operation, Position start, Position end) {
		if (operation == Factory.OP_NEG_NBR_MINUS) return toExprNumber(expr, start, end);
		return expr.getFactory().toExprDouble(expr); // TODOX other types
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		if (mode == MODE_REF) {
			_writeOut(bc, MODE_VALUE);
			adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_DOUBLE_FROM_DOUBLE);
			return Types.DOUBLE;
		}

		expr.writeOut(bc, MODE_VALUE);
		adapter.visitInsn(Opcodes.DNEG);

		return Types.DOUBLE_VALUE;
	}
}