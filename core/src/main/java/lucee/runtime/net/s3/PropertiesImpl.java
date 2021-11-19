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
package lucee.runtime.net.s3;

import lucee.commons.lang.StringUtil;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public final class PropertiesImpl implements Properties {
	private String accessKeyId;
	private String secretAccessKey;
	private String defaultLocation = null;
	private String host = "s3.amazonaws.com";
	private String acl;
	private long cache;

	@Override
	public Struct toStruct() {
		Struct sct = new StructImpl();

		sct.setEL("accessKeyId", accessKeyId);
		sct.setEL("awsSecretKey", secretAccessKey);
		sct.setEL("defaultLocation", defaultLocation);
		sct.setEL("host", host);
		if (!StringUtil.isEmpty(acl)) sct.setEL("acl", acl);

		return sct;
	}

	/**
	 * @return the accessKeyId
	 */
	@Override
	public String getAccessKeyId() {
		return accessKeyId;
	}

	// FUTURE add to interface
	public String getACL() {
		return acl;
	}

	// FUTURE add to interface
	public void setACL(String acl) {
		this.acl = acl;
	}

	/**
	 * @return the host
	 */
	@Override
	public String getHost() {
		return host;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the defaultLocation
	 */
	@Override
	public String getDefaultLocation() {
		return defaultLocation;
	}

	/**
	 * @param defaultLocation the defaultLocation to set
	 */
	public void setDefaultLocation(String defaultLocation) {
		this.defaultLocation = improveLocation(defaultLocation);
	}

	/**
	 * @param accessKeyId the accessKeyId to set
	 */
	public void setAccessKeyId(String accessKeyId) {
		this.accessKeyId = accessKeyId;
	}

	/**
	 * @return the secretAccessKey
	 */
	@Override
	public String getSecretAccessKey() {
		return secretAccessKey;
	}

	/**
	 * @param secretAccessKey the secretAccessKey to set
	 */
	public void setSecretAccessKey(String secretAccessKey) {
		this.secretAccessKey = secretAccessKey;
	}

	@Override
	public String toString() {
		return "accessKeyId:" + accessKeyId + ";defaultLocation:" + defaultLocation + ";host:" + host + ";secretAccessKey:" + secretAccessKey;
	}

	private static String improveLocation(String location) {
		if (location == null) return location;
		location = location.toLowerCase().trim();
		if ("usa".equals(location)) return "us";
		if ("u.s.".equals(location)) return "us";
		if ("u.s.a.".equals(location)) return "us";
		if ("united states of america".equals(location)) return "us";

		if ("europe.".equals(location)) return "eu";
		if ("euro.".equals(location)) return "eu";
		if ("e.u.".equals(location)) return "eu";

		if ("usa-west".equals(location)) return "us-west";

		return location;
	}

	public void setCache(long millis) {
		this.cache = millis;
	}

	public long getCache() { // FUTURE add to interface
		return this.cache;
	}
}