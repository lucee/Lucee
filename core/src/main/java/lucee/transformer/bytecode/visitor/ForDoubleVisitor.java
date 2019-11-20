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
package lucee.transformer.bytecode.visitor;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import lucee.transformer.Position;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.util.ExpressionUtil;
import lucee.transformer.bytecode.util.Types;

public final class ForDoubleVisitor implements Opcodes, LoopVisitor {

	public Label beforeInit = new Label();
	public Label beforeExpr = new Label(), afterExpr = new Label();
	public Label beforeBody = new Label(), afterBody = new Label();
	public Label beforeUpdate = new Label(), afterUpdate = new Label();
	private final static Type TYPE_THREAD = Type.getType(Thread.class);
	private final static Type TYPE_EXCEPTION = Type.getType(InterruptedException.class);
	private final static Method METHOD_INTERRUPTED = new Method("interrupted", Type.BOOLEAN_TYPE, new Type[] {});
	private int toIt;	
	public int i;

	public int visitBeforeExpression(GeneratorAdapter adapter, int start, int step, boolean isLocal) {
		// init
		adapter.visitLabel(beforeInit);
		forInit(adapter, start, isLocal);
		adapter.goTo(beforeExpr);

		// update
		adapter.visitLabel(beforeUpdate);
		forUpdate(adapter, step, isLocal);

		// expression
		adapter.visitLabel(beforeExpr);
		return i;
	}

	public void visitAfterExpressionBeginBody(GeneratorAdapter adapter) {
		adapter.ifZCmp(Opcodes.IFEQ, afterBody);
	}

	public void visitEndBody(BytecodeContext bc, Position line) {
		bc.getAdapter().goTo(beforeUpdate);
		ExpressionUtil.visitLine(bc, line);
		bc.getAdapter().visitLabel(afterBody);
		// adapter.visitLocalVariable("i", "I", null, beforeInit, afterBody, i);

		Label endPreempt = new Label();
		// Check if the thread is interrupted
		bc.getAdapter().invokeStatic(TYPE_THREAD, METHOD_INTERRUPTED);
		// Thread hasn't been interrupted, go to afterUpdate
		bc.getAdapter().ifZCmp(Opcodes.IFEQ, endPreempt);
		// Thread interrupted, throw Interrupted Exception
		bc.getAdapter().throwException(TYPE_EXCEPTION, "Timeout in ForInt loop");
		// ExpressionUtil.visitLine(bc, getStartLine());
		bc.getAdapter().visitLabel(endPreempt);
	}

	public void forInit(GeneratorAdapter adapter, int start, boolean isLocal) {
		toIt = adapter.newLocal(Types.ITERATOR);
		adapter.push(0);
		adapter.storeLocal(toIt, Type.INT_TYPE);

		i = adapter.newLocal(Types.DOUBLE_VALUE);
		if (isLocal) adapter.loadLocal(start, Types.DOUBLE_VALUE);
		else adapter.push((double) start);
		adapter.visitVarInsn(DSTORE, i);
	}

	public void forUpdate(GeneratorAdapter adapter, int step, boolean isLocal) {
		Label loopPreemptEnd= new Label();
		if (isLocal) {
			adapter.visitVarInsn(DLOAD, i);
			adapter.loadLocal(step);
			adapter.visitInsn(DADD);
			adapter.visitVarInsn(DSTORE, i);
		}
		else adapter.visitIincInsn(i, step);

		// Check Once every 10K iteration
		adapter.iinc(toIt, 1);
		adapter.loadLocal(toIt);
		adapter.push(10000);
		adapter.ifICmp(Opcodes.IFLT, loopPreemptEnd);
		// reset counter
		adapter.push(0);
		adapter.storeLocal(toIt);
		// Check if the thread is interrupted
		adapter.invokeStatic(TYPE_THREAD, METHOD_INTERRUPTED);
		// Thread hasn't been interrupted, go to begin
		adapter.ifZCmp(Opcodes.IFEQ, loopPreemptEnd);
		// Thread interrupted, throw Interrupted Exception
		adapter.throwException(TYPE_EXCEPTION, "Timeout in While loop");
		adapter.visitLabel(loopPreemptEnd);
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.visitor.LoopVisitor#visitContinue(org.objectweb.asm.commons.GeneratorAdapter)
	 */
	@Override
	public void visitContinue(BytecodeContext bc) {
		bc.getAdapter().visitJumpInsn(Opcodes.GOTO, beforeUpdate);
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.visitor.LoopVisitor#visitBreak(org.objectweb.asm.commons.GeneratorAdapter)
	 */
	@Override
	public void visitBreak(BytecodeContext bc) {
		bc.getAdapter().visitJumpInsn(Opcodes.GOTO, afterBody);
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.visitor.LoopVisitor#getContinueLabel()
	 */
	@Override
	public Label getContinueLabel() {
		return beforeUpdate;
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.visitor.LoopVisitor#getBreakLabel()
	 */
	@Override
	public Label getBreakLabel() {
		return afterBody;
	}

}