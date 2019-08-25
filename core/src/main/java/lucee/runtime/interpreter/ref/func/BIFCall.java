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
package lucee.runtime.interpreter.ref.func;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lucee.commons.lang.CFTypes;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.interpreter.InterpreterException;
import lucee.runtime.interpreter.ref.Ref;
import lucee.runtime.interpreter.ref.RefSupport;
import lucee.runtime.interpreter.ref.cast.Casting;
import lucee.runtime.interpreter.ref.literal.LFunctionValue;
import lucee.runtime.interpreter.ref.util.RefUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Constants;
import lucee.runtime.type.FunctionValue;
import lucee.runtime.type.FunctionValueImpl;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.UDFUtil;
import lucee.transformer.library.function.FunctionLibFunction;
import lucee.transformer.library.function.FunctionLibFunctionArg;

/**
 * a built In Function call
 *
 *
 */
public final class BIFCall extends RefSupport implements Ref {

	private Ref[] refArgs;
	private FunctionLibFunction flf;
	private Object obj;

	/**
	 * constructor of the class
	 * 
	 * @param pc
	 * @param flf
	 * @param refArgs
	 */
	public BIFCall(FunctionLibFunction flf, Ref[] refArgs) {
		this.flf = flf;
		this.refArgs = refArgs;
	}

	public BIFCall(Object obj, FunctionLibFunction flf, Ref[] refArgs) {
		this.obj = obj;
		this.flf = flf;
		this.refArgs = refArgs;
	}

	@Override
	public Object getValue(PageContext pc) throws PageException {

		Object[] arguments = null;

		if (isDynamic()) {
			arguments = RefUtil.getValue(pc, refArgs);
			if (flf.hasDefaultValues()) {
				List<Object> tmp = new ArrayList<Object>();
				ArrayList<FunctionLibFunctionArg> args = flf.getArg();
				Iterator<FunctionLibFunctionArg> it = args.iterator();
				FunctionLibFunctionArg arg;
				while (it.hasNext()) {
					arg = it.next();
					if (arg.getDefaultValue() != null) tmp.add(new FunctionValueImpl(arg.getName(), arg.getDefaultValue()));
				}
				for (int i = 0; i < arguments.length; i++) {
					tmp.add(arguments[i]);
				}
				arguments = tmp.toArray();
			}
			arguments = new Object[] { arguments };
		}
		else {
			if (isNamed(pc, refArgs)) {
				FunctionValue[] fvalues = getFunctionValues(pc, refArgs);
				String[] names = getNames(fvalues);

				ArrayList<FunctionLibFunctionArg> list = flf.getArg();
				Iterator<FunctionLibFunctionArg> it = list.iterator();
				arguments = new Object[list.size()];

				FunctionLibFunctionArg flfa;
				int index = 0;
				VT vt;
				while (it.hasNext()) {
					flfa = it.next();
					vt = getMatchingValueAndType(flfa, fvalues, names);
					if (vt.index != -1) names[vt.index] = null;
					arguments[index++] = new Casting(vt.type, CFTypes.toShort(vt.type, false, CFTypes.TYPE_UNKNOW), vt.value).getValue(pc);
				}

				for (int y = 0; y < names.length; y++) {
					if (names[y] != null) {
						ExpressionException ee = new InterpreterException("argument [" + names[y] + "] is not allowed for function [" + flf.getName() + "]");
						UDFUtil.addFunctionDoc(ee, flf);
						throw ee;
					}
				}

			}
			else {
				arguments = RefUtil.getValue(pc, refArgs);
			}
		}
		BIF bif = flf.getBIF();

		if (flf.getMemberChaining() && obj != null) {
			bif.invoke(pc, arguments);
			return obj;
		}

		if (!isDynamic() && flf.getArgMin() > arguments.length) {
			throw new FunctionException(pc, flf.getName(), flf.getArgMin(), flf.getArgMax(), arguments.length);
		}

		return Caster.castTo(pc, flf.getReturnTypeAsString(), bif.invoke(pc, arguments), false);
	}

	private VT getMatchingValueAndType(FunctionLibFunctionArg flfa, FunctionValue[] fvalues, String[] names) throws ExpressionException {
		String flfan = flfa.getName();

		// first search if an argument match
		for (int i = 0; i < names.length; i++) {
			if (names[i] != null && names[i].equalsIgnoreCase(flfan)) {
				return new VT(fvalues[i].getValue(), flfa.getTypeAsString(), i);
			}
		}

		// then check if an alias match
		String alias = flfa.getAlias();
		if (!StringUtil.isEmpty(alias)) {
			for (int i = 0; i < names.length; i++) {
				if (names[i] != null && lucee.runtime.type.util.ListUtil.listFindNoCase(alias, names[i], ",") != -1) {
					return new VT(fvalues[i].getValue(), flfa.getTypeAsString(), i);
				}
			}
		}

		// if not required return the default value
		if (!flfa.getRequired()) {
			String defaultValue = flfa.getDefaultValue();
			String type = flfa.getTypeAsString().toLowerCase();

			if (defaultValue == null) {
				if (type.equals("boolean") || type.equals("bool")) return new VT(Boolean.FALSE, type, -1);
				if (type.equals("number") || type.equals("numeric") || type.equals("double")) return new VT(Constants.DOUBLE_ZERO, type, -1);
				return new VT(null, type, -1);
			}
			return new VT(defaultValue, type, -1);

		}
		ExpressionException ee = new InterpreterException("missing required argument [" + flfan + "] for function [" + flfa.getFunction().getName() + "]");
		UDFUtil.addFunctionDoc(ee, flfa.getFunction());
		throw ee;
	}

	private String[] getNames(FunctionValue[] fvalues) {
		String[] names = new String[fvalues.length];
		for (int i = 0; i < fvalues.length; i++) {
			names[i] = fvalues[i].getNameAsString();
		}
		return names;
	}

	private FunctionValue[] getFunctionValues(PageContext pc, Ref[] refArgs) throws PageException {
		FunctionValue[] fvalues = new FunctionValue[refArgs.length];
		for (int i = 0; i < refArgs.length; i++) {
			fvalues[i] = (FunctionValue) ((LFunctionValue) ((Casting) refArgs[i]).getRef()).getValue(pc);
		}
		return fvalues;
	}

	private boolean isNamed(PageContext pc, Ref[] refArgs) throws PageException {
		if (ArrayUtil.isEmpty(refArgs)) return false;
		Casting cast;
		int count = 0;
		for (int i = 0; i < refArgs.length; i++) {
			if (refArgs[i] instanceof Casting) {
				cast = (Casting) refArgs[i];
				if (cast.getRef() instanceof LFunctionValue && ((LFunctionValue) cast.getRef()).getValue(pc) instanceof FunctionValue) {
					count++;
				}
			}
		}
		if (count != 0 && count != refArgs.length) {
			ExpressionException ee = new InterpreterException("invalid argument for function " + flf.getName() + ", you can not mix named and unnamed arguments");
			UDFUtil.addFunctionDoc(ee, flf);
			throw ee;
		}
		return count != 0;
	}

	private boolean isDynamic() {
		return flf.getArgType() == FunctionLibFunction.ARG_DYNAMIC;
	}

	@Override
	public String getTypeName() {
		return "built in function";
	}

}

class VT {

	Object value;
	String type;
	int index;

	public VT(Object value, String type, int index) {
		this.value = value;
		this.type = type;
		this.index = index;
	}

}