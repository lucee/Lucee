package lucee.runtime.regex;

import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ApplicationException;

public final class RegexFactory {

	public static final int TYPE_PERL = 1;
	public static final int TYPE_JAVA = 2;
	public static final int TYPE_UNDEFINED = 0;

	public static String toType(int regexName, String defaultValue) {
		if (regexName == TYPE_JAVA) return "java";
		if (regexName == TYPE_PERL) return "perl";
		return defaultValue;
	}

	public static Regex toRegex(int regexName, Regex defaultValue) {
		if (regexName == TYPE_JAVA) return new JavaRegex();
		if (regexName == TYPE_PERL) return new Perl5Regex();
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
		return defaultValue;
	}

	public static int toType(String regexName) throws ApplicationException {
		int res = toType(regexName, -1);
		if (res != -1) return res;

		throw new ApplicationException("invalid regex name [" + regexName + "], valid names are [java or perl]");
	}
}
