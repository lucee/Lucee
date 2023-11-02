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

public final class CFTypes {

	/**
	 * Field <code>TYPE_ANY</code>
	 */
	public static final short TYPE_ANY = 0; // do not change used in extension
	/**
	 * Field <code>TYPE_ARRAY</code>
	 */
	public static final short TYPE_ARRAY = 1;
	/**
	 * Field <code>TYPE_BOOLEAN</code>
	 */
	public static final short TYPE_BASE64 = 20;
	/**
	 * Field <code>TYPE_BOOLEAN</code>
	 */
	public static final short TYPE_BOOLEAN = 2; // do not change used in extension
	/**
	 * Field <code>TYPE_BINARY</code>
	 */
	public static final short TYPE_BINARY = 3;
	/**
	 * Field <code>TYPE_DATETIME</code>
	 */
	public static final short TYPE_DATETIME = 4;
	/**
	 * Field <code>TYPE_NUMERIC</code>
	 */
	public static final short TYPE_NUMERIC = 5;
	/**
	 * Field <code>TYPE_QUERY</code>
	 */
	public static final short TYPE_QUERY = 6;
	/**
	 * Field <code>TYPE_STRING</code>
	 */
	public static final short TYPE_STRING = 7; // do not change used in extension
	/**
	 * Field <code>TYPE_STRUCT</code>
	 */
	public static final short TYPE_STRUCT = 8;
	/**
	 * Field <code>TYPE_TIMESPAN</code>
	 */
	public static final short TYPE_TIMESPAN = 9;
	/**
	 * Field <code>TYPE_UUID</code>
	 */
	public static final short TYPE_UUID = 10;
	/**
	 * Field <code>TYPE_VARIABLE_NAME</code>
	 */
	public static final short TYPE_VARIABLE_NAME = 11;
	/**
	 * Field <code>TYPE_VARIABLE_STRING</code>
	 */
	public static final short TYPE_VARIABLE_STRING = 12;
	/**
	 * Field <code>TYPE_UNKNOW</code>
	 */
	public static final short TYPE_UNKNOW = -1; // do not change used in extension
	/**
	 * Field <code>TYPE_UNKNOW</code>
	 */
	public static final short TYPE_UNDEFINED = 14;
	/**
	 * Field <code>TYPE_VOID</code>
	 */
	public static final short TYPE_VOID = 15;// do never change this is hardcoded in flex extension

	/**
	 * Field <code>TYPE_XML</code>
	 */
	public static final short TYPE_XML = 16;

	// public static final short TYPE_SIZE = 21;

	public static final short TYPE_GUID = 22;

	public static final short TYPE_FUNCTION = 23;
	public static final short TYPE_QUERY_COLUMN = 24;
	public static final short TYPE_IMAGE = 25; // used in extension image
	public static final short TYPE_LOCALE = 26;
	public static final short TYPE_TIMEZONE = 27;

	/**
	 * Wandelt einen String Datentypen in ein CFML short Typ um.
	 * 
	 * @param type
	 * @param defaultValue
	 * @return short Data Type
	 */
	public static String toString(int type, String defaultValue) {
		switch (type) {
		case TYPE_ANY:
			return "any";
		case TYPE_ARRAY:
			return "array";
		case TYPE_BASE64:
			return "base64";
		case TYPE_BINARY:
			return "binary";
		case TYPE_BOOLEAN:
			return "boolean";
		case TYPE_DATETIME:
			return "datetime";
		case TYPE_GUID:
			return "guid";
		case TYPE_IMAGE:
			return "image";
		case TYPE_NUMERIC:
			return "numeric";
		case TYPE_QUERY:
			return "query";
		case TYPE_QUERY_COLUMN:
			return "querycolumn";
		case TYPE_STRING:
			return "string";
		case TYPE_STRUCT:
			return "struct";
		case TYPE_TIMESPAN:
			return "timespan";
		case TYPE_UNDEFINED:
			return "any";
		case TYPE_UNKNOW:
			return "any";
		case TYPE_UUID:
			return "uuid";
		case TYPE_VARIABLE_NAME:
			return "variablename";
		case TYPE_VARIABLE_STRING:
			return "variablestring";
		case TYPE_VOID:
			return "void";
		case TYPE_XML:
			return "xml";
		case TYPE_LOCALE:
			return "locale";
		case TYPE_TIMEZONE:
			return "timezone";
		case TYPE_FUNCTION:
			return "function";
		}
		return defaultValue;

	}

	public static short toShortStrict(String type, short defaultValue) {
		type = type.toLowerCase().trim();
		if (type.length() > 2) {
			char first = type.charAt(0);
			switch (first) {
			case 'a':
				if (type.equals("any")) return TYPE_ANY;
				if (type.equals("array")) return TYPE_ARRAY;
				break;
			case 'b':
				if (type.equals("boolean") || type.equals("bool")) return TYPE_BOOLEAN;
				if (type.equals("binary")) return TYPE_BINARY;

				break;
			case 'd':
				if (type.equals("date") || type.equals("datetime")) return TYPE_DATETIME;
			case 'f':
				if (type.equals("function")) return TYPE_FUNCTION;
				break;
			case 'g':
				if ("guid".equals(type)) return TYPE_GUID;
				break;
			case 'i':
				if ("image".equals(type)) return TYPE_IMAGE;
				break;
			case 'n':
				if (type.equals("numeric")) return TYPE_NUMERIC;
				else if (type.equals("number")) return TYPE_NUMERIC;
				break;
			case 'l':
				if (type.equals("locale")) return TYPE_LOCALE;
				break;
			case 'o':
				if (type.equals("object")) return TYPE_ANY;
				break;
			case 'q':
				if (type.equals("query")) return TYPE_QUERY;
				if (type.equals("querycolumn")) return TYPE_QUERY_COLUMN;
				break;
			case 's':
				if (type.equals("string")) return TYPE_STRING;
				else if (type.equals("struct")) return TYPE_STRUCT;
				break;
			case 't':
				if (type.equals("timespan")) return TYPE_TIMESPAN;
				if (type.equals("time")) return TYPE_DATETIME;
				if (type.equals("timestamp")) return TYPE_DATETIME;
				if (type.equals("timezone")) return TYPE_TIMEZONE;
				break;
			case 'u':
				if (type.equals("uuid")) return TYPE_UUID;
				break;
			case 'v':
				if (type.equals("variablename")) return TYPE_VARIABLE_NAME;
				if (type.equals("variable_name")) return TYPE_VARIABLE_NAME;
				if (type.equals("variablestring")) return TYPE_VARIABLE_STRING;
				if (type.equals("variable_string")) return TYPE_VARIABLE_STRING;
				if (type.equals("void")) return TYPE_VOID;
				break;
			case 'x':
				if (type.equals("xml")) return TYPE_XML;
				break;
			}
		}
		return defaultValue;
	}

	public static short toShort(String type, boolean alsoAlias, short defaultValue) {
		type = type.toLowerCase().trim();
		if (type.length() > 2) {
			char first = type.charAt(0);
			switch (first) {
			case 'a':
				if (type.equals("any")) return TYPE_ANY;
				if (type.equals("array")) return TYPE_ARRAY;
				break;
			case 'b':
				if (type.equals("boolean") || (alsoAlias && type.equals("bool"))) return TYPE_BOOLEAN;
				if (type.equals("binary")) return TYPE_BINARY;
				if (alsoAlias && type.equals("bigint")) return TYPE_NUMERIC;
				if ("base64".equals(type)) return TYPE_STRING;

				break;
			case 'c':
				if (alsoAlias && "char".equals(type)) return TYPE_STRING;

				break;
			case 'd':
				if (alsoAlias && "double".equals(type)) return TYPE_NUMERIC;
				if (alsoAlias && "decimal".equals(type)) return TYPE_STRING;
				if (type.equals("date") || type.equals("datetime")) return TYPE_DATETIME;
				break;

			case 'e':
				if ("eurodate".equals(type)) return TYPE_DATETIME;
				break;
			case 'f':
				if (alsoAlias && "float".equals(type)) return TYPE_NUMERIC;
				if ("function".equals(type)) return TYPE_FUNCTION;
				break;
			case 'g':
				if ("guid".equals(type)) return TYPE_GUID;
				break;

			case 'i':
				if (alsoAlias && ("int".equals(type) || "integer".equals(type))) return TYPE_NUMERIC;
				// conflicts with component named Image if("image".equals(type)) return TYPE_IMAGE;
				break;

			case 'l':
				if (alsoAlias && "long".equals(type)) return TYPE_NUMERIC;
				if ("locale".equals(type)) return TYPE_LOCALE;
				break;

			case 'n':
				if (type.equals("numeric")) return TYPE_NUMERIC;
				else if (type.equals("number")) return TYPE_NUMERIC;
				if (alsoAlias) {
					if (type.equals("node")) return TYPE_XML;
					else if (type.equals("nvarchar")) return TYPE_STRING;
					else if (type.equals("nchar")) return TYPE_STRING;
				}
				break;
			case 'o':
				if (type.equals("object")) return TYPE_ANY;
				if (alsoAlias && type.equals("other")) return TYPE_ANY;
				break;
			case 'q':
				if (type.equals("query")) return TYPE_QUERY;
				if (type.equals("querycolumn")) return TYPE_QUERY_COLUMN;
				break;
			case 's':
				if (type.equals("string")) return TYPE_STRING;
				else if (type.equals("struct")) return TYPE_STRUCT;
				if (alsoAlias && "short".equals(type)) return TYPE_NUMERIC;
				break;
			case 't':
				if (type.equals("timespan")) return TYPE_TIMESPAN;
				if (type.equals("timezone")) return TYPE_TIMEZONE;
				if (type.equals("time")) return TYPE_DATETIME;
				if (alsoAlias && type.equals("timestamp")) return TYPE_DATETIME;
				if (alsoAlias && type.equals("text")) return TYPE_STRING;
				break;
			case 'u':
				if (type.equals("uuid")) return TYPE_UUID;
				if (alsoAlias && "usdate".equals(type)) return TYPE_DATETIME;
				if (alsoAlias && "udf".equals(type)) return TYPE_FUNCTION;
				break;
			case 'v':
				if (type.equals("variablename")) return TYPE_VARIABLE_NAME;
				if (alsoAlias && type.equals("variable_name")) return TYPE_VARIABLE_NAME;

				if (type.equals("variablestring")) return TYPE_VARIABLE_STRING;
				if (alsoAlias && type.equals("variable_string")) return TYPE_VARIABLE_STRING;

				if (type.equals("void")) return TYPE_VOID;
				if (alsoAlias && type.equals("varchar")) return TYPE_STRING;
				break;
			case 'x':
				if (type.equals("xml")) return TYPE_XML;
				break;
			}
		}
		return defaultValue;
	}

	public static boolean isSimpleType(short type) {
		return type == TYPE_BOOLEAN || type == TYPE_DATETIME || type == TYPE_NUMERIC || type == TYPE_STRING;
	}
}