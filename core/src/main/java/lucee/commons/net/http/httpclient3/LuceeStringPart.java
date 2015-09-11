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
package lucee.commons.net.http.httpclient3;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.httpclient.methods.multipart.StringPart;


public class LuceeStringPart extends StringPart {

	private String value;

	public LuceeStringPart(String name, String value) {
		super(name, value);
		this.value=value;
	}
	
	public LuceeStringPart(String name, String value, String charset) {
		super(name, value, charset);
		this.value=value;		
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	@Override
	protected void sendDispositionHeader(OutputStream out)  throws IOException {
		ResourcePart.sendDispositionHeader(getName(),null,getCharSet(),out);
	}

}