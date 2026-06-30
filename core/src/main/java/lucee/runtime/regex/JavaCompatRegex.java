package lucee.runtime.regex;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

class JavaCompatRegex extends JavaRegex {

	private static final Map<String, Pattern> compatCache = new ConcurrentHashMap<>();

	@Override
	protected Pattern toPattern(String strPattern, boolean caseSensitive, boolean multiLine) {
		int f = 0;
		if (!caseSensitive) f += Pattern.CASE_INSENSITIVE;
		if (multiLine) f += Pattern.MULTILINE;
		final int flags = f;
		String translated = translatePattern(strPattern);
		String key = flags + "\0" + translated;
		return compatCache.computeIfAbsent(key, k -> Pattern.compile(translated, flags));
	}

	// Translates a CFML/Perl pattern string to java.util.regex syntax.
	// Currently a passthrough — POSIX class mapping etc. added here later.
	private static String translatePattern(String pattern) {
		return pattern;
	}

	@Override
	public String replace(String strInput, String strPattern, String replacement, boolean caseSensitive, boolean multiLine) throws PageException {
		try {
			Matcher m = toPattern(strPattern, caseSensitive, multiLine).matcher(strInput);
			StringBuilder sb = new StringBuilder();
			if (m.find()) {
				m.appendReplacement(sb, Matcher.quoteReplacement(expandReplacement(m, replacement)));
			}
			m.appendTail(sb);
			return sb.toString();
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public String replaceAll(String strInput, String strPattern, String replacement, boolean caseSensitive, boolean multiLine) throws PageException {
		try {
			Matcher m = toPattern(strPattern, caseSensitive, multiLine).matcher(strInput);
			StringBuilder sb = new StringBuilder();
			while (m.find()) {
				m.appendReplacement(sb, Matcher.quoteReplacement(expandReplacement(m, replacement)));
			}
			m.appendTail(sb);
			return sb.toString();
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	// Expands a Perl-style replacement string against the current match.
	// Handles backslash-N backrefs and case modifiers (u l U L E).
	// Literal $ is preserved as-is; non-special backslash sequences pass through.
	static String expandReplacement(Matcher m, String repl) {
		if (repl == null) return "";
		if (repl.indexOf('\\') < 0) return repl;

		StringBuilder out = new StringBuilder(repl.length() + 16);
		// 0=none  1=next_upper  2=next_lower  3=all_upper  4=all_lower
		int mode = 0;
		int i = 0, n = repl.length();

		while (i < n) {
			char c = repl.charAt(i);
			if (c == '$') {
				out.append('$');
				if (mode == 1 || mode == 2) mode = 0;
				i++;
			}
			else if (c == '\\' && i + 1 < n) {
				char next = repl.charAt(i + 1);
				if (next == 'u') { mode = 1; i += 2; }
				else if (next == 'l') { mode = 2; i += 2; }
				else if (next == 'U') { mode = 3; i += 2; }
				else if (next == 'L') { mode = 4; i += 2; }
				else if (next == 'E') { mode = 0; i += 2; }
				else if (next == 'k') {
					if (i + 2 >= n || repl.charAt(i + 2) != '<')
						throw new IllegalArgumentException("invalid \\k in replacement — expected \\k<name>");
					int close = repl.indexOf('>', i + 3);
					if (close < 0)
						throw new IllegalArgumentException("unclosed \\k<name> in replacement — missing '>'");
					String name = repl.substring(i + 3, close);
					String group = m.group(name);
					if (group == null) group = "";
					out.append(applyGroupCase(group, mode));
					if (mode == 1 || mode == 2) mode = 0;
					i = close + 1;
				}
				else if (next >= '0' && next <= '9') {
					String group = m.group(next - '0');
					if (group == null) group = "";
					out.append(applyGroupCase(group, mode));
					if (mode == 1 || mode == 2) mode = 0;
					i += 2;
				}
				else {
					// non-special backslash sequence — keep backslash, apply case to following char
					out.append('\\');
					out.append(applyCharCase(next, mode));
					if (mode == 1 || mode == 2) mode = 0;
					i += 2;
				}
			}
			else {
				out.append(applyCharCase(c, mode));
				if (mode == 1 || mode == 2) mode = 0;
				i++;
			}
		}
		return out.toString();
	}

	private static char applyCharCase(char c, int mode) {
		if (mode == 1 || mode == 3) return Character.toUpperCase(c);
		if (mode == 2 || mode == 4) return Character.toLowerCase(c);
		return c;
	}

	private static String applyGroupCase(String group, int mode) {
		if (group.isEmpty()) return group;
		switch (mode) {
			case 3: {
				StringBuilder sb = new StringBuilder(group.length());
				for (int i = 0; i < group.length(); i++) sb.append(Character.toUpperCase(group.charAt(i)));
				return sb.toString();
			}
			case 4: {
				StringBuilder sb = new StringBuilder(group.length());
				for (int i = 0; i < group.length(); i++) sb.append(Character.toLowerCase(group.charAt(i)));
				return sb.toString();
			}
			case 1: return Character.toUpperCase(group.charAt(0)) + group.substring(1);
			case 2: return Character.toLowerCase(group.charAt(0)) + group.substring(1);
			default: return group;
		}
	}

	@Override
	public String getTypeName() {
		return "compat";
	}
}
