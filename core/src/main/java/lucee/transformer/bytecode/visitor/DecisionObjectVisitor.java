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

public final class DecisionObjectVisitor {

	public static final int EQ = Opcodes.IF_ACMPEQ;
	public static final int NEQ = Opcodes.IF_ACMPNE;

	private int operation;

	public void visitBegin() {

	}

	public void visitMiddle(int operation) {
		this.operation = operation;
	}

	public void visitEQ() {
		this.operation = EQ;
	}

	public void visitNEQ() {
		this.operation = NEQ;
	}

	public void visitEnd(BytecodeContext bc) {
		GeneratorAdapter adapter = bc.getAdapter();

		Label l1 = new Label();
		adapter.visitJumpInsn(operation, l1);
		// mv.visitJumpInsn(IF_ICMPGT, l1);
		adapter.visitInsn(Opcodes.ICONST_0);
		Label l2 = new Label();
		adapter.visitJumpInsn(Opcodes.GOTO, l2);
		adapter.visitLabel(l1);
		adapter.visitInsn(Opcodes.ICONST_1);
		adapter.visitLabel(l2);

	}
}