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
import lucee.transformer.expression.ExprFloat;
import lucee.transformer.expression.literal.LitFloat;

/**
 * Literal Double Value
 */
public final class LitFloatImpl extends ExpressionBase implements LitFloat, ExprFloat {

	private float f;

	/**
	 * constructor of the class
	 * 
	 * @param d
	 * @param line
	 */
	public LitFloatImpl(Factory fac, float f, Position start, Position end) {
		super(fac, start, end);
		this.f = f;
	}

	@Override
	public float getFloatValue() {
		return f;
	}

	@Override
	public Float getFloat() {
		return new Float(f);
	}

	@Override
	public String getString() {
		return Caster.toString(f);
	}

	/**
	 * @return return value as a Boolean Object
	 */
	public Boolean getBoolean() {
		return Caster.toBoolean(f);
	}

	/**
	 * @return return value as a boolean value
	 */
	public boolean getBooleanValue() {
		return Caster.toBooleanValue(f);
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) {
		GeneratorAdapter adapter = bc.getAdapter();
		adapter.push(f);
		if (mode == MODE_REF) {
			adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_FLOAT_FROM_FLOAT);
			return Types.FLOAT;
		}
		return Types.FLOAT_VALUE;
	}

	@Override
	public Double getDouble(Double defaultValue) {
		return new Double(getFloatValue());
	}

	@Override
	public Boolean getBoolean(Boolean defaultValue) {
		return getBoolean();
	}
}