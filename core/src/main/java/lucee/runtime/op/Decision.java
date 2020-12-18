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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.sql.Blob;
import java.sql.Clob;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lucee.commons.date.DateTimeUtil;
import lucee.commons.i18n.FormatUtil;
import lucee.commons.lang.CFTypes;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.coder.Base64Util;
import lucee.runtime.converter.WDDXConverter;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.image.ImageUtil;
import lucee.runtime.java.JavaObject;
import lucee.runtime.net.mail.MailUtil;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.op.validators.ValidateCreditCard;
import lucee.runtime.text.xml.XMLCaster;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.text.xml.struct.XMLStruct;
import lucee.runtime.type.Array;
import lucee.runtime.type.Closure;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Lambda;
import lucee.runtime.type.ObjectWrap;
import lucee.runtime.type.Objects;
import lucee.runtime.type.Pojo;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryColumn;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.DateTime;

/**
 * Object to test if an Object is a specific type
 */
public final class Decision {

	private static final String STRING_DEFAULT_VALUE = "this is a unique string";

	private static Pattern ssnPattern;
	private static Pattern phonePattern;
	private static Pattern zipPattern;

	/**
	 * tests if value is a simple value (Number,String,Boolean,Date,Printable)
	 * 
	 * @param value value to test
	 * @return is value a simple value
	 */
	public static boolean isSimpleValue(Object value) {
		return (value instanceof Number) || (value instanceof Locale) || (value instanceof TimeZone) || (value instanceof String) || (value instanceof Boolean)
				|| (value instanceof Date) || ((value instanceof Castable) && !(value instanceof Objects) && !(value instanceof Collection));
	}

	public static boolean isSimpleValueLimited(Object value) {
		return (value instanceof Number) || (value instanceof Locale) || (value instanceof TimeZone) || (value instanceof String) || (value instanceof Boolean)
				|| (value instanceof Date);
	}

	/**
	 * tests if value is Numeric
	 * 
	 * @param value value to test
	 * @return is value numeric
	 */
	public static boolean isNumber(Object value) {
		if (value instanceof Number) return true;
		else if (value instanceof CharSequence || value instanceof Character) {
			boolean numeric = true;
			try {
				Double num = Double.parseDouble(value.toString());
			}
			catch (NumberFormatException e) {
				numeric = false;
			}
			if (numeric) return true;
			return isNumber(value.toString());
		}

		else return false;
	}

	public static boolean isCastableToNumeric(Object o) {

		if (isNumber(o)) return true;
		else if (isBoolean(o)) return true;
		else if (isDateSimple(o, false)) return true;
		else if (o == null) return true;
		else if (o instanceof ObjectWrap) return isCastableToNumeric(((ObjectWrap) o).getEmbededObject("notanumber"));

		else if (o instanceof Castable) {
			return Decision.isValid(((Castable) o).castToDoubleValue(Double.NaN));

		}
		return false;
	}

	public static boolean isCastableToDate(Object o) {
		if (isDateAdvanced(o, true)) return true;
		else if (isBoolean(o)) return true;

		else if (o instanceof ObjectWrap) return isCastableToDate(((ObjectWrap) o).getEmbededObject("notadate"));

		else if (o instanceof Castable) {
			return ((Castable) o).castToDateTime(null) != null;

		}
		return false;
	}

	/**
	 * tests if value is Numeric
	 * 
	 * @param value value to test
	 * @return is value numeric
	 */
	public static boolean isNumber(Object value, boolean alsoBooleans) {
		if (alsoBooleans && isBoolean(value)) return true;
		return isNumber(value);
	}

	/**
	 * tests if String value is Numeric
	 * 
	 * @param str value to test
	 * @return is value numeric
	 */
	public static boolean isNumber(String str) {
		if (str == null) return false;
		str = str.trim();

		int pos = 0;
		int len = str.length();
		if (len == 0) return false;
		char curr = str.charAt(pos);

		if (curr == '+' || curr == '-') {
			if (len == ++pos) return false;
			curr = str.charAt(pos);
		}

		boolean hasDot = false;
		boolean hasExp = false;
		for (; pos < len; pos++) {
			curr = str.charAt(pos);
			if (curr < '0') {
				if (curr == '.') {
					if (pos + 1 >= len || hasDot) return false;
					hasDot = true;
				}
				else return false;
			}
			else if (curr > '9') {
				if (curr == 'e' || curr == 'E') {
					if (pos + 1 >= len || hasExp) return false;
					hasExp = true;
					hasDot = true;
				}
				else return false;
			}
		}
		if (hasExp) {
			try {
				if (Double.isInfinite(Double.parseDouble(str))) return false;

				return true;
			}
			catch (NumberFormatException e) {
				return false;
			}
		}
		return true;
	}

	public static boolean isInteger(Object value) {
		return isInteger(value, false);
	}

	public static boolean isInteger(Object value, boolean alsoBooleans) {
		if (!alsoBooleans && value instanceof Boolean) return false;
		double dbl = Caster.toDoubleValue(value, false, Double.NaN);
		if (!Decision.isValid(dbl)) return false;
		int i = (int) dbl;
		return i == dbl;
	}

	/**
	 * tests if String value is Hex Value
	 * 
	 * @param str value to test
	 * @return is value numeric
	 */
	public static boolean isHex(String str) {
		if (str == null || str.length() == 0) return false;

		for (int i = str.length() - 1; i >= 0; i--) {
			char c = str.charAt(i);
			if (!(c >= '0' && c <= '9')) {
				c = Character.toLowerCase(c);
				if (!(c == 'a' || c == 'b' || c == 'c' || c == 'd' || c == 'e' || c == 'f')) return false;
			}
		}
		return true;
	}

	/**
	 * tests if String value is UUID Value
	 * 
	 * @param obj value to test
	 * @return is value numeric
	 */
	public static boolean isUUId(Object obj) {
		String str = Caster.toString(obj, null);
		if (str == null) return false;

		if (str.length() == 35) {
			return Decision.isHex(str.substring(0, 8)) && str.charAt(8) == '-' && Decision.isHex(str.substring(9, 13)) && str.charAt(13) == '-'
					&& Decision.isHex(str.substring(14, 18)) && str.charAt(18) == '-' && Decision.isHex(str.substring(19));
		}
		else if (str.length() == 32) return Decision.isHex(str);
		return false;
	}

	public static boolean isGUId(Object obj) {
		String str = Caster.toString(obj, null);
		if (str == null) return false;

		// GUID
		if (str.length() == 36) {
			return Decision.isHex(str.substring(0, 8)) && str.charAt(8) == '-' && Decision.isHex(str.substring(9, 13)) && str.charAt(13) == '-'
					&& Decision.isHex(str.substring(14, 18)) && str.charAt(18) == '-' && Decision.isHex(str.substring(19, 23)) && str.charAt(23) == '-'
					&& Decision.isHex(str.substring(24));
		}
		return false;
	}

	public static boolean isGUIdSimple(Object obj) {
		String str = Caster.toString(obj, null);
		if (str == null) return false;

		// GUID
		if (str.length() == 36) {
			return str.charAt(8) == '-' && str.charAt(13) == '-' && str.charAt(18) == '-' && str.charAt(23) == '-';
		}
		return false;
	}

	/**
	 * tests if value is a Boolean (Numbers are not acctepeted)
	 * 
	 * @param value value to test
	 * @return is value boolean
	 */
	public static boolean isBoolean(Object value) {
		if (value instanceof Boolean) return true;
		else if (value instanceof String) {
			return isBoolean(value.toString());
		}
		else if (value instanceof ObjectWrap) return isBoolean(((ObjectWrap) value).getEmbededObject(null));
		else return false;
	}

	public static boolean isCastableToBoolean(Object value) {
		if (value instanceof Boolean) return true;
		if (value instanceof Number) return true;
		else if (value instanceof String) {
			String str = (String) value;
			return isBoolean(str) || isNumber(str);
		}
		else if (value instanceof Castable) {
			return ((Castable) value).castToBoolean(null) != null;

		}
		else if (value instanceof ObjectWrap) return isCastableToBoolean(((ObjectWrap) value).getEmbededObject(null));
		else return false;
	}

	public static boolean isBoolean(Object value, boolean alsoNumbers) {
		if (isBoolean(value)) return true;
		else if (alsoNumbers) return isNumber(value);
		else return false;
	}

	/**
	 * tests if value is a Boolean
	 * 
	 * @param str value to test
	 * @return is value boolean
	 */
	public static boolean isBoolean(String str) {
		// str=str.trim();
		if (str.length() < 2) return false;

		switch (str.charAt(0)) {
		case 't':
		case 'T':
			return str.equalsIgnoreCase("true");
		case 'f':
		case 'F':
			return str.equalsIgnoreCase("false");
		case 'y':
		case 'Y':
			return str.equalsIgnoreCase("yes");
		case 'n':
		case 'N':
			return str.equalsIgnoreCase("no");
		}
		return false;
	}

	/**
	 * tests if value is DateTime Object
	 * 
	 * @param value value to test
	 * @param alsoNumbers interpret also a number as date
	 * @return is value a DateTime Object
	 */
	public static boolean isDate(Object value, boolean alsoNumbers) {
		return isDateSimple(value, alsoNumbers);
	}

	public static boolean isDateSimple(Object value, boolean alsoNumbers) {
		return isDateSimple(value, alsoNumbers, false);
	}

	public static boolean isDateSimple(Object value, boolean alsoNumbers, boolean alsoMonthString) {
		// return DateCaster.toDateEL(value)!=null;
		if (value instanceof DateTime) return true;
		else if (value instanceof Date) return true;
		// wrong timezone but this isent importend because date will not be importend
		else if (value instanceof String) return DateCaster.toDateSimple(value.toString(), alsoNumbers ? DateCaster.CONVERTING_TYPE_OFFSET : DateCaster.CONVERTING_TYPE_NONE,
				alsoMonthString, TimeZone.getDefault(), null) != null;
		else if (value instanceof ObjectWrap) {
			return isDateSimple(((ObjectWrap) value).getEmbededObject(null), alsoNumbers);
		}
		else if (value instanceof Castable) {
			return ((Castable) value).castToDateTime(null) != null;

		}
		else if (alsoNumbers && value instanceof Number) return true;
		else if (value instanceof Calendar) return true;
		return false;
	}

	public static boolean isDateAdvanced(Object value, boolean alsoNumbers) {
		// return DateCaster.toDateEL(value)!=null;
		if (value instanceof Date) return true;
		// wrong timezone but this isent importend because date will not be importend
		else if (value instanceof String) return DateCaster.toDateAdvanced(value.toString(), alsoNumbers ? DateCaster.CONVERTING_TYPE_OFFSET : DateCaster.CONVERTING_TYPE_NONE,
				TimeZone.getDefault(), null) != null;
		else if (value instanceof Castable) {
			return ((Castable) value).castToDateTime(null) != null;

		}
		else if (alsoNumbers && value instanceof Number) return true;
		else if (value instanceof ObjectWrap) {
			return isDateAdvanced(((ObjectWrap) value).getEmbededObject(null), alsoNumbers);
		}
		else if (value instanceof Calendar) return true;
		return false;
	}

	private static char[] DATE_DEL = new char[] { '.', '/', '-' };

	public static boolean isUSDate(Object value) {
		String str = Caster.toString(value, "");
		return isUSorEuroDateEuro(str, false);
	}

	public static boolean isUSDate(String str) {
		return isUSorEuroDateEuro(str, false);
	}

	public static boolean isEuroDate(Object value) {
		String str = Caster.toString(value, "");
		return isUSorEuroDateEuro(str, true);
	}

	public static boolean isEuroDate(String str) {
		return isUSorEuroDateEuro(str, true);
	}

	private static boolean isUSorEuroDateEuro(String str, boolean isEuro) {
		if (StringUtil.isEmpty(str)) return false;

		for (int i = 0; i < DATE_DEL.length; i++) {
			Array arr = lucee.runtime.type.util.ListUtil.listToArrayRemoveEmpty(str, DATE_DEL[i]);
			if (arr.size() != 3) continue;

			int month = Caster.toIntValue(arr.get(isEuro ? 2 : 1, Constants.INTEGER_0), Integer.MIN_VALUE);
			int day = Caster.toIntValue(arr.get(isEuro ? 1 : 2, Constants.INTEGER_0), Integer.MIN_VALUE);
			int year = Caster.toIntValue(arr.get(3, Constants.INTEGER_0), Integer.MIN_VALUE);

			if (month == Integer.MIN_VALUE) continue;
			if (month > 12) continue;
			if (day == Integer.MIN_VALUE) continue;
			if (day > 31) continue;
			if (year == Integer.MIN_VALUE) continue;
			if (DateTimeUtil.getInstance().toTime(null, year, month, day, 0, 0, 0, 0, Long.MIN_VALUE) == Long.MIN_VALUE) continue;
			return true;
		}
		return false;
	}

	public static boolean isCastableToStruct(Object o) {
		if (isStruct(o)) return true;
		if (o == null) return false;
		else if (o instanceof ObjectWrap) {
			if (o instanceof JavaObject) return true;
			return isCastableToStruct(((ObjectWrap) o).getEmbededObject(null));
		}
		if (Decision.isSimpleValue(o)) {
			return false;
		}
		// if(isArray(o) || isQuery(o)) return false;
		return false;
	}

	/**
	 * tests if object is a struct
	 * 
	 * @param o
	 * @return is struct or not
	 */
	public static boolean isStruct(Object o) {
		if (o instanceof Struct) return true;
		else if (o instanceof Map) return true;
		else if (o instanceof Node) return true;
		return false;
	}

	/**
	 * can this type be casted to an array
	 * 
	 * @param o
	 * @return
	 * @throws PageException
	 */
	public static boolean isCastableToArray(Object o) {
		if (isArray(o)) return true;
		else if (o instanceof Set) return true;
		else if (o instanceof Struct) {
			Struct sct = (Struct) o;
			Iterator<Key> it = sct.keyIterator();

			while (it.hasNext()) {
				if (!isInteger(it.next(), false)) return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * tests if object is an array
	 * 
	 * @param o
	 * @return is array or not
	 */
	public static boolean isArray(Object o) {
		if (o instanceof Array) return true;
		if (o instanceof List) return true;
		if (isNativeArray(o)) return true;
		if (o instanceof ObjectWrap) {
			return isArray(((ObjectWrap) o).getEmbededObject(null));
		}
		return false;
	}

	/**
	 * tests if object is a native java array
	 * 
	 * @param o
	 * @return is a native (java) array
	 */
	public static boolean isNativeArray(Object o) {
		// return o.getClass().isArray();
		if (o instanceof Object[]) return true;
		else if (o instanceof boolean[]) return true;
		else if (o instanceof byte[]) return true;
		else if (o instanceof char[]) return true;
		else if (o instanceof short[]) return true;
		else if (o instanceof int[]) return true;
		else if (o instanceof long[]) return true;
		else if (o instanceof float[]) return true;
		else if (o instanceof double[]) return true;
		return false;
	}

	/**
	 * tests if object is catable to a binary
	 * 
	 * @param object
	 * @return boolean
	 */
	public static boolean isCastableToBinary(Object object, boolean checkBase64String) {
		if (isBinary(object)) return true;
		if (object instanceof InputStream) return true;
		if (object instanceof ByteArrayOutputStream) return true;
		if (object instanceof Blob) return true;

		// Base64 String
		if (!checkBase64String) return false;
		String str = Caster.toString(object, null);
		if (str == null) return false;
		return Base64Util.isBase64(str);

	}

	/**
	 * tests if object is a binary
	 * 
	 * @param object
	 * @return boolean
	 */
	public static boolean isBinary(Object object) {
		if (object instanceof byte[]) return true;
		if (object instanceof ObjectWrap) return isBinary(((ObjectWrap) object).getEmbededObject(""));
		return false;
	}

	/**
	 * tests if object is a Component
	 * 
	 * @param object
	 * @return boolean
	 */
	public static boolean isComponent(Object object) {
		return object instanceof Component;
	}

	/**
	 * tests if object is a Query
	 * 
	 * @param object
	 * @return boolean
	 */
	public static boolean isQuery(Object object) {
		if (object instanceof Query) return true;
		else if (object instanceof ObjectWrap) {
			return isQuery(((ObjectWrap) object).getEmbededObject(null));
		}
		return false;
	}

	public static boolean isQueryColumn(Object object) {
		if (object instanceof QueryColumn) return true;
		else if (object instanceof ObjectWrap) {
			return isQueryColumn(((ObjectWrap) object).getEmbededObject(null));
		}
		return false;
	}

	/**
	 * tests if object is a binary
	 * 
	 * @param object
	 * @return boolean
	 */
	public static boolean isUserDefinedFunction(Object object) {
		return object instanceof UDF;
	}

	/**
	 * tests if year is a leap year
	 * 
	 * @param year year to check
	 * @return boolean
	 */
	public static final boolean isLeapYear(int year) {
		return DateTimeUtil.getInstance().isLeapYear(year);
		// return new GregorianCalendar().isLeapYear(year);
	}

	/**
	 * tests if object is a WDDX Object
	 * 
	 * @param o Object to check
	 * @return boolean
	 */
	public static boolean isWddx(Object o) {
		if (!(o instanceof String)) return false;
		String str = o.toString();
		if (!(str.indexOf("wddxPacket") > 0)) return false;

		// wrong timezone but this isent importend because date will not be used
		WDDXConverter converter = new WDDXConverter(TimeZone.getDefault(), false, true);
		try {
			converter.deserialize(Caster.toString(o), true);
		}
		catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * tests if object is a XML Object
	 * 
	 * @param o Object to check
	 * @return boolean
	 */
	public static boolean isXML(Object o) {
		if (o instanceof Node || o instanceof NodeList) return true;
		if (o instanceof ObjectWrap) {
			return isXML(((ObjectWrap) o).getEmbededObject(null));
		}
		try {
			XMLCaster.toXMLStruct(XMLUtil.parse(XMLUtil.toInputSource(null, o), null, false), false);
			return true;
		}
		catch (Exception outer) {
			return false;
		}

	}

	public static boolean isVoid(Object o) {
		if (o == null) return true;
		else if (o instanceof String) return o.toString().length() == 0;
		else if (o instanceof Number) return ((Number) o).intValue() == 0;
		else if (o instanceof Boolean) return ((Boolean) o).booleanValue() == false;
		else if (o instanceof ObjectWrap) return isVoid(((ObjectWrap) o).getEmbededObject(("isnotnull")));
		return false;
	}

	/**
	 * tests if object is a XML Element Object
	 * 
	 * @param o Object to check
	 * @return boolean
	 */
	public static boolean isXMLElement(Object o) {
		return o instanceof Element;
	}

	/**
	 * tests if object is a XML Document Object
	 * 
	 * @param o Object to check
	 * @return boolean
	 */
	public static boolean isXMLDocument(Object o) {
		return o instanceof Document;
	}

	/**
	 * tests if object is a XML Root Element Object
	 * 
	 * @param o Object to check
	 * @return boolean
	 */
	public static boolean isXMLRootElement(Object o) {
		if (o instanceof Node) {
			Node n = (Node) o;
			if (n instanceof XMLStruct) n = ((XMLStruct) n).toNode();
			return XMLUtil.getDocument(n) != null && XMLUtil.getDocument(n).getDocumentElement() == n;
		}
		return false;
	}

	/**
	 * @param obj
	 * @return returns if string represent a variable name
	 */
	public static boolean isVariableName(Object obj) {
		if (obj instanceof String) return isVariableName((String) obj);
		return false;
	}

	public static boolean isFunction(Object obj) {
		if (obj instanceof UDF) return true;
		else if (obj instanceof ObjectWrap) {
			return isFunction(((ObjectWrap) obj).getEmbededObject(null));
		}
		return false;
	}

	public static boolean isClosure(Object obj) {
		if (obj instanceof Closure) return true;
		else if (obj instanceof ObjectWrap) {
			return isClosure(((ObjectWrap) obj).getEmbededObject(null));
		}
		return false;
	}

	public static boolean isLambda(Object obj) {
		if (obj instanceof Lambda) return true;
		else if (obj instanceof ObjectWrap) {
			return isLambda(((ObjectWrap) obj).getEmbededObject(null));
		}
		return false;
	}

	/**
	 * @param string
	 * @return returns if string represent a variable name
	 */
	public static boolean isVariableName(String string) {
		if (string.length() == 0) return false;
		int len = string.length();
		int pos = 0;
		while (pos < len) {
			char first = string.charAt(pos);
			if (!((first >= 'a' && first <= 'z') || (first >= 'A' && first <= 'Z') || (first == '_'))) return false;
			pos++;
			for (; pos < len; pos++) {
				char c = string.charAt(pos);
				if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || (c == '_'))) break;
			}
			if (pos == len) return true;
			if (string.charAt(pos) == '.') pos++;
		}
		return false;
	}

	/**
	 * @param string
	 * @return returns if string represent a variable name
	 */
	public static boolean isSimpleVariableName(String string) {
		if (string.length() == 0) return false;

		char first = string.charAt(0);
		if (!((first >= 'a' && first <= 'z') || (first >= 'A' && first <= 'Z') || (first == '_'))) return false;
		for (int i = string.length() - 1; i > 0; i--) {
			char c = string.charAt(i);
			if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || (c == '_'))) return false;
		}
		return true;
	}

	/**
	 * @param key
	 * @return returns if string represent a variable name
	 */
	public static boolean isSimpleVariableName(Collection.Key key) {
		String strKey = key.getLowerString();
		if (strKey.length() == 0) return false;

		char first = strKey.charAt(0);
		if (!((first >= 'a' && first <= 'z') || (first == '_'))) return false;
		for (int i = strKey.length() - 1; i > 0; i--) {
			char c = strKey.charAt(i);
			if (!((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || (c == '_'))) return false;
		}
		return true;
	}

	/**
	 * returns if object is a CFML object
	 * 
	 * @param o Object to check
	 * @return is or not
	 */
	public static boolean isObject(Object o) {
		return isComponent(o)

				|| (!isArray(o) && !isQuery(o) && !isSimpleValue(o) && !isStruct(o) && !isUserDefinedFunction(o) && !isXML(o));
	}

	/**
	 * @param obj
	 * @return return if a String is "Empty", that means NULL or String with length 0 (whitespaces will
	 *         not counted)
	 */
	public static boolean isEmpty(Object obj) {
		if (obj instanceof String) return StringUtil.isEmpty((String) obj);
		return obj == null;
	}

	/**
	 * @deprecated use instead <code>StringUtil.isEmpty(String)</code>
	 * @param str
	 * @return return if a String is "Empty", that means NULL or String with length 0 (whitespaces will
	 *         not counted)
	 */
	@Deprecated
	public static boolean isEmpty(String str) {
		return StringUtil.isEmpty(str);
	}

	/**
	 * @deprecated use instead <code>StringUtil.isEmpty(String)</code>
	 * @param str
	 * @param trim
	 * @return return if a String is "Empty", that means NULL or String with length 0 (whitespaces will
	 *         not counted)
	 */
	@Deprecated
	public static boolean isEmpty(String str, boolean trim) {
		return StringUtil.isEmpty(str, trim);
	}

	/**
	 * returns if a value is a credit card
	 * 
	 * @param value
	 * @return is credit card
	 */
	public static boolean isCreditCard(Object value) {
		return ValidateCreditCard.isValid(Caster.toString(value, "0"));
	}

	/**
	 * returns if given object is an email
	 * 
	 * @param value
	 * @return
	 */
	public static boolean isEmail(Object value) {

		return MailUtil.isValidEmail(value);
	}

	/**
	 * returns if given object is a social security number (usa)
	 * 
	 * @param value
	 * @return
	 */
	public static boolean isSSN(Object value) {
		String str = Caster.toString(value, null);
		if (str == null) return false;

		if (ssnPattern == null) ssnPattern = Pattern.compile("^[0-9]{3}[-|]{1}[0-9]{2}[-|]{1}[0-9]{4}$");

		return ssnPattern.matcher(str.trim()).matches();

	}

	/**
	 * returns if given object is a phone
	 * 
	 * @param value
	 * @return
	 */
	public static boolean isPhone(Object value) {
		String str = Caster.toString(value, null);
		if (str == null) return false;

		if (phonePattern == null) phonePattern = Pattern
				.compile("^(\\+?1?[ \\-\\.]?([\\(]?([1-9][0-9]{2})[\\)]?))?[ ,\\-,\\.]?([^0-1]){1}([0-9]){2}[ ,\\-,\\.]?([0-9]){4}(( )((x){0,1}([0-9]){1,5}){0,1})?$");
		return phonePattern.matcher(str.trim()).matches();

	}

	/**
	 * returns true if the given object is a valid URL
	 * 
	 * @param value
	 * @return
	 */
	public static boolean isURL(Object value) {

		String str = Caster.toString(value, null);

		if (str == null) return false;
		if (str.indexOf(':') == -1) return false;

		str = str.toLowerCase().trim();

		if (!str.startsWith("http://") && !str.startsWith("https://") && !str.startsWith("file://") && !str.startsWith("ftp://") && !str.startsWith("mailto:")
				&& !str.startsWith("news:") && !str.startsWith("urn:"))
			return false;

		try {

			URI uri = new URI(str);
			String proto = uri.getScheme();

			if (proto == null) return false;

			if (proto.equals("http") || proto.equals("https") || proto.equals("file") || proto.equals("ftp")) {

				if (uri.getHost() == null) return false;

				String path = uri.getPath();
				if (path != null) {

					int len = path.length();
					for (int i = 0; i < len; i++) {

						if ("?<>*|\"".indexOf(path.charAt(i)) > -1) return false;
					}
				}
			}

			return true;
		}
		catch (Exception ex) {

			return false;
		}
	}

	/**
	 * returns if given object is a zip code
	 * 
	 * @param value
	 * @return
	 */
	public static boolean isZipCode(Object value) {
		String str = Caster.toString(value, null);
		if (str == null) return false;

		if (zipPattern == null) zipPattern = Pattern.compile("([0-9]{5,5})|([0-9]{5,5}[- ]{1}[0-9]{4,4})");
		return zipPattern.matcher(str.trim()).matches();
	}

	public static boolean isString(Object o) {
		if (o instanceof String) return true;
		else if (o instanceof Boolean) return true;
		else if (o instanceof Number) return true;
		else if (o instanceof Date) return true;
		else if (o instanceof Castable) {
			return ((Castable) o).castToString(STRING_DEFAULT_VALUE) != STRING_DEFAULT_VALUE;

		}
		else if (o instanceof Clob) return true;
		else if (o instanceof Node) return true;
		else if (o instanceof Map || o instanceof List || o instanceof Function) return false;
		else if (o == null) return true;
		else if (o instanceof ObjectWrap) return isString(((ObjectWrap) o).getEmbededObject(""));
		return true;
	}

	public static boolean isCastableToString(Object o) {
		return isString(o);
	}

	public static boolean isValid(String type, Object value) throws ExpressionException {
		type = StringUtil.toLowerCase(type.trim());
		char first = type.charAt(0);
		switch (first) {
		case 'a':
			if ("any".equals(type)) return true;// isSimpleValue(value);
			if ("array".equals(type)) return isArray(value);
			break;
		case 'b':
			if ("binary".equals(type)) return isBinary(value);
			if ("boolean".equals(type)) return isBoolean(value, true);
			break;
		case 'c':
			if ("creditcard".equals(type)) return isCreditCard(value);
			if ("component".equals(type)) return isComponent(value);
			if ("cfc".equals(type)) return isComponent(value);
			if ("class".equals(type)) return isComponent(value);
			if ("closure".equals(type)) return isClosure(value);
			break;
		case 'd':
			if ("date".equals(type)) return isDateAdvanced(value, true); // ist zwar nicht logisch aber ident. zu Neo
			if ("datetime".equals(type)) return isDateAdvanced(value, true); // ist zwar nicht logisch aber ident. zu Neo
			if ("double".equals(type)) return isCastableToNumeric(value);
			break;
		case 'e':
			if ("eurodate".equals(type)) return isEuroDate(value);
			if ("email".equals(type)) return isEmail(value);
			break;
		case 'f':
			if ("float".equals(type)) return isNumber(value, true);
			if ("function".equals(type)) return isFunction(value);
			break;
		case 'g':
			if ("guid".equals(type)) return isGUId(value);
			break;
		case 'i':
			if ("integer".equals(type)) return isInteger(value, false);
			if ("image".equals(type)) return ImageUtil.isImage(value);
			break;
		case 'l':
			if ("lambda".equals(type)) return isLambda(value);
			break;
		case 'n':
			if ("numeric".equals(type)) return isCastableToNumeric(value);
			if ("number".equals(type)) return isCastableToNumeric(value);
			if ("node".equals(type)) return isXML(value);
			break;

		case 'p':
			if ("phone".equals(type)) return isPhone(value);
			break;
		case 'q':
			if ("query".equals(type)) return isQuery(value);
			break;
		case 's':
			if ("simple".equals(type)) return isSimpleValue(value);
			if ("struct".equals(type)) return isStruct(value);
			if ("ssn".equals(type)) return isSSN(value);
			if ("social_security_number".equals(type)) return isSSN(value);
			if ("string".equals(type)) return isString(value);
			break;
		case 't':
			if ("telephone".equals(type)) return isPhone(value);
			if ("time".equals(type)) return isDateAdvanced(value, false);
			break;
		case 'u':
			if ("usdate".equals(type)) return isUSDate(value);
			if ("uuid".equals(type)) return isUUId(value);
			if ("url".equals(type)) return isURL(value);
			break;
		case 'v':
			if ("variablename".equals(type)) return isVariableName(Caster.toString(value, ""));
			break;
		case 'x':
			if ("xml".equals(type)) return isXML(value); // DIFF 23
			break;
		case 'z':
			if ("zip".equals(type)) return isZipCode(value);
			if ("zipcode".equals(type)) return isZipCode(value);
			break;
		}
		throw new ExpressionException("invalid type [" + type
				+ "], valid types are [any,array,binary,boolean,component,creditcard,date,time,email,eurodate,float,numeric,guid,integer,query,simple,ssn,string,struct,telephone,URL,UUID,USdate,variableName,zipcode]");

	}

	/**
	 * checks if a value is castable to a certain type
	 * 
	 * @param type any,array,boolean,binary, ...
	 * @param o value to check
	 * @param alsoPattern also check patterns like creditcards,email,phone ...
	 * @param maxlength only used for email,url, string, ignored otherwise
	 * @return
	 */
	public static boolean isCastableTo(String type, Object o, boolean alsoAlias, boolean alsoPattern, int maxlength) {

		type = StringUtil.toLowerCase(type).trim();
		if (type.length() > 2) {
			char first = type.charAt(0);
			switch (first) {
			case 'a':
				if (type.equals("any")) {
					return true;
				}
				else if (type.equals("array")) {
					return isCastableToArray(o);
				}
				break;
			case 'b':
				if (type.equals("boolean") || (alsoAlias && type.equals("bool"))) {
					return isCastableToBoolean(o);
				}
				else if (type.equals("binary")) {
					return isCastableToBinary(o, true);
				}
				else if (alsoAlias && type.equals("bigint")) {
					return isCastableToNumeric(o);
				}
				else if (type.equals("base64")) {
					return Caster.toBase64(o, null, null) != null;
				}
				break;
			case 'c':
				if (alsoPattern && type.equals("creditcard")) {
					return Caster.toCreditCard(o, null) != null;
				}
				if (alsoPattern && type.equals("char")) {
					if (maxlength > -1) {
						String str = Caster.toString(o, null);
						if (str == null) return false;
						return str.length() <= maxlength;
					}
					return isCastableToString(o);
				}
				break;
			case 'd':
				if (type.equals("date")) {
					return isDateAdvanced(o, true);
				}
				else if (type.equals("datetime")) {
					return isDateAdvanced(o, true);
				}
				else if (alsoAlias && type.equals("double")) {
					return isCastableToNumeric(o);
				}
				else if (alsoAlias && type.equals("decimal")) {
					return Caster.toDecimal(o, true, null) != null;
				}
				break;
			case 'e':
				if (alsoAlias && type.equals("eurodate")) {
					return isDateAdvanced(o, true);
				}
				else if (alsoPattern && type.equals("email")) {
					if (maxlength > -1) {
						String str = Caster.toEmail(o, null);
						if (str == null) return false;
						return str.length() <= maxlength;
					}
					return Caster.toEmail(o, null) != null;
				}
				break;
			case 'f':
				if (alsoAlias && type.equals("float")) {
					return isCastableToNumeric(o);
				}
				if (type.equals("function")) {
					return isFunction(o);
				}
				break;
			case 'g':
				if (type.equals("guid")) {
					return isGUId(o);
				}
				break;
			case 'i':
				if (alsoAlias && (type.equals("integer") || type.equals("int"))) {
					return isCastableToNumeric(o);
				}
				break;
			case 'l':
				if (alsoAlias && type.equals("long")) {
					return isCastableToNumeric(o);
				}
				break;
			case 'n':
				if (type.equals("numeric")) {
					return isCastableToNumeric(o);
				}
				else if (type.equals("number")) {
					return isCastableToNumeric(o);
				}

				if (alsoAlias) {
					if (type.equals("node")) return isXML(o);
					else if (type.equals("nvarchar") || type.equals("nchar")) {
						if (maxlength > -1) {
							String str = Caster.toString(o, null);
							if (str == null) return false;
							return str.length() <= maxlength;
						}
						return isCastableToString(o);
					}
				}

				break;
			case 'o':
				if (type.equals("object")) {
					return true;
				}
				else if (alsoAlias && type.equals("other")) {
					return true;
				}
				break;
			case 'p':
				if (alsoPattern && type.equals("phone")) {
					return Caster.toPhone(o, null) != null;
				}
				break;
			case 'q':
				if (type.equals("query")) {
					return isQuery(o);
				}

				if (type.equals("querycolumn")) return isQueryColumn(o);

				break;
			case 's':
				if (type.equals("string")) {
					if (maxlength > -1) {
						String str = Caster.toString(o, null);
						if (str == null) return false;
						return str.length() <= maxlength;
					}
					return isCastableToString(o);
				}
				else if (type.equals("struct")) {
					return isCastableToStruct(o);
				}
				else if (alsoAlias && type.equals("short")) {
					return isCastableToNumeric(o);
				}
				else if (alsoPattern && (type.equals("ssn") || type.equals("social_security_number"))) {
					return Caster.toSSN(o, null) != null;
				}
				break;
			case 't':
				if (type.equals("timespan")) {
					return Caster.toTimespan(o, null) != null;
				}
				if (type.equals("time")) {
					return isDateAdvanced(o, true);
				}
				if (alsoPattern && type.equals("telephone")) {
					return Caster.toPhone(o, null) != null;
				}

				if (alsoAlias && type.equals("timestamp")) return isDateAdvanced(o, true);
				if (alsoAlias && type.equals("text")) {
					if (maxlength > -1) {
						String str = Caster.toString(o, null);
						if (str == null) return false;
						return str.length() <= maxlength;
					}
					return isCastableToString(o);
				}

			case 'u':
				if (type.equals("uuid")) {
					return isUUId(o);
				}
				if (alsoAlias && type.equals("usdate")) {
					return isDateAdvanced(o, true);
				}
				if (alsoPattern && type.equals("url")) {
					if (maxlength > -1) {
						String str = Caster.toURL(o, null);
						if (str == null) return false;
						return str.length() <= maxlength;
					}
					return Caster.toURL(o, null) != null;
				}
				if (alsoAlias && type.equals("udf")) {
					return isFunction(o);
				}
				break;
			case 'v':
				if (type.equals("variablename")) {
					return isVariableName(o);
				}
				else if (type.equals("void")) {
					return isVoid(o);// Caster.toVoid(o,Boolean.TRUE)!=Boolean.TRUE;
				}
				else if (alsoAlias && type.equals("variable_name")) {
					return isVariableName(o);
				}
				else if (alsoAlias && type.equals("variable-name")) {
					return isVariableName(o);
				}
				if (type.equals("varchar")) {
					if (maxlength > -1) {
						String str = Caster.toString(o, null);
						if (str == null) return false;
						return str.length() <= maxlength;
					}
					return isCastableToString(o);
				}
				break;
			case 'x':
				if (type.equals("xml")) {
					return isXML(o);
				}
				break;
			case 'z':
				if (alsoPattern && (type.equals("zip") || type.equals("zipcode"))) {
					return Caster.toZip(o, null) != null;
				}
				break;
			}
		}
		return _isCastableTo(null, type, o);
	}

	private static boolean _isCastableTo(PageContext pcMaybeNull, String type, Object o) {
		if (o instanceof Component) {
			Component comp = ((Component) o);
			return comp.instanceOf(type);
		}
		if (o instanceof Pojo) {
			pcMaybeNull = ThreadLocalPageContext.get(pcMaybeNull);
			return pcMaybeNull != null && Caster.toComponent(pcMaybeNull, ((Pojo) o), type, null) != null;
		}

		if (isArrayType(type) && isArray(o)) {
			String _strType = type.substring(0, type.length() - 2);
			short _type = CFTypes.toShort(_strType, false, (short) -1);
			Array arr = Caster.toArray(o, null);
			if (arr != null) {
				Iterator<Object> it = arr.valueIterator();
				while (it.hasNext()) {
					Object obj = it.next();
					if (!isCastableTo(pcMaybeNull, _type, _strType, obj)) return false;
				}
				return true;
			}
		}
		return false;
	}

	private static boolean isArrayType(String type) {
		return type.endsWith("[]");
	}

	public static boolean isCastableTo(PageContext pc, short type, String strType, Object o) {
		switch (type) {
		case CFTypes.TYPE_ANY:
			return true;
		case CFTypes.TYPE_STRING:
			return isCastableToString(o);
		case CFTypes.TYPE_BOOLEAN:
			return isCastableToBoolean(o);
		case CFTypes.TYPE_NUMERIC:
			return isCastableToNumeric(o);
		case CFTypes.TYPE_STRUCT:
			return isCastableToStruct(o);
		case CFTypes.TYPE_ARRAY:
			return isCastableToArray(o);
		case CFTypes.TYPE_QUERY:
			return isQuery(o);
		case CFTypes.TYPE_QUERY_COLUMN:
			return isQueryColumn(o);
		case CFTypes.TYPE_DATETIME:
			return isDateAdvanced(o, true);
		case CFTypes.TYPE_VOID:
			return isVoid(o);// Caster.toVoid(o,Boolean.TRUE)!=Boolean.TRUE;
		case CFTypes.TYPE_BINARY:
			return isCastableToBinary(o, true);
		case CFTypes.TYPE_TIMESPAN:
			return Caster.toTimespan(o, null) != null;
		case CFTypes.TYPE_UUID:
			return isUUId(o);
		case CFTypes.TYPE_GUID:
			return isGUId(o);
		case CFTypes.TYPE_VARIABLE_NAME:
			return isVariableName(o);
		case CFTypes.TYPE_FUNCTION:
			return isFunction(o);
		case CFTypes.TYPE_IMAGE:
			return ImageUtil.isCastableToImage(pc, o);
		case CFTypes.TYPE_XML:
			return isXML(o);
		}

		return _isCastableTo(pc, strType, o);
	}

	public static boolean isDate(String str, Locale locale, TimeZone tz, boolean lenient) {
		str = str.trim();
		tz = ThreadLocalPageContext.getTimeZone(tz);
		DateFormat[] df;

		// get Calendar
		// Calendar c=JREDateTimeUtil.getThreadCalendar(locale,tz);

		// datetime
		ParsePosition pp = new ParsePosition(0);
		df = FormatUtil.getDateTimeFormats(locale, tz, false);// dfc[FORMATS_DATE_TIME];
		Date d;
		for (int i = 0; i < df.length; i++) {
			pp.setErrorIndex(-1);
			pp.setIndex(0);
			df[i].setTimeZone(tz);
			d = df[i].parse(str, pp);
			if (pp.getIndex() == 0 || d == null || pp.getIndex() < str.length()) continue;

			return true;
		}

		// date
		df = FormatUtil.getDateFormats(locale, tz, false);
		for (int i = 0; i < df.length; i++) {
			pp.setErrorIndex(-1);
			pp.setIndex(0);
			df[i].setTimeZone(tz);
			d = df[i].parse(str, pp);
			if (pp.getIndex() == 0 || d == null || pp.getIndex() < str.length()) continue;
			return true;
		}

		// time
		df = FormatUtil.getTimeFormats(locale, tz, false);
		for (int i = 0; i < df.length; i++) {
			pp.setErrorIndex(-1);
			pp.setIndex(0);
			df[i].setTimeZone(tz);
			d = df[i].parse(str, pp);
			if (pp.getIndex() == 0 || d == null || pp.getIndex() < str.length()) continue;

			return true;
		}

		if (lenient) return isDateSimple(str, false);
		return false;
	}

	/**
	 * Checks if number is valid (not infinity or NaN)
	 * 
	 * @param dbl
	 * @return
	 */
	public static boolean isValid(double dbl) {
		return !Double.isNaN(dbl) && !Double.isInfinite(dbl);
	}

	public static boolean isAnyType(String type) {
		return StringUtil.isEmpty(type) || type.equalsIgnoreCase("object") || type.equalsIgnoreCase("any");
	}

	public static boolean isWrapped(Object o) {
		return o instanceof JavaObject || o instanceof ObjectWrap;
	}
}