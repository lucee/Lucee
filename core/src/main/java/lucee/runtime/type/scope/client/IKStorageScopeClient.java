/**
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
package lucee.runtime.type.scope.client;

import java.util.Map;

import lucee.runtime.PageContext;
import lucee.runtime.type.Collection;
import lucee.runtime.type.scope.Client;
import lucee.runtime.type.scope.storage.IKHandler;
import lucee.runtime.type.scope.storage.IKStorageScopeItem;
import lucee.runtime.type.scope.storage.IKStorageScopeSupport;

public final class IKStorageScopeClient extends IKStorageScopeSupport implements Client {

	private static final long serialVersionUID = -875719423763891692L;

	public IKStorageScopeClient(PageContext pc, IKHandler handler, String appName, String name, Map<Collection.Key, IKStorageScopeItem> data, long lastStored, long timeSpan) {
		super(pc, handler, appName, name, "client", SCOPE_CLIENT, data, lastStored, timeSpan);
	}

	/**
	 * Constructor of the class, clone existing
	 * 
	 * @param other
	 */
	private IKStorageScopeClient(IKStorageScopeSupport other, boolean deepCopy) {
		super(other, deepCopy);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return new IKStorageScopeClient(this, deepCopy);
	}
}