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
package lucee.runtime.type.scope;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;

import lucee.runtime.Component;
import lucee.runtime.ComponentScope;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Collection;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.KeyConstants;

public final class ClosureScope extends ScopeSupport implements Variables, Externalizable {

	private Argument arg;
	private Local local;
	private Variables var;
	private boolean debug;
	private boolean localAlways;

	public ClosureScope(PageContext pc, Argument arg, Local local, Variables var) {
		super("variables", SCOPE_VARIABLES, StructImpl.DEFAULT_TYPE);
		arg.setBind(true);
		local.setBind(true);
		var.setBind(true);
		this.localAlways = pc.undefinedScope().getLocalAlways();
		this.arg = arg;
		this.local = local;
		this.var = var;
		this.debug = ((PageContextImpl) pc).hasDebugOptions(ConfigPro.DEBUG_IMPLICIT_ACCESS);
	}

	/*
	 * ONLY USED BY SERIALISATION
	 */
	public ClosureScope() {
		super("variables", SCOPE_VARIABLES, StructImpl.DEFAULT_TYPE);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(arg);
		out.writeObject(local);
		out.writeObject(prepare(var));
		out.writeBoolean(debug);
		out.writeBoolean(localAlways);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		arg = (Argument) in.readObject();
		local = (Local) in.readObject();
		var = (Variables) in.readObject();
		debug = in.readBoolean();
		localAlways = in.readBoolean();
	}

	public static Variables prepare(Variables var) {

		if (!(var instanceof ComponentScope)) return var;

		VariablesImpl rtn = new VariablesImpl();
		Iterator<Entry<Key, Object>> it = var.entryIterator();
		Entry<Key, Object> e;
		while (it.hasNext()) {
			e = it.next();
			if (KeyConstants._this.equals(e.getKey()) && e.getValue() instanceof Component) break;
			rtn.setEL(e.getKey(), e.getValue());
		}

		rtn.initialize(null);
		return rtn;
	}

	/**
	 * LDEV-2213: true if the key exists in a scope captured by this closure (the local or arguments scope
	 * of the enclosing function, also through nested closures). Used so an unscoped assignment inside a
	 * closure with localMode "modern" updates that variable instead of creating a new closure local (like
	 * ACF).
	 */
	public boolean containsCapturedKey(Key key) {
		if (local.containsKey(key) || arg.containsKey(key)) return true;
		return var instanceof ClosureScope && ((ClosureScope) var).containsCapturedKey(key);
	}

	/**
	 * LDEV-4771: value from a captured local or arguments scope (also through nested closures), but not
	 * from the variables scope at the end of the chain. Used so captured function arguments win over
	 * query columns, while the variables scope keeps its place after the queries.
	 */
	public Object getCaptured(PageContext pc, Key key, Object defaultValue) {
		Object value = local.get(pc, key, CollectionUtil.NULL);
		if (value != CollectionUtil.NULL) return value;
		value = arg.get(pc, key, CollectionUtil.NULL);
		if (value != CollectionUtil.NULL) return value;
		if (var instanceof ClosureScope) return ((ClosureScope) var).getCaptured(pc, key, defaultValue);
		return defaultValue;
	}

	public Argument getArgument() {
		return arg;
	}

	public Variables getVariables() {
		return var;
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
	public int size() {
		return var.size();
	}

	@Override
	public Key[] keys() {
		return var.keys();
	}

	/*
	 * LDEV-3696: the methods of the Variables interface (get, set, remove, containsKey) are used for an
	 * explicit "variables." access inside the closure, they only see the variables scope of the
	 * enclosing component / page (like ACF). The "...Cascading" methods below also look in the
	 * captured local and arguments scopes, they are used by the undefined scope for unscoped access.
	 */
	@Override
	public Object remove(Key key) throws PageException {
		return var.remove(key);
	}

	@Override
	public Object removeEL(Key key) {
		return var.removeEL(key);
	}

	@Override
	public void clear() {
		var.clear();
	}

	@Override
	public Object get(Key key) throws PageException {
		return var.get(key);
	}

	@Override
	public Object get(PageContext pc, Key key) throws PageException {
		return var.get(pc, key);
	}

	@Override
	public Object get(Key key, Object defaultValue) {
		return var.get(key, defaultValue);
	}

	@Override
	public Object get(PageContext pc, Key key, Object defaultValue) {
		return var.get(pc, key, defaultValue);
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		return var.set(key, value);
	}

	@Override
	public Object setEL(Key key, Object value) {
		return var.setEL(key, value);
	}

	public static Object removeCascading(Variables var, Key key) throws PageException {
		if (var instanceof ClosureScope) {
			ClosureScope cs = (ClosureScope) var;
			if (cs.local.containsKey(key)) return cs.local.remove(key);
			return removeCascading(cs.var, key);
		}
		return var.remove(key);
	}

	public static Object removeELCascading(Variables var, Key key) {
		if (var instanceof ClosureScope) {
			ClosureScope cs = (ClosureScope) var;
			if (cs.local.containsKey(key)) return cs.local.removeEL(key);
			return removeELCascading(cs.var, key);
		}
		return var.removeEL(key);
	}

	public static Object getCascading(PageContext pc, Variables var, Key key, Object defaultValue) {
		if (!(var instanceof ClosureScope)) return var.get(pc, key, defaultValue);
		ClosureScope cs = (ClosureScope) var;
		// local
		Object value = cs.local.get(pc, key, CollectionUtil.NULL);
		if (value != CollectionUtil.NULL) return value;

		// arg
		value = cs.arg.get(pc, key, CollectionUtil.NULL);
		if (value != CollectionUtil.NULL) {
			if (cs.debug) UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(pc), cs.arg.getTypeAsString(), key);
			return value;
		}

		// var
		value = getCascading(pc, cs.var, key, CollectionUtil.NULL);
		if (value != CollectionUtil.NULL) {
			if (cs.debug) UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(pc), cs.var.getTypeAsString(), key);
			return value;
		}
		return defaultValue;
	}

	/**
	 * the scope (captured local, captured arguments or variables) an unscoped key is found in, null if
	 * not found
	 */
	public static Collection scopeFor(PageContext pc, Variables var, Key key) {
		if (!(var instanceof ClosureScope)) return var.get(pc, key, CollectionUtil.NULL) != CollectionUtil.NULL ? var : null;
		ClosureScope cs = (ClosureScope) var;
		if (cs.local.get(pc, key, CollectionUtil.NULL) != CollectionUtil.NULL) return cs.local;
		if (cs.arg.get(pc, key, CollectionUtil.NULL) != CollectionUtil.NULL) return cs.arg;
		return scopeFor(pc, cs.var, key);
	}

	public static Object setCascading(Variables var, Key key, Object value) throws PageException {
		if (!(var instanceof ClosureScope)) return var.set(key, value);
		ClosureScope cs = (ClosureScope) var;
		if (cs.local.containsKey(key)) return cs.local.set(key, value);
		if (cs.arg.containsKey(key)) {
			if (cs.debug) UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(), cs.arg.getTypeAsString(), key);
			return cs.arg.set(key, value);
		}
		if (cs.debug) UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(), cs.var.getTypeAsString(), key);
		return setCascading(cs.var, key, value);
	}

	public static Object setELCascading(Variables var, Key key, Object value) {
		if (!(var instanceof ClosureScope)) return var.setEL(key, value);
		ClosureScope cs = (ClosureScope) var;
		if (cs.local.containsKey(key)) return cs.local.setEL(key, value);
		if (cs.arg.containsKey(key)) {
			if (cs.debug) UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(), cs.arg.getTypeAsString(), key);
			return cs.arg.setEL(key, value);
		}
		if (cs.debug) UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(), cs.var.getTypeAsString(), key);
		return setELCascading(cs.var, key, value);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return new ClosureScope(ThreadLocalPageContext.get(), (Argument) Duplicator.duplicate(arg, deepCopy), (Local) Duplicator.duplicate(local, deepCopy),
				(Variables) Duplicator.duplicate(var, deepCopy));
	}

	@Override
	public final boolean containsKey(Key key) {
		return get(key, CollectionUtil.NULL) != CollectionUtil.NULL;
	}

	@Override
	public final boolean containsKey(PageContext pc, Key key) {
		return get(pc, key, CollectionUtil.NULL) != CollectionUtil.NULL;
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return var.keyIterator();
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return var.keysAsStringIterator();
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return var.entryIterator();
	}

	@Override
	public Iterator<Object> valueIterator() {
		return var.valueIterator();
	}

	@Override
	public void setBind(boolean bind) {
	}

	@Override
	public boolean isBind() {
		return true;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {

		DumpTable dt = (DumpTable) super.toDumpData(pageContext, maxlevel, properties);
		dt.setTitle("Closure Variable Scope");
		return dt;
	}
}