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

import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.statement.FlowControlFinal;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.Types;

public class TryFinallyVisitor implements Opcodes {

	private Label beforeTry;
	private Label afterTry;
	private Label beforeFinally;
	private Label afterFinally;
	private int lThrow;
	private OnFinally onFinally;
	private FlowControlFinal fcf;

	public TryFinallyVisitor(OnFinally onFinally, FlowControlFinal fcf) {
		this.onFinally = onFinally;
		this.fcf = fcf;
	}

	public void visitTryBegin(BytecodeContext bc) {
		GeneratorAdapter ga = bc.getAdapter();
		bc.pushOnFinally(onFinally);
		beforeTry = new Label();
		afterTry = new Label();
		beforeFinally = new Label();
		afterFinally = new Label();

		ga.visitLabel(beforeTry);
	}

	public void visitTryEnd(BytecodeContext bc) throws TransformerException {
		GeneratorAdapter ga = bc.getAdapter();
		bc.popOnFinally();
		ga.visitJumpInsn(GOTO, beforeFinally);

		ga.visitLabel(afterTry);
		lThrow = ga.newLocal(Types.THROWABLE);
		ga.storeLocal(lThrow);

		onFinally.writeOut(bc);

		ga.loadLocal(lThrow);
		ga.visitInsn(ATHROW);

		ga.visitLabel(beforeFinally);

		onFinally.writeOut(bc);
		if (fcf != null && fcf.getAfterFinalGOTOLabel() != null) {
			Label _end = new Label();
			ga.visitJumpInsn(Opcodes.GOTO, _end); // ignore when coming not from break/continue
			ASMUtil.visitLabel(ga, fcf.getFinalEntryLabel());
			onFinally.writeOut(bc);
			ga.visitJumpInsn(Opcodes.GOTO, fcf.getAfterFinalGOTOLabel());
			ga.visitLabel(_end);
		}

		ga.visitLabel(afterFinally);

		ga.visitTryCatchBlock(beforeTry, afterTry, afterTry, null);
	}
}