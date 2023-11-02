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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;

public class EmptyHttpEntity extends AbstractHttpEntity implements Entity4 {

	private ContentType ct;

	/**
	 * Constructor of the class
	 * 
	 * @param contentType
	 */
	public EmptyHttpEntity(ContentType contentType) {
		super();
		this.ct = contentType;
		setContentType(ct != null ? ct.toString() : null);
	}

	@Override
	public long getContentLength() {
		return 0;
	}

	@Override
	public boolean isRepeatable() {
		return true;
	}

	@Override
	public void writeTo(OutputStream os) {
		// do nothing
	}

	@Override
	public InputStream getContent() throws IOException, IllegalStateException {
		return new ByteArrayInputStream(new byte[0]);
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