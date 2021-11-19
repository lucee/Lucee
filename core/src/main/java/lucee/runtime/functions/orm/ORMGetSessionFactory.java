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
package lucee.runtime.functions.orm;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.orm.ORMUtil;

public class ORMGetSessionFactory extends BIF {

	private static final long serialVersionUID = -8739815940242857106L;

	public static Object call(PageContext pc) throws PageException {
		return call(pc, null);
	}

	public static Object call(PageContext pc, String datasource) throws PageException {
		String dsn = ORMUtil.getDataSource(pc, datasource).getName();
		return ORMUtil.getSession(pc).getRawSessionFactory(dsn);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 0) return call(pc);
		return call(pc, Caster.toString(args[0]));
	}
}