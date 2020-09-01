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
package lucee.runtime.functions.other;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.functions.arrays.ArraySort;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.util.ArrayUtil;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.function.FunctionLibFunction;
import lucee.transformer.library.tag.TagLib;

public class GetFunctionKeywords {
	private final static Object token = new Object();
	private static Array keywords = null;

	public static Array call(PageContext pc) throws PageException {
		synchronized (token) {
			if (keywords == null) {
				Set<String> set = new HashSet<String>();
				FunctionLib[] flds;
				flds = ((ConfigImpl) pc.getConfig()).getFLDs(pc.getCurrentTemplateDialect());
				Map<String, FunctionLibFunction> functions;
				Iterator<FunctionLibFunction> it;
				FunctionLibFunction flf;
				String[] arr;
				for (int i = 0; i < flds.length; i++) {
					functions = flds[i].getFunctions();
					it = functions.values().iterator();

					while (it.hasNext()) {
						flf = it.next();
						if (flf.getStatus() != TagLib.STATUS_HIDDEN && flf.getStatus() != TagLib.STATUS_UNIMPLEMENTED && !ArrayUtil.isEmpty(flf.getKeywords())) {
							arr = flf.getKeywords();
							if (arr != null) for (int y = 0; y < arr.length; y++) {
								set.add(arr[y].toLowerCase());
							}

						}
					}
				}
				keywords = Caster.toArray(set);
				ArraySort.call(pc, keywords, "textnocase");
				// }
			}
		}
		return keywords;
	}

}