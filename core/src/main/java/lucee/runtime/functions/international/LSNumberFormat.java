/**
 *
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.
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
 * Implements the CFML Function lsnumberformat
 */
package lucee.runtime.functions.international;

import java.util.Locale;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.util.InvalidMaskException;
import lucee.runtime.util.NumberFormat.Mask;

public final class LSNumberFormat extends BIF {

	private static final long serialVersionUID = -7981883050285346336L;

	public static String call(PageContext pc, Object object) throws PageException {
		return _call(pc, object, null, pc.getLocale());
	}

	public static String call(PageContext pc, Object object, String mask) throws PageException {
		return _call(pc, object, mask, pc.getLocale());
	}

	public static String call(PageContext pc, Object object, String mask, Locale locale) throws PageException {
		return _call(pc, object, mask, locale == null ? pc.getLocale() : locale);
	}

	private static String _call(PageContext pc, Object object, String mask, Locale locale) throws PageException {

		try {

			lucee.runtime.util.NumberFormat formatter = new lucee.runtime.util.NumberFormat();

			if (mask == null) return formatter.format(locale, lucee.runtime.functions.displayFormatting.NumberFormat.toNumber(pc, object, 0));
			Mask m = lucee.runtime.util.NumberFormat.convertMask(mask);
			double number = lucee.runtime.functions.displayFormatting.NumberFormat.toNumber(pc, object, m.right);
			return formatter.format(locale, number, m);
		}
		catch (InvalidMaskException e) {
			throw new FunctionException(pc, "lsnumberFormat", 1, "number", e.getMessage());
		}
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 3) return call(pc, args[0], Caster.toString(args[1]), Caster.toLocale(args[2]));
		if (args.length == 2) return call(pc, args[0], Caster.toString(args[1]));
		if (args.length == 1) return call(pc, args[0]);

		throw new FunctionException(pc, "LSNumberFormat", 1, 3, args.length);
	}
}