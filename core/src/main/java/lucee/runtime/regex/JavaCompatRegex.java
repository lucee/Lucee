package lucee.runtime.regex;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

class JavaCompatRegex extends JavaRegex {

	private static final Map<String, Pattern> compatCache = new ConcurrentHashMap<>();

	// Oro's POSIX classes use the Character.isXxx() methods, these are the exact java equivalents
	private static final String PUNCT = "\\p{Pc}\\p{Pd}\\p{Ps}\\p{Pe}\\p{Po}\\p{Sm}\\p{Sc}\\p{Sk}";
	private static final String WORD = "\\p{L}\\p{Nd}_";
	private static final Map<String, String> POSIX = Map.ofEntries(Map.entry("alpha", "\\p{L}"), Map.entry("digit", "\\p{Nd}"), Map.entry("alnum", "\\p{L}\\p{Nd}"),
			Map.entry("word", WORD), Map.entry("upper", "\\p{IsUppercase}"), Map.entry("lower", "\\p{IsLowercase}"), Map.entry("space", "\\p{javaWhitespace}"),
			Map.entry("blank", "\\p{javaSpaceChar}"), Map.entry("punct", PUNCT), Map.entry("graph", "\\p{L}\\p{Nd}" + PUNCT), Map.entry("print", "\\p{L}\\p{Nd}" + PUNCT + "\\p{Z}"),
			Map.entry("cntrl", "\\p{Cc}"), Map.entry("xdigit", "\\p{XDigit}"));

	// Oro's \w \d \s and \b use the same Character.isXxx() methods, java's are ASCII.
	// UNICODE_CHARACTER_CLASS isn't a match, its \w also takes combining marks and its \s takes nbsp
	private static final String W = "[" + WORD + "]";
	private static final Map<Character, String> SHORTHAND = Map.of('w', W, 'W', "[^" + WORD + "]", 'd', "\\p{Nd}", 'D', "\\P{Nd}", 's', "\\p{javaWhitespace}", 'S',
			"\\P{javaWhitespace}", 'b', "(?:(?<=" + W + ")(?!" + W + ")|(?<!" + W + ")(?=" + W + "))", 'B', "(?:(?<=" + W + ")(?=" + W + ")|(?<!" + W + ")(?!" + W + "))");

	// the whitespace Oro's (?x) skips inside a run of literals, between atoms it skips any Character.isWhitespace()
	private static final String RUN_SPACE = " \t\n\r\f\u000B";
	// the inline modifiers Oro accepts in (?...)
	private static final String ORO_MODIFIERS = "iogmsx-";

	// letters java.util.regex accepts after a backslash, Oro reads any other letter as itself
	private static final String JAVA_ESCAPE_LETTERS = "tnrfaecdDhHsSvVwWpPbBAGZzQERXNkxu";

	// Oro compiled non-multiline patterns with SINGLELINE_MASK, so . matches \n, and only \n ends a line
	@Override
	protected Pattern toPattern(String strPattern, boolean caseSensitive, boolean multiLine) {
		int f = Pattern.UNIX_LINES;
		if (!caseSensitive) f += Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE;
		f += multiLine ? Pattern.MULTILINE : Pattern.DOTALL;
		final int flags = f;
		String key = flags + "\0" + strPattern;
		return compatCache.computeIfAbsent(key, k -> compile(strPattern, flags));
	}

	// a syntax error names the pattern as written, the index into the translation would be misleading
	private static Pattern compile(String strPattern, int flags) {
		String translated = translatePattern(strPattern);
		try {
			return Pattern.compile(translated, flags);
		}
		catch (PatternSyntaxException e) {
			if (translated.equals(strPattern)) throw e;
			PatternSyntaxException pse = new PatternSyntaxException(e.getDescription(), strPattern, -1);
			pse.initCause(e);
			throw pse;
		}
	}

	// Perl doesn't report an empty match where the previous match ended
	@Override
	protected boolean findNext(Matcher matcher, int prevEnd) {
		while (matcher.find()) {
			if (matcher.start() != matcher.end() || matcher.start() != prevEnd) return true;
		}
		return false;
	}

	// Translates a CFML/Perl pattern string to java.util.regex syntax.
	// Oro reads these as literals where java throws or reads them differently: a { that doesn't start a quantifier,
	// a [ or && inside a class, a letter escape java doesn't know, \xH and \0. POSIX [:name:] becomes \p{...},
	// and \w \W \d \D \s \S \b \B become their Unicode equivalents.
	// Oro's (?x) is handled here and dropped from the translation, see the comments below.
	static String translatePattern(String pattern) {
		if (pattern.indexOf('{') < 0 && pattern.indexOf('[') < 0 && pattern.indexOf('\\') < 0 && pattern.indexOf("(?") < 0) return pattern;
		int n = pattern.length();
		StringBuilder sb = new StringBuilder(n + 16);
		boolean inClass = false;
		// Oro's (?x) is on from where it appears to the end of the pattern, or a (?-x), not to the end of the group like java's
		boolean extended = false;
		// where the current run of literals starts in sb, and whether (?x) skipped whitespace since its last char:
		// Oro checks for a quantifier before it skips, so a quantifier after the whitespace is one more literal
		int runStart = -1;
		boolean skipped = false;
		// sb length right after a plain word char literal, so a \b straight after it knows one side
		int wordEnd = -1;
		for (int i = 0; i < n; i++) {
			char c = pattern.charAt(i);
			int len = sb.length();
			if (extended && !inClass && (c == '#' || (runStart >= 0 ? RUN_SPACE.indexOf(c) >= 0 : Character.isWhitespace(c)))) {
				if (c == '#') while (i + 1 < n && pattern.charAt(i + 1) != '\n') i++;
				if (runStart >= 0) skipped = true;
				continue;
			}
			if (skipped && "*+?{".indexOf(c) >= 0) {
				// unless another quantifier follows, which backs it off and then repeats the whole run
				if (c != '{' && i + 1 < n && isOroQuantifier(pattern, i + 1)) {
					sb.insert(runStart, "(?:").append(')').append(c);
					runStart = -1;
				}
				else sb.append('\\').append(c);
				skipped = false;
				continue;
			}
			skipped = false;
			int modEnd = !inClass && c == '(' ? modifiersEnd(pattern, i) : -1;
			if (modEnd > 0) {
				extended = translateModifiers(pattern, i, modEnd, extended, sb);
				runStart = -1;
				i = modEnd;
			}
			else if (c == '\\' && !inClass && !extended && i + 1 < n && (pattern.charAt(i + 1) == 'b' || pattern.charAt(i + 1) == 'B')) {
				int j = translateBoundary(pattern, i, sb.length() == wordEnd, sb);
				// it moved the following literal in front of the check
				if (j == i + 2) wordEnd = sb.length();
				i = j;
			}
			else if (c == '\\') {
				// Oro ends a run of literals at these, any other escape is one more literal
				boolean ends = i + 1 < n && ("AGZwWbBsSdD".indexOf(pattern.charAt(i + 1)) >= 0 || (pattern.charAt(i + 1) >= '1' && pattern.charAt(i + 1) <= '9'));
				i = translateEscape(pattern, i, inClass, sb);
				if (!inClass) runStart = ends ? -1 : runStart < 0 ? len : runStart;
			}
			else if (inClass) {
				if (c == ']') {
					inClass = false;
					sb.append(c);
				}
				else if (c == '[') {
					int end = pattern.startsWith("[:", i) ? pattern.indexOf(":]", i + 2) : -1;
					String posix = end < 0 ? null : POSIX.get(pattern.substring(i + 2, end));
					if (posix == null) sb.append("\\[");
					else {
						sb.append(posix);
						i = end + 1;
					}
				}
				else if (c == '&' && i + 1 < n && pattern.charAt(i + 1) == '&') {
					sb.append("\\&\\&");
					i++;
				}
				else sb.append(c);
			}
			else if (c == '[') {
				inClass = true;
				runStart = -1;
				sb.append(c);
				if (i + 1 < n && pattern.charAt(i + 1) == '^') sb.append(pattern.charAt(++i));
				// a ] straight after [ or [^ is a literal
				if (i + 1 < n && pattern.charAt(i + 1) == ']') {
					sb.append("\\]");
					i++;
				}
			}
			else if (c == '{') {
				// a quantifier at the very start has nothing to repeat, Oro reads it as a literal
				int end = i == 0 ? -1 : quantifierEnd(pattern, i);
				if (end < 0) {
					sb.append("\\{");
					if (runStart < 0) runStart = len;
				}
				else {
					sb.append(pattern, i, end + 1);
					runStart = -1;
					i = end;
				}
			}
			else {
				sb.append(c);
				if (isWordChar(c)) wordEnd = sb.length();
				runStart = "^$.()|*+?".indexOf(c) >= 0 ? -1 : runStart < 0 ? len : runStart;
			}
		}
		return sb.toString();
	}

	// index of the ) closing an Oro inline modifier group like (?ix) that starts at i, -1 if it isn't one
	private static int modifiersEnd(String pattern, int i) {
		int n = pattern.length(), j = i + 2;
		if (j > n || pattern.charAt(i + 1) != '?') return -1;
		while (j < n && ORO_MODIFIERS.indexOf(pattern.charAt(j)) >= 0) j++;
		return j < n && pattern.charAt(j) == ')' ? j : -1;
	}

	// copies the modifier group without x, returns whether (?x) is on after it
	private static boolean translateModifiers(String pattern, int i, int end, boolean extended, StringBuilder sb) {
		String flags = pattern.substring(i + 2, end);
		int minus = flags.indexOf('-');
		int x = flags.indexOf('x');
		if (x < 0) {
			sb.append(pattern, i, end + 1);
			return extended;
		}
		// Oro sets the flags before the - and then clears the ones after it
		boolean on = minus < 0 || (x < minus && flags.indexOf('x', minus) < 0);
		String rest = flags.replace("x", "");
		if (!rest.isEmpty() && !rest.equals("-")) sb.append("(?").append(rest.endsWith("-") ? rest.substring(0, rest.length() - 1) : rest).append(')');
		return on;
	}

	// Oro's test for a quantifier when it decides where a run of literals ends
	private static boolean isOroQuantifier(String pattern, int j) {
		char c = pattern.charAt(j);
		if (c == '*' || c == '+' || c == '?') return true;
		if (c != '{') return false;
		int n = pattern.length(), k = j + 1;
		if (k >= n || !Character.isDigit(pattern.charAt(k))) return false;
		while (k < n && Character.isDigit(pattern.charAt(k))) k++;
		if (k < n && pattern.charAt(k) == ',') k++;
		while (k < n && Character.isDigit(pattern.charAt(k))) k++;
		return k < n && pattern.charAt(k) == '}';
	}

	private static boolean isWordChar(char c) {
		return Character.isLetterOrDigit(c) || c == '_';
	}

	// \b and \B with a literal word char on one side only need to test the other side.
	// java tries a pattern's first node at every position, so a leading lookbehind is tried on every char:
	// \bund becomes u(?<!Wu)nd, which only looks behind where the u matched
	private static int translateBoundary(String pattern, int i, boolean afterWord, StringBuilder sb) {
		boolean b = pattern.charAt(i + 1) == 'b';
		if (afterWord) {
			sb.append(b ? "(?!" : "(?=").append(W).append(')');
			return i + 1;
		}
		int n = pattern.length();
		if (i + 2 < n && isWordChar(pattern.charAt(i + 2)) && (i + 3 == n || "?*+{".indexOf(pattern.charAt(i + 3)) < 0)) {
			char c = pattern.charAt(i + 2);
			sb.append(c).append(b ? "(?<!" : "(?<=").append(W).append(c).append(')');
			return i + 2;
		}
		sb.append(SHORTHAND.get(pattern.charAt(i + 1)));
		return i + 1;
	}

	// appends the escape starting at the backslash at index i, returns the index of its last char
	private static int translateEscape(String pattern, int i, boolean inClass, StringBuilder sb) {
		int n = pattern.length();
		if (i + 1 == n) {
			sb.append('\\');
			return i;
		}
		char e = pattern.charAt(i + 1);
		// \Q..\E, \p{..}, \x{..}, \N{..}, \k<..> and \cX are copied as they are
		if (e == 'Q') {
			int end = pattern.indexOf("\\E", i + 2);
			end = end < 0 ? n - 1 : end + 1;
			sb.append(pattern, i, end + 1);
			return end;
		}
		if ((e == 'p' || e == 'P' || e == 'x' || e == 'N') && i + 2 < n && pattern.charAt(i + 2) == '{') return copyTo(pattern, i, '}', sb);
		if (e == 'k' && i + 2 < n && pattern.charAt(i + 2) == '<') return copyTo(pattern, i, '>', sb);
		if (e == 'c' && i + 2 < n) {
			sb.append(pattern, i, i + 3);
			return i + 2;
		}
		if (e == 'x') {
			int digits = 0;
			while (digits < 2 && i + 2 + digits < n && Character.digit(pattern.charAt(i + 2 + digits), 16) >= 0) digits++;
			if (digits == 1) {
				sb.append("\\x0").append(pattern.charAt(i + 2));
				return i + 2;
			}
			if (digits == 0) {
				sb.append("\\x00");
				return i + 1;
			}
		}
		else if (e == '0' && (i + 2 == n || pattern.charAt(i + 2) < '0' || pattern.charAt(i + 2) > '7')) {
			sb.append("\\x00");
			return i + 1;
		}
		else if (e == 'b' && inClass) {
			sb.append("\\x08");
			return i + 1;
		}
		else if (e == 'w' && inClass) {
			sb.append(WORD);
			return i + 1;
		}
		else if (SHORTHAND.containsKey(e) && !(e == 'B' && inClass)) {
			sb.append(SHORTHAND.get(e));
			return i + 1;
		}
		// \k and \N without their bracket (handled above) are unknown letters too
		else if (Character.isLetter(e) && e < 128 && (JAVA_ESCAPE_LETTERS.indexOf(e) < 0 || e == 'k' || e == 'N')) {
			sb.append(e);
			return i + 1;
		}
		sb.append('\\').append(e);
		return i + 1;
	}

	private static int copyTo(String pattern, int i, char close, StringBuilder sb) {
		int end = pattern.indexOf(close, i + 3);
		if (end < 0) end = pattern.length() - 1;
		sb.append(pattern, i, end + 1);
		return end;
	}

	// java only reads ASCII digits in a quantifier
	private static boolean isAsciiDigit(char c) {
		return c >= '0' && c <= '9';
	}

	// index of the } closing a {n}, {n,} or {n,m} quantifier that starts at i, -1 if it isn't one
	private static int quantifierEnd(String pattern, int i) {
		int n = pattern.length(), j = i + 1;
		int start = j;
		while (j < n && isAsciiDigit(pattern.charAt(j))) j++;
		if (j == start || j == n) return -1;
		if (pattern.charAt(j) == ',') {
			j++;
			while (j < n && isAsciiDigit(pattern.charAt(j))) j++;
		}
		return j < n && pattern.charAt(j) == '}' ? j : -1;
	}

	@Override
	public String replace(String strInput, String strPattern, String replacement, boolean caseSensitive, boolean multiLine) throws PageException {
		try {
			Matcher m = toPattern(strPattern, caseSensitive, multiLine).matcher(strInput);
			if (!m.find()) return strInput;
			StringBuilder sb = new StringBuilder(strInput.length());
			m.appendReplacement(sb, Matcher.quoteReplacement(expandReplacement(m, replacement)));
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
			if (!m.find()) return strInput;
			StringBuilder sb = new StringBuilder(strInput.length());
			do {
				m.appendReplacement(sb, Matcher.quoteReplacement(expandReplacement(m, replacement)));
			}
			while (findNext(m, m.end()));
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
