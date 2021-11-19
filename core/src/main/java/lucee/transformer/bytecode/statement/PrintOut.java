/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 * Copyright (c) 2016, Lucee Association Switzerland. All rights reserved.
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
import org.objectweb.asm.commons.Method;

import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;

public final class PrintOut extends StatementBaseNoFinal {

	// void write (String)
	private final static Method METHOD_WRITE = new Method("write", Types.VOID, new Type[] { Types.STRING });
	// void writePSQ (Object) TODO muss param 1 wirklich objekt sein
	private final static Method METHOD_WRITE_PSQ = new Method("writePSQ", Types.VOID, new Type[] { Types.OBJECT });

	private final static Method METHOD_WRITE_ENCODE_STRING = new Method("writeEncodeFor", Types.VOID, new Type[] { Types.STRING, Types.STRING });

	Expression expr;

	private boolean checkPSQ;

	private Expression encodeFor;

	/**
	 * constructor of the class
	 * 
	 * @param expr
	 * @param line
	 */
	public PrintOut(Expression expr, Position start, Position end) {
		super(expr.getFactory(), start, end);
		this.expr = expr.getFactory().toExprString(expr);
	}

	/**
	 * @see lucee.transformer.bytecode.Statement#_writeOut(org.objectweb.asm.commons.GeneratorAdapter)
	 */
	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		boolean doEncode = !checkPSQ && encodeFor != null;
		GeneratorAdapter adapter = bc.getAdapter();
		adapter.loadArg(0);
		if (doEncode) adapter.checkCast(Types.PAGE_CONTEXT_IMPL); // FUTURE keyword:encodefore remove

		ExprString es = bc.getFactory().toExprString(expr);
		boolean usedExternalizer = false;

		if (!usedExternalizer) es.writeOut(bc, Expression.MODE_REF);
		if (doEncode) {
			/*
			 * if(encodeForIsInt) { encodeFor.writeOut(bc, Expression.MODE_VALUE);
			 * adapter.visitInsn(Opcodes.I2S);
			 * adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL,METHOD_WRITE_ENCODE_SHORT); // FUTURE
			 * keyword:encodefore remove _IMPL } else {
			 */
			encodeFor.writeOut(bc, Expression.MODE_REF);
			adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, METHOD_WRITE_ENCODE_STRING); // FUTURE keyword:encodefore remove _IMPL
			// }
		}
		else adapter.invokeVirtual(Types.PAGE_CONTEXT, checkPSQ ? METHOD_WRITE_PSQ : METHOD_WRITE);
	}

	/**
	 * @return the expr
	 */
	public Expression getExpr() {
		return expr;
	}

	/**
	 * @param expr the expr to set
	 */
	public void setExpr(Expression expr) {
		this.expr = expr;
	}

	/**
	 * @param preserveSingleQuote the preserveSingleQuote to set
	 */
	public void setCheckPSQ(boolean checkPSQ) {
		this.checkPSQ = checkPSQ;
	}

	public void setEncodeFor(Expression encodeFor) {
		this.encodeFor = expr.getFactory().toExprString(encodeFor);
	}
}