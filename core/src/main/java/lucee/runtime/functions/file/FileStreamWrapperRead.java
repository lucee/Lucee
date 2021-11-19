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
package lucee.runtime.functions.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;

public class FileStreamWrapperRead extends FileStreamWrapper {

	private BufferedReader br;
	private String charset;
	private boolean seekable;
	private RandomAccessFile raf;

	/**
	 * Constructor of the class
	 * 
	 * @param res
	 * @param charset
	 * @throws IOException
	 */
	public FileStreamWrapperRead(Resource res, String charset, boolean seekable) {
		super(res);
		this.charset = charset;
		this.seekable = seekable;
	}

	@Override
	public Object read(int len) throws IOException {
		if (seekable) {
			byte[] barr = new byte[len];
			len = getRAF().read(barr);
			if (len == -1) throw new IOException("End of file reached");
			return new String(barr, 0, len, charset);
		}

		char[] carr = new char[len];
		len = _getBR().read(carr);
		if (len == -1) throw new IOException("End of file reached");

		return new String(carr, 0, len);
	}

	@Override
	public String readLine() throws IOException {
		if (seekable) {
			return getRAF().readLine();
		}

		if (!_getBR().ready()) throw new IOException(" End of file reached");
		return _getBR().readLine();
	}

	@Override
	public void close() throws IOException {
		super.setStatus(FileStreamWrapper.STATE_CLOSE);
		if (br != null) br.close();
		if (raf != null) raf.close();
	}

	@Override
	public String getMode() {
		return "read";
	}

	@Override
	public boolean isEndOfFile() {
		if (seekable) {
			long pos = 0;
			try {
				pos = getRAF().getFilePointer();
			}
			catch (IOException ioe) {
				throw new PageRuntimeException(Caster.toPageException(ioe));
			}
			try {
				if (raf.read() == -1) return true;
				raf.seek(pos);
			}
			catch (IOException e) {
				return true;
			}
			return false;
		}

		try {
			return !_getBR().ready();
		}
		catch (IOException e) {
			return true;
		}
	}

	@Override
	public long getSize() {
		return res.length();
	}

	@Override
	public void skip(int len) throws PageException {
		if (seekable) {
			try {
				getRAF().skipBytes(len);
			}
			catch (IOException e) {
				throw Caster.toPageException(e);
			}
			return;
		}

		try {
			_getBR().skip(len);
			return;
		}
		catch (IOException e) {
		}

		throw Caster.toPageException(new IOException("skip is only supported when you have set argument seekable of function fileOpen to true"));
	}

	@Override
	public void seek(long pos) throws PageException {
		if (seekable) {
			try {
				getRAF().seek(pos);
			}
			catch (IOException e) {
				throw Caster.toPageException(e);
			}
		}
		else throw Caster.toPageException(new IOException("seek is only supported when you have set argument seekable of function fileOpen to true"));
	}

	private RandomAccessFile getRAF() throws IOException {
		if (raf == null) {
			if (!(res instanceof File)) throw new IOException("only resources for local filesytem support seekable");

			raf = new RandomAccessFile((File) res, "r");
		}
		return raf;
	}

	private BufferedReader _getBR() throws IOException {
		if (br == null) {
			br = IOUtil.toBufferedReader(IOUtil.getReader(res.getInputStream(), charset));
		}
		return br;
	}
}