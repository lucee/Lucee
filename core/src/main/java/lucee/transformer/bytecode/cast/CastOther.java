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
package lucee.transformer.bytecode.cast;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.StringUtil;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.expression.var.VariableImpl;
import lucee.transformer.bytecode.expression.var.VariableString;
import lucee.transformer.bytecode.util.Methods_Caster;
import lucee.transformer.bytecode.util.Methods_Operator;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.cast.Cast;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.var.Variable;

/**
 * cast an Expression to a Double
 */
public final class CastOther extends ExpressionBase implements Cast {
	// TODO support short type
	private ExpressionBase expr;
	private String type;
	private String lcType;

	private CastOther(Expression expr, String type, String lcType) {
		super(expr.getFactory(), expr.getStart(), expr.getEnd());
		this.expr = (ExpressionBase) expr;
		this.type = type;
		this.lcType = lcType;

	}

	public static Expression toExpression(Expression expr, String type) {
		if (type == null) return expr;

		String lcType = StringUtil.toLowerCase(type);
		switch (lcType.charAt(0)) {
		case 'a':
			if ("any".equals(lcType)) {
				return expr;
			}
			break;
		case 'b':
			if ("boolean".equals(type) || "bool".equals(lcType)) return expr.getFactory().toExprBoolean(expr);
			break;
		case 'd':
			if ("double".equals(type)) return expr.getFactory().toExprNumber(expr);
			break;
		case 'i':
			if ("int".equals(lcType)) return expr.getFactory().toExprInt(expr);
		case 'n':
			if ("number".equals(lcType) || "numeric".equals(lcType)) {
				return expr.getFactory().toExprNumber(expr);
			}
			break;
		case 'o':
			if ("object".equals(lcType)) {
				return expr;
			}
			break;
		case 's':
			if ("string".equals(lcType)) return expr.getFactory().toExprString(expr);
			break;
		case 'u':
			if ("uuid".equals(lcType)) return expr.getFactory().toExprString(expr);
			break;
		case 'v':
			if ("variablename".equals(lcType)) return VariableString.toExprString(expr);
			if ("variable_name".equals(lcType)) return VariableString.toExprString(expr);
			if ("variablestring".equals(lcType)) return VariableString.toExprString(expr);
			if ("variable_string".equals(lcType)) return VariableString.toExprString(expr);
			if ("void".equals(lcType)) return expr;
			break;
		}
		return new CastOther(expr, type, lcType);
	}

	// Array toArray(Object)
	final public static Method TO_ARRAY = new Method("toArray", Types.ARRAY, new Type[] { Types.OBJECT });

	// String toBase64 (Object);
	final public static Method TO_BASE64 = new Method("toBase64", Types.STRING, new Type[] { Types.OBJECT });

	// byte[] toBinary (Object)
	final public static Method TO_BINARY = new Method("toBinary", Types.BYTE_VALUE_ARRAY, new Type[] { Types.OBJECT });

	// byte[] toCollection (Object)
	final public static Method TO_COLLECTION = new Method("toCollection", Types.BYTE_VALUE_ARRAY, new Type[] { Types.OBJECT });

	// lucee.runtime.Component toComponent (Object)
	final public static Method TO_COMPONENT = new Method("toComponent", Types.COMPONENT, new Type[] { Types.OBJECT });

	// String toDecimal (Object)
	final public static Method TO_DECIMAL = new Method("toDecimal", Types.STRING, new Type[] { Types.OBJECT });

	// lucee.runtime.config.Config getConfig ()
	final public static Method GET_CONFIG = new Method("getConfig", Types.CONFIG_WEB, new Type[] {});

	// java.util.TimeZone getTimeZone ()
	final public static Method GET_TIMEZONE = new Method("getTimeZone", Types.TIMEZONE, new Type[] {});

	// Excel toExcel (Object)
	/*
	 * final public static Method TO_EXCEL = new Method("toExcel", Types.EXCEL, new
	 * Type[]{Types.OBJECT});
	 */

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		// Caster.toDecimal(null);
		GeneratorAdapter adapter = bc.getAdapter();
		char first = lcType.charAt(0);
		Type rtn;
		switch (first) {
		case 'a':
			if ("array".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.ARRAY)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_ARRAY);
				return Types.ARRAY;
			}
			break;
		case 'b':
			if ("base64".equals(lcType)) {
				expr.writeOut(bc, MODE_REF);
				adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_BASE64);
				return Types.STRING;
			}
			if ("binary".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.BYTE_VALUE_ARRAY)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_BINARY);
				return Types.BYTE_VALUE_ARRAY;
			}
			if ("byte".equals(type)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.BYTE_VALUE)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_BYTE_VALUE[Methods_Operator.getType(rtn)]);
				return Types.BYTE_VALUE;
			}
			if ("byte".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.BYTE)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_BYTE[Methods_Operator.getType(rtn)]);
				return Types.BYTE;
			}
			if ("boolean".equals(lcType)) {
				return ((ExpressionBase) bc.getFactory().toExprBoolean(expr)).writeOutAsType(bc, MODE_REF);
			}
			break;
		case 'c':
			if ("char".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.CHAR)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_CHAR_VALUE[Methods_Operator.getType(rtn)]);
				return Types.CHAR;
			}
			if ("character".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.CHARACTER)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_CHARACTER[Methods_Operator.getType(rtn)]);
				return Types.CHARACTER;
			}
			if ("collection".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.COLLECTION)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_COLLECTION);
				return Types.COLLECTION;
			}
			if ("component".equals(lcType) || "class".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.COMPONENT)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_COMPONENT);
				return Types.COMPONENT;
			}
			break;
		case 'd':
			if ("double".equals(lcType)) {
				return ((ExpressionBase) bc.getFactory().toExprNumber(expr)).writeOutAsType(bc, MODE_REF);
			}
			if ("date".equals(lcType) || "datetime".equals(lcType)) {
				// First Arg
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (rtn.equals(Types.DATE_TIME)) return Types.DATE_TIME;

				int type = Methods_Operator.getType(rtn);

				// Second Arg
				adapter.loadArg(0);
				// adapter.invokeVirtual(Types.PAGE_CONTEXT,GET_CONFIG);
				// adapter.invokeInterface(Types.CONFIG_WEB,GET_TIMEZONE);
				adapter.invokeVirtual(Types.PAGE_CONTEXT, GET_TIMEZONE);

				adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_DATE[type]);
				return Types.DATE_TIME;
			}
			if ("decimal".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_DECIMAL[Methods_Operator.getType(rtn)]);
				return Types.STRING;
			}
			break;
		case 'e':
			/*
			 * if("excel".equals(type)) { expr.writeOut(bc,MODE_REF);
			 * adapter.invokeStatic(Types.EXCEL_UTIL,TO_EXCEL); return Types.EXCEL; }
			 */
			break;
		case 'f':
			if ("file".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.FILE)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_FILE);
				return Types.FILE;
			}
			if ("float".equals(type)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.FLOAT_VALUE)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_FLOAT_VALUE[Methods_Operator.getType(rtn)]);
				return Types.FLOAT_VALUE;
			}
			if ("float".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.FLOAT)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_FLOAT[Methods_Operator.getType(rtn)]);
				return Types.FLOAT;
			}
			break;
		case 'i':
			if ("int".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.INT_VALUE)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_INT_VALUE[Methods_Operator.getType(rtn)]);
				return Types.INT_VALUE;
			}
			if ("integer".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.INTEGER)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_INTEGER[Methods_Operator.getType(rtn)]);
				return Types.INTEGER;
			}
			/*
			 * ext.img if("image".equals(lcType)) { rtn=expr.writeOut(bc,MODE_REF); Type it =
			 * ImageUtil.getImageType(); if(!rtn.equals(it)) { adapter.loadArg(0);
			 * adapter.invokeStatic(it,Methods_Caster.TO_IMAGE); } return it; }
			 */
			break;
		case 'j':

			if ("java.lang.boolean".equals(lcType)) {
				return ((ExpressionBase) bc.getFactory().toExprBoolean(expr)).writeOutAsType(bc, MODE_REF);
			}
			if ("java.lang.double".equals(lcType)) {
				return ((ExpressionBase) bc.getFactory().toExprNumber(expr)).writeOutAsType(bc, MODE_REF);
			}
			if ("java.lang.string".equals(lcType)) {
				return ((ExpressionBase) bc.getFactory().toExprString(expr)).writeOutAsType(bc, MODE_REF);
			}
			if ("java.lang.stringbuffer".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.STRING_BUFFER)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_STRING_BUFFER);
				return Types.STRING_BUFFER;
			}
			if ("java.lang.byte".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.BYTE)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_BYTE[Methods_Operator.getType(rtn)]);
				return Types.BYTE;
			}
			if ("java.lang.character".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.CHARACTER)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_CHARACTER[Methods_Operator.getType(rtn)]);
				return Types.CHARACTER;
			}
			if ("java.lang.short".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.SHORT)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_SHORT[Methods_Operator.getType(rtn)]);
				return Types.SHORT;
			}
			if ("java.lang.integer".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.INTEGER)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_INTEGER[Methods_Operator.getType(rtn)]);
				return Types.INTEGER;
			}
			if ("java.lang.long".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.LONG)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_LONG[Methods_Operator.getType(rtn)]);
				return Types.LONG;
			}
			if ("java.lang.float".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.FLOAT)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_FLOAT[Methods_Operator.getType(rtn)]);
				return Types.FLOAT;
			}
			if ("java.io.file".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.FILE)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_FILE);
				return Types.FILE;
			}
			if ("java.lang.object".equals(lcType)) {
				return expr.writeOutAsType(bc, MODE_REF);
			}
			else if ("java.util.date".equals(lcType)) {
				// First Arg
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (rtn.equals(Types.DATE)) return Types.DATE;
				if (rtn.equals(Types.DATE_TIME)) return Types.DATE_TIME;

				// Second Arg
				adapter.loadArg(0);
				// adapter.invokeVirtual(Types.PAGE_CONTEXT,GET_CONFIG);
				// adapter.invokeVirtual(Types.CONFIG_WEB,GET_TIMEZONE);
				adapter.invokeVirtual(Types.PAGE_CONTEXT, GET_TIMEZONE);

				adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_DATE[Methods_Operator.getType(rtn)]);
				return Types.DATE_TIME;
			}
			break;
		case 'l':
			if ("long".equals(type)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.LONG_VALUE)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_LONG_VALUE[Methods_Operator.getType(rtn)]);
				return Types.LONG_VALUE;
			}
			else if ("long".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.LONG)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_LONG[Methods_Operator.getType(rtn)]);
				return Types.LONG;
			}
			else if ("locale".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_LOCALE);
				return Types.LOCALE;
			}
			break;
		case 'n':
			if ("node".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.NODE)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_NODE);
				return Types.NODE;
			}
			else if ("null".equals(lcType)) {
				expr.writeOut(bc, MODE_REF);
				adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_NULL);
				// TODO gibt es einen null typ?
				return Types.OBJECT;
			}
			break;
		case 'o':
			if ("object".equals(lcType) || "other".equals(lcType)) {
				expr.writeOut(bc, MODE_REF);
				return Types.OBJECT;
			}
			break;
		case 't':
			if ("timezone".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_TIMEZONE);
				return Types.TIMEZONE;
			}
			else if ("timespan".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.TIMESPAN)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_TIMESPAN);
				return Types.TIMESPAN;
			}
			break;
		case 's':
			if ("struct".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.STRUCT)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_STRUCT);
				return Types.STRUCT;
			}
			if ("short".equals(type)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.SHORT_VALUE)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_SHORT_VALUE[Methods_Operator.getType(rtn)]);
				return Types.SHORT_VALUE;
			}
			if ("short".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.SHORT)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_SHORT[Methods_Operator.getType(rtn)]);
				return Types.SHORT;
			}
			if ("stringbuffer".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.STRING_BUFFER)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_STRING_BUFFER);
				return Types.STRING_BUFFER;
			}
			break;

		case 'x':
			if ("xml".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.NODE)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_NODE);
				return Types.NODE;
			}
			break;
		default:
			if ("query".equals(lcType)) {
				rtn = expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.QUERY)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_QUERY);
				return Types.QUERY;
			}
			if ("querycolumn".equals(lcType)) {
				rtn = (expr instanceof Variable) ? ((VariableImpl) expr).writeOutCollectionAsType(bc, mode) : expr.writeOutAsType(bc, MODE_REF);
				if (!rtn.equals(Types.QUERY_COLUMN)) {

					adapter.loadArg(0);
					adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_QUERY_COLUMN);
				}
				return Types.QUERY_COLUMN;
			}
		}
		Type t = getType(type);

		expr.writeOut(bc, MODE_REF);
		adapter.checkCast(t);
		return t;
	}

	public static Type getType(String type) throws TransformerException {
		if (StringUtil.isEmpty(type)) return Types.OBJECT;

		String lcType = StringUtil.toLowerCase(type);
		switch (lcType.charAt(0)) {
		case 'a':
			if ("any".equals(lcType)) return Types.OBJECT;
			if ("array".equals(lcType)) return Types.ARRAY;
			break;
		case 'b':
			if ("bool".equals(lcType) || "boolean".equals(type)) return Types.BOOLEAN_VALUE;
			if ("boolean".equals(lcType)) return Types.BOOLEAN;
			if ("base64".equals(lcType)) return Types.STRING;

			if ("binary".equals(lcType)) return Types.BYTE_VALUE_ARRAY;
			if ("byte".equals(type)) return Types.BYTE_VALUE;
			if ("byte".equals(lcType)) return Types.BYTE;

			break;

		case 'c':
			if ("char".equals(lcType)) return Types.CHAR;
			if ("character".equals(lcType)) return Types.CHARACTER;
			if ("collection".equals(lcType)) return Types.COLLECTION;
			if ("component".equals(lcType)) return Types.COMPONENT;
			if ("class".equals(lcType)) return Types.COMPONENT;
			break;

		case 'd':
			if ("double".equals(type)) return Types.DOUBLE_VALUE;
			if ("double".equals(lcType)) return Types.DOUBLE;

			if ("date".equals(lcType) || "datetime".equals(lcType)) return Types.DATE_TIME;
			if ("decimal".equals(lcType)) return Types.STRING;

			break;

		case 'e':
			// if("excel".equals(lcType)) return Types.EXCEL;
			break;
		case 'f':
			if ("file".equals(lcType)) return Types.FILE;
			if ("float".equals(type)) return Types.FLOAT_VALUE;
			if ("float".equals(lcType)) return Types.FLOAT;
			if ("function".equals(lcType)) return Types.UDF;
			break;

		case 'i':
			// ext.img if("image".equals(lcType)) return ImageUtil.getImageType();
			if ("int".equals(lcType)) return Types.INT_VALUE;
			if ("integer".equals(lcType)) return Types.INTEGER;
			break;

		case 'l':
			if ("long".equals(type)) return Types.LONG_VALUE;
			if ("long".equals(lcType)) return Types.LONG;
			if ("locale".equals(lcType)) return Types.LOCALE;
			if ("lucee.runtime.type.Collection$Key".equals(type)) return Types.COLLECTION_KEY;

			break;
		case 'n':
			if ("node".equals(lcType)) return Types.NODE;
			if ("null".equals(lcType)) return Types.OBJECT;
			if ("number".equals(lcType)) return Types.DOUBLE_VALUE;
			if ("numeric".equals(lcType)) return Types.DOUBLE_VALUE;
			break;
		case 's':
			if ("string".equals(lcType)) return Types.STRING;
			if ("struct".equals(lcType)) return Types.STRUCT;
			if ("short".equals(type)) return Types.SHORT_VALUE;
			if ("short".equals(lcType)) return Types.SHORT;
			break;
		case 'o':
			if ("other".equals(lcType)) return Types.OBJECT;
			if ("object".equals(lcType)) return Types.OBJECT;
			break;
		case 'u':
			if ("uuid".equals(lcType)) return Types.STRING;
			if ("udf".equals(lcType)) return Types.UDF;
			break;
		case 'q':
			if ("query".equals(lcType)) return Types.QUERY;
			if ("querycolumn".equals(lcType)) return Types.QUERY_COLUMN;
			break;
		case 't':
			if ("timespan".equals(lcType)) return Types.TIMESPAN;
			if ("timezone".equals(lcType)) return Types.TIMEZONE;
			break;
		case 'v':
			if ("variablename".equals(lcType)) return Types.STRING;
			if ("variable_name".equals(lcType)) return Types.STRING;
			if ("variablestring".equals(lcType)) return Types.STRING;
			if ("variable_string".equals(lcType)) return Types.STRING;
			if ("void".equals(lcType)) return Types.VOID;
			break;
		case 'x':
			if ("xml".equals(lcType)) return Types.NODE;
			break;
		}
		try {

			return Type.getType(ClassUtil.loadClass(type));
		}
		catch (ClassException e) {
			throw new TransformerException(e.getMessage(), null);
		}

	}

	@Override
	public Expression getExpr() {
		return expr;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

}