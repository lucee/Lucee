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

import lucee.transformer.Position;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.util.ExpressionUtil;
import lucee.transformer.bytecode.util.Types;

/**
 * @deprecated replaced with ForIntVisitor
 */
@Deprecated
public final class ForConditionIntVisitor implements Opcodes, LoopVisitor {

	private Label l0;
	private Label l1;
	private Label l2;
	private Label l3;
	private int i;
	private Label lend;
	private Label lbegin;

	public int visitBegin(GeneratorAdapter adapter, int start, boolean isLocal) {

		lend = new Label();
		lbegin = new Label();

		i = adapter.newLocal(Types.INT_VALUE);

		l0 = new Label();
		adapter.visitLabel(l0);
		if (isLocal) adapter.loadLocal(start, Types.INT_VALUE);
		else adapter.push(start);
		// mv.visitInsn(ICONST_1);
		adapter.visitVarInsn(ISTORE, i);
		l1 = new Label();
		adapter.visitLabel(l1);
		l2 = new Label();
		adapter.visitJumpInsn(GOTO, l2);
		l3 = new Label();
		adapter.visitLabel(l3);

		return i;
	}

	public void visitEndBeforeCondition(BytecodeContext bc, int step, boolean isLocal, Position startline) {
		GeneratorAdapter adapter = bc.getAdapter();

		adapter.visitLabel(lbegin);
		if (isLocal) {
			adapter.visitVarInsn(ILOAD, i);
			// adapter.loadLocal(i);
			adapter.loadLocal(step);
			adapter.visitInsn(IADD);
			// adapter.dup();
			adapter.visitVarInsn(ISTORE, i);

		}
		else adapter.visitIincInsn(i, step);
		ExpressionUtil.visitLine(bc, startline);
		adapter.visitLabel(l2);
	}

	public void visitEndAfterCondition(BytecodeContext bc) {
		GeneratorAdapter adapter = bc.getAdapter();

		adapter.ifZCmp(Opcodes.IFNE, l3);

		adapter.visitLabel(lend);

		adapter.visitLocalVariable("i", "I", null, l1, lend, i);

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