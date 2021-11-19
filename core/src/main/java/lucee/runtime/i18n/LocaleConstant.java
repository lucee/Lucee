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

import java.util.Locale;

public class LocaleConstant {

	public static final Locale ALBANIAN_ALBANIA = new Locale("sq", "AL");

	public static final Locale ARABIC_ALGERIA = new Locale("ar", "DZ");
	public static final Locale ARABIC_BAHRAIN = new Locale("ar", "BH");
	public static final Locale ARABIC_EGYPT = new Locale("ar", "EG");
	public static final Locale ARABIC_IRAQ = new Locale("ar", "IQ");
	public static final Locale ARABIC_JORDAN = new Locale("ar", "JO");
	public static final Locale ARABIC_KUWAIT = new Locale("ar", "KW");
	public static final Locale ARABIC_LEBANON = new Locale("ar", "LB");
	public static final Locale ARABIC_LIBYA = new Locale("ar", "LY");
	public static final Locale ARABIC_MAROCCO = new Locale("ar", "MA");
	public static final Locale ARABIC_OMAN = new Locale("ar", "OM");
	public static final Locale ARABIC_QATAR = new Locale("ar", "QA");
	public static final Locale ARABIC_SAUDI_ARABIA = new Locale("ar", "SA");
	public static final Locale ARABIC_SUDAN = new Locale("ar", "SD");
	public static final Locale ARABIC_SYRIA = new Locale("ar", "SY");
	public static final Locale ARABIC_TUNISIA = new Locale("ar", "TN");
	public static final Locale ARABIC_UNITED_ARAB_EMIRATES = new Locale("ar", "AE");
	public static final Locale ARABIC_YEMEN = new Locale("ar", "YE");

	public static final Locale CHINESE_HONG_KONG = new Locale("zh", "HK");
	public static final Locale CHINESE_SINGAPORE = new Locale("zh", "SG");
	public static final Locale CHINESE_TAIWAN = new Locale("zh", "TW");

	public static final Locale DUTCH_BELGIUM = new Locale("nl", "BE");
	public static final Locale DUTCH_NETHERLANDS = new Locale("nl", "NL");

	public static final Locale ENGLISH_AUSTRALIA = new Locale("en", "AU");
	public static final Locale ENGLISH_CANADA = new Locale("en", "CA");
	public static final Locale ENGLISH_NEW_ZEALAND = new Locale("en", "NZ");
	public static final Locale ENGLISH_UNITED_KINDOM = new Locale("en", "GB");
	public static final Locale ENGLISH_UNITED_STATES = new Locale("en", "US");
	public static final Locale PORTUGUESE_PORTUGAL = new Locale("pt", "PT");
	public static final Locale PORTUGUESE_BRASIL = new Locale("pt", "BR");

	/*
	 * static { setLocalAlias("albanian (albania)", LocaleConstant.ALBANIAN_ALBANIA);
	 * 
	 * setLocalAlias("arabic (algeria)", LocaleConstant.ARABIC_ALGERIA);
	 * setLocalAlias("arabic (bahrain)", LocaleConstant.ARABIC_BAHRAIN); setLocalAlias("arabic (egypt)",
	 * LocaleConstant.ARABIC_EGYPT); setLocalAlias("arabic (iraq)", LocaleConstant.ARABIC_IRAQ);
	 * setLocalAlias("arabic (jordan)", LocaleConstant.ARABIC_JORDAN); setLocalAlias("arabic (kuwait)",
	 * LocaleConstant.ARABIC_KUWAIT); setLocalAlias("arabic (lebanon)", LocaleConstant.ARABIC_LEBANON);
	 * setLocalAlias("arabic (libya)", LocaleConstant.ARABIC_LIBYA); setLocalAlias("arabic (morocco)",
	 * LocaleConstant.ARABIC_MAROCCO); setLocalAlias("arabic (oman)", LocaleConstant.ARABIC_OMAN);
	 * setLocalAlias("arabic (qatar)", LocaleConstant.ARABIC_QATAR);
	 * setLocalAlias("arabic (saudi arabia)", LocaleConstant.ARABIC_SAUDI_ARABIA);
	 * setLocalAlias("arabic (sudan)", LocaleConstant.ARABIC_SUDAN); setLocalAlias("arabic (syria)",
	 * LocaleConstant.ARABIC_SYRIA); setLocalAlias("arabic (tunisia)", LocaleConstant.ARABIC_TUNISIA);
	 * setLocalAlias("arabic (united arab emirates)", LocaleConstant.ARABIC_UNITED_ARAB_EMIRATES);
	 * setLocalAlias("arabic (yemen)", LocaleConstant.ARABIC_YEMEN);
	 * 
	 * setLocalAlias("chinese (china)", Locale.CHINA);
	 * setLocalAlias("chinese (hong kong)",LocaleConstant.CHINESE_HONG_KONG);
	 * setLocalAlias("chinese (singapore)",LocaleConstant.CHINESE_SINGAPORE);
	 * setLocalAlias("chinese (taiwan)",LocaleConstant.CHINESE_TAIWAN);
	 * setLocalAlias("dutch (belgian)",LocaleConstant.DUTCH_BELGIUM);
	 * setLocalAlias("dutch (belgium)",LocaleConstant.DUTCH_BELGIUM);
	 * setLocalAlias("dutch (standard)",LocaleConstant.DUTCH_NETHERLANDS);
	 * setLocalAlias("english (australian)",LocaleConstant.ENGLISH_AUSTRALIA);
	 * setLocalAlias("english (australia)",LocaleConstant.ENGLISH_AUSTRALIA);
	 * setLocalAlias("english (canadian)",LocaleConstant.ENGLISH_CANADA);
	 * setLocalAlias("english (canada)",LocaleConstant.ENGLISH_CANADA);
	 * setLocalAlias("english (new zealand)",LocaleConstant.ENGLISH_NEW_ZEALAND);
	 * setLocalAlias("english (uk)",LocaleConstant.ENGLISH_UNITED_KINDOM);
	 * setLocalAlias("english (united kingdom)",LocaleConstant.ENGLISH_UNITED_KINDOM);
	 * setLocalAlias("english (gb)",LocaleConstant.ENGLISH_UNITED_KINDOM);
	 * setLocalAlias("english (great britan)",LocaleConstant.ENGLISH_UNITED_KINDOM);
	 * setLocalAlias("english (us)",LocaleConstant.ENGLISH_UNITED_STATES);
	 * setLocalAlias("english (united states)",LocaleConstant.ENGLISH_UNITED_STATES);
	 * setLocalAlias("english (united states of america)",LocaleConstant.ENGLISH_UNITED_STATES);
	 * setLocalAlias("english (usa)",LocaleConstant.ENGLISH_UNITED_STATES);
	 * setLocalAlias("french (belgium)",new Locale("fr","BE")); setLocalAlias("french (belgian)",new
	 * Locale("fr","BE")); setLocalAlias("french (canadian)",new Locale("fr","CA"));
	 * setLocalAlias("french (canadia)",new Locale("fr","CA")); setLocalAlias("french (standard)",new
	 * Locale("fr","FRA")); setLocalAlias("french (swiss)",new Locale("fr","CH"));
	 * setLocalAlias("german (austrian)",new Locale("de","AT")); setLocalAlias("german (austria)",new
	 * Locale("de","AT")); setLocalAlias("german (standard)",new Locale("de","DE"));
	 * setLocalAlias("german (swiss)",new Locale("de","CH")); setLocalAlias("italian (standard)",new
	 * Locale("it","IT")); setLocalAlias("italian (swiss)",new Locale("it","CH"));
	 * setLocalAlias("japanese",new Locale("ja","JP")); setLocalAlias("korean",Locale.KOREAN);
	 * setLocalAlias("norwegian (bokmal)",new Locale("no","NO"));
	 * setLocalAlias("norwegian (nynorsk)",new Locale("no","NO"));
	 * setLocalAlias("portuguese (brazilian)",LocaleConstant.PORTUGUESE_BRASIL);
	 * setLocalAlias("portuguese (brazil)",new LocaleConstant.PORTUGUESE_BRASIL);
	 * setLocalAlias("portuguese (standard)",LocaleConstant.PORTUGUESE_PORTUGAL);
	 * setLocalAlias("rhaeto-romance (swiss)",new Locale("rm","CH"));
	 * locales.put("rhaeto-romance (swiss)",new Locale("rm","CH")); setLocalAlias("spanish (modern)",new
	 * Locale("es","ES")); setLocalAlias("spanish (standard)",new Locale("es","ES"));
	 * setLocalAlias("swedish",new Locale("sv","SE")); } private static void setLocalAlias(String
	 * string, Locale china) {
	 * 
	 * }
	 */

	// TODO add all from http://www.oracle.com/technetwork/java/javase/locales-137662.html

}