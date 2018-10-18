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
 * Implements the CFML Function gettemplatepath
 */
package lucee.runtime.functions.system;

import java.util.Collection;
import java.util.Iterator;

import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.ext.function.Function;

public final class PagePoolClear implements Function {
	
	public static boolean call(PageContext pc) {
		ConfigWebImpl config=(ConfigWebImpl) pc.getConfig();
		clear(config.getMappings());
		clear(config.getCustomTagMappings());
		clear(pc.getApplicationContext().getMappings());
		clear(config.getComponentMappings());
		clear(config.getFunctionMappings());
		clear(config.getServerFunctionMappings());
		clear(config.getTagMappings());
		clear(config.getServerTagMappings());
    	
		return true;
	}
	public static void clear(Collection<Mapping> mappings) {
		if(mappings==null)return;
		Iterator<Mapping> it = mappings.iterator();
		while(it.hasNext())	{
			clear(it.next());
		}	
	}
	public static void clear(Mapping[] mappings) {
		if(mappings==null)return;
		for(int i=0;i<mappings.length;i++)	{
			clear(mappings[i]);
		}	
	}
	public static void clear(Mapping mapping) {
		if(mapping==null)return;
		((MappingImpl) mapping).getPageSourcePool().clearPages(null);
	}
}