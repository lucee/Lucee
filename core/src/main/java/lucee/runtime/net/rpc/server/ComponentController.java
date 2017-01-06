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
package lucee.runtime.net.rpc.server;

import javax.xml.rpc.encoding.TypeMapping;

import lucee.commons.lang.CFTypes;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.rpc.AxisCaster;
import lucee.runtime.net.rpc.TypeMappingUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.FunctionArgument;
import lucee.runtime.type.UDF;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;

/**
 * 
 */
public final class ComponentController {

	private static ThreadLocal<Component> component=new ThreadLocal<Component>();
	private static ThreadLocal<PageContext> pagecontext=new ThreadLocal<PageContext>();
	private static ThreadLocal<MessageContext> messageContext=new ThreadLocal<MessageContext>();

	/**
	 * invokes thread local component
	 * @param name
	 * @param args
	 * @return
	 * @throws AxisFault 
	 * @throws PageException
	 */
	public static Object invoke(String name, Object[] args) throws AxisFault {
		try {
			return _invoke(name, args);
		} 
		catch(Throwable t) {t.printStackTrace();
			throw AxisFault.makeFault((Caster.toPageException(t)));
		}
	}
	public static Object _invoke(String name, Object[] args) throws PageException {
		Key key = Caster.toKey(name);
		Component c=component.get();
		PageContext p=pagecontext.get();
		MessageContext mc = messageContext.get();
		if(c==null) throw new ApplicationException("missing component");
		if(p==null) throw new ApplicationException("missing pagecontext");
		
		UDF udf = Caster.toFunction(c.get(p,key,null),null);
		FunctionArgument[] fa=null;
		if(udf!=null) fa = udf.getFunctionArguments();
		
		for(int i=0;i<args.length;i++) {
			if(fa!=null && i<fa.length && fa[i].getType()==CFTypes.TYPE_UNKNOW) {
				args[i]=AxisCaster.toLuceeType(p,fa[i].getTypeAsString(),args[i]);
			}
			else
				args[i]=AxisCaster.toLuceeType(p,args[i]);
		}
			
		
		// return type
		String rtnType=udf!=null?udf.getReturnTypeAsString():"any";
		
		
		Object rtn = c.call(p, key, args);
		
		// cast return value to Axis type
		try {
			RPCServer server = RPCServer.getInstance(p.getId(),p,p.getServletContext());
			TypeMapping tm = mc!=null?mc.getTypeMapping():TypeMappingUtil.getServerTypeMapping(server.getEngine().getTypeMappingRegistry());
			rtn=Caster.castTo(p, rtnType, rtn, false);
			Class<?> clazz = Caster.cfTypeToClass(rtnType);
			return AxisCaster.toAxisType(tm,rtn,clazz.getComponentType()!=null?clazz:null);
		} 
		catch(Throwable t) {
			throw Caster.toPageException(t);
		}
	}

	/**
	 * removes PageContext and Component
	 * sets component and pageContext to invoke
	 * @param p
	 * @param c
	 */
	public static void set(PageContext p,Component c) {
		pagecontext.set(p);
		component.set(c);
	}
	public static void set(MessageContext mc) {
		messageContext.set(mc);
	}
	
	/**
	 * 
	 */
	public static void release() {
		pagecontext.set(null);
		component.set(null);
		messageContext.set(null);
	}
}