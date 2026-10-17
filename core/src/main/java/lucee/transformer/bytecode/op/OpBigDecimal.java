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

import java.math.BigDecimal;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.Factory;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.Expression;

public final class OpBigDecimal extends ExpressionBase {

	private static final Method TO_BIG_DECIMAL = new Method("toBigDecimal", Types.BIG_DECIMAL, new Type[] { Types.OBJECT });

	private static final Method _ADD = new Method("add", Types.BIG_DECIMAL, new Type[] { Types.BIG_DECIMAL });
	private static final Method _SUBSTRACT = new Method("subtract", Types.BIG_DECIMAL, new Type[] { Types.BIG_DECIMAL });
	private static final Method _DIVIDE = new Method("divide", Types.BIG_DECIMAL, new Type[] { Types.BIG_DECIMAL, Types.INT_VALUE, Types.INT_VALUE });
	private static final Method _MULTIPLY = new Method("multiply", Types.BIG_DECIMAL, new Type[] { Types.BIG_DECIMAL });
	private static final Method _REMAINER = new Method("remainder", Types.BIG_DECIMAL, new Type[] { Types.BIG_DECIMAL });

	private int operation;
	private Expression left;
	private Expression right;

	public OpBigDecimal(Expression left, Expression right, int operation) {
		super(left.getFactory(), left.getStart(), right.getEnd());
		this.left = left;
		this.right = right;
		this.operation = operation;
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.expression.ExpressionBase#_writeOut(lucee.transformer.bytecode.BytecodeContext,
	 *      int)
	 */
	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		return writeOutNumber(bc, mode);
	}

	public Type writeOutNumber(BytecodeContext bc, int mode) throws TransformerException {

		// LDEV-3020: BigDecimal has no exponent operation, the result of the number operation needs to be converted to a BigDecimal (that is what the caller expects)
		if (operation == Factory.OP_DBL_EXP) {
			new OpNumber(left, right, operation).writeOutNumber(bc, MODE_REF);
			bc.getAdapter().invokeStatic(Types.CASTER, TO_BIG_DECIMAL);
			return Types.BIG_DECIMAL;
		}

		GeneratorAdapter adapter = bc.getAdapter();

		toBigDecimal(bc, left);
		toBigDecimal(bc, right);

		// Caster.toBigDecimal("1").add(Caster.toBigDecimal("1"));
		if (operation == Factory.OP_DBL_PLUS) {
			adapter.invokeVirtual(Types.BIG_DECIMAL, _ADD);
		}
		else if (operation == Factory.OP_DBL_MINUS) {
			adapter.invokeVirtual(Types.BIG_DECIMAL, _SUBSTRACT);
		}
		else if (operation == Factory.OP_DBL_DIVIDE) {
			adapter.push(34);
			adapter.push(BigDecimal.ROUND_HALF_EVEN);
			adapter.invokeVirtual(Types.BIG_DECIMAL, _DIVIDE);
		}
		else if (operation == Factory.OP_DBL_INTDIV) {
			adapter.push(0);
			adapter.push(BigDecimal.ROUND_DOWN);
			adapter.invokeVirtual(Types.BIG_DECIMAL, _DIVIDE);
		}
		else if (operation == Factory.OP_DBL_MULTIPLY) {
			adapter.invokeVirtual(Types.BIG_DECIMAL, _MULTIPLY);
		}

		else if (operation == Factory.OP_DBL_MODULUS) {
			adapter.invokeVirtual(Types.BIG_DECIMAL, _REMAINER);
		}
		return Types.BIG_DECIMAL;
	}

	private static void toBigDecimal(BytecodeContext bc, Expression expr) throws TransformerException {
		expr.writeOut(bc, MODE_REF);
		if (expr instanceof OpBigDecimal) return;
		bc.getAdapter().invokeStatic(Types.CASTER, TO_BIG_DECIMAL);
	}

	public Expression getLeft() {
		return left;
	}

	public Expression getRight() {
		return right;
	}

	@Override
	public void dump(Struct sct) {
		super.dump(sct);
		sct.setEL(KeyConstants._type, "BinaryExpression");
		sct.setEL(KeyConstants._operator, OpNumber.toString(operation));
		// left
		{
			Struct sctLeft = new StructImpl(StructImpl.TYPE_LINKED_NOT_SYNC, 8);
			getLeft().dump(sctLeft);
			sct.setEL(KeyConstants._left, sctLeft);
		}
		// right
		{
			Struct sctRight = new StructImpl(StructImpl.TYPE_LINKED_NOT_SYNC, 8);
			getRight().dump(sctRight);
			sct.setEL(KeyConstants._right, sctRight);
		}
	}

}