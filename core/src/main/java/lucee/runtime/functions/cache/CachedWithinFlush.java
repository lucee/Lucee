/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
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
 */
package lucee.runtime.functions.cache;

import java.io.IOException;

import lucee.commons.io.cache.Cache;
import lucee.runtime.PageContext;
import lucee.runtime.cache.CacheObject;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;

public final class CachedWithinFlush extends BIF {

	private static final long serialVersionUID = 2693279465493647569L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {

		CacheObject co = CachedWithinId.getCacheObject(pc, args, "CachedWithinFlush");
		Collection arguments = CachedWithinId.getArguments(args, "CachedWithinFlush");

		try {
			String id = co.getCacheId(arguments, null);
			if (id == null) return false;
			Cache cache = CacheUtil.getCache(pc, null, co.getCachetype());
			return cache.remove(id);

		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}

}