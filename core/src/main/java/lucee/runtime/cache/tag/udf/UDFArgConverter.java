/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.runtime.cache.tag.udf;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.text.xml.struct.XMLStruct;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;

public class UDFArgConverter {

	public static String serialize(Object o)  {
		return serialize(o,new HashSet<Object>());
	}
	
	
	private static String serialize(Object o,Set<Object> done)  {
		
		if(o==null) return "null";
		Object raw=toRaw(o);
		
		if(done.contains(raw)) return "parent reference";
		done.add(raw);
		Collection c=null;
		Object other=null;
		try{
			if((c=Caster.toCollection(o,null))!=null) {
				if(o!=c){
					done.add(c);
					other=c;
				}
				return serializeCollection(c,done);
			}
			/*if(o instanceof String) {
				return "'"+escape((String)o)+"'";
			}
			if(o instanceof SimpleValue || o instanceof Number || o instanceof Boolean)
				return Caster.toString(o,"");
			*/
			
			return o.toString();
		}
		finally {
			if(other!=null) done.remove(other);
			done.remove(raw);
		}
	}

	private static Object toRaw(Object o) {
		if(o instanceof XMLStruct)return ((XMLStruct)o).toNode();
		return o;
	}


	private static String serializeCollection(Collection coll, Set<Object> done) {
		if(coll instanceof Query) {
			Query qry=(Query) coll;
			StringBuilder sb=new StringBuilder(8192);
			
			Iterator<Key> it = qry.keyIterator();
			Key k;
			sb.append("{");
			int len=qry.getRecordcount();
			while(it.hasNext()) {
				k = it.next();
			    sb.append(',');
			    sb.append(k.getLowerString());
	            sb.append('[');
				boolean doIt=false;
				for(int y=1;y<=len;y++) {
				    if(doIt)sb.append(',');
				    doIt=true;
				    try {
						sb.append(serialize(qry.getAt(k,y),done));
					} catch (PageException e) {
						sb.append(serialize(e.getMessage(),done));
					}
				}
				sb.append(']');
			}
			
			sb.append('}');
			return sb.toString();
		}
		
		
		StringBuilder sb=new StringBuilder("{");
		Iterator<Entry<Key, Object>> it = coll.entryIterator();
		Entry<Key, Object> e;
		boolean notFirst=false;
		while(it.hasNext()) {
			if(notFirst)sb.append(",");
			e=it.next();
			sb.append(e.getKey().getLowerString());
			sb.append(":");
			sb.append(serialize(e.getValue(),done));
			notFirst=true;
		}
		
		return sb.append("}").toString();
	}
	
	private static String escape(String str) {
        return StringUtil.replace(str,"'","''",false);
    }
}