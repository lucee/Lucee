/**
 *
 * Copyright (c) 2026, Lucee Association Switzerland. All rights reserved.
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
package lucee.runtime.interpreter;

/**
 * Validates a string against the JSON grammar of RFC 8259, and nothing else. In contrast to the
 * {@link JSONExpressionInterpreter} it does not evaluate anything and does not accept any
 * extension: object keys and strings must be double quoted, no comments, no trailing commas, no
 * hex or leading-zero numbers, no control characters in strings.
 */
public final class StrictJSONValidator {

	private final String str;
	private final int len;
	private int pos;

	// one entry per open container, true for an object, false for an array
	private boolean[] stack = new boolean[32];
	private int depth;

	private StrictJSONValidator(String str) {
		this.str = str;
		this.len = str.length();
	}

	public static boolean isValid(String str) {
		if (str == null) return false;
		return new StrictJSONValidator(str).validate();
	}

	private boolean validate() {
		// iterative, so deeply nested input cannot cause a stack overflow
		while (true) {
			// expect a value
			skipWhitespace();
			if (pos >= len) return false;
			char c = str.charAt(pos);
			boolean complete = true; // false when we only opened a container that has content
			if (c == '{') {
				pos++;
				push(true);
				skipWhitespace();
				if (pos < len && str.charAt(pos) == '}') {
					pos++;
					depth--;
				}
				else {
					if (!member()) return false;
					complete = false;
				}
			}
			else if (c == '[') {
				pos++;
				push(false);
				skipWhitespace();
				if (pos < len && str.charAt(pos) == ']') {
					pos++;
					depth--;
				}
				else complete = false;
			}
			else if (!scalar(c)) return false;

			if (!complete) continue; // the first element follows

			// a value is complete, look at what follows it
			boolean next = false;
			while (depth > 0) {
				skipWhitespace();
				if (pos >= len) return false;
				char d = str.charAt(pos);
				if (d == ',') {
					pos++;
					skipWhitespace();
					if (stack[depth - 1] && !member()) return false;
					next = true;
					break;
				}
				if (d == (stack[depth - 1] ? '}' : ']')) {
					pos++;
					depth--;
				}
				else return false;
			}
			if (next) continue;
			skipWhitespace();
			return pos == len;
		}
	}

	private boolean scalar(char c) {
		switch (c) {
		case '"':
			return string();
		case 't':
			return literal("true");
		case 'f':
			return literal("false");
		case 'n':
			return literal("null");
		default:
			return number();
		}
	}

	// "key" ws : ws
	private boolean member() {
		if (pos >= len || str.charAt(pos) != '"') return false;
		if (!string()) return false;
		skipWhitespace();
		if (pos >= len || str.charAt(pos) != ':') return false;
		pos++;
		skipWhitespace();
		return true;
	}

	private void push(boolean isObject) {
		if (depth == stack.length) {
			boolean[] tmp = new boolean[stack.length * 2];
			System.arraycopy(stack, 0, tmp, 0, depth);
			stack = tmp;
		}
		stack[depth++] = isObject;
	}

	private boolean string() {
		pos++; // opening quote
		while (pos < len) {
			char c = str.charAt(pos++);
			if (c == '"') return true;
			if (c < 0x20) return false;
			if (c == '\\') {
				if (pos >= len) return false;
				char e = str.charAt(pos++);
				switch (e) {
				case '"':
				case '\\':
				case '/':
				case 'b':
				case 'f':
				case 'n':
				case 'r':
				case 't':
					break;
				case 'u':
					if (pos + 4 > len) return false;
					for (int i = 0; i < 4; i++) {
						if (!isHex(str.charAt(pos++))) return false;
					}
					break;
				default:
					return false;
				}
			}
		}
		return false; // not closed
	}

	private boolean number() {
		if (pos < len && str.charAt(pos) == '-') pos++;
		if (pos >= len) return false;
		char c = str.charAt(pos);
		if (c == '0') pos++;
		else if (c >= '1' && c <= '9') skipDigits();
		else return false;

		if (pos < len && str.charAt(pos) == '.') {
			pos++;
			if (skipDigits() == 0) return false;
		}
		if (pos < len && (str.charAt(pos) == 'e' || str.charAt(pos) == 'E')) {
			pos++;
			if (pos < len && (str.charAt(pos) == '+' || str.charAt(pos) == '-')) pos++;
			if (skipDigits() == 0) return false;
		}
		return true;
	}

	private int skipDigits() {
		int start = pos;
		while (pos < len && str.charAt(pos) >= '0' && str.charAt(pos) <= '9')
			pos++;
		return pos - start;
	}

	private boolean literal(String lit) {
		if (!str.startsWith(lit, pos)) return false;
		pos += lit.length();
		return true;
	}

	private void skipWhitespace() {
		while (pos < len) {
			char c = str.charAt(pos);
			if (c != ' ' && c != '\t' && c != '\n' && c != '\r') break;
			pos++;
		}
	}

	private static boolean isHex(char c) {
		return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
	}
}
