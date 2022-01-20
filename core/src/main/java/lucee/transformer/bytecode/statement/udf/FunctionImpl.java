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
package lucee.transformer.bytecode.statement.udf;

import org.objectweb.asm.commons.GeneratorAdapter;

import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.Body;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.Root;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.Literal;

public final class FunctionImpl extends Function {

	public FunctionImpl(Root root, Expression name, Expression returnType, Expression returnFormat, Expression output, Expression bufferOutput, int access, Expression displayName,
			Expression description, Expression hint, Expression secureJson, Expression verifyClient, Expression localMode, Literal cachedWithin, int modifier, Body body,
			Position start, Position end) {
		super(root, name, returnType, returnFormat, output, bufferOutput, access, displayName, description, hint, secureJson, verifyClient, localMode, cachedWithin, modifier, body,
				start, end);
	}

	public FunctionImpl(Root root, String name, int access, int modifier, String returnType, Body body, Position start, Position end) {
		super(root, name, access, modifier, returnType, body, start, end);
	}

	@Override
	public final void _writeOut(BytecodeContext bc, int pageType) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();

		if (pageType == PAGE_TYPE_INTERFACE) {
			adapter.loadArg(0);
		}
		else if (pageType == PAGE_TYPE_COMPONENT) {
			adapter.loadArg(1);
		}
		// pc.variablesScope().set(<name>,udf);
		else {
			adapter.loadArg(0);
			adapter.invokeVirtual(Types.PAGE_CONTEXT, VARIABLE_SCOPE);
		}

		bc.getFactory().registerKey(bc, name, true);
		if (pageType == PAGE_TYPE_COMPONENT) {
			if (this.jf != null) {
				bc.registerJavaFunction(jf);
				adapter.push(jf.getClassName());
				adapter.invokeVirtual(Types.COMPONENT_IMPL, REG_JAVA_FUNCTION);
			}
			else {
				loadUDFProperties(bc, valueIndex, arrayIndex, TYPE_UDF);
				adapter.invokeVirtual(Types.COMPONENT_IMPL, "staticConstructor".equals(bc.getMethod().getName()) ? REG_STATIC_UDF_KEY : REG_UDF_KEY);
			}
		}
		else if (pageType == PAGE_TYPE_INTERFACE) {
			if (this.jf != null) {
				bc.registerJavaFunction(jf);
				adapter.push(jf.getClassName());
				adapter.invokeVirtual(Types.INTERFACE_IMPL, REG_JAVA_FUNCTION);
			}
			else {
				loadUDFProperties(bc, valueIndex, arrayIndex, TYPE_UDF);
				adapter.invokeVirtual(Types.INTERFACE_IMPL, "staticConstructor".equals(bc.getMethod().getName()) ? REG_STATIC_UDF_KEY : REG_UDF_KEY);
			}
		}
		else {
			if (this.jf != null) {
				bc.registerJavaFunction(jf);
				adapter.loadArg(0);
				adapter.checkCast(Types.PAGE_CONTEXT_IMPL);
				adapter.visitVarInsn(ALOAD, 0);
				adapter.push(jf.getClassName());
				adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, USE_JAVA_FUNCTION);
			}
			else {
				adapter.newInstance(Types.UDF_IMPL);
				adapter.dup();
				loadUDFProperties(bc, valueIndex, arrayIndex, TYPE_UDF);
				adapter.invokeConstructor(Types.UDF_IMPL, INIT_UDF_IMPL_PROP);
			}
			// loadUDF(bc, index);
			adapter.invokeInterface(Types.VARIABLES, SET_KEY);
			adapter.pop();
		}

	}

	@Override
	public int getType() {
		return TYPE_UDF;
	}

}