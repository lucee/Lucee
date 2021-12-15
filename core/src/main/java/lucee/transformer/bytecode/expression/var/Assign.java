/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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

	// java.lang.Object set(String,Object)
	private final static Method METHOD_SCOPE_SET_KEY = new Method("set", Types.OBJECT, new Type[] { Types.COLLECTION_KEY, Types.OBJECT });

	// .setArgument(obj)
	private final static Method SET_ARGUMENT = new Method("setArgument", Types.OBJECT, new Type[] { Types.OBJECT });

	// Object touch (Object,String)
	private final static Method TOUCH_KEY = new Method("touch", Types.OBJECT, new Type[] { Types.OBJECT, Types.COLLECTION_KEY });

	// Object set (Object,String,Object)
	private final static Method SET_KEY = new Method("set", Types.OBJECT, new Type[] { Types.OBJECT, Types.COLLECTION_KEY, Types.OBJECT });

	// Object getFunction (Object,String,Object[])
	private final static Method GET_FUNCTION_KEY = new Method("getFunction", Types.OBJECT, new Type[] { Types.OBJECT, Types.COLLECTION_KEY, Types.OBJECT_ARRAY });

	// Object getFunctionWithNamedValues (Object,String,Object[])
	private final static Method GET_FUNCTION_WITH_NAMED_ARGS_KEY = new Method("getFunctionWithNamedValues", Types.OBJECT,
			new Type[] { Types.OBJECT, Types.COLLECTION_KEY, Types.OBJECT_ARRAY });

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

		boolean doOnlyScope = variable.getScope() == Scope.SCOPE_LOCAL;

		Type rtn = Types.OBJECT;
		// boolean last;
		for (int i = doOnlyScope ? 0 : 1; i < count; i++) {
			adapter.loadArg(0);
		}
		rtn = _writeOutFirst(bc, (variable.getMembers().get(0)), mode, count == 1, doOnlyScope);

		// pc.get(
		for (int i = doOnlyScope ? 0 : 1; i < count; i++) {
			Member member = (variable.getMembers().get(i));
			boolean last = (i + 1) == count;

			// Data Member
			if (member instanceof DataMember) {
				// ((DataMember)member).getName().writeOut(bc, MODE_REF);
				getFactory().registerKey(bc, ((DataMember) member).getName(), false);

				if (last) writeValue(bc);
				adapter.invokeVirtual(Types.PAGE_CONTEXT, last ? SET_KEY : TOUCH_KEY);
				rtn = Types.OBJECT;
			}

			// UDF
			else if (member instanceof UDF) {
				if (last) throw new TransformerException("can't assign value to a user defined function", getStart());
				UDF udf = (UDF) member;
				getFactory().registerKey(bc, udf.getName(), false);
				ExpressionUtil.writeOutExpressionArray(bc, Types.OBJECT, udf.getArguments());
				adapter.invokeVirtual(Types.PAGE_CONTEXT, udf.hasNamedArgs() ? GET_FUNCTION_WITH_NAMED_ARGS_KEY : GET_FUNCTION_KEY);
				rtn = Types.OBJECT;
			}
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
			if (last) throw new TransformerException("can't assign value to a user defined function", getStart());
			return VariableImpl._writeOutFirstUDF(bc, (UDF) member, variable.getScope(), doOnlyScope);
		}
		else {
			if (last) throw new TransformerException("can't assign value to a built in function", getStart());
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
			writeValue(bc);
			adapter.invokeInterface(TypeScope.SCOPE_ARGUMENT, SET_ARGUMENT);
		}
		else {
			adapter.loadArg(0);
			TypeScope.invokeScope(adapter, Scope.SCOPE_UNDEFINED);
			getFactory().registerKey(bc, bc.getFactory().createLitString(ScopeFactory.toStringScope(variable.getScope(), "undefined")), false);
			writeValue(bc);
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