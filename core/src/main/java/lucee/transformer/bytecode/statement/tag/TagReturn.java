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
package lucee.transformer.bytecode.statement.tag;

import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.Statement;
import lucee.transformer.bytecode.statement.Return;
import lucee.transformer.expression.Expression;

public final class TagReturn extends TagBaseNoFinal {

	public TagReturn(Factory f, Position start, Position end) {
		super(f, start, end);
		setHasFlowController(true);
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.statement.tag.TagBase#_writeOut(org.objectweb.asm.commons.GeneratorAdapter)
	 */
	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		Attribute attr = getAttribute("expr");
		Expression expr = null;
		if (attr != null) expr = attr.getValue();
		new Return(expr, expr.getStart(), expr.getEnd()).writeOut(bc);
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.statement.StatementBase#setParent(lucee.transformer.bytecode.Statement)
	 */
	@Override
	public void setParent(Statement parent) {
		super.setParent(parent);
		parent.setHasFlowController(true);
	}

}