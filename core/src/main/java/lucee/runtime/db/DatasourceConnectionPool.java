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
package lucee.runtime.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.types.RefInteger;
import lucee.commons.lang.types.RefIntegerSync;
import lucee.runtime.config.Config;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ArrayUtil;

public class DatasourceConnectionPool {

	private ConcurrentHashMap<String,DCStack> dcs=new ConcurrentHashMap<String,DCStack>();
	private Map<String,RefInteger> counter=new ConcurrentHashMap<String,RefInteger>();
	
	// !!! do not change used in hibernate extension
	public DatasourceConnection getDatasourceConnection(Config config,DataSource datasource, String user, String pass) throws PageException {
		config=ThreadLocalPageContext.getConfig(config);
		
		if(StringUtil.isEmpty(user)) {
            user=datasource.getUsername();
            pass=datasource.getPassword();
        }
        if(pass==null)pass="";
		
		
		// get stack
		DCStack stack=getDCStack(datasource,user,pass);
		
		
		// max connection
		int max=datasource.getConnectionLimit();
		synchronized (stack) {
			while(max!=-1 && max<=_size(datasource,user,pass)) {
				try {
					stack.wait(10000L);
				} 
				catch (InterruptedException e) {
					throw Caster.toPageException(e);
				}
			}

			while(!stack.isEmpty()) {
				DatasourceConnectionImpl dc=(DatasourceConnectionImpl) stack.get();
				if(dc!=null && isValid(dc,Boolean.TRUE)){
					_inc(datasource,user,pass);
					return dc.using();
				}	
			}
			//config=ThreadLocalPageContext.getConfig();
			
			_inc(datasource,user,pass);

		}			
		return loadDatasourceConnection(config,datasource, user, pass).using();
	}

	private DatasourceConnectionImpl loadDatasourceConnection(Config config,DataSource ds, String user, String pass) throws PageException {
        Connection conn=null;
        try {
        	conn = ds.getConnection(config, user, pass);// DBUtil.getConnection(connStr, user, pass);
        	conn.setAutoCommit(true);
        } 
        catch (SQLException e) {
        	throw new DatabaseException(e,null);
        }
        catch (Exception e) {
        	throw Caster.toPageException(e);
        }
		//print.err("create connection");
        return new DatasourceConnectionImpl(conn,ds,user,pass);
    }
	
	public void releaseDatasourceConnection(DatasourceConnection dc,boolean closeIt) {
		if(dc==null) return;
		
		DCStack stack=getDCStack(dc.getDatasource(), dc.getUsername(), dc.getPassword());
		synchronized (stack) {
			if(closeIt) IOUtil.closeEL(dc.getConnection());
			else stack.add(dc);
			
			int max =dc.getDatasource().getConnectionLimit();
			if(max!=-1) {
				_dec(dc.getDatasource(),dc.getUsername(),dc.getPassword());
				stack.notify();
			}
			else _dec(dc.getDatasource(),dc.getUsername(),dc.getPassword());
		}
	}
	
	public void releaseDatasourceConnection(DatasourceConnection dc) {
		releaseDatasourceConnection(dc, false);
	}

	public void clear() {
		// remove all timed out conns
		try{
			Object[] arr = dcs.entrySet().toArray();
			if(ArrayUtil.isEmpty(arr)) return;
			for(int i=0;i<arr.length;i++) {
				DCStack conns=(DCStack) ((Map.Entry) arr[i]).getValue();
				if(conns!=null)conns.clear();
			}
		}
		catch(Throwable t){}
	}

	public void remove(DataSource datasource) {
		Object[] arr = dcs.keySet().toArray();
		String key,id=datasource.id(); // MUST
        for(int i=0;i<arr.length;i++) {
        	key=(String) arr[i];
        	if(key.startsWith(id)) {
				DCStack conns=dcs.get(key);
				conns.clear();
        	}
		}
        
        RefInteger ri=counter.get(id);
		if(ri!=null)ri.setValue(0);
		else counter.put(id,new RefIntegerSync(0));
        
	}
	

	
	public static boolean isValid(DatasourceConnection dc,Boolean autoCommit) {
		try {
			if(dc.getConnection().isClosed())return false;
		} 
		catch (Throwable t) {return false;}

		try {
			if(dc.getDatasource().validate() && !DataSourceUtil.isValid(dc,1000))return false;
		} 
		catch (Throwable t) {} // not all driver support this, because of that we ignore a error here, also protect from java 5
		
		
		try {
			if(autoCommit!=null) dc.getConnection().setAutoCommit(autoCommit.booleanValue());
		} 
		catch (Throwable t) {return false;}
		
		
		return true;
	}


	private DCStack getDCStack(DataSource datasource, String user, String pass) {
		String id = createId(datasource,user,pass);
		synchronized(id) {
			DCStack stack=dcs.get(id);
		
			if(stack==null){
				dcs.put(id, stack=new DCStack(datasource,user,pass));
			}
			return stack;
		}
	}
	
	public int openConnections() {
		Iterator<DCStack> it = dcs.values().iterator();
		int count=0;
		while(it.hasNext()){
			count+=it.next().openConnections();
		}
		return count;
	}

	private void _inc(DataSource datasource, String username,String password) {
		RefInteger c = _getCounter(datasource,username,password);
		c.plus(1);
	}
	private void _dec(DataSource datasource, String username,String password) {
		RefInteger c = _getCounter(datasource,username,password);
		c.minus(1);
	}
	private int _size(DataSource datasource, String username,String password) {
		return _getCounter(datasource,username,password).toInt();
	}

	private RefInteger _getCounter(DataSource datasource, String username,String password) {
		String did = createId(datasource, username, password);
		synchronized (counter) {
			RefInteger ri=counter.get(did);
			if(ri==null) {
				counter.put(did,ri=new RefIntegerSync(0));
			}
			return ri;
		}
		
	}

	public static String createId(DataSource datasource, String user, String pass) {
		return HashUtil.create64BitHashAsString(datasource.id()+":"+user+":"+pass);
	}
}