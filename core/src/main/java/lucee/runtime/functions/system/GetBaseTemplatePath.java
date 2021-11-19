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
 * Implements the CFML Function getbasetemplatepath
 */
package lucee.runtime.functions.system;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;

public final class GetBaseTemplatePath extends BIF {

	private static final long serialVersionUID = -6810643607690049685L;

	public static String call(PageContext pc) throws PageException {
		// pc.getTemplatePath();
		PageSource ps = pc.getBasePageSource();
		if (ps == null) {
			ps = ((PageContextImpl) pc).getPageSource(1);
			if (ps == null) throw new ApplicationException("current context does not have a template it is based on");
		}
		return ps.getResourceTranslated(pc).getAbsolutePath();
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 0) return call(pc);
		throw new FunctionException(pc, "GetBaseTemplatePath", 0, 0, args.length);
	}
}