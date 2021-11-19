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
package lucee.runtime.jsr223;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;

import lucee.runtime.PageContext;
import lucee.runtime.type.scope.ScopeContext;
import lucee.runtime.type.scope.Server;

public class GlobalBinding implements Bindings {

	private final Server server;

	public GlobalBinding(PageContext pc) {
		this.server = ScopeContext.getServerScope(pc, true);
	}

	@Override
	public int size() {
		return server.size();
	}

	@Override
	public boolean isEmpty() {
		return server.isEmpty();
	}

	@Override
	public boolean containsValue(Object value) {
		return server.containsValue(value);
	}

	@Override
	public void clear() {
		server.clear();
	}

	@Override
	public Set<String> keySet() {
		return server.keySet();
	}

	@Override
	public Collection<Object> values() {
		return server.values();
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return server.entrySet();
	}

	@Override
	public Object put(String name, Object value) {
		return server.put(name, value);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> toMerge) {
		server.putAll(toMerge);
	}

	@Override
	public boolean containsKey(Object key) {
		return server.containsKey(key);
	}

	@Override
	public Object get(Object key) {
		return server.get(key);
	}

	@Override
	public Object remove(Object key) {
		return server.remove(key);
	}

}