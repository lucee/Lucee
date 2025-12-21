/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
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

import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.Methods;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.ExprNumber;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.Literal;

public final class OpNegateNumber extends ExpressionBase implements ExprNumber {

	private ExprNumber expr;
	private int operation;

	private OpNegateNumber(Expression expr, int operation, Position start, Position end) {
		super(expr.getFactory(), start, end);
		this.expr = expr.getFactory().toExprNumber(expr);
		this.operation = operation;
	}

	/**
	 * Create a String expression from an Expression
	 *
	 * @param expr
	 * @param start
	 * @param end
	 *
	 * @return String expression
	 */
	public static ExprNumber toExprNumber(Expression expr, Position start, Position end) {
		return toExprNumber(expr, Factory.OP_NEG_NBR_MINUS, start, end);
	}

	public static ExprNumber toExprNumber(Expression expr, int operation, Position start, Position end) {
		if (operation == Factory.OP_NEG_NBR_MINUS) {
			// For minus, try to fold literals
			if (expr instanceof Literal) {
				Number n = ((Literal) expr).getNumber(null);
				if (n != null) {
					if (n instanceof BigDecimal) return expr.getFactory().createLitNumber(((BigDecimal) n).negate(), start, end);
					return expr.getFactory().createLitNumber(BigDecimal.valueOf(-n.doubleValue()), start, end);
				}
			}
		}
		// For plus, we could fold literals too, but the value doesn't change
		// Still need to create the node to preserve the unary plus in AST
		return new OpNegateNumber(expr, operation, start, end);
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();

		// For unary plus, just write out the expression as a number
		if (operation == Factory.OP_NEG_NBR_PLUS) {
			if (mode == MODE_VALUE) {
				expr.writeOut(bc, MODE_VALUE);
				return Types.DOUBLE_VALUE;
			}
			expr.writeOut(bc, MODE_REF);
			return Types.NUMBER;
		}

		// For unary minus, negate the value
		if (mode == MODE_VALUE) {
			expr.writeOut(bc, MODE_VALUE);
			adapter.visitInsn(Opcodes.DNEG);
			return Types.DOUBLE_VALUE;
		}

		expr.writeOut(bc, MODE_REF);
		adapter.invokeStatic(Types.CASTER, Methods.METHOD_NEGATE_NUMBER);
		return Types.NUMBER;
	}

	@Override
	public void dump(Struct sct) {
		super.dump(sct);
		sct.setEL(KeyConstants._type, "UnaryExpression");
		sct.setEL(KeyConstants._operator, operation == Factory.OP_NEG_NBR_PLUS ? "PLUS" : "NEGATE");
		sct.setEL(KeyConstants._prefix, Boolean.TRUE);
		// argument
		{
			Struct sctArg = new StructImpl(Struct.TYPE_LINKED);
			expr.dump(sctArg);
			sct.setEL(KeyConstants._argument, sctArg);
		}
	}
}