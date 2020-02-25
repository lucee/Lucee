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
package lucee.commons.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.zip.ZipFile;

import javax.mail.Transport;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.URLEncoder;
import lucee.runtime.exp.PageException;

/**
 * I/O Util
 */
public final class IOUtil {

	/**
	 * copy an inputstream to an outputstream
	 * 
	 * @param in
	 * @param out
	 * @param closeIS
	 * @param closeOS
	 * @throws IOException
	 */
	public static final void copy(InputStream in, OutputStream out, boolean closeIS, boolean closeOS) throws IOException {
		try {
			copy(in, out, 0xffff);
		}
		finally {
			if (closeIS) closeEL(in);
			if (closeOS && out != null) out.close();
		}
	}

	/**
	 * copy an inputstream to an outputstream
	 * 
	 * @param in
	 * @param out
	 * @param closeIS
	 * @param closeOS
	 * @throws IOException
	 */
	public static final void merge(InputStream in1, InputStream in2, OutputStream out, boolean closeIS1, boolean closeIS2, boolean closeOS) throws IOException {
		try {
			merge(in1, in2, out, 0xffff);
		}
		finally {
			if (closeIS1) closeEL(in1);
			if (closeIS2) closeEL(in2);
			if (closeOS) closeEL(out);
		}
	}

	/**
	 * copy an inputstream to an outputstream
	 * 
	 * @param in
	 * @param out
	 * @param closeIS
	 * @param closeOS
	 * @throws IOException
	 */
	public static final void copy(OutputStream out, InputStream in, boolean closeIS, boolean closeOS) throws IOException {
		copy(in, out, closeIS, closeOS);
	}

	/**
	 * copy an input resource to an output resource
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public static void copy(Resource in, Resource out) throws IOException {
		in.copyTo(out, false);
	}

	public static void merge(Resource in1, Resource in2, Resource out) throws IOException {
		InputStream is1 = null;
		InputStream is2 = null;
		OutputStream os = null;
		try {
			is1 = toBufferedInputStream(in1.getInputStream());
			is2 = toBufferedInputStream(in2.getInputStream());
			os = toBufferedOutputStream(out.getOutputStream());
		}
		catch (IOException ioe) {
			IOUtil.closeEL(is1);
			IOUtil.closeEL(is2);
			IOUtil.closeEL(os);
			throw ioe;
		}
		merge(is1, is2, os, true, true, true);
	}

	/**
	 * copy an input resource to an output resource
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public static void copy(InputStream is, Resource out, boolean closeIS) throws IOException {
		OutputStream os = null;
		try {
			os = toBufferedOutputStream(out.getOutputStream());
		}
		catch (IOException ioe) {
			IOUtil.closeEL(os);
			throw ioe;
		}
		copy(is, os, closeIS, true);
	}

	/**
	 * copy an input resource to an output resource
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public static void copy(Resource in, OutputStream os, boolean closeOS) throws IOException {
		InputStream is = null;
		try {
			is = toBufferedInputStream(in.getInputStream());
		}
		catch (IOException ioe) {
			IOUtil.closeEL(is);
			throw ioe;
		}
		copy(is, os, true, closeOS);
	}

	public static final void copy(InputStream in, OutputStream out, int offset, int length) throws IOException {
		copy(in, out, offset, length, 0xffff);
	}

	public static final void copy(InputStream in, OutputStream out, long offset, long length) throws IOException {
		int len;
		byte[] buffer;
		int block = 0xffff;

		// first offset to start
		if (offset > 0) {
			long skipped = 0;
			try {
				skipped = in.skip(offset);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				// skipped will be -1, see below
			}
			if (skipped <= 0) {
				while (true) {
					if (block > offset) block = (int) offset;
					buffer = new byte[block];
					len = in.read(buffer);
					if (len == -1) throw new IOException("reading offset is bigger than input itself");
					// dnos.write(buffer, 0, len);
					offset -= len;
					if (offset <= 0) break;
				}
			}
		}

		// write part
		if (length < 0) {
			copy(in, out, block);
			return;
		}

		while (true) {
			if (block > length) block = (int) length;
			buffer = new byte[block];
			len = in.read(buffer);
			if (len == -1) break;
			out.write(buffer, 0, len);
			length -= len;
			if (length <= 0) break;
		}
	}

	public static final void copy(InputStream in, OutputStream out, int offset, int length, int blockSize) throws IOException {

		int len;
		byte[] buffer;
		int block;// 0xffff;

		// first offset to start
		if (offset > 0) {
			long skipped = 0;
			try {
				skipped = in.skip(offset);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				// skipped will be -1, see below
			}

			if (skipped <= 0) {
				block = blockSize;// 0xffff;
				while (true) {
					if (block > offset) block = offset;
					buffer = new byte[block];
					len = in.read(buffer);
					if (len == -1) throw new IOException("reading offset is bigger than input itself");
					// dnos.write(buffer, 0, len);
					offset -= len;
					if (offset <= 0) break;
				}
			}
		}

		// write part
		if (length < 0) {
			copy(in, out, blockSize);
			return;
		}
		block = blockSize;// 0xffff;
		while (true) {
			if (block > length) block = length;
			buffer = new byte[block];
			len = in.read(buffer);
			if (len == -1) break;
			out.write(buffer, 0, len);
			length -= len;
			if (length <= 0) break;
		}
	}

	/**
	 * copy an inputstream to an outputstream
	 * 
	 * @param in
	 * @param out
	 * @param blockSize
	 * @throws IOException
	 */
	private static final void copy(InputStream in, OutputStream out, int blockSize) throws IOException {
		byte[] buffer = new byte[blockSize];
		int len;
		while ((len = in.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}
	}

	private static final void merge(InputStream in1, InputStream in2, OutputStream out, int blockSize) throws IOException {
		copy(in1, out, blockSize);
		copy(in2, out, blockSize);
	}

	/**
	 * copy a reader to a writer
	 * 
	 * @param r
	 * @param w
	 * @throws IOException
	 */
	private static final void copy(Reader r, Writer w, long timeout) throws IOException {
		copy(r, w, 0xffff, timeout);
	}

	/**
	 * copy a reader to a writer
	 * 
	 * @param reader
	 * @param writer
	 * @param closeReader
	 * @param closeWriter
	 * @throws IOException
	 */
	public static final void copy(Reader reader, Writer writer, boolean closeReader, boolean closeWriter) throws IOException {
		try {
			copy(reader, writer, 0xffff, -1);
		}
		finally {
			if (closeReader) closeEL(reader);
			if (closeWriter) closeEL(writer);
		}
	}

	/**
	 * copy a reader to a writer
	 * 
	 * @param r
	 * @param w
	 * @param blockSize
	 * @throws IOException
	 */
	private static final void copy(Reader r, Writer w, int blockSize, long timeout) throws IOException {
		if (timeout < 1) {
			char[] buffer = new char[blockSize];
			int len;

			while ((len = r.read(buffer)) != -1)
				w.write(buffer, 0, len);
		}
		else {
			Copy c = new Copy(r, w, blockSize, timeout);
			c.start();

			try {
				synchronized (c.notifier) {// print.err(timeout);
					c.notifier.wait(timeout + 1);
				}
			}
			catch (InterruptedException ie) {
				throw ExceptionUtil.toIOException(c.t);
			}
			if (c.t != null) throw ExceptionUtil.toIOException(c.t);
			if (!c.finished) throw new IOException("reached timeout (" + timeout + "ms) while copying data");

		}
	}

	/**
	 * copy content of in file to out File
	 * 
	 * @param in input
	 * @param out output
	 * @throws IOException
	 */
	public void copy(File in, File out) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new BufferedFileInputStream(in);
			os = new BufferedFileOutputStream(out);
		}
		catch (IOException ioe) {
			closeEL(is, os);
			throw ioe;
		}
		copy(is, os, true, true);
	}

	/**
	 * close inputstream without an Exception
	 * 
	 * @param is
	 * @param os
	 */
	public static void closeEL(InputStream is, OutputStream os) {
		closeEL(is);
		closeEL(os);
	}

	public static void closeEL(Connection conn) {
		try {
			if (conn != null) conn.close();
		}
		// catch (AlwaysThrow at) {throw at;}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	/**
	 * close inputstream without an Exception
	 * 
	 * @param is
	 */
	public static void closeEL(InputStream is) {
		try {
			if (is != null) is.close();
		}
		// catch (AlwaysThrow at) {throw at;}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	public static void closeEL(ZipFile zip) {
		try {
			if (zip != null) zip.close();
		}
		// catch (AlwaysThrow at) {throw at;}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	/**
	 * close outputstream without an Exception
	 * 
	 * @param os
	 */
	public static void closeEL(OutputStream os) {
		try {
			if (os != null) os.close();
		}
		// catch (AlwaysThrow at) {throw at;}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	public static void closeEL(ResultSet rs) {
		try {
			if (rs != null) rs.close();
		}
		catch (Throwable e) {
			ExceptionUtil.rethrowIfNecessary(e);
		}
	}

	/**
	 * close Reader without an Exception
	 * 
	 * @param r
	 */
	public static void closeEL(Reader r) {
		try {
			if (r != null) r.close();
		}
		// catch (AlwaysThrow at) {throw at;}
		catch (Throwable e) {
			ExceptionUtil.rethrowIfNecessary(e);
		}
	}

	/**
	 * close Closeable without an Exception
	 * 
	 * @param r
	 */
	public static void closeEL(Closeable c) {
		try {
			if (c != null) c.close();
		}
		// catch (AlwaysThrow at) {throw at;}
		catch (Throwable e) {
			ExceptionUtil.rethrowIfNecessary(e);
		}
	}

	/**
	 * close Writer without an Exception
	 * 
	 * @param w
	 */
	public static void closeEL(Writer w) {
		try {
			if (w != null) w.close();
		}
		// catch (AlwaysThrow at) {throw at;}
		catch (Throwable e) {
			ExceptionUtil.rethrowIfNecessary(e);
		}
	}

	/**
	 * close Writer without an Exception
	 * 
	 * @param w
	 */
	public static void closeEL(Transport t) {
		try {
			if (t != null && t.isConnected()) t.close();
		}
		catch (Throwable e) {
			ExceptionUtil.rethrowIfNecessary(e);
		}
	}

	/**
	 * call close method from any Object with a close method.
	 * 
	 * @param obj
	 */
	public static void closeEL(Object obj) {
		if (obj instanceof InputStream) IOUtil.closeEL((InputStream) obj);
		else if (obj instanceof OutputStream) IOUtil.closeEL((OutputStream) obj);
		else if (obj instanceof Writer) IOUtil.closeEL((Writer) obj);
		else if (obj instanceof Reader) IOUtil.closeEL((Reader) obj);
		else if (obj instanceof Closeable) IOUtil.closeEL((Closeable) obj);
		else if (obj instanceof ZipFile) IOUtil.closeEL((ZipFile) obj);
		else if (obj instanceof ResultSet) IOUtil.closeEL((ResultSet) obj);
		else if (obj instanceof Connection) IOUtil.closeEL((Connection) obj);
		else {
			try {
				Method method = obj.getClass().getMethod("close", new Class[0]);
				method.invoke(obj, new Object[0]);
			}
			catch (Throwable e) {
				ExceptionUtil.rethrowIfNecessary(e);
			}
		}
	}

	/**
	 * @deprecated use instead <code>{@link #getReader(Resource, Charset)}</code>
	 * @param res
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	@Deprecated
	public static Reader getReader(Resource res, String charset) throws IOException {
		return getReader(res, CharsetUtil.toCharset(charset));
	}

	public static Reader getReader(Resource res, Charset charset) throws IOException {
		/*
		 * 00 00 FE FF UTF-32, big-endian FF FE 00 00 UTF-32, little-endian
		 */

		InputStream is = null;
		try {
			is = res.getInputStream();
			boolean markSupported = is.markSupported();
			if (markSupported) is.mark(4);
			int first = is.read();
			int second = is.read();
			// FE FF UTF-16, big-endian
			if (first == 0xFE && second == 0xFF) {
				return _getReader(is, CharsetUtil.UTF16BE);
			}
			// FF FE UTF-16, little-endian
			if (first == 0xFF && second == 0xFE) {
				return _getReader(is, CharsetUtil.UTF16LE);
			}

			int third = is.read();
			// EF BB BF UTF-8
			if (first == 0xEF && second == 0xBB && third == 0xBF) {
				// is.reset();
				return _getReader(is, CharsetUtil.UTF8);
			}
			/*
			 * int forth=is.read(); // 00 00 FE FF UTF-32, big-endian if (first == 0x00 && second == 0x00 &&
			 * third == 0xFE && forth == 0xFF) { is.reset(); return _getReader(is, "utf-32"); } // FF FE 00 00
			 * UTF-32, little-endian if (first == 0xFF && second == 0xFE && third == 0x00 && forth == 0x00) {
			 * is.reset(); return _getReader(is, "utf-32"); }
			 */

			if (markSupported) {
				is.reset();
				return _getReader(is, charset);
			}
		}
		catch (IOException ioe) {
			IOUtil.closeEL(is);
			throw ioe;
		}

		// when mark not supported return new reader
		closeEL(is);
		is = null;
		try {
			is = res.getInputStream();
		}
		catch (IOException ioe) {
			closeEL(is);
			throw ioe;
		}
		return _getReader(is, charset);
	}

	public static Reader getReader(InputStream is, Charset charset) throws IOException {

		boolean markSupported = is.markSupported();
		if (!markSupported) return _getReader(is, charset);

		if (markSupported) is.mark(4);

		int first = is.read();
		int second = is.read();
		// FE FF UTF-16, big-endian
		if (first == 0xFE && second == 0xFF) {
			// is.reset();
			return _getReader(is, CharsetUtil.UTF16BE);
		}
		// FF FE UTF-16, little-endian
		if (first == 0xFF && second == 0xFE) {
			// TODO FF FE 00 00 UTF-32 little-endian
			return _getReader(is, CharsetUtil.UTF16LE);
		}

		int third = is.read();
		// EF BB BF UTF-8
		if (first == 0xEF && second == 0xBB && third == 0xBF) {
			return _getReader(is, CharsetUtil.UTF8);
		}

		// 00 00 FE FF UTF-32 big-endian
		int forth = is.read();
		if (first == 0x00 && second == 0x00 && third == 0xFE && forth == 0xFF) {
			return _getReader(is, CharsetUtil.UTF32BE);
		}

		is.reset();
		return _getReader(is, charset);
	}

	/**
	 * @deprecated use instead <code>{@link #getReader(InputStream, Charset)}</code>
	 * @param is
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	@Deprecated
	public static Reader getReader(InputStream is, String charset) throws IOException {
		return getReader(is, CharsetUtil.toCharset(charset));
	}

	/**
	 * returns a Reader for the given InputStream
	 * 
	 * @param is
	 * @param charset
	 * @return Reader
	 * @throws IOException
	 */
	private static Reader _getReader(InputStream is, Charset charset) throws IOException {
		if (charset == null) charset = SystemUtil.getCharset();
		return new BufferedReader(new InputStreamReader(is, charset));
	}

	/**
	 * @deprecated use instead <code>{@link #toString(InputStream, Charset)}</code>
	 * @param is
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	@Deprecated
	public static String toString(InputStream is, String charset) throws IOException {
		return toString(is, CharsetUtil.toCharset(charset));
	}

	/**
	 * reads string data from an InputStream
	 * 
	 * @param is
	 * @param charset
	 * @return string from inputstream
	 * @throws IOException
	 */
	public static String toString(InputStream is, Charset charset) throws IOException {
		return toString(getReader(is, charset));
	}

	/**
	 * reads string data from an InputStream
	 * 
	 * @param is
	 * @param charset
	 * @param timeout in milliseconds
	 * @return string from inputstream
	 * @throws IOException
	 */
	public static String toString(InputStream is, Charset charset, long timeout) throws IOException {
		return toString(getReader(is, charset), timeout);
	}

	/**
	 * @deprecated use instead <code>{@link #toString(byte[], Charset)}</code>
	 * @param barr
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	@Deprecated
	public static String toString(byte[] barr, String charset) throws IOException {
		return toString(barr, CharsetUtil.toCharset(charset));
	}

	public static String toString(byte[] barr, Charset charset) throws IOException {
		return toString(getReader(new ByteArrayInputStream(barr), charset));
	}

	/**
	 * reads String data from a Reader
	 * 
	 * @param reader
	 * @return readed string
	 * @throws IOException
	 */
	public static String toString(Reader reader) throws IOException {
		return toString(reader, -1);
	}

	/**
	 * reads String data from a Reader
	 * 
	 * @param reader
	 * @param timeout timeout in milliseconds
	 * @return readed string
	 * @throws IOException
	 */
	public static String toString(Reader reader, long timeout) throws IOException {
		StringWriter sw = new StringWriter(512);
		copy(toBufferedReader(reader), sw, timeout);
		sw.close();
		return sw.toString();
	}

	/**
	 * reads String data from a Reader
	 * 
	 * @param reader
	 * @return readed string
	 * @throws IOException
	 */
	public static String toString(Reader reader, boolean buffered) throws IOException {
		StringWriter sw = new StringWriter(512);
		if (buffered) copy(toBufferedReader(reader), sw, -1);
		else copy(reader, sw, -1);
		sw.close();
		return sw.toString();
	}

	/**
	 * @deprecated use instead <code>{@link #toString(Resource, Charset)}</code>
	 * @param file
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	@Deprecated
	public static String toString(Resource file, String charset) throws IOException {
		return toString(file, CharsetUtil.toCharset(charset));
	}

	/**
	 * reads String data from File
	 * 
	 * @param file
	 * @param charset
	 * @return readed string
	 * @throws IOException
	 */
	public static String toString(Resource file, Charset charset) throws IOException {
		Reader r = null;
		try {
			r = getReader(file, charset);
			String str = toString(r);
			return str;
		}
		finally {
			closeEL(r);
		}
	}

	/**
	 * @param reader Reader to get content from it
	 * @return returns the content of the file as String Array (Line by Line)
	 * @throws IOException
	 */
	public static String[] toStringArray(Reader reader) throws IOException {
		if (reader == null) return new String[0];
		BufferedReader br = new BufferedReader(reader);
		LinkedList<String> list = new LinkedList<String>();

		String line;
		while ((line = br.readLine()) != null) {
			list.add(line);
		}
		br.close();
		String[] content = new String[list.size()];
		int count = 0;
		while (!list.isEmpty()) {
			content[count++] = list.removeFirst();
		}
		return content;
	}

	/**
	 * @deprecated use instead <code>{@link #write(Resource, String, Charset, boolean)}</code> writes a
	 *             String to an object
	 * @param file
	 * @param string String to write to file
	 * @param charset
	 * @param append append to cuuretn data or overwrite existing data
	 * @throws IOException
	 */
	@Deprecated
	public static void write(File file, String string, String strCharset, boolean append) throws IOException {
		Charset charset;
		if (StringUtil.isEmpty(strCharset)) {
			charset = SystemUtil.getCharset();
		}
		else charset = CharsetUtil.toCharset(strCharset);

		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(new BufferedFileOutputStream(file, append), charset);
			writer.write(string);

		}
		finally {
			closeEL(writer);
		}
	}

	/**
	 * @deprecated use instead <code>{@link #write(Resource, String, Charset, boolean)}</code>
	 * @param res
	 * @param string
	 * @param charset
	 * @param append
	 * @throws IOException
	 */
	@Deprecated
	public static void write(Resource res, String string, String charset, boolean append) throws IOException {
		write(res, string, CharsetUtil.toCharset(charset), append);
	}

	public static void write(Resource res, String string, Charset charset, boolean append) throws IOException {
		if (charset == null) {
			charset = SystemUtil.getCharset();
		}

		Writer writer = null;
		try {
			writer = getWriter(res, charset, append);
			writer.write(string);
		}
		finally {
			closeEL(writer);
		}
	}

	public static void write(Resource res, byte[] barr) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(barr);
		OutputStream os = IOUtil.toBufferedOutputStream(res.getOutputStream());
		IOUtil.copy(bais, os, true, true);
	}

	public static void write(Resource res, byte[] barr, boolean append) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(barr);
		OutputStream os = IOUtil.toBufferedOutputStream(res.getOutputStream(append));
		IOUtil.copy(bais, os, true, true);
	}

	/**
	 * @deprecated use instead <code>{@link #toBytes(Resource)}</code>
	 * @param file
	 * @return returns the Content of the file as byte array
	 * @throws IOException
	 */
	@Deprecated
	public static byte[] toBytes(File file) throws IOException {
		BufferedFileInputStream bfis = null;
		try {
			bfis = new BufferedFileInputStream(file);
			byte[] barr = toBytes(bfis);
			return barr;
		}
		finally {
			closeEL(bfis);
		}
	}

	/**
	 * @param res
	 * @return returns the Content of the file as byte array
	 * @throws IOException
	 */
	public static byte[] toBytes(Resource res) throws IOException {
		BufferedInputStream bfis = null;
		try {
			bfis = toBufferedInputStream(res.getInputStream());
			byte[] barr = toBytes(bfis);
			return barr;
		}
		finally {
			closeEL(bfis);
		}
	}

	public static BufferedInputStream toBufferedInputStream(InputStream is) {
		if (is instanceof BufferedInputStream) return (BufferedInputStream) is;
		return new BufferedInputStream(is);
	}

	public static BufferedOutputStream toBufferedOutputStream(OutputStream os) {
		if (os instanceof BufferedOutputStream) return (BufferedOutputStream) os;
		return new BufferedOutputStream(os);
	}

	public static BufferedReader toBufferedReader(Reader r) {
		if (r instanceof BufferedReader) return (BufferedReader) r;
		return new BufferedReader(r);
	}

	/**
	 * @deprecated use instead <code>{@link #getBufferedReader(Resource, Charset)}</code>
	 * @param res
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	@Deprecated
	public static BufferedReader getBufferedReader(Resource res, String charset) throws IOException {
		return getBufferedReader(res, CharsetUtil.toCharset(charset));
	}

	public static BufferedReader getBufferedReader(Resource res, Charset charset) throws IOException {
		return toBufferedReader(getReader(res, charset));
	}

	public static BufferedWriter toBufferedWriter(Writer w) {
		if (w instanceof BufferedWriter) return (BufferedWriter) w;
		return new BufferedWriter(w);
	}

	/**
	 * @param is
	 * @return returns the Content of the file as byte array
	 * @throws IOException
	 */
	public static byte[] toBytes(InputStream is) throws IOException {
		return toBytes(is, false);
	}

	public static byte[] toBytes(InputStream is, boolean closeStream) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		copy(is, baos, closeStream, true);
		return baos.toByteArray();
	}

	public static byte[] toBytes(InputStream is, boolean closeStream, byte[] defaultValue) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			copy(is, baos, closeStream, true);
			return baos.toByteArray();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}
	}

	public static byte[] toBytesMax(InputStream is, int max) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		copy(is, baos, 0, max);
		return baos.toByteArray();
	}

	/**
	 * flush OutputStream without an Exception
	 * 
	 * @param os
	 */
	public static void flushEL(OutputStream os) {
		try {
			if (os != null) os.flush();
		}
		catch (Exception e) {}
	}

	/**
	 * flush OutputStream without an Exception
	 * 
	 * @param os
	 */
	public static void flushEL(Writer w) {
		try {
			if (w != null) w.flush();
		}
		catch (Exception e) {}
	}

	/**
	 * check if given encoding is ok
	 * 
	 * @param encoding
	 * @throws PageException
	 */
	public static void checkEncoding(String encoding) throws IOException {
		try {
			URLEncoder.encode("", encoding);
		}
		catch (UnsupportedEncodingException e) {
			throw new IOException("invalid encoding [" + encoding + "]");
		}
	}

	/**
	 * return the mime type of a file, dont check extension
	 * 
	 * @param barr
	 * @param defaultValue
	 * @return mime type of the file
	 */
	public static String getMimeType(InputStream is, String defaultValue) {
		try {
			return getMimeType(IOUtil.toBytesMax(is, 1000), defaultValue);
		}
		catch (IOException e) {
			return defaultValue;
		}
	}

	/**
	 * return the mime type of a file, dont check extension
	 * 
	 * @param barr
	 * @return mime type of the file
	 * @throws IOException
	 */
	public static String getMimeType(byte[] barr, String defaultValue) {
		try {
			Tika tika = new Tika();
			return tika.detect(barr);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}
	}

	public static String getMimeType(Resource res, String defaultValue) {
		return getMimeType(res, null, defaultValue);
	}

	public static String getMimeType(String fileName, String defaultValue) {
		try {
			Tika tika = new Tika();
			return tika.detect(fileName);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	public static String getMimeType(Resource res, String fileName, String defaultValue) {
		Metadata md = new Metadata();

		String ext = StringUtil.isEmpty(fileName, true) ? null : ResourceUtil.getExtension(fileName.trim(), null);

		md.set(Metadata.RESOURCE_NAME_KEY, ext == null ? res.getName() : fileName.trim());
		md.set(Metadata.CONTENT_LENGTH, Long.toString(res.length()));

		InputStream is = null;
		try {
			Tika tika = new Tika();
			String result = tika.detect(is = res.getInputStream(), md);
			if (result.indexOf("tika") != -1) {
				String tmp = ResourceUtil.EXT_MT.get(ext != null ? ext : ResourceUtil.getExtension(res, "").toLowerCase());
				if (!StringUtil.isEmpty(tmp)) return tmp;
				if (!StringUtil.isEmpty(defaultValue)) return defaultValue;
			}
			return result;
		}
		catch (Exception e) {
			String tmp = ResourceUtil.EXT_MT.get(ext != null ? ext : ResourceUtil.getExtension(res, "").toLowerCase());
			if (tmp != null && tmp.indexOf("tika") == -1 && !StringUtil.isEmpty(tmp)) return tmp;
			return defaultValue;
		}
		finally {
			IOUtil.closeEL(is);
		}
	}

	public static String getMimeType(URL url, String defaultValue) {
		try {
			Tika tika = new Tika();
			return tika.detect(url);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * @deprecated use instead <code>{@link #getWriter(Resource, Charset)}</code>
	 * @param res
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	@Deprecated
	public static Writer getWriter(Resource res, String charset) throws IOException {
		return getWriter(res, CharsetUtil.toCharset(charset));
	}

	public static Writer getWriter(Resource res, Charset charset) throws IOException {
		OutputStream os = null;
		try {
			os = res.getOutputStream();
		}
		catch (IOException ioe) {
			closeEL(os);
			throw ioe;
		}
		return getWriter(os, charset);

	}

	/**
	 * @deprecated use instead <code>{@link #getWriter(Resource, Charset,boolean)}</code>
	 * @param res
	 * @param charset
	 * @param append
	 * @return
	 * @throws IOException
	 */
	@Deprecated
	public static Writer getWriter(Resource res, String charset, boolean append) throws IOException {
		return getWriter(res, CharsetUtil.toCharset(charset), append);
	}

	public static Writer getWriter(Resource res, Charset charset, boolean append) throws IOException {
		OutputStream os = null;
		try {
			os = res.getOutputStream(append);
		}
		catch (IOException ioe) {
			closeEL(os);
			throw ioe;
		}
		return getWriter(os, charset);
	}

	/**
	 * @deprecated use instead <code>{@link #getWriter(Resource, Charset)}</code> returns a Reader for
	 *             the given File and charset (Automatically check BOM Files)
	 * @param file
	 * @param charset
	 * @return Reader
	 * @throws IOException
	 */
	@Deprecated
	public static Writer getWriter(File file, String charset) throws IOException {
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
		}
		catch (IOException ioe) {
			closeEL(os);
			throw ioe;
		}
		return getWriter(os, charset);
	}

	/**
	 * @deprecated use instead <code>{@link #getWriter(Resource, Charset, boolean)}</code> returns a
	 *             Reader for the given File and charset (Automatically check BOM Files)
	 * @param file
	 * @param charset
	 * @return Reader
	 * @throws IOException
	 */
	@Deprecated
	public static Writer getWriter(File file, String charset, boolean append) throws IOException {
		OutputStream os = null;
		try {
			os = new FileOutputStream(file, append);
		}
		catch (IOException ioe) {
			closeEL(os);
			throw ioe;
		}
		return getWriter(os, charset);
	}

	/**
	 * @deprecated use instead <code>{@link #getWriter(OutputStream, Charset)}</code>
	 * @param os
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	@Deprecated
	public static Writer getWriter(OutputStream os, String charset) throws IOException {
		return getWriter(os, CharsetUtil.toCharset(charset));
	}

	/**
	 * returns a Reader for the given InputStream
	 * 
	 * @param is
	 * @param charset
	 * @return Reader
	 * @throws IOException
	 */
	public static Writer getWriter(OutputStream os, Charset charset) throws IOException {
		if (charset == null) charset = SystemUtil.getCharset();
		return new BufferedWriter(new OutputStreamWriter(os, charset));
	}

	public static String read(Reader reader, int size) throws IOException {
		return read(reader, new char[size]);
	}

	public static String read(Reader reader, char[] carr) throws IOException {
		int rst = reader.read(carr);
		if (rst == -1) return null;
		return new String(carr, 0, rst);
	}

	private static class Copy extends Thread {

		private Reader r;
		private Writer w;
		private int blockSize;
		private long timeout;
		private boolean finished;
		private Throwable t;
		private Object notifier = new Object();

		private Copy(Reader r, Writer w, int blockSize, long timeout) {
			this.r = r;
			this.w = w;
			this.blockSize = blockSize;
			this.timeout = timeout;
		}

		@Override
		public void run() {
			try {
				IOUtil.copy(r, w, blockSize, -1);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				this.t = t;
			}
			finally {
				finished = true;
				SystemUtil.notify(notifier);
			}
		}
	}
}
