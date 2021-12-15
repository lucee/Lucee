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
package lucee.transformer.bytecode.expression.var;

import lucee.transformer.expression.Expression;
import lucee.transformer.expression.var.Member;
import lucee.transformer.expression.var.Variable;

public abstract class FunctionMember implements Member, Func {
	private Argument[] arguments = new Argument[0];
	private boolean _hasNamedArgs;
	private Variable parent;
	private boolean safeNavigated;
	private Expression safeNavigatedValue;

	@Override
	public final void setParent(Variable parent) {
		this.parent = parent;
	}

	@Override
	public final Variable getParent() {
		return parent;
	}

	@Override
	public void addArgument(Argument argument) {
		if (argument instanceof NamedArgument) _hasNamedArgs = true;
		Argument[] tmp = new Argument[arguments.length + 1];
		for (int i = 0; i < arguments.length; i++) {
			tmp[i] = arguments[i];
		}
		tmp[arguments.length] = argument;
		arguments = tmp;
	}

	/**
	 * @return the arguments
	 */
	public Argument[] getArguments() {
		return arguments;
	}

	public void setArguments(Argument[] arguments) {
		this.arguments = arguments;
	}

	public boolean hasNamedArgs() {
		return _hasNamedArgs;
	}

	@Override
	public void setSafeNavigated(boolean safeNavigated) {
		this.safeNavigated = safeNavigated;
	}

	@Override
	public boolean getSafeNavigated() {
		return this.safeNavigated;
	}

	@Override
	public void setSafeNavigatedValue(Expression safeNavigatedValue) {
		this.safeNavigatedValue = safeNavigatedValue;
	}

	@Override
	public Expression getSafeNavigatedValue() {
		return safeNavigatedValue;
	}
}