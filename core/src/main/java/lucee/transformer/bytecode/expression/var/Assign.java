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
package lucee.transformer.bytecode.expression.var;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.scope.ScopeFactory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.ExpressionUtil;
import lucee.transformer.bytecode.util.TypeScope;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.var.DataMember;
import lucee.transformer.expression.var.Member;
import lucee.transformer.expression.var.Variable;

public class Assign extends ExpressionBase {
	private static final Type CK = Types.COLLECTION_KEY;
	// java.lang.Object set(String,Object)
	private final static Method METHOD_SCOPE_SET_KEY = new Method("set", Types.OBJECT, new Type[] { CK, Types.OBJECT });

	// .setArgument(obj)
	private final static Method SET_ARGUMENT = new Method("setArgument", Types.OBJECT, new Type[] { Types.OBJECT });

	// Object touch (Object,String)
	private final static Method TOUCH_KEY = new Method("touch", Types.OBJECT, new Type[] { Types.OBJECT, CK });
	private final static Method TOUCH_KEY_AM = new Method("touch", Types.OBJECT, new Type[] { Types.OBJECT, CK, Types.INT_VALUE, Types.INT_VALUE });

	// Object set (Object,String,Object)
	private final static Method SET_KEY = new Method("set", Types.OBJECT, new Type[] { Types.OBJECT, CK, Types.OBJECT });
	private final static Method SET_KEY_AM = new Method("set", Types.OBJECT, new Type[] { Types.OBJECT, CK, Types.OBJECT, Types.INT_VALUE, Types.INT_VALUE });
	private final static Method US_SET_KEY1 = new Method("us", Types.OBJECT, new Type[] { CK, Types.OBJECT });
	private final static Method US_SET_KEY2 = new Method("us", Types.OBJECT, new Type[] { CK, CK, Types.OBJECT });
	private final static Method US_SET_KEY3 = new Method("us", Types.OBJECT, new Type[] { CK, CK, CK, Types.OBJECT });
	private final static Method US_SET_KEY4 = new Method("us", Types.OBJECT, new Type[] { CK, CK, CK, CK, Types.OBJECT });
	private final static Method[] US_SET_KEYS = new Method[] { US_SET_KEY1, US_SET_KEY2, US_SET_KEY3, US_SET_KEY4 };

	private final static Method VS_SET_KEY1 = new Method("vs", Types.OBJECT, new Type[] { CK, Types.OBJECT });
	private final static Method VS_SET_KEY2 = new Method("vs", Types.OBJECT, new Type[] { CK, CK, Types.OBJECT });
	private final static Method VS_SET_KEY3 = new Method("vs", Types.OBJECT, new Type[] { CK, CK, CK, Types.OBJECT });
	private final static Method VS_SET_KEY4 = new Method("vs", Types.OBJECT, new Type[] { CK, CK, CK, CK, Types.OBJECT });
	private final static Method[] VS_SET_KEYS = new Method[] { VS_SET_KEY1, VS_SET_KEY2, VS_SET_KEY3, VS_SET_KEY4 };

	private final static Method LS_SET_KEY1 = new Method("ls", Types.OBJECT, new Type[] { CK, Types.OBJECT });
	private final static Method LS_SET_KEY2 = new Method("ls", Types.OBJECT, new Type[] { CK, CK, Types.OBJECT });
	private final static Method LS_SET_KEY3 = new Method("ls", Types.OBJECT, new Type[] { CK, CK, CK, Types.OBJECT });
	private final static Method LS_SET_KEY4 = new Method("ls", Types.OBJECT, new Type[] { CK, CK, CK, CK, Types.OBJECT });
	private final static Method[] LS_SET_KEYS = new Method[] { LS_SET_KEY1, LS_SET_KEY2, LS_SET_KEY3, LS_SET_KEY4 };

	private final static Method[][] SET_KEYS = new Method[Scope.SCOPE_COUNT][4];

	static {
		SET_KEYS[Scope.SCOPE_VARIABLES] = VS_SET_KEYS;
		SET_KEYS[Scope.SCOPE_LOCAL] = LS_SET_KEYS;
		SET_KEYS[Scope.SCOPE_UNDEFINED] = US_SET_KEYS;
	}

	// Object getFunction (Object,String,Object[])
	private final static Method GET_FUNCTION_KEY = new Method("getFunction", Types.OBJECT, new Type[] { Types.OBJECT, CK, Types.OBJECT_ARRAY });

	// Object getFunctionWithNamedValues (Object,String,Object[])
	private final static Method GET_FUNCTION_WITH_NAMED_ARGS_KEY = new Method("getFunctionWithNamedValues", Types.OBJECT, new Type[] { Types.OBJECT, CK, Types.OBJECT_ARRAY });

	private static final Method DATA_MEMBER_INIT = new Method("<init>", Types.VOID, new Type[] { Types.INT_VALUE, Types.INT_VALUE, Types.OBJECT });

	private final Variable variable;
	private final Expression value;

	private int access = -1;
	private int modifier = 0;

	/**
	 * Constructor of the class
	 * 
	 * @param variable
	 * @param value
	 */
	public Assign(Variable variable, Expression value, Position end) {
		super(variable.getFactory(), variable.getStart(), end);
		this.variable = variable;
		this.value = value;
		if (value instanceof Variable) ((Variable) value).assign(this);
		// this.returnOldValue=returnOldValue;
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();

		int count = variable.getCount();
		// count 0
		if (count == 0) {
			if (variable.ignoredFirstMember() && variable.getScope() == Scope.SCOPE_VAR) {
				// print.dumpStack();
				return Types.VOID;
			}
			return _writeOutEmpty(bc);
		}

		// TOOD make sure the compile key include the loader version and check loader version and if
		// specific bversion no longer do checkCast

		// supported scopes
		int scope = -1;
		switch (variable.getScope()) {
		case Scope.SCOPE_UNDEFINED:
			scope = Scope.SCOPE_UNDEFINED;
			break;
		case Scope.SCOPE_VARIABLES:
			scope = Scope.SCOPE_VARIABLES;
			break;
		case Scope.SCOPE_LOCAL:
			scope = Scope.SCOPE_LOCAL;
			break;
		}

		// undefined
		boolean doHaveAccessOrModifier = (access > -1 || modifier > 0);
		if (!doHaveAccessOrModifier) {
			outer: while (count > 0 && scope != -1 && count <= SET_KEYS[scope].length) {

				for (int i = 0; i < count; i++) {
					if (!(variable.getMembers().get(i) instanceof DataMember)) {
						break outer;
					}
				}
				// load pc
				adapter.loadArg(0);
				adapter.checkCast(Types.PAGE_CONTEXT_IMPL);

				// write keys
				for (int i = 0; i < count; i++) {
					Member member = (variable.getMembers().get(i));
					getFactory().registerKey(bc, ((DataMember) member).getName(), false);
				}

				// load value
				value.writeOut(bc, MODE_REF);
				// call set function
				adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, SET_KEYS[scope][count - 1]);

				return Types.OBJECT;
			}
		}

		boolean doOnlyScope = variable.getScope() == Scope.SCOPE_LOCAL || variable.getScope() == Scope.SCOPE_CLUSTER;

		Type rtn = Types.OBJECT;
		// boolean last;
		for (int i = doOnlyScope ? 0 : 1; i < count; i++) {
			adapter.loadArg(0);
			if (doHaveAccessOrModifier) {
				boolean last = (i + 1) == count;
				if (last) adapter.checkCast(Types.PAGE_CONTEXT_IMPL);
			}
		}
		rtn = _writeOutFirst(bc, (variable.getMembers().get(0)), mode, count == 1, doOnlyScope);

		boolean first = true;
		boolean doAM;
		for (int i = doOnlyScope ? 0 : 1; i < count; i++) {
			Member member = (variable.getMembers().get(i));
			boolean last = (i + 1) == count;

			// Data Member
			if (member instanceof DataMember) {
				// ((DataMember)member).getName().writeOut(bc, MODE_REF);
				getFactory().registerKey(bc, ((DataMember) member).getName(), false);
				doAM = first && doHaveAccessOrModifier;

				if (last) value.writeOut(bc, MODE_REF);

				if (doAM) {
					GeneratorAdapter ga = bc.getAdapter();
					ga.push(access);
					ga.push(modifier);
				}

				adapter.invokeVirtual(doAM ? Types.PAGE_CONTEXT_IMPL : Types.PAGE_CONTEXT, last ? (doAM ? SET_KEY_AM : SET_KEY) : (doAM ? TOUCH_KEY_AM : TOUCH_KEY));
				rtn = Types.OBJECT;
			}

			// UDF
			else if (member instanceof UDF) {
				if (last) throw new TransformerException(bc, "can't assign value to a user defined function", getStart());
				UDF udf = (UDF) member;
				getFactory().registerKey(bc, udf.getName(), false);
				ExpressionUtil.writeOutExpressionArray(bc, Types.OBJECT, udf.getArguments());
				adapter.invokeVirtual(Types.PAGE_CONTEXT, udf.hasNamedArgs() ? GET_FUNCTION_WITH_NAMED_ARGS_KEY : GET_FUNCTION_KEY);
				rtn = Types.OBJECT;
			}
			first = false;
		}
		return rtn;
	}

	private void writeValue(BytecodeContext bc) throws TransformerException {
		// set Access
		if ((access > -1 || modifier > 0)) {
			GeneratorAdapter ga = bc.getAdapter();
			ga.newInstance(Types.DATA_MEMBER);
			ga.dup();
			ga.push(access);
			ga.push(modifier);
			value.writeOut(bc, MODE_REF);
			ga.invokeConstructor(Types.DATA_MEMBER, DATA_MEMBER_INIT);
		}
		else value.writeOut(bc, MODE_REF);

	}

	private Type _writeOutFirst(BytecodeContext bc, Member member, int mode, boolean last, boolean doOnlyScope) throws TransformerException {

		if (member instanceof DataMember) {
			return _writeOutOneDataMember(bc, (DataMember) member, last, doOnlyScope);
			// return Variable._writeOutFirstDataMember(adapter,(DataMember)member,variable.scope, last);
		}
		else if (member instanceof UDF) {
			if (last) throw new TransformerException(bc, "can't assign value to a user defined function", getStart());
			return VariableImpl._writeOutFirstUDF(bc, (UDF) member, variable.getScope(), doOnlyScope);
		}
		else {
			if (last) throw new TransformerException(bc, "can't assign value to a built in function", getStart());
			return VariableImpl._writeOutFirstBIF(bc, (BIF) member, mode, last, getStart());
		}
	}

	private Type _writeOutOneDataMember(BytecodeContext bc, DataMember member, boolean last, boolean doOnlyScope) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();

		if (doOnlyScope) {
			adapter.loadArg(0);
			if (variable.getScope() == Scope.SCOPE_LOCAL) {
				return TypeScope.invokeScope(adapter, TypeScope.METHOD_LOCAL_TOUCH, Types.PAGE_CONTEXT);
			}
			else if (variable.getScope() == Scope.SCOPE_CLUSTER) {
				adapter.checkCast(Types.PAGE_CONTEXT_IMPL);
				return TypeScope.invokeScope(adapter, TypeScope.METHOD_THREAD_TOUCH, Types.PAGE_CONTEXT_IMPL);
			}
			return TypeScope.invokeScope(adapter, variable.getScope());
		}

		// pc.get
		adapter.loadArg(0);
		if (last) {
			TypeScope.invokeScope(adapter, variable.getScope());
			getFactory().registerKey(bc, member.getName(), false);
			writeValue(bc);
			adapter.invokeInterface(TypeScope.SCOPES[variable.getScope()], METHOD_SCOPE_SET_KEY);

		}
		else {
			adapter.loadArg(0);
			TypeScope.invokeScope(adapter, variable.getScope());
			getFactory().registerKey(bc, member.getName(), false);
			adapter.invokeVirtual(Types.PAGE_CONTEXT, TOUCH_KEY);
		}
		return Types.OBJECT;

	}

	private Type _writeOutEmpty(BytecodeContext bc) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();

		if (variable.getScope() == Scope.SCOPE_ARGUMENTS) {
			adapter.loadArg(0);
			TypeScope.invokeScope(adapter, Scope.SCOPE_ARGUMENTS);
			value.writeOut(bc, MODE_REF);
			adapter.invokeInterface(TypeScope.SCOPE_ARGUMENT, SET_ARGUMENT);
		}
		else {
			adapter.loadArg(0);
			TypeScope.invokeScope(adapter, Scope.SCOPE_UNDEFINED);
			getFactory().registerKey(bc, bc.getFactory().createLitString(ScopeFactory.toStringScope(variable.getScope(), "undefined")), false);
			value.writeOut(bc, MODE_REF);
			adapter.invokeInterface(TypeScope.SCOPES[Scope.SCOPE_UNDEFINED], METHOD_SCOPE_SET_KEY);
		}

		return Types.OBJECT;
	}

	/**
	 * @return the value
	 */
	public Expression getValue() {
		return value;
	}

	/**
	 * @return the variable
	 */
	public Variable getVariable() {
		return variable;
	}

	public void setAccess(int access) {
		this.access = access;
	}

	public int getAccess() {
		return access;
	}

	public void setModifier(int modifier) {
		this.modifier = modifier;
	}

	public int getModifier() {
		return modifier;
	}

	public void setFinal(boolean _final) {
		// TODO Auto-generated method stub

	}
}