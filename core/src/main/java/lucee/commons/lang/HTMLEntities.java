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
package lucee.commons.lang;

import lucee.runtime.util.HTMLUtil;

/**
 * <p>
 * Provides HTML and XML entity utilities.
 * </p>
 *
 */
public final class HTMLEntities {

	public static final short HTMLV20 = HTMLUtil.HTMLV20;
	public static final short HTMLV32 = HTMLUtil.HTMLV32;
	public static final short HTMLV40 = HTMLUtil.HTMLV40;

	// Basic
	private static final int OFFSET_BASIC = 34;
	private static final String[] BASIC_ARRAY = new String[63 - OFFSET_BASIC];
	static {
		BASIC_ARRAY[34 - OFFSET_BASIC] = "quot";
		BASIC_ARRAY[38 - OFFSET_BASIC] = "amp";
		BASIC_ARRAY[60 - OFFSET_BASIC] = "lt";
		BASIC_ARRAY[62 - OFFSET_BASIC] = "gt";
	}
	// HTML 32
	private static final int OFFSET_ISO8859_1 = 160;
	private static final String[] ISO8859_1_ARRAY = new String[256 - OFFSET_ISO8859_1];
	static {
		ISO8859_1_ARRAY[160 - OFFSET_ISO8859_1] = "nbsp";
		ISO8859_1_ARRAY[161 - OFFSET_ISO8859_1] = "iexcl";
		ISO8859_1_ARRAY[162 - OFFSET_ISO8859_1] = "cent";
		ISO8859_1_ARRAY[163 - OFFSET_ISO8859_1] = "pound";
		ISO8859_1_ARRAY[164 - OFFSET_ISO8859_1] = "curren";
		ISO8859_1_ARRAY[165 - OFFSET_ISO8859_1] = "yen";
		ISO8859_1_ARRAY[166 - OFFSET_ISO8859_1] = "brvbar";
		ISO8859_1_ARRAY[167 - OFFSET_ISO8859_1] = "sect";
		ISO8859_1_ARRAY[168 - OFFSET_ISO8859_1] = "uml";
		ISO8859_1_ARRAY[169 - OFFSET_ISO8859_1] = "copy";
		ISO8859_1_ARRAY[170 - OFFSET_ISO8859_1] = "ordf";
		ISO8859_1_ARRAY[171 - OFFSET_ISO8859_1] = "laquo";
		ISO8859_1_ARRAY[172 - OFFSET_ISO8859_1] = "not";
		ISO8859_1_ARRAY[173 - OFFSET_ISO8859_1] = "shy";
		ISO8859_1_ARRAY[174 - OFFSET_ISO8859_1] = "reg";
		ISO8859_1_ARRAY[175 - OFFSET_ISO8859_1] = "macr";
		ISO8859_1_ARRAY[176 - OFFSET_ISO8859_1] = "deg";
		ISO8859_1_ARRAY[177 - OFFSET_ISO8859_1] = "plusmn";
		ISO8859_1_ARRAY[178 - OFFSET_ISO8859_1] = "sup2";
		ISO8859_1_ARRAY[179 - OFFSET_ISO8859_1] = "sup3";
		ISO8859_1_ARRAY[180 - OFFSET_ISO8859_1] = "acute";
		ISO8859_1_ARRAY[181 - OFFSET_ISO8859_1] = "micro";
		ISO8859_1_ARRAY[182 - OFFSET_ISO8859_1] = "para";
		ISO8859_1_ARRAY[183 - OFFSET_ISO8859_1] = "middot";
		ISO8859_1_ARRAY[184 - OFFSET_ISO8859_1] = "cedil";
		ISO8859_1_ARRAY[185 - OFFSET_ISO8859_1] = "sup1";
		ISO8859_1_ARRAY[186 - OFFSET_ISO8859_1] = "ordm";
		ISO8859_1_ARRAY[187 - OFFSET_ISO8859_1] = "raquo";
		ISO8859_1_ARRAY[188 - OFFSET_ISO8859_1] = "frac14";
		ISO8859_1_ARRAY[189 - OFFSET_ISO8859_1] = "frac12";
		ISO8859_1_ARRAY[190 - OFFSET_ISO8859_1] = "frac34";
		ISO8859_1_ARRAY[191 - OFFSET_ISO8859_1] = "iquest";
		ISO8859_1_ARRAY[192 - OFFSET_ISO8859_1] = "Agrave";
		ISO8859_1_ARRAY[193 - OFFSET_ISO8859_1] = "Aacute";
		ISO8859_1_ARRAY[194 - OFFSET_ISO8859_1] = "Acirc";
		ISO8859_1_ARRAY[195 - OFFSET_ISO8859_1] = "Atilde";
		ISO8859_1_ARRAY[196 - OFFSET_ISO8859_1] = "Auml";
		ISO8859_1_ARRAY[197 - OFFSET_ISO8859_1] = "Aring";
		ISO8859_1_ARRAY[198 - OFFSET_ISO8859_1] = "AElig";
		ISO8859_1_ARRAY[199 - OFFSET_ISO8859_1] = "Ccedil";
		ISO8859_1_ARRAY[200 - OFFSET_ISO8859_1] = "Egrave";
		ISO8859_1_ARRAY[201 - OFFSET_ISO8859_1] = "Eacute";
		ISO8859_1_ARRAY[202 - OFFSET_ISO8859_1] = "Ecirc";
		ISO8859_1_ARRAY[203 - OFFSET_ISO8859_1] = "Euml";
		ISO8859_1_ARRAY[204 - OFFSET_ISO8859_1] = "Igrave";
		ISO8859_1_ARRAY[205 - OFFSET_ISO8859_1] = "Iacute";
		ISO8859_1_ARRAY[206 - OFFSET_ISO8859_1] = "Icirc";
		ISO8859_1_ARRAY[207 - OFFSET_ISO8859_1] = "Iuml";
		ISO8859_1_ARRAY[208 - OFFSET_ISO8859_1] = "ETH";
		ISO8859_1_ARRAY[209 - OFFSET_ISO8859_1] = "Ntilde";
		ISO8859_1_ARRAY[210 - OFFSET_ISO8859_1] = "Ograve";
		ISO8859_1_ARRAY[211 - OFFSET_ISO8859_1] = "Oacute";
		ISO8859_1_ARRAY[212 - OFFSET_ISO8859_1] = "Ocirc";
		ISO8859_1_ARRAY[213 - OFFSET_ISO8859_1] = "Otilde";
		ISO8859_1_ARRAY[214 - OFFSET_ISO8859_1] = "Ouml";
		ISO8859_1_ARRAY[215 - OFFSET_ISO8859_1] = "times";
		ISO8859_1_ARRAY[216 - OFFSET_ISO8859_1] = "Oslash";
		ISO8859_1_ARRAY[217 - OFFSET_ISO8859_1] = "Ugrave";
		ISO8859_1_ARRAY[218 - OFFSET_ISO8859_1] = "Uacute";
		ISO8859_1_ARRAY[219 - OFFSET_ISO8859_1] = "Ucirc";
		ISO8859_1_ARRAY[220 - OFFSET_ISO8859_1] = "Uuml";
		ISO8859_1_ARRAY[221 - OFFSET_ISO8859_1] = "Yacute";
		ISO8859_1_ARRAY[222 - OFFSET_ISO8859_1] = "THORN";
		ISO8859_1_ARRAY[223 - OFFSET_ISO8859_1] = "szlig";
		ISO8859_1_ARRAY[224 - OFFSET_ISO8859_1] = "agrave";
		ISO8859_1_ARRAY[225 - OFFSET_ISO8859_1] = "aacute";
		ISO8859_1_ARRAY[226 - OFFSET_ISO8859_1] = "acirc";
		ISO8859_1_ARRAY[227 - OFFSET_ISO8859_1] = "atilde";
		ISO8859_1_ARRAY[228 - OFFSET_ISO8859_1] = "auml";
		ISO8859_1_ARRAY[229 - OFFSET_ISO8859_1] = "aring";
		ISO8859_1_ARRAY[230 - OFFSET_ISO8859_1] = "aelig";
		ISO8859_1_ARRAY[231 - OFFSET_ISO8859_1] = "ccedil";
		ISO8859_1_ARRAY[232 - OFFSET_ISO8859_1] = "egrave";
		ISO8859_1_ARRAY[233 - OFFSET_ISO8859_1] = "eacute";
		ISO8859_1_ARRAY[234 - OFFSET_ISO8859_1] = "ecirc";
		ISO8859_1_ARRAY[235 - OFFSET_ISO8859_1] = "euml";
		ISO8859_1_ARRAY[236 - OFFSET_ISO8859_1] = "igrave";
		ISO8859_1_ARRAY[237 - OFFSET_ISO8859_1] = "iacute";
		ISO8859_1_ARRAY[238 - OFFSET_ISO8859_1] = "icirc";
		ISO8859_1_ARRAY[239 - OFFSET_ISO8859_1] = "iuml";
		ISO8859_1_ARRAY[240 - OFFSET_ISO8859_1] = "eth";
		ISO8859_1_ARRAY[241 - OFFSET_ISO8859_1] = "ntilde";
		ISO8859_1_ARRAY[242 - OFFSET_ISO8859_1] = "ograve";
		ISO8859_1_ARRAY[243 - OFFSET_ISO8859_1] = "oacute";
		ISO8859_1_ARRAY[244 - OFFSET_ISO8859_1] = "ocirc";
		ISO8859_1_ARRAY[245 - OFFSET_ISO8859_1] = "otilde";
		ISO8859_1_ARRAY[246 - OFFSET_ISO8859_1] = "ouml";
		ISO8859_1_ARRAY[247 - OFFSET_ISO8859_1] = "divide";
		ISO8859_1_ARRAY[248 - OFFSET_ISO8859_1] = "oslash";
		ISO8859_1_ARRAY[249 - OFFSET_ISO8859_1] = "ugrave";
		ISO8859_1_ARRAY[250 - OFFSET_ISO8859_1] = "uacute";
		ISO8859_1_ARRAY[251 - OFFSET_ISO8859_1] = "ucirc";
		ISO8859_1_ARRAY[252 - OFFSET_ISO8859_1] = "uuml";
		ISO8859_1_ARRAY[253 - OFFSET_ISO8859_1] = "yacute";
		ISO8859_1_ARRAY[254 - OFFSET_ISO8859_1] = "thorn";
		ISO8859_1_ARRAY[255 - OFFSET_ISO8859_1] = "yuml";
	}

	// HTML 40 (1)
	private static final int OFFSET_HTML40_1 = 402;
	private static final String[] HTML40_1_ARRAY = new String[403 - OFFSET_HTML40_1];
	static {
		HTML40_1_ARRAY[402 - OFFSET_HTML40_1] = "fnof";
	}

	// HTML 40 (2)
	private static final int OFFSET_HTML40_2 = 913;
	private static final String[] HTML40_2_ARRAY = new String[983 - OFFSET_HTML40_2];
	static {
		HTML40_2_ARRAY[913 - OFFSET_HTML40_2] = "Alpha";
		HTML40_2_ARRAY[914 - OFFSET_HTML40_2] = "Beta";
		HTML40_2_ARRAY[915 - OFFSET_HTML40_2] = "Gamma";
		HTML40_2_ARRAY[916 - OFFSET_HTML40_2] = "Delta";
		HTML40_2_ARRAY[917 - OFFSET_HTML40_2] = "Epsilon";
		HTML40_2_ARRAY[918 - OFFSET_HTML40_2] = "Zeta";
		HTML40_2_ARRAY[919 - OFFSET_HTML40_2] = "Eta";
		HTML40_2_ARRAY[920 - OFFSET_HTML40_2] = "Theta";
		HTML40_2_ARRAY[921 - OFFSET_HTML40_2] = "Iota";
		HTML40_2_ARRAY[922 - OFFSET_HTML40_2] = "Kappa";
		HTML40_2_ARRAY[923 - OFFSET_HTML40_2] = "Lambda";
		HTML40_2_ARRAY[924 - OFFSET_HTML40_2] = "Mu";
		HTML40_2_ARRAY[925 - OFFSET_HTML40_2] = "Nu";
		HTML40_2_ARRAY[926 - OFFSET_HTML40_2] = "Xi";
		HTML40_2_ARRAY[927 - OFFSET_HTML40_2] = "Omicron";
		HTML40_2_ARRAY[928 - OFFSET_HTML40_2] = "Pi";
		HTML40_2_ARRAY[929 - OFFSET_HTML40_2] = "Rho";
		HTML40_2_ARRAY[931 - OFFSET_HTML40_2] = "Sigma";
		HTML40_2_ARRAY[932 - OFFSET_HTML40_2] = "Tau";
		HTML40_2_ARRAY[933 - OFFSET_HTML40_2] = "Upsilon";
		HTML40_2_ARRAY[934 - OFFSET_HTML40_2] = "Phi";
		HTML40_2_ARRAY[935 - OFFSET_HTML40_2] = "Chi";
		HTML40_2_ARRAY[936 - OFFSET_HTML40_2] = "Psi";
		HTML40_2_ARRAY[937 - OFFSET_HTML40_2] = "Omega";
		HTML40_2_ARRAY[945 - OFFSET_HTML40_2] = "alpha";
		HTML40_2_ARRAY[946 - OFFSET_HTML40_2] = "beta";
		HTML40_2_ARRAY[947 - OFFSET_HTML40_2] = "gamma";
		HTML40_2_ARRAY[948 - OFFSET_HTML40_2] = "delta";
		HTML40_2_ARRAY[949 - OFFSET_HTML40_2] = "epsilon";
		HTML40_2_ARRAY[950 - OFFSET_HTML40_2] = "zeta";
		HTML40_2_ARRAY[951 - OFFSET_HTML40_2] = "eta";
		HTML40_2_ARRAY[952 - OFFSET_HTML40_2] = "theta";
		HTML40_2_ARRAY[953 - OFFSET_HTML40_2] = "iota";
		HTML40_2_ARRAY[954 - OFFSET_HTML40_2] = "kappa";
		HTML40_2_ARRAY[955 - OFFSET_HTML40_2] = "lambda";
		HTML40_2_ARRAY[956 - OFFSET_HTML40_2] = "mu";
		HTML40_2_ARRAY[957 - OFFSET_HTML40_2] = "nu";
		HTML40_2_ARRAY[958 - OFFSET_HTML40_2] = "xi";
		HTML40_2_ARRAY[959 - OFFSET_HTML40_2] = "omicron";
		HTML40_2_ARRAY[960 - OFFSET_HTML40_2] = "pi";
		HTML40_2_ARRAY[961 - OFFSET_HTML40_2] = "rho";
		HTML40_2_ARRAY[962 - OFFSET_HTML40_2] = "sigmaf";
		HTML40_2_ARRAY[963 - OFFSET_HTML40_2] = "sigma";
		HTML40_2_ARRAY[964 - OFFSET_HTML40_2] = "tau";
		HTML40_2_ARRAY[965 - OFFSET_HTML40_2] = "upsilon";
		HTML40_2_ARRAY[966 - OFFSET_HTML40_2] = "phi";
		HTML40_2_ARRAY[967 - OFFSET_HTML40_2] = "chi";
		HTML40_2_ARRAY[968 - OFFSET_HTML40_2] = "psi";
		HTML40_2_ARRAY[969 - OFFSET_HTML40_2] = "omega";
		HTML40_2_ARRAY[977 - OFFSET_HTML40_2] = "thetasym";
		HTML40_2_ARRAY[978 - OFFSET_HTML40_2] = "upsih";
		HTML40_2_ARRAY[982 - OFFSET_HTML40_2] = "piv";
	}

	// HTML 40 (3)
	private static final int OFFSET_HTML40_3 = 338;
	private static final String[] HTML40_3_ARRAY = new String[377 - OFFSET_HTML40_3];
	static {
		HTML40_2_ARRAY[338 - OFFSET_HTML40_3] = "OElig";
		HTML40_2_ARRAY[339 - OFFSET_HTML40_3] = "oelig";
		HTML40_2_ARRAY[352 - OFFSET_HTML40_3] = "Scaron";
		HTML40_2_ARRAY[353 - OFFSET_HTML40_3] = "scaron";
		HTML40_2_ARRAY[376 - OFFSET_HTML40_3] = "Yuml";
	}

	// HTML 40 (4)
	private static final int OFFSET_HTML40_4 = 710;
	private static final String[] HTML40_4_ARRAY = new String[733 - OFFSET_HTML40_4];
	static {
		HTML40_4_ARRAY[710 - OFFSET_HTML40_4] = "circ";
		HTML40_4_ARRAY[732 - OFFSET_HTML40_4] = "tilde";
	}

	// HTML 40 (5)
	private static final int OFFSET_HTML40_5 = 8194;
	private static final String[] HTML40_5_ARRAY = new String[9831 - OFFSET_HTML40_5];
	static {
		HTML40_5_ARRAY[8194 - OFFSET_HTML40_5] = "ensp";
		HTML40_5_ARRAY[8195 - OFFSET_HTML40_5] = "emsp";
		HTML40_5_ARRAY[8201 - OFFSET_HTML40_5] = "thinsp";
		HTML40_5_ARRAY[8204 - OFFSET_HTML40_5] = "zwnj";
		HTML40_5_ARRAY[8205 - OFFSET_HTML40_5] = "zwj";
		HTML40_5_ARRAY[8206 - OFFSET_HTML40_5] = "lrm";
		HTML40_5_ARRAY[8207 - OFFSET_HTML40_5] = "rlm";
		HTML40_5_ARRAY[8211 - OFFSET_HTML40_5] = "ndash";
		HTML40_5_ARRAY[8212 - OFFSET_HTML40_5] = "mdash";
		HTML40_5_ARRAY[8216 - OFFSET_HTML40_5] = "lsquo";
		HTML40_5_ARRAY[8217 - OFFSET_HTML40_5] = "rsquo";
		HTML40_5_ARRAY[8218 - OFFSET_HTML40_5] = "sbquo";
		HTML40_5_ARRAY[8220 - OFFSET_HTML40_5] = "ldquo";
		HTML40_5_ARRAY[8221 - OFFSET_HTML40_5] = "rdquo";
		HTML40_5_ARRAY[8222 - OFFSET_HTML40_5] = "bdquo";
		HTML40_5_ARRAY[8224 - OFFSET_HTML40_5] = "dagger";
		HTML40_5_ARRAY[8225 - OFFSET_HTML40_5] = "Dagger";
		HTML40_5_ARRAY[8226 - OFFSET_HTML40_5] = "bull";
		HTML40_5_ARRAY[8230 - OFFSET_HTML40_5] = "hellip";
		HTML40_5_ARRAY[8240 - OFFSET_HTML40_5] = "permil";
		HTML40_5_ARRAY[8242 - OFFSET_HTML40_5] = "prime";
		HTML40_5_ARRAY[8243 - OFFSET_HTML40_5] = "Prime";
		HTML40_5_ARRAY[8249 - OFFSET_HTML40_5] = "lsaquo";
		HTML40_5_ARRAY[8250 - OFFSET_HTML40_5] = "rsaquo";
		HTML40_5_ARRAY[8254 - OFFSET_HTML40_5] = "oline";
		HTML40_5_ARRAY[8260 - OFFSET_HTML40_5] = "frasl";
		HTML40_5_ARRAY[8364 - OFFSET_HTML40_5] = "euro";
		HTML40_5_ARRAY[8472 - OFFSET_HTML40_5] = "weierp";
		HTML40_5_ARRAY[8465 - OFFSET_HTML40_5] = "image";
		HTML40_5_ARRAY[8476 - OFFSET_HTML40_5] = "real";
		HTML40_5_ARRAY[8482 - OFFSET_HTML40_5] = "trade";
		HTML40_5_ARRAY[8501 - OFFSET_HTML40_5] = "alefsym";
		HTML40_5_ARRAY[8592 - OFFSET_HTML40_5] = "larr";
		HTML40_5_ARRAY[8593 - OFFSET_HTML40_5] = "uarr";
		HTML40_5_ARRAY[8594 - OFFSET_HTML40_5] = "rarr";
		HTML40_5_ARRAY[8595 - OFFSET_HTML40_5] = "darr";
		HTML40_5_ARRAY[8596 - OFFSET_HTML40_5] = "harr";
		HTML40_5_ARRAY[8629 - OFFSET_HTML40_5] = "crarr";
		HTML40_5_ARRAY[8656 - OFFSET_HTML40_5] = "lArr";
		HTML40_5_ARRAY[8657 - OFFSET_HTML40_5] = "uArr";
		HTML40_5_ARRAY[8658 - OFFSET_HTML40_5] = "rArr";
		HTML40_5_ARRAY[8659 - OFFSET_HTML40_5] = "dArr";
		HTML40_5_ARRAY[8660 - OFFSET_HTML40_5] = "hArr";
		HTML40_5_ARRAY[8704 - OFFSET_HTML40_5] = "forall";
		HTML40_5_ARRAY[8706 - OFFSET_HTML40_5] = "part";
		HTML40_5_ARRAY[8707 - OFFSET_HTML40_5] = "exist";
		HTML40_5_ARRAY[8709 - OFFSET_HTML40_5] = "empty";
		HTML40_5_ARRAY[8711 - OFFSET_HTML40_5] = "nabla";
		HTML40_5_ARRAY[8712 - OFFSET_HTML40_5] = "isin";
		HTML40_5_ARRAY[8713 - OFFSET_HTML40_5] = "notin";
		HTML40_5_ARRAY[8715 - OFFSET_HTML40_5] = "ni";
		HTML40_5_ARRAY[8719 - OFFSET_HTML40_5] = "prod";
		HTML40_5_ARRAY[8721 - OFFSET_HTML40_5] = "sum";
		HTML40_5_ARRAY[8722 - OFFSET_HTML40_5] = "minus";
		HTML40_5_ARRAY[8727 - OFFSET_HTML40_5] = "lowast";
		HTML40_5_ARRAY[8730 - OFFSET_HTML40_5] = "radic";
		HTML40_5_ARRAY[8733 - OFFSET_HTML40_5] = "prop";
		HTML40_5_ARRAY[8734 - OFFSET_HTML40_5] = "infin";
		HTML40_5_ARRAY[8736 - OFFSET_HTML40_5] = "ang";
		HTML40_5_ARRAY[8743 - OFFSET_HTML40_5] = "and";
		HTML40_5_ARRAY[8744 - OFFSET_HTML40_5] = "or";
		HTML40_5_ARRAY[8745 - OFFSET_HTML40_5] = "cap";
		HTML40_5_ARRAY[8746 - OFFSET_HTML40_5] = "cup";
		HTML40_5_ARRAY[8747 - OFFSET_HTML40_5] = "int";
		HTML40_5_ARRAY[8756 - OFFSET_HTML40_5] = "there4";
		HTML40_5_ARRAY[8764 - OFFSET_HTML40_5] = "sim";
		HTML40_5_ARRAY[8773 - OFFSET_HTML40_5] = "cong";
		HTML40_5_ARRAY[8776 - OFFSET_HTML40_5] = "asymp";
		HTML40_5_ARRAY[8800 - OFFSET_HTML40_5] = "ne";
		HTML40_5_ARRAY[8801 - OFFSET_HTML40_5] = "equiv";
		HTML40_5_ARRAY[8804 - OFFSET_HTML40_5] = "le";
		HTML40_5_ARRAY[8805 - OFFSET_HTML40_5] = "ge";
		HTML40_5_ARRAY[8834 - OFFSET_HTML40_5] = "sub";
		HTML40_5_ARRAY[8835 - OFFSET_HTML40_5] = "sup";
		HTML40_5_ARRAY[8838 - OFFSET_HTML40_5] = "sube";
		HTML40_5_ARRAY[8839 - OFFSET_HTML40_5] = "supe";
		HTML40_5_ARRAY[8853 - OFFSET_HTML40_5] = "oplus";
		HTML40_5_ARRAY[8855 - OFFSET_HTML40_5] = "otimes";
		HTML40_5_ARRAY[8869 - OFFSET_HTML40_5] = "perp";
		HTML40_5_ARRAY[8901 - OFFSET_HTML40_5] = "sdot";
		HTML40_5_ARRAY[8968 - OFFSET_HTML40_5] = "lceil";
		HTML40_5_ARRAY[8969 - OFFSET_HTML40_5] = "rceil";
		HTML40_5_ARRAY[8970 - OFFSET_HTML40_5] = "lfloor";
		HTML40_5_ARRAY[8971 - OFFSET_HTML40_5] = "rfloor";
		HTML40_5_ARRAY[9001 - OFFSET_HTML40_5] = "lang";
		HTML40_5_ARRAY[9002 - OFFSET_HTML40_5] = "rang";
		HTML40_5_ARRAY[9674 - OFFSET_HTML40_5] = "loz";
		HTML40_5_ARRAY[9824 - OFFSET_HTML40_5] = "spades";
		HTML40_5_ARRAY[9827 - OFFSET_HTML40_5] = "clubs";
		HTML40_5_ARRAY[9829 - OFFSET_HTML40_5] = "hearts";
		HTML40_5_ARRAY[9830 - OFFSET_HTML40_5] = "diams";
	}

	// HTML 20
	private static final String[][] HTML20_DATA = { BASIC_ARRAY };
	private static final int[] HTML20_OFFSET = { OFFSET_BASIC };

	// HTML 32
	private static final String[][] HTML32_DATA = { BASIC_ARRAY, ISO8859_1_ARRAY };
	private static final int[] HTML32_OFFSET = { OFFSET_BASIC, OFFSET_ISO8859_1 };

	// HTML 40
	private static final String[][] HTML40_DATA = { BASIC_ARRAY, ISO8859_1_ARRAY, HTML40_1_ARRAY, HTML40_2_ARRAY, HTML40_3_ARRAY, HTML40_4_ARRAY, HTML40_5_ARRAY };
	private static final int[] HTML40_OFFSET = { OFFSET_BASIC, OFFSET_ISO8859_1, OFFSET_HTML40_1, OFFSET_HTML40_2, OFFSET_HTML40_3, OFFSET_HTML40_4, OFFSET_HTML40_5 };
	private static final char CR = (char) 13;

	/**
	 * escapes html character inside a string
	 * 
	 * @param str html code to escape
	 * @return escaped html code
	 */
	public static String escapeHTML(String str) {
		return escapeHTML(str, HTMLV40);
	}

	/**
	 * escapes html character inside a string
	 * 
	 * @param str html code to escape
	 * @param version HTML Version ()
	 * @return escaped html code
	 */
	public static String escapeHTML(String str, short version) {
		String[][] data;
		int[] offset;
		StringBuilder rtn = new StringBuilder(str.length());
		char[] chars = str.toCharArray();

		if (version == HTMLV20) {
			data = HTML20_DATA;
			offset = HTML20_OFFSET;
		}
		else if (version == HTMLV32) {
			data = HTML32_DATA;
			offset = HTML32_OFFSET;
		}
		else {
			data = HTML40_DATA;
			offset = HTML40_OFFSET;
		}

		outer: for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (c == CR) continue;// for compatibility to ACF
			for (int y = 0; y < offset.length; y++) {
				if (c >= offset[y] && c < data[y].length + offset[y]) {

					String replacement = data[y][c - offset[y]];
					if (replacement != null) {
						rtn.append('&');
						rtn.append(replacement);
						rtn.append(';');
						continue outer;
					}
				}
			}
			rtn.append(c);
		}
		return rtn.toString();
	}

	/**
	 * unescapes html character inside a string
	 * 
	 * @param str html code to unescape
	 * @return unescaped html code
	 */
	public static String unescapeHTML(String str) {

		StringBuilder rtn = new StringBuilder();
		int posStart = -1;
		int posFinish = -1;
		while ((posStart = str.indexOf('&', posStart)) != -1) {
			int last = posFinish + 1;

			posFinish = str.indexOf(';', posStart);
			if (posFinish == -1) break;
			rtn.append(str.substring(last, posStart));
			if (posStart + 1 < posFinish) {
				rtn.append(unescapeHTMLEntity(str.substring(posStart + 1, posFinish)));
			}
			else {
				rtn.append("&;");
			}

			posStart = posFinish + 1;
		}
		rtn.append(str.substring(posFinish + 1));
		return rtn.toString();
	}

	private static String unescapeHTMLEntity(String str) {
		String[][] ranges = HTML40_DATA;
		int[] offset = HTML40_OFFSET;

		// Number Entity
		if (str.indexOf('#') == 0) {
			if (str.length() == 1) return "&" + str + ";";
			try {
				return ((char) Integer.parseInt(str.substring(1))) + "";
			}
			catch (NumberFormatException nfe) {
				return "&" + str + ";";
			}
		}

		// String Entity
		// else {
		for (int i = 0; i < ranges.length; i++) {
			String[] range = ranges[i];
			for (int y = 0; y < range.length; y++) {
				String el = range[y];
				if (el != null && el.equalsIgnoreCase(str)) {
					return ((char) (y + offset[i])) + "";
				}
			}
		}
		// }

		return "&" + str + ";";
	}
}