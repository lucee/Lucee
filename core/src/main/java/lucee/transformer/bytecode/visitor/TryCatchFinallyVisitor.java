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
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.statement.FlowControlFinal;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.Types;

public class TryCatchFinallyVisitor implements Opcodes {
	private OnFinally onFinally;
	private Label beginTry;
	private Label endTry;
	private Label endTry2;
	private Label l3;
	private Label l4;
	private Label l5;
	private Label l6;
	private Type type = Types.THROWABLE;
	private FlowControlFinal fcf;

	public TryCatchFinallyVisitor(OnFinally onFinally, FlowControlFinal fcf) {
		this.onFinally = onFinally;
		this.fcf = fcf;
	}

	public void visitTryBegin(BytecodeContext bc) {
		GeneratorAdapter ga = bc.getAdapter();
		beginTry = new Label();
		endTry = new Label();
		endTry2 = new Label();
		l3 = new Label();
		l4 = new Label();
		bc.pushOnFinally(onFinally);
		ga.visitLabel(beginTry);
	}

	public int visitTryEndCatchBeging(BytecodeContext bc) {
		GeneratorAdapter ga = bc.getAdapter();

		ga.visitTryCatchBlock(beginTry, endTry, endTry2, type.getInternalName());
		ga.visitLabel(endTry);
		l5 = new Label();
		ga.visitJumpInsn(GOTO, l5);
		ga.visitLabel(endTry2);
		int lThrow = ga.newLocal(type);
		ga.storeLocal(lThrow);
		// mv.visitVarInsn(ASTORE, 1);
		l6 = new Label();
		ga.visitLabel(l6);
		return lThrow;
	}

	public void visitCatchEnd(BytecodeContext bc) throws TransformerException {
		Label end = new Label();
		GeneratorAdapter ga = bc.getAdapter();
		bc.popOnFinally();
		ga.visitLabel(l3);
		ga.visitJumpInsn(GOTO, l5);
		ga.visitLabel(l4);
		int lThrow = ga.newLocal(Types.THROWABLE);
		ga.storeLocal(lThrow);
		// mv.visitVarInsn(ASTORE, 2);
		Label l8 = new Label();
		ga.visitLabel(l8);

		onFinally.writeOut(bc);

		ga.loadLocal(lThrow);
		ga.visitInsn(ATHROW);
		ga.visitLabel(l5);

		onFinally.writeOut(bc);
		if (fcf != null && fcf.getAfterFinalGOTOLabel() != null) {
			Label _end = new Label();
			ga.visitJumpInsn(Opcodes.GOTO, _end); // ignore when coming not from break/continue
			ASMUtil.visitLabel(ga, fcf.getFinalEntryLabel());
			onFinally.writeOut(bc);
			ga.visitJumpInsn(Opcodes.GOTO, fcf.getAfterFinalGOTOLabel());
			ga.visitLabel(_end);
		}

		ga.visitLabel(end);
		ga.visitTryCatchBlock(beginTry, l3, l4, null);

	}
}