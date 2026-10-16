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
import lucee.runtime.listener.ModernApplicationContext;
import lucee.runtime.type.Null;

public final class NullSupportHelper {

	public final static boolean full(PageContext pc) {

		if (pc == null) {
			// we know
			if (!ModernApplicationContext.hasCustomNullSupportSetting) {
				return false;
			}

			pc = ThreadLocalPageContext.get();
			if (pc == null) return false;
		}
		return ((PageContextImpl) pc).getFullNullSupport();
	}

	public final static boolean full() {
		return full((PageContext) null);
	}

	public final static boolean isNull(PageContext pc, Object val) {
		return val == Null.NULL || (val == null && !full(pc));
	}

	public final static boolean isNull(Object val) {
		return val == Null.NULL || (val == null && !full());
	}

	public final static Object NULL(boolean fns) {
		return fns ? Null.NULL : null;
	}

	public final static Object NULL(PageContext pc) {
		return full(pc) ? Null.NULL : null;
	}

	public final static Object empty(PageContext pc) {
		return full(pc) ? null : "";
	}
}