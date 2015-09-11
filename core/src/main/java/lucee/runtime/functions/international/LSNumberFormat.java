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
/** DIFF 23
 * Implements the CFML Function lsnumberformat
 */
package lucee.runtime.functions.international;

import java.util.Locale;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.functions.displayFormatting.NumberFormat;
import lucee.runtime.util.InvalidMaskException;

public final class LSNumberFormat implements Function {
	
	private static final long serialVersionUID = -7981883050285346336L;

	public static String call(PageContext pc , Object object) throws PageException {
		return _call(pc, object, null, pc.getLocale());
	}
	
	public static String call(PageContext pc , Object object, String mask) throws PageException {
	    return _call(pc, object, mask, pc.getLocale());
	}
	
	public static String call(PageContext pc , Object object, String mask, Locale locale) throws PageException {
		return _call(pc, object, mask, 
				locale==null?pc.getLocale():locale);
	}
	
	private static String _call(PageContext pc , Object object, String mask, Locale locale) throws PageException {
		try {
            if(mask==null) 
            	return new lucee.runtime.util.NumberFormat().format(locale,NumberFormat.toNumber(pc,object));
			return new lucee.runtime.util.NumberFormat().format(locale,NumberFormat.toNumber(pc,object),mask);
        } 
        catch (InvalidMaskException e) {
            throw new FunctionException(pc,"lsnumberFormat",1,"number",e.getMessage());
        }
	}
}