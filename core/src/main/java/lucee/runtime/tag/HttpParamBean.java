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

import lucee.commons.io.res.Resource;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

/**
 * 
 */
public final class HttpParamBean {

	public static final int TYPE_URL = 1;
	public static final int TYPE_FORM = 2;
	public static final int TYPE_CGI = 3;
	public static final int TYPE_HEADER = 4;
	public static final int TYPE_COOKIE = 5;
	public static final int TYPE_FILE = 6;
	public static final int TYPE_XML = 7;
	public static final int TYPE_BODY = 8;

	/** Specifies the value of the URL, FormField, Cookie, File, or CGI variable being passed. */
	private Object value;

	/** The transaction type. */
	private int type = TYPE_URL;

	/** Required for type = "File". */
	private Resource file;

	/** A variable name for the data being passed. */
	private String name;

	private short encoded = Http.ENCODED_AUTO;

	private String mimeType = "";

	/**
	 * set the value value Specifies the value of the URL, FormField, Cookie, File, or CGI variable
	 * being passed.
	 * 
	 * @param value value to set
	 **/
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * set the value type The transaction type.
	 * 
	 * @param type value to set
	 **/
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * set the value file Required for type = "File".
	 * 
	 * @param file value to set
	 **/
	public void setFile(Resource file) {
		this.file = file;
	}

	/**
	 * set the value name A variable name for the data being passed.
	 * 
	 * @param name value to set
	 **/
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the file.
	 */
	public Resource getFile() {
		return file;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return Returns the value.
	 * @throws PageException
	 */
	public String getValueAsString() throws PageException {
		return Caster.toString(value);
	}

	/**
	 * @return Returns the value.
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Returns the value of encoded.
	 * 
	 * @return value encoded
	 */
	public short getEncoded() {
		return encoded;
	}

	/**
	 * sets the encoded value.
	 * 
	 * @param encoded The encoded to set.
	 */
	public void setEncoded(short encoded) {
		this.encoded = encoded;
	}

	/**
	 * Returns the value of mimeType.
	 * 
	 * @return value mimeType
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * sets the mimeType value.
	 * 
	 * @param mimeType The mimeType to set.
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
}