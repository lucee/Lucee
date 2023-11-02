package lucee.transformer.interpreter.literal;

import lucee.runtime.op.Caster;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.expression.ExprBoolean;
import lucee.transformer.expression.literal.LitBoolean;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

/**
 * Literal Boolean
 */
public final class LitBooleanImpl extends ExpressionBase implements LitBoolean, ExprBoolean {

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
	public Class<?> _writeOut(InterpreterContext ic, int mode) {
		if (mode == MODE_REF) {
			ic.stack(b ? Boolean.TRUE : Boolean.FALSE);
			return Boolean.class;
		}
		ic.stack(b);
		return boolean.class;
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
	 * @see lucee.transformer.expression.literal.Literal#getDouble(java.lang.Double)
	 */
	@Override
	public Double getDouble(Double defaultValue) {
		return getDouble();
	}

	/**
	 * @see lucee.transformer.expression.literal.Literal#getBoolean(java.lang.Boolean)
	 */
	@Override
	public Boolean getBoolean(Boolean defaultValue) {
		return getBoolean();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return b + "";
	}
}