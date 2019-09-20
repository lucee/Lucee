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

public final class ForIntVisitor implements Opcodes, LoopVisitor {

	private Label beforeInit = new Label();
	private Label beforeExpr = new Label(), afterExpr = new Label();
	private Label beforeBody = new Label(), afterBody = new Label();
	private Label beforeUpdate = new Label(), afterUpdate = new Label();
	private int i;

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
	}

	private void forInit(GeneratorAdapter adapter, int start, boolean isLocal) {
		i = adapter.newLocal(Types.INT_VALUE);
		if (isLocal) adapter.loadLocal(start, Types.INT_VALUE);
		else adapter.push(start);
		adapter.visitVarInsn(ISTORE, i);
	}

	private void forUpdate(GeneratorAdapter adapter, int step, boolean isLocal) {
		if (isLocal) {
			adapter.visitVarInsn(ILOAD, i);
			adapter.loadLocal(step);
			adapter.visitInsn(IADD);
			adapter.visitVarInsn(ISTORE, i);
		}
		else adapter.visitIincInsn(i, step);
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