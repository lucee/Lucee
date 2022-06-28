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

/**
 * BodyContent implementation that dont store input
 */
public final class DevNullBodyContent extends BodyContent {

	private JspWriter enclosingWriter;

	/**
	 * default constructor
	 */
	public DevNullBodyContent() {
		super(null);
	}

	/**
	 * @see javax.servlet.jsp.tagext.BodyContent#getReader()
	 */
	@Override
	public Reader getReader() {
		return new StringReader("");
	}

	/**
	 * @see javax.servlet.jsp.tagext.BodyContent#getString()
	 */
	@Override
	public String getString() {
		return "";
	}

	/**
	 * 
	 * @see javax.servlet.jsp.tagext.BodyContent#writeOut(java.io.Writer)
	 */
	@Override
	public void writeOut(Writer writer) {

	}

	/**
	 * @see javax.servlet.jsp.JspWriter#newLine()
	 */
	@Override
	public void newLine() {

	}

	/**
	 * @see javax.servlet.jsp.JspWriter#print(boolean)
	 */
	@Override
	public void print(boolean b) {
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#print(char)
	 */
	@Override
	public void print(char c) {
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#print(int)
	 */
	@Override
	public void print(int i) {
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#print(long)
	 */
	@Override
	public void print(long l) {
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#print(float)
	 */
	@Override
	public void print(float f) {
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#print(double)
	 */
	@Override
	public void print(double d) {
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#print(char[])
	 */
	@Override
	public void print(char[] c) {
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#print(java.lang.String)
	 */
	@Override
	public void print(String str) {
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#print(java.lang.Object)
	 */
	@Override
	public void print(Object o) {
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println()
	 */
	@Override
	public void println() {
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println(boolean)
	 */
	@Override
	public void println(boolean b) {
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println(char)
	 */
	@Override
	public void println(char c) {
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println(int)
	 */
	@Override
	public void println(int i) {
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println(long)
	 */
	@Override
	public void println(long l) {
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println(float)
	 */
	@Override
	public void println(float f) {
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println(double)
	 */
	@Override
	public void println(double d) {
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println(char[])
	 */
	@Override
	public void println(char[] c) {
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println(java.lang.String)
	 */
	@Override
	public void println(String str) {
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println(java.lang.Object)
	 */
	@Override
	public void println(Object o) {
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#clear()
	 */
	@Override
	public void clear() {
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#clearBuffer()
	 */
	@Override
	public void clearBuffer() {
	}

	/**
	 * @see java.io.Writer#close()
	 */
	@Override
	public void close() throws IOException {
		if (enclosingWriter != null) enclosingWriter.close();
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#getRemaining()
	 */
	@Override
	public int getRemaining() {
		return 0;
	}

	/**
	 * @see java.io.Writer#write(char[], int, int)
	 */
	@Override
	public void write(char[] cbuf, int off, int len) {
	}

	/**
	 * @see javax.servlet.jsp.tagext.BodyContent#clearBody()
	 */
	@Override
	public void clearBody() {

	}

	/**
	 * @see java.io.Writer#flush()
	 */
	@Override
	public void flush() throws IOException {
		if (enclosingWriter != null) enclosingWriter.flush();
	}

	/**
	 * @see javax.servlet.jsp.tagext.BodyContent#getEnclosingWriter()
	 */
	@Override
	public JspWriter getEnclosingWriter() {
		return enclosingWriter;
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#getBufferSize()
	 */
	@Override
	public int getBufferSize() {
		return 0;
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#isAutoFlush()
	 */
	@Override
	public boolean isAutoFlush() {
		return false;
	}
}