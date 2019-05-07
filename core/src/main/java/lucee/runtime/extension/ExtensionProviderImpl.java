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
package lucee.runtime.extension;

import java.net.MalformedURLException;
import java.net.URL;

public class ExtensionProviderImpl implements ExtensionProvider {

	// private String name;
	private URL url;
	private String strUrl;
	private boolean readOnly;

	public ExtensionProviderImpl(URL url, boolean readOnly) {
		// this.name = name;
		this.url = url;
		this.readOnly = readOnly;
	}

	public ExtensionProviderImpl(String strUrl, boolean readOnly) {
		// this.name = name;
		this.strUrl = strUrl;
		this.readOnly = readOnly;
	}

	/**
	 * @return the url
	 * @throws MalformedURLException
	 */
	@Override
	public URL getUrl() throws MalformedURLException {
		if (url == null) url = new URL(strUrl);
		return url;
	}

	@Override
	public String getUrlAsString() {
		if (strUrl != null) return strUrl;
		return url.toExternalForm();
	}

	@Override
	public boolean isReadOnly() {
		return readOnly;
	}

	@Override
	public String toString() {
		return "url:" + getUrlAsString() + ";";
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		// if(!(obj instanceof ExtensionProvider))return false;

		return toString().equals(obj.toString());
	}

}