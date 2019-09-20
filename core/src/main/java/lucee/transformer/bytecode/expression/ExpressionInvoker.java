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
package lucee.transformer.bytecode.expression;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.var.UDF;
import lucee.transformer.bytecode.util.ExpressionUtil;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.Invoker;
import lucee.transformer.expression.var.DataMember;
import lucee.transformer.expression.var.Member;

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

	/**
	 *
	 * @see lucee.transformer.expression.Invoker#addMember(lucee.transformer.expression.var.Member)
	 */
	@Override
	public void addMember(Member member) {
		members.add(member);
	}

	/**
	 *
	 * @see lucee.transformer.expression.Invoker#getMembers()
	 */
	@Override
	public List<Member> getMembers() {
		return members;
	}

	@Override
	public Member removeMember(int index) {
		return members.remove(index);
	}

}