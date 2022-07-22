/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.transformer.bytecode.expression.var;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.types.RefInteger;
import lucee.commons.lang.types.RefIntegerImpl;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.exp.TemplateException;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.UDFUtil;
import lucee.transformer.Context;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.ASMConstants;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.ExpressionUtil;
import lucee.transformer.bytecode.util.TypeScope;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.bytecode.visitor.ArrayVisitor;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.expression.var.DataMember;
import lucee.transformer.expression.var.Member;
import lucee.transformer.expression.var.Variable;
import lucee.transformer.library.function.FunctionLibFunction;
import lucee.transformer.library.function.FunctionLibFunctionArg;

public class VariableImpl extends ExpressionBase implements Variable {

	// java.lang.Object get(Key)
	final static Method METHOD_SCOPE_GET_KEY = new Method("get", Types.OBJECT, new Type[] { Types.COLLECTION_KEY });

	// Object getCollection(Key)
	final static Method METHOD_SCOPE_GET_COLLECTION_KEY = new Method("getCollection", Types.OBJECT, new Type[] { Types.COLLECTION_KEY });

	// public Object get(PageContext pc,Object coll, Key[] keys, Object defaultValue) {
	/* ??? */private final static Method CALLER_UTIL_GET = new Method("get", Types.OBJECT,
			new Type[] { Types.PAGE_CONTEXT, Types.OBJECT, Types.COLLECTION_KEY_ARRAY, Types.OBJECT });

	final static Method INIT = new Method("init", Types.COLLECTION_KEY, new Type[] { Types.STRING });
	final static Method TO_KEY = new Method("toKey", Types.COLLECTION_KEY, new Type[] { Types.OBJECT });

	private static final int TWO = 0;
	private static final int THREE = 1;
	private static final int THREE2 = 2;

	// Object getCollection (Object,Key[,Object])
	private final static Method[] GET_COLLECTION = new Method[] { new Method("getCollection", Types.OBJECT, new Type[] { Types.OBJECT, Types.COLLECTION_KEY }),
			new Method("getCollection", Types.OBJECT, new Type[] { Types.OBJECT, Types.COLLECTION_KEY, Types.OBJECT }) };

	// Object get (Object,Key)
	private final static Method[] GET = new Method[] { new Method("get", Types.OBJECT, new Type[] { Types.OBJECT, Types.COLLECTION_KEY }),
			new Method("get", Types.OBJECT, new Type[] { Types.OBJECT, Types.COLLECTION_KEY, Types.OBJECT }) };

	private final static Method[] GET_FUNCTION = new Method[] { new Method("getFunction", Types.OBJECT, new Type[] { Types.OBJECT, Types.COLLECTION_KEY, Types.OBJECT_ARRAY }),
			new Method("getFunction", Types.OBJECT, new Type[] { Types.OBJECT, Types.COLLECTION_KEY, Types.OBJECT_ARRAY, Types.OBJECT }),
			new Method("getFunction2", Types.OBJECT, new Type[] { Types.OBJECT, Types.COLLECTION_KEY, Types.OBJECT_ARRAY, Types.OBJECT }) };
	// Object getFunctionWithNamedValues (Object,String,Object[])
	private final static Method[] GET_FUNCTION_WITH_NAMED_ARGS = new Method[] {
			new Method("getFunctionWithNamedValues", Types.OBJECT, new Type[] { Types.OBJECT, Types.COLLECTION_KEY, Types.OBJECT_ARRAY }),
			new Method("getFunctionWithNamedValues", Types.OBJECT, new Type[] { Types.OBJECT, Types.COLLECTION_KEY, Types.OBJECT_ARRAY, Types.OBJECT }),
			new Method("getFunctionWithNamedValues2", Types.OBJECT, new Type[] { Types.OBJECT, Types.COLLECTION_KEY, Types.OBJECT_ARRAY, Types.OBJECT }) };

	private static final Method RECORDCOUNT = new Method("recordcount", Types.OBJECT, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT });
	private static final Method CURRENTROW = new Method("currentrow", Types.OBJECT, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT });
	private static final Method COLUMNLIST = new Method("columnlist", Types.OBJECT, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT });

	// THIS
	private static final Method THIS_GET0 = new Method("thisGet", Types.OBJECT, new Type[] {});
	private static final Method THIS_TOUCH0 = new Method("thisTouch", Types.OBJECT, new Type[] {});
	private static final Method THIS_GET1 = new Method("thisGet", Types.OBJECT, new Type[] { Types.OBJECT });
	private static final Method THIS_TOUCH1 = new Method("thisTouch", Types.OBJECT, new Type[] { Types.OBJECT });

	// STATIC
	private static final Method STATIC_GET0 = new Method("staticGet", Types.OBJECT, new Type[] {});
	private static final Method STATIC_TOUCH0 = new Method("staticTouch", Types.OBJECT, new Type[] {});
	private static final Method STATIC_GET1 = new Method("staticGet", Types.OBJECT, new Type[] { Types.OBJECT });
	private static final Method STATIC_TOUCH1 = new Method("staticTouch", Types.OBJECT, new Type[] { Types.OBJECT });
	private static final Method INVOKE = new Method("invoke", Types.OBJECT, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT_ARRAY, Types.STRING, Types.STRING, Types.STRING });

	private int scope = Scope.SCOPE_UNDEFINED;
	List<Member> members = new ArrayList<Member>();
	int countDM = 0;
	int countFM = 0;
	private boolean ignoredFirstMember;

	private boolean fromHash = false;
	private Expression defaultValue;
	private Boolean asCollection;
	private Assign assign;

	public VariableImpl(Factory factory, Position start, Position end) {
		super(factory, start, end);
	}

	public VariableImpl(Factory factory, int scope, Position start, Position end) {
		super(factory, start, end);
		this.scope = scope;
	}

	@Override
	public Expression getDefaultValue() {
		return defaultValue;
	}

	@Override
	public void setDefaultValue(Expression defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public Boolean getAsCollection() {
		return asCollection;
	}

	@Override
	public void setAsCollection(Boolean asCollection) {
		this.asCollection = asCollection;
	}

	@Override
	public int getScope() {
		return scope;
	}

	/**
	 * @param scope the scope to set
	 */
	public void setScope(int scope) {
		this.scope = scope;
	}

	@Override
	public void addMember(Member member) {
		if (member instanceof DataMember) countDM++;
		else countFM++;
		member.setParent(this);
		members.add(member);
	}

	@Override
	public Member removeMember(int index) {
		Member rtn = members.remove(index);
		if (rtn instanceof DataMember) countDM--;
		else countFM--;
		return rtn;
	}

	@Override
	public final Class<?> writeOutCollection(Context c, int mode) throws TransformerException {
		try {
			return Types.toClass(writeOutCollectionAsType(c, mode));
		}
		catch (ClassException e) {
			throw new TransformerException(e, null);
		}
	}

	public final Type writeOutCollectionAsType(Context c, int mode) throws TransformerException {
		BytecodeContext bc = (BytecodeContext) c;
		ExpressionUtil.visitLine(bc, getStart());
		Type type = _writeOut(bc, mode, Boolean.TRUE);
		ExpressionUtil.visitLine(bc, getEnd());
		return type;
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		if (defaultValue != null && countFM == 0 && countDM != 0) return _writeOutCallerUtil(bc, mode);
		return _writeOut(bc, mode, asCollection);
	}

	private Type _writeOut(BytecodeContext bc, int mode, Boolean asCollection) throws TransformerException {

		final GeneratorAdapter adapter = bc.getAdapter();
		final int count = countFM + countDM;

		// count 0
		if (count == 0) return _writeOutEmpty(bc);

		boolean doOnlyScope = scope == Scope.SCOPE_LOCAL;

		// boolean last;
		int c = 0;
		for (int i = doOnlyScope ? 0 : 1; i < count; i++) {
			Member member = (members.get((count - 1) - c));
			c++;
			adapter.loadArg(0);
			if (member.getSafeNavigated() && member instanceof UDF) adapter.checkCast(Types.PAGE_CONTEXT_IMPL);
		}

		Type rtn = _writeOutFirst(bc, (members.get(0)), mode, count == 1, doOnlyScope, null, null);

		// pc.get(
		for (int i = doOnlyScope ? 0 : 1; i < count; i++) {
			Member member = (members.get(i));
			boolean last = (i + 1) == count;

			// Data Member
			if (member instanceof DataMember) {
				ExprString name = ((DataMember) member).getName();
				if (last && ASMUtil.isDotKey(name)) {
					LitString ls = (LitString) name;
					if (ls.getString().equalsIgnoreCase("RECORDCOUNT")) {
						adapter.invokeStatic(Types.VARIABLE_UTIL_IMPL, RECORDCOUNT);
					}
					else if (ls.getString().equalsIgnoreCase("CURRENTROW")) {
						adapter.invokeStatic(Types.VARIABLE_UTIL_IMPL, CURRENTROW);
					}
					else if (ls.getString().equalsIgnoreCase("COLUMNLIST")) {
						adapter.invokeStatic(Types.VARIABLE_UTIL_IMPL, COLUMNLIST);
					}
					else {
						getFactory().registerKey(bc, name, false);
						// safe nav
						int type;
						if (member.getSafeNavigated()) {
							Expression val = member.getSafeNavigatedValue();
							if (val == null) ASMConstants.NULL(adapter);
							else val.writeOut(bc, Expression.MODE_REF);
							type = THREE;
						}
						else type = TWO;
						adapter.invokeVirtual(Types.PAGE_CONTEXT, asCollection(asCollection, last) ? GET_COLLECTION[type] : GET[type]);
					}
				}
				else {
					getFactory().registerKey(bc, name, false);
					// safe nav
					int type;
					if (member.getSafeNavigated()) {
						Expression val = member.getSafeNavigatedValue();
						if (val == null) ASMConstants.NULL(adapter);
						else val.writeOut(bc, Expression.MODE_REF);
						type = THREE;
					}
					else type = TWO;
					adapter.invokeVirtual(Types.PAGE_CONTEXT, asCollection(asCollection, last) ? GET_COLLECTION[type] : GET[type]);
				}
				rtn = Types.OBJECT;
			}

			// UDF
			else if (member instanceof UDF) {
				rtn = _writeOutUDF(bc, (UDF) member);
			}
		}
		return rtn;
	}

	private Type _writeOutCallerUtil(BytecodeContext bc, int mode) throws TransformerException {

		GeneratorAdapter adapter = bc.getAdapter();
		final int count = countFM + countDM;

		// count 0
		if (count == 0) return _writeOutEmpty(bc);

		// pc
		adapter.loadArg(0);

		// collection
		RefInteger startIndex = new RefIntegerImpl();
		_writeOutFirst(bc, (members.get(0)), mode, count == 1, true, defaultValue, startIndex);

		// keys
		Iterator<Member> it = members.iterator();
		ArrayVisitor av = new ArrayVisitor();
		av.visitBegin(adapter, Types.COLLECTION_KEY, countDM - startIndex.toInt());
		int index = 0, i = 0;
		while (it.hasNext()) {
			DataMember member = (DataMember) it.next();
			if (i++ < startIndex.toInt()) continue;
			av.visitBeginItem(adapter, index++);
			getFactory().registerKey(bc, member.getName(), false);
			av.visitEndItem(bc.getAdapter());

		}
		av.visitEnd();

		// defaultValue
		defaultValue.writeOut(bc, MODE_REF);
		bc.getAdapter().invokeStatic(Types.CALLER_UTIL, CALLER_UTIL_GET);
		return Types.OBJECT;
	}

	private boolean asCollection(Boolean asCollection, boolean last) {
		if (!last) return true;
		return asCollection != null && asCollection.booleanValue();
	}

	/**
	 * outputs an empty Variable, only scope Example: pc.formScope();
	 * 
	 * @param adapter
	 * @throws TemplateException
	 */
	private Type _writeOutEmpty(BytecodeContext bc) throws TransformerException {
		if (ignoredFirstMember && (scope == Scope.SCOPE_LOCAL || scope == Scope.SCOPE_VAR)) return Types.VOID;

		GeneratorAdapter adapter = bc.getAdapter();
		adapter.loadArg(0);
		Method m;
		Type t = Types.PAGE_CONTEXT;
		if (scope == Scope.SCOPE_ARGUMENTS) {
			getFactory().TRUE().writeOut(bc, MODE_VALUE);
			m = TypeScope.METHOD_ARGUMENT_BIND;
		}
		else if (scope == Scope.SCOPE_LOCAL) {
			t = Types.PAGE_CONTEXT;
			getFactory().TRUE().writeOut(bc, MODE_VALUE);
			m = TypeScope.METHOD_LOCAL_BIND;
		}
		else if (scope == Scope.SCOPE_VAR) {
			t = Types.PAGE_CONTEXT;
			getFactory().TRUE().writeOut(bc, MODE_VALUE);
			m = TypeScope.METHOD_VAR_BIND;
		}
		else m = TypeScope.METHODS[scope];

		TypeScope.invokeScope(adapter, m, t);

		return m.getReturnType();
	}

	private Type _writeOutFirst(BytecodeContext bc, Member member, int mode, boolean last, boolean doOnlyScope, Expression defaultValue, RefInteger startIndex)
			throws TransformerException {

		if (member instanceof DataMember) return _writeOutFirstDataMember(bc, (DataMember) member, scope, last, doOnlyScope, defaultValue, startIndex);
		else if (member instanceof UDF) return _writeOutFirstUDF(bc, (UDF) member, scope, doOnlyScope);
		else return _writeOutFirstBIF(bc, (BIF) member, mode, last, getStart());
	}

	static Type _writeOutFirstBIF(BytecodeContext bc, BIF bif, int mode, boolean last, Position line) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		adapter.loadArg(0);
		// class
		ClassDefinition bifCD = bif.getClassDefinition();
		Class clazz = null;
		try {
			clazz = bifCD.getClazz();
		}
		catch (Exception e) {
			LogUtil.log(null, VariableImpl.class.getName(), e);
		}
		Type rtnType = Types.toType(bif.getReturnType());
		if (rtnType == Types.VOID) rtnType = Types.STRING;

		// arguments
		Argument[] args = bif.getArguments();
		Type[] argTypes;
		boolean core = bif.getFlf().isCore(); // MUST setting this to false need to work !!!

		if (bif.getArgType() == FunctionLibFunction.ARG_FIX && !bifCD.isBundle() && core) {
			if (isNamed(bif.getFlf().getName(), args)) {
				NamedArgument[] nargs = toNamedArguments(args);

				String[] names = new String[nargs.length];
				// get all names
				for (int i = 0; i < nargs.length; i++) {
					names[i] = getName(nargs[i].getName());
				}
				ArrayList<FunctionLibFunctionArg> list = bif.getFlf().getArg();
				Iterator<FunctionLibFunctionArg> it = list.iterator();

				argTypes = new Type[list.size() + 1];
				argTypes[0] = Types.PAGE_CONTEXT;

				FunctionLibFunctionArg flfa;
				int index = 0;
				VT vt;
				while (it.hasNext()) {
					flfa = it.next();
					vt = getMatchingValueAndType(bc.getFactory(), flfa, nargs, names, line);
					if (vt.index != -1) names[vt.index] = null;
					argTypes[++index] = Types.toType(vt.type);
					if (vt.value == null) ASMConstants.NULL(bc.getAdapter());
					else vt.value.writeOut(bc, Types.isPrimitiveType(argTypes[index]) ? MODE_VALUE : MODE_REF);
				}

				for (int y = 0; y < names.length; y++) {
					if (names[y] != null) {
						TransformerException bce = new TransformerException("argument [" + names[y] + "] is not allowed for function [" + bif.getFlf().getName() + "]",
								args[y].getStart());
						UDFUtil.addFunctionDoc(bce, bif.getFlf());
						throw bce;
					}
				}
			}
			else {
				argTypes = new Type[args.length + 1];
				argTypes[0] = Types.PAGE_CONTEXT;

				for (int y = 0; y < args.length; y++) {
					argTypes[y + 1] = Types.toType(args[y].getStringType());
					args[y].writeOutValue(bc, Types.isPrimitiveType(argTypes[y + 1]) ? MODE_VALUE : MODE_REF);
				}
				// if no method exists for the exact match of arguments, call the method with all arguments (when
				// exists)
				if (methodExists(clazz, "call", argTypes, rtnType) == Boolean.FALSE) {
					ArrayList<FunctionLibFunctionArg> _args = bif.getFlf().getArg();

					Type[] tmp = new Type[_args.size() + 1];

					// fill the existing
					for (int i = 0; i < argTypes.length; i++) {
						tmp[i] = argTypes[i];
					}

					// get the rest with default values
					FunctionLibFunctionArg flfa;
					VT def;
					for (int i = argTypes.length; i < tmp.length; i++) {
						flfa = _args.get(i - 1);
						tmp[i] = Types.toType(flfa.getTypeAsString());
						def = getDefaultValue(bc.getFactory(), flfa);

						if (def.value != null) def.value.writeOut(bc, Types.isPrimitiveType(tmp[i]) ? MODE_VALUE : MODE_REF);
						else ASMConstants.NULL(bc.getAdapter());

					}
					argTypes = tmp;
				}
			}
		}
		// Arg Type DYN or bundle based
		else {
			///////////////////////////////////////////////////////////////
			if (bif.getArgType() == FunctionLibFunction.ARG_FIX) {
				if (isNamed(bif.getFlf().getName(), args)) {
					NamedArgument[] nargs = toNamedArguments(args);
					String[] names = getNames(nargs);
					ArrayList<FunctionLibFunctionArg> list = bif.getFlf().getArg();
					Iterator<FunctionLibFunctionArg> it = list.iterator();
					LinkedList<Argument> tmpArgs = new LinkedList<Argument>();
					LinkedList<Boolean> nulls = new LinkedList<Boolean>();

					FunctionLibFunctionArg flfa;
					VT vt;
					while (it.hasNext()) {
						flfa = it.next();
						vt = getMatchingValueAndType(bc.getFactory(), flfa, nargs, names, line);
						if (vt.index != -1) names[vt.index] = null;
						if (vt.value == null) tmpArgs.add(new Argument(bif.getFactory().createNull(), "any")); // has to by any otherwise a caster is set
						else tmpArgs.add(new Argument(vt.value, vt.type));

						nulls.add(vt.value == null);
					}

					for (int y = 0; y < names.length; y++) {
						if (names[y] != null) {
							TransformerException bce = new TransformerException("argument [" + names[y] + "] is not allowed for function [" + bif.getFlf().getName() + "]",
									args[y].getStart());
							UDFUtil.addFunctionDoc(bce, bif.getFlf());
							throw bce;
						}
					}
					// remove null at the end
					Boolean tmp;
					while ((tmp = nulls.pollLast()) != null) {
						if (!tmp.booleanValue()) break;
						tmpArgs.pollLast();
					}
					args = tmpArgs.toArray(new Argument[tmpArgs.size()]);
				}
			}
			///////////////////////////////////////////////////////////////
			argTypes = new Type[2];
			argTypes[0] = Types.PAGE_CONTEXT;
			argTypes[1] = Types.OBJECT_ARRAY;
			ExpressionUtil.writeOutExpressionArray(bc, Types.OBJECT, args);
		}

		// core
		if (core && !bifCD.isBundle()) {
			adapter.invokeStatic(Type.getType(clazz), new Method("call", rtnType, argTypes));
		}
		// external
		else {
			// in that case we need 3 additional args
			// className
			if (bifCD.getClassName() != null) adapter.push(bifCD.getClassName());
			else ASMConstants.NULL(adapter);
			if (bifCD.getName() != null) adapter.push(bifCD.getName());// bundle name
			else ASMConstants.NULL(adapter);
			if (bifCD.getVersionAsString() != null) adapter.push(bifCD.getVersionAsString());// bundle version
			else ASMConstants.NULL(adapter);

			adapter.invokeStatic(Types.FUNCTION_HANDLER_POOL, INVOKE);
			rtnType = Types.OBJECT;
		}

		if (mode == MODE_REF || !last) {
			if (Types.isPrimitiveType(rtnType)) {
				adapter.invokeStatic(Types.CASTER, new Method("toRef", Types.toRefType(rtnType), new Type[] { rtnType }));
				rtnType = Types.toRefType(rtnType);
			}
		}
		return rtnType;
	}

	/**
	 * checks if a method exists
	 * 
	 * @param clazz
	 * @param methodName
	 * @param args
	 * @param returnType
	 * @return returns null when checking fi
	 */

	private static Boolean methodExists(Class clazz, String methodName, Type[] args, Type returnType) {
		try {
			Class<?>[] _args = new Class[args.length];
			for (int i = 0; i < _args.length; i++) {
				_args[i] = Types.toClass(args[i]);
			}
			Class<?> rtn = Types.toClass(returnType);
			try {
				java.lang.reflect.Method m = clazz.getMethod(methodName, _args);
				return m.getReturnType() == rtn;
			}
			catch (Exception e) {
				return false;
			}
		}
		catch (Exception e) {
			LogUtil.log(null, VariableImpl.class.getName(), e);
			return null;
		}
	}

	static Type _writeOutFirstUDF(BytecodeContext bc, UDF udf, int scope, boolean doOnlyScope) throws TransformerException {

		GeneratorAdapter adapter = bc.getAdapter();
		// pc.getFunction (Object,String,Object[])
		// pc.getFunctionWithNamedValues (Object,String,Object[])
		adapter.loadArg(0);
		if (udf.getSafeNavigated()) adapter.checkCast(Types.PAGE_CONTEXT_IMPL);// FUTURE remove if no longer necessary to have PageContextImpl

		if (!doOnlyScope) adapter.loadArg(0);
		Type rtn = TypeScope.invokeScope(adapter, scope);
		if (doOnlyScope) return rtn;

		return _writeOutUDF(bc, udf);
	}

	private static Type _writeOutUDF(BytecodeContext bc, UDF udf) throws TransformerException {
		bc.getFactory().registerKey(bc, udf.getName(), false);
		Argument[] args = udf.getArguments();

		// no arguments
		if (args.length == 0) {
			bc.getAdapter().getStatic(Types.CONSTANTS, "EMPTY_OBJECT_ARRAY", Types.OBJECT_ARRAY);
		}
		else ExpressionUtil.writeOutExpressionArray(bc, Types.OBJECT, args);

		int type;
		if (udf.getSafeNavigated()) {
			type = THREE;
			Expression val = udf.getSafeNavigatedValue();
			if (val == null) {
				ASMConstants.NULL(bc.getAdapter());
				type = THREE;
			}
			else {
				val.writeOut(bc, Expression.MODE_REF);
				type = THREE2;
			}
		}
		else type = TWO;
		bc.getAdapter().invokeVirtual(udf.getSafeNavigated() ? Types.PAGE_CONTEXT_IMPL : Types.PAGE_CONTEXT,
				udf.hasNamedArgs() ? GET_FUNCTION_WITH_NAMED_ARGS[type] : GET_FUNCTION[type]);
		return Types.OBJECT;
	}

	Type _writeOutFirstDataMember(BytecodeContext bc, DataMember member, int scope, boolean last, boolean doOnlyScope, Expression defaultValue, RefInteger startIndex)
			throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		if (startIndex != null) startIndex.setValue(doOnlyScope ? 0 : 1);

		// this/static
		if (scope == Scope.SCOPE_UNDEFINED) {
			ExprString name = member.getName();
			if (ASMUtil.isDotKey(name)) {
				LitString ls = (LitString) name;

				// THIS
				if (ls.getString().equalsIgnoreCase("THIS")) {
					if (startIndex != null) startIndex.setValue(1);
					adapter.loadArg(0);
					adapter.checkCast(Types.PAGE_CONTEXT_IMPL);
					if (defaultValue != null) {
						defaultValue.writeOut(bc, MODE_REF);
						adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, (countFM + countDM) == 1 ? THIS_GET1 : THIS_TOUCH1);
					}
					else adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, (countFM + countDM) == 1 ? THIS_GET0 : THIS_TOUCH0);
					return Types.OBJECT;
				}
				// STATIC
				if (ls.getString().equalsIgnoreCase("STATIC")) {
					if (startIndex != null) startIndex.setValue(1);
					adapter.loadArg(0);
					adapter.checkCast(Types.PAGE_CONTEXT_IMPL);
					if (defaultValue != null) {
						defaultValue.writeOut(bc, MODE_REF);
						adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, (countFM + countDM) == 1 ? STATIC_GET1 : STATIC_TOUCH1);
					}
					else adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, (countFM + countDM) == 1 ? STATIC_GET0 : STATIC_TOUCH0);
					return Types.OBJECT;
				}
			}
		}

		// LDEV3496
		// subsequent logic will conditionally require a PageContext be pushed onto the stack, as part of a call to resolve a save-nav expression member
		// But, we only want to push it if it will be consumed
		// root cause of LDEV3496 was this was pushed in cases where it would not be consumed, and an extra unanticpated stack variable would break during class verification
		// (jvm would report "expected a stackmap frame", javassist would report "InvocationTargetException: Operand stacks could not be merged, they are different sizes!")
		final boolean needsAndWillConsumePageContextForSafeNavigationResolution = member.getSafeNavigated() && !doOnlyScope;
		if (needsAndWillConsumePageContextForSafeNavigationResolution) {
			adapter.loadArg(0);
		}

		// collection
		Type rtn;
		if (scope == Scope.SCOPE_LOCAL && defaultValue != null) { // local
			adapter.loadArg(0);
			adapter.checkCast(Types.PAGE_CONTEXT_IMPL);
			getFactory().FALSE().writeOut(bc, MODE_VALUE);
			defaultValue.writeOut(bc, MODE_VALUE);
			adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, TypeScope.METHOD_LOCAL_EL);
			rtn = Types.OBJECT;
		}
		else { // all other scopes
			adapter.loadArg(0);
			rtn = TypeScope.invokeScope(adapter, scope);
		}

		if (doOnlyScope) return rtn;
		getFactory().registerKey(bc, member.getName(), false);

		boolean _last = !last && scope == Scope.SCOPE_UNDEFINED;
		if (!member.getSafeNavigated()) {
			adapter.invokeInterface(TypeScope.SCOPES[scope], _last ? METHOD_SCOPE_GET_COLLECTION_KEY : METHOD_SCOPE_GET_KEY);
		}
		else {
			Expression val = member.getSafeNavigatedValue(); // LDEV-1201
			if (val == null) ASMConstants.NULL(bc.getAdapter());
			else val.writeOut(bc, Expression.MODE_REF);

			adapter.invokeVirtual(Types.PAGE_CONTEXT, _last ? GET_COLLECTION[THREE] : GET[THREE]);
		}
		return Types.OBJECT;
	}

	@Override
	public List<Member> getMembers() {
		return members;
	}

	@Override
	public Member getFirstMember() {
		if (members.isEmpty()) return null;
		return members.get(0);
	}

	@Override
	public Member getLastMember() {
		if (members.isEmpty()) return null;
		return members.get(members.size() - 1);
	}

	@Override
	public void ignoredFirstMember(boolean b) {
		this.ignoredFirstMember = b;
	}

	@Override
	public boolean ignoredFirstMember() {
		return ignoredFirstMember;
	}

	private static VT getMatchingValueAndType(Factory factory, FunctionLibFunctionArg flfa, NamedArgument[] nargs, String[] names, Position line) throws TransformerException {
		String flfan = flfa.getName();

		// first search if an argument match
		for (int i = 0; i < nargs.length; i++) {
			if (names[i] != null && names[i].equalsIgnoreCase(flfan)) {
				nargs[i].setValue(nargs[i].getRawValue(), flfa.getTypeAsString());
				return new VT(nargs[i].getValue(), flfa.getTypeAsString(), i);
			}
		}

		// then check if an alias match
		String alias = flfa.getAlias();
		if (!StringUtil.isEmpty(alias)) {
			// String[] arrAlias =
			// lucee.runtime.type.List.toStringArray(lucee.runtime.type.List.trimItems(lucee.runtime.type.List.listToArrayRemoveEmpty(alias,
			// ',')));
			for (int i = 0; i < nargs.length; i++) {
				if (names[i] != null && lucee.runtime.type.util.ListUtil.listFindNoCase(alias, names[i], ",") != -1) {
					nargs[i].setValue(nargs[i].getRawValue(), flfa.getTypeAsString());
					return new VT(nargs[i].getValue(), flfa.getTypeAsString(), i);
				}
			}
		}

		// if not required return the default value
		if (!flfa.getRequired()) {
			return getDefaultValue(factory, flfa);
		}
		TransformerException be = new TransformerException("missing required argument [" + flfan + "] for function [" + flfa.getFunction().getName() + "]", line);
		UDFUtil.addFunctionDoc(be, flfa.getFunction());
		throw be;
	}

	private static VT getDefaultValue(Factory factory, FunctionLibFunctionArg flfa) {
		String defaultValue = flfa.getDefaultValue();
		String type = flfa.getTypeAsString();
		if (defaultValue == null) {
			if (type.equals("boolean") || type.equals("bool")) return new VT(factory.FALSE(), type, -1);
			if (type.equals("number") || type.equals("numeric") || type.equals("double")) return new VT(factory.NUMBER_ONE(), type, -1);
			return new VT(null, type, -1);
		}
		return new VT(factory.toExpression(factory.createLitString(defaultValue), type), type, -1);
	}

	private static String getName(Expression expr) throws TransformerException {
		String name = ASMUtil.toString(expr);
		if (name == null) throw new TransformerException("cannot extract a string from an object of type [" + expr.getClass().getName() + "]", null);
		return name;
	}

	private static String[] getNames(NamedArgument[] args) throws TransformerException {
		String[] names = new String[args.length];
		for (int i = 0; i < args.length; i++) {
			names[i] = getName(args[i].getName());
		}
		return names;
	}

	/**
	 * translate an array of arguments to an array of NamedArguments, attention no check if the elements
	 * are really named arguments
	 * 
	 * @param args
	 * @return
	 */
	private static NamedArgument[] toNamedArguments(Argument[] args) {
		NamedArgument[] nargs = new NamedArgument[args.length];
		for (int i = 0; i < args.length; i++) {
			nargs[i] = (NamedArgument) args[i];
		}

		return nargs;
	}

	/**
	 * check if the arguments are named arguments or regular arguments, throws an exception when mixed
	 * 
	 * @param funcName
	 * @param args
	 * @param line
	 * @return
	 * @throws TransformerException
	 */
	private static boolean isNamed(String funcName, Argument[] args) throws TransformerException {
		if (ArrayUtil.isEmpty(args)) return false;
		boolean named = false;
		boolean unNamed = false;
		for (int i = 0; i < args.length; i++) {
			if (args[i] instanceof NamedArgument) named = true;
			else unNamed = true;
			if (named && unNamed)
				throw new TransformerException("Invalid argument for function [ " + funcName + " ], You can't mix named and unNamed arguments", args[i].getStart());
		}
		return named;
	}

	@Override
	public void fromHash(boolean fromHash) {
		this.fromHash = fromHash;
	}

	@Override
	public boolean fromHash() {
		return fromHash;
	}

	@Override
	public int getCount() {
		return countDM + countFM;
	}

	@Override
	public void assign(Assign assign) {
		this.assign = assign;
	}

	@Override
	public Assign assign() {
		return assign;
	}

}

class VT {
	Expression value;
	String type;
	int index;

	public VT(Expression value, String type, int index) {
		this.value = value;
		this.type = type;
		this.index = index;
	}
}
