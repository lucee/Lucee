package lucee.transformer.interpreter.literal;

import lucee.runtime.op.Caster;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.expression.literal.LitLong;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

/**
 * Literal Double Value
 */
public final class LitLongImpl extends ExpressionBase implements LitLong {

	private long l;

	/**
	 * constructor of the class
	 * 
	 * @param d
	 * @param line
	 */
	public LitLongImpl(Factory f, long l, Position start, Position end) {
		super(f, start, end);
		this.l = l;
	}

	@Override
	public Class<?> _writeOut(InterpreterContext ic, int mode) {
		if (mode == MODE_REF) {
			ic.stack(Long.valueOf(l));
			return Long.class;
		}
		ic.stack(l);
		return long.class;
	}

	@Override
	public long getLongValue() {
		return l;
	}

	@Override
	public Long getLong() {
		return new Long(l);
	}

	@Override
	public String getString() {
		return Caster.toString(l);
	}

	/**
	 * @return return value as a Boolean Object
	 */
	public Boolean getBoolean() {
		return Caster.toBoolean(l);
	}

	/**
	 * @return return value as a boolean value
	 */
	public boolean getBooleanValue() {
		return Caster.toBooleanValue(l);
	}

	@Override
	public Double getDouble(Double defaultValue) {
		return getDouble();
	}

	private Double getDouble() {
		return new Double(l);
	}

	@Override
	public Boolean getBoolean(Boolean defaultValue) {
		return getBoolean();
	}
}