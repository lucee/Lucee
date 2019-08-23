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
 * Implements the CFML Function lsisdate
 */
package lucee.runtime.functions.international;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import lucee.runtime.PageContext;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Decision;

public final class LSIsDate implements Function {

	private static final long serialVersionUID = -8517171925554806088L;

	public static boolean call(PageContext pc, Object object) {
		return _call(pc, object, pc.getLocale(), pc.getTimeZone());
	}

	public static boolean call(PageContext pc, Object object, Locale locale) {
		return _call(pc, object, locale, pc.getTimeZone());
	}

	public static boolean call(PageContext pc, Object object, Locale locale, TimeZone tz) {
		return _call(pc, object, locale == null ? pc.getLocale() : locale, tz == null ? pc.getTimeZone() : tz);
	}

	private static boolean _call(PageContext pc, Object object, Locale locale, TimeZone tz) {
		if (object instanceof Date) return true;
		else if (object instanceof String) {
			String str = object.toString();
			if (str.length() < 2) return false;
			// print.out(Caster.toDateTime(locale,str,pc.getTimeZone(),null));
			return Decision.isDate(str, locale, tz, locale.equals(Locale.US));
			// return Caster.toDateTime(locale,str,pc.getTimeZone(),null)!=null;
		}
		return false;
	}

}