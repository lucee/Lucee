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
package lucee.runtime.functions.owasp;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

public class EncodeForLDAP extends BIF {

	private static final long serialVersionUID = 946895856040970887L;

	public static String call(PageContext pc , String item, boolean canonicalize) throws PageException  {
		return ESAPIEncode.encode(item, ESAPIEncode.ENC_LDAP,canonicalize);
	}
	
	public static String call(PageContext pc , String item) throws PageException  {
		return call(pc, item, false);
	}
	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if(args.length==1) return call(pc,Caster.toString(args[0]));
		if(args.length==2) return call(pc,Caster.toString(args[0]),Caster.toBooleanValue(args[1]));
		throw new FunctionException(pc, "EncodeForJavaScript", 1, 2, args.length);
	}

}