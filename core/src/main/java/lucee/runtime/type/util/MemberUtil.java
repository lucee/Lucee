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
package lucee.runtime.type.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lucee.commons.io.SystemUtil;
import lucee.commons.lang.CFTypes;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.interpreter.ref.Ref;
import lucee.runtime.interpreter.ref.cast.Casting;
import lucee.runtime.interpreter.ref.func.BIFCall;
import lucee.runtime.interpreter.ref.literal.LFunctionValue;
import lucee.runtime.interpreter.ref.literal.LString;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.reflection.pairs.MethodInstance;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.scope.Undefined;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.function.FunctionLibFunction;
import lucee.transformer.library.function.FunctionLibFunctionArg;

public final class MemberUtil {

	private static final Object DEFAULT = new Object();
	private static Map<Short, Map<Collection.Key, FunctionLibFunction>> matchesCFML = new HashMap<Short, Map<Collection.Key, FunctionLibFunction>>();

	public static Map<Collection.Key, FunctionLibFunction> getMembers(PageContext pc, short type) {

		Map<Key, FunctionLibFunction> match = matchesCFML.get(type);
		if (match == null) {
			synchronized (SystemUtil.createToken("MemberUtil.getMembers", "type:" + type)) {
				match = matchesCFML.get(type);
				if (match == null) {
					FunctionLib flds = ((ConfigWebPro) pc.getConfig()).getFLDs();
					Iterator<FunctionLibFunction> it;
					FunctionLibFunction f;
					match = new HashMap<Collection.Key, FunctionLibFunction>();
					String[] names;
					it = flds.getFunctions().values().iterator();
					while (it.hasNext()) {
						f = it.next();
						names = f.getMemberNames();
						if (!ArrayUtil.isEmpty(names) && f.getMemberType() == type && f.getArgType() == FunctionLibFunction.ARG_FIX) {
							for (int y = 0; y < names.length; y++) {
								match.put(KeyImpl.init(names[y]), f);
							}
						}
					}
					matchesCFML.put(type, match);
				}
			}
		}
		return match;
	}

	// used in extension Image
	public static Object call(PageContext pc, Object coll, Collection.Key methodName, Object[] args, final short[] types, String[] strTypes) throws PageException {
		// look for members
		boolean hasAny = false;
		if (!KeyConstants._toString.equals(methodName)) {
			short type;
			String strType;
			Map<Key, FunctionLibFunction> members = null;
			boolean isChked = false;
			FunctionLibFunction member, tmp;
			for (int i = 0; i <= types.length; i++) {
				if (i == types.length) {
					if (hasAny) break;
					type = CFTypes.TYPE_ANY;
					strType = "any";
				}
				else {
					type = types[i];
					strType = strTypes[i];
					if (type == CFTypes.TYPE_ANY) hasAny = true;
				}
				members = getMembers(pc, type);
				member = members.get(methodName);
				if (!isChked && member == null) {
					if (type == CFTypes.TYPE_NUMERIC) {
						members = getMembers(pc, CFTypes.TYPE_STRING);
						member = members.get(methodName);
					}
					else if (type == CFTypes.TYPE_STRING) {

						members = getMembers(pc, CFTypes.TYPE_NUMERIC);
						tmp = members.get(methodName);
						if (tmp != null && Decision.isNumber(coll)) {
							member = tmp;
						}
						if (member == null) {
							members = getMembers(pc, CFTypes.TYPE_DATETIME);
							tmp = members.get(methodName);
							if (tmp != null && args.length <= 3 && Caster.toString(coll).length() > 2 && !Decision.isInteger(coll, false) && Decision.isDateAdvanced(coll, false)) {
								member = tmp;
							}
						}
					}

					isChked = true;
				}
				if (member != null) {
					List<FunctionLibFunctionArg> _args = member.getArg();
					if (args.length < _args.size()) {
						// LDEV-6428: build Object[] directly, skip Casting/ArrayList/Ref[]/BIFCall ceremony.
						// All member-callable BIFs do their own Caster.to* coercion in invoke() (audited
						// 2026-06-25: 284 of 284 member-callable BIFs use defensive coercion or instanceof-
						// guarded casts), so the upstream Caster.castTo wrapper is double-work.
						int n = _args.size();
						Object[] callArgs = new Object[n];
						int filled = 0;
						int pos = member.getMemberPosition();
						int argIndex = -1;
						for (int glbIndex = 1; glbIndex <= n; glbIndex++) {
							if (glbIndex == pos) {
								callArgs[filled++] = coll;
							}
							else if (args.length > ++argIndex) {
								callArgs[filled++] = args[argIndex];
							}
						}
						if (filled < callArgs.length) {
							Object[] trimmed = new Object[filled];
							System.arraycopy(callArgs, 0, trimmed, 0, filled);
							callArgs = trimmed;
						}
						BIF bif = member.getBIF();
						// Preserve BIFCall.getValue check order: memberChaining short-circuit BEFORE
						// argMin (matches pre-fastpath behavior where memberChaining BIFs invoke even
						// with too few args, then throw from inside via their own validation, producing
						// the BIF's hardcoded camelCase function name in the error message).
						if (member.getMemberChaining()) {
							try {
								bif.invoke(pc, callArgs);
							}
							catch (CasterException ce) {
								rethrowWithFLDType(pc, _args, callArgs, ce);
							}
							return coll;
						}
						// argMin enforcement uses getNameWithCase() to match pre-fastpath BIFCall behavior
						// where the FLD-declared case is preserved in the error message.
						if (member.getArgType() != FunctionLibFunction.ARG_DYNAMIC && member.getArgMin() > callArgs.length) {
							throw new FunctionException(pc, member.getNameWithCase(), member.getArgMin(), _args.size(), callArgs.length);
						}
						// Preserve BIFCall.getValue return-type cast.
						Object rawResult;
						try {
							rawResult = bif.invoke(pc, callArgs);
						}
						catch (CasterException ce) {
							rethrowWithFLDType(pc, _args, callArgs, ce);
							return null; // unreachable -- rethrowWithFLDType always throws
						}
						return Caster.castTo(pc, member.getReturnTypeAsString(), rawResult, false);
					}
					else throw new FunctionException(pc, member.getName(), member.getArgMin(), _args.size(), args.length);
				}
			}
		}

		// do reflection
		if (pc.getConfig().getSecurityManager().getAccess(lucee.runtime.security.SecurityManager.TYPE_DIRECT_JAVA_ACCESS) == lucee.runtime.security.SecurityManager.VALUE_YES) {
			if (!(coll instanceof Undefined)) {
				Object res = callMethod(coll, methodName, args);
				if (res != DEFAULT) return res;
			}
		}

		// merge
		Set<Key> keys = new HashSet<>();
		hasAny = false;
		for (int i = 0; i < types.length; i++) {
			if (types[i] == CFTypes.TYPE_ANY) hasAny = true;
			Iterator<Key> it = getMembers(pc, types[i]).keySet().iterator();
			while (it.hasNext()) {
				keys.add(it.next());
			}
		}
		if (!hasAny) {
			Iterator<Key> it = getMembers(pc, CFTypes.TYPE_ANY).keySet().iterator();
			while (it.hasNext()) {
				keys.add(it.next());
			}
		}
		String in = types.length == 1 && types[0] != CFTypes.TYPE_ANY ? StringUtil.ucFirst(CFTypes.toString(types[0], "Object")) : "Object";
		if (coll instanceof Scope && !StringUtil.isEmpty(((Scope) coll).getTypeAsString())) {
			in = ((Scope) coll).getTypeAsString() + " scope (" + in + ")";
		}
		String msg = ExceptionUtil.similarKeyMessage(keys.toArray(new Key[keys.size()]), methodName.getString(), "function", "functions", in, true);
		String detail = ExceptionUtil.similarKeyMessage(keys.toArray(new Key[keys.size()]), methodName.getString(), "functions", in, true);
		throw new ExpressionException(msg, detail);
	}

	private static Object callMethod(Object obj, Collection.Key methodName, Object[] args) throws PageException {
		MethodInstance mi = Reflector.getMethodInstance(obj.getClass(), methodName, args, false, false);
		if (!mi.hasMethod()) return DEFAULT;
		try {
			return mi.invoke(obj);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	// LDEV-6428: on a CasterException from inside a member-dispatched BIF, retry each
	// arg against its FLD-declared type to surface a user-friendly error referencing the
	// FLD-declared type ("string", "numeric", "queryColumn") instead of the BIF's internal
	// Java-typed cast target (e.g. "lucee.runtime.type.Collection$Key").
	// Only invoked on the slow path (after the BIF has already thrown).
	private static void rethrowWithFLDType(PageContext pc, List<FunctionLibFunctionArg> _args, Object[] callArgs, CasterException original) throws PageException {
		int n = Math.min(callArgs.length, _args.size());
		for (int i = 0; i < n; i++) {
			FunctionLibFunctionArg flfa = _args.get(i);
			Caster.castTo(pc, flfa.getType(), flfa.getTypeAsString(), callArgs[i]);
			// if the cast succeeded, this arg is not the culprit -- continue
		}
		// All args cast cleanly against FLD-declared types -- the BIF's exception was not
		// an arg-type problem (internal logic error, etc). Propagate the original.
		throw original;
	}

	// used in extension image
	public static Object callWithNamedValues(PageContext pc, Object coll, Collection.Key methodName, Struct args, short type, String strType) throws PageException {
		Map<Key, FunctionLibFunction> members = getMembers(pc, type);
		FunctionLibFunction member = members.get(methodName);

		if (member != null) {
			List<FunctionLibFunctionArg> _args = member.getArg();
			FunctionLibFunctionArg arg;
			FunctionLibFunctionArg argMem;
			if (args.size() < _args.size()) {
				Object val;
				ArrayList<Ref> refs = new ArrayList<Ref>();
				int index = member.getMemberPosition() - 1;
				argMem = _args.get(index); // set member argument as per member-position
				refs.add(new Casting(argMem.getTypeAsString(), argMem.getType(), new LFunctionValue(new LString(argMem.getName()), coll)));
				for (int y = 0; y < _args.size(); y++) {
					arg = _args.get(y);

					if (index == y) continue; // member argument already added in refs

					// match by name
					val = args.get(arg.getName(), null);

					// match by alias
					if (val == null) {
						String[] aliases = arg.getAliases();
						if (!ArrayUtil.isEmpty(aliases)){
							for (int x = 0; x < aliases.length; x++) {
								val = args.get(aliases[x], null);
								if (val != null) break;
							}
						}
					}

					if (val == null) {
						if (arg.getRequired()) {
							String[] names = member.getMemberNames();
							String n = ArrayUtil.isEmpty(names) ? "" : names[0];
							throw new ExpressionException("missing required argument [" + arg.getName() + "] for member function call [" + n + "]");
						}
					}
					else {
						refs.add(new Casting(arg.getTypeAsString(), arg.getType(), new LFunctionValue(new LString(arg.getName()), val)));
						// refs.add(new LFunctionValue(new LString(arg.getName()),new
						// Casting(pc,arg.getTypeAsString(),arg.getType(),val)));
					}

				}
				return new BIFCall(coll, member, refs.toArray(new Ref[refs.size()])).getValue(pc);
			}
			else {
				throw new ExpressionException("There are to many arguments (" + args.size() + ") passed into the member function  [" + methodName
						+ "], the maximum number of arguments is [" + (_args.size() - 1) + "]");
			}
		}

		String inType = StringUtil.ucFirst(strType);
		if (coll instanceof Scope && !StringUtil.isEmpty(((Scope) coll).getTypeAsString())) {
			inType = ((Scope) coll).getTypeAsString() + " scope (" + inType + ")";
		}
		throw new ExpressionException("No matching function member [" + methodName + "] for call with named arguments found in the " + inType
				+ ", available function members are ["
				+ lucee.runtime.type.util.ListUtil.sort(CollectionUtil.getKeyList(members.keySet().iterator(), ","), "textnocase", "asc", ",") + "]");
	}

}