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
 * Implements the CFML Function javacast
 */
package lucee.runtime.functions.string;

import java.math.BigDecimal;
import java.math.BigInteger;

import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;

public final class JavaCast implements Function {

	private static final long serialVersionUID = -5053403312467568511L;

	public static Object calls(PageContext pc , String string, Object object) throws PageException {
		throw new ExpressionException("method javacast not implemented yet"); // MUST ????
	}
	public static Object call(PageContext pc , String type, Object obj) throws PageException {
		type=type.trim();
		String lcType=StringUtil.toLowerCase(type);
		
		if(type.endsWith("[]")){
			return toArray(pc,type, lcType, obj);
			
		}
		Class clazz = toClass(pc, lcType, type);
		return to(pc,obj,clazz);
		
	}
	
	public static Object toArray(PageContext pc,String type,String lcType, Object obj) throws PageException {
		lcType=lcType.substring(0,lcType.length()-2);
		type=type.substring(0,type.length()-2);
		
		
		
		Array arr = Caster.toArray(obj);
		Class clazz = toClass(pc, lcType, type);
		Object trg= java.lang.reflect.Array.newInstance(clazz, arr.size());
		
		
		for(int i=arr.size()-1;i>=0;i--) {
			java.lang.reflect.Array.set(trg, i,to(pc,arr.getE(i+1),clazz));
			
		}
		return trg;
	}
	
	
	private static Object to(PageContext pc, Object obj,Class trgClass) throws PageException {
		if(trgClass==null)return Caster.toNull(obj); 
		else if(trgClass==BigDecimal.class)return new BigDecimal(Caster.toString(obj)); 
		else if(trgClass==BigInteger.class)return new BigInteger(Caster.toString(obj)); 
		return Caster.castTo(pc, trgClass, obj);
		//throw new ExpressionException("can't cast only to the following data types (bigdecimal,int, long, float ,double ,boolean ,string,null ), "+lcType+" is invalid");
	}
	
	private static Class toClass(PageContext pc,String lcType, String type) throws PageException {
		 
		if(lcType.equals("null")){
			return null; 
		}  
		if(lcType.equals("biginteger")){
			return BigInteger.class; 
		}  
		if(lcType.equals("bigdecimal")){
			return BigDecimal.class; 
		} 
		try {
			return ClassUtil.toClass(type);
		} catch (ClassException e) {
			throw Caster.toPageException(e);
		}
	}
	
}