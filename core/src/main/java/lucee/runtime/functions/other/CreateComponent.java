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
package lucee.runtime.functions.other;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.KeyConstants;

public class CreateComponent {
	private static final Object[] EMPTY = new Object[0];

	public static Component call(PageContext pc, String path) throws PageException {
		return call(pc, path, null);
	}

	public static Component call(PageContext pc, String path, Object args) throws PageException {

		// first argument is the component itself
		Component c = CreateObject.doComponent(pc, path);

		if (c.get(KeyConstants._init, null) instanceof UDF) {
			// no arguments
			if (args == null) {
				c.call(pc, KeyConstants._init, EMPTY);
			}
			// named arguments
			else if (Decision.isStruct(args)) {
				Struct sct = Caster.toStruct(args);
				c.callWithNamedValues(pc, KeyConstants._init, sct);
			}
			// not named arguments
			else if (Decision.isArray(args)) {
				Object[] arr = Caster.toNativeArray(args);
				c.call(pc, KeyConstants._init, arr);
			}
			else {
				c.call(pc, KeyConstants._init, new Object[] { args });
			}
		}

		return c;
	}

}