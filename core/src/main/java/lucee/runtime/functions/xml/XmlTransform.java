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
 * Implements the CFML Function xmltransform
 */
package lucee.runtime.functions.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.XMLException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.type.Struct;

public final class XmlTransform implements Function {

	public static String call(PageContext pc, Object oXml, String xsl) throws PageException {
		return call(pc, oXml, xsl, null);
	}

	public static String call(PageContext pc, Object oXml, String xsl, Struct parameters) throws PageException {
		try {
			Document doc;
			if (oXml instanceof String) {
				doc = XMLUtil.parse(XMLUtil.toInputSource(pc, oXml.toString()), null, false);
			}
			else if (oXml instanceof Node) doc = XMLUtil.getDocument((Node) oXml);
			else throw new XMLException("XML Object is of invalid type, must be a XML String or a XML Object", "now it is " + Caster.toClassName(oXml));

			return XMLUtil.transform(doc, XMLUtil.toInputSource(pc, xsl), parameters);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}
}