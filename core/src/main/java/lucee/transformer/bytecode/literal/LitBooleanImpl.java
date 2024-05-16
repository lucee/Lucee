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

import java.math.BigDecimal;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import lucee.runtime.listener.AppListenerUtil;
import lucee.runtime.op.Caster;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.ExprBoolean;
import lucee.transformer.expression.literal.LitBoolean;

/**
 * Literal Boolean
 */
public final class LitBooleanImpl extends ExpressionBase implements LitBoolean, ExprBoolean {

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return b + "";
	}

	private boolean b;

	/**
	 * constructor of the class
	 * 
	 * @param b
	 * @param line
	 */
	public LitBooleanImpl(Factory f, boolean b, Position start, Position end) {
		super(f, start, end);
		this.b = b;
	}

	@Override
	public Number getNumber(Number defaultValue) {
		if (AppListenerUtil.getPreciseMath(null, null)) return b ? BigDecimal.ONE : BigDecimal.ZERO;
		return Caster.toDouble(b);
	}

	/**
	 * @return return value as double value
	 */
	public double getDoubleValue() {
		return Caster.toDoubleValue(b);
	}

	/**
	 * @return return value as Double Object
	 */
	public Double getDouble() {
		return Caster.toDouble(b);
	}

	/**
	 * @see lucee.transformer.expression.literal.Literal#getString()
	 */
	@Override
	public String getString() {
		return Caster.toString(b);
	}

	/**
	 * @return return value as a Boolean Object
	 */
	public Boolean getBoolean() {
		return Caster.toBoolean(b);
	}

	/**
	 * @return return value as a boolean value
	 */
	@Override
	public boolean getBooleanValue() {
		return b;
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.expression.ExpressionBase#_writeOut(org.objectweb.asm.commons.GeneratorAdapter,
	 *      int)
	 */
	@Override
	public Type _writeOut(BytecodeContext bc, int mode) {
		GeneratorAdapter adapter = bc.getAdapter();

		if (mode == MODE_REF) {
			adapter.getStatic(Types.BOOLEAN, b ? "TRUE" : "FALSE", Types.BOOLEAN);
			return Types.BOOLEAN;
		}
		adapter.visitInsn(b ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
		return Types.BOOLEAN_VALUE;
	}

	@Override
	public Boolean getBoolean(Boolean defaultValue) {
		return getBoolean();
	}
}