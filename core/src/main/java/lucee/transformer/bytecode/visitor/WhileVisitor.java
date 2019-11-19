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
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import lucee.transformer.Position;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.util.ExpressionUtil;
import lucee.transformer.bytecode.util.Types;


public final class WhileVisitor implements LoopVisitor {

	private Label begin;
	private Label end;

	private final static Type TYPE_THREAD = Type.getType(Thread.class);
	private final static Type TYPE_EXCEPTION = Type.getType(InterruptedException.class);
	private final static Method METHOD_INTERRUPTED = new Method("interrupted", Type.BOOLEAN_TYPE, new Type[] {});
	private int toIt;

	public void visitBeforeExpression(BytecodeContext bc) {
		begin = new Label();
		end = new Label();
		toIt = bc.getAdapter().newLocal(Types.ITERATOR);
		bc.getAdapter().push(0);
		bc.getAdapter().storeLocal(toIt, Type.INT_TYPE);
		bc.getAdapter().visitLabel(begin);
	}

	public void visitAfterExpressionBeforeBody(BytecodeContext bc) {
		bc.getAdapter().ifZCmp(Opcodes.IFEQ, end);
	}

	public void visitAfterBody(BytecodeContext bc, Position endline) {
		// Check Once every 10K iteration
		bc.getAdapter().iinc(toIt, 1);
		bc.getAdapter().loadLocal(toIt);
		bc.getAdapter().push(10000);
		bc.getAdapter().ifICmp(Opcodes.IFLT, begin);
		// reset counter
		bc.getAdapter().push(0);
		bc.getAdapter().storeLocal(toIt);
		// Check if the thread is interrupted
		bc.getAdapter().invokeStatic(TYPE_THREAD, METHOD_INTERRUPTED);
		// Thread hasn't been interrupted, go to begin
		bc.getAdapter().ifZCmp(Opcodes.IFEQ, begin);
		// Thread interrupted, throw Interrupted Exception
		bc.getAdapter().throwException(TYPE_EXCEPTION, "Timeout in While loop");
		bc.getAdapter().visitLabel(end);
		ExpressionUtil.visitLine(bc, endline);
		Label endPreempt = new Label();
		// Check if the thread is interrupted
		bc.getAdapter().invokeStatic(TYPE_THREAD, METHOD_INTERRUPTED);
		// Thread hasn't been interrupted, go to afterUpdate
		bc.getAdapter().ifZCmp(Opcodes.IFEQ, endPreempt);
		// Thread interrupted, throw Interrupted Exception
		bc.getAdapter().throwException(TYPE_EXCEPTION, "Timeout in For loop");
		// ExpressionUtil.visitLine(bc, getStartLine());
		bc.getAdapter().visitLabel(endPreempt);		

	}

	/**
	 *
	 * @see lucee.transformer.bytecode.visitor.LoopVisitor#visitContinue(org.objectweb.asm.commons.GeneratorAdapter)
	 */
	@Override
	public void visitContinue(BytecodeContext bc) {
		bc.getAdapter().visitJumpInsn(Opcodes.GOTO, begin);
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.visitor.LoopVisitor#visitBreak(org.objectweb.asm.commons.GeneratorAdapter)
	 */
	@Override
	public void visitBreak(BytecodeContext bc) {
		bc.getAdapter().visitJumpInsn(Opcodes.GOTO, end);
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.visitor.LoopVisitor#getContinueLabel()
	 */
	@Override
	public Label getContinueLabel() {
		return begin;
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.visitor.LoopVisitor#getBreakLabel()
	 */
	@Override
	public Label getBreakLabel() {
		return end;
	}

}