/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import lucee.commons.io.res.Resource;

// FUTURE remove the class
public class Pack200Util {

	public static void pack2Jar(InputStream is, Resource jar, boolean closeIS) throws IOException {
		if (!jar.exists()) jar.createFile(false);
		pack2Jar(is, jar.getOutputStream(), closeIS, true);
	}

	public static void pack2Jar(Resource p200, OutputStream os, boolean closeOS) throws IOException {
		pack2Jar(p200.getInputStream(), os, true, closeOS);
	}

	public static void pack2Jar(Resource p200, Resource jar) throws IOException {
		if (!jar.exists()) jar.createFile(false);
		pack2Jar(p200.getInputStream(), jar.getOutputStream(), true, true);
	}

	public static void pack2Jar(File p200, File jar) throws IOException {
		if (!jar.exists()) jar.createNewFile();
		pack2Jar(new FileInputStream(p200), new FileOutputStream(jar), true, true);
	}

	public static void jar2pack(InputStream is, Resource p200, boolean closeIS) throws IOException {
		if (!p200.exists()) p200.createFile(false);
		jar2pack(is, p200.getOutputStream(), closeIS, true);
	}

	public static void jar2pack(Resource jar, OutputStream os, boolean closeOS) throws IOException {
		jar2pack(jar.getInputStream(), os, true, closeOS);
	}

	public static void jar2pack(Resource jar, Resource p200) throws IOException {
		if (!p200.exists()) p200.createFile(false);
		jar2pack(jar.getInputStream(), p200.getOutputStream(), true, true);
	}

	public static void jar2pack(File jar, File p200) throws IOException {
		if (!p200.exists()) p200.createNewFile();
		jar2pack(new FileInputStream(jar), new FileOutputStream(p200), true, true);
	}

	public static void pack2Jar(InputStream is, OutputStream os, boolean closeIS, boolean closeOS) throws IOException {
		throw new IOException("pack2Jar no longer supported!");
	}

	public static void jar2pack(InputStream is, OutputStream os, boolean closeIS, boolean closeOS) throws IOException {
		throw new IOException("pack2Jar no longer supported!");
	}

	public final static class DevNullOutputStream extends OutputStream implements Serializable {

		/**
		 * Constructor of the class
		 */
		private DevNullOutputStream() {}

		@Override
		public void close() {}

		@Override
		public void flush() {}

		@Override
		public void write(byte[] b, int off, int len) {}

		@Override
		public void write(byte[] b) {}

		@Override
		public void write(int b) {}

	}

}