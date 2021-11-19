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

public class IdentificationServerImpl extends IdentificationImpl implements IdentificationServer {

	public IdentificationServerImpl(ConfigServerImpl c, String securityKey, String apiKey) {
		super(c, securityKey, apiKey);
	}

	@Override
	public String toQueryString() {
		StringBuilder qs = new StringBuilder();
		append(qs, "serverApiKey", getApiKey());
		append(qs, "serverId", getId());
		append(qs, "serverSecurityKey", getSecurityKey());

		return qs.toString();
	}

}