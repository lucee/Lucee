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

public final class Methods_Operator {

	public static final Method OPERATOR_EQV_PC_B_B = new Method("eqv", Types.BOOLEAN_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.BOOLEAN, Types.BOOLEAN });

	public static final Method OPERATOR_IMP_PC_B_B = new Method("imp", Types.BOOLEAN_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.BOOLEAN, Types.BOOLEAN });

	public static final Method OPERATOR_CT_PC_O_O = new Method("ct", Types.BOOLEAN_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT });
	public static final Method OPERATOR_EEQ_PC_O_O = new Method("eeq", Types.BOOLEAN_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT });
	public static final Method OPERATOR_NEEQ_PC_O_O = new Method("neeq", Types.BOOLEAN_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT });

	public static final Method OPERATOR_NCT_PC_O_O = new Method("nct", Types.BOOLEAN_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT });

	public static final Method[][] COMPARATORS = new Method[][] {
			// Object
			new Method[] { new Method("compare", Types.INT_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT }),
					new Method("compare", Types.INT_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT, Types.BOOLEAN }),
					new Method("compare", Types.INT_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT, Types.NUMBER }),
					new Method("compare", Types.INT_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT, Types.STRING }) },
			// boolean
			new Method[] { new Method("compare", Types.INT_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.BOOLEAN, Types.OBJECT }),
					new Method("compare", Types.INT_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.BOOLEAN, Types.BOOLEAN }),
					new Method("compare", Types.INT_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.BOOLEAN, Types.NUMBER }),
					new Method("compare", Types.INT_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.BOOLEAN, Types.STRING }) },
			// double
			new Method[] { new Method("compare", Types.INT_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.NUMBER, Types.OBJECT }),
					new Method("compare", Types.INT_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.NUMBER, Types.BOOLEAN }),
					new Method("compare", Types.INT_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.NUMBER, Types.NUMBER }),
					new Method("compare", Types.INT_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.NUMBER, Types.STRING }) },
			// String
			new Method[] { new Method("compare", Types.INT_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.STRING, Types.OBJECT }),
					new Method("compare", Types.INT_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.STRING, Types.BOOLEAN }),
					new Method("compare", Types.INT_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.STRING, Types.NUMBER }),
					new Method("compare", Types.INT_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.STRING, Types.STRING }) } };

	public static int getType(Type type) {
		String className = type.getClassName();

		if (Types.BOOLEAN.equals(type)) return Types._BOOLEAN;
		if (Types.DOUBLE.equals(type)) return Types._NUMBER;
		if (Types.NUMBER.equals(type)) return Types._NUMBER;
		if (Types.STRING.equals(type)) return Types._STRING;

		if (Types.BYTE.equals(type)) return Types._NUMBER;
		if (Types.SHORT.equals(type)) return Types._NUMBER;
		if (Types.FLOAT.equals(type)) return Types._NUMBER;
		if (Types.LONG.equals(type)) return Types._NUMBER;
		if (Types.INTEGER.equals(type)) return Types._NUMBER;

		return Types._OBJECT;
	}
}