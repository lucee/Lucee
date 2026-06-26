package lucee.runtime.regex;

import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.Prop;
import lucee.runtime.config.PropFactory;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public final class RegexFactory implements PropFactory<Regex> {

	public static final int TYPE_PERL = 1;
	public static final int TYPE_JAVA = 2;
	public static final int TYPE_COMPAT = 3;
	public static final int TYPE_UNDEFINED = 0;

	private static volatile RegexFactory instance;

	public static RegexFactory getInstance() {
		if (instance == null) {
			synchronized (RegexFactory.class) {
				if (instance == null) instance = new RegexFactory();
			}
		}
		return instance;
	}

	public static String toType(int regexName, String defaultValue) {
		if (regexName == TYPE_JAVA) return "java";
		if (regexName == TYPE_PERL) return "perl";
		if (regexName == TYPE_COMPAT) return "compat";
		return defaultValue;
	}

	public static Regex toRegex(int regexName, Regex defaultValue) {
		if (regexName == TYPE_JAVA) return new JavaRegex();
		if (regexName == TYPE_PERL) return new Perl5Regex();
		if (regexName == TYPE_COMPAT) return new JavaCompatRegex();
		return defaultValue;
	}

	public static Regex toRegex(boolean useJavaAsRegexEngine) {
		if (useJavaAsRegexEngine) return new JavaRegex();
		return new Perl5Regex();
	}

	public static int toType(String regexName, int defaultValue) {
		if (StringUtil.isEmpty(regexName, true)) return defaultValue;
		regexName = regexName.trim();

		if ("java".equalsIgnoreCase(regexName) || "modern".equalsIgnoreCase(regexName)) return TYPE_JAVA;
		else if ("perl".equalsIgnoreCase(regexName) || "perl5".equalsIgnoreCase(regexName) || "classic".equalsIgnoreCase(regexName)) return TYPE_PERL;
		else if ("compat".equalsIgnoreCase(regexName)) return TYPE_COMPAT;
		return defaultValue;
	}

	public static int toType(String regexName) throws ApplicationException {
		int res = toType(regexName, -1);
		if (res != -1) return res;

		throw new ApplicationException("invalid regex name [" + regexName + "], valid names are [java, perl, compat]");
	}

	@Override
	public Regex evaluate(Config config, String name, Object val, short source) throws PageException {
		String strRegex = Caster.toString(val);
		if (StringUtil.isEmpty(strRegex, true)) {
			throw new ApplicationException("regex cannot be an empty string");
		}

		int type = toType(strRegex);

		return toRegex(type, null);
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
			"The regular expression engine to use. 'java' (modern) is the standard JVM engine, 'perl' (classic) is the Apache ORO engine, 'compat' is java with Perl-style replacement syntax (backref \\N, case modifiers \\u \\l \\U \\L \\E).");

		// Define the aliases supported by the toType(String) logic
		Array enums = new ArrayImpl();
		enums.appendEL("java");
		enums.appendEL("modern");
		enums.appendEL("perl");
		enums.appendEL("perl5");
		enums.appendEL("classic");
		enums.appendEL("compat");

		sct.setEL("enum", enums);

		return sct;
	}

	@Override
	public Object resolvedValue(Regex value) {
		return value;
	}
}
