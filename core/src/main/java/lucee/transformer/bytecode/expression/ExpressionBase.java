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
package lucee.transformer.bytecode.expression;

import org.objectweb.asm.Type;

import lucee.commons.lang.ClassException;
import lucee.runtime.exp.TemplateException;
import lucee.transformer.Context;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.Expression;

/**
 * An Expression (Operation, Literal aso.)
 */
public abstract class ExpressionBase implements Expression {

	private Position start;
	private Position end;
	private Factory factory;

	public ExpressionBase(Factory factory, Position start, Position end) {
		this.start = start;
		this.end = end;
		this.factory = factory;
	}

	@Override
	public final Class<?> writeOut(Context c, int mode) throws TransformerException {
		try {
			return Types.toClass(writeOutAsType(c, mode));
		}
		catch (ClassException e) {
			throw new TransformerException(c, e, null);
		}
	}

	public final Type writeOutAsType(Context c, int mode) throws TransformerException {
		BytecodeContext bc = (BytecodeContext) c;
		bc.visitLine(start);
		Type type = _writeOut(bc, mode);
		bc.visitLine(end);
		return type;
	}

	/**
	 * write out the statement to the adapter
	 * 
	 * @param bc
	 * @param mode
	 * @return return Type of expression
	 * @throws TransformerException
	 */
	public abstract Type _writeOut(BytecodeContext bc, int mode) throws TransformerException;

	@Override
	public Factory getFactory() {
		return factory;
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
	public void setStart(Position start) {
		this.start = start;
	}

	@Override
	public void setEnd(Position end) {
		this.end = end;
	}

}