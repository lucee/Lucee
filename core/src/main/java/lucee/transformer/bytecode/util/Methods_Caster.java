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
package lucee.transformer.bytecode.util;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

public final class Methods_Caster {

	public static final int OBJECT = 0;
	public static final int BOOLEAN = 1;
	public static final int DOUBLE = 2;
	public static final int STRING = 3;

	// lucee.runtime.type.Array toArray (Object)
	final public static Method TO_ARRAY = new Method("toArray", Types.ARRAY, new Type[] { Types.OBJECT });

	/*
	 * ext.img final public static Method TO_IMAGE = new Method("toImage", ImageUtil.getImageType(), new
	 * Type[]{Types.OBJECT,Types.PAGE_CONTEXT});
	 */
	// String toBase64 (Object)
	final public static Method TO_BASE64 = new Method("toBase64", Types.STRING, new Type[] { Types.OBJECT });
	// byte[] toBinary(Object)
	final public static Method TO_BINARY = new Method("toBinary", Types.BYTE_VALUE_ARRAY, new Type[] { Types.OBJECT });
	// lucee.runtime.type.Collection toCollection (Object)
	final public static Method TO_COLLECTION = new Method("toCollection", Types.COLLECTION, new Type[] { Types.OBJECT });
	// lucee.runtime.Component toComponent (Object)
	final public static Method TO_COMPONENT = new Method("toComponent", Types.COMPONENT, new Type[] { Types.OBJECT });
	// java.io.File toFile (Object)
	final public static Method TO_FILE = new Method("toFile", Types.FILE, new Type[] { Types.OBJECT });
	// org.w3c.dom.Node toNode (Object)
	final public static Method TO_NODE = new Method("toNode", Types.NODE, new Type[] { Types.OBJECT });
	// Object toNull (Object)
	final public static Method TO_NULL = new Method("toNull", Types.OBJECT, new Type[] { Types.OBJECT });
	// lucee.runtime.type.Query toQuery (Object)
	final public static Method TO_QUERY = new Method("toQuery", Types.QUERY, new Type[] { Types.OBJECT });
	// lucee.runtime.type.Query toQueryColumn (Object)
	final public static Method TO_QUERY_COLUMN = new Method("toQueryColumn", Types.QUERY_COLUMN, new Type[] { Types.OBJECT, Types.PAGE_CONTEXT });
	// lucee.runtime.type.Struct toStruct (Object)
	final public static Method TO_STRUCT = new Method("toStruct", Types.STRUCT, new Type[] { Types.OBJECT });
	// lucee.runtime.type.dt.TimeSpan toTimespan (Object)
	final public static Method TO_TIMESPAN = new Method("toTimespan", Types.TIMESPAN, new Type[] { Types.OBJECT });

	final public static Method TO_TIMEZONE = new Method("toTimeZone", Types.TIMEZONE, new Type[] { Types.OBJECT });

	final public static Method TO_LOCALE = new Method("toLocale", Types.LOCALE, new Type[] { Types.OBJECT });

	public static final Method TO_STRING_BUFFER = new Method("toStringBuffer", Types.STRING_BUFFER, new Type[] { Types.OBJECT });

	//
	public static final Method[] TO_DECIMAL = new Method[] { new Method("toDecimal", Types.STRING, new Type[] { Types.OBJECT }),
			new Method("toDecimal", Types.STRING, new Type[] { Types.BOOLEAN_VALUE }), new Method("toDecimal", Types.STRING, new Type[] { Types.DOUBLE_VALUE }),
			new Method("toDecimal", Types.STRING, new Type[] { Types.STRING }) };

	public static final Method[] TO_DATE = new Method[] { new Method("toDate", Types.DATE_TIME, new Type[] { Types.OBJECT, Types.TIMEZONE }),
			new Method("toDate", Types.DATE_TIME, new Type[] { Types.BOOLEAN, Types.TIMEZONE }), new Method("toDate", Types.DATE_TIME, new Type[] { Types.NUMBER, Types.TIMEZONE }),
			new Method("toDate", Types.DATE_TIME, new Type[] { Types.STRING, Types.TIMEZONE }) };

	public static final Method[] TO_STRING = new Method[] { new Method("toString", Types.STRING, new Type[] { Types.OBJECT }),
			new Method("toString", Types.STRING, new Type[] { Types.BOOLEAN_VALUE }), new Method("toString", Types.STRING, new Type[] { Types.DOUBLE_VALUE }),
			new Method("toString", Types.STRING, new Type[] { Types.STRING }) };

	public static final Method[] TO_BOOLEAN = new Method[] { new Method("toBoolean", Types.BOOLEAN, new Type[] { Types.OBJECT }),
			new Method("toBoolean", Types.BOOLEAN, new Type[] { Types.BOOLEAN_VALUE }), new Method("toBoolean", Types.BOOLEAN, new Type[] { Types.DOUBLE_VALUE }),
			new Method("toBoolean", Types.BOOLEAN, new Type[] { Types.STRING }) };

	public static final Method[] TO_BOOLEAN_VALUE = new Method[] { new Method("toBooleanValue", Types.BOOLEAN_VALUE, new Type[] { Types.OBJECT }),
			new Method("toBooleanValue", Types.BOOLEAN_VALUE, new Type[] { Types.BOOLEAN_VALUE }),
			new Method("toBooleanValue", Types.BOOLEAN_VALUE, new Type[] { Types.DOUBLE_VALUE }), new Method("toBooleanValue", Types.BOOLEAN_VALUE, new Type[] { Types.STRING }) };

	public static final Method[] TO_BYTE = new Method[] { new Method("toByte", Types.BYTE, new Type[] { Types.OBJECT }),
			new Method("toByte", Types.BYTE, new Type[] { Types.BOOLEAN }), new Method("toByte", Types.BYTE, new Type[] { Types.NUMBER }),
			new Method("toByte", Types.BYTE, new Type[] { Types.STRING }) };

	public static final Method[] TO_BYTE_VALUE = new Method[] { new Method("toByteValue", Types.BYTE_VALUE, new Type[] { Types.OBJECT }),
			new Method("toByteValue", Types.BYTE_VALUE, new Type[] { Types.BOOLEAN }), new Method("toByteValue", Types.BYTE_VALUE, new Type[] { Types.NUMBER }),
			new Method("toByteValue", Types.BYTE_VALUE, new Type[] { Types.STRING }) };

	public static final Method[] TO_CHARACTER = new Method[] { new Method("toCharacter", Types.CHARACTER, new Type[] { Types.OBJECT }),
			new Method("toCharacter", Types.CHARACTER, new Type[] { Types.BOOLEAN }), new Method("toCharacter", Types.CHARACTER, new Type[] { Types.NUMBER }),
			new Method("toCharacter", Types.CHARACTER, new Type[] { Types.STRING }) };

	public static final Method[] TO_CHAR_VALUE = new Method[] { new Method("toCharValue", Types.CHAR, new Type[] { Types.OBJECT }),
			new Method("toCharValue", Types.CHAR, new Type[] { Types.BOOLEAN }), new Method("toCharValue", Types.CHAR, new Type[] { Types.NUMBER }),
			new Method("toCharValue", Types.CHAR, new Type[] { Types.STRING }) };

	public static final Method[] TO_SHORT = new Method[] { new Method("toShort", Types.SHORT, new Type[] { Types.OBJECT }),
			new Method("toShort", Types.SHORT, new Type[] { Types.BOOLEAN }), new Method("toShort", Types.SHORT, new Type[] { Types.NUMBER }),
			new Method("toShort", Types.SHORT, new Type[] { Types.STRING }) };

	public static final Method[] TO_SHORT_VALUE = new Method[] { new Method("toShortValue", Types.SHORT_VALUE, new Type[] { Types.OBJECT }),
			new Method("toShortValue", Types.SHORT_VALUE, new Type[] { Types.BOOLEAN }), new Method("toShortValue", Types.SHORT_VALUE, new Type[] { Types.NUMBER }),
			new Method("toShortValue", Types.SHORT_VALUE, new Type[] { Types.STRING }) };

	public static final Method[] TO_INTEGER = new Method[] { new Method("toInteger", Types.INTEGER, new Type[] { Types.OBJECT }),
			new Method("toInteger", Types.INTEGER, new Type[] { Types.BOOLEAN }), new Method("toInteger", Types.INTEGER, new Type[] { Types.NUMBER }),
			new Method("toInteger", Types.INTEGER, new Type[] { Types.STRING }) };

	public static final Method[] TO_INT_VALUE = new Method[] { new Method("toIntValue", Types.INT_VALUE, new Type[] { Types.OBJECT }),
			new Method("toIntValue", Types.INT_VALUE, new Type[] { Types.BOOLEAN }), new Method("toIntValue", Types.INT_VALUE, new Type[] { Types.NUMBER }),
			new Method("toIntValue", Types.INT_VALUE, new Type[] { Types.STRING }) };

	public static final Method[] TO_LONG = new Method[] { new Method("toLong", Types.LONG, new Type[] { Types.OBJECT }),
			new Method("toLong", Types.LONG, new Type[] { Types.BOOLEAN }), new Method("toLong", Types.LONG, new Type[] { Types.NUMBER }),
			new Method("toLong", Types.LONG, new Type[] { Types.STRING }) };

	public static final Method[] TO_LONG_VALUE = new Method[] { new Method("toLongValue", Types.LONG_VALUE, new Type[] { Types.OBJECT }),
			new Method("toLongValue", Types.LONG_VALUE, new Type[] { Types.BOOLEAN }), new Method("toLongValue", Types.LONG_VALUE, new Type[] { Types.NUMBER }),
			new Method("toLongValue", Types.LONG_VALUE, new Type[] { Types.STRING }) };

	public static final Method[] TO_FLOAT = new Method[] { new Method("toFloat", Types.FLOAT, new Type[] { Types.OBJECT }),
			new Method("toFloat", Types.FLOAT, new Type[] { Types.BOOLEAN }), new Method("toFloat", Types.FLOAT, new Type[] { Types.NUMBER }),
			new Method("toFloat", Types.FLOAT, new Type[] { Types.STRING }) };

	public static final Method[] TO_FLOAT_VALUE = new Method[] { new Method("toFloatValue", Types.FLOAT_VALUE, new Type[] { Types.OBJECT }),
			new Method("toFloatValue", Types.FLOAT_VALUE, new Type[] { Types.BOOLEAN }), new Method("toFloatValue", Types.FLOAT_VALUE, new Type[] { Types.NUMBER }),
			new Method("toFloatValue", Types.FLOAT_VALUE, new Type[] { Types.STRING }) };

	public static final Method[] TO_DOUBLE = new Method[] { new Method("toDouble", Types.DOUBLE, new Type[] { Types.OBJECT }),
			new Method("toDouble", Types.DOUBLE, new Type[] { Types.BOOLEAN_VALUE }), new Method("toDouble", Types.DOUBLE, new Type[] { Types.DOUBLE_VALUE }),
			new Method("toDouble", Types.DOUBLE, new Type[] { Types.STRING }) };

	public static final Method[] TO_DOUBLE_VALUE = new Method[] { new Method("toDoubleValue", Types.DOUBLE_VALUE, new Type[] { Types.OBJECT }),
			new Method("toDoubleValue", Types.DOUBLE_VALUE, new Type[] { Types.BOOLEAN_VALUE }), new Method("toDoubleValue", Types.DOUBLE_VALUE, new Type[] { Types.DOUBLE_VALUE }),
			new Method("toDoubleValue", Types.DOUBLE_VALUE, new Type[] { Types.STRING }) };

}