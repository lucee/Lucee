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

import lucee.transformer.bytecode.BytecodeContext;

// TODO testen wurde noch nicht getestet

public final class DoWhileVisitor implements LoopVisitor {

	private Label begin;
	private Label end;
	private Label beforeEnd;

	public void visitBeginBody(GeneratorAdapter mv) {
		end = new Label();
		beforeEnd = new Label();

		begin = new Label();
		mv.visitLabel(begin);
	}

	public void visitEndBodyBeginExpr(GeneratorAdapter mv) {
		mv.visitLabel(beforeEnd);
	}

	public void visitEndExpr(GeneratorAdapter mv) {
		mv.ifZCmp(Opcodes.IFNE, begin);
		mv.visitLabel(end);
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