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

import lucee.runtime.PageContext;

/**
 * creates Local and Argument scopes and recyle it
 */
public final class ScopeFactory {

	int argumentCounter = 0;
	final Argument[] arguments = new Argument[] { new ArgumentImpl(), new ArgumentImpl(), new ArgumentImpl(), new ArgumentImpl(), new ArgumentImpl(), new ArgumentImpl(),
			new ArgumentImpl(), new ArgumentImpl(), new ArgumentImpl(), new ArgumentImpl(), new ArgumentImpl(), new ArgumentImpl(), new ArgumentImpl(), new ArgumentImpl(),
			new ArgumentImpl(), new ArgumentImpl(), new ArgumentImpl(), new ArgumentImpl(), new ArgumentImpl() };

	int localCounter = 0;
	LocalImpl[] locals = new LocalImpl[] { new LocalImpl(), new LocalImpl(), new LocalImpl(), new LocalImpl(), new LocalImpl(), new LocalImpl(), new LocalImpl(), new LocalImpl(),
			new LocalImpl(), new LocalImpl(), new LocalImpl(), new LocalImpl(), new LocalImpl(), new LocalImpl(), new LocalImpl(), new LocalImpl(), new LocalImpl(),
			new LocalImpl() };

	/**
	 * @return returns an Argument scope
	 */
	public Argument getArgumentInstance() {
		if (argumentCounter < arguments.length) {
			return arguments[argumentCounter++];
		}
		return new ArgumentImpl();
	}

	/**
	 * @return retruns a Local Instance
	 */
	public LocalImpl getLocalInstance() {
		if (localCounter < locals.length) {
			return locals[localCounter++];
		}
		return new LocalImpl();
	}

	/**
	 * @param argument recycle an Argument scope for reuse
	 */
	public void recycle(PageContext pc, Argument argument) {
		if (argumentCounter <= 0 || argument.isBind()) return;
		argument.release(pc);
		arguments[--argumentCounter] = argument;
	}

	/**
	 * @param local recycle a Local scope for reuse
	 */
	public void recycle(PageContext pc, LocalImpl local) {
		if (localCounter <= 0 || local.isBind()) return;
		local.release(pc);
		locals[--localCounter] = local;
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
			return "cluster";
		}

		return defaultValue;
	}

}