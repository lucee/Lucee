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
package lucee.runtime.config;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.crypt.CFMXCompat;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.functions.other.Encrypt;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.rpc.client.WSClient;
import lucee.runtime.op.Caster;
import lucee.runtime.spooler.remote.RemoteClientTask;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

public class RemoteClientImpl implements RemoteClient {

	private String url;
	private String serverUsername;
	private String serverPassword;
	private ProxyData proxyData;
	private String type;
	private String adminPassword;
	private String securityKey;
	private String label;
	private String usage;
	private String id;

	public RemoteClientImpl(String label, String type, String url, String serverUsername, String serverPassword, String adminPassword, ProxyData proxyData, String securityKey,
			String usage) {
		this.label = label;
		this.url = url;
		this.serverUsername = serverUsername;
		this.serverPassword = serverPassword;
		this.proxyData = proxyData;
		this.type = type;
		this.adminPassword = adminPassword;
		this.securityKey = securityKey;
		this.usage = usage;
	}

	/**
	 * @return the url
	 */
	@Override
	public String getUrl() {
		return url;
	}

	/**
	 * @return the serverUsername
	 */
	@Override
	public String getServerUsername() {
		return serverUsername;
	}

	/**
	 * @return the serverPassword
	 */
	@Override
	public String getServerPassword() {
		return serverPassword;
	}

	/**
	 * @return the proxyData
	 */
	@Override
	public ProxyData getProxyData() {
		return proxyData;
	}

	/**
	 * @return the type
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * @return the adminPassword
	 */
	@Override
	public String getAdminPassword() {
		return adminPassword;
	}

	/**
	 * @return the securityKey
	 */
	@Override
	public String getSecurityKey() {
		return securityKey;
	}

	@Override
	public String getAdminPasswordEncrypted() {
		try {
			return Encrypt.invoke(getAdminPassword(), getSecurityKey(), CFMXCompat.ALGORITHM_NAME, "uu", null, 0, true);
		}
		catch (PageException e) {
			return null;
		}
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getUsage() {
		return usage;
	}

	@Override
	public boolean hasUsage(String usage) {
		return ListUtil.listFindNoCaseIgnoreEmpty(this.usage, usage, ',') != -1;
	}

	@Override
	public String getId(Config config) {

		if (id != null) return id;

		Struct attrColl = new StructImpl();
		attrColl.setEL(KeyConstants._action, "getToken");

		Struct args = new StructImpl();
		args.setEL(KeyConstants._type, getType());
		args.setEL(RemoteClientTask.PASSWORD, getAdminPasswordEncrypted());
		args.setEL(RemoteClientTask.CALLER_ID, "undefined");
		args.setEL(RemoteClientTask.ATTRIBUTE_COLLECTION, attrColl);

		try {
			WSClient rpc = ((ConfigWebPro) ThreadLocalPageContext.getConfig(config)).getWSHandler().getWSClient(getUrl(), getServerUsername(), getServerPassword(), getProxyData());

			Object result = rpc.callWithNamedValues(config, KeyConstants._invoke, args);
			return id = IdentificationImpl.createId(securityKey, Caster.toString(result, null), false, null);

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return null;
		}
	}

}