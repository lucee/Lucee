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

public final class ForVisitor implements Opcodes, LoopVisitor {

	private Label l0 = new Label();
	private Label l1 = new Label();
	private Label l2 = new Label();
	private Label l3 = new Label();
	private int i;
	private Label lend = new Label();
	private Label lbegin = new Label();
	private final static Type TYPE_THREAD = Type.getType(Thread.class);
	private final static Type TYPE_EXCEPTION = Type.getType(InterruptedException.class);
	private final static Method METHOD_INTERRUPTED = new Method("interrupted", Type.BOOLEAN_TYPE, new Type[] {});
	private int toIt;

	public int visitBegin(GeneratorAdapter adapter, int start, boolean isLocal) {
		adapter.visitLabel(l0);

		forInit(adapter, start, isLocal);

		adapter.visitLabel(l1);
		adapter.visitJumpInsn(GOTO, l2);
		adapter.visitLabel(l3);

		return i;
	}

	public void visitEnd(BytecodeContext bc, int end, boolean isLocal, Position startline) {
		GeneratorAdapter adapter = bc.getAdapter();

		adapter.visitLabel(lbegin);
		forUpdate(adapter);

		ExpressionUtil.visitLine(bc, startline);
		adapter.visitLabel(l2);
		adapter.visitVarInsn(ILOAD, i);

		if (isLocal) adapter.loadLocal(end);
		else adapter.push(end);
		adapter.visitJumpInsn(IF_ICMPLE, l3);

		adapter.visitLabel(lend);

		// adapter.visitLocalVariable("i", "I", null, l1, lend, i);
		Label endPreempt = new Label();
		// Check if the thread is interrupted
		adapter.invokeStatic(TYPE_THREAD, METHOD_INTERRUPTED);
		// Thread hasn't been interrupted, go to afterUpdate
		adapter.ifZCmp(Opcodes.IFEQ, endPreempt);
		// Thread interrupted, throw Interrupted Exception
		adapter.throwException(TYPE_EXCEPTION, "Timeout in For loop");
		// ExpressionUtil.visitLine(bc, getStartLine());
		adapter.visitLabel(endPreempt);

	}

	private void forUpdate(GeneratorAdapter adapter) {
		Label loopPreemptEnd= new Label();
		adapter.visitIincInsn(i, 1);
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

	private void forInit(GeneratorAdapter adapter, int start, boolean isLocal) {
		i = adapter.newLocal(Types.INT_VALUE);
		if (isLocal) adapter.loadLocal(start);
		else adapter.push(start);
		adapter.visitVarInsn(ISTORE, i);

		toIt = adapter.newLocal(Types.ITERATOR);
		adapter.push(0);
		adapter.storeLocal(toIt, Type.INT_TYPE);
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.visitor.LoopVisitor#visitContinue(org.objectweb.asm.commons.GeneratorAdapter)
	 */
	@Override
	public void visitContinue(BytecodeContext bc) {
		bc.getAdapter().visitJumpInsn(Opcodes.GOTO, lbegin);
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.visitor.LoopVisitor#visitBreak(org.objectweb.asm.commons.GeneratorAdapter)
	 */
	@Override
	public void visitBreak(BytecodeContext bc) {
		bc.getAdapter().visitJumpInsn(Opcodes.GOTO, lend);
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.visitor.LoopVisitor#getContinueLabel()
	 */
	@Override
	public Label getContinueLabel() {
		return lbegin;
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.visitor.LoopVisitor#getBreakLabel()
	 */
	@Override
	public Label getBreakLabel() {
		return lend;
	}
}