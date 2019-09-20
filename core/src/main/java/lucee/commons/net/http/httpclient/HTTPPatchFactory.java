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

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

public class HTTPPatchFactory {

	public static HttpEntityEnclosingRequestBase getHTTPPatch(String url) throws PageException {
		// try to load the class, perhaps class does not exists with older jars
		Class clazz = ClassUtil.loadClass(HttpEntityEnclosingRequestBase.class.getClassLoader(), "org.apache.http.client.methods.HttpPatch", null);
		if (clazz == null) throw new ApplicationException("cannot load class [org.apache.http.client.methods.HttpPatch], you have to update your apache-commons-http*** jars");
		try {
			return (HttpEntityEnclosingRequestBase) ClassUtil.loadInstance(clazz, new Object[] { url });
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}

		// FUTURE if we have the new jar for sure return new HttpPatch(url);
	}
}