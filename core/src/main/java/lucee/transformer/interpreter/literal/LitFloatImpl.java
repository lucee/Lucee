package lucee.transformer.interpreter.literal;

import lucee.runtime.op.Caster;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.expression.ExprFloat;
import lucee.transformer.expression.literal.LitFloat;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

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
	public LitFloatImpl(Factory f, float fl, Position start, Position end) {
		super(f, start, end);

		this.f = fl;
	}

	@Override
	public Class<?> _writeOut(InterpreterContext ic, int mode) {
		if (mode == MODE_REF) {
			ic.stack(Float.valueOf(f));
			return Float.class;
		}
		ic.stack(f);
		return float.class;
	}

	/**
	 * @see lucee.transformer.expression.literal.Literal#getString()
	 */
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
	public Boolean getBoolean(Boolean defaultValue) {
		return getBoolean();
	}

	@Override
	public float getFloatValue() {
		return f;
	}

	@Override
	public Float getFloat() {
		return f;
	}

	@Override
	public Number getNumber() {
		return getFloat();
	}

	@Override
	public Number getNumber(Number defaultValue) {
		return getFloat();
	}
}