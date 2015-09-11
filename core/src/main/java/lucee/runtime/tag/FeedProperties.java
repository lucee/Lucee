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
package lucee.runtime.tag;

import lucee.runtime.op.Caster;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.StructUtil;

public class FeedProperties {
	private static final Collection.Key ITEM = KeyImpl.intern("ITEM");
	private static final Collection.Key ITEMS = KeyImpl.intern("ITEMS");
	private static final Collection.Key ENTRY = KeyImpl.intern("ENTRY");
	private static final Collection.Key RDF = KeyImpl.intern("RDF");
	private static final Collection.Key RSS = KeyImpl.intern("RSS");
	private static final Collection.Key CHANNEL = KeyImpl.intern("channel");
	
	
	public static Struct toProperties(Struct data) {
		data=(Struct) Duplicator.duplicate(data,true);
		
		

		Struct rdf = Caster.toStruct(data.removeEL(RDF),null,false);
		if(rdf==null)rdf = Caster.toStruct(data.removeEL(RSS),null,false);
		if(rdf!=null){
			rdf.removeEL(ITEM);
			Struct channel = Caster.toStruct(rdf.get(CHANNEL,null),null,false);
			if(channel!=null){
				channel.removeEL(ITEMS);
				StructUtil.copy(channel, data, true);
				
			}
		}
		

		data.removeEL(ITEM);
		data.removeEL(ENTRY);
		
		return data;
	}
}