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
package lucee.runtime.net.rpc.axis1.server;

import java.lang.reflect.Method;

import javax.xml.rpc.encoding.TypeMapping;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.rpc.axis1.AxisCaster;
import lucee.runtime.net.rpc.server.WSServer;

import org.apache.axis.MessageContext;
import org.apache.axis.providers.java.RPCProvider;

 final class ComponentProvider extends RPCProvider {

	public static final String PAGE_CONTEXT = PageContext.class.getName();
	public static final String COMPONENT = Component.class.getName();
 
	
	@Override
	protected Object invokeMethod(MessageContext mc, Method method, Object trg, Object[] args) throws Exception {
		PageContext pc=(PageContext) mc.getProperty(Constants.PAGE_CONTEXT);
		Component c= (Component) mc.getProperty(Constants.COMPONENT);
        
		WSServer server = Axis1Server.getInstance(pc);
		TypeMapping tm =mc.getTypeMapping();//TypeMappingUtil.getServerTypeMapping(server.getEngine().getTypeMappingRegistry());
		
		return AxisCaster.toAxisType(tm,c.call(pc,method.getName(),toLuceeType(pc,args)),null);
	}

	private Object[] toLuceeType(PageContext pc,Object[] args) throws PageException {
		Object[] trgs=new Object[args.length];
		for(int i=0;i<trgs.length;i++) {
			trgs[i]=AxisCaster.toLuceeType(pc,args[i]);
		}
		return trgs;
	}
	

}