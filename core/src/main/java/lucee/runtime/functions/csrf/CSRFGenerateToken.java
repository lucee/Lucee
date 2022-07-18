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
package lucee.runtime.functions.csrf;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.scope.CSRFTokenSupport;
import lucee.runtime.type.scope.Session;
import lucee.runtime.type.scope.storage.StorageScope;

public class CSRFGenerateToken implements Function {

	private static final long serialVersionUID = -2411153524245619987L;

	public static String call(PageContext pc) throws PageException {
		return call(pc, null, false);
	}

	public static String call(PageContext pc, String key) throws PageException {
		return call(pc, key, false);
	}

	public static String call(PageContext pc, String key, boolean forceNew) throws PageException {
		return getStorageScope(pc).generateToken(key, forceNew);
	}

	public static CSRFTokenSupport getStorageScope(PageContext pc) throws PageException {
		Session session = pc.sessionScope();
		if (!(session instanceof CSRFTokenSupport)) throw new ExpressionException("Session scope does not support CSRF Tokens");
		return (CSRFTokenSupport) session;
	}
}