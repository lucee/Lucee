package lucee.runtime.regex;

import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigServerImpl;
import lucee.runtime.config.ConfigUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;

/**
 * A regex engine that is provided by an extension and selected by name (for example "perl"). The
 * engine is resolved at every use, so a configuration or application that asks for an engine whose
 * extension is not (yet) installed keeps working for everything else and only fails, with a clear
 * message, when the engine is used.
 */
final class NamedRegex implements Regex {

	private final Config config;
	private final String name;

	NamedRegex(Config config, String name) {
		this.config = config;
		this.name = name.trim().toLowerCase();
	}

	private Regex resolve() throws PageException {
		Regex regex = lookup();
		if (regex != null) return regex;

		String engine = RegexFactory.toCanonicalLegacyName(name);
		throw new ApplicationException("The regex engine [" + engine + "] is not available.",
				"The engine [" + engine + "] is provided by an extension (for example the Perl Regex extension for [perl]), install it, or select one of the built-in engines ["
						+ RegexFactory.JAVA + ", " + RegexFactory.COMPAT + "] with the setting regex type (this.regex.type in Application.cfc or the regexType setting of the server). "
						+ "Available engines: " + RegexFactory.availableNames(config) + ".");
	}

	private Regex lookup() {
		Config cfg = config != null ? config : lucee.runtime.engine.ThreadLocalPageContext.getConfig();
		if (cfg == null) return null;
		ConfigServerImpl cs = ConfigUtil.getConfigServerImpl(cfg);
		return cs == null ? null : cs.getRegexEngine(name);
	}

	@Override
	public boolean matches(String strPattern, String strInput) throws PageException {
		return resolve().matches(strPattern, strInput);
	}

	@Override
	public boolean matches(String strPattern, String strInput, boolean defaultValue) {
		Regex regex = lookup();
		return regex == null ? defaultValue : regex.matches(strPattern, strInput, defaultValue);
	}

	@Override
	public String match(String strPattern, String strInput, int offset, boolean caseSensitive, boolean multiLine) throws PageException {
		return resolve().match(strPattern, strInput, offset, caseSensitive, multiLine);
	}

	@Override
	public Array matchAll(String strPattern, String strInput, int offset, boolean caseSensitive, boolean multiLine) throws PageException {
		return resolve().matchAll(strPattern, strInput, offset, caseSensitive, multiLine);
	}

	@Override
	public int indexOf(String strPattern, String strInput, int offset, boolean caseSensitive, boolean multiLine) throws PageException {
		return resolve().indexOf(strPattern, strInput, offset, caseSensitive, multiLine);
	}

	@Override
	public Object indexOfAll(String strPattern, String strInput, int offset, boolean caseSensitive, boolean multiLine) throws PageException {
		return resolve().indexOfAll(strPattern, strInput, offset, caseSensitive, multiLine);
	}

	@Override
	public Struct find(String strPattern, String strInput, int offset, boolean caseSensitive, boolean multiLine) throws PageException {
		return resolve().find(strPattern, strInput, offset, caseSensitive, multiLine);
	}

	@Override
	public Array findAll(String strPattern, String strInput, int offset, boolean caseSensitive, boolean multiLine) throws PageException {
		return resolve().findAll(strPattern, strInput, offset, caseSensitive, multiLine);
	}

	@Override
	public String replace(String strInput, String strPattern, String replacement, boolean caseSensitive, boolean multiLine) throws PageException {
		return resolve().replace(strInput, strPattern, replacement, caseSensitive, multiLine);
	}

	@Override
	public String replaceAll(String strInput, String strPattern, String replacement, boolean caseSensitive, boolean multiLine) throws PageException {
		return resolve().replaceAll(strInput, strPattern, replacement, caseSensitive, multiLine);
	}

	@Override
	public String escape(String strInput) throws PageException {
		return resolve().escape(strInput);
	}

	boolean isAvailable() {
		return lookup() != null;
	}

	/**
	 * the canonical name of the engine, an alias like perl5, oro or classic is reported as the engine
	 * itself reports it (perl), so code that compares with the canonical name keeps working
	 */
	@Override
	public String getTypeName() {
		Regex regex = lookup();
		if (regex != null) return regex.getTypeName();
		return RegexFactory.toCanonicalLegacyName(name);
	}
}
