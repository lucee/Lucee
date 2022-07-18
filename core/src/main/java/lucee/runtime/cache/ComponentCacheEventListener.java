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
package lucee.runtime.cache;

import lucee.commons.io.cache.CacheEntry;
import lucee.commons.io.cache.CacheEventListener;
import lucee.runtime.Component;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;

public class ComponentCacheEventListener implements CacheEventListener {

	private static final long serialVersionUID = 6271280246677734153L;
	private static final Collection.Key ON_EXPIRES = KeyImpl.getInstance("onExpires");
	private static final Collection.Key ON_PUT = KeyImpl.getInstance("onPut");
	private static final Collection.Key ON_REMOVE = KeyImpl.getInstance("onRemove");
	private Component component;

	public ComponentCacheEventListener(Component component) {
		this.component = component;
	}

	@Override
	public void onRemove(CacheEntry entry) {
		call(ON_REMOVE, entry);
	}

	@Override
	public void onPut(CacheEntry entry) {
		call(ON_PUT, entry);
	}

	@Override
	public void onExpires(CacheEntry entry) {
		call(ON_EXPIRES, entry);
	}

	private void call(Key methodName, CacheEntry entry) {
		// Struct data = entry.getCustomInfo();
		// cfc.callWithNamedValues(pc, methodName, data);
	}

	@Override
	public CacheEventListener duplicate() {
		return new ComponentCacheEventListener((Component) component.duplicate(false));
	}

}