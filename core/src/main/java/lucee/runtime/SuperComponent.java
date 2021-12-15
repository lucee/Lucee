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
/**
 * 
 */
package lucee.runtime;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import lucee.commons.lang.types.RefBoolean;
import lucee.runtime.component.DataMember;
import lucee.runtime.component.Member;
import lucee.runtime.component.MemberSupport;
import lucee.runtime.component.Property;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.UDFProperties;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.scope.Variables;
import lucee.runtime.type.util.StructUtil;

/**
 * 
 */
public class SuperComponent extends MemberSupport implements Component, Member {

	private ComponentImpl comp;

	private SuperComponent(ComponentImpl comp) {
		super(Component.ACCESS_PRIVATE);
		this.comp = comp;
	}

	public static Member superMember(ComponentImpl comp) {
		if (comp == null) return new DataMember(Component.ACCESS_PRIVATE, Member.MODIFIER_NONE, new StructImpl());
		return new SuperComponent(comp);
	}

	public static Collection superInstance(ComponentImpl comp) {
		if (comp == null) return new StructImpl();
		return new SuperComponent(comp);
	}

	@Override
	public Object getValue() {
		return this;
	}

	@Override
	public Object call(PageContext pc, String name, Object[] args) throws PageException {
		return comp._call(pc, getAccess(), KeyImpl.init(name), null, args, true);
	}

	@Override
	public Object call(PageContext pc, Key name, Object[] args) throws PageException {
		return comp._call(pc, getAccess(), name, null, args, true);
	}

	@Override
	public Object callWithNamedValues(PageContext pc, String name, Struct args) throws PageException {
		return comp._call(pc, getAccess(), KeyImpl.init(name), args, null, true);
	}

	@Override
	public Object callWithNamedValues(PageContext pc, Key methodName, Struct args) throws PageException {
		return comp._call(pc, getAccess(), methodName, args, null, true);
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return comp.castToBooleanValue(true);
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return comp.castToBoolean(true, defaultValue);
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return comp.castToDateTime(true);
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return comp.castToDateTime(true, defaultValue);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return comp.castToDoubleValue(true);
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return comp.castToDoubleValue(true, defaultValue);
	}

	@Override
	public String castToString() throws PageException {
		return comp.castToString(true);
	}

	@Override
	public String castToString(String defaultValue) {
		return comp.castToString(true, defaultValue);
	}

	@Override
	public void clear() {
		comp.clear();
	}

	@Override
	public Object clone() {
		return duplicate(true);
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		return comp.compareTo(b);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return comp.compareTo(dt);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return comp.compareTo(d);
	}

	@Override
	public int compareTo(String str) throws PageException {
		return comp.compareTo(str);
	}

	@Override
	public boolean containsKey(String name) {
		return comp.contains(getAccess(), (name));
	}

	@Override
	public boolean containsKey(Key key) {
		return comp.contains(getAccess(), key.getLowerString());
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return new SuperComponent((ComponentImpl) Duplicator.duplicate(comp, deepCopy));
	}

	@Override
	public Object get(PageContext pc, Key key) throws PageException {
		return get(key);
	}

	@Override
	public Object get(PageContext pc, Key key, Object defaultValue) {
		return get(key, defaultValue);
	}

	@Override
	public Object get(String name) throws PageException {
		return get(KeyImpl.init(name));
	}

	@Override
	public Object get(String name, Object defaultValue) {
		return get(KeyImpl.init(name), defaultValue);
	}

	@Override
	public Object get(Key key) throws PageException {
		Member member = comp.getMember(getAccess(), key, true, true);
		if (member != null) return member.getValue();
		return comp.get(getAccess(), key);
	}

	@Override
	public Object get(Key key, Object defaultValue) {
		Member member = comp.getMember(getAccess(), key, true, true);
		if (member != null) return member.getValue();
		return comp.get(getAccess(), key, defaultValue);
	}

	@Override
	public String getAbsName() {
		return comp.getAbsName();
	}

	@Override
	public String getBaseAbsName() {
		return comp.getBaseAbsName();
	}

	@Override
	public boolean isBasePeristent() {
		return comp.isPersistent();
	}

	@Override
	public int getModifier() {
		return comp.getModifier();
	}

	@Override
	public String getCallName() {
		return comp.getCallName();
	}

	@Override
	public String getDisplayName() {
		return comp.getDisplayName();
	}

	@Override
	public String getExtends() {
		return comp.getExtends();
	}

	@Override
	public String getHint() {
		return comp.getHint();
	}

	@Override
	public Class getJavaAccessClass(RefBoolean isNew) throws PageException {
		return comp.getJavaAccessClass(isNew);
	}

	@Override
	public Struct getMetaData(PageContext pc) throws PageException {
		return comp.getMetaData(pc);
	}

	@Override
	public String getName() {
		return comp.getName();
	}

	@Override
	public boolean getOutput() {
		return comp.getOutput();
	}

	@Override
	public boolean instanceOf(String type) {
		return comp.top.instanceOf(type);
	}

	public boolean isInitalized() {
		return comp.top.isInitalized();
	}

	@Override
	public boolean isValidAccess(int access) {
		return comp.isValidAccess(access);
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return comp.keyIterator(getAccess());
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return comp.keysAsStringIterator(getAccess());
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return comp.entryIterator(getAccess());
	}

	@Override
	public Key[] keys() {
		return comp.keys(getAccess());
	}

	@Override
	public Object remove(Key key) throws PageException {
		return comp.remove(key);
	}

	@Override
	public Object removeEL(Key key) {
		return comp.removeEL(key);
	}

	@Override
	public Object remove(Key key, Object defaultValue) {
		return comp.remove(key, defaultValue);
	}

	@Override
	public Object set(PageContext pc, Key key, Object value) throws PageException {
		return comp.set(pc, key, value);
	}

	@Override
	public Object set(String name, Object value) throws PageException {
		return comp.set(name, value);
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		return comp.set(key, value);
	}

	@Override
	public Object setEL(PageContext pc, Key name, Object value) {
		return comp.setEL(pc, name, value);
	}

	@Override
	public Object setEL(String name, Object value) {
		return comp.setEL(name, value);
	}

	@Override
	public Object setEL(Key key, Object value) {
		return comp.setEL(key, value);
	}

	@Override
	public int size() {
		return comp.size(getAccess());
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return comp.top.toDumpData(pageContext, maxlevel, dp);
	}

	@Override
	public PageSource getPageSource() {
		return comp.getPageSource();
	}

	@Override
	public boolean containsKey(Object key) {
		return containsKey(KeyImpl.toKey(key, null));
	}

	@Override
	public Set entrySet() {
		return StructUtil.entrySet(this);
	}

	@Override
	public Object get(Object key) {
		return get(KeyImpl.toKey(key, null), null);
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public Set keySet() {
		return StructUtil.keySet(this);
	}

	@Override
	public Object put(Object key, Object value) {
		return setEL(KeyImpl.toKey(key, null), value);
	}

	@Override
	public void putAll(Map map) {
		StructUtil.putAll(this, map);
	}

	@Override
	public Object remove(Object key) {
		return removeEL(KeyImpl.toKey(key, null));
	}

	@Override
	public java.util.Collection values() {
		return StructUtil.values(this);
	}

	@Override
	public boolean containsValue(Object value) {
		return values().contains(value);
	}

	@Override
	public Iterator<Object> valueIterator() {
		return comp.valueIterator();
	}

	@Override
	public Property[] getProperties(boolean onlyPeristent) {
		return comp.getProperties(onlyPeristent);
	}

	@Override
	public Property[] getProperties(boolean onlyPeristent, boolean includeBaseProperties, boolean overrideProperties, boolean inheritedMappedSuperClassOnly) {
		return comp.getProperties(onlyPeristent, includeBaseProperties, overrideProperties, inheritedMappedSuperClassOnly);
	}

	@Override
	public ComponentScope getComponentScope() {
		return comp.getComponentScope();
	}

	@Override
	public boolean contains(PageContext pc, Key key) {
		return comp.contains(getAccess(), key);
	}

	/*
	 * private Member getMember(int access, Key key, boolean dataMember,boolean superAccess) { return
	 * comp.getMember(access, key, dataMember, superAccess); }
	 */

	@Override
	public void setProperty(Property property) throws PageException {
		comp.setProperty(property);
	}

	@Override
	public boolean equalTo(String type) {
		return comp.top.equalTo(type);
	}

	@Override
	public String getWSDLFile() {
		return comp.getWSDLFile();
	}

	@Override
	public void registerUDF(Collection.Key key, UDF udf) throws ApplicationException {
		comp.registerUDF(key, udf);
	}

	@Override
	public void registerUDF(Collection.Key key, UDFProperties props) throws ApplicationException {
		comp.registerUDF(key, props);
	}

	@Override
	public java.util.Iterator<String> getIterator() {
		return keysAsStringIterator();
	}

	@Override
	public Class getJavaAccessClass(PageContext pc, RefBoolean isNew, boolean writeLog, boolean takeTop, boolean create, boolean supressWSbeforeArg) throws PageException {
		return comp.getJavaAccessClass(pc, isNew, writeLog, takeTop, create, supressWSbeforeArg);
	}

	@Override
	public Class getJavaAccessClass(PageContext pc, RefBoolean isNew, boolean writeLog, boolean takeTop, boolean create, boolean supressWSbeforeArg, boolean output,
			boolean returnValue) throws PageException {
		return comp.getJavaAccessClass(pc, isNew, writeLog, takeTop, create, supressWSbeforeArg, output, returnValue);
	}

	@Override
	public boolean isPersistent() {
		return comp.isPersistent();
	}

	@Override
	public boolean isAccessors() {
		return comp.isAccessors();
	}

	@Override
	public void setEntity(boolean entity) {
		comp.setEntity(entity);
	}

	@Override
	public boolean isEntity() {
		return comp.isEntity();
	}

	@Override
	public Component getBaseComponent() {
		return comp.getBaseComponent();
	}

	@Override
	public Set<Key> keySet(int access) {
		return comp.keySet(access);
	}

	@Override
	public Object getMetaStructItem(Key name) {
		return comp.getMetaStructItem(name);
	}

	@Override
	public Object call(PageContext pc, int access, Key name, Object[] args) throws PageException {
		return comp.call(pc, access, name, args);
	}

	@Override
	public Object callWithNamedValues(PageContext pc, int access, Key name, Struct args) throws PageException {
		return comp.callWithNamedValues(pc, access, name, args);
	}

	@Override
	public int size(int access) {
		return comp.size();
	}

	@Override
	public Key[] keys(int access) {
		return comp.keys(access);
	}

	@Override
	public Iterator<Key> keyIterator(int access) {
		return comp.keyIterator(access);
	}

	@Override
	public Iterator<String> keysAsStringIterator(int access) {
		return comp.keysAsStringIterator(access);
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator(int access) {
		return comp.entryIterator(access);
	}

	@Override
	public Iterator<Object> valueIterator(int access) {
		return comp.valueIterator(access);
	}

	@Override
	public Object get(int access, Key key) throws PageException {
		return comp.get(access, key);
	}

	@Override
	public Object get(int access, Key key, Object defaultValue) {
		return comp.get(access, key, defaultValue);
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp, int access) {
		return toDumpData(pageContext, maxlevel, dp, access);
	}

	@Override
	public boolean contains(int access, Key name) {
		return comp.contains(access, name);
	}

	@Override
	public Member getMember(int access, Key key, boolean dataMember, boolean superAccess) {
		return comp.getMember(access, key, dataMember, superAccess);
	}

	@Override
	public Scope staticScope() {
		return comp.staticScope();
	}

	@Override
	public Variables beforeStaticConstructor(PageContext pc) {
		return comp.beforeStaticConstructor(pc);
	}

	@Override
	public void afterStaticConstructor(PageContext pc, Variables var) {
		comp.afterStaticConstructor(pc, var);
	}

	@Override
	public Interface[] getInterfaces() {
		return comp.getInterfaces();
	}

	@Override
	public String id() {
		return comp.id();
	}
}