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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

public class FileStreamWrapperReadBinary extends FileStreamWrapper {

	private BufferedInputStream bis;
	private boolean isEOF;
	private boolean seekable;
	private RandomAccessFile raf;

	/**
	 * Constructor of the class
	 * 
	 * @param res
	 * @param charset
	 * @throws IOException
	 */
	public FileStreamWrapperReadBinary(Resource res, boolean seekable) {
		super(res);
		this.seekable = seekable;
	}

	@Override
	public Object read(int len) throws IOException {
		byte[] barr = new byte[len];
		len = seekable ? getRAF().read(barr) : _getBIS().read(barr);
		if (len != barr.length) {
			byte[] rtn = new byte[len];
			for (int i = 0; i < len; i++) {
				rtn[i] = barr[i];
			}
			barr = rtn;
			isEOF = true;
		}
		return barr;
	}

	@Override
	public void close() throws IOException {
		super.setStatus(FileStreamWrapper.STATE_CLOSE);
		if (bis != null) bis.close();
		if (raf != null) raf.close();
	}

	@Override
	public String getMode() {
		return "readBinary";
	}

	@Override
	public boolean isEndOfFile() {
		return isEOF;
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
			_getBIS().skip(len);
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

	private BufferedInputStream _getBIS() throws IOException {
		if (bis == null) bis = IOUtil.toBufferedInputStream(res.getInputStream());
		return bis;
	}
}