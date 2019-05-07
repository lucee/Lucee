package lucee.transformer.interpreter.literal;

import lucee.runtime.op.Caster;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

/**
 * A Literal String
 */
public class LitStringImpl extends ExpressionBase implements LitString, ExprString {

	private String str;
	private boolean fromBracket;

	/**
	 * constructor of the class
	 * 
	 * @param str
	 * @param line
	 */
	public LitStringImpl(Factory f, String str, Position start, Position end) {
		super(f, start, end);
		this.str = str;
	}

	@Override
	public Class<?> _writeOut(InterpreterContext ic, int mode) {
		ic.stack(str);
		return String.class;
	}

	@Override
	public String getString() {
		return str;
	}

	@Override
	public Double getDouble(Double defaultValue) {
		return Caster.toDouble(getString(), defaultValue);
	}

	@Override
	public Boolean getBoolean(Boolean defaultValue) {
		return Caster.toBoolean(getString(), defaultValue);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof LitString)) return false;

		return str.equals(((LitStringImpl) obj).getString());
	}

	@Override
	public String toString() {
		return str;
	}

	@Override
	public void upperCase() {
		str = str.toUpperCase();
	}

	public void lowerCase() {
		str = str.toLowerCase();
	}

	@Override
	public LitString duplicate() {
		return new LitStringImpl(getFactory(), str, getStart(), getEnd());
	}

	@Override
	public void fromBracket(boolean fromBracket) {
		this.fromBracket = fromBracket;
	}

	@Override
	public boolean fromBracket() {
		return fromBracket;
	}
}