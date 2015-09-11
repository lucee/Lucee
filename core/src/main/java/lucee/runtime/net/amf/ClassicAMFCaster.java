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
package lucee.runtime.net.amf;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import lucee.commons.lang.CFTypes;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Component;
import lucee.runtime.ComponentSpecificAccess;
import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.component.ComponentLoader;
import lucee.runtime.component.Property;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.Constants;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.Image;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.op.Duplicator;
import lucee.runtime.text.xml.XMLCaster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.wrap.ArrayAsList;
import lucee.runtime.type.wrap.ListAsArray;
import lucee.runtime.type.wrap.MapAsStruct;

import org.w3c.dom.Node;

import flex.messaging.io.amf.ASObject;


/**
 * Cast a CFML object to AMF Objects and the other way
 */
class ClassicAMFCaster implements AMFCaster {

	
	
	private static final Collection.Key REMOTING_FETCH = KeyImpl.intern("remotingFetch");

	//private static ClassicAMFCaster singelton;
	
	protected boolean forceCFCLower;
	protected boolean forceStructLower;
	protected boolean forceQueryLower;

	private int methodAccessLevel;

	@Override
	public void init(Map arguments){
		forceCFCLower=Caster.toBooleanValue(arguments.get("force-cfc-lowercase"),false);
		forceQueryLower=Caster.toBooleanValue(arguments.get("force-query-lowercase"),false);
		forceStructLower=Caster.toBooleanValue(arguments.get("force-struct-lowercase"),false);
		// method access level
		String str=Caster.toString(arguments.get("method-access-level"),"remote");
		if("private".equalsIgnoreCase(str))methodAccessLevel=Component.ACCESS_PRIVATE;
		else if("package".equalsIgnoreCase(str))methodAccessLevel=Component.ACCESS_PACKAGE;
		else if("public".equalsIgnoreCase(str))methodAccessLevel=Component.ACCESS_PUBLIC;
		else methodAccessLevel=Component.ACCESS_REMOTE;
		
	}
	

	@Override
	public Object toAMFObject(Object cf) throws PageException {
		if(cf instanceof Node) return toAMFObject((Node)cf);
		if(cf instanceof List) return toAMFObject((List)cf);
		if(cf instanceof Array) return toAMFObject(ArrayAsList.toList((Array)cf));
		if(cf instanceof Component)	return toAMFObject((Component)cf);
		if(cf instanceof Query) return toAMFObject((Query)cf);
		if(cf instanceof Image) return toAMFObject((Image)cf);
		if(cf instanceof Map) return toAMFObject((Map)cf);
		if(cf instanceof Object[]) return toAMFObject((Object[])cf);
		
		return cf;
	}

	protected Object toAMFObject(Node node) {
		return XMLCaster.toRawNode(node);
	}
	protected Object toAMFObject(Query query) throws PageException {
		List<ASObject> result = new ArrayList<ASObject>();
		int len=query.getRecordcount();
        Collection.Key[] columns=CollectionUtil.keys(query);
    	ASObject row;
        for(int r=1;r<=len;r++) {
        	result.add(row = new ASObject());
            for(int c=0;c<columns.length;c++) {
                row.put(toString(columns[c],forceQueryLower), toAMFObject(query.getAt(columns[c],r)) ); 
            }
        }
		return result;
	}
	
	protected Object toAMFObject(Image img) throws PageException {
		try{
			return img.getImageBytes(null);
		}
		catch(Throwable t){
			return img.getImageBytes("png");
		}
	}

	protected ASObject toAMFObject(Component cfc) throws PageException {
		ASObject aso = new ASObject();
		aso.setType(cfc.getCallName());
		
		
		Component c=ComponentSpecificAccess.toComponentSpecificAccess(methodAccessLevel,cfc);

		Property[] prop = cfc.getProperties(false);
		Object v; UDF udf;
    	if(prop!=null)for(int i=0;i<prop.length;i++) {
    		boolean remotingFetch = Caster.toBooleanValue(prop[i].getDynamicAttributes().get(REMOTING_FETCH,Boolean.TRUE),true);
    		if(!remotingFetch) continue;
    		
    		v=cfc.get(prop[i].getName(),null);
    		if(v==null){
    			v=c.get("get"+prop[i].getName(),null);
	    		if(v instanceof UDF){
	            	udf=(UDF) v;
	            	if(udf.getReturnType()==CFTypes.TYPE_VOID) continue;
	            	if(udf.getFunctionArguments().length>0) continue;
	            	
	            	try {
						v=c.call(ThreadLocalPageContext.get(), udf.getFunctionName(), ArrayUtil.OBJECT_EMPTY);
					} catch (PageException e) {
						continue;
					}
	            }
    		}
    		
    		aso.put(toString(prop[i].getName(),forceCFCLower), toAMFObject(v));
    	}
    	return aso;
	}
    
	protected Object toAMFObject(Map map) throws PageException {
    	if(forceStructLower && map instanceof Struct) toAMFObject((Struct)map);
    	
    	map=(Map) Duplicator.duplicate(map,false);
    	Iterator it = map.entrySet().iterator();
        Map.Entry entry;
        while(it.hasNext()) {
            entry=(Entry) it.next();
            entry.setValue(toAMFObject(entry.getValue()));
        }
        return MapAsStruct.toStruct(map, false);
    }
    
	protected Object toAMFObject(Struct src) throws PageException {
    	Struct trg=new StructImpl();
    	//Key[] keys = src.keys();
    	Iterator<Entry<Key, Object>> it = src.entryIterator();
    	Entry<Key, Object> e;
        while(it.hasNext()) {
        	e = it.next();
            trg.set(KeyImpl.init(toString(e.getKey(),forceStructLower)), toAMFObject(e.getValue()));
        }
        return trg;
    }
    
    
	
	protected Object toAMFObject(List list) throws PageException {
		Object[] trg=new Object[list.size()];
		ListIterator it = list.listIterator();
        
        while(it.hasNext()) {
        	trg[it.nextIndex()]=toAMFObject(it.next());
        }
        return trg;
    }
	
	protected Object toAMFObject(Object[] src) throws PageException {
		Object[] trg=new Object[src.length];
		for(int i=0;i<src.length;i++){
			trg[i]=toAMFObject(src[i]);
		}
		return trg;
    }
	

	@Override
	public Object toCFMLObject(Object amf) throws PageException {
		if(amf instanceof Node) return toCFMLObject((Node)amf);
		if(amf instanceof List) return toCFMLObject((List)amf);
		if(Decision.isNativeArray(amf)) {
			if(amf instanceof byte[]) return amf;
			if(amf instanceof char[]) return new String((char[])amf);
			return toCFMLObject(Caster.toNativeArray(amf));
		}
		if(amf instanceof ASObject) return toCFMLObject((ASObject)amf);
		if(amf instanceof Map) return toCFMLObject((Map)amf);
		if(amf instanceof Date) return new DateTimeImpl((Date)amf);
        if(amf == null) return "";
        
		return amf;
	}

	protected Object toCFMLObject(Node node) {
		return XMLCaster.toXMLStruct(node, true);
    }
	protected Object toCFMLObject(Object[] arr) throws PageException {
		Array trg=new ArrayImpl();
		for(int i=0;i<arr.length;i++){
			trg.setEL(i+1, toCFMLObject(arr[i]));
		}
		return trg;
    }
	
	protected Object toCFMLObject(List list) throws PageException {
        ListIterator it = list.listIterator();
        while(it.hasNext()) {
        	//arr.setE(it.nextIndex()+1, toCFMLObject(it.next()));
            list.set(it.nextIndex(),toCFMLObject(it.next()));
        }
        return ListAsArray.toArray(list);
    }

	protected Object toCFMLObject(Map map) throws PageException {
		Iterator it = map.entrySet().iterator();
        Map.Entry entry;
        while(it.hasNext()) {
            entry=(Entry) it.next();
            entry.setValue(toCFMLObject(entry.getValue()));
        }
        return MapAsStruct.toStruct(map, false);
    }
	
	protected Object toCFMLObject(ASObject aso) throws PageException {
		if(!StringUtil.isEmpty(aso.getType())){
			PageContext pc = ThreadLocalPageContext.get();
			ConfigWeb config = pc.getConfig();
			
				String name="/"+aso.getType().replace('.', '/')+"."+Constants.getCFMLComponentExtension();

				Page p = PageSourceImpl.loadPage(pc, ((PageContextImpl)pc).getPageSources(name), null) ;

				if(p==null)throw new ApplicationException("Could not find a Component with name ["+aso.getType()+"]");
				
				Component cfc = ComponentLoader.loadComponent(pc, p,  aso.getType(), false,false,false,true);
				ComponentSpecificAccess cw=ComponentSpecificAccess.toComponentSpecificAccess(config.getComponentDataMemberDefaultAccess(),cfc);
				
				Iterator it = aso.entrySet().iterator();
				Map.Entry entry;
				while(it.hasNext()){
					entry = (Entry) it.next();
					cw.set(KeyImpl.toKey(entry.getKey()), toCFMLObject(entry.getValue()));
				}
				return cfc;
			
			
		}
		return toCFMLObject((Map)aso);
    }
	
	protected String toString(Object key, boolean forceLower) {
		if(key instanceof Key) return toString((Key)key, forceLower);
		return toString(Caster.toString(key,""), forceLower);
	}
	
	protected String toString(Key key, boolean forceLower) {
		if(forceLower) return key.getLowerString();
		return key.getString();
	}
	
	protected String toString(String key, boolean forceLower) {
		if(forceLower) return key.toLowerCase();
		return key;
	}
}