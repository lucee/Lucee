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
package lucee.runtime.cache.tag.query;

import lucee.commons.io.res.util.UDFFilterSupport;
import lucee.runtime.cache.tag.CacheHandlerFilter;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Query;
import lucee.runtime.type.UDF;

public class QueryCacheHandlerFilterUDF extends UDFFilterSupport implements CacheHandlerFilter {

	private UDF udf;

	public QueryCacheHandlerFilterUDF(UDF udf) throws ExpressionException {
		super(udf);
		this.udf = udf;
	}

	@Override
	public boolean accept(Object obj) {
		if (!(obj instanceof Query)) return false;

		args[0] = ((Query) obj).getSql();
		try {
			return Caster.toBooleanValue(udf.call(ThreadLocalPageContext.get(), args, true));
		}
		catch (PageException e) {
			throw new PageRuntimeException(e);
		}
	}

}