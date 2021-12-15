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
package lucee.runtime.type.scope;

import java.io.Serializable;

import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigServer;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;

public final class ClusterWrap extends ScopeSupport implements Cluster {

	private static final long serialVersionUID = -4952656252539755770L;

	private ClusterRemote core;
	private int offset;
	private ConfigServer configServer;

	public ClusterWrap(ConfigServer cs, ClusterRemote core) {
		this(cs, core, false);
	}

	private ClusterWrap(ConfigServer configServer, ClusterRemote core, boolean duplicate) {
		super("cluster", Scope.SCOPE_CLUSTER, Struct.TYPE_LINKED);
		this.configServer = configServer;
		if (duplicate) this.core = core.duplicate();
		else this.core = core;
		this.core.init(configServer, this);
	}

	@Override
	public void init(ConfigServer configServer) {
		// for the custer wrap this method is not invoked, but it is part of the interface
	}

	@Override
	public Object get(Key key) throws PageException {
		return ((ClusterEntry) super.get(key)).getValue();
	}

	@Override
	public Object get(PageContext pc, Key key) throws PageException {
		return ((ClusterEntry) super.get(pc, key)).getValue();
	}

	@Override
	public Object get(Key key, Object defaultValue) {
		Object res = super.get(key, defaultValue);
		if (res instanceof ClusterEntry) return ((ClusterEntry) res).getValue();
		return res;
	}

	@Override
	public Object get(PageContext pc, Key key, Object defaultValue) {
		Object res = super.get(pc, key, defaultValue);
		if (res instanceof ClusterEntry) return ((ClusterEntry) res).getValue();
		return res;
	}

	@Override
	public Object remove(Key key) throws PageException {
		core.addEntry(new ClusterEntryImpl(key, null, offset));
		return ((ClusterEntry) super.remove(key)).getValue();

	}

	@Override
	public Object removeEL(Key key) {
		core.addEntry(new ClusterEntryImpl(key, null, offset));
		ClusterEntry entry = (ClusterEntry) super.removeEL(key);
		if (entry != null) return entry.getValue();
		return null;
	}

	@Override
	public Object setEL(Key key, Object value) {
		if (core.checkValue(value)) {
			ClusterEntry entry;
			core.addEntry(entry = new ClusterEntryImpl(key, (Serializable) value, offset));
			super.setEL(key, entry);
		}
		return value;
	}

	@Override
	public void setEntry(ClusterEntry newEntry) {
		ClusterEntry existingEntry = (ClusterEntry) super.get(newEntry.getKey(), null);
		// add
		if (existingEntry == null || existingEntry.getTime() < newEntry.getTime()) {
			if (newEntry.getValue() == null) removeEL(newEntry.getKey());
			else {
				core.addEntry(newEntry);
				super.setEL(newEntry.getKey(), newEntry);
			}
		}
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		if (!core.checkValue(value)) throw new ExpressionException("object from type [" + Caster.toTypeName(value) + "] are not allowed in cluster scope");
		ClusterEntry entry;
		core.addEntry(entry = new ClusterEntryImpl(key, (Serializable) value, offset));
		super.setEL(key, entry);
		return value;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return super.toDumpData(pageContext, maxlevel, dp);
	}

	@Override
	public int getType() {
		return SCOPE_CLUSTER;
	}

	@Override
	public String getTypeAsString() {
		return "cluster";
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return new ClusterWrap(configServer, core, true);
	}

	@Override
	public void broadcast() {
		core.broadcastEntries();
	}
}