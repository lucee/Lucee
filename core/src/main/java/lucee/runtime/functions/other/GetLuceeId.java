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

package lucee.runtime.functions.other;

import lucee.runtime.PageContext;
import lucee.runtime.config.IdentificationServer;
import lucee.runtime.config.IdentificationWeb;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

/**
 * Implements the CFML Function createGuid
 */
public final class GetLuceeId implements Function {

	private static final long serialVersionUID = 105306626462365773L;

	private static final Collection.Key SECURITY_KEY = KeyImpl.getInstance("securityKey");
	private static final Collection.Key API_KEY = KeyImpl.getInstance("apiKey");

	public static Struct call(PageContext pc) throws PageException {
		Struct sct = new StructImpl();
		Struct web = new StructImpl();
		Struct server = new StructImpl();

		IdentificationWeb idw = pc.getConfig().getIdentification();
		IdentificationServer ids = idw.getServerIdentification();

		// Web
		web.set(SECURITY_KEY, idw.getSecurityKey());
		web.set(KeyConstants._id, idw.getId());
		web.set(API_KEY, idw.getApiKey());
		sct.set(KeyConstants._web, web);

		// Server
		server.set(SECURITY_KEY, ids.getSecurityKey());
		server.set(KeyConstants._id, ids.getId());
		server.set(API_KEY, ids.getApiKey());
		sct.set(KeyConstants._server, server);

		sct.set(KeyConstants._request, Caster.toString(pc.getId()));
		return sct;
	}

}