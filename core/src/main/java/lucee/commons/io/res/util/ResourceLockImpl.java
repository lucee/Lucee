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
import java.util.Iterator;
import java.util.Map;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceLock;
import lucee.commons.lang.SerializableObject;
import lucee.runtime.CFMLFactory;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.PageContextImpl;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.net.http.ReqRspUtil;

public final class ResourceLockImpl implements ResourceLock {

	private static final long serialVersionUID = 6888529579290798651L;

	private long lockTimeout;
	private boolean caseSensitive;

	public ResourceLockImpl(long timeout, boolean caseSensitive) {
		this.lockTimeout = timeout;
		this.caseSensitive = caseSensitive;
	}

	private Object token = new SerializableObject();
	private Map<String, Thread> resources = new HashMap<String, Thread>();

	@Override
	public void lock(Resource res) {
		String path = getPath(res);

		synchronized (token) {
			_read(path);
			resources.put(path, Thread.currentThread());
		}
	}

	private String getPath(Resource res) {
		return caseSensitive ? res.getPath() : res.getPath().toLowerCase();
	}

	@Override
	public void unlock(Resource res) {
		String path = getPath(res);
		// if(path.endsWith(".dmg"))print.err("unlock:"+path);
		synchronized (token) {
			resources.remove(path);
			token.notifyAll();
		}
	}

	@Override
	public void read(Resource res) {
		String path = getPath(res);
		synchronized (token) {
			// print.ln(".......");
			_read(path);
		}
	}

	private void _read(String path) {
		long start = -1, now;
		Thread t;
		do {
			if ((t = resources.get(path)) == null) {
				return;
			}
			if (t == Thread.currentThread()) {
				Config config = ThreadLocalPageContext.getConfig();
				LogUtil.log(config, Log.LEVEL_ERROR, "file", "Conflict in same thread: on [" + path + "]");
				return;
			}
			// bugfix when lock from dead thread, ignore it
			if (!t.isAlive()) {
				resources.remove(path);
				return;
			}
			if (start == -1) start = System.currentTimeMillis();
			try {
				token.wait(lockTimeout);
				now = System.currentTimeMillis();
				if ((start + lockTimeout) <= now) {
					Config config = ThreadLocalPageContext.getConfig();

					if (config != null) {
						PageContextImpl pc = null;
						String add = "";
						if (config instanceof ConfigWeb) {
							CFMLFactory factory = ((ConfigWeb) config).getFactory();
							if (factory instanceof CFMLFactoryImpl) {
								Map<Integer, PageContextImpl> pcs = ((CFMLFactoryImpl) factory).getActivePageContexts();
								Iterator<PageContextImpl> it = pcs.values().iterator();
								PageContextImpl tmp;
								while (it.hasNext()) {
									tmp = it.next();
									if (t == tmp.getThread()) {
										pc = tmp;
										break;
									}
								}
							}
						}
						if (pc != null) {
							add = " The file is locked by a request on the following URL [" + ReqRspUtil.getRequestURL(pc.getHttpServletRequest(), true) + "], that request started "
									+ (System.currentTimeMillis() - pc.getStartTime()) + "ms ago.";
						}

						LogUtil.log(config, Log.LEVEL_ERROR, "file",
								"Timeout after " + (now - start) + " ms (" + (lockTimeout) + " ms) occurred while accessing file [" + path + "]." + add);

					}
					else LogUtil.log(config, Log.LEVEL_ERROR, "file", "Timeout (" + (lockTimeout) + " ms) occurred while accessing file [" + path + "].");

					return;
				}
			}
			catch (InterruptedException e) {}
		}
		while (true);
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
