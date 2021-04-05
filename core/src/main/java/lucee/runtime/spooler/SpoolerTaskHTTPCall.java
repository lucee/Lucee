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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import lucee.commons.lang.StringUtil;
import lucee.commons.net.HTTPUtil;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.httpclient.HTTPEngine4Impl;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.config.Constants;
import lucee.runtime.config.RemoteClient;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.JSONExpressionInterpreter;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public abstract class SpoolerTaskHTTPCall extends SpoolerTaskSupport {

	private static final long serialVersionUID = -1994776413696459993L;

	private RemoteClient client;

	public SpoolerTaskHTTPCall(ExecutionPlan[] plans, RemoteClient client) {
		super(plans);
		this.client = client;
	}

	/**
	 * @return
	 * @see lucee.runtime.spooler.SpoolerTask#execute()
	 */
	@Override
	public final Object execute(Config config) throws PageException {
		return execute(client, config, getMethodName(), getArguments());
	}

	public static final Object execute(RemoteClient client, Config config, String methodName, Struct args) throws PageException {
		// return rpc.callWithNamedValues(config, getMethodName(), getArguments());
		PageContext pc = ThreadLocalPageContext.get();

		// remove wsdl if necessary
		String url = client.getUrl();
		if (StringUtil.endsWithIgnoreCase(url, "?wsdl")) url = url.substring(0, url.length() - 5);

		// Params
		Map<String, String> params = new HashMap<String, String>();
		params.put("method", methodName);
		params.put("returnFormat", "json");
		try {
			Charset cs = pc.getWebCharset();
			params.put("argumentCollection", new JSONConverter(true, cs).serialize(pc, args, SerializationSettings.SERIALIZE_AS_ROW));

			HTTPResponse res = HTTPEngine4Impl.post(HTTPUtil.toURL(url, HTTPUtil.ENCODED_AUTO), client.getServerUsername(), client.getServerPassword(), -1L, true,
					pc.getWebCharset().name(), Constants.NAME + " Remote Invocation", client.getProxyData(), null, params);

			return new JSONExpressionInterpreter().interpret(pc, res.getContentAsString());

		}
		catch (IOException ioe) {
			throw Caster.toPageException(ioe);
		}
		catch (ConverterException ce) {
			throw Caster.toPageException(ce);
		}

	}

	/**
	 * @see lucee.runtime.spooler.SpoolerTask#subject()
	 */
	@Override
	public String subject() {
		return client.getLabel();
	}

	/**
	 * @see lucee.runtime.spooler.SpoolerTask#detail()
	 */
	@Override
	public Struct detail() {
		Struct sct = new StructImpl();
		sct.setEL(KeyConstants._label, client.getLabel());
		sct.setEL(KeyConstants._url, client.getUrl());

		return sct;
	}

	protected abstract String getMethodName();

	protected abstract Struct getArguments();
}