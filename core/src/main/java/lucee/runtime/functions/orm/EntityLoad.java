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
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.orm.ORMSession;
import lucee.runtime.orm.ORMUtil;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public class EntityLoad {

	public static Object call(PageContext pc, String name) throws PageException {

		ORMSession session = ORMUtil.getSession(pc);
		return session.loadAsArray(pc, name, new StructImpl());
	}

	public static Object call(PageContext pc, String name, Object idOrFilter) throws PageException {
		return call(pc, name, idOrFilter, Boolean.FALSE);
	}

	public static Object call(PageContext pc, String name, Object idOrFilter, Object uniqueOrOptions) throws PageException {
		ORMSession session = ORMUtil.getSession(pc);

		// id
		if (Decision.isSimpleValue(idOrFilter)) {
			// id,unique
			if (Decision.isCastableToBoolean(uniqueOrOptions)) {
				// id,unique=true
				if (Caster.toBooleanValue(uniqueOrOptions)) return session.load(pc, name, Caster.toString(idOrFilter));
				// id,unique=false
				return session.loadAsArray(pc, name, Caster.toString(idOrFilter));
			}
			else if (Decision.isString(uniqueOrOptions)) {
				return session.loadAsArray(pc, name, Caster.toString(idOrFilter), Caster.toString(uniqueOrOptions));
			}

			// id,options
			return session.loadAsArray(pc, name, Caster.toString(idOrFilter));
		}

		// filter,[unique|sortorder]
		if (Decision.isSimpleValue(uniqueOrOptions)) {
			// filter,unique
			if (Decision.isBoolean(uniqueOrOptions)) {
				if (Caster.toBooleanValue(uniqueOrOptions)) return session.load(pc, name, Caster.toStruct(idOrFilter));
				return session.loadAsArray(pc, name, Caster.toStruct(idOrFilter));
			}
			// filter,sortorder
			return session.loadAsArray(pc, name, Caster.toStruct(idOrFilter), (Struct) null, Caster.toString(uniqueOrOptions));
		}
		// filter,options
		return session.loadAsArray(pc, name, Caster.toStruct(idOrFilter), Caster.toStruct(uniqueOrOptions));
	}

	public static Object call(PageContext pc, String name, Object filter, Object order, Object options) throws PageException {
		ORMSession session = ORMUtil.getSession(pc);
		return session.loadAsArray(pc, name, Caster.toStruct(filter), Caster.toStruct(options), Caster.toString(order));
	}
}