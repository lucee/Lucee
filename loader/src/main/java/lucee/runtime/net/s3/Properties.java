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
package lucee.runtime.net.s3;

import lucee.runtime.type.Struct;

public interface Properties {

	public Struct toStruct();

	/**
	 * @return the accessKeyId
	 */
	public String getAccessKeyId();

	/**
	 * @return the host
	 */
	public String getHost();

	/**
	 * @param host the host to set
	 */
	// public void setHost(String host);

	/**
	 * @return the defaultLocation
	 */
	public String getDefaultLocation();

	/**
	 * @param defaultLocation the defaultLocation to set
	 */
	// public void setDefaultLocation(String defaultLocation);

	/**
	 * @param accessKeyId the accessKeyId to set
	 */
	// public void setAccessKeyId(String accessKeyId);

	/**
	 * @return the secretAccessKey
	 */
	public String getSecretAccessKey();

	/**
	 * @param secretAccessKey the secretAccessKey to set
	 */
	// public void setSecretAccessKey(String secretAccessKey);

}