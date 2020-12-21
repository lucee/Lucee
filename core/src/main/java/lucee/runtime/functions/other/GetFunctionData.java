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
/**
 * Implements the CFML Function getfunctiondescription
 */
package lucee.runtime.functions.other;

import java.util.ArrayList;

import lucee.commons.lang.CFTypes;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.functions.system.CFFunction;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.FunctionArgument;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.function.FunctionLibFunction;
import lucee.transformer.library.function.FunctionLibFunctionArg;
import lucee.transformer.library.tag.TagLibFactory;

public final class GetFunctionData implements Function {
	private static final Collection.Key SOURCE = KeyConstants._source;
	private static final Collection.Key RETURN_TYPE = KeyImpl.intern("returnType");
	private static final Collection.Key ARGUMENT_TYPE = KeyImpl.intern("argumentType");
	private static final Collection.Key ARG_MIN = KeyImpl.intern("argMin");
	private static final Collection.Key ARG_MAX = KeyImpl.intern("argMax");
	static final Collection.Key INTRODUCED = KeyImpl.intern("introduced");

	public static Struct call(PageContext pc, String strFunctionName) throws PageException {
		return _call(pc, strFunctionName, pc.getCurrentTemplateDialect());
	}

	public static Struct call(PageContext pc, String strFunctionName, String strDialect) throws PageException {
		int dialect = ConfigWebUtil.toDialect(strDialect, -1);
		if (dialect == -1) throw new FunctionException(pc, "GetFunctionData", 2, "dialect", "value [" + strDialect + "] is invalid, valid values are [cfml,lucee]");

		return _call(pc, strFunctionName, dialect);
	}

	private static Struct _call(PageContext pc, String strFunctionName, int dialect) throws PageException {

		FunctionLib[] flds;
		flds = ((ConfigImpl) pc.getConfig()).getFLDs(dialect);

		FunctionLibFunction function = null;
		for (int i = 0; i < flds.length; i++) {
			function = flds[i].getFunction(strFunctionName.toLowerCase());
			if (function != null) break;
		}
		if (function == null) throw new ExpressionException("Function [" + strFunctionName + "] is not a built in function");

		// CFML Based Function
		Class clazz = null;
		try {
			clazz = function.getFunctionClassDefinition().getClazz();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		if (clazz == lucee.runtime.functions.system.CFFunction.class) {
			return cfmlBasedFunction(pc, function);
		}
		return javaBasedFunction(function);

	}

	private static Struct javaBasedFunction(FunctionLibFunction function) throws PageException {
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		sct.set(KeyConstants._name, function.getName());
		sct.set(KeyConstants._status, TagLibFactory.toStatus(function.getStatus()));
		if (function.getIntroduced() != null) sct.set(INTRODUCED, function.getIntroduced().toString());
		// else if(inside.equals("introduced")) att.setIntroduced(value);

		sct.set(KeyConstants._description, StringUtil.emptyIfNull(function.getDescription()));
		if (!ArrayUtil.isEmpty(function.getKeywords())) sct.set("keywords", Caster.toArray(function.getKeywords()));

		sct.set(RETURN_TYPE, StringUtil.emptyIfNull(function.getReturnTypeAsString()));
		sct.set(ARGUMENT_TYPE, StringUtil.emptyIfNull(function.getArgTypeAsString()));
		sct.set(ARG_MIN, Caster.toDouble(function.getArgMin()));
		sct.set(ARG_MAX, Caster.toDouble(function.getArgMax()));
		sct.set(KeyConstants._type, "java");
		String[] names = function.getMemberNames();
		if (!ArrayUtil.isEmpty(names) && function.getMemberType() != CFTypes.TYPE_UNKNOW) {
			StructImpl mem = new StructImpl(Struct.TYPE_LINKED);
			sct.set(KeyConstants._member, mem);
			mem.set(KeyConstants._name, names[0]);
			mem.set(KeyConstants._chaining, Caster.toBoolean(function.getMemberChaining()));
			mem.set(KeyConstants._type, function.getMemberTypeAsString());
			mem.set("position", Caster.toDouble(function.getMemberPosition()));

		}

		Array _args = new ArrayImpl();
		sct.set(KeyConstants._arguments, _args);
		if (function.getArgType() != FunctionLibFunction.ARG_DYNAMIC) {
			ArrayList<FunctionLibFunctionArg> args = function.getArg();
			for (int i = 0; i < args.size(); i++) {
				FunctionLibFunctionArg arg = args.get(i);
				Struct _arg = new StructImpl(Struct.TYPE_LINKED);
				_arg.set(KeyConstants._required, arg.getRequired() ? Boolean.TRUE : Boolean.FALSE);
				_arg.set(KeyConstants._type, StringUtil.emptyIfNull(arg.getTypeAsString()));
				_arg.set(KeyConstants._name, StringUtil.emptyIfNull(arg.getName()));
				_arg.set(KeyConstants._status, TagLibFactory.toStatus(arg.getStatus()));
				if (arg.getIntroduced() != null) _arg.set(INTRODUCED, arg.getIntroduced().toString());
				if (!StringUtil.isEmpty(arg.getAlias(), true)) _arg.set(KeyConstants._alias, arg.getAlias());

				_arg.set("defaultValue", arg.getDefaultValue());
				_arg.set(KeyConstants._description, StringUtil.toStringEmptyIfNull(arg.getDescription()));

				_args.append(_arg);
			}
		}
		return sct;
	}

	private static Struct cfmlBasedFunction(PageContext pc, FunctionLibFunction function) throws PageException {
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		ArrayList<FunctionLibFunctionArg> args = function.getArg();

		String filename = Caster.toString(args.get(0).getDefaultValue());
		Key name = KeyImpl.toKey(args.get(1).getDefaultValue());
		boolean isWeb = Caster.toBooleanValue(args.get(2).getDefaultValue());
		String mappingName = Caster.toString(args.get(3).getDefaultValue());
		UDF udf = CFFunction.loadUDF(pc, filename, mappingName, name, isWeb);

		sct.set(KeyConstants._name, function.getName());
		sct.set(ARGUMENT_TYPE, "fixed");
		sct.set(KeyConstants._description, StringUtil.emptyIfNull(udf.getHint()));
		sct.set(RETURN_TYPE, StringUtil.emptyIfNull(udf.getReturnTypeAsString()));
		sct.set(KeyConstants._type, "cfml");
		sct.set(SOURCE, udf.getSource());
		sct.set(KeyConstants._status, "implemented");

		FunctionArgument[] fas = udf.getFunctionArguments();
		Array _args = new ArrayImpl();
		sct.set(KeyConstants._arguments, _args);
		int min = 0, max = 0;
		for (int i = 0; i < fas.length; i++) {
			FunctionArgument fa = fas[i];
			Struct meta = fa.getMetaData();

			Struct _arg = new StructImpl(Struct.TYPE_LINKED);
			if (fa.isRequired()) min++;
			max++;
			_arg.set(KeyConstants._required, fa.isRequired() ? Boolean.TRUE : Boolean.FALSE);
			_arg.set(KeyConstants._type, StringUtil.emptyIfNull(fa.getTypeAsString()));
			_arg.set(KeyConstants._name, StringUtil.emptyIfNull(fa.getName()));
			_arg.set(KeyConstants._description, StringUtil.emptyIfNull(fa.getHint()));

			String status;
			if (meta == null) status = "implemented";
			else status = TagLibFactory.toStatus(TagLibFactory.toStatus(Caster.toString(meta.get(KeyConstants._status, "implemented"))));

			_arg.set(KeyConstants._status, status);

			_args.append(_arg);
		}
		sct.set(ARG_MIN, Caster.toDouble(min));
		sct.set(ARG_MAX, Caster.toDouble(max));

		return sct;
	}
}
