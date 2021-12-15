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

public final class Methods {

	// Caster String
	// String toString (Object)
	final public static Method METHOD_TO_STRING = new Method("toString", Types.STRING, new Type[] { Types.OBJECT });
	// String toString (String)
	// final public static Method METHOD_TO_STRING_FROM_STRING = new Method("toString",Types.STRING,new
	// Type[]{Types.STRING});

	// String toString (double)
	final public static Method METHOD_TO_STRING_FROM_DOUBLE = new Method("toString", Types.STRING, new Type[] { Types.DOUBLE_VALUE });
	// String toString (boolean)
	final public static Method METHOD_TO_STRING_FROM_BOOLEAN = new Method("toString", Types.STRING, new Type[] { Types.BOOLEAN_VALUE });

	// Caster Boolean
	// Boolean toBoolean (Object)
	final public static Method METHOD_TO_BOOLEAN = new Method("toBoolean", Types.BOOLEAN, new Type[] { Types.OBJECT });
	// boolean toBooleanValue (Object)
	final public static Method METHOD_TO_BOOLEAN_VALUE = new Method("toBooleanValue", Types.BOOLEAN_VALUE, new Type[] { Types.OBJECT });

	// Boolean toBoolean (double)
	final public static Method METHOD_TO_BOOLEAN_FROM_DOUBLE = new Method("toBoolean", Types.BOOLEAN, new Type[] { Types.DOUBLE_VALUE });

	// boolean toBooleanValue (double)
	final public static Method METHOD_TO_BOOLEAN_VALUE_FROM_DOUBLE = new Method("toBooleanValue", Types.BOOLEAN_VALUE, new Type[] { Types.DOUBLE_VALUE });

	// Boolean toBoolean (boolean)
	final public static Method METHOD_TO_BOOLEAN_FROM_BOOLEAN = new Method("toBoolean", Types.BOOLEAN, new Type[] { Types.BOOLEAN_VALUE });
	// boolean toBooleanValue (boolean)
	// final public static Method METHOD_TO_BOOLEAN_VALUE_FROM_BOOLEAN = new
	// Method("toBooleanValue",Types.BOOLEAN_VALUE,new Type[]{Types.BOOLEAN_VALUE});

	// Boolean toBoolean (String)
	final public static Method METHOD_TO_BOOLEAN_FROM_STRING = new Method("toBoolean", Types.BOOLEAN, new Type[] { Types.STRING });
	// boolean toBooleanValue (String)
	final public static Method METHOD_TO_BOOLEAN_VALUE_FROM_STRING = new Method("toBooleanValue", Types.BOOLEAN_VALUE, new Type[] { Types.STRING });

	// Caster Double
	// Double toDouble (Object)
	final public static Method METHOD_TO_DOUBLE = new Method("toDouble", Types.DOUBLE, new Type[] { Types.OBJECT });
	final public static Method METHOD_TO_FLOAT = new Method("toFloat", Types.FLOAT, new Type[] { Types.OBJECT });
	final public static Method METHOD_TO_INTEGER = new Method("toInteger", Types.INTEGER, new Type[] { Types.OBJECT });

	// double toDouble Value(Object)
	final public static Method METHOD_TO_DOUBLE_VALUE = new Method("toDoubleValue", Types.DOUBLE_VALUE, new Type[] { Types.OBJECT });
	final public static Method METHOD_TO_FLOAT_VALUE = new Method("toFloatValue", Types.FLOAT_VALUE, new Type[] { Types.OBJECT });
	final public static Method METHOD_TO_INT_VALUE = new Method("toIntValue", Types.FLOAT_VALUE, new Type[] { Types.OBJECT });

	final public static Method METHOD_TO_INTEGER_FROM_INT = new Method("toInteger", Types.INTEGER, new Type[] { Types.INT_VALUE });
	final public static Method METHOD_TO_LONG_FROM_LONG_VALUE = new Method("toLong", Types.LONG, new Type[] { Types.LONG_VALUE });

	// Double toDouble (double)
	final public static Method METHOD_TO_DOUBLE_FROM_DOUBLE = new Method("toDouble", Types.DOUBLE, new Type[] { Types.DOUBLE_VALUE });
	final public static Method METHOD_TO_DOUBLE_FROM_FLOAT_VALUE = new Method("toDouble", Types.DOUBLE, new Type[] { Types.FLOAT_VALUE });
	final public static Method METHOD_TO_FLOAT_FROM_DOUBLE = new Method("toFloat", Types.FLOAT, new Type[] { Types.DOUBLE_VALUE });

	final public static Method METHOD_TO_FLOAT_FROM_FLOAT = new Method("toFloat", Types.FLOAT, new Type[] { Types.FLOAT_VALUE });
	// double toDoubleValue (double)
	// final public static Method METHOD_TO_DOUBLE_VALUE_FROM_DOUBLE = new
	// Method("toDoubleValue",Types.DOUBLE_VALUE,new Type[]{Types.DOUBLE_VALUE});
	final public static Method METHOD_TO_FLOAT_VALUE_FROM_DOUBLE = new Method("toFloatValue", Types.FLOAT_VALUE, new Type[] { Types.DOUBLE_VALUE });

	final public static Method METHOD_TO_INT_VALUE_FROM_DOUBLE = new Method("toIntValue", Types.INT_VALUE, new Type[] { Types.DOUBLE_VALUE });
	final public static Method METHOD_TO_INTEGER_FROM_DOUBLE = new Method("toInteger", Types.INTEGER, new Type[] { Types.DOUBLE_VALUE });

	// Double toDouble (boolean)
	final public static Method METHOD_TO_DOUBLE_FROM_BOOLEAN = new Method("toDouble", Types.DOUBLE, new Type[] { Types.BOOLEAN_VALUE });
	final public static Method METHOD_TO_FLOAT_FROM_BOOLEAN = new Method("toFloat", Types.FLOAT, new Type[] { Types.BOOLEAN_VALUE });

	// double toDoubleValue (boolean)
	final public static Method METHOD_TO_DOUBLE_VALUE_FROM_BOOLEAN = new Method("toDoubleValue", Types.DOUBLE_VALUE, new Type[] { Types.BOOLEAN_VALUE });
	final public static Method METHOD_TO_FLOAT_VALUE_FROM_BOOLEAN = new Method("toFloatValue", Types.FLOAT_VALUE, new Type[] { Types.BOOLEAN_VALUE });

	final public static Method METHOD_TO_INT_VALUE_FROM_BOOLEAN = new Method("toIntValue", Types.INT_VALUE, new Type[] { Types.BOOLEAN_VALUE });
	final public static Method METHOD_TO_INTEGER_FROM_BOOLEAN = new Method("toInteger", Types.INTEGER, new Type[] { Types.BOOLEAN_VALUE });

	final public static Method METHOD_TO_DOUBLE_VALUE_FROM_DOUBLE = new Method("toDoubleValue", Types.DOUBLE_VALUE, new Type[] { Types.DOUBLE });

	// Double toDouble (String)
	final public static Method METHOD_TO_DOUBLE_FROM_STRING = new Method("toDouble", Types.DOUBLE, new Type[] { Types.STRING });
	final public static Method METHOD_TO_FLOAT_FROM_STRING = new Method("toFloat", Types.FLOAT, new Type[] { Types.STRING });
	final public static Method METHOD_TO_INTEGER_FROM_STRING = new Method("toInteger", Types.INTEGER, new Type[] { Types.STRING });

	// double toDoubleValue (String)
	final public static Method METHOD_TO_DOUBLE_VALUE_FROM_STRING = new Method("toDoubleValue", Types.DOUBLE_VALUE, new Type[] { Types.STRING });
	final public static Method METHOD_TO_FLOAT_VALUE_FROM_STRING = new Method("toFloatValue", Types.FLOAT_VALUE, new Type[] { Types.STRING });
	final public static Method METHOD_TO_INT_VALUE_FROM_STRING = new Method("toIntValue", Types.INT_VALUE, new Type[] { Types.STRING });
}