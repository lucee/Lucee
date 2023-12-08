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
package lucee.runtime.type.scope;

import lucee.runtime.PageContext;
import lucee.runtime.type.Struct;

public final class LocalImpl extends ScopeSupport implements Scope, Local {

	private static final long serialVersionUID = -7155406303949924403L;
	private boolean bind;

	public LocalImpl() {
		// super("local", Scope.SCOPE_LOCAL, Struct.TYPE_SYNC, 4);
		super("local", Scope.SCOPE_LOCAL, Struct.TYPE_REGULAR, 4);
	}

	@Override
	public void release(PageContext pc) {
		super.release(pc);
	}

	@Override
	public boolean isBind() {
		return bind;
	}

	@Override
	public void setBind(boolean bind) {
		if (bind) {
			makeSynchronized();
		}
		this.bind = bind;
	}
}