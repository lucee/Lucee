package lucee.runtime.interpreter.ref.op;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.ref.Ref;
import lucee.runtime.interpreter.ref.RefSupport;
import lucee.runtime.op.Caster;

/**
 * Negate operation under precise math, the interpreter creates this instead of {@link Negate} when
 * preciseMath is on for the expression being parsed
 */
public final class BigNegate extends RefSupport implements Ref {

	private final Ref ref;

	public BigNegate(Ref ref) {
		this.ref = ref;
	}

	@Override
	public Object getValue(PageContext pc) throws PageException {
		return Caster.negate(Caster.toBigDecimal(ref.getValue(pc)));
	}

	@Override
	public String getTypeName() {
		return "operation";
	}

}
