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
package lucee.transformer.bytecode.statement;

import java.util.Map;

import lucee.runtime.type.FunctionArgument;
import lucee.transformer.Factory;
import lucee.transformer.expression.ExprBoolean;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.expression.literal.Literal;

public final class Argument {

	private ExprString name;
	private ExprString type;
	private ExprBoolean required;
	private Expression defaultValue;
	private ExprString displayName;
	private ExprString hint;
	private Map meta;
	private ExprBoolean passByReference;

	/**
	 * Constructor of the class
	 * 
	 * @param name
	 * @param type
	 * @param required
	 * @param defaultValue
	 * @param displayName
	 * @param hint
	 * @param hint2
	 * @param meta
	 */
	public Argument(Expression name, Expression type, Expression required, Expression defaultValue, ExprBoolean passByReference, Expression displayName, Expression hint,
			Map meta) {
		LitString re = name.getFactory().createLitString("[runtime expression]");

		this.name = name.getFactory().toExprString(name);
		this.type = name.getFactory().toExprString(type);
		this.required = name.getFactory().toExprBoolean(required);
		this.defaultValue = defaultValue;
		this.displayName = litString(name.getFactory().toExprString(displayName), re);
		this.hint = litString(hint, re);
		this.passByReference = passByReference;
		this.meta = meta;
	}

	private LitString litString(Expression expr, LitString defaultValue) {
		ExprString str = expr.getFactory().toExprString(expr);
		if (str instanceof LitString) return (LitString) str;
		return defaultValue;
	}

	/**
	 * @return the defaultValue
	 */
	public Expression getDefaultValue() {
		return defaultValue;
	}

	public Expression getDefaultValueType(Factory f) {
		if (defaultValue == null) return f.createLitInteger(FunctionArgument.DEFAULT_TYPE_NULL);
		if (defaultValue instanceof Literal) return f.createLitInteger(FunctionArgument.DEFAULT_TYPE_LITERAL);
		return f.createLitInteger(FunctionArgument.DEFAULT_TYPE_RUNTIME_EXPRESSION);
	}

	/**
	 * @return the displayName
	 */
	public ExprString getDisplayName() {
		return displayName;
	}

	/**
	 * @return the hint
	 */
	public ExprString getHint() {
		return hint;
	}

	/**
	 * @return the name
	 */
	public ExprString getName() {
		return name;
	}

	/**
	 * @return the passBy
	 */
	public ExprBoolean isPassByReference() {
		return passByReference;
	}

	/**
	 * @return the required
	 */
	public ExprBoolean getRequired() {
		return required;
	}

	public ExprString getType() {
		return type;
	}

	public Map getMetaData() {
		return meta;
	}

}