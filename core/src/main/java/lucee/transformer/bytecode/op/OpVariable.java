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
package lucee.transformer.bytecode.op;

import lucee.transformer.Position;
import lucee.transformer.bytecode.expression.var.Assign;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.var.Variable;

public final class OpVariable extends Assign {

	public OpVariable(Variable variable, Expression value, Position end) {
		super(variable, value, end);
	}

	public OpVariable(Variable variable, double value, Position end) {
		super(variable, variable.getFactory().createLitDouble(value, variable.getEnd(), end), end);
	}
}