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
package lucee.commons.net.http.httpclient;

import org.apache.http.Header;

public class HeaderWrap implements lucee.commons.net.http.Header {
	public final Header header;

	public HeaderWrap(Header header) {
		this.header = header;
	}

	@Override
	public String getName() {
		return header.getName();
	}

	@Override
	public String getValue() {
		return header.getValue();
	}

	@Override
	public String toString() {
		return header.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return header.equals(obj);
	}
}