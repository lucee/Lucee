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

import lucee.runtime.PageContext;
import lucee.runtime.cache.CacheObject;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Collection;

public final class CachedWithinId extends BIF {

	private static final long serialVersionUID = 5373201629836888264L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {

		CacheObject co = getCacheObject(pc, args, "CachedWithinId");
		Collection arguments = getArguments(args, "CachedWithinId");

		return co.getCacheId(arguments, null);
	}

	static CacheObject getCacheObject(PageContext pc, Object[] args, String name) throws FunctionException {
		// first arguments is the object
		if (args.length == 0) {
			throw new FunctionException(pc, name, 0, Integer.MAX_VALUE, args.length);
		}
		Object obj = args[0];
		if (obj instanceof CacheObject) {
			return (CacheObject) obj;
		}

		throw new FunctionException(pc, name, 1, "cacheObject", "input must be a valid cache object like [query, function or http struct]");

	}

	static Collection getArguments(Object[] args, String name) throws PageException {
		// has arguments
		if (args.length > 1) {
			if (Decision.isStruct(args[1])) {
				return Caster.toStruct(args[1]);
			}
			return Caster.toArray(args[1]);
		}
		return null;
	}

}