/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
/**
 * Implements the CFML Function formatbasen
 */
package lucee.runtime.functions.displayFormatting;

import java.util.Locale;

import lucee.runtime.PageContext;
import lucee.runtime.ext.function.Function;

public final class GetLocaleDisplayName implements Function {

	private static final long serialVersionUID = -4084704416496042957L;

	public static String call(PageContext pc) {
		return _call(pc, pc.getLocale(), pc.getLocale());
	}

	public static String call(PageContext pc, Locale locale) {
		return _call(pc, locale, locale);
	}

	public static String call(PageContext pc, Locale locale, Locale dspLocale) {
		return _call(pc, locale, dspLocale);
	}

	private static String _call(PageContext pc, Locale locale, Locale dspLocale) {
		if (locale == null) locale = pc.getLocale();
		if (dspLocale == null) dspLocale = locale;
		return locale.getDisplayName(dspLocale);
	}

}