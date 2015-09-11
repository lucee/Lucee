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
 * Implements the CFML Function listprepend
 */
package lucee.runtime.functions.list;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public final class ListPrepend extends BIF {

	private static final long serialVersionUID = -4252541560957800011L;
	
	public static String call(PageContext pc , String list, String value) {
		if(list.length()==0) return value;
		return new StringBuilder(value).append(',').append(list).toString();
	}
	public static String call(PageContext pc , String list, String value, String delimiter) {
		if(list.length()==0) return value;
		if(StringUtil.isEmpty(delimiter)) {
		    return call(pc,list,value);
        }
        return new StringBuilder(value).append(delimiter.charAt(0)).append(list).toString();
	}

    @Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
    	if(args.length==2)
			return call(pc, Caster.toString(args[0]), Caster.toString(args[1]));
		if(args.length==3)
			return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]));

		throw new FunctionException(pc, "ListPrepend", 1, 3, args.length);
	}
}