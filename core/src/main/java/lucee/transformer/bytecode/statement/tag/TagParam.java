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
package lucee.transformer.bytecode.statement.tag;

import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.cfml.evaluator.impl.Argument;
import lucee.transformer.expression.Expression;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

public final class TagParam extends TagBaseNoFinal {

	// void param(String type, String name, Object defaultValue)
	private static final Method PARAM_TYPE_NAME_DEFAULTVALUE = new Method(
			"param",
			Types.VOID,
			new Type[]{Types.STRING,Types.STRING,Types.OBJECT}
	);
	private static final Method PARAM_TYPE_NAME_DEFAULTVALUE_REGEX = new Method(
			"param",
			Types.VOID,
			new Type[]{Types.STRING,Types.STRING,Types.OBJECT,Types.STRING}
	);
	private static final Method PARAM_TYPE_NAME_DEFAULTVALUE_MIN_MAX = new Method(
			"param",
			Types.VOID,
			new Type[]{Types.STRING,Types.STRING,Types.OBJECT,Types.DOUBLE_VALUE,Types.DOUBLE_VALUE}
	);
	private static final Method PARAM_TYPE_NAME_DEFAULTVALUE_MAXLENGTH = new Method(
			"param",
			Types.VOID,
			new Type[]{Types.STRING,Types.STRING,Types.OBJECT,Types.INT_VALUE}
	);
	
	public TagParam(Factory f, Position start,Position end) {
		super(f,start,end);
	}

	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		Argument.checkDefaultValue(this);
		
		// pc
		adapter.loadArg(0);
		
		// type
		Attribute attrType = getAttribute("type");
		if(attrType!=null) {
			attrType.getValue().writeOut(bc, Expression.MODE_REF);
		}
		else adapter.push("any");
		
		// name
		getAttribute("name").getValue().writeOut(bc, Expression.MODE_REF);
		
		// default
		Attribute attrDefault = getAttribute("default");
		if(attrDefault!=null) {
			attrDefault.getValue().writeOut(bc, Expression.MODE_REF);
		}
		else adapter.visitInsn(Opcodes.ACONST_NULL);
		
		Attribute attrMin = getAttribute("min");
		Attribute attrMax = getAttribute("max");
		Attribute attrPattern = getAttribute("pattern");
		Attribute maxLength = getAttribute("maxLength");

		if(attrMin!=null || attrMax!=null) {
			// min
			if(attrMin!=null)attrMin.getValue().writeOut(bc, Expression.MODE_VALUE);
			else {
				adapter.visitLdcInsn(new Double("NaN"));
			}
			// max
			if(attrMax!=null)attrMax.getValue().writeOut(bc, Expression.MODE_VALUE);
			else {
				adapter.visitLdcInsn(new Double("NaN"));
			}
			adapter.invokeVirtual(Types.PAGE_CONTEXT, PARAM_TYPE_NAME_DEFAULTVALUE_MIN_MAX);
		}
		else if(attrPattern!=null) {
			attrPattern.getValue().writeOut(bc, Expression.MODE_REF);
			adapter.invokeVirtual(Types.PAGE_CONTEXT, PARAM_TYPE_NAME_DEFAULTVALUE_REGEX);
		}
		else if(maxLength!=null) {
			bc.getFactory().toExprInt(maxLength.getValue()).writeOut(bc, Expression.MODE_VALUE);
			adapter.invokeVirtual(Types.PAGE_CONTEXT, PARAM_TYPE_NAME_DEFAULTVALUE_MAXLENGTH);
		}
		else adapter.invokeVirtual(Types.PAGE_CONTEXT, PARAM_TYPE_NAME_DEFAULTVALUE);

	}

}