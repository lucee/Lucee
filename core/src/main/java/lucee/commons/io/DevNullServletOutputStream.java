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
package lucee.commons.io;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

/**
 * ServletOutputStream impl.
 */
public final class DevNullServletOutputStream extends ServletOutputStream {

	@Override
	public void close() {
	}

	@Override
	public void flush() {
	}

	@Override
	public void write(byte[] b, int off, int len) {
	}

	@Override
	public void write(byte[] b) {
	}

	@Override
	public void write(int b) {
	}

	@Override
	public void print(boolean b) {
	}

	@Override
	public void print(char c) {
	}

	@Override
	public void print(double d) {
	}

	@Override
	public void print(float f) {
	}

	@Override
	public void print(int i) {
	}

	@Override
	public void print(long l) {
	}

	@Override
	public void print(String str) {
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
	public void println(double d) {
	}

	@Override
	public void println(float f) {
	}

	@Override
	public void println(int i) {
	}

	@Override
	public void println(long l) {
	}

	@Override
	public void println(String str) {
	}

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public void setWriteListener(WriteListener arg0) {
	}

}