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
 * Implements the CFML Function lscurrencyformat
 */
package lucee.runtime.functions.international;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public final class LSCurrencyFormat extends BIF {
	private static final char NBSP = (char) 160;
	private static final long serialVersionUID = -3173006221339130136L;

	public static String call(PageContext pc, Object number) throws PageException {
		return format(toDouble(number), "local", pc.getLocale());
	}

	public static String call(PageContext pc, Object number, String type) throws PageException {
		return format(toDouble(number), type, pc.getLocale());
	}

	public static String call(PageContext pc, Object number, String type, Locale locale) throws PageException {
		return format(toDouble(number), type, locale == null ? pc.getLocale() : locale);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 3) return call(pc, args[0], Caster.toString(args[1]), Caster.toLocale(args[2]));
		if (args.length == 2) return call(pc, args[0], Caster.toString(args[1]));
		if (args.length == 1) return call(pc, args[0]);

		throw new FunctionException(pc, "LSCurrencyFormat", 1, 3, args.length);
	}

	public static String format(double number, String type, Locale locale) throws ExpressionException {
		if (StringUtil.isEmpty(type)) return local(locale, number);
		type = type.trim().toLowerCase();

		if (type.equals("none")) return none(locale, number);
		else if (type.equals("local")) return local(locale, number);
		else if (type.equals("international")) return international(locale, number);
		else {
			throw new ExpressionException("invalid type for function lsCurrencyFormat", "types are: local, international or none");
		}

	}

	public static String none(Locale locale, double number) {
		NumberFormat nf = NumberFormat.getCurrencyInstance(locale);
		return clean(StringUtil.replace(nf.format(number), nf.getCurrency().getSymbol(locale), "", false));
	}

	public static String local(Locale locale, double number) {
		return clean(NumberFormat.getCurrencyInstance(locale).format(number));
	}

	public static String international(Locale locale, double number) {
		NumberFormat nf = NumberFormat.getCurrencyInstance(locale);
		Currency currency = nf.getCurrency();
		String str = clean(StringUtil.replace(nf.format(number), nf.getCurrency().getSymbol(locale), "", false));
		return currency.getCurrencyCode() + " " + str;
	}

	private static String clean(String str) {
		// Java 10 returns nbsp instead of a regular space
		return str.replace(NBSP, ' ').trim();
	}

	public static double toDouble(Object number) throws PageException {
		if (number instanceof String && ((String) number).length() == 0) return 0d;
		return Caster.toDoubleValue(number);
	}
}