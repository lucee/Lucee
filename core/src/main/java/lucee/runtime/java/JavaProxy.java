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
package lucee.runtime.java;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.thread.ThreadUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryColumn;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.ArrayUtil;

/**
 * creates a Java Proxy for components, so you can use componets as java classes following a certain
 * interface or class
 */
public class JavaProxy {

	public static Object call(ConfigWeb config, Component cfc, String methodName, Object... arguments) {
		boolean unregister = false;
		PageContext pc = null;
		try {
			pc = ThreadLocalPageContext.get();
			// create PageContext if necessary
			if (pc == null) {
				pc = ThreadUtil.createDummyPageContext(config);
				unregister = true;
				pc.addPageSource(cfc.getPageSource(), true);
			}
			return cfc.call(pc, methodName, arguments);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
		finally {
			if (unregister) config.getFactory().releaseLuceePageContext(pc, true);
		}
	}

	public static Object call(ConfigWeb config, UDF udf, String methodName, Object[] arguments) {
		boolean unregister = false;
		PageContext pc = null;
		try {
			pc = ThreadLocalPageContext.get();
			// create PageContext if necessary
			if (pc == null) {
				pc = ThreadUtil.createDummyPageContext(config);
				unregister = true;
				// pc.addPageSource(udf.getPageSource(), true);
			}
			return udf.call(pc, KeyImpl.init(methodName), arguments, true);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
		finally {
			if (unregister) config.getFactory().releaseLuceePageContext(pc, true);
		}
	}

	public static boolean toBoolean(Object obj) {
		try {
			return Caster.toBooleanValue(obj);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	public static float toFloat(Object obj) {
		try {
			return Caster.toFloatValue(obj);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	public static int toInt(Object obj) {
		try {
			return Caster.toIntValue(obj);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	public static double toDouble(Object obj) {
		try {
			return Caster.toDoubleValue(obj);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	public static long toLong(Object obj) {
		try {
			return Caster.toLongValue(obj);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	public static char toChar(Object obj) {
		try {
			return Caster.toCharValue(obj);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	public static byte toByte(Object obj) {
		try {
			return Caster.toByteValue(obj);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	public static short toShort(Object obj) {
		try {
			return Caster.toShortValue(obj);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	public static String toString(Object obj) {
		try {
			return Caster.toString(obj);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	public static Object to(Object obj, Class clazz) {
		try {
			return Caster.castTo(ThreadLocalPageContext.get(), clazz, obj);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	public static Object to(Object obj, String className) {
		try {
			return Caster.castTo(ThreadLocalPageContext.get(), className, obj, false);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	public static Object toCFML(boolean value) {
		return value ? Boolean.TRUE : Boolean.FALSE;
	}

	public static Object toCFML(byte value) {
		return Caster.toDouble(value);
	}

	public static Object toCFML(char value) {
		return new String(new char[] { value });
	}

	public static Object toCFML(double value) {
		return Caster.toDouble(value);
	}

	public static Object toCFML(float value) {
		return Caster.toDouble(value);
	}

	public static Object toCFML(int value) {
		return Caster.toDouble(value);
	}

	public static Object toCFML(long value) {
		return Caster.toDouble(value);
	}

	public static Object toCFML(short value) {
		return Caster.toDouble(value);
	}

	public static Object toCFML(Object value) {
		try {
			return _toCFML(value);
		}
		catch (PageException e) {
			return value;
		}
	}

	public static Object _toCFML(Object value) throws PageException {
		if (value instanceof Date || value instanceof Calendar) {// do not change to caster.isDate
			return Caster.toDate(value, null);
		}
		if (value instanceof Object[]) {
			Object[] arr = (Object[]) value;
			if (!ArrayUtil.isEmpty(arr)) {
				boolean allTheSame = true;
				// byte
				if (arr[0] instanceof Byte) {
					for (int i = 1; i < arr.length; i++) {
						if (!(arr[i] instanceof Byte)) {
							allTheSame = false;
							break;
						}
					}
					if (allTheSame) {
						byte[] bytes = new byte[arr.length];
						for (int i = 0; i < arr.length; i++) {
							bytes[i] = Caster.toByteValue(arr[i]);
						}
						return bytes;
					}
				}
			}
		}
		if (value instanceof Byte[]) {
			Byte[] arr = (Byte[]) value;
			if (!ArrayUtil.isEmpty(arr)) {
				byte[] bytes = new byte[arr.length];
				for (int i = 0; i < arr.length; i++) {
					bytes[i] = arr[i].byteValue();
				}
				return bytes;
			}
		}
		if (value instanceof byte[]) {
			return value;
		}
		if (!(value instanceof Collection)) {
			if (Decision.isArray(value)) {
				Array a = Caster.toArray(value);
				int len = a.size();
				Object o;
				for (int i = 1; i <= len; i++) {
					o = a.get(i, null);
					if (o != null) a.setEL(i, toCFML(o));
				}
				return a;
			}
			if (value instanceof Map) {
				Struct sct = new StructImpl();
				Iterator it = ((Map) value).entrySet().iterator();
				Map.Entry entry;
				while (it.hasNext()) {
					entry = (Entry) it.next();
					sct.setEL(Caster.toString(entry.getKey()), toCFML(entry.getValue()));
				}
				return sct;

				// return StructUtil.copyToStruct((Map)value);
			}
			if (Decision.isQuery(value)) {
				Query q = Caster.toQuery(value);
				int recorcount = q.getRecordcount();
				String[] strColumns = q.getColumns();

				QueryColumn col;
				int row;
				for (int i = 0; i < strColumns.length; i++) {
					col = q.getColumn(strColumns[i]);
					for (row = 1; row <= recorcount; row++) {
						col.set(row, toCFML(col.get(row, null)));
					}
				}
				return q;
			}
		}
		return value;
	}

}