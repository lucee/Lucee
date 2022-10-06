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
package lucee.runtime.regex;

import java.util.Map;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Substitution;
import org.apache.oro.text.regex.Util;

import lucee.commons.collection.MapFactory;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Constants;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

/**
 * 
 */
final class Perl5Util {

	private static Map<String, Pattern> patterns = MapFactory.<String, Pattern>getConcurrentMap();

	/**
	 * return index of the first occurence of the pattern in input text
	 * 
	 * @param strPattern pattern to search
	 * @param strInput text to search pattern
	 * @param offset
	 * @param caseSensitive
	 * @return position of the first occurence
	 * @throws MalformedPatternException
	 */
	public static Object indexOf(String strPattern, String strInput, int offset, boolean caseSensitive, boolean matchAll, boolean multiLine) throws MalformedPatternException {
		// Perl5Compiler compiler = new Perl5Compiler();
		PatternMatcherInput input = new PatternMatcherInput(strInput);
		Perl5Matcher matcher = new Perl5Matcher();

		int compileOptions = caseSensitive ? 0 : Perl5Compiler.CASE_INSENSITIVE_MASK;
		compileOptions += multiLine ? Perl5Compiler.MULTILINE_MASK : Perl5Compiler.SINGLELINE_MASK;
		if (offset < 1) offset = 1;

		Pattern pattern = getPattern(strPattern, compileOptions);
		// Pattern pattern = compiler.compile(strPattern,compileOptions);

		if (offset <= strInput.length()) input.setCurrentOffset(offset - 1);

		if (offset <= strInput.length()) {
			Array matches = new ArrayImpl();
			while (matcher.contains(input, pattern)) {
				int match = matcher.getMatch().beginOffset(0) + 1;
				if (!matchAll) {
					return new Double(match);
				}

				matches.appendEL(match);
			}

			if (matches.size() != 0) {
				return matches;
			}
		}
		return 0;
	}

	public static Object match(String strPattern, String strInput, int offset, boolean caseSensitive, boolean matchAll, boolean multiLine) throws MalformedPatternException {

		Perl5Matcher matcher = new Perl5Matcher();
		PatternMatcherInput input = new PatternMatcherInput(strInput);

		int compileOptions = caseSensitive ? 0 : Perl5Compiler.CASE_INSENSITIVE_MASK;
		compileOptions += multiLine ? Perl5Compiler.MULTILINE_MASK : Perl5Compiler.SINGLELINE_MASK;
		if (offset < 1) offset = 1;

		Pattern pattern = getPattern(strPattern, compileOptions);
		if (offset <= strInput.length()) input.setCurrentOffset(offset - 1);

		Array rtn = new ArrayImpl();
		MatchResult result;
		while (matcher.contains(input, pattern)) {
			result = matcher.getMatch();
			if (!matchAll) return result.toString();
			rtn.appendEL(result.toString());
		}
		if (!matchAll) return "";
		return rtn;
	}

	/**
	 * find occurence of a pattern in a string (same like indexOf), but dont return first ocurence , it
	 * return struct with all information
	 * 
	 * @param strPattern
	 * @param strInput
	 * @param offset
	 * @param caseSensitive
	 * @return
	 * @throws MalformedPatternException
	 */
	public static Object find(String strPattern, String strInput, int offset, boolean caseSensitive, boolean matchAll, boolean multiLine) throws MalformedPatternException {
		Perl5Matcher matcher = new Perl5Matcher();
		PatternMatcherInput input = new PatternMatcherInput(strInput);
		Array matches = new ArrayImpl();

		int compileOptions = caseSensitive ? 0 : Perl5Compiler.CASE_INSENSITIVE_MASK;
		compileOptions += multiLine ? Perl5Compiler.MULTILINE_MASK : Perl5Compiler.SINGLELINE_MASK;
		if (offset < 1) offset = 1;

		Pattern pattern = getPattern(strPattern, compileOptions);

		if (offset <= strInput.length()) {
			input.setCurrentOffset(offset - 1);

			while (matcher.contains(input, pattern)) {
				Struct matchStruct = getMatchStruct(matcher.getMatch(), strInput);
				if (!matchAll) {
					return matchStruct;
				}

				matches.appendEL(matchStruct);
			}

			if (matches.size() != 0) {
				return matches;
			}
		}

		Array posArray = new ArrayImpl();
		Array lenArray = new ArrayImpl();
		Array matchArray = new ArrayImpl();
		posArray.appendEL(Constants.INTEGER_0);
		lenArray.appendEL(Constants.INTEGER_0);
		matchArray.appendEL("");

		Struct struct = new StructImpl();
		struct.setEL("pos", posArray);
		struct.setEL("len", lenArray);
		struct.setEL("match", matchArray);

		if (matchAll) {
			matches.appendEL(struct);
			return matches;
		}

		return struct;
	}

	public static Struct getMatchStruct(MatchResult result, String input) {
		int groupCount = result.groups();

		Array posArray = new ArrayImpl();
		Array lenArray = new ArrayImpl();
		Array matchArray = new ArrayImpl();
		int beginOff;
		int endOff;
		for (int i = 0; i < groupCount; i++) {
			beginOff = result.beginOffset(i);
			endOff = result.endOffset(i);
			if (beginOff == -1 && endOff == -1) {
				posArray.appendEL(Integer.valueOf(0));
				lenArray.appendEL(Integer.valueOf(0));
				matchArray.appendEL(null);
				continue;
			}
			posArray.appendEL(Integer.valueOf(beginOff + 1));
			lenArray.appendEL(Integer.valueOf(endOff - beginOff));
			matchArray.appendEL(input.substring(beginOff, endOff));
		}

		Struct struct = new StructImpl();
		struct.setEL("pos", posArray);
		struct.setEL("len", lenArray);
		struct.setEL("match", matchArray);

		return struct;
	}

	private static boolean _matches(String strPattern, String strInput) throws MalformedPatternException {
		Pattern pattern = new Perl5Compiler().compile(strPattern, Perl5Compiler.DEFAULT_MASK);
		PatternMatcherInput input = new PatternMatcherInput(strInput);
		return new Perl5Matcher().matches(input, pattern);
	}

	public static boolean matches(String strPattern, String strInput) throws PageException {
		try {
			return _matches(strPattern, strInput);
		}
		catch (MalformedPatternException e) {
			throw new ExpressionException("The provided pattern [" + strPattern + "] is invalid", e.getMessage());
		}

	}

	public static boolean matches(String strPattern, String strInput, boolean defaultValue) {
		try {
			return _matches(strPattern, strInput);
		}
		catch (MalformedPatternException e) {
			return defaultValue;
		}
	}

	private static Pattern getPattern(String strPattern, int type) throws MalformedPatternException {
		Object o = patterns.get(strPattern + type);
		if (o == null) {
			Pattern pattern = new Perl5Compiler().compile(strPattern, type);
			patterns.put(strPattern + type, pattern);
			return pattern;
		}
		return (Pattern) o;

	}

	/**
	 * replace the first/all occurence of given pattern
	 * 
	 * @param strInput text to search pattern
	 * @param strPattern pattern to search
	 * @param replacement text to replace with pattern
	 * @param caseSensitive
	 * @param replaceAll do replace all or only one
	 * @return transformed text
	 * @throws MalformedPatternException
	 */
	public static String replace(String strInput, String strPattern, String replacement, boolean caseSensitive, boolean replaceAll, boolean multiLine)
			throws MalformedPatternException {
		return _replace(strInput, strPattern, escape(replacement), caseSensitive, replaceAll, multiLine);
	}

	private static String _replace(String strInput, String strPattern, String replacement, boolean caseSensitive, boolean replaceAll, boolean multiLine)
			throws MalformedPatternException {

		int flag = (caseSensitive ? 0 : Perl5Compiler.CASE_INSENSITIVE_MASK) + (multiLine ? Perl5Compiler.MULTILINE_MASK : Perl5Compiler.SINGLELINE_MASK);
		Pattern pattern = getPattern(strPattern, flag);
		return Util.substitute(new Perl5Matcher(), pattern, new Perl5Substitution(replacement), strInput, replaceAll ? -1 : 1);
	}

	private static String escape(String replacement) throws MalformedPatternException {
		replacement = _replace(replacement, "\\\\", "\\\\\\\\", false, true, false);
		replacement = _escape(replacement);
		replacement = _replace(replacement, "\\\\\\\\(\\d)", "\\$$1", false, true, false);
		return replacement;
	}

	private static String _escape(String str) {
		StringBuffer sb = new StringBuffer();
		int len = str.length();
		char c;

		for (int i = 0; i < len; i++) {
			c = str.charAt(i);

			if ('+' == c) sb.append("\\+");
			else if ('?' == c) sb.append("\\?");
			else if ('$' == c) sb.append("\\$");
			else if ('^' == c) sb.append("\\^");
			else if ('\\' == c) {
				if (i + 1 < len) {
					char n = str.charAt(i + 1);
					if ('\\' == n) {
						if (i + 2 < len) {
							char nn = str.charAt(i + 2);
							char x = 0;
							if ('U' == nn) x = 'U';
							else if ('L' == nn) x = 'L';
							else if ('u' == nn) x = 'u';
							else if ('l' == nn) x = 'l';
							else if ('E' == nn) x = 'E';
							// else if('d'==nn) x='d';
							if (x != 0) {
								sb.append("\\" + x);
								i += 2;
								continue;
							}
						}
					}
				}
				sb.append(c);
			}
			else sb.append(c);
		}
		return sb.toString();
	}
}
