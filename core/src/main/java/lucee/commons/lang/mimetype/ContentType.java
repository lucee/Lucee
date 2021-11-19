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
package lucee.commons.lang.mimetype;

import lucee.commons.lang.StringUtil;

public class ContentType {
	private String mimeType;
	private String charset;

	public ContentType(String mimeType) {
		this.mimeType = mimeType;
	}

	public ContentType(String mimeType, String charset) {
		this.mimeType = mimeType;
		setCharset(charset);
	}

	public String getMimeType() {
		return mimeType;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		if (!StringUtil.isEmpty(charset, true)) {
			this.charset = charset.trim();
		}
		else this.charset = null;
	}

	@Override
	public String toString() {
		if (charset == null) return mimeType.toString();
		return mimeType + "; charset=" + charset;
	}
}