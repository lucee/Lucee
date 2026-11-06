package lucee.runtime.ai;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;


import lucee.commons.digest.HashUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigFactoryImpl;
import lucee.runtime.config.Prop;
import lucee.runtime.config.PropFactory;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public final class AIEngineFactory implements PropFactory<AIEngine> {

	// keys that belong to the connection itself, every other key of a flat connection struct is an engine property
	private static final Key[] CONNECTION_KEYS = new Key[] { KeyConstants._class, KeyConstants._classname, KeyImpl.init("class-name"), KeyConstants._bundleName,
			KeyConstants._bundleVersion, KeyConstants._maven, KeyConstants._component, KeyConstants._default, KeyConstants._custom, KeyConstants._properties,
			KeyConstants._arguments };
	private static final Key[] CLASS_KEYS = new Key[] { KeyConstants._class, KeyConstants._classname, KeyImpl.init("class-name"), KeyConstants._maven, KeyConstants._component };

	private static final Map<String, SoftReference<AIEngine>> instances = new ConcurrentHashMap<>();
	private static AIEngineFactory instance;

	public static String createId(ClassDefinition<? extends AIEngine> cd, Struct properties, String name, String _default) {
		Key[] keys = properties.keys();
		Arrays.sort(keys, new Comparator<Key>() {
			@Override
			public int compare(Key k1, Key k2) {
				return k1.getUpperString().compareTo(k2.getUpperString());
			}
		});

		StringBuilder sb = new StringBuilder()

				.append(name).append(';')

				.append(_default).append(';')

				.append(cd.toString()).append(';');

		for (Key k: keys) {
			sb.append(k).append(':').append(properties.get(k, "")).append(';');
		}
		return HashUtil.create64BitHashAsString(sb.toString());
	}

	public static AIEngineFactory getInstance() {
		if (instance == null) {
			instance = new AIEngineFactory();
		}
		return instance;
	}

	public static AIEngine getInstance(Config config, String name, Struct data) throws PageException, ClassException, IOException {

		ClassDefinition<AIEngine> cd;

		cd = ConfigFactoryImpl.getClassDefinition(config, data, "", config.getIdentification());
		if (cd.hasClass()) {

			Struct custom = Caster.toStruct(data.get(KeyConstants._custom, null), null);
			if (custom == null) custom = Caster.toStruct(data.get(KeyConstants._properties, null), null);
			if (custom == null) custom = Caster.toStruct(data.get(KeyConstants._arguments, null), null);
			String _default = Caster.toString(data.get(KeyConstants._default, null), null);
			return getInstance(config, cd, custom, name, _default);
		}
		throw new ApplicationException("class defintion is invalid");
	}

	private static AIEngine getInstance(Config config, ClassDefinition<? extends AIEngine> cd, Struct properties, String name, String _default)
			throws PageException, ClassException, IOException {
		if (properties == null) properties = new StructImpl();
		String id = createId(cd, properties, name, _default);
		AIEngine aie = getExistingInstance(id, null);
		if (aie != null) return aie;

		aie = (AIEngine) ClassUtil.loadInstance(cd.getClazz());
		LogUtil.logx(config, Log.LEVEL_TRACE, "ai-factory", "create AI instance [" + cd.toString() + "]", "ai", "application");
		if (aie instanceof AIEngineSupport) ((AIEngineSupport) aie).setConfig(config);
		aie.init(cd, properties, name, _default, id);
		instances.put(id, new SoftReference<AIEngine>(aie));
		return aie;
	}

	/**
	 * converts a connection struct passed in place of an AI connection name to the structure of a
	 * connection defined in the Application.cfc or in the configuration. A struct that is already in
	 * that form (keys like class, custom, default) is taken as it is, otherwise all keys that do not
	 * belong to the connection itself are handled as properties of the engine. When no class is
	 * defined, it is derived from the property type (for example openai, other (an OpenAI compatible endpoint defined with `url`), claude or gemini).
	 * 
	 * @param data the connection struct as passed by the caller, is not modified
	 * @return a new struct, that can be passed to getInstance
	 */
	public static Struct toConnection(Struct data) throws PageException {
		boolean hasProps = false;
		for (Key k: new Key[] { KeyConstants._custom, KeyConstants._properties, KeyConstants._arguments }) {
			if (data.get(k, null) != null) {
				hasProps = true;
				break;
			}
		}

		Struct connection = new StructImpl(Struct.TYPE_LINKED);
		Struct flat = new StructImpl(Struct.TYPE_LINKED);
		Iterator<Entry<Key, Object>> it = data.entryIterator();
		Entry<Key, Object> e;
		while (it.hasNext()) {
			e = it.next();
			if (isConnectionKey(e.getKey())) connection.setEL(e.getKey(), e.getValue());
			else if (!hasProps) flat.setEL(e.getKey(), e.getValue());
		}
		if (!hasProps) connection.setEL(KeyConstants._custom, flat);

		boolean hasClass = false;
		for (Key k: CLASS_KEYS) {
			if (!StringUtil.isEmpty(Caster.toString(connection.get(k, null), null), true)) {
				hasClass = true;
				break;
			}
		}
		if (!hasClass && StringUtil.isEmpty(Caster.toString(connection.get(KeyConstants._bundleName, null), null), true)) {
			Struct props = Caster.toStruct(connection.get(KeyConstants._custom, null), null);
			if (props == null) props = Caster.toStruct(connection.get(KeyConstants._properties, null), null);
			if (props == null) props = Caster.toStruct(connection.get(KeyConstants._arguments, null), null);
			String cn = props == null ? null : classNameForType(Caster.toString(props.get(KeyConstants._type, null), null));
			if (cn == null) {
				throw new ApplicationException(
						"the AI connection struct needs to define the engine, either with the key [class] (or [component]) or with the property [type] set to one of [openai, chatgpt, ollama, perplexity, deepseek, grok, other, claude, gemini]");
			}
			connection.setEL(KeyConstants._class, cn);
		}
		return connection;
	}

	private static boolean isConnectionKey(Key key) {
		for (Key k: CONNECTION_KEYS) {
			if (k.equals(key)) return true;
		}
		return false;
	}

	private static String classNameForType(String type) {
		if (StringUtil.isEmpty(type, true)) return null;
		type = type.trim().toLowerCase();
		if ("openai".equals(type) || "chatgpt".equals(type) || "ollama".equals(type) || "perplexity".equals(type) || "deepseek".equals(type) || "grok".equals(type)
				|| "other".equals(type)) {
			return "lucee.runtime.ai.openai.OpenAIEngine";
		}
		if ("claude".equals(type)) return "lucee.runtime.ai.anthropic.ClaudeEngine";
		if ("gemini".equals(type)) return "lucee.runtime.ai.google.GeminiEngine";
		return null;
	}

	public static AIEngine getExistingInstance(String id, AIEngine defaultValue) {
		AIEngine aie;
		SoftReference<AIEngine> ref = instances.get(id);
		if (ref != null) {
			aie = ref.get();
			if (aie != null) return aie;
		}

		return defaultValue;
	}

	@Override
	public AIEngine evaluate(Config config, String name, Object val, short source) throws PageException {
		try {
			return getInstance(config, name, Caster.toStruct(val));
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public Struct schema(Prop<AIEngine> prop) {
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._type, "object");
		sct.setEL(KeyConstants._description, "Defines a Lucee AI Engine connection.");

		Struct properties = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._properties, properties);

		// 1. ClassDefinition (No prefix used here as keys are "class", "bundleName", etc.)
		PropFactory.appendClassDefinitionProps(properties, "");

		// 2. custom (The dynamic configuration block)
		Struct custom = new StructImpl(Struct.TYPE_LINKED);
		custom.setEL(KeyConstants._type, "object");
		custom.setEL(KeyConstants._description, "Engine-specific parameters (e.g. model, temperature, apiKey).");
		custom.setEL(KeyImpl.init("additionalProperties"), true);

		Struct customProps = new StructImpl(Struct.TYPE_LINKED);
		customProps.setEL(AITokenLimits.MAX_TOKENS_PER_SESSION,
				PropFactory.createSimple("integer", "Maximum number of tokens a session may use. Further requests are refused (or logged) once it is reached. 0 or empty means no limit."));
		customProps.setEL(AITokenLimits.MAX_TOKENS_PER_REQUEST,
				PropFactory.createSimple("integer", "Maximum number of tokens of a single request. Limits the output of the provider and refuses (or logs) requests that are already as big as the limit. 0 or empty means no limit."));
		customProps.setEL(AITokenLimits.TOKEN_LIMIT_ACTION, PropFactory.createSimple("string", "What to do when a limit is reached: throw (default) or log."));
		customProps.setEL(AITokenLimits.TOKEN_WARNING_THRESHOLD,
				PropFactory.createSimple("number", "Fraction (0-1) of maxTokensPerSession at which a warning is written to the ai log, once per session."));
		custom.setEL(KeyConstants._properties, customProps);

		properties.setEL(KeyConstants._custom, custom);

		// 3. default
		properties.setEL(KeyConstants._default, PropFactory.createSimple("string", "Optional label for this engine instance."));

		// Required
		Array required = new ArrayImpl();
		required.appendEL("class");
		sct.setEL(KeyImpl.init("required"), required);

		return sct;
	}

	@Override
	public Object resolvedValue(AIEngine defaultValue) {
		return defaultValue;
	}
}
