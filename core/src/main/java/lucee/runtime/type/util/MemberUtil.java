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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.ref.Ref;
import lucee.runtime.interpreter.ref.cast.Casting;
import lucee.runtime.interpreter.ref.func.BIFCall;
import lucee.runtime.interpreter.ref.literal.LFunctionValue;
import lucee.runtime.interpreter.ref.literal.LString;
import lucee.runtime.op.Caster;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.reflection.pairs.MethodInstance;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.scope.Undefined;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.function.FunctionLibFunction;
import lucee.transformer.library.function.FunctionLibFunctionArg;

public class MemberUtil {

	private static final Object DEFAULT = new Object();
	private static Map<Short, Map<Collection.Key, FunctionLibFunction>> matchesLucee = new HashMap<Short, Map<Collection.Key, FunctionLibFunction>>();
	private static Map<Short, Map<Collection.Key, FunctionLibFunction>> matchesCFML = new HashMap<Short, Map<Collection.Key, FunctionLibFunction>>();

	public static Map<Collection.Key, FunctionLibFunction> getMembers(PageContext pc, short type) {
		Map<Short, Map<Key, FunctionLibFunction>> matches = pc.getCurrentTemplateDialect() == CFMLEngine.DIALECT_LUCEE ? matchesLucee : matchesCFML;

		Map<Key, FunctionLibFunction> match = matches.get(type);
		if (match != null) return match;

		FunctionLib[] flds = ((ConfigWebImpl) pc.getConfig()).getFLDs(pc.getCurrentTemplateDialect());
		Iterator<FunctionLibFunction> it;
		FunctionLibFunction f;
		match = new HashMap<Collection.Key, FunctionLibFunction>();
		String[] names;
		for (int i = 0; i < flds.length; i++) {
			it = flds[i].getFunctions().values().iterator();
			while (it.hasNext()) {
				f = it.next();
				names = f.getMemberNames();
				if (!ArrayUtil.isEmpty(names) && f.getMemberType() == type && f.getArgType() == FunctionLibFunction.ARG_FIX) {
					for (int y = 0; y < names.length; y++)

						match.put(KeyImpl.getInstance(names[y]), f);
				}
			}
		}
		matches.put(type, match);
		return match;
	}

	// used in extension Image
	public static Object call(PageContext pc, Object coll, Collection.Key methodName, Object[] args, short[] types, String[] strTypes) throws PageException {

		// look for members
		short type;
		String strType;
		Map<Key, FunctionLibFunction> members = null;
		for (int i = 0; i < types.length; i++) {
			type = types[i];
			strType = strTypes[i];
			members = getMembers(pc, type);
			FunctionLibFunction member = members.get(methodName);
			if (member != null) {
				List<FunctionLibFunctionArg> _args = member.getArg();
				if (args.length < _args.size()) {
					ArrayList<Ref> refs = new ArrayList<Ref>();

					int pos = member.getMemberPosition();
					FunctionLibFunctionArg flfa;
					Iterator<FunctionLibFunctionArg> it = _args.iterator();
					int glbIndex = 0, argIndex = -1;
					while (it.hasNext()) {
						glbIndex++;
						flfa = it.next();
						if (glbIndex == pos) {
							refs.add(new Casting(strType, type, coll));
						}
						else if (args.length > ++argIndex) { // careful, argIndex is only incremented when condition above is false
							refs.add(new Casting(flfa.getTypeAsString(), flfa.getType(), args[argIndex]));
						}
					}
					return new BIFCall(coll, member, refs.toArray(new Ref[refs.size()])).getValue(pc);
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
		if (types.length > 1) {
			Map<Key, FunctionLibFunction> tmp;
			members = null;
			for (int i = 0; i < types.length; i++) {
				tmp = getMembers(pc, types[i]);
				if (members == null) members = tmp;
				else {
					Iterator<Entry<Key, FunctionLibFunction>> it = tmp.entrySet().iterator();
					Entry<Key, FunctionLibFunction> e;
					while (it.hasNext()) {
						e = it.next();
						members.put(e.getKey(), e.getValue());
					}
				}
			}
		}
		Set<Key> set = members.keySet();
		String msg = ExceptionUtil.similarKeyMessage(set.toArray(new Key[set.size()]), methodName.getString(), "function", "functions", "Object", true);
		throw new ExpressionException(msg);
		// throw new ExpressionException("No matching function member ["+methodName+"] found, available
		// function members are ["+
		// lucee.runtime.type.util.ListUtil.sort(CollectionUtil.getKeyList(members.keySet().iterator(),
		// ","),"textnocase","asc",",")+"]");
	}

	private static Object callMethod(Object obj, Collection.Key methodName, Object[] args) throws PageException {
		MethodInstance mi = Reflector.getMethodInstanceEL(obj, obj.getClass(), methodName, args);
		if (mi == null) return DEFAULT;
		try {
			return mi.invoke(obj);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	// used in extension image
	public static Object callWithNamedValues(PageContext pc, Object coll, Collection.Key methodName, Struct args, short type, String strType) throws PageException {
		Map<Key, FunctionLibFunction> members = getMembers(pc, type);
		FunctionLibFunction member = members.get(methodName);

		if (member != null) {
			List<FunctionLibFunctionArg> _args = member.getArg();
			FunctionLibFunctionArg arg;
			if (args.size() < _args.size()) {
				Object val;
				ArrayList<Ref> refs = new ArrayList<Ref>();
				arg = _args.get(0);
				refs.add(new Casting(arg.getTypeAsString(), arg.getType(), new LFunctionValue(new LString(arg.getName()), coll)));
				for (int y = 1; y < _args.size(); y++) {
					arg = _args.get(y);

					// match by name
					val = args.get(arg.getName(), null);

					// match by alias
					if (val == null) {
						String alias = arg.getAlias();
						if (!StringUtil.isEmpty(alias, true)) {
							String[] aliases = lucee.runtime.type.util.ListUtil.trimItems(lucee.runtime.type.util.ListUtil.listToStringArray(alias, ','));
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

		}
		throw new ExpressionException("No matching function member [" + methodName + "] for call with named arguments found, available function members are ["
				+ lucee.runtime.type.util.ListUtil.sort(CollectionUtil.getKeyList(members.keySet().iterator(), ","), "textnocase", "asc", ",") + "]");
	}

}