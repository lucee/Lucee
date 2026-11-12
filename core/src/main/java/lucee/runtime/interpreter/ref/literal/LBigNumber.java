package lucee.runtime.interpreter.ref.literal;

import java.math.BigDecimal;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.ref.Ref;
import lucee.runtime.interpreter.ref.util.RefUtil;
import lucee.runtime.op.Caster;

/**
 * Literal Number under precise math, the interpreter creates this instead of {@link LNumber} when
 * preciseMath is on for the expression being parsed
 */
public final class LBigNumber implements Literal {

	public static final LBigNumber ONE = new LBigNumber(BigDecimal.ONE);
	public static final LBigNumber MINUS_ONE = new LBigNumber(BigDecimal.valueOf(Double.valueOf(-1)));

	private final BigDecimal literal;

	public LBigNumber(BigDecimal literal) {
		this.literal = literal;
	}

	@Override
	public Object getValue(PageContext pc) {
		return literal;
	}

	@Override
	public Object getCollection(PageContext pc) {
		return literal;
	}

	@Override
	public String getTypeName() {
		return "number";
	}

	@Override
	public Object touchValue(PageContext pc) {
		return literal;
	}

	@Override
	public String getString(PageContext pc) {
		return toString();
	}

	@Override
	public String toString() {
		return Caster.toString(literal);
	}

	@Override
	public boolean eeq(PageContext pc, Ref other) throws PageException {
		if (other instanceof LBigNumber) {
			return literal.equals(((LBigNumber) other).literal);
		}
		return RefUtil.eeq(pc, this, other);
	}
}
