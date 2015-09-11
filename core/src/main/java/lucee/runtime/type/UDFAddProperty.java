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
package lucee.runtime.type;

import java.util.HashMap;
import java.util.Map;

import lucee.commons.lang.CFTypes;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.component.Property;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.PropertyFactory;
import lucee.runtime.type.util.UDFUtil;

public final class UDFAddProperty extends UDFGSProperty {

	private final Property prop;
	//private ComponentScope scope;
	
	private final Key propName;
	
	private static final Object NULL=new Object();

	public UDFAddProperty(Component component,Property prop)  {
		super(component,"add"+StringUtil.ucFirst(PropertyFactory.getSingularName(prop)),getFunctionArgument(prop),CFTypes.TYPE_ANY,"wddx");
		this.prop=prop;
		this.propName=KeyImpl.getInstance(prop.getName());
	} 

	private static FunctionArgument[] getFunctionArgument(Property prop) {
		String t = PropertyFactory.getType(prop);
		FunctionArgument value = new FunctionArgumentImpl(KeyImpl.init(PropertyFactory.getSingularName(prop)),"any",CFTypes.TYPE_ANY,true);
		if("struct".equalsIgnoreCase(t)){
			FunctionArgument key = new FunctionArgumentImpl(KeyConstants._key,"string",CFTypes.TYPE_STRING,true);
			return new FunctionArgument[]{key,value};
		}
		return new FunctionArgument[]{value};
	}	
 
	@Override
	public UDF duplicate() {
		return new UDFAddProperty(component,prop);
	}
	
	@Override
	public Object call(PageContext pageContext, Object[] args,boolean doIncludePath) throws PageException {
		// struct
		if(this.arguments.length==2) {
			if(args.length<2)
				throw new ExpressionException("The function "+getFunctionName()+" need 2 arguments, only "+args.length+" argment"+(args.length==1?" is":"s are")+" passed in.");
			return _call(pageContext, args[0], args[1]);
		}
		// array
		else if(this.arguments.length==1) {
			if(args.length<1)
				throw new ExpressionException("The parameter "+this.arguments[0].getName()+" to function "+getFunctionName()+" is required but was not passed in.");
			return _call(pageContext, null, args[0]);
		}
		
		// never reached
		return component;
		
	}

	@Override
	public Object callWithNamedValues(PageContext pageContext, Struct values,boolean doIncludePath) throws PageException {
		UDFUtil.argumentCollection(values,getFunctionArguments());
		
		
		// struct
		if(this.arguments.length==2) {
			Key keyName = arguments[0].getName();
			Key valueName = arguments[1].getName();
			Object key = values.get(keyName,null);
			Object value = values.get(valueName,null);
			if(key==null)
				throw new ExpressionException("The parameter "+keyName+" to function "+getFunctionName()+" is required but was not passed in.");
			if(value==null)
				throw new ExpressionException("The parameter "+valueName+" to function "+getFunctionName()+" is required but was not passed in.");
			
			return _call(pageContext, key, value);
		}
		// array
		else if(this.arguments.length==1) {
			Key valueName = arguments[0].getName();
			Object value = values.get(valueName,null);
			if(value==null){
				Key[] keys = CollectionUtil.keys(values);
				if(keys.length==1) {
					value=values.get(keys[0]);
				}
				else throw new ExpressionException("The parameter "+valueName+" to function "+getFunctionName()+" is required but was not passed in.");
			}
			return _call(pageContext, null, value);
		}

		// never reached
		return component;
	}
	
	
	private Object _call(PageContext pageContext, Object key, Object value) throws PageException {
		
		
		Object propValue = component.getComponentScope().get(propName,null);
		
		// struct
		if(this.arguments.length==2) {
			key=cast(pageContext,arguments[0],key,1);
			value=cast(pageContext,arguments[1],value,2);
			if(propValue==null){
				HashMap map=new HashMap();
				component.getComponentScope().setEL(propName,map);
				propValue=map;
			}	
			if(propValue instanceof Struct) {
				((Struct)propValue).set(KeyImpl.toKey(key), value);
			}
			else if(propValue instanceof Map) {
				((Map)propValue).put(key, value);
			}
		}
		else {
			value=cast(pageContext,arguments[0],value,1);
			if(propValue==null){
				/* jira2049
				PageContext pc = ThreadLocalPageContext.get();
				ORMSession sess = ORMUtil.getSession(pc);
				SessionImpl s=(SessionImpl) sess.getRawSession();
				propValue=new PersistentList(s);
				component.getComponentScope().setEL(propName,propValue);*/
				Array arr=new ArrayImpl();
				component.getComponentScope().setEL(propName,arr);
				propValue=arr;
			}	
			if(propValue instanceof Array) {
				((Array)propValue).appendEL(value);
			}
			else if(propValue instanceof java.util.List) {
				((java.util.List)propValue).add(value);
			}
		}
		return component;
	}

	@Override
	public Object implementation(PageContext pageContext) throws Throwable {
		return null;
	}
	
	@Override
	public Object getDefaultValue(PageContext pc, int index) throws PageException {
		return prop.getDefault();
	}
	
	@Override
	public Object getDefaultValue(PageContext pc, int index, Object defaultValue) throws PageException {
		return prop.getDefault();
	}

	@Override
	public String getReturnTypeAsString() {
		return "any";
	}
}