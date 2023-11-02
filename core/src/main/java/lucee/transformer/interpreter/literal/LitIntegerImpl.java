package lucee.transformer.interpreter.literal;

import lucee.runtime.op.Caster;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.expression.ExprInt;
import lucee.transformer.expression.literal.LitInteger;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

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

	@Override
	public Class<?> _writeOut(InterpreterContext ic, int mode) {
		if (mode == MODE_REF) {
			ic.stack(Integer.valueOf(i));
			return Integer.class;
		}
		ic.stack(i);
		return int.class;
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
		return new Integer(i);
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
	 * @see lucee.transformer.expression.literal.Literal#getDouble(java.lang.Double)
	 */
	@Override
	public Double getDouble(Double defaultValue) {
		return getDouble();
	}

	private Double getDouble() {
		return new Double(i);
	}

	/**
	 * @see lucee.transformer.expression.literal.Literal#getBoolean(java.lang.Boolean)
	 */
	@Override
	public Boolean getBoolean(Boolean defaultValue) {
		return getBoolean();
	}
}