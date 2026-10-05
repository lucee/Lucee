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
import lucee.transformer.bytecode.visitor.IfVisitor;

public final class TagScript extends TagBaseNoFinal {

	// Store positions for AST, but don't pass to super (prevents bytecode line emission)
	private Position astStart;
	private Position astEnd;

	/**
	 * Constructor stores positions for AST but passes null to super to prevent line number emissions.
	 * The cfscript tag itself is not executable - it's just a container for script content. Emitting
	 * line numbers for it causes conflicts when functions are extracted and written before the body,
	 * resulting in the same bytecode position having multiple line numbers.
	 */
	public TagScript(Factory f, Position start, Position end) {
		super(f, null, null);
		this.astStart = start;
		this.astEnd = end;
	}

	@Override
	public Position getStart() {
		return astStart;
	}

	@Override
	public void setStart(Position start) {
		this.astStart = start;
	}

	@Override
	public Position getEnd() {
		return astEnd;
	}

	@Override
	public void setEnd(Position end) {
		this.astEnd = end;
	}

	/**
	 * @see lucee.transformer.bytecode.statement.StatementBase#_writeOut(lucee.transformer.bytecode.BytecodeContext)
	 */
	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		IfVisitor ifv = new IfVisitor();
		ifv.visitBeforeExpression();
		bc.getAdapter().push(true);
		ifv.visitAfterExpressionBeforeBody(bc);
		getBody().writeOut(bc);
		ifv.visitAfterBody(bc);
	}
}