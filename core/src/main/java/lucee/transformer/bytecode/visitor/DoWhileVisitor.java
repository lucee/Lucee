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
package lucee.transformer.bytecode.visitor;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.util.Types;

// TODO testen wurde noch nicht getestet

public final class DoWhileVisitor implements LoopVisitor {

	private Label begin;
	private Label end;
	private Label beforeEnd;

	private final static Type TYPE_THREAD = Type.getType(Thread.class);
	private final static Type TYPE_EXCEPTION = Type.getType(InterruptedException.class);
	private final static Method METHOD_INTERRUPTED = new Method("interrupted", Type.BOOLEAN_TYPE, new Type[] {});
	private int toIt;

	public void visitBeginBody(GeneratorAdapter mv) {
		end = new Label();
		beforeEnd = new Label();

		begin = new Label();
		toIt = mv.newLocal(Types.ITERATOR);
		mv.push(0);
		mv.storeLocal(toIt, Type.INT_TYPE);
		mv.visitLabel(begin);
	}

	public void visitEndBodyBeginExpr(GeneratorAdapter mv) {
		// Check Once every 10K iteration
		mv.iinc(toIt, 1);
		mv.loadLocal(toIt);
		mv.push(10000);
		mv.ifICmp(Opcodes.IFLT, beforeEnd);
		// reset counter
		mv.push(0);
		mv.storeLocal(toIt);
		// Check if the thread is interrupted
		mv.invokeStatic(TYPE_THREAD, METHOD_INTERRUPTED);
		// Thread hasn't been interrupted, go to beforeEnd
		mv.ifZCmp(Opcodes.IFEQ, beforeEnd);
		// Thread interrupted, throw Interrupted Exception
		mv.throwException(TYPE_EXCEPTION, "timeout in do {} while() loop");

		mv.visitLabel(beforeEnd);
	}

	public void visitEndExpr(GeneratorAdapter mv) {
		mv.ifZCmp(Opcodes.IFNE, begin);
		mv.visitLabel(end);
		// Preempt
		Label endPreempt = new Label();
		// Check if the thread is interrupted
		mv.invokeStatic(TYPE_THREAD, METHOD_INTERRUPTED);
		// Thread hasn't been interrupted, go to afterUpdate
		mv.ifZCmp(Opcodes.IFEQ, endPreempt);
		// Thread interrupted, throw Interrupted Exception
		mv.throwException(TYPE_EXCEPTION, "Timeout in For loop");
		// ExpressionUtil.visitLine(bc, getStartLine());
		mv.visitLabel(endPreempt);
	}

	/**
	 * @see lucee.transformer.bytecode.visitor.LoopVisitor#getBreakLabel()
	 */
	@Override
	public Label getBreakLabel() {
		return end;
	}

	/**
	 * @see lucee.transformer.bytecode.visitor.LoopVisitor#getContinueLabel()
	 */
	@Override
	public Label getContinueLabel() {
		return beforeEnd;
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.visitor.LoopVisitor#visitContinue(org.objectweb.asm.commons.GeneratorAdapter)
	 */
	@Override
	public void visitContinue(BytecodeContext bc) {
		bc.getAdapter().visitJumpInsn(Opcodes.GOTO, getContinueLabel());
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.visitor.LoopVisitor#visitBreak(org.objectweb.asm.commons.GeneratorAdapter)
	 */
	@Override
	public void visitBreak(BytecodeContext bc) {
		bc.getAdapter().visitJumpInsn(Opcodes.GOTO, getBreakLabel());
	}

}