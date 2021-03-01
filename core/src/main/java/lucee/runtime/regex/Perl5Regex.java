package lucee.runtime.regex;

import org.apache.oro.text.regex.MalformedPatternException;

import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;

class Perl5Regex implements Regex {

	@Override
	public boolean matches(String strPattern, String strInput) throws PageException {
		return Perl5Util.matches(strPattern, strInput);
	}

	@Override
	public boolean matches(String strPattern, String strInput, boolean defaultValue) {
		return Perl5Util.matches(strPattern, strInput, defaultValue);
	}

	@Override
	public String match(String strPattern, String strInput, int offset, boolean caseSensitive, boolean multiLine) throws PageException {
		try {
			return Caster.toString(Perl5Util.match(strPattern, strInput, offset, caseSensitive, false, multiLine));
		}
		catch (MalformedPatternException e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public Array matchAll(String strPattern, String strInput, int offset, boolean caseSensitive, boolean multiLine) throws PageException {
		try {
			return (Array) Perl5Util.match(strPattern, strInput, offset, caseSensitive, true, multiLine);
		}
		catch (MalformedPatternException e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public int indexOf(String strPattern, String strInput, int offset, boolean caseSensitive, boolean multiLine) throws PageException {
		try {
			return Caster.toIntValue(Perl5Util.indexOf(strPattern, strInput, offset, caseSensitive, false, multiLine));
		}
		catch (MalformedPatternException e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public Array indexOfAll(String strPattern, String strInput, int offset, boolean caseSensitive, boolean multiLine) throws PageException {
		try {
			return Caster.toArray(Perl5Util.indexOf(strPattern, strInput, offset, caseSensitive, true, multiLine));
		}
		catch (MalformedPatternException e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public Struct find(String strPattern, String strInput, int offset, boolean caseSensitive, boolean multiLine) throws PageException {
		try {
			return Caster.toStruct(Perl5Util.find(strPattern, strInput, offset, caseSensitive, false, multiLine));
		}
		catch (MalformedPatternException e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public Array findAll(String strPattern, String strInput, int offset, boolean caseSensitive, boolean multiLine) throws PageException {
		try {
			return Caster.toArray(Perl5Util.find(strPattern, strInput, offset, caseSensitive, true, multiLine));
		}
		catch (MalformedPatternException e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public String replace(String strInput, String strPattern, String replacement, boolean caseSensitive, boolean multiLine) throws PageException {
		try {
			return Perl5Util.replace(strInput, strPattern, replacement, caseSensitive, false, multiLine);
		}
		catch (MalformedPatternException e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public String replaceAll(String strInput, String strPattern, String replacement, boolean caseSensitive, boolean multiLine) throws PageException {
		try {
			return Perl5Util.replace(strInput, strPattern, replacement, caseSensitive, true, multiLine);
		}
		catch (MalformedPatternException e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public String getTypeName() {
		return "perl";
	}
}
