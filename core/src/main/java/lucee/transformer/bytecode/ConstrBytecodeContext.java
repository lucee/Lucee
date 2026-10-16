/**
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.
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
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.runtime.PageSource;
import lucee.runtime.config.Config;
import lucee.transformer.bytecode.statement.udf.Function;
import lucee.transformer.expression.literal.LitString;

public final class ConstrBytecodeContext extends BytecodeContext {

	private List<Data> properties = new ArrayList<Data>();

	public ConstrBytecodeContext(Config config, PageSource ps, PageImpl page, Map<LitString, Integer> keys, ClassWriter classWriter, String className, GeneratorAdapter adapter,
			Method method, boolean writeLog, boolean suppressWSbeforeArg, boolean output, boolean returnValue, int sourceOffset) {
		super(config, ps, null, page, keys, classWriter, className, adapter, method, writeLog, suppressWSbeforeArg, output, returnValue, sourceOffset);
	}

	/**
	 * Override to return this instance as the constructor context.
	 * This ensures child BytecodeContexts get a reference to us via getConstructor().
	 */
	@Override
	public ConstrBytecodeContext getConstructor() {
		return this;
	}

	public void addUDFProperty(Function function, int arrayIndex, int valueIndex, int type) {
		properties.add(new Data(function, arrayIndex, valueIndex, type));
	}

	public List<Data> getUDFProperties() {
		return properties;
	}

	/**
	 * Track an executable line from child BytecodeContext instances.
	 * Called during compilation so all lines end up in the constructor context.
	 */
	public void trackExecutableLine(int line) {
		// Uses the executableLines Set inherited from BytecodeContext
		// We don't call visitLineNumber as that would emit bytecode
		if (super.executableLines == null) super.executableLines = new java.util.TreeSet<>();
		super.executableLines.add(line);
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