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
 * Implements the CFML Function xmlnew
 */
package lucee.runtime.functions.xml;

import org.w3c.dom.Node;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.text.xml.XMLCaster;
import lucee.runtime.text.xml.XMLUtil;

public final class XmlNew implements Function {
	public static Node call(PageContext pc) throws PageException {
		return call(pc, false);
	}

	public static Node call(PageContext pc, boolean caseSensitive) throws PageException {
		try {
			return XMLCaster.toXMLStruct(XMLUtil.newDocument(), caseSensitive);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

}