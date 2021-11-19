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
package lucee.runtime.op;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

import lucee.commons.io.IOUtil;
import lucee.commons.io.TemporaryStream;
import lucee.commons.io.res.Resource;
import lucee.runtime.util.IO;

public class IOImpl implements IO {

	private static IO singelton;

	public static IO getInstance() {
		if (singelton == null) singelton = new IOImpl();
		return singelton;
	}

	@Override
	public void closeSilent(InputStream is) {
		IOUtil.closeEL(is);
	}

	@Override
	public void closeSilent(OutputStream os) {
		IOUtil.closeEL(os);
	}

	@Override
	public void closeSilent(InputStream is, OutputStream os) {
		IOUtil.closeEL(is, os);
	}

	@Override
	public void closeSilent(Reader r) {
		IOUtil.closeEL(r);
	}

	@Override
	public void closeSilent(Writer w) {
		IOUtil.closeEL(w);
	}

	@Override
	public void closeSilent(Object o) {
		IOUtil.closeEL(o);
	}

	@Override
	public String toString(InputStream is, Charset charset) throws IOException {
		return IOUtil.toString(is, charset);
	}

	@Override
	public String toString(Reader r) throws IOException {
		return IOUtil.toString(r);
	}

	@Override
	public String toString(byte[] barr, Charset charset) throws IOException {
		return IOUtil.toString(barr, charset);
	}

	@Override
	public String toString(Resource res, Charset charset) throws IOException {
		return IOUtil.toString(res, charset);
	}

	@Override
	public void copy(InputStream in, OutputStream out, boolean closeIS, boolean closeOS) throws IOException {
		IOUtil.copy(in, out, closeIS, closeOS);
	}

	@Override
	public void copy(Reader r, Writer w, boolean closeR, boolean closeW) throws IOException {
		IOUtil.copy(r, w, closeR, closeW);
	}

	@Override
	public void copy(Resource src, Resource trg) throws IOException {
		IOUtil.copy(src, trg);
	}

	@Override
	public BufferedInputStream toBufferedInputStream(InputStream is) {
		return IOUtil.toBufferedInputStream(is);
	}

	@Override
	public BufferedOutputStream toBufferedOutputStream(OutputStream os) {
		return IOUtil.toBufferedOutputStream(os);
	}

	@Override
	public void write(Resource res, String content, boolean append, Charset charset) throws IOException {
		IOUtil.write(res, content, charset, append);
	}

	@Override
	public void write(Resource res, byte[] content, boolean append) throws IOException {
		IOUtil.write(res, content, append);
	}

	@Override
	public Reader getReader(InputStream is, Charset charset) throws IOException {
		return IOUtil.getReader(is, charset);
	}

	@Override
	public Reader getReader(Resource res, Charset charset) throws IOException {
		return IOUtil.getReader(res, charset);
	}

	@Override
	public Reader toBufferedReader(Reader reader) {
		return IOUtil.toBufferedReader(reader);
	}

	@Override
	public void copy(InputStream is, Resource out, boolean closeIS) throws IOException {
		IOUtil.copy(is, out, closeIS);
	}

	@Override
	public OutputStream createTemporaryStream() {
		return new TemporaryStream();
	}
}