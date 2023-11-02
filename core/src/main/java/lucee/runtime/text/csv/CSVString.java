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
package lucee.runtime.text.csv;

import java.util.ArrayList;
import java.util.List;

import lucee.commons.lang.StringUtil;

public class CSVString {

	private static final char LF = 10;
	private static final char CR = 13;

	private char[] buffer;
	private int pos;
	private char delim;

	public CSVString(String input, char delim) {
		this.buffer = StringUtil.trim(input, true, false, input).toCharArray();
		this.delim = delim;
	}

	public List<List<String>> parse() {

		List<List<String>> result = new ArrayList<List<String>>();
		List<String> line = new ArrayList<String>();

		if (buffer.length == 0) return result;

		StringBuilder sb = new StringBuilder();
		char c;

		do {

			c = buffer[pos];
			if (c == '"' || c == '\'') {
				sb.append(fwdQuote(c));
			}
			else if (c == LF || c == CR) {
				if (c == CR && isNext(LF)) next();
				line.add(sb.toString().trim());
				sb = new StringBuilder();
				if (isValidLine(line)) result.add(line);
				line = new ArrayList<String>();
			}
			else if (c == delim) {

				line.add(sb.toString().trim());
				sb = new StringBuilder();
			}
			else sb.append(c);

			next();
		}
		while (pos < buffer.length);

		line.add(sb.toString());

		if (isValidLine(line)) result.add(line);
		return result;
	}

	/** forward pos until the end of quote */
	StringBuilder fwdQuote(char q) {

		StringBuilder sb = new StringBuilder();

		while (hasNext()) {

			next();
			sb.append(buffer[pos]);

			if (isCurr(q)) {
				if (isNext(q)) { // consecutive quote sign
					next();
				}
				else {
					break;
				}
			}
		}

		if (sb.length() > 0) sb.setLength(sb.length() - 1); // remove closing quote sign

		return sb;
	}

	void next() {

		pos++;
	}

	boolean hasNext() {

		return pos < (buffer.length - 1);
	}

	boolean isNext(char c) {

		if (!hasNext()) return false;

		return buffer[pos + 1] == c;
	}

	boolean isCurr(char c) {

		if (!isValidPos()) return false;

		return buffer[pos] == c;
	}

	boolean isValidPos() {

		return pos >= 0 && pos < buffer.length - 1;
	}

	boolean isValidLine(List<String> line) {

		for (String s: line) {

			if (!StringUtil.isEmpty(s, true)) return true;
		}

		return false;
	}

}