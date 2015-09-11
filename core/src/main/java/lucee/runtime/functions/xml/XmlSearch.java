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

import javax.xml.transform.TransformerException;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.text.xml.XMLCaster;
import lucee.runtime.text.xml.struct.XMLObject;
import lucee.runtime.text.xml.struct.XMLStruct;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;

import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implements the CFML Function xmlsearch
 */
public final class XmlSearch implements Function {

	public static Object call(PageContext pc , Node node, String expr) throws PageException {
		boolean caseSensitive=true;
		if(node instanceof XMLObject) {
			caseSensitive=((XMLObject)node).getCaseSensitive();
		}
		if(node instanceof XMLStruct) {
			node=((XMLStruct)node).toNode();
		}
		return _call(node,expr,caseSensitive);
		
	}
	public static Object _call(Node node, String expr, boolean caseSensitive) throws PageException {
		if(StringUtil.endsWith(expr,'/')) 
			expr = expr.substring(0,expr.length()-1);
		try {
			XObject rs = XPathAPI.eval(node,expr);
			
			switch(rs.getType()){
			case XObject.CLASS_NODESET:
				return nodelist(rs,caseSensitive);
			case XObject.CLASS_BOOLEAN:
				return Caster.toBoolean(rs.bool());
			case XObject.CLASS_NULL:
				return "";
			case XObject.CLASS_NUMBER:
				return Caster.toDouble(rs.num());
			case XObject.CLASS_STRING:
				return rs.str();
			default:
				return rs.object();
			}
		} catch (Throwable e) {
			throw Caster.toPageException(e);
		}
		
		
		
	}
	private static Array nodelist(XObject rs, boolean caseSensitive) throws TransformerException, PageException {
		
		NodeList list = rs.nodelist();
		int len=list.getLength();
		Array rtn=new ArrayImpl();
		for(int i=0;i<len;i++) {
			Node n=list.item(i);
			if(n !=null)
			rtn.append(XMLCaster.toXMLStruct(n,caseSensitive));
		}
		return rtn;
	}
}