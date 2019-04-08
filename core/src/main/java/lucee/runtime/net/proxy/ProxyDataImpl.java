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
package lucee.runtime.net.proxy;

import java.io.Serializable;

import lucee.commons.lang.StringUtil;

public class ProxyDataImpl implements ProxyData, Serializable {

    public static final ProxyData NO_PROXY = new ProxyDataImpl();

    private String server;
    private int port = -1;
    private String username;
    private String password;

    public ProxyDataImpl(String server, int port, String username, String password) {
	if (!StringUtil.isEmpty(server, true)) this.server = server;
	if (port > 0) this.port = port;
	if (!StringUtil.isEmpty(username, true)) this.username = username;
	if (!StringUtil.isEmpty(password, true)) this.password = password;
    }

    public ProxyDataImpl() {}

    @Override
    public void release() {
	server = null;
	port = -1;
	username = null;
	password = null;
    }

    /**
     * @return the password
     */
    @Override
    public String getPassword() {
	return password;
    }

    /**
     * @param password the password to set
     */
    @Override
    public void setPassword(String password) {
	this.password = password;
    }

    /**
     * @return the port
     */
    @Override
    public int getPort() {
	return port;
    }

    /**
     * @param port the port to set
     */
    @Override
    public void setPort(int port) {
	this.port = port;
    }

    /**
     * @return the server
     */
    @Override
    public String getServer() {
	return server;
    }

    /**
     * @param server the server to set
     */
    @Override
    public void setServer(String server) {
	this.server = server;
    }

    /**
     * @return the username
     */
    @Override
    public String getUsername() {
	return username;
    }

    /**
     * @param username the username to set
     */
    @Override
    public void setUsername(String username) {
	this.username = username;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == this) return true;
	if (!(obj instanceof ProxyData)) return false;

	ProxyData other = (ProxyData) obj;

	return _eq(other.getServer(), server) && _eq(other.getUsername(), username) && _eq(other.getPassword(), password) && other.getPort() == port;

    }

    private boolean _eq(String left, String right) {
	if (left == null) return right == null;
	return left.equals(right);
    }

    public static boolean isValid(ProxyData pd) {
	if (pd == null || pd.equals(NO_PROXY)) return false;
	return true;
    }

    public static boolean hasCredentials(ProxyData data) {
	return StringUtil.isEmpty(data.getUsername(), true);
    }

    public static ProxyData getInstance(String proxyserver, int proxyport, String proxyuser, String proxypassword) {
	if (StringUtil.isEmpty(proxyserver, true)) return null;
	return new ProxyDataImpl(proxyserver, proxyport, proxyuser, proxypassword);
    }

    @Override
    public String toString() {
	return "server:" + server + ";port:" + port + ";user:" + username + ";pass:" + password;
    }
}