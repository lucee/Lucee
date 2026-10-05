/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
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

import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.Context;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.BytecodeStatement;
import lucee.transformer.statement.Statement;

/**
 * A single Statement
 */
public abstract class StatementBase implements BytecodeStatement {

	private Position start;
	private Position end;
	private Statement parent;
	private int hasReturnChild = -1;
	private Factory factory;

	/**
	 * constructor of the class
	 * 
	 * @param factory
	 * @param start
	 * @param end
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
	 * @see lucee.lucee.transformer.statement.Statement#setParent(lucee.lucee.transformer.statement.Statement)
	 */
	@Override
	public void setParent(Statement parent) {
		this.parent = parent;
		if (hasReturnChild != -1 && parent != null) parent.setHasFlowController(hasReturnChild == 1);
	}

	/**
	 * write out the statement to adapter
	 * 
	 * @param c
	 * @throws TransformerException
	 */
	@Override
	public final void writeOut(Context c) throws TransformerException {
		BytecodeContext bc = (BytecodeContext) c;
		bc.visitLine(start);
		_writeOut(bc);
		// Skip emitting end line - it causes bytecode position conflicts when the next statement's
		// start line is emitted before any bytecode is generated. The start position is sufficient
		// for debugging and breakpoints. The end position (closing brace) isn't executable anyway.
	}

	/**
	 * write out the statement to the adapter
	 * 
	 * @param bc
	 * @throws TransformerException
	 */
	public abstract void _writeOut(BytecodeContext bc) throws TransformerException;

	/**
	 * sets the start value.
	 * 
	 * @param start The start position.
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

	@Override
	public void dump(Struct sct) {
		// start - use getter to allow subclasses to override
		Position s = getStart();
		if (s != null) {
			Struct sctStart = new StructImpl(StructImpl.TYPE_LINKED_NOT_SYNC, 8);
			sctStart.setEL(KeyConstants._line, s.line);
			sctStart.setEL(KeyConstants._column, s.displayColumn());
			sctStart.setEL(KeyConstants._offset, s.displayPosition());
			sct.setEL(KeyConstants._start, sctStart);
		}
		// end - use getter to allow subclasses to override
		Position e = getEnd();
		if (e != null) {
			Struct sctEnd = new StructImpl(StructImpl.TYPE_LINKED_NOT_SYNC, 8);
			sctEnd.setEL(KeyConstants._line, e.line);
			sctEnd.setEL(KeyConstants._column, e.displayColumn());
			sctEnd.setEL(KeyConstants._offset, e.displayPosition());
			sct.setEL(KeyConstants._end, sctEnd);
		}
	}
}