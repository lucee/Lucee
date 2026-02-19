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
package lucee.transformer.bytecode.expression;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.var.FunctionMember;
import lucee.transformer.bytecode.expression.var.UDF;
import lucee.transformer.expression.var.Argument;
import lucee.transformer.bytecode.util.ExpressionUtil;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.Invoker;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.expression.literal.Literal;
import lucee.transformer.expression.var.DataMember;
import lucee.transformer.expression.var.Member;
import lucee.transformer.expression.var.Variable;

public final class ExpressionInvoker extends ExpressionBase implements Invoker {

	// Object getCollection (Object,String)
	private final static Method GET_COLLECTION = new Method("getCollection", Types.OBJECT, new Type[] { Types.OBJECT, Types.STRING });

	// Object get (Object,String)
	private final static Method GET = new Method("get", Types.OBJECT, new Type[] { Types.OBJECT, Types.STRING });

	// Object getFunction (Object,String,Object[])
	private final static Method GET_FUNCTION = new Method("getFunction", Types.OBJECT, new Type[] { Types.OBJECT, Types.STRING, Types.OBJECT_ARRAY });

	// Object getFunctionWithNamedValues (Object,String,Object[])
	private final static Method GET_FUNCTION_WITH_NAMED_ARGS = new Method("getFunctionWithNamedValues", Types.OBJECT,
			new Type[] { Types.OBJECT, Types.STRING, Types.OBJECT_ARRAY });

	private Expression expr;
	private List<Member> members = new ArrayList<Member>();

	public ExpressionInvoker(Expression expr) {
		super(expr.getFactory(), expr.getStart(), expr.getEnd());
		this.expr = expr;
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {

		GeneratorAdapter adapter = bc.getAdapter();

		Type rtn = Types.OBJECT;
		int count = members.size();

		for (int i = 0; i < count; i++) {
			adapter.loadArg(0);
		}

		expr.writeOut(bc, Expression.MODE_REF);

		for (int i = 0; i < count; i++) {
			Member member = members.get(i);

			// Data Member
			if (member instanceof DataMember) {
				((DataMember) member).getName().writeOut(bc, MODE_REF);
				adapter.invokeVirtual(Types.PAGE_CONTEXT, ((i + 1) == count) ? GET : GET_COLLECTION);
				rtn = Types.OBJECT;
			}

			// UDF
			else if (member instanceof UDF) {
				UDF udf = (UDF) member;

				udf.getName().writeOut(bc, MODE_REF);
				ExpressionUtil.writeOutExpressionArray(bc, Types.OBJECT, udf.getArguments());

				adapter.invokeVirtual(Types.PAGE_CONTEXT, udf.hasNamedArgs() ? GET_FUNCTION_WITH_NAMED_ARGS : GET_FUNCTION);
				rtn = Types.OBJECT;

			}
		}

		return rtn;
	}

	@Override
	public void addMember(Member member) {
		members.add(member);
	}

	@Override
	public List<Member> getMembers() {
		return members;
	}

	@Override
	public Member removeMember(int index) {
		return members.remove(index);
	}

	@Override
	public void addListener(Expression listener) {
		if (expr instanceof Variable) ((Variable) expr).addListener(listener);
		else if (expr instanceof Invoker) ((Invoker) expr).addListener(listener);
	}

	@Override
	public void dump(Struct sct) {
		// If no members, just dump the wrapped expression
		if (members.isEmpty()) {
			expr.dump(sct);
			return;
		}

		// Build the member expression chain iteratively
		// Start with the base expression
		Struct current = new StructImpl(Struct.TYPE_LINKED);
		expr.dump(current);

		// Add each member to the chain
		for (Member member : members) {
			Struct newNode = new StructImpl(Struct.TYPE_LINKED);

			if (member instanceof FunctionMember) {
				FunctionMember fm = (FunctionMember) member;
				Expression nameExpr = fm.getName();
				String funcName = nameExpr.toString();

				// Function call - create CallExpression
				newNode.setEL(KeyConstants._type, "CallExpression");

				// Create MemberExpression for callee (object.method)
				Struct callee = new StructImpl(Struct.TYPE_LINKED);
				callee.setEL(KeyConstants._type, "MemberExpression");
				callee.setEL(KeyConstants._computed, Boolean.FALSE);
				callee.setEL(KeyConstants._object, current);

				Struct property = new StructImpl(Struct.TYPE_LINKED);
				property.setEL(KeyConstants._type, "Identifier");
				property.setEL(KeyConstants._name, funcName);
				callee.setEL(KeyConstants._property, property);

				newNode.setEL(KeyConstants._callee, callee);

				// Add arguments
				Array arrArgs = new ArrayImpl();
				newNode.setEL(KeyConstants._arguments, arrArgs);
				for (Argument arg : fm.getSourceArguments()) {
					Struct sctArg = new StructImpl(Struct.TYPE_LINKED);
					arrArgs.appendEL(sctArg);
					arg.dump(sctArg);
				}
			}
			else if (member instanceof DataMember) {
				// Property access - create MemberExpression
				DataMember dm = (DataMember) member;
				newNode.setEL(KeyConstants._type, "MemberExpression");

				Expression memberName = dm.getName();
				boolean isComputed = (memberName instanceof LitString && ((LitString) memberName).fromBracket())
						|| !(memberName instanceof Literal);
				newNode.setEL(KeyConstants._computed, isComputed);
				newNode.setEL(KeyConstants._object, current);

				Struct property = new StructImpl(Struct.TYPE_LINKED);
				if (isComputed && memberName instanceof Literal) {
					property.setEL(KeyConstants._type, "StringLiteral");
					property.setEL(KeyConstants._value, memberName.toString());
				}
				else if (isComputed) {
					memberName.dump(property);
				}
				else {
					property.setEL(KeyConstants._type, "Identifier");
					property.setEL(KeyConstants._name, memberName.toString());
				}
				newNode.setEL(KeyConstants._property, property);
			}

			current = newNode;
		}

		// Copy the final result to the output struct
		Key[] keys = current.keys();
		for (Key key : keys) {
			sct.setEL(key, current.get(key, null));
		}
	}
}