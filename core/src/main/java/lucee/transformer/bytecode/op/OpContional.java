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
package lucee.transformer.bytecode.op;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.ExpressionUtil;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.ExprBoolean;
import lucee.transformer.expression.Expression;

public final class OpContional extends ExpressionBase {

	private ExprBoolean cont;
	private Expression left;
	private Expression right;

	/**
	 *
	 * @see lucee.transformer.bytecode.expression.ExpressionBase#_writeOut(org.objectweb.asm.commons.GeneratorAdapter,
	 *      int)
	 */
	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();

		Label yes = new Label();
		Label end = new Label();

		// cont
		ExpressionUtil.visitLine(bc, cont.getStart());
		cont.writeOut(bc, MODE_VALUE);
		ExpressionUtil.visitLine(bc, cont.getEnd());
		adapter.visitJumpInsn(Opcodes.IFEQ, yes);

		// left
		ExpressionUtil.visitLine(bc, left.getStart());
		left.writeOut(bc, MODE_REF);
		ExpressionUtil.visitLine(bc, left.getEnd());
		adapter.visitJumpInsn(Opcodes.GOTO, end);

		// right
		ExpressionUtil.visitLine(bc, right.getStart());
		adapter.visitLabel(yes);
		right.writeOut(bc, MODE_REF);
		ExpressionUtil.visitLine(bc, right.getEnd());
		adapter.visitLabel(end);

		return Types.OBJECT;

	}

	private OpContional(Expression cont, Expression left, Expression right) {
		super(left.getFactory(), left.getStart(), right.getEnd());
		this.cont = left.getFactory().toExprBoolean(cont);
		this.left = left;
		this.right = right;
	}

	public static Expression toExpr(Expression cont, Expression left, Expression right) {
		return new OpContional(cont, left, right);
	}

	/*
	 * *
	 * 
	 * @see lucee.transformer.bytecode.expression.Expression#getType() / public int getType() { return
	 * Types._BOOLEAN; }
	 */
}