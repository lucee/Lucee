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

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import lucee.commons.io.SystemUtil;
import lucee.runtime.PageContext;
import lucee.runtime.op.Caster;

/**
 * creates Local and Argument scopes and recyle it
 */
public final class ScopeFactory {

	private static final int MAX_SIZE;

	static {
		MAX_SIZE = Caster.toIntValue( SystemUtil.getSystemPropOrEnvVar( "lucee.scope.pool.maxsize", "50" ), 50 );
	}

	private final ConcurrentLinkedQueue<Argument> arguments = new ConcurrentLinkedQueue<Argument>();
	private final ConcurrentLinkedQueue<LocalImpl> locals = new ConcurrentLinkedQueue<LocalImpl>();

	private final AtomicInteger argumentsSize = new AtomicInteger( 0 );
	private final AtomicInteger localsSize = new AtomicInteger( 0 );

	/**
	 * @return returns an Argument scope
	 */
	public Argument getArgumentInstance() {
		Argument arg = arguments.poll();
		if (arg != null) {
			argumentsSize.decrementAndGet();
			return arg;
		}
		return new ArgumentImpl();
	}

	/**
	 * @return retruns a Local Instance
	 */
	public LocalImpl getLocalInstance() {
		LocalImpl lcl = locals.poll();
		if (lcl != null) {
			localsSize.decrementAndGet();
			return lcl;
		}
		return new LocalImpl();
	}

	/**
	 * @param argument recycle an Argument scope for reuse
	 */
	public void recycle(PageContext pc, Argument argument) {
		if (argument.isBind()) return;

		int size = argumentsSize.getAndIncrement();
		if (size >= MAX_SIZE) {
			argumentsSize.decrementAndGet(); // rollback
			return;
		}

		argument.release(pc);
		arguments.add(argument);
	}

	/**
	 * @param local recycle a Local scope for reuse
	 */
	public void recycle(PageContext pc, LocalImpl local) {
		if (local.isBind()) return;

		int size = localsSize.getAndIncrement();
		if (size >= MAX_SIZE) {
			localsSize.decrementAndGet(); // rollback
			return;
		}

		local.release(pc);
		locals.add(local);
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