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
package lucee.commons.io.res.util;

import java.io.File;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.UDF;

public class UDFFilter extends UDFFilterSupport implements ResourceAndResourceNameFilter {

	public UDFFilter(UDF udf) throws ExpressionException {
		super(udf);
	}

	public boolean accept(String path) {
		args[0] = path;
		try {
			return Caster.toBooleanValue(udf.call(ThreadLocalPageContext.get(), args, true));

		}
		catch (PageException e) {
			throw new PageRuntimeException(e);
		}
	}

	@Override
	public boolean accept(Resource file) {
		return accept(file.getAbsolutePath());
	}

	@Override
	public boolean accept(Resource parent, String name) {
		String path = parent.getAbsolutePath();
		if (path.endsWith(File.separator)) path += name;
		else path += File.separator + name;
		return accept(path);
	}

	@Override
	public String toString() {
		return "UDFFilter:" + udf;
	}

	public static ResourceAndResourceNameFilter createResourceAndResourceNameFilter(Object filter) throws PageException {
		if (filter instanceof UDF) return createResourceAndResourceNameFilter((UDF) filter);
		return createResourceAndResourceNameFilter(Caster.toString(filter));
	}

	public static ResourceAndResourceNameFilter createResourceAndResourceNameFilter(UDF filter) throws PageException {
		return new UDFFilter(filter);
	}

	public static ResourceAndResourceNameFilter createResourceAndResourceNameFilter(String pattern) {

		if (!StringUtil.isEmpty(pattern, true)) return new WildcardPatternFilter(pattern, "|");

		return null;
	}
}