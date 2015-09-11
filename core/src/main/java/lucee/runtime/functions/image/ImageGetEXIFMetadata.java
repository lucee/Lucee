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
package lucee.runtime.functions.image;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.Image;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public class ImageGetEXIFMetadata {

	public static Struct call(PageContext pc, Object name) throws PageException {
		//if(name instanceof String) name=pc.getVariable(Caster.toString(name));
		Image img = Image.toImage(pc,name);
		return getData(img);
	}

	public static Struct getData(Image img) throws PageException {
		Struct sct = img.info(),data=new StructImpl();
		Iterator it = sct.entrySet().iterator();
		Map.Entry entry;
		while(it.hasNext()){
			entry=(Entry) it.next();
			if(entry.getValue() instanceof Map) 
				fill(data,(Map)entry.getValue());
			else if(entry.getValue() instanceof List) 
				fill(data,entry.getKey(),(List)entry.getValue());
			else
				data.put(entry.getKey(),entry.getValue());
		}
		
		return data;
	}

	private static void fill(Struct data, Map map) throws PageException {
		Iterator it = map.entrySet().iterator();
		Map.Entry entry;
		while(it.hasNext()){
			entry=(Entry) it.next();
			if(entry.getValue() instanceof Map) 
				fill(data,(Map)entry.getValue());
			else if(entry.getValue() instanceof List) 
				fill(data,entry.getKey(),(List)entry.getValue());
			else
				data.put(entry.getKey(),entry.getValue());
		}
	}

	private static void fill(Struct data, Object key, List list) throws PageException {
		data.put(
				key,
				lucee.runtime.type.util.ListUtil.listToList(list, ","));
	}
}