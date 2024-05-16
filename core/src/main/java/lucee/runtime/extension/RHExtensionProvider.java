/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
 */
package lucee.runtime.extension;

import java.net.MalformedURLException;
import java.net.URL;

import lucee.commons.net.HTTPUtil;

public class RHExtensionProvider {

	private final URL url;
	private final boolean readonly;

	public RHExtensionProvider(String strUrl, boolean readonly) throws MalformedURLException {
		this.url = HTTPUtil.toURL(strUrl, HTTPUtil.ENCODED_AUTO);
		this.readonly = readonly;
	}

	public RHExtensionProvider(URL url, boolean readonly) {
		this.url = url;
		this.readonly = readonly;
	}

	public URL getURL() {
		return url;
	}

	public boolean isReadonly() {
		return readonly;
	}

	@Override
	public String toString() {
		return url.toExternalForm();
	}
}