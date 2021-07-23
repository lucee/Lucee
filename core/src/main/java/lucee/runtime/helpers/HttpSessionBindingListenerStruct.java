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
package lucee.runtime.helpers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import lucee.runtime.type.Collection;
import lucee.runtime.type.StructImpl;

public final class HttpSessionBindingListenerStruct extends StructImpl implements HttpSessionBindingListener {

	private URL url;

	/**
	 * Constructor of the class
	 * 
	 * @param strUrl
	 * @throws MalformedURLException
	 */
	public HttpSessionBindingListenerStruct(String strUrl) throws MalformedURLException {
		this(new URL(strUrl));
	}

	/**
	 * Constructor of the class
	 * 
	 * @param url
	 */
	public HttpSessionBindingListenerStruct(URL url) {
		this.url = url;
	}

	@Override
	public void valueBound(HttpSessionBindingEvent event) {

	}

	@Override
	public void valueUnbound(HttpSessionBindingEvent event) {
		try {
			url.getContent();
		}
		catch (IOException e) {
		}
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		HttpSessionBindingListenerStruct trg = new HttpSessionBindingListenerStruct(url);
		copy(this, trg, deepCopy);
		return trg;
	}
}