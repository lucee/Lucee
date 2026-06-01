/**
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.
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

package lucee.runtime.functions.other;

import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigUtil;
import lucee.runtime.config.Identification;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

/**
 * Implements the CFML Function createGuid
 */
public final class GetLuceeId implements Function {

	private static final long serialVersionUID = 105306626462365773L;

	public static Struct call(PageContext pc) throws PageException {
		Struct sct = new StructImpl();
		Struct web = new StructImpl();
		Struct server = new StructImpl();

		Identification id = ConfigUtil.getConfigServerImpl(pc.getConfig()).getIdentification();

		// Web FUTURE remove
		web.set(KeyConstants._securityKey, id.getSecurityKey());
		web.set(KeyConstants._id, id.getId());
		web.set(KeyConstants._apiKey, id.getApiKey());
		sct.set(KeyConstants._web, web);

		// Server FUTURE remove
		server.set(KeyConstants._securityKey, id.getSecurityKey());
		server.set(KeyConstants._id, id.getId());
		server.set(KeyConstants._apiKey, id.getApiKey());
		sct.set(KeyConstants._server, server);

		sct.set(KeyConstants._securityKey, id.getSecurityKey());
		sct.set(KeyConstants._id, id.getId());
		sct.set(KeyConstants._apiKey, id.getApiKey());

		sct.set(KeyConstants._request, Caster.toString(pc.getId()));

		return sct;
	}

}