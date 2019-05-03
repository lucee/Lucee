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
import lucee.runtime.op.Decision;

public final class JavaCast implements Function {

	private static final long serialVersionUID = -5053403312467568511L;

	public static Object calls(PageContext pc, String string, Object object) throws PageException {
		throw new ExpressionException("method javacast not implemented yet"); // MUST ????
	}

	public static Object call(PageContext pc, String type, Object obj) throws PageException {
		type = type.trim();
		String lcType = StringUtil.toLowerCase(type);

		if (type.endsWith("[]")) {

			return toArray(pc, type, lcType, obj);
		}
		Class<?> clazz = toClass(pc, lcType, type);
		return to(pc, obj, clazz);

	}

	public static Object toArray(PageContext pc, String type, String lcType, Object obj) throws PageException {
		// byte
		if ("byte[]".equals(lcType)) {
			if (obj instanceof byte[]) return (byte[]) obj;
			if (Decision.isBinary(obj)) return Caster.toBinary(obj);
		}

		// char
		else if ("char[]".equals(lcType)) {
			if (obj instanceof char[]) return (char[]) obj;
			if (obj instanceof CharSequence) return obj.toString().toCharArray();
		}

		return _toArray(pc, type, lcType, obj);
	}

	public static Object _toArray(PageContext pc, String type, String lcType, Object obj) throws PageException {
		lcType = lcType.substring(0, lcType.length() - 2);
		type = type.substring(0, type.length() - 2);

		// other
		Object[] arr = Caster.toList(obj).toArray();
		Class<?> clazz = toClass(pc, lcType, type);
		Object trg = java.lang.reflect.Array.newInstance(clazz, arr.length);

		for (int i = arr.length - 1; i >= 0; i--) {
			java.lang.reflect.Array.set(trg, i, type.endsWith("[]") ? _toArray(pc, type, lcType, arr[i]) : to(pc, arr[i], clazz));
		}
		return trg;
	}

	private static Object to(PageContext pc, Object obj, Class<?> trgClass) throws PageException {
		if (trgClass == null) return Caster.toNull(obj);
		else if (trgClass == BigDecimal.class) return Caster.toBigDecimal(obj);
		else if (trgClass == BigInteger.class) return Caster.toBigInteger(obj);
		return Caster.castTo(pc, trgClass, obj);
		// throw new ExpressionException("can't cast only to the following data types (bigdecimal,int, long,
		// float ,double ,boolean ,string,null ), "+lcType+" is invalid");
	}

	private static Class<?> toClass(PageContext pc, String lcType, String type) throws PageException {

		if (lcType.equals("null")) {
			return null;
		}
		if (lcType.equals("biginteger")) {
			return BigInteger.class;
		}
		if (lcType.equals("bigdecimal")) {
			return BigDecimal.class;
		}
		try {
			return ClassUtil.toClass(type);
		}
		catch (ClassException e) {
			throw Caster.toPageException(e);
		}
	}

}