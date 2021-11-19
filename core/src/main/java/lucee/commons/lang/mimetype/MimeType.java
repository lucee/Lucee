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
package lucee.commons.lang.mimetype;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lucee.commons.io.CharsetUtil;
import lucee.commons.lang.CharSet;
import lucee.commons.lang.StringUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.ListUtil;

public class MimeType {

	private static int DEFAULT_MXB = 100000;
	private static double DEFAULT_MXT = 5;
	private static double DEFAULT_QUALITY = 1;
	private static CharSet DEFAULT_CHARSET = null;

	public static final MimeType ALL = new MimeType(null, null, null);
	public static final MimeType APPLICATION_JSON = new MimeType("application", "json", null);
	public static final MimeType APPLICATION_XML = new MimeType("application", "xml", null);
	public static final MimeType APPLICATION_WDDX = new MimeType("application", "wddx", null);
	public static final MimeType APPLICATION_CFML = new MimeType("application", "cfml", null);
	public static final MimeType APPLICATION_PLAIN = new MimeType("application", "lazy", null);

	public static final MimeType IMAGE_GIF = new MimeType("image", "gif", null);
	public static final MimeType IMAGE_JPG = new MimeType("image", "jpeg", null);
	public static final MimeType IMAGE_PNG = new MimeType("image", "png", null);
	public static final MimeType IMAGE_TIFF = new MimeType("image", "tiff", null);
	public static final MimeType IMAGE_BMP = new MimeType("image", "bmp", null);
	public static final MimeType IMAGE_WBMP = new MimeType("image", "vnd.wap.wbmp", null);
	public static final MimeType IMAGE_FBX = new MimeType("image", "fbx", null);
	public static final MimeType IMAGE_PNM = new MimeType("image", "x-portable-anymap", null);
	public static final MimeType IMAGE_PGM = new MimeType("image", "x-portable-graymap", null);
	public static final MimeType IMAGE_PBM = new MimeType("image", "x-portable-bitmap", null);
	public static final MimeType IMAGE_ICO = new MimeType("image", "ico", null);
	public static final MimeType IMAGE_PSD = new MimeType("image", "psd", null);
	public static final MimeType IMAGE_ASTERIX = new MimeType("image", null, null);
	public static final MimeType APPLICATION_JAVA = new MimeType("application", "java", null);

	public static final MimeType TEXT_HTML = new MimeType("text", "html", null);

	private String type;
	private String subtype;
	// private double quality;
	// private int mxb;
	// private double mxt;
	private Map<String, String> properties;
	private double q = -1;
	private CharSet cs;
	private boolean initCS = true;

	private MimeType(String type, String subtype, Map<String, String> properties) {
		// if(quality<0 || quality>1)
		// throw new RuntimeException("quality must be a number between 0 and 1, now ["+quality+"]");

		this.type = type;
		this.subtype = subtype;
		this.properties = properties;
		// this.quality=quality;
		// this.mxb=mxb;
		// this.mxt=mxt;
	}

	private static MimeType getInstance(String type, String subtype, Map<String, String> properties) {
		// TODO read this from an external File
		if ("text".equals(type)) {
			if ("xml".equals(subtype)) return new MimeType("application", "xml", properties);
			if ("x-json".equals(subtype)) return new MimeType("application", "json", properties);
			if ("javascript".equals(subtype)) return new MimeType("application", "json", properties);
			if ("x-javascript".equals(subtype)) return new MimeType("application", "json", properties);
			if ("wddx".equals(subtype)) return new MimeType("application", "wddx", properties);
		}
		else if ("application".equals(type)) {
			if ("x-json".equals(subtype)) return new MimeType("application", "json", properties);
			if ("javascript".equals(subtype)) return new MimeType("application", "json", properties);
			if ("x-javascript".equals(subtype)) return new MimeType("application", "json", properties);

			if ("jpg".equals(subtype)) return new MimeType("image", "jpeg", properties);
			if ("x-jpg".equals(subtype)) return new MimeType("image", "jpeg", properties);

			if ("png".equals(subtype)) return new MimeType("image", "png", properties);
			if ("x-png".equals(subtype)) return new MimeType("image", "png", properties);

			if ("tiff".equals(subtype)) return new MimeType("image", "tiff", properties);
			if ("tif".equals(subtype)) return new MimeType("image", "tiff", properties);
			if ("x-tiff".equals(subtype)) return new MimeType("image", "tiff", properties);
			if ("x-tif".equals(subtype)) return new MimeType("image", "tiff", properties);

			if ("fpx".equals(subtype)) return new MimeType("image", "fpx", properties);
			if ("x-fpx".equals(subtype)) return new MimeType("image", "fpx", properties);
			if ("vnd.fpx".equals(subtype)) return new MimeType("image", "fpx", properties);
			if ("vnd.netfpx".equals(subtype)) return new MimeType("image", "fpx", properties);

			if ("ico".equals(subtype)) return new MimeType("image", "ico", properties);
			if ("x-ico".equals(subtype)) return new MimeType("image", "ico", properties);
			if ("x-icon".equals(subtype)) return new MimeType("image", "ico", properties);

			if ("psd".equals(subtype)) return new MimeType("image", "psd", properties);
			if ("x-photoshop".equals(subtype)) return new MimeType("image", "psd", properties);
			if ("photoshop".equals(subtype)) return new MimeType("image", "psd", properties);
		}
		else if ("image".equals(type)) {
			if ("gi_".equals(subtype)) return new MimeType("image", "gif", properties);

			if ("pjpeg".equals(subtype)) return new MimeType("image", "jpeg", properties);
			if ("jpg".equals(subtype)) return new MimeType("image", "jpeg", properties);
			if ("jpe".equals(subtype)) return new MimeType("image", "jpeg", properties);
			if ("vnd.swiftview-jpeg".equals(subtype)) return new MimeType("image", "jpeg", properties);
			if ("pipeg".equals(subtype)) return new MimeType("image", "jpeg", properties);
			if ("jp_".equals(subtype)) return new MimeType("image", "jpeg", properties);

			if ("x-png".equals(subtype)) return new MimeType("image", "png", properties);

			if ("tif".equals(subtype)) return new MimeType("image", "tiff", properties);
			if ("x-tif".equals(subtype)) return new MimeType("image", "tiff", properties);
			if ("x-tiff".equals(subtype)) return new MimeType("image", "tiff", properties);

			if ("x-fpx".equals(subtype)) return new MimeType("image", "fpx", properties);
			if ("vnd.fpx".equals(subtype)) return new MimeType("image", "fpx", properties);
			if ("vnd.netfpx".equals(subtype)) return new MimeType("image", "fpx", properties);

			if ("x-portable/graymap".equals(subtype)) return new MimeType("image", "x-portable-anymap", properties);
			if ("portable graymap".equals(subtype)) return new MimeType("image", "x-portable-anymap", properties);
			if ("x-pnm".equals(subtype)) return new MimeType("image", "x-portable-anymap", properties);
			if ("pnm".equals(subtype)) return new MimeType("image", "x-portable-anymap", properties);

			if ("x-portable/graymap".equals(subtype)) return new MimeType("image", "x-portable-anymap", properties);
			if ("portable graymap".equals(subtype)) return new MimeType("image", "x-portable-anymap", properties);
			if ("x-pgm".equals(subtype)) return new MimeType("image", "x-portable-anymap", properties);
			if ("pgm".equals(subtype)) return new MimeType("image", "x-portable-graymap", properties);

			if ("portable bitmap".equals(subtype)) return new MimeType("image", "x-portable-bitmap", properties);
			if ("x-portable/bitmap".equals(subtype)) return new MimeType("image", "x-portable-bitmap", properties);
			if ("x-pbm".equals(subtype)) return new MimeType("image", "x-portable-bitmap", properties);
			if ("pbm".equals(subtype)) return new MimeType("image", "x-portable-bitmap", properties);

			if ("x-ico".equals(subtype)) return new MimeType("image", "ico", properties);
			if ("x-icon".equals(subtype)) return new MimeType("image", "ico", properties);

			if ("x-photoshop".equals(subtype)) return new MimeType("image", "psd", properties);
			if ("photoshop".equals(subtype)) return new MimeType("image", "psd", properties);
		}
		else if ("zz-application".equals(type)) {
			if ("zz-winassoc-psd".equals(subtype)) return new MimeType("image", "psd", properties);
		}
		/*
		 * 
		 * if("image/x-p".equals(mt)) return "ppm"; if("image/x-ppm".equals(mt)) return "ppm";
		 * if("image/ppm".equals(mt)) return "ppm";
		 * 
		 */
		return new MimeType(type, subtype, properties);
	}

	/**
	 * returns a mimetype that match given string
	 * 
	 * @param strMimeType
	 * @return
	 */
	public static MimeType getInstance(String strMimeType) {
		if (strMimeType == null) return ALL;
		strMimeType = strMimeType.trim();

		if ("*".equals(strMimeType) || strMimeType.length() == 0) return ALL;
		String[] arr = ListUtil.listToStringArray(strMimeType, ';');
		if (arr.length == 0) return ALL;

		String[] arrCT = ListUtil.listToStringArray(arr[0].trim(), '/');

		// subtype
		String type = null, subtype = null;

		// type
		if (arrCT.length >= 1) {
			type = arrCT[0].trim();
			if ("*".equals(type)) type = null;

			if (arrCT.length >= 2) {
				subtype = arrCT[1].trim();
				if ("*".equals(subtype)) subtype = null;
			}
		}
		if (arr.length == 1) return getInstance(type, subtype, null);

		final Map<String, String> properties = new HashMap<String, String>();
		String entry;
		String[] _arr;
		for (int i = 1; i < arr.length; i++) {
			entry = arr[i].trim();
			_arr = ListUtil.listToStringArray(entry, '=');
			if (_arr.length >= 2) properties.put(_arr[0].trim().toLowerCase(), _arr[1].trim());
			else if (_arr.length == 1 && !_arr[0].trim().toLowerCase().equals("*")) properties.put(_arr[0].trim().toLowerCase(), "");

		}
		return getInstance(type, subtype, properties);
	}

	public static MimeType[] getInstances(String strMimeTypes, char delimiter) {
		if (StringUtil.isEmpty(strMimeTypes, true)) return new MimeType[0];
		String[] arr = ListUtil.trimItems(ListUtil.listToStringArray(strMimeTypes, delimiter));
		MimeType[] mtes = new MimeType[arr.length];
		for (int i = 0; i < arr.length; i++) {
			mtes[i] = getInstance(arr[i]);
		}
		return mtes;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the subtype
	 */
	public String getSubtype() {
		return subtype;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	/**
	 * @return the type
	 */
	String getTypeNotNull() {
		return type == null ? "*" : type;
	}

	/**
	 * @return the subtype
	 */
	String getSubtypeNotNull() {
		return subtype == null ? "*" : subtype;
	}

	public double getQuality() {
		if (q == -1) {
			if (properties == null) q = DEFAULT_QUALITY;
			else q = Caster.toDoubleValue(getProperty("q"), DEFAULT_QUALITY);
		}
		return q;
	}

	public Charset getCharset() {
		if (initCS) {
			if (properties == null) cs = DEFAULT_CHARSET;
			else {
				String str = getProperty("charset");
				cs = StringUtil.isEmpty(str) ? DEFAULT_CHARSET : CharsetUtil.toCharSet(str);
			}
			initCS = false;
		}
		return CharsetUtil.toCharset(cs);
	}

	/*
	 * public int getMxb() { return Caster.toIntValue(properties.get("mxb"),DEFAULT_MXB); }
	 * 
	 * public double getMxt() { return Caster.toDoubleValue(properties.get("mxt"),DEFAULT_MXT); }
	 */

	private String getProperty(String name) {
		if (properties != null) {
			String value = properties.get(name);
			if (value != null) return value;

			Iterator<Entry<String, String>> it = properties.entrySet().iterator();
			Entry<String, String> e;
			while (it.hasNext()) {
				e = it.next();
				if (name.equalsIgnoreCase(e.getKey())) return e.getValue();
			}
		}
		return null;
	}

	public boolean hasWildCards() {
		return type == null || subtype == null;
	}

	/**
	 * checks if given mimetype is covered by current mimetype
	 * 
	 * @param other
	 * @return
	 */
	public boolean match(MimeType other) {
		if (this == other) return true;
		if (type != null && other.type != null && !type.equals(other.type)) return false;
		if (subtype != null && other.subtype != null && !subtype.equals(other.subtype)) return false;
		return true;
	}

	public MimeType bestMatch(MimeType[] others) {
		MimeType best = null;

		for (int i = 0; i < others.length; i++) {
			if (match(others[i]) && (best == null || best.getQuality() < others[i].getQuality())) {
				best = others[i];
			}
		}
		return best;
	}

	/**
	 * checks if other is from the same type, just type and subtype are checked, properties (q,mxb,mxt)
	 * are ignored.
	 * 
	 * @param other
	 * @return
	 */
	public boolean same(MimeType other) {
		if (this == other) return true;
		return getTypeNotNull().equals(other.getTypeNotNull()) && getSubtypeNotNull().equals(other.getSubtypeNotNull());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;

		MimeType other;
		if (obj instanceof MimeType) other = (MimeType) obj;
		else if (obj instanceof String) other = MimeType.getInstance((String) obj);
		else return false;

		if (!same(other)) return false;

		return other.toString().equals(toString());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(type == null ? "*" : type);
		sb.append("/");
		sb.append(subtype == null ? "*" : subtype);
		if (properties != null) {
			String[] keys = properties.keySet().toArray(new String[properties.size()]);
			Arrays.sort(keys);
			// Iterator<Entry<String, String>> it = properties.entrySet().iterator();
			// Entry<String, String> e;
			for (int i = 0; i < keys.length; i++) {
				sb.append("; ");
				sb.append(keys[i]);
				sb.append("=");
				sb.append(properties.get(keys[i]));
			}
		}
		return sb.toString();
	}

	public static MimeType toMimetype(int format, MimeType defaultValue) {
		switch (format) {
		case UDF.RETURN_FORMAT_JSON:
			return MimeType.APPLICATION_JSON;
		case UDF.RETURN_FORMAT_WDDX:
			return MimeType.APPLICATION_WDDX;
		case UDF.RETURN_FORMAT_SERIALIZE:
			return MimeType.APPLICATION_CFML;
		case UDF.RETURN_FORMAT_XML:
			return MimeType.APPLICATION_XML;
		case UDF.RETURN_FORMAT_PLAIN:
			return MimeType.APPLICATION_PLAIN;
		case UDF.RETURN_FORMAT_JAVA:
			return MimeType.APPLICATION_JAVA;

		}
		return defaultValue;
	}

	public static int toFormat(List<MimeType> mimeTypes, int ignore, int defaultValue) {
		if (mimeTypes == null || mimeTypes.size() == 0) return defaultValue;
		Iterator<MimeType> it = mimeTypes.iterator();
		int res;
		while (it.hasNext()) {
			res = toFormat(it.next(), -1);
			if (res != -1 && res != ignore) return res;
		}
		return defaultValue;
	}

	public static int toFormat(MimeType mt, int defaultValue) {
		if (mt == null) return defaultValue;
		if (MimeType.APPLICATION_JSON.same(mt)) return UDF.RETURN_FORMAT_JSON;
		if (MimeType.APPLICATION_WDDX.same(mt)) return UDF.RETURN_FORMAT_WDDX;
		if (MimeType.APPLICATION_CFML.same(mt)) return UDF.RETURN_FORMAT_SERIALIZE;
		if (MimeType.APPLICATION_XML.same(mt)) return UDF.RETURN_FORMAT_XML;
		if (MimeType.APPLICATION_PLAIN.same(mt)) return UDF.RETURN_FORMAT_PLAIN;
		if (MimeType.APPLICATION_JAVA.same(mt)) return UDF.RETURN_FORMAT_JAVA;
		return defaultValue;
	}

}