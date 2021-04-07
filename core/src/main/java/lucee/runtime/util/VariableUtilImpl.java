/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.runtime.util;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.w3c.dom.Node;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.CFTypes;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.security.SecurityManager;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.text.xml.struct.XMLStructFactory;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.FunctionValue;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Objects;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryColumn;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.scope.Undefined;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.MemberUtil;
import lucee.runtime.type.util.Type;
import lucee.runtime.type.wrap.MapAsStruct;

/**
 * Class to handle CF Variables (set,get,call)
 */
public final class VariableUtilImpl implements VariableUtil {

	private static Boolean _logReflectionCalls;

	@Override
	public Object getCollection(PageContext pc, Object coll, String key, Object defaultValue) {
		if (coll instanceof Query) {
			// TODO sollte nicht null sein
			return ((Query) coll).getColumn(key, null);
		}
		return get(pc, coll, key, defaultValue);
	}

	@Override
	public Object getCollection(PageContext pc, Object coll, Collection.Key key, Object defaultValue) {
		if (coll instanceof Query) {
			QueryColumn qc = ((Query) coll).getColumn(key, null);
			if (qc == null) return defaultValue;
			return qc;
		}
		return get(pc, coll, key, defaultValue);
	}

	@Override
	public Object get(PageContext pc, Object coll, String key, Object defaultValue) {
		// Objects
		if (coll instanceof Objects) {
			return ((Objects) coll).get(pc, KeyImpl.init(key), defaultValue);
		}
		// Collection
		else if (coll instanceof Collection) {
			return ((Collection) coll).get(key, defaultValue);
		}
		// Map
		else if (coll instanceof Map) {
			Object rtn = ((Map) coll).get(key);
			// if(rtn==null)rtn=((Map)coll).get(MapAsStruct.getCaseSensitiveKey((Map)coll, key));
			if (rtn != null) return rtn;
			return defaultValue;
		}
		// List
		else if (coll instanceof List) {
			int index = Caster.toIntValue(key, Integer.MIN_VALUE);
			if (index == Integer.MIN_VALUE) return defaultValue;
			try {
				return ((List) coll).get(index - 1);
			}
			catch (IndexOutOfBoundsException e) {
				return defaultValue;
			}
		}
		// Native Array
		else if (Decision.isNativeArray(coll)) {
			return ArrayUtil.get(coll, Caster.toIntValue(key, Integer.MIN_VALUE) - 1, defaultValue);
		}
		// Node
		else if (coll instanceof Node) {
			return XMLStructFactory.newInstance((Node) coll, false).get(key, defaultValue);
		}
		// Direct Object Access
		if (pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_DIRECT_JAVA_ACCESS) == SecurityManager.VALUE_YES) {
			if (doLogReflectionCalls()) LogUtil.log(pc.getConfig(), Log.LEVEL_INFO, "reflection", "get-property:" + key + " from class " + Caster.toTypeName(coll));
			return Reflector.getProperty(coll, key, defaultValue);
		}
		return null;

	}

	public static boolean doLogReflectionCalls() {
		if (_logReflectionCalls == null) _logReflectionCalls = Caster.toBoolean(lucee.commons.io.SystemUtil.getSystemPropOrEnvVar("lucee.log.reflection", null), Boolean.FALSE);
		return _logReflectionCalls.booleanValue();
	}

	@Override
	public Object get(PageContext pc, Object coll, Collection.Key key, Object defaultValue) {
		// Objects
		if (coll instanceof Objects) {
			return ((Objects) coll).get(pc, key, defaultValue);
		}
		// Collection
		else if (coll instanceof Collection) {
			return ((Collection) coll).get(key, defaultValue);
		}
		// Map
		else if (coll instanceof Map) {

			Object rtn = ((Map) coll).get(key.getString());
			// if(rtn==null)rtn=((Map)coll).get(MapAsStruct.getCaseSensitiveKey((Map)coll, key.getString()));
			if (rtn != null) return rtn;
			return defaultValue;

		}
		// List
		else if (coll instanceof List) {
			int index = Caster.toIntValue(key, Integer.MIN_VALUE);
			if (index == Integer.MIN_VALUE) return defaultValue;
			try {
				return ((List) coll).get(index - 1);
			}
			catch (IndexOutOfBoundsException e) {
				return defaultValue;
			}
		}
		// Native Array
		else if (Decision.isNativeArray(coll)) {
			return ArrayUtil.get(coll, Caster.toIntValue(key, Integer.MIN_VALUE) - 1, defaultValue);
		}
		// Node
		else if (coll instanceof Node) {
			return XMLStructFactory.newInstance((Node) coll, false).get(key, defaultValue);
		}
		else if (coll == null) return defaultValue;

		// Direct Object Access
		if (pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_DIRECT_JAVA_ACCESS) == SecurityManager.VALUE_YES) {
			if (doLogReflectionCalls()) LogUtil.log(pc.getConfig(), Log.LEVEL_INFO, "reflection", "get-property:" + key + " from class " + Caster.toTypeName(coll));
			return Reflector.getProperty(coll, key.getString(), defaultValue);
		}
		return defaultValue;
	}

	public Object getLight(PageContext pc, Object coll, Collection.Key key, Object defaultValue) {
		// Objects
		if (coll instanceof Objects) {
			return ((Objects) coll).get(pc, key, defaultValue);
		}
		// Collection
		else if (coll instanceof Collection) {
			return ((Collection) coll).get(key, defaultValue);
		}
		// Map
		else if (coll instanceof Map) {
			// Object rtn=null;
			try {
				Object rtn = ((Map) coll).get(key.getString());
				// if(rtn==null)rtn=((Map)coll).get(MapAsStruct.getCaseSensitiveKey((Map)coll, key.getString()));
				if (rtn != null) return rtn;
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
			return Reflector.getField(coll, key.getString(), defaultValue);
			// return rtn;
		}
		// List
		else if (coll instanceof List) {
			int index = Caster.toIntValue(key.getString(), Integer.MIN_VALUE);
			if (index == Integer.MIN_VALUE) return null;
			try {
				return ((List) coll).get(index - 1);
			}
			catch (IndexOutOfBoundsException e) {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	@Override
	public Object getLight(PageContext pc, Object coll, String key, Object defaultValue) {
		// Objects
		if (coll instanceof Objects) {
			return ((Objects) coll).get(pc, KeyImpl.init(key), defaultValue);
		}
		// Collection
		else if (coll instanceof Collection) {
			return ((Collection) coll).get(key, defaultValue);
		}
		// Map
		else if (coll instanceof Map) {
			try {
				Object rtn = ((Map) coll).get(key);
				// if(rtn==null)rtn=((Map)coll).get(MapAsStruct.getCaseSensitiveKey((Map)coll, key));
				if (rtn != null) return rtn;
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
			return Reflector.getProperty(coll, key, defaultValue);
			// return rtn;
		}
		// List
		else if (coll instanceof List) {
			int index = Caster.toIntValue(key, Integer.MIN_VALUE);
			if (index == Integer.MIN_VALUE) return null;
			try {
				return ((List) coll).get(index - 1);
			}
			catch (IndexOutOfBoundsException e) {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	@Override
	public Object getCollection(PageContext pc, Object coll, String key) throws PageException {
		if (coll instanceof Query) {
			return ((Query) coll).getColumn(key);
		}
		return get(pc, coll, key);
	}

	public Object getCollection(PageContext pc, Object coll, Collection.Key key) throws PageException {
		if (coll instanceof Query) {
			return ((Query) coll).getColumn(key);
		}
		return get(pc, coll, key);
	}

	public Object get(PageContext pc, Object coll, Collection.Key key) throws PageException {
		// Objects
		if (coll instanceof Objects) {
			return ((Objects) coll).get(pc, key);
		}
		// Collection
		else if (coll instanceof Collection) {
			return ((Collection) coll).get(key);
		}
		// Map
		else if (coll instanceof Map) {
			Object rtn = null;
			try {
				rtn = ((Map) coll).get(key.getString());
				if (rtn == null && coll.getClass().getName().startsWith("org.hibernate.")) rtn = ((Map) coll).get(MapAsStruct.getCaseSensitiveKey((Map) coll, key.getString()));
				if (rtn != null) return rtn;
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
			rtn = Reflector.getProperty(coll, key.getString(), null);
			if (rtn != null) return rtn;

			String realKey = MapAsStruct.getCaseSensitiveKey((Map) coll, key.getString());
			String detail = null;
			if (realKey != null) {
				detail = "The keys for this Map are case-sensitive, use bracked notation like this \"map['" + realKey + "']\" instead of dot notation like this  \"map." + realKey
						+ "\" to address the Map";
			}

			throw new ExpressionException("Key [" + key.getString() + "] doesn't exist in Map (" + ((Map) coll).getClass().getName() + ")", detail);
		}
		// List
		else if (coll instanceof List) {
			try {
				Object rtn = ((List) coll).get(Caster.toIntValue(key.getString()) - 1);
				if (rtn == null) throw new ExpressionException("Key [" + key.getString() + "] doesn't exist in List");
				return rtn;
			}
			catch (IndexOutOfBoundsException e) {
				throw new ExpressionException("Key [" + key.getString() + "] doesn't exist in List");
			}
		}
		// Native Array
		else if (Decision.isNativeArray(coll)) {
			Object rtn = ArrayUtil.get(coll, Caster.toIntValue(key.getString()) - 1, null);
			if (rtn == null) throw new ExpressionException("Key [" + key.getString() + "] doesn't exist in Native Array");
			return rtn;
		}
		// Node
		else if (coll instanceof Node) {
			// print.out("get:"+key);
			return XMLStructFactory.newInstance((Node) coll, false).get(key);
		}
		else if (coll instanceof String) {
			if (Decision.isInteger(key.getString())) { // i do the decision call and the caster call, because in most cases the if will be false
				String str = (String) coll;
				int index = Caster.toIntValue(key.getString(), -1);
				if (index > 0 && index <= str.length()) {
					return str.substring(index - 1, index);
				}
			}
		}
		// HTTPSession
		/*
		 * else if(coll instanceof HttpSession) { return ((HttpSession)coll).getAttribute(key.getString());
		 * }
		 */

		// Direct Object Access
		if (coll != null && pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_DIRECT_JAVA_ACCESS) == SecurityManager.VALUE_YES) {
			if (doLogReflectionCalls()) LogUtil.log(pc.getConfig(), Log.LEVEL_INFO, "reflection", "get-property:" + key + " from class " + Caster.toTypeName(coll));
			return Reflector.getProperty(coll, key.getString());
		}
		throw new ExpressionException("No matching property [" + key.getString() + "] found");

	}

	@Override
	public Object get(PageContext pc, Object coll, String key) throws PageException {
		// Objects
		if (coll instanceof Objects) {
			return ((Objects) coll).get(pc, KeyImpl.init(key));
		}
		// Collection
		else if (coll instanceof Collection) {

			return ((Collection) coll).get(KeyImpl.init(key));
		}
		// Map
		else if (coll instanceof Map) {
			Object rtn = null;
			try {
				rtn = ((Map) coll).get(key);
				// if(rtn==null)rtn=((Map)coll).get(MapAsStruct.getCaseSensitiveKey((Map)coll, key));
				if (rtn != null) return rtn;

			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
			rtn = Reflector.getProperty(coll, key, null);
			if (rtn != null) return rtn;
			throw new ExpressionException("Key [" + key + "] doesn't exist in Map (" + Caster.toClassName(coll) + ")", "keys are [" + keyList(((Map) coll)) + "]");
		}
		// List
		else if (coll instanceof List) {
			try {
				Object rtn = ((List) coll).get(Caster.toIntValue(key) - 1);
				if (rtn == null) throw new ExpressionException("Key [" + key + "] doesn't exist in List");
				return rtn;
			}
			catch (IndexOutOfBoundsException e) {
				throw new ExpressionException("Key [" + key + "] doesn't exist in List");
			}
		}
		// Native Array
		else if (Decision.isNativeArray(coll)) {
			Object rtn = ArrayUtil.get(coll, Caster.toIntValue(key) - 1, null);
			if (rtn == null) throw new ExpressionException("Key [" + key + "] doesn't exist in Native Array");
			return rtn;
		}
		// Node
		else if (coll instanceof Node) {
			return XMLStructFactory.newInstance((Node) coll, false).get(key);
		}
		// Direct Object Access
		if (pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_DIRECT_JAVA_ACCESS) == SecurityManager.VALUE_YES) {
			if (doLogReflectionCalls()) LogUtil.log(pc.getConfig(), Log.LEVEL_INFO, "reflection", "get-property:" + key + " from class " + Caster.toTypeName(coll));
			return Reflector.getProperty(coll, key);
		}
		throw new ExpressionException("No matching property [" + key + "] found");

	}

	private String keyList(Map map) {
		StringBuffer sb = new StringBuffer();
		Iterator it = map.keySet().iterator();
		while (it.hasNext()) {
			if (sb.length() > 0) sb.append(',');
			sb.append(StringUtil.toStringNative(it.next(), ""));
		}
		return sb.toString();
	}

	@Override
	public Object set(PageContext pc, Object coll, Collection.Key key, Object value) throws PageException {
		// Objects
		if (coll instanceof Objects) {
			((Objects) coll).set(pc, key, value);
			return value;
		}
		// Collection
		else if (coll instanceof Collection) {
			((Collection) coll).set(key, value);
			return value;
		}
		// Map
		else if (coll instanceof Map) {
			/*
			 * no idea why this is here try { Reflector.setProperty(coll,key.getString(),value); return value; }
			 * catch(Throwable t) {ExceptionUtil.rethrowIfNecessary(t);}
			 */
			((Map) coll).put(key.getString(), value);
			return value;
		}
		// List
		else if (coll instanceof List) {
			List list = ((List) coll);
			int index = Caster.toIntValue(key.getString());
			if (list.size() >= index) list.set(index - 1, value);
			else {
				while (list.size() < index - 1)
					list.add(null);
				list.add(value);
			}
			return value;
		}
		// Native Array
		else if (Decision.isNativeArray(coll)) {
			try {
				return ArrayUtil.set(coll, Caster.toIntValue(key.getString()) - 1, value);
			}
			catch (Exception e) {
				throw new ExpressionException("Invalid index [" + key.getString() + "] for Native Array, can't expand Native Arrays");
			}
		}
		// Node
		else if (coll instanceof Node) {
			return XMLUtil.setProperty((Node) coll, key, value);
		}
		// Direct Object Access
		if (pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_DIRECT_JAVA_ACCESS) == SecurityManager.VALUE_YES) {
			try {
				if (doLogReflectionCalls()) LogUtil.log(pc.getConfig(), Log.LEVEL_INFO, "reflection", "set-property:" + key + " in class " + Caster.toTypeName(coll));
				Reflector.setProperty(coll, key.getString(), value);
				return value;
			}
			catch (PageException pe) {}
		}
		throw new ExpressionException("Can't assign value to an Object of this type [" + Type.getName(coll) + "] with key [" + key.getString() + "]");
	}

	/**
	 * @see lucee.runtime.util.VariableUtil#set(lucee.runtime.PageContext, java.lang.Object,
	 *      java.lang.String, java.lang.Object)
	 */
	@Override
	public Object set(PageContext pc, Object coll, String key, Object value) throws PageException {
		// Objects
		if (coll instanceof Objects) {
			((Objects) coll).set(pc, KeyImpl.init(key), value);
			return value;
		}
		// Collection
		else if (coll instanceof Collection) {
			((Collection) coll).set(key, value);
			return value;
		}
		// Map
		else if (coll instanceof Map) {
			/*
			 * try { Reflector.setProperty(coll,key,value); return value; } catch(Throwable t)
			 * {ExceptionUtil.rethrowIfNecessary(t);}
			 */
			((Map) coll).put(key, value);
			return value;
		}
		// List
		else if (coll instanceof List) {
			List list = ((List) coll);
			int index = Caster.toIntValue(key);
			if (list.size() >= index) list.set(index - 1, value);
			else {
				while (list.size() < index - 1)
					list.add(null);
				list.add(value);
			}
			return value;
		}
		// Native Array
		else if (Decision.isNativeArray(coll)) {
			try {
				return ArrayUtil.set(coll, Caster.toIntValue(key) - 1, value);
			}
			catch (Exception e) {
				throw new ExpressionException("invalid index [" + key + "] for Native Array, can't expand Native Arrays");
			}
		}
		// Node
		else if (coll instanceof Node) {
			return XMLUtil.setProperty((Node) coll, KeyImpl.init(key), value);
		}
		// Direct Object Access
		if (pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_DIRECT_JAVA_ACCESS) == SecurityManager.VALUE_YES) {
			try {
				if (doLogReflectionCalls()) LogUtil.log(pc.getConfig(), Log.LEVEL_INFO, "reflection", "set-property:" + key + " in class " + Caster.toTypeName(coll));
				Reflector.setProperty(coll, key, value);
				return value;
			}
			catch (PageException pe) {}
		}
		throw new ExpressionException("Can't assign value to an Object of this type [" + Type.getName(coll) + "] with key [" + key + "]");
	}

	/**
	 *
	 * @see lucee.runtime.util.VariableUtil#setEL(lucee.runtime.PageContext, java.lang.Object,
	 *      java.lang.String, java.lang.Object)
	 */
	@Override
	public Object setEL(PageContext pc, Object coll, String key, Object value) {
		// Objects
		if (coll instanceof Objects) {
			((Objects) coll).setEL(pc, KeyImpl.init(key), value);
			return value;
		}
		// Collection
		else if (coll instanceof Collection) {
			((Collection) coll).setEL(KeyImpl.init(key), value);
			return value;
		}
		// Map
		else if (coll instanceof Map) {
			try {
				Reflector.setProperty(coll, key, value);
				return value;
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
			((Map) coll).put(key, value);
			return value;
		}
		// List
		else if (coll instanceof List) {
			List list = ((List) coll);
			int index = Caster.toIntValue(key, Integer.MIN_VALUE);
			if (index == Integer.MIN_VALUE) return null;
			if (list.size() >= index) list.set(index - 1, value);
			else {
				while (list.size() < index - 1)
					list.add(null);
				list.add(value);
			}
			return value;
		}
		// Native Array
		else if (Decision.isNativeArray(coll)) {
			return ArrayUtil.setEL(coll, Caster.toIntValue(key, Integer.MIN_VALUE) - 1, value);
		}
		// Node
		else if (coll instanceof Node) {
			return XMLUtil.setPropertyEL((Node) coll, KeyImpl.init(key), value);
		}
		// Direct Object Access
		if (pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_DIRECT_JAVA_ACCESS) == SecurityManager.VALUE_YES) {
			if (doLogReflectionCalls()) LogUtil.log(pc.getConfig(), Log.LEVEL_INFO, "reflection", "set-property:" + key + " in class " + Caster.toTypeName(coll));
			Reflector.setPropertyEL(coll, key, value);
			return value;
		}
		return null;
	}

	/**
	 * @see lucee.runtime.util.VariableUtil#setEL(lucee.runtime.PageContext, java.lang.Object,
	 *      lucee.runtime.type.Collection.Key, java.lang.Object)
	 */
	@Override
	public Object setEL(PageContext pc, Object coll, Collection.Key key, Object value) {
		// Objects
		if (coll instanceof Objects) {
			((Objects) coll).setEL(pc, key, value);
			return value;
		}
		// Collection
		else if (coll instanceof Collection) {
			((Collection) coll).setEL(key, value);
			return value;
		}
		// Map
		else if (coll instanceof Map) {
			try {
				Reflector.setProperty(coll, key.getString(), value);
				return value;
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
			((Map) coll).put(key, value);
			return value;
		}
		// List
		else if (coll instanceof List) {
			List list = ((List) coll);
			int index = Caster.toIntValue(key, Integer.MIN_VALUE);
			if (index == Integer.MIN_VALUE) return null;
			if (list.size() >= index) list.set(index - 1, value);
			else {
				while (list.size() < index - 1)
					list.add(null);
				list.add(value);
			}
			return value;
		}
		// Native Array
		else if (Decision.isNativeArray(coll)) {
			return ArrayUtil.setEL(coll, Caster.toIntValue(key, Integer.MIN_VALUE) - 1, value);
		}
		// Node
		else if (coll instanceof Node) {
			return XMLUtil.setPropertyEL((Node) coll, key, value);
		}
		// Direct Object Access
		if (pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_DIRECT_JAVA_ACCESS) == SecurityManager.VALUE_YES) {
			if (doLogReflectionCalls()) LogUtil.log(pc.getConfig(), Log.LEVEL_INFO, "reflection", "set-property:" + key + " in class " + Caster.toTypeName(coll));
			Reflector.setPropertyEL(coll, key.getString(), value);
			return value;
		}
		return null;
	}

	@Override
	public Object removeEL(Object coll, String key) {
		// Collection
		if (coll instanceof Collection) {
			return ((Collection) coll).removeEL(KeyImpl.init(key));
		}
		// Map
		else if (coll instanceof Map) {
			Object obj = ((Map) coll).remove(key);
			// if(obj==null)obj=((Map)coll).remove(MapAsStruct.getCaseSensitiveKey((Map)coll, key));
			return obj;
		}
		// List
		else if (coll instanceof List) {
			int i = Caster.toIntValue(key, Integer.MIN_VALUE);
			if (i == Integer.MIN_VALUE) return null;
			return ((List) coll).remove(i);
		}
		return null;
	}

	@Override
	public Object removeEL(Object coll, Collection.Key key) {
		// Collection
		if (coll instanceof Collection) {
			return ((Collection) coll).removeEL(key);
		}
		// Map
		else if (coll instanceof Map) {
			Object obj = ((Map) coll).remove(key.getString());
			// if(obj==null)obj=((Map)coll).remove(MapAsStruct.getCaseSensitiveKey((Map)coll, key));
			return obj;
		}
		// List
		else if (coll instanceof List) {
			int i = Caster.toIntValue(key, Integer.MIN_VALUE);
			if (i == Integer.MIN_VALUE) return null;
			return ((List) coll).remove(i);
		}
		return null;
	}

	/**
	 * @see lucee.runtime.util.VariableUtil#remove(java.lang.Object, java.lang.String)
	 */
	@Override
	public Object remove(Object coll, String key) throws PageException {
		// Collection
		if (coll instanceof Collection) {
			return ((Collection) coll).remove(KeyImpl.init(key));
		}
		// Map
		else if (coll instanceof Map) {
			Object obj = ((Map) coll).remove(key);
			// if(obj==null)obj=((Map)coll).remove(MapAsStruct.getCaseSensitiveKey((Map)coll, key));
			if (obj == null) throw new ExpressionException("Can't remove key [" + key + "] from map");
			return obj;
		}
		// List
		else if (coll instanceof List) {
			int i = Caster.toIntValue(key);
			Object obj = ((List) coll).remove(i);
			if (obj == null) throw new ExpressionException("Can't remove index [" + key + "] from list");
			return obj;
		}
		/*
		 * / Native Array TODO this below else if(Decision.isNativeArray(o)) { try { return
		 * ArrayUtil.set(o,Caster.toIntValue(key)-1,value); } catch (Exception e) { return
		 * getDirectProperty(o, key, new ExpressionException("Key doesn't exist in Native Array"),false); }
		 * }
		 */
		// TODO Support for Node
		throw new ExpressionException("Can't remove key [" + key + "] from Object of type [" + Caster.toTypeName(coll) + "]");
	}

	@Override
	public Object remove(Object coll, Collection.Key key) throws PageException {
		// Collection
		if (coll instanceof Collection) {
			return ((Collection) coll).remove(key);
		}
		// Map
		else if (coll instanceof Map) {
			Object obj = ((Map) coll).remove(key.getString());
			// if(obj==null)obj=((Map)coll).remove(MapAsStruct.getCaseSensitiveKey((Map)coll, key));
			if (obj == null) throw new ExpressionException("Can't remove key [" + key + "] from map");
			return obj;
		}
		// List
		else if (coll instanceof List) {
			int i = Caster.toIntValue(key);
			Object obj = ((List) coll).remove(i);
			if (obj == null) throw new ExpressionException("can't remove index [" + key + "] from list");
			return obj;
		}
		/*
		 * / Native Array TODO this below else if(Decision.isNativeArray(o)) { try { return
		 * ArrayUtil.set(o,Caster.toIntValue(key)-1,value); } catch (Exception e) { return
		 * getDirectProperty(o, key, new ExpressionException("Key doesn't exist in Native Array"),false); }
		 * }
		 */
		// TODO Support for Node
		throw new ExpressionException("Can't remove key [" + key + "] from Object of type [" + Caster.toTypeName(coll) + "]");
	}

	/**
	 * @see lucee.runtime.util.VariableUtil#callFunction(lucee.runtime.PageContext, java.lang.Object,
	 *      java.lang.String, java.lang.Object[])
	 */
	@Override
	public Object callFunction(PageContext pc, Object coll, String key, Object[] args) throws PageException {
		if (args.length > 0 && args[0] instanceof FunctionValue) return callFunctionWithNamedValues(pc, coll, key, args);
		return callFunctionWithoutNamedValues(pc, coll, key, args);
	}

	/**
	 * @see lucee.runtime.util.VariableUtil#callFunctionWithoutNamedValues(lucee.runtime.PageContext,
	 *      java.lang.Object, java.lang.String, java.lang.Object[])
	 */
	@Override
	public Object callFunctionWithoutNamedValues(PageContext pc, Object coll, String key, Object[] args) throws PageException {
		return callFunctionWithoutNamedValues(pc, coll, KeyImpl.init(key), args);
	}

	@Override
	public Object callFunctionWithoutNamedValues(PageContext pc, Object coll, Collection.Key key, Object[] args) throws PageException {
		// Objects
		if (coll instanceof Objects) {
			return ((Objects) coll).call(pc, key, args);
		}
		// call UDF
		Object prop = getLight(pc, coll, key, null);
		if (prop instanceof UDF) {
			return ((UDF) prop).call(pc, key, args, false);
		}
		// Strings
		if (coll instanceof String) {
			return MemberUtil.call(pc, coll, key, args, new short[] { CFTypes.TYPE_STRING }, new String[] { "string" });
		}
		// Map || XML
		if (coll instanceof Map) {
			if (coll instanceof Node) return MemberUtil.call(pc, coll, key, args, new short[] { CFTypes.TYPE_XML, CFTypes.TYPE_STRUCT }, new String[] { "xml", "struct" });
			return MemberUtil.call(pc, coll, key, args, new short[] { CFTypes.TYPE_STRUCT }, new String[] { "struct" });
		}
		// List
		if (coll instanceof List) {
			return MemberUtil.call(pc, coll, key, args, new short[] { CFTypes.TYPE_ARRAY }, new String[] { "array" });
		}
		// Date
		if (coll instanceof Date) {
			return MemberUtil.call(pc, coll, key, args, new short[] { CFTypes.TYPE_DATETIME }, new String[] { "date" });
		}
		// Boolean
		if (coll instanceof Boolean) {
			return MemberUtil.call(pc, coll, key, args, new short[] { CFTypes.TYPE_BOOLEAN }, new String[] { "boolean" });
		}
		// Number
		if (coll instanceof Number) {
			return MemberUtil.call(pc, coll, key, args, new short[] { CFTypes.TYPE_NUMERIC }, new String[] { "numeric" });
		}
		// Locale
		if (coll instanceof Locale) {
			return MemberUtil.call(pc, coll, key, args, new short[] { CFTypes.TYPE_LOCALE }, new String[] { "locale" });
		}
		// TimeZone
		if (coll instanceof TimeZone) {
			return MemberUtil.call(pc, coll, key, args, new short[] { CFTypes.TYPE_TIMEZONE }, new String[] { "timezone" });
		}

		// call Object Wrapper
		if (pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_DIRECT_JAVA_ACCESS) == SecurityManager.VALUE_YES) {
			if (doLogReflectionCalls()) LogUtil.log(pc.getConfig(), Log.LEVEL_INFO, "reflection", "call-method:" + key + " from class " + Caster.toTypeName(coll));
			if (!(coll instanceof Undefined)) return Reflector.callMethod(coll, key, args);
		}
		throw new ExpressionException("No matching Method/Function for " + key + "(" + Reflector.getDspMethods(Reflector.getClasses(args)) + ")");

	}

	// FUTURE add to interface
	public Object callFunctionWithoutNamedValues(PageContext pc, Object coll, Collection.Key key, Object[] args, boolean noNull, Object defaultValue) {
		// MUST make an independent impl for performance reasons
		try {
			if (!noNull || NullSupportHelper.full(pc)) return callFunctionWithoutNamedValues(pc, coll, key, args);
			Object obj = callFunctionWithoutNamedValues(pc, coll, key, args);
			return obj == null ? defaultValue : obj;

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}

	}

	/**
	 * @see lucee.runtime.util.VariableUtil#callFunctionWithNamedValues(lucee.runtime.PageContext,
	 *      java.lang.Object, java.lang.String, java.lang.Object[])
	 */
	@Override
	public Object callFunctionWithNamedValues(PageContext pc, Object coll, String key, Object[] args) throws PageException {
		return callFunctionWithNamedValues(pc, coll, KeyImpl.init(key), args);
	}

	@Override
	public Object callFunctionWithNamedValues(PageContext pc, Object coll, Collection.Key key, Object[] args) throws PageException {
		// Objects
		if (coll instanceof Objects) {
			return ((Objects) coll).callWithNamedValues(pc, key, Caster.toFunctionValues(args));
		}
		// call UDF
		Object prop = getLight(pc, coll, key, null);
		if (prop instanceof UDF) {
			return ((UDF) prop).callWithNamedValues(pc, key, Caster.toFunctionValues(args), false);
		}

		// Strings
		if (coll instanceof String) {
			return MemberUtil.callWithNamedValues(pc, coll, key, Caster.toFunctionValues(args), CFTypes.TYPE_STRING, "string");
		}

		throw new ExpressionException("No matching Method/Function [" + key + "] for call with named arguments found ");
	}

	// FUTURE add to interface
	public Object callFunctionWithNamedValues(PageContext pc, Object coll, Collection.Key key, Object[] args, boolean noNull, Object defaultValue) {
		// MUST make an independent impl for performance reasons
		try {
			if (!noNull || NullSupportHelper.full(pc)) return callFunctionWithNamedValues(pc, coll, key, args);
			Object obj = callFunctionWithNamedValues(pc, coll, key, args);
			return obj == null ? defaultValue : obj;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}
	}

	@Override
	public Object callFunctionWithNamedValues(PageContext pc, Object coll, Collection.Key key, Struct args) throws PageException {
		// Objects
		if (coll instanceof Objects) {
			return ((Objects) coll).callWithNamedValues(pc, key, args);
		}
		// call UDF
		Object prop = getLight(pc, coll, key, null);
		if (prop instanceof UDF) {
			return ((UDF) prop).callWithNamedValues(pc, key, args, false);
		}
		throw new ExpressionException("No matching Method/Function for call with named arguments found");
	}

	// used by generated bytecode
	public static Object recordcount(PageContext pc, Object obj) throws PageException {
		if (obj instanceof Query) return Caster.toDouble(((Query) obj).getRecordcount());
		return pc.getCollection(obj, KeyConstants._RECORDCOUNT);
	}

	// used by generated bytecode
	public static Object currentrow(PageContext pc, Object obj) throws PageException {
		if (obj instanceof Query) return Caster.toDouble(((Query) obj).getCurrentrow(pc.getId()));
		return pc.getCollection(obj, KeyConstants._CURRENTROW);
	}

	// used by generated bytecode
	public static Object columnlist(PageContext pc, Object obj) throws PageException {
		if (obj instanceof Query) {
			Key[] columnNames = ((Query) obj).getColumnNames();

			boolean upperCase = pc.getCurrentTemplateDialect() == CFMLEngine.DIALECT_CFML;

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < columnNames.length; i++) {
				if (i > 0) sb.append(',');
				sb.append(upperCase ? columnNames[i].getUpperString() : columnNames[i].getString());
			}
			return sb.toString();

		}
		return pc.getCollection(obj, KeyConstants._COLUMNLIST);
	}

}
