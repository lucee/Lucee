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
package lucee.runtime.functions.system;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.Mapping;
import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Collection;
import lucee.runtime.type.FunctionValue;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.scope.Variables;
import lucee.runtime.type.scope.VariablesImpl;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.KeyConstants;

public class CFFunction {

	private static final Variables VAR = new VariablesImpl();
	// private static Map udfs=new ReferenceMap();

	public static Object call(PageContext pc, Object[] objArr) throws PageException {
		if (objArr.length < 3) throw new ExpressionException("invalid call of a CFML Based built in function");

		// translate arguments
		String filename = Caster.toString((((FunctionValue) objArr[0]).getValue()));
		Collection.Key name = KeyImpl.toKey((((FunctionValue) objArr[1]).getValue()));
		boolean isweb = Caster.toBooleanValue((((FunctionValue) objArr[2]).getValue()));

		// function from archive may come without mapping definition
		int offset = 3;
		String mappingName = "mapping-function";
		// clearly no mapping definition
		if (objArr.length > 3 && objArr[3] instanceof FunctionValue) {
			FunctionValue fv = (FunctionValue) objArr[3];
			if (fv.getName().equals("__mapping")) {
				mappingName = Caster.toString(fv.getValue());
				offset = 4;
			}
		}

		UDF udf = loadUDF(pc, filename, mappingName, name, isweb);
		Struct meta = udf.getMetaData(pc);
		boolean callerScopes = (meta == null) ? false : Caster.toBooleanValue(meta.get("callerScopes", Boolean.FALSE), false);
		boolean caller = meta == null ? false : Caster.toBooleanValue(meta.get(KeyConstants._caller, Boolean.FALSE), false);

		Struct namedArguments = null, cs = null;
		if (callerScopes) {

			cs = new StructImpl();
			if (pc.undefinedScope().getCheckArguments()) {
				cs.set(KeyConstants._local, pc.localScope().duplicate(false));
				cs.set(KeyConstants._arguments, pc.argumentsScope().duplicate(false));
			}
		}

		Object[] arguments = null;
		if (objArr.length <= offset) arguments = ArrayUtil.OBJECT_EMPTY;
		else if (objArr[offset] instanceof FunctionValue) {
			FunctionValue fv;
			namedArguments = new StructImpl(Struct.TYPE_LINKED);
			if (callerScopes) namedArguments.setEL(KeyConstants._caller, cs);
			else if (caller) namedArguments.setEL(KeyConstants._caller, Duplicator.duplicate(pc.undefinedScope(), false));
			for (int i = offset; i < objArr.length; i++) {
				fv = toFunctionValue(name, objArr[i]);
				namedArguments.set(fv.getName(), fv.getValue());
			}
		}
		else {
			int off = (caller || callerScopes ? 3 : 4);
			arguments = new Object[objArr.length - off];
			if (callerScopes) arguments[0] = cs;
			else if (caller) arguments[0] = Duplicator.duplicate(pc.undefinedScope(), false);
			for (int i = offset; i < objArr.length; i++) {
				arguments[i - off] = toObject(name, objArr[i]);
			}
		}
		// execute UDF
		if (namedArguments == null) {
			return udf.call(pc, name, arguments, false);
		}

		return udf.callWithNamedValues(pc, name, namedArguments, false);
	}

	public static UDF loadUDF(PageContext pc, Resource res, Collection.Key name, boolean isweb, boolean cache) throws PageException {
		PageSource ps = pc.toPageSource(res, null);
		if (ps == null) throw new ExpressionException("could not load template [" + res + "]");
		return loadUDF(pc, ps, name, isweb, cache);
	}

	public static UDF loadUDF(PageContext pc, String filename, String mappingName, Collection.Key name, boolean isweb) throws PageException {
		ConfigWebPro config = (ConfigWebPro) pc.getConfig();
		Mapping mapping = isweb ? config.getFunctionMapping(mappingName) : config.getServerFunctionMapping(mappingName);
		return loadUDF(pc, mapping.getPageSource(filename), name, isweb, true);
	}

	public static UDF loadUDF(PageContext pc, PageSource ps, Collection.Key name, boolean isweb, boolean cache) throws PageException {
		ConfigWebPro config = (ConfigWebPro) pc.getConfig();
		String key = isweb ? name.getString() + config.getIdentification().getId() : name.getString();
		UDF udf = cache ? config.getFromFunctionCache(key) : null;
		if (udf != null) return udf;

		Page p = ps.loadPage(pc, false);

		// execute page
		Variables old = pc.variablesScope();
		pc.setVariablesScope(VAR);
		boolean wasSilent = pc.setSilent();
		try {
			p.call(pc);
			Object o = pc.variablesScope().get(name, null);
			if (o instanceof UDF) {
				udf = (UDF) o;
				if (cache) config.putToFunctionCache(key, udf);
				return udf;
			}
			throw new ExpressionException("there is no Function defined with name [" + name + "] in template [" + ps.getDisplayPath() + "]");
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}
		finally {
			pc.setVariablesScope(old);
			if (!wasSilent) pc.unsetSilent();
		}
	}

	private static FunctionValue toFunctionValue(Collection.Key name, Object obj) throws ExpressionException {
		if (obj instanceof FunctionValue) return (FunctionValue) obj;
		throw new ExpressionException("invalid argument for function " + name + ", you can not mix named and unnamed arguments");
	}

	private static Object toObject(Collection.Key name, Object obj) throws ExpressionException {
		if (obj instanceof FunctionValue) throw new ExpressionException("invalid argument for function " + name + ", you can not mix named and unnamed arguments");
		return obj;
	}
}