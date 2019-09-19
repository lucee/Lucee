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
package lucee.transformer.bytecode.literal;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.runtime.type.scope.Scope;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.Page;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.ASMConstants;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.var.Variable;

public class NullConstant extends ExpressionBase {

	private static final Method FULL = new Method("full", Types.BOOLEAN_VALUE, new Type[] { Types.PAGE_CONTEXT });
	private static final Method GET = new Method("get", Types.OBJECT, new Type[] { Types.COLLECTION_KEY });

	public NullConstant(Factory f, Position start, Position end) {
		super(f, start, end);
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		GeneratorAdapter a = bc.getAdapter();

		// public static boolean full(PageContext pc)
		a.loadArg(0);
		bc.getAdapter().invokeStatic(Types.NULL_SUPPORT_HELPER, FULL);

		Label beforeNull = new Label();
		Label beforeGet = new Label();
		Label end = new Label();

		a.visitJumpInsn(Opcodes.IFNE, beforeNull);
		a.visitLabel(beforeGet);
		a.loadArg(0);
		a.invokeVirtual(Types.PAGE_CONTEXT, Page.UNDEFINED_SCOPE);
		a.getStatic(Types.KEY_CONSTANTS, "_NULL", Types.COLLECTION_KEY);
		a.invokeInterface(Types.UNDEFINED, GET);
		a.visitJumpInsn(Opcodes.GOTO, end);
		a.visitLabel(beforeNull);
		ASMConstants.NULL(bc.getAdapter());
		a.visitLabel(end);
		return Types.OBJECT;
	}

	public Variable toVariable() {
		Variable v = getFactory().createVariable(Scope.SCOPE_UNDEFINED, getStart(), getEnd());
		v.addMember(getFactory().createDataMember(getFactory().createLitString("null")));
		return v;
	}
}