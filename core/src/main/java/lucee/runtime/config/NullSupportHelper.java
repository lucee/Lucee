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
package lucee.runtime.config;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.type.Null;

public class NullSupportHelper {

	public static boolean full(PageContext pc) {

		if (pc == null) {
			pc = ThreadLocalPageContext.get();
			if (pc == null) return false;
		}
		return ((PageContextImpl) pc).getFullNullSupport();
	}

	public static boolean full() {
		/*
		 * String str = ExceptionUtil.getStacktrace(new Throwable(), false); if
		 * (str.indexOf("lucee.runtime.reflection.storage.SoftMethodStorage.storeArgs") == -1 &&
		 * str.indexOf("lucee.runtime.type.scope.CGIImplReadOnly.get") == -1 &&
		 * str.indexOf("lucee.runtime.type.scope.RequestImpl.get") == -1 &&
		 * str.indexOf("lucee.runtime.type.QueryImpl.getAt") == -1
		 * 
		 * ) print.e(str);
		 */

		return full(ThreadLocalPageContext.get());
	}

	public static Object NULL(boolean fns) {
		return fns ? Null.NULL : null;
	}

	public static Object NULL(PageContext pc) {
		return full(pc) ? Null.NULL : null;
	}

	public static Object NULL() {
		return full() ? Null.NULL : null;
	}

	public static Object empty(PageContext pc) {
		return full(pc) ? null : "";
	}
}