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
import java.util.Map.Entry;

import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.function.FunctionLibFunction;
import lucee.transformer.library.tag.TagLib;

public final class GetFunctionList implements Function {

	private static final long serialVersionUID = -7313412061811118382L;

	public static lucee.runtime.type.Struct call(PageContext pc) throws PageException {

		Struct sct = new StructImpl(StructImpl.TYPE_LINKED);
		// synchronized(sct) {
		// hasSet=true;
		FunctionLib flds = ((ConfigPro) pc.getConfig()).getFLDs();
		FunctionLibFunction func;
		Map<String, FunctionLibFunction> _functions;
		Iterator<Entry<String, FunctionLibFunction>> it;
		Entry<String, FunctionLibFunction> e;
		ArrayList<String> tagList = new ArrayList<>();
		_functions = flds.getFunctions();
		it = _functions.entrySet().iterator();

		while (it.hasNext()) {
			e = it.next();
			func = e.getValue();
			if (func.getStatus() != TagLib.STATUS_HIDDEN && func.getStatus() != TagLib.STATUS_UNIMPLEMENTED) {
				// sct.set(e.getKey(), "");
				tagList.add(e.getKey());
			}
		}
		Collections.sort(tagList);
		for (String t: tagList) {
			sct.put(t, "");
		}
		return sct;
	}
}