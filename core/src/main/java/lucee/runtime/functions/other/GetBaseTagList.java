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
 * Implements the CFML Function getbasetaglist
 */
package lucee.runtime.functions.other;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.jsp.tagext.Tag;

import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.ext.function.Function;
import lucee.runtime.ext.tag.AppendixTag;
import lucee.runtime.tag.CFImportTag;
import lucee.runtime.tag.CFTag;
import lucee.runtime.tag.CFTagCore;
import lucee.runtime.tag.Module;
import lucee.runtime.type.util.ListUtil;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibTag;

public final class GetBaseTagList implements Function {
	public static String call(PageContext pc) {
		return call(pc, ",");
	}

	public static String call(PageContext pc, String delimiter) {
		Tag tag = pc.getCurrentTag();
		StringBuilder sb = new StringBuilder();
		while (tag != null) {
			if (sb.length() > 0) sb.append(delimiter);
			sb.append(getName(pc, tag));
			tag = tag.getParent();
		}
		return sb.toString();
	}

	private static String getName(PageContext pc, Tag tag) {
		Class clazz = tag.getClass();
		if (clazz == CFImportTag.class) clazz = CFTag.class;
		String className = clazz.getName();
		TagLib[] tlds = ((ConfigPro) pc.getConfig()).getTLDs(pc.getCurrentTemplateDialect());
		TagLibTag tlt;

		for (int i = 0; i < tlds.length; i++) {
			// String ns = tlds[i].getNameSpaceAndSeparator();

			Map tags = tlds[i].getTags();
			Iterator it = tags.keySet().iterator();

			while (it.hasNext()) {
				tlt = (TagLibTag) tags.get(it.next());
				if (tlt.getTagClassDefinition().isClassNameEqualTo(className)) {
					// custm tag
					if (tag instanceof AppendixTag) {
						AppendixTag atag = (AppendixTag) tag;
						if (atag.getAppendix() != null && !(tag instanceof Module)) {
							return tlt.getFullName().toUpperCase() + atag.getAppendix().toUpperCase();
						}
					}
					// built in cfc based custom tag
					if (tag instanceof CFTagCore) {
						if (((CFTagCore) tag).getName().equals(tlt.getAttribute("__name").getDefaultValue())) return tlt.getFullName().toUpperCase();
						continue;
					}

					return tlt.getFullName().toUpperCase();
				}
			}
		}
		return ListUtil.last(className, ".", true).toUpperCase();

	}
}