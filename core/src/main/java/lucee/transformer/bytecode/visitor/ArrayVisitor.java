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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

public final class ArrayVisitor {

	public void visitBegin(GeneratorAdapter adapter, Type type, int length) {
		adapter.push(length);
		adapter.newArray(type);
	}

	public void visitBeginItem(GeneratorAdapter adapter, int index) {
		adapter.dup();
		adapter.push(index);
	}
	/*
	 * public void visitEndItem(BytecodeContext bc) { bc.getAdapter().visitInsn(Opcodes.AASTORE); }
	 */

	public void visitEndItem(GeneratorAdapter adapter) {
		adapter.visitInsn(Opcodes.AASTORE);
	}

	public void visitEnd() {
	}

}