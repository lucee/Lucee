/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.config;

import java.io.IOException;
import java.io.Serializable;

import lucee.commons.digest.Hash;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Md5;
import lucee.loader.util.Util;

public abstract class IdentificationImpl implements Identification, Serializable {

	private final String apiKey;
	private String id;
	private final String securityKey;
	private final String securityToken;

	public IdentificationImpl(ConfigPro c, String securityKey, String apiKey) {
		this.apiKey = apiKey;
		this.securityKey = securityKey;
		this.securityToken = createSecurityToken(c.getConfigDir());
	}

	@Override
	public String getApiKey() {
		return apiKey;
	}

	@Override
	public String getId() {
		// this is here for performance reasons
		if (id == null) id = createId(securityKey, securityToken, false, securityKey);
		return id;
	}

	@Override
	public String getSecurityKey() {
		return securityKey;
	}

	@Override
	public String getSecurityToken() {
		return securityToken;
	}

	static String createId(String key, String token, boolean addMacAddress, String defaultValue) {

		try {
			if (addMacAddress) {// because this was new we could swutch to a new ecryption // FUTURE cold we get rid of the old one?
				return Hash.sha256(key + ";" + token + ":" + SystemUtil.getMacAddress(""));
			}
			return Md5.getDigestAsString(key + token);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}
	}

	private static String createSecurityToken(Resource dir) {
		try {
			return Md5.getDigestAsString(dir.getAbsolutePath());
		}
		catch (IOException e) {
			return null;
		}

	}

	protected static void append(StringBuilder qs, String name, String value) {
		if (Util.isEmpty(value, true)) return;

		if (qs.length() > 0) qs.append('&');
		else qs.append('?');
		qs.append(name).append('=').append(value); // TODO encoding
	}

}