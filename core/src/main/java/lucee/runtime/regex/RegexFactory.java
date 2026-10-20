package lucee.runtime.regex;

import java.util.ArrayList;
import java.util.List;

import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigServerImpl;
import lucee.runtime.config.ConfigUtil;
import lucee.runtime.config.Prop;
import lucee.runtime.config.PropFactory;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

/**
 * Selects a regex engine by name. "java" (alias "modern") and "compat" are built in, every other
 * engine (for example "perl") is provided by an extension that declares it in its manifest with the
 * attribute "regex-engine".
 */
public final class RegexFactory implements PropFactory<Regex> {

	public static final String JAVA = "java";
	public static final String COMPAT = "compat";

	/**
	 * names of the engine that was part of Lucee before it moved to the Perl Regex extension, they are
	 * accepted even when the extension is not installed, so existing settings stay valid
	 */
	private static final String[] LEGACY_PERL_NAMES = new String[] { "perl", "perl5", "classic", "oro" };

	private static volatile RegexFactory instance;

	public static RegexFactory getInstance() {
		if (instance == null) {
			synchronized (RegexFactory.class) {
				if (instance == null) instance = new RegexFactory();
			}
		}
		return instance;
	}

	public static Regex getDefault() {
		return new JavaRegex();
	}

	private static Regex builtIn(String name) {
		if (JAVA.equals(name) || "modern".equals(name)) return new JavaRegex();
		if (COMPAT.equals(name)) return new JavaCompatRegex();
		return null;
	}

	private static boolean isLegacyPerl(String name) {
		for (String legacy: LEGACY_PERL_NAMES) {
			if (legacy.equals(name)) return true;
		}
		return false;
	}

	private static ConfigServerImpl toConfigServer(Config config) {
		if (config == null) config = ThreadLocalPageContext.getConfig();
		return config == null ? null : ConfigUtil.getConfigServerImpl(config);
	}

	/**
	 * @param config configuration used to find the engines provided by extensions
	 * @param name name of the engine
	 * @param defaultValue value returned if there is no engine with that name
	 * @return the engine, an engine provided by an extension is resolved when it is used
	 */
	public static Regex toRegex(Config config, String name, Regex defaultValue) {
		if (StringUtil.isEmpty(name, true)) return defaultValue;
		name = name.trim().toLowerCase();

		Regex regex = builtIn(name);
		if (regex != null) return regex;

		ConfigServerImpl cs = toConfigServer(config);
		if (isLegacyPerl(name) || (cs != null && cs.hasRegexEngine(name))) return new NamedRegex(config, name);
		return defaultValue;
	}

	public static Regex toRegex(Config config, String name) throws ApplicationException {
		Regex regex = toRegex(config, name, null);
		if (regex != null) return regex;

		throw new ApplicationException("invalid regex name [" + name + "], valid names are [" + availableNames(config) + "]");
	}

	/**
	 * the legacy setting useJavaAsRegexEngine
	 */
	public static Regex toRegex(Config config, boolean useJavaAsRegexEngine) {
		if (useJavaAsRegexEngine) return new JavaRegex();
		return new NamedRegex(config, "perl");
	}

	public static String availableNames(Config config) {
		List<String> names = new ArrayList<>();
		names.add(JAVA);
		names.add(COMPAT);
		ConfigServerImpl cs = toConfigServer(config);
		if (cs != null) {
			for (String name: cs.getRegexEngineNames()) {
				if (!names.contains(name)) names.add(name);
			}
		}
		return String.join(", ", names);
	}

	@Override
	public Regex evaluate(Config config, String name, Object val, short source) throws PageException {
		String strRegex = Caster.toString(val);
		if (StringUtil.isEmpty(strRegex, true)) {
			throw new ApplicationException("regex cannot be an empty string");
		}

		return toRegex(config, strRegex);
	}

	@Override
	public Object serialize(Config config, Regex val) throws PageException {
		return val.getTypeName();
	}

	@Override
	public Struct schema(Prop<Regex> prop) {
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._type, "string");

		sct.setEL(KeyConstants._description,
			"The regular expression engine to use. 'java' (modern) is the standard JVM engine and the default, 'compat' is java with Perl-style replacement syntax (backref \\N, case modifiers \\u \\l \\U \\L \\E). "
					+ "Other engines are provided by extensions and selected by the names they declare, for example 'perl' (alias 'perl5', 'classic', 'oro') by the Perl Regex extension (Apache ORO).");

		Array enums = new ArrayImpl();
		enums.appendEL("java");
		enums.appendEL("modern");
		enums.appendEL("compat");
		for (String legacy: LEGACY_PERL_NAMES) {
			enums.appendEL(legacy);
		}

		sct.setEL("enum", enums);

		return sct;
	}

	@Override
	public Object resolvedValue(Regex value) {
		return value;
	}
}
