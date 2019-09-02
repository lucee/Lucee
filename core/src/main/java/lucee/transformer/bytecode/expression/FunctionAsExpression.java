/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.transformer.bytecode.expression;

import org.objectweb.asm.Type;

import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.statement.udf.Function;
import lucee.transformer.bytecode.util.Types;

public class FunctionAsExpression extends ExpressionBase {

	private Function function;

	public FunctionAsExpression(Function function) {
		super(function.getFactory(), function.getStart(), function.getEnd());
		this.function = function;
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		function._writeOut(bc);
		return Types.UDF_IMPL;
	}

	/**
	 * @return the closure
	 */
	public Function getFunction() {
		return function;
	}
}