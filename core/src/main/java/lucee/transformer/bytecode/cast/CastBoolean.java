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
import lucee.transformer.expression.ExprNumber;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.Literal;

/**
 * Cast to a Boolean
 */
public final class CastBoolean extends ExpressionBase implements ExprBoolean, Cast {

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "(boolean)" + expr;
	}

	private Expression expr;

	/**
	 * constructor of the class
	 * 
	 * @param expr
	 */
	private CastBoolean(Expression expr) {
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
	public static ExprBoolean toExprBoolean(Expression expr) {
		if (expr instanceof ExprBoolean) return (ExprBoolean) expr;
		if (expr instanceof Literal) {
			Boolean bool = ((Literal) expr).getBoolean(null);
			if (bool != null) return expr.getFactory().createLitBoolean(bool.booleanValue(), expr.getStart(), expr.getEnd());
			// TODO throw new TemplateException("can't cast value to a boolean value");
		}
		return new CastBoolean(expr);
	}

	/**
	 * @see lucee.transformer.expression.Expression#writeOut(org.objectweb.asm.commons.GeneratorAdapter,
	 *      int)
	 */
	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		if (expr instanceof ExprNumber) {
			expr.writeOut(bc, mode);
			if (mode == MODE_VALUE) adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_BOOLEAN_VALUE_FROM_DOUBLE_VALUE);
			else adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_BOOLEAN_FROM_NUMBER);
		}
		else if (expr instanceof ExprString) {
			expr.writeOut(bc, MODE_REF);
			if (mode == MODE_VALUE) adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_BOOLEAN_VALUE_FROM_STRING);
			else adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_BOOLEAN_FROM_STRING);
		}
		else {
			Type rtn = ((ExpressionBase) expr).writeOutAsType(bc, mode);

			if (mode == MODE_VALUE) {
				if (!Types.isPrimitiveType(rtn)) {
					adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_BOOLEAN_VALUE);
				}
				else if (Types.BOOLEAN_VALUE.equals(rtn)) {
				}
				else if (Types.DOUBLE_VALUE.equals(rtn)) {
					adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_BOOLEAN_VALUE_FROM_DOUBLE_VALUE);
				}
				else {
					adapter.invokeStatic(Types.CASTER, new Method("toRef", Types.toRefType(rtn), new Type[] { rtn }));
					adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_BOOLEAN_VALUE);
				}
				// return Types.BOOLEAN_VALUE;
			}
			else {
				if (Types.BOOLEAN.equals(rtn)) {
				}
				else adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_BOOLEAN);
			}
		}

		if (mode == MODE_VALUE) return Types.BOOLEAN_VALUE;
		return Types.BOOLEAN;
	}

	@Override
	public Expression getExpr() {
		return expr;
	}
}