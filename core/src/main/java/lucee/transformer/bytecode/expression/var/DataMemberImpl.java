/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.transformer.bytecode.expression.var;

import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.var.DataMember;
import lucee.transformer.expression.var.Variable;

public final class DataMemberImpl implements DataMember {
	private ExprString name;
	private Variable parent;
	private boolean safeNavigated;
	private Expression safeNavigatedValue;

	public DataMemberImpl(ExprString name) {
		this.name = name;
	}

	public void setParent(Variable parent) {
		this.parent = parent;
	}

	public Variable getParent() {
		return parent;
	}

	@Override
	public ExprString getName() {
		return name;
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