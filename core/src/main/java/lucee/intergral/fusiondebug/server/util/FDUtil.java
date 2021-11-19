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
package lucee.intergral.fusiondebug.server.util;

public class FDUtil {

	/**
	 * replace the last occurrence of from with to
	 * 
	 * @param str
	 * @param from
	 * @param to
	 * @return changed string
	 */
	private static String replaceLast(String str, char from, char to) {
		int index = str.lastIndexOf(from);
		if (index == -1) return str;
		return str.substring(0, index) + to + str.substring(index + 1);
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
	 * translate a string to a valid variable string
	 * 
	 * @param str string to translate
	 * @return translated String
	 */
	private static String toVariableName(String str) {

		StringBuffer rtn = new StringBuffer();
		char[] chars = str.toCharArray();
		long changes = 0;
		boolean doCorrect = true;
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (i == 0 && (c >= '0' && c <= '9')) rtn.append("_" + c);
			else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_' || c == '$') rtn.append(c);
			else {
				doCorrect = false;
				rtn.append('_');
				changes += (c * (i + 1));
			}
		}

		if (changes > 0) rtn.append(changes);

		if (doCorrect) return correctReservedWord(rtn.toString());
		return rtn.toString();
	}

	/**
	 * creates a classbane from give source path
	 * 
	 * @param str
	 * @return
	 */
	public static String toClassName(String str) {
		StringBuffer javaName = new StringBuffer();
		String[] arr = lucee.runtime.type.util.ListUtil.listToStringArray(str, '/');

		for (int i = 0; i < arr.length; i++) {
			if (i == (arr.length - 1)) arr[i] = replaceLast(arr[i], '.', '$');
			if (i != 0) javaName.append('.');
			javaName.append(toVariableName(arr[i]));
		}
		return javaName.toString().toLowerCase();
	}
}