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
package lucee.runtime.type;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lucee.commons.digest.Hash;
import lucee.commons.lang.CFTypes;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.component.MemberSupport;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.ref.Ref;
import lucee.runtime.interpreter.ref.cast.Casting;
import lucee.runtime.interpreter.ref.func.BIFCall;
import lucee.runtime.interpreter.ref.literal.LFunctionValue;
import lucee.runtime.interpreter.ref.literal.LString;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.UDFUtil;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.function.FunctionLibFunction;
import lucee.transformer.library.function.FunctionLibFunctionArg;

public class BIF extends MemberSupport implements UDFPlus {

	private final FunctionLibFunction flf;
	private short rtnType = CFTypes.TYPE_UNKNOW;
	private Component owner;
	private final ConfigPro cp;
	private FunctionArgument[] args;
	private String id;

	public static BIF getInstance(PageContext pc, String name, BIF defaultValue) {
		FunctionLib fl = ((ConfigPro) pc.getConfig()).getCombinedFLDs(pc.getCurrentTemplateDialect());
		FunctionLibFunction flf = fl.getFunction(name);

		// BIF not found
		if (flf == null) return defaultValue;
		return new BIF(pc.getConfig(), flf);
	}

	public BIF(PageContext pc, String name) throws ApplicationException {
		super(Component.ACCESS_PUBLIC);
		cp = (ConfigPro) pc.getConfig();
		FunctionLib fl = cp.getCombinedFLDs(pc.getCurrentTemplateDialect());
		flf = fl.getFunction(name);

		// BIF not found
		if (flf == null) {
			Key[] keys = CollectionUtil.toKeys(fl.getFunctions().keySet());
			String msg = ExceptionUtil.similarKeyMessage(keys, name, "Built in function", "Built in functions", null, false);
			String detail = ExceptionUtil.similarKeyMessage(keys, name, "Built in functions", null, false);
			throw new ApplicationException(msg, detail);
		}
		try {
			this.id = Hash.md5(name);
		}
		catch (NoSuchAlgorithmException e) {
			this.id = name;
		}
	}

	public BIF(Config config, FunctionLibFunction flf) {
		super(Component.ACCESS_PUBLIC);
		cp = (ConfigPro) config;
		this.flf = flf;
	}

	@Override
	public FunctionArgument[] getFunctionArguments() {
		if (args == null) {
			ArrayList<FunctionLibFunctionArg> src = flf.getArg();
			args = new FunctionArgument[src.size()];

			String def;
			int index = -1;
			FunctionLibFunctionArg arg;
			Iterator<FunctionLibFunctionArg> it = src.iterator();
			while (it.hasNext()) {
				arg = it.next();
				def = arg.getDefaultValue();
				args[++index] = new FunctionArgumentImpl(KeyImpl.init(arg.getName()), arg.getTypeAsString(), arg.getType(), arg.getRequired(),
						def == null ? FunctionArgument.DEFAULT_TYPE_NULL : FunctionArgument.DEFAULT_TYPE_LITERAL, true, arg.getName(), arg.getDescription(), null);
			}
		}

		return args;
	}

	@Override
	public Object callWithNamedValues(PageContext pageContext, Struct values, boolean doIncludePath) throws PageException {
		ArrayList<FunctionLibFunctionArg> flfas = flf.getArg();
		Iterator<FunctionLibFunctionArg> it = flfas.iterator();
		FunctionLibFunctionArg arg;
		Object val;

		List<Ref> refs = new ArrayList<Ref>();
		while (it.hasNext()) {
			arg = it.next();

			// match by name
			val = values.get(arg.getName(), null);

			// match by alias
			if (val == null) {
				String alias = arg.getAlias();
				if (!StringUtil.isEmpty(alias, true)) {
					String[] aliases = lucee.runtime.type.util.ListUtil.trimItems(lucee.runtime.type.util.ListUtil.listToStringArray(alias, ','));
					for (int x = 0; x < aliases.length; x++) {
						val = values.get(aliases[x], null);
						if (val != null) break;
					}
				}
			}

			if (val == null) {
				if (arg.getRequired()) {
					String[] names = flf.getMemberNames();
					String n = ArrayUtil.isEmpty(names) ? "" : names[0];
					throw new ExpressionException("Missing required argument [" + arg.getName() + "] for built in function call [" + n + "]");
				}
			}
			else {
				refs.add(new Casting(arg.getTypeAsString(), arg.getType(), new LFunctionValue(new LString(arg.getName()), val)));
			}
		}

		BIFCall call = new BIFCall(flf, refs.toArray(new Ref[refs.size()]));
		return call.getValue(pageContext);
	}

	@Override
	public Object call(PageContext pageContext, Object[] args, boolean doIncludePath) throws PageException {
		ArrayList<FunctionLibFunctionArg> flfas = flf.getArg();
		FunctionLibFunctionArg flfa;
		List<Ref> refs = new ArrayList<Ref>();
		for (int i = 0; i < args.length; i++) {
			if (i >= flfas.size()) throw new ApplicationException("Too many Attributes in function call [" + flf.getName() + "]");
			flfa = flfas.get(i);
			refs.add(new Casting(flfa.getTypeAsString(), flfa.getType(), args[i]));
		}
		BIFCall call = new BIFCall(flf, refs.toArray(new Ref[refs.size()]));
		return call.getValue(pageContext);
	}

	@Override
	public Object callWithNamedValues(PageContext pageContext, Key calledName, Struct values, boolean doIncludePath) throws PageException {
		return callWithNamedValues(pageContext, values, doIncludePath);
	}

	@Override
	public Object call(PageContext pageContext, Key calledName, Object[] args, boolean doIncludePath) throws PageException {
		return call(pageContext, args, doIncludePath);
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		DumpTable dt = (DumpTable) UDFUtil.toDumpData(pageContext, maxlevel, dp, this, UDFUtil.TYPE_BIF);
		// dt.setTitle(title);
		return dt;
	}

	@Override
	public UDF duplicate() {
		return new BIF(cp, flf);
	}

	@Override
	public Object duplicate(boolean deepCopy) {
		return duplicate();
	}

	@Override
	public Component getOwnerComponent() {
		return owner;
	}

	@Override
	public String getDisplayName() {
		return flf.getName();
	}

	@Override
	public String getHint() {
		return flf.getDescription();
	}

	@Override
	public String getFunctionName() {
		return flf.getName();
	}

	@Override
	public int getReturnType() {
		if (rtnType == CFTypes.TYPE_UNKNOW) rtnType = CFTypes.toShort(flf.getReturnTypeAsString(), false, CFTypes.TYPE_UNKNOW);
		return rtnType;
	}

	@Override
	public String getDescription() {
		return flf.getDescription();
	}

	@Override
	public void setOwnerComponent(Component owner) {
		this.owner = owner;
	}

	@Override
	public int getReturnFormat(int defaultFormat) {
		return getReturnFormat();
	}

	@Override
	public int getReturnFormat() {
		return UDF.RETURN_FORMAT_JSON;
	}

	@Override
	public String getReturnTypeAsString() {
		return flf.getReturnTypeAsString();
	}

	@Override
	public Object getValue() {
		return this;
	}

	@Override
	public boolean getOutput() {
		return false;
	}

	@Override
	public Object getDefaultValue(PageContext pc, int index) throws PageException {
		return null;
	}

	@Override
	public Boolean getSecureJson() {
		return null;
	}

	@Override
	public Boolean getVerifyClient() {
		return null;
	}

	/*
	 * @Override public PageSource getPageSource() { return null; }
	 */

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof UDF)) return false;
		return UDFImpl.equals(this, (UDF) other);
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public String getSource() {
		return "";
	}

	@Override
	public int getIndex() {
		return -1;
	}

	@Override
	public Object getDefaultValue(PageContext pc, int index, Object defaultValue) throws PageException {
		return null;
	}

	// MUST
	@Override
	public Struct getMetaData(PageContext pc) throws PageException {
		// TODO Auto-generated method stub
		return new StructImpl();
	}

	@Override
	public Object implementation(PageContext pageContext) throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PageSource getPageSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getBufferOutput(PageContext pc) {
		return pc.getApplicationContext().getBufferOutput();
	}

}
