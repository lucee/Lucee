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

import lucee.transformer.bytecode.BytecodeContext;

public final class ConditionVisitor {

	private Label end;
	private Label endIf;

	public void visitBefore() {
		end = new Label();
	}

	public void visitWhenBeforeExpr() {
	}

	public void visitWhenAfterExprBeforeBody(BytecodeContext bc) {
		endIf = new Label();
		bc.getAdapter().ifZCmp(Opcodes.IFEQ, endIf);
	}

	public void visitWhenAfterBody(BytecodeContext bc) {
		bc.getAdapter().visitJumpInsn(Opcodes.GOTO, end);
		bc.getAdapter().visitLabel(endIf);
	}

	public void visitOtherviseBeforeBody() {
	}

	public void visitOtherviseAfterBody() {
	}

	public void visitAfter(BytecodeContext bc) {
		bc.getAdapter().visitLabel(end);
	}

}