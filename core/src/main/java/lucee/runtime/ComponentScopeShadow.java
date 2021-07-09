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
package lucee.runtime;

import java.util.Iterator;
import java.util.Map;

import lucee.commons.collection.MapFactory;
import lucee.commons.lang.CFTypes;
import lucee.runtime.component.Member;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.it.EntryIterator;
import lucee.runtime.type.it.KeyIterator;
import lucee.runtime.type.it.StringIterator;
import lucee.runtime.type.it.ValueIterator;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.ComponentUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.MemberUtil;
import lucee.runtime.type.util.StructSupport;
import lucee.runtime.type.util.StructUtil;

public class ComponentScopeShadow extends StructSupport implements ComponentScope {

	private static final long serialVersionUID = 4930100230796574243L;

	private final ComponentImpl component;
	private static final int access = Component.ACCESS_PRIVATE;
	private final Map<Key, Object> shadow;

	/**
	 * Constructor of the class
	 * 
	 * @param component
	 * @param shadow
	 */
	public ComponentScopeShadow(ComponentImpl component, Map<Key, Object> shadow) {
		this.component = component;
		this.shadow = shadow;

	}

	/**
	 * Constructor of the class
	 * 
	 * @param component
	 * @param shadow
	 */
	public ComponentScopeShadow(ComponentImpl component, ComponentScopeShadow scope, boolean cloneShadow) {
		this.component = component;
		this.shadow = cloneShadow ? Duplicator.duplicateMap(scope.shadow, MapFactory.getConcurrentMap(), false) : scope.shadow;
	}

	@Override
	public Component getComponent() {
		return component.top;
	}

	@Override
	public int getType() {
		return SCOPE_VARIABLES;
	}

	@Override
	public String getTypeAsString() {
		return "variables";
	}

	@Override
	public void initialize(PageContext pc) {
	}

	@Override
	public boolean isInitalized() {
		return component.isInitalized();
	}

	@Override
	public void release(PageContext pc) {
	}

	@Override
	public void clear() {
		shadow.clear();
	}

	@Override
	public final boolean containsKey(Collection.Key key) {
		return get(key, null) != null;
	}

	@Override
	public final boolean containsKey(PageContext pc, Collection.Key key) {
		return get(pc, key, null) != null;
	}

	@Override
	public Object get(Key key) throws PageException {
		Object o = get(key, CollectionUtil.NULL);
		if (o != CollectionUtil.NULL) return o;
		throw new ExpressionException("Component [" + component.getCallName() + "] has no accessible Member with name [" + key + "]");
	}

	@Override
	public Object get(Key key, Object defaultValue) {
		return get(null, key, defaultValue);
	}

	@Override
	public Object get(PageContext pc, Key key, Object defaultValue) {
		if (key.equalsIgnoreCase(KeyConstants._SUPER)) {
			Component ac = ComponentUtil.getActiveComponent(ThreadLocalPageContext.get(pc), component);
			return SuperComponent.superInstance((ComponentImpl) ac.getBaseComponent());
		}
		if (key.equalsIgnoreCase(KeyConstants._THIS)) return component.top;
		if (key.equalsIgnoreCase(KeyConstants._STATIC)) return component.staticScope();

		Object val = shadow.getOrDefault(key, CollectionUtil.NULL);
		if (val == CollectionUtil.NULL) return defaultValue;
		if (val == null && !NullSupportHelper.full(pc)) return defaultValue;
		return val;
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return new KeyIterator(keys());
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return new StringIterator(keys());
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return new EntryIterator(this, keys());
	}

	@Override
	public Iterator<Object> valueIterator() {
		return new ValueIterator(this, keys());
	}

	@Override
	public Collection.Key[] keys() {
		Collection.Key[] keys = new Collection.Key[shadow.size() + 1];
		Iterator<Key> it = shadow.keySet().iterator();
		int index = 0;
		while (it.hasNext()) {
			keys[index++] = it.next();
		}
		keys[index] = KeyConstants._THIS;
		return keys;
	}

	@Override
	public Object remove(Collection.Key key) throws PageException {
		if (key.equalsIgnoreCase(KeyConstants._this) || key.equalsIgnoreCase(KeyConstants._super) || key.equalsIgnoreCase(KeyConstants._static))
			throw new ExpressionException("key [" + key.getString() + "] is part of the component and can't be removed");

		if (NullSupportHelper.full()) {
			if (!shadow.containsKey(key)) throw new ExpressionException("can't remove key [" + key.getString() + "] from struct, key doesn't exist");
			return shadow.remove(key);
		}

		Object o = shadow.remove(key);
		if (o != null) return o;
		throw new ExpressionException("can't remove key [" + key.getString() + "] from struct, key doesn't exist");
	}

	@Override
	public Object removeEL(Key key) {
		if (key.equalsIgnoreCase(KeyConstants._this) || key.equalsIgnoreCase(KeyConstants._super) || key.equalsIgnoreCase(KeyConstants._static)) return null;

		return shadow.remove(key);
	}

	@Override
	public Object set(Collection.Key key, Object value) throws ApplicationException {
		if (key.equalsIgnoreCase(KeyConstants._this) || key.equalsIgnoreCase(KeyConstants._super) || key.equalsIgnoreCase(KeyConstants._static)) return value;

		if (!component.afterConstructor && value instanceof UDF) {
			component.addConstructorUDF(key, (UDF) value);
		}
		shadow.put(key, value);
		return value;
	}

	@Override
	public Object setEL(Collection.Key key, Object value) {
		try {
			return set(key, value);
		}
		catch (ApplicationException e) {
			return value;
		}
	}

	@Override
	public int size() {
		return keys().length;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		DumpTable cp = StructUtil.toDumpTable(this, "Component Variable Scope", pageContext, maxlevel, dp);
		cp.setComment("Component: " + component.getPageSource().getComponentName());
		return cp;
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		throw new ExpressionException("Can't cast Complex Object Type to a boolean value");
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return defaultValue;
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		throw new ExpressionException("Can't cast Complex Object Type to a Date Object");
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return defaultValue;
	}

	@Override
	public double castToDoubleValue() throws PageException {
		throw new ExpressionException("Can't cast Complex Object Type to a numeric value");
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return defaultValue;
	}

	@Override
	public String castToString() throws PageException {
		throw new ExpressionException("Can't cast Complex Object Type to a String");
	}

	@Override
	public String castToString(String defaultValue) {
		return defaultValue;
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		throw new ExpressionException("can't compare Complex Object with a boolean value");
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		throw new ExpressionException("can't compare Complex Object with a DateTime Object");
	}

	@Override
	public int compareTo(double d) throws PageException {
		throw new ExpressionException("can't compare Complex Object with a numeric value");
	}

	@Override
	public int compareTo(String str) throws PageException {
		throw new ExpressionException("can't compare Complex Object with a String");
	}

	/*
	 * public Object call(PageContext pc, String key, Object[] arguments) throws PageException { return
	 * call(pc, KeyImpl.init(key), arguments); }
	 */

	@Override
	public Object call(PageContext pc, Collection.Key key, Object[] arguments) throws PageException {
		// first check variables
		Object o = shadow.get(key);
		if (o instanceof UDF) {
			return ((UDF) o).call(pc, key, arguments, false);
		}

		// then check in component
		Member m = component.getMember(access, key, false, false);
		if (m != null) {
			if (m instanceof UDF) return ((UDF) m).call(pc, key, arguments, false);
		}

		return MemberUtil.call(pc, this, key, arguments, new short[] { CFTypes.TYPE_STRUCT }, new String[] { "struct" });
		// throw ComponentUtil.notFunction(component, key, m!=null?m.getValue():null,access);
	}

	/*
	 * public Object callWithNamedValues(PageContext pc, String key,Struct args) throws PageException {
	 * return callWithNamedValues(pc, KeyImpl.init(key), args); }
	 */

	@Override
	public Object callWithNamedValues(PageContext pc, Key key, Struct args) throws PageException {
		// first check variables
		Object o = shadow.get(key);
		if (o instanceof UDF) {
			return ((UDF) o).callWithNamedValues(pc, key, args, false);
		}

		Member m = component.getMember(access, key, false, false);
		if (m != null) {
			if (m instanceof UDF) return ((UDF) m).callWithNamedValues(pc, key, args, false);
			return MemberUtil.callWithNamedValues(pc, this, key, args, CFTypes.TYPE_STRUCT, "struct");
			// throw ComponentUtil.notFunction(component, key, m.getValue(),access);
		}
		return MemberUtil.callWithNamedValues(pc, this, key, args, CFTypes.TYPE_STRUCT, "struct");
		// throw ComponentUtil.notFunction(component, key, null,access);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		StructImpl sct = new StructImpl();
		StructImpl.copy(this, sct, deepCopy);
		return sct;
		// MUST muss deepCopy checken
		// return new ComponentScopeShadow(component,shadow);//new
		// ComponentScopeThis(component.cloneComponentImpl());
	}

	/*
	 * public Object get(PageContext pc, String key, Object defaultValue) { return get(key,
	 * defaultValue); }
	 */

	@Override
	public Object set(PageContext pc, Collection.Key propertyName, Object value) throws PageException {
		return set(propertyName, value);
	}

	/*
	 * public Object setEL(PageContext pc, String propertyName, Object value) { return
	 * setEL(propertyName, value); }
	 */

	@Override
	public Object setEL(PageContext pc, Collection.Key propertyName, Object value) {
		return setEL(propertyName, value);
	}

	/*
	 * public Object get(PageContext pc, String key) throws PageException { return get(key); }
	 */

	@Override
	public Object get(PageContext pc, Collection.Key key) throws PageException {
		return get(key);
	}

	public Map<Key, Object> getShadow() {
		return shadow;
	}

	@Override
	public void setBind(boolean bind) {
	}

	@Override
	public boolean isBind() {
		return true;
	}
}