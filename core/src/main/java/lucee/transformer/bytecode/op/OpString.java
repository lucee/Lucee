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
package lucee.transformer.bytecode.op;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.Literal;

public final class OpString extends ExpressionBase implements ExprString {

	private ExprString right;
	private ExprString left;

	// String concat (String)
	private final static Method METHOD_CONCAT = new Method("concat", Types.STRING, new Type[] { Types.STRING });
	private static final int MAX_SIZE = 65535;

	private OpString(Expression left, Expression right) {
		super(left.getFactory(), left.getStart(), right.getEnd());
		this.left = left.getFactory().toExprString(left);
		this.right = left.getFactory().toExprString(right);
	}

	public static ExprString toExprString(Expression left, Expression right, boolean concatStatic) {
		if (concatStatic && left instanceof Literal && right instanceof Literal) {
			String l = ((Literal) left).getString();
			String r = ((Literal) right).getString();
			if ((l.length() + r.length()) <= MAX_SIZE) return left.getFactory().createLitString(l.concat(r), left.getStart(), right.getEnd());
		}
		return new OpString(left, right);
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		left.writeOut(bc, MODE_REF);
		right.writeOut(bc, MODE_REF);
		bc.getAdapter().invokeVirtual(Types.STRING, METHOD_CONCAT);
		return Types.STRING;
	}
}