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
package lucee.runtime.functions.system;

import lucee.commons.io.SystemUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.Query;

public final class GetMemoryUsage implements Function {

	private static final long serialVersionUID = -7937791531186794443L;

	public static Query call(PageContext pc) throws PageException {
		return call(pc, null);
	}

	public static Query call(PageContext pc, String type) throws PageException {
		if (StringUtil.isEmpty(type)) return SystemUtil.getMemoryUsageAsQuery(SystemUtil.MEMORY_TYPE_ALL);

		type = type.trim().toLowerCase();
		if ("heap".equalsIgnoreCase(type)) return SystemUtil.getMemoryUsageAsQuery(SystemUtil.MEMORY_TYPE_HEAP);
		if ("non_heap".equalsIgnoreCase(type) || "nonheap".equalsIgnoreCase(type) || "non-heap".equalsIgnoreCase(type) || "none_heap".equalsIgnoreCase(type)
				|| "noneheap".equalsIgnoreCase(type) || "none-heap".equalsIgnoreCase(type))
			return SystemUtil.getMemoryUsageAsQuery(SystemUtil.MEMORY_TYPE_NON_HEAP);

		throw new FunctionException(pc, "GetMemoryUsage", 1, "type", "invalid value [" + type + "], valid values are [heap,non_heap]");
	}
}