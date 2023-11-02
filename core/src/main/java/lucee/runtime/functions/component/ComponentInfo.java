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
/**
 * Implements the CFML Function createdate
 */
package lucee.runtime.functions.component;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.ext.function.Function;
import lucee.runtime.tag.util.DeprecatedUtil;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

/**
 * @deprecated use function GetMetaData instead
 *
 */
@Deprecated
public final class ComponentInfo implements Function {
	public static Struct call(PageContext pc, Component component) {
		DeprecatedUtil.function(pc, "ComponentInfo", "GetMetaData");
		Struct sct = new StructImpl();
		sct.setEL(KeyConstants._name, component.getName());
		sct.setEL(KeyConstants._fullname, component.getCallName());
		String extend = component.getExtends();
		if (extend == null || extend.length() == 0) extend = "Component"; // TODO Object instead?
		sct.setEL(KeyConstants._extends, extend);
		sct.setEL(KeyConstants._hint, component.getHint());

		return sct;
	}
}