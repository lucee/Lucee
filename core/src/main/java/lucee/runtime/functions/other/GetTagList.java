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
 * Implements the CFML Function getfunctionlist
 */
package lucee.runtime.functions.other;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibTag;

public final class GetTagList implements Function {

	private static final long serialVersionUID = -5143967669895264247L;

	public static lucee.runtime.type.Struct call(PageContext pc) throws PageException {
		return call(pc, null);
	}

	public static lucee.runtime.type.Struct call(PageContext pc, String extensionFilter) throws PageException {
		Struct sct = new StructImpl(StructImpl.TYPE_LINKED);
		TagLib[] tlds;
		TagLibTag tag;
		tlds = ((ConfigPro) pc.getConfig()).getTLDs();

		boolean filterByExtension = extensionFilter != null && !extensionFilter.isEmpty();

		for (int i = 0; i < tlds.length; i++) {
			String ns = tlds[i].getNameSpaceAndSeparator();

			Map<String, TagLibTag> tags = tlds[i].getTags();
			Iterator<String> it = tags.keySet().iterator();
			ArrayList<String> tagList = new ArrayList<>();
			while (it.hasNext()) {
				Object n = it.next();
				tag = tlds[i].getTag(n.toString());
				if (tag.getStatus() != TagLib.STATUS_HIDDEN && tag.getStatus() != TagLib.STATUS_UNIMPLEMENTED) {
					// inner.set(n.toString(), "");
					if (filterByExtension) {
						String src = tag.getSource();
						if (src == null || (!src.equalsIgnoreCase(extensionFilter) && !src.contains(extensionFilter))) continue;
					}
					tagList.add(n.toString());
				}
			}
			if (!tagList.isEmpty() || !filterByExtension) {
				Collections.sort(tagList);
				Struct inner = new StructImpl(StructImpl.TYPE_LINKED);
				sct.set(ns, inner);
				for (String t: tagList) {
					inner.put(t, "");
				}
			}
		}
		return sct;
	}
}
