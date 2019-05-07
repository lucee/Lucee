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
package lucee.runtime.functions.xml;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.net.rpc.client.WSClient;

public class AddSOAPRequestHeader implements Function {

	private static final long serialVersionUID = 4305004275924545217L;

	public static boolean call(PageContext pc, Object client, String nameSpace, String name, Object value) throws PageException {
		return call(pc, client, nameSpace, name, value, false);
	}

	public static boolean call(PageContext pc, Object client, String nameSpace, String name, Object value, boolean mustUnderstand) throws PageException {
		if (!(client instanceof WSClient))
			throw new FunctionException(pc, "addSOAPRequestHeader", 1, "webservice", "value must be a webservice Object generated with createObject/<cfobject>");
		((WSClient) client).addSOAPRequestHeader(nameSpace, name, value, mustUnderstand);
		return true;
	}
}