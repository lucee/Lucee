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
package lucee.transformer.bytecode.statement.udf;

import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.Body;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.Root;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.Literal;

public final class Lambda extends Function {

	public Lambda(Root root, Expression name, Expression returnType, Expression returnFormat, Expression output, Expression bufferOutput, int access, Expression displayName,
			Expression description, Expression hint, Expression secureJson, Expression verifyClient, Expression localMode, Literal cachedWithin, int modifier, Body body,
			Position start, Position end) {
		super(name, returnType, returnFormat, output, bufferOutput, access, displayName, description, hint, secureJson, verifyClient, localMode, cachedWithin, modifier, body,
				start, end);

	}

	public Lambda(Root root, String name, int access, int modifier, String returnType, Body body, Position start, Position end) {
		super(name, access, modifier, returnType, body, start, end);
	}

	@Override
	public final void _writeOut(BytecodeContext bc, int pageType) throws TransformerException {
		createFunction(bc, valueIndex, TYPE_LAMBDA);
	}

	@Override
	public int getType() {
		return TYPE_LAMBDA;
	}

}