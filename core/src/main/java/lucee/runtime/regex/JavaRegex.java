package lucee.runtime.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

class JavaRegex implements Regex {

	private static final Double ZERO = Double.valueOf(0);
	private static final Key LEN = KeyConstants._len;
	private static final Key POS = KeyConstants._pos;
	private static final Key MATCH = KeyConstants._match;

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
			if (offset > 1) matcher.region(offset-1, strLen);
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
			if (offset > 1) matcher.region(offset-1, strLen);

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
			if (offset > 1) matcher.region(offset-1, strLen);
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
			if (offset > strLen){
				arr.add(findEmpty());
				return arr;	
			}

			Matcher matcher = toPattern(strPattern, caseSensitive, multiLine).matcher(strInput);
			if (offset > 1 ) matcher.region(offset-1, strLen);
			
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

	private Struct findEmpty() {
		Struct sct = new StructImpl();
		Array a = new ArrayImpl();
		a.appendEL(ZERO);
		sct.setEL(LEN, a);
		a = new ArrayImpl();
		a.appendEL(ZERO);
		sct.setEL(POS, a);
		a = new ArrayImpl();
		a.appendEL("");
		sct.setEL(MATCH, a);
		return sct;
	}

	private Struct toStruct(Matcher matcher, String input) {
		Struct sct = new StructImpl();
		Array lenArray = new ArrayImpl();
		Array posArray = new ArrayImpl();
		Array matchArray = new ArrayImpl();

		for(int i=0; i<=matcher.groupCount();i++) {
			lenArray.appendEL(matcher.end(i) - matcher.start(i));
			posArray.appendEL(matcher.start(i) + 1);
			matchArray.appendEL(input.substring(matcher.start(i), matcher.end(i)));
		}

		sct.setEL(POS, posArray);
		sct.setEL(LEN, lenArray);
		sct.setEL(MATCH, matchArray);
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
