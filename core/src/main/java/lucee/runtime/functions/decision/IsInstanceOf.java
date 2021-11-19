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
/**
 * Implements the CFML Function isdate
 */
package lucee.runtime.functions.decision;

import java.io.IOException;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.java.JavaObject;
import lucee.runtime.op.Caster;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.type.ObjectWrap;

public final class IsInstanceOf implements Function {
	public static boolean call(PageContext pc, Object obj, String typeName) throws PageException {
		if (obj instanceof Component) return ((Component) obj).instanceOf(typeName);
		if (obj instanceof JavaObject) {
			try {
				return Reflector.isInstaneOf(((PageContextImpl) pc).getClassLoader(), ((JavaObject) obj).getClazz(), typeName);
			}
			catch (IOException ioe) {
				throw Caster.toPageException(ioe);
			}
		}
		if (obj instanceof ObjectWrap) return call(pc, ((ObjectWrap) obj).getEmbededObject(), typeName);

		try {
			return Reflector.isInstaneOf(((PageContextImpl) pc).getClassLoader(), obj.getClass(), typeName);
		}
		catch (IOException ioe) {
			throw Caster.toPageException(ioe);
		}

	}
}