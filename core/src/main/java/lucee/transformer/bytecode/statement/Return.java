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
package lucee.transformer.bytecode.statement;

import java.util.Stack;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.util.ASMConstants;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.bytecode.visitor.OnFinally;
import lucee.transformer.expression.Expression;
import lucee.transformer.statement.Statement;

/**
 * Return Statement
 */
public final class Return extends StatementBaseNoFinal {

	Expression expr;

	/**
	 * Constructor of the class
	 * 
	 * @param f
	 * @param start
	 * @param end
	 */
	public Return(Factory f, Position start, Position end) {
		super(f, start, end);
		setHasFlowController(true);
		// expr=LitString.toExprString("", line);
	}

	/**
	 * Constructor of the class
	 * 
	 * @param expr
	 * @param start
	 * @param end
	 */
	public Return(Expression expr, Position start, Position end) {
		super(expr.getFactory(), start, end);
		this.expr = expr;
		setHasFlowController(true);
		// if(expr==null)expr=LitString.toExprString("", line);
	}

	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();

		if (expr == null) ASMConstants.NULL(adapter);
		else expr.writeOut(bc, Expression.MODE_REF);

		Stack<OnFinally> finallies = bc.getOnFinallyStack();
		int len = finallies.size();
		OnFinally onFinally;
		if (len > 0) {
			int rtn = adapter.newLocal(Types.OBJECT);
			adapter.storeLocal(rtn, Types.OBJECT);
			for (int i = len - 1; i >= 0; i--) {
				onFinally = finallies.get(i);
				if (!bc.insideFinally(onFinally)) onFinally.writeOut(bc);
			}
			adapter.loadLocal(rtn, Types.OBJECT);
		}
		if (bc.getMethod().getReturnType().equals(Types.VOID)) {
			adapter.pop();
			adapter.visitInsn(Opcodes.RETURN);
		}
		else adapter.visitInsn(Opcodes.ARETURN);
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.statement.StatementBase#setParent(lucee.lucee.transformer.statement.Statement)
	 */
	@Override
	public void setParent(Statement parent) {
		super.setParent(parent);
		parent.setHasFlowController(true);
	}

	@Override
	public void dump(Struct sct) {
		super.dump(sct);
		sct.setEL(KeyConstants._type, "ReturnStatement");

		// argument
		if (expr != null) {
			Struct argument = new StructImpl(StructImpl.TYPE_LINKED_NOT_SYNC, 8);
			expr.dump(argument);
			sct.setEL(KeyConstants._argument, argument);
		}
		else {
			sct.setEL(KeyConstants._argument, null);
		}
	}
}