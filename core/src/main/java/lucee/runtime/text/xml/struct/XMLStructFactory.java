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

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * 
 */
public final class XMLStructFactory {
	/**
	 * @param node
	 * @param caseSensitive
	 * @return XMLStruct instance
	 */
	public static XMLStruct newInstance(Node node, boolean caseSensitive) {
		// TODO set Case Sensitive
		if (node instanceof XMLStruct) return ((XMLStruct) node);

		if (node instanceof Document) return new XMLDocumentStruct((Document) node, caseSensitive);
		else if (node instanceof Text) return new XMLTextStruct((Text) node, caseSensitive);
		else if (node instanceof CDATASection) return new XMLCDATASectionStruct((CDATASection) node, caseSensitive);
		else if (node instanceof Element) return new XMLElementStruct((Element) node, caseSensitive);
		else if (node instanceof Attr) return new XMLAttrStruct((Attr) node, caseSensitive);

		else return new XMLNodeStruct(node, caseSensitive);
	}
}