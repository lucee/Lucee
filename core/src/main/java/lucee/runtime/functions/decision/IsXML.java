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

import java.io.StringReader;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import lucee.runtime.PageContext;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.text.xml.XMLUtil;

/**
 * Check if a String is a well-formed XML
 */
public final class IsXML implements Function {

	public static boolean call(PageContext pc, Object xml) {
		if (xml instanceof Node) return true;

		try {
			XMLUtil.parse(new InputSource(new StringReader(Caster.toString(xml))), null, false);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
}