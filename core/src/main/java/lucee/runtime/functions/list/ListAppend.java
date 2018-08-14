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
 * Implements the CFML Function listappend
 */
package lucee.runtime.functions.list;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public final class ListAppend extends BIF {

	private static final long serialVersionUID = -4893447489733907241L;

	public static String call(PageContext pc , String list, String value, String delimiter) {
		if(list.length()==0) return value;
        switch(delimiter.length()) {
        case 0:return list;
        case 1:return new StringBuilder(list).append(delimiter).append(value).toString();
        }
        return new StringBuilder(list).append(delimiter.charAt(0)).append(value).toString();
	}

    @Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
    	if(args.length==2)
			return call(pc, Caster.toString(args[0]), Caster.toString(args[1]),",");
    	if(args.length==3)
			return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]));

		throw new FunctionException(pc, "ListAppend", 2, 3, args.length);
	}
	
}