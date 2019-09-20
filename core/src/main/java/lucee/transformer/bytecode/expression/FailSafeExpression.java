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
package lucee.transformer.bytecode.expression;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.Expression;

public class FailSafeExpression extends ExpressionBase implements Opcodes {

	private Expression expr;
	private Expression defaultValue;

	public FailSafeExpression(Expression expr, Expression defaultValue) {
		super(expr.getFactory(), expr.getStart(), expr.getEnd());
		this.expr = expr;
		this.defaultValue = defaultValue;
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		GeneratorAdapter mv = bc.getAdapter();

		int local = mv.newLocal(Types.OBJECT);

		{
			Label begin = new Label();
			Label onSuccess = new Label();
			Label onFail = new Label();
			Label end = new Label();

			mv.visitTryCatchBlock(begin, onSuccess, onFail, "java/lang/Throwable");
			mv.visitLabel(begin);

			expr.writeOut(bc, MODE_REF);
			mv.storeLocal(local);

			mv.visitLabel(onSuccess);
			mv.visitJumpInsn(GOTO, end);

			mv.visitLabel(onFail);
			// mv.visitVarInsn(ASTORE, 2);

			defaultValue.writeOut(bc, MODE_REF);
			mv.storeLocal(local);

			mv.visitLabel(end);
			mv.loadLocal(local);

			// mv.visitLocalVariable("e", "Ljava/lang/Throwable;", null, l4, l3, 3);

		}

		return Types.OBJECT;
	}

}