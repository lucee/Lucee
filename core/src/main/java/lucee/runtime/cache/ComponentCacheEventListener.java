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
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.util.KeyConstants;

public final class ComponentCacheEventListener implements CacheEventListener {

	private static final long serialVersionUID = 6271280246677734153L;
	private Component component;

	public ComponentCacheEventListener(Component component) {
		this.component = component;
	}

	@Override
	public void onRemove(CacheEntry entry) {
		call(KeyConstants._onRemove, entry);
	}

	@Override
	public void onPut(CacheEntry entry) {
		call(KeyConstants._onPut, entry);
	}

	@Override
	public void onExpires(CacheEntry entry) {
		call(KeyConstants._onExpires, entry);
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