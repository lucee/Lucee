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
package lucee.runtime.net.proxy;

import java.util.Properties;

import lucee.commons.lang.StringUtil;
import lucee.commons.lang.lock.KeyLock;
import lucee.commons.lang.lock.KeyLockListener;

public final class Proxy {

	// private static Map map=new HashTable();
	private static KeyLock kl = new KeyLock();

	public static void start(ProxyData proxyData) {
		start(proxyData.getServer(), proxyData.getPort(), proxyData.getUsername(), proxyData.getPassword());
	}

	public static void start(String server, int port, String user, String password) {
		String key = StringUtil.toString(server, "") + ":" + StringUtil.toString(port + "", "") + ":" + StringUtil.toString(user, "") + ":" + StringUtil.toString(password, "");
		kl.setListener(new ProxyListener(server, port, user, password));
		kl.start(key);
	}

	public static void end() {
		kl.end();
	}

}

class ProxyListener implements KeyLockListener {

	private String server;
	private int port;
	private String user;
	private String password;

	public ProxyListener(String server, int port, String user, String password) {
		this.server = server;
		this.port = port;
		this.user = user;
		this.password = password;
	}

	@Override
	public void onStart(String key, boolean isFirst) {
		// print.ln(" start:"+key+" _ "+isFirst);
		if (!isFirst) return;

		Properties props = System.getProperties();
		if (!StringUtil.isEmpty(server)) {
			// Server
			props.setProperty("socksProxyHost", server);
			props.setProperty("http.proxyHost", server);
			props.setProperty("https.proxyHost", server);
			props.setProperty("ftp.proxyHost", server);
			props.setProperty("smtp.proxyHost", server);

			// Port
			if (port > 0) {
				String strPort = String.valueOf(port);
				props.setProperty("socksProxyPort", strPort);
				props.setProperty("http.proxyPort", strPort);
				props.setProperty("https.proxyPort", strPort);
				props.setProperty("ftp.proxyPort", strPort);
				props.setProperty("smtp.proxyPort", strPort);
			}
			else removePort(props);

			if (!StringUtil.isEmpty(user)) {
				props.setProperty("socksProxyUser", user);
				props.setProperty("java.net.socks.username", user);
				props.setProperty("http.proxyUser", user);
				props.setProperty("https.proxyUser", user);
				props.setProperty("ftp.proxyUser", user);
				props.setProperty("smtp.proxyUser", user);

				if (password == null) password = "";
				props.setProperty("socksProxyPassword", user);
				props.setProperty("java.net.socks.password", user);
				props.setProperty("http.proxyPassword", user);
				props.setProperty("https.proxyPassword", user);
				props.setProperty("ftp.proxyPassword", user);
				props.setProperty("smtp.proxyPassword", user);
			}
			else removeUserPass(props);
		}
		else {
			removeAll(props);
		}
	}

	@Override
	public void onEnd(String key, boolean isLast) {
		// print.ln(" end:"+key+key+" _ "+isLast);
		if (!isLast) return;
		removeAll(System.getProperties());
	}

	private void removeAll(Properties props) {
		removeHost(props);
		removePort(props);
		removeUserPass(props);

	}

	private void removeHost(Properties props) {
		remove(props, "socksProxyHost");

		remove(props, "http.proxyHost");
		remove(props, "https.proxyHost");
		remove(props, "ftp.proxyHost");
		remove(props, "smtp.proxyHost");

	}

	private void removePort(Properties props) {
		remove(props, "socksProxyPort");
		remove(props, "http.proxyPort");
		remove(props, "https.proxyPort");
		remove(props, "ftp.proxyPort");
		remove(props, "smtp.proxyPort");
	}

	private void removeUserPass(Properties props) {
		remove(props, "socksProxyUser");
		remove(props, "socksProxyPassword");

		remove(props, "java.net.socks.username");
		remove(props, "java.net.socks.password");

		remove(props, "http.proxyUser");
		remove(props, "http.proxyPassword");

		remove(props, "https.proxyUser");
		remove(props, "https.proxyPassword");

		remove(props, "ftp.proxyUser");
		remove(props, "ftp.proxyPassword");

		remove(props, "smtp.proxyUser");
		remove(props, "smtp.proxyPassword");

	}

	private static void remove(Properties props, String key) {
		if (props.containsKey(key)) props.remove(key);
	}

}

/*
 * class ProxyThread extends Thread { private String s; private int po; private int id; private
 * String u; private String p;
 * 
 * public ProxyThread(int id,String s, int po, String u, String p) { this.s=s; this.id=id;
 * this.po=po; this.u=u; this.p=p; } public void run() { try { _run(); } catch (Exception e) {
 * 
 * } } public void _run() throws Exception {
 * //print.ln("start("+Thread.currentThread().getName()+"):"+s+":"+po+":"+u+":"+p);
 * Proxy.start(id,s, po, u, p); sleep(1000); Proxy.end(id); } }
 */