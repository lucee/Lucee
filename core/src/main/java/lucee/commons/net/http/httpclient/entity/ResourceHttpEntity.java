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
package lucee.commons.net.http.httpclient.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;

/**
 * A RequestEntity that represents a Resource.
 */
public class ResourceHttpEntity extends AbstractHttpEntity implements Entity4 {

	final Resource res;
	private ContentType ct;

	public ResourceHttpEntity(final Resource res, final ContentType contentType) {
		super();
		this.res = res;
		if (contentType != null) setContentType(contentType.toString());
		ct = contentType;
	}

	@Override
	public long getContentLength() {
		return this.res.length();
	}

	@Override
	public boolean isRepeatable() {
		return true;
	}

	@Override
	public InputStream getContent() throws IOException {
		return res.getInputStream();
	}

	@Override
	public void writeTo(final OutputStream out) throws IOException {
		IOUtil.copy(res.getInputStream(), out, true, false);
	}

	@Override
	public boolean isStreaming() {
		return false;
	}

	@Override
	public long contentLength() {
		return getContentLength();
	}

	@Override
	public String contentType() {
		return ct != null ? ct.toString() : null;
	}
}