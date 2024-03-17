/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;

import lucee.commons.lang.CharBuffer;

/**
 * implementation of the BodyContent
 */
public class BodyContentImpl extends BodyContent {

	CharBuffer charBuffer = new CharBuffer(128);
	JspWriter enclosingWriter;

	/**
	 * default constructor
	 * 
	 * @param jspWriter
	 */
	public BodyContentImpl(JspWriter jspWriter) {
		super(jspWriter);
		enclosingWriter = jspWriter;

	}

	/**
	 * initialize the BodyContent with the enclosing jsp writer
	 * 
	 * @param jspWriter
	 */
	public void init(JspWriter jspWriter) {
		enclosingWriter = jspWriter;
		clearBuffer();

	}

	@Override
	public Reader getReader() {
		return new StringReader(charBuffer.toString());
	}

	@Override
	public String getString() {
		return charBuffer.toString();
	}

	@Override
	public void writeOut(Writer writer) throws IOException {
		charBuffer.writeOut(writer);
	}

	@Override
	public void newLine() {
		println();
	}

	@Override
	public void print(boolean arg) {
		print(arg ? "true" : "false");
	}

	@Override
	public void print(char arg) {
		charBuffer.append(String.valueOf(arg));
	}

	@Override
	public void print(int arg) {
		charBuffer.append(String.valueOf(arg));
	}

	@Override
	public void print(long arg) {
		charBuffer.append(String.valueOf(arg));
	}

	@Override
	public void print(float arg) {
		charBuffer.append(String.valueOf(arg));
	}

	@Override
	public void print(double arg) {
		charBuffer.append(String.valueOf(arg));
	}

	@Override
	public void print(char[] arg) {
		charBuffer.append(arg);
	}

	@Override
	public void print(String arg) {
		charBuffer.append(arg);
	}

	@Override
	public void print(Object arg) {
		charBuffer.append(String.valueOf(arg));
	}

	@Override
	public void println() {
		charBuffer.append("\n");
	}

	@Override
	public void println(boolean arg) {
		print(arg);
		println();
	}

	@Override
	public void println(char arg) {
		print(arg);
		println();
	}

	@Override
	public void println(int arg) {
		print(arg);
		println();
	}

	@Override
	public void println(long arg) {
		print(arg);
		println();
	}

	@Override
	public void println(float arg) {
		print(arg);
		println();
	}

	@Override
	public void println(double arg) {
		print(arg);
		println();
	}

	@Override
	public void println(char[] arg) {
		print(arg);
		println();
	}

	@Override
	public void println(String arg) {
		print(arg);
		println();
	}

	@Override
	public void println(Object arg) {
		print(arg);
		println();
	}

	@Override
	public void clear() throws IOException {
		charBuffer.clear();
		enclosingWriter.clear();
	}

	@Override
	public void clearBuffer() {
		charBuffer.clear();
	}

	@Override
	public void flush() throws IOException {
		enclosingWriter.write(charBuffer.toCharArray());
		charBuffer.clear();
	}

	@Override
	public void close() throws IOException {
		flush();
		enclosingWriter.close();
	}

	@Override
	public int getRemaining() {
		return bufferSize - charBuffer.size();
	}

	@Override
	public void write(char[] cbuf, int off, int len) {
		charBuffer.append(cbuf, off, len);
	}

	@Override
	public void write(char[] cbuf) {
		charBuffer.append(cbuf);
	}

	@Override
	public void write(int c) {
		print(c);
	}

	@Override
	public void write(String str, int off, int len) {
		charBuffer.append(str, off, len);
	}

	@Override
	public void write(String str) {
		charBuffer.append(str);
	}

	@Override
	public String toString() {
		return charBuffer.toString();
	}

	@Override
	public void clearBody() {
		charBuffer.clear();
	}

	@Override
	public JspWriter getEnclosingWriter() {
		return enclosingWriter;
	}

	/**
	 * returns the inner char buffer
	 * 
	 * @return intern CharBuffer
	 */
	public CharBuffer getCharBuffer() {
		return charBuffer;
	}

	/**
	 * sets the inner Charbuffer
	 * 
	 * @param charBuffer
	 */
	public void setCharBuffer(CharBuffer charBuffer) {
		this.charBuffer = charBuffer;
	}

	@Override
	public int getBufferSize() {
		return charBuffer.size();
	}

	@Override
	public boolean isAutoFlush() {
		return super.isAutoFlush();
	}

	@Override
	public Writer append(CharSequence csq) throws IOException {
		write(csq.toString());
		return this;
	}

	@Override
	public Writer append(CharSequence csq, int start, int end) throws IOException {
		write(csq.subSequence(start, end).toString());
		return this;
	}

	/**
	 * @see java.io.Writer#append(char)
	 */
	@Override
	public Writer append(char c) throws IOException {
		write(c);
		return this;
	}

}