package lucee.transformer.interpreter.literal;

import lucee.runtime.op.Caster;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.expression.ExprDouble;
import lucee.transformer.expression.literal.LitDouble;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

/**
 * Literal Double Value
 */
public final class LitDoubleImpl extends ExpressionBase implements LitDouble, ExprDouble {

	// public static final LitDouble ZERO=new LitDouble(0,null,null);

	private double d;

	/**
	 * constructor of the class
	 * 
	 * @param d
	 * @param line
	 */
	public LitDoubleImpl(Factory f, double d, Position start, Position end) {
		super(f, start, end);

		this.d = d;
	}

	@Override
	public Class<?> _writeOut(InterpreterContext ic, int mode) {
		if (mode == MODE_REF) {
			ic.stack(Double.valueOf(d));
			return Double.class;
		}
		ic.stack(d);
		return double.class;
	}

	/**
	 * @return return value as double value
	 */
	@Override
	public double getDoubleValue() {
		return d;
	}

	/**
	 * @return return value as Double Object
	 */
	public Double getDouble() {
		return new Double(d);
	}

	/**
	 * @see lucee.transformer.expression.literal.Literal#getString()
	 */
	@Override
	public String getString() {
		return Caster.toString(d);
	}

	/**
	 * @return return value as a Boolean Object
	 */
	public Boolean getBoolean() {
		return Caster.toBoolean(d);
	}

	/**
	 * @return return value as a boolean value
	 */
	public boolean getBooleanValue() {
		return Caster.toBooleanValue(d);
	}

	@Override
	public Double getDouble(Double defaultValue) {
		return getDouble();
	}

	@Override
	public Boolean getBoolean(Boolean defaultValue) {
		return getBoolean();
	}
}