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
package lucee.runtime.cfx.customtag;

import java.lang.reflect.Method;

import com.allaire.cfx.CustomTag;
import com.allaire.cfx.Request;
import com.allaire.cfx.Response;

import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.runtime.cfx.CFXTagException;

public class CPPCustomTag implements CustomTag {

	// this is loaded dynamic, because the lib is optional
	private static Method processRequest;

	private boolean keepAlive;
	private String procedure;
	private String serverLibrary;

	public CPPCustomTag(String serverLibrary, String procedure, boolean keepAlive) throws CFXTagException {
		this.serverLibrary = serverLibrary;
		this.procedure = procedure;
		this.keepAlive = keepAlive;
		if (processRequest == null) {
			Class clazz = null;
			try {
				clazz = ClassUtil.loadClass("com.naryx.tagfusion.cfx.CFXNativeLib");
			}
			catch (ClassException e) {

				throw new CFXTagException("cannot initialize C++ Custom tag library, make sure you have added all the required jar files. "
						+ "GO to the Lucee Server Administrator and on the page Services/Update, click on \"Update JARs\"");

			}
			try {
				processRequest = clazz.getMethod("processRequest", new Class[] { String.class, String.class, Request.class, Response.class, boolean.class });
			}
			catch (NoSuchMethodException e) {
				throw new CFXTagException(e);
			}
		}
	}

	@Override
	public void processRequest(Request request, Response response) throws Exception {

		processRequest.invoke(null, new Object[] { serverLibrary, procedure, request, response, keepAlive });
		// CFXNativeLib.processRequest(serverLibrary, procedure, request, response, keepAlive);
	}

}