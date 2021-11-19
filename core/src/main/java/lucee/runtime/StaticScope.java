/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.component.DataMember;
import lucee.runtime.component.Member;
import lucee.runtime.component.StaticStruct;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.debug.DebugEntryTemplate;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Objects;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.scope.Variables;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.StructSupport;

public class StaticScope extends StructSupport implements Variables, Objects {

	private static final long serialVersionUID = -2692540782121852340L;

	private final StaticScope base;
	private ComponentPageImpl cp;
	private final int dataMemberDefaultAccess;
	private final ComponentImpl c;

	public StaticScope(StaticScope base, ComponentImpl c, ComponentPageImpl cp, int dataMemberDefaultAccess) {
		this.base = base;
		this.cp = cp;
		this.c = c;
		this.dataMemberDefaultAccess = dataMemberDefaultAccess;
	}

	public PageSource getPageSource() {
		return this.cp.getPageSource();
	}

	@Override
	public int size() {
		int s = cp.getStaticStruct().size();
		return (base == null) ? s : base.size() + s;

	}

	public Member _remove(PageContext pc, Key key) throws PageException {
		StaticStruct ss = cp.getStaticStruct();
		// does the current struct has this key
		Member m = ss.get(key);
		if (m != null) {
			if (m.getModifier() == Member.MODIFIER_FINAL)
				throw new ExpressionException("Cannot remove key [" + key + "] in static scope from component [" + cp.getComponentName() + "], that member is set to final");

			if (!c.isAccessible(ThreadLocalPageContext.get(pc), m.getAccess()))
				throw new ExpressionException("Component from type [" + cp.getComponentName() + "] has no accessible static Member with name [" + key + "]");
			return ss.remove(key);
		}
		// if not the parent (inside the static constructor we do not remove keys from base static scopes)
		if (base != null && !c.insideStaticConstr.getOrDefault(ThreadLocalPageContext.getThreadId(pc), Boolean.FALSE)) return base._remove(pc, key);
		return null;
	}

	@Override
	public Object remove(Key key) throws PageException {
		Member m = _remove(ThreadLocalPageContext.get(), key);
		if (m != null) return m.getValue();
		return null;
	}

	@Override
	public Object removeEL(Key key) {
		try {
			return remove(key);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return null;
		}
	}

	@Override
	public void clear() {
		if (base != null) base.clear();
		cp.getStaticStruct().clear();
	}

	private Member _get(PageContext pc, Key key, Member defaultValue) {
		// does the current struct has this key
		StaticStruct ss = cp.getStaticStruct();
		if (!ss.isEmpty()) {
			Member m = ss.get(key);

			if (m != null) {
				if (c.isAccessible(pc, m)) return m;
				return null;
			}
		}
		// if not the parent
		if (base != null) return base._get(pc, key, defaultValue);
		return null;
	}

	@Override
	public Object get(Key key) throws PageException {
		return get((PageContext) null, key);
	}

	@Override
	public Object get(PageContext pc, Key key) throws PageException {
		if (key.equalsIgnoreCase(KeyConstants._STATIC)) return c.top._static;

		Member m = _get(ThreadLocalPageContext.get(pc), key, null);
		if (m != null) return m.getValue();

		throw new ExpressionException("Component from type [" + cp.getComponentName() + "] has no accessible static Member with name [" + key + "]");
	}

	@Override
	public Object get(Key key, Object defaultValue) {
		return get(null, key, defaultValue);
	}

	@Override
	public Object get(PageContext pc, Key key, Object defaultValue) {
		if (key.equalsIgnoreCase(KeyConstants._STATIC)) return c.top._static;
		Member m = _get(ThreadLocalPageContext.get(pc), key, null);
		if (m != null) return m.getValue();
		return defaultValue;
	}

	public Member getMember(PageContext pc, Key key, Member defaultValue) {
		return _get(ThreadLocalPageContext.get(pc), key, null);
	}

	private Member _setIfExists(PageContext pc, Key key, Object value) throws PageException {
		// does the current struct has this key
		Member m = cp.getStaticStruct().get(key);
		if (m != null) {
			if (m.getModifier() == Member.MODIFIER_FINAL)
				throw new ExpressionException("Cannot update key [" + key + "] in static scope from component [" + cp.getComponentName() + "], that member is set to final");

			return _set(pc, m, key, value);
		}
		// if not the parent (we only do this if we are outside the static constructor)
		if (base != null && !c.insideStaticConstr.getOrDefault(ThreadLocalPageContext.getThreadId(pc), Boolean.FALSE)) return base._setIfExists(pc, key, value);
		return null;
	}

	private Member _set(PageContext pc, Member existing, Key key, Object value) throws ExpressionException {
		if (value instanceof Member) {
			return cp.getStaticStruct().put(key, (Member) value);
		}

		// check if user has access
		if (!c.isAccessible(pc, existing != null ? existing.getAccess() : dataMemberDefaultAccess))
			throw new ExpressionException("Component from type [" + cp.getComponentName() + "] has no accessible static Member with name [" + key + "]");

		// set
		return cp.getStaticStruct().put(key,
				new DataMember(existing != null ? existing.getAccess() : dataMemberDefaultAccess, existing != null ? existing.getModifier() : Member.MODIFIER_NONE, value));
	}

	@Override
	public Object set(Key propertyName, Object value) throws PageException {
		return set(null, propertyName, value);
	}

	@Override
	public Object set(PageContext pc, Key key, Object value) throws PageException {
		pc = ThreadLocalPageContext.get(pc);
		Member m = _setIfExists(pc, key, value);
		if (m != null) return m.getValue();
		// if not exists set to current
		m = _set(pc, null, key, value);
		if (m != null) return m.getValue();
		return null;
	}

	@Override
	public Object setEL(Key key, Object value) {
		return setEL(null, key, value);
	}

	@Override
	public Object setEL(PageContext pc, Key propertyName, Object value) {
		try {
			return set(ThreadLocalPageContext.get(pc), propertyName, value);
		}
		catch (PageException e) {
			return null;
		}
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return this;
	}

	@Override
	public final boolean containsKey(Key key) {
		if (base != null && base.containsKey(key)) return true;
		return cp.getStaticStruct().containsKey(key);
	}

	@Override
	public final boolean containsKey(PageContext pc, Key key) {
		if (base != null && base.containsKey(pc, key)) return true;
		StaticStruct ss = cp.getStaticStruct();
		return cp.getStaticStruct().containsKey(key);
	}

	@Override
	public Key[] keys() {
		Set<Key> keys = _entries(new HashMap<Key, Object>(), c.getAccess(ThreadLocalPageContext.get())).keySet();
		return keys.toArray(new Key[keys.size()]);
	}

	@Override
	public Iterator<Key> keyIterator() {
		return _entries(new HashMap<Key, Object>(), c.getAccess(ThreadLocalPageContext.get())).keySet().iterator();
	}

	@Override
	public Iterator<Object> valueIterator() {
		return _entries(new HashMap<Key, Object>(), c.getAccess(ThreadLocalPageContext.get())).values().iterator();
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return _entries(new HashMap<Key, Object>(), c.getAccess(ThreadLocalPageContext.get())).entrySet().iterator();
	}

	private Map<Key, Object> _entries(Map<Key, Object> map, int access) {
		// call parent
		if (base != null) base._entries(map, access);

		// fill accessable keys
		if (!cp.getStaticStruct().isEmpty()) {
			Iterator<Entry<Key, Member>> it = cp.getStaticStruct().entrySet().iterator();
			Entry<Key, Member> e;
			while (it.hasNext()) {
				e = it.next();
				if (e.getValue().getAccess() <= access) map.put(e.getKey(), e.getValue().getValue());
			}
		}
		return map;
	}

	private Map<Key, Member> all(Map<Key, Member> map) {
		// call parent
		if (base != null) base.all(map);

		// fill accessable keys
		if (!cp.getStaticStruct().isEmpty()) {
			Iterator<Entry<Key, Member>> it = cp.getStaticStruct().entrySet().iterator();
			Entry<Key, Member> e;
			while (it.hasNext()) {
				e = it.next();
				map.put(e.getKey(), e.getValue());
			}
		}
		return map;
	}

	@Override
	public Object call(PageContext pc, Key key, Object[] args) throws PageException {
		Member m = _get(pc, key, null);
		if (m instanceof UDF) {
			return _call(pc, key, ((UDF) m), null, args);
		}

		throw new ExpressionException("Component from type [" + cp.getComponentName() + "] has no accessible static Member with name [" + key + "]");
	}

	@Override
	public Object callWithNamedValues(PageContext pc, Key key, Struct args) throws PageException {
		Member m = _get(pc, key, null);
		if (m instanceof UDF) {
			return _call(pc, key, ((UDF) m), args, null);
		}

		throw new ExpressionException("Component from type [" + cp.getComponentName() + "] has no accessible static Member with name [" + key + "]");
	}

	Object _call(PageContext pc, Collection.Key calledName, UDF udf, Struct namedArgs, Object[] args) throws PageException {

		Object rtn = null;
		Variables parent = null;

		// INFO duplicate code is for faster execution -> less contions

		// debug yes
		if (pc.getConfig().debug() && ((ConfigPro) pc.getConfig()).hasDebugOptions(ConfigPro.DEBUG_TEMPLATE)) {
			DebugEntryTemplate debugEntry = pc.getDebugger().getEntry(pc, cp.getPageSource(), udf.getFunctionName());// new DebugEntry(src,udf.getFunctionName());
			long currTime = pc.getExecutionTime();
			long time = System.nanoTime();

			try {
				parent = c.beforeStaticConstructor(pc);
				if (args != null) rtn = udf.call(pc, calledName, args, true);
				else rtn = udf.callWithNamedValues(pc, calledName, namedArgs, true);
			}
			finally {
				c.afterStaticConstructor(pc, parent);
				long diff = ((System.nanoTime() - time) - (pc.getExecutionTime() - currTime));
				pc.setExecutionTime(pc.getExecutionTime() + diff);
				debugEntry.updateExeTime(diff);
			}
		}

		// debug no
		else { // this.cp._static
			try {
				parent = c.beforeStaticConstructor(pc);
				if (args != null) rtn = udf.call(pc, calledName, args, true);
				else rtn = udf.callWithNamedValues(pc, calledName, namedArgs, true);
			}
			finally {
				c.afterStaticConstructor(pc, parent);
			}
		}
		return rtn;
	}

	@Override
	public boolean isInitalized() {
		return true;
	}

	@Override
	public void initialize(PageContext pc) {

	}

	@Override
	public void release(PageContext pc) {

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
	public void setBind(boolean bind) {
	}

	@Override
	public boolean isBind() {
		return true;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		int access = c.getAccess(pageContext);
		DumpTable table = new DumpTable("component", "#99cc99", "#ccffcc", "#000000");
		table.setTitle("Static Scope from Component " + cp.getComponentName());
		table.setComment("Only the functions and data members that are accessible from your location are displayed");

		DumpTable content = _toDumpData(c.top, pageContext, maxlevel, dp, access);
		if (!content.isEmpty()) table.appendRow(1, new SimpleDumpData(""), content);
		return table;
	}

	DumpTable _toDumpData(ComponentImpl ci, PageContext pc, int maxlevel, DumpProperties dp, int access) {
		maxlevel--;

		DumpTable[] accesses = new DumpTable[4];
		accesses[Component.ACCESS_PRIVATE] = new DumpTable("#ff6633", "#ff9966", "#000000");
		accesses[Component.ACCESS_PRIVATE].setTitle("private");
		accesses[Component.ACCESS_PRIVATE].setWidth("100%");
		accesses[Component.ACCESS_PACKAGE] = new DumpTable("#ff9966", "#ffcc99", "#000000");
		accesses[Component.ACCESS_PACKAGE].setTitle("package");
		accesses[Component.ACCESS_PACKAGE].setWidth("100%");
		accesses[Component.ACCESS_PUBLIC] = new DumpTable("#ffcc99", "#ffffcc", "#000000");
		accesses[Component.ACCESS_PUBLIC].setTitle("public");
		accesses[Component.ACCESS_PUBLIC].setWidth("100%");

		Iterator<Entry<Key, Member>> it = all(new HashMap<Key, Member>()).entrySet().iterator();
		Entry<Key, Member> e;
		while (it.hasNext()) {
			e = it.next();
			int a = access(pc, e.getValue().getAccess());

			DumpTable box = accesses[a];
			Object o = e.getValue().getValue();

			if (DumpUtil.keyValid(dp, maxlevel, e.getKey())) box.appendRow(1, new SimpleDumpData(e.getKey().getString()), DumpUtil.toDumpData(o, pc, maxlevel, dp));
		}

		DumpTable table = new DumpTable("#ffffff", "#cccccc", "#000000");

		if (!accesses[Component.ACCESS_PUBLIC].isEmpty()) {
			table.appendRow(0, accesses[Component.ACCESS_PUBLIC]);
		}
		if (!accesses[Component.ACCESS_PACKAGE].isEmpty()) {
			table.appendRow(0, accesses[Component.ACCESS_PACKAGE]);
		}
		if (!accesses[Component.ACCESS_PRIVATE].isEmpty()) {
			table.appendRow(0, accesses[Component.ACCESS_PRIVATE]);
		}
		return table;
	}

	private int access(PageContext pc, int access) {
		if (access > -1) return access;
		return pc.getConfig().getComponentDataMemberDefaultAccess();
	}

	public Component getComponent() {
		return c;
	}

}