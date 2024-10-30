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
package lucee.transformer.bytecode.visitor;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import lucee.transformer.Position;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.util.InterruptHandlerInjector;

public final class WhileVisitor implements LoopVisitor {

	private Label begin;
	private Label end;
	private int loopCounter;

	public void visitBeforeExpression(BytecodeContext bc) {
		begin = new Label();
		end = new Label();
		bc.getAdapter().visitLabel(begin);
		loopCounter = InterruptHandlerInjector.writeLoopInit(bc.getAdapter());
	}

	public void visitAfterExpressionBeforeBody(BytecodeContext bc) {
		bc.getAdapter().ifZCmp(Opcodes.IFEQ, end);
	}

	public void visitAfterBody(BytecodeContext bc, Position endline) {
		bc.getAdapter().visitJumpInsn(Opcodes.GOTO, begin);
		
		InterruptHandlerInjector.writeLoopBodyEnd(bc.getAdapter(), loopCounter, end, "during while loop");
		bc.getAdapter().visitLabel(end);
		bc.visitLine(endline);
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