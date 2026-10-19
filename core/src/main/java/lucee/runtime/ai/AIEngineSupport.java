package lucee.runtime.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;

public abstract class AIEngineSupport implements AIEngine {

	public static final String DEFAULT_USERAGENT = "Lucee (AI Request)";
	private String id;
	private ClassDefinition<? extends AIEngine> cd;
	private Struct properties;
	private String name;
	private String _default;

	public static final Key MODEL_PREFERENCE = KeyImpl.init("modelPreference");
	private List<String> modelPreference;
	private boolean autoModel;
	private Config config;
	/** models that rejected the setting temperature, see {@link #isTemperatureRejected(String)} */
	private final Set<String> noTemperature = ConcurrentHashMap.newKeySet();

	@Override
	public AIEngine init(ClassDefinition<? extends AIEngine> cd, Struct properties, String name, String _default, String id) throws PageException {
		this.cd = cd;
		this.properties = properties;
		this.name = name;
		this._default = _default;
		this.id = id;
		return this;
	}

	@Override
	public String getDefault() {
		return _default;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ClassDefinition<? extends AIEngine> getClassDefinition() {
		return cd;
	}

	@Override
	public Struct getProperties() {
		return properties;
	}

	public final AISession createSession(String inialMessage) throws PageException {
		return createSession(inialMessage, null, -1, -1D, -1, -1);
	}

	public final AISession createSession(String inialMessage, Conversation[] history) throws PageException {
		return createSession(inialMessage, history, -1, -1D, -1, -1);
	}

	@Override
	public String getId() {
		return id;
	}

	public static void log(Exception e) {
		LogUtil.log("ai", "ai", e);
	}

	@Override
	public List<AIModel> getModels(List<AIModel> defaultValue) {
		try {
			return getModels();
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	protected static Map<String, String> toHeaders(Struct struct) throws PageException {
		if (struct == null || struct.isEmpty()) {
			return new HashMap<>();
		}
		Map<String, String> map = new HashMap<>();
		Iterator<Entry<Key, Object>> it = struct.entryIterator();
		Entry<Key, Object> e;
		while (it.hasNext()) {
			e = it.next();
			map.put(e.getKey().getString(), Caster.toString(e.getValue()));
		}

		return map;
	}

	/**
	 * reads the model and the model preference from the given properties and removes both from it. Returns the configured model or
	 * null if the model is resolved automatically ("auto", or empty when emptyIsAuto is true).
	 */
	protected final String readModel(Struct props, boolean emptyIsAuto) throws PageException {
		modelPreference = AIModelResolver.toPatterns(props.remove(MODEL_PREFERENCE, null));
		String model = Caster.toStringTrim(props.remove(lucee.runtime.type.util.KeyConstants._model, null), null);
		if (AIModelResolver.isAuto(model) || (emptyIsAuto && StringUtil.isEmpty(model, true))) {
			autoModel = true;
			return null;
		}
		autoModel = false;
		return StringUtil.isEmpty(model, true) ? null : model.trim();
	}

	/**
	 * true if the model is resolved automatically (model "auto")
	 */
	public final boolean isAutoModel() {
		return autoModel;
	}

	public final List<String> getModelPreference() {
		return modelPreference;
	}

	/**
	 * returns the model when it is configured, otherwise it resolves it (see {@link AIModelResolver}), if that fails, "auto" is
	 * returned and the error is logged
	 */
	protected final String getModel(String configured) {
		if (!autoModel) return configured;
		try {
			return AIModelResolver.resolve(this);
		}
		catch (Exception e) {
			log(e);
			return AIModelResolver.AUTO;
		}
	}

	/**
	 * resolves the model when it is set to "auto", throws an exception if that is not possible
	 */
	protected final void resolveModel() throws PageException {
		if (autoModel) AIModelResolver.resolve(this);
	}

	/**
	 * the provider's own "auto" model (a router that picks the model on the provider side), null if the provider has none
	 */
	public String getProviderAutoModel() {
		return null;
	}

	/**
	 * built-in fallback patterns used for "auto" when no model preference matches
	 */
	public List<String> getFallbackModels() {
		return new ArrayList<>();
	}

	/**
	 * names of the models available for chat, used to resolve "auto"
	 */
	public List<String> listModelNames() throws PageException {
		List<String> names = new ArrayList<>();
		for (AIModel m: getModels()) {
			names.add(m.getName());
		}
		return names;
	}

	/**
	 * the config the engine was created with, used for logging, can be null
	 */
	public final Config getConfig() {
		return config;
	}

	/**
	 * set by the {@link AIEngineFactory} when the engine is created
	 */
	public final void setConfig(Config config) {
		this.config = config;
	}

	/**
	 * false if the given model rejected the setting temperature before
	 */
	public final boolean acceptsTemperature(String model) {
		return model == null || !noTemperature.contains(model.toLowerCase());
	}

	/**
	 * Some models reject the setting temperature (e.g. Claude: "temperature is deprecated for this model", OpenAI reasoning models:
	 * "Unsupported value: 'temperature' does not support ..."). When a request fails with such an error, the session retries the
	 * request once without temperature and calls this method, so following requests for that model are sent without temperature.
	 */
	public final void rejectTemperature(String model) {
		if (model == null) return;
		if (noTemperature.add(model.toLowerCase())) {
			LogUtil.logx(config, Log.LEVEL_INFO, "ai", "AI connection [" + getName() + "] (" + getLabel() + "): the model [" + model
					+ "] does not accept the setting [temperature], it is no longer sent for this model", "ai", "application");
		}
	}

	/**
	 * true if the given error message from the provider says that the setting temperature is not supported by the model
	 */
	public static boolean isTemperatureRejected(String msg) {
		if (msg == null) return false;
		String m = msg.toLowerCase();
		if (m.indexOf("temperature") == -1) return false;
		return m.indexOf("deprecated") != -1 || m.indexOf("unsupported") != -1 || m.indexOf("not supported") != -1 || m.indexOf("does not support") != -1
				|| m.indexOf("not allowed") != -1 || m.indexOf("only the default") != -1;
	}
}
