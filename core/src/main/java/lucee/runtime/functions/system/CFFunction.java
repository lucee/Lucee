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
package lucee.runtime.functions.system;

import java.io.File;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.Mapping;
import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Collection;
import lucee.runtime.type.FunctionValue;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.UDFImpl;
import lucee.runtime.type.scope.Variables;
import lucee.runtime.type.scope.VariablesImpl;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.KeyConstants;

public class CFFunction {
	
	
	private static final Variables VAR = new VariablesImpl();
	//private static Map udfs=new ReferenceMap();
	
	public static Object call(PageContext pc , Object[] objArr) throws PageException {
		if(objArr.length<3)
			throw new ExpressionException("invalid call of a CFML Based built in function");
		
		// translate arguments
		String filename=Caster.toString((((FunctionValue) objArr[0]).getValue()));
		Collection.Key name=KeyImpl.toKey((((FunctionValue) objArr[1]).getValue()));
		boolean isweb=Caster.toBooleanValue((((FunctionValue) objArr[2]).getValue()));
		
		
		UDF udf=loadUDF(pc, filename, name, isweb);
		Struct meta = udf.getMetaData(pc);
		boolean callerScopes=(meta==null)?false:Caster.toBooleanValue(meta.get("callerScopes",Boolean.FALSE),false);
		boolean caller=meta==null?false:Caster.toBooleanValue(meta.get(KeyConstants._caller,Boolean.FALSE),false);
		
		Struct namedArguments=null,cs=null;
		if(callerScopes) {
			
			cs=new StructImpl();
			if(pc.undefinedScope().getCheckArguments()) {
				cs.set(KeyConstants._local, pc.localScope().duplicate(false));
				cs.set(KeyConstants._arguments, pc.argumentsScope().duplicate(false));
			}
		}
		
		Object[] arguments=null;
		if(objArr.length<=3)arguments=ArrayUtil.OBJECT_EMPTY;
		else if(objArr[3] instanceof FunctionValue){
			FunctionValue fv;
			namedArguments=new StructImpl(Struct.TYPE_LINKED);
			if(callerScopes)	namedArguments.setEL(KeyConstants._caller, cs);
			else if(caller)		namedArguments.setEL(KeyConstants._caller, Duplicator.duplicate(pc.undefinedScope(),false));
			for(int i=3;i<objArr.length;i++){
				fv=toFunctionValue(name,objArr[i]);
				namedArguments.set(fv.getName(), fv.getValue());
			}
		}
		else {
			int offset=(caller||callerScopes?2:3);
			arguments=new Object[objArr.length-offset];
			if(callerScopes) arguments[0]=cs;
			else if(caller)arguments[0]=Duplicator.duplicate(pc.undefinedScope(),false);
			for(int i=3;i<objArr.length;i++){
				arguments[i-offset]=toObject(name,objArr[i]);
			}
		}
		
		// execute UDF
		if(namedArguments==null){
			return ((UDFImpl)udf).call(pc,name, arguments, false);
		}
		
		
		return ((UDFImpl)udf).callWithNamedValues(pc,name, namedArguments, false);
	}

	public static synchronized UDF loadUDF(PageContext pc, String filename,Collection.Key name,boolean isweb) throws PageException {
		ConfigWebImpl config = (ConfigWebImpl) pc.getConfig();
		String key=isweb?name.getString()+config.getIdentification().getId():name.getString();
    	UDF udf=config.getFromFunctionCache(key);
		if(udf!=null) return udf;
		
		Mapping mapping=isweb?config.getFunctionMapping():config.getServerFunctionMapping();
    	Page p = mapping.getPageSource(filename).loadPage(pc,false);	
		
    	
    	// execute page
    	Variables old = pc.variablesScope();
    	pc.setVariablesScope(VAR);
    	boolean wasSilent = pc.setSilent();
    	try {
			p.call(pc);
			Object o= pc.variablesScope().get(name,null);
			if(o instanceof UDF) {
				udf= (UDF) o;
				config.putToFunctionCache(key, udf);
				return udf;
			}
			throw new ExpressionException("there is no Function defined with name ["+name+"] in template ["+mapping.getStrPhysical()+File.separator+filename+"]");
		} 
    	catch(Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}
		finally{
			pc.setVariablesScope(old);
			if(!wasSilent)pc.unsetSilent();
		}
		
		
	}

	private static FunctionValue toFunctionValue(Collection.Key name,Object obj) throws ExpressionException {
		if(obj instanceof FunctionValue)
			return (FunctionValue) obj;
		throw new ExpressionException("invalid argument for function "+name+", you can not mix named and unnamed arguments");
	}

	private static Object toObject(Collection.Key name,Object obj) throws ExpressionException {
		if(obj instanceof FunctionValue)
			throw new ExpressionException("invalid argument for function "+name+", you can not mix named and unnamed arguments");
		return obj;
	}
}