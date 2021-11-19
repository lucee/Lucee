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

/**
 * This Exception will be thrown, when page Excecution is aborted (tag abort).
 */
public class Abort extends AbortException {

	public final static int SCOPE_PAGE = 0;
	public final static int SCOPE_REQUEST = 1;
	private int scope;

	/**
	 * Constructor of the Class
	 */
	public Abort(int scope) {
		super("Page request is aborted");
		this.scope = scope;
	}

	protected Abort(int scope, String msg) {
		super(msg);
		this.scope = scope;
	}

	public static Abort newInstance(int scope) {
		return new Abort(scope);
	}

	public int getScope() {
		return scope;
	}

	public static boolean isSilentAbort(Throwable t) {
		if (t instanceof PageExceptionBox) {
			return isSilentAbort(((PageExceptionBox) t).getPageException());
		}
		return t instanceof Abort && !(t instanceof RequestTimeoutException);
	}

	public static boolean isAbort(Throwable t) {
		if (t instanceof Abort) return true;
		if (t instanceof PageExceptionBox) {
			return (((PageExceptionBox) t).getPageException() instanceof Abort);
		}
		return false;
	}

	public static boolean isAbort(Throwable t, int scope) {
		if (t instanceof PageExceptionBox) {
			return isAbort(((PageExceptionBox) t).getPageException(), scope);
		}
		return t instanceof Abort && ((Abort) t).getScope() == scope;
	}
}