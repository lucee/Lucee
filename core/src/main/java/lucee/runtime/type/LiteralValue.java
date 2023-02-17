package lucee.runtime.type;

import java.math.BigDecimal;

import lucee.runtime.PageContext;
import lucee.runtime.exp.CasterException;
import lucee.runtime.listener.ApplicationContextSupport;
import lucee.runtime.op.Caster;

/**
 * This class should only be used by created bytecode, because it does not the necessary checking at
 * runtime and expect the compiler did.
 * 
 */
public class LiteralValue {

	public static Number toNumber(PageContext pc, long l) {

		if (((ApplicationContextSupport) pc.getApplicationContext()).getPreciseMath()) return BigDecimal.valueOf(l);
		else return Double.valueOf(l);

	}

	public static Number toNumber(PageContext pc, double d) {
		if (((ApplicationContextSupport) pc.getApplicationContext()).getPreciseMath()) return BigDecimal.valueOf(d);
		else return Double.valueOf(d);

	}

	public static Number toNumber(PageContext pc, String nbr) throws CasterException {// excpetion is not expected to bi driggerd
		if (((ApplicationContextSupport) pc.getApplicationContext()).getPreciseMath()) return Caster.toBigDecimal(nbr);
		else return Double.valueOf(nbr);
	}
}
