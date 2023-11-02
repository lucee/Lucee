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
package lucee.runtime.functions.system;

import java.io.IOException;
import java.util.Iterator;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;

public class CallStackDump {

	public static String call(PageContext pc) throws PageException {
		return call(pc, null);
	}

	public static String call(PageContext pc, String output) throws PageException {
		Array arr = (Array) CallStackGet.call(pc);
		Struct sct = null;
		String func;

		// create stack
		StringBuilder sb = new StringBuilder();
		Iterator<Object> it = arr.valueIterator();
		while (it.hasNext()) {
			sct = (Struct) it.next();
			func = (String) sct.get(KeyConstants._function);
			sb.append(sct.get(KeyConstants._template));
			if (func.length() > 0) {
				sb.append(':');
				sb.append(func);
			}
			sb.append(':');
			sb.append(Caster.toString(sct.get(CallStackGet.LINE_NUMBER)));
			sb.append('\n');
		}

		// output
		try {
			if (StringUtil.isEmpty(output, true) || output.trim().equalsIgnoreCase("browser")) {
				pc.forceWrite("<pre>");
				pc.forceWrite(sb.toString());
				pc.forceWrite("</pre>");
			}
			else if (output.trim().equalsIgnoreCase("console")) {
				System.out.println(sb.toString());
			}
			else {
				Resource res = ResourceUtil.toResourceNotExisting(pc, output);
				IOUtil.write(res, sb.toString() + "\n", ((PageContextImpl) pc).getResourceCharset().name(), true);
			}
		}
		catch (IOException ioe) {
			throw Caster.toPageException(ioe);
		}

		return null;
	}
}