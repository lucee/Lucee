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
 * Implements the CFML Function lsparsecurrency
 */
package lucee.runtime.functions.international;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Currency;
import java.util.Locale;
import java.util.WeakHashMap;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

public final class LSParseCurrency implements Function {

	private static final long serialVersionUID = -7023441119083818436L;
	private static WeakHashMap currFormatter = new WeakHashMap();
	private static WeakHashMap numbFormatter = new WeakHashMap();

	public static String call(PageContext pc, String string) throws PageException {
		return Caster.toString(toDoubleValue(pc.getLocale(), string, false));
	}

	public static String call(PageContext pc, String string, Locale locale) throws PageException {
		return Caster.toString(toDoubleValue(locale == null ? pc.getLocale() : locale, string, false));
	}

	public static double toDoubleValue(Locale locale, String str) throws PageException {
		return toDoubleValue(locale, str, false);
	}

	public static double toDoubleValue(Locale locale, String str, boolean strict) throws PageException {

		str = str.trim();

		NumberFormat cnf = getCurrencyInstance(locale);
		Currency currency = cnf.getCurrency();

		if (currency.getCurrencyCode().equals("XXX")) throw new ExpressionException("Unknown currency [" + locale.toString() + "]");

		cnf.setParseIntegerOnly(false);
		try {
			return cnf.parse(str).doubleValue();
		}
		catch (ParseException e) {

			String stripped = str.replace(currency.getSymbol(locale), "").replace(currency.getCurrencyCode(), "");

			NumberFormat nf = getInstance(locale);
			ParsePosition pp = new ParsePosition(0);
			Number n = nf.parse(stripped, pp);

			if (n == null || pp.getIndex() == 0 || (strict && stripped.length() != pp.getIndex()))
				throw new ExpressionException(String.format("Unparseable value [%s] for currency %s", str, locale.toString()));

			return n.doubleValue();
		}
	}

	private static NumberFormat getInstance(Locale locale) {
		Object o = numbFormatter.get(locale);
		if (o != null) return (NumberFormat) o;

		NumberFormat nf = NumberFormat.getInstance(locale);
		numbFormatter.put(locale, nf);
		return nf;
	}

	private static NumberFormat getCurrencyInstance(Locale locale) {
		Object o = currFormatter.get(locale);
		if (o != null) return (NumberFormat) o;

		NumberFormat nf = NumberFormat.getCurrencyInstance(locale);
		currFormatter.put(locale, nf);
		return nf;
	}
}