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
 * Implements the CFML Function xmlelemnew
 */
package lucee.runtime.functions.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.text.xml.struct.XMLStructFactory;
import lucee.runtime.type.util.ListUtil;

public final class XmlElemNew implements Function {

	private static final long serialVersionUID = -2601887739406776466L;

	public static Element call(PageContext pc, Node node, String childname) throws FunctionException {
		return call(pc, node, null, childname);
	}

	public static Element call(PageContext pc, Node node, String namespace, String childname) throws FunctionException {
		Document doc = XMLUtil.getDocument(node);

		if (StringUtil.isEmpty(childname)) {
			if (!StringUtil.isEmpty(namespace)) {
				childname = namespace;
				namespace = null;
			}
			else throw new FunctionException(pc, "XmlElemNew", 3, "childname", "argument is required");
		}

		Element el = null;

		// without namespace
		if (StringUtil.isEmpty(namespace)) {
			if (childname.indexOf(':') != -1) {
				String[] parts = ListUtil.listToStringArray(childname, ':');
				childname = parts[1];
				String prefix = parts[0];
				namespace = getNamespaceForPrefix(doc.getDocumentElement(), prefix);
				if (StringUtil.isEmpty(namespace)) {
					el = doc.createElement(childname);
				}
				else {
					el = doc.createElementNS(namespace, childname);
					el.setPrefix(prefix);
				}

			}
			else {
				el = doc.createElement(childname);
			}
		}
		// with namespace
		else {
			el = doc.createElementNS(namespace, childname);
		}
		return (Element) XMLStructFactory.newInstance(el, false);
	}

	private static String getNamespaceForPrefix(Node node, String prefix) {
		if (node == null) return null;
		NamedNodeMap atts = node.getAttributes();

		if (atts != null) {
			String currLocalName, currPrefix;
			int len = atts.getLength();
			for (int i = 0; i < len; i++) {
				Node currAttr = atts.item(i);
				currLocalName = currAttr.getLocalName();
				currPrefix = currAttr.getPrefix();
				if (prefix.equals(currLocalName) && "xmlns".equals(currPrefix)) {
					return currAttr.getNodeValue();
				}
				else if (StringUtil.isEmpty(prefix) && "xmlns".equals(currLocalName) && StringUtil.isEmpty(currPrefix)) {
					return currAttr.getNodeValue();
				}
			}
		}
		return null;
	}

}