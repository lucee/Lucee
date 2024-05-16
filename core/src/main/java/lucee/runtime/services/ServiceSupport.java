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
package lucee.runtime.services;

import java.util.HashMap;
import java.util.Map;

import coldfusion.server.Service;
import coldfusion.server.ServiceException;
import coldfusion.server.ServiceMetaData;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.SecurityException;

public class ServiceSupport implements Service {

	@Override
	public void start() throws ServiceException {
	}

	@Override
	public void stop() throws ServiceException {
	}

	@Override
	public void restart() throws ServiceException {
	}

	@Override
	public int getStatus() {
		return STARTED;
	}

	@Override
	public ServiceMetaData getMetaData() {
		return new EmptyServiceMetaData();
	}

	@Override
	public Object getProperty(String key) {
		return null;
	}

	@Override
	public void setProperty(String key, Object value) {
	}

	@Override
	public Map getResourceBundle() {
		return new HashMap();
	}

	protected void checkWriteAccess() throws SecurityException {
		ConfigWebUtil.checkGeneralWriteAccess(config(), null);
	}

	protected void checkReadAccess() throws SecurityException {
		ConfigWebUtil.checkGeneralReadAccess(config(), null);
	}

	protected ConfigPro config() {
		return (ConfigPro) ThreadLocalPageContext.getConfig();
	}

	protected PageContext pc() {
		return ThreadLocalPageContext.get();
	}
}