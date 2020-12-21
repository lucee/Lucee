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
package lucee.runtime.functions.dynamicEvaluation;

import java.util.Iterator;
import java.util.Map.Entry;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.runtime.Component;
import lucee.runtime.ComponentScope;
import lucee.runtime.ComponentSpecificAccess;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.ComponentUtil;
import lucee.runtime.type.util.KeyConstants;

public final class EvaluateComponent {
	public static Object call(PageContext pc, String name, String md5, Struct sctThis) throws PageException {
		return invoke(pc, name, md5, sctThis, null);
	}

	public static Object call(PageContext pc, String name, String md5, Struct sctThis, Struct sctVariables) throws PageException {
		return invoke(pc, name, md5, sctThis, sctVariables);
	}

	public static Component invoke(PageContext pc, String name, String md5, Struct sctThis, Struct sctVariables) throws PageException {
		// Load comp
		Component comp = null;
		try {
			comp = pc.loadComponent(name);
			if (!ComponentUtil.md5(comp).equals(md5)) {
				LogUtil.log(ThreadLocalPageContext.getConfig(pc), Log.LEVEL_ERROR, EvaluateComponent.class.getName(), "component [" + name
						+ "] in this environment has not the same interface as the component to load, it is possible that one off the components has Functions added dynamically.");
			}
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		setInternalState(comp, sctThis, sctVariables);
		return comp;
	}

	public static void setInternalState(Component comp, Struct sctThis, Struct sctVariables) throws PageException {

		// this
		// delete this scope data members
		ComponentSpecificAccess cw = ComponentSpecificAccess.toComponentSpecificAccess(Component.ACCESS_PRIVATE, comp);
		Collection.Key[] cwKeys = CollectionUtil.keys(cw);
		Object member;
		for (int i = 0; i < cwKeys.length; i++) {
			member = cw.get(cwKeys[i]);
			if (member instanceof UDF) continue;
			cw.removeEL(cwKeys[i]);
		}

		// set this scope data members
		Iterator<Entry<Key, Object>> it = sctThis.entryIterator();
		Entry<Key, Object> e;
		// keys = sctThis.keys();
		while (it.hasNext()) {
			e = it.next();
			comp.set(e.getKey(), e.getValue());
		}

		// Variables

		ComponentScope scope = comp.getComponentScope();

		// delete variables scope data members
		Key[] sKeys = CollectionUtil.keys(scope);
		for (int i = 0; i < sKeys.length; i++) {
			if (KeyConstants._this.equals(sKeys[i])) continue;
			if (scope.get(sKeys[i]) instanceof UDF) continue;
			scope.removeEL(sKeys[i]);
		}

		// set variables scope data members
		it = sctVariables.entryIterator();
		// keys = sctVariables.keys();
		while (it.hasNext()) {
			e = it.next();
			scope.set(e.getKey(), e.getValue());
		}

	}
}