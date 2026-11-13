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

package lucee.runtime.sql.old;

public final class ParseException extends Exception {

	public ParseException(Token token, int ai[][], String as[]) {
		super("");
		eol = System.getProperty("line.separator", "\n");
		specialConstructor = true;
		currentToken = token;
		expectedTokenSequences = ai;
		tokenImage = as;
	}

	public ParseException() {
		eol = System.getProperty("line.separator", "\n");
		specialConstructor = false;
	}

	public ParseException(String s) {
		super(s);
		eol = System.getProperty("line.separator", "\n");
		specialConstructor = false;
	}

	/**
	 * The short, single line message: what was encountered and where. The list of expected tokens is
	 * available with {@link #getDetail()}.
	 */
	@Override
	public String getMessage() {
		if (!specialConstructor) return super.getMessage();
		int maxSequenceLength = getMaxSequenceLength();

		StringBuilder sb = new StringBuilder("Encountered \"");
		Token token = currentToken.next;
		for (int l = 0; l < maxSequenceLength; l++) {
			if (l != 0) sb.append(' ');
			if (token.kind == 0) {
				sb.append(tokenImage[0]);
				break;
			}
			sb.append(add_escapes(token.image));
			token = token.next;
		}

		sb.append("\" at line ").append(currentToken.next.beginLine).append(", column ").append(currentToken.next.beginColumn).append('.');
		return sb.toString();
	}

	/**
	 * The list of expected tokens ("Was expecting one of: ..."), or null if this exception does not
	 * carry one.
	 */
	public String getDetail() {
		if (!specialConstructor) return null;

		StringBuilder sb = new StringBuilder(expectedTokenSequences.length == 1 ? "Was expecting:" : "Was expecting one of:");
		for (int j = 0; j < expectedTokenSequences.length; j++) {
			sb.append(eol).append("    ");
			for (int k = 0; k < expectedTokenSequences[j].length; k++) {
				sb.append(tokenImage[expectedTokenSequences[j][k]]).append(' ');
			}
			if (expectedTokenSequences[j][expectedTokenSequences[j].length - 1] != 0) sb.append("...");
		}
		return sb.toString();
	}

	private int getMaxSequenceLength() {
		int max = 0;
		for (int j = 0; j < expectedTokenSequences.length; j++) {
			if (max < expectedTokenSequences[j].length) max = expectedTokenSequences[j].length;
		}
		return max;
	}

	protected String add_escapes(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c;
			switch (s.charAt(i)) {
			case 0: // '\0'
				break;

			case 8: // '\b'
				sb.append("\\b");
				break;

			case 9: // '\t'
				sb.append("\\t");
				break;

			case 10: // '\n'
				sb.append("\\n");
				break;

			case 12: // '\f'
				sb.append("\\f");
				break;

			case 13: // '\r'
				sb.append("\\r");
				break;

			case 34: // '"'
				sb.append("\\\"");
				break;

			case 39: // '\''
				sb.append("\\'");
				break;

			case 92: // '\\'
				sb.append("\\\\");
				break;

			default:
				if ((c = s.charAt(i)) < ' ' || c > '~') {
					String s1 = "0000" + Integer.toString(c, 16);
					sb.append("\\u" + s1.substring(s1.length() - 4, s1.length()));
				}
				else {
					sb.append(c);
				}
				break;
			}
		}

		return sb.toString();
	}

	protected boolean specialConstructor;
	public Token currentToken;
	public int expectedTokenSequences[][];
	public String tokenImage[];
	protected String eol;
}