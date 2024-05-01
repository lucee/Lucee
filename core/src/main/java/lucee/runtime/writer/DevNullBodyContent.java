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
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import javax.servlet.jsp.tagext.BodyContent;

/**
 * BodyContent implementation that dont store input
 */
public final class DevNullBodyContent extends BodyContent {

	/**
	 * default constructor
	 */
	public DevNullBodyContent() {
		super(null);
	}

	@Override
	public Reader getReader() {
		return new StringReader("");
	}

	@Override
	public String getString() {
		return "";
	}

	@Override
	public void writeOut(Writer writer) {

	}

	@Override
	public void newLine() {

	}

	@Override
	public void print(boolean b) {
	}

	@Override
	public void print(char c) {
	}

	@Override
	public void print(int i) {
	}

	@Override
	public void print(long l) {
	}

	@Override
	public void print(float f) {
	}

	@Override
	public void print(double d) {
	}

	@Override
	public void print(char[] c) {
	}

	@Override
	public void print(String str) {
	}

	@Override
	public void print(Object o) {
	}

	@Override
	public void println() {
	}

	@Override
	public void println(boolean b) {
	}

	@Override
	public void println(char c) {
	}

	@Override
	public void println(int i) {
	}

	@Override
	public void println(long l) {
	}

	@Override
	public void println(float f) {
	}

	@Override
	public void println(double d) {
	}

	@Override
	public void println(char[] c) {
	}

	@Override
	public void println(String str) {
	}

	@Override
	public void println(Object o) {
	}

	@Override
	public void clear() {
	}

	@Override
	public void clearBuffer() {
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public int getRemaining() {
		return 0;
	}

	@Override
	public void write(char[] cbuf, int off, int len) {
	}

	@Override
	public void clearBody() {

	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public CFMLWriter getEnclosingWriter() {
		return null;
	}

	@Override
	public int getBufferSize() {
		return 0;
	}

	@Override
	public boolean isAutoFlush() {
		return false;
	}
}