/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public final class GetLocaleInfo implements Function {

	private static final long serialVersionUID = -4084704416496042957L;

	public static Struct call(PageContext pc) {
		return _call(pc, pc.getLocale(), pc.getLocale());
	}

	public static Struct call(PageContext pc, Locale locale) {
		return _call(pc, locale, locale);
	}

	public static Struct call(PageContext pc, Locale locale, Locale dspLocale) {
		return _call(pc, locale, dspLocale);
	}

	private static Struct _call(PageContext pc, Locale locale, Locale dspLocale) {
		if (locale == null) locale = pc.getLocale();
		if (dspLocale == null) dspLocale = locale;

		Struct sct = new StructImpl();
		Struct dsp = new StructImpl();
		sct.setEL(KeyConstants._display, dsp);
		dsp.setEL(KeyConstants._country, locale.getDisplayCountry(dspLocale));
		dsp.setEL(KeyConstants._language, locale.getDisplayLanguage(dspLocale));

		sct.setEL(KeyConstants._country, locale.getCountry());
		sct.setEL(KeyConstants._language, locale.getLanguage());

		sct.setEL(KeyConstants._name, locale.getDisplayName(dspLocale));
		// MMMUST sct.setEL(KeyConstants._script, locale.getDisplayScript(dspLocale));
		sct.setEL("variant", locale.getDisplayVariant(dspLocale));

		Struct iso = new StructImpl();
		sct.setEL("iso", iso);
		iso.setEL(KeyConstants._country, locale.getISO3Country());
		iso.setEL(KeyConstants._language, locale.getISO3Language());

		return sct;
	}

}