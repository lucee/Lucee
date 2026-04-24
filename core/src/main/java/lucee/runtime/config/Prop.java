package lucee.runtime.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.CharsetX;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.op.OpUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

public class Prop<T> {

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
		if (type != TYPE_SIMPLE) { // only happens when set wrong in code
			throw new RuntimeException("Invalid type [" + type + "]");
		}

		if (access != -1) {
			if (!ConfigUtil.hasAccess(config, access)) return defaultValue;
		}
		Object val;
		String str;
		Struct data;
		if (parent == null) {
			data = root;
		}
		else {
			data = ConfigUtil.getAsStruct(parent, root);
		}

		try {
			for (String key: envVarSystemProps()) {
				str = SystemUtil.getSystemPropOrEnvVar(key, null);
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
					return factory.evaluate(config, key, str);
				}
			}

			for (String key: keys) {
				val = data.get(KeyImpl.init(key), null);
				if (val == null) continue;

				if (Decision.isSimpleValue(val)) {
					str = Caster.toString(val);
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
						return factory.evaluate(config, key, str);
					}
				}
				else {
					if (val == null || (handleEmptyAsNull && StringUtil.isEmpty(val, true))) {
						return defaultValue;
					}
					return factory.evaluate(config, key, val);
				}
			}
		}
		catch (Exception ex) {
			ConfigFactoryImpl.log(config, ex);

			try {
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

	public Map<String, T> map(ConfigServerImpl config, Struct root) {
		return map(config, root, new ConcurrentHashMap<>());
	}

	public Map<String, T> map(ConfigServerImpl config, Struct root, Map<String, T> map) {
		if (type != TYPE_MAP) { // only happens when set wrong in code
			throw new RuntimeException("Invalid type [" + type + "]");
		}
		if (access != -1) {
			if (!ConfigUtil.hasAccess(config, access)) return map;
		}

		Struct data;

		if (parent == null) {
			data = root;
		}
		else {
			data = ConfigUtil.getAsStruct(parent, root);
		}

		try {
			// TODO
			if (customEnvVarSystemProps != null) {
				throw new RuntimeException("not supported yet");
			}

			data = ConfigUtil.getAsStruct(config, data, true, keys);
			Iterator<Entry<Key, Object>> it = data.entryIterator();
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
				map.put(key, factory.evaluate(config, key, e.getValue()));
			}
			return map;
		}
		catch (Exception ex) {
			if (logGlobal) LogUtil.logGlobal(config, "config-loading", ex);
			else ConfigFactoryImpl.log(config, ex);

			try {
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

	public List<T> list(ConfigServerImpl config, Struct root) {
		if (type != TYPE_LIST) { // only happens when set wrong in code
			throw new RuntimeException("Invalid type [" + type + "]");
		}
		List<T> list = new ArrayList<>();
		if (access != -1) {
			if (!ConfigUtil.hasAccess(config, access)) return list;
		}

		Struct data;

		if (parent == null) {
			data = root;
		}
		else {
			data = ConfigUtil.getAsStruct(parent, root);
		}

		try {
			String str;
			for (String key: envVarSystemProps()) {
				str = SystemUtil.getSystemPropOrEnvVar(key, null);
				if (!StringUtil.isEmpty(str, true)) {
					str = str.trim();
					T tmp;
					int index = 0;
					for (String val: ListUtil.listToStringArray(str.trim(), ',')) {
						if (val == null || (handleEmptyAsNull && StringUtil.isEmpty(val, true))) {
							continue;
						}
						tmp = factory.evaluate(config, "" + (++index), val);
						if (tmp != null) list.add(tmp);
					}
					return list;
				}
			}

			Array arr = ConfigUtil.getAsArray(config, data, true, keys);
			Iterator<Entry<Key, Object>> it = arr.entryIterator();
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
				list.add(factory.evaluate(config, key, val));
			}
			return list;
		}
		catch (Exception ex) {
			if (logGlobal) LogUtil.logGlobal(config, "config-loading", ex);
			else ConfigFactoryImpl.log(config, ex);
			try {
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
					if (val != null && (full || !val.equals(p.defaultValue))) {
						sct.set(name, field.get(cs));
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

	public static Query createSystemPropEnvVar() throws PageException {
		Key keySP = KeyImpl.init("systemProperties");
		Key keyEV = KeyImpl.init("environmentVariables");

		Query qry = new QueryImpl(new Key[] { KeyConstants._description, keySP, keyEV }, 0, "env vars");
		int row;
		for (Prop<?> p: instances) {
			if (p.hidden || p.type == TYPE_MAP || p.deprecated) continue;
			row = qry.addRow();

			qry.setAt(KeyConstants._description, row, p.description);
			String[] raw = p.envVarSystemProps();

			// system properties
			qry.setAt(keySP, row, new ArrayImpl(raw));

			// env var
			ArrayImpl arr = new ArrayImpl();
			for (String r: raw) {
				arr.add(SystemUtil.convertSystemPropToEnvVar(r));
			}
			qry.setAt(keyEV, row, new ArrayImpl(raw));
		}

		return qry;
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
			if (customEnvVarSystemProps != null) {
				for (String k: customEnvVarSystemProps) {
					set.add(k);
				}
			}

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
}