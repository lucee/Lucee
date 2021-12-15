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

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.interpreter.VariableInterpreter;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.util.ASMConstants;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.cfml.evaluator.impl.Argument;
import lucee.transformer.expression.Expression;

public final class TagParam extends TagBaseNoFinal {
	//
	public static final Type NULL_SUPPORT_HELPER = Type.getType(NullSupportHelper.class);
	public static final Type VARIABLE_INTERPRETER = Type.getType(VariableInterpreter.class);

	// void param(String type, String name, Object defaultValue)
	private static final Method PARAM_TYPE_NAME_DEFAULTVALUE = new Method("param", Types.VOID, new Type[] { Types.STRING, Types.STRING, Types.OBJECT });
	private static final Method PARAM_TYPE_NAME_DEFAULTVALUE_REGEX = new Method("param", Types.VOID, new Type[] { Types.STRING, Types.STRING, Types.OBJECT, Types.STRING });
	private static final Method PARAM_TYPE_NAME_DEFAULTVALUE_MIN_MAX = new Method("param", Types.VOID,
			new Type[] { Types.STRING, Types.STRING, Types.OBJECT, Types.DOUBLE_VALUE, Types.DOUBLE_VALUE });
	private static final Method PARAM_TYPE_NAME_DEFAULTVALUE_MAXLENGTH = new Method("param", Types.VOID, new Type[] { Types.STRING, Types.STRING, Types.OBJECT, Types.INT_VALUE });

	private static final Method IS_EMPTY = new Method("isEmpty", Types.BOOLEAN_VALUE, new Type[] { Types.STRING });

	private static final Method CONSTR_STRING = new Method("<init>", Types.VOID, new Type[] { Types.STRING }//
	);

	private static final Method NULL = new Method("NULL", Types.OBJECT, new Type[] { Types.PAGE_CONTEXT }//
	);
	private static final Method GET_VARIABLE_EL = new Method("getVariableEL", Types.OBJECT, new Type[] { Types.PAGE_CONTEXT, Types.STRING, Types.OBJECT }//
	);
	private static final Method APPEND_OBJECT = new Method("append", Types.STRING_BUILDER, new Type[] { Types.OBJECT });
	private static final Method APPEND_STRING = new Method("append", Types.STRING_BUILDER, new Type[] { Types.STRING });
	private static final Method TO_STRING = new Method("toString", Types.STRING, new Type[] {});
	private static final Method SUB_PARAM = new Method("subparam", Types.VOID,
			new Type[] { Types.STRING, Types.STRING, Types.OBJECT, Types.DOUBLE_VALUE, Types.DOUBLE_VALUE, Types.STRING, Types.INT_VALUE, Types.BOOLEAN_VALUE });
	private static final Method T = new Method("t", Types.VOID, new Type[] { Types.STRING, Types.STRING, Types.OBJECT, Types.DOUBLE_VALUE, Types.DOUBLE_VALUE

	});

	public TagParam(Factory f, Position start, Position end) {
		super(f, start, end);
	}

	private double t(PageContext pc) throws ExpressionException {

		// if(StringUtil.isEmpty(name)) throw new ExpressionException("The attribute name is required");

		// String name="kkk";
		// Object value=VariableInterpreter.getVariableEL(pc,name,NullSupportHelper.NULL(pc));

		/*
		 * Object value=null; Object defaultValue=null; boolean isNew=false;
		 * 
		 * if(NullSupportHelper.NULL(pc)==value) { if(defaultValue==null) throw new
		 * ExpressionException("The required parameter ["+name+"] was not provided."); value=defaultValue;
		 * isNew=true; }
		 */

		return Double.NaN;
	}

	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		Argument.checkDefaultValue(this);

		// attributes
		Expression name = getAttribute("name").getValue();
		Attribute attrDefault = getAttribute("default");
		Expression def = null;
		if (attrDefault != null) def = attrDefault.getValue();// .writeOut(bc, Expression.MODE_REF);
		// else adapter.visitInsn(Opcodes.ACONST_NULL);

		// check attributes name
		// if(StringUtil.isEmpty(name)) throw new ExpressionException("The attribute name is required");
		/*
		 * name.writeOut(bc, Expression.MODE_REF); adapter.invokeStatic(Types.STRING_UTIL, IS_EMPTY); Label
		 * end = new Label(); adapter.visitJumpInsn(Opcodes.IFEQ, end); //adapter.visitLabel(new Label());
		 * adapter.newInstance(Types.EXPRESSION_EXCEPTION); adapter.dup();
		 * adapter.push("for param a name is required");
		 * adapter.invokeConstructor(Types.EXPRESSION_EXCEPTION, CONSTR_STRING); adapter.throwException();
		 * adapter.visitLabel(end);
		 */

		// value
		// Object value=VariableInterpreter.getVariableEL(this,name,NullSupportHelper.NULL(this));
		int value = adapter.newLocal(Types.OBJECT);
		adapter.loadArg(0); // pc
		name.writeOut(bc, Expression.MODE_REF);// name
		adapter.loadArg(0); // pc
		adapter.invokeStatic(NULL_SUPPORT_HELPER, NULL);
		adapter.invokeStatic(VARIABLE_INTERPRETER, GET_VARIABLE_EL);
		adapter.storeLocal(value);

		// check value 2=value; 3=defaultValue; isNew=4
		int isNew = adapter.newLocal(Types.BOOLEAN_VALUE);
		adapter.push(false);
		adapter.storeLocal(isNew, Types.BOOLEAN_VALUE);

		// Label l3 = new Label();
		// mv.visitLabel(l3);
		adapter.loadArg(0);
		adapter.invokeStatic(NULL_SUPPORT_HELPER, NULL);
		adapter.loadLocal(value);
		Label l4 = new Label();
		adapter.visitJumpInsn(Opcodes.IF_ACMPNE, l4);
		Label l5 = new Label();
		adapter.visitLabel(l5);
		if (def != null) def.writeOut(bc, Expression.MODE_REF);
		else ASMConstants.NULL(adapter);
		adapter.dup();
		int ldef = adapter.newLocal(Types.OBJECT);
		adapter.storeLocal(ldef);
		Label l6 = new Label();
		adapter.visitJumpInsn(Opcodes.IFNONNULL, l6);
		Label l7 = new Label();
		adapter.visitLabel(l7);

		adapter.newInstance(Types.EXPRESSION_EXCEPTION);
		adapter.dup();
		adapter.newInstance(Types.STRING_BUILDER);
		adapter.dup();
		adapter.push("The required parameter [");
		adapter.invokeConstructor(Types.STRING_BUILDER, CONSTR_STRING);
		name.writeOut(bc, Expression.MODE_REF);
		adapter.invokeVirtual(Types.STRING_BUILDER, APPEND_OBJECT);
		adapter.visitLdcInsn("] was not provided.");
		adapter.invokeVirtual(Types.STRING_BUILDER, APPEND_STRING);
		adapter.invokeVirtual(Types.STRING_BUILDER, TO_STRING);
		adapter.invokeConstructor(Types.EXPRESSION_EXCEPTION, CONSTR_STRING);
		adapter.throwException();
		adapter.visitLabel(l6);

		adapter.loadLocal(ldef);
		adapter.storeLocal(value);

		adapter.push(true);
		adapter.storeLocal(isNew);

		adapter.visitLabel(l4);

		// pc
		adapter.loadArg(0);
		adapter.checkCast(Types.PAGE_CONTEXT_IMPL);

		// type
		Attribute attrType = getAttribute("type");
		if (attrType != null) attrType.getValue().writeOut(bc, Expression.MODE_REF);
		else adapter.push("any");
		// adapter.push("any");

		// name
		name.writeOut(bc, Expression.MODE_REF);
		// adapter.push("url.test");

		// value
		adapter.loadLocal(value);

		Attribute attrMin = getAttribute("min");
		Attribute attrMax = getAttribute("max");
		Attribute attrPattern = getAttribute("pattern");
		Attribute maxLength = getAttribute("maxLength");

		// min/max
		if (attrMin != null || attrMax != null) {
			// min
			if (attrMin != null) attrMin.getValue().writeOut(bc, Expression.MODE_VALUE);
			else adapter.visitLdcInsn(new Double("NaN"));

			// max
			if (attrMax != null) attrMax.getValue().writeOut(bc, Expression.MODE_VALUE);
			else adapter.visitLdcInsn(new Double("NaN"));
		}
		else {
			adapter.visitLdcInsn(new Double("NaN"));
			adapter.visitLdcInsn(new Double("NaN"));
		}
		// adapter.push(-1);
		// adapter.push(-1);

		// adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, T);
		// if(true)return;

		// pattern
		if (attrPattern != null) attrPattern.getValue().writeOut(bc, Expression.MODE_REF);
		else ASMConstants.NULL(adapter);
		// ASMConstants.NULL(adapter);

		// maxlength
		if (maxLength != null) bc.getFactory().toExprInt(maxLength.getValue()).writeOut(bc, Expression.MODE_VALUE);
		else adapter.push(-1);
		// adapter.push(-1);

		// isNew
		adapter.loadLocal(isNew, Types.BOOLEAN_VALUE);
		// adapter.push(true);

		// subparam
		adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, SUB_PARAM);
		// param(type, name, defaultValue,Double.NaN,Double.NaN,regex,-1);
		// subparam(type, name, value, min, max, strPattern, maxLength, isNew); // SSO

	}
}