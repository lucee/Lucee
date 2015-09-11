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
import java.util.Map;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.jar.Pack200.Packer;
import java.util.jar.Pack200.Unpacker;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;


public class Pack200Util {
	


	public static void decompress(InputStream is,Resource jar, boolean closeIS) throws IOException {
		if(!jar.exists())jar.createFile(false);
		decompress(is, jar.getOutputStream(), closeIS, true);
	}
	
	public static void decompress(Resource p200,OutputStream os, boolean closeOS) throws IOException {
		decompress(p200.getInputStream(), os, true, closeOS);
	}
	
	public static void decompress(Resource p200,Resource jar) throws IOException {
		if(!jar.exists())jar.createFile(false);
		decompress(p200.getInputStream(), jar.getOutputStream(), true, true);
	}
	
	public static void decompress(File p200,File jar) throws IOException {
		if(!jar.exists())jar.createNewFile();
		decompress(new FileInputStream(p200), new FileOutputStream(jar), true, true);
	}

	public static void compress(InputStream is, Resource p200, boolean closeIS) throws IOException {
		if(!p200.exists())p200.createFile(false);
		compress(is, p200.getOutputStream(), closeIS, true);
	}
	
	public static void compress(Resource jar, OutputStream os, boolean closeOS) throws IOException {
		compress(jar.getInputStream(), os, true, closeOS);
	}
	
	public static void compress(Resource jar, Resource p200) throws IOException {
		if(!p200.exists())p200.createFile(false);
		compress(jar.getInputStream(), p200.getOutputStream(), true, true);
	}
	
	public static void compress(File jar, File p200) throws IOException {
		if(!p200.exists())p200.createNewFile();
		compress(new FileInputStream(jar), new FileOutputStream(p200), true, true);
	}

	public static void decompress(InputStream is,OutputStream os, boolean closeIS, boolean closeOS) throws IOException {
		
		Unpacker unpacker = Pack200.newUnpacker();
		is=new GZIPInputStream(is);
		JarOutputStream jos=null;
		try{
			jos = new JarOutputStream(os);
			unpacker.unpack(is, jos);
		}
		finally{
			if(closeIS)IOUtil.closeEL(is);
			if(closeOS)IOUtil.closeEL(jos);
		}
	}
	
	public static void compress(InputStream is,OutputStream os, boolean closeIS, boolean closeOS) throws IOException {
		// Create the Packer object
		Packer packer = Pack200.newPacker();

		// Initialize the state by setting the desired properties
		Map p = packer.properties();
		// take more time choosing codings for better compression
		p.put(Packer.EFFORT, "7");  // default is "5"
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
		p.put(Packer.CODE_ATTRIBUTE_PFX+"LineNumberTable", Packer.STRIP);
		// throw an error if an attribute is unrecognized
		p.put(Packer.UNKNOWN_ATTRIBUTE, Packer.ERROR);
		
		
		JarInputStream jis=null;
		os=new GZIPOutputStream(os);
		try{
			jis = new JarInputStream(is);
			packer.pack(jis, os);
		}
		finally{
			if(closeIS)IOUtil.closeEL(jis);
			if(closeOS)IOUtil.closeEL(os);
		}
	}
	
}