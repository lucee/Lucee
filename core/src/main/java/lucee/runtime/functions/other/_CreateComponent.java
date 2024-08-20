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
package lucee.runtime.functions.other;

import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.runtime.Component;
import lucee.runtime.ComponentImpl;
import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.component.ComponentLoader;
import lucee.runtime.component.ImportDefintion;
import lucee.runtime.component.ImportDefintionImpl;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.functions.orm.EntityNew;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.FunctionValue;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.KeyConstants;

public class _CreateComponent {

	private static final Object[] EMPTY = new Object[0];
	private static final ImportDefintion JAVA_LANG = new ImportDefintionImpl("java.lang", "*");
	private static ImportDefintion[] EMPTY_ID = new ImportDefintion[0];

	public static Object call(PageContext pc, Object[] objArr) throws PageException {
		String path = Caster.toString(objArr[objArr.length - 1]);
		// not store the index to make it faster
		Component cfc = ComponentLoader.searchComponent(pc, null, path, null, null, false, true, true, false);
		Class cls = null;
		if (cfc == null) {
			// no package
			if (path.indexOf('.') == -1) {
				ImportDefintion[] imports = getImportDefintions(pc);
				ImportDefintion id;
				for (int i = 0; i <= imports.length; i++) {
					id = i == imports.length ? JAVA_LANG : imports[i];
					if ("*".equals(id.getName()) || path.equals(id.getName())) {
						try {// TODO do method with defaultValue
							cls = ClassUtil.loadClass(pc, id.getPackage() + "." + path);
							break;
						}
						catch (Exception e) {

						}
					}
				}

			}
			if (cls == null) {
				try {
					cls = ClassUtil.loadClass(pc, path);
				}
				catch (Exception e) {
					cfc = ComponentLoader.searchComponent(pc, null, path, null, null, false, true, true, true);
					// ApplicationException ae = new ApplicationException("could not find component or class with name
					// [" + path + "]");
					// ExceptionUtil.initCauseEL(ae, e);
					// throw ae;
				}
			}

		}

		// no init method
		if (cfc != null && !(cfc.get(pc, KeyConstants._init, null) instanceof UDF)) {

			if (objArr.length > 1) { // we have arguments passed in
				Object arg1 = objArr[0];
				if (arg1 instanceof FunctionValue) {
					Struct args = Caster.toFunctionValues(objArr, 0, objArr.length - 1);
					EntityNew.setPropeties(pc, cfc, args, true);
				}
				else if (Decision.isStruct(arg1) && !Decision.isComponent(arg1) && objArr.length == 2) { // we only do this in case there is only argument set, otherwise we assume
					// that this is simply a missuse of the new operator
					Struct args = Caster.toStruct(arg1);
					EntityNew.setPropeties(pc, cfc, args, true);
				}
			}

			return cfc;
		}

		Object rtn;
		// no arguments
		if (objArr.length == 1) {
			if (cfc != null) rtn = cfc.call(pc, KeyConstants._init, EMPTY);
			else {
				try {
					rtn = ClassUtil.loadInstance(cls);
				}
				catch (ClassException e) {
					throw Caster.toPageException(e);
				}
			}
		}
		// named arguments
		else if (objArr[0] instanceof FunctionValue) {
			if (cfc == null) throw new ApplicationException("named arguments are not supported with classes.");
			Struct args = Caster.toFunctionValues(objArr, 0, objArr.length - 1);
			rtn = cfc.callWithNamedValues(pc, KeyConstants._init, args);
		}
		// no name arguments
		else {
			Object[] args = new Object[objArr.length - 1];
			for (int i = 0; i < objArr.length - 1; i++) {
				args[i] = objArr[i];
				if (args[i] instanceof FunctionValue)
					throw new ExpressionException("invalid argument definition, when using named parameters to a function, every parameter must have a name.");
			}
			if (cfc != null) rtn = cfc.call(pc, KeyConstants._init, args);
			else {
				try {
					rtn = ClassUtil.loadInstance(cls, args);
				}
				catch (Exception e) {
					throw Caster.toPageException(e);
				}
			}

		}
		if (rtn == null && cfc != null) {
			return cfc;
		}

		return rtn;
	}

	private static ImportDefintion[] getImportDefintions(PageContext pc) {
		PageSource currPS = pc.getCurrentPageSource(null);

		ImportDefintion[] importDefintions = null;
		if (currPS != null) {
			Page currP;
			Component cfc = pc.getActiveComponent();
			if (cfc instanceof ComponentImpl && currPS.equals(cfc.getPageSource())) {
				importDefintions = ((ComponentImpl) cfc)._getImportDefintions();
			}
			else if ((currP = currPS.loadPage(pc, false, null)) != null) {
				importDefintions = currP.getImportDefintions();
			}
		}
		if (importDefintions == null) return EMPTY_ID;
		return importDefintions;
	}

}