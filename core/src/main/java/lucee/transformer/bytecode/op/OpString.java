/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
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

import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.cast.Cast;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.Literal;

public final class OpString extends ExpressionBase implements ExprString {

	private ExprString right;
	private ExprString left;
	private boolean fromInterpolation = false;
	private char quoteChar; // Original quote character (' or ") for TemplateLiteral AST dump

	// String concat (String)
	private final static Method METHOD_CONCAT = new Method("concat", Types.STRING, new Type[] { Types.STRING });
	private static final int MAX_SIZE = 65535;

	private OpString(Expression left, Expression right) {
		super(left.getFactory(), left.getStart(), right.getEnd());
		this.left = left.getFactory().toExprString(left);
		this.right = left.getFactory().toExprString(right);
	}

	public void setFromInterpolation(boolean fromInterpolation) {
		this.fromInterpolation = fromInterpolation;
	}

	public boolean isFromInterpolation() {
		return fromInterpolation;
	}

	public void setQuoteChar(char quoteChar) {
		this.quoteChar = quoteChar;
	}

	public char getQuoteChar() {
		return quoteChar;
	}

	public static ExprString toExprString(Expression left, Expression right, boolean concatStatic) {
		if (concatStatic && left instanceof Literal && right instanceof Literal) {
			String l = ((Literal) left).getString();
			String r = ((Literal) right).getString();
			if ((l.length() + r.length()) <= MAX_SIZE) return left.getFactory().createLitString(l.concat(r), left.getStart(), right.getEnd());
		}
		return new OpString(left, right);
	}

	/**
	 * Create an OpString that represents string interpolation (e.g., "foo.#bar#.baz")
	 * rather than explicit concatenation (e.g., foo & bar).
	 * The result will be dumped as TemplateLiteral/InterpolatedString in the AST.
	 */
	public static ExprString toExprStringInterpolation(Expression left, Expression right) {
		// For interpolated strings, we don't want to merge literals at parse time
		// because we want to preserve the original structure for AST output
		ExprString result = toExprString(left, right, false);
		if (result instanceof OpString) {
			OpString opStr = (OpString) result;
			opStr.setFromInterpolation(true);
			// Also propagate interpolation flag from left operand if it's an OpString
			if (opStr.left instanceof OpString && ((OpString) opStr.left).isFromInterpolation()) {
				// Already set on this one, nothing more needed
			}
		}
		return result;
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		left.writeOut(bc, MODE_REF);
		right.writeOut(bc, MODE_REF);
		bc.getAdapter().invokeVirtual(Types.STRING, METHOD_CONCAT);
		return Types.STRING;
	}

	@Override
	public void dump(Struct sct) {
		super.dump(sct);

		if (fromInterpolation) {
			// Output as TemplateLiteral (like JS template literals)
			// Collect all parts: quasis (string literals) and expressions
			java.util.List<Expression> quasis = new java.util.ArrayList<>();
			java.util.List<Expression> expressions = new java.util.ArrayList<>();
			collectInterpolationParts(this, quasis, expressions);

			sct.setEL(KeyConstants._type, "TemplateLiteral");

			// Include quoteChar if set (for round-trip fidelity)
			if (quoteChar != 0) {
				sct.setEL("quoteChar", String.valueOf(quoteChar));
			}

			// Add quasis array
			Array quasisArr = new ArrayImpl();
			for (Expression q : quasis) {
				Struct qNode = new StructImpl(Struct.TYPE_LINKED);
				q.dump(qNode);
				quasisArr.appendEL(qNode);
			}
			sct.setEL("quasis", quasisArr);

			// Add expressions array
			Array exprsArr = new ArrayImpl();
			for (Expression e : expressions) {
				Struct eNode = new StructImpl(Struct.TYPE_LINKED);
				Expression expr = (e instanceof Cast) ? ((Cast) e).getExpr() : e;
				expr.dump(eNode);
				exprsArr.appendEL(eNode);
			}
			sct.setEL("expressions", exprsArr);
		}
		else {
			sct.setEL(KeyConstants._type, "BinaryExpression");
			sct.setEL(KeyConstants._operator, "CONCAT");
			// left - unwrap CastString wrapper for cleaner AST output
			{
				Struct sctLeft = new StructImpl(Struct.TYPE_LINKED);
				Expression leftExpr = (left instanceof Cast) ? ((Cast) left).getExpr() : left;
				leftExpr.dump(sctLeft);
				sct.setEL(KeyConstants._left, sctLeft);
			}
			// right - unwrap CastString wrapper for cleaner AST output
			{
				Struct sctRight = new StructImpl(Struct.TYPE_LINKED);
				Expression rightExpr = (right instanceof Cast) ? ((Cast) right).getExpr() : right;
				rightExpr.dump(sctRight);
				sct.setEL(KeyConstants._right, sctRight);
			}
		}
	}

	/**
	 * Recursively collect quasis (string literals) and expressions from an interpolation chain.
	 * Traverses left-to-right, building parallel arrays of quasis and expressions.
	 */
	private static void collectInterpolationParts(Expression expr, java.util.List<Expression> quasis, java.util.List<Expression> expressions) {
		Expression unwrapped = (expr instanceof Cast) ? ((Cast) expr).getExpr() : expr;

		if (unwrapped instanceof OpString) {
			OpString op = (OpString) unwrapped;
			if (op.fromInterpolation) {
				// Recursively process left side
				collectInterpolationParts(op.left, quasis, expressions);
				// Right side is either literal or expression
				Expression rightUnwrapped = (op.right instanceof Cast) ? ((Cast) op.right).getExpr() : op.right;
				if (rightUnwrapped instanceof Literal) {
					quasis.add(rightUnwrapped);
				}
				else {
					expressions.add(rightUnwrapped);
				}
				return;
			}
		}

		// Base case: not an OpString from interpolation
		if (unwrapped instanceof Literal) {
			quasis.add(unwrapped);
		}
		else {
			expressions.add(unwrapped);
		}
	}
}