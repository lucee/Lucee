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

import java.util.Iterator;
import java.util.Map.Entry;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.orm.ORMSession;
import lucee.runtime.orm.ORMUtil;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.KeyConstants;

public class EntityNew {

	public static Object call(PageContext pc, String name) throws PageException {
		return call(pc, name, null);
	}

	public static Object call(PageContext pc, String name, Struct properties) throws PageException {
		ORMSession session = ORMUtil.getSession(pc);
		if (properties == null) return session.create(pc, name);

		Component entity = session.create(pc, name);
		setPropeties(pc, entity, properties, false);
		return entity;

	}

	public static void setPropeties(PageContext pc, Component c, Struct properties, boolean ignoreNotExisting) throws PageException {
		if (properties == null) return;

		// argumentCollection
		if (properties.size() == 1 && properties.containsKey(KeyConstants._argumentCollection) && !c.containsKey(KeyConstants._setArgumentCollection)) {
			properties = Caster.toStruct(properties.get(KeyConstants._argumentCollection));
		}

		Iterator<Entry<Key, Object>> it = properties.entryIterator();
		Entry<Key, Object> e;
		while (it.hasNext()) {
			e = it.next();
			Key funcName = KeyImpl.init("set" + e.getKey().getString());
			if (ignoreNotExisting) {
				if (c.get(funcName, null) instanceof UDF) c.call(pc, funcName, new Object[] { e.getValue() });
			}
			else {
				c.call(pc, funcName, new Object[] { e.getValue() });
			}
		}
	}
}