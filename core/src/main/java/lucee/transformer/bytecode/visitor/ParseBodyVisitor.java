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

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.util.Types;

public final class ParseBodyVisitor {

	// void outputStart()
	public final static Method OUTPUT_START = new Method("outputStart", Types.VOID, new Type[] {});

	// void outputEnd()
	public final static Method OUTPUT_END = new Method("outputEnd", Types.VOID, new Type[] {});
	private TryFinallyVisitor tfv;

	public void visitBegin(BytecodeContext bc) {
		GeneratorAdapter adapter = bc.getAdapter();

		tfv = new TryFinallyVisitor(new OnFinally() {
			@Override
			public void _writeOut(BytecodeContext bc) {
				// ExpressionUtil.visitLine(bc, line);
				bc.getAdapter().loadArg(0);
				bc.getAdapter().invokeVirtual(Types.PAGE_CONTEXT, OUTPUT_END);
			}
		}, null);

		// ExpressionUtil.visitLine(bc, line);
		adapter.loadArg(0);
		adapter.invokeVirtual(Types.PAGE_CONTEXT, OUTPUT_START);
		tfv.visitTryBegin(bc);

	}

	public void visitEnd(BytecodeContext bc) throws TransformerException {

		tfv.visitTryEnd(bc);

	}
}