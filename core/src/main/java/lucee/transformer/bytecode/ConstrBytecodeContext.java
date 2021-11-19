/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.transformer.bytecode;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.runtime.PageSource;
import lucee.transformer.bytecode.statement.udf.Function;
import lucee.transformer.expression.literal.LitString;

public class ConstrBytecodeContext extends BytecodeContext {

	private List<Data> properties = new ArrayList<Data>();

	public ConstrBytecodeContext(PageSource ps, Page page, List<LitString> keys, ClassWriter classWriter, String className, GeneratorAdapter adapter, Method method,
			boolean writeLog, boolean suppressWSbeforeArg, boolean output, boolean returnValue) {
		super(ps, null, page, keys, classWriter, className, adapter, method, writeLog, suppressWSbeforeArg, output, returnValue);
	}

	public void addUDFProperty(Function function, int arrayIndex, int valueIndex, int type) {
		properties.add(new Data(function, arrayIndex, valueIndex, type));
	}

	public List<Data> getUDFProperties() {
		return properties;
	}

	/*
	 * cga.visitVarInsn(ALOAD, 0); cga.visitFieldInsn(GETFIELD, bc.getClassName(), "udfs",
	 * Types.UDF_PROPERTIES_ARRAY.toString()); cga.push(arrayIndex);
	 * createUDFProperties(constr,valueIndex,type); cga.visitInsn(AASTORE);
	 */

	public static class Data {

		public final Function function;
		public final int arrayIndex;
		public final int valueIndex;
		public final int type;

		public Data(Function function, int arrayIndex, int valueIndex, int type) {
			this.function = function;
			this.arrayIndex = arrayIndex;
			this.valueIndex = valueIndex;
			this.type = type;
		}

	}

}