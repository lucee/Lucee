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

package lucee.runtime.exp;

import lucee.commons.lang.StringUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.Type;

/**
 * 
 */
public class CasterException extends ExpressionException {

	/**
	 * constructor of the Exception
	 * 
	 * @param o
	 * @param type
	 */
	public CasterException(Object o, String type) {
		super(createMessage(o, type), createDetail(o));
	}

	public CasterException(Object o, Class clazz) {
		super(createMessage(o, Caster.toTypeName(clazz)), createDetail(o));
	}

	/**
	 * constructor of the Exception
	 * 
	 * @param message
	 */
	public CasterException(String message) {
		super(message);
	}

	public CasterException(String message, String detail) {
		super(message, detail);
	}

	private static String createDetail(Object o) {
		if (o != null) return "Java type of the object is " + Caster.toClassName(o);
		return "value is null";
	}

	public static String createMessage(Object o, String type) {

		if (o instanceof String) return "Can't cast String [" + crop(o.toString()) + "] to a value of type [" + type + "]";
		if (o != null) return "Can't cast Object type [" + Type.getName(o) + "] to a value of type [" + type + "]";
		return "Can't cast Null value to value of type [" + type + "]";
	}

	public static String crop(Object obj) {
		int max = 100;
		String str = obj.toString();
		if (StringUtil.isEmpty(str) || str.length() <= max) return str;
		return str.substring(0, max) + "...";
	}
}