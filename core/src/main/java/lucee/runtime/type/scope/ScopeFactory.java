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
package lucee.runtime.type.scope;

import java.util.Arrays;

import lucee.commons.io.SystemUtil;
import lucee.runtime.PageContext;
import lucee.runtime.op.Caster;

/**
 * creates Local and Argument scopes and recyle it.
 * Per-PageContext instance; pools are single-threaded by design (PC owns one request thread at a time).
 * LIFO ordering: most-recently-recycled scope is reused first (cache-friendly).
 */
public final class ScopeFactory {

	private static final int MAX_SIZE;
	private static final int INITIAL_SIZE;
	static {
		MAX_SIZE = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("lucee.scope.pool.maxsize", "50"), 50);
		INITIAL_SIZE = Math.min(MAX_SIZE, 8);
	}

	// Lazy-allocated LIFO stacks. Many ScopeFactory instances never recycle a scope; they pay nothing.
	private Argument[] argPool;
	private int argPoolTop = -1;
	private LocalImpl[] localPool;
	private int localPoolTop = -1;

	/**
	 * @return returns an Argument scope
	 */
	public Argument getArgumentInstance() {
		return (argPoolTop < 0) ? new ArgumentImpl() : argPool[argPoolTop--];
	}

	/**
	 * @return retruns a Local Instance
	 */
	public LocalImpl getLocalInstance() {
		return (localPoolTop < 0) ? new LocalImpl() : localPool[localPoolTop--];
	}

	/**
	 * @param argument recycle an Argument scope for reuse
	 */
	public void recycle(PageContext pc, Argument argument) {
		// isBind first: cheap field read, short-circuits captured-by-closure scopes (LDEV-3210)
		if (argument.isBind() || argPoolTop + 1 >= MAX_SIZE) return;
		argument.release(pc);
		if (argPool == null) argPool = new Argument[INITIAL_SIZE];
		else if (argPoolTop + 1 >= argPool.length) argPool = Arrays.copyOf(argPool, Math.min(argPool.length * 2, MAX_SIZE));
		argPool[++argPoolTop] = argument;
	}

	/**
	 * @param local recycle a Local scope for reuse
	 */
	public void recycle(PageContext pc, LocalImpl local) {
		if (local.isBind() || localPoolTop + 1 >= MAX_SIZE) return;
		local.release(pc);
		if (localPool == null) localPool = new LocalImpl[INITIAL_SIZE];
		else if (localPoolTop + 1 >= localPool.length) localPool = Arrays.copyOf(localPool, Math.min(localPool.length * 2, MAX_SIZE));
		localPool[++localPoolTop] = local;
	}

	/**
	 * cast an int scope definition to a string definition
	 * 
	 * @param scope
	 * @return
	 */
	public static String toStringScope(int scope, String defaultValue) {
		switch (scope) {
		case Scope.SCOPE_APPLICATION:
			return "application";
		case Scope.SCOPE_ARGUMENTS:
			return "arguments";
		case Scope.SCOPE_CALLER:
			return "caller";
		case Scope.SCOPE_CGI:
			return "cgi";
		case Scope.SCOPE_CLIENT:
			return "client";
		case Scope.SCOPE_COOKIE:
			return "cookie";
		case Scope.SCOPE_FORM:
			return "form";
		case Scope.SCOPE_VAR:
		case Scope.SCOPE_LOCAL:
			return "local";
		case Scope.SCOPE_REQUEST:
			return "request";
		case Scope.SCOPE_SERVER:
			return "server";
		case Scope.SCOPE_SESSION:
			return "session";
		case Scope.SCOPE_UNDEFINED:
			return "undefined";
		case Scope.SCOPE_URL:
			return "url";
		case Scope.SCOPE_VARIABLES:
			return "variables";
		case Scope.SCOPE_CLUSTER:
			return "thread";
		}

		return defaultValue;
	}

}