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
package lucee.runtime.listener;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.scope.Application;
import lucee.runtime.type.scope.Session;

public abstract class AppListenerSupport implements ApplicationListener {

	@Override
	public boolean hasOnApplicationStart() {
		return false;
	}

	@Override
	public boolean hasOnSessionStart(PageContext pc) {
		return false;
	}

	@Override
	public void onServerStart() throws PageException {
	}

	@Override
	public void onServerEnd() throws PageException {
	}

	@Override
	public void onTimeout(PageContext pc) {
	}

	// FUTURE add to interface
	public abstract void onSessionStart(PageContext pc, Session session) throws PageException;

	public abstract boolean onApplicationStart(PageContext pc, Application application) throws PageException;
}