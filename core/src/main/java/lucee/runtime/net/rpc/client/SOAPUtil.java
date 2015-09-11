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
package lucee.runtime.net.rpc.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.namespace.QName;

import lucee.commons.lang.Pair;
import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.text.xml.ArrayNodeList;
import lucee.runtime.text.xml.XMLUtil;

import org.apache.axis.Constants;
import org.apache.axis.wsdl.symbolTable.BaseType;
import org.apache.axis.wsdl.symbolTable.DefinedType;
import org.apache.axis.wsdl.symbolTable.ElementDecl;
import org.apache.axis.wsdl.symbolTable.SymbolTable;
import org.apache.axis.wsdl.symbolTable.Type;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class SOAPUtil {
	
	private static QName[] SOAP=new QName[]{
		Constants.SOAP_ARRAY
		,Constants.SOAP_ARRAY12
		,Constants.SOAP_ARRAY_ATTRS11 
		,Constants.SOAP_ARRAY_ATTRS12
		,Constants.SOAP_BASE64
		,Constants.SOAP_BASE64BINARY
		,Constants.SOAP_BOOLEAN
		,Constants.SOAP_BYTE
		,Constants.SOAP_COMMON_ATTRS11
		,Constants.SOAP_COMMON_ATTRS12
		,Constants.SOAP_DECIMAL
		,Constants.SOAP_DOCUMENT
		,Constants.SOAP_DOUBLE
		,Constants.SOAP_ELEMENT
		,Constants.SOAP_FLOAT
		,Constants.SOAP_INT
		,Constants.SOAP_INTEGER
		,Constants.SOAP_LONG
		,Constants.SOAP_MAP
		,Constants.SOAP_SHORT
		,Constants.SOAP_STRING
		,Constants.SOAP_VECTOR
	};
	
	private static QName[] XSD=new QName[]{
		Constants.XSD_ANY
		,Constants.XSD_ANYSIMPLETYPE
		,Constants.XSD_ANYTYPE
		,Constants.XSD_ANYURI
		,Constants.XSD_BASE64
		,Constants.XSD_BOOLEAN
		,Constants.XSD_BYTE
		,Constants.XSD_DATE
		,Constants.XSD_DATETIME
		,Constants.XSD_DAY
		,Constants.XSD_DECIMAL
		,Constants.XSD_DOUBLE
		,Constants.XSD_DURATION
		,Constants.XSD_ENTITIES
		,Constants.XSD_ENTITY
		,Constants.XSD_FLOAT
		,Constants.XSD_HEXBIN
		,Constants.XSD_ID
		,Constants.XSD_IDREF
		,Constants.XSD_IDREFS
		,Constants.XSD_INT
		,Constants.XSD_INTEGER
		,Constants.XSD_LANGUAGE
		,Constants.XSD_LONG
		,Constants.XSD_MONTH
		,Constants.XSD_MONTHDAY
		,Constants.XSD_NAME
		,Constants.XSD_NCNAME
		,Constants.XSD_NEGATIVEINTEGER
		,Constants.XSD_NMTOKEN
		,Constants.XSD_NMTOKENS
		,Constants.XSD_NONNEGATIVEINTEGER
		,Constants.XSD_NONPOSITIVEINTEGER
		,Constants.XSD_NORMALIZEDSTRING
		,Constants.XSD_NOTATION
		,Constants.XSD_POSITIVEINTEGER
		,Constants.XSD_QNAME
		,Constants.XSD_SCHEMA
		,Constants.XSD_SHORT
		,Constants.XSD_STRING
		,Constants.XSD_TIME
		,Constants.XSD_TIMEINSTANT1999
		,Constants.XSD_TIMEINSTANT2000
		,Constants.XSD_TOKEN
		,Constants.XSD_UNSIGNEDBYTE
		,Constants.XSD_UNSIGNEDINT
		,Constants.XSD_UNSIGNEDLONG
		,Constants.XSD_UNSIGNEDSHORT
		,Constants.XSD_YEAR
		,Constants.XSD_YEARMONTH
	};
	
	
	public static Vector getTypes(Element body, SymbolTable st ) throws ApplicationException {
		
		
		// get the data
		List<TempType> hrefs=new ArrayList<SOAPUtil.TempType>();
		Map<String,TempType> ids=new HashMap<String,SOAPUtil.TempType>();
		ArrayList<TempType> res = new ArrayList<SOAPUtil.TempType>();
		toTempTypes(XMLUtil.getChildNodes(body, Node.ELEMENT_NODE).iterator(),res,hrefs,ids,res);
		
		// replace href with real data
		Iterator<TempType> it = hrefs.iterator();
		TempType href,id;
		while(it.hasNext()){
			href = it.next();
			id=ids.get(href.href);
			if(StringUtil.isEmpty(id)) throw new ApplicationException("cannot handle href "+href.href);
			
			href.href=null;
			href.id=id.id;
			href.prefix=id.prefix;
			href.namespace=id.namespace;
			href.type=id.type;
			href.children=id.children;
			id.replicated=true;
		}
		
		
		// removes replicated types in root
		it = res.iterator();
		TempType t;
		while(it.hasNext()){
			t=it.next();
			if(t.replicated)it.remove();
		}
		
		// now convert to types
		return toTypes(res,false);
	}
	
	private static Vector toTypes(List<TempType> res,boolean contained) {
		Iterator<TempType> it = res.iterator();
		Vector types=new Vector();
		Type t;
		TempType tt;
		Object o;
		while(it.hasNext()){
			tt = it.next();
			o=t=toType(tt);
			if(contained)o=new ElementDecl(t,new QName(tt.name));
			types.add(o);
		}
		return types;
	}

	private static Type toType(TempType tt) {
		Type t=toBaseType(tt.prefix, tt.type);
		if(t==null) t=toDefinedType(tt);
		
		
		return t;
	}

	private static DefinedType toDefinedType(TempType tt) {
		if(tt.isArray) {
			tt.isArray=false;
			DefinedType ref = toDefinedType(tt);
			String type=ref.getQName().getLocalPart();
			if(type.startsWith("ArrayOf")) type="ArrayOf"+type;
			else type="ArrayOf:"+type;
			
			QName qn = StringUtil.isEmpty(tt.namespace)?new QName(type):new QName(tt.namespace,type);
			DefinedType dt=new DefinedType(qn, tt.parent);
			dt.setRefType(ref);
		}
		
		QName qn = StringUtil.isEmpty(tt.namespace)?new QName(tt.type):new QName(tt.namespace,tt.type);
		DefinedType dt=new DefinedType(qn, tt.parent);
		dt.setBaseType(false);
		// children
		if(tt.children!=null && tt.children.size()>0) {
			dt.setContainedElements(toTypes(tt.children,true));
		}
		
		return dt;
	}

	private static void toTempTypes(Iterator<? extends Node> it,List<TempType> children,List<TempType> hrefs,Map<String,TempType> ids,List<TempType> root) {
		Element e;
		TempType t;
		while(it.hasNext()){
			e=(Element)it.next();
			if(StringUtil.isEmpty(e.getAttribute("xsi:type")) && StringUtil.isEmpty(e.getAttribute("soapenc:type")) && StringUtil.isEmpty(e.getAttribute("href"))) continue;
			t=toTempType(e,hrefs,ids,root);
			children.add(t);
		}
	}
	
	private static TempType toTempType(Element e,List<TempType> hrefs,Map<String,TempType> ids,List<TempType> root) {
		String name=e.getLocalName();
		Pair<String, String> arrayType=null;
		// type and namespace
		int index;
		Pair<String, String> type = parseType(e.getAttribute("xsi:type"));
		
		// optional values
		String id=e.getAttribute("id");
		String href=e.getAttribute("href");
		String array=e.getAttribute("soapenc:arrayType");
		
		// is Array
		if(!StringUtil.isEmpty(type.getValue()) && !StringUtil.isEmpty(array)) {
			arrayType=type;
			array=array.trim();
			array=array.substring(0,array.indexOf('['));
			type = parseType(array);
		}
		
		// namespace
		String namespace=e.getAttribute("xmlns:"+type.getName());
		if(StringUtil.isEmpty(namespace)) {
			NamedNodeMap attrs = e.getAttributes();
			int len = attrs.getLength();
	        Attr attr;
	        for(int i=0;i<len;i++) {
	        	attr=(Attr)attrs.item(i);
	        	if(attr.getName().startsWith("xmlns:"))
	        		namespace=attr.getValue();
	        }
		}
		
		
		TempType t = new TempType(namespace,name,type.getName(),type.getValue(),id,href,arrayType!=null,e.getParentNode());
		ArrayNodeList children = XMLUtil.getChildNodes(e, Node.ELEMENT_NODE);
		if(children!=null && children.size()>0) {
			List<TempType> _children = new ArrayList<SOAPUtil.TempType>();
			// if no array type, the children are members
			if(arrayType==null){
				toTempTypes(children.iterator(),_children,hrefs,ids,root);
				t.setChildren(_children);
				
			}
			// no members just values in the array
			else {
				toTempTypes(children.iterator(),_children,hrefs,ids,root);
				
				// make sure we have every type only once
				Map<String, TempType> tmp=new HashMap<String, SOAPUtil.TempType>();
				Iterator<TempType> it = _children.iterator();
				TempType tt;
				while(it.hasNext()){
					tt=it.next();
					tmp.put(tt.prefix+":"+tt.type, tt);
				}
				
				it=tmp.values().iterator();
				while(it.hasNext()){
					tt=it.next();
					root.add(tt);
				}
			}
		}

		if(!StringUtil.isEmpty(href)) hrefs.add(t);
		if(!StringUtil.isEmpty(id)) ids.put(id,t);
		
		return t;
		
	}
	
	private static Pair<String,String> parseType(String strType) { 
		int index;
		String ns=null;
		if(!StringUtil.isEmpty(strType) && (index=strType.indexOf(':'))!=-1) {
			ns=strType.substring(0,index);
			strType=strType.substring(index+1);
		}
		return new Pair<String, String>(ns, strType);
	}

	private static BaseType toBaseType(String ns, String type) {
		if(StringUtil.isEmpty(ns) || StringUtil.isEmpty(type)) return null;
		
		if("xsd".equalsIgnoreCase(ns)) {
			for(int i=0;i<XSD.length;i++){
				if(XSD[i].getLocalPart().equalsIgnoreCase(type.trim()))
					return new BaseType(XSD[i]);
			}
		}
		if("soap".equalsIgnoreCase(ns) || "soapenc".equalsIgnoreCase(ns)) {
			for(int i=0;i<SOAP.length;i++){
				if(SOAP[i].getLocalPart().equalsIgnoreCase(type.trim()))
					return new BaseType(SOAP[i]);
			}
		}
		return null;
	}
	
	

	public static class TempType {

		public boolean replicated;
		private String type;
		private String id;
		private String prefix;
		private String namespace;
		private String name;
		private String href;
		private List<TempType> children;
		private boolean isArray;
		private Node parent;

		public TempType(String namespace, String name, String prefix, String type,String id,String href, boolean isArray, Node parent) {
			if(!StringUtil.isEmpty(href)) {
				href=href.trim();
				if(href.startsWith("#"))href=href.substring(1);
			}
			
			this.namespace=!StringUtil.isEmpty(namespace)?namespace.trim():null;
			this.id=!StringUtil.isEmpty(id)?id.trim():null;
			this.type=!StringUtil.isEmpty(type)?type.trim():null;
			this.prefix=!StringUtil.isEmpty(prefix)?prefix.trim():null;
			this.href=href;
			this.name=!StringUtil.isEmpty(name)?name.trim():null;
			this.isArray=isArray;
			this.parent=parent;
		}

		public void setChildren(List<TempType> children) {
			this.children=children;
		}
		public List<TempType> getChildren() {
			return children;
		}

		@Override
		public String toString() {
			StringBuilder sb=new StringBuilder();
			if(!StringUtil.isEmpty(name))sb.append("name:").append(name);
			if(!StringUtil.isEmpty(id))sb.append(";id:").append(id);
			if(!StringUtil.isEmpty(prefix))sb.append(";prefix:").append(prefix);
			if(!StringUtil.isEmpty(type))sb.append(";type:").append(type);
			if(!StringUtil.isEmpty(href))sb.append(";href:").append(href);
			sb.append(";array?:").append(isArray);
			
			if(children!=null && children.size()>0) {
				sb.append(";children:{\n");
				Iterator<TempType> it = children.iterator();
				while(it.hasNext()){
					sb.append('	').append(StringUtil.replace(it.next().toString(),"\n","\t\n",false)).append(",\n");
				}

				sb.append("}");
			}
			
			
			
			return sb.toString();
		}
	}
	
}