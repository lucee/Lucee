/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
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
 */
package lucee.runtime.writer;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lucee.runtime.PageContext;

/**
 * LDEV-1524 JSP Writer that removes empty lines and trailing whitespace from the given content, but
 * keeps the indentation and the whitespace within a line. Content within the tags &lt;CODE&gt;
 * &lt;PRE&gt; and &lt;TEXTAREA&gt; is not touched.
 */
public final class CFMLWriterWSLight extends CFMLWriterImpl implements WhiteSpaceWriter {

	public static final char CHAR_NL = '\n';
	public static final char CHAR_RETURN = '\r';

	private static final char CHAR_GT = '>';
	private static final char CHAR_LT = '<';
	private static final char CHAR_SL = '/';
	private static final String[] EXCLUDE_TAGS = { "code", "pre", "textarea" };

	private static int minTagLen = 64;

	private int[] depths;
	private int depthSum = 0;
	// true as long as there is no content on the current line
	private boolean lineStart = true;
	// whitespace (not line breaks) held back until we know if it is indentation/inner whitespace or trailing whitespace
	private final StringBuilder pending = new StringBuilder();
	// possible start of one of the excluded tags
	private final StringBuilder sb = new StringBuilder();

	static {
		for (String s: EXCLUDE_TAGS)
			if (s.length() < minTagLen) minTagLen = s.length();

		minTagLen++; // add 1 for LessThan symbol
	}

	public CFMLWriterWSLight(PageContext pc, HttpServletRequest req, HttpServletResponse rsp, int bufferSize, boolean autoFlush, boolean closeConn, boolean showVersion,
			boolean contentLength) {
		super(pc, req, rsp, bufferSize, autoFlush, closeConn, showVersion, contentLength);
		depths = new int[EXCLUDE_TAGS.length];
	}

	synchronized void printBuffer() throws IOException {
		int len = sb.length();
		if (len > 0) {
			char[] chars = new char[len];
			sb.getChars(0, len, chars, 0);
			sb.setLength(0);
			super.write(chars, 0, chars.length);
		}
	}

	private void printPending() throws IOException {
		int len = pending.length();
		if (len > 0) {
			char[] chars = new char[len];
			pending.getChars(0, len, chars, 0);
			pending.setLength(0);
			super.write(chars, 0, chars.length);
		}
	}

	/**
	 * called before something that is content is written
	 */
	private void beforeContent() throws IOException {
		printBuffer();
		printPending();
		lineStart = false;
	}

	@Override
	void flushWhitespaceBuffer() throws IOException {
		printBuffer();
		printPending();
	}

	@Override
	void onAdopted() {
		lineStart = atLineStart();
	}

	void printBufferEL() {
		if (sb.length() > 0) {
			try {
				printBuffer();
			}
			catch (IOException e) {
			}
		}
	}

	private boolean addToBuffer(char c) throws IOException {
		int len = sb.length();
		if (len == 0 && c != CHAR_LT) return false; // buffer must starts with '<'

		sb.append(c);
		if (++len >= minTagLen) {
			boolean isClosingTag = (len >= 2 && sb.charAt(1) == CHAR_SL);
			String substr;
			if (isClosingTag) substr = sb.substring(2);
			else substr = sb.substring(1);
			for (int i = 0; i < EXCLUDE_TAGS.length; i++) {
				if (substr.equalsIgnoreCase(EXCLUDE_TAGS[i])) {
					if (isClosingTag) {
						if (--depths[i] < 0) depths[i] = 0;
						depthCalc();
						printBuffer();
					}
					else {
						depths[i]++;
						depthCalc();
					}
				}
			}
		}
		return true;
	}

	private void depthCalc() {
		int sum = 0;
		for (int d: depths)
			sum += d;
		depthSum = sum;
	}

	@Override
	public void print(char c) throws IOException {
		if (Character.isWhitespace(c)) {
			if (sb.length() > 0) printBuffer(); // the buffer never contains whitespace
			if (depthSum > 0) { // within a tag we do not touch
				printPending();
				lineStart = false;
				super.print(c);
				return;
			}
			if (c == CHAR_RETURN) return;
			if (c == CHAR_NL) {
				pending.setLength(0); // trailing whitespace or whitespace of an empty line
				if (!lineStart) {
					super.print(c);
					lineStart = true;
				}
				return;
			}
			pending.append(c);
			return;
		}

		if (c == CHAR_GT && sb.length() > 0) printBuffer(); // the buffer never contains ">"
		printPending();
		lineStart = false;
		if (!addToBuffer(c)) super.print(c);
	}

	@Override
	public void writeRaw(String str) throws IOException {
		beforeContent();
		super.write(str);
	}

	@Override
	public void initHeaderBuffer() throws IOException {
		resetHTMLHead();
	}

	@Override
	public final void clear() throws IOException {
		printBuffer();
		pending.setLength(0);
		super.clear();
		lineStart = true;
	}

	@Override
	public final void clearBuffer() {
		printBufferEL();
		pending.setLength(0);
		super.clearBuffer();
		lineStart = true;
	}

	@Override
	public final void close() throws IOException {
		printBuffer();
		pending.setLength(0); // trailing whitespace
		super.close();
	}

	@Override
	public final void flush() throws IOException {
		printBuffer();
		super.flush();
	}

	@Override
	public final int getRemaining() {
		printBufferEL();
		return super.getRemaining();
	}

	@Override
	public final void newLine() throws IOException {
		print(CHAR_NL);
	}

	@Override
	public final void print(boolean b) throws IOException {
		beforeContent();
		super.print(b);
	}

	@Override
	public final void print(char[] chars) throws IOException {
		write(chars, 0, chars.length);
	}

	@Override
	public final void print(double d) throws IOException {
		beforeContent();
		super.print(d);
	}

	@Override
	public final void print(float f) throws IOException {
		beforeContent();
		super.print(f);
	}

	@Override
	public final void print(int i) throws IOException {
		beforeContent();
		super.print(i);
	}

	@Override
	public final void print(long l) throws IOException {
		beforeContent();
		super.print(l);
	}

	@Override
	public final void print(Object obj) throws IOException {
		print(obj.toString());
	}

	@Override
	public final void print(String str) throws IOException {
		write(str.toCharArray(), 0, str.length());
	}

	@Override
	public final void println() throws IOException {
		print(CHAR_NL);
	}

	@Override
	public final void println(boolean b) throws IOException {
		print(b);
		print(CHAR_NL);
	}

	@Override
	public final void println(char c) throws IOException {
		print(c);
		print(CHAR_NL);
	}

	@Override
	public final void println(char[] chars) throws IOException {
		write(chars, 0, chars.length);
		print(CHAR_NL);
	}

	@Override
	public final void println(double d) throws IOException {
		print(d);
		print(CHAR_NL);
	}

	@Override
	public final void println(float f) throws IOException {
		print(f);
		print(CHAR_NL);
	}

	@Override
	public final void println(int i) throws IOException {
		print(i);
		print(CHAR_NL);
	}

	@Override
	public final void println(long l) throws IOException {
		print(l);
		print(CHAR_NL);
	}

	@Override
	public final void println(Object obj) throws IOException {
		println(obj.toString());
	}

	@Override
	public final void println(String str) throws IOException {
		print(str);
		print(CHAR_NL);
	}

	@Override
	public final void write(char[] chars, int off, int len) throws IOException {
		for (int i = off; i < off + len; i++) {
			print(chars[i]);
		}
	}

	@Override
	public final void write(String str, int off, int len) throws IOException {
		write(str.toCharArray(), off, len);
	}

	@Override
	public final void write(char[] chars) throws IOException {
		write(chars, 0, chars.length);
	}

	@Override
	public final void write(int i) throws IOException {
		print((char) i);
	}

	@Override
	public final void write(String str) throws IOException {
		write(str.toCharArray(), 0, str.length());
	}
}
