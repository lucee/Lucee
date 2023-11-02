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

	/**
	 * @see javax.servlet.jsp.tagext.BodyContent#getReader()
	 */
	@Override
	public Reader getReader() {
		return new StringReader(charBuffer.toString());
	}

	/**
	 * @see javax.servlet.jsp.tagext.BodyContent#getString()
	 */
	@Override
	public String getString() {
		return charBuffer.toString();
	}

	/**
	 * @see javax.servlet.jsp.tagext.BodyContent#writeOut(java.io.Writer)
	 */
	@Override
	public void writeOut(Writer writer) throws IOException {
		charBuffer.writeOut(writer);
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#newLine()
	 */
	@Override
	public void newLine() {
		println();
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#print(boolean)
	 */
	@Override
	public void print(boolean arg) {
		print(arg ? "true" : "false");
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#print(char)
	 */
	@Override
	public void print(char arg) {
		charBuffer.append(String.valueOf(arg));
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#print(int)
	 */
	@Override
	public void print(int arg) {
		charBuffer.append(String.valueOf(arg));
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#print(long)
	 */
	@Override
	public void print(long arg) {
		charBuffer.append(String.valueOf(arg));
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#print(float)
	 */
	@Override
	public void print(float arg) {
		charBuffer.append(String.valueOf(arg));
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#print(double)
	 */
	@Override
	public void print(double arg) {
		charBuffer.append(String.valueOf(arg));
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#print(char[])
	 */
	@Override
	public void print(char[] arg) {
		charBuffer.append(arg);
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#print(java.lang.String)
	 */
	@Override
	public void print(String arg) {
		charBuffer.append(arg);
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#print(java.lang.Object)
	 */
	@Override
	public void print(Object arg) {
		charBuffer.append(String.valueOf(arg));
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println()
	 */
	@Override
	public void println() {
		charBuffer.append("\n");
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println(boolean)
	 */
	@Override
	public void println(boolean arg) {
		print(arg);
		println();
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println(char)
	 */
	@Override
	public void println(char arg) {
		print(arg);
		println();
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println(int)
	 */
	@Override
	public void println(int arg) {
		print(arg);
		println();
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println(long)
	 */
	@Override
	public void println(long arg) {
		print(arg);
		println();
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println(float)
	 */
	@Override
	public void println(float arg) {
		print(arg);
		println();
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println(double)
	 */
	@Override
	public void println(double arg) {
		print(arg);
		println();
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println(char[])
	 */
	@Override
	public void println(char[] arg) {
		print(arg);
		println();
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println(java.lang.String)
	 */
	@Override
	public void println(String arg) {
		print(arg);
		println();
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println(java.lang.Object)
	 */
	@Override
	public void println(Object arg) {
		print(arg);
		println();
	}

	/**
	 * @throws IOException
	 * @see javax.servlet.jsp.JspWriter#clear()
	 */
	@Override
	public void clear() throws IOException {
		charBuffer.clear();
		enclosingWriter.clear();
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#clearBuffer()
	 */
	@Override
	public void clearBuffer() {
		charBuffer.clear();
	}

	/**
	 * @see java.io.Writer#flush()
	 */
	@Override
	public void flush() throws IOException {
		enclosingWriter.write(charBuffer.toCharArray());
		charBuffer.clear();
	}

	/**
	 * @see java.io.Writer#close()
	 */
	@Override
	public void close() throws IOException {
		flush();
		enclosingWriter.close();
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#getRemaining()
	 */
	@Override
	public int getRemaining() {
		return bufferSize - charBuffer.size();
	}

	/**
	 * @see java.io.Writer#write(char[], int, int)
	 */
	@Override
	public void write(char[] cbuf, int off, int len) {
		charBuffer.append(cbuf, off, len);
	}

	/**
	 * @see java.io.Writer#write(char[])
	 */
	@Override
	public void write(char[] cbuf) {
		charBuffer.append(cbuf);
	}

	/**
	 * @see java.io.Writer#write(int)
	 */
	@Override
	public void write(int c) {
		print(c);
	}

	/**
	 * @see java.io.Writer#write(java.lang.String, int, int)
	 */
	@Override
	public void write(String str, int off, int len) {
		charBuffer.append(str, off, len);
	}

	/**
	 * @see java.io.Writer#write(java.lang.String)
	 */
	@Override
	public void write(String str) {
		charBuffer.append(str);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return charBuffer.toString();
	}

	/**
	 * @see javax.servlet.jsp.tagext.BodyContent#clearBody()
	 */
	@Override
	public void clearBody() {
		charBuffer.clear();
	}

	/**
	 * @see javax.servlet.jsp.tagext.BodyContent#getEnclosingWriter()
	 */
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

	/**
	 * @see javax.servlet.jsp.JspWriter#getBufferSize()
	 */
	@Override
	public int getBufferSize() {
		return charBuffer.size();
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#isAutoFlush()
	 */
	@Override
	public boolean isAutoFlush() {
		return super.isAutoFlush();
	}

	/**
	 * @see java.io.Writer#append(java.lang.CharSequence)
	 */
	@Override
	public Writer append(CharSequence csq) throws IOException {
		write(csq.toString());
		return this;
	}

	/**
	 * @see java.io.Writer#append(java.lang.CharSequence, int, int)
	 */
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