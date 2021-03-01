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
package lucee.runtime.tag;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.debug.ActiveLock;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.LockException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.BodyTagTryCatchFinallyImpl;
import lucee.runtime.lock.LockData;
import lucee.runtime.lock.LockManager;
import lucee.runtime.lock.LockManagerImpl;
import lucee.runtime.lock.LockTimeoutException;
import lucee.runtime.lock.LockTimeoutExceptionImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.scope.ApplicationImpl;
import lucee.runtime.type.scope.RequestImpl;
import lucee.runtime.type.scope.ServerImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.util.PageContextUtil;

/**
 * Provides two types of locks to ensure the integrity of shared data: Exclusive lock and Read-only
 * lock. An exclusive lock single-threads access to the CFML constructs in its body. Single-threaded
 * access implies that the body of the tag can be executed by at most one request at a time. A
 * request executing inside a cflock tag has an "exclusive lock" on the tag. No other requests can
 * start executing inside the tag while a request has an exclusive lock. CFML issues exclusive locks
 * on a first-come, first-served basis. A read-only lock allows multiple requests to access the CFML
 * constructs inside its body concurrently. Therefore, read-only locks should be used only when the
 * shared data is read only and not modified. If another request already has an exclusive lock on
 * the shared data, the request waits for the exclusive lock to be released.
 *
 *
 *
 **/
public final class Lock extends BodyTagTryCatchFinallyImpl {

	private static final short SCOPE_NONE = 0;
	private static final short SCOPE_SERVER = 1;
	private static final short SCOPE_APPLICATION = 2;
	private static final short SCOPE_SESSION = 3;
	private static final short SCOPE_REQUEST = 4;

	private String id = "anonymous";

	/**
	 * Specifies the maximum amount of time, in seconds, to wait to obtain a lock. If a lock can be
	 ** obtained within the specified period, execution continues inside the body of the tag. Otherwise,
	 * the behavior depends on the value of the throwOnTimeout attribute.
	 */
	private int timeoutInMillis;

	/**
	 * readOnly or Exclusive. Specifies the type of lock: read-only or exclusive. Default is Exclusive.
	 ** A read-only lock allows more than one request to read shared data. An exclusive lock allows only
	 * one request to read or write to shared data.
	 */
	private short type = LockManager.TYPE_EXCLUSIVE;

	/**
	 * Specifies the scope as one of the following: Application, Server, or Session. This attribute is
	 * mutually exclusive with the name attribute.
	 */
	private short scope = SCOPE_NONE;

	/**
	 * Yes or No. Specifies how timeout conditions are handled. If the value is Yes, an exception is
	 ** generated to provide notification of the timeout. If the value is No, execution continues past
	 * the cfclock tag. Default is Yes.
	 */
	private boolean throwontimeout = true;

	/** Specifies the name of the lock. */
	private String name;

	private LockManager manager;
	private LockData data = null;
	private long start;
	private String result = "cflock";

	@Override
	public void release() {
		super.release();
		type = LockManager.TYPE_EXCLUSIVE;
		scope = SCOPE_NONE;
		throwontimeout = true;
		name = null;
		manager = null;
		this.data = null;
		id = "anonymous";
		timeoutInMillis = 0;
		result = "cflock";
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * set the value timeout Specifies the maximum amount of time, in seconds, to wait to obtain a lock.
	 * If a lock can be obtained within the specified period, execution continues inside the body of the
	 * tag. Otherwise, the behavior depends on the value of the throwOnTimeout attribute.
	 * 
	 * @param timeout value to set
	 **/
	public void setTimeout(Object oTimeout) throws PageException {
		if (oTimeout instanceof TimeSpan) this.timeoutInMillis = toInt(((TimeSpan) oTimeout).getMillis());
		else this.timeoutInMillis = toInt(Caster.toDoubleValue(oTimeout) * 1000D);
		// print.out(Caster.toString(timeoutInMillis));
	}

	public void setTimeout(double timeout) {
		this.timeoutInMillis = toInt(timeout * 1000D);
	}

	/**
	 * set the value type readOnly or Exclusive. Specifies the type of lock: read-only or exclusive.
	 * Default is Exclusive. A read-only lock allows more than one request to read shared data. An
	 * exclusive lock allows only one request to read or write to shared data.
	 * 
	 * @param type value to set
	 * @throws ApplicationException
	 **/
	public void setType(String type) throws ApplicationException {
		type = type.toLowerCase().trim();

		if (type.equals("exclusive")) {
			this.type = LockManager.TYPE_EXCLUSIVE;
		}
		else if (type.startsWith("read")) {
			this.type = LockManager.TYPE_READONLY;
		}
		else throw new ApplicationException("invalid value [" + type + "] for attribute [type] from tag [lock]", "valid values are [exclusive,read-only]");
	}

	/**
	 * set the value scope Specifies the scope as one of the following: Application, Server, or Session.
	 * This attribute is mutually exclusive with the name attribute.
	 * 
	 * @param scope value to set
	 * @throws ApplicationException
	 **/
	public void setScope(String scope) throws ApplicationException {
		scope = scope.toLowerCase().trim();

		if (scope.equals("server")) this.scope = SCOPE_SERVER;
		else if (scope.equals("application")) this.scope = SCOPE_APPLICATION;
		else if (scope.equals("session")) this.scope = SCOPE_SESSION;
		else if (scope.equals("request")) this.scope = SCOPE_REQUEST;
		else throw new ApplicationException("invalid value [" + scope + "] for attribute [scope] from tag [lock]", "valid values are [server,application,session]");
	}

	/**
	 * set the value throwontimeout Yes or No. Specifies how timeout conditions are handled. If the
	 * value is Yes, an exception is generated to provide notification of the timeout. If the value is
	 * No, execution continues past the cfclock tag. Default is Yes.
	 * 
	 * @param throwontimeout value to set
	 **/
	public void setThrowontimeout(boolean throwontimeout) {
		this.throwontimeout = throwontimeout;
	}

	/**
	 * set the value name
	 * 
	 * @param name value to set
	 * @throws ApplicationException
	 **/
	public void setName(String name) throws ApplicationException {
		if (name == null) return;
		this.name = name.trim();
		if (name.length() == 0) throw new ApplicationException("invalid attribute definition", "attribute [name] can't be an empty string");
	}

	public void setResult(String result) throws ApplicationException {
		if (StringUtil.isEmpty(result)) return;
		this.result = result.trim();
	}

	@Override
	public int doStartTag() throws PageException {
		if (timeoutInMillis <= 0) {
			TimeSpan remaining = PageContextUtil.remainingTime(pageContext, true);
			this.timeoutInMillis = toInt(remaining.getMillis());
		}

		manager = pageContext.getConfig().getLockManager();
		// check attributes
		if (name != null && scope != SCOPE_NONE) {
			throw new LockException(LockException.OPERATION_CREATE, this.name, "invalid attribute combination", "attribute [name] and [scope] can't be used together");
		}
		if (name == null && scope == SCOPE_NONE) {
			name = "id-" + id;
		}

		String lockType = null;
		if (name == null) {
			String cid = pageContext.getConfig().getIdentification().getId();
			// Session
			if (scope == SCOPE_REQUEST) {
				lockType = "request";
				name = "__request_" + cid + "__" + ((RequestImpl) pageContext.requestScope())._getId();
			}
			// Session
			else if (scope == SCOPE_SESSION) {
				lockType = "session";
				name = "__session_" + cid + "__" + pageContext.sessionScope()._getId();
			}
			// Application
			else if (scope == SCOPE_APPLICATION) {
				lockType = "application";
				name = "__application_" + cid + "__" + ((ApplicationImpl) pageContext.applicationScope())._getId();
			}
			// Server
			else if (scope == SCOPE_SERVER) {
				lockType = "server";
				name = "__server_" + ((ServerImpl) pageContext.serverScope())._getId();
			}
		}
		Struct cflock = new StructImpl();
		cflock.set(KeyConstants._succeeded, Boolean.TRUE);
		cflock.set(KeyConstants._errortext, "");
		pageContext.setVariable(result, cflock);
		start = System.nanoTime();
		try {
			((PageContextImpl) pageContext).setActiveLock(new ActiveLock(type, name, timeoutInMillis)); // this has to be first, otherwise LockTimeoutException has nothing to
			// release
			data = manager.lock(type, name, timeoutInMillis, pageContext.getId());
		}
		catch (LockTimeoutException e) {
			LockManagerImpl mi = (LockManagerImpl) manager;
			Boolean hasReadLock = mi.isReadLocked(name);
			Boolean hasWriteLock = mi.isWriteLocked(name);
			String msg = LockTimeoutExceptionImpl.createMessage(type, name, lockType, timeoutInMillis, hasReadLock, hasWriteLock);

			_release(pageContext, System.nanoTime() - start);
			name = null;

			cflock.set(KeyConstants._succeeded, Boolean.FALSE);
			cflock.set(KeyConstants._errortext, msg);

			if (throwontimeout) throw new LockException(LockException.OPERATION_TIMEOUT, this.name, msg);

			return SKIP_BODY;
		}
		catch (InterruptedException e) {
			_release(pageContext, System.nanoTime() - start);
			cflock.set(KeyConstants._succeeded, Boolean.FALSE);
			cflock.set(KeyConstants._errortext, e.getMessage());

			if (throwontimeout) throw Caster.toPageException(e);

			return SKIP_BODY;

		}

		return EVAL_BODY_INCLUDE;
	}

	private int toInt(long l) {
		if (l > Integer.MAX_VALUE) return Integer.MAX_VALUE;
		return (int) l;
	}

	private int toInt(double d) {
		if (d > Integer.MAX_VALUE) return Integer.MAX_VALUE;
		return (int) d;
	}

	private void _release(PageContext pc, long exe) {
		ActiveLock al = ((PageContextImpl) pc).releaseActiveLock();
		// listener
		((ConfigWebPro) pc.getConfig()).getActionMonitorCollector().log(pageContext, "lock", "Lock", exe, al.name + ":" + al.timeoutInMillis);

	}

	@Override
	public void doFinally() {
		_release(pageContext, System.nanoTime() - start);
		if (name != null) manager.unlock(data);
	}
}