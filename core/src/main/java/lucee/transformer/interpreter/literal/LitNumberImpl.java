package lucee.transformer.interpreter.literal;

import lucee.runtime.op.Caster;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.expression.ExprNumber;
import lucee.transformer.expression.literal.LitNumber;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

/**
 * Literal Double Value
 */
public final class LitNumberImpl extends ExpressionBase implements LitNumber, ExprNumber {

	// public static final LitDouble ZERO=new LitDouble(0,null,null);

	private Number n;

	/**
	 * constructor of the class
	 * 
	 * @param d
	 * @param line
	 */
	public LitNumberImpl(Factory f, Number n, Position start, Position end) {
		super(f, start, end);
		this.n = n;
	}

	@Override
	public Class<?> _writeOut(InterpreterContext ic, int mode) {
		if (mode == MODE_REF) {
			ic.stack(n);
			return Number.class;
		}
		ic.stack(n);
		return double.class;
	}

	@Override
	public Number getNumber() {
		return n;
	}

	@Override
	public Number getNumber(Number defaultValue) {
		return n;
	}

	/**
	 * @see lucee.transformer.expression.literal.Literal#getString()
	 */
	@Override
	public String getString() {
		return Caster.toString(n);
	}

	/**
	 * @return return value as a Boolean Object
	 */
	public Boolean getBoolean() {
		return Caster.toBoolean(n);
	}

	/**
	 * @return return value as a boolean value
	 */
	public boolean getBooleanValue() {
		return Caster.toBooleanValue(n);
	}

	@Override
	public Boolean getBoolean(Boolean defaultValue) {
		return getBoolean();
	}
}