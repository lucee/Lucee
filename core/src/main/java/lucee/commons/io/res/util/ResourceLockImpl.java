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
package lucee.commons.io.res.util;

import java.util.HashMap;
import java.util.Map;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceLock;
import lucee.commons.lang.SerializableObject;
import lucee.commons.lang.SystemOut;
import lucee.runtime.config.Config;
import lucee.runtime.engine.ThreadLocalPageContext;

public final class ResourceLockImpl implements ResourceLock {
	
	private static final long serialVersionUID = 6888529579290798651L;
	
	private long lockTimeout;
	private boolean caseSensitive;

	public ResourceLockImpl(long timeout,boolean caseSensitive) {
		this.lockTimeout=timeout;
		this.caseSensitive=caseSensitive;
	}

	private Object token=new SerializableObject();
	private Map<String,Thread> resources=new HashMap<String,Thread>();
	
	@Override
	public void lock(Resource res) {
		String path=getPath(res);
		
		synchronized(token)  {
			_read(path);
			resources.put(path,Thread.currentThread());
		}
	}

	private String getPath(Resource res) {
		return caseSensitive?res.getPath():res.getPath().toLowerCase();
	}

	@Override
	public void unlock(Resource res) {
		String path=getPath(res);
		//if(path.endsWith(".dmg"))print.err("unlock:"+path);
		synchronized(token)  {
			resources.remove(path);
			token.notifyAll();
		}
	}

	@Override
	public void read(Resource res) {
		String path=getPath(res);
		synchronized(token)  {
			//print.ln(".......");
			_read(path);
		}
	}

	private void _read(String path) {
		long start=-1,now;
		Thread t;
		do {
			if((t=resources.get(path))==null) {
				//print.ln("read ok");
				return;
			}
			if(t==Thread.currentThread()) {
				//aprint.err(path);
				Config config = ThreadLocalPageContext.getConfig();
				if(config!=null)
					SystemOut.printDate(config.getErrWriter(),"conflict in same thread: on "+path);
				//SystemOut.printDate(config.getErrWriter(),"conflict in same thread: on "+path+"\nStacktrace:\n"+StringUtil.replace(ExceptionUtil.getStacktrace(new Throwable(), false),"java.lang.Throwable\n","",true));
				return;
			}
			// bugfix when lock von totem thread, wird es ignoriert
			if(!t.isAlive()) {
				resources.remove(path);
				return;
			}
			if(start==-1)start=System.currentTimeMillis();
			try {
				token.wait(lockTimeout);
				now=System.currentTimeMillis();
				if((start+lockTimeout)<=now) {
					Config config = ThreadLocalPageContext.getConfig();
					if(config!=null)
						SystemOut.printDate(config.getErrWriter(),"timeout after "+(now-start)+" ms ("+(lockTimeout)+" ms) occured while accessing file ["+path+"]");
					else 
						SystemOut.printDate("timeout ("+(lockTimeout)+" ms) occured while accessing file ["+path+"]");
					return;
				}
			} 
			catch (InterruptedException e) {
			}
		}
		while(true);
	}

	@Override
	public long getLockTimeout() {
		return lockTimeout;
	}

	/**
	 * @param lockTimeout the lockTimeout to set
	 */
	@Override
	public void setLockTimeout(long lockTimeout) {
		this.lockTimeout = lockTimeout;
	}

	/**
	 * @param caseSensitive the caseSensitive to set
	 */
	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}
}