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
package lucee.runtime.op;

import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;

/**
 * implementation of the interface Decision
 */
public final class DecisionImpl implements lucee.runtime.util.Decision {

	private static DecisionImpl singelton;

	@Override
	public boolean isArray(Object o) {
		return Decision.isArray(o);
	}

	@Override
	public boolean isBinary(Object object) {
		return Decision.isBinary(object);
	}

	@Override
	public boolean isBoolean(Object value) {
		return Decision.isBoolean(value);
	}

	@Override
	public boolean isBoolean(String str) {
		return Decision.isBoolean(str);
	}

	@Override
	public boolean isComponent(Object object) {
		return Decision.isComponent(object);
	}

	@Override
	public boolean isDate(Object value, boolean alsoNumbers) {
		return Decision.isDateAdvanced(value, alsoNumbers);
	}

	@Override
	public boolean isEmpty(String str, boolean trim) {
		return StringUtil.isEmpty(str, trim);
	}

	@Override
	public boolean isEmpty(String str) {
		return StringUtil.isEmpty(str);
	}

	@Override
	public boolean isHex(String str) {
		return Decision.isHex(str);
	}

	@Override
	public boolean isLeapYear(int year) {
		return Decision.isLeapYear(year);
	}

	@Override
	public boolean isNativeArray(Object o) {
		return Decision.isNativeArray(o);
	}

	@Override
	public boolean isNumeric(Object value) {
		return isNumber(value);
	}

	@Override
	public boolean isNumeric(String str) {
		return isNumber(str);
	}

	@Override
	public boolean isNumber(Object value) {
		return Decision.isNumber(value);
	}

	@Override
	public boolean isNumber(String str) {
		return Decision.isNumber(str);
	}

	@Override
	public boolean isObject(Object o) {
		return Decision.isObject(o);
	}

	@Override
	public boolean isQuery(Object object) {
		return Decision.isQuery(object);
	}

	@Override
	public boolean isSimpleValue(Object value) {
		return Decision.isSimpleValue(value);
	}

	@Override
	public boolean isSimpleVariableName(String string) {
		return Decision.isSimpleVariableName(string);
	}

	@Override
	public boolean isStruct(Object o) {
		return Decision.isStruct(o);
	}

	@Override
	public boolean isUserDefinedFunction(Object object) {
		return Decision.isUserDefinedFunction(object);
	}

	@Override
	public boolean isUUID(String str) {
		return Decision.isUUId(str);
	}

	@Override
	public boolean isVariableName(String string) {
		return Decision.isVariableName(string);
	}

	@Override
	public boolean isWddx(Object o) {
		return Decision.isWddx(o);
	}

	@Override
	public boolean isXML(Object o) {
		return Decision.isXML(o);
	}

	@Override
	public boolean isXMLDocument(Object o) {
		return Decision.isXMLDocument(o);
	}

	@Override
	public boolean isXMLElement(Object o) {
		return Decision.isXMLElement(o);
	}

	@Override
	public boolean isXMLRootElement(Object o) {
		return Decision.isXMLRootElement(o);
	}

	public static lucee.runtime.util.Decision getInstance() {
		if (singelton == null) singelton = new DecisionImpl();
		return singelton;
	}

	@Override
	public Key toKey(Object obj) throws PageException {
		return KeyImpl.toKey(obj);
	}

	@Override
	public Key toKey(Object obj, Key defaultValue) {
		return KeyImpl.toKey(obj, defaultValue);
	}

	@Override
	public boolean isAnyType(String type) {
		return Decision.isAnyType(type);
	}

	@Override
	public boolean isValid(double dbl) {
		return Decision.isValid(dbl);
	}
	/*
	 * public boolean isTemplateExtension(String ext) { return Constants.isTemplateExtension(ext); }
	 * 
	 * public boolean isComponentExtension(String ext) { return Constants.isComponentExtension(ext); }
	 */

	@Override
	public boolean isCastableToBoolean(Object obj) {
		return Decision.isCastableToBoolean(obj);
	}

	@Override
	public boolean isCastableTo(String type, Object o, boolean alsoAlias, boolean alsoPattern, int maxlength) {
		return Decision.isCastableTo(type, o, alsoAlias, alsoPattern, maxlength);
	}

	@Override
	public boolean isCastableToArray(Object o) {
		return Decision.isCastableToArray(o);
	}

	@Override
	public boolean isCastableToBinary(Object object, boolean checkBase64String) {
		return Decision.isCastableToBinary(object, checkBase64String);
	}

	@Override
	public boolean isCastableToDate(Object o) {
		return Decision.isCastableToDate(o);
	}

	@Override
	public boolean isCastableToNumeric(Object o) {
		return Decision.isCastableToNumeric(o);
	}

	@Override
	public boolean isCastableToString(Object o) {
		return Decision.isCastableToString(o);
	}

	@Override
	public boolean isCastableToStruct(Object o) {
		return Decision.isCastableToStruct(o);
	}

	@Override
	public boolean isClosure(Object o) {
		return Decision.isClosure(o);
	}

	@Override
	public boolean isLambda(Object o) {
		return Decision.isLambda(o);
	}

	@Override
	public boolean isCreditCard(Object o) {
		return Decision.isCreditCard(o);
	}

	@Override
	public boolean isEmpty(Object o) {
		return Decision.isEmpty(o);
	}

	@Override
	public boolean isGUid(Object o) {
		return Decision.isGUId(o);
	}

	@Override
	public boolean is(String type, Object o) throws ExpressionException {
		return Decision.isValid(type, o);
	}

	@Override
	public boolean isFunction(Object o) {
		return Decision.isFunction(o);
	}

}