/**
 *
 * Copyright (c) 2026, Lucee Association Switzerland. All rights reserved.
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
package lucee.transformer.bytecode.expression;

import org.objectweb.asm.Type;

import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;

/**
 * Expression that loads a JVM local variable of the generated method. It is used by statements
 * that keep a value in a local variable and need to hand it to another expression (for example an
 * assignment) as if it were a regular expression.
 */
public final class LocalExpression extends ExpressionBase {

	private final int local;
	private final Type type;

	public LocalExpression(Factory f, Position start, Position end, int local, Type type) {
		super(f, start, end);
		this.local = local;
		this.type = type;
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		bc.getAdapter().loadLocal(local, type);
		return type;
	}
}
