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
package lucee.runtime.interpreter.ref.var;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.VariableInterpreter;
import lucee.runtime.interpreter.ref.Ref;
import lucee.runtime.interpreter.ref.RefSupport;
import lucee.runtime.interpreter.ref.Set;
import lucee.runtime.interpreter.ref.literal.LString;

/**
 * 
 */
public final class Scope extends RefSupport implements Set {

	private int scope;

	/**
	 * contructor of the class
	 * 
	 * @param pc
	 * @param scope
	 */
	public Scope(int scope) {
		this.scope = scope;
	}

	@Override
	public Object getValue(PageContext pc) throws PageException {
		return VariableInterpreter.scope(pc, scope, false);
	}

	@Override
	public String getTypeName() {
		return "scope";
	}

	@Override
	public Object touchValue(PageContext pc) throws PageException {
		return VariableInterpreter.scope(pc, scope, true);
	}

	@Override
	public Object setValue(PageContext pc, Object obj) throws PageException {
		return pc.undefinedScope().set(getKeyAsString(pc), obj);
	}

	/**
	 * @return scope
	 */
	public int getScope() {
		return scope;
	}

	@Override
	public Ref getParent(PageContext pc) throws PageException {
		return null;
	}

	@Override
	public Ref getKey(PageContext pc) throws PageException {
		return new LString(getKeyAsString(pc));
	}

	@Override
	public String getKeyAsString(PageContext pc) throws PageException {
		// return ScopeFactory.toStringScope(scope,null);
		return VariableInterpreter.scopeInt2String(scope);
	}
}