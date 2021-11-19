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
package lucee.runtime.functions.decision;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lucee.runtime.PageContext;
import lucee.runtime.ext.function.Function;
import lucee.runtime.text.xml.struct.XMLStruct;

/**
 * Check if a value is a XML Attribut (XML Attr Node)
 */
public final class IsXmlAttribute implements Function {

	public static boolean call(PageContext pc, Object value) {

		if (value instanceof Attr) return true;
		else if (value instanceof NodeList) return ((NodeList) value).item(0).getNodeType() == Node.ATTRIBUTE_NODE;
		else if (value instanceof XMLStruct) return ((XMLStruct) value).getNodeType() == Node.ATTRIBUTE_NODE;
		return false;
	}
}