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
package lucee.transformer.bytecode.cast;

import java.math.BigDecimal;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.runtime.exp.TemplateException;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.Methods;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.cast.Cast;
import lucee.transformer.expression.ExprBoolean;
import lucee.transformer.expression.ExprNumber;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.Literal;

/**
 * cast an Expression to a Double
 */
public final class CastNumber extends ExpressionBase implements ExprNumber, Cast {

	private Expression expr;

	private CastNumber(Expression expr) {
		super(expr.getFactory(), expr.getStart(), expr.getEnd());
		this.expr = expr;
	}

	/**
	 * Create a String expression from an Expression
	 * 
	 * @param expr
	 * @return String expression
	 * @throws TemplateException
	 */
	public static ExprNumber toExprNumber(Expression expr) {
		if (expr instanceof ExprNumber) return (ExprNumber) expr;
		if (expr instanceof Literal) {
			Number n = ((Literal) expr).getNumber(null);
			if (n != null) {
				if (n instanceof BigDecimal) return expr.getFactory().createLitNumber((BigDecimal) n, expr.getStart(), expr.getEnd());
				return expr.getFactory().createLitNumber(BigDecimal.valueOf(n.doubleValue()), expr.getStart(), expr.getEnd());
			}
		}
		return new CastNumber(expr);
	}

	/**
	 * @see lucee.transformer.expression.Expression#_writeOut(org.objectweb.asm.commons.GeneratorAdapter,
	 *      int)
	 */
	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {

		GeneratorAdapter adapter = bc.getAdapter();
		if (expr instanceof ExprBoolean) {
			expr.writeOut(bc, MODE_VALUE);
			adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_DOUBLE_FROM_BOOLEAN);
			return Types.NUMBER;
		}
		else if (expr instanceof ExprNumber) {
			expr.writeOut(bc, MODE_REF);
			return Types.NUMBER;
		}
		else if (expr instanceof ExprString) {
			expr.writeOut(bc, MODE_REF);
			adapter.invokeStatic(Types.CASTER, Methods.METHOD_NUMBER_FROM_STRING);
			// if (Factory.PERCISENUMBERS) adapter.invokeStatic(Types.CASTER,
			// Methods.METHOD_TO_BIG_DECIMAL_FROM_STRING);
			// else adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_DOUBLE_FROM_STRING);
			return Types.NUMBER;
		}
		else {
			Type rtn = ((ExpressionBase) expr).writeOutAsType(bc, mode);
			if (Types.isPrimitiveType(rtn)) {
				if (Types.DOUBLE_VALUE.equals(rtn)) {
					adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_DOUBLE_FROM_DOUBLE);
				}
				else if (Types.BOOLEAN_VALUE.equals(rtn)) {
					adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_DOUBLE_FROM_BOOLEAN);
				}
				else {
					adapter.invokeStatic(Types.CASTER, new Method("toRef", Types.toRefType(rtn), new Type[] { rtn }));
					adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_DOUBLE);
				}
				return Types.NUMBER;
			}

			if (Types.DOUBLE.equals(rtn)) return Types.NUMBER;
			if (Types.BIG_DECIMAL.equals(rtn)) return Types.NUMBER;
			if (Types.FLOAT.equals(rtn)) return Types.NUMBER;
			if (Types.LONG.equals(rtn)) return Types.NUMBER;
			if (Types.INTEGER.equals(rtn)) return Types.NUMBER;
			if (Types.SHORT.equals(rtn)) return Types.NUMBER;

			adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_NUMBER);
			return Types.NUMBER;
		}
	}

	@Override
	public Expression getExpr() {
		return expr;
	}

}