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
package lucee.runtime.config;

import lucee.loader.engine.CFMLEngine;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.type.Null;

public class NullSupportHelper {
	private static final Null NULL=Null.NULL;
	
	protected static boolean fullNullSupport=false;

	public static boolean full(PageContext pc) {
		pc=ThreadLocalPageContext.get(pc);
		if(pc==null) return false;
		return pc.getCurrentTemplateDialect()!=CFMLEngine.DIALECT_CFML || pc.getConfig().getFullNullSupport();
	}
	public static Object NULL(PageContext pc) {
		return full(pc)?NULL:null;
	}
	
	public static Object empty(PageContext pc) {
		return full(pc)?null:"";
	}
	
	public static boolean full() {
		PageContext pc = ThreadLocalPageContext.get();
		//print.ds("has-pc:"+(ThreadLocalPageContext.get()!=null));
		if(pc!=null) return full(pc);
			
		//print.ds("has-config:"+(ThreadLocalPageContext.getConfig()!=null));
		
		
		return true;
	}
	
	public static Object NULL() {
		return full()?NULL:null;
	}
}