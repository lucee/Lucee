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
package lucee.runtime.op;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lucee.commons.date.DateTimeUtil;
import lucee.commons.date.JREDateTimeUtil;
import lucee.commons.date.TimeZoneUtil;
import lucee.commons.digest.Base64Encoder;
import lucee.commons.io.FileUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.CFTypes;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.PhysicalClassLoader;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.HTTPUtil;
import lucee.runtime.Component;
import lucee.runtime.ComponentScope;
import lucee.runtime.ComponentSpecificAccess;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.coder.Base64Coder;
import lucee.runtime.component.Member;
import lucee.runtime.component.Property;
import lucee.runtime.component.PropertyImpl;
import lucee.runtime.config.Config;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.ScriptConverter;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.NativeException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageExceptionBox;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.exp.RequestTimeoutException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.functions.file.FileStreamWrapper;
import lucee.runtime.i18n.LocaleFactory;
import lucee.runtime.image.ImageUtil;
import lucee.runtime.interpreter.CFMLExpressionInterpreter;
import lucee.runtime.interpreter.VariableInterpreter;
import lucee.runtime.java.JavaObject;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.op.validators.ValidateCreditCard;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.text.xml.XMLCaster;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.text.xml.struct.XMLMultiElementArray;
import lucee.runtime.text.xml.struct.XMLMultiElementStruct;
import lucee.runtime.text.xml.struct.XMLStruct;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.CollectionStruct;
import lucee.runtime.type.FunctionValue;
import lucee.runtime.type.FunctionValueImpl;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.ObjectWrap;
import lucee.runtime.type.Objects;
import lucee.runtime.type.Pojo;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryColumn;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.dt.Time;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.dt.TimeSpanImpl;
import lucee.runtime.type.scope.ObjectStruct;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ComponentUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.wrap.ArrayAsList;
import lucee.runtime.type.wrap.ListAsArray;
import lucee.runtime.type.wrap.MapAsStruct;
import lucee.runtime.type.wrap.StructAsArray;
import lucee.runtime.util.ForEachUtil;

/**
 * This class can cast object of one type to another by CFML rules
 */
public final class Caster {
	private Caster() {}
	// static Map calendarsMap=new ReferenceMap(ReferenceMap.SOFT,ReferenceMap.SOFT);

	private static final int NUMBERS_MIN = 0;
	private static final int NUMBERS_MAX = 999;
	private static final String[] NUMBERS = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23",
			"24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51",
			"52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79",
			"80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "100", "101", "102", "103", "104", "105", "106",
			"107", "108", "109", "110", "111", "112", "113", "114", "115", "116", "117", "118", "119", "120", "121", "122", "123", "124", "125", "126", "127", "128", "129", "130",
			"131", "132", "133", "134", "135", "136", "137", "138", "139", "140", "141", "142", "143", "144", "145", "146", "147", "148", "149", "150", "151", "152", "153", "154",
			"155", "156", "157", "158", "159", "160", "161", "162", "163", "164", "165", "166", "167", "168", "169", "170", "171", "172", "173", "174", "175", "176", "177", "178",
			"179", "180", "181", "182", "183", "184", "185", "186", "187", "188", "189", "190", "191", "192", "193", "194", "195", "196", "197", "198", "199", "200", "201", "202",
			"203", "204", "205", "206", "207", "208", "209", "210", "211", "212", "213", "214", "215", "216", "217", "218", "219", "220", "221", "222", "223", "224", "225", "226",
			"227", "228", "229", "230", "231", "232", "233", "234", "235", "236", "237", "238", "239", "240", "241", "242", "243", "244", "245", "246", "247", "248", "249", "250",
			"251", "252", "253", "254", "255", "256", "257", "258", "259", "260", "261", "262", "263", "264", "265", "266", "267", "268", "269", "270", "271", "272", "273", "274",
			"275", "276", "277", "278", "279", "280", "281", "282", "283", "284", "285", "286", "287", "288", "289", "290", "291", "292", "293", "294", "295", "296", "297", "298",
			"299", "300", "301", "302", "303", "304", "305", "306", "307", "308", "309", "310", "311", "312", "313", "314", "315", "316", "317", "318", "319", "320", "321", "322",
			"323", "324", "325", "326", "327", "328", "329", "330", "331", "332", "333", "334", "335", "336", "337", "338", "339", "340", "341", "342", "343", "344", "345", "346",
			"347", "348", "349", "350", "351", "352", "353", "354", "355", "356", "357", "358", "359", "360", "361", "362", "363", "364", "365", "366", "367", "368", "369", "370",
			"371", "372", "373", "374", "375", "376", "377", "378", "379", "380", "381", "382", "383", "384", "385", "386", "387", "388", "389", "390", "391", "392", "393", "394",
			"395", "396", "397", "398", "399", "400", "401", "402", "403", "404", "405", "406", "407", "408", "409", "410", "411", "412", "413", "414", "415", "416", "417", "418",
			"419", "420", "421", "422", "423", "424", "425", "426", "427", "428", "429", "430", "431", "432", "433", "434", "435", "436", "437", "438", "439", "440", "441", "442",
			"443", "444", "445", "446", "447", "448", "449", "450", "451", "452", "453", "454", "455", "456", "457", "458", "459", "460", "461", "462", "463", "464", "465", "466",
			"467", "468", "469", "470", "471", "472", "473", "474", "475", "476", "477", "478", "479", "480", "481", "482", "483", "484", "485", "486", "487", "488", "489", "490",
			"491", "492", "493", "494", "495", "496", "497", "498", "499", "500", "501", "502", "503", "504", "505", "506", "507", "508", "509", "510", "511", "512", "513", "514",
			"515", "516", "517", "518", "519", "520", "521", "522", "523", "524", "525", "526", "527", "528", "529", "530", "531", "532", "533", "534", "535", "536", "537", "538",
			"539", "540", "541", "542", "543", "544", "545", "546", "547", "548", "549", "550", "551", "552", "553", "554", "555", "556", "557", "558", "559", "560", "561", "562",
			"563", "564", "565", "566", "567", "568", "569", "570", "571", "572", "573", "574", "575", "576", "577", "578", "579", "580", "581", "582", "583", "584", "585", "586",
			"587", "588", "589", "590", "591", "592", "593", "594", "595", "596", "597", "598", "599", "600", "601", "602", "603", "604", "605", "606", "607", "608", "609", "610",
			"611", "612", "613", "614", "615", "616", "617", "618", "619", "620", "621", "622", "623", "624", "625", "626", "627", "628", "629", "630", "631", "632", "633", "634",
			"635", "636", "637", "638", "639", "640", "641", "642", "643", "644", "645", "646", "647", "648", "649", "650", "651", "652", "653", "654", "655", "656", "657", "658",
			"659", "660", "661", "662", "663", "664", "665", "666", "667", "668", "669", "670", "671", "672", "673", "674", "675", "676", "677", "678", "679", "680", "681", "682",
			"683", "684", "685", "686", "687", "688", "689", "690", "691", "692", "693", "694", "695", "696", "697", "698", "699", "700", "701", "702", "703", "704", "705", "706",
			"707", "708", "709", "710", "711", "712", "713", "714", "715", "716", "717", "718", "719", "720", "721", "722", "723", "724", "725", "726", "727", "728", "729", "730",
			"731", "732", "733", "734", "735", "736", "737", "738", "739", "740", "741", "742", "743", "744", "745", "746", "747", "748", "749", "750", "751", "752", "753", "754",
			"755", "756", "757", "758", "759", "760", "761", "762", "763", "764", "765", "766", "767", "768", "769", "770", "771", "772", "773", "774", "775", "776", "777", "778",
			"779", "780", "781", "782", "783", "784", "785", "786", "787", "788", "789", "790", "791", "792", "793", "794", "795", "796", "797", "798", "799", "800", "801", "802",
			"803", "804", "805", "806", "807", "808", "809", "810", "811", "812", "813", "814", "815", "816", "817", "818", "819", "820", "821", "822", "823", "824", "825", "826",
			"827", "828", "829", "830", "831", "832", "833", "834", "835", "836", "837", "838", "839", "840", "841", "842", "843", "844", "845", "846", "847", "848", "849", "850",
			"851", "852", "853", "854", "855", "856", "857", "858", "859", "860", "861", "862", "863", "864", "865", "866", "867", "868", "869", "870", "871", "872", "873", "874",
			"875", "876", "877", "878", "879", "880", "881", "882", "883", "884", "885", "886", "887", "888", "889", "890", "891", "892", "893", "894", "895", "896", "897", "898",
			"899", "900", "901", "902", "903", "904", "905", "906", "907", "908", "909", "910", "911", "912", "913", "914", "915", "916", "917", "918", "919", "920", "921", "922",
			"923", "924", "925", "926", "927", "928", "929", "930", "931", "932", "933", "934", "935", "936", "937", "938", "939", "940", "941", "942", "943", "944", "945", "946",
			"947", "948", "949", "950", "951", "952", "953", "954", "955", "956", "957", "958", "959", "960", "961", "962", "963", "964", "965", "966", "967", "968", "969", "970",
			"971", "972", "973", "974", "975", "976", "977", "978", "979", "980", "981", "982", "983", "984", "985", "986", "987", "988", "989", "990", "991", "992", "993", "994",
			"995", "996", "997", "998", "999" };

	/**
	 * cast a boolean value to a boolean value (do nothing)
	 * 
	 * @param b boolean value to cast
	 * @return casted boolean value
	 */
	public static boolean toBooleanValue(boolean b) {
		return b;
	}

	/**
	 * cast an int value to a boolean value (primitive value type)
	 * 
	 * @param i int value to cast
	 * @return casted boolean value
	 */
	public static boolean toBooleanValue(int i) {
		return i != 0;
	}

	/**
	 * cast a long value to a boolean value (primitive value type)
	 * 
	 * @param l long value to cast
	 * @return casted boolean value
	 */
	public static boolean toBooleanValue(long l) {
		return l != 0;
	}

	/**
	 * cast a double value to a boolean value (primitive value type)
	 * 
	 * @param d double value to cast
	 * @return casted boolean value
	 */
	public static boolean toBooleanValue(double d) {
		return d != 0;
	}

	/**
	 * cast a double value to a boolean value (primitive value type)
	 * 
	 * @param c char value to cast
	 * @return casted boolean value
	 */
	public static boolean toBooleanValue(char c) {
		return c != 0;
	}

	/**
	 * cast an Object to a boolean value (primitive value type)
	 * 
	 * @param o Object to cast
	 * @return casted boolean value
	 * @throws PageException
	 */
	public static boolean toBooleanValue(Object o) throws PageException {
		if (o instanceof Boolean) return ((Boolean) o).booleanValue();
		else if (o instanceof Number) return toBooleanValue(((Number) o).doubleValue());
		else if (o instanceof String) return toBooleanValue((String) o);
		else if (o instanceof Castable) return ((Castable) o).castToBooleanValue();
		else if (o == null) return toBooleanValue("");
		else if (o instanceof ObjectWrap) return toBooleanValue(((ObjectWrap) o).getEmbededObject());
		throw new CasterException(o, "boolean");
	}

	/**
	 * tranlate a Boolean object to a boolean value
	 * 
	 * @param b
	 * @return
	 */
	public static boolean toBooleanValue(Boolean b) {
		return b.booleanValue();
	}

	/**
	 * cast an Object to a boolean value (primitive value type)
	 * 
	 * @param str String to cast
	 * @return casted boolean value
	 * @throws PageException
	 */
	public static boolean toBooleanValue(String str) throws PageException {
		Boolean b = toBoolean(str, null);
		if (b != null) return b.booleanValue();
		throw new CasterException("Can't cast String [" + CasterException.crop(str) + "] to a boolean");
	}

	public static Boolean toBoolean(String str, Boolean defaultValue) {
		if (str == null) return defaultValue;
		int i = stringToBooleanValueEL(str);
		if (i != -1) return (i == 1) ? Boolean.TRUE : Boolean.FALSE;

		double d = toDoubleValue(str, Double.NaN);
		if (!Double.isNaN(d)) return toBoolean(d);

		return defaultValue;
	}

	/**
	 * cast an Object to a Double Object (reference Type)
	 * 
	 * @param f Object to cast
	 * @return casted Double Object
	 * @throws PageException
	 */
	public static Double toDouble(float f) {
		return new Double(f);

	}

	public static Double toDouble(Float f) {
		return new Double(f.doubleValue());
	}

	/**
	 * cast an Object to a Double Object (reference Type)
	 * 
	 * @param o Object to cast
	 * @return casted Double Object
	 * @throws PageException
	 */
	public static Double toDouble(Object o) throws PageException {
		if (o instanceof Double) return (Double) o;
		return new Double(toDoubleValue(o));

	}

	/**
	 * cast an Object to a Double Object (reference Type)
	 * 
	 * @param str string to cast
	 * @return casted Double Object
	 * @throws PageException
	 */
	public static Double toDouble(String str) throws PageException {
		return new Double(toDoubleValue(str));

	}

	/**
	 * cast an Object to a Double Object (reference Type)
	 * 
	 * @param o Object to cast
	 * @param defaultValue
	 * @return casted Double Object
	 */
	public static Double toDouble(Object o, Double defaultValue) {
		if (o instanceof Double) return (Double) o;
		double dbl = toDoubleValue(o, true, Double.NaN);
		if (Double.isNaN(dbl)) return defaultValue;
		return new Double(dbl);

	}

	/**
	 * cast a double value to a Double Object (reference Type)
	 * 
	 * @param d double value to cast
	 * @return casted Double Object
	 */
	private static final int MAX_SMALL_DOUBLE = 10000;
	private static final Double[] smallDoubles = new Double[MAX_SMALL_DOUBLE];
	private static final Object DEFAULT = new Object();
	static {
		for (int i = 0; i < MAX_SMALL_DOUBLE; i++)
			smallDoubles[i] = new Double(i);
	}

	public static Double toDouble(double d) {
		if (d < MAX_SMALL_DOUBLE && d >= 0) {
			int i;
			if ((i = ((int) d)) == d) return smallDoubles[i];
		}
		return new Double(d);
	}

	/**
	 * cast a boolean value to a Double Object (reference Type)
	 * 
	 * @param b boolean value to cast
	 * @return casted Double Object
	 */
	public static Double toDouble(boolean b) {
		return new Double(b ? 1 : 0);
	}

	/**
	 * cast an Object to a double value (primitive value Type)
	 * 
	 * @param o Object to cast
	 * @return casted double value
	 * @throws PageException
	 */
	public static double toDoubleValue(Object o) throws PageException {
		if (o instanceof Number) {
			return ((Number) o).doubleValue();
		}
		else if (o instanceof Boolean) return ((Boolean) o).booleanValue() ? 1 : 0;
		else if (o instanceof CharSequence) return toDoubleValue(o.toString(), true);
		// else if(o instanceof Clob) return toDoubleValue(toString(o));
		else if (o instanceof Castable) return ((Castable) o).castToDoubleValue();
		else if (o == null) return 0;// toDoubleValue("");
		else if (o instanceof ObjectWrap) return toDoubleValue(((ObjectWrap) o).getEmbededObject());
		else if (o instanceof Date) return DateTimeUtil.getInstance().toDoubleValue(((Date) o).getTime());
		else if (o instanceof Calendar) return DateTimeUtil.getInstance().toDoubleValue(((Calendar) o).getTimeInMillis());
		else if (o instanceof Character) return (((Character) o).charValue());
		throw new CasterException(o, "number");
	}

	public static double toDoubleValue(Double d) {
		if (d == null) return 0;
		return d.doubleValue();
	}

	/**
	 * cast an Object to a double value (primitive value Type)
	 * 
	 * @param str String to cast
	 * @return casted double value
	 * @throws CasterException
	 */
	public static double toDoubleValue(String str) throws CasterException {
		return toDoubleValue(str, true);
	}

	public static double toDoubleValue(String strNumber, int radix, boolean alsoFromDate, double defaultValue) {
		strNumber = strNumber.trim();

		if (StringUtil.startsWithIgnoreCase(strNumber, "0x")) {
			radix = 16;
			strNumber = strNumber.substring(2);
		}

		if (radix == 10) return Caster.toDoubleValue(strNumber, 0);
		else if (strNumber.indexOf('.') != -1 && radix != 10) return defaultValue;// throw new CasterException("the radix con only be [dec] for floating point numbers");

		return Integer.parseInt(strNumber, radix);
	}

	public static double toDoubleValue(String str, boolean alsoFromDate) throws CasterException {
		if (str == null) return 0;// throw new CasterException("can't cast empty string to a number value");
		str = str.trim();
		double rtn = 0;
		// double rtn_=0;
		// double _rtn=0;
		int eCount = 0;
		double deep = 1;
		int pos = 0;
		int len = str.length();

		if (len == 0) throw new CasterException("can't cast empty string to a number value");
		char curr = str.charAt(pos);
		boolean isMinus = false;

		if (curr == '+') {
			if (len == ++pos) throw new CasterException("can't cast [+] string to a number value");
		}
		if (curr == '-') {
			if (len == ++pos) throw new CasterException("can't cast [-] string to a number value");
			isMinus = true;
		}
		boolean hasDot = false;
		// boolean hasExp=false;
		do {
			curr = str.charAt(pos);
			if (curr < '0') {
				if (curr == '.') {
					if (hasDot) {
						if (!alsoFromDate) throw new CasterException("cannot cast [" + str + "] string to a number value");
						return toDoubleValueViaDate(str);
					}
					hasDot = true;
				}
				else {
					if (pos == 0 && Decision.isBoolean(str)) return toBooleanValue(str, false) ? 1.0D : 0.0D;
					if (!alsoFromDate) throw new CasterException("cannot cast [" + str + "] string to a number value");
					return toDoubleValueViaDate(str);
					// throw new CasterException("can't cast ["+str+"] string to a number value");
				}
			}
			else if (curr > '9') {
				if (pos > 0 && (curr == 'e' || curr == 'E')) {
					try {
						return Double.parseDouble(str);
					}
					catch (NumberFormatException e) {
						if (!alsoFromDate) throw new CasterException("cannot cast [" + str + "] string to a number value");
						return toDoubleValueViaDate(str);
						// throw new CasterException("can't cast ["+str+"] string to a number value");
					}
				}
				// else {
				if (pos == 0 && Decision.isBoolean(str)) return toBooleanValue(str, false) ? 1.0D : 0.0D;
				if (!alsoFromDate) throw new CasterException("cannot cast [" + str + "] string to a number value");
				return toDoubleValueViaDate(str);
				// throw new CasterException("can't cast ["+str+"] string to a number value");
				// }
			}
			else {
				rtn *= 10;
				rtn += toDigit(curr);
				if (hasDot) {
					deep *= 10;
					if (deep > 1000000000000000000000D) return Double.parseDouble(str); // patch for LDEV-2654
				}

			}
		}
		while (++pos < len);

		if (deep > 1) {
			rtn /= deep;
		}
		if (isMinus) rtn = -rtn;
		if (eCount > 0) for (int i = 0; i < eCount; i++)
			rtn *= 10;
		// print.e("here:"+rtn_);
		return rtn;
	}

	private static double toDoubleValueViaDate(String str) throws CasterException {
		DateTime date = DateCaster.toDateSimple(str, DateCaster.CONVERTING_TYPE_NONE, false, null, null);// not advanced here, neo also only support simple
		if (date == null) throw new CasterException("can't cast [" + str + "] string to a number value");
		return date.castToDoubleValue(0);
	}

	private static double toDoubleValueViaDate(String str, double defaultValue) {
		DateTime date = DateCaster.toDateSimple(str, DateCaster.CONVERTING_TYPE_NONE, false, null, null);// not advanced here, neo also only support simple
		if (date == null) return defaultValue;
		return date.castToDoubleValue(0);
	}

	/**
	 * cast an Object to a double value (primitive value Type)
	 * 
	 * @param o Object to cast
	 * @param defaultValue if can't cast return this value
	 * @return casted double value / public static double toDoubleValue(Object o,double defaultValue) {
	 *         return toDoubleValue(o, true, defaultValue); }
	 */

	/**
	 * cast an Object to a double value (primitive value Type)
	 * 
	 * @param o Object to cast
	 * @param defaultValue if can't cast return this value
	 * @return casted double value
	 */
	public static double toDoubleValue(Object o, boolean alsoFromDate, double defaultValue) {
		if (o instanceof Number) return ((Number) o).doubleValue();
		else if (o instanceof Boolean) return ((Boolean) o).booleanValue() ? 1 : 0;
		else if (o instanceof CharSequence) return toDoubleValue(o.toString(), alsoFromDate, defaultValue);
		else if (o instanceof Castable) {
			return ((Castable) o).castToDoubleValue(defaultValue);

		}
		// else if(o == null) return defaultValue;
		else if (o instanceof ObjectWrap) return toDoubleValue(((ObjectWrap) o).getEmbededObject(new Double(defaultValue)), true, defaultValue);
		else if (o instanceof Date) return DateTimeUtil.getInstance().toDoubleValue(((Date) o).getTime());
		else if (o instanceof Calendar) return DateTimeUtil.getInstance().toDoubleValue(((Calendar) o).getTimeInMillis());
		else if (o instanceof Character) return (((Character) o).charValue());
		return defaultValue;
	}

	/**
	 * cast an Object to a double value (primitive value Type), if can't return Double.NaN
	 * 
	 * @param str String to cast
	 * @param defaultValue if can't cast return this value
	 * @return casted double value
	 */
	public static double toDoubleValue(String str, double defaultValue) {
		return toDoubleValue(str, true, defaultValue);
	}

	public static double toDoubleValue(String str, boolean alsoFromDate, double defaultValue) {
		if (str == null) return defaultValue;
		str = str.trim();

		int len = str.length();
		if (len == 0) return defaultValue;

		double rtn = 0;
		int eCount = 0;
		// double deep=10;
		double deep = 1;
		int pos = 0;

		char curr = str.charAt(pos);
		boolean isMinus = false;

		if (curr == '+') {
			if (len == ++pos) return defaultValue;
		}
		else if (curr == '-') {
			if (len == ++pos) return defaultValue;
			isMinus = true;
		}

		boolean hasDot = false;
		// boolean hasExp=false;
		do {
			curr = str.charAt(pos);

			if (curr < '0') {
				if (curr == '.') {
					if (hasDot) {
						if (!alsoFromDate) return defaultValue;
						return toDoubleValueViaDate(str, defaultValue);
					}
					hasDot = true;
				}
				else {
					if (pos == 0 && Decision.isBoolean(str)) return toBooleanValue(str, false) ? 1.0D : 0.0D;
					if (!alsoFromDate) return defaultValue;
					return toDoubleValueViaDate(str, defaultValue);
				}
			}
			else if (curr > '9') {
				if (pos > 0 && (curr == 'e' || curr == 'E')) {
					try {
						return Double.parseDouble(str);
					}
					catch (NumberFormatException e) {
						if (!alsoFromDate) return defaultValue;
						return toDoubleValueViaDate(str, defaultValue);
					}
				}
				// else {
				if (pos == 0 && Decision.isBoolean(str)) return toBooleanValue(str, false) ? 1.0D : 0.0D;
				if (!alsoFromDate) return defaultValue;
				return toDoubleValueViaDate(str, defaultValue);
				// }
			}
			else {
				rtn *= 10;
				rtn += toDigit(curr);
				if (hasDot) deep *= 10;
			}

		}
		while (++pos < len);

		if (deep > 1) {
			rtn /= deep;
		}
		if (isMinus) rtn = -rtn;
		if (eCount > 0) for (int i = 0; i < eCount; i++)
			rtn *= 10;
		return rtn;

	}

	private static int toDigit(char c) {
		return c - 48;
	}

	/**
	 * cast a double value to a double value (do nothing)
	 * 
	 * @param d double value to cast
	 * @return casted double value
	 */
	public static double toDoubleValue(double d) {
		return d;
	}

	public static double toDoubleValue(float f) {
		return f;
	}

	public static double toDoubleValue(Float f) {
		return f.doubleValue();
	}

	/**
	 * cast a boolean value to a double value (primitive value type)
	 * 
	 * @param b boolean value to cast
	 * @return casted double value
	 */
	public static double toDoubleValue(boolean b) {
		return b ? 1 : 0;
	}

	/**
	 * cast a char value to a double value (primitive value type)
	 * 
	 * @param c char value to cast
	 * @return casted double value
	 */
	public static double toDoubleValue(char c) {
		return c;
	}

	/**
	 * cast an Object to an int value (primitive value type)
	 * 
	 * @param o Object to cast
	 * @return casted int value
	 * @throws PageException
	 */
	public static int toIntValue(Object o) throws PageException {

		if (o instanceof Number) return ((Number) o).intValue();
		else if (o instanceof Boolean) return ((Boolean) o).booleanValue() ? 1 : 0;
		else if (o instanceof CharSequence) return toIntValue(o.toString().trim());
		else if (o instanceof Character) return (((Character) o).charValue());
		// else if(o instanceof Clob) return toIntValue(toString(o));
		else if (o instanceof Castable) return (int) ((Castable) o).castToDoubleValue();
		else if (o instanceof Date) return (int) new DateTimeImpl((Date) o).castToDoubleValue();

		if (o instanceof String) throw new ExpressionException("Can't cast String [" + CasterException.crop(o) + "] to a number");
		else if (o instanceof ObjectWrap) return toIntValue(((ObjectWrap) o).getEmbededObject());

		throw new CasterException(o, "number");
	}

	/**
	 * cast an Object to an int value (primitive value type)
	 * 
	 * @param o Object to cast
	 * @param defaultValue
	 * @return casted int value
	 */
	public static int toIntValue(Object o, int defaultValue) {
		if (o == null) return defaultValue;
		else if (o instanceof Number) return ((Number) o).intValue();
		else if (o instanceof Boolean) return ((Boolean) o).booleanValue() ? 1 : 0;
		else if (o instanceof CharSequence) return toIntValue(o.toString().trim(), defaultValue);
		// else if(o instanceof Clob) return toIntValue(toString(o));
		else if (o instanceof Character) return (((Character) o).charValue());
		else if (o instanceof Castable) {
			return (int) ((Castable) o).castToDoubleValue(defaultValue);

		}
		else if (o instanceof Date) return (int) new DateTimeImpl((Date) o).castToDoubleValue();
		else if (o instanceof ObjectWrap) return toIntValue(((ObjectWrap) o).getEmbededObject(Integer.valueOf(defaultValue)), defaultValue);

		return defaultValue;
	}

	public static int toIntValue(Integer i, int defaultValue) {
		if (i == null) return defaultValue;
		return i.intValue();
	}

	/**
	 * cast a String to an int value (primitive value type)
	 * 
	 * @param str String to cast
	 * @return casted int value
	 * @throws ExpressionException
	 */
	public static int toIntValue(String str) throws ExpressionException {
		return (int) toDoubleValue(str, false);
	}

	/**
	 * cast an Object to a double value (primitive value Type), if can't return Integer.MIN_VALUE
	 * 
	 * @param str String to cast
	 * @param defaultValue
	 * @return casted double value
	 */
	public static int toIntValue(String str, int defaultValue) {
		return (int) toDoubleValue(str, false, defaultValue);
	}

	/**
	 * cast a double value to an int value (primitive value type)
	 * 
	 * @param d double value to cast
	 * @return casted int value
	 */
	public static int toIntValue(double d) {
		return (int) d;
	}

	/**
	 * cast an int value to an int value (do nothing)
	 * 
	 * @param i int value to cast
	 * @return casted int value
	 */
	public static int toIntValue(int i) {
		return i;
	}

	/**
	 * cast a boolean value to an int value (primitive value type)
	 * 
	 * @param b boolean value to cast
	 * @return casted int value
	 */
	public static int toIntValue(boolean b) {
		return b ? 1 : 0;
	}

	/**
	 * cast a char value to an int value (primitive value type)
	 * 
	 * @param c char value to cast
	 * @return casted int value
	 */
	public static int toIntValue(char c) {
		return c;
	}

	/**
	 * cast a double to a decimal value (String:xx.xx)
	 * 
	 * @param value Object to cast
	 * @return casted decimal value
	 */
	public static String toDecimal(boolean value) {
		if (value) return "1.00";
		return "0.00";
	}

	/**
	 * cast a double to a decimal value (String:xx.xx)
	 * 
	 * @param value Object to cast
	 * @return casted decimal value
	 * @throws PageException
	 */
	public static String toDecimal(Object value) throws PageException {
		return toDecimal(Caster.toDoubleValue(value));
	}

	public static String toDecimal(Object value, boolean separator) throws PageException {
		return toDecimal(Caster.toDoubleValue(value), separator);
	}

	/**
	 * cast a double to a decimal value (String:xx.xx)
	 * 
	 * @param value Object to cast
	 * @return casted decimal value
	 * @throws PageException
	 */
	public static String toDecimal(String value) throws PageException {
		return toDecimal(Caster.toDoubleValue(value));
	}

	public static String toDecimal(String value, boolean separator) throws PageException {
		return toDecimal(Caster.toDoubleValue(value), separator);
	}

	/**
	 * cast a double to a decimal value (String:xx.xx)
	 * 
	 * @param value Object to cast
	 * @param defaultValue
	 * @return casted decimal value
	 */
	public static String toDecimal(Object value, String defaultValue) {
		double res = toDoubleValue(value, true, Double.NaN);
		if (Double.isNaN(res)) return defaultValue;
		return toDecimal(res);
	}

	public static String toDecimal(Object value, boolean separator, String defaultValue) {
		double res = toDoubleValue(value, true, Double.NaN);
		if (Double.isNaN(res)) return defaultValue;
		return toDecimal(res, separator);
	}

	/**
	 * cast an Object to a decimal value (String:xx.xx)
	 * 
	 * @param value Object to cast
	 * @return casted decimal value
	 */
	public static String toDecimal(double value) {
		return toDecimal(value, '.', ',');
	}

	public static String toDecimal(double value, boolean separator) {
		return toDecimal(value, '.', separator ? ',' : ((char) 0));
	}

	private static String toDecimal(double value, char decDel, char thsDel) {
		// TODO Caster toDecimal bessere impl.
		String str = new BigDecimal((StrictMath.round(value * 100) / 100D)).toString();
		// str=toDouble(value).toString();
		String[] arr = str.split("\\.");

		// right value
		String rightValue;
		if (arr.length == 1) {
			rightValue = "00";
		}
		else {
			rightValue = arr[1];
			rightValue = StrictMath.round(Caster.toDoubleValue("0." + rightValue, 0) * 100) + "";
			if (rightValue.length() < 2) rightValue = 0 + rightValue;
		}

		// left value
		String leftValue = arr[0];
		int leftValueLen = leftValue.length();
		int ends = (StringUtil.startsWith(str, '-')) ? 1 : 0;
		if (leftValueLen > 3) {
			StringBuffer tmp = new StringBuffer();
			int i;
			for (i = leftValueLen - 3; i > 0; i -= 3) {
				tmp.insert(0, leftValue.substring(i, i + 3));
				if (i != ends && thsDel > ((char) 0)) tmp.insert(0, thsDel);
			}
			tmp.insert(0, leftValue.substring(0, i + 3));
			leftValue = tmp.toString();

		}
		return leftValue + decDel + rightValue;
	}

	/**
	 * cast a boolean value to a Boolean Object(reference type)
	 * 
	 * @param b boolean value to cast
	 * @return casted Boolean Object
	 */
	public static Boolean toBoolean(boolean b) {
		return b ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * cast a char value to a Boolean Object(reference type)
	 * 
	 * @param c char value to cast
	 * @return casted Boolean Object
	 */
	public static Boolean toBoolean(char c) {
		return c != 0 ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * cast an int value to a Boolean Object(reference type)
	 * 
	 * @param i int value to cast
	 * @return casted Boolean Object
	 */
	public static Boolean toBoolean(int i) {
		return i != 0 ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * cast a long value to a Boolean Object(reference type)
	 * 
	 * @param l long value to cast
	 * @return casted Boolean Object
	 */
	public static Boolean toBoolean(long l) {
		return l != 0 ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * cast a double value to a Boolean Object(reference type)
	 * 
	 * @param d double value to cast
	 * @return casted Boolean Object
	 */
	public static Boolean toBoolean(double d) {
		return d != 0 ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * cast an Object to a Boolean Object(reference type)
	 * 
	 * @param o Object to cast
	 * @return casted Boolean Object
	 * @throws PageException
	 */
	public static Boolean toBoolean(Object o) throws PageException {
		if (o instanceof Boolean) return (Boolean) o;
		return toBooleanValue(o) ? Boolean.TRUE : Boolean.FALSE;

	}

	/**
	 * cast an Object to a Boolean Object(reference type)
	 * 
	 * @param str String to cast
	 * @return casted Boolean Object
	 * @throws PageException
	 */
	public static Boolean toBoolean(String str) throws PageException {
		return toBooleanValue(str) ? Boolean.TRUE : Boolean.FALSE;

	}

	/**
	 * cast an Object to a boolean value (primitive value type), Exception Less
	 * 
	 * @param o Object to cast
	 * @param defaultValue
	 * @return casted boolean value
	 */
	public static boolean toBooleanValue(Object o, boolean defaultValue) {
		if (o instanceof Boolean) return ((Boolean) o).booleanValue();
		else if (o instanceof Double) return toBooleanValue(((Double) o).doubleValue());
		else if (o instanceof Number) return toBooleanValue(((Number) o).doubleValue());
		else if (o instanceof String) {
			Boolean b = toBoolean(o.toString(), null);
			if (b != null) return b;
		}
		// else if(o instanceof Clob) return toBooleanValueEL(toStringEL(o));
		else if (o instanceof Castable) {
			return ((Castable) o).castToBoolean(Caster.toBoolean(defaultValue)).booleanValue();

		}
		else if (o == null) return toBooleanValue("", defaultValue);
		else if (o instanceof ObjectWrap) return toBooleanValue(((ObjectWrap) o).getEmbededObject(toBoolean(defaultValue)), defaultValue);

		return defaultValue;
	}

	/**
	 * cast an Object to a boolean value (refrence type), Exception Less
	 * 
	 * @param o Object to cast
	 * @param defaultValue default value
	 * @return casted boolean reference
	 */
	public static Boolean toBoolean(Object o, Boolean defaultValue) {
		if (o instanceof Boolean) return ((Boolean) o);
		else if (o instanceof Number) return ((Number) o).intValue() == 0 ? Boolean.FALSE : Boolean.TRUE;
		else if (o instanceof String) {
			int rtn = stringToBooleanValueEL(o.toString());
			if (rtn == 1) return Boolean.TRUE;
			else if (rtn == 0) return Boolean.FALSE;
			else {
				double dbl = toDoubleValue(o.toString(), Double.NaN);
				if (!Double.isNaN(dbl)) return toBooleanValue(dbl) ? Boolean.TRUE : Boolean.FALSE;
			}

		}
		// else if(o instanceof Clob) return toBooleanValueEL(toStringEL(o));
		else if (o instanceof Castable) {
			return ((Castable) o).castToBoolean(defaultValue);
		}
		else if (o instanceof ObjectWrap) return toBoolean(((ObjectWrap) o).getEmbededObject(defaultValue), defaultValue);

		else if (o == null) return toBoolean("", defaultValue);
		return defaultValue;
	}

	/**
	 * cast a boolean value to a char value
	 * 
	 * @param b boolean value to cast
	 * @return casted char value
	 */
	public static char toCharValue(boolean b) {
		return (char) (b ? 1 : 0);
	}

	/**
	 * cast a double value to a char value (primitive value type)
	 * 
	 * @param d double value to cast
	 * @return casted char value
	 */
	public static char toCharValue(double d) {
		return (char) d;
	}

	/**
	 * cast a char value to a char value (do nothing)
	 * 
	 * @param c char value to cast
	 * @return casted char value
	 */
	public static char toCharValue(char c) {
		return c;
	}

	/**
	 * cast an Object to a char value (primitive value type)
	 * 
	 * @param o Object to cast
	 * @return casted char value
	 * @throws PageException
	 */
	public static char toCharValue(Object o) throws PageException {
		if (o instanceof Character) return ((Character) o).charValue();
		else if (o instanceof Boolean) return (char) ((((Boolean) o).booleanValue()) ? 1 : 0);
		else if (o instanceof Double) return (char) (((Double) o).doubleValue());
		else if (o instanceof Number) return (char) (((Number) o).doubleValue());
		else if (o instanceof String) {
			String str = o.toString();
			if (str.length() > 0) return str.charAt(0);
			throw new ExpressionException("can't cast empty string to a char");

		}
		else if (o instanceof ObjectWrap) {
			return toCharValue(((ObjectWrap) o).getEmbededObject());
		}
		else if (o == null) return toCharValue("");
		throw new CasterException(o, "char");
	}

	/**
	 * cast an Object to a char value (primitive value type)
	 * 
	 * @param str Object to cast
	 * @return casted char value
	 * @throws PageException
	 */
	public static char toCharValue(String str) throws PageException {
		if (str.length() > 0) return str.charAt(0);
		throw new ExpressionException("can't cast empty string to a char");
	}

	/**
	 * cast an Object to a char value (primitive value type)
	 * 
	 * @param o Object to cast
	 * @param defaultValue
	 * @return casted char value
	 */
	public static char toCharValue(Object o, char defaultValue) {
		if (o instanceof Character) return ((Character) o).charValue();
		else if (o instanceof Boolean) return (char) ((((Boolean) o).booleanValue()) ? 1 : 0);
		else if (o instanceof Double) return (char) (((Double) o).doubleValue());
		else if (o instanceof Number) return (char) (((Number) o).doubleValue());
		else if (o instanceof String) {
			String str = o.toString();
			if (str.length() > 0) return str.charAt(0);
			return defaultValue;

		}
		else if (o instanceof ObjectWrap) {
			return toCharValue(((ObjectWrap) o).getEmbededObject(toCharacter(defaultValue)), defaultValue);
		}
		else if (o == null) return toCharValue("", defaultValue);
		return defaultValue;
	}

	/**
	 * cast a boolean value to a Character Object(reference type)
	 * 
	 * @param b boolean value to cast
	 * @return casted Character Object
	 */
	public static Character toCharacter(boolean b) {
		return new Character(toCharValue(b));
	}

	/**
	 * cast a char value to a Character Object(reference type)
	 * 
	 * @param c char value to cast
	 * @return casted Character Object
	 */
	public static Character toCharacter(char c) {
		return new Character(toCharValue(c));
	}

	/**
	 * cast a double value to a Character Object(reference type)
	 * 
	 * @param d double value to cast
	 * @return casted Character Object
	 */
	public static Character toCharacter(double d) {
		return new Character(toCharValue(d));
	}

	/**
	 * cast an Object to a Character Object(reference type)
	 * 
	 * @param o Object to cast
	 * @return casted Character Object
	 * @throws PageException
	 */
	public static Character toCharacter(Object o) throws PageException {
		if (o instanceof Character) return (Character) o;
		return new Character(toCharValue(o));

	}

	/**
	 * cast an Object to a Character Object(reference type)
	 * 
	 * @param str Object to cast
	 * @return casted Character Object
	 * @throws PageException
	 */
	public static Character toCharacter(String str) throws PageException {
		return new Character(toCharValue(str));

	}

	/**
	 * cast an Object to a Character Object(reference type)
	 * 
	 * @param o Object to cast
	 * @param defaultValue
	 * @return casted Character Object
	 */
	public static Character toCharacter(Object o, Character defaultValue) {
		if (o instanceof Character) return (Character) o;
		if (defaultValue != null) return new Character(toCharValue(o, defaultValue.charValue()));

		char res = toCharValue(o, Character.MIN_VALUE);
		if (res == Character.MIN_VALUE) return defaultValue;
		return new Character(res);
	}

	/**
	 * cast a boolean value to a byte value
	 * 
	 * @param b boolean value to cast
	 * @return casted byte value
	 */
	public static byte toByteValue(boolean b) {
		return (byte) (b ? 1 : 0);
	}

	/**
	 * cast a double value to a byte value (primitive value type)
	 * 
	 * @param d double value to cast
	 * @return casted byte value
	 */
	public static byte toByteValue(double d) {
		return (byte) d;
	}

	/**
	 * cast a char value to a byte value (do nothing)
	 * 
	 * @param c char value to cast
	 * @return casted byte value
	 */
	public static byte toByteValue(char c) {
		return (byte) c;
	}

	/**
	 * cast an Object to a byte value (primitive value type)
	 * 
	 * @param o Object to cast
	 * @return casted byte value
	 * @throws PageException
	 * @throws CasterException
	 */
	public static byte toByteValue(Object o) throws PageException {
		if (o instanceof Byte) return ((Byte) o).byteValue();
		if (o instanceof Character) return (byte) (((Character) o).charValue());
		else if (o instanceof Boolean) return (byte) ((((Boolean) o).booleanValue()) ? 1 : 0);
		else if (o instanceof Number) return (((Number) o).byteValue());
		else if (o instanceof String) return (byte) toDoubleValue(o.toString());
		else if (o instanceof ObjectWrap) {
			return toByteValue(((ObjectWrap) o).getEmbededObject());
		}
		throw new CasterException(o, "byte");
	}

	/**
	 * cast an Object to a byte value (primitive value type)
	 * 
	 * @param str Object to cast
	 * @return casted byte value
	 * @throws PageException
	 * @throws CasterException
	 */
	public static byte toByteValue(String str) throws PageException {
		return (byte) toDoubleValue(str.toString());
	}

	/**
	 * cast an Object to a byte value (primitive value type)
	 * 
	 * @param o Object to cast
	 * @param defaultValue
	 * @return casted byte value
	 */
	public static byte toByteValue(Object o, byte defaultValue) {
		if (o instanceof Byte) return ((Byte) o).byteValue();
		if (o instanceof Character) return (byte) (((Character) o).charValue());
		else if (o instanceof Boolean) return (byte) ((((Boolean) o).booleanValue()) ? 1 : 0);
		else if (o instanceof Number) return (((Number) o).byteValue());
		else if (o instanceof String) return (byte) toDoubleValue(o.toString(), defaultValue);
		else if (o instanceof ObjectWrap) {
			return toByteValue(((ObjectWrap) o).getEmbededObject(toByte(defaultValue)), defaultValue);
		}
		return defaultValue;
	}

	/**
	 * cast a boolean value to a Byte Object(reference type)
	 * 
	 * @param b boolean value to cast
	 * @return casted Byte Object
	 */
	public static Byte toByte(boolean b) {
		return new Byte(toByteValue(b));
	}

	/**
	 * cast a char value to a Byte Object(reference type)
	 * 
	 * @param c char value to cast
	 * @return casted Byte Object
	 */
	public static Byte toByte(char c) {
		return new Byte(toByteValue(c));
	}

	/**
	 * cast a double value to a Byte Object(reference type)
	 * 
	 * @param d double value to cast
	 * @return casted Byte Object
	 */
	public static Byte toByte(double d) {
		return new Byte(toByteValue(d));
	}

	/**
	 * cast an Object to a Byte Object(reference type)
	 * 
	 * @param o Object to cast
	 * @return casted Byte Object
	 * @throws PageException
	 */
	public static Byte toByte(Object o) throws PageException {
		if (o instanceof Byte) return (Byte) o;
		return new Byte(toByteValue(o));

	}

	/**
	 * cast an Object to a Byte Object(reference type)
	 * 
	 * @param str String to cast
	 * @return casted Byte Object
	 * @throws PageException
	 */
	public static Byte toByte(String str) throws PageException {
		return new Byte(toByteValue(str));

	}

	/**
	 * cast an Object to a Byte Object(reference type)
	 * 
	 * @param o Object to cast
	 * @param defaultValue
	 * @return casted Byte Object
	 */
	public static Byte toByte(Object o, Byte defaultValue) {
		if (o instanceof Byte) return (Byte) o;
		if (defaultValue != null) return new Byte(toByteValue(o, defaultValue.byteValue()));
		byte res = toByteValue(o, Byte.MIN_VALUE);
		if (res == Byte.MIN_VALUE) return defaultValue;
		return new Byte(res);
	}

	/**
	 * cast a boolean value to a long value
	 * 
	 * @param b boolean value to cast
	 * @return casted long value
	 */
	public static long toLongValue(boolean b) {
		return (b ? 1L : 0L);
	}

	/**
	 * cast a double value to a long value (primitive value type)
	 * 
	 * @param d double value to cast
	 * @return casted long value
	 */
	public static long toLongValue(double d) {
		return (long) d;
	}

	/**
	 * cast a char value to a long value (do nothing)
	 * 
	 * @param c char value to cast
	 * @return casted long value
	 */
	public static long toLongValue(char c) {
		return c;
	}

	/**
	 * cast an Object to a long value (primitive value type)
	 * 
	 * @param o Object to cast
	 * @return casted long value
	 * @throws PageException
	 */
	public static long toLongValue(Object o) throws PageException {
		if (o instanceof Boolean) return ((((Boolean) o).booleanValue()) ? 1L : 0L);
		else if (o instanceof Number) return (((Number) o).longValue());
		else if (o instanceof CharSequence) {
			String str = o.toString();
			try {
				return Long.parseLong(str);
			}
			catch (NumberFormatException nfe) {
				return (long) toDoubleValue(str);
			}
		}
		else if (o instanceof Character) return (((Character) o).charValue());
		else if (o instanceof Castable) return (long) ((Castable) o).castToDoubleValue();
		else if (o instanceof ObjectWrap) return toLongValue(((ObjectWrap) o).getEmbededObject());

		throw new CasterException(o, "long");
	}

	/**
	 * cast an Object to a long value (primitive value type)
	 * 
	 * @param str Object to cast
	 * @return casted long value
	 * @throws PageException
	 */
	public static long toLongValue(String str) throws PageException {
		BigInteger bi = null;
		try {
			bi = new BigInteger(str);

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		if (bi != null) {
			if (bi.bitLength() < 64) return bi.longValue();
			throw new ApplicationException("number [" + str + "] cannot be casted to a long value, number is to long (" + (bi.bitLength() + 1) + " bit)");
		}
		return (long) toDoubleValue(str);
	}

	/**
	 * returns a number Object, this can be a BigDecimal,BigInteger,Long, Double, depending on the
	 * input.
	 * 
	 * @param str
	 * @return
	 * @throws PageException
	 */
	public static Number toNumber(String str, Number defaultValue) {
		try {
			// float
			if (str.indexOf('.') != -1) {
				return new BigDecimal(str);
			}
			// integer
			BigInteger bi = new BigInteger(str);
			int l = bi.bitLength();
			if (l < 32) return new Integer(bi.intValue());
			if (l < 64) return new Long(bi.longValue());
			return bi;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}
	}

	/**
	 * cast an Object to a long value (primitive value type)
	 * 
	 * @param o Object to cast
	 * @param defaultValue
	 * @return casted long value
	 */
	public static long toLongValue(Object o, long defaultValue) {
		if (o instanceof Character) return (((Character) o).charValue());
		else if (o instanceof Boolean) return ((((Boolean) o).booleanValue()) ? 1L : 0L);
		else if (o instanceof Number) return (((Number) o).longValue());
		else if (o instanceof CharSequence) return (long) toDoubleValue(o.toString(), defaultValue);
		else if (o instanceof Castable) {
			return (long) ((Castable) o).castToDoubleValue(defaultValue);
		}
		else if (o instanceof Character) return (((Character) o).charValue());
		else if (o instanceof ObjectWrap) return toLongValue(((ObjectWrap) o).getEmbededObject(toLong(defaultValue)), defaultValue);

		return defaultValue;
	}

	/**
	 * cast a boolean value to a Long Object(reference type)
	 * 
	 * @param b boolean value to cast
	 * @return casted Long Object
	 */
	public static Long toLong(boolean b) {
		return Long.valueOf(toLongValue(b));
	}

	/**
	 * cast a char value to a Long Object(reference type)
	 * 
	 * @param c char value to cast
	 * @return casted Long Object
	 */
	public static Long toLong(char c) {
		return Long.valueOf(toLongValue(c));
	}

	/**
	 * cast a double value to a Long Object(reference type)
	 * 
	 * @param d double value to cast
	 * @return casted Long Object
	 */
	public static Long toLong(double d) {
		return Long.valueOf(toLongValue(d));
	}

	/**
	 * cast an Object to a Long Object(reference type)
	 * 
	 * @param o Object to cast
	 * @return casted Long Object
	 * @throws PageException
	 */
	public static Long toLong(Object o) throws PageException {
		if (o instanceof Long) return (Long) o;
		return Long.valueOf(toLongValue(o));

	}

	/**
	 * cast an Object to a Long Object(reference type)
	 * 
	 * @param str Object to cast
	 * @return casted Long Object
	 * @throws PageException
	 */
	public static Long toLong(String str) throws PageException {
		return Long.valueOf(toLongValue(str));

	}

	/**
	 * cast a long to a Long Object(reference type)
	 * 
	 * @param l long to cast
	 * @return casted Long Object
	 */
	public static Long toLong(long l) {
		return Long.valueOf(l);

	}

	/**
	 * cast an Object to a Long Object(reference type)
	 * 
	 * @param o Object to cast
	 * @param defaultValue
	 * @return casted Long Object
	 */
	public static Long toLong(Object o, Long defaultValue) {
		if (o instanceof Long) return (Long) o;
		if (defaultValue != null) return Long.valueOf(toLongValue(o, defaultValue.longValue()));

		long res = toLongValue(o, Long.MIN_VALUE);
		if (res == Long.MIN_VALUE) return defaultValue;
		return Long.valueOf(res);
	}

	/**
	 * cast a boolean value to a Float Object(reference type)
	 * 
	 * @param b boolean value to cast
	 * @return casted Float Object
	 */
	public static Float toFloat(boolean b) {
		return new Float(toFloatValue(b));
	}

	/**
	 * cast a char value to a Float Object(reference type)
	 * 
	 * @param c char value to cast
	 * @return casted Float Object
	 */
	public static Float toFloat(char c) {
		return new Float(toFloatValue(c));
	}

	/**
	 * cast a double value to a Float Object(reference type)
	 * 
	 * @param d double value to cast
	 * @return casted Float Object
	 */
	public static Float toFloat(double d) {
		return new Float(toFloatValue(d));
	}

	/**
	 * cast an Object to a Float Object(reference type)
	 * 
	 * @param o Object to cast
	 * @return casted Float Object
	 * @throws PageException
	 */
	public static Float toFloat(Object o) throws PageException {
		if (o instanceof Float) return (Float) o;
		return new Float(toFloatValue(o));
	}

	/**
	 * cast an Object to a Float Object(reference type)
	 * 
	 * @param str Object to cast
	 * @return casted Float Object
	 * @throws PageException
	 */
	public static Float toFloat(String str) throws PageException {
		return new Float(toFloatValue(str));
	}

	/**
	 * cast an Object to a Float Object(reference type)
	 * 
	 * @param o Object to cast
	 * @param defaultValue
	 * @return casted Float Object
	 */
	public static Float toFloat(Object o, Float defaultValue) {
		if (o instanceof Float) return (Float) o;
		if (defaultValue != null) return new Float(toFloatValue(o, defaultValue.floatValue()));

		float res = toFloatValue(o, Float.MIN_VALUE);
		if (res == Float.MIN_VALUE) return defaultValue;
		return new Float(res);
	}

	/**
	 * cast a boolean value to a float value
	 * 
	 * @param b boolean value to cast
	 * @return casted long value
	 */
	public static float toFloatValue(boolean b) {
		return (b ? 1F : 0F);
	}

	/**
	 * cast a double value to a long value (primitive value type)
	 * 
	 * @param d double value to cast
	 * @return casted long value
	 */
	public static float toFloatValue(double d) {
		return (float) d;
	}

	/**
	 * cast a char value to a long value (do nothing)
	 * 
	 * @param c char value to cast
	 * @return casted long value
	 */
	public static float toFloatValue(char c) {
		return c;
	}

	/**
	 * cast an Object to a long value (primitive value type)
	 * 
	 * @param o Object to cast
	 * @return casted long value
	 * @throws PageException
	 */
	public static float toFloatValue(Object o) throws PageException {
		if (o instanceof Boolean) return ((((Boolean) o).booleanValue()) ? 1F : 0F);
		else if (o instanceof Number) return (((Number) o).floatValue());
		else if (o instanceof CharSequence) return (float) toDoubleValue(o.toString());
		else if (o instanceof Character) return (((Character) o).charValue());
		else if (o instanceof Castable) return (float) ((Castable) o).castToDoubleValue();
		else if (o instanceof ObjectWrap) return toFloatValue(((ObjectWrap) o).getEmbededObject());

		throw new CasterException(o, "float");
	}

	/**
	 * cast an Object to a long value (primitive value type)
	 * 
	 * @param str Object to cast
	 * @return casted long value
	 * @throws PageException
	 */
	public static float toFloatValue(String str) throws PageException {
		return (float) toDoubleValue(str);
	}

	/**
	 * cast an Object to a float value (primitive value type)
	 * 
	 * @param o Object to cast
	 * @param defaultValue
	 * @return casted float value
	 */
	public static float toFloatValue(Object o, float defaultValue) {
		if (o instanceof Character) return (((Character) o).charValue());
		else if (o instanceof Boolean) return ((((Boolean) o).booleanValue()) ? 1F : 0F);
		else if (o instanceof Number) return (((Number) o).floatValue());
		else if (o instanceof CharSequence) return (float) toDoubleValue(o.toString(), defaultValue);
		else if (o instanceof Character) return (((Character) o).charValue());
		else if (o instanceof Castable) {
			return (float) ((Castable) o).castToDoubleValue(defaultValue);

		}
		else if (o instanceof ObjectWrap) return toFloatValue(((ObjectWrap) o).getEmbededObject(toFloat(defaultValue)), defaultValue);

		return defaultValue;
	}

	/**
	 * cast a boolean value to a short value
	 * 
	 * @param b boolean value to cast
	 * @return casted short value
	 */
	public static short toShortValue(boolean b) {
		return (short) (b ? 1 : 0);
	}

	/**
	 * cast a double value to a short value (primitive value type)
	 * 
	 * @param d double value to cast
	 * @return casted short value
	 */
	public static short toShortValue(double d) {
		return (short) d;
	}

	/**
	 * cast a char value to a short value (do nothing)
	 * 
	 * @param c char value to cast
	 * @return casted short value
	 */
	public static short toShortValue(char c) {
		return (short) c;
	}

	/**
	 * cast an Object to a short value (primitive value type)
	 * 
	 * @param o Object to cast
	 * @return casted short value
	 * @throws PageException
	 */
	public static short toShortValue(Object o) throws PageException {
		if (o instanceof Short) return ((Short) o).shortValue();
		else if (o instanceof CharSequence) return (short) toDoubleValue(o.toString());
		else if (o instanceof Character) return (short) (((Character) o).charValue());
		else if (o instanceof Boolean) return (short) ((((Boolean) o).booleanValue()) ? 1 : 0);
		else if (o instanceof Number) return (((Number) o).shortValue());
		else if (o instanceof Castable) return (short) ((Castable) o).castToDoubleValue();
		else if (o instanceof ObjectWrap) return toShortValue(((ObjectWrap) o).getEmbededObject());

		throw new CasterException(o, "short");
	}

	/**
	 * cast an Object to a short value (primitive value type)
	 * 
	 * @param str Object to cast
	 * @return casted short value
	 * @throws PageException
	 */
	public static short toShortValue(String str) throws PageException {
		return (short) toDoubleValue(str);
	}

	/**
	 * cast an Object to a short value (primitive value type)
	 * 
	 * @param o Object to cast
	 * @param defaultValue
	 * @return casted short value
	 */
	public static short toShortValue(Object o, short defaultValue) {
		if (o instanceof Short) return ((Short) o).shortValue();
		else if (o instanceof Boolean) return (short) ((((Boolean) o).booleanValue()) ? 1 : 0);
		else if (o instanceof Number) return (((Number) o).shortValue());
		else if (o instanceof CharSequence) return (short) toDoubleValue(o.toString(), defaultValue);
		else if (o instanceof Character) return (short) (((Character) o).charValue());
		else if (o instanceof Castable) {
			return (short) ((Castable) o).castToDoubleValue(defaultValue);

		}
		else if (o instanceof ObjectWrap) return toShortValue(((ObjectWrap) o).getEmbededObject(toShort(defaultValue)), defaultValue);

		return defaultValue;
	}

	/**
	 * cast a boolean value to a Short Object(reference type)
	 * 
	 * @param b boolean value to cast
	 * @return casted Short Object
	 */
	public static Short toShort(boolean b) {
		return Short.valueOf(toShortValue(b));
	}

	/**
	 * cast a char value to a Short Object(reference type)
	 * 
	 * @param c char value to cast
	 * @return casted Short Object
	 */
	public static Short toShort(char c) {
		return Short.valueOf(toShortValue(c));
	}

	/**
	 * cast a double value to a Byte Object(reference type)
	 * 
	 * @param d double value to cast
	 * @return casted Byte Object
	 */
	public static Short toShort(double d) {
		return Short.valueOf(toShortValue(d));
	}

	/**
	 * cast an Object to a Byte Object(reference type)
	 * 
	 * @param o Object to cast
	 * @return casted Byte Object
	 * @throws PageException
	 */
	public static Short toShort(Object o) throws PageException {
		if (o instanceof Short) return (Short) o;
		return Short.valueOf(toShortValue(o));

	}

	/**
	 * cast an Object to a Byte Object(reference type)
	 * 
	 * @param str Object to cast
	 * @return casted Byte Object
	 * @throws PageException
	 */
	public static Short toShort(String str) throws PageException {
		return Short.valueOf(toShortValue(str));

	}

	/**
	 * cast an Object to a Byte Object(reference type)
	 * 
	 * @param o Object to cast
	 * @param defaultValue
	 * @return casted Byte Object
	 */
	public static Short toShort(Object o, Short defaultValue) {
		if (o instanceof Short) return (Short) o;
		if (defaultValue != null) return Short.valueOf(toShortValue(o, defaultValue.shortValue()));
		short res = toShortValue(o, Short.MIN_VALUE);
		if (res == Short.MIN_VALUE) return defaultValue;
		return Short.valueOf(res);
	}

	/**
	 * cast a String to a boolean value (primitive value type)
	 * 
	 * @param str String to cast
	 * @return casted boolean value
	 * @throws ExpressionException
	 */
	public static boolean stringToBooleanValue(String str) throws ExpressionException {
		str = StringUtil.toLowerCase(str.trim());
		if (str.equals("yes") || str.equals("true")) return true;
		else if (str.equals("no") || str.equals("false")) return false;
		throw new CasterException("Can't cast String [" + CasterException.crop(str) + "] to boolean");
	}

	/**
	 * cast a String to a boolean value (primitive value type), return 1 for true, 0 for false and -1 if
	 * can't cast to a boolean type
	 * 
	 * @param str String to cast
	 * @return casted boolean value
	 */
	public static int stringToBooleanValueEL(String str) {
		if (str.length() < 2) return -1;
		switch (str.charAt(0)) {
		case 't':
		case 'T':
			return str.equalsIgnoreCase("true") ? 1 : -1;
		case 'f':
		case 'F':
			return str.equalsIgnoreCase("false") ? 0 : -1;
		case 'y':
		case 'Y':
			return str.equalsIgnoreCase("yes") ? 1 : -1;
		case 'n':
		case 'N':
			return str.equalsIgnoreCase("no") ? 0 : -1;
		}
		return -1;
	}

	/**
	 * cast an Object to a String
	 * 
	 * @param o Object to cast
	 * @return casted String
	 * @throws PageException
	 */
	public static String toString(Object o) throws PageException {
		if (o instanceof String) return (String) o;
		else if (o instanceof Number) return toString(((Number) o));
		else if (o instanceof Boolean) return toString(((Boolean) o).booleanValue());
		else if (o instanceof Castable) return ((Castable) o).castToString();
		else if (o instanceof Date) {
			if (o instanceof DateTime) return ((DateTime) o).castToString();
			return new DateTimeImpl((Date) o).castToString();
		}
		else if (o instanceof Clob) return toString((Clob) o);
		else if (o instanceof Locale) return toString((Locale) o);
		else if (o instanceof TimeZone) return toString((TimeZone) o);
		else if (o instanceof Node) return XMLCaster.toString((Node) o);
		else if (o instanceof Reader) {
			Reader r = null;
			try {
				return IOUtil.toString(r = (Reader) o);
			}
			catch (IOException e) {
				throw Caster.toPageException(e);
			}
			finally {
				try {
					IOUtil.close(r);
				}
				catch (IOException e) {
					throw Caster.toPageException(e);
				}
			}
		}
		else if (o instanceof Throwable) {
			return toString((Throwable) o, true);
		}
		else if (o instanceof InputStream) {
			PageContextImpl pc = (PageContextImpl) ThreadLocalPageContext.get();
			InputStream r = null;
			try {
				return IOUtil.toString(r = (InputStream) o, pc.getWebCharset());
			}
			catch (IOException e) {
				throw Caster.toPageException(e);
			}
			finally {
				try {
					IOUtil.close(r);
				}
				catch (IOException e) {
					throw Caster.toPageException(e);
				}
			}
		}
		else if (o instanceof byte[]) {
			PageContextImpl pc = (PageContextImpl) ThreadLocalPageContext.get();

			try {
				return new String((byte[]) o, pc.getWebCharset());
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				return new String((byte[]) o);
			}
		}
		else if (o instanceof char[]) return new String((char[]) o);
		else if (o instanceof ObjectWrap) return toString(((ObjectWrap) o).getEmbededObject());
		else if (o instanceof Calendar) return toString(((Calendar) o).getTime());
		else if (o == null) return "";

		// INFO Collection is new of type Castable
		if (o instanceof Map || o instanceof List || o instanceof Function) throw new CasterException(o, "string");
		/*
		 * if((x instanceof Query) || (x instanceof RowSet) || (x instanceof coldfusion.runtime.Array) || (x
		 * instanceof JavaProxy) || (x instanceof FileStreamWrapper))
		 */

		return o.toString();
	}

	/**
	 * cast a String to a String (do Nothing)
	 * 
	 * @param str
	 * @return casted String
	 * @throws PageException
	 */
	public static String toString(String str) {
		return str;
	}

	public static StringBuffer toStringBuffer(Object obj) throws PageException {
		if (obj instanceof StringBuffer) return (StringBuffer) obj;
		return new StringBuffer(toString(obj));
	}

	public static Collection.Key toKey(Object o) throws CasterException {
		return KeyImpl.toKey(o);
	}

	public static Collection.Key toKey(Object o, Collection.Key defaultValue) {
		return KeyImpl.toKey(o, defaultValue);
	}

	/**
	 * cast an Object to a String dont throw an exception, if can't cast to a string return an empty
	 * string
	 * 
	 * @param o Object to cast
	 * @param defaultValue
	 * @return casted String
	 */
	public static String toString(Object o, String defaultValue) {
		return toString(o, true, defaultValue);
	}

	public static String toString(Object o, boolean executeDefaultToStringMethod, String defaultValue) {
		if (o instanceof String) return (String) o;
		else if (o instanceof Boolean) return toString(((Boolean) o).booleanValue());
		else if (o instanceof Number) return toString(((Number) o));
		else if (o instanceof Castable) return ((Castable) o).castToString(defaultValue);
		else if (o instanceof Date) {
			if (o instanceof DateTime) {
				return ((DateTime) o).castToString(defaultValue);

			}
			return new DateTimeImpl((Date) o).castToString(defaultValue);
		}
		else if (o instanceof Clob) {
			try {
				return toString((Clob) o);
			}
			catch (ExpressionException e) {
				return defaultValue;
			}
		}
		else if (o instanceof Node) {
			try {
				return XMLCaster.toString((Node) o);
			}
			catch (PageException e) {
				return defaultValue;
			}
		}
		else if (o instanceof Map || o instanceof List || o instanceof Function) return defaultValue;
		else if (o == null) return "";
		else if (o instanceof ObjectWrap) return toString(((ObjectWrap) o).getEmbededObject(defaultValue), defaultValue);
		return executeDefaultToStringMethod ? o.toString() : defaultValue;
		/// TODO diese methode ist nicht gleich wie toString(Object)
	}

	private static String toString(Clob clob) throws ExpressionException {
		try {
			Reader in = clob.getCharacterStream();
			StringBuffer buf = new StringBuffer();
			for (int c = in.read(); c != -1; c = in.read()) {
				buf.append((char) c);
			}
			return buf.toString();
		}
		catch (Exception e) {
			throw ExpressionException.newInstance(e);
		}
	}

	public static String toString(Locale l) {
		return LocaleFactory.toString(l);
	}

	public static Locale toLocale(Object obj) throws PageException {
		if (obj instanceof Locale) return (Locale) obj;
		return LocaleFactory.getLocale(toString(obj));
	}

	public static Locale toLocale(Object obj, Locale defaultValue) {
		if (obj instanceof Locale) return (Locale) obj;
		String str = toString(obj, null);
		if (str == null) return defaultValue;
		return toLocale(str, defaultValue);
	}

	public static String toString(TimeZone tz) {
		return TimeZoneUtil.toString(tz);
	}

	public static String toString(Throwable t, boolean addMessage) {
		return ExceptionUtil.getStacktrace(t, addMessage);
	}

	public static TimeZone toTimeZone(Object obj) throws PageException {
		if (obj instanceof TimeZone) return (TimeZone) obj;
		return TimeZoneUtil.toTimeZone(toString(obj));
	}

	/**
	 * cast a double value to a String
	 * 
	 * @param d double value to cast
	 * @return casted String
	 */
	public static String toString3(double d) {

		long l = (long) d;
		if (l == d) return toString(l);
		String str = Double.toString(d);
		int pos;
		if ((pos = str.indexOf('E')) != -1 && pos == str.length() - 2) {
			return new StringBuffer(pos + 2).append(str.charAt(0)).append(str.substring(2, toDigit(str.charAt(pos + 1)) + 2)).append('.')
					.append(str.substring(toDigit(str.charAt(pos + 1)) + 2, pos)).toString();

		}
		return str;
	}

	private static DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(Locale.US);// ("#.###########");
	static {
		df.applyLocalizedPattern("#.############");
	}

	public static String toString(double d) {
		long l = (long) d;
		if (l == d) return toString(l);

		if (d > l && (d - l) < 0.000000000001) return toString(l);
		if (l > d && (l - d) < 0.000000000001) return toString(l);

		return df.format(d);
	}

	public static String toString(Number n) {
		double d = n.doubleValue();
		long l = (long) d;
		if (l == d) return toString(l);

		if (d > l && (d - l) < 0.000000000001) return toString(l);
		if (l > d && (l - d) < 0.000000000001) return toString(l);

		if (n instanceof Double) return toString(n.doubleValue());
		return n.toString();
		// return df.format(d);
	}

	/**
	 * cast a long value to a String
	 * 
	 * @param l long value to cast
	 * @return casted String
	 */
	public static String toString(long l) {
		if (l < NUMBERS_MIN || l > NUMBERS_MAX) {
			return Long.toString(l, 10);
		}
		return NUMBERS[(int) l];
	}

	/**
	 * cast an int value to a String
	 * 
	 * @param i int value to cast
	 * @return casted String
	 */
	public static String toString(int i) {
		if (i < NUMBERS_MIN || i > NUMBERS_MAX) return Integer.toString(i, 10);
		return NUMBERS[i];
	}

	/**
	 * cast a boolean value to a String
	 * 
	 * @param b boolean value to cast
	 * @return casted String
	 */
	public static String toString(boolean b) {
		return b ? "true" : "false";
	}

	public static UDF toFunction(Object o) throws PageException {
		if (o instanceof UDF) return (UDF) o;

		else if (o instanceof ObjectWrap) {
			return toFunction(((ObjectWrap) o).getEmbededObject());
		}
		throw new CasterException(o, "function");
	}

	public static UDF toFunction(Object o, UDF defaultValue) {
		if (o instanceof UDF) return (UDF) o;

		else if (o instanceof ObjectWrap) {
			return toFunction(((ObjectWrap) o).getEmbededObject(defaultValue), defaultValue);
		}
		return defaultValue;
	}

	/**
	 * cast an Object to an Array Object
	 * 
	 * @param o Object to cast
	 * @return casted Array
	 * @throws PageException
	 */
	public static List toList(Object o) throws PageException {
		return toList(o, false);
	}

	/**
	 * cast an Object to an Array Object
	 * 
	 * @param o Object to cast
	 * @param defaultValue
	 * @return casted Array
	 */
	public static List toList(Object o, List defaultValue) {
		return toList(o, false, defaultValue);
	}

	/**
	 * cast an Object to an Array Object
	 * 
	 * @param o Object to cast
	 * @param duplicate
	 * @param defaultValue
	 * @return casted Array
	 */
	public static List toList(Object o, boolean duplicate, List defaultValue) {
		try {
			return toList(o, duplicate);
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	/**
	 * cast an Object to an Array Object
	 * 
	 * @param o Object to cast
	 * @param duplicate
	 * @return casted Array
	 * @throws PageException
	 */
	public static List toList(Object o, boolean duplicate) throws PageException {
		if (o instanceof List) {
			if (duplicate) {
				List src = (List) o;
				int size = src.size();
				ArrayList trg = new ArrayList();

				for (int i = 0; i < size; i++) {
					trg.add(i, src.get(i));
				}
				return trg;

			}
			return (List) o;
		}
		else if (o instanceof Object[]) {
			ArrayList list = new ArrayList();
			Object[] arr = (Object[]) o;
			for (int i = 0; i < arr.length; i++)
				list.add(i, arr[i]);
			return list;
		}
		else if (o instanceof Array) {
			if (!duplicate) return ArrayAsList.toList((Array) o);
			ArrayList list = new ArrayList();
			Array arr = (Array) o;
			for (int i = 0; i < arr.size(); i++)
				list.add(i, arr.get(i + 1, null));
			return list;
		}
		else if (o instanceof Iterator) {
			Iterator it = (Iterator) o;
			ArrayList list = new ArrayList();
			while (it.hasNext()) {
				list.add(it.next());
			}
			return list;
		}
		else if (o instanceof XMLStruct) {
			XMLStruct sct = ((XMLStruct) o);
			if (sct instanceof XMLMultiElementStruct) return toList(new XMLMultiElementArray((XMLMultiElementStruct) o));
			ArrayList list = new ArrayList();
			list.add(sct);
			return list;
		}
		else if (o instanceof ObjectWrap) {
			return toList(((ObjectWrap) o).getEmbededObject());
		}
		else if (o instanceof Struct) {
			if (o instanceof Component) {
				try {
					Object tmp = Reflector.componentToClass(ThreadLocalPageContext.get(), (Component) o, List.class);
					if (tmp instanceof List) return (List) tmp;
				}
				catch (PageException e) {}
			}

			Struct sct = (Struct) o;
			ArrayList arr = new ArrayList();

			Iterator<Entry<Key, Object>> it = sct.entryIterator();
			Entry<Key, Object> e = null;
			try {
				while (it.hasNext()) {
					e = it.next();
					arr.add(toIntValue(e.getKey().getString()), e.getValue());
				}
			}
			catch (ExpressionException ee) {
				throw new ExpressionException("can't cast struct to an array, key [" + (e != null ? e.getKey() : "") + "] is not a number");
			}
			return arr;
		}
		else if (o instanceof boolean[]) return toList(ArrayUtil.toReferenceType((boolean[]) o));
		else if (o instanceof byte[]) return toList(ArrayUtil.toReferenceType((byte[]) o));
		else if (o instanceof char[]) return toList(ArrayUtil.toReferenceType((char[]) o));
		else if (o instanceof short[]) return toList(ArrayUtil.toReferenceType((short[]) o));
		else if (o instanceof int[]) return toList(ArrayUtil.toReferenceType((int[]) o));
		else if (o instanceof long[]) return toList(ArrayUtil.toReferenceType((long[]) o));
		else if (o instanceof float[]) return toList(ArrayUtil.toReferenceType((float[]) o));
		else if (o instanceof double[]) return toList(ArrayUtil.toReferenceType((double[]) o));

		throw new CasterException(o, "List");

	}

	/**
	 * cast an Object to an Array Object
	 * 
	 * @param o Object to cast
	 * @return casted Array
	 * @throws PageException
	 */
	public static Array toArray(Object o) throws PageException {
		if (o instanceof Array) return (Array) o;
		else if (o instanceof Object[]) {
			return new ArrayImpl((Object[]) o);
		}
		else if (o instanceof List) {
			return ListAsArray.toArray((List) o);// new ArrayImpl(((List) o).toArray());
		}
		else if (o instanceof Set) {
			return toArray(((Set) o).toArray());// new ArrayImpl(((List) o).toArray());
		}
		else if (o instanceof XMLStruct) {
			XMLMultiElementStruct xmes;
			if (o instanceof XMLMultiElementStruct) {
				xmes = (XMLMultiElementStruct) o;
			}
			else {
				XMLStruct sct = (XMLStruct) o;
				Array a = new ArrayImpl();
				a.append(o);
				xmes = new XMLMultiElementStruct(a, sct.getCaseSensitive());
			}
			return new XMLMultiElementArray(xmes);
		}
		else if (o instanceof ObjectWrap) {
			return toArray(((ObjectWrap) o).getEmbededObject());
		}
		else if (o instanceof Struct) {

			// function _toArray
			if (o instanceof Component) {
				Component c = (Component) o;
				PageContext pc = ThreadLocalPageContext.get();
				if (pc != null) {
					Member member = c.getMember(Component.ACCESS_PRIVATE, KeyConstants.__toArray, false, false);
					// Object o = get(pc,"_toString",null);
					if (member instanceof UDF) {
						UDF udf = (UDF) member;
						if (udf.getReturnType() == CFTypes.TYPE_ARRAY && udf.getFunctionArguments().length == 0) {
							return Caster.toArray(c.call(pc, KeyConstants.__toArray, new Object[0]));
						}
					}
				}
			}

			return StructAsArray.toArray((Struct) o);
			/*
			 * Struct sct=(Struct) o; Array arr=new ArrayImpl();
			 * 
			 * Iterator<Entry<Key, Object>> it = sct.entryIterator(); Entry<Key, Object> e=null; try {
			 * while(it.hasNext()) { e = it.next(); arr.setE(toIntValue(e.getKey().getString()),e.getValue()); }
			 * } catch (ExpressionException ee) { throw new
			 * ExpressionException("can't cast struct to an array, key ["+e.getKey().getString()
			 * +"] is not a number"); } return arr;
			 */
		}
		else if (o instanceof boolean[]) return new ArrayImpl(ArrayUtil.toReferenceType((boolean[]) o));
		else if (o instanceof byte[]) return new ArrayImpl(ArrayUtil.toReferenceType((byte[]) o));
		else if (o instanceof char[]) return new ArrayImpl(ArrayUtil.toReferenceType((char[]) o));
		else if (o instanceof short[]) return new ArrayImpl(ArrayUtil.toReferenceType((short[]) o));
		else if (o instanceof int[]) return new ArrayImpl(ArrayUtil.toReferenceType((int[]) o));
		else if (o instanceof long[]) return new ArrayImpl(ArrayUtil.toReferenceType((long[]) o));
		else if (o instanceof float[]) return new ArrayImpl(ArrayUtil.toReferenceType((float[]) o));
		else if (o instanceof double[]) return new ArrayImpl(ArrayUtil.toReferenceType((double[]) o));

		throw new CasterException(o, "Array");
	}

	public static Object[] toNativeArray(Object o, Object[] defaultValue) {
		if (o instanceof Object[]) {
			return (Object[]) o;
		}
		else if (o instanceof Array) {
			Array arr = (Array) o;
			Object[] objs = new Object[arr.size()];
			for (int i = 0; i < objs.length; i++) {
				objs[i] = arr.get(i + 1, null);
			}
			return objs;
		}
		else if (o instanceof List) {
			return ((List) o).toArray();
		}
		else if (o instanceof XMLStruct) {
			XMLStruct sct = ((XMLStruct) o);
			// if(sct instanceof XMLMultiElementStruct) return toNativeArray((sct));
			Object[] a = new Object[1];
			a[0] = sct;
			return a;
		}
		else if (o instanceof ObjectWrap) {
			return toNativeArray(((ObjectWrap) o).getEmbededObject(defaultValue), defaultValue);
		}
		else if (o instanceof Struct) {
			Struct sct = (Struct) o;
			Array arr = new ArrayImpl();

			Iterator<Entry<Key, Object>> it = sct.entryIterator();
			Entry<Key, Object> e = null;
			try {
				while (it.hasNext()) {
					e = it.next();
					arr.setEL(toIntValue(e.getKey().getString()), e.getValue());
				}
			}
			catch (ExpressionException ee) {
				return defaultValue;
			}
			return toNativeArray(arr, defaultValue);
		}
		else if (o instanceof boolean[]) return ArrayUtil.toReferenceType((boolean[]) o);
		else if (o instanceof byte[]) return ArrayUtil.toReferenceType((byte[]) o);
		else if (o instanceof char[]) return ArrayUtil.toReferenceType((char[]) o);
		else if (o instanceof short[]) return ArrayUtil.toReferenceType((short[]) o);
		else if (o instanceof int[]) return ArrayUtil.toReferenceType((int[]) o);
		else if (o instanceof long[]) return ArrayUtil.toReferenceType((long[]) o);
		else if (o instanceof float[]) return ArrayUtil.toReferenceType((float[]) o);
		else if (o instanceof double[]) return ArrayUtil.toReferenceType((double[]) o);

		return defaultValue;
	}

	public static Object[] toNativeArray(Object o) throws PageException {
		if (o instanceof Object[]) {
			return (Object[]) o;
		}
		else if (o instanceof Array) {
			Array arr = (Array) o;
			Object[] objs = new Object[arr.size()];
			for (int i = 0; i < objs.length; i++) {
				objs[i] = arr.get(i + 1, null);
			}
			return objs;
		}
		else if (o instanceof List) {
			return ((List) o).toArray();
		}
		else if (o instanceof XMLStruct) {
			XMLStruct sct = ((XMLStruct) o);
			// if(sct instanceof XMLMultiElementStruct) return toNativeArray((sct));
			Object[] a = new Object[1];
			a[0] = sct;
			return a;
		}
		else if (o instanceof ObjectWrap) {
			return toNativeArray(((ObjectWrap) o).getEmbededObject());
		}
		else if (o instanceof Struct) {
			Struct sct = (Struct) o;
			Array arr = new ArrayImpl();

			Iterator<Entry<Key, Object>> it = sct.entryIterator();
			Entry<Key, Object> e = null;
			try {
				while (it.hasNext()) {
					e = it.next();
					arr.setE(toIntValue(e.getKey().getString()), e.getValue());
				}
			}
			catch (ExpressionException ee) {
				throw new ExpressionException("can't cast struct to an array, key [" + e.getKey() + "] is not a number");
			}
			return toNativeArray(arr);
		}
		else if (o instanceof boolean[]) return ArrayUtil.toReferenceType((boolean[]) o);
		else if (o instanceof byte[]) return ArrayUtil.toReferenceType((byte[]) o);
		else if (o instanceof char[]) return ArrayUtil.toReferenceType((char[]) o);
		else if (o instanceof short[]) return ArrayUtil.toReferenceType((short[]) o);
		else if (o instanceof int[]) return ArrayUtil.toReferenceType((int[]) o);
		else if (o instanceof long[]) return ArrayUtil.toReferenceType((long[]) o);
		else if (o instanceof float[]) return ArrayUtil.toReferenceType((float[]) o);
		else if (o instanceof double[]) return ArrayUtil.toReferenceType((double[]) o);

		throw new CasterException(o, "Array");
	}

	/**
	 * cast an Object to an Array Object
	 * 
	 * @param o Object to cast
	 * @param defaultValue
	 * @return casted Array
	 */
	public static Array toArray(Object o, Array defaultValue) {
		if (o instanceof Array) return (Array) o;
		else if (o instanceof Object[]) {
			return new ArrayImpl((Object[]) o);
		}
		else if (o instanceof List) {
			return new ArrayImpl(((List) o).toArray());
		}
		else if (o instanceof Set) {
			return new ArrayImpl(((Set) o).toArray());
		}
		else if (o instanceof XMLStruct) {
			Array arr = new ArrayImpl();
			arr.appendEL(o);
			return arr;
		}
		else if (o instanceof ObjectWrap) {
			return toArray(((ObjectWrap) o).getEmbededObject(defaultValue), defaultValue);
			// if(io!=null)return toArray(io,defaultValue);
		}
		else if (o instanceof Struct) {
			return StructAsArray.toArray((Struct) o, defaultValue);

			/*
			 * Struct sct=(Struct) o; Array arr=new ArrayImpl();
			 * 
			 * Iterator<Entry<Key, Object>> it = sct.entryIterator(); Entry<Key, Object> e=null; try {
			 * while(it.hasNext()) { e=it.next(); arr.setEL(toIntValue(e.getKey().getString()),e.getValue()); }
			 * } catch (ExpressionException ee) { return defaultValue; } return arr;
			 */
		}
		else if (o instanceof boolean[]) return new ArrayImpl(ArrayUtil.toReferenceType((boolean[]) o));
		else if (o instanceof byte[]) return new ArrayImpl(ArrayUtil.toReferenceType((byte[]) o));
		else if (o instanceof char[]) return new ArrayImpl(ArrayUtil.toReferenceType((char[]) o));
		else if (o instanceof short[]) return new ArrayImpl(ArrayUtil.toReferenceType((short[]) o));
		else if (o instanceof int[]) return new ArrayImpl(ArrayUtil.toReferenceType((int[]) o));
		else if (o instanceof long[]) return new ArrayImpl(ArrayUtil.toReferenceType((long[]) o));
		else if (o instanceof float[]) return new ArrayImpl(ArrayUtil.toReferenceType((float[]) o));
		else if (o instanceof double[]) return new ArrayImpl(ArrayUtil.toReferenceType((double[]) o));

		return defaultValue;
	}

	/**
	 * cast an Object to a Map Object
	 * 
	 * @param o Object to cast
	 * @return casted Struct
	 * @throws PageException
	 */
	public static Map toMap(Object o) throws PageException {
		return toMap(o, false);
	}

	/**
	 * cast an Object to a Map Object
	 * 
	 * @param o Object to cast
	 * @param defaultValue
	 * @return casted Struct
	 */
	public static Map toMap(Object o, Map defaultValue) {
		return toMap(o, false, defaultValue);
	}

	/**
	 * cast an Object to a Map Object
	 * 
	 * @param o Object to cast
	 * @param duplicate
	 * @param defaultValue
	 * @return casted Struct
	 */
	public static Map toMap(Object o, boolean duplicate, Map defaultValue) {
		try {
			return toMap(o, duplicate);
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	/**
	 * cast an Object to a Map Object
	 * 
	 * @param o Object to cast
	 * @param duplicate
	 * @return casted Struct
	 * @throws PageException
	 */
	public static Map toMap(Object o, boolean duplicate) throws PageException {
		if (o instanceof Struct) {
			if (o instanceof Component) {
				try {
					Object tmp = Reflector.componentToClass(ThreadLocalPageContext.get(), (Component) o, Map.class);
					if (tmp instanceof Map) return (Map) tmp;
				}
				catch (PageException e) {}
			}
			if (duplicate) return (Map) Duplicator.duplicate(o, false);
			return ((Struct) o);
		}
		else if (o instanceof Map) {
			if (duplicate) return (Map) Duplicator.duplicate(o, false);
			return (Map) o;
		}
		else if (o instanceof Node) {
			if (duplicate) {
				return toMap(XMLCaster.toXMLStruct((Node) o, false), duplicate);
			}
			return (XMLCaster.toXMLStruct((Node) o, false));
		}
		else if (o instanceof ObjectWrap) {
			return toMap(((ObjectWrap) o).getEmbededObject(), duplicate);
		}
		throw new CasterException(o, "Map");
	}

	public static Struct toStruct(Query qry, int row) {
		Key[] names = qry.getColumnNames();
		Struct sct = new StructImpl();
		for (int i = 0; i < names.length; i++) {
			sct.setEL(names[i], qry.getAt(names[i], row, null));
		}
		return sct;
	}

	/**
	 * cast an Object to a Struct Object
	 * 
	 * @param o Object to cast
	 * @param defaultValue
	 * @return casted Struct
	 */

	public static Struct toStruct(Object o, Struct defaultValue, boolean caseSensitive) {
		if (o instanceof Struct) return (Struct) o;
		else if (o instanceof Map) {
			return MapAsStruct.toStruct((Map) o, caseSensitive);
		}
		else if (o instanceof Node) return XMLCaster.toXMLStruct((Node) o, false);
		else if (o instanceof ObjectWrap) {
			return toStruct(((ObjectWrap) o).getEmbededObject(defaultValue), defaultValue, caseSensitive);
		}
		return defaultValue;
	}

	/**
	 * cast an Object to a Struct Object
	 * 
	 * @param o Object to cast
	 * @return casted Struct
	 */
	public static Struct toStruct(Object o) throws PageException {
		return toStruct(o, true);
	}

	public static Struct toStruct(Object o, Struct defaultValue) {
		return toStruct(o, defaultValue, true);
	}

	public static Struct toStruct(Object o, boolean caseSensitive) throws PageException {
		if (o instanceof Struct) return (Struct) o;
		else if (o instanceof Map) return MapAsStruct.toStruct((Map) o, caseSensitive);// _toStruct((Map)o,caseSensitive);
		else if (o instanceof Node) return XMLCaster.toXMLStruct((Node) o, false);
		else if (o instanceof ObjectWrap) {
			if (o instanceof JavaObject) {
				Struct sct = toStruct(((JavaObject) o).getEmbededObject(null), null, caseSensitive);
				if (sct != null) return sct;

				JavaObject jo = (JavaObject) o;
				return new ObjectStruct(jo);
			}
			return toStruct(((ObjectWrap) o).getEmbededObject(), caseSensitive);
		}
		if (Decision.isSimpleValue(o) || Decision.isArray(o)) throw new CasterException(o, "Struct");
		if (o instanceof Collection) return new CollectionStruct((Collection) o);

		if (o == null) throw new CasterException("null can not be casted to a Struct");

		return new ObjectStruct(o);
	}

	/*
	 * private static Struct _toStruct(Map map) { Struct sct = new StructImpl(); Iterator
	 * it=map.keySet().iterator(); while(it.hasNext()) { Object key=it.next();
	 * sct.set(StringUtil.toLowerCase(Caster.toString(key)),map.get(key)); } return sct; }
	 */

	/**
	 * cast an Object to a Binary
	 * 
	 * @param o Object to cast
	 * @return casted Binary
	 * @throws PageException
	 */
	public static byte[] toBinary(Object o) throws PageException {
		if (o instanceof byte[]) return (byte[]) o;
		else if (o instanceof ObjectWrap) return toBinary(((ObjectWrap) o).getEmbededObject(""));

		else if (o instanceof InputStream) {
			ByteArrayOutputStream barr = new ByteArrayOutputStream();
			try {
				IOUtil.copy((InputStream) o, barr, false, true);
			}
			catch (IOException e) {
				throw ExpressionException.newInstance(e);
			}
			return barr.toByteArray();
		}
		// depending on extension Image
		else if (o != null && o.getClass().getName().equals("org.lucee.extension.image.Image")) {
			return ImageUtil.getImageBytes(o, null);
		}
		else if (o instanceof BufferedImage) {
			return ImageUtil.getImageBytes((BufferedImage) o);
		}
		else if (o instanceof ByteArrayOutputStream) {
			return ((ByteArrayOutputStream) o).toByteArray();
		}
		else if (o instanceof Blob) {
			InputStream is = null;
			try {
				is = ((Blob) o).getBinaryStream();
				return IOUtil.toBytes(is);
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
			finally {
				try {
					IOUtil.close(is);
				}
				catch (IOException e) {
					throw Caster.toPageException(e);
				}
			}
		}
		try {
			return Base64Encoder.decode(toString(o));
		}
		catch (PageException e) {
			throw new CasterException(o, "binary");
		}
	}

	/**
	 * cast an Object to a Binary
	 * 
	 * @param o Object to cast
	 * @param defaultValue
	 * @return casted Binary
	 */
	public static byte[] toBinary(Object o, byte[] defaultValue) {
		try {
			return toBinary(o);
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	public static Object toCreditCard(Object o) throws PageException {
		return ValidateCreditCard.toCreditcard(toString(o));
	}

	public static Object toCreditCard(Object o, String defaultValue) {
		// print.out("enter");
		String str = toString(o, null);
		if (str == null) return defaultValue;
		// print.out("enter:"+str+":"+ValidateCreditCard.toCreditcard(str,defaultValue));

		return ValidateCreditCard.toCreditcard(str, defaultValue);
	}

	public static String toBase64(Object o) throws PageException {
		String str = toBase64(o, "UTF-8", null);
		if (str == null) throw new CasterException(o, "base 64");
		return str;
	}

	/**
	 * cast an Object to a Base64 value
	 * 
	 * @param o Object to cast
	 * @return to Base64 String
	 * @throws PageException
	 */
	public static String toBase64(Object o, String charset) throws PageException {
		String str = toBase64(o, charset, null);
		if (str == null) throw new CasterException(o, "base 64");
		return str;
	}

	/**
	 * cast an Object to a Base64 value
	 * 
	 * @param o Object to cast
	 * @param defaultValue
	 * @return to Base64 String
	 */
	public static String toBase64(Object o, String charset, String defaultValue) {
		if (o instanceof byte[]) return toB64((byte[]) o, defaultValue);
		else if (o instanceof String) return toB64((String) o, charset, defaultValue);
		else if (o instanceof Number) return toB64(toString(((Number) o)), charset, defaultValue);
		else if (o instanceof ObjectWrap) {
			return toBase64(((ObjectWrap) o).getEmbededObject(defaultValue), charset, defaultValue);
		}
		else if (o == null) {
			return toBase64("", charset, defaultValue);
		}
		else {
			byte[] b = toBinary(o, null);
			if (b != null) return toB64(b, defaultValue);
			else {
				String str = toString(o, null);
				if (str != null) return toBase64(str, charset, defaultValue);
				else return defaultValue;
			}
		}
	}

	public static String toB64(String str, String charset) throws UnsupportedEncodingException {
		return toB64(str.getBytes(charset));
	}

	public static String toB64(byte[] b) {
		return Base64Coder.encode(b);
	}

	public static String toB64(String str, String charset, String defaultValue) {
		if (StringUtil.isEmpty(charset, true)) charset = "UTF-8";
		try {
			return Base64Coder.encodeFromString(str, charset);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}
	}

	public static String toB64(byte[] b, String defaultValue) {
		try {
			return Base64Coder.encode(b);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * cast a boolean to a DateTime Object
	 * 
	 * @param b boolean to cast
	 * @param tz
	 * @return casted DateTime Object
	 */
	public static DateTime toDate(boolean b, TimeZone tz) {
		return DateCaster.toDateSimple(b, tz);
	}

	/**
	 * cast a char to a DateTime Object
	 * 
	 * @param c char to cast
	 * @param tz
	 * @return casted DateTime Object
	 */
	public static DateTime toDate(char c, TimeZone tz) {
		return DateCaster.toDateSimple(c, tz);
	}

	/**
	 * cast a double to a DateTime Object
	 * 
	 * @param d double to cast
	 * @param tz
	 * @return casted DateTime Object
	 */
	public static DateTime toDate(double d, TimeZone tz) {
		return DateCaster.toDateSimple(d, tz);
	}

	/**
	 * cast an Object to a DateTime Object
	 * 
	 * @param o Object to cast
	 * @param tz
	 * @return casted DateTime Object
	 * @throws PageException
	 */
	public static DateTime toDate(Object o, TimeZone tz) throws PageException {
		return DateCaster.toDateAdvanced(o, tz);
	}

	/**
	 * cast an Object to a DateTime Object
	 * 
	 * @param str String to cast
	 * @param tz
	 * @return casted DateTime Object
	 * @throws PageException
	 */
	public static DateTime toDate(String str, TimeZone tz) throws PageException {
		return DateCaster.toDateAdvanced(str, tz);
	}

	public static DateTime toDate(Object o) throws PageException {
		return DateCaster.toDateAdvanced(o, DateCaster.CONVERTING_TYPE_OFFSET, ThreadLocalPageContext.getTimeZone());
	}

	/**
	 * cast an Object to a DateTime Object
	 * 
	 * @param o Object to cast
	 * @param alsoNumbers define if also numbers will casted to a datetime value
	 * @param tz
	 * @return casted DateTime Object
	 * @throws PageException
	 */
	public static DateTime toDate(Object o, boolean alsoNumbers, TimeZone tz) throws PageException {
		return DateCaster.toDateAdvanced(o, alsoNumbers ? DateCaster.CONVERTING_TYPE_OFFSET : DateCaster.CONVERTING_TYPE_NONE, tz);
	}

	/**
	 * cast an Object to a DateTime Object
	 * 
	 * @param o Object to cast
	 * @param alsoNumbers define if also numbers will casted to a datetime value
	 * @param tz
	 * @param defaultValue
	 * @return casted DateTime Object
	 */
	public static DateTime toDate(Object o, boolean alsoNumbers, TimeZone tz, DateTime defaultValue) {
		return DateCaster.toDateAdvanced(o, alsoNumbers ? DateCaster.CONVERTING_TYPE_OFFSET : DateCaster.CONVERTING_TYPE_NONE, tz, defaultValue);
	}

	/**
	 * cast an Object to a DateTime Object
	 * 
	 * @param str String to cast
	 * @param alsoNumbers define if also numbers will casted to a datetime value
	 * @param tz
	 * @param defaultValue
	 * @return casted DateTime Object
	 */
	public static DateTime toDate(String str, boolean alsoNumbers, TimeZone tz, DateTime defaultValue) {
		return DateCaster.toDateAdvanced(str, alsoNumbers ? DateCaster.CONVERTING_TYPE_OFFSET : DateCaster.CONVERTING_TYPE_NONE, tz, defaultValue);
	}

	/**
	 * cast an Object to a DateTime Object
	 * 
	 * @param o Object to cast
	 * @param tz
	 * @return casted DateTime Object
	 * @throws PageException
	 */
	public static DateTime toDateTime(Object o, TimeZone tz) throws PageException {
		return DateCaster.toDateAdvanced(o, tz);
	}

	/**
	 * cast an Object to a DateTime Object (alias for toDateTime)
	 * 
	 * @param o Object to cast
	 * @param tz
	 * @return casted DateTime Object
	 * @throws PageException
	 */
	public static DateTime toDatetime(Object o, TimeZone tz) throws PageException {
		return DateCaster.toDateAdvanced(o, tz);
	}

	/**
	 * cast an Object to a Query Object
	 * 
	 * @param o Object to cast
	 * @return casted Query Object
	 * @throws PageException
	 */
	public static Query toQuery(Object o) throws PageException {
		if (o instanceof Query) return (Query) o;
		if (o instanceof ObjectWrap) {
			return toQuery(((ObjectWrap) o).getEmbededObject());
		}
		if (o instanceof ResultSet) return new QueryImpl((ResultSet) o, "query", ThreadLocalPageContext.getTimeZone());
		if (o instanceof Component) {
			Member member = ((Component) o).getMember(Component.ACCESS_PRIVATE, KeyConstants.__toQuery, false, false);
			if (member instanceof UDF) {
				UDF udf = (UDF) member;
				if (udf.getReturnType() == CFTypes.TYPE_QUERY && udf.getFunctionArguments().length == 0) {
					return Caster.toQuery(((Component) o).call(ThreadLocalPageContext.get(), KeyConstants.__toQuery, new Object[] {}));
				}
			}
		}

		throw new CasterException(o, "query");
	}

	/**
	 * converts an Object to a QueryColumn, if possible
	 * 
	 * @param o
	 * @return
	 * @throws PageException
	 */
	public static QueryColumn toQueryColumn(Object o) throws PageException {
		if (o instanceof QueryColumn) return (QueryColumn) o;
		throw new CasterException(o, "querycolumn");
	}

	public static QueryColumn toQueryColumn(Object o, QueryColumn defaultValue) {
		if (o instanceof QueryColumn) return (QueryColumn) o;
		return defaultValue;
	}

	/**
	 * converts an Object to a QueryColumn, if possible, also variable declarations are allowed. this
	 * method is used within the generated bytecode
	 * 
	 * @param o
	 * @return
	 * @throws PageException
	 * @info used in bytecode generation
	 */
	public static QueryColumn toQueryColumn(Object o, PageContext pc) throws PageException {
		if (o instanceof QueryColumn) return (QueryColumn) o;
		if (o instanceof String) {
			o = VariableInterpreter.getVariableAsCollection(pc, (String) o);
			if (o instanceof QueryColumn) return (QueryColumn) o;
		}

		throw new CasterException(o, "querycolumn");
	}

	/**
	 * cast an Object to a Query Object
	 * 
	 * @param o Object to cast
	 * @param defaultValue
	 * @return casted Query Object
	 */
	public static Query toQuery(Object o, Query defaultValue) {
		if (o instanceof Query) return (Query) o;
		else if (o instanceof ObjectWrap) {
			return toQuery(((ObjectWrap) o).getEmbededObject(defaultValue), defaultValue);
		}
		return defaultValue;
	}

	/**
	 * cast an Object to a Query Object
	 * 
	 * @param o Object to cast
	 * @param duplicate duplicate the object or not
	 * @param defaultValue
	 * @return casted Query Object
	 */
	public static Query toQuery(Object o, boolean duplicate, Query defaultValue) {
		try {
			return toQuery(o, duplicate);
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	/**
	 * cast an Object to a Query Object
	 * 
	 * @param o Object to cast
	 * @param duplicate duplicate the object or not
	 * @return casted Query Object
	 * @throws PageException
	 */
	public static Query toQuery(Object o, boolean duplicate) throws PageException {

		if (o instanceof Query) {
			if (duplicate) {
				Query src = (Query) o;
				Query trg = new QueryImpl(src.getColumnNames(), src.getRowCount(), "query");

				Collection.Key[] keys = src.getColumnNames();
				QueryColumn[] columnsSrc = new QueryColumn[keys.length];
				for (int i = 0; i < columnsSrc.length; i++) {
					columnsSrc[i] = src.getColumn(keys[i]);
				}

				keys = trg.getColumnNames();
				QueryColumn[] columnsTrg = new QueryColumn[keys.length];
				for (int i = 0; i < columnsTrg.length; i++) {
					columnsTrg[i] = trg.getColumn(keys[i]);
				}

				int i;
				for (int row = trg.getRecordcount(); row > 0; row--) {
					for (i = 0; i < columnsTrg.length; i++) {
						columnsTrg[i].set(row, columnsSrc[i].get(row, null));
					}
				}
				return trg;
			}
			return (Query) o;
		}
		else if (o instanceof ObjectWrap) {
			return toQuery(((ObjectWrap) o).getEmbededObject(), duplicate);
		}
		throw new CasterException(o, "query");
	}

	/**
	 * cast an Object to a UUID
	 * 
	 * @param o Object to cast
	 * @return casted Query Object
	 * @throws PageException
	 */
	public static Object toUUId(Object o) throws PageException {
		String str = toString(o);
		if (!Decision.isUUId(str)) throw new ExpressionException("can't cast [" + str + "] to uuid value");
		return str;
	}

	/**
	 * cast an Object to a UUID
	 * 
	 * @param o Object to cast
	 * @param defaultValue
	 * @return casted Query Object
	 */
	public static Object toUUId(Object o, Object defaultValue) {
		String str = toString(o, null);
		if (str == null) return defaultValue;

		if (!Decision.isUUId(str)) return defaultValue;
		return str;
	}

	/**
	 * cast an Object to a GUID
	 * 
	 * @param o Object to cast
	 * @return casted Query Object
	 * @throws PageException
	 */
	public static Object toGUId(Object o) throws PageException {
		String str = toString(o);
		if (!Decision.isGUId(str)) throw new ExpressionException("can't cast [" + str + "] to guid value");
		return str;
	}

	/**
	 * cast an Object to a GUID
	 * 
	 * @param o Object to cast
	 * @param defaultValue
	 * @return casted Query Object
	 */
	public static Object toGUId(Object o, Object defaultValue) {
		String str = toString(o, null);
		if (str == null) return defaultValue;

		if (!Decision.isGUId(str)) return defaultValue;
		return str;
	}

	/**
	 * cast an Object to a Variable Name
	 * 
	 * @param o Object to cast
	 * @return casted Variable Name
	 * @throws PageException
	 */
	public static String toVariableName(Object o) throws PageException {
		String str = toString(o);
		if (!Decision.isVariableName(str)) throw new ExpressionException("can't cast [" + str + "] to variable name value");
		return str;
	}

	/**
	 * cast an Object to a Variable Name
	 * 
	 * @param o Object to cast
	 * @param defaultValue
	 * @return casted Variable Name
	 */
	public static String toVariableName(Object o, String defaultValue) {
		String str = toString(o, null);

		if (str == null || !Decision.isVariableName(str)) return defaultValue;
		return str;
	}

	/**
	 * cast an Object to a TimeSpan Object
	 * 
	 * @param o Object to cast
	 * @return casted TimeSpan Object
	 * @throws PageException
	 */
	public static TimeSpan toTimeSpan(Object o) throws PageException {
		return toTimespan(o);
	}

	/**
	 * cast an Object to a TimeSpan Object (alias for toTimeSpan)
	 * 
	 * @param o Object to cast
	 * @param defaultValue
	 * @return casted TimeSpan Object
	 */
	public static TimeSpan toTimespan(Object o, TimeSpan defaultValue) {
		if (o instanceof TimeSpan) return (TimeSpan) o;
		else if (o instanceof String) {
			String[] arr = o.toString().split(",");
			if (arr.length == 4) {
				int[] values = new int[4];
				try {
					for (int i = 0; i < arr.length; i++) {
						values[i] = toIntValue(arr[i]);
					}
					return new TimeSpanImpl(values[0], values[1], values[2], values[3]);
				}
				catch (ExpressionException e) {}
			}
		}
		else if (o instanceof ObjectWrap) {
			Object embeded = ((ObjectWrap) o).getEmbededObject(DEFAULT);
			if (embeded == DEFAULT) return defaultValue;
			return toTimespan(embeded, defaultValue);
		}

		double dbl = toDoubleValue(o, true, Double.NaN);
		if (!Double.isNaN(dbl)) return TimeSpanImpl.fromDays(dbl);

		return defaultValue;
	}

	/**
	 * cast an Object to a TimeSpan Object (alias for toTimeSpan)
	 * 
	 * @param o Object to cast
	 * @return casted TimeSpan Object
	 * @throws PageException
	 */
	public static TimeSpan toTimespan(Object o) throws PageException {
		TimeSpan ts = toTimespan(o, null);
		if (ts != null) return ts;

		throw new CasterException(o, "timespan");
	}

	/**
	 * cast a Throwable Object to a PageException Object
	 * 
	 * @param t Throwable to cast
	 * @return casted PageException Object
	 */
	public static PageException toPageException(Throwable t) {
		return toPageException(t, true);
	}

	public static PageRuntimeException toPageRuntimeException(Throwable t) {
		if (t instanceof PageRuntimeException) return (PageRuntimeException) t;
		return new PageRuntimeException(toPageException(t, true));
	}

	public static PageException toPageException(Throwable t, boolean rethrowIfNecessary) {
		if (t instanceof PageException) {
			return (PageException) t;
		}
		if (t instanceof PageExceptionBox) {
			return ((PageExceptionBox) t).getPageException();
		}
		if (t instanceof InvocationTargetException) {
			return toPageException(((InvocationTargetException) t).getTargetException());
		}
		if (t instanceof ExceptionInInitializerError) {
			return toPageException(((ExceptionInInitializerError) t).getCause());
		}
		if (t instanceof ExecutionException) {
			return toPageException(((ExecutionException) t).getCause());
		}
		if (t instanceof InterruptedException) {
			PageContext pc = ThreadLocalPageContext.get();
			if (pc instanceof PageContextImpl) {
				PageContextImpl pci = (PageContextImpl) pc;
				StackTraceElement[] tst = pci.getTimeoutStackTrace();
				if (tst != null) {
					return new RequestTimeoutException(pc, tst);
				}
			}

		}
		if (t instanceof OutOfMemoryError) {
			ThreadLocalPageContext.getConfig().checkPermGenSpace(true);
		}
		// Throwable cause = t.getCause();
		// if(cause!=null && cause!=t) return toPageException(cause);
		return NativeException.newInstance(t, rethrowIfNecessary);
	}

	/**
	 * return the type name of an object (string, boolean, int aso.), type is not same like class name
	 * 
	 * @param o Object to get type from
	 * @return type of the object
	 */
	public static String toTypeName(Object o) {
		if (o == null) return "null";
		else if (o instanceof String) return "string";
		else if (o instanceof Boolean) return "boolean";
		else if (o instanceof Number) return "int";
		else if (o instanceof Array) return "array";
		else if (o instanceof Component) return "component";
		else if (o instanceof Struct) return "struct";
		else if (o instanceof Query) return "query";
		else if (o instanceof DateTime) return "datetime";
		else if (o instanceof byte[]) return "binary";
		else if (o instanceof ObjectWrap) {
			return toTypeName(((ObjectWrap) o).getEmbededObject(null));
		}

		Class clazz = o.getClass();
		String className = clazz.getName();
		if (className.startsWith("java.lang.")) {
			return className.substring(10);
		}
		return toClassName(clazz);
	}

	public static String toTypeName(Class clazz) {
		if (Reflector.isInstaneOf(clazz, String.class, false)) return "string";
		if (Reflector.isInstaneOf(clazz, Boolean.class, false)) return "boolean";
		if (Reflector.isInstaneOf(clazz, Number.class, false)) return "numeric";
		if (Reflector.isInstaneOf(clazz, Array.class, false)) return "array";
		if (Reflector.isInstaneOf(clazz, Struct.class, false)) return "struct";
		if (Reflector.isInstaneOf(clazz, Query.class, false)) return "query";
		if (Reflector.isInstaneOf(clazz, DateTime.class, false)) return "datetime";
		if (Reflector.isInstaneOf(clazz, byte[].class, false)) return "binary";

		String className = clazz.getName();
		if (className.startsWith("java.lang.")) {
			return className.substring(10);
		}
		return toClassName(clazz);
	}

	public static String toClassName(Object o) {
		if (o == null) return "null";
		if (o instanceof ObjectWrap) {
			try {
				return toClassName(((ObjectWrap) o).getEmbededObject());
			}
			catch (PageException e) {}
		}
		return toClassName(o.getClass());
	}

	public static String toClassName(Class clazz) {
		if (clazz.isArray()) {
			return toClassName(clazz.getComponentType()) + "[]";
		}
		return clazz.getName();
	}

	public static Class cfTypeToClass(String type) throws PageException {
		// TODO weitere typen siehe bytecode.cast.Cast

		type = type.trim();
		String lcType = StringUtil.toLowerCase(type);

		if (lcType.length() > 2) {
			char first = lcType.charAt(0);
			switch (first) {
			case 'a':
				if (lcType.equals("any")) {
					return Object.class;
				}
				else if (lcType.equals("array")) {
					return Array.class;
				}
				break;
			case 'b':
				if (lcType.equals("boolean") || lcType.equals("bool")) {
					return Boolean.class;
				}
				else if (lcType.equals("binary")) {
					return byte[].class;
				}
				else if (lcType.equals("base64")) {
					return String.class;
				}
				else if (lcType.equals("byte")) {
					return Byte.class;
				}
				break;
			case 'c':
				if (lcType.equals("creditcard")) {
					return String.class;
				}
				else if (lcType.equals("component") || lcType.equals("class")) {
					return Component.class;
				}
				break;
			case 'd':
				if (lcType.equals("date")) {
					return Date.class;
				}
				else if (lcType.equals("datetime")) {
					return Date.class;
				}
				break;
			case 'g':
				if (lcType.equals("guid")) {
					return Object.class;
				}
				break;
			case 'n':
				if (lcType.equals("numeric")) {
					return Double.class;
				}
				else if (lcType.equals("number")) {
					return Double.class;
				}
				else if (lcType.equals("node")) {
					return Node.class;
				}
				break;
			case 'o':
				if (lcType.equals("object")) {
					return Object.class;
				}
				break;
			case 'q':
				if (lcType.equals("query")) {
					return Query.class;
				}
				break;
			case 's':
				if (lcType.equals("string")) {
					return String.class;
				}
				else if (lcType.equals("struct")) {
					return Struct.class;
				}
				break;
			case 't':
				if (lcType.equals("timespan")) {
					return TimeSpan.class;
				}
				break;
			case 'u':
				if (lcType.equals("uuid")) {
					return Object.class;
				}
				break;
			case 'v':
				if (lcType.equals("variablename")) {
					return Object.class;
				}
				if (lcType.equals("void")) {
					return Object.class;
				}
				break;
			case 'x':
				if (lcType.equals("xml")) {
					return Node.class;
				}
				break;
			}
		}
		// array
		if (type.endsWith("[]")) {
			Class clazz = cfTypeToClass(type.substring(0, type.length() - 2));
			clazz = ClassUtil.toArrayClass(clazz);
			return clazz;
		}
		// check for argument
		Class<?> clazz;
		try {
			clazz = otherTypeToClass(type);
		}
		catch (ClassException e) {
			throw Caster.toPageException(e);
		}
		return clazz;
	}

	private static Class<?> otherTypeToClass(String type) throws PageException, ClassException {
		PageContext pc = ThreadLocalPageContext.get();
		PageException pe = null;
		// try to load as cfc
		if (pc != null) {
			try {
				Component c = pc.loadComponent(type);
				return ComponentUtil.getComponentPropertiesClass(pc, c);
			}
			catch (PageException e) {
				pe = e;
			}
		}
		// try to load as class
		try {
			return ClassUtil.loadClass(type);
		}
		catch (ClassException ce) {
			if (pe != null) throw pe;
			throw ce;
		}
	}

	public static Object castTo(String type, Object o) throws PageException {
		return castTo(ThreadLocalPageContext.get(), type, o, false);
	}

	/**
	 * cast a value to a value defined by type argument
	 * 
	 * @param pc
	 * @param type type of the returning Value
	 * @param o Object to cast
	 * @return casted Value
	 * @throws PageException
	 */
	public static Object castTo(PageContext pc, String type, Object o, boolean alsoPattern) throws PageException {
		type = type.trim();
		String lctype = StringUtil.toLowerCase(type);
		if (lctype.length() > 2) {
			char first = lctype.charAt(0);
			switch (first) {
			case 'a':
				if (lctype.equals("any")) {
					return o;
				}
				else if (lctype.equals("array")) {
					return toArray(o);
				}
				break;
			case 'b':
				if (lctype.equals("boolean") || lctype.equals("bool")) {
					return toBoolean(o);
				}
				else if (lctype.equals("binary")) {
					return toBinary(o);
				}
				else if (lctype.equals("byte[]")) {
					return toBinary(o);
				}
				else if (lctype.equals("base64")) {
					return toBase64(o, null);
				}
				else if (lctype.equals("bigdecimal") || lctype.equals("big_decimal")) {
					return toBigDecimal(o);
				}
				else if (lctype.equals("biginteger") || lctype.equals("big_integer")) {
					return toBigInteger(o);
				}
				break;
			case 'c':
				if (alsoPattern && lctype.equals("creditcard")) {
					return toCreditCard(o);
				}
				break;
			case 'd':
				if (lctype.equals("date")) {
					return DateCaster.toDateAdvanced(o, pc.getTimeZone());
				}
				else if (lctype.equals("datetime")) {
					return DateCaster.toDateAdvanced(o, pc.getTimeZone());
				}
				else if (lctype.equals("double")) {
					return toDouble(o);
				}
				else if (lctype.equals("decimal")) {
					return toDecimal(o);
				}
				break;
			case 'e':
				if (lctype.equals("eurodate")) {
					return DateCaster.toEuroDate(o, pc.getTimeZone());
				}
				else if (alsoPattern && lctype.equals("email")) {
					return toEmail(o);
				}
				break;
			case 'f':
				if (lctype.equals("float")) {
					return toDouble(o);
				}
				else if (lctype.equals("function")) {
					return toFunction(o);
				}
				break;
			case 'g':
				if (lctype.equals("guid")) {
					return toGUId(o);
				}
				break;
			case 'i':
				if (lctype.equals("integer") || lctype.equals("int")) {
					return toInteger(o);
				}
				break;
			case 'l':
				if (lctype.equals("long")) {
					return toLong(o);
				}
				break;
			case 'n':
				if (lctype.equals("numeric")) {
					return toDouble(o);
				}
				else if (lctype.equals("number")) {
					return toDouble(o);
				}
				else if (lctype.equals("node")) {
					return toXML(o);
				}
				break;
			case 'o':
				if (lctype.equals("object")) {
					return o;
				}
				else if (lctype.equals("other")) {
					return o;
				}
				break;
			case 'p':
				if (alsoPattern && lctype.equals("phone")) {
					return toPhone(o);
				}
				break;
			case 'q':
				if (lctype.equals("query")) {
					return toQuery(o);
				}
				break;
			case 's':
				if (lctype.equals("string")) {
					return toString(o);
				}
				else if (lctype.equals("struct")) {
					return toStruct(o);
				}
				else if (lctype.equals("short")) {
					return toShort(o);
				}
				else if (alsoPattern && (lctype.equals("ssn") || lctype.equals("social_security_number"))) {
					return toSSN(o);
				}
				break;
			case 't':
				if (lctype.equals("timespan")) {
					return toTimespan(o);
				}
				if (lctype.equals("time")) {
					return DateCaster.toDateAdvanced(o, pc.getTimeZone());
				}
				if (alsoPattern && lctype.equals("telephone")) {
					return toPhone(o);
				}
				break;
			case 'u':
				if (lctype.equals("uuid")) {
					return toUUId(o);
				}
				if (alsoPattern && lctype.equals("url")) {
					return toURL(o);
				}
				if (lctype.equals("usdate")) {
					return DateCaster.toUSDate(o, pc.getTimeZone());
					// return DateCaster.toDate(o,pc.getTimeZone());
				}
				break;
			case 'v':
				if (lctype.equals("variablename")) {
					return toVariableName(o);
				}
				else if (lctype.equals("void")) {
					return toVoid(o);
				}
				else if (lctype.equals("variable_name")) {
					return toVariableName(o);
				}
				else if (lctype.equals("variable-name")) {
					return toVariableName(o);
				}
				break;
			case 'x':
				if (lctype.equals("xml")) {
					return toXML(o);
				}
			case 'z':
				if (alsoPattern && (lctype.equals("zip") || lctype.equals("zipcode"))) {
					return toZip(o);
				}
				break;
			}
		}

		// <type>[]
		if (lctype.endsWith("[]")) {
			String componentType = lctype.substring(0, lctype.length() - 2);
			Object[] src = toNativeArray(o);
			Array trg = new ArrayImpl();
			for (int i = 0; i < src.length; i++) {
				if (src[i] == null) {
					continue;
				}
				trg.setE(i + 1, castTo(pc, componentType, src[i], alsoPattern));
			}
			return trg;
		}

		return _castTo(pc, type, o);
	}

	public static String toZip(Object o) throws PageException {
		String str = toString(o);
		if (Decision.isZipCode(str)) return str;
		throw new ExpressionException("can't cast value [" + str + "] to a zip code");
	}

	public static String toZip(Object o, String defaultValue) {
		String str = toString(o, null);
		if (str == null) return defaultValue;
		if (Decision.isZipCode(str)) return str;
		return defaultValue;
	}

	public static String toURL(Object o) throws PageException {
		String str = toString(o);
		if (Decision.isURL(str)) return str;

		try {
			return HTTPUtil.toURL(str, HTTPUtil.ENCODED_AUTO).toExternalForm();
		}
		catch (MalformedURLException e) {
			throw new ExpressionException("can't cast value [" + str + "] to a URL", e.getMessage());
		}
	}

	public static String toURL(Object o, String defaultValue) {
		String str = toString(o, null);
		if (str == null) return defaultValue;
		if (Decision.isURL(str)) return str;
		try {
			return HTTPUtil.toURL(str, HTTPUtil.ENCODED_AUTO).toExternalForm();
		}
		catch (MalformedURLException e) {
			return defaultValue;
		}
	}

	public static String toPhone(Object o) throws PageException {
		String str = toString(o);
		if (Decision.isPhone(str)) return str;
		throw new ExpressionException("can't cast value [" + str + "] to a telephone number");
	}

	public static String toPhone(Object o, String defaultValue) {
		String str = toString(o, null);
		if (str == null) return defaultValue;
		if (Decision.isPhone(str)) return str;
		return defaultValue;
	}

	public static String toSSN(Object o) throws PageException {
		String str = toString(o);
		if (Decision.isSSN(str)) return str;
		throw new ExpressionException("can't cast value [" + str + "] to a U.S. social security number");
	}

	public static String toSSN(Object o, String defaultValue) {
		String str = toString(o, null);
		if (str == null) return defaultValue;
		if (Decision.isSSN(str)) return str;
		return defaultValue;
	}

	public static String toEmail(Object o) throws PageException {
		String str = toString(o);
		if (Decision.isEmail(str)) return str;
		throw new ExpressionException("can't cast value [" + str + "] to an E-Mail Address");
	}

	public static String toEmail(Object o, String defaultValue) {
		String str = toString(o, null);
		if (str == null) return defaultValue;
		if (Decision.isEmail(str)) return str;
		return defaultValue;
	}

	public static Object castTo(PageContext pc, short type, String strType, Object o, Object defaultValue) {
		// TODO weitere typen siehe bytecode.cast.Cast
		Object res = null;
		if (type == CFTypes.TYPE_ANY) return o;
		else if (type == CFTypes.TYPE_ARRAY) res = toArray(o, null);
		else if (type == CFTypes.TYPE_BOOLEAN) res = toBoolean(o, null);
		else if (type == CFTypes.TYPE_BINARY) res = toBinary(o, null);
		else if (type == CFTypes.TYPE_DATETIME) res = DateCaster.toDateAdvanced(o, pc.getTimeZone(), null);
		else if (type == CFTypes.TYPE_NUMERIC) res = toDouble(o, null);
		else if (type == CFTypes.TYPE_QUERY) res = toQuery(o, null);
		else if (type == CFTypes.TYPE_QUERY_COLUMN) res = toQueryColumn(o, (QueryColumn) null);
		else if (type == CFTypes.TYPE_STRING) res = toString(o, null);
		else if (type == CFTypes.TYPE_STRUCT) res = toStruct(o, null);
		else if (type == CFTypes.TYPE_TIMESPAN) res = toTimespan(o, null);
		else if (type == CFTypes.TYPE_UUID) res = toUUId(o, null);
		else if (type == CFTypes.TYPE_GUID) res = toGUId(o, null);
		else if (type == CFTypes.TYPE_VARIABLE_NAME) res = toVariableName(o, null);
		else if (type == CFTypes.TYPE_VOID) res = toVoid(o, null);
		else if (type == CFTypes.TYPE_XML) res = toXML(o, null);
		else if (type == CFTypes.TYPE_FUNCTION) res = toFunction(o, null);
		// ext.img else if(type==CFTypes.TYPE_IMAGE) res= ImageUtil.toImage(pc,o,true,null);
		else if (type == CFTypes.TYPE_LOCALE) res = toLocale(o, null);
		else if (type == CFTypes.TYPE_TIMEZONE) res = toTimeZone(o, null);

		if (res != null) return res;

		try {
			return _castTo(pc, strType, o);
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	/**
	 * cast a value to a value defined by type argument
	 * 
	 * @param pc
	 * @param type type of the returning Value
	 * @param strType type as String
	 * @param o Object to cast
	 * @return casted Value
	 * @throws PageException
	 */
	public static Object castTo(PageContext pc, short type, String strType, Object o) throws PageException {
		// TODO weitere typen siehe bytecode.cast.Cast
		if (type == CFTypes.TYPE_ANY) return o;
		else if (type == CFTypes.TYPE_ARRAY) return toArray(o);
		else if (type == CFTypes.TYPE_BOOLEAN) return toBoolean(o);
		else if (type == CFTypes.TYPE_BINARY) return toBinary(o);
		else if (type == CFTypes.TYPE_DATETIME) return DateCaster.toDateAdvanced(o, pc.getTimeZone());
		else if (type == CFTypes.TYPE_NUMERIC) return toDouble(o);
		else if (type == CFTypes.TYPE_QUERY) return toQuery(o);
		else if (type == CFTypes.TYPE_QUERY_COLUMN) return toQueryColumn(o);
		else if (type == CFTypes.TYPE_STRING) return toString(o);
		else if (type == CFTypes.TYPE_STRUCT) return toStruct(o);
		else if (type == CFTypes.TYPE_TIMESPAN) return toTimespan(o);
		else if (type == CFTypes.TYPE_UUID) return toUUId(o);
		else if (type == CFTypes.TYPE_GUID) return toGUId(o);
		else if (type == CFTypes.TYPE_VARIABLE_NAME) return toVariableName(o);
		else if (type == CFTypes.TYPE_VOID) return toVoid(o);
		else if (type == CFTypes.TYPE_XML) return toXML(o);
		else if (type == CFTypes.TYPE_FUNCTION) return toFunction(o);
		// ext.img else if(type==CFTypes.TYPE_IMAGE) return ImageUtil.toImage(pc,o,true);
		else if (type == CFTypes.TYPE_LOCALE) return toLocale(o);
		else if (type == CFTypes.TYPE_TIMEZONE) return toTimeZone(o);

		return _castTo(pc, strType, o);
	}

	private static Object _castTo(PageContext pc, String strType, Object o) throws PageException {

		if (o instanceof Component) {
			Component comp = ((Component) o);
			if (comp.instanceOf(strType)) return o;

			try {
				Class<?> trgClass = ClassUtil.loadClass(strType);
				if (trgClass.isInterface()) {
					return Reflector.componentToClass(pc, comp, trgClass);
				}
			}
			catch (ClassException ce) {
				throw Caster.toPageException(ce);
			}

			throw new ExpressionException("can't cast Component of Type [" + comp.getAbsName() + "] to [" + strType + "]");
		}
		if (o instanceof UDF) {
			try {
				Class<?> trgClass = ClassUtil.loadClass(strType);
				if (trgClass.isInterface()) {
					return Reflector.udfToClass(pc, (UDF) o, trgClass);
				}
			}
			catch (ClassException ce) {
				throw Caster.toPageException(ce);
			}
		}

		if (o instanceof Pojo) {
			Component cfc = toComponent(pc, ((Pojo) o), strType, null);
			if (cfc != null) return cfc;
			throw new ExpressionException("can't cast Pojo of Type [" + o.getClass().getName() + "] to [" + strType + "]");
		}

		if (strType.endsWith("[]") && Decision.isArray(o)) {
			String _strType = strType.substring(0, strType.length() - 2);
			short _type = CFTypes.toShort(_strType, false, (short) -1);
			Array arr = Caster.toArray(o, null);
			if (arr != null) {

				// convert the values
				Iterator<Entry<Key, Object>> it = arr.entryIterator();
				Array _arr = new ArrayImpl();
				Entry<Key, Object> e;
				Object src, trg;
				boolean hasChanged = false;
				while (it.hasNext()) {
					e = it.next();
					src = e.getValue();
					trg = castTo(pc, _type, _strType, src);
					_arr.setEL(e.getKey(), trg);
					if (src != trg) hasChanged = true;
				}
				if (!hasChanged) return arr;
				return _arr;
			}

		}
		throw new CasterException(o, strType);
	}

	public static Component toComponent(PageContext pc, Pojo pojo, String compPath, Component defaultValue) {
		try {
			Component cfc = pc.loadComponent(compPath);
			Property[] props = cfc.getProperties(false, true, false, false);
			lucee.runtime.net.rpc.PojoIterator it = new lucee.runtime.net.rpc.PojoIterator(pojo);
			// only when the same amount of properties
			if (props.length == it.size()) {
				Map<Collection.Key, Property> propMap = propToMap(props);
				Property p;
				lucee.commons.lang.Pair<Collection.Key, Object> pair;
				ComponentScope scope = cfc.getComponentScope();
				while (it.hasNext()) {
					pair = it.next();
					p = propMap.get(pair.getName());
					if (p == null) return defaultValue;
					Object val = null;
					try {
						val = Caster.castTo(pc, p.getType(), pair.getValue(), false);
					}
					catch (PageException e) {}

					// store in variables and this scope
					scope.setEL(pair.getName(), val);
					cfc.setEL(pair.getName(), val);
				}
				return cfc;
			}
		}
		catch (PageException e) {}
		return defaultValue;
	}

	private static Map<Key, Property> propToMap(Property[] props) {
		Map<Collection.Key, Property> map = new HashMap<>();
		for (Property p: props) {
			map.put(KeyImpl.init(p.getName()), p);
		}
		return map;
	}

	/**
	 * cast a value to a value defined by type argument
	 * 
	 * @param pc
	 * @param type type of the returning Value
	 * @param o Object to cast
	 * @return casted Value
	 * @throws PageException
	 */
	public static Object castTo(PageContext pc, short type, Object o) throws PageException {
		if (type == CFTypes.TYPE_ANY) return o;
		else if (type == CFTypes.TYPE_ARRAY) return toArray(o);
		else if (type == CFTypes.TYPE_BOOLEAN) return toBoolean(o);
		else if (type == CFTypes.TYPE_BINARY) return toBinary(o);
		else if (type == CFTypes.TYPE_DATETIME) return DateCaster.toDateAdvanced(o, pc.getTimeZone());
		else if (type == CFTypes.TYPE_NUMERIC) return toDouble(o);
		else if (type == CFTypes.TYPE_QUERY) return toQuery(o);
		else if (type == CFTypes.TYPE_QUERY_COLUMN) return toQueryColumn(o);
		else if (type == CFTypes.TYPE_STRING) return toString(o);
		else if (type == CFTypes.TYPE_STRUCT) return toStruct(o);
		else if (type == CFTypes.TYPE_TIMESPAN) return toTimespan(o);
		else if (type == CFTypes.TYPE_UUID) return toGUId(o);
		else if (type == CFTypes.TYPE_UUID) return toUUId(o);
		else if (type == CFTypes.TYPE_VARIABLE_NAME) return toVariableName(o);
		else if (type == CFTypes.TYPE_VOID) return toVoid(o);
		else if (type == CFTypes.TYPE_FUNCTION) return toFunction(o);
		else if (type == CFTypes.TYPE_XML) return toXML(o);
		// ext.img else if(type==CFTypes.TYPE_IMAGE) return ImageUtil.toImage(pc,o,true);

		if (type == CFTypes.TYPE_UNDEFINED) throw new ExpressionException("type isn't defined (TYPE_UNDEFINED)");
		throw new ExpressionException("invalid type [" + type + "]");
	}

	/**
	 * cast a value to void (Empty String)
	 * 
	 * @param o
	 * @return void value
	 * @throws ExpressionException
	 */
	public static Object toVoid(Object o) throws ExpressionException {
		if (o == null) return null;
		else if (o instanceof String && o.toString().length() == 0) return null;
		else if (o instanceof Number && ((Number) o).intValue() == 0) return null;
		else if (o instanceof Boolean && ((Boolean) o).booleanValue() == false) return null;
		else if (o instanceof ObjectWrap) return toVoid(((ObjectWrap) o).getEmbededObject(null));
		throw new CasterException(o, "void");
	}

	/**
	 * cast a value to void (Empty String)
	 * 
	 * @param o
	 * @param defaultValue
	 * @return void value
	 */
	public static Object toVoid(Object o, Object defaultValue) {
		if (o == null) return null;
		else if (o instanceof String && o.toString().length() == 0) return null;
		else if (o instanceof Number && ((Number) o).intValue() == 0) return null;
		else if (o instanceof Boolean && ((Boolean) o).booleanValue() == false) return null;
		else if (o instanceof ObjectWrap) return toVoid(((ObjectWrap) o).getEmbededObject((defaultValue)), defaultValue);
		return defaultValue;
	}

	/**
	 * cast an Object to a reference type (Object), in that case this method to nothing, because an
	 * Object is already a reference type
	 * 
	 * @param o Object to cast
	 * @return casted Object
	 */
	public static Object toRef(Object o) {
		return o;
	}

	/**
	 * cast a String to a reference type (Object), in that case this method to nothing, because a String
	 * is already a reference type
	 * 
	 * @param o Object to cast
	 * @return casted Object
	 */
	public static String toRef(String o) {
		return o;
	}

	/**
	 * cast a Collection to a reference type (Object), in that case this method to nothing, because a
	 * Collection is already a reference type
	 * 
	 * @param o Collection to cast
	 * @return casted Object
	 */
	public static Collection toRef(Collection o) {
		return o;
	}

	/**
	 * cast a char value to his (CFML) reference type String
	 * 
	 * @param c char to cast
	 * @return casted String
	 */
	public static String toRef(char c) {
		return "" + c;
	}

	/**
	 * cast a boolean value to his (CFML) reference type Boolean
	 * 
	 * @param b boolean to cast
	 * @return casted Boolean
	 */
	public static Boolean toRef(boolean b) {
		return b ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * cast a byte value to his (CFML) reference type Integer
	 * 
	 * @param b byte to cast
	 * @return casted Integer
	 */
	public static Byte toRef(byte b) {
		return new Byte(b);
	}

	/**
	 * cast an int value to his (CFML) reference type Integer
	 * 
	 * @param i int to cast
	 * @return casted Integer
	 */
	public static Integer toRef(int i) {
		return Integer.valueOf(i);
	}

	/**
	 * cast a float value to his (CFML) reference type Float
	 * 
	 * @param f float to cast
	 * @return casted Float
	 */
	public static Float toRef(float f) {
		return new Float(f);
	}

	/**
	 * cast a long value to his (CFML) reference type Long
	 * 
	 * @param l long to cast
	 * @return casted Long
	 */
	public static Long toRef(long l) {
		return Long.valueOf(l);
	}

	/**
	 * cast a double value to his (CFML) reference type Double
	 * 
	 * @param d doble to cast
	 * @return casted Double
	 */
	public static Double toRef(double d) {
		return Double.valueOf(d);
	}

	/**
	 * cast a double value to his (CFML) reference type Double
	 * 
	 * @param s short to cast
	 * @return casted Short
	 */
	public static Short toRef(short s) {
		return Short.valueOf(s);
	}

	/**
	 * cast an Object to an Iterator or get Iterator from Object
	 * 
	 * @param o Object to cast
	 * @return casted Collection
	 * @throws PageException
	 */
	public static Iterator toIterator(Object o) throws PageException {
		return ForEachUtil.forEach(o);
	}

	/**
	 * cast an Object to a Collection
	 * 
	 * @param o Object to cast
	 * @return casted Collection
	 * @throws PageException
	 */
	public static Collection toCollection(Object o) throws PageException {
		if (o instanceof Collection) return (Collection) o;
		else if (o instanceof Node) return XMLCaster.toXMLStruct((Node) o, false);
		else if (o instanceof Component) {
			try {
				Object tmp = Reflector.componentToClass(ThreadLocalPageContext.get(), (Component) o, Collection.class);
				if (tmp instanceof Collection) return (Collection) tmp;
			}
			catch (PageException e) {}
		}
		else if (o instanceof Map) {
			return MapAsStruct.toStruct((Map) o, true);// StructImpl((Map)o);
		}
		else if (o instanceof ObjectWrap) {
			return toCollection(((ObjectWrap) o).getEmbededObject());
		}
		else if (Decision.isCastableToArray(o)) {
			return toArray(o);
		}
		throw new CasterException(o, "collection");
	}

	public static java.util.Collection toJavaCollection(Object o) throws PageException {
		if (o instanceof java.util.Collection) return (java.util.Collection) o;
		return toList(o);
	}

	/**
	 * cast an Object to a Component
	 * 
	 * @param o Object to cast
	 * @return casted Component
	 * @throws PageException
	 */
	public static Component toComponent(Object o) throws PageException {
		if (o instanceof Component) return (Component) o;
		else if (o instanceof ObjectWrap) {
			return toComponent(((ObjectWrap) o).getEmbededObject());
		}
		throw new CasterException(o, "Component");
	}

	public static Component toComponent(Object o, Component defaultValue) {
		if (o instanceof Component) return (Component) o;
		else if (o instanceof ObjectWrap) {
			return toComponent(((ObjectWrap) o).getEmbededObject(defaultValue), defaultValue);
		}
		return defaultValue;
	}

	/**
	 * cast an Object to a Collection, if not returns null
	 * 
	 * @param o Object to cast
	 * @param defaultValue
	 * @return casted Collection
	 */
	public static Collection toCollection(Object o, Collection defaultValue) {
		if (o instanceof Collection) return (Collection) o;
		else if (o instanceof Node) return XMLCaster.toXMLStruct((Node) o, false);
		else if (o instanceof Component) {
			try {
				Object tmp = Reflector.componentToClass(ThreadLocalPageContext.get(), (Component) o, Collection.class);
				if (tmp instanceof Collection) return (Collection) tmp;
			}
			catch (PageException e) {}
		}
		else if (o instanceof Map) {
			return MapAsStruct.toStruct((Map) o, true);
		}
		else if (o instanceof ObjectWrap) {
			return toCollection(((ObjectWrap) o).getEmbededObject(defaultValue), defaultValue);
		}
		else if (Decision.isArray(o)) {
			try {
				return toArray(o);
			}
			catch (PageException e) {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	/**
	 * convert an object to a File
	 * 
	 * @param obj
	 * @return File
	 * @throws PageException
	 */
	public static File toFile(Object obj) throws PageException {
		if (obj instanceof File) return (File) obj;
		return FileUtil.toFile(Caster.toString(obj));
	}

	/**
	 * convert an object to a File
	 * 
	 * @param obj
	 * @param defaultValue
	 * @return File
	 */
	public static File toFile(Object obj, File defaultValue) {
		if (obj instanceof File) return (File) obj;
		String str = Caster.toString(obj, null);
		if (str == null) return defaultValue;
		return FileUtil.toFile(str);
	}

	/**
	 * convert an object array to a HashMap filled with Function value Objects
	 * 
	 * @param args Object array to convert
	 * @return hashmap containing Function values
	 * @throws ExpressionException
	 */
	public static Struct toFunctionValues(Object[] args) throws ExpressionException {
		return toFunctionValues(args, 0, args.length);
	}

	public static Struct toFunctionValues(Object[] args, int offset, int len) throws ExpressionException {
		// TODO nicht sehr optimal
		Struct sct = new StructImpl(StructImpl.TYPE_LINKED);
		for (int i = offset; i < offset + len; i++) {
			if (args[i] instanceof FunctionValueImpl) {
				FunctionValueImpl value = (FunctionValueImpl) args[i];
				sct.setEL(value.getNameAsKey(), value.getValue());
			}
			else throw new ExpressionException(
					"Missing argument name, when using named parameters to a function, every parameter must have a name [" + i + ":" + args[i].getClass().getName() + "].");
		}
		return sct;
	}

	public static Object[] toFunctionValues(Struct args) {
		// TODO nicht sehr optimal
		Iterator<Entry<Key, Object>> it = args.entryIterator();
		Entry<Key, Object> e;
		List<FunctionValue> fvalues = new ArrayList<FunctionValue>();
		while (it.hasNext()) {
			e = it.next();
			fvalues.add(new FunctionValueImpl(e.getKey().getString(), e.getValue()));
		}
		return fvalues.toArray(new FunctionValue[fvalues.size()]);
	}

	/**
	 * casts a string to a Locale
	 * 
	 * @param strLocale
	 * @return Locale from String
	 * @throws ExpressionException
	 */
	public static Locale toLocale(String strLocale) throws ExpressionException {
		return LocaleFactory.getLocale(strLocale);
	}

	/**
	 * casts a string to a Locale
	 * 
	 * @param strLocale
	 * @param defaultValue
	 * @return Locale from String
	 */
	public static Locale toLocale(String strLocale, Locale defaultValue) {
		return LocaleFactory.getLocale(strLocale, defaultValue);
	}

	/**
	 * casts a string to a TimeZone
	 * 
	 * @param strTimeZone
	 * @return TimeZone from String
	 * @throws ExpressionException
	 */
	public static TimeZone toTimeZone(String strTimeZone) throws ExpressionException {
		return TimeZoneUtil.toTimeZone(strTimeZone);
	}

	/**
	 * casts a string to a TimeZone
	 * 
	 * @param strTimeZone
	 * @param defaultValue
	 * @return TimeZone from String
	 */
	public static TimeZone toTimeZone(String strTimeZone, TimeZone defaultValue) {
		return TimeZoneUtil.toTimeZone(strTimeZone, defaultValue);
	}

	public static TimeZone toTimeZone(Object oTimeZone, TimeZone defaultValue) {
		if (oTimeZone instanceof TimeZone) return (TimeZone) oTimeZone;
		return TimeZoneUtil.toTimeZone(Caster.toString(oTimeZone, null), defaultValue);
	}

	/**
	 * casts an Object to a Node List
	 * 
	 * @param o Object to Cast
	 * @return NodeList from Object
	 * @throws PageException
	 */
	public static NodeList toNodeList(Object o) throws PageException {
		// print.ln("nodeList:"+o);
		if (o instanceof NodeList) {
			return (NodeList) o;
		}
		else if (o instanceof ObjectWrap) {
			return toNodeList(((ObjectWrap) o).getEmbededObject());
		}
		throw new CasterException(o, "NodeList");
	}

	/**
	 * casts an Object to a Node List
	 * 
	 * @param o Object to Cast
	 * @param defaultValue
	 * @return NodeList from Object
	 */
	public static NodeList toNodeList(Object o, NodeList defaultValue) {
		// print.ln("nodeList:"+o);
		if (o instanceof NodeList) {
			return (NodeList) o;
		}
		else if (o instanceof ObjectWrap) {
			return toNodeList(((ObjectWrap) o).getEmbededObject(defaultValue), defaultValue);
		}
		return defaultValue;
	}

	/**
	 * casts an Object to a XML Node
	 * 
	 * @param o Object to Cast
	 * @return Node from Object
	 * @throws PageException
	 */
	public static Node toNode(Object o) throws PageException {
		return toXML(o);
		/*
		 * if(o instanceof Node)return (Node)o; else if(o instanceof String) { try { return
		 * XMLCaster.toXMLStruct(XMLUtil.parse(o.toString(),false),false);
		 * 
		 * } catch (Exception e) { throw Caster.toPageException(e); } } else if(o instanceof ObjectWrap) {
		 * return toNode(((ObjectWrap)o).getEmbededObject()); } throw new CasterException(o,"Node");
		 */
	}

	/**
	 * casts an Object to a XML Node
	 * 
	 * @param o Object to Cast
	 * @param defaultValue
	 * @return Node from Object
	 */
	public static Node toNode(Object o, Node defaultValue) {
		return toXML(o, defaultValue);
		/*
		 * if(o instanceof Node)return (Node)o; else if(o instanceof String) { try { return
		 * XMLCaster.toXMLStruct(XMLUtil.parse(o.toString(),false),false);
		 * 
		 * } catch (Exception e) { return defaultValue; } } else if(o instanceof ObjectWrap) { return
		 * toNode(((ObjectWrap)o).getEmbededObject(defaultValue),defaultValue); } return defaultValue;
		 */
	}

	/**
	 * casts a boolean to an Integer
	 * 
	 * @param b
	 * @return Integer from boolean
	 */
	public static Integer toInteger(boolean b) {
		return b ? Constants.INTEGER_1 : Constants.INTEGER_0;
	}

	/**
	 * casts a char to an Integer
	 * 
	 * @param c
	 * @return Integer from char
	 */
	public static Integer toInteger(char c) {
		return Integer.valueOf(c);
	}

	/**
	 * casts a double to an Integer
	 * 
	 * @param d
	 * @return Integer from double
	 */
	public static Integer toInteger(double d) {
		return Integer.valueOf((int) d);
	}

	/**
	 * casts an Object to an Integer
	 * 
	 * @param o Object to cast to Integer
	 * @return Integer from Object
	 * @throws PageException
	 */
	public static Integer toInteger(Object o) throws PageException {
		return Integer.valueOf(toIntValue(o));
	}

	/**
	 * casts an Object to an Integer
	 * 
	 * @param str Object to cast to Integer
	 * @return Integer from Object
	 * @throws PageException
	 */
	public static Integer toInteger(String str) throws PageException {
		return Integer.valueOf(toIntValue(str));
	}

	// used in bytecode genrator
	public static Integer toInteger(int i) {
		return Integer.valueOf(i);
	}

	/**
	 * casts an Object to an Integer
	 * 
	 * @param o Object to cast to Integer
	 * @param defaultValue
	 * @return Integer from Object
	 */
	public static Integer toInteger(Object o, Integer defaultValue) {
		if (defaultValue != null) return Integer.valueOf(toIntValue(o, defaultValue.intValue()));
		int res = toIntValue(o, Integer.MIN_VALUE);
		if (res == Integer.MIN_VALUE) return defaultValue;
		return Integer.valueOf(res);
	}

	/**
	 * casts an Object to null
	 * 
	 * @param value
	 * @return to null from Object
	 * @throws PageException
	 */
	public static Object toNull(Object value) throws PageException {
		if (value == null) return null;
		if (value instanceof String && Caster.toString(value).trim().length() == 0) return null;
		if (value instanceof Number && ((Number) value).intValue() == 0) return null;
		throw new CasterException(value, "null");
	}

	/**
	 * casts an Object to null
	 * 
	 * @param value
	 * @param defaultValue
	 * @return to null from Object
	 */
	public static Object toNull(Object value, Object defaultValue) {
		if (value == null) return null;
		if (value instanceof String && Caster.toString(value, "").trim().length() == 0) return null;
		if (value instanceof Number && ((Number) value).intValue() == 0) return null;
		return defaultValue;
	}

	/**
	 * cast Object to a XML Node
	 * 
	 * @param value
	 * @param defaultValue
	 * @return XML Node
	 */
	public static Node toXML(Object value, Node defaultValue) {
		try {
			return toXML(value);
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	/**
	 * cast Object to a XML Node
	 * 
	 * @param value
	 * @return XML Node
	 * @throws PageException
	 */
	public static Node toXML(Object value) throws PageException {
		if (value instanceof Node) return XMLCaster.toXMLStruct((Node) value, false);
		if (value instanceof ObjectWrap) {
			return toXML(((ObjectWrap) value).getEmbededObject());
		}
		try {
			return XMLCaster.toXMLStruct(XMLUtil.parse(XMLUtil.toInputSource(null, value), null, false), false);
		}
		catch (Exception outer) {
			throw Caster.toPageException(outer);
		}

	}

	public static String toStringForce(Object value, String defaultValue) {
		String rtn = toString(value, null);
		if (rtn != null) return rtn;

		try {
			if (value instanceof Struct) {
				return new ScriptConverter().serialize(value);
			}
			else if (value instanceof Array) {
				return new ScriptConverter().serialize(value);
			}
		}
		catch (ConverterException e) {}
		return defaultValue;
	}

	public static Resource toResource(PageContext pc, Object src, boolean existing) throws ExpressionException {
		return toResource(pc, src, existing, pc.getConfig().allowRealPath());
	}

	public static Resource toResource(PageContext pc, Object src, boolean existing, boolean allowRealpath) throws ExpressionException {
		if (src instanceof Resource) return (Resource) src;
		if (src instanceof File) src = src.toString();
		if (src instanceof String) {
			if (existing) return ResourceUtil.toResourceExisting(pc, (String) src, allowRealpath);
			return ResourceUtil.toResourceNotExisting(pc, (String) src, allowRealpath, false);
		}
		if (src instanceof FileStreamWrapper) return ((FileStreamWrapper) src).getResource();
		throw new CasterException(src, "Resource");
	}

	public static Resource toResource(Config config, Object src, boolean existing) throws ExpressionException {
		if (src instanceof Resource) return (Resource) src;
		if (src instanceof File) src = src.toString();
		if (src instanceof String) {
			if (existing) return ResourceUtil.toResourceExisting(config, (String) src);
			return ResourceUtil.toResourceNotExisting(config, (String) src);
		}
		if (src instanceof FileStreamWrapper) return ((FileStreamWrapper) src).getResource();
		throw new CasterException(src, "Resource");
	}

	public static Hashtable toHashtable(Object obj) throws PageException {
		if (obj instanceof Hashtable) return (Hashtable) obj;
		return (Hashtable) Duplicator.duplicateMap(toMap(obj, false), new Hashtable(), false);
	}

	public static Vector toVetor(Object obj) throws PageException {
		if (obj instanceof Vector) return (Vector) obj;
		return (Vector) Duplicator.duplicateList(toList(obj, false), new Vector(), false);
	}

	public static Calendar toCalendar(Date date, TimeZone tz, Locale l) {
		tz = ThreadLocalPageContext.getTimeZone(tz);
		Calendar c = tz == null ? JREDateTimeUtil.newInstance(tz, l) : JREDateTimeUtil.newInstance(tz, l);
		c.setTime(date);
		return c;
	}

	public static Calendar toCalendar(long time, TimeZone tz, Locale l) {
		tz = ThreadLocalPageContext.getTimeZone(tz);
		Calendar c = tz == null ? JREDateTimeUtil.newInstance(tz, l) : JREDateTimeUtil.newInstance(tz, l);
		c.setTimeInMillis(time);
		return c;
	}

	public static Serializable toSerializable(Object object) throws CasterException {
		if (object instanceof Serializable) return (Serializable) object;
		throw new CasterException(object, "Serializable");
	}

	public static Serializable toSerializable(Object object, Serializable defaultValue) {
		if (object instanceof Serializable) return (Serializable) object;
		return defaultValue;
	}

	public static byte[] toBytes(Object obj, Charset charset) throws PageException {
		try {
			if (obj instanceof byte[]) return (byte[]) obj;
			if (obj instanceof InputStream) return IOUtil.toBytes((InputStream) obj);
			if (obj instanceof Resource) return IOUtil.toBytes((Resource) obj);
			if (obj instanceof File) return IOUtil.toBytes((File) obj);
			if (obj instanceof String) return ((String) obj).getBytes(charset == null ? SystemUtil.getCharset() : charset);
			if (obj instanceof Blob) {
				InputStream is = null;
				try {
					is = ((Blob) obj).getBinaryStream();
					return IOUtil.toBytes(is);
				}
				finally {
					IOUtil.close(is);
				}
			}
		}
		catch (IOException ioe) {
			throw toPageException(ioe);
		}
		catch (SQLException ioe) {
			throw toPageException(ioe);
		}
		throw new CasterException(obj, byte[].class);
	}

	public static InputStream toInputStream(Object obj, Charset charset) throws PageException {
		try {
			if (obj instanceof InputStream) return (InputStream) obj;
			if (obj instanceof byte[]) return new ByteArrayInputStream((byte[]) obj);
			if (obj instanceof Resource) return ((Resource) obj).getInputStream();
			if (obj instanceof File) return new FileInputStream((File) obj);
			if (obj instanceof String) return new ByteArrayInputStream(((String) obj).getBytes(charset == null ? SystemUtil.getCharset() : charset));
			if (obj instanceof Blob) return ((Blob) obj).getBinaryStream();
		}
		catch (IOException ioe) {
			throw toPageException(ioe);
		}
		catch (SQLException ioe) {
			throw toPageException(ioe);
		}
		throw new CasterException(obj, InputStream.class);
	}

	public static OutputStream toOutputStream(Object obj) throws PageException {
		if (obj instanceof OutputStream) return (OutputStream) obj;
		throw new CasterException(obj, OutputStream.class);
	}

	public static Object castTo(PageContext pc, Class trgClass, Object obj) throws PageException {
		if (trgClass == null) return Caster.toNull(obj);
		else if (obj.getClass() == trgClass) return obj;

		else if (trgClass == byte[].class) return Caster.toBinary(obj);
		else if (trgClass == boolean.class) return Caster.toBoolean(obj);
		else if (trgClass == byte.class) return Caster.toByte(obj);
		else if (trgClass == short.class) return Caster.toShort(obj);
		else if (trgClass == int.class) return Caster.toInteger(obj);
		else if (trgClass == long.class) return Caster.toLong(obj);
		else if (trgClass == float.class) return Caster.toFloat(obj);
		else if (trgClass == double.class) return Caster.toDouble(obj);
		else if (trgClass == char.class) return Caster.toCharacter(obj);

		else if (trgClass == Boolean.class) return Caster.toBoolean(obj);
		else if (trgClass == Byte.class) return Caster.toByte(obj);
		else if (trgClass == Short.class) return Caster.toShort(obj);
		else if (trgClass == Integer.class) return Caster.toInteger(obj);
		else if (trgClass == Long.class) return Caster.toLong(obj);
		else if (trgClass == Float.class) return Caster.toFloat(obj);
		else if (trgClass == Double.class) return Caster.toDouble(obj);
		else if (trgClass == Character.class) return Caster.toCharacter(obj);

		else if (trgClass == Object.class) return obj;
		else if (trgClass == String.class) return Caster.toString(obj);

		if (Reflector.isInstaneOf(obj.getClass(), trgClass, false)) return obj;

		if (obj instanceof Component) {
			if (trgClass == Component.class) return obj;
			Component comp = ((Component) obj);
			if (trgClass.isInterface()) { // TODO allow not only intefaces
				return Reflector.componentToClass(pc, comp, trgClass);
			}
		}

		return castTo(pc, Caster.toClassName(trgClass), obj, false);
	}

	public static Objects toObjects(PageContext pc, Object obj) throws PageException {
		if (obj instanceof Objects) return (Objects) obj;
		if (obj instanceof ObjectWrap) return toObjects(pc, ((ObjectWrap) obj).getEmbededObject());
		return new JavaObject(pc.getVariableUtil(), obj);
	}

	public static BigDecimal toBigDecimal(Object o) throws PageException {
		if (o instanceof BigDecimal) return (BigDecimal) o;
		if (o instanceof Number) {
			return new BigDecimal(((Number) o).toString());
		}
		else if (o instanceof Boolean) return new BigDecimal(((Boolean) o).booleanValue() ? 1 : 0);
		else if (o instanceof CharSequence) return new BigDecimal(o.toString());
		else if (o instanceof Character) return new BigDecimal((((Character) o).charValue()));
		else if (o instanceof Castable) return new BigDecimal(((Castable) o).castToDoubleValue());
		else if (o == null) return BigDecimal.ZERO;
		else if (o instanceof ObjectWrap) return toBigDecimal(((ObjectWrap) o).getEmbededObject());
		throw new CasterException(o, "number");
	}

	public static BigInteger toBigInteger(Object o) throws PageException {
		if (o instanceof BigInteger) return (BigInteger) o;
		if (o instanceof Number) {
			return new BigInteger(((Number) o).toString());
		}
		else if (o instanceof Boolean) return new BigInteger(((Boolean) o).booleanValue() ? "1" : "0");
		else if (o instanceof CharSequence) return new BigInteger(o.toString());
		else if (o instanceof Character) return new BigInteger(String.valueOf((int) (((Character) o).charValue())));
		else if (o instanceof Castable) return new BigInteger("" + Caster.toIntValue(((Castable) o).castToDoubleValue()));
		else if (o == null) return BigInteger.ZERO;
		else if (o instanceof ObjectWrap) return toBigInteger(((ObjectWrap) o).getEmbededObject());
		throw new CasterException(o, "number");
	}

	public static Object unwrap(Object value) throws PageException {
		if (value == null) return null;
		if (value instanceof ObjectWrap) {
			return ((ObjectWrap) value).getEmbededObject();
		}
		if (value instanceof JavaObject) {
			return ((JavaObject) value).getEmbededObject();
		}

		return value;
	}

	public static Object unwrap(Object value, Object defaultValue) {
		if (value == null) return null;
		if (value instanceof ObjectWrap) {
			return ((ObjectWrap) value).getEmbededObject(defaultValue);
		}
		if (value instanceof JavaObject) {
			return ((JavaObject) value).getEmbededObject(defaultValue);
		}
		return value;
	}

	public static CharSequence toCharSequence(Object obj) throws PageException {
		if (obj instanceof CharSequence) return (CharSequence) obj;
		if (obj instanceof Component) {
			try {
				Object tmp = Reflector.componentToClass(ThreadLocalPageContext.get(), (Component) obj, CharSequence.class);
				if (tmp instanceof CharSequence) return (CharSequence) tmp;
			}
			catch (PageException pe) {}
		}
		return Caster.toString(obj);
	}

	public static CharSequence toCharSequence(Object obj, CharSequence defaultValue) {
		if (obj instanceof CharSequence) return (CharSequence) obj;
		if (obj instanceof Component) {
			try {
				Object tmp = Reflector.componentToClass(ThreadLocalPageContext.get(), (Component) obj, CharSequence.class);
				if (tmp instanceof CharSequence) return (CharSequence) tmp;
			}
			catch (PageException pe) {}
		}

		String str = Caster.toString(obj, null);
		if (str == null) return defaultValue;
		return str;
	}

	public static Pojo toPojo(Pojo pojo, Component comp, Set<Object> done) throws PageException {
		PageContext pc = ThreadLocalPageContext.get();
		try {
			return _toPojo(pc, pojo, comp, done);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private static Pojo _toPojo(PageContext pc, Pojo pojo, Component comp, Set<Object> done) throws PageException {// print.ds();System.exit(0);
		comp = ComponentSpecificAccess.toComponentSpecificAccess(Component.ACCESS_PRIVATE, comp);
		ComponentScope scope = comp.getComponentScope();

		// create Pojo
		if (pojo == null) {
			try {
				pojo = (Pojo) ClassUtil.loadInstance(ComponentUtil.getComponentPropertiesClass(pc, comp));
			}
			catch (ClassException e) {
				throw Caster.toPageException(e);
			}
		}

		// initialize Pojo
		Property[] props = comp.getProperties(false, true, false, false);
		_initPojo(pc, pojo, props, scope, comp, done);

		return pojo;
	}

	public static Pojo toPojo(Pojo pojo, Struct sct, Set<Object> done) throws PageException {
		PageContext pc = ThreadLocalPageContext.get();
		try {
			return _toPojo(pc, pojo, sct, done);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private static Pojo _toPojo(PageContext pc, Pojo pojo, Struct sct, Set<Object> done) throws PageException {// print.ds();System.exit(0);
		if (pojo == null) {
			try {
				PhysicalClassLoader cl = (PhysicalClassLoader) pc.getConfig().getRPCClassLoader(false);
				pojo = (Pojo) ClassUtil.loadInstance(ComponentUtil.getStructPropertiesClass(pc, sct, cl));
			}
			catch (ClassException e) {
				throw Caster.toPageException(e);
			}
			catch (IOException e) {
				throw Caster.toPageException(e);
			}
		}

		// initialize
		List<Property> props = new ArrayList<Property>();
		Iterator<Entry<Key, Object>> it = sct.entryIterator();
		Entry<Key, Object> e;
		PropertyImpl p;
		while (it.hasNext()) {
			e = it.next();
			p = new PropertyImpl();
			p.setAccess(Component.ACCESS_PUBLIC);
			p.setName(e.getKey().getString());
			p.setType(e.getValue() == null ? "any" : Caster.toTypeName(e.getValue()));
			props.add(p);
		}

		_initPojo(pc, pojo, props.toArray(new Property[props.size()]), sct, null, done);

		return pojo;
	}

	private static void _initPojo(PageContext pc, Pojo pojo, Property[] props, Struct sct, Component comp, Set<Object> done) throws PageException {
		Property p;
		Object v;
		Collection.Key k;
		CFMLExpressionInterpreter interpreter = new CFMLExpressionInterpreter(false);

		for (int i = 0; i < props.length; i++) {
			p = props[i];
			k = Caster.toKey(p.getName());
			// value
			v = sct.get(k, null);
			if (v == null && comp != null) v = comp.get(k, null);

			// default

			if (v != null) v = Caster.castTo(pc, p.getType(), v, false);
			else {
				if (!StringUtil.isEmpty(p.getDefault())) {
					try {
						v = Caster.castTo(pc, p.getType(), p.getDefault(), false);

					}
					catch (PageException pe) {
						try {
							v = interpreter.interpret(pc, p.getDefault());
							v = Caster.castTo(pc, p.getType(), v, false);
						}
						catch (PageException pe2) {
							throw new ExpressionException("can not use default value [" + p.getDefault() + "] for property [" + p.getName() + "] with type [" + p.getType() + "]");
						}
					}
				}
			}

			// set or throw
			if (v == null) {
				if (p.isRequired()) throw new ExpressionException("required property [" + p.getName() + "] is not defined");
			}
			else {
				Reflector.callSetter(pojo, p.getName().toLowerCase(), v);
			}
		}
	}

	public static long toTime(lucee.runtime.type.dt.Date date, Time time, TimeZone tz) {
		if (time == null) return date.getTime();
		tz = ThreadLocalPageContext.getTimeZone(tz);
		Calendar c = JREDateTimeUtil.getThreadCalendar(tz);
		c.setTimeInMillis(date.getTime());
		int y = c.get(Calendar.YEAR);
		int m = c.get(Calendar.MONTH);
		int d = c.get(Calendar.DAY_OF_MONTH);
		c.setTimeInMillis(time.getTime());
		c.set(Calendar.YEAR, y);
		c.set(Calendar.MONTH, m);
		c.set(Calendar.DAY_OF_MONTH, d);
		return c.getTimeInMillis();
	}
}
