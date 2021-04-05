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
package lucee.runtime.spooler;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.config.RemoteClient;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.rpc.client.WSClient;
import lucee.runtime.op.Caster;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public abstract class SpoolerTaskWS extends SpoolerTaskSupport {

	private RemoteClient client;

	public SpoolerTaskWS(ExecutionPlan[] plans, RemoteClient client) {
		super(plans);
		this.client = client;
	}

	@Override
	public final Object execute(Config config) throws PageException {
		try {
			WSClient rpc = ((ConfigWebPro) ThreadLocalPageContext.getConfig(config)).getWSHandler().getWSClient(client.getUrl(), client.getServerUsername(),
					client.getServerPassword(), client.getProxyData());

			return rpc.callWithNamedValues(config, KeyImpl.init(getMethodName()), getArguments());
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}
	}

	@Override
	public String subject() {
		return client.getLabel();
	}

	@Override
	public Struct detail() {
		Struct sct = new StructImpl();
		sct.setEL("label", client.getLabel());
		sct.setEL("url", client.getUrl());

		return sct;
	}

	protected abstract String getMethodName();

	protected abstract Struct getArguments();
}