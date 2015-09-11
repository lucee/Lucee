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
package lucee.commons.net.http.httpclient4.entity;

import lucee.commons.lang.StringUtil;

import org.apache.http.Header;
import org.apache.http.entity.ByteArrayEntity;

public class ByteArrayHttpEntity extends ByteArrayEntity implements Entity4 {
 
	
	private String strContentType;
	private int contentLength;

	public ByteArrayHttpEntity(byte[] barr, String contentType) {
		super(barr);
		contentLength=barr==null?0:barr.length;
		
		if(StringUtil.isEmpty(contentType,true)) {
			Header h = getContentType();
			if(h!=null) strContentType=h.getValue();
		}
		else this.strContentType=contentType;
	}

	@Override
	public long contentLength() {
		return contentLength;
	}

	@Override
	public String contentType() {
		return strContentType;
	}

}