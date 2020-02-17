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

import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;

/**
 * Object to test if an Object is a specific type
 */
public interface Decision {

	public boolean isAnyType(String type);

	/**
	 * tests if value is a simple value (Number,String,Boolean,Date,Printable)
	 * 
	 * @param value value to test
	 * @return is value a simple value
	 */
	public boolean isSimpleValue(Object value);

	/**
	 * tests if value is Numeric
	 * 
	 * @param value value to test
	 * @return is value numeric
	 */
	public boolean isNumber(Object value);

	/**
	 * tests if String value is Numeric
	 * 
	 * @param str value to test
	 * @return is value numeric
	 */
	public boolean isNumber(String str);

	/**
	 * @deprecated use insteas isNumber
	 */
	@Deprecated
	public boolean isNumeric(Object value);

	/**
	 * @deprecated use insteas isNumber
	 */
	@Deprecated
	public boolean isNumeric(String str);

	/**
	 * tests if String value is Hex Value
	 * 
	 * @param str value to test
	 * @return is value numeric
	 */
	public boolean isHex(String str);

	/**
	 * tests if String value is UUID Value
	 * 
	 * @param str value to test
	 * @return is value numeric
	 */
	public boolean isUUID(String str);

	/**
	 * tests if value is a Boolean (Numbers are not accepted)
	 * 
	 * @param value value to test
	 * @return is value boolean
	 */
	public boolean isBoolean(Object value);

	/**
	 * tests if value is a Boolean
	 * 
	 * @param str value to test
	 * @return is value boolean
	 */
	public boolean isBoolean(String str);

	/**
	 * tests if value is DateTime Object
	 * 
	 * @param value value to test
	 * @param alsoNumbers interpret also a number as date
	 * @return is value a DateTime Object
	 */
	public boolean isDate(Object value, boolean alsoNumbers);

	/**
	 * tests if object is a struct
	 * 
	 * @param o
	 * @return is struct or not
	 */
	public boolean isStruct(Object o);

	/**
	 * tests if object is an array
	 * 
	 * @param o
	 * @return is array or not
	 */
	public boolean isArray(Object o);

	/**
	 * tests if object is a native java array
	 * 
	 * @param o
	 * @return is a native (java) array
	 */
	public boolean isNativeArray(Object o);

	/**
	 * tests if object is a binary
	 * 
	 * @param object
	 * @return boolean
	 */
	public boolean isBinary(Object object);

	/**
	 * tests if object is a Component
	 * 
	 * @param object
	 * @return boolean
	 */
	public boolean isComponent(Object object);

	/**
	 * tests if object is a Query
	 * 
	 * @param object
	 * @return boolean
	 */
	public boolean isQuery(Object object);

	/**
	 * tests if object is a binary
	 * 
	 * @param object
	 * @return boolean
	 */
	public boolean isUserDefinedFunction(Object object);

	/**
	 * tests if year is a leap year
	 * 
	 * @param year year to check
	 * @return boolean
	 */
	public boolean isLeapYear(int year);

	/**
	 * tests if object is a WDDX Object
	 * 
	 * @param o Object to check
	 * @return boolean
	 */
	public boolean isWddx(Object o);

	/**
	 * tests if object is a XML Object
	 * 
	 * @param o Object to check
	 * @return boolean
	 */
	public boolean isXML(Object o);

	/**
	 * tests if object is a XML Element Object
	 * 
	 * @param o Object to check
	 * @return boolean
	 */
	public boolean isXMLElement(Object o);

	/**
	 * tests if object is a XML Document Object
	 * 
	 * @param o Object to check
	 * @return boolean
	 */
	public boolean isXMLDocument(Object o);

	/**
	 * tests if object is a XML Root Element Object
	 * 
	 * @param o Object to check
	 * @return boolean
	 */
	public boolean isXMLRootElement(Object o);

	/**
	 * @param string
	 * @return returns if string represent a variable name
	 */
	public boolean isVariableName(String string);

	/**
	 * @param string
	 * @return returns if string represent a variable name
	 */
	public boolean isSimpleVariableName(String string);

	/**
	 * returns if object is a CFML object
	 * 
	 * @param o Object to check
	 * @return is or not
	 */
	public boolean isObject(Object o);

	/**
	 * 
	 * @param str
	 * @return return if a String is "Empty", that means NULL or String with length 0 (whitespaces will
	 *         not counted)
	 */
	public boolean isEmpty(String str);

	/**
	 * 
	 * @param str
	 * @param trim
	 * @return return if a String is "Empty", that means NULL or String with length 0 (whitespaces will
	 *         not counted)
	 */
	public boolean isEmpty(String str, boolean trim);

	public Key toKey(Object obj) throws PageException;

	public Key toKey(Object obj, Collection.Key defaultValue);

	/**
	 * Checks if number is valid (not infinity or NaN)
	 * 
	 * @param dbl
	 * @return
	 */
	public boolean isValid(double dbl);

	public boolean isCastableTo(String type, Object o, boolean alsoAlias, boolean alsoPattern, int maxlength);

	public boolean isCastableToArray(Object o);

	public boolean isCastableToBinary(Object object, boolean checkBase64String);

	public boolean isCastableToBoolean(Object obj);

	public boolean isCastableToDate(Object o);

	public boolean isCastableToNumeric(Object o);

	public boolean isCastableToString(Object o);

	public boolean isCastableToStruct(Object o);

	public boolean isClosure(Object o);

	public boolean isLambda(Object o);

	public boolean isFunction(Object o);

	public boolean isCreditCard(Object o);

	public boolean isEmpty(Object o);

	public boolean isGUid(Object o);

	/**
	 * 
	 * @param type
	 * @param o
	 * @return
	 * @throws PageException when type is unknown
	 */
	public boolean is(String type, Object o) throws PageException;

}