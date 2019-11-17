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
package lucee.transformer.bytecode.statement;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.Body;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.expression.ExprBoolean;
import lucee.transformer.expression.Expression;
import lucee.transformer.bytecode.util.Types;
import lucee.runtime.util.PageContextUtil;
import lucee.runtime.PageContext;

public final class While extends StatementBaseNoFinal implements FlowControlBreak, FlowControlContinue, HasBody {

	private ExprBoolean expr;
	private Body body;

	private Label begin = new Label();
	private Label end = new Label();
	private String label;
	private static final Type TYPE_PCU = Type.getType(PageContextUtil.class);
	private static final Type TYPE_PC = Type.getType(PageContext.class);
	private static final Method METHOD_CRT = new Method("checkRequestTimeout", Type.VOID_TYPE, new Type[] {TYPE_PC});

	/**
	 * Constructor of the class
	 * 
	 * @param expr
	 * @param body
	 * @param line
	 */
	public While(Expression expr, Body body, Position start, Position end, String label) {
		super(expr.getFactory(), start, end);
		this.expr = expr.getFactory().toExprBoolean(expr);
		this.body = body;
		body.setParent(this);
		this.label = label;
	}

	/**
	 * Constructor of the class
	 * 
	 * @param b
	 * @param body
	 * @param line
	 */
	public While(boolean b, Body body, Position start, Position end, String label) {
		this(body.getFactory().createLitBoolean(b), body, start, end, label);
	}

	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		final int toIt = adapter.newLocal(Types.ITERATOR);
		adapter.push(0);
		adapter.storeLocal(toIt, Type.INT_TYPE);
		adapter.visitLabel(begin);

		expr.writeOut(bc, Expression.MODE_VALUE);
		adapter.ifZCmp(Opcodes.IFEQ, end);

		body.writeOut(bc);

		// only test timeout once out of 10K iteration (performance optimization)
		adapter.iinc(toIt, 1);
		adapter.loadLocal(toIt);
		adapter.push(10000);
		// Check if the thread is interrupted
		adapter.ifICmp(Opcodes.IFLT, begin);
		adapter.push(0);
		adapter.storeLocal(toIt);
		// Check if the thread is timedout
		adapter.loadArg(0);
		adapter.invokeStatic(TYPE_PCU, METHOD_CRT);	
		adapter.visitJumpInsn(Opcodes.GOTO, begin);

		adapter.visitLabel(end);
	}

	@Override
	public Label getBreakLabel() {
		return end;
	}

	@Override
	public Label getContinueLabel() {
		return begin;
	}

	@Override
	public Body getBody() {
		return body;
	}

	@Override
	public String getLabel() {
		return label;
	}
}