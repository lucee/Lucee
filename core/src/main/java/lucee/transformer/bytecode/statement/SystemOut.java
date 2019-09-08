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

import java.io.PrintStream;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.Expression;

public final class SystemOut extends StatementBaseNoFinal {

	// void println (Object)
	private final static Method METHOD_PRINTLN = new Method("println", Types.VOID, new Type[] { Types.OBJECT });

	Expression expr;

	/**
	 * constructor of the class
	 * 
	 * @param expr
	 * @param line
	 */
	public SystemOut(Expression expr, Position start, Position end) {
		super(expr.getFactory(), start, end);
		this.expr = expr;
	}

	/**
	 * @see lucee.transformer.bytecode.statement.StatementBase#_writeOut(org.objectweb.asm.commons.GeneratorAdapter)
	 */
	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		adapter.getStatic(Type.getType(System.class), "out", Type.getType(PrintStream.class));
		expr.writeOut(bc, Expression.MODE_REF);
		adapter.invokeVirtual(Type.getType(PrintStream.class), METHOD_PRINTLN);
	}
}