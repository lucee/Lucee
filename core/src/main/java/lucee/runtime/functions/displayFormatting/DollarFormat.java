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
 * Implements the CFML Function dollarformat
 */
package lucee.runtime.functions.displayFormatting;

import java.util.Locale;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.functions.international.LSCurrencyFormat;
import lucee.runtime.op.Caster;

public final class DollarFormat extends BIF {

	private static final long serialVersionUID = -9006485495830329674L;

	public static String call(PageContext pc, String strDollar) throws PageException {
		if (StringUtil.isEmpty(strDollar)) strDollar = "0";// "$0,00";
		return LSCurrencyFormat.call(pc, strDollar, "local", Locale.US);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));
		throw new FunctionException(pc, "DollarFormat", 1, 1, args.length);
	}
}