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
import java.util.Set;

import lucee.commons.lang.types.RefBoolean;
import lucee.runtime.component.Member;
import lucee.runtime.component.Property;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Objects;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.UDFProperties;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.scope.Variables;
import lucee.runtime.type.util.StructSupport;

public final class ComponentSpecificAccess extends StructSupport implements Component, Objects {

	private int access;
	private Component component;

	/**
	 * constructor of the class
	 * 
	 * @param access
	 * @param component
	 * @throws ExpressionException
	 */
	public ComponentSpecificAccess(int access, Component component) {
		this.access = access;
		this.component = component;
	}

	public static ComponentSpecificAccess toComponentSpecificAccess(int access, Component component) {
		if (component instanceof ComponentSpecificAccess) {
			ComponentSpecificAccess csa = (ComponentSpecificAccess) component;
			if (access == csa.getAccess()) return csa;
			component = csa.getComponent();
		}

		return new ComponentSpecificAccess(access, component);
	}

	@Override
	public PageSource getPageSource() {
		return component.getPageSource();
	}

	@Override
	public Set keySet() {
		return component.keySet(access);
	}

	@Override
	public String getDisplayName() {
		return component.getDisplayName();
	}

	@Override
	public String getExtends() {
		return component.getExtends();
	}

	@Override
	public String getHint() {
		return component.getHint();
	}

	@Override
	public String getName() {
		return component.getName();
	}

	@Override
	public String getCallName() {
		return component.getCallName();
	}

	@Override
	public String getAbsName() {
		return component.getAbsName();
	}

	@Override
	public String getBaseAbsName() {
		return component.getBaseAbsName();
	}

	@Override
	public boolean isBasePeristent() {
		return component.isPersistent();
	}

	@Override
	public boolean getOutput() {
		return component.getOutput();
	}

	@Override
	public boolean instanceOf(String type) {
		return component.instanceOf(type);
	}

	@Override
	public boolean isValidAccess(int access) {
		return component.isValidAccess(access);
	}

	@Override
	public Struct getMetaData(PageContext pc) throws PageException {
		return component.getMetaData(pc);
	}

	@Override
	public Object call(PageContext pc, String key, Object[] args) throws PageException {
		return call(pc, KeyImpl.init(key), args);
	}

	@Override
	public Object call(PageContext pc, Collection.Key key, Object[] args) throws PageException {
		return component.call(pc, access, key, args);
	}

	@Override
	public Object callWithNamedValues(PageContext pc, String key, Struct args) throws PageException {
		return callWithNamedValues(pc, KeyImpl.init(key), args);
	}

	@Override
	public Object callWithNamedValues(PageContext pc, Collection.Key key, Struct args) throws PageException {
		return component.callWithNamedValues(pc, access, key, args);
	}

	@Override
	public int size() {
		return component.size(access);
	}

	@Override
	public Collection.Key[] keys() {
		return component.keys(access);
	}

	@Override
	public Object remove(Collection.Key key) throws PageException {
		return component.remove(key);
	}

	@Override
	public Object removeEL(Collection.Key key) {
		return component.removeEL(key);
	}

	@Override
	public void clear() {
		component.clear();
	}

	@Override
	public Object get(Collection.Key key) throws PageException {
		return component.get(access, key);
	}

	@Override
	public Object get(Collection.Key key, Object defaultValue) {
		return component.get(access, key, defaultValue);
	}

	@Override
	public Object set(Collection.Key key, Object value) throws PageException {
		return component.set(key, value);
	}

	@Override
	public Object setEL(Collection.Key key, Object value) {
		return component.setEL(key, value);
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return component.keyIterator(access);
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return component.keysAsStringIterator(access);
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return component.entryIterator(access);
	}

	@Override
	public Iterator<Object> valueIterator() {
		return component.valueIterator(access);
	}

	@Override
	public final boolean containsKey(Collection.Key key) {
		return component.get(access, key, null) != null;
	}

	@Override
	public final boolean containsKey(PageContext pc, Collection.Key key) {
		return component.get(access, key, null) != null;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return component.toDumpData(pageContext, maxlevel, dp, access);
	}

	@Override
	public String castToString() throws PageException {
		return component.castToString();
	}

	@Override
	public String castToString(String defaultValue) {
		return component.castToString(defaultValue);
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return component.castToBooleanValue();
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return component.castToBoolean(defaultValue);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return component.castToDoubleValue();
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return component.castToDoubleValue(defaultValue);
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return component.castToDateTime();
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return component.castToDateTime(defaultValue);
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		return component.compareTo(b);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return component.compareTo(dt);
	}

	@Override
	public int compareTo(String str) throws PageException {
		return component.compareTo(str);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return component.compareTo(d);
	}

	/*
	 * public Object get(PageContext pc, String key, Object defaultValue) { return
	 * get(pc,KeyImpl.init(key),defaultValue); }
	 */

	@Override
	public Object get(PageContext pc, Collection.Key key, Object defaultValue) {
		return component.get(access, key, defaultValue);
	}

	/*
	 * public Object get(PageContext pc, String key) throws PageException { return
	 * get(pc,KeyImpl.init(key)); }
	 */

	@Override
	public Object get(PageContext pc, Collection.Key key) throws PageException {
		return component.get(access, key);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return new ComponentSpecificAccess(access, (Component) component.duplicate(deepCopy));
	}

	/*
	 * public Object set(PageContext pc, String propertyName, Object value) throws PageException {
	 * return component.set(propertyName,value); }
	 */

	@Override
	public Object set(PageContext pc, Collection.Key propertyName, Object value) throws PageException {
		return component.set(propertyName, value);
	}

	/*
	 * public Object setEL(PageContext pc, String propertyName, Object value) { return
	 * component.setEL(propertyName,value); }
	 */

	@Override
	public Object setEL(PageContext pc, Key propertyName, Object value) {
		return component.setEL(propertyName, value);
	}

	public int getAccess() {
		return access;
	}

	@Override
	public Class getJavaAccessClass(RefBoolean isNew) throws PageException {
		return component.getJavaAccessClass(isNew);
	}

	@Override
	public String getWSDLFile() {
		return component.getWSDLFile();
	}

	@Override
	public Property[] getProperties(boolean onlyPeristent) {
		return component.getProperties(onlyPeristent);
	}

	@Override
	public Property[] getProperties(boolean onlyPeristent, boolean includeBaseProperties, boolean overrideProperties, boolean inheritedMappedSuperClassOnly) {
		return component.getProperties(onlyPeristent, includeBaseProperties, overrideProperties, inheritedMappedSuperClassOnly);
	}

	@Override
	public ComponentScope getComponentScope() {
		return component.getComponentScope();
	}

	public Component getComponent() {
		return component;
	}

	@Override
	public boolean contains(PageContext pc, Key key) {
		return component.contains(access, key);
	}

	@Override
	public Member getMember(int access, Key key, boolean dataMember, boolean superAccess) {
		return component.getMember(access, key, dataMember, superAccess);
	}

	@Override
	public void setProperty(Property property) throws PageException {
		component.setProperty(property);
	}

	@Override
	public boolean equalTo(String type) {
		return component.equalTo(type);
	}

	@Override
	public void registerUDF(Collection.Key key, UDF udf) throws PageException {
		component.registerUDF(key, udf);
	}

	@Override
	public void registerUDF(Collection.Key key, UDFProperties props) throws PageException {
		component.registerUDF(key, props);
	}

	@Override
	public boolean isPersistent() {
		return component.isPersistent();
	}

	@Override
	public boolean isAccessors() {
		return component.isAccessors();
	}

	@Override
	public void setEntity(boolean entity) {
		component.setEntity(entity);
	}

	@Override
	public boolean isEntity() {
		return component.isEntity();
	}

	@Override
	public Component getBaseComponent() {
		return component.getBaseComponent();
	}

	@Override
	public Set<Key> keySet(int access) {
		return component.keySet(access);
	}

	@Override
	public Object getMetaStructItem(Key name) {
		return component.getMetaStructItem(name);
	}

	@Override
	public Object call(PageContext pc, int access, Key name, Object[] args) throws PageException {
		return component.call(pc, access, name, args);
	}

	@Override
	public Object callWithNamedValues(PageContext pc, int access, Key name, Struct args) throws PageException {
		return component.callWithNamedValues(pc, access, name, args);
	}

	@Override
	public int size(int access) {
		return component.size();
	}

	@Override
	public Key[] keys(int access) {
		return component.keys(access);
	}

	@Override
	public Iterator<Key> keyIterator(int access) {
		return component.keyIterator(access);
	}

	@Override
	public Iterator<String> keysAsStringIterator(int access) {
		return component.keysAsStringIterator(access);
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator(int access) {
		return component.entryIterator(access);
	}

	@Override
	public Iterator<Object> valueIterator(int access) {
		return component.valueIterator(access);
	}

	@Override
	public Object get(int access, Key key) throws PageException {
		return component.get(access, key);
	}

	@Override
	public Object get(int access, Key key, Object defaultValue) {
		return component.get(access, key, defaultValue);
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp, int access) {
		return toDumpData(pageContext, maxlevel, dp, access);
	}

	@Override
	public boolean contains(int access, Key name) {
		return component.contains(access, name);
	}

	@Override
	public Class getJavaAccessClass(PageContext pc, RefBoolean isNew, boolean writeLog, boolean takeTop, boolean create, boolean supressWSbeforeArg) throws PageException {
		return component.getJavaAccessClass(pc, isNew, writeLog, takeTop, create, supressWSbeforeArg);
	}

	@Override
	public Class getJavaAccessClass(PageContext pc, RefBoolean isNew, boolean writeLog, boolean takeTop, boolean create, boolean supressWSbeforeArg, boolean output,
			boolean returnValue) throws PageException {
		return component.getJavaAccessClass(pc, isNew, writeLog, takeTop, create, supressWSbeforeArg, output, returnValue);
	}

	@Override
	public int getModifier() {
		return component.getModifier();
	}

	@Override
	public String id() {
		return component.id();
	}

	@Override
	public Scope staticScope() {
		return component.staticScope();
	}

	@Override
	public Variables beforeStaticConstructor(PageContext pc) {
		return component.beforeStaticConstructor(pc);
	}

	@Override
	public void afterStaticConstructor(PageContext pc, Variables var) {
		component.afterStaticConstructor(pc, var);
	}

	@Override
	public Interface[] getInterfaces() {
		return component.getInterfaces();
	}

	@Override
	public int getType() {
		if (component instanceof ComponentImpl) {
			return ((ComponentImpl) component).getType();
		}
		return Struct.TYPE_REGULAR;
	}
}