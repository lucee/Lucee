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
package lucee.runtime.type.scope.session;

import java.util.Map;

import lucee.runtime.PageContext;
import lucee.runtime.type.Collection;
import lucee.runtime.type.scope.Session;
import lucee.runtime.type.scope.storage.IKHandler;
import lucee.runtime.type.scope.storage.IKStorageScopeItem;
import lucee.runtime.type.scope.storage.IKStorageScopeSupport;
import lucee.runtime.type.util.StructUtil;

public final class IKStorageScopeSession extends IKStorageScopeSupport implements Session {

	private static final long serialVersionUID = -875719423763891692L;

	public IKStorageScopeSession(PageContext pc, IKHandler handler, String appName, String name, Map<Collection.Key, IKStorageScopeItem> data, long lastModified, long timeSpan) {
		super(pc, handler, appName, name, "session", SCOPE_SESSION, data, lastModified, timeSpan);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return StructUtil.duplicate(this, deepCopy);
	}
}