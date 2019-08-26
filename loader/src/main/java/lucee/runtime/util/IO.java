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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

import lucee.commons.io.res.Resource;

public interface IO {
	/**
	 * close stream silently (no Exception)
	 * 
	 * @param is
	 */
	public void closeSilent(InputStream is);

	/**
	 * close stream silently (no Exception)
	 * 
	 * @param os
	 */
	public void closeSilent(OutputStream os);

	/**
	 * close streams silently (no Exception)
	 * 
	 * @param is
	 * @param os
	 */
	public void closeSilent(InputStream is, OutputStream os);

	/**
	 * close streams silently (no Exception)
	 * 
	 * @param r
	 */
	public void closeSilent(Reader r);

	/**
	 * close streams silently (no Exception)
	 * 
	 * @param w
	 */
	public void closeSilent(Writer w);

	/**
	 * close any object with a close method silently
	 * 
	 * @param o
	 */
	public void closeSilent(Object o);

	/**
	 * converts an InputStream to a String
	 * 
	 * @param is
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	public String toString(InputStream is, Charset charset) throws IOException;

	/**
	 * reads the content of a Resource
	 * 
	 * @param res
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	public String toString(Resource res, Charset charset) throws IOException;

	/**
	 * converts a byte array to a String
	 * 
	 * @param barr
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	public String toString(byte[] barr, Charset charset) throws IOException;

	/**
	 * reads readers data as String
	 * 
	 * @param r
	 * @return
	 * @throws IOException
	 */
	public String toString(Reader r) throws IOException;

	/**
	 * copy data from input stream to output stream
	 * 
	 * @param in
	 * @param out
	 * @param closeIS close input stream when done
	 * @param closeOS close output stream when done
	 * @throws IOException
	 */
	public void copy(InputStream in, OutputStream out, boolean closeIS, boolean closeOS) throws IOException;

	/**
	 * copy data from reader to writer
	 * 
	 * @param in
	 * @param out
	 * @param closeR close the reader when done
	 * @param closeW close the writer when done
	 * @throws IOException
	 */
	public void copy(Reader in, Writer out, boolean closeR, boolean closeW) throws IOException;

	/**
	 * copy content from source to target
	 * 
	 * @param src
	 * @param trg
	 * @throws IOException
	 */
	public void copy(Resource src, Resource trg) throws IOException;

	public BufferedInputStream toBufferedInputStream(InputStream is);

	public BufferedOutputStream toBufferedOutputStream(OutputStream os);

	public void write(Resource res, String content, boolean append, Charset charset) throws IOException;

	public void write(Resource res, byte[] content, boolean append) throws IOException;

	public Reader getReader(InputStream is, Charset charset) throws IOException;

	public Reader getReader(Resource res, Charset charset) throws IOException;

	public Reader toBufferedReader(Reader reader);

	public void copy(InputStream is, Resource out, boolean closeIS) throws IOException;

	public OutputStream createTemporaryStream();

}