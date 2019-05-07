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
package lucee.runtime.util;

import java.net.URL;
import java.util.List;

import lucee.commons.lang.HTMLEntities;

public class HTMLUtilImpl implements HTMLUtil {

	@Override
	public String escapeHTML(String str) {
		return HTMLEntities.escapeHTML(str);
	}

	@Override
	public String escapeHTML(String str, short version) {
		return HTMLEntities.escapeHTML(str, version);
	}

	@Override
	public String unescapeHTML(String str) {
		return HTMLEntities.unescapeHTML(str);
	}

	@Override
	public List<URL> getURLS(String html, URL url) {
		lucee.commons.lang.HTMLUtil hu = new lucee.commons.lang.HTMLUtil();
		return hu.getURLS(html, url);
	}

}