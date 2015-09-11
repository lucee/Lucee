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
package lucee.commons.net.http.httpclient3.entity;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.httpclient.methods.RequestEntity;


public class EmptyRequestEntity implements RequestEntity,Entity3 {

	private final String contentType;
	
	/**
	 * Constructor of the class
	 * @param contentType
	 */
	public EmptyRequestEntity(String contentType) {
		this.contentType=contentType;
	}
	
	@Override
	public long getContentLength() {
		return 0;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public boolean isRepeatable() {
		return true;
	}

	@Override
	public void writeRequest(OutputStream os) throws IOException {
		// do nothing
	}
	
	@Override
	public long contentLength() {
		return getContentLength();
	}

	@Override
	public String contentType() {
		return getContentType();
	}
}