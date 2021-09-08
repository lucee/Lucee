/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package lucee.runtime.op;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import lucee.commons.date.DateTimeUtil;
import lucee.commons.date.TimeZoneUtil;
import lucee.commons.lang.CFTypes;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.component.Member;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.i18n.LocaleFactory;
import lucee.runtime.interpreter.VariableInterpreter;
import lucee.runtime.listener.AppListenerUtil;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.ref.VariableReference;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.wrap.ListAsArray;
import lucee.runtime.type.wrap.MapAsStruct;

/**
 * class to compare objects and primitive value types
 * 
 * 
 */
public final class OpUtil {

	/**
	 * compares two Objects
	 * 
	 * @param left
	 * @param right
	 * @return different of objects as int
	 * @throws PageException
	 */
	public static int compare(PageContext pc, Object left, Object right) throws PageException {
		// print.dumpStack();
		if (left instanceof String) return compare(pc, (String) left, right);
		else if (left instanceof Number) return compare(pc, (Number) left, right);
		else if (left instanceof Boolean) return compare(pc, (Boolean) left, right);
		else if (left instanceof Date) return compare(pc, (Date) left, right);
		else if (left instanceof Castable) return compare(pc, (Castable) left, right);
		else if (left instanceof Locale) return compare(pc, (Locale) left, right);
		else if (left == null) return compare(pc, "", right);
		else if (left instanceof Enum) return compare(pc, ((Enum) left).toString(), right);
		else if (left instanceof Character) return compare(pc, ((Character) left).toString(), right);
		else if (left instanceof Calendar) return compare(pc, ((Calendar) left).getTime(), right);
		else if (left instanceof TimeZone) return compare(pc, (TimeZone) (left), right);
		else {
			return error(false, true);
		}
	}

	public static int compare(PageContext pc, TimeZone left, Object right) throws PageException {
		if (right instanceof String) return compare(pc, left, (String) right);
		else if (right instanceof Number) return compare(pc, left, Caster.toString(right));
		else if (right instanceof Boolean) return compare(pc, left, Caster.toString(right));
		else if (right instanceof Date) return compare(pc, left, Caster.toString(right));
		else if (right instanceof Castable) {
			if (isComparableComponent((Castable) right)) return -compareComponent(pc, (Castable) right, left);

			return compare(pc, left, ((Castable) right).castToString());
		}
		else if (right instanceof TimeZone) return left.toString().compareTo(right.toString());
		else if (right == null) return compare(pc, left, "");
		else if (right instanceof Enum) return compare(pc, left, ((Enum) right).toString());
		else if (right instanceof Character) return compare(pc, left, ((Character) right).toString());
		else if (right instanceof Calendar) return compare(pc, left, Caster.toString(((Calendar) right).getTime()));
		else if (right instanceof Locale) return compare(pc, left, Caster.toString(right));
		else return error(false, true);
	}

	public static int compare(PageContext pc, Locale left, Object right) throws PageException {
		if (right instanceof String) return compare(pc, left, (String) right);
		else if (right instanceof Number) return compare(pc, left, Caster.toString(right));
		else if (right instanceof Boolean) return compare(pc, left, Caster.toString(right));
		else if (right instanceof Date) return compare(pc, left, Caster.toString(right));
		else if (right instanceof Castable) {
			if (isComparableComponent((Castable) right)) return -compareComponent(pc, (Castable) right, left);
			return compare(pc, left, ((Castable) right).castToString());
		}
		else if (right instanceof Locale) return left.toString().compareTo(right.toString());
		else if (right == null) return compare(pc, left, "");
		else if (right instanceof Enum) return compare(pc, left, ((Enum) right).toString());
		else if (right instanceof Character) return compare(pc, left, ((Character) right).toString());
		else if (right instanceof Calendar) return compare(pc, left, Caster.toString(((Calendar) right).getTime()));
		else if (right instanceof TimeZone) return compare(pc, left, Caster.toString(right));
		else return error(false, true);
	}

	public static int compare(PageContext pc, Locale left, String right) {
		Locale rightLocale = LocaleFactory.getLocale(right, null);
		if (rightLocale == null) return LocaleFactory.toString(left).compareTo(right);
		return left.toString().compareTo(rightLocale.toString());
	}

	public static int compare(PageContext pc, TimeZone left, String right) {
		TimeZone rtz = TimeZoneUtil.toTimeZone(right, null);
		if (rtz == null) return TimeZoneUtil.toString(left).compareTo(right);
		return left.toString().compareTo(rtz.toString());
	}

	/**
	 * compares an Object with a String
	 * 
	 * @param left
	 * @param right
	 * @return difference as int
	 * @throws PageException
	 */
	public static int compare(PageContext pc, Object left, String right) throws PageException {
		if (left instanceof String) return compare(pc, (String) left, right);
		else if (left instanceof Number) return compare(pc, (Number) left, right);
		else if (left instanceof Boolean) return compare(pc, (Boolean) left, right);
		else if (left instanceof Date) return compare(pc, (Date) left, right);
		else if (left instanceof Castable) {
			if (isComparableComponent((Castable) left)) return compareComponent(pc, (Castable) left, right);
			return ((Castable) left).compareTo(right);
		}
		else if (left instanceof Locale) return compare(pc, (Locale) left, right);
		else if (left == null) return "".compareToIgnoreCase(right);
		else if (left instanceof Enum) return compare(pc, ((Enum) left).toString(), right);
		else if (left instanceof Character) return compare(pc, ((Character) left).toString(), right);
		else if (left instanceof Calendar) return compare(pc, ((Calendar) left).getTime(), right);
		else if (left instanceof TimeZone) return compare(pc, (TimeZone) left, right);

		else return error(false, true);
	}

	/**
	 * compares a String with an Object
	 * 
	 * @param left
	 * @param right
	 * @return difference as int
	 * @throws PageException
	 */
	public static int compare(PageContext pc, String left, Object right) throws PageException {
		if (right instanceof String) return compare(pc, left, (String) right);
		else if (right instanceof Number) return compare(pc, left, (Number) right);
		else if (right instanceof Boolean) return compare(pc, left, (Boolean) right ? BigDecimal.ONE : BigDecimal.ZERO);
		else if (right instanceof Date) return compare(pc, left, (Date) right);
		else if (right instanceof Castable) {
			if (isComparableComponent((Castable) right)) return -compareComponent(pc, (Castable) right, left);
			return -((Castable) right).compareTo(left);// compare(left ,((Castable)right).castToString());
		}
		else if (right instanceof Locale) return compare(pc, left, (Locale) right);
		else if (right == null) return left.compareToIgnoreCase("");
		else if (right instanceof Enum) return compare(pc, left, ((Enum) right).toString());
		else if (right instanceof Character) return compare(pc, left, ((Character) right).toString());
		else if (right instanceof Calendar) return compare(pc, left, ((Calendar) right).getTime());
		else if (right instanceof TimeZone) return compare(pc, left, (TimeZone) right);
		else return error(false, true);
	}

	/**
	 * compares an Object with a double
	 * 
	 * @param left
	 * @param right
	 * @return difference as int
	 * @throws PageException
	 */
	public static int compare(PageContext pc, Object left, Number right) throws PageException {
		if (left instanceof Number) return compare(pc, ((Number) left), right);
		else if (left instanceof String) return compare(pc, (String) left, right);
		else if (left instanceof Boolean) return compare(pc, ((Boolean) left).booleanValue() ? BigDecimal.ONE : BigDecimal.ZERO, right);
		else if (left instanceof Date) return compare(pc, (Date) left, right);
		else if (left instanceof Castable) {
			if (isComparableComponent((Castable) left)) return compareComponent(pc, (Castable) left, right);
			return ((Castable) left).compareTo(right.doubleValue());
		}
		// else if(left instanceof Castable) return compare(((Castable)left).castToDoubleValue() , right );
		else if (left instanceof Locale) return compare(pc, (Locale) left, Caster.toString(right));
		else if (left == null) return -1;
		else if (left instanceof Enum) return compare(pc, ((Enum) left).toString(), right);
		else if (left instanceof Character) return compare(pc, ((Character) left).toString(), right);
		else if (left instanceof Calendar) return compare(pc, ((Calendar) left).getTime(), right);
		else if (left instanceof TimeZone) return compare(pc, ((TimeZone) left), Caster.toString(right));
		else {
			return error(false, true);
		}
	}

	/**
	 * compares a double with an Object
	 * 
	 * @param left
	 * @param right
	 * @return difference as int
	 * @throws PageException
	 */
	public static int compare(PageContext pc, Number left, Object right) throws PageException {
		if (right instanceof Number) return compare(pc, left, ((Number) right));
		else if (right instanceof String) return compare(pc, left, (String) right);
		else if (right instanceof Boolean) return compare(pc, left, ((Boolean) right).booleanValue() ? BigDecimal.ONE : BigDecimal.ZERO);
		else if (right instanceof Date) return compare(pc, left, (Date) right);
		else if (right instanceof Castable) {
			if (isComparableComponent((Castable) right)) return -compareComponent(pc, (Castable) right, left);
			return -((Castable) right).compareTo(left.doubleValue());// compare(left ,((Castable)right).castToDoubleValue());
		}
		else if (right instanceof Locale) return compare(pc, Caster.toString(left), (Locale) right);
		else if (right == null) return 1;
		else if (right instanceof Enum) return compare(pc, left, ((Enum) right).toString());
		else if (right instanceof Character) return compare(pc, left, ((Character) right).toString());
		else if (right instanceof Calendar) return compare(pc, left, ((Calendar) right).getTime());
		else if (right instanceof TimeZone) return compare(pc, Caster.toString(left), ((TimeZone) right));
		else return error(true, false);
	}

	/**
	 * compares a boolean with an Object
	 * 
	 * @param left
	 * @param right
	 * @return difference as int
	 * @throws PageException
	 */
	public static int compare(PageContext pc, Boolean left, Object right) throws PageException {
		if (right instanceof Boolean) return compare(pc, left, (Boolean) right);
		else if (right instanceof String) return compare(pc, left, (String) right);
		else if (right instanceof Number) return compare(pc, left, (Number) right);
		else if (right instanceof Date) return compare(pc, left ? BigDecimal.ONE : BigDecimal.ZERO, (Date) right);
		else if (right instanceof Castable) {
			if (isComparableComponent((Castable) right)) return -compareComponent(pc, (Castable) right, left);
			return -((Castable) right).compareTo(left);// compare(left ,((Castable)right).castToBooleanValue());
		}
		else if (right instanceof Locale) return compare(pc, Caster.toString(left), ((Locale) right));
		else if (right == null) return 1;
		else if (right instanceof Enum) return compare(pc, left, ((Enum) right).toString());
		else if (right instanceof Character) return compare(pc, left, ((Character) right).toString());
		else if (right instanceof Calendar) return compare(pc, left ? BigDecimal.ONE : BigDecimal.ZERO, ((Calendar) right).getTime());
		else if (right instanceof TimeZone) return compare(pc, Caster.toString(left), ((TimeZone) right));
		else return error(true, false);
	}

	/**
	 * compares a Date with an Object
	 * 
	 * @param left
	 * @param right
	 * @return difference as int
	 * @throws PageException
	 */
	public static int compare(PageContext pc, Date left, Object right) throws PageException {
		if (right instanceof String) return compare(pc, left, (String) right);
		else if (right instanceof Number) return compare(pc, left, (Number) right);
		else if (right instanceof Boolean) return compare(pc, left, ((Boolean) right).booleanValue() ? BigDecimal.ONE : BigDecimal.ZERO);
		else if (right instanceof Date) return compare(pc, left, (Date) right);
		else if (right instanceof Castable) {
			if (isComparableComponent((Castable) right)) return -compareComponent(pc, (Castable) right, left);
			return -((Castable) right).compareTo(Caster.toDate(left, null));// compare(left ,(Date)((Castable)right).castToDateTime());
		}
		else if (right instanceof Locale) return compare(pc, Caster.toString(left), (Locale) right);
		else if (right == null) return compare(pc, left, "");
		else if (right instanceof Enum) return compare(pc, left, ((Enum) right).toString());
		else if (right instanceof Character) return compare(pc, left, ((Character) right).toString());
		else if (right instanceof Calendar) return compare(pc, left, ((Calendar) right).getTime());
		else if (right instanceof TimeZone) return compare(pc, Caster.toString(left), (TimeZone) right);
		else return error(true, false);
	}

	public static int compare(PageContext pc, Castable left, Object right) throws PageException {
		//
		if (isComparableComponent(left)) return compareComponent(pc, left, right);

		if (right instanceof String) return left.compareTo((String) right);
		else if (right instanceof Number) return left.compareTo(((Number) right).doubleValue());
		else if (right instanceof Boolean) return left.compareTo(((Boolean) right).booleanValue() ? 1d : 0d);
		else if (right instanceof Date) return left.compareTo(Caster.toDate(right));
		else if (right instanceof Castable) return compare(pc, left.castToString(), ((Castable) right).castToString());
		else if (right instanceof Locale) return compare(pc, left.castToString(), right);
		else if (right == null) return compare(pc, left.castToString(), "");
		else if (right instanceof Enum) return left.compareTo(((Enum) right).toString());
		else if (right instanceof Character) return left.compareTo(((Character) right).toString());
		else if (right instanceof Calendar) return left.compareTo(new DateTimeImpl(((Calendar) right).getTime()));
		else if (right instanceof TimeZone) return compare(pc, left.castToString(), right);
		else return error(true, false);
	}

	private static int compareComponent(PageContext pc, Castable c, Object o) throws PageException {
		return Caster.toIntValue(((Component) c).call(pc, KeyConstants.__compare, new Object[] { o }));
	}

	private static boolean isComparableComponent(Castable c) {
		if (!(c instanceof Component)) return false;

		Member member = ((Component) c).getMember(Component.ACCESS_PRIVATE, KeyConstants.__compare, false, false);
		if (!(member instanceof UDF)) return false;

		UDF udf = (UDF) member;
		if (udf.getReturnType() == CFTypes.TYPE_NUMERIC && udf.getFunctionArguments().length == 1) {
			return true;
		}

		return false;
	}

	/**
	 * compares a String with a String
	 * 
	 * @param left
	 * @param right
	 * @return difference as int
	 * @throws PageException
	 */
	public static int compare(PageContext pc, String left, String right) throws PageException {
		if (Decision.isNumber(left)) {
			if (Decision.isNumber(right)) {
				return compare(pc, Caster.toNumber(pc, left), Caster.toNumber(pc, right));
			}
			return compare(pc, Caster.toNumber(pc, left), right);
		}
		if (Decision.isBoolean(left)) return compare(pc, Caster.toBoolean(left), right);
		// NICE Date compare, perhaps datetime to double
		return left.compareToIgnoreCase(right);
	}

	/**
	 * compares a String with a double
	 * 
	 * @param left
	 * @param right
	 * @return difference as int
	 * @throws PageException
	 */
	public static int compare(PageContext pc, String left, Number right) throws PageException {
		if (Decision.isNumber(left)) return compare(pc, Caster.toNumber(pc, left), right);
		if (Decision.isBoolean(left)) return compare(pc, Caster.toBoolean(left), right);

		if (left.length() == 0) return -1;
		char leftFirst = left.charAt(0);
		if (leftFirst >= '0' && leftFirst <= '9') return left.compareToIgnoreCase(Caster.toString(right));
		return leftFirst - '0';
	}

	/**
	 * compares a String with a boolean
	 * 
	 * @param left
	 * @param right
	 * @return difference as int
	 * @throws PageException
	 */
	public static int compare(PageContext pc, String left, Boolean right) throws PageException {
		if (Decision.isBoolean(left)) return compare(pc, Caster.toBoolean(left), right);
		if (Decision.isNumber(left)) return compare(pc, Caster.toNumber(pc, left), right ? BigDecimal.ONE : BigDecimal.ZERO);

		if (left.length() == 0) return -1;
		char leftFirst = left.charAt(0);
		// print.ln(left+".compareTo("+Caster.toString(right)+")");
		// p(left);
		if (leftFirst >= '0' && leftFirst <= '9') return left.compareToIgnoreCase(Caster.toString(right ? 1D : 0D));
		return leftFirst - '0';
	}

	/**
	 * compares a double with a double
	 * 
	 * @param left
	 * @param right
	 * @return difference as int
	 */
	public static int compare(final PageContext pc, final Number left, final Number right) {
		if (AppListenerUtil.getPreciseMath(pc, null)) {
			return Caster.toBigDecimal(left).compareTo(Caster.toBigDecimal(right));
		}
		final double l = left.doubleValue();
		final double r = right.doubleValue();

		if (l < r) return -1;
		else if (l > r) return 1;
		else return 0;
	}

	/**
	 * compares a double with a boolean
	 * 
	 * @param left
	 * @param right
	 * @return difference as int
	 */
	public static int compare(PageContext pc, Number left, Boolean right) {
		return compare(pc, left, right ? BigDecimal.ONE : BigDecimal.ZERO);
	}

	/**
	 * compares a double with a Date
	 * 
	 * @param left
	 * @param right
	 * @return difference as int
	 */
	public static int compare(PageContext pc, Number left, Date right) {
		return compare(pc, (Date) DateTimeUtil.getInstance().toDateTime(left.doubleValue()), right);
	}

	/**
	 * compares a boolean with a double
	 * 
	 * @param left
	 * @param right
	 * @return difference as int
	 */
	public static int compare(PageContext pc, Boolean left, Number right) {
		return compare(pc, left ? BigDecimal.ONE : BigDecimal.ZERO, right);
	}

	/**
	 * compares a boolean with a boolean
	 * 
	 * @param left
	 * @param right
	 * @return difference as int
	 */
	public static int compare(PageContext pc, Boolean left, Boolean right) {
		if (left) return right ? 0 : 1;
		return right ? -1 : 0;
	}

	/**
	 * compares a boolean with a Date
	 * 
	 * @param left
	 * @param right
	 * @return difference as int
	 */
	public static int compare(PageContext pc, Boolean left, Date right) {
		return compare(pc, left ? BigDecimal.ONE : BigDecimal.ZERO, right);
	}

	/**
	 * compares a Date with a String
	 * 
	 * @param left
	 * @param right
	 * @return difference as int
	 * @throws PageException
	 */
	public static int compare(PageContext pc, Date left, String right) throws PageException {
		if (Decision.isNumber(right)) return compare(pc, left, Caster.toNumber(pc, right));
		Date dt = DateCaster.toDateAdvanced(right, DateCaster.CONVERTING_TYPE_OFFSET, null, null);
		if (dt != null) {
			return compare(pc, left, dt);
		}
		return Caster.toString(left).compareToIgnoreCase(right);
	}

	/**
	 * compares a Date with a boolean
	 * 
	 * @param left
	 * @param right
	 * @return difference as int
	 */
	public static int compare(PageContext pc, Date left, Boolean right) {
		return compare(pc, left, right ? BigDecimal.ONE : BigDecimal.ZERO);
	}

	/**
	 * compares a Date with a Date
	 * 
	 * @param left
	 * @param right
	 * @return difference as int
	 */
	public static int compare(PageContext pc, Date left, Date right) {
		long l = left.getTime() / 1000L;
		long r = right.getTime() / 1000L;

		if ((l) < (r)) return -1;
		else if ((l) > (r)) return 1;
		else return 0;
	}

	public static int compare(PageContext pc, Object left, Boolean right) throws PageException {
		return -compare(pc, right, left);
	}

	public static int compare(PageContext pc, Object left, Locale right) throws PageException {
		return -compare(pc, right, left);
	}

	public static int compare(PageContext pc, Object left, TimeZone right) throws PageException {
		return -compare(pc, right, left);
	}

	public static int compare(PageContext pc, String left, Locale right) {
		return -compare(pc, right, left);
	}

	public static int compare(PageContext pc, String left, TimeZone right) {
		return -compare(pc, right, left);
	}

	public static int compare(PageContext pc, Object left, Date right) throws PageException {
		return -compare(pc, right, left);
	}

	public static int compare(PageContext pc, Object left, Castable right) throws PageException {
		return -compare(pc, right, left);
	}

	public static int compare(PageContext pc, String left, Date right) throws PageException {
		return -compare(pc, right, left);
	}

	public static int compare(PageContext pc, Number left, String right) throws PageException {
		return -compare(pc, right, left);
	}

	public static int compare(PageContext pc, Date left, Number right) {
		return -compare(pc, right, left);
	}

	public static int compare(PageContext pc, Boolean left, String right) throws PageException {
		return -compare(pc, right, left);
	}

	private static int error(boolean leftIsOk, boolean rightIsOk) throws ExpressionException {
		// TODO remove this method
		throw new ExpressionException("can't compare complex object types as simple value");
	}

	/**
	 * Method to compare to different values, return true of objects are same otherwise false
	 * 
	 * @param left left value to compare
	 * @param right right value to compare
	 * @param caseSensitive check case sensitive or not
	 * @return is same or not
	 * @throws PageException
	 */
	public static boolean equals(PageContext pc, Object left, Object right, boolean caseSensitive) throws PageException {
		if (caseSensitive) {
			try {
				return Caster.toString(left).equals(Caster.toString(right));
			}
			catch (ExpressionException e) {
				return compare(pc, left, right) == 0;
			}
		}
		return compare(pc, left, right) == 0;
	}

	public static boolean equalsEL(PageContext pc, Object left, Object right, boolean caseSensitive, boolean allowComplexValues) {
		if (!allowComplexValues || (Decision.isSimpleValue(left) && Decision.isSimpleValue(right))) {
			try {
				return equals(pc, left, right, caseSensitive);
			}
			catch (PageException e) {
				return false;
			}
		}
		return equalsComplexEL(pc, left, right, caseSensitive, false);
	}

	public static boolean equalsComplexEL(PageContext pc, Object left, Object right, boolean caseSensitive, boolean checkOnlyPublicAppearance) {
		return _equalsComplexEL(pc, null, left, right, caseSensitive, checkOnlyPublicAppearance);
	}

	public static boolean _equalsComplexEL(PageContext pc, Set<Object> done, Object left, Object right, boolean caseSensitive, boolean checkOnlyPublicAppearance) {
		if (left == right) return true;
		if (Decision.isSimpleValue(left) && Decision.isSimpleValue(right)) {
			try {
				return equals(pc, left, right, caseSensitive);
			}
			catch (PageException e) {
				return false;
			}
		}
		if (left == null) return right == null;

		if (done == null) done = new HashSet<Object>();
		else if (done.contains(left) && done.contains(right)) return true;
		done.add(left);
		done.add(right);

		if (left instanceof Component && right instanceof Component)
			return __equalsComplexEL(pc, done, (Component) left, (Component) right, caseSensitive, checkOnlyPublicAppearance);

		if (left instanceof UDF && right instanceof UDF) return __equalsComplexEL(pc, done, (UDF) left, (UDF) right, caseSensitive, checkOnlyPublicAppearance);

		if (left instanceof Collection && right instanceof Collection)
			return __equalsComplexEL(pc, done, (Collection) left, (Collection) right, caseSensitive, checkOnlyPublicAppearance);

		if (left instanceof List && right instanceof List)
			return __equalsComplexEL(pc, done, ListAsArray.toArray((List) left), ListAsArray.toArray((List) right), caseSensitive, checkOnlyPublicAppearance);

		if (left instanceof Map && right instanceof Map)
			return __equalsComplexEL(pc, done, MapAsStruct.toStruct((Map) left, true), MapAsStruct.toStruct((Map) right, true), caseSensitive, checkOnlyPublicAppearance);
		return left.equals(right);
	}

	private static boolean __equalsComplexEL(PageContext pc, Set<Object> done, UDF left, UDF right, boolean caseSensitive, boolean checkOnlyPublicAppearance) {
		if (left == null || right == null) {
			if (left == right) return true;
			return false;
		}
		return left.equals(right);
	}

	private static boolean __equalsComplexEL(PageContext pc, Set<Object> done, Component left, Component right, boolean caseSensitive, boolean checkOnlyPublicAppearance) {
		if (left == null || right == null) {
			if (left == right) return true;
			return false;
		}
		if (!left.getPageSource().equals(right.getPageSource())) return false;
		if (!checkOnlyPublicAppearance && !__equalsComplexEL(pc, done, left.getComponentScope(), right.getComponentScope(), caseSensitive, checkOnlyPublicAppearance)) return false;
		if (!__equalsComplexEL(pc, done, (Collection) left, (Collection) right, caseSensitive, checkOnlyPublicAppearance)) return false;
		return true;
	}

	private static boolean __equalsComplexEL(PageContext pc, Set<Object> done, Collection left, Collection right, boolean caseSensitive, boolean checkOnlyPublicAppearance) {
		if (left.size() != right.size()) return false;
		Iterator<Key> it = left.keyIterator();
		Key k;
		Object l, r;
		while (it.hasNext()) {
			k = it.next();
			l = left.get(k, CollectionUtil.NULL);
			r = right.get(k, CollectionUtil.NULL);
			if (l == CollectionUtil.NULL || r == CollectionUtil.NULL) {
				if (l == r) continue;
				return false;
			}

			if (!_equalsComplexEL(pc, done, r, l, caseSensitive, checkOnlyPublicAppearance)) {
				return false;
			}
		}
		return true;
	}

	public static boolean equals(PageContext pc, Object left, Object right, boolean caseSensitive, boolean allowComplexValues) throws PageException {
		if (!allowComplexValues || (Decision.isSimpleValue(left) && Decision.isSimpleValue(right))) return equals(pc, left, right, caseSensitive);
		return equalsComplex(pc, left, right, caseSensitive, false);
	}

	public static boolean equalsComplex(PageContext pc, Object left, Object right, boolean caseSensitive, boolean checkOnlyPublicAppearance) throws PageException {
		if (checkOnlyPublicAppearance) throw new IllegalArgumentException("checkOnlyPublicAppearance cannot be true");// MUST implement
		return _equalsComplex(pc, null, left, right, caseSensitive);
	}

	public static boolean _equalsComplex(PageContext pc, Set<Object> done, Object left, Object right, boolean caseSensitive) throws PageException {
		if (Decision.isSimpleValue(left) && Decision.isSimpleValue(right)) {
			return equals(pc, left, right, caseSensitive);
		}
		if (left == null) return right == null;
		if (done == null) done = new HashSet<Object>();
		else if (done.contains(left) && done.contains(right)) return true;
		done.add(left);
		done.add(right);

		if (left instanceof Collection && right instanceof Collection) return __equalsComplex(pc, done, (Collection) left, (Collection) right, caseSensitive);

		if (left instanceof List && right instanceof List) return __equalsComplex(pc, done, ListAsArray.toArray((List) left), ListAsArray.toArray((List) right), caseSensitive);

		if (left instanceof Map && right instanceof Map)
			return __equalsComplex(pc, done, MapAsStruct.toStruct((Map) left, true), MapAsStruct.toStruct((Map) right, true), caseSensitive);

		return left.equals(right);
	}

	private static boolean __equalsComplex(PageContext pc, Set<Object> done, Collection left, Collection right, boolean caseSensitive) throws PageException {
		if (left.size() != right.size()) return false;
		Iterator<Key> it = left.keyIterator();
		Key k;
		Object l, r;
		while (it.hasNext()) {
			k = it.next();
			r = right.get(k, CollectionUtil.NULL);
			if (r == CollectionUtil.NULL) return false;
			l = left.get(k, CollectionUtil.NULL);
			if (!_equalsComplex(pc, done, r, l, caseSensitive)) return false;
		}
		return true;
	}

	/**
	 * check if left is inside right (String-> ignore case)
	 * 
	 * @param left string to check
	 * @param right substring to find in string
	 * @return return if substring has been found
	 * @throws PageException
	 */
	public static boolean ct(PageContext pc, Object left, Object right) throws PageException {
		return Caster.toString(left).toLowerCase().indexOf(Caster.toString(right).toLowerCase()) != -1;
	}

	/**
	 * Equivalence: Return True if both operands are True or both are False. The EQV operator is the
	 * opposite of the XOR operator. For example, True EQV True is True, but True EQV False is False.
	 * 
	 * @param left value to check
	 * @param right value to check
	 * @return result of operation
	 * @throws PageException
	 */
	public static boolean eqv(PageContext pc, Object left, Object right) throws PageException {
		return eqv(pc, Caster.toBooleanValue(left), Caster.toBooleanValue(right));
	}

	/**
	 * Equivalence: Return True if both operands are True or both are False. The EQV operator is the
	 * opposite of the XOR operator. For example, True EQV True is True, but True EQV False is False.
	 * 
	 * @param left value to check
	 * @param right value to check
	 * @return result of operation
	 */
	public static boolean eqv(PageContext pc, Boolean left, Boolean right) {
		return (left == true && right == true) || (left == false && right == false);
	}

	/**
	 * Implication: The statement A IMP B is the equivalent of the logical statement "If A Then B." A
	 * IMP B is False only if A is True and B is False. It is True in all other cases.
	 * 
	 * @param left value to check
	 * @param right value to check
	 * @return result
	 * @throws PageException
	 */
	public static boolean imp(PageContext pc, Object left, Object right) throws PageException {
		return imp(pc, Caster.toBoolean(left), Caster.toBoolean(right));
	}

	/**
	 * Implication: The statement A IMP B is the equivalent of the logical statement "If A Then B." A
	 * IMP B is False only if A is True and B is False. It is True in all other cases.
	 * 
	 * @param left value to check
	 * @param right value to check
	 * @return result
	 */
	public static boolean imp(PageContext pc, Boolean left, Boolean right) {
		return !(left == true && right == false);
	}

	/**
	 * check if left is not inside right (String-> ignore case)
	 * 
	 * @param left string to check
	 * @param right substring to find in string
	 * @return return if substring NOT has been found
	 * @throws PageException
	 */
	public static boolean nct(PageContext pc, Object left, Object right) throws PageException {
		return !ct(pc, left, right);
	}

	/**
	 * simple reference compersion
	 * 
	 * @param left
	 * @param right
	 * @return
	 * @throws PageException
	 */
	public static boolean eeq(PageContext pc, Object left, Object right) throws PageException {
		return left == right;
	}

	/**
	 * simple reference compersion
	 * 
	 * @param left
	 * @param right
	 * @return
	 * @throws PageException
	 */
	public static boolean neeq(PageContext pc, Object left, Object right) throws PageException {
		return left != right;
	}

	/**
	 * calculate the exponent of the left value
	 * 
	 * @param left value to get exponent from
	 * @param right exponent count
	 * @return return expoinended value
	 * @throws PageException
	 */
	public static Number exponent(PageContext pc, Object left, Object right) throws PageException {
		if (AppListenerUtil.getPreciseMath(pc, null)) {
			return Caster.toBigDecimal(left).pow(Caster.toIntValue(right));
		}
		return Double.valueOf(StrictMath.pow(Caster.toDoubleValue(left), Caster.toDoubleValue(right)));
	}

	public static Number exponent(PageContext pc, Number left, Number right) throws PageException {
		if (AppListenerUtil.getPreciseMath(pc, null)) {
			return Caster.toBigDecimal(left).pow(right.intValue());
		}
		return Double.valueOf(StrictMath.pow(left.doubleValue(), right.doubleValue()));
	}

	public static Number intdiv(PageContext pc, Number left, Number right) {
		return Double.valueOf(left.intValue() / right.intValue());
	}

	public static Number div(PageContext pc, Number left, Number right) {
		if (AppListenerUtil.getPreciseMath(pc, null)) {
			BigDecimal r = Caster.toBigDecimal(right);
			if (r.equals(BigDecimal.ZERO)) throw new ArithmeticException("Division by zero is not possible");
			return Caster.toBigDecimal(left).divide(r);
		}

		double r = right.doubleValue();
		if (r == 0d) throw new ArithmeticException("Division by zero is not possible");
		return Double.valueOf(left.doubleValue() / r);
	}

	public static float exponent(PageContext pc, float left, float right) {
		return (float) StrictMath.pow(left, right);
	}

	/**
	 * concat 2 CharSequences
	 * 
	 * @param left
	 * @param right
	 * @return concated String
	 */
	public static CharSequence concat(PageContext pc, CharSequence left, CharSequence right) {
		if (left instanceof Appendable) {
			try {
				((Appendable) left).append(right);
				return left;
			}
			catch (IOException e) {
			}
		}
		return new StringBuilder(left).append(right);
	}

	/**
	 * plus operation
	 * 
	 * @param left
	 * @param right
	 * @return result of the opertions
	 */
	public final static Number plus(PageContext pc, Number left, Number right) {
		if (AppListenerUtil.getPreciseMath(pc, null)) {
			return Caster.toBigDecimal(left).add(Caster.toBigDecimal(right));
		}
		return Double.valueOf(left.doubleValue() + right.doubleValue());
	}

	/**
	 * minus operation
	 * 
	 * @param left
	 * @param right
	 * @return result of the opertions
	 */
	public static Number minus(PageContext pc, Number left, Number right) {
		if (AppListenerUtil.getPreciseMath(pc, null)) {
			return Caster.toBigDecimal(left).subtract(Caster.toBigDecimal(right));
		}
		return Double.valueOf(left.doubleValue() - right.doubleValue());
	}

	/**
	 * modulus operation
	 * 
	 * @param left
	 * @param right
	 * @return result of the opertions
	 */
	public static Number modulus(PageContext pc, Number left, Number right) {
		if (right.doubleValue() == 0d) throw new ArithmeticException("Division by zero is not possible");
		return Double.valueOf(left.doubleValue() % right.doubleValue());
	}

	/**
	 * divide operation
	 * 
	 * @param left
	 * @param right
	 * @return result of the opertions
	 */
	public static Number divide(PageContext pc, Number left, Number right) {
		if (AppListenerUtil.getPreciseMath(pc, null)) {
			return Caster.toBigDecimal(left).divide(Caster.toBigDecimal(right));
		}
		return Double.valueOf(left.doubleValue() / right.doubleValue());
	}

	/**
	 * multiply operation
	 * 
	 * @param left
	 * @param right
	 * @return result of the opertions
	 */
	public static Number multiply(PageContext pc, Number left, Number right) {
		if (AppListenerUtil.getPreciseMath(pc, null)) {
			return Caster.toBigDecimal(left).multiply(Caster.toBigDecimal(right));
		}
		return Double.valueOf(left.doubleValue() * right.doubleValue());
	}

	/**
	 * bitand operation
	 * 
	 * @param left
	 * @param right
	 * @return result of the opertions
	 */
	public static Number bitand(PageContext pc, Number left, Number right) {
		return left.intValue() & right.intValue();
	}

	/**
	 * bitand operation
	 * 
	 * @param left
	 * @param right
	 * @return result of the opertions
	 */
	public static Number bitor(PageContext pc, Number left, Number right) {
		return left.intValue() | right.intValue();
	}

	public static Number divRef(PageContext pc, Object left, Object right) throws PageException {
		if (AppListenerUtil.getPreciseMath(pc, null)) {
			BigDecimal bd = Caster.toBigDecimal(right);
			if (bd.equals(BigDecimal.ZERO)) throw new ArithmeticException("Division by zero is not possible");
			return Caster.toBigDecimal(left).divide(bd);
		}

		double r = Caster.toDoubleValue(right);
		if (r == 0d) throw new ArithmeticException("Division by zero is not possible");
		return Caster.toDouble(Caster.toDoubleValue(left) / r);
	}

	public static Number intdivRef(PageContext pc, Object left, Object right) throws PageException {
		return Caster.toDouble(Caster.toIntValue(left) / Caster.toIntValue(right));
	}

	public static Number exponentRef(PageContext pc, Object left, Object right) throws PageException {
		if (AppListenerUtil.getPreciseMath(pc, null)) {// TODOX add PC
			return Caster.toBigDecimal(left).pow(Caster.toIntValue(right));
		}
		return Caster.toDouble(StrictMath.pow(Caster.toDoubleValue(left), Caster.toDoubleValue(right)));
	}

	public static Number plusRef(PageContext pc, Object left, Object right) throws PageException {
		if (AppListenerUtil.getPreciseMath(pc, null)) { // TODOX add PC
			return Caster.toBigDecimal(left).add(Caster.toBigDecimal(right));
		}
		return Caster.toDouble(Caster.toDoubleValue(left) + Caster.toDoubleValue(right));
	}

	public static Number minusRef(PageContext pc, Object left, Object right) throws PageException {
		if (AppListenerUtil.getPreciseMath(pc, null)) { // TODOX add pc
			return Caster.toBigDecimal(left).subtract(Caster.toBigDecimal(right));
		}
		return Caster.toDouble(Caster.toDoubleValue(left) - Caster.toDoubleValue(right));
	}

	public static Number modulusRef(PageContext pc, Object left, Object right) throws PageException {
		double rightAsDouble = Caster.toDoubleValue(right);
		if (rightAsDouble == 0d) throw new ArithmeticException("Division by zero is not possible");
		return Caster.toDouble(Caster.toDoubleValue(left) % rightAsDouble);
	}

	public static Number multiplyRef(PageContext pc, Object left, Object right) throws PageException {
		if (AppListenerUtil.getPreciseMath(pc, null)) {
			return Caster.toBigDecimal(left).multiply(Caster.toBigDecimal(right));
		}
		return Caster.toDouble(Caster.toDoubleValue(left) * Caster.toDoubleValue(right));
	}

	// post plus
	public static Number unaryPostPlus(PageContext pc, Collection.Key[] keys, Number value) throws PageException {
		VariableReference ref = VariableInterpreter.getVariableReference(pc, keys, true);
		Number rtn = Caster.toNumber(pc, ref.get(pc));
		ref.set(plus(pc, rtn, value));
		return rtn;
	}

	public static Number unaryPostPlus(PageContext pc, Collection coll, Collection.Key key, Number value) throws PageException {
		Number rtn = Caster.toNumber(pc, coll.get(key));
		coll.set(key, plus(pc, rtn, value));
		return rtn;
	}

	public static Number unaryPoPl(PageContext pc, Collection.Key[] keys, Number value) throws PageException {
		VariableReference ref = VariableInterpreter.getVariableReference(pc, keys, true);
		Number rtn = Caster.toNumber(pc, ref.get(pc));
		ref.set(plus(pc, rtn, value));
		return rtn;
	}

	public static Number unaryPoPl(PageContext pc, Collection.Key key, Number value) throws PageException {
		return unaryPoPl(pc, pc.undefinedScope(), key, value);
	}

	public static Number unaryPoPl(PageContext pc, Collection coll, Collection.Key key, Number value) throws PageException {
		Number rtn = Caster.toNumber(pc, coll.get(key));
		coll.set(key, plus(pc, rtn, value));
		return rtn;
	}

	// post minus
	public static Number unaryPostMinus(PageContext pc, Collection.Key[] keys, Number value) throws PageException {
		VariableReference ref = VariableInterpreter.getVariableReference(pc, keys, true);
		Number rtn = Caster.toNumber(pc, ref.get(pc));
		ref.set(minus(pc, rtn, value));
		return rtn;
	}

	public static Number unaryPostMinus(PageContext pc, Collection coll, Collection.Key key, Number value) throws PageException {
		Number rtn = Caster.toNumber(pc, coll.get(key));
		coll.set(key, minus(pc, rtn, value));
		return rtn;
	}

	public static Number unaryPoMi(PageContext pc, Collection.Key[] keys, Number value) throws PageException {
		VariableReference ref = VariableInterpreter.getVariableReference(pc, keys, true);
		Number rtn = Caster.toNumber(pc, ref.get(pc));
		ref.set(minus(pc, rtn, value));
		return rtn;
	}

	public static Number unaryPoMi(PageContext pc, Collection.Key key, Number value) throws PageException {
		return unaryPoMi(pc, pc.undefinedScope(), key, value);
	}

	public static Number unaryPoMi(PageContext pc, Collection coll, Collection.Key key, Number value) throws PageException {
		Number rtn = Caster.toNumber(pc, coll.get(key));
		coll.set(key, minus(pc, rtn, value));
		return rtn;
	}

	// pre plus
	public static Number unaryPrePlus(PageContext pc, Collection.Key[] keys, Number value) throws PageException {
		VariableReference ref = VariableInterpreter.getVariableReference(pc, keys, true);
		Number rtn = plus(pc, Caster.toNumber(pc, ref.get(pc)), value);
		ref.set(rtn);
		return rtn;
	}

	public static Number unaryPrePlus(PageContext pc, Collection coll, Collection.Key key, Number value) throws PageException {
		Number rtn = plus(pc, Caster.toNumber(pc, coll.get(key)), value);
		coll.set(key, rtn);
		return rtn;
	}

	public static Number unaryPrPl(PageContext pc, Collection.Key[] keys, Number value) throws PageException {
		VariableReference ref = VariableInterpreter.getVariableReference(pc, keys, true);
		Number rtn = plus(pc, Caster.toNumber(pc, ref.get(pc)), value);
		ref.set(rtn);
		return rtn;
	}

	public static Number unaryPrPl(PageContext pc, Collection.Key key, Number value) throws PageException {
		return unaryPrPl(pc, pc.undefinedScope(), key, value);
	}

	public static Number unaryPrPl(PageContext pc, Collection coll, Collection.Key key, Number value) throws PageException {
		Number rtn = plus(pc, Caster.toNumber(pc, coll.get(key)), value);
		coll.set(key, rtn);
		return rtn;
	}

	// pre minus
	public static Number unaryPreMinus(PageContext pc, Collection.Key[] keys, Number value) throws PageException {
		VariableReference ref = VariableInterpreter.getVariableReference(pc, keys, true);
		Number rtn = minus(pc, Caster.toNumber(pc, ref.get(pc)), value);
		ref.set(rtn);
		return rtn;
	}

	public static Number unaryPreMinus(PageContext pc, Collection coll, Collection.Key key, Number value) throws PageException {
		Number rtn = minus(pc, Caster.toNumber(pc, coll.get(key)), value);
		coll.set(key, rtn);
		return rtn;
	}

	public static Number unaryPrMi(PageContext pc, Collection.Key[] keys, Number value) throws PageException {
		VariableReference ref = VariableInterpreter.getVariableReference(pc, keys, true);
		Number rtn = minus(pc, Caster.toNumber(pc, ref.get(pc)), value);
		ref.set(rtn);
		return rtn;
	}

	public static Number unaryPrMi(PageContext pc, Collection.Key key, Number value) throws PageException {
		return unaryPrMi(pc, pc.undefinedScope(), key, value);
	}

	public static Number unaryPrMi(PageContext pc, Collection coll, Collection.Key key, Number value) throws PageException {
		Number rtn = minus(pc, Caster.toNumber(pc, coll.get(key)), value);
		coll.set(key, rtn);
		return rtn;
	}

	// pre multiply
	public static Number unaryPreMultiply(PageContext pc, Collection.Key[] keys, Number value) throws PageException {
		VariableReference ref = VariableInterpreter.getVariableReference(pc, keys, true);
		Number rtn = multiply(pc, Caster.toNumber(pc, ref.get(pc)), value);
		ref.set(rtn);
		return rtn;
	}

	public static Number unaryPreMultiply(PageContext pc, Collection coll, Collection.Key key, Number value) throws PageException {
		Number rtn = multiply(pc, Caster.toNumber(pc, coll.get(key)), value);
		coll.set(key, rtn);
		return rtn;
	}

	public static Number unaryPrMu(PageContext pc, Collection.Key[] keys, Number value) throws PageException {
		VariableReference ref = VariableInterpreter.getVariableReference(pc, keys, true);
		Number rtn = multiply(pc, Caster.toNumber(pc, ref.get(pc)), value);
		ref.set(rtn);
		return rtn;
	}

	public static Number unaryPrMu(PageContext pc, Collection.Key key, Number value) throws PageException {
		return unaryPrMu(pc, pc.undefinedScope(), key, value);
	}

	public static Number unaryPrMu(PageContext pc, Collection coll, Collection.Key key, Number value) throws PageException {
		Number rtn = multiply(pc, Caster.toNumber(pc, coll.get(key)), value);
		coll.set(key, rtn);
		return rtn;
	}

	// pre divide
	public static Number unaryPreDivide(PageContext pc, Collection.Key[] keys, Number value) throws PageException {
		VariableReference ref = VariableInterpreter.getVariableReference(pc, keys, true);
		Number rtn = divide(pc, Caster.toNumber(pc, ref.get(pc)), value);
		ref.set(rtn);
		return rtn;
	}

	public static Number unaryPreDivide(PageContext pc, Collection coll, Collection.Key key, Number value) throws PageException {
		Number rtn = divide(pc, Caster.toNumber(pc, coll.get(key)), value);
		coll.set(key, rtn);
		return rtn;
	}

	public static Number unaryPrDi(PageContext pc, Collection.Key[] keys, Number value) throws PageException {
		VariableReference ref = VariableInterpreter.getVariableReference(pc, keys, true);
		Number rtn = divide(pc, Caster.toNumber(pc, ref.get(pc)), value);
		ref.set(rtn);
		return rtn;
	}

	public static Number unaryPrDi(PageContext pc, Collection.Key key, Number value) throws PageException {
		return unaryPrDi(pc, pc.undefinedScope(), key, value);
	}

	public static Number unaryPrDi(PageContext pc, Collection coll, Collection.Key key, Number value) throws PageException {
		Number rtn = divide(pc, Caster.toNumber(pc, coll.get(key)), value);
		coll.set(key, rtn);
		return rtn;
	}

	// Concat
	public static String unaryPreConcat(PageContext pc, Collection.Key[] keys, String value) throws PageException {
		VariableReference ref = VariableInterpreter.getVariableReference(pc, keys, true);
		String rtn = Caster.toString(ref.get(pc)).concat(value);
		ref.set(pc, rtn);
		return rtn;
	}

	public static String unaryPreConcat(PageContext pc, Collection coll, Collection.Key key, String value) throws PageException {
		String rtn = Caster.toString(coll.get(key)).concat(value);
		coll.set(key, rtn);
		return rtn;
	}

	public static String unaryPreConcat(PageContext pc, Collection.Key key, String value) throws PageException {
		VariableReference ref = VariableInterpreter.getVariableReference(pc, key, true);
		String rtn = Caster.toString(ref.get(pc)).concat(value);
		ref.set(pc, rtn);
		return rtn;
	}
}