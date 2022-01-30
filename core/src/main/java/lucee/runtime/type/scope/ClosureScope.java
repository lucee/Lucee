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

public class ClosureScope extends ScopeSupport implements Variables, Externalizable {

	private Argument arg;
	private Local local;
	private Variables var;
	private boolean debug;
	private boolean localAlways;

	public ClosureScope(PageContext pc, Argument arg, Local local, Variables var) {
		super("variables", SCOPE_VARIABLES, StructImpl.TYPE_UNDEFINED);
		arg.setBind(true);
		local.setBind(true);
		var.setBind(true);
		this.localAlways = pc.undefinedScope().getLocalAlways();
		this.arg = arg;
		this.local = local;
		this.var = var;
		this.debug = pc.getConfig().debug();
	}

	/*
	 * ONLY USED BY SERIALISATION
	 */
	public ClosureScope() {
		super("variables", SCOPE_VARIABLES, StructImpl.TYPE_UNDEFINED);
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

	@Override
	public Object remove(Key key) throws PageException {
		if (local.containsKey(key)) return local.remove(key);
		return var.remove(key);
	}

	@Override
	public Object removeEL(Key key) {
		if (local.containsKey(key)) return local.removeEL(key);
		return var.removeEL(key);
	}

	@Override
	public void clear() {
		var.clear();
	}

	@Override
	public Object get(Key key) throws PageException {
		return get((PageContext) null, key);
	}

	@Override
	public Object get(PageContext pc, Key key) throws PageException {
		Object _null = CollectionUtil.NULL;
		Object value = local.get(pc, key, _null);
		if (value != _null) return value;
		value = arg.get(pc, key, _null);
		if (value != _null) {
			if (debug) UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(pc), arg.getTypeAsString(), key);
			return value;
		}

		value = var.get(pc, key);
		if (debug) UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(pc), var.getTypeAsString(), key);
		return value;
	}

	@Override
	public Object get(Key key, Object defaultValue) {
		return get(null, key, defaultValue);
	}

	@Override
	public Object get(PageContext pc, Key key, Object defaultValue) {
		Object _null = CollectionUtil.NULL;

		// local
		Object value = local.get(pc, key, _null);
		if (value != _null) return value;

		// arg
		value = arg.get(pc, key, _null);
		if (value != _null) {
			if (debug) UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(pc), arg.getTypeAsString(), key);
			return value;
		}

		// var
		value = var.get(pc, key, _null);
		if (value != _null) {
			if (debug) UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(pc), var.getTypeAsString(), key);
			return value;
		}
		return defaultValue;
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		if (localAlways || local.containsKey(key)) return local.set(key, value);
		if (arg.containsKey(key)) {
			if (debug) UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(), arg.getTypeAsString(), key);
			return arg.set(key, value);
		}
		if (debug) UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(), var.getTypeAsString(), key);
		return var.set(key, value);
	}

	@Override
	public Object setEL(Key key, Object value) {
		if (localAlways || local.containsKey(key)) return local.setEL(key, value);
		if (arg.containsKey(key)) {
			if (debug) UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(), arg.getTypeAsString(), key);
			return arg.setEL(key, value);
		}

		if (debug) UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(), var.getTypeAsString(), key);
		return var.setEL(key, value);
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