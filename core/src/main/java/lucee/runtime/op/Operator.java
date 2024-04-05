// FUTURE remove, this class only exist for old bytecode , use instead OPUtil
package lucee.runtime.op;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;

/**
 * class to compare objects and primitive value types
 * 
 * 
 */
public final class Operator {

	@Deprecated
	public static int compare(Object left, Object right) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(TimeZone left, Object right) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(Locale left, Object right) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(Object left, Locale right) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(Object left, TimeZone right) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(Locale left, String right) {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(TimeZone left, String right) {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(String left, Locale right) {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(String left, TimeZone right) {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(Object left, String right) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(String left, Object right) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(Object left, double right) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(double left, Object right) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(Object left, boolean right) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(boolean left, Object right) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(Object left, Date right) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(Date left, Object right) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(Castable left, Object right) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(Object left, Castable right) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(String left, String right) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(String left, double right) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(String left, boolean right) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(String left, Date right) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(double left, String right) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(double left, double right) {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(long left, long right) {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(double left, boolean right) {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(double left, Date right) {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(boolean left, double right) {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(boolean left, String right) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(boolean left, boolean right) {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(boolean left, Date right) {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(Date left, String right) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(Date left, double right) {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(Date left, boolean right) {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static int compare(Date left, Date right) {
		return OpUtil.compare(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static boolean equals(Object left, Object right, boolean caseSensitive) throws PageException {
		return OpUtil.equals(ThreadLocalPageContext.get(), left, right, caseSensitive);
	}

	@Deprecated
	public static boolean equalsEL(Object left, Object right, boolean caseSensitive, boolean allowComplexValues) {
		return OpUtil.equalsEL(ThreadLocalPageContext.get(), left, right, caseSensitive, allowComplexValues);
	}

	@Deprecated
	public static boolean equalsComplexEL(Object left, Object right, boolean caseSensitive, boolean checkOnlyPublicAppearance) {
		return OpUtil.equalsComplexEL(ThreadLocalPageContext.get(), left, right, caseSensitive, checkOnlyPublicAppearance);
	}

	@Deprecated
	public static boolean equals(Object left, Object right, boolean caseSensitive, boolean allowComplexValues) throws PageException {
		return OpUtil.equals(ThreadLocalPageContext.get(), left, right, caseSensitive, allowComplexValues);
	}

	@Deprecated
	public static boolean equalsComplex(Object left, Object right, boolean caseSensitive, boolean checkOnlyPublicAppearance) throws PageException {
		return OpUtil.equalsComplex(ThreadLocalPageContext.get(), left, right, caseSensitive, checkOnlyPublicAppearance);
	}

	@Deprecated
	public static boolean ct(Object left, Object right) throws PageException {
		return OpUtil.ct(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static boolean eqv(Object left, Object right) throws PageException {
		return OpUtil.eqv(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static boolean eqv(boolean left, boolean right) {
		return OpUtil.eqv(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static boolean imp(Object left, Object right) throws PageException {
		return OpUtil.imp(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static boolean imp(boolean left, boolean right) {
		return OpUtil.imp(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static boolean nct(Object left, Object right) throws PageException {
		return OpUtil.nct(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static boolean eeq(Object left, Object right) throws PageException {
		return OpUtil.eeq(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static boolean neeq(Object left, Object right) throws PageException {
		return OpUtil.neeq(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static double exponent(Object left, Object right) throws PageException {
		return OpUtil.exponent(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static double exponent(double left, double right) {
		return OpUtil.exponent(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static double intdiv(double left, double right) {
		return OpUtil.intdiv(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static double div(double left, double right) {
		if (right == 0d) throw new ArithmeticException("Division by zero is not possible");
		return left / right;
	}

	@Deprecated
	public static float exponent(float left, float right) {
		return OpUtil.exponent(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static CharSequence concat(CharSequence left, CharSequence right) {
		return OpUtil.concat(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public final static double plus(double left, double right) {
		return OpUtil.plus(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static double minus(double left, double right) {
		return OpUtil.minus(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static double modulus(double left, double right) {
		return OpUtil.modulus(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static double divide(double left, double right) {
		return OpUtil.divide(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static double multiply(double left, double right) {
		return OpUtil.multiply(ThreadLocalPageContext.get(), left, right);
	}

	@Deprecated
	public static double bitand(double left, double right) {
		return Caster.toDoubleValue(OpUtil.bitand(ThreadLocalPageContext.get(), left, right));
	}

	@Deprecated
	public static double bitor(double left, double right) {
		return Caster.toDoubleValue(OpUtil.bitor(ThreadLocalPageContext.get(), left, right));
	}

	@Deprecated
	public static Double divRef(Object left, Object right) throws PageException {
		double r = Caster.toDoubleValue(right);
		if (r == 0d) throw new ArithmeticException("Division by zero is not possible");
		return Caster.toDouble(Caster.toDoubleValue(left) / r);
	}

	@Deprecated
	public static Double exponentRef(Object left, Object right) throws PageException {
		return Caster.toDouble(OpUtil.exponentRef(ThreadLocalPageContext.get(), left, right));
	}

	@Deprecated
	public static Double intdivRef(Object left, Object right) throws PageException {
		return Caster.toDouble(OpUtil.intdivRef(ThreadLocalPageContext.get(), left, right));
	}

	@Deprecated
	public static Double plusRef(Object left, Object right) throws PageException {
		return Caster.toDouble(OpUtil.plusRef(ThreadLocalPageContext.get(), left, right));
	}

	@Deprecated
	public static Double minusRef(Object left, Object right) throws PageException {
		return Caster.toDouble(OpUtil.minusRef(ThreadLocalPageContext.get(), left, right));
	}

	@Deprecated
	public static Double modulusRef(Object left, Object right) throws PageException {
		return Caster.toDouble(OpUtil.modulusRef(ThreadLocalPageContext.get(), left, right));
	}

	@Deprecated
	public static Double divideRef(Object left, Object right) throws PageException {
		return Caster.toDouble(OpUtil.divideRef(ThreadLocalPageContext.get(), left, right));
	}

	@Deprecated
	public static Double multiplyRef(Object left, Object right) throws PageException {
		return Caster.toDouble(OpUtil.multiplyRef(ThreadLocalPageContext.get(), left, right));
	}

	@Deprecated
	public static Double unaryPostPlus(PageContext pc, Collection.Key[] keys, double value) throws PageException {
		return Caster.toDouble(OpUtil.unaryPostPlus(pc, keys, value));
	}

	@Deprecated
	public static Double unaryPostPlus(Collection coll, Collection.Key key, double value) throws PageException {
		return Caster.toDouble(OpUtil.unaryPostPlus(ThreadLocalPageContext.get(), coll, key, value));
	}

	@Deprecated
	public static double unaryPoPl(PageContext pc, Collection.Key[] keys, double value) throws PageException {
		return Caster.toDoubleValue(OpUtil.unaryPoPl(pc, keys, value));
	}

	@Deprecated
	public static double unaryPoPl(PageContext pc, Collection.Key key, double value) throws PageException {
		return Caster.toDoubleValue(OpUtil.unaryPoPl(pc, key, value));
	}

	@Deprecated
	public static double unaryPoPl(Collection coll, Collection.Key key, double value) throws PageException {
		return Caster.toDoubleValue(OpUtil.unaryPoPl(ThreadLocalPageContext.get(), coll, key, value));
	}

	@Deprecated
	public static Double unaryPostMinus(PageContext pc, Collection.Key[] keys, double value) throws PageException {
		return Caster.toDouble(OpUtil.unaryPostMinus(pc, keys, value));
	}

	@Deprecated
	public static Double unaryPostMinus(Collection coll, Collection.Key key, double value) throws PageException {
		return Caster.toDouble(OpUtil.unaryPostMinus(ThreadLocalPageContext.get(), coll, key, value));
	}

	@Deprecated
	public static double unaryPoMi(PageContext pc, Collection.Key[] keys, double value) throws PageException {
		return Caster.toDoubleValue(OpUtil.unaryPoMi(pc, keys, value));
	}

	@Deprecated
	public static double unaryPoMi(PageContext pc, Collection.Key key, double value) throws PageException {
		return Caster.toDoubleValue(OpUtil.unaryPoMi(pc, key, value));
	}

	@Deprecated
	public static double unaryPoMi(Collection coll, Collection.Key key, double value) throws PageException {
		return Caster.toDoubleValue(OpUtil.unaryPoMi(ThreadLocalPageContext.get(), coll, key, value));
	}

	@Deprecated
	public static Double unaryPrePlus(PageContext pc, Collection.Key[] keys, double value) throws PageException {
		return Caster.toDouble(OpUtil.unaryPrePlus(pc, keys, value));
	}

	@Deprecated
	public static Double unaryPrePlus(Collection coll, Collection.Key key, double value) throws PageException {
		return Caster.toDouble(OpUtil.unaryPrePlus(ThreadLocalPageContext.get(), coll, key, value));
	}

	@Deprecated
	public static double unaryPrPl(PageContext pc, Collection.Key[] keys, double value) throws PageException {
		return Caster.toDoubleValue(OpUtil.unaryPrPl(pc, keys, value));
	}

	@Deprecated
	public static double unaryPrPl(PageContext pc, Collection.Key key, double value) throws PageException {
		return Caster.toDoubleValue(OpUtil.unaryPrPl(pc, key, value));
	}

	@Deprecated
	public static double unaryPrPl(Collection coll, Collection.Key key, double value) throws PageException {
		return Caster.toDoubleValue(OpUtil.unaryPrPl(ThreadLocalPageContext.get(), coll, key, value));
	}

	@Deprecated
	public static Double unaryPreMinus(PageContext pc, Collection.Key[] keys, double value) throws PageException {
		return Caster.toDouble(OpUtil.unaryPreMinus(pc, keys, value));
	}

	@Deprecated
	public static Double unaryPreMinus(Collection coll, Collection.Key key, double value) throws PageException {
		return Caster.toDouble(OpUtil.unaryPreMinus(ThreadLocalPageContext.get(), coll, key, value));
	}

	@Deprecated
	public static double unaryPrMi(PageContext pc, Collection.Key[] keys, double value) throws PageException {
		return Caster.toDoubleValue(OpUtil.unaryPrMi(ThreadLocalPageContext.get(), keys, value));
	}

	@Deprecated
	public static double unaryPrMi(PageContext pc, Collection.Key key, double value) throws PageException {
		return Caster.toDoubleValue(OpUtil.unaryPrMi(ThreadLocalPageContext.get(), key, value));
	}

	@Deprecated
	public static double unaryPrMi(Collection coll, Collection.Key key, double value) throws PageException {
		return Caster.toDoubleValue(OpUtil.unaryPrMi(ThreadLocalPageContext.get(), coll, key, value));
	}

	@Deprecated
	public static Double unaryPreMultiply(PageContext pc, Collection.Key[] keys, double value) throws PageException {
		return Caster.toDoubleValue(OpUtil.unaryPreMultiply(ThreadLocalPageContext.get(), keys, value));
	}

	@Deprecated
	public static Double unaryPreMultiply(Collection coll, Collection.Key key, double value) throws PageException {
		return Caster.toDouble(OpUtil.unaryPreMultiply(ThreadLocalPageContext.get(), coll, key, value));
	}

	@Deprecated
	public static double unaryPrMu(PageContext pc, Collection.Key[] keys, double value) throws PageException {
		return Caster.toDoubleValue(OpUtil.unaryPrMu(ThreadLocalPageContext.get(), keys, value));
	}

	@Deprecated
	public static double unaryPrMu(PageContext pc, Collection.Key key, double value) throws PageException {
		return Caster.toDoubleValue(OpUtil.unaryPrMu(ThreadLocalPageContext.get(), key, value));
	}

	@Deprecated
	public static double unaryPrMu(Collection coll, Collection.Key key, double value) throws PageException {
		return Caster.toDoubleValue(OpUtil.unaryPrMu(ThreadLocalPageContext.get(), coll, key, value));
	}

	@Deprecated
	public static Double unaryPreDivide(PageContext pc, Collection.Key[] keys, double value) throws PageException {
		return Caster.toDouble(OpUtil.unaryPreDivide(ThreadLocalPageContext.get(), keys, value));
	}

	@Deprecated
	public static Double unaryPreDivide(Collection coll, Collection.Key key, double value) throws PageException {
		return Caster.toDouble(OpUtil.unaryPreDivide(ThreadLocalPageContext.get(), coll, key, value));
	}

	@Deprecated
	public static double unaryPrDi(PageContext pc, Collection.Key[] keys, double value) throws PageException {
		return Caster.toDoubleValue(OpUtil.unaryPrDi(ThreadLocalPageContext.get(), keys, value));
	}

	@Deprecated
	public static double unaryPrDi(PageContext pc, Collection.Key key, double value) throws PageException {
		return Caster.toDoubleValue(OpUtil.unaryPrDi(ThreadLocalPageContext.get(), key, value));
	}

	@Deprecated
	public static double unaryPrDi(Collection coll, Collection.Key key, double value) throws PageException {
		return Caster.toDoubleValue(OpUtil.unaryPrDi(ThreadLocalPageContext.get(), coll, key, value));
	}

	@Deprecated
	public static String unaryPreConcat(PageContext pc, Collection.Key[] keys, String value) throws PageException {
		return OpUtil.unaryPreConcat(pc, keys, value);
	}

	@Deprecated
	public static String unaryPreConcat(Collection coll, Collection.Key key, String value) throws PageException {
		return OpUtil.unaryPreConcat(ThreadLocalPageContext.get(), coll, key, value);
	}

	@Deprecated
	public static String unaryPreConcat(PageContext pc, Collection.Key key, String value) throws PageException {
		return OpUtil.unaryPreConcat(pc, key, value);
	}
}