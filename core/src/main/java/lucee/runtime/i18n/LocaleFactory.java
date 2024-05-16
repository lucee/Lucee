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

package lucee.runtime.i18n;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lucee.runtime.exp.ExpressionException;
import lucee.runtime.type.util.ListUtil;

/**
 * Factory to create Locales by CFML rules
 */
public final class LocaleFactory {
	// private static Pattern localePattern =
	// Pattern.compile("^\\s*([^\\s\\(]+)\\s*(\\(\\s*([^\\s\\)]+)\\s*\\))?\\s*$");
	private static Pattern localePattern = Pattern.compile("^\\s*([^\\(]+)\\s*(\\(\\s*([^\\)]+)\\s*\\))?\\s*$");
	private static Pattern localePattern2 = Pattern.compile("^([a-z]{2})_([a-z]{2,3})$");
	private static Pattern localePattern3 = Pattern.compile("^([a-z]{2})_([a-z]{2,3})_([a-z]{2,})$");

	private static Map<String, Locale> locales = new LinkedHashMap<String, Locale>();
	private static Map<String, Locale> localeAlias = new LinkedHashMap<String, Locale>();

	private static String list;
	static {
		Locale[] ls = Locale.getAvailableLocales();

		String key;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ls.length; i++) {
			key = ls[i].getDisplayName(Locale.US).toLowerCase();
			locales.put(key, ls[i]);
			if (key.indexOf(',') != -1) {
				key = ls[i].toString();
				// print.ln(key);

			}
			if (i > 0) sb.append(",");
			sb.append(key);
		}
		list = sb.toString();

		setLocalAlias("albanian (albania)", LocaleConstant.ALBANIAN_ALBANIA);

		setLocalAlias("arabic (algeria)", LocaleConstant.ARABIC_ALGERIA);
		setLocalAlias("arabic (bahrain)", LocaleConstant.ARABIC_BAHRAIN);
		setLocalAlias("arabic (egypt)", LocaleConstant.ARABIC_EGYPT);
		setLocalAlias("arabic (iraq)", LocaleConstant.ARABIC_IRAQ);
		setLocalAlias("arabic (jordan)", LocaleConstant.ARABIC_JORDAN);
		setLocalAlias("arabic (kuwait)", LocaleConstant.ARABIC_KUWAIT);
		setLocalAlias("arabic (lebanon)", LocaleConstant.ARABIC_LEBANON);
		setLocalAlias("arabic (libya)", LocaleConstant.ARABIC_LIBYA);
		setLocalAlias("arabic (morocco)", LocaleConstant.ARABIC_MAROCCO);
		setLocalAlias("arabic (oman)", LocaleConstant.ARABIC_OMAN);
		setLocalAlias("arabic (qatar)", LocaleConstant.ARABIC_QATAR);
		setLocalAlias("arabic (saudi arabia)", LocaleConstant.ARABIC_SAUDI_ARABIA);
		setLocalAlias("arabic (sudan)", LocaleConstant.ARABIC_SUDAN);
		setLocalAlias("arabic (syria)", LocaleConstant.ARABIC_SYRIA);
		setLocalAlias("arabic (tunisia)", LocaleConstant.ARABIC_TUNISIA);
		setLocalAlias("arabic (united arab emirates)", LocaleConstant.ARABIC_UNITED_ARAB_EMIRATES);
		setLocalAlias("arabic (yemen)", LocaleConstant.ARABIC_YEMEN);

		setLocalAlias("chinese (china)", Locale.CHINA);
		setLocalAlias("chinese (hong kong)", LocaleConstant.CHINESE_HONG_KONG);
		setLocalAlias("chinese (singapore)", LocaleConstant.CHINESE_SINGAPORE);
		setLocalAlias("chinese (taiwan)", LocaleConstant.CHINESE_TAIWAN);
		setLocalAlias("dutch (belgian)", LocaleConstant.DUTCH_BELGIUM);
		setLocalAlias("dutch (belgium)", LocaleConstant.DUTCH_BELGIUM);
		setLocalAlias("dutch (standard)", LocaleConstant.DUTCH_NETHERLANDS);
		setLocalAlias("english (australian)", LocaleConstant.ENGLISH_AUSTRALIA);
		setLocalAlias("english (australia)", LocaleConstant.ENGLISH_AUSTRALIA);
		setLocalAlias("english (canadian)", LocaleConstant.ENGLISH_CANADA);
		setLocalAlias("english (canada)", LocaleConstant.ENGLISH_CANADA);
		setLocalAlias("english (new zealand)", LocaleConstant.ENGLISH_NEW_ZEALAND);
		setLocalAlias("english (uk)", LocaleConstant.ENGLISH_UNITED_KINDOM);
		setLocalAlias("english (united kingdom)", LocaleConstant.ENGLISH_UNITED_KINDOM);
		setLocalAlias("english (gb)", LocaleConstant.ENGLISH_UNITED_KINDOM);
		setLocalAlias("english (great britan)", LocaleConstant.ENGLISH_UNITED_KINDOM);
		setLocalAlias("english (us)", LocaleConstant.ENGLISH_UNITED_STATES);
		setLocalAlias("english (united states)", LocaleConstant.ENGLISH_UNITED_STATES);
		setLocalAlias("english (united states of america)", LocaleConstant.ENGLISH_UNITED_STATES);
		setLocalAlias("english (usa)", LocaleConstant.ENGLISH_UNITED_STATES);
		setLocalAlias("french (belgium)", new Locale("fr", "BE"));
		setLocalAlias("french (belgian)", new Locale("fr", "BE"));
		setLocalAlias("french (canadian)", new Locale("fr", "CA"));
		setLocalAlias("french (canadia)", new Locale("fr", "CA"));
		setLocalAlias("french (standard)", new Locale("fr", "FRA"));
		setLocalAlias("french (swiss)", new Locale("fr", "CH"));
		setLocalAlias("german (austrian)", new Locale("de", "AT"));
		setLocalAlias("german (austria)", new Locale("de", "AT"));
		setLocalAlias("german (standard)", new Locale("de", "DE"));
		setLocalAlias("german (swiss)", new Locale("de", "CH"));
		setLocalAlias("italian (standard)", new Locale("it", "IT"));
		setLocalAlias("italian (swiss)", new Locale("it", "CH"));
		setLocalAlias("japanese", new Locale("ja", "JP"));
		setLocalAlias("korean", Locale.KOREAN);
		setLocalAlias("norwegian (bokmal)", new Locale("no", "NO"));
		setLocalAlias("norwegian (nynorsk)", new Locale("no", "NO"));
		setLocalAlias("portuguese (brazilian)", LocaleConstant.PORTUGUESE_BRASIL);
		setLocalAlias("portuguese (brazil)", LocaleConstant.PORTUGUESE_BRASIL);
		setLocalAlias("portuguese (standard)", LocaleConstant.PORTUGUESE_PORTUGAL);
		setLocalAlias("rhaeto-romance (swiss)", new Locale("rm", "CH"));
		locales.put("rhaeto-romance (swiss)", new Locale("rm", "CH"));
		setLocalAlias("spanish (modern)", new Locale("es", "ES"));
		setLocalAlias("spanish (standard)", new Locale("es", "ES"));
		setLocalAlias("swedish", new Locale("sv", "SE"));
		setLocalAlias("welsh", new Locale("cy", "GB"));
	}

	private LocaleFactory() {
	}

	private static void setLocalAlias(String name, Locale locale) {
		if (!localeAlias.containsKey(name)) localeAlias.put(name, locale);
	}

	/**
	 * @param strLocale
	 * @param defaultValue
	 * @return return locale match to String
	 */
	public static Locale getLocale(String strLocale, Locale defaultValue) {
		try {
			return getLocale(strLocale);
		}
		catch (ExpressionException e) {
			return defaultValue;
		}
	}

	/**
	 * @param strLocale
	 * @return return locale match to String
	 * @throws ExpressionException
	 */
	public static Locale getLocale(String strLocale) throws ExpressionException {
		String strLocaleLC = strLocale.toLowerCase().trim();
		Locale l = locales.get(strLocaleLC);
		if (l != null) return l;

		l = localeAlias.get(strLocaleLC);
		if (l != null) return l;

		Matcher matcher = localePattern2.matcher(strLocaleLC);
		if (matcher.find()) {
			int len = matcher.groupCount();
			if (len == 2) {
				String lang = matcher.group(1).trim();
				String country = matcher.group(2).trim();
				Locale locale = new Locale(lang, country);

				try {
					locale.getISO3Language();
					setLocalAlias(strLocaleLC, locale);
					return locale;
				}
				catch (Exception e) {
				}
			}
		}

		matcher = localePattern3.matcher(strLocaleLC);
		if (matcher.find()) {
			int len = matcher.groupCount();
			if (len == 3) {
				String lang = matcher.group(1).trim();
				String country = matcher.group(2).trim();
				String variant = matcher.group(3).trim();
				Locale locale = new Locale(lang, country, variant);

				try {
					locale.getISO3Language();
					setLocalAlias(strLocaleLC, locale);
					return locale;
				}
				catch (Exception e) {
				}
			}
		}

		matcher = localePattern.matcher(strLocaleLC);
		if (matcher.find()) {
			int len = matcher.groupCount();

			if (len == 3) {

				String lang = matcher.group(1).trim();
				String country = matcher.group(3);
				if (country != null) country = country.trim();
				Object objLocale = null;

				if (country != null) objLocale = locales.get(lang.toLowerCase() + " (" + (country.toLowerCase()) + ")");
				else objLocale = locales.get(lang.toLowerCase());
				if (objLocale != null) return (Locale) objLocale;

				Locale locale;
				if (country != null) locale = new Locale(lang.toUpperCase(), country.toLowerCase());
				else locale = new Locale(lang);

				try {
					locale.getISO3Language();
				}
				catch (Exception e) {
					if (strLocale.indexOf('-') != -1) return getLocale(strLocale.replace('-', '_'));
					throw new ExpressionException("unsupported Locale [" + strLocale + "]", "supported Locales are:" + getSupportedLocalesAsString());
				}
				setLocalAlias(strLocaleLC, locale);
				return locale;

			}
		}

		throw new ExpressionException("can't cast value (" + strLocale + ") to a Locale", "supported Locales are:" + getSupportedLocalesAsString());
	}

	private static String getSupportedLocalesAsString() {
		// TODO chnge from ArryObject to string
		String[] arr = locales.keySet().toArray(new String[locales.size()]);
		Arrays.sort(arr);
		return ListUtil.arrayToList(arr, ",");

	}

	/**
	 * @param locale
	 * @return cast a Locale to a String
	 */
	public static String getDisplayName(Locale locale) {
		String lang = locale.getLanguage();
		String country = locale.getCountry();

		synchronized (localeAlias) {
			Iterator<Entry<String, Locale>> it = localeAlias.entrySet().iterator();
			Map.Entry<String, Locale> entry;
			while (it.hasNext()) {
				entry = it.next();
				// Object qkey=it.next();
				Locale curr = entry.getValue();
				if (lang.equals(curr.getLanguage()) && country.equals(curr.getCountry())) {
					return entry.getKey().toString();
				}
			}
		}
		return locale.getDisplayName(Locale.ENGLISH);
	}

	public static String toString(Locale locale) {
		if (locale == null) return "";
		return locale.toString();
		// return getDisplayName(locale);
	}

	/**
	 * @return Returns the locales.
	 */
	public static Map<String, Locale> getLocales() {
		return locales;
	}

	public static String getLocaleList() {
		return list;
	}
}