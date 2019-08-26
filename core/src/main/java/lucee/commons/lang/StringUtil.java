/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.commons.lang;

import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.ArrayUtil;

/**
 * Util to do some additional String Operations
 */
public final class StringUtil {

	private static final char[] SPECIAL_WHITE_SPACE_CHARS = new char[] { 0x85 // NEL, Next line
			, 0xa0 // no-break space
			, 0x1680 // ogham space mark
			, 0x180e // mongolian vowel separator
			, 0x2000 // en quad
			, 0x2001 // em quad
			, 0x2002 // en space
			, 0x2003 // em space
			, 0x2004 // three-per-em space
			, 0x2005 // four-per-em space
			, 0x2006 // six-per-em space
			, 0x2007 // figure space
			, 0x2008 // punctuation space
			, 0x2009 // thin space
			, 0x200A // hair space
			, 0x2028 // line separator
			, 0x2029 // paragraph separator
			, 0x202F // narrow no-break space
			, 0x205F // medium mathematical space
			, 0x3000 // ideographic space
	};

	/**
	 * do first Letter Upper case
	 * 
	 * @param str String to operate
	 * @return uppercase string
	 */
	public static String ucFirst(String str) {
		if (str == null) return null;
		else if (str.length() <= 1) return str.toUpperCase();
		else {
			return str.substring(0, 1).toUpperCase() + str.substring(1);
		}
	}

	public static String capitalize(String input, char[] delims) {

		if (isEmpty(input)) return input;

		if (ArrayUtil.isEmpty(delims)) delims = new char[] { '.', '-', '(', ')' };

		StringBuilder sb = new StringBuilder(input.length());

		boolean isLastDelim = true, isLastSpace = true;
		int len = input.length();
		for (int i = 0; i < len; i++) {

			char c = input.charAt(i);

			if (Character.isWhitespace(c)) {

				if (!isLastSpace) sb.append(' ');

				isLastSpace = true;
			}
			else {

				sb.append((isLastSpace || isLastDelim) ? Character.toUpperCase(c) : c);

				isLastDelim = _contains(delims, c);
				isLastSpace = false;
			}
		}

		return sb.toString();
	}

	private static boolean _contains(char[] chars, char c) {
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == c) return true;
		}
		return false;
	}

	/**
	 * do first Letter Upper case
	 * 
	 * @param str String to operate
	 * @return lower case String
	 */
	public static String lcFirst(String str) {
		if (str == null) return null;
		else if (str.length() <= 1) return str.toLowerCase();
		else {
			return str.substring(0, 1).toLowerCase() + str.substring(1);
		}
	}

	/**
	 * Unescapes HTML Tags
	 * 
	 * @param html html code to escape
	 * @return escaped html code
	 */
	public static String unescapeHTML(String html) {
		return HTMLEntities.unescapeHTML(html);
	}

	/**
	 * Escapes XML Tags
	 * 
	 * @param html html code to unescape
	 * @return unescaped html code
	 */
	public static String escapeHTML(String html) {
		return HTMLEntities.escapeHTML(html);
	}

	/**
	 * escapes JS sensitive characters
	 * 
	 * @param str String to escape
	 * @return escapes String
	 */
	public static String escapeJS(String str, char quotesUsed) {
		return escapeJS(str, quotesUsed, (CharsetEncoder) null);
	}

	public static String escapeJS(String str, char quotesUsed, java.nio.charset.Charset charset) {
		return escapeJS(str, quotesUsed, charset == null ? null : charset.newEncoder());
	}

	/**
	 * escapes JS sensitive characters
	 * 
	 * @param str String to escape
	 * @param charset if not null, it checks if the given string is supported by the encoding, if not,
	 *            lucee encodes the string
	 * @return escapes String
	 */
	public static String escapeJS(String str, char quotesUsed, CharsetEncoder enc) {
		char[] arr = str.toCharArray();
		StringBuilder rtn = new StringBuilder(arr.length);
		rtn.append(quotesUsed);

		for (int i = 0; i < arr.length; i++) {
			switch (arr[i]) {
			case '\\':
				rtn.append("\\\\");
				break;
			case '\n':
				rtn.append("\\n");
				break;
			case '\r':
				rtn.append("\\r");
				break;
			case '\f':
				rtn.append("\\f");
				break;
			case '\b':
				rtn.append("\\b");
				break;
			case '\t':
				rtn.append("\\t");
				break;
			case '"':
				if (quotesUsed == '"') rtn.append("\\\"");
				else rtn.append('"');
				break;
			case '\'':
				if (quotesUsed == '\'') rtn.append("\\\'");
				else rtn.append('\'');
				break;
			case '/':
				// escape </script>
				if (i > 0 && arr[i - 1] == '<' && i + 1 < arr.length && arr[i + 1] == 's' && i + 2 < arr.length && arr[i + 2] == 'c' && i + 3 < arr.length && arr[i + 3] == 'r'
						&& i + 4 < arr.length && arr[i + 4] == 'i' && i + 5 < arr.length && arr[i + 5] == 'p' && i + 6 < arr.length && arr[i + 6] == 't' && i + 7 < arr.length
						&& (isWhiteSpace(arr[i + 7]) || arr[i + 7] == '>')

				) {
					rtn.append("\\/");
					break;
				}

			default:
				if (Character.isISOControl(arr[i]) || (arr[i] >= 128 && (enc == null || !enc.canEncode(arr[i])))) {
					if (arr[i] < 0x10) rtn.append("\\u000");
					else if (arr[i] < 0x100) rtn.append("\\u00");
					else if (arr[i] < 0x1000) rtn.append("\\u0");
					else rtn.append("\\u");
					rtn.append(Integer.toHexString(arr[i]));
				}
				else {
					rtn.append(arr[i]);
				}
				break;
			}
		}
		return rtn.append(quotesUsed).toString();
	}

	/**
	 * reapeats a string
	 * 
	 * @param str string to repeat
	 * @param count how many time string will be repeated
	 * @return reapted string
	 */
	public static String repeatString(String str, int count) {
		if (count <= 0) return "";
		char[] chars = str.toCharArray();
		char[] rtn = new char[chars.length * count];
		int pos = 0;
		for (int i = 0; i < count; i++) {
			for (int y = 0; y < chars.length; y++)
				rtn[pos++] = chars[y];
			// rtn.append(str);
		}
		return new String(rtn);
	}

	/**
	 * translate, like method toString, an object to a string, but when value is null value will be
	 * translated to an empty String ("").
	 * 
	 * @param o Object to convert
	 * @return converted String
	 */
	public static String toStringEmptyIfNull(Object o) {
		if (o == null) return "";
		return o.toString();
	}

	public static String emptyIfNull(String str) {
		if (str == null) return "";
		return str;
	}

	public static String emptyIfNull(Collection.Key key) {
		if (key == null) return "";
		return key.getString();
	}

	/**
	 * escape all special characters of the regular expresson language
	 * 
	 * @param str String to escape
	 * @return escaped String
	 */
	public static String reqExpEscape(String str) {
		char[] arr = str.toCharArray();
		StringBuilder sb = new StringBuilder(str.length() * 2);

		for (int i = 0; i < arr.length; i++) {
			sb.append('\\');
			sb.append(arr[i]);
		}

		return sb.toString();
	}

	/**
	 * translate a string to a valid identity variable name
	 * 
	 * @param varName variable name template to translate
	 * @return translated variable name
	 */
	public static String toIdentityVariableName(String varName) {
		char[] chars = varName.toCharArray();
		long changes = 0;

		StringBuilder rtn = new StringBuilder(chars.length + 2);
		rtn.append("CF");

		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) rtn.append(c);
			else {
				rtn.append('_');
				changes += (c * (i + 1));
			}
		}

		return rtn.append(changes).toString();
	}

	/**
	 * translate a string to a valid classname string
	 * 
	 * @param str string to translate
	 * @return translated String
	 */
	public static String toClassName(String str) {
		StringBuilder rtn = new StringBuilder();
		String[] arr = str.split("[\\\\|//]");
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].length() == 0) continue;
			if (rtn.length() != 0) rtn.append('.');
			char[] chars = arr[i].toCharArray();
			long changes = 0;
			for (int y = 0; y < chars.length; y++) {
				char c = chars[y];
				if (y == 0 && (c >= '0' && c <= '9')) rtn.append("_" + c);
				else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) rtn.append(c);
				else {
					rtn.append('_');
					changes += (c * (i + 1));
				}
			}
			if (changes > 0) rtn.append(changes);
		}
		return rtn.toString();
	}

	/**
	 * translate a string to a valid variable string
	 * 
	 * @param str string to translate
	 * @return translated String
	 */
	public static String toVariableName(String str) {
		return toVariableName(str, true, false);
	}

	public static String toJavaClassName(String str) {
		return toVariableName(str, true, true);
	}

	public static String toVariableName(String str, boolean addIdentityNumber, boolean allowDot) {

		StringBuilder rtn = new StringBuilder();
		char[] chars = str.toCharArray();
		long changes = 0;
		boolean doCorrect = true;
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (i == 0 && (c >= '0' && c <= '9')) rtn.append("_" + c);
			else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_' || c == '$' || (allowDot && c == '.')) rtn.append(c);
			else {
				doCorrect = false;
				rtn.append('_');
				changes += (c * (i + 1));
			}
		}

		if (addIdentityNumber && changes > 0) rtn.append(changes);
		// print.ln(" - "+rtn);

		if (doCorrect) return correctReservedWord(rtn.toString());
		return rtn.toString();
	}

	/**
	 * if given string is a keyword it will be replaced with none keyword
	 * 
	 * @param str
	 * @return corrected word
	 */
	private static String correctReservedWord(String str) {
		char first = str.charAt(0);

		switch (first) {
		case 'a':
			if (str.equals("abstract")) return "_" + str;
			break;
		case 'b':
			if (str.equals("boolean")) return "_" + str;
			else if (str.equals("break")) return "_" + str;
			else if (str.equals("byte")) return "_" + str;
			break;
		case 'c':
			if (str.equals("case")) return "_" + str;
			else if (str.equals("catch")) return "_" + str;
			else if (str.equals("char")) return "_" + str;
			else if (str.equals("const")) return "_" + str;
			else if (str.equals("class")) return "_" + str;
			else if (str.equals("continue")) return "_" + str;
			break;
		case 'd':
			if (str.equals("default")) return "_" + str;
			else if (str.equals("do")) return "_" + str;
			else if (str.equals("double")) return "_" + str;
			break;
		case 'e':
			if (str.equals("else")) return "_" + str;
			else if (str.equals("extends")) return "_" + str;
			else if (str.equals("enum")) return "_" + str;
			break;
		case 'f':
			if (str.equals("false")) return "_" + str;
			else if (str.equals("final")) return "_" + str;
			else if (str.equals("finally")) return "_" + str;
			else if (str.equals("float")) return "_" + str;
			else if (str.equals("for")) return "_" + str;
			break;
		case 'g':
			if (str.equals("goto")) return "_" + str;
			break;
		case 'i':
			if (str.equals("if")) return "_" + str;
			else if (str.equals("implements")) return "_" + str;
			else if (str.equals("import")) return "_" + str;
			else if (str.equals("instanceof")) return "_" + str;
			else if (str.equals("int")) return "_" + str;
			else if (str.equals("interface")) return "_" + str;
			break;
		case 'j':
			if (str.equals("java")) return "_" + str;
			break;
		case 'n':
			if (str.equals("native")) return "_" + str;
			else if (str.equals("new")) return "_" + str;
			else if (str.equals("null")) return "_" + str;
			break;
		case 'p':
			if (str.equals("package")) return "_" + str;
			else if (str.equals("private")) return "_" + str;
			else if (str.equals("protected")) return "_" + str;
			else if (str.equals("public")) return "_" + str;
			break;
		case 'r':
			if (str.equals("return")) return "_" + str;
			break;
		case 's':
			if (str.equals("short")) return "_" + str;
			else if (str.equals("static")) return "_" + str;
			else if (str.equals("strictfp")) return "_" + str;
			else if (str.equals("super")) return "_" + str;
			else if (str.equals("switch")) return "_" + str;
			else if (str.equals("synchronized")) return "_" + str;
			break;
		case 't':
			if (str.equals("this")) return "_" + str;
			else if (str.equals("throw")) return "_" + str;
			else if (str.equals("throws")) return "_" + str;
			else if (str.equals("transient")) return "_" + str;
			else if (str.equals("true")) return "_" + str;
			else if (str.equals("try")) return "_" + str;
			break;
		case 'v':
			if (str.equals("void")) return "_" + str;
			else if (str.equals("volatile")) return "_" + str;
			break;
		case 'w':
			if (str.equals("while")) return "_" + str;
			break;
		}
		return str;

	}

	/**
	 * This function returns a string with whitespace stripped from the beginning of str
	 * 
	 * @param str String to clean
	 * @return cleaned String
	 */
	public static String ltrim(String str, String defaultValue) {
		if (str == null) return defaultValue;
		int len = str.length();
		int st = 0;

		while ((st < len) && (str.charAt(st) <= ' ')) {
			st++;
		}
		return ((st > 0)) ? str.substring(st) : str;
	}

	/**
	 * This function returns a string with whitespace stripped from the end of str
	 * 
	 * @param str String to clean
	 * @return cleaned String
	 */
	public static String rtrim(String str, String defaultValue) {
		if (str == null) return defaultValue;
		int len = str.length();

		while ((0 < len) && (str.charAt(len - 1) <= ' ')) {
			len--;
		}
		return (len < str.length()) ? str.substring(0, len) : str;
	}

	/**
	 * trim given value, return defaultvalue when input is null
	 * 
	 * @param str
	 * @param defaultValue
	 * @return trimmed string or defaultValue
	 */
	public static String trim(String str, String defaultValue) {
		if (str == null) return defaultValue;
		return str.trim();
	}

	/**
	 * 
	 * @param c character to check
	 * @param checkSpecialWhiteSpace if set to true, lucee checks also uncommon white spaces.
	 * @return
	 */
	public static boolean isWhiteSpace(char c, boolean checkSpecialWhiteSpace) {
		if (Character.isWhitespace(c)) return true;
		if (checkSpecialWhiteSpace) {
			for (int i = 0; i < SPECIAL_WHITE_SPACE_CHARS.length; i++) {
				if (c == SPECIAL_WHITE_SPACE_CHARS[i]) return true;
			}
		}
		return false;
	}

	public static boolean isWhiteSpace(char c) {
		return isWhiteSpace(c, false);
	}

	/**
	 * trim given value, return defaultvalue when input is null this function no only removes the
	 * "classic" whitespaces, it also removes Byte order masks forgotten to remove when reading a UTF
	 * file.
	 * 
	 * @param str
	 * @param removeBOM if set to true, Byte Order Mask that got forgotten get removed as well
	 * @param removeSpecialWhiteSpace if set to true, lucee removes also uncommon white spaces.
	 * @param defaultValue
	 * @return trimmed string or defaultValue
	 */
	public static String trim(String str, boolean removeBOM, boolean removeSpecialWhiteSpace, String defaultValue) {
		if (str == null) return defaultValue;
		if (str.isEmpty()) return str;
		// remove leading BOM Marks
		if (removeBOM) {
			// UTF-16, big-endian
			if (str.charAt(0) == '\uFEFF') str = str.substring(1);
			else if (str.charAt(0) == '\uFFFD') str = str.substring(1);

			// UTF-16, little-endian
			else if (str.charAt(0) == '\uFFFE') str = str.substring(1);

			// UTF-8
			else if (str.length() >= 2) {
				// TODO i get this from UTF-8 files generated by suplime text, i was expecting something else
				if (str.charAt(0) == '\uBBEF' && str.charAt(1) == '\uFFFD') str = str.substring(2);
			}
		}

		if (removeSpecialWhiteSpace) {
			int len = str.length();
			int startIndex = 0, endIndex = len - 1;
			// left
			while ((startIndex < len) && isWhiteSpace(str.charAt(startIndex), true)) {
				startIndex++;
			}
			// right
			while ((startIndex < endIndex) && isWhiteSpace(str.charAt(endIndex), true)) {
				endIndex--;
			}
			return ((startIndex > 0) || (endIndex + 1 < len)) ? str.substring(startIndex, endIndex + 1) : str;
		}

		return str.trim();
	}

	/**
	 * return if in a string are line feeds or not
	 * 
	 * @param str string to check
	 * @return translated string
	 */
	public static boolean hasLineFeed(String str) {
		int len = str.length();
		char c;
		for (int i = 0; i < len; i++) {
			c = str.charAt(i);
			if (c == '\n' || c == '\r') return true;
		}
		return false;
	}

	/**
	 * remove all white spaces followed by whitespaces
	 * 
	 * @param str string to translate
	 * @return translated string
	 */
	public static String suppressWhiteSpace(String str) {
		int len = str.length();
		StringBuilder sb = new StringBuilder(len);
		// boolean wasWS=false;

		char c;
		char buffer = 0;
		for (int i = 0; i < len; i++) {
			c = str.charAt(i);
			if (c == '\n' || c == '\r') buffer = '\n';
			else if (isWhiteSpace(c)) {
				if (buffer == 0) buffer = c;
			}
			else {
				if (buffer != 0) {
					sb.append(buffer);
					buffer = 0;
				}
				sb.append(c);
			}
			// sb.append(c);
		}
		if (buffer != 0) sb.append(buffer);

		return sb.toString();
	}

	/**
	 * returns string, if given string is null or length 0 return default value
	 * 
	 * @param value
	 * @param defaultValue
	 * @return value or default value
	 */
	public static String toString(String value, String defaultValue) {
		return value == null || value.length() == 0 ? defaultValue : value;
	}

	/**
	 * returns string, if given string is null or length 0 return default value
	 * 
	 * @param value
	 * @param defaultValue
	 * @return value or default value
	 */
	public static String toString(Object value, String defaultValue) {
		if (value == null) return defaultValue;
		return toString(value.toString(), defaultValue);
	}

	/**
	 * cut string to max size if the string is greater, otherwise to nothing
	 * 
	 * @param content
	 * @param max
	 * @return cutted string
	 */

	public static String max(String content, int max) {
		return max(content, max, "");
	}

	public static String max(String content, int max, String dotDotDot) {
		if (content == null) return null;
		if (content.length() <= max) return content;

		return content.substring(0, max) + dotDotDot;
	}

	/**
	 * performs a replace operation on a string
	 * 
	 * @param input - the string input to work on
	 * @param find - the substring to find
	 * @param repl - the substring to replace the matches with
	 * @param firstOnly - if true then only the first occurrence of {@code find} will be replaced
	 * @param ignoreCase - if true then matches will not be case sensitive
	 * @return
	 */

	public static String replace(String input, String find, String repl, boolean firstOnly, boolean ignoreCase) {
		return _replace(input, find, repl, firstOnly, ignoreCase, null).toString();
	}

	public static CharSequence _replace(String input, String find, String repl, boolean firstOnly, boolean ignoreCase, List<Pos> positions) {
		int findLen = find.length();

		if (findLen == 0) return input;

		// String scan = input;

		/*
		 * if ( ignoreCase ) { scan = scan.toLowerCase(); find = find.toLowerCase(); } else
		 */ if (!ignoreCase && findLen == repl.length()) {

			if (find.equals(repl)) return input;
			if (!firstOnly && findLen == 1 && positions == null) return input.replace(find.charAt(0), repl.charAt(0));
		}

		int pos = ignoreCase ? indexOfIgnoreCase(input, find) : input.indexOf(find);
		if (pos == -1) return input;

		int start = 0;
		StringBuilder sb = new StringBuilder(repl.length() > find.length() ? (int) Math.ceil(input.length() * 1.2) : input.length());

		while (pos != -1) {
			if (positions != null) positions.add(new Pos(pos, repl.length()));
			sb.append(input.substring(start, pos));
			sb.append(repl);
			start = pos + findLen;
			if (firstOnly) break;
			pos = ignoreCase ? indexOfIgnoreCase(input, find, start) : input.indexOf(find, start);
		}
		if (input.length() > start) sb.append(input.substring(start));
		return sb;
	}

	private static class Pos {
		private int position;
		private int len;

		private Pos(int position, int len) {
			this.position = position;
			this.len = len;
		}

		@Override
		public String toString() {
			return "pos:" + position + ";len:" + len;
		}
	}

	public static String replace(PageContext pc, String input, String find, UDF udf, boolean firstOnly) throws PageException {
		int len;
		if ((len = find.length()) == 0) return input;

		StringBuilder sb = new StringBuilder();
		String repl;
		int index, last = 0;
		while ((index = input.indexOf(find, last)) != -1) {
			sb.append(input.substring(last, index));
			repl = Caster.toString(udf.call(pc, new Object[] { find, index, input }, true));
			sb.append(repl);
			last = index + len;
			if (firstOnly) break;
		}
		if (last < input.length()) sb.append(input.substring(last));
		return sb.toString();
	}

	/**
	 * maintains the legacy signature of this method where matches are CaSe sensitive (sets the default
	 * of ignoreCase to false).
	 * 
	 * @param input - the string input to work on
	 * @param find - the substring to find
	 * @param repl - the substring to replace the matches with
	 * @param firstOnly - if true then only the first occurrence of {@code find} will be replaced
	 * @return - calls replace( input, find, repl, firstOnly, false )
	 */
	public static String replace(String input, String find, String repl, boolean firstOnly) {
		return replace(input, find, repl, firstOnly, false);
	}

	/**
	 * performs a CaSe sensitive replace all
	 * 
	 * @param input - the string input to work on
	 * @param find - the substring to find
	 * @param repl - the substring to replace the matches with
	 * @return - calls replace( input, find, repl, false, false )
	 */
	public static String replace(String input, String find, String repl) {

		return replace(input, find, repl, false, false);
	}

	/**
	 * adds zeros add the begin of an int example: addZeros(2,3) return "002"
	 * 
	 * @param i number to add nulls
	 * @param size
	 * @return min len of return value;
	 */
	public static String addZeros(int i, int size) {
		String rtn = Caster.toString(i);
		if (rtn.length() < size) return repeatString("0", size - rtn.length()) + rtn;
		return rtn;
	}

	/**
	 * adds zeros add the begin of an int example: addZeros(2,3) return "002"
	 * 
	 * @param i number to add nulls
	 * @param size
	 * @return min len of return value;
	 */
	public static String addZeros(long i, int size) {
		String rtn = Caster.toString(i);
		if (rtn.length() < size) return repeatString("0", size - rtn.length()) + rtn;
		return rtn;
	}

	public static int indexOf(String haystack, String needle) {
		if (haystack == null) return -1;
		return haystack.indexOf(needle);
	}

	public static int indexOfIgnoreCase(String haystack, String needle) {
		return indexOfIgnoreCase(haystack, needle, 0);
	}

	public static int indexOfIgnoreCase(String haystack, String needle, int offset) {
		if (StringUtil.isEmpty(haystack) || StringUtil.isEmpty(needle)) return -1;
		needle = needle.toLowerCase();

		if (offset > 0) haystack = haystack.substring(offset);
		else offset = 0;

		int lenHaystack = haystack.length();
		int lenNeedle = needle.length();

		char lastNeedle = needle.charAt(lenNeedle - 1);
		char c;
		outer: for (int i = lenNeedle - 1; i < lenHaystack; i++) {
			c = Character.toLowerCase(haystack.charAt(i));
			if (c == lastNeedle) {
				for (int y = 0; y < lenNeedle - 1; y++) {
					if (needle.charAt(y) != Character.toLowerCase(haystack.charAt(i - (lenNeedle - 1) + y))) continue outer;
				}
				return (i - (lenNeedle - 1)) + offset;
			}
		}
		return -1;
	}

	/**
	 * Tests if this string starts with the specified prefix.
	 * 
	 * @param str string to check first char
	 * @param prefix the prefix.
	 * @return is first of given type
	 */
	public static boolean startsWith(String str, char prefix) {
		return str != null && str.length() > 0 && str.charAt(0) == prefix;
	}

	public static boolean startsWith(String str, char prefix1, char prefix2) {
		return str != null && str.length() > 0 && (str.charAt(0) == prefix1 || str.charAt(0) == prefix2);
	}

	/**
	 * Tests if this string ends with the specified suffix.
	 * 
	 * @param str string to check first char
	 * @param suffix the suffix.
	 * @return is last of given type
	 */
	public static boolean endsWith(String str, char suffix) {
		return str != null && str.length() > 0 && str.charAt(str.length() - 1) == suffix;
	}

	public static boolean endsWith(String str, char prefix1, char prefix2) {
		return str != null && str.length() > 0 && (str.charAt(str.length() - 1) == prefix1 || str.charAt(str.length() - 1) == prefix2);
	}

	/**
	 * Tests if this string ends with the specified suffix.
	 * 
	 * @param str string to check first char
	 * @param suffix the suffix.
	 * @return is last of given type
	 */
	/**
	 * Helper functions to query a strings start portion. The comparison is case insensitive.
	 *
	 * @param base the base string.
	 * @param start the starting text.
	 *
	 * @return true, if the string starts with the given starting text.
	 */
	public static boolean startsWithIgnoreCase(final String base, final String start) {
		if (base.length() < start.length()) {
			return false;
		}
		return base.regionMatches(true, 0, start, 0, start.length());
	}

	/**
	 * Helper functions to query a strings end portion. The comparison is case insensitive.
	 *
	 * @param base the base string.
	 * @param end the ending text.
	 *
	 * @return true, if the string ends with the given ending text.
	 */
	public static boolean endsWithIgnoreCase(final String base, final String end) {
		if (base.length() < end.length()) {
			return false;
		}
		return base.regionMatches(true, base.length() - end.length(), end, 0, end.length());
	}

	/**
	 * returns if byte arr is a BOM character Stream (UTF-8,UTF-16)
	 * 
	 * @param barr
	 * @return is BOM or not
	 */
	public static boolean isBOM(byte[] barr) {
		return barr.length >= 3 && barr[0] == 0xEF && barr[1] == 0xBB && barr[2] == 0xBF;
	}

	/**
	 * return "" if value is null otherwise return same string
	 * 
	 * @param str
	 * @return string (not null)
	 */
	public static String valueOf(String str) {
		if (str == null) return "";
		return str;
	}

	/**
	 * cast a string a lower case String, is faster than the String.toLowerCase, if all Character are
	 * already Low Case
	 * 
	 * @param str
	 * @return lower case value
	 */
	public static String toLowerCase(String str) {
		int len = str.length();
		char c;
		for (int i = 0; i < len; i++) {
			c = str.charAt(i);
			if (!((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9'))) {
				return str.toLowerCase();
			}
		}

		return str;
	}

	public static String toUpperCase(String str) {
		int len = str.length();
		char c;
		for (int i = 0; i < len; i++) {
			c = str.charAt(i);
			if (!((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9'))) {
				return str.toUpperCase();
			}
		}

		return str;
	}

	/**
	 * soundex function
	 * 
	 * @param str
	 * @return soundex from given string
	 */
	public static String soundex(String str) {
		return new org.apache.commons.codec.language.Soundex().soundex(str);
	}

	/**
	 * return the last character of a string, if string ist empty return 0;
	 * 
	 * @param str string to get last character
	 * @return last character
	 */
	public static char lastChar(String str) {
		if (str == null || str.length() == 0) return 0;
		return str.charAt(str.length() - 1);
	}

	/**
	 * 
	 * @param str
	 * @return return if a String is "Empty", that means NULL or String with length 0 (whitespaces will
	 *         not counted)
	 */
	public static boolean isEmpty(CharSequence str) {
		return str == null || str.length() == 0;
	}

	/**
	 * 
	 * @param str
	 * @return return if a String is "Empty", that means NULL or String with length 0 (whitespaces will
	 *         not counted)
	 */
	public static boolean isEmpty(String str, boolean trim) {
		if (!trim) return isEmpty(str);
		return str == null || str.trim().length() == 0;
	}

	/**
	 * return the first character of a string, if string ist empty return 0;
	 * 
	 * @param str string to get first character
	 * @return first character
	 */
	public static char firstChar(String str) {
		if (isEmpty(str)) return 0;
		return str.charAt(0);
	}

	public static String removeWhiteSpace(String str) {
		if (isEmpty(str)) return str;
		StringBuilder sb = new StringBuilder();
		char[] carr = str.trim().toCharArray();
		for (int i = 0; i < carr.length; i++) {
			if (!isWhiteSpace(carr[i])) sb.append(carr[i]);
		}
		return sb.toString();
	}

	/**
	 * collapses multiple whitespace characters into a single space. the whitespace returned is always a
	 * standard chr(32) .
	 * 
	 * @param str
	 * @return
	 */
	public static String collapseWhitespace(String str) {

		if (isEmpty(str)) return str;

		StringBuilder sb = new StringBuilder(str.length());
		boolean wasLastWs = false;
		char[] carr = str.trim().toCharArray();
		for (int i = 0; i < carr.length; i++) {
			if (isWhiteSpace(carr[i])) {
				if (wasLastWs) continue;

				sb.append(' ');
				wasLastWs = true;
			}
			else {
				sb.append(carr[i]);
				wasLastWs = false;
			}
		}

		return sb.toString();
	}

	public static String replaceLast(String str, char from, char to) {
		int index = str.lastIndexOf(from);
		if (index == -1) return str;
		return str.substring(0, index) + to + str.substring(index + 1);
	}

	public static String replaceLast(String str, String from, String to) {
		int index = str.lastIndexOf(from);
		if (index == -1) return str;
		return str.substring(0, index) + to + str.substring(index + from.length());
	}

	/**
	 * removes quotes(",') that wraps the string
	 * 
	 * @param string
	 * @return
	 */
	public static String removeQuotes(String string, boolean trim) {
		if (string == null) return string;
		if (trim) string = string.trim();
		if (string.length() < 2) return string;

		if ((StringUtil.startsWith(string, '"') && StringUtil.endsWith(string, '"')) || (StringUtil.startsWith(string, '\'') && StringUtil.endsWith(string, '\''))) {
			string = string.substring(1, string.length() - 1);
			if (trim) string = string.trim();
		}
		return string;
	}

	public static boolean isEmpty(Object obj, boolean trim) {
		if (obj == null) return true;
		if (obj instanceof String) return isEmpty((String) obj, trim);
		if (obj instanceof StringBuffer) return isEmpty((StringBuffer) obj, trim);
		if (obj instanceof StringBuilder) return isEmpty((StringBuilder) obj, trim);
		if (obj instanceof Collection.Key) return isEmpty(((Collection.Key) obj).getString(), trim);
		return false;
	}

	public static boolean isEmpty(Object obj) {
		if (obj == null) return true;
		if (obj instanceof CharSequence) return isEmpty(((CharSequence) obj));
		if (obj instanceof Collection.Key) return isEmpty(((Collection.Key) obj).getString());
		return false;
	}

	public static boolean isEmpty(StringBuffer sb, boolean trim) {
		if (trim) return sb == null || sb.toString().trim().length() == 0;
		return sb == null || sb.length() == 0;
	}

	public static boolean isEmpty(StringBuilder sb, boolean trim) {
		if (trim) return sb == null || sb.toString().trim().length() == 0;
		return sb == null || sb.length() == 0;
	}

	public static boolean isEmpty(StringBuffer sb) {
		return sb == null || sb.length() == 0;
	}

	public static boolean isEmpty(StringBuilder sb) {
		return sb == null || sb.length() == 0;
	}

	public static String removeStarting(String str, String sub) {
		if (isEmpty(str) || isEmpty(sub) || !str.startsWith(sub)) return str;
		return str.substring(sub.length());
	}

	public static String removeStartingIgnoreCase(String str, String sub) {
		if (isEmpty(sub) || !startsWithIgnoreCase(str, sub)) return str;
		return str.substring(sub.length());
	}

	public static String[] merge(String str, String[] arr) {
		String[] narr = new String[arr.length + 1];
		narr[0] = str;
		for (int i = 0; i < arr.length; i++) {
			narr[i + 1] = arr[i];
		}
		return narr;

	}

	public static int length(String str) {
		if (str == null) return 0;
		return str.length();
	}

	public static int length(String str, boolean trim) {
		if (str == null) return 0;
		return str.trim().length();
	}

	public static boolean hasUpperCase(String str) {
		if (isEmpty(str)) return false;
		return !str.equals(str.toLowerCase());
	}

	public static boolean contains(String str, String substr) {
		if (str == null) return false;
		return str.indexOf(substr) != -1;
	}

	public static boolean containsIgnoreCase(String str, String substr) {
		return indexOfIgnoreCase(str, substr) != -1;
	}

	public static String substringEL(String str, int index, String defaultValue) {
		if (str == null || index < 0 || index > str.length()) return defaultValue;
		return str.substring(index);
	}

	/**
	 * translate a string in camel notation to a string in hypen notation example: helloWorld ->
	 * hello-world
	 * 
	 * @param str
	 * @return
	 */
	public static String camelToHypenNotation(String str) {
		if (isEmpty(str)) return str;

		StringBuilder sb = new StringBuilder();
		// int len=str.length();
		char c;

		sb.append(Character.toLowerCase(str.charAt(0)));
		for (int i = 1; i < str.length(); i++) {
			c = str.charAt(i);
			if (Character.isUpperCase(c)) {
				sb.append('-');
				sb.append(Character.toLowerCase(c));
			}
			else sb.append(c);
		}
		return sb.toString();
	}

	/**
	 * translate a string in hypen notation to a string in camel notation example: hello-world ->
	 * helloWorld
	 * 
	 * @param str
	 * @return
	 */
	public static String hypenToCamelNotation(String str) {
		if (isEmpty(str)) return str;

		StringBuilder sb = new StringBuilder();
		int len = str.length();
		char c;

		for (int i = 0; i < str.length(); i++) {
			c = str.charAt(i);
			if (c == '-') {
				if (len > ++i) sb.append(Character.toUpperCase(str.charAt(i)));
			}
			else sb.append(c);
		}
		return sb.toString();
	}

	public static boolean isAscii(String str) {
		if (str == null) return false;

		for (int i = str.length() - 1; i >= 0; i--) {
			if (str.charAt(i) > 127) return false;
		}
		return true;
	}

	/**
	 * returns true if all characters in the string are letters
	 *
	 * @param str
	 * @return
	 */
	public static boolean isAllAlpha(String str) {

		if (str == null) return false;

		for (int i = str.length() - 1; i >= 0; i--) {

			if (!Character.isLetter(str.charAt(i))) return false;
		}

		return true;
	}

	/**
	 * returns true if the input string has letters and they are all UPPERCASE
	 *
	 * @param str
	 * @return
	 */
	public static boolean isAllUpperCase(String str) {

		if (str == null) return false;

		boolean hasLetters = false;
		char c;

		for (int i = str.length() - 1; i >= 0; i--) {

			c = str.charAt(i);
			if (Character.isLetter(c)) {

				if (!Character.isUpperCase(c)) return false;

				hasLetters = true;
			}
		}

		return hasLetters;
	}

	public static boolean isWhiteSpace(String str) {
		if (str == null) return false;
		for (int i = str.length() - 1; i >= 0; i--) {
			if (!isWhiteSpace(str.charAt(i))) return false;
		}
		return true;
	}

	/**
	 * this method works different from the regular substring method, the regular substring method takes
	 * startIndex and endIndex as second and third argument, this method takes offset and length
	 * 
	 * @param str
	 * @param off
	 * @param len
	 * @return
	 */
	public static String substring(String str, int off, int len) {
		return str.substring(off, off + len);
	}

	public static String insertAt(String str, CharSequence substring, int pos) {

		if (isEmpty(substring)) return str;

		int len = str.length();

		StringBuilder sb = new StringBuilder(len + substring.length());

		if (pos > len) pos = len;

		if (pos > 0) sb.append(str.substring(0, pos));

		sb.append(substring);
		sb.append(str.substring(pos));

		return sb.toString();
	}

	/**
	 * this is the public entry point for the replaceMap() method
	 * 
	 * @param input - the string on which the replacements should be performed.
	 * @param map - a java.util.Map with key/value pairs where the key is the substring to find and the
	 *            value is the substring with which to replace the matched key
	 * @param ignoreCase - if true then matches will not be case sensitive
	 * @return
	 * @throws PageException
	 */
	public static String replaceMap(String input, Map map, boolean ignoreCase) throws PageException {
		// if (doResolveInternals) map = resolveInternals(map, ignoreCase, 0);

		CharSequence result = input;
		Iterator<Map.Entry> it = map.entrySet().iterator();
		Map.Entry e;
		Map<Pos, String> positions = new LinkedHashMap<>();
		String k, v;
		List<Pos> tmp;
		while (it.hasNext()) {
			e = it.next();
			k = Caster.toString(e.getKey());
			v = Caster.toString(e.getValue());
			tmp = new ArrayList<Pos>();
			result = _replace(result.toString(), k, placeholder(k), false, ignoreCase, tmp);

			for (Pos pos: tmp) {
				positions.put(pos, v);
			}
		}
		if (result instanceof StringBuilder) {
			StringBuilder sb = (StringBuilder) result;
			List<Map.Entry<Pos, String>> list = new ArrayList<Map.Entry<Pos, String>>(positions.entrySet());
			// <Map.Entry<Integer,String>>
			Collections.sort(list, new Comparator<Map.Entry<Pos, String>>() {
				@Override
				public int compare(Map.Entry<Pos, String> a, Map.Entry<Pos, String> b) {
					return Integer.compare(b.getKey().position, a.getKey().position);
				}
			});
			for (Map.Entry<Pos, String> entry: list) {
				sb.delete(entry.getKey().position, entry.getKey().position + entry.getKey().len);
				sb.insert(entry.getKey().position, entry.getValue());
			}
			return sb.toString();
		}
		return result.toString();
	}

	private static String placeholder(String str) {
		int count = str == null ? 0 : str.length();
		if (count == 0) return "";

		char r = (char) 0xFFFF;
		// r = '_';
		char[] carr = new char[count];
		for (int i = 0; i < count; i++) {
			carr[i] = r;
		}
		return new String(carr);
	}

	/*
	 * public static void main(String[] args) throws PageException { Map<String, String> map = new
	 * HashMap<>(); map.put("target", "!target!"); map.put("replace", "er"); map.put("susi", "Susanne");
	 * print.e(
	 * replaceMap("I want replace replace to add 1 underscore with struct-replace... 'target' replace",
	 * map, false));
	 * 
	 * map = new HashMap<>(); map.put("Susi", "Sorglos"); map.put("Sorglos", "Susi");
	 * print.e(replaceMap("Susi Sorglos foehnte ihr Haar", map, false));
	 * 
	 * }
	 */

	public static String unwrap(String str) {
		if (StringUtil.isEmpty(str)) return "";
		str = str.trim();
		if ((startsWith(str, '"') || startsWith(str, (char) 8220)) && (endsWith(str, '"') || endsWith(str, (char) 8221))) // #8220 and #8221 are left and right "double quotes"
			str = str.substring(1, str.length() - 1);
		if (startsWith(str, '\'') && endsWith(str, '\'')) str = str.substring(1, str.length() - 1);
		return str;
	}

	public static String toStringNative(Object obj, String defaultValue) {
		return obj == null ? defaultValue : obj.toString();
	}

	public static String emptyAsNull(String str, boolean trim) {
		if (isEmpty(str, trim)) return null;
		return str;
	}
}