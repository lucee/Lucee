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

public class EngineBinding implements Bindings {

	private PageContext pc;

	public EngineBinding(PageContext pc) {
		this.pc = pc;
	}

	@Override
	public int size() {
		return pc.undefinedScope().size();
	}

	@Override
	public boolean isEmpty() {
		return pc.undefinedScope().isEmpty();
	}

	@Override
	public boolean containsValue(Object value) {
		return pc.undefinedScope().containsValue(value);
	}

	@Override
	public void clear() {
		pc.undefinedScope().clear();
	}

	@Override
	public Set<String> keySet() {
		return pc.undefinedScope().keySet();
	}

	@Override
	public Collection<Object> values() {
		return pc.undefinedScope().values();
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return pc.undefinedScope().entrySet();
	}

	@Override
	public Object put(String name, Object value) {
		return pc.undefinedScope().put(name, value);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> toMerge) {
		pc.undefinedScope().putAll(toMerge);
	}

	@Override
	public boolean containsKey(Object key) {
		return pc.undefinedScope().containsKey(key);
	}

	@Override
	public Object get(Object key) {
		return pc.undefinedScope().get(key);
	}

	@Override
	public Object remove(Object key) {
		return pc.undefinedScope().remove(key);
	}

}