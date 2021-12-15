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
package lucee.runtime.functions.international;

import java.util.Locale;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;

/**
 * Implements the CFML Function lsparseeurocurrency
 */
public final class LSParseEuroCurrency implements Function {

	private static final long serialVersionUID = -4153683932862857234L;

	public static String call(PageContext pc, String string) throws PageException {
		return LSParseCurrency.call(pc, string);
	}

	public static String call(PageContext pc, String string, Locale locale) throws PageException {
		return LSParseCurrency.call(pc, string, locale);
	}
}