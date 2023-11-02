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
package lucee.runtime.text.xml.struct;

import org.w3c.dom.Node;

import lucee.runtime.text.xml.XMLNodeList;
import lucee.runtime.type.Struct;

/**
 * 
 */
public interface XMLStruct extends Struct, Node, XMLObject {
	/**
	 * @return casts XML Struct to a XML Node
	 */
	public Node toNode();

	/**
	 * @return returns the children of the Node
	 */
	public XMLNodeList getXMLNodeList();

	public boolean isCaseSensitive();
}