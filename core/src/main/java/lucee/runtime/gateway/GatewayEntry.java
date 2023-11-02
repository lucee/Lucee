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
package lucee.runtime.gateway;

import org.osgi.framework.BundleException;

import lucee.commons.lang.ClassException;
import lucee.runtime.config.Config;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;

public interface GatewayEntry {

	public static int STARTUP_MODE_AUTOMATIC = 1;
	public static int STARTUP_MODE_MANUAL = 2;
	public static int STARTUP_MODE_DISABLED = 4;

	/**
	 * @return the gateway
	 * @throws ClassException
	 * @throws PageException
	 * @throws BundleException
	 */
	public void createGateway(Config config) throws ClassException, PageException, BundleException;

	public Gateway getGateway();

	/**
	 * @return the id
	 */
	public abstract String getId();

	// public abstract Class getClazz();

	/**
	 * @return the custom
	 */
	public abstract Struct getCustom();

	/**
	 * @return the readOnly
	 */
	public abstract boolean isReadOnly();

	/**
	 * @return the cfcPath
	 */
	public String getListenerCfcPath();

	public String getCfcPath();

	/**
	 * @return the startupMode
	 */
	public int getStartupMode();

	public ClassDefinition getClassDefinition();

}