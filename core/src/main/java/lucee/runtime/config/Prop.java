package lucee.runtime.config;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.CharsetX;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.exp.SecurityException;
import lucee.runtime.interpreter.JSONExpressionInterpreter;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.op.OpUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

public class Prop<T> {

	public static short SOURCE_INTERNAL = 0;
	public static short SOURCE_CFCONFIG = 1;
	public static short SOURCE_SYSPROPENVVAR = 2;

	public static short TYPE_SIMPLE = 1;
	public static short TYPE_MAP = 2;
	public static short TYPE_LIST = 4;
	public static List<Prop<?>> instances = new ArrayList<>();

	String[] keys;
	T defaultValue;

	Choice<T>[] choices;
	String[] customEnvVarSystemProps;
	String description;
	private String parent;
	// private Class<T> type;
	final private PropFactory<T> factory;
	private int access = -1;
	private boolean logGlobal;
	private boolean deprecated;
	private boolean hidden;
	private boolean noEnvVar;
	private final short type;
	private boolean lowerCaseKeys;
	private boolean handleEmptyAsNull = true;
	private String[] envVarSystemProps;

	private Prop(PropFactory<T> factory) {
		this(factory, TYPE_SIMPLE);
	}

	private Prop(PropFactory<T> factory, short type) {
		if (factory == null) throw new NullPointerException();
		this.factory = factory;
		this.type = type;
		instances.add(this);
	}

	public static Prop<Array> arr() {
		return new Prop<Array>(PropFactory.ARRAY_FACTORY);
	}

	public static Prop<Struct> sct() {
		return new Prop<Struct>(PropFactory.STRUCT_FACTORY);
	}

	public static Prop<Struct> sct(short type) {
		return new Prop<Struct>(PropFactory.STRUCT_FACTORY, type);
	}

	public static Prop<String> str() {
		return new Prop<String>(PropFactory.STRING_FACTORY);
	}

	public static Prop<String> str(short type) {
		return new Prop<String>(PropFactory.STRING_FACTORY, type);
	}

	public static Prop<Boolean> bool() {
		return new Prop<Boolean>(PropFactory.BOOLEAN_FACTORY);
	}

	public static Prop<Boolean> bool(short type) {
		return new Prop<Boolean>(PropFactory.BOOLEAN_FACTORY, type);
	}

	public static Prop<TimeSpan> timespan() {
		return new Prop<TimeSpan>(PropFactory.TIMESPAN_FACTORY);
	}

	public static Prop<TimeSpan> timespan(short type) {
		return new Prop<TimeSpan>(PropFactory.TIMESPAN_FACTORY, type);
	}

	public static Prop<TimeZone> timezone() {
		return new Prop<TimeZone>(PropFactory.TIMEZONE_FACTORY);
	}

	public static Prop<TimeZone> timezone(short type) {
		return new Prop<TimeZone>(PropFactory.TIMEZONE_FACTORY, type);
	}

	public static Prop<Locale> locale() {
		return new Prop<Locale>(PropFactory.LOCALE_FACTORY);
	}

	public static Prop<Locale> locale(short type) {
		return new Prop<Locale>(PropFactory.LOCALE_FACTORY, type);
	}

	public static Prop<Short> shor() {
		return new Prop<Short>(PropFactory.SHORT_FACTORY);
	}

	public static Prop<Short> shor(short type) {
		return new Prop<Short>(PropFactory.SHORT_FACTORY, type);
	}

	public static Prop<Double> dbl() {
		return new Prop<Double>(PropFactory.DOUBLE_FACTORY);
	}

	public static Prop<Float> procentage() {
		return new Prop<Float>(PropFactory.PROCENTAGE_FACTORY);
	}

	public static Prop<Double> dbl(short type) {
		return new Prop<Double>(PropFactory.DOUBLE_FACTORY, type);
	}

	public static Prop<Integer> integer() {
		return new Prop<Integer>(PropFactory.INTEGER_FACTORY);
	}

	public static Prop<Integer> integer(short type) {
		return new Prop<Integer>(PropFactory.INTEGER_FACTORY, type);
	}

	public static Prop<Long> loong() {
		return new Prop<Long>(PropFactory.LONG_FACTORY);
	}

	public static Prop<Long> loong(short type) {
		return new Prop<Long>(PropFactory.LONG_FACTORY, type);
	}

	public static Prop<Charset> charset() {
		return new Prop<Charset>(PropFactory.CHARSET_FACTORY);
	}

	public static Prop<Charset> charset(short type) {
		return new Prop<Charset>(PropFactory.CHARSET_FACTORY, type);
	}

	public static Prop<CharsetX> charSet() {
		return new Prop<CharsetX>(PropFactory.CHARSETX_FACTORY);
	}

	public static Prop<CharsetX> charSet(short type) {
		return new Prop<CharsetX>(PropFactory.CHARSETX_FACTORY, type);
	}

	public static <T> Prop<T> custom(PropFactory<T> instance) {
		return new Prop<T>(instance);
	}

	public static <T> Prop<T> custom(PropFactory<T> instance, short type) {
		return new Prop<T>(instance, type);
	}

	public Prop<T> keys(String... keys) {
		this.keys = keys;
		return this;
	}

	public Prop<T> description(String description) {
		this.description = description;
		return this;
	}

	public Prop<T> defaultValue(T defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}

	public Prop<T> choices(Choice<T>... choices) {
		this.choices = choices;
		return this;
	}

	public Prop<T> systemPropEnvVar(String... envVarSystemProps) {
		this.customEnvVarSystemProps = envVarSystemProps;
		return this;
	}

	public Prop<T> access(int access) {
		this.access = access;
		return this;
	}

	public Prop<T> deprecated() {
		this.deprecated = true;
		return this;
	}

	public Prop<T> lowerCaseKeys() {
		this.lowerCaseKeys = true;
		return this;
	}

	public Prop<T> handleEmptyAsNull(boolean handleEmptyAsNull) {
		this.handleEmptyAsNull = handleEmptyAsNull;
		return this;
	}

	public Prop<T> hidden() {
		this.hidden = true;
		return this;
	}

	public Prop<T> noEnvVar() {
		this.noEnvVar = true;
		return this;
	}

	public Prop<T> parent(String parent) {
		this.parent = parent;
		return this;
	}

	public Prop<T> logGlobal() {
		this.logGlobal = true;
		return this;
	}

	static class Choice<T> {
		final Object[] values;
		final T value;
		private String description;

		public Choice(T value, Object... values) {
			this.values = values;
			this.value = value;
		}

		public boolean matches(Object val) {
			for (Object v: values) {
				try {
					if (OpUtil.compare(null, v, val) == 0) {
						return true;
					}
				}
				catch (PageException pe) {}
			}
			return false;
		}

		public boolean equal(Object obj) {
			return this.value.equals(obj);
		}

		public Choice<T> description(String description) {
			this.description = description;
			return this;
		}
	}

	public Object getDefaultValueResolved() {
		// If it's a factory-based prop (no choices), just return the raw default
		if (choices != null && choices.length > 0) {
			for (Choice<T> c: choices) {
				// Compare the internal T value to the defaultValue T
				if (Objects.equals(c.value, defaultValue)) {
					return c.values[0]; // Return the "Canonical" name (always, currentToRoot, etc.)
				}
			}
		}
		return factory.resolvedValue(defaultValue);
	}

	public T get(ConfigServerImpl config, Struct root) {
		return get(config, root, true);
	}

	public T get(ConfigServerImpl config, Struct root, boolean checkEnv) {
		if (type != TYPE_SIMPLE) { // only happens when set wrong in code
			throw new RuntimeException("Invalid type [" + type + "]");
		}

		if (access != -1) {
			if (!ConfigUtil.hasAccess(config, access)) return defaultValue;
		}

		Struct data = null;

		try {
			// check system properties and env var
			if (checkEnv && !noEnvVar) {
				for (String key: envVarSystemProps()) {
					final Object val = SystemUtil.getSystemPropOrEnvVarObject(key, null);
					if (StringUtil.isEmpty(val)) continue;
					return get(config, key, val, Prop.SOURCE_SYSPROPENVVAR);
				}
			}

			if (parent == null) {
				data = root;
			}
			else {
				data = ConfigUtil.getAsStruct(parent, root);
			}

			for (String key: keys) {
				final Object val = data.get(KeyImpl.init(key), null);
				if (StringUtil.isEmpty(val)) continue;
				return get(config, key, val, Prop.SOURCE_CFCONFIG);
			}
		}
		catch (Exception ex) {
			ConfigFactoryImpl.log(config, ex);

			try {
				if (data == null) {
					throw new PageRuntimeException(ex);
				}
				String s = CFMLEngineFactory.getInstance().getCastUtil().fromStructToJsonString(data);
				PageRuntimeException pre = new PageRuntimeException("could not load [" + s + "]");
				ExceptionUtil.initCauseEL(pre, ex);
				throw pre;
			}
			catch (Exception e) {
				throw new PageRuntimeException(ex);
			}
		}
		return defaultValue;
	}

	private T get(ConfigServerImpl config, String key, Object val, short source) throws PageException {
		// only string values can contain placeholders (${...}); resolve them before evaluating
		if (Decision.isSimpleValue(val) && val instanceof String) {
			String str = Caster.toString(val);
			if (!StringUtil.isEmpty(str, true)) {
				str = config.replacePlaceHolder(str.trim());
				if (choices != null) {
					for (Choice<T> choice: choices) {
						if (choice.matches(str)) {
							return choice.value;
						}
					}
					return defaultValue;
				}
				if (str == null || (handleEmptyAsNull && StringUtil.isEmpty(str, true))) {
					return defaultValue;
				}
				return factory.evaluate(config, key, str, source);
			}
			return null;
		}
		else {
			// non-string values (TimeSpan, numbers, booleans, ...) are handed to the factory as-is;
			// stringifying them here would be lossy, e.g. a TimeSpan becomes a fractional-day decimal
			// and loses sub-second precision when parsed back (50 minutes -> 49 minutes 59 seconds)
			if ((handleEmptyAsNull && StringUtil.isEmpty(val, true))) {
				return defaultValue;
			}
			if (choices != null) {
				String str = Caster.toString(val);
				for (Choice<T> choice: choices) {
					if (choice.matches(str)) {
						return choice.value;
					}
				}
				return defaultValue;
			}
			return factory.evaluate(config, key, val, source);
		}
	}

	private Object getSystemPropOrEnvVar(Config config, String key) throws PageException {

		// simple value
		String str = SystemUtil.getSystemPropOrEnvVar(key, null);
		if (!StringUtil.isEmpty(str, true)) {
			str = str.trim();

			if (choices != null) {
				for (Choice<T> choice: choices) {
					if (choice.matches(str)) {
						return choice.value;
					}

				}
				return defaultValue;
			}
			if (str == null || (handleEmptyAsNull && StringUtil.isEmpty(str, true))) {
				return defaultValue;
			}
			return factory.evaluate(config, key, str, Prop.SOURCE_SYSPROPENVVAR);
		}

		// structure

		return null;
	}

	// the admin input is always a flat struct keyed by the config key(s), so the value is read flat
	// (ignoring any parent), while it is stored under this prop's canonical parent + keys[0]
	public void write(ConfigServerImpl config, Struct input) throws PageException {

		Struct root = config.raw();

		if (access != -1) {
			if (!ConfigUtil.hasAccess(config, access)) {
				// TODO improve exception message
				throw new SecurityException("no access to update this setting");
			}
		}

		T existing = getFromInput(config, input);
		Object serialized = null;
		if (choices != null) {
			boolean matchFound = false;
			for (Choice<T> choice: choices) {
				if (choice.equal(existing)) {
					serialized = choice.values[0];
					matchFound = true;
					break;
				}
			}
			if (!matchFound) {
				// TODO improve
				throw new ApplicationException("no matching choice found");
			}
		}
		else {
			serialized = this.factory.serialize(config, existing);
		}

		Struct data;
		if (parent == null) {
			data = root;
		}
		else {
			data = ConfigUtil.getAsStruct(parent, root);
		}

		Key key = KeyImpl.init(keys[0]);
		if (existing == defaultValue) {
			data.removeEL(key);
		}
		else {
			data.set(key, serialized);
		}

	}

	private T getFromInput(ConfigServerImpl config, Struct input) throws PageException {
		for (String key: keys) {
			final Object val = input.get(KeyImpl.init(key), null);
			if (StringUtil.isEmpty(val)) continue;
			return get(config, key, val, Prop.SOURCE_CFCONFIG);
		}
		return defaultValue;
	}

	public Map<String, T> map(ConfigServerImpl config, Struct root) {
		return map(config, root, new ConcurrentHashMap<>(), true, KeyConstants._name);
	}

	public Map<String, T> map(ConfigServerImpl config, Struct root, Map<String, T> map) {
		return map(config, root, map, true, KeyConstants._name);
	}

	public Map<String, T> map(ConfigServerImpl config, Struct root, Map<String, T> map, boolean checkEnv, Key fieldName) {
		if (type != TYPE_MAP) { // only happens when set wrong in code
			throw new RuntimeException("Invalid type [" + type + "]");
		}
		if (access != -1) {
			if (!ConfigUtil.hasAccess(config, access)) return map;
		}
		Struct data = null;
		try {
			// TODO
			if (customEnvVarSystemProps != null) {
				throw new RuntimeException("not supported yet");
			}

			// env var
			if (checkEnv) {
				for (String key: envVarSystemProps()) {
					Object val = SystemUtil.getSystemPropOrEnvVarObject(key, null);
					if (StringUtil.isEmpty(val)) continue;

					Struct sct = Caster.toStruct(val, null);
					if (sct == null) {
						sct = arrayToStruct(fieldName, val, null);
					}

					if (sct == null) continue;
					_map(config, sct, map, Prop.SOURCE_SYSPROPENVVAR);
				}
			}

			// CFConfig data
			if (parent == null) {
				data = root;
			}
			else {
				data = ConfigUtil.getAsStruct(parent, root);
			}
			data = ConfigUtil.getAsStruct(config, data, true, keys);
			_map(config, data, map, Prop.SOURCE_CFCONFIG);
			return map;
		}
		catch (Exception ex) {
			if (logGlobal) LogUtil.logGlobal(config, "config-loading", ex);
			else ConfigFactoryImpl.log(config, ex);

			try {
				if (data == null) {
					throw new PageRuntimeException(ex);
				}
				String str = CFMLEngineFactory.getInstance().getCastUtil().fromStructToJsonString(data);
				PageRuntimeException pre = new PageRuntimeException("could not load [" + str + "]");
				ExceptionUtil.initCauseEL(pre, ex);
				throw pre;
			}
			catch (Exception e) {
				throw new PageRuntimeException(ex);
			}
		}
	}

	private Struct arrayToStruct(Key fieldName, Object val, Struct defaultValue) {
		Object[] arr = Caster.toNativeArray(val, null);

		if (arr == null || arr.length == 0) return defaultValue;
		Struct structs = new StructImpl();
		Struct tmp;
		String name;
		for (Object o: arr) {
			tmp = Caster.toStruct(o, null);

			if (tmp == null) continue;
			name = Caster.toString(tmp.get(fieldName, null), null);
			if (StringUtil.isEmpty(name)) continue;
			structs.setEL(name, tmp);
		}

		return structs;
	}

	private void _map(ConfigServerImpl config, Struct data, Map<String, T> map, short source) throws PageException {
		Iterator<Entry<Key, Object>> it = data.entryIterator();
		Entry<Key, Object> e;
		String key;
		Object val;
		while (it.hasNext()) {
			e = it.next();
			key = lowerCaseKeys ? e.getKey().getLowerString() : e.getKey().getString();
			val = e.getValue();
			if (val == null || (handleEmptyAsNull && StringUtil.isEmpty(val, true)) || map.containsKey(key)) {
				continue;
			}
			T evaluated = factory.evaluate(config, key, e.getValue(), source);
			if (evaluated == null) continue;
			map.put(key, evaluated);
		}
	}

	public List<T> list(ConfigServerImpl config, Struct root) {
		return list(config, root, true);
	}

	public List<T> list(ConfigServerImpl config, Struct root, boolean checkEnv) {
		if (type != TYPE_LIST) { // only happens when set wrong in code
			throw new RuntimeException("Invalid type [" + type + "]");
		}
		List<T> list = new ArrayList<>();
		if (access != -1) {
			if (!ConfigUtil.hasAccess(config, access)) return list;
		}

		Struct data = null;

		try {
			if (checkEnv) {
				for (String key: envVarSystemProps()) {
					Object val = SystemUtil.getSystemPropOrEnvVarObject(key, null);
					if (StringUtil.isEmpty(val)) continue;
					Array arr = Caster.toArray(val, null);
					if (arr == null) continue;
					list(config, arr, list, Prop.SOURCE_SYSPROPENVVAR);

				}
			}

			if (parent == null) {
				data = root;
			}
			else {
				data = ConfigUtil.getAsStruct(parent, root);
			}

			Array arr = ConfigUtil.getAsArray(config, data, true, keys);
			list(config, arr, list, Prop.SOURCE_CFCONFIG);
			return list;
		}
		catch (Exception ex) {
			if (logGlobal) LogUtil.logGlobal(config, "config-loading", ex);
			else ConfigFactoryImpl.log(config, ex);
			try {
				if (data == null) {
					throw new PageRuntimeException(ex);
				}
				String str = CFMLEngineFactory.getInstance().getCastUtil().fromStructToJsonString(data);
				PageRuntimeException pre = new PageRuntimeException("could not load [" + str + "]");
				ExceptionUtil.initCauseEL(pre, ex);
				throw pre;
			}
			catch (Exception e) {
				throw new PageRuntimeException(ex);
			}

		}
	}

	private void list(ConfigServerImpl config, Array raw, List<T> list, short source) throws PageException {
		Iterator<Entry<Key, Object>> it = raw.entryIterator();
		Entry<Key, Object> e;
		String key;
		Object val;
		while (it.hasNext()) {
			e = it.next();
			key = lowerCaseKeys ? e.getKey().getLowerString() : e.getKey().getString();
			val = e.getValue();
			if (val == null || (handleEmptyAsNull && StringUtil.isEmpty(val, true))) {
				continue;
			}
			T evaluated = factory.evaluate(config, key, val, source);
			if (evaluated == null) continue;
			list.add(evaluated);
		}
	}

	public static Struct createConfig(Config config, boolean full) throws IllegalArgumentException, IllegalAccessException, PageException {

		ConfigServerImpl cs = ConfigUtil.getConfigServerImpl(config);

		instances.sort(new PropComparator());
		Struct root = new StructImpl(Struct.TYPE_LINKED);
		// because we have lazy loading we need to make sure all props are loaded first

		Map<Key, Field> fields = new HashMap<>();
		for (Field f: ConfigServerImpl.class.getDeclaredFields()) {
			if (Modifier.isStatic(f.getModifiers())) continue;
			// print.e(f.getName());
			fields.put(KeyImpl.init(f.getName()), f);
		}
		try {
			cs.touchAll(null);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}

		Key name, fullName, parentName;
		Field field;
		Struct sct;
		Object val;
		outer: for (Prop<?> p: instances) {
			// print.e("---- " + p.parent + "->" + p.keys[0] + " ---");

			for (String k: p.keys) {
				name = KeyImpl.init(k);
				if (p.parent != null) fullName = KeyImpl.init(p.parent + k);
				else fullName = KeyImpl.init(k);
				field = fields.get(fullName);
				if (field != null) {
					field.setAccessible(true);
					if (p.parent != null) {
						parentName = KeyImpl.init(p.parent);
						sct = Caster.toStruct(root.get(parentName, null), null);
						if (sct == null) {
							sct = new StructImpl(Struct.TYPE_LINKED);
							root.set(parentName, sct);
						}
					}
					else {
						sct = root;
					}
					val = field.get(cs);
					// LDEV-6362 unwrap lazy ConfigValue holders, mirroring the old nullable field semantics
					if (val instanceof ConfigValue) {
						ConfigValue<?> cv = (ConfigValue<?>) val;
						val = cv.isInitialized() ? cv.peek() : null;
					}
					if (val != null && (full || !val.equals(p.defaultValue))) {
						sct.set(name, val);
						sct.set(name.getString() + "_default", p.defaultValue);
					}
					// print.e("ok: " + key);
					continue outer;
				}
				break;
			}
			// print.e("ko: " + p.keys[0]);

			// Object field = Reflector.getField(cs, p.keys[0], null);
			// if (field != null) print.e("ok: " + p.keys[0]);
			// else print.e("ko: " + p.keys[0]);
		}

		/*
		 * MethodInstance getter; Object result; outer: for (Prop<?> p: instances) { print.e("---- " +
		 * p.parent + "->" + p.keys[0] + " ---"); for (String key: p.keys) { if (p.parent != null) { key =
		 * p.parent + key; } getter = Reflector.getGetter(cs.getClass(), key, false, true, true, null); if
		 * (getter != null) { result = getter.invoke(cs); sct.put(key, result); continue outer; } }
		 * 
		 * print.e("ko: " + p.keys[0]);
		 * 
		 * }
		 */
		return root;
	}

	public static Struct createSystemPropEnvVar() throws PageException {
		Key keySP = KeyImpl.init("systemProperties");
		Key keyEV = KeyImpl.init("environmentVariables");
		Struct data = new StructImpl(Struct.TYPE_LINKED), item;
		int row;
		String key;
		for (Prop<?> p: instances) {
			// if (p.hidden || p.deprecated) continue;
			key = StringUtil.isEmpty(p.parent) ? p.keys[0] : p.parent + "_" + p.keys[0];

			item = new StructImpl(Struct.TYPE_LINKED);
			data.set(KeyImpl.init(key), item);

			if (p.type == TYPE_MAP) item.set(KeyConstants._type, "map");
			else if (p.type == TYPE_LIST) item.set(KeyConstants._type, "list");
			else item.set(KeyConstants._type, "simple");

			item.set(KeyConstants._description, p.description);

			String[] raw = p.envVarSystemProps();

			// system properties
			item.set(keySP, new ArrayImpl(raw));
			// env var
			ArrayImpl arr = new ArrayImpl();
			for (String r: raw) {
				arr.add(SystemUtil.convertSystemPropToEnvVar(r));
			}
			item.set(keyEV, arr);
		}

		return data;
	}

	public static Struct createEnvVars() throws PageException {
		Key keySysprop = KeyImpl.init("sysprop");
		Key keyEnvvar = KeyImpl.init("envvar");
		Key keyDesc = KeyImpl.init("desc");
		Key keyCategory = KeyImpl.init("category");
		Key keyType = KeyImpl.init("type");
		Key keySource = KeyImpl.init("source");
		Key keyCfconfig = KeyImpl.init("cfconfig");
		Key keyDeprecated = KeyImpl.init("deprecated");

		Map<String, Struct> bySysprop = new LinkedHashMap<>();

		Array staticEntries = loadStaticSyspropEnvvarEntries();
		Iterator<Object> it = staticEntries.valueIterator();
		while (it.hasNext()) {
			Struct entry = Caster.toStruct(it.next());
			String sysprop = Caster.toString(entry.get(keySysprop, null), null);
			if (StringUtil.isEmpty(sysprop)) continue;
			Struct copy = (Struct) entry.duplicate(true);
			copy.set(keySource, "runtime");
			bySysprop.put(sysprop, copy);
		}

		for (Prop<?> p: instances) {
			if (p.hidden) continue;

			for (String sysprop: p.envVarSystemProps()) {
				Struct entry = bySysprop.get(sysprop);
				if (entry == null) {
					entry = new StructImpl(Struct.TYPE_LINKED);
					entry.set(keySysprop, sysprop);
					entry.set(keyEnvvar, SystemUtil.convertSystemPropToEnvVar(sysprop));
					bySysprop.put(sysprop, entry);
				}

				if (!StringUtil.isEmpty(p.description)) {
					entry.set(keyDesc, p.description);
				}
				else if (entry.get(keyDesc, null) == null) {
					entry.set(keyDesc, "");
				}

				entry.set(keyType, p.envVarType());

				if (p.defaultValue != null) {
					entry.set(KeyConstants._default, p.getDefaultValueResolved());
				}

				if (entry.get(keyCategory, null) == null) {
					entry.set(keyCategory, p.envVarCategory());
				}

				String cfconfig = p.cfconfigKey();
				if (cfconfig != null) {
					entry.set(keyCfconfig, cfconfig);
				}

				entry.set(keySource, "prop");

				if (p.deprecated && entry.get(keyDeprecated, null) == null) {
					entry.set(keyDeprecated, true);
				}
			}
		}

		List<String> sorted = new ArrayList<>(bySysprop.keySet());
		sorted.sort(String.CASE_INSENSITIVE_ORDER);

		Array entries = new ArrayImpl();
		for (String sysprop: sorted) {
			Struct entry = bySysprop.get(sysprop);
			if (entry.get(keyEnvvar, null) == null) {
				entry.set(keyEnvvar, SystemUtil.convertSystemPropToEnvVar(sysprop));
			}
			entries.appendEL(entry);
		}

		Struct root = new StructImpl(Struct.TYPE_LINKED);
		root.setEL(KeyImpl.init("$schema"), "https://lucee.org/schemas/env-vars-1.json");
		root.setEL(KeyConstants._title, "Lucee System Properties & Environment Variables");
		root.setEL(KeyImpl.init("entries"), entries);
		return root;
	}

	private static Array loadStaticSyspropEnvvarEntries() throws PageException {
		InputStream is = null;
		try {
			is = Prop.class.getClassLoader().getResourceAsStream("/resource/setting/sysprop-envvar.json");
			if (is == null) throw new ApplicationException("Failed to read [/resource/setting/sysprop-envvar.json]");
			String raw = IOUtil.toString(is, StandardCharsets.UTF_8);
			return Caster.toArray(new JSONExpressionInterpreter(false, JSONExpressionInterpreter.FORMAT_JSON5).interpret(null, raw));
		}
		catch (PageException pe) {
			throw pe;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}
		finally {
			IOUtil.closeEL(is);
		}
	}

	private String envVarType() {
		if (factory == PropFactory.BOOLEAN_FACTORY) return "boolean";
		if (factory == PropFactory.TIMESPAN_FACTORY) return "timespan";
		if (factory == PropFactory.INTEGER_FACTORY || factory == PropFactory.LONG_FACTORY || factory == PropFactory.SHORT_FACTORY || factory == PropFactory.DOUBLE_FACTORY
				|| factory == PropFactory.PROCENTAGE_FACTORY) {
			return "numeric";
		}
		return "string";
	}

	private String envVarCategory() {
		if (!StringUtil.isEmpty(parent, true)) {
			if ("monitoring".equalsIgnoreCase(parent)) return "debugging";
			return parent.toLowerCase(Locale.ENGLISH);
		}
		return "deployment";
	}

	private String cfconfigKey() {
		if (keys == null || keys.length == 0) return null;
		if (!StringUtil.isEmpty(parent, true)) return parent + "." + keys[0];
		return keys[0];
	}

	public static Struct createConfigSchema(boolean strict) {
		instances.sort(new PropComparator());

		Struct root = new StructImpl(Struct.TYPE_LINKED);
		root.setEL(KeyImpl.init("$id"), "https://lucee.org/schema-" + (strict ? "strict" : "permissive") + ".json");
		root.setEL(KeyImpl.init("$schema"), "https://json-schema.org/draft/2020-12/schema");
		root.setEL(KeyConstants._title, "Lucee Config Schema (" + (strict ? "Strict" : "Permissive") + ")");
		root.setEL(KeyConstants._type, "object");

		Struct rootProps = new StructImpl(Struct.TYPE_LINKED);
		root.setEL(KeyConstants._properties, rootProps);

		for (Prop<?> p: instances) {
			// 1. Filter out hidden props and, if strict, deprecated props
			if (p.hidden) continue;
			if (strict && p.deprecated) continue;

			Struct targetProps = StringUtil.isEmpty(p.parent) ? rootProps : getOrCreateParentProps(rootProps, p.parent);

			// 2. Resolve Item Schema (Choice oneOf vs Factory)
			Struct itemSchema = new StructImpl(Struct.TYPE_LINKED);

			if (p.choices != null && p.choices.length > 0) {
				Array oneOf = new ArrayImpl();

				for (Choice<?> choice: p.choices) {
					// Primary value for this choice
					Object primaryValue = choice.values[0];

					Struct option = new StructImpl(Struct.TYPE_LINKED);
					option.setEL(KeyConstants._const, primaryValue);

					if (!StringUtil.isEmpty(choice.description)) {
						option.setEL(KeyConstants._description, choice.description);
					}
					oneOf.appendEL(option);

					// If NOT strict, add aliases as hidden options for validation
					if (!strict && choice.values.length > 1) {
						for (int i = 1; i < choice.values.length; i++) {
							Struct aliasOpt = new StructImpl(Struct.TYPE_LINKED);
							aliasOpt.setEL(KeyConstants._const, choice.values[i]);
							aliasOpt.setEL(KeyConstants._description, "Alias for '" + primaryValue + "'");
							oneOf.appendEL(aliasOpt);
						}
					}
				}
				itemSchema.setEL(KeyImpl.init("oneOf"), oneOf);
				// NOTE: type and enum are omitted here as oneOf handles constraints
			}
			else {
				// Fallback to Factory for complex types or simple types without specific choices
				itemSchema = p.factory.schema((Prop) p);
			}

			// 3. Add Metadata
			if (!StringUtil.isEmpty(p.description)) {
				itemSchema.setEL(KeyConstants._description, p.description);
			}
			if (p.defaultValue != null) {
				itemSchema.setEL(KeyConstants._default, p.getDefaultValueResolved());
			}

			if (!strict && p.deprecated) {
				itemSchema.setEL(KeyImpl.init("deprecated"), true);
			}

			// 4. Handle Structure Wrapping (Map/List)
			Struct finalSchema = itemSchema;
			if (Prop.TYPE_MAP == p.type) {
				finalSchema = new StructImpl(Struct.TYPE_LINKED);
				finalSchema.setEL(KeyConstants._type, "object");
				finalSchema.setEL(KeyImpl.init("additionalProperties"), itemSchema);
			}
			else if (Prop.TYPE_LIST == p.type) {
				finalSchema = new StructImpl(Struct.TYPE_LINKED);
				finalSchema.setEL(KeyConstants._type, "array");
				finalSchema.setEL(KeyConstants._items, itemSchema);
			}

			// 5. Register Keys/Aliases
			if (p.keys != null && p.keys.length > 0) {
				// Register Primary Key
				targetProps.setEL(KeyImpl.init(p.keys[0]), finalSchema);

				// Register Aliases if not in strict mode
				if (!strict && p.keys.length > 1) {
					for (int i = 1; i < p.keys.length; i++) {
						String key = p.keys[i];
						Struct copy = (Struct) finalSchema.duplicate(true);

						copy.setEL(KeyConstants._deprecated, true);

						String aliasDesc = "Deprecated: Use '" + p.keys[0] + "' instead.";
						if (!StringUtil.isEmpty(p.description)) {
							aliasDesc += "\n" + p.description;
						}
						copy.setEL(KeyConstants._description, aliasDesc);

						targetProps.setEL(KeyImpl.init(key), copy);
					}
				}
			}
		}
		return root;
	}

	public Object getCanonicalValue(Object internalValue) {
		if (choices != null) {
			for (Choice<T> choice: choices) {
				if (choice.matches(internalValue)) {
					return choice.values[0];
				}
			}
		}
		return internalValue;
	}

	private String[] envVarSystemProps() {
		if (envVarSystemProps == null) {
			Set<String> set = new LinkedHashSet<>();

			for (String k: keys) {
				StringBuilder sb = new StringBuilder("lucee");
				// parent
				if (!StringUtil.isEmpty(parent, true)) {
					sb.append('.').append(parent);
				}
				// keys
				sb.append('.').append(k);
				set.add(sb.toString());
			}

			if (customEnvVarSystemProps != null) {
				for (String k: customEnvVarSystemProps) {
					set.add(k);
				}
			}
			envVarSystemProps = ListUtil.toStringArray(set);
		}
		return envVarSystemProps;
	}

	private static Struct getOrCreateParentProps(Struct rootProps, String parentName) {
		Object existing = rootProps.get(parentName, null);
		Struct parentObj;

		if (existing instanceof Struct) {
			parentObj = (Struct) existing;
		}
		else {
			parentObj = new StructImpl(Struct.TYPE_LINKED);
			parentObj.setEL(KeyConstants._type, "object");
			parentObj.setEL(KeyConstants._properties, new StructImpl(Struct.TYPE_LINKED));
			rootProps.setEL(parentName, parentObj);
		}

		return (Struct) parentObj.get(KeyConstants._properties, null);
	}

	private static class PropComparator implements Comparator<Prop<?>> {

		@Override
		public int compare(Prop<?> p1, Prop<?> p2) {
			// 1. Handle Parent Comparison
			int parentComp = 0;
			if (p1.parent == null && p2.parent != null) return -1;
			if (p1.parent != null && p2.parent == null) return 1;

			// If both have parents, compare them alphabetically
			if (p1.parent != null && p2.parent != null) {
				parentComp = p1.parent.compareToIgnoreCase(p2.parent);
				if (parentComp != 0) {
					return parentComp;
				}
			}

			// 2. If they are in the same group (same parent or both null), sort by Key
			String key1 = (p1.keys != null && p1.keys.length > 0) ? p1.keys[0] : "";
			String key2 = (p2.keys != null && p2.keys.length > 0) ? p2.keys[0] : "";
			return key1.compareToIgnoreCase(key2);
		}
	}

	public Choice<T>[] getChoices() {
		return choices;
	}

	public static String toSource(short source, String defaultValue) {
		if (source == SOURCE_CFCONFIG) return "cfconfig";
		if (source == SOURCE_SYSPROPENVVAR) return "sysprop_envvar";
		return defaultValue;
	}
}