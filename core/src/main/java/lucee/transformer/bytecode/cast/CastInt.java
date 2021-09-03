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
import lucee.transformer.expression.ExprInt;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.Literal;

/**
 * cast an Expression to a Double
 */
public final class CastInt extends ExpressionBase implements ExprInt, Cast {

	private Expression expr;

	private CastInt(Expression expr) {
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
	public static ExprInt toExprInt(Expression expr) {
		if (expr instanceof ExprInt) return (ExprInt) expr;
		if (expr instanceof Literal) {
			Number n = ((Literal) expr).getNumber(null);
			if (n != null) return expr.getFactory().createLitInteger(n.intValue(), expr.getStart(), expr.getEnd());
		}
		return new CastInt(expr);
	}

	/**
	 * @see lucee.transformer.expression.Expression#_writeOut(org.objectweb.asm.commons.GeneratorAdapter,
	 *      int)
	 */
	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();

		if (expr instanceof ExprString) {
			expr.writeOut(bc, MODE_REF);
			if (mode == MODE_VALUE) adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_INT_VALUE_FROM_STRING);
			else adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_INTEGER_FROM_STRING);
		}
		else {
			Type rtn = ((ExpressionBase) expr).writeOutAsType(bc, mode);
			if (mode == MODE_VALUE) {
				if (!Types.isPrimitiveType(rtn)) {
					adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_INT_VALUE);
				}
				else if (Types.BOOLEAN_VALUE.equals(rtn)) {
					adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_INT_VALUE_FROM_BOOLEAN);
				}
				else if (Types.SHORT_VALUE.equals(rtn)) {
					// No Cast needed
				}
				else if (Types.FLOAT_VALUE.equals(rtn)) {
					adapter.cast(Types.FLOAT_VALUE, Types.INT_VALUE);
				}
				else if (Types.LONG_VALUE.equals(rtn)) {
					adapter.cast(Types.LONG_VALUE, Types.INT_VALUE);
				}
				else if (Types.DOUBLE_VALUE.equals(rtn)) {
					adapter.cast(Types.DOUBLE_VALUE, Types.INT_VALUE);
				}
				else if (Types.INT_VALUE.equals(rtn)) {
					// No Cast needed
				}
				else {
					adapter.invokeStatic(Types.CASTER, new Method("toRef", Types.toRefType(rtn), new Type[] { rtn }));
					adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_INT_VALUE);
				}
				return Types.INT_VALUE;

			}
			// TODOX other number types?
			else if (Types.isPrimitiveType(rtn)) {
				if (Types.DOUBLE_VALUE.equals(rtn)) {
					adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_INTEGER_FROM_DOUBLE);
				}
				else if (Types.BOOLEAN_VALUE.equals(rtn)) {
					adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_INTEGER_FROM_BOOLEAN);
				}
				else {
					adapter.invokeStatic(Types.CASTER, new Method("toRef", Types.toRefType(rtn), new Type[] { rtn }));
					adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_INTEGER);
				}
				return Types.INTEGER;
			}

			if (!Types.INTEGER.equals(rtn)) adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_INTEGER);
			return Types.INTEGER;
		}

		if (mode == MODE_VALUE) return Types.INT_VALUE;
		return Types.INTEGER;
	}

	@Override
	public Expression getExpr() {
		return expr;
	}
}