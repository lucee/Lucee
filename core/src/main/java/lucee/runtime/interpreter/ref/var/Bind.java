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
import lucee.runtime.interpreter.ref.Ref;
import lucee.runtime.interpreter.ref.RefSupport;
import lucee.runtime.interpreter.ref.Set;
import lucee.runtime.type.scope.BindScope;

public final class Bind extends RefSupport implements Set {

	private Scope scope;

	public Bind(Scope scope) {
		this.scope = scope;
	}

	@Override
	public Object touchValue(PageContext pc) throws PageException {
		Object obj = scope.touchValue(pc);
		if (obj instanceof BindScope) ((BindScope) obj).setBind(true);
		return obj;
	}

	@Override
	public Object getValue(PageContext pc) throws PageException {
		Object obj = scope.getValue(pc);
		if (obj instanceof BindScope) ((BindScope) obj).setBind(true);
		return obj;
	}

	@Override
	public String getTypeName() {
		return scope.getTypeName() + " bind";
	}

	@Override
	public Object setValue(PageContext pc, Object obj) throws PageException {
		return scope.setValue(pc, obj);
	}

	@Override
	public Ref getParent(PageContext pc) throws PageException {
		return scope.getParent(pc);
	}

	@Override
	public Ref getKey(PageContext pc) throws PageException {
		return scope.getKey(pc);
	}

	@Override
	public String getKeyAsString(PageContext pc) throws PageException {
		return scope.getKeyAsString(pc);
	}
}