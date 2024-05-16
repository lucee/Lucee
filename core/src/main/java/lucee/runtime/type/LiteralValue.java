package lucee.runtime.type;

import java.math.BigDecimal;

import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.CasterException;
import lucee.runtime.listener.ApplicationContextSupport;
import lucee.runtime.op.Caster;

/**
 * This class should only be used by created bytecode, because it does not the necessary checking at
 * runtime and expect the compiler did.
 * 
 */
public class LiteralValue {

	private static final double DBL_0 = 0d;
	private static final double DBL_1 = 1d;
	private static final double DBL_2 = 2d;
	private static final double DBL_3 = 3d;
	private static final double DBL_4 = 4d;
	private static final double DBL_5 = 5d;
	private static final double DBL_6 = 6d;
	private static final double DBL_7 = 7d;
	private static final double DBL_8 = 8d;
	private static final double DBL_9 = 9d;
	private static final double DBL_10 = 10d;

	private static final long LO_0 = 0l;
	private static final long LO_1 = 1l;
	private static final long LO_2 = 2l;
	private static final long LO_3 = 3l;
	private static final long LO_4 = 4l;
	private static final long LO_5 = 5l;
	private static final long LO_6 = 6l;
	private static final long LO_7 = 7l;
	private static final long LO_8 = 8l;
	private static final long LO_9 = 9l;
	private static final long LO_10 = 10l;

	private static final BigDecimal BG_2 = Caster.toBigDecimal(2L);
	private static final BigDecimal BG_3 = Caster.toBigDecimal(3L);
	private static final BigDecimal BG_4 = Caster.toBigDecimal(4L);
	private static final BigDecimal BG_5 = Caster.toBigDecimal(5L);
	private static final BigDecimal BG_6 = Caster.toBigDecimal(6L);
	private static final BigDecimal BG_7 = Caster.toBigDecimal(7L);
	private static final BigDecimal BG_8 = Caster.toBigDecimal(8L);
	private static final BigDecimal BG_9 = Caster.toBigDecimal(9L);

	public static Number toNumber(long l) {
		return toNumber(ThreadLocalPageContext.get(), l);

	}

	public static Number toNumber(PageContext pc, long l) {
		if (((ApplicationContextSupport) pc.getApplicationContext()).getPreciseMath()) return BigDecimal.valueOf(l);
		else return Double.valueOf(l);

	}

	public static Number toNumber(PageContext pc, double d) {
		if (((ApplicationContextSupport) pc.getApplicationContext()).getPreciseMath()) return BigDecimal.valueOf(d);
		else return Double.valueOf(d);

	}

	public static Number toNumber(String nbr) throws CasterException {
		return toNumber(ThreadLocalPageContext.get(), nbr);

	}

	public static Number toNumber(PageContext pc, String nbr) throws CasterException {// exception is not expected to bi driggerd
		if (((ApplicationContextSupport) pc.getApplicationContext()).getPreciseMath()) return Caster.toBigDecimal(nbr);
		else return Double.valueOf(nbr);
	}

	// ZERO
	public static Number l0() {
		if (((ApplicationContextSupport) ThreadLocalPageContext.get().getApplicationContext()).getPreciseMath()) return BigDecimal.ZERO;
		else return LO_0;
	}

	public static Number l0(PageContext pc) {
		if (((ApplicationContextSupport) pc.getApplicationContext()).getPreciseMath()) return BigDecimal.ZERO;
		else return LO_0;
	}

	public static Number l1() {
		if (((ApplicationContextSupport) ThreadLocalPageContext.get().getApplicationContext()).getPreciseMath()) return BigDecimal.ONE;
		else return LO_1;
	}

	public static Number l1(PageContext pc) {
		if (((ApplicationContextSupport) pc.getApplicationContext()).getPreciseMath()) return BigDecimal.ONE;
		else return LO_1;
	}

	public static Number l2() {
		if (((ApplicationContextSupport) ThreadLocalPageContext.get().getApplicationContext()).getPreciseMath()) return BG_2;
		else return LO_2;
	}

	public static Number l2(PageContext pc) {
		if (((ApplicationContextSupport) pc.getApplicationContext()).getPreciseMath()) return BG_2;
		else return LO_2;
	}

	public static Number l3() {
		if (((ApplicationContextSupport) ThreadLocalPageContext.get().getApplicationContext()).getPreciseMath()) return BG_3;
		else return LO_3;
	}

	public static Number l3(PageContext pc) {
		if (((ApplicationContextSupport) pc.getApplicationContext()).getPreciseMath()) return BG_3;
		else return LO_3;
	}

	public static Number l4() {
		if (((ApplicationContextSupport) ThreadLocalPageContext.get().getApplicationContext()).getPreciseMath()) return BG_4;
		else return LO_4;
	}

	public static Number l4(PageContext pc) {
		if (((ApplicationContextSupport) pc.getApplicationContext()).getPreciseMath()) return BG_4;
		else return LO_4;
	}

	public static Number l5() {
		if (((ApplicationContextSupport) ThreadLocalPageContext.get().getApplicationContext()).getPreciseMath()) return BG_5;
		else return LO_5;
	}

	public static Number l5(PageContext pc) {
		if (((ApplicationContextSupport) pc.getApplicationContext()).getPreciseMath()) return BG_5;
		else return LO_5;
	}

	public static Number l6() {
		if (((ApplicationContextSupport) ThreadLocalPageContext.get().getApplicationContext()).getPreciseMath()) return BG_6;
		else return LO_6;
	}

	public static Number l6(PageContext pc) {
		if (((ApplicationContextSupport) pc.getApplicationContext()).getPreciseMath()) return BG_6;
		else return LO_6;
	}

	public static Number l7() {
		if (((ApplicationContextSupport) ThreadLocalPageContext.get().getApplicationContext()).getPreciseMath()) return BG_7;
		else return LO_7;
	}

	public static Number l7(PageContext pc) {
		if (((ApplicationContextSupport) pc.getApplicationContext()).getPreciseMath()) return BG_7;
		else return LO_7;
	}

	public static Number l8() {
		if (((ApplicationContextSupport) ThreadLocalPageContext.get().getApplicationContext()).getPreciseMath()) return BG_8;
		else return LO_8;
	}

	public static Number l8(PageContext pc) {
		if (((ApplicationContextSupport) pc.getApplicationContext()).getPreciseMath()) return BG_8;
		else return LO_8;
	}

	public static Number l9() {
		if (((ApplicationContextSupport) ThreadLocalPageContext.get().getApplicationContext()).getPreciseMath()) return BG_9;
		else return LO_9;
	}

	public static Number l9(PageContext pc) {
		if (((ApplicationContextSupport) pc.getApplicationContext()).getPreciseMath()) return BG_9;
		else return LO_9;
	}

	public static Number l10() {
		if (((ApplicationContextSupport) ThreadLocalPageContext.get().getApplicationContext()).getPreciseMath()) return BigDecimal.TEN;
		else return LO_10;
	}

	public static Number l10(PageContext pc) {
		if (((ApplicationContextSupport) pc.getApplicationContext()).getPreciseMath()) return BigDecimal.TEN;
		else return LO_10;
	}

}
