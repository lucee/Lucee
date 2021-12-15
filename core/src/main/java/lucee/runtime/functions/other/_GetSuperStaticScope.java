/**
 * Copyright (c) 2017, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.functions.other;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.Struct;

public class _GetSuperStaticScope implements Function {

	private static final long serialVersionUID = -2676531632543576056L;

	public static Struct call(PageContext pc) throws PageException {
		Component cfc = pc.getActiveComponent();
		if (cfc == null) throw new ApplicationException("[static::] is not supported outside a component.");
		Component base = cfc.getBaseComponent();
		if (base == null) throw new ApplicationException("component [" + cfc.getCallName() + "] does not have a base component.");
		return base.staticScope();
	}

}