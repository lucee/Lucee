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

public interface ExtensionProvider {

	/**
	 * return the url of the extension
	 * 
	 * @return url
	 * @throws MalformedURLException Malformed URL Exception
	 */
	public URL getUrl() throws MalformedURLException;

	/**
	 * returns the url of the extension as a string
	 * 
	 * @return url
	 */
	public String getUrlAsString();

	/**
	 * is the extension readonly
	 * 
	 * @return is readonly
	 */
	public boolean isReadOnly();
}