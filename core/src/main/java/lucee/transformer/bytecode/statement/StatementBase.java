/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.transformer.bytecode.statement;

import lucee.runtime.exp.TemplateException;
import lucee.transformer.Context;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.Statement;
import lucee.transformer.bytecode.util.ExpressionUtil;

/**
 * A single Statement
 */
public abstract class StatementBase implements Statement {

	private Position start;
	private Position end;
	private Statement parent;
	private int hasReturnChild = -1;
	private Factory factory;

	/**
	 * constructor of the class
	 * 
	 * @param line
	 */
	public StatementBase(Factory factory, Position start, Position end) {
		this.factory = factory;
		this.start = start;
		this.end = end;
	}

	@Override
	public Statement getParent() {
		return parent;
	}

	@Override
	public Factory getFactory() {
		return factory;
	}

	/**
	 * @see lucee.transformer.bytecode.Statement#setParent(lucee.transformer.bytecode.Statement)
	 */
	@Override
	public void setParent(Statement parent) {
		this.parent = parent;
		if (hasReturnChild != -1 && parent != null) parent.setHasFlowController(hasReturnChild == 1);
	}

	/**
	 * write out the statement to adapter
	 * 
	 * @param adapter
	 * @throws TemplateException
	 */
	@Override
	public final void writeOut(Context c) throws TransformerException {
		BytecodeContext bc = (BytecodeContext) c;
		ExpressionUtil.visitLine(bc, start);
		_writeOut(bc);
		ExpressionUtil.visitLine(bc, end);

	}

	/**
	 * write out the statement to the adapter
	 * 
	 * @param adapter
	 * @throws TransformerException
	 */
	public abstract void _writeOut(BytecodeContext bc) throws TransformerException;

	/**
	 * sets the line value.
	 * 
	 * @param line The line to set.
	 */
	@Override
	public void setStart(Position start) {
		this.start = start;
	}

	@Override
	public void setEnd(Position end) {
		this.end = end;
	}

	@Override
	public Position getStart() {
		return start;
	}

	@Override
	public Position getEnd() {
		return end;
	}

	@Override
	public boolean hasFlowController() {
		return hasReturnChild == 1;
	}

	@Override
	public void setHasFlowController(boolean hasReturnChild) {
		if (parent != null) parent.setHasFlowController(hasReturnChild);
		this.hasReturnChild = hasReturnChild ? 1 : 0;
	}
}