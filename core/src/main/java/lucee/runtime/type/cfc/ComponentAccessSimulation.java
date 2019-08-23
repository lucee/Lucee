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
package lucee.runtime.type.cfc;

public class ComponentAccessSimulation {// implements ComponentAccess {
	/*
	 * private Component cfc;
	 * 
	 * public ComponentAccessSimulation(Component cfc){ this.cfc=cfc; }
	 * 
	 * 
	 * public ComponentAccess _base() { return cfc._base(); }
	 * 
	 * public boolean hasInjectedFunctions() { return cfc.hasInjectedFunctions(); }
	 * 
	 * public void setEntity(boolean entity) { cfc.setEntity(entity); }
	 * 
	 * 
	 * public boolean isEntity() { return cfc.isEntity(); }
	 * 
	 * 
	 * public long sizeOf() { return cfc.sizeOf(); }
	 * 
	 * public Collection duplicate(boolean deepCopy) { return cfc.duplicate(deepCopy); }
	 * 
	 * public int size(int access) { return cfc.size(access); }
	 * 
	 * public Set<Key> keySet(int access) { return cfc.keySet(access); }
	 * 
	 * public Iterator<Key> keyIterator(int access) { // TODO Auto-generated method stub return
	 * cfc.keyIterator(access); }
	 * 
	 * public Iterator<String> keysAsStringIterator(int access) { // TODO Auto-generated method stub
	 * return cfc.keysAsStringIterator(access); }
	 * 
	 * public Iterator<Entry<Key, Object>> entryIterator(int access) { // TODO Auto-generated method
	 * stub return cfc.entryIterator(access); }
	 * 
	 * public Iterator<Object> valueIterator(int access) { // TODO Auto-generated method stub return
	 * cfc.valueIterator(access); }
	 * 
	 * public Iterator<Object> valueIterator() { return cfc.valueIterator(); }
	 * 
	 * 
	 * public Key[] keys(int access) { return cfc.keys(access); }
	 * 
	 * public void clear() { cfc.clear(); }
	 * 
	 * public Member getMember(int access, Key key, boolean dataMember, boolean superAccess) { return
	 * cfc.getMember(access, key, dataMember, superAccess); }
	 * 
	 * protected Member getMember(PageContext pc, Key key, boolean dataMember, boolean superAccess) {
	 * return cfc.getMember(pc, key, dataMember, superAccess); }
	 * 
	 * public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) { return
	 * cfc.toDumpData(pageContext, maxlevel, dp); }
	 * 
	 * public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp, int access)
	 * { return cfc.toDumpData(pageContext, maxlevel, dp, access); }
	 * 
	 * protected String getCallPath() { return cfc.getCallPath(); }
	 * 
	 * 
	 * public String getDisplayName() { return cfc.getDisplayName(); }
	 * 
	 * 
	 * public String getExtends() { return cfc.getExtends(); }
	 * 
	 * 
	 * public String getBaseAbsName() { return cfc.getBaseAbsName(); }
	 * 
	 * public boolean isBasePeristent() { return cfc.isBasePeristent(); }
	 * 
	 * 
	 * public String getHint() { return cfc.getHint(); }
	 * 
	 * public String getWSDLFile() { return cfc.getWSDLFile(); }
	 * 
	 * public String getName() { return cfc.getName(); }
	 * 
	 * public String _getName() { return cfc._getName(); }
	 * 
	 * 
	 * public PageSource _getPageSource() { return cfc._getPageSource(); }
	 * 
	 * public String getCallName() { return cfc.getCallName(); }
	 * 
	 * public String getAbsName() { return cfc.getAbsName(); }
	 * 
	 * public boolean getOutput() { return cfc.getOutput(); }
	 * 
	 * public boolean instanceOf(String type) { return cfc.instanceOf(type); }
	 * 
	 * public boolean equalTo(String type) { return cfc.equalTo(type); }
	 * 
	 * public boolean isValidAccess(int access) { return cfc.isValidAccess(access); }
	 * 
	 * public PageSource getPageSource() { return cfc.getPageSource(); }
	 * 
	 * public String castToString() throws PageException { return cfc.castToString(); }
	 * 
	 * public String castToString(String defaultValue) { return cfc.castToString(defaultValue); }
	 * 
	 * public boolean castToBooleanValue() throws PageException { return cfc.castToBooleanValue(); }
	 * 
	 * public Boolean castToBoolean(Boolean defaultValue) { return cfc.castToBoolean(defaultValue); }
	 * 
	 * public double castToDoubleValue() throws PageException { return cfc.castToDoubleValue(); }
	 * 
	 * 
	 * public double castToDoubleValue(double defaultValue) { return
	 * cfc.castToDoubleValue(defaultValue); }
	 * 
	 * public DateTime castToDateTime() throws PageException { return cfc.castToDateTime(); }
	 * 
	 * public DateTime castToDateTime(DateTime defaultValue) { return cfc.castToDateTime(defaultValue);
	 * }
	 * 
	 * public synchronized Struct getMetaData(PageContext pc) throws PageException { return
	 * cfc.getMetaData(pc); }
	 * 
	 * public synchronized Object getMetaStructItem(Key name) { return cfc.getMetaStructItem(name); }
	 * 
	 * public void registerUDF(String key, UDF udf) { cfc.registerUDF(key, udf); }
	 * 
	 * public void registerUDF(String key, UDFProperties prop) { cfc.registerUDF(key, prop); }
	 * 
	 * public void registerUDF(Key key, UDF udf) { cfc.registerUDF(key, udf); }
	 * 
	 * 
	 * public void registerUDF(Key key, UDFProperties prop) { cfc.registerUDF(key, prop); }
	 * 
	 * public void registerUDF(Key key, UDFPlus udf, boolean useShadow, boolean injected) {
	 * cfc.registerUDF(key, udf, useShadow, injected); }
	 * 
	 * public Object remove(Key key) throws PageException { return cfc.remove(key); }
	 * 
	 * public Object removeEL(Key key) { return cfc.removeEL(key); }
	 * 
	 * public Object set(PageContext pc, Key key, Object value) throws PageException { return
	 * cfc.set(pc, key, value); }
	 * 
	 * public Object set(Key key, Object value) throws PageException { return cfc.set(key, value); }
	 * 
	 * public Object setEL(PageContext pc, Key name, Object value) { return cfc.setEL(pc, name, value);
	 * }
	 * 
	 * public Object setEL(Key key, Object value) { return cfc.setEL(key, value); }
	 * 
	 * public Object get(PageContext pc, Key key) throws PageException { return cfc.get(pc, key); }
	 * 
	 * public Object get(int access, Key key) throws PageException { return cfc.get(access, key); }
	 * 
	 * public Object get(PageContext pc, Key key, Object defaultValue) { return cfc.get(pc, key,
	 * defaultValue); }
	 * 
	 * public Object get(int access, Key key, Object defaultValue) { return cfc.get(access, key,
	 * defaultValue); }
	 * 
	 * public Object get(Key key) throws PageException { return cfc.get(key); }
	 * 
	 * public Object get(Key key, Object defaultValue) { return cfc.get(key, defaultValue); }
	 * 
	 * public Object call(PageContext pc, String name, Object[] args) throws PageException { return
	 * cfc.call(pc, name, args); }
	 * 
	 * public Object call(PageContext pc, Key name, Object[] args) throws PageException { return
	 * cfc.call(pc, name, args); }
	 * 
	 * public Object call(PageContext pc, int access, Key name, Object[] args) throws PageException {
	 * return cfc.call(pc, access, name, args); }
	 * 
	 * public Object callWithNamedValues(PageContext pc, String name, Struct args) throws PageException
	 * { return cfc.callWithNamedValues(pc, name, args); }
	 * 
	 * public Object callWithNamedValues(PageContext pc, Key methodName, Struct args) throws
	 * PageException { return cfc.callWithNamedValues(pc, methodName, args); }
	 * 
	 * public Object callWithNamedValues(PageContext pc, int access, Key name, Struct args) throws
	 * PageException { return cfc.callWithNamedValues(pc, access, name, args); }
	 * 
	 * public boolean contains(PageContext pc, Key key) { return cfc.contains(pc, key); }
	 * 
	 * public boolean containsKey(Key key) { return cfc.containsKey(key); }
	 * 
	 * public boolean contains(int access, Key name) { return cfc.contains(access, name); }
	 * 
	 * public Iterator<Key> keyIterator() { return cfc.keyIterator(); }
	 * 
	 * public Iterator<String> keysAsStringIterator() { return cfc.keysAsStringIterator(); }
	 * 
	 * public Iterator<Entry<Key, Object>> entryIterator() { return cfc.entryIterator(); }
	 * 
	 * public Key[] keys() { return cfc.keys(); }
	 * 
	 * public int size() { return cfc.size(); }
	 * 
	 * public boolean isPersistent() { return cfc.isPersistent(); }
	 * 
	 * 
	 * public void setProperty(Property property) throws PageException { cfc.setProperty(property); }
	 * 
	 * public Property[] getProperties(boolean onlyPeristent) { return cfc.getProperties(onlyPeristent);
	 * }
	 * 
	 * public Property[] getProperties(boolean onlyPeristent, boolean includeBaseProperties, boolean
	 * preferBaseProperties, boolean inheritedMappedSuperClassOnly) { return
	 * cfc.getProperties(onlyPeristent, includeBaseProperties, preferBaseProperties,
	 * inheritedMappedSuperClassOnly); }
	 * 
	 * public ComponentScope getComponentScope() { return cfc.getComponentScope(); }
	 * 
	 * public int compareTo(boolean b) throws PageException { return cfc.compareTo(b); }
	 * 
	 * public int compareTo(DateTime dt) throws PageException { return cfc.compareTo(dt); }
	 * 
	 * 
	 * public int compareTo(double d) throws PageException { return cfc.compareTo(d); }
	 * 
	 * 
	 * public int compareTo(String str) throws PageException { return cfc.compareTo(str); }
	 * 
	 * public Set entrySet() { return cfc.entrySet(); }
	 * 
	 * 
	 * public Set keySet() { return cfc.keySet(); }
	 * 
	 * 
	 * public String toString() { return cfc.toString(); }
	 * 
	 * 
	 * public java.util.Collection values() { return cfc.values(); }
	 * 
	 * 
	 * public boolean containsValue(Object value) { return cfc.containsValue(value); }
	 * 
	 * 
	 * public Iterator<String> getIterator() { return cfc.getIterator(); }
	 * 
	 * 
	 * public boolean equals(Object obj) { return cfc.equals(obj); }
	 */

}