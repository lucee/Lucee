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

import lucee.transformer.bytecode.literal.LitStringImpl;
import lucee.transformer.bytecode.cast.CastString;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
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
	private static final Method CONSTR_NO_STRING = new Method("<init>", Types.VOID, new Type[] { });
	private static final Method CONSTR_STRING = new Method("<init>", Types.VOID, new Type[] { Types.STRING });
	private static final Method APPEND_OBJECT = new Method("append", Types.STRING_BUILDER, new Type[] { Types.OBJECT });
	private static final Method APPEND_STRING = new Method("append", Types.STRING_BUILDER, new Type[] { Types.STRING });
	private static final Method TO_STRING = new Method("toString", Types.STRING, new Type[] {});

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
		GeneratorAdapter adapter=bc.getAdapter();
		// new bytecode generation that does the same optimization for string concatenation that the Java compiler does.

		// only creates the stringbuilder object once
		adapter.newInstance(Types.STRING_BUILDER);
		adapter.dup();
		adapter.invokeConstructor(Types.STRING_BUILDER, CONSTR_NO_STRING); // TODO pass left into constructor
		_writeOutAppend(adapter, bc, mode, left);
		_writeOutAppend(adapter, bc, mode, right);

		// only runs toString once at the end
		adapter.invokeVirtual(Types.STRING_BUILDER, TO_STRING);
		return Types.STRING;
	}

	public void _writeOutAppend(GeneratorAdapter adapter, BytecodeContext bc, int mode, ExprString exprString) throws TransformerException {
		// we append all the expressions based on their type
		// stringbuilder is able to convert to string from other types when you pass object, so Lucee doesn't need to do that anymore
		if(exprString instanceof CastString) {
			CastString exprStringCastString = ((CastString) exprString);
//			exprStringCastString._writeOutRef(bc, MODE_REF);
			exprStringCastString._writeOut(bc, MODE_REF);
			adapter.invokeVirtual(Types.STRING_BUILDER, APPEND_STRING);
		}else if(exprString instanceof LitStringImpl) {
			LitStringImpl exprStringLit = ((LitStringImpl) exprString);
			exprStringLit._writeOut(bc, MODE_REF);
			adapter.invokeVirtual(Types.STRING_BUILDER, APPEND_STRING);
		}else if(exprString instanceof OpString){
			// the OpString is left & right pairs of expressions, so this code handles the recursion through this structure correctly without making duplicate objects/strings
			OpString op=((OpString) exprString);
//			op._writeOut(bc, MODE_REF);
			op._writeOutAppend(adapter, bc, mode, op.left); 
			op._writeOutAppend(adapter, bc, mode, op.right);
		}
		// TODO else required
	}
}