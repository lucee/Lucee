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

import java.io.Serializable;

import lucee.runtime.engine.ThreadLocalPageContext;

public class IdentificationWebImpl extends IdentificationImpl implements IdentificationWeb, Serializable {

	private transient ConfigWebPro cw;

	public IdentificationWebImpl(ConfigWebPro cw, String securityKey, String apiKey) {
		super(cw, securityKey, apiKey);
		this.cw = cw;
	}

	@Override
	public IdentificationServer getServerIdentification() {
		return ((ConfigWebImpl) ThreadLocalPageContext.getConfig(cw)).getConfigServerImpl().getIdentification();
	}

	@Override
	public String toQueryString() {
		StringBuilder qs = new StringBuilder();
		append(qs, "webApiKey", getApiKey());
		append(qs, "webId", getId());
		append(qs, "webSecurityKey", getSecurityKey());

		IdentificationServer sid = getServerIdentification();
		append(qs, "serverApiKey", sid.getApiKey());
		append(qs, "serverId", sid.getId());
		append(qs, "serverSecurityKey", sid.getSecurityKey());

		return qs.toString();
	}
}