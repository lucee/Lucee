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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;

public class FileStreamWrapperWrite extends FileStreamWrapper {

	private BufferedOutputStream bos;
	private boolean append;
	private String charset;
	private boolean seekable;
	private RandomAccessFile raf;

	public FileStreamWrapperWrite(Resource res, String charset, boolean append, boolean seekable) {
		super(res);

		this.charset = charset;
		this.append = append;
		this.seekable = seekable;
	}

	@Override
	public void write(Object obj) throws IOException {
		byte[] bytes = null;
		InputStream is = null;
		if (Decision.isBinary(obj)) {
			bytes = Caster.toBinary(obj, null);
		}
		else if (obj instanceof FileStreamWrapper) {
			is = ((FileStreamWrapper) obj).getResource().getInputStream();
		}
		else if (obj instanceof Resource) {
			is = ((Resource) obj).getInputStream();
		}
		else {// if(Decision.isSimpleValue(obj)){
			String str = Caster.toString(obj, false, null);
			if (str != null) bytes = str.getBytes(charset);
		}

		if (bytes != null) {
			if (seekable) getRAF().write(bytes);
			else _getOS().write(bytes);
		}
		else if (is != null) {
			if (seekable) writeToRAF(is, getRAF());
			else IOUtil.copy(is, _getOS(), true, false);
		}
		else throw new IOException("can't write down object of type [" + Caster.toTypeName(obj) + "] to resource [" + res + "]");

	}

	@Override
	public void close() throws IOException {
		super.setStatus(FileStreamWrapper.STATE_CLOSE);
		if (bos != null) bos.close();
		if (raf != null) raf.close();
	}

	@Override
	public String getMode() {
		return append ? "append" : "write";
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
		}
		else throw Caster.toPageException(new IOException("skip is only supported when you have set argument seekable of function fileOpen to true"));
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

	public static void writeToRAF(InputStream is, RandomAccessFile raf) throws IOException {

		byte[] buffer = new byte[2048];
		int tmp = 0;

		while ((tmp = is.read(buffer)) != -1) {
			raf.write(buffer, 0, tmp);
		}
	}

	private RandomAccessFile getRAF() throws IOException {
		if (raf == null) {
			if (!(res instanceof File)) throw new IOException("only resources for local filesytem support seekable");

			raf = new RandomAccessFile((File) res, "rw");
			if (append) raf.seek(res.length());
		}
		return raf;
	}

	private BufferedOutputStream _getOS() throws IOException {
		if (bos == null) bos = IOUtil.toBufferedOutputStream(res.getOutputStream(append));
		return bos;
	}
}