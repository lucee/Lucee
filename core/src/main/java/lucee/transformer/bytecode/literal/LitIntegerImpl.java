/**
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.
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
package lucee.transformer.bytecode.literal;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import lucee.runtime.op.Caster;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.Methods;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.ExprInt;
import lucee.transformer.expression.literal.LitInteger;

/**
 * Literal Double Value
 */
public final class LitIntegerImpl extends ExpressionBase implements LitInteger, ExprInt {

	private int i;

	/**
	 * constructor of the class
	 * 
	 * @param d
	 * @param line
	 */
	public LitIntegerImpl(Factory f, int i, Position start, Position end) {
		super(f, start, end);
		this.i = i;
	}

	/**
	 * @return return value as int
	 */
	@Override
	public int geIntValue() {
		return i;
	}

	/**
	 * @return return value as Double Object
	 */
	@Override
	public Integer getInteger() {
		return Integer.valueOf(i);
	}

	@Override
	public Number getNumber() {
		return getInteger();
	}

	@Override
	public Number getNumber(Number defaultValue) {
		return getInteger();
	}

	/**
	 * @see lucee.transformer.expression.literal.Literal#getString()
	 */
	@Override
	public String getString() {
		return Caster.toString(i);
	}

	/**
	 * @return return value as a Boolean Object
	 */
	public Boolean getBoolean() {
		return Caster.toBoolean(i);
	}

	/**
	 * @return return value as a boolean value
	 */
	public boolean getBooleanValue() {
		return Caster.toBooleanValue(i);
	}

	/**
	 * @see lucee.transformer.expression.Expression#_writeOut(org.objectweb.asm.commons.GeneratorAdapter,
	 *      int)
	 */
	@Override
	public Type _writeOut(BytecodeContext bc, int mode) {
		GeneratorAdapter adapter = bc.getAdapter();
		adapter.push(i);
		if (mode == MODE_REF) {
			adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_INTEGER_FROM_INT);
			return Types.INTEGER;
		}
		return Types.INT_VALUE;
	}

	@Override
	public Boolean getBoolean(Boolean defaultValue) {
		return getBoolean();
	}
}