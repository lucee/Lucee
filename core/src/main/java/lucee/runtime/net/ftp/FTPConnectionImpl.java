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




/**
 *  
 */
public final class FTPConnectionImpl implements FTPConnection {
    
    private String name;
    private String server;
    private String username;
    private String password;
    private int port;
    private int timeout;
    private short transferMode;
    private boolean passive;
    private String proxyserver;
    private int proxyport;
    private String proxyuser;
    private String proxypassword;

    /**
     * @param name
     * @param server
     * @param username
     * @param password
     * @param port
     * @param timeout
     * @param transferMode
     * @param passive
     * @param proxyserver
     */
    public FTPConnectionImpl(String name, String server, String username, String password,int port, int timeout, short transferMode,boolean passive, 
    		String proxyserver,int proxyport,String proxyuser, String proxypassword) {
        this.name=name==null?null:name.toLowerCase().trim();
        this.server=server;
        this.username=username;
        this.password=password;
        this.port=port;
        this.timeout=timeout;
        this.transferMode=transferMode;
        this.passive=passive;
        
        this.proxyserver=proxyserver;
        this.proxyport=proxyport;
        this.proxyuser=proxyuser;
        this.proxypassword=proxypassword;
    }
    @Override
    public String getName() {
        return name;
    }
    @Override
    public String getPassword() {
        return password;
    }
    @Override
    public String getServer() {
        return server;
    }
    @Override
    public String getUsername() {
        return username;
    }
    @Override
    public boolean hasLoginData() {
        return server!=null;// && username!=null && password!=null;
    }
    @Override
    public boolean hasName() {
        return name!=null;
    }
    @Override
    public int getPort() {
        return port;
    }
    @Override
    public int getTimeout() {
        return timeout;
    }
    @Override
    public short getTransferMode() {
        return transferMode;
    }
    

	public void setTransferMode(short transferMode) {
		this.transferMode=transferMode;
	}
    
    @Override
    public boolean isPassive() {
        return passive;
    }
    @Override
    public boolean loginEquals(FTPConnection conn) {
        return 
        	server.equalsIgnoreCase(conn.getServer()) && 
        	username.equals(conn.getUsername()) && 
        	password.equals(conn.getPassword());
    }
    
	@Override
	public String getProxyPassword() {
		return proxypassword;
	}
	
	@Override
	public int getProxyPort() {
		return proxyport;
	}
	
	@Override
	public String getProxyServer() {
		return proxyserver;
	}
	
	@Override
	public String getProxyUser() {
		return proxyuser;
	}
	
	public boolean equal(Object o){
		if(!(o instanceof FTPConnection)) return false;
		FTPConnection other=(FTPConnection) o;
		
		if(neq(other.getPassword(),getPassword())) return false;
		if(neq(other.getProxyPassword(),getProxyPassword())) return false;
		if(neq(other.getProxyServer(),getProxyServer())) return false;
		if(neq(other.getProxyUser(),getProxyUser())) return false;
		if(neq(other.getServer(),getServer())) return false;
		if(neq(other.getUsername(),getUsername())) return false;
		
		if(other.getPort()!=getPort()) return false;
		if(other.getProxyPort()!=getProxyPort()) return false;
		//if(other.getTimeout()!=getTimeout()) return false;
		if(other.getTransferMode()!=getTransferMode()) return false;
		
		return true;
	}
	
	private boolean neq(String left, String right) {
		if(left==null) left="";
		if(right==null) right="";
		
		return !left.equals(right);
	}
	
}