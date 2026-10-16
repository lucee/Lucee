package lucee.runtime.config;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.TimeZone;

import lucee.commons.io.CharsetUtil;
import lucee.commons.lang.CharsetX;
import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.util.KeyConstants;

public interface PropFactory<T> {

	public static ArrayPropFactory ARRAY_FACTORY = new ArrayPropFactory();
	public static StructPropFactory STRUCT_FACTORY = new StructPropFactory();
	public static StringPropFactory STRING_FACTORY = new StringPropFactory();
	public static BooleanPropFactory BOOLEAN_FACTORY = new BooleanPropFactory();
	public static TimeSpanPropFactory TIMESPAN_FACTORY = new TimeSpanPropFactory();
	public static TimeZonePropFactory TIMEZONE_FACTORY = new TimeZonePropFactory();
	public static LocalePropFactory LOCALE_FACTORY = new LocalePropFactory();
	public static ShortPropFactory SHORT_FACTORY = new ShortPropFactory();
	public static DoublePropFactory DOUBLE_FACTORY = new DoublePropFactory();
	public static IntegerPropFactory INTEGER_FACTORY = new IntegerPropFactory();
	public static LongPropFactory LONG_FACTORY = new LongPropFactory();
	public static CharsetPropFactory CHARSET_FACTORY = new CharsetPropFactory();
	public static CharsetXPropFactory CHARSETX_FACTORY = new CharsetXPropFactory();
	public static ProcentagePropFactory PROCENTAGE_FACTORY = new ProcentagePropFactory();

	/**
	 * evaluates
	 * 
	 * @param config
	 * @param name
	 * @param val
	 * @param defaultValue
	 * @return
	 */
	public T evaluate(Config config, String name, Object val, short source) throws PageException;

	default Object serialize(Config config, T val) throws PageException {
		if (false) throw new ApplicationException("never"); // never runs, but satisfies compiler
		throw new UnsupportedOperationException("serialize() is not implemented; " + this.getClass().getName());
	}

	public Struct schema(Prop<T> prop);

	public Object resolvedValue(T defaultValue);

	public static class ArrayPropFactory implements PropFactory<Array> {
		private ArrayPropFactory() {}

		private static ArrayPropFactory instance;

		public static ArrayPropFactory getInstance() {
			if (instance == null) instance = new ArrayPropFactory();
			return instance;
		}

		@Override
		public Array evaluate(Config config, String name, Object val, short source) throws PageException {
			return Caster.toArray(val);
		}

		@Override
		public Struct schema(Prop<Array> prop) {
			Struct sct = new StructImpl(Struct.TYPE_LINKED);
			sct.setEL(KeyConstants._type, "array");

			// Define the items allowed in the array
			Struct items = new StructImpl(Struct.TYPE_LINKED);

			Array simpleTypes = new ArrayImpl();
			simpleTypes.appendEL("string");
			simpleTypes.appendEL("number");
			simpleTypes.appendEL("integer");
			simpleTypes.appendEL("boolean");
			simpleTypes.appendEL("null");

			items.setEL(KeyConstants._type, simpleTypes);
			items.setEL(KeyConstants._description, "Only simple values (string, number, boolean) are allowed within this array.");

			sct.setEL("items", items);

			return sct;
		}

		@Override
		public Object resolvedValue(Array value) {
			return value;
		}
	}

	public static class StructPropFactory implements PropFactory<Struct> {
		private StructPropFactory() {}

		@Override
		public Struct evaluate(Config config, String name, Object val, short source) throws PageException {
			return Caster.toStruct(val);
		}

		@Override
		public Struct schema(Prop<Struct> prop) {
			Struct sct = new StructImpl(Struct.TYPE_LINKED);
			sct.setEL(KeyConstants._type, "object");

			// Define that this object can have any number of keys,
			// but the values must be simple (no nesting).
			Struct additionalProps = new StructImpl(Struct.TYPE_LINKED);

			Array simpleTypes = new ArrayImpl();
			simpleTypes.appendEL("string");
			simpleTypes.appendEL("number");
			simpleTypes.appendEL("integer");
			simpleTypes.appendEL("boolean");
			simpleTypes.appendEL("null");

			additionalProps.setEL(KeyConstants._type, simpleTypes);
			additionalProps.setEL(KeyConstants._description, "Only simple values (string, number, boolean) are allowed here.");

			sct.setEL("additionalProperties", additionalProps);

			return sct;
		}

		@Override
		public Object resolvedValue(Struct value) {
			return value;
		}
	}

	public static class StringPropFactory implements PropFactory<String> {
		private StringPropFactory() {}

		@Override
		public String evaluate(Config config, String name, Object val, short source) throws PageException {
			return Caster.toString(val);
		}

		@Override
		public Object serialize(Config config, String val) throws PageException {
			return val;
		}

		@Override
		public Struct schema(Prop<String> prop) {
			Struct sct = new StructImpl(Struct.TYPE_LINKED);
			sct.setEL(KeyConstants._type, "string");
			return sct;
		}

		@Override
		public Object resolvedValue(String value) {
			return value;
		}
	}

	public static class BooleanPropFactory implements PropFactory<Boolean> {
		private BooleanPropFactory() {}

		@Override
		public Boolean evaluate(Config config, String name, Object val, short source) throws PageException {
			return Caster.toBoolean(val);
		}

		@Override
		public Object serialize(Config config, Boolean val) throws PageException {
			return val;
		}

		@Override
		public Struct schema(Prop<Boolean> prop) {
			Struct sct = new StructImpl(Struct.TYPE_LINKED);
			sct.setEL(KeyConstants._type, "boolean");
			return sct;
		}

		@Override
		public Object resolvedValue(Boolean value) {
			return value;
		}
	}

	public static class TimeSpanPropFactory implements PropFactory<TimeSpan> {
		private TimeSpanPropFactory() {}

		@Override
		public TimeSpan evaluate(Config config, String name, Object val, short source) throws PageException {
			return Caster.toTimespan(val);
		}

		@Override
		public Object serialize(Config config, TimeSpan val) throws PageException {
			if (val == null) return null;
			return val.getDay() + "," + val.getHour() + "," + val.getMinute() + "," + val.getSecond();
		}

		@Override
		public Struct schema(Prop<TimeSpan> prop) {
			Struct sct = new StructImpl(Struct.TYPE_LINKED);

			// Define the two possible valid formats
			Array oneOf = new ArrayImpl();

			// 1. The String Format (e.g., "0,0,30,0")
			Struct stringFormat = new StructImpl(Struct.TYPE_LINKED);
			stringFormat.setEL(KeyConstants._type, "string");
			stringFormat.setEL(KeyConstants._description, "Format: 'days,hours,minutes,seconds'");
			// Optional: Add a regex pattern to validate the comma-separated format
			stringFormat.setEL(KeyConstants._pattern, "^\\d+,\\d+,\\d+,\\d+$");

			// 2. The Numeric Format (e.g., 0.1 for days)
			Struct numericFormat = new StructImpl(Struct.TYPE_LINKED);
			numericFormat.setEL(KeyConstants._type, "number");
			numericFormat.setEL(KeyConstants._description, "Numeric value representing a fraction of a day (e.g., 0.5 = 12 hours)");

			oneOf.appendEL(stringFormat);
			oneOf.appendEL(numericFormat);

			sct.setEL("oneOf", oneOf);

			return sct;
		}

		@Override
		public Object resolvedValue(TimeSpan value) {
			return value.getDay() + "," + value.getHour() + "," + value.getMinute() + "," + value.getSeconds();
		}
	}

	public static class LocalePropFactory implements PropFactory<Locale> {
		private LocalePropFactory() {}

		@Override
		public Locale evaluate(Config config, String name, Object val, short source) throws PageException {
			return Caster.toLocale(val);
		}

		@Override
		public Object serialize(Config config, Locale val) throws PageException {
			return Caster.toString(val);
		}

		@Override
		public Struct schema(Prop<Locale> prop) {
			Struct sct = new StructImpl(Struct.TYPE_LINKED);
			sct.setEL(KeyConstants._type, "string");
			sct.setEL(KeyConstants._description, "Locale identifier. Supports named locales (e.g., 'English (US)') " + "or ISO codes (e.g., 'en_US', 'en-GB', 'en_US_variant').");

			// Use a regex pattern that matches the logic in your getLocale() Matchers
			// This validates the format without restricting the specific language/country
			sct.setEL("pattern", "^([a-zA-Z\\s\\(\\)]+|[a-zA-Z]{2,3}([-_][a-zA-Z]{2,3})?([-_][a-zA-Z0-9]+)?)$");

			return sct;
		}

		@Override
		public Object resolvedValue(Locale value) {
			return value;
		}
	}

	public static class TimeZonePropFactory implements PropFactory<TimeZone> {
		private TimeZonePropFactory() {}

		@Override
		public TimeZone evaluate(Config config, String name, Object val, short source) throws PageException {
			return Caster.toTimeZone(val);
		}

		@Override
		public Object serialize(Config config, TimeZone val) throws PageException {
			return Caster.toString(val);
		}

		@Override
		public Struct schema(Prop<TimeZone> prop) {
			Struct sct = new StructImpl(Struct.TYPE_LINKED);
			sct.setEL(KeyConstants._type, "string");
			sct.setEL(KeyConstants._description,
					"TimeZone identifier. Supports standard IDs (e.g., 'America/New_York'), " + "GMT/UTC offsets (e.g., 'GMT+05:30', 'UTC-8'), or English display names.");

			// We use anyOf to allow for the three main patterns identified in your code
			Array anyOf = new ArrayImpl();

			// 1. Standard IDs and Display Names (General string)
			Struct generalId = new StructImpl(Struct.TYPE_LINKED);
			generalId.setEL(KeyConstants._type, "string");
			generalId.setEL(KeyConstants._description, "Standard ID (Area/City) or Display Name.");
			// Pattern matches 'Area/City', 'Area/City/Sub', or words for Display Names
			generalId.setEL("pattern", "^([a-zA-Z0-9_\\-\\+/ ]+)$");

			// 2. GMT/UTC Offset Pattern
			Struct offsetPattern = new StructImpl(Struct.TYPE_LINKED);
			offsetPattern.setEL(KeyConstants._type, "string");
			offsetPattern.setEL(KeyConstants._description, "Offset format (e.g., GMT+2, UTC-05:30, etc/gmt-7).");
			// Matches gmt/utc/etc variants followed by +/- and numbers/colons
			offsetPattern.setEL("pattern", "^(?i)(etc/)?(gmt|utc)[+-]?\\d{1,2}(:?\\d{2})?$");

			anyOf.appendEL(generalId);
			anyOf.appendEL(offsetPattern);

			sct.setEL("anyOf", anyOf);

			return sct;
		}

		@Override
		public Object resolvedValue(TimeZone value) {
			return value;
		}
	}

	public static class ShortPropFactory implements PropFactory<Short> {
		private ShortPropFactory() {}

		@Override
		public Short evaluate(Config config, String name, Object val, short source) throws PageException {
			return Caster.toShort(val);
		}

		@Override
		public Object serialize(Config config, Short val) throws PageException {
			return val != null ? Caster.toInteger(val) : null;
		}

		@Override
		public Struct schema(Prop<Short> prop) {
			Struct sct = new StructImpl(Struct.TYPE_LINKED);
			sct.setEL(KeyConstants._type, "integer");
			return sct;
		}

		@Override
		public Object resolvedValue(Short value) {
			return value;
		}
	}

	public static class DoublePropFactory implements PropFactory<Double> {
		private DoublePropFactory() {}

		@Override
		public Double evaluate(Config config, String name, Object val, short source) throws PageException {
			return Caster.toDouble(val);
		}

		@Override
		public Object serialize(Config config, Double val) throws PageException {
			return val;
		}

		@Override
		public Struct schema(Prop<Double> prop) {
			Struct sct = new StructImpl(Struct.TYPE_LINKED);
			sct.setEL(KeyConstants._type, "number");
			return sct;
		}

		@Override
		public Object resolvedValue(Double value) {
			return value;
		}
	}

	public static class ProcentagePropFactory implements PropFactory<Float> {
		private ProcentagePropFactory() {}

		@Override
		public Float evaluate(Config config, String name, Object val, short source) throws PageException {

			String str = Caster.toString(val);
			if (StringUtil.isEmpty(str)) return 0F;
			str = StringUtil.unwrap(str);
			if (StringUtil.isEmpty(str)) return 0F;

			float res = Caster.toFloatValue(str, 0F);
			if (res < 0F) return 0F;
			if (res > 1F) return 1F;
			return res;
		}

		@Override
		public Object serialize(Config config, Float val) throws PageException {
			if (val == null) return 0f;
			if (val < 0F) return 0F;
			if (val > 1F) return 1F;
			return val;
		}

		@Override
		public Struct schema(Prop<Float> prop) {
			Struct sct = new StructImpl(Struct.TYPE_LINKED);
			sct.setEL(KeyConstants._type, "number");
			return sct;
		}

		@Override
		public Object resolvedValue(Float value) {
			return value;
		}
	}

	public static class IntegerPropFactory implements PropFactory<Integer> {
		private IntegerPropFactory() {}

		@Override
		public Integer evaluate(Config config, String name, Object val, short source) throws PageException {
			return Caster.toInteger(val);
		}

		@Override
		public Object serialize(Config config, Integer val) throws PageException {
			return val;
		}

		@Override
		public Struct schema(Prop<Integer> prop) {
			Struct sct = new StructImpl(Struct.TYPE_LINKED);
			sct.setEL(KeyConstants._type, "integer");
			return sct;
		}

		@Override
		public Object resolvedValue(Integer value) {
			return value;
		}
	}

	public static class LongPropFactory implements PropFactory<Long> {
		private LongPropFactory() {}

		@Override
		public Long evaluate(Config config, String name, Object val, short source) throws PageException {
			return Caster.toLong(val);
		}

		@Override
		public Object serialize(Config config, Long val) throws PageException {
			return val;
		}

		@Override
		public Struct schema(Prop<Long> prop) {
			Struct sct = new StructImpl(Struct.TYPE_LINKED);
			sct.setEL(KeyConstants._type, "integer");
			return sct;
		}

		@Override
		public Object resolvedValue(Long value) {
			return value;
		}
	}

	public static class CharsetPropFactory implements PropFactory<Charset> {
		private CharsetPropFactory() {}

		@Override
		public Charset evaluate(Config config, String name, Object val, short source) throws PageException {
			return CharsetUtil.toCharset(Caster.toString(val));
		}

		@Override
		public Object serialize(Config config, Charset val) throws PageException {
			return Caster.toString(val);
		}

		@Override
		public Struct schema(Prop<Charset> prop) {
			Struct sct = new StructImpl(Struct.TYPE_LINKED);
			sct.setEL(KeyConstants._type, "string");
			sct.setEL(KeyConstants._description, "A valid Java Charset name (e.g., 'UTF-8', 'ISO-8859-1', 'US-ASCII').");

			// Charset names must start with a letter or digit and can contain
			// dots, dashes, underscores, or colons.
			sct.setEL("pattern", "^[a-zA-Z0-9][a-zA-Z0-9\\.\\-_:]*$");

			return sct;
		}

		@Override
		public Object resolvedValue(Charset value) {
			String res = Caster.toString(value, null);
			if (res == null) return value;
			return res;
		}
	}

	public static void appendClassDefinitionProps(Struct properties) {
		appendClassDefinitionProps(properties, null);
	}

	public static void appendClassDefinitionProps(Struct properties, String prefix) {
		boolean hasPrefix = !StringUtil.isEmpty(prefix);
		String p = hasPrefix ? prefix.trim() : "";
		if (hasPrefix && !p.endsWith("-")) p += "-";

		// 1. Basic Class Identification
		Struct cl = createSimple("string", "The fully qualified Java class name.");
		properties.setEL(KeyImpl.init(p + "class"), cl);

		// If there is no prefix, we add the "classname" alias specifically
		if (!hasPrefix) {
			properties.setEL(KeyConstants._classname, cl);
		}
		else {
			properties.setEL(KeyImpl.init(p + "classname"), cl);
		}

		// 2. OSGi Bundle Support
		properties.setEL(KeyImpl.init(p + "bundleName"), createSimple("string", "The OSGi symbolic name of the bundle."));
		properties.setEL(KeyImpl.init(p + "bundleVersion"), createSimple("string", "The version of the OSGi bundle."));

		// 3. Maven Support
		properties.setEL(KeyImpl.init(p + "maven"), createSimple("string", "Maven coordinates (group:artifact:version)."));

		// 4. CFML Component Proxy
		properties.setEL(KeyImpl.init(p + "component"), createSimple("string", "Path to a CFML component used as a driver proxy."));
	}

	public static Struct createSimple(String type, String desc) {
		Struct s = new StructImpl(Struct.TYPE_LINKED);
		s.setEL(KeyConstants._type, type);
		s.setEL(KeyConstants._description, desc);
		return s;
	}

	public static class CharsetXPropFactory implements PropFactory<CharsetX> {
		private CharsetXPropFactory() {}

		@Override
		public CharsetX evaluate(Config config, String name, Object val, short source) throws PageException {
			return CharsetUtil.toCharsetX(Caster.toString(val));
		}

		@Override
		public Object serialize(Config config, CharsetX val) throws PageException {
			return Caster.toString(val);
		}

		@Override
		public Struct schema(Prop<CharsetX> prop) {
			Struct sct = new StructImpl(Struct.TYPE_LINKED);
			sct.setEL(KeyConstants._type, "string");
			sct.setEL(KeyConstants._description, "A valid Java Charset name (e.g., 'UTF-8', 'ISO-8859-1', 'US-ASCII').");

			// Charset names must start with a letter or digit and can contain
			// dots, dashes, underscores, or colons.
			sct.setEL("pattern", "^[a-zA-Z0-9][a-zA-Z0-9\\.\\-_:]*$");

			return sct;
		}

		@Override
		public Object resolvedValue(CharsetX value) {
			String res = Caster.toString(value, null);
			if (res == null) return value;
			return res;
		}
	}

}
