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

import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.Body;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.ExpressionUtil;
import lucee.transformer.expression.Expression;
import lucee.runtime.util.PageContextUtil;
import lucee.runtime.PageContext;
import lucee.transformer.bytecode.util.Types;


public final class For extends StatementBaseNoFinal implements FlowControlBreak, FlowControlContinue, HasBody {

	private Expression init;
	private Expression condition;
	private Expression update;
	private Body body;
	private static final Type TYPE_PCU = Type.getType(PageContextUtil.class);
	private static final Type TYPE_PC = Type.getType(PageContext.class);
	private static final Method METHOD_CRT = new Method("checkRequestTimeout", Type.VOID_TYPE, new Type[] {TYPE_PC});

	// private static final int I=1;

	Label beforeUpdate = new Label();
	Label end = new Label();
	private String label;

	/**
	 * Constructor of the class
	 * 
	 * @param init
	 * @param condition
	 * @param update
	 * @param body
	 * @param line
	 */
	public For(Factory f, Expression init, Expression condition, Expression update, Body body, Position start, Position end, String label) {
		super(f, start, end);
		this.init = init;
		this.condition = condition;
		this.update = update;
		this.body = body;
		this.label = label;
		body.setParent(this);

	}

	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		final int toIt = adapter.newLocal(Types.ITERATOR);
		Label beforeInit = new Label();
		Label afterInit = new Label();
		Label afterUpdate = new Label();
		adapter.push(0);
		adapter.storeLocal(toIt, Type.INT_TYPE);
		
		ExpressionUtil.visitLine(bc, getStart());
		adapter.visitLabel(beforeInit);
		if (init != null) {
			init.writeOut(bc, Expression.MODE_VALUE);
			adapter.pop();
		}
		adapter.visitJumpInsn(Opcodes.GOTO, afterUpdate);
		adapter.visitLabel(afterInit);

		body.writeOut(bc);

		adapter.visitLabel(beforeUpdate);
		// ExpressionUtil.visitLine(bc, getStartLine());
		if (update != null) {
			update.writeOut(bc, Expression.MODE_VALUE);
			ASMUtil.pop(adapter, update, Expression.MODE_VALUE);
		}

		// only test timeout once out of 10K iteration (performance optimization)
		adapter.iinc(toIt, 1);
		adapter.loadLocal(toIt);
		adapter.push(10000);
		adapter.ifICmp(Opcodes.IFLT, afterUpdate);
		// Check if the thread is timedout
		adapter.loadArg(0);
		adapter.invokeStatic(TYPE_PCU, METHOD_CRT);
		// reset counter
		adapter.push(0);
		adapter.storeLocal(toIt);
		adapter.visitLabel(afterUpdate);

		if (condition != null) condition.writeOut(bc, Expression.MODE_VALUE);
		else bc.getFactory().TRUE().writeOut(bc, Expression.MODE_VALUE);
		adapter.visitJumpInsn(Opcodes.IFNE, afterInit);
		// ExpressionUtil.visitLine(bc, getEndLine());
		adapter.visitLabel(end);

	}

	@Override
	public Label getBreakLabel() {
		return end;
	}

	@Override
	public Label getContinueLabel() {
		return beforeUpdate;
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