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
package lucee.runtime.net.ftp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ApplicationException;

/**
 * Pool of FTP Client
 */
public final class FTPPoolImpl {

	Map<String, FTPWrap> wraps = new HashMap<String, FTPWrap>();

	public AFTPClient get(FTPConnection conn) throws IOException, ApplicationException {
		AFTPClient client = _get(conn).getClient();
		if (client == null) throw new ApplicationException("can't connect to server [" + conn.getServer() + "]");

		FTPWrap.setConnectionSettings(client, conn);

		return client;
	}

	/**
	 * returns a client from given connection
	 * 
	 * @param conn
	 * @return
	 * @return matching wrap
	 * @throws IOException
	 * @throws ApplicationException
	 */
	protected FTPWrap _get(FTPConnection conn) throws IOException, ApplicationException {
		FTPWrap wrap = null;

		if (!conn.hasLoginData()) {
			if (StringUtil.isEmpty(conn.getName())) {
				throw new ApplicationException("can't connect ftp server, missing connection definition");
			}

			wrap = wraps.get(conn.getName());
			if (wrap == null) {
				throw new ApplicationException("can't connect ftp server, missing connection [" + conn.getName() + "]");
			}
			else if (!wrap.getClient().isConnected() || wrap.getConnection().getTransferMode() != conn.getTransferMode()) {
				wrap.reConnect(conn.getTransferMode());
			}
			return wrap;
		}
		String name = conn.hasName() ? conn.getName() : "__noname__";

		wrap = wraps.get(name);
		if (wrap != null) {
			if (conn.loginEquals(wrap.getConnection())) {
				return _get(new FTPConnectionImpl(name, null, null, null, conn.getPort(), conn.getTimeout(), conn.getTransferMode(), conn.isPassive(), conn.getProxyServer(),
						conn.getProxyPort(), conn.getProxyUser(), conn.getProxyPassword(), conn.getFingerprint(), conn.getStopOnError(), conn.secure()));
			}
			disconnect(wrap.getClient());
		}

		wrap = new FTPWrap(conn);
		wraps.put(name, wrap);

		return wrap;
	}

	/**
	 * disconnect a client
	 * 
	 * @param client
	 */
	private void disconnect(AFTPClient client) {
		try {
			if (client != null && client.isConnected()) {
				client.quit();
				client.disconnect();
			}
		}
		catch (IOException ioe) {
		}
	}

	public AFTPClient remove(FTPConnection conn) {
		return remove(conn.getName());
	}

	public AFTPClient remove(String name) {
		FTPWrap wrap = wraps.remove(name);
		if (wrap == null) return null;

		AFTPClient client = wrap.getClient();
		disconnect(client);
		return client;
	}

	public void clear() {
		if (!wraps.isEmpty()) {
			Iterator<Entry<String, FTPWrap>> it = wraps.entrySet().iterator();
			while (it.hasNext()) {
				try {
					Entry<String, FTPWrap> entry = it.next();
					FTPWrap wrap = entry.getValue();
					if (wrap != null && wrap.getClient().isConnected()) wrap.getClient().disconnect();
				}
				catch (IOException e) {
				}
			}
			wraps.clear();
		}
	}
}