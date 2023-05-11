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
package lucee.commons.io.res.type.http;

import java.io.IOException;
import java.util.Map;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourceProviderPro;
import lucee.commons.io.res.Resources;
import lucee.commons.io.res.util.ResourceLockImpl;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.op.Caster;

public class HTTPResourceProvider implements ResourceProviderPro {

	private int lockTimeout = 20000;
	private final ResourceLockImpl lock = new ResourceLockImpl(lockTimeout, false);
	private String scheme = "http";
	private int clientTimeout = 30000;
	private int socketTimeout = 20000;
	private Map arguments;

	@Override
	public String getScheme() {
		return scheme;
	}

	public String getProtocol() {
		return scheme;
	}

	public void setScheme(String scheme) {
		if (!StringUtil.isEmpty(scheme)) this.scheme = scheme;
	}

	@Override
	public ResourceProvider init(String scheme, Map arguments) {
		setScheme(scheme);

		if (arguments != null) {
			this.arguments = arguments;
			// client-timeout
			String strTimeout = (String) arguments.get("client-timeout");
			if (strTimeout != null) {
				clientTimeout = Caster.toIntValue(strTimeout, clientTimeout);
			}
			// socket-timeout
			strTimeout = (String) arguments.get("socket-timeout");
			if (strTimeout != null) {
				socketTimeout = Caster.toIntValue(strTimeout, socketTimeout);
			}
			// lock-timeout
			strTimeout = (String) arguments.get("lock-timeout");
			if (strTimeout != null) {
				lockTimeout = Caster.toIntValue(strTimeout, lockTimeout);
			}
		}
		lock.setLockTimeout(lockTimeout);
		return this;
	}

	@Override
	public Resource getResource(String path) {

		int indexQ = path.indexOf('?');
		if (indexQ != -1) {
			int indexS = path.lastIndexOf('/');
			while ((indexS = path.lastIndexOf('/')) > indexQ) {
				path = path.substring(0, indexS) + "%2F" + path.substring(indexS + 1);
			}
		}

		path = ResourceUtil.translatePath(ResourceUtil.removeScheme(scheme, path), false, false);

		return new HTTPResource(this, new HTTPConnectionData(path, getSocketTimeout()));
	}

	@Override
	public boolean isAttributesSupported() {
		return false;
	}

	@Override
	public boolean isCaseSensitive() {
		return false;
	}

	@Override
	public boolean isModeSupported() {
		return false;
	}

	@Override
	public void setResources(Resources resources) {
	}

	@Override
	public void lock(Resource res) throws IOException {
		lock.lock(res);
	}

	@Override
	public void unlock(Resource res) {
		lock.unlock(res);
	}

	@Override
	public void read(Resource res) throws IOException {
		lock.read(res);
	}

	/**
	 * @return the clientTimeout
	 */
	public int getClientTimeout() {
		return clientTimeout;
	}

	/**
	 * @return the lockTimeout
	 */
	public int getLockTimeout() {
		return lockTimeout;
	}

	/**
	 * @return the socketTimeout
	 */
	public int getSocketTimeout() {
		return socketTimeout;
	}

	@Override
	public Map getArguments() {
		return arguments;
	}

	@Override
	public char getSeparator() {
		return '/';
	}
}