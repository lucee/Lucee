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
import lucee.transformer.expression.ExprBoolean;
import lucee.transformer.expression.ExprDouble;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.Literal;

/**
 * cast an Expression to a Double
 */
public final class CastDouble extends ExpressionBase implements ExprDouble, Cast {

	private Expression expr;

	private CastDouble(Expression expr) {
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
	public static ExprDouble toExprDouble(Expression expr) {
		if (expr instanceof ExprDouble) return (ExprDouble) expr;
		if (expr instanceof Literal) {
			Double dbl = ((Literal) expr).getDouble(null);
			if (dbl != null) return expr.getFactory().createLitDouble(dbl.doubleValue(), expr.getStart(), expr.getEnd());
		}
		return new CastDouble(expr);
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
			if (mode == MODE_VALUE) adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_DOUBLE_VALUE_FROM_BOOLEAN);
			else adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_DOUBLE_FROM_BOOLEAN);
		}
		else if (expr instanceof ExprDouble) {
			expr.writeOut(bc, mode);
			// if(mode==MODE_VALUE)adapter.invokeStatic(Types.CASTER,Methods.METHOD_TO_DOUBLE_VALUE_FROM_DOUBLE);
			// if(mode==MODE_REF) adapter.invokeStatic(Types.CASTER,Methods.METHOD_TO_DOUBLE_FROM_DOUBLE);
		}
		else if (expr instanceof ExprString) {
			expr.writeOut(bc, MODE_REF);
			if (mode == MODE_VALUE) adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_DOUBLE_VALUE_FROM_STRING);
			else adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_DOUBLE_FROM_STRING);
		}
		else {
			Type rtn = ((ExpressionBase) expr).writeOutAsType(bc, mode);
			if (mode == MODE_VALUE) {
				if (!Types.isPrimitiveType(rtn)) {
					adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_DOUBLE_VALUE);
				}
				else if (Types.DOUBLE_VALUE.equals(rtn)) {}
				else if (Types.BOOLEAN_VALUE.equals(rtn)) {
					adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_DOUBLE_VALUE_FROM_BOOLEAN);
				}
				else {
					adapter.invokeStatic(Types.CASTER, new Method("toRef", Types.toRefType(rtn), new Type[] { rtn }));
					adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_DOUBLE_VALUE);
				}
				return Types.DOUBLE_VALUE;
			}
			else if (Types.isPrimitiveType(rtn)) {
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
				return Types.DOUBLE;
			}
			// else {
			if (!Types.DOUBLE.equals(rtn)) adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_DOUBLE);
			return Types.DOUBLE;
			// }
		}

		if (mode == MODE_VALUE) return Types.DOUBLE_VALUE;
		return Types.DOUBLE;
	}

	@Override
	public Expression getExpr() {
		return expr;
	}

}