/**
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.
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

import lucee.runtime.PageContext;
import lucee.runtime.StaticScope;
import lucee.runtime.component.ComponentLoader;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.java.JavaObject;

public class _GetStaticScope implements Function {

	private static final long serialVersionUID = -2676531632543576056L;

	public static Object call(PageContext pc, String componentPath) throws PageException {
		return call(pc, componentPath, _CreateComponent.TYPE_BOTH);
	}

	public static Object call(PageContext pc, String componentPath, String type) throws PageException {
		if ("java".equalsIgnoreCase(type)) return call(pc, componentPath, _CreateComponent.TYPE_JAVA);
		if ("cfml".equalsIgnoreCase(type)) return call(pc, componentPath, _CreateComponent.TYPE_CFML);
		return call(pc, componentPath, _CreateComponent.TYPE_BOTH);
	}

	private static Object call(PageContext pc, String componentPath, int type) throws PageException {

		if (type != _CreateComponent.TYPE_JAVA) {
			StaticScope ss = ComponentLoader.getStaticScope(pc, null, componentPath, null, null, type == _CreateComponent.TYPE_CFML);
			if (ss != null) return ss;
		}

		// no if needed, if type=="cfml", getStaticScope return a result or throw an exception
		Class cls = _CreateComponent.loadClass(pc, componentPath, type);
		return new JavaObject((pc).getVariableUtil(), cls, false);

	}
}