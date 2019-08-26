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

import java.util.List;
import java.util.Map;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.orm.ORMSession;
import lucee.runtime.orm.ORMUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;

public class ORMExecuteQuery {

	public static Object call(PageContext pc, String hql) throws PageException {
		return _call(pc, hql, null, false, null);
	}

	public static Object call(PageContext pc, String hql, Object paramsOrUnique) throws PageException {
		if (Decision.isCastableToBoolean(paramsOrUnique)) {
			return _call(pc, hql, null, Caster.toBooleanValue(paramsOrUnique), null);
		}
		return _call(pc, hql, paramsOrUnique, false, null);
	}

	public static Object call(PageContext pc, String hql, Object paramsOrUnique, Object uniqueOrQueryOptions) throws PageException {
		if (Decision.isCastableToBoolean(paramsOrUnique)) {
			return _call(pc, hql, null, Caster.toBooleanValue(paramsOrUnique), Caster.toStruct(uniqueOrQueryOptions));
		}
		if (Decision.isCastableToBoolean(uniqueOrQueryOptions)) {
			return _call(pc, hql, paramsOrUnique, Caster.toBooleanValue(uniqueOrQueryOptions), null);
		}
		return _call(pc, hql, paramsOrUnique, false, Caster.toStruct(uniqueOrQueryOptions));
	}

	public static Object call(PageContext pc, String hql, Object params, Object unique, Object queryOptions) throws PageException {
		return _call(pc, hql, params, Caster.toBooleanValue(unique), Caster.toStruct(queryOptions));
	}

	private static Object _call(PageContext pc, String hql, Object params, boolean unique, Struct queryOptions) throws PageException {
		ORMSession session = ORMUtil.getSession(pc);
		String dsn = null;
		if (queryOptions != null) dsn = Caster.toString(queryOptions.get(KeyConstants._datasource, null), null);
		if (StringUtil.isEmpty(dsn, true)) dsn = ORMUtil.getDefaultDataSource(pc).getName();

		if (params == null) return toCFML(session.executeQuery(pc, dsn, hql, new ArrayImpl(), unique, queryOptions));
		else if (Decision.isStruct(params)) return toCFML(session.executeQuery(pc, dsn, hql, Caster.toStruct(params), unique, queryOptions));
		else if (Decision.isArray(params)) return toCFML(session.executeQuery(pc, dsn, hql, Caster.toArray(params), unique, queryOptions));
		else if (Decision.isCastableToStruct(params)) return toCFML(session.executeQuery(pc, dsn, hql, Caster.toStruct(params), unique, queryOptions));
		else if (Decision.isCastableToArray(params)) return toCFML(session.executeQuery(pc, dsn, hql, Caster.toArray(params), unique, queryOptions));
		else throw new FunctionException(pc, "ORMExecuteQuery", 2, "params", "cannot convert the params to an array or a struct");

	}

	private static Object toCFML(Object obj) throws PageException {
		if (obj instanceof List<?> && !(obj instanceof Array)) return Caster.toArray(obj);
		if (obj instanceof Map<?, ?> && !(obj instanceof Struct)) return Caster.toStruct(obj, false);
		return obj;
	}
}