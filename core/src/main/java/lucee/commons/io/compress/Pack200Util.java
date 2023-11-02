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
package lucee.commons.io.compress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.SortedMap;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.jar.Pack200.Packer;
import java.util.jar.Pack200.Unpacker;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import lucee.commons.io.DevNullOutputStream;
import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;

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
		File tmp = null;

		// we do this because Pack200 closes the stram
		if (!closeIS) {
			tmp = File.createTempFile("temp-pack200-file", "packgz");
			FileOutputStream tmpos = new FileOutputStream(tmp);
			IOUtil.copy(is, tmpos, false, true);
			is = new FileInputStream(tmp);
			closeIS = true;
		}

		Unpacker unpacker = Pack200.newUnpacker();

		SortedMap<String, String> p = unpacker.properties();
		p.put(Unpacker.DEFLATE_HINT, Unpacker.TRUE);

		is = new GZIPInputStream(is);
		JarOutputStream jos = null;
		try {
			jos = new JarOutputStream(os);
			unpacker.unpack(is, jos);
			jos.finish();
		}
		finally {
			if (closeIS && closeOS) IOUtil.close(is, jos);
			else {
				if (closeIS) IOUtil.close(is);
				if (closeOS) IOUtil.close(jos);
			}
			if (tmp != null && tmp.isFile()) tmp.delete();
		}
	}

	public static void jar2pack(InputStream is, OutputStream os, boolean closeIS, boolean closeOS) throws IOException {
		// Create the Packer object
		Packer packer = Pack200.newPacker();

		// Initialize the state by setting the desired properties
		Map p = packer.properties();
		// take more time choosing codings for better compression
		p.put(Packer.EFFORT, "7"); // default is "5"
		// use largest-possible archive segments (>10% better compression).
		p.put(Packer.SEGMENT_LIMIT, "-1");
		// reorder files for better compression.
		p.put(Packer.KEEP_FILE_ORDER, Packer.FALSE);
		// smear modification times to a single value.
		p.put(Packer.MODIFICATION_TIME, Packer.LATEST);
		// ignore all JAR deflation requests,
		// transmitting a single request to use "store" mode.
		p.put(Packer.DEFLATE_HINT, Packer.FALSE);
		// discard debug attributes
		p.put(Packer.CODE_ATTRIBUTE_PFX + "LineNumberTable", Packer.STRIP);
		// throw an error if an attribute is unrecognized
		p.put(Packer.UNKNOWN_ATTRIBUTE, Packer.ERROR);

		JarInputStream jis = null;
		os = new GZIPOutputStream(os);

		PrintStream err = System.err;
		try {
			System.setErr(new PrintStream(DevNullOutputStream.DEV_NULL_OUTPUT_STREAM));
			jis = new JarInputStream(is);
			packer.pack(jis, os);
		}
		finally {
			System.setErr(err);
			if (closeIS && closeOS) IOUtil.close(jis, os);
			else {
				if (closeIS) IOUtil.close(jis);
				if (closeOS) IOUtil.close(os);
			}
		}
	}

	public static String removePack200Ext(String name) {
		int index = name.indexOf(".pack.gz");
		if (index == -1) return name;
		return name.substring(0, index);
	}

}