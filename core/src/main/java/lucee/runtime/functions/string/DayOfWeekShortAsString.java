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
 * Implements the CFML Function dayofweekasstring
 */
package lucee.runtime.functions.string;

import java.util.Locale;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.ext.function.Function;

public final class DayOfWeekShortAsString implements Function {

	private static final long serialVersionUID = 3088890446888229079L;

	public static String call(PageContext pc , double dow) throws ExpressionException {
		return DayOfWeekAsString.call(pc,dow, pc.getLocale(),false);
	}
	
	public static String call(PageContext pc , double dow, Locale locale) throws ExpressionException {
		return DayOfWeekAsString.call(pc,dow, locale==null?pc.getLocale():locale,false);
	}
	
}