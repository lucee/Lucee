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
 * Implements the CFML Function gettemplatepath
 */
package lucee.runtime.functions.system;

import lucee.commons.lang.StringUtil;
import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;

public final class PagePoolList implements Function {

	private static final long serialVersionUID = 7743072823224800862L;

	public static Array call(PageContext pc) throws PageException {
		ArrayImpl arr = new ArrayImpl();
		fill(arr, ConfigWebUtil.getAllMappings(pc));
		return arr;
	}

	private static void fill(Array arr, Mapping[] mappings) throws PageException {
		if (mappings == null) return;
		MappingImpl mapping;
		for (int i = 0; i < mappings.length; i++) {
			mapping = (MappingImpl) mappings[i];
			mapping.getDisplayPathes(arr);
		}
	}

	public static String removeStartingSlash(String virtual) {
		virtual = virtual.trim();
		if (StringUtil.startsWith(virtual, '/')) virtual = virtual.substring(1);
		if (StringUtil.isEmpty(virtual)) return "root";
		return virtual;
	}
}