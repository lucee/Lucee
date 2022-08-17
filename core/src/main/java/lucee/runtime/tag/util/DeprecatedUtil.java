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
package lucee.runtime.tag.util;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;

public class DeprecatedUtil {

	public static void tagAttribute(String tagName, String attrName) {
		tagAttribute(null, tagName, attrName);
	}

	public static void tagAttribute(PageContext pc, String tagName, String attrName) {
		pc = ThreadLocalPageContext.get(pc);
		if (pc == null) return;
		LogUtil.log((pc), Log.LEVEL_ERROR, DeprecatedUtil.class.getName(), "attribute " + attrName + " of the tag " + tagName + " is no longer supported and ignored.");
	}

	public static void function(PageContext pc, String old) {
		pc = ThreadLocalPageContext.get(pc);
		if (pc == null) return;
		LogUtil.log((pc), Log.LEVEL_ERROR, DeprecatedUtil.class.getName(), "function " + old + " is deprecated");
	}

	public static void function(PageContext pc, String old, String replacement) {
		pc = ThreadLocalPageContext.get(pc);
		if (pc == null) return;
		LogUtil.log((pc), Log.LEVEL_ERROR, DeprecatedUtil.class.getName(), "function " + old + " is deprecated, please use instead function " + replacement);
	}

}