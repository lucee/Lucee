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
package lucee.runtime.functions.other;

import java.io.IOException;

import lucee.commons.lang.ClassUtil;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.java.JavaObject;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.util.ListUtil;
import lucee.transformer.bytecode.util.JavaProxyFactory;

public class CreateDynamicProxy implements Function {
	
	private static final long serialVersionUID = -1787490871697335220L;

	public static Object call(PageContext pc , Object oCFC,Object oInterfaces) throws PageException {
		try {
			return _call(pc, oCFC, oInterfaces);
		} catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}
	
	public static Object _call(PageContext pc , Object oCFC,Object oInterfaces) throws PageException, IOException {
		
		// Component
		Component cfc;
		if(oCFC instanceof Component)
			cfc= (Component)oCFC;
		else
			cfc=pc.loadComponent(Caster.toString(oCFC));
		
		// string list to array
		if(Decision.isString(oInterfaces)) {
			String list = Caster.toString(oInterfaces);
			oInterfaces=ListUtil.listToStringArray(list, ',');
		}
		
		Class[] interfaces=null;
		if(Decision.isArray(oInterfaces)) {
			Object[] arr = Caster.toNativeArray(oInterfaces);
			
			ClassLoader cl = ((PageContextImpl)pc).getClassLoader();
			interfaces=new Class[arr.length];
			for(int i=0;i<arr.length;i++){
				if(arr[i] instanceof JavaObject) 
					interfaces[i]=((JavaObject)arr[i]).getClazz();
				else 
					interfaces[i]=ClassUtil.loadClass(cl, Caster.toString(arr[i]));
			}
			//strInterfaces=ListUtil.toStringArray(Caster.toArray(oInterfaces));
		}
		else if(oInterfaces instanceof JavaObject){
			interfaces=new Class[]{((JavaObject)oInterfaces).getClazz()};
		}
		else
			throw new FunctionException(pc, "CreateDynamicProxy", 2, "interfaces", "invalid type ["+Caster.toClassName(oInterfaces)+"] for class defintion");
		
		// check if all classes are interfaces
		for(int i=0;i<interfaces.length;i++){
			if(!interfaces[i].isInterface()) 
				throw new FunctionException(pc, "CreateDynamicProxy", 2, "interfaces", "definition ["+interfaces[i].getClass()+"] is a class and not a interface");
		}
		
		return JavaProxyFactory.createProxy(pc,cfc, null,interfaces);
	}
	    
}