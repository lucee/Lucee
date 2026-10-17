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
package lucee.transformer.bytecode.statement;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import lucee.runtime.type.Struct;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.expression.FunctionAsExpression;
import lucee.transformer.bytecode.statement.udf.Closure;
import lucee.transformer.bytecode.statement.udf.Function;
import lucee.transformer.bytecode.statement.udf.Lambda;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.Literal;

public final class ExpressionAsStatement extends StatementBaseNoFinal {

	private ExpressionBase expr;

	/**
	 * Constructor of the class
	 * 
	 * @param expr
	 */
	public ExpressionAsStatement(Expression expr) {
		super(expr.getFactory(), expr.getStart(), expr.getEnd());
		this.expr = (ExpressionBase) expr;
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.statement.StatementBase#_writeOut(lucee.transformer.bytecode.BytecodeContext)
	 */
	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		int rtn = bc.getReturn();
		// set rtn
		if (rtn > -1) {
			Type type = expr.writeOutAsType(bc, Expression.MODE_REF);
			bc.getAdapter().storeLocal(rtn);
		}
		else {
			// LDEV-4286: a closure or lambda written as a statement is never used (same as ACF, we do not allow this).
			// checked here and not in the parser, because a parse error would also break the speculative tag based parsing of script components
			if (expr instanceof FunctionAsExpression) {
				Function func = ((FunctionAsExpression) expr).getFunction();
				if (func instanceof Closure || func instanceof Lambda) {
					throw new TransformerException(bc, "Invalid construct: a " + (func instanceof Lambda ? "lambda" : "closure")
							+ " written as a statement is never used; assign it to a variable or pass it as an argument", getStart());
				}
			}
			if (!(expr instanceof Literal)) {
				Type type = expr.writeOutAsType(bc, Expression.MODE_VALUE);
				if (!type.equals(Types.VOID)) {
					ASMUtil.pop(adapter, type);
				}
			}
		}
	}

	/**
	 * @return the expr
	 */
	public Expression getExpr() {
		return expr;
	}

	@Override
	public void dump(Struct sct) {
		expr.dump(sct);
	}
}