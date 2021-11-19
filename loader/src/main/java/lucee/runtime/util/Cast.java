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
package lucee.runtime.util;

import java.awt.Color;
import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import lucee.commons.io.res.Resource;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.TimeSpan;

/**
 * This class can cast object of one type to another by CFML rules
 */
public interface Cast {

	/**
	 * cast a boolean value to a boolean value (do nothing)
	 * 
	 * @param b boolean value to cast
	 * @return casted boolean value
	 */
	public boolean toBooleanValue(boolean b);

	/**
	 * cast a double value to a boolean value (primitive value type)
	 * 
	 * @param d double value to cast
	 * @return casted boolean value
	 */
	public boolean toBooleanValue(double d);

	/**
	 * cast a double value to a boolean value (primitive value type)
	 * 
	 * @param c char value to cast
	 * @return casted boolean value
	 */
	public boolean toBooleanValue(char c);

	/**
	 * cast an Object to a boolean value (primitive value type)
	 * 
	 * @param o Object to cast
	 * @return casted boolean value
	 * @throws PageException Page Exception
	 */
	public boolean toBooleanValue(Object o) throws PageException;

	/**
	 * cast an Object to a Double Object (reference Type)
	 * 
	 * @param o Object to cast
	 * @return casted Double Object
	 * @throws PageException Page Exception
	 */
	public Double toDouble(Object o) throws PageException;

	/**
	 * cast an Object to a Double Object (reference Type)
	 * 
	 * @param o Object to cast
	 * @param defaultValue Default Value
	 * @return casted Double Object
	 */
	public Double toDouble(Object o, Double defaultValue);

	/**
	 * cast a String to a Double Object (reference Type)
	 * 
	 * @param str String to cast
	 * @return casted Double Object
	 * @throws PageException Page Exception
	 */
	public Double toDouble(String str) throws PageException;

	/**
	 * cast a String to a Double Object (reference Type)
	 * 
	 * @param str String to cast
	 * @param defaultValue Default Value
	 * @return casted Double Object
	 */
	public Double toDouble(String str, Double defaultValue);

	/**
	 * cast a double value to a Double Object (reference Type)
	 * 
	 * @param d double value to cast
	 * @return casted Double Object
	 */
	public Double toDouble(double d);

	/**
	 * cast a boolean value to a Double Object (reference Type)
	 * 
	 * @param b boolean value to cast
	 * @return casted Double Object
	 */
	public Double toDouble(boolean b);

	/**
	 * cast a char value to a Double Object (reference Type)
	 * 
	 * @param c char value to cast
	 * @return casted Double Object
	 */
	public Double toDouble(char c);

	/**
	 * cast an Object to a double value (primitive value Type)
	 * 
	 * @param o Object to cast
	 * @return casted double value
	 * @throws PageException Page Exception
	 */
	public double toDoubleValue(Object o) throws PageException;

	/**
	 * cast an Object to a double value (primitive value Type)
	 * 
	 * @param str String to cast
	 * @return casted double value
	 * @throws PageException Page Exception
	 */
	public double toDoubleValue(String str) throws PageException;

	/**
	 * cast an Object to a double value (primitive value Type)
	 * 
	 * @param o Object to cast
	 * @param defaultValue if can't cast return this value
	 * @return casted double value
	 */
	public double toDoubleValue(Object o, double defaultValue);

	/**
	 * cast an Object to a double value (primitive value Type), if can't return Double.NaN
	 * 
	 * @param str String to cast
	 * @param defaultValue if can't cast return this value
	 * @return casted double value
	 */
	public double toDoubleValue(String str, double defaultValue);

	/**
	 * cast a double value to a double value (do nothing)
	 * 
	 * @param d double value to cast
	 * @return casted double value
	 */
	public double toDoubleValue(double d);

	/**
	 * cast a boolean value to a double value (primitive value type)
	 * 
	 * @param b boolean value to cast
	 * @return casted double value
	 */
	public double toDoubleValue(boolean b);

	/**
	 * cast a char value to a double value (primitive value type)
	 * 
	 * @param c char value to cast
	 * @return casted double value
	 */
	public double toDoubleValue(char c);

	/**
	 * cast an Object to an int value (primitive value type)
	 * 
	 * @param o Object to cast
	 * @return casted int value
	 * @throws PageException Page Exception
	 */
	public int toIntValue(Object o) throws PageException;

	/**
	 * cast an Object to an int value (primitive value type)
	 * 
	 * @param o Object to cast
	 * @param defaultValue Default Value
	 * @return casted int value
	 */
	public int toIntValue(Object o, int defaultValue);

	/**
	 * cast a String to an int value (primitive value type)
	 * 
	 * @param str String to cast
	 * @return casted int value
	 * @throws PageException Page Exception
	 */
	public int toIntValue(String str) throws PageException;

	/**
	 * cast an Object to a double value (primitive value Type), if can't return Integer.MIN_VALUE
	 * 
	 * @param str String to cast
	 * @param defaultValue Default Value
	 * @return casted double value
	 */
	public int toIntValue(String str, int defaultValue);

	/**
	 * cast a double value to an int value (primitive value type)
	 * 
	 * @param d double value to cast
	 * @return casted int value
	 */
	public int toIntValue(double d);

	/**
	 * cast a boolean value to an int value (primitive value type)
	 * 
	 * @param b boolean value to cast
	 * @return casted int value
	 */
	public int toIntValue(boolean b);

	/**
	 * cast a char value to an int value (primitive value type)
	 * 
	 * @param c char value to cast
	 * @return casted int value
	 */
	public int toIntValue(char c);

	/**
	 * cast a double to a decimal value (String:xx.xx)
	 * 
	 * @param value Object to cast
	 * @return casted decimal value
	 * @throws PageException Page Exception
	 */
	public String toDecimal(Object value) throws PageException;

	/**
	 * cast a double to a decimal value (String:xx.xx)
	 * 
	 * @param value Object to cast
	 * @param defaultValue Default Value
	 * @return casted decimal value
	 */
	public String toDecimal(Object value, String defaultValue);

	/**
	 * cast a char to a decimal value (String:xx.xx)
	 * 
	 * @param c char to cast
	 * @return casted decimal value
	 */
	public String toDecimal(char c);

	/**
	 * cast a boolean to a decimal value (String:xx.xx)
	 * 
	 * @param b boolean to cast
	 * @return casted decimal value
	 */
	public String toDecimal(boolean b);

	/**
	 * cast a double to a decimal value (String:xx.xx)
	 * 
	 * @param d double to cast
	 * @return casted decimal value
	 */
	public String toDecimal(double d);

	/**
	 * cast a boolean value to a Boolean Object(reference type)
	 * 
	 * @param b boolean value to cast
	 * @return casted Boolean Object
	 */
	public Boolean toBoolean(boolean b);

	/**
	 * cast a char value to a Boolean Object(reference type)
	 * 
	 * @param c char value to cast
	 * @return casted Boolean Object
	 */
	public Boolean toBoolean(char c);

	/**
	 * cast a double value to a Boolean Object(reference type)
	 * 
	 * @param d double value to cast
	 * @return casted Boolean Object
	 */
	public Boolean toBoolean(double d);

	/**
	 * cast an Object to a Boolean Object(reference type)
	 * 
	 * @param o Object to cast
	 * @return casted Boolean Object
	 * @throws PageException Page Exception
	 */
	public Boolean toBoolean(Object o) throws PageException;

	/**
	 * cast an Object to a Boolean Object(reference type)
	 * 
	 * @param str String to cast
	 * @return casted Boolean Object
	 * @throws PageException Page Exception
	 */
	public Boolean toBoolean(String str) throws PageException;

	/**
	 * cast an Object to a boolean value (primitive value type), Exception Less
	 * 
	 * @param o Object to cast
	 * @param defaultValue Default Value
	 * @return casted boolean value
	 */
	public boolean toBooleanValue(Object o, boolean defaultValue);

	/**
	 * cast an Object to a boolean value (reference type), Exception Less
	 * 
	 * @param o Object to cast
	 * @param defaultValue default value
	 * @return casted boolean reference
	 */
	public Boolean toBoolean(Object o, Boolean defaultValue);

	/**
	 * cast an Object to a boolean value (reference type), Exception Less
	 * 
	 * @param str String to cast
	 * @param defaultValue default value
	 * @return casted boolean reference
	 */
	public Boolean toBoolean(String str, Boolean defaultValue);

	/**
	 * cast a boolean value to a char value
	 * 
	 * @param b boolean value to cast
	 * @return casted char value
	 */
	public char toCharValue(boolean b);

	/**
	 * cast a double value to a char value (primitive value type)
	 * 
	 * @param d double value to cast
	 * @return casted char value
	 */
	public char toCharValue(double d);

	/**
	 * cast a char value to a char value (do nothing)
	 * 
	 * @param c char value to cast
	 * @return casted char value
	 */
	public char toCharValue(char c);

	/**
	 * cast an Object to a char value (primitive value type)
	 * 
	 * @param o Object to cast
	 * @return casted char value
	 * @throws PageException Page Exception
	 */
	public char toCharValue(Object o) throws PageException;

	/**
	 * cast an Object to a char value (primitive value type)
	 * 
	 * @param o Object to cast
	 * @param defaultValue Default Value
	 * @return casted char value
	 */
	public char toCharValue(Object o, char defaultValue);

	/**
	 * cast a boolean value to a Character Object(reference type)
	 * 
	 * @param b boolean value to cast
	 * @return casted Character Object
	 */
	public Character toCharacter(boolean b);

	/**
	 * cast a char value to a Character Object(reference type)
	 * 
	 * @param c char value to cast
	 * @return casted Character Object
	 */
	public Character toCharacter(char c);

	/**
	 * cast a double value to a Character Object(reference type)
	 * 
	 * @param d double value to cast
	 * @return casted Character Object
	 */
	public Character toCharacter(double d);

	/**
	 * cast an Object to a Character Object(reference type)
	 * 
	 * @param o Object to cast
	 * @param defaultValue Default Value
	 * @return casted Character Object
	 */
	public Character toCharacter(Object o, Character defaultValue);

	/**
	 * cast an Object to a Character Object(reference type)
	 * 
	 * @param o Object to cast
	 * @return casted Character Object
	 * @throws PageException Page Exception
	 */
	public Character toCharacter(Object o) throws PageException;

	/**
	 * cast a boolean value to a byte value
	 * 
	 * @param b boolean value to cast
	 * @return casted byte value
	 */
	public byte toByteValue(boolean b);

	/**
	 * cast a double value to a byte value (primitive value type)
	 * 
	 * @param d double value to cast
	 * @return casted byte value
	 */
	public byte toByteValue(double d);

	/**
	 * cast a char value to a byte value (do nothing)
	 * 
	 * @param c char value to cast
	 * @return casted byte value
	 */
	public byte toByteValue(char c);

	/**
	 * cast an Object to a byte value (primitive value type)
	 * 
	 * @param o Object to cast
	 * @return casted byte value
	 * @throws PageException Page Exception
	 */
	public byte toByteValue(Object o) throws PageException;

	/**
	 * cast an Object to a byte value (primitive value type)
	 * 
	 * @param o Object to cast
	 * @param defaultValue Default Value
	 * @return casted byte value
	 */
	public byte toByteValue(Object o, byte defaultValue);

	/**
	 * cast a boolean value to a Byte Object(reference type)
	 * 
	 * @param b boolean value to cast
	 * @return casted Byte Object
	 */
	public Byte toByte(boolean b);

	/**
	 * cast a char value to a Byte Object(reference type)
	 * 
	 * @param c char value to cast
	 * @return casted Byte Object
	 */
	public Byte toByte(char c);

	/**
	 * cast a double value to a Byte Object(reference type)
	 * 
	 * @param d double value to cast
	 * @return casted Byte Object
	 */
	public Byte toByte(double d);

	/**
	 * cast an Object to a Byte Object(reference type)
	 * 
	 * @param o Object to cast
	 * @return casted Byte Object
	 * @throws PageException Page Exception
	 */
	public Byte toByte(Object o) throws PageException;

	/**
	 * cast an Object to a Byte Object(reference type)
	 * 
	 * @param o Object to cast
	 * @param defaultValue Default Value
	 * @return casted Byte Object
	 */
	public Byte toByte(Object o, Byte defaultValue);

	/**
	 * cast a boolean value to a long value
	 * 
	 * @param b boolean value to cast
	 * @return casted long value
	 */
	public long toLongValue(boolean b);

	/**
	 * cast a double value to a long value (primitive value type)
	 * 
	 * @param d double value to cast
	 * @return casted long value
	 */
	public long toLongValue(double d);

	/**
	 * cast a char value to a long value (do nothing)
	 * 
	 * @param c char value to cast
	 * @return casted long value
	 */
	public long toLongValue(char c);

	/**
	 * cast an Object to a long value (primitive value type)
	 * 
	 * @param o Object to cast
	 * @return casted long value
	 * @throws PageException Page Exception
	 */
	public long toLongValue(Object o) throws PageException;

	/**
	 * cast an Object to a long value (primitive value type)
	 * 
	 * @param o Object to cast
	 * @param defaultValue Default Value
	 * @return casted long value
	 */
	public long toLongValue(Object o, long defaultValue);

	/**
	 * cast a boolean value to a Long Object(reference type)
	 * 
	 * @param b boolean value to cast
	 * @return casted Long Object
	 */
	public Long toLong(boolean b);

	/**
	 * cast a char value to a Long Object(reference type)
	 * 
	 * @param c char value to cast
	 * @return casted Long Object
	 */
	public Long toLong(char c);

	/**
	 * cast a double value to a Long Object(reference type)
	 * 
	 * @param d double value to cast
	 * @return casted Long Object
	 */
	public Long toLong(double d);

	/**
	 * cast an Object to a Long Object(reference type)
	 * 
	 * @param o Object to cast
	 * @return casted Long Object
	 * @throws PageException Page Exception
	 */
	public Long toLong(Object o) throws PageException;

	/**
	 * cast an Object to a Long Object(reference type)
	 * 
	 * @param o Object to cast
	 * @param defaultValue Default Value
	 * @return casted Long Object
	 */
	public Long toLong(Object o, Long defaultValue);

	public Collection.Key toKey(Object o) throws PageException;

	public Collection.Key toKey(String str);

	public Collection.Key toKey(Object o, Collection.Key defaultValue);

	/**
	 * cast a boolean value to a short value
	 * 
	 * @param b boolean value to cast
	 * @return casted short value
	 */
	public short toShortValue(boolean b);

	/**
	 * cast a double value to a short value (primitive value type)
	 * 
	 * @param d double value to cast
	 * @return casted short value
	 */
	public short toShortValue(double d);

	/**
	 * cast a char value to a short value (do nothing)
	 * 
	 * @param c char value to cast
	 * @return casted short value
	 */
	public short toShortValue(char c);

	/**
	 * cast an Object to a short value (primitive value type)
	 * 
	 * @param o Object to cast
	 * @return casted short value
	 * @throws PageException Page Exception
	 */
	public short toShortValue(Object o) throws PageException;

	/**
	 * cast an Object to a short value (primitive value type)
	 * 
	 * @param o Object to cast
	 * @param defaultValue Default Value
	 * @return casted short value
	 */
	public short toShortValue(Object o, short defaultValue);

	/**
	 * cast a boolean value to a Short Object(reference type)
	 * 
	 * @param b boolean value to cast
	 * @return casted Short Object
	 */
	public Short toShort(boolean b);

	/**
	 * cast a char value to a Short Object(reference type)
	 * 
	 * @param c char value to cast
	 * @return casted Short Object
	 */
	public Short toShort(char c);

	/**
	 * cast a double value to a Byte Object(reference type)
	 * 
	 * @param d double value to cast
	 * @return casted Byte Object
	 */
	public Short toShort(double d);

	/**
	 * cast an Object to a Short Object(reference type)
	 * 
	 * @param o Object to cast
	 * @return casted Short Object
	 * @throws PageException Page Exception
	 */
	public Short toShort(Object o) throws PageException;

	/**
	 * cast an Object to a Short Object(reference type)
	 * 
	 * @param o Object to cast
	 * @param defaultValue Default Value
	 * @return casted Short Object
	 */
	public Short toShort(Object o, Short defaultValue);

	/**
	 * cast a String to a boolean value (primitive value type)
	 * 
	 * @param str String to cast
	 * @return casted boolean value
	 * @throws PageException Page Exception
	 */
	public boolean toBooleanValue(String str) throws PageException;

	/**
	 * cast a String to a boolean value (primitive value type), return 1 for true, 0 for false and -1 if
	 * can't cast to a boolean type
	 * 
	 * @param str String to cast
	 * @param defaultValue Default Value
	 * @return casted boolean value
	 */
	public boolean toBooleanValue(String str, boolean defaultValue);

	/**
	 * cast an Object to a String
	 * 
	 * @param o Object to cast
	 * @return casted String
	 * @throws PageException Page Exception
	 */
	public String toString(Object o) throws PageException;

	/**
	 * cast an Object to a String dont throw an exception, if can't cast to a string return an empty string
	 * 
	 * @param o Object to cast
	 * @param defaultValue Default Value
	 * @return casted String
	 */
	public String toString(Object o, String defaultValue);

	/**
	 * cast a double value to a String
	 * 
	 * @param d double value to cast
	 * @return casted String
	 */
	public String toString(double d);

	/**
	 * cast a long value to a String
	 * 
	 * @param l long value to cast
	 * @return casted String
	 */
	public String toString(long l);

	/**
	 * cast an int value to a String
	 * 
	 * @param i int value to cast
	 * @return casted String
	 */
	public String toString(int i);

	/**
	 * cast a boolean value to a String
	 * 
	 * @param b boolean value to cast
	 * @return casted String
	 */
	public String toString(boolean b);

	/**
	 * cast an Object to an Array Object
	 * 
	 * @param o Object to cast
	 * @return casted Array
	 * @throws PageException Page Exception
	 */
	public List toList(Object o) throws PageException;

	/**
	 * cast an Object to an Array Object
	 * 
	 * @param o Object to cast
	 * @param defaultValue Default Value
	 * @return casted Array
	 */
	public List toList(Object o, List defaultValue);

	/**
	 * cast an Object to an Array Object
	 * 
	 * @param o Object to cast
	 * @param duplicate duplicate
	 * @return casted Array
	 * @throws PageException Page Exception
	 */
	public List toList(Object o, boolean duplicate) throws PageException;

	/**
	 * cast an Object to an Array Object
	 * 
	 * @param o Object to cast
	 * @param duplicate duplicate
	 * @param defaultValue Default Value
	 * @return casted Array
	 */
	public List toList(Object o, boolean duplicate, List defaultValue);

	/**
	 * cast an Object to an Array Object
	 * 
	 * @param obj Object to cast
	 * @return casted Array
	 * @throws PageException Page Exception
	 */
	public Array toArray(Object obj) throws PageException;

	/**
	 * cast an Object to an Array Object
	 * 
	 * @param obj Object to cast
	 * @param defaultValue Default Value
	 * @return casted Array
	 */
	public Array toArray(Object obj, Array defaultValue);

	/**
	 * cast an Object to a "native" Java Array
	 * 
	 * @param obj Object to cast
	 * @return casted Array
	 * @throws PageException Page Exception
	 */
	public Object[] toNativeArray(Object obj) throws PageException;

	/**
	 * cast an Object to a "native" Java Array
	 * 
	 * @param obj Object to cast
	 * @param defaultValue Default Value
	 * @return casted Array
	 */
	public Object[] toNativeArray(Object obj, Object[] defaultValue);

	/**
	 * cast an Object to a Map Object
	 * 
	 * @param o Object to cast
	 * @return casted Struct
	 * @throws PageException Page Exception
	 */
	public Map<?, ?> toMap(Object o) throws PageException;

	/**
	 * cast an Object to a Map Object
	 * 
	 * @param o Object to cast
	 * @param defaultValue Default Value
	 * @return casted Struct
	 */
	public Map toMap(Object o, Map defaultValue);

	/**
	 * cast an Object to a Map Object
	 * 
	 * @param o Object to cast
	 * @param duplicate duplicate
	 * @return casted Struct
	 * @throws PageException Page Exception
	 */
	public Map toMap(Object o, boolean duplicate) throws PageException;

	/**
	 * cast an Object to a Map Object
	 * 
	 * @param o Object to cast
	 * @param duplicate duplicate
	 * @param defaultValue Default Value
	 * @return casted Struct
	 */
	public Map toMap(Object o, boolean duplicate, Map defaultValue);

	/**
	 * cast an Object to a Struct Object
	 * 
	 * @param o Object to cast
	 * @return casted Struct
	 * @throws PageException Page Exception
	 */
	public Struct toStruct(Object o) throws PageException;

	/**
	 * cast an Object to a Struct Object
	 * 
	 * @param o Object to cast
	 * @param defaultValue Default Value
	 * @return casted Struct
	 */
	public Struct toStruct(Object o, Struct defaultValue);

	public Struct toStruct(Object o, Struct defaultValue, boolean caseSensitive);

	/**
	 * cast an Object to a Binary
	 * 
	 * @param obj Object to cast
	 * @return casted Binary
	 * @throws PageException Page Exception
	 */
	public byte[] toBinary(Object obj) throws PageException;

	/**
	 * cast an Object to a Binary
	 * 
	 * @param obj Object to cast
	 * @param defaultValue Default Value
	 * @return casted Binary
	 */
	public byte[] toBinary(Object obj, byte[] defaultValue);

	/**
	 * cast an Object to a Base64 value
	 * 
	 * @param o Object to cast
	 * @return to Base64 String
	 * @throws PageException Page Exception
	 */
	public String toBase64(Object o) throws PageException;

	/**
	 * cast an Object to a Base64 value
	 * 
	 * @param o Object to cast
	 * @param defaultValue Default Value
	 * @return to Base64 String
	 */
	public String toBase64(Object o, String defaultValue);

	/**
	 * cast a boolean to a DateTime Object
	 * 
	 * @param b boolean to cast
	 * @param tz timezone
	 * @return casted DateTime Object
	 */
	public DateTime toDate(boolean b, TimeZone tz);

	/**
	 * cast a char to a DateTime Object
	 * 
	 * @param c char to cast
	 * @param tz timezone
	 * @return casted DateTime Object
	 */
	public DateTime toDate(char c, TimeZone tz);

	/**
	 * cast a double to a DateTime Object
	 * 
	 * @param d double to cast
	 * @param tz timezone
	 * @return casted DateTime Object
	 */
	public DateTime toDate(double d, TimeZone tz);

	/**
	 * cast an Object to a DateTime Object
	 * 
	 * @param o Object to cast
	 * @param tz timezone
	 * @return casted DateTime Object
	 * @throws PageException Page Exception
	 */
	public DateTime toDate(Object o, TimeZone tz) throws PageException;

	/**
	 * cast an Object to a DateTime Object
	 * 
	 * @param str String to cast
	 * @param tz timezone
	 * @return casted DateTime Object
	 * @throws PageException Page Exception
	 */
	public DateTime toDate(String str, TimeZone tz) throws PageException;

	/**
	 * cast an Object to a DateTime Object
	 * 
	 * @param o Object to cast
	 * @param alsoNumbers define if also numbers will casted to a datetime value
	 * @param tz timezone
	 * @param defaultValue Default Value
	 * @return casted DateTime Object
	 */
	public DateTime toDate(Object o, boolean alsoNumbers, TimeZone tz, DateTime defaultValue);

	/**
	 * cast an Object to a DateTime Object
	 * 
	 * @param str String to cast
	 * @param alsoNumbers define if also numbers will casted to a datetime value
	 * @param tz timezone
	 * @param defaultValue Default Value
	 * @return casted DateTime Object
	 */
	public DateTime toDate(String str, boolean alsoNumbers, TimeZone tz, DateTime defaultValue);

	/**
	 * cast an Object to a DateTime Object
	 * 
	 * @param o Object to cast
	 * @param tz timezone
	 * @return casted DateTime Object
	 * @throws PageException Page Exception
	 */
	public DateTime toDateTime(Object o, TimeZone tz) throws PageException;

	/**
	 * cast an Object to a DateTime Object
	 * 
	 * @param o Object to cast
	 * @param tz timezone
	 * @param defaultValue Default Value
	 * @return casted DateTime Object
	 */
	public DateTime toDateTime(Object o, TimeZone tz, DateTime defaultValue);

	/**
	 * cast an Object to a DateTime Object (alias for toDateTime)
	 * 
	 * @param o Object to cast
	 * @param tz timezone
	 * @return casted DateTime Object
	 * @throws PageException Page Exception
	 */
	@Deprecated
	public DateTime toDatetime(Object o, TimeZone tz) throws PageException;

	/**
	 * parse a string to a Datetime Object
	 * 
	 * @param locale locale
	 * @param str String representation of a locale Date
	 * @param tz timezone
	 * @return DateTime Object
	 * @throws PageException Page Exception
	 */
	public DateTime toDate(Locale locale, String str, TimeZone tz) throws PageException;

	/**
	 * parse a string to a Datetime Object, returns null if can't convert
	 * 
	 * @param locale locale
	 * @param str String representation of a locale Date
	 * @param tz timezone
	 * @param defaultValue Default Value
	 * @return datetime object
	 */
	public DateTime toDate(Locale locale, String str, TimeZone tz, DateTime defaultValue);

	/**
	 * cast an Object to a Query Object
	 * 
	 * @param o Object to cast
	 * @return casted Query Object
	 * @throws PageException Page Exception
	 */
	public Query toQuery(Object o) throws PageException;

	/**
	 * cast an Object to a Query Object
	 * 
	 * @param o Object to cast
	 * @param defaultValue Default Value
	 * @return casted Query Object
	 */
	public Query toQuery(Object o, Query defaultValue);

	/**
	 * cast an Object to a Query Object
	 * 
	 * @param o Object to cast
	 * @param duplicate duplicate the object or not
	 * @return casted Query Object
	 * @throws PageException Page Exception
	 */
	public Query toQuery(Object o, boolean duplicate) throws PageException;

	/**
	 * cast an Object to a Query Object
	 * 
	 * @param o Object to cast
	 * @param duplicate duplicate the object or not
	 * @param defaultValue Default Value
	 * @return casted Query Object
	 */
	public Query toQuery(Object o, boolean duplicate, Query defaultValue);

	/**
	 * cast an Object to an UUID
	 * 
	 * @param o Object to cast
	 * @return casted Query Object
	 * @throws PageException Page Exception
	 */
	public Object toUUId(Object o) throws PageException;

	/**
	 * cast an Object to an UUID
	 * 
	 * @param o Object to cast
	 * @param defaultValue Default Value
	 * @return casted Query Object
	 */
	public Object toUUId(Object o, Object defaultValue);

	/**
	 * cast an Object to a Variable Name
	 * 
	 * @param o Object to cast
	 * @return casted Variable Name
	 * @throws PageException Page Exception
	 */
	public String toVariableName(Object o) throws PageException;

	/**
	 * cast an Object to a Variable Name
	 * 
	 * @param obj Object to cast
	 * @param defaultValue Default Value
	 * @return casted Variable Name
	 */
	public String toVariableName(Object obj, String defaultValue);

	@Deprecated
	public Object toVariableName(Object obj, Object defaultValue);

	/**
	 * cast an Object to a TimeSpan Object (alias for toTimeSpan)
	 * 
	 * @param o Object to cast
	 * @return casted TimeSpan Object
	 * @throws PageException Page Exception
	 */
	public TimeSpan toTimespan(Object o) throws PageException;

	/**
	 * cast an Object to a TimeSpan Object (alias for toTimeSpan)
	 * 
	 * @param o Object to cast
	 * @param defaultValue Default Value
	 * @return casted TimeSpan Object
	 */
	public TimeSpan toTimespan(Object o, TimeSpan defaultValue);

	/**
	 * convert milliseconds to a timespan
	 * 
	 * @param millis milliseconds to convert
	 * @return casted TimeSpan Object
	 */
	public TimeSpan toTimespan(long millis);

	/**
	 * cast a Throwable Object to a PageException Object
	 * 
	 * @param t Throwable to cast
	 * @return casted PageException Object
	 */
	public PageException toPageException(Throwable t);

	/**
	 * cast a Throwable Object to a PageRuntimeException Object (RuntimeException)
	 * 
	 * @param t Throwable to cast
	 * @return casted PageException Object
	 */
	public RuntimeException toPageRuntimeException(Throwable t);

	/**
	 * return the type name of an object (string, boolean, int aso.), type is not same like class name
	 * 
	 * @param o Object to get type from
	 * @return type of the object
	 */
	public String toTypeName(Object o);

	/**
	 * cast a value to a value defined by type argument
	 * 
	 * @param pc Page Context
	 * @param type type of the returning Value
	 * @param o Object to cast
	 * @return casted Value
	 * @throws PageException Page Exception
	 */
	public Object castTo(PageContext pc, String type, Object o) throws PageException;

	/**
	 * cast a value to a value defined by type argument
	 * 
	 * @param pc Page Context
	 * @param type type of the returning Value
	 * @param o Object to cast
	 * @param alsoPattern mean supporting also none real types like email or creditcard ...
	 * @return casted Value
	 * @throws PageException Page Exception
	 */
	public Object castTo(PageContext pc, String type, Object o, boolean alsoPattern) throws PageException;

	/**
	 * cast a value to a value defined by type argument
	 * 
	 * @param pc Page Context
	 * @param type type of the returning Value (Example: Cast.TYPE_QUERY)
	 * @param strType type as String
	 * @param o Object to cast
	 * @return casted Value
	 * @throws PageException Page Exception
	 */
	public Object castTo(PageContext pc, short type, String strType, Object o) throws PageException;

	/**
	 * cast a value to a value defined by type argument
	 * 
	 * @param pc Page Context
	 * @param type type of the returning Value (Example: Cast.TYPE_QUERY)
	 * @param o Object to cast
	 * @return casted Value
	 * @throws PageException Page Exception
	 */
	public Object castTo(PageContext pc, short type, Object o) throws PageException;

	/**
	 * cast a value to a value defined by type a class
	 * 
	 * @param pc Page Context
	 * @param trgClass class to generate
	 * @param obj Object to cast
	 * @return casted Value
	 * @throws PageException Page Exception
	 */
	public Object castTo(PageContext pc, Class trgClass, Object obj) throws PageException;

	/**
	 * cast a value to void (Empty String)
	 * 
	 * @param o Object to Cast
	 * @return void value
	 * @throws PageException Page Exception
	 */
	public Object toVoid(Object o) throws PageException;

	/**
	 * cast a value to void (Empty String)
	 * 
	 * @param o Object to cast
	 * @param defaultValue Default Value
	 * @return void value
	 */
	public Object toVoid(Object o, Object defaultValue);

	/**
	 * cast an Object to a reference type (Object), in that case this method to nothing, because an Object
	 * is already a reference type
	 * 
	 * @param o Object to cast
	 * @return casted Object
	 */
	public Object toRef(Object o);

	/**
	 * cast a String to a reference type (Object), in that case this method to nothing, because a String
	 * is already a reference type
	 * 
	 * @param o Object to cast
	 * @return casted Object
	 */
	public String toRef(String o);

	/**
	 * cast a Collection to a reference type (Object), in that case this method to nothing, because a
	 * Collection is already a reference type
	 * 
	 * @param o Collection to cast
	 * @return casted Object
	 */
	public Collection toRef(Collection o);

	/**
	 * cast a char value to his (CFML) reference type String
	 * 
	 * @param c char to cast
	 * @return casted String
	 */
	public String toRef(char c);

	/**
	 * cast a boolean value to his (CFML) reference type Boolean
	 * 
	 * @param b boolean to cast
	 * @return casted Boolean
	 */
	public Boolean toRef(boolean b);

	/**
	 * cast a byte value to his (CFML) reference type Boolean
	 * 
	 * @param b byte to cast
	 * @return casted Boolean
	 */
	public Byte toRef(byte b);

	/**
	 * cast a short value to his (CFML) reference type Integer
	 * 
	 * @param s short to cast
	 * @return casted Integer
	 */
	public Short toRef(short s);

	/**
	 * cast an int value to his (CFML) reference type Integer
	 * 
	 * @param i int to cast
	 * @return casted Integer
	 */
	public Integer toRef(int i);

	/**
	 * cast a float value to his (CFML) reference type Float
	 * 
	 * @param f float to cast
	 * @return casted Float
	 */
	public Float toRef(float f);

	/**
	 * cast a long value to his (CFML) reference type Long
	 * 
	 * @param l long to cast
	 * @return casted Long
	 */
	public Long toRef(long l);

	/**
	 * cast a double value to his (CFML) reference type Double
	 * 
	 * @param d double to cast
	 * @return casted Double
	 */
	public Double toRef(double d);

	/**
	 * cast an Object to an Iterator or get Iterator from Object
	 * 
	 * @param o Object to cast
	 * @return casted Collection
	 * @throws PageException Page Exception
	 */
	public Iterator toIterator(Object o) throws PageException;

	/**
	 * cast an Object to a Collection
	 * 
	 * @param o Object to cast
	 * @return casted Collection
	 * @throws PageException Page Exception
	 */
	public Collection toCollection(Object o) throws PageException;

	/**
	 * cast to a color object
	 * 
	 * @param o Object to cast
	 * @return Casted Color object
	 * @throws PageException Page Exception
	 */
	public Color toColor(Object o) throws PageException;

	/**
	 * cast an Object to a Collection, if not returns null
	 * 
	 * @param o Object to cast
	 * @param defaultValue Default Value
	 * @return casted Collection
	 */
	public Collection toCollection(Object o, Collection defaultValue);

	/**
	 * convert an object to a Resource
	 * 
	 * @param obj Object to Cast
	 * @return File
	 * @throws PageException Page Exception
	 */
	public Resource toResource(Object obj) throws PageException;

	/**
	 * convert an object to a Resource
	 * 
	 * @param obj Object to Cast
	 * @param defaultValue Default Value
	 * @return Resource
	 */
	public Resource toResource(Object obj, Resource defaultValue);

	/**
	 * convert an object to a File
	 * 
	 * @param obj Object to Cast
	 * @return File
	 * @throws PageException Page Exception
	 */
	public File toFile(Object obj) throws PageException;

	/**
	 * convert an object to a File
	 * 
	 * @param obj Object to Cast
	 * @param defaultValue Default Value
	 * @return File
	 */
	public File toFile(Object obj, File defaultValue);

	/**
	 * casts a string to a Locale
	 * 
	 * @param strLocale string
	 * @return Locale from String
	 * @throws PageException Page Exception
	 */
	public Locale toLocale(String strLocale) throws PageException;

	/**
	 * casts a string to a Locale
	 * 
	 * @param strLocale string
	 * @param defaultValue Default Value
	 * @return Locale from String
	 */
	public Locale toLocale(String strLocale, Locale defaultValue);

	/*
	 * * casts an Object to a Node List
	 * 
	 * @param o Object to Cast
	 * 
	 * @return NodeList from Object
	 * 
	 * @throws PageException Page Exception
	 */
	// public NodeList toNodeList(Object o) throws PageException;

	/*
	 * * casts an Object to a Node List
	 * 
	 * @param o Object to Cast
	 * 
	 * @param defaultValue Default Value
	 * 
	 * @return NodeList from Object
	 */
	// public NodeList toNodeList(Object o, NodeList defaultValue);

	/*
	 * * casts an Object to a XML Node
	 * 
	 * @param o Object to Cast
	 * 
	 * @return Node from Object
	 * 
	 * @throws PageException Page Exception
	 */
	// public Node toNode(Object o) throws PageException;

	/*
	 * * casts an Object to a XML Node
	 * 
	 * @param o Object to Cast
	 * 
	 * @param defaultValue Default Value
	 * 
	 * @return Node from Object
	 */
	// public Node toNode(Object o, Node defaultValue);

	/**
	 * casts a boolean to an Integer
	 * 
	 * @param b boolean value
	 * @return Integer from boolean
	 */
	public Integer toInteger(boolean b);

	/**
	 * casts a char to an Integer
	 * 
	 * @param c char value
	 * @return Integer from char
	 */
	public Integer toInteger(char c);

	/**
	 * casts a double to an Integer
	 * 
	 * @param d double value
	 * @return Integer from double
	 */
	public Integer toInteger(double d);

	/**
	 * casts an Object to an Integer
	 * 
	 * @param o Object to cast to Integer
	 * @return Integer from Object
	 * @throws PageException Page Exception
	 */
	public Integer toInteger(Object o) throws PageException;

	/**
	 * casts an Object to an Integer
	 * 
	 * @param o Object to cast to Integer
	 * @param defaultValue Default Value
	 * @return Integer from Object
	 */
	public Integer toInteger(Object o, Integer defaultValue);

	/**
	 * casts an Object to null
	 * 
	 * @param value value
	 * @return to null from Object
	 * @throws PageException Page Exception
	 */
	public Object toNull(Object value) throws PageException;

	public Float toFloat(Object o) throws PageException;

	public Float toFloat(Object o, Float defaultValue);

	public float toFloatValue(Object o) throws PageException;

	public float toFloatValue(Object o, float defaultValue);

	/**
	 * casts an Object to null
	 * 
	 * @param value value
	 * @param defaultValue Default Value
	 * @return to null from Object
	 */
	public Object toNull(Object value, Object defaultValue);

	/*
	 * * cast Object to a XML Node
	 * 
	 * @param value value
	 * 
	 * @return XML Node
	 * 
	 * @throws PageException Page Exception
	 */
	// public Node toXML(Object value) throws PageException;

	/*
	 * * cast Object to a XML Node
	 * 
	 * @param value value
	 * 
	 * @param defaultValue Default Value
	 * 
	 * @return XML Node
	 */
	// public Node toXML(Object value, Node defaultValue);

	/**
	 * cast to given type
	 * 
	 * @param type Object type
	 * @param o Object
	 * @param alsoPattern also Pattern
	 * @return Object casted to Type
	 * @throws PageException Page Exception
	 */
	public Object to(String type, Object o, boolean alsoPattern) throws PageException;

	/**
	 * cast Object to a Serializable Object
	 * 
	 * @param obj Object to Cast
	 * @return Serializable Object
	 * @throws PageException Page Exception
	 */
	public Serializable toSerializable(Object obj) throws PageException;

	/**
	 * cast Object to a Serializable Object
	 * 
	 * @param object Object to Cast
	 * @param defaultValue Default Value
	 * @return Returns a Serializable Object.
	 */
	public Serializable toSerializable(Object object, Serializable defaultValue);

	public Charset toCharset(String str) throws PageException;

	public Charset toCharset(String str, Charset defaultValue);

	public BigDecimal toBigDecimal(Object obj) throws PageException;

	public BigDecimal toBigDecimal(Object obj, BigDecimal defaultValue);

	public Component toComponent(Object obj) throws PageException;

	public Component toComponent(Object obj, Component defaultValue);

	public TimeZone toTimeZone(Object obj) throws PageException;

	public TimeZone toTimeZone(Object obj, TimeZone defaultValue);

	public Calendar toCalendar(long time, TimeZone timeZone, Locale locale);

	public DumpData toDumpTable(Struct sct, String title, PageContext pageContext, int maxlevel, DumpProperties dp);

	// FUTURE
	// public Credentials toCredentials(String username, String password);

}