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
package lucee.runtime.exp;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.lock.LockManager;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ListUtil;

public class RequestTimeoutException extends Abort implements Stop {

	private static final long serialVersionUID = -37886162001453270L;

	private StackTraceElement[] stacktrace;

	private ThreadDeath threadDeath;

	public RequestTimeoutException(PageContextImpl pc, ThreadDeath td) {
		this(pc, pc.getTimeoutStackTrace());
		this.threadDeath = td;
	}

	public RequestTimeoutException(PageContext pc, StackTraceElement[] stacktrace) {
		super(SCOPE_REQUEST, "request [" + getPath(pc) + "] has run into a timeout (timeout: [" + (pc.getRequestTimeout() / 1000)
				+ "] seconds) and has been stopped. The thread started [" + (System.currentTimeMillis() - pc.getStartTime()) + "] ms ago." + locks(pc));	
		this.stacktrace = stacktrace;
		setStackTrace(stacktrace);
		// TODO Auto-generated constructor stub
	}

	@Override
	public StackTraceElement[] getStackTrace() {
		return stacktrace;
	}

	public static String locks(PageContext pc) {
		String strLocks = "";
		try {
			LockManager manager = pc.getConfig().getLockManager();
			String[] locks = manager.getOpenLockNames();
			if (!ArrayUtil.isEmpty(locks)) strLocks = " Open locks at this time [" + ListUtil.arrayToList(locks, ", ") + "].";
			// LockManagerImpl.unlockAll(pc.getId());
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		return strLocks;
	}

	private static String getPath(PageContext pc) {
		try {
			PageSource ps = pc.getBasePageSource();
			return ps.getRealpathWithVirtual() + " (" + pc.getBasePageSource().getDisplayPath() + ")";
		}
		catch (NullPointerException npe) {
			return "(no path available)";
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return "(fail to retrieve path:" + t.getClass().getName() + ":" + t.getMessage() + ")";
		}
	}

	public ThreadDeath getThreadDeath() {
		return threadDeath;
	}
}
