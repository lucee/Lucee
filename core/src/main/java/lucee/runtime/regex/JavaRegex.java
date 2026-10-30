package lucee.runtime.regex;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

class JavaRegex implements Regex {

	private static final Double ZERO = Double.valueOf(0);
	private static final Map<String, Pattern> cache = new ConcurrentHashMap<>();

	@Override
	public boolean matches(String strPattern, String strInput) throws PageException {
		try {
			return strInput.matches(strPattern);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public boolean matches(String strPattern, String strInput, boolean defaultValue) {
		try {
			return strInput.matches(strPattern);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	@Override
	public int indexOf(String strPattern, String strInput, int offset, boolean caseSensitive, boolean multiLine) throws PageException {
		try {
			int strLen = strInput.length();
			if (offset > strLen) return 0;

			Matcher matcher = toPattern(strPattern, caseSensitive, multiLine).matcher(strInput);
			if (offset > 1) startAt(matcher, offset - 1);
			if (!matcher.find()) return 0;

			return matcher.start() + 1;
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public Object indexOfAll(String strPattern, String strInput, int offset, boolean caseSensitive, boolean multiLine) throws PageException {
		try {
			int strLen = strInput.length();
			if (offset > strLen) return 0;

			Matcher matcher = toPattern(strPattern, caseSensitive, multiLine).matcher(strInput);
			if (offset > 1) startAt(matcher, offset - 1);

			ArrayImpl arr = null;
			int prevEnd = -1;
			while (findNext(matcher, prevEnd)) {
				if (arr == null) arr = new ArrayImpl();
				arr.append(matcher.start() + 1);
				prevEnd = matcher.end();
			}
			return arr == null ? 0 : arr;
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public Struct find(String strPattern, String strInput, int offset, boolean caseSensitive, boolean multiLine) throws PageException {
		try {
			int strLen = strInput.length();
			if (offset > strLen) return findEmpty();

			Matcher matcher = toPattern(strPattern, caseSensitive, multiLine).matcher(strInput);
			if (offset > 1) startAt(matcher, offset - 1);
			if (!matcher.find()) return findEmpty();

			return toStruct(matcher, strInput);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public Array findAll(String strPattern, String strInput, int offset, boolean caseSensitive, boolean multiLine) throws PageException {
		try {
			ArrayImpl arr = new ArrayImpl();

			int strLen = strInput.length();
			if (offset > strLen) {
				arr.add(findEmpty());
				return arr;
			}

			Matcher matcher = toPattern(strPattern, caseSensitive, multiLine).matcher(strInput);
			if (offset > 1) startAt(matcher, offset - 1);

			int prevEnd = -1;
			while (findNext(matcher, prevEnd)) {
				arr.append(toStruct(matcher, strInput));
				prevEnd = matcher.end();
			}
			if (arr.isEmpty()) arr.add(findEmpty());
			return arr;
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public String match(String strPattern, String strInput, int offset, boolean caseSensitive, boolean multiLine) throws PageException {
		try {
			Matcher matcher = toPattern(strPattern, caseSensitive, multiLine).matcher(strInput);
			if (!matcher.find()) return "";

			return strInput.substring(matcher.start(), matcher.end());
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public Array matchAll(String strPattern, String strInput, int offset, boolean caseSensitive, boolean multiLine) throws PageException {
		try {
			Matcher matcher = toPattern(strPattern, caseSensitive, multiLine).matcher(strInput);

			ArrayImpl arr = new ArrayImpl();
			int prevEnd = -1;
			while (findNext(matcher, prevEnd)) {
				arr.append(strInput.substring(matcher.start(), matcher.end()));
				prevEnd = matcher.end();
			}
			return arr;
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public String replace(String strInput, String strPattern, String replacement, boolean caseSensitive, boolean multiLine) throws PageException {
		try {
			Matcher m = toPattern(strPattern, caseSensitive, multiLine).matcher(strInput);
			if (!m.find()) return strInput;
			// Matcher.replaceFirst starts from a 16-char builder and regrows it to the input's length
			StringBuilder sb = new StringBuilder(strInput.length());
			m.appendReplacement(sb, replacement);
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
			// Matcher.replaceAll starts from a 16-char builder and regrows it to the input's length
			StringBuilder sb = new StringBuilder(strInput.length());
			do {
				m.appendReplacement(sb, replacement);
			}
			while (findNext(m, m.end()));
			m.appendTail(sb);
			return sb.toString();
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	static String translateReplacement(String repl) {
		if (repl == null || (repl.indexOf('\\') < 0 && repl.indexOf('$') < 0)) return repl;
		StringBuilder out = new StringBuilder(repl.length());
		int i = 0, n = repl.length();
		while (i < n) {
			char c = repl.charAt(i);
			if (c == '$') { out.append("\\$"); i++; }
			else if (c == '\\' && i + 1 < n) {
				char next = repl.charAt(i + 1);
				if (next >= '0' && next <= '9') { out.append('$').append(next); i += 2; }
				else if (next == 'E') { i += 2; }
				else { out.append(c).append(next); i += 2; }
			}
			else { out.append(c); i++; }
		}
		return out.toString();
	}

	@Override
	public String escape(String strInput) throws PageException {
		try {
			StringBuilder strEscape = new StringBuilder();
			for (char c: strInput.toCharArray()) {
				if (!Character.isLetterOrDigit(c)) {
					strEscape.append("\\");
				}
				strEscape.append(c);
			}
			return strEscape.toString();
			/*
			 * // Pattern.quote just wraps the string with \Q \E return
			 * removeQE(Pattern.compile(Pattern.quote(strInput)).toString());
			 */
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}
	/*
	 * private static String removeQE(String input) { if (input.startsWith("\\Q") &&
	 * input.endsWith("\\E")) { return input.substring(2, input.length() - 2); } return input; }
	 */

	private Struct findEmpty() {
		Struct sct = new StructImpl(StructImpl.TYPE_LINKED, 4);
		Array a = new ArrayImpl(1);
		a.appendEL(ZERO);
		sct.setEL(KeyConstants._len, a);
		a = new ArrayImpl(1);
		a.appendEL(ZERO);
		sct.setEL(KeyConstants._pos, a);
		a = new ArrayImpl(1);
		a.appendEL("");
		sct.setEL(KeyConstants._match, a);
		return sct;
	}

	private Struct toStruct(Matcher matcher, String input) {
		Struct sct = new StructImpl(StructImpl.TYPE_LINKED, 4);
		int groups = matcher.groupCount() + 1;
		Array lenArray = new ArrayImpl(groups);
		Array posArray = new ArrayImpl(groups);
		Array matchArray = new ArrayImpl(groups);

		for (int i = 0; i < groups; i++) {
			lenArray.appendEL(matcher.end(i) - matcher.start(i));
			posArray.appendEL(matcher.start(i) + 1);
			matchArray.appendEL(matcher.group(i));
		}

		sct.setEL(KeyConstants._pos, posArray);
		sct.setEL(KeyConstants._len, lenArray);
		sct.setEL(KeyConstants._match, matchArray);
		return sct;
	}

	// next match for the scope "all" loops, prevEnd is where the previous match ended, -1 before the first
	protected boolean findNext(Matcher matcher, int prevEnd) {
		return matcher.find();
	}

	// Starts the search at index start without cutting the input there, as Oro's setCurrentOffset() does.
	// A region on its own has anchoring bounds (^ and \A match at the region start) and opaque bounds
	// (\b and lookarounds can't see the chars before it), so code that loops reFind from increasing offsets
	// with a leading ^ re-anchored at every restart, Preside's PresideObjectService._reSearch() for one.
	// The region end is always the input end, so $ is unchanged. Plain java changes too, see LDEV-6798.
	private static void startAt(Matcher matcher, int start) {
		matcher.region(start, matcher.regionEnd()).useAnchoringBounds(false).useTransparentBounds(true);
	}

	protected Pattern toPattern(String strPattern, boolean caseSensitive, boolean multiLine) {
		int f = 0;
		if (!caseSensitive) f += Pattern.CASE_INSENSITIVE;
		if (multiLine) f += Pattern.MULTILINE;
		final int flags = f;
		String key = flags + "\0" + strPattern;
		return cache.computeIfAbsent(key, k -> Pattern.compile(strPattern, flags));
	}

	@Override
	public String getTypeName() {
		return "java";
	}

}