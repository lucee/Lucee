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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import lucee.commons.lang.CFTypes;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Component;
import lucee.runtime.ComponentSpecificAccess;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.wrap.ArrayAsList;
import flex.messaging.io.amf.ASObject;


/**
 * Cast a CFML object to AMF Objects and the other way
 */
final class ModernAMFCaster extends ClassicAMFCaster {

	private boolean doProperties=true;
	private boolean doGetters=true;
	private boolean doRemoteValues=true;
	
	@Override
	public void init(Map arguments){
		super.init(arguments);
		
		String strValues = Caster.toString(arguments.get("component-values"),null);
		if(!StringUtil.isEmpty(strValues)){
			doProperties = lucee.runtime.type.util.ListUtil.listFindNoCase(strValues, "properties", ",")!=-1;
			doGetters=lucee.runtime.type.util.ListUtil.listFindNoCase(strValues, "getters", ",")!=-1;
			doRemoteValues=lucee.runtime.type.util.ListUtil.listFindNoCase(strValues, "remote-values", ",")!=-1;
		}
	}

	@Override
	public Object toAMFObject(Object cf) throws PageException {
		if(cf instanceof List) return toAMFObject((List)cf);
		if(cf instanceof Array) return toAMFObject(ArrayAsList.toList((Array)cf));
		if(cf instanceof Component)	return toAMFObject((Component)cf);
		if(cf instanceof Query) return super.toAMFObject((Query)cf);
		if(cf instanceof Map) return super.toAMFObject((Map)cf);
		if(cf instanceof Object[]) return toAMFObject((Object[])cf);
		
		return cf;
	}
	

	@Override
	protected ASObject toAMFObject(Component cfc) throws PageException {
		// add properties
		ASObject aso = doProperties?super.toAMFObject(cfc):new ASObject();
		ComponentSpecificAccess cw=ComponentSpecificAccess.toComponentSpecificAccess(Component.ACCESS_REMOTE,cfc);
		
		Iterator it = cfc.entrySet().iterator();
        Map.Entry entry;
        Object v;
        Collection.Key k;
        UDF udf;
        String name;
        while(it.hasNext()) {
            entry=(Entry) it.next();
            k=KeyImpl.toKey(entry.getKey());
            v=entry.getValue();
            
            // add getters
            if(v instanceof UDF){
            	if(!doGetters) continue;
            	udf=(UDF) v;
            	name=udf.getFunctionName();
            	if(!StringUtil.startsWithIgnoreCase(name, "get"))continue;
            	if(udf.getReturnType()==CFTypes.TYPE_VOID) continue;
            	if(udf.getFunctionArguments().length>0) continue;
            	
            	try {
					v=cfc.call(ThreadLocalPageContext.get(), name, ArrayUtil.OBJECT_EMPTY);
				} catch (PageException e) {
					continue;
				}
            	name=name.substring(3);
            	
            	aso.put(toString(name,forceCFCLower), toAMFObject(v));
            }
            
            // add remote data members
            if(cw!=null && doRemoteValues){
            	v=cw.get(k,null);
            	if(v!=null)aso.put(toString(k,forceCFCLower), toAMFObject(v));
            }
        }
        return aso;
	}
    
	@Override
	protected Object toAMFObject(List list) throws PageException {
		list = Duplicator.duplicateList(list, false);
        ListIterator it = list.listIterator();
        while(it.hasNext()) {
        	list.set(it.nextIndex(),toAMFObject(it.next()));
        }
        return list;
    }
	
	@Override
	protected Object toAMFObject(Object[] src) throws PageException {
		ArrayList list=new ArrayList();
		for(int i=0;i<src.length;i++){
			list.add(toAMFObject(src[i]));
		}
		return list;
    }
}