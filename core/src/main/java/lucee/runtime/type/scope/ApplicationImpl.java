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

import java.util.Map;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.functions.system.GetApplicationSettings;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;

/**
 * Session Scope
 */
public final class ApplicationImpl extends ScopeSupport implements Application, SharedScope {

	private static final long serialVersionUID = 700830188207594563L;

	private static final Collection.Key APPLICATION_NAME = KeyImpl.getInstance("applicationname");
	private long lastAccess;
	private long timeSpan;
	private long created;

	private Component component;

	/**
	 * default constructor of the session scope
	 */
	public ApplicationImpl() {
		super("application", SCOPE_APPLICATION, Struct.TYPE_LINKED);
		created = System.currentTimeMillis();
	}

	@Override
	public long getLastAccess() {
		return lastAccess;
	}

	@Override
	public long getTimeSpan() {
		return timeSpan;
	}

	@Override
	public void touchBeforeRequest(PageContext pc) {
		ApplicationContext appContext = pc.getApplicationContext();
		setEL(APPLICATION_NAME, appContext.getName());
		timeSpan = appContext.getApplicationTimeout().getMillis();
		lastAccess = System.currentTimeMillis();
	}

	@Override
	public void touchAfterRequest(PageContext pc) {
		// do nothing
	}

	@Override
	public boolean isExpired() {
		return (lastAccess + timeSpan) < System.currentTimeMillis();
	}

	/**
	 * @param lastAccess the lastAccess to set
	 */
	public void setLastAccess(long lastAccess) {
		this.lastAccess = lastAccess;
	}

	@Override
	public void touch() {
		lastAccess = System.currentTimeMillis();
	}

	/**
	 * undocumented Feature in ACF
	 * 
	 * @return
	 * @throws PageException
	 */
	public Map getApplicationSettings() throws PageException {
		return GetApplicationSettings.call(ThreadLocalPageContext.get());
	}

	@Override
	public long getCreated() {
		return created;
	}

	public void setComponent(Component component) {
		this.component = component;
	}

	public Component getComponent() {
		return component;
	}
}