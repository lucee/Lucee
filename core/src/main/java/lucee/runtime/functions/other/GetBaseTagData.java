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
 * Implements the CFML Function getbasetagdata
 */
package lucee.runtime.functions.other;

import javax.servlet.jsp.tagext.Tag;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.tag.CFTag;
import lucee.runtime.tag.CFTagCore;
import lucee.runtime.type.Struct;

public final class GetBaseTagData implements Function {

	private static final long serialVersionUID = -7016207088098049143L;

	public static Struct call(PageContext pc, String tagName) throws PageException {
		return call(pc, tagName, -1);
	}

	public static Struct call(PageContext pc, String tagName, double minLevel) throws PageException {
		CFTag tag = getParentCFTag(pc.getCurrentTag(), tagName, (int) minLevel);
		if (tag == null) throw new ExpressionException("can't find base tag with name [" + tagName + "]");
		return tag.getVariablesScope();
	}

	public static CFTag getParentCFTag(Tag tag, String trgTagName, int minLevel) {
		String pureName = trgTagName;
		int level = 0;
		CFTag cfTag;
		while (tag != null) {
			if (tag instanceof CFTag && minLevel <= (level++)) {
				cfTag = (CFTag) tag;
				if (cfTag instanceof CFTagCore) {

					CFTagCore tc = (CFTagCore) cfTag;

					if ((tc.getName() + "").equalsIgnoreCase(pureName)) return cfTag;
					if (StringUtil.startsWithIgnoreCase(pureName, "cf")) {
						pureName = pureName.substring(2);
					}
					if ((tc.getName() + "").equalsIgnoreCase(pureName)) return cfTag;
				}
				else if (cfTag.getAppendix().equalsIgnoreCase(pureName)) {
					return cfTag;
				}
				else if (StringUtil.startsWithIgnoreCase(pureName, "cf_")) {
					pureName = pureName.substring(3);
					if (cfTag.getAppendix().equalsIgnoreCase(pureName)) return cfTag;
				}
			}
			tag = tag.getParent();
		}
		return null;
	}
}