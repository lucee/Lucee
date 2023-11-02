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
package lucee.transformer.bytecode.expression.var;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;

public final class DynAssign extends ExpressionBase {

	private ExprString name;
	private Expression value;

	// Object setVariable(String, Object)
	private final static Method METHOD_SET_VARIABLE = new Method("setVariable", Types.OBJECT, new Type[] { Types.STRING, Types.OBJECT });

	public DynAssign(Factory f, Position start, Position end) {
		super(f, start, end);
	}

	/**
	 * Constructor of the class
	 * 
	 * @param name
	 * @param value
	 */
	public DynAssign(Expression name, Expression value) {
		super(name.getFactory(), name.getStart(), name.getEnd());
		this.name = name.getFactory().toExprString(name);
		this.value = value;
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		adapter.loadArg(0);
		name.writeOut(bc, Expression.MODE_REF);
		value.writeOut(bc, Expression.MODE_REF);
		adapter.invokeVirtual(Types.PAGE_CONTEXT, METHOD_SET_VARIABLE);
		return Types.OBJECT;
	}

	/*
	 * *
	 *
	 * @see lucee.transformer.bytecode.expression.Expression#getType() / public int getType() { return
	 * Types._OBJECT; }
	 */

	/**
	 * @return the name
	 */
	public ExprString getName() {
		return name;
	}

	/**
	 * @return the value
	 */
	public Expression getValue() {
		return value;
	}

}