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
package lucee.runtime.exp;

import java.net.URL;

import lucee.runtime.config.Config;
import lucee.runtime.type.util.KeyConstants;

/**
 * Exception class for the HTTP Handling
 */
public final class HTTPException extends ApplicationException {

	private int statusCode;
	private String statusText;
	private URL url;

	/**
	 * Constructor of the class
	 * 
	 * @param message
	 * @param detail
	 * @param statusCode
	 */
	public HTTPException(String message, String detail, int statusCode, String statusText, URL url) {
		super(message, detail);
		this.statusCode = statusCode;
		this.statusText = statusText;
		this.url = url;

		setAdditional(KeyConstants._statuscode, new Double(statusCode));
		setAdditional(KeyConstants._statustext, statusText);
		if (url != null) setAdditional(KeyConstants._url, url.toExternalForm());
	}

	/**
	 * @return Returns the statusCode.
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * @return Returns the status text.
	 */
	public String getStatusText() {
		return statusText;
	}

	public URL getURL() {
		return url;
	}

	@Override
	public CatchBlock getCatchBlock(Config config) {
		CatchBlock sct = super.getCatchBlock(config);
		sct.setEL("statusCode", statusCode + "");
		sct.setEL("statusText", statusText);
		if (url != null) sct.setEL("url", url.toExternalForm());
		return sct;
	}
}