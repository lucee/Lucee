package lucee.runtime.regex;

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
			if (offset > 1) matcher.region(offset - 1, strLen);
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
			if (offset > 1) matcher.region(offset - 1, strLen);

			ArrayImpl arr = null;
			while (matcher.find()) {
				if (arr == null) arr = new ArrayImpl();
				arr.append(matcher.start() + 1);
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
			if (offset > 1) matcher.region(offset - 1, strLen);
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
			if (offset > 1) matcher.region(offset - 1, strLen);

			while (matcher.find()) {
				arr.append(toStruct(matcher, strInput));
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
			while (matcher.find()) {
				arr.append(strInput.substring(matcher.start(), matcher.end()));
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
			return toPattern(strPattern, caseSensitive, multiLine).matcher(strInput).replaceFirst(replacement);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public String replaceAll(String strInput, String strPattern, String replacement, boolean caseSensitive, boolean multiLine) throws PageException {
		try {
			return toPattern(strPattern, caseSensitive, multiLine).matcher(strInput).replaceAll(replacement);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
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
		Struct sct = new StructImpl(StructImpl.TYPE_LINKED);
		Array a = new ArrayImpl();
		a.appendEL(ZERO);
		sct.setEL(KeyConstants._len, a);
		a = new ArrayImpl();
		a.appendEL(ZERO);
		sct.setEL(KeyConstants._pos, a);
		a = new ArrayImpl();
		a.appendEL("");
		sct.setEL(KeyConstants._match, a);
		return sct;
	}

	private Struct toStruct(Matcher matcher, String input) {
		Struct sct = new StructImpl(StructImpl.TYPE_LINKED);
		Array lenArray = new ArrayImpl();
		Array posArray = new ArrayImpl();
		Array matchArray = new ArrayImpl();

		for (int i = 0; i <= matcher.groupCount(); i++) {
			lenArray.appendEL(matcher.end(i) - matcher.start(i));
			posArray.appendEL(matcher.start(i) + 1);
			matchArray.appendEL(matcher.group(i));
		}

		sct.setEL(KeyConstants._pos, posArray);
		sct.setEL(KeyConstants._len, lenArray);
		sct.setEL(KeyConstants._match, matchArray);
		return sct;
	}

	private Pattern toPattern(String strPattern, boolean caseSensitive, boolean multiLine) {
		int flags = 0;
		if (!caseSensitive) flags += Pattern.CASE_INSENSITIVE;
		if (multiLine) flags += Pattern.MULTILINE;
		return Pattern.compile(strPattern, flags);
	}

	@Override
	public String getTypeName() {
		return "java";
	}

}