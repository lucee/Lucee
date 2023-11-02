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

import java.io.Serializable;

import lucee.runtime.net.proxy.ProxyData;

public interface RemoteClient extends Serializable {

	/**
	 * @return the url
	 */
	public String getUrl();

	/**
	 * @return the serverUsername
	 */
	public String getServerUsername();

	/**
	 * @return the serverPassword
	 */
	public String getServerPassword();

	/**
	 * @return the proxyData
	 */
	public ProxyData getProxyData();

	/**
	 * @return the type
	 */
	public String getType();

	/**
	 * @return the adminPassword
	 */
	public String getAdminPassword();

	/**
	 * @return the securityKey
	 */
	public String getSecurityKey();

	public String getAdminPasswordEncrypted();

	public String getLabel();

	public String getUsage();

	public boolean hasUsage(String usage);

	public String getId(Config config);

	// TODO doc
}