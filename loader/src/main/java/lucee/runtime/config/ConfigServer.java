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
package lucee.runtime.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import lucee.loader.engine.CFMLEngine;
import lucee.runtime.CFMLFactory;
import lucee.runtime.security.SecurityManager;

/**
 * Config for the server
 */
public interface ConfigServer extends Config {

	/**
	 * @return returns all config webs
	 */
	public abstract ConfigWeb[] getConfigWebs();

	/**
	 * @param realpath
	 * @return returns config web matching given realpath
	 */
	public abstract ConfigWeb getConfigWeb(String realpath);

	/**
	 * @return Returns the contextes.
	 */
	public abstract Map<String, CFMLFactory> getJSPFactoriesAsMap();

	/**
	 * @param id
	 * @return returns SecurityManager matching config
	 */
	public abstract SecurityManager getSecurityManager(String id);

	/**
	 * is there an individual security manager for given id
	 * 
	 * @param id for the security manager
	 * @return returns SecurityManager matching config
	 */
	public abstract boolean hasIndividualSecurityManager(String id);

	/**
	 * @return Returns the securityManager.
	 */
	public abstract SecurityManager getDefaultSecurityManager();

	/**
	 * @param updateType The updateType to set.
	 */
	public abstract void setUpdateType(String updateType);

	/**
	 * @param updateLocation The updateLocation to set.
	 */
	public abstract void setUpdateLocation(URL updateLocation);

	/**
	 * @param strUpdateLocation The updateLocation to set.
	 * @throws MalformedURLException
	 */
	public abstract void setUpdateLocation(String strUpdateLocation) throws MalformedURLException;

	/**
	 * @param strUpdateLocation The updateLocation to set.
	 * @param defaultValue
	 */
	public abstract void setUpdateLocation(String strUpdateLocation, URL defaultValue);

	/**
	 * @return the configListener
	 */
	public ConfigListener getConfigListener();

	/**
	 * @param configListener the configListener to set
	 */
	public void setConfigListener(ConfigListener configListener);

	@Override
	public RemoteClient[] getRemoteClients();

	/**
	 * @deprecated use instead getEngine
	 * @return
	 */
	@Deprecated
	public abstract CFMLEngine getCFMLEngine();

	public abstract CFMLEngine getEngine();

	@Override
	public IdentificationServer getIdentification();

}