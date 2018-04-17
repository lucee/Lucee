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
package lucee.runtime.tag;

public class ZipParamContent implements ZipParamAbstr {

	private Object content;
	private String entryPath;
	private String charset;
	private Boolean compress;

	public ZipParamContent(Object content, String entryPath, String charset, boolean compress) {
		this.content=content;
		this.entryPath=entryPath;
		this.charset=charset;
		this.compress=compress;
	}

	/**
	 * @return the content
	 */
	public Object getContent() {
		return content;
	}

	/**
	 * @return the entryPath
	 */
	public String getEntryPath() {
		return entryPath;
	}

	/**
	 * @return the charset
	 */
	public String getCharset() {
		return charset;
	}
	/**
	 * @return the compress
	 */
	public Boolean getCompress() {
		return compress;
	}


}