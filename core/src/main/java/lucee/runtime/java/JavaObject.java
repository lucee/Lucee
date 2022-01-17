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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Operator;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.reflection.pairs.MethodInstance;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.ObjectWrap;
import lucee.runtime.type.Objects;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.util.VariableUtil;
import lucee.runtime.util.VariableUtilImpl;

/**
 * class to handle initialising and call native object from lucee
 */
public class JavaObject implements Objects, ObjectWrap {

	private static final long serialVersionUID = -3716657460843769960L;

	private Class clazz;
	private boolean isInit = false;
	private Object object;
	private transient VariableUtil _variableUtil;

	/**
	 * constructor with className to load
	 * 
	 * @param variableUtil
	 * @param clazz
	 * @throws ExpressionException
	 */
	public JavaObject(VariableUtil variableUtil, Class clazz) {
		this._variableUtil = variableUtil;
		this.clazz = clazz;
	}

	public JavaObject(VariableUtil variableUtil, Object object) {
		this._variableUtil = variableUtil;
		this.clazz = object.getClass();
		this.object = object;
		isInit = object != null;
	}

	public Object get(PageContext pc, String propertyName) throws PageException {
		if (isInit) {
			return variableUtil(pc).get(pc, object, propertyName);
		}

		if (VariableUtilImpl.doLogReflectionCalls())
			LogUtil.log(pc.getConfig(), Log.LEVEL_INFO, "reflection", "get-property:" + propertyName + " from class " + Caster.toTypeName(clazz));
		// Check Field
		Field[] fields = Reflector.getFieldsIgnoreCase(clazz, propertyName, null);
		if (!ArrayUtil.isEmpty(fields) && Modifier.isStatic(fields[0].getModifiers())) {
			try {
				return fields[0].get(null);
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
		}
		// Getter
		MethodInstance mi = Reflector.getGetterEL(clazz, propertyName);
		if (mi != null) {
			if (Modifier.isStatic(mi.getMethod().getModifiers())) {
				try {
					return mi.invoke(null);
				}
				catch (IllegalAccessException e) {
					throw Caster.toPageException(e);
				}
				catch (InvocationTargetException e) {
					throw Caster.toPageException(e.getTargetException());
				}
			}
		}
		// male Instance
		return variableUtil(pc).get(pc, init(), propertyName);
	}

	private VariableUtil variableUtil(PageContext pc) {
		if (_variableUtil != null) return _variableUtil;
		return ThreadLocalPageContext.get(pc).getVariableUtil();
	}

	@Override
	public Object get(PageContext pc, Collection.Key key) throws PageException {
		return get(pc, key.getString());
	}

	public Object get(PageContext pc, String propertyName, Object defaultValue) {
		if (isInit) {
			return variableUtil(pc).get(pc, object, propertyName, defaultValue);
		}
		if (VariableUtilImpl.doLogReflectionCalls())
			LogUtil.log(pc.getConfig(), Log.LEVEL_INFO, "reflection", "get-property:" + propertyName + " from class " + Caster.toTypeName(clazz));

		// Field
		Field[] fields = Reflector.getFieldsIgnoreCase(clazz, propertyName, null);
		if (!ArrayUtil.isEmpty(fields) && Modifier.isStatic(fields[0].getModifiers())) {
			try {
				return fields[0].get(null);
			}
			catch (Exception e) {
			}
		}
		// Getter
		MethodInstance mi = Reflector.getGetterEL(clazz, propertyName);
		if (mi != null) {
			if (Modifier.isStatic(mi.getMethod().getModifiers())) {
				try {
					return mi.invoke(null);
				}
				catch (Exception e) {
				}
			}
		}
		try {
			return variableUtil(pc).get(pc, init(), propertyName, defaultValue);
		}
		catch (PageException e1) {
			return defaultValue;
		}
	}

	@Override
	public Object get(PageContext pc, Collection.Key key, Object defaultValue) {
		return get(pc, key.getString(), defaultValue);
	}

	@Override
	public Object set(PageContext pc, Collection.Key propertyName, Object value) throws PageException {
		if (isInit) {
			return ((VariableUtilImpl) variableUtil(pc)).set(pc, object, propertyName, value);
		}

		if (VariableUtilImpl.doLogReflectionCalls())
			LogUtil.log(pc.getConfig(), Log.LEVEL_INFO, "reflection", "set-property:" + propertyName + " in class " + Caster.toTypeName(clazz));

		// Field
		Field[] fields = Reflector.getFieldsIgnoreCase(clazz, propertyName.getString(), null);
		if (!ArrayUtil.isEmpty(fields) && Modifier.isStatic(fields[0].getModifiers())) {
			try {
				fields[0].set(null, value);
				return value;
			}
			catch (Exception e) {
				Caster.toPageException(e);
			}
		}
		// Getter
		MethodInstance mi = Reflector.getSetter(clazz, propertyName.getString(), value, null);
		if (mi != null) {
			if (Modifier.isStatic(mi.getMethod().getModifiers())) {
				try {
					return mi.invoke(null);
				}
				catch (IllegalAccessException e) {
					throw Caster.toPageException(e);
				}
				catch (InvocationTargetException e) {
					throw Caster.toPageException(e.getTargetException());
				}
			}
		}

		return ((VariableUtilImpl) variableUtil(pc)).set(pc, init(), propertyName, value);
	}

	@Override
	public Object setEL(PageContext pc, Collection.Key propertyName, Object value) {
		if (isInit) {
			return variableUtil(pc).setEL(pc, object, propertyName, value);
		}

		if (VariableUtilImpl.doLogReflectionCalls())
			LogUtil.log(pc.getConfig(), Log.LEVEL_INFO, "reflection", "set-property:" + propertyName + " in class " + Caster.toTypeName(clazz));

		// Field
		Field[] fields = Reflector.getFieldsIgnoreCase(clazz, propertyName.getString(), null);
		if (!ArrayUtil.isEmpty(fields) && Modifier.isStatic(fields[0].getModifiers())) {
			try {
				fields[0].set(null, value);
			}
			catch (Exception e) {
			}
			return value;
		}
		// Getter
		MethodInstance mi = Reflector.getSetter(clazz, propertyName.getString(), value, null);
		if (mi != null) {
			if (Modifier.isStatic(mi.getMethod().getModifiers())) {
				try {
					return mi.invoke(null);
				}
				catch (Exception e) {
				}
			}
		}

		try {
			return variableUtil(pc).setEL(pc, init(), propertyName, value);
		}
		catch (PageException e1) {
			return value;
		}
	}

	public Object call(PageContext pc, String methodName, Object[] arguments) throws PageException {
		if (arguments == null) arguments = new Object[0];

		if (VariableUtilImpl.doLogReflectionCalls())
			LogUtil.log(pc.getConfig(), Log.LEVEL_INFO, "reflection", "call-method:" + methodName + " from class " + Caster.toTypeName(clazz));

		// edge cases
		if (methodName.equalsIgnoreCase("init")) {
			return init(arguments);
		}
		else if (methodName.equalsIgnoreCase("getClass")) {
			return clazz;
		}
		else if (isInit) {
			return Reflector.callMethod(object, methodName, arguments);
		}

		try {
			// get method
			MethodInstance mi = Reflector.getMethodInstance(this, clazz, KeyImpl.init(methodName), arguments);
			// call static method if exist
			if (Modifier.isStatic(mi.getMethod().getModifiers())) {
				return mi.invoke(null);
			}

			if (arguments.length == 0 && methodName.equalsIgnoreCase("getClass")) {
				return clazz;
			}

			// invoke constructor and call instance method
			return mi.invoke(init());
		}
		catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			if (target instanceof PageException) throw (PageException) target;
			throw Caster.toPageException(e.getTargetException());
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public Object call(PageContext pc, Collection.Key methodName, Object[] arguments) throws PageException {
		return call(pc, methodName.getString(), arguments);
	}

	public Object callWithNamedValues(PageContext pc, String methodName, Struct args) throws PageException {
		Iterator<Object> it = args.valueIterator();
		List<Object> values = new ArrayList<Object>();
		while (it.hasNext()) {
			values.add(it.next());
		}
		return call(pc, methodName, values.toArray(new Object[values.size()]));
	}

	@Override
	public Object callWithNamedValues(PageContext pc, Collection.Key methodName, Struct args) throws PageException {
		return callWithNamedValues(pc, methodName.getString(), args);
	}

	/**
	 * initialize method (default no object)
	 * 
	 * @return initialize object
	 * @throws PageException
	 */
	private Object init() throws PageException {
		return init(new Object[0]);
	}

	private Object init(Object defaultValue) {
		return init(new Object[0], defaultValue);
	}

	/**
	 * initialize method
	 * 
	 * @param arguments
	 * @return Initalised Object
	 * @throws PageException
	 */
	private Object init(Object[] arguments) throws PageException {
		object = Reflector.callConstructor(clazz, arguments);
		isInit = true;
		return object;
	}

	private Object init(Object[] arguments, Object defaultValue) {
		object = Reflector.callConstructor(clazz, arguments, defaultValue);
		isInit = object != defaultValue;
		return object;
	}

	@Override
	public Object getEmbededObject() throws PageException {
		if (object == null) init();
		return object;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties props) {
		try {
			return DumpUtil.toDumpData(getEmbededObject(), pageContext, maxlevel, props);
		}
		catch (PageException e) {
			return DumpUtil.toDumpData(clazz, pageContext, maxlevel, props);
		}
	}

	/**
	 * @return the containing Class
	 */
	public Class getClazz() {
		return clazz;
	}

	public boolean isInitalized() {
		return isInit;
	}

	@Override
	public String castToString() throws PageException {
		return Caster.toString(getEmbededObject());
	}

	@Override
	public String castToString(String defaultValue) {
		try {
			return Caster.toString(getEmbededObject(), defaultValue);
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return Caster.toBooleanValue(getEmbededObject());
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		try {
			return Caster.toBoolean(getEmbededObject(), defaultValue);
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return Caster.toDoubleValue(getEmbededObject());
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		try {
			return Caster.toDoubleValue(getEmbededObject(), true, defaultValue);
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return Caster.toDatetime(getEmbededObject(), null);
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		try {
			return DateCaster.toDateAdvanced(getEmbededObject(), DateCaster.CONVERTING_TYPE_OFFSET, null, defaultValue);
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	@Override
	public Object getEmbededObject(Object def) {
		if (object == null) init(def);
		return object;
	}

	/**
	 * @return the object
	 */
	public Object getObject() {
		return object;
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		return Operator.compare(castToBooleanValue(), b);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return Operator.compare((Date) castToDateTime(), (Date) dt);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return Operator.compare(castToDoubleValue(), d);
	}

	@Override
	public int compareTo(String str) throws PageException {
		return Operator.compare(castToString(), str);
	}

}