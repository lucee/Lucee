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
package lucee.commons.net.http.httpclient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.content.AbstractContentBody;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;

public class ResourceBody extends AbstractContentBody {

	public static final String DEFAULT_MIMETYPE = "application/octet-stream";

	private String fileName = null;
	private Resource res;
	private String charset;

	public ResourceBody(Resource res, String mimetype, String fileName, String charset) throws FileNotFoundException {
		super(StringUtil.isEmpty(mimetype, true) ? DEFAULT_MIMETYPE : mimetype);
		this.res = res;
		if (!res.isFile()) {
			throw new FileNotFoundException("File is not a normal file.");
		}
		if (!res.isReadable()) {
			throw new FileNotFoundException("File is not readable.");
		}
		this.fileName = StringUtil.isEmpty(fileName, true) ? res.getName() : fileName;
		this.charset = charset;

	}

	@Override
	public String getFilename() {
		return (fileName == null) ? "noname" : fileName;
	}

	@Override
	public void writeTo(OutputStream os) throws IOException {
		IOUtil.copy(res, os, false);
	}

	@Override
	public String getCharset() {
		return charset;
	}

	@Override
	public long getContentLength() {
		if (this.res != null) {
			return this.res.length();
		}
		return 0;
	}

	@Override
	public String getTransferEncoding() {
		return MIME.ENC_BINARY;
	}

	/**
	 * @return the res
	 */
	public Resource getResource() {
		return res;
	}
}