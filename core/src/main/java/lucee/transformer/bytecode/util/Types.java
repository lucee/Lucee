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
package lucee.transformer.bytecode.util;

import java.io.BufferedReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.Tag;

import org.objectweb.asm.Type;
import org.w3c.dom.Node;

import lucee.commons.color.ConstantsDouble;
import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.CIPage;
import lucee.runtime.InterfacePageImpl;
import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageImpl;
import lucee.runtime.PageSource;
import lucee.runtime.component.ImportDefintion;
import lucee.runtime.component.ImportDefintionImpl;
import lucee.runtime.component.Member;
import lucee.runtime.component.StaticStruct;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.Identification;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.exp.Abort;
import lucee.runtime.exp.ExceptionHandler;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.DynamicAttributes;
import lucee.runtime.functions.FunctionHandlerPool;
import lucee.runtime.interpreter.VariableInterpreter;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Constants;
import lucee.runtime.op.OpUtil;
import lucee.runtime.security.SecurityManager;
import lucee.runtime.tag.TagUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.Closure;
import lucee.runtime.type.Collection;
import lucee.runtime.type.FunctionValue;
import lucee.runtime.type.FunctionValueImpl;
import lucee.runtime.type.Iteratorable;
import lucee.runtime.type.Lambda;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryColumn;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.UDFImpl;
import lucee.runtime.type.UDFProperties;
import lucee.runtime.type.UDFPropertiesImpl;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.ref.Reference;
import lucee.runtime.type.ref.VariableReference;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.scope.Undefined;
import lucee.runtime.type.scope.Variables;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.util.CallerUtil;
import lucee.runtime.util.NumberRange;
import lucee.runtime.util.PageContextUtil;
import lucee.runtime.util.VariableUtilImpl;
import lucee.runtime.writer.BodyContentUtil;
import lucee.transformer.TransformerException;

public final class Types {

	// TODO muss wohl alle Prim typen sein plus Object
	public static final int _BOOLEAN = 1;
	public static final int _NUMBER = 2;
	private static final int _SHORT = 7;

	public static final int _OBJECT = 0;
	public static final int _STRING = 3;

	private static final int _CHAR = _NUMBER;
	private static final int _FLOAT = _NUMBER;
	private static final int _LONG = _NUMBER;
	private static final int _INT = _NUMBER;
	private static final int _BYTE = _NUMBER;
	private static final int _DOUBLE = _NUMBER;

	// public static final int SIZE_INT_TYPES=10;

	public static final Type ABORT = Type.getType(Abort.class);
	public static final Type ARRAY = Type.getType(lucee.runtime.type.Array.class);
	public static final Type ARRAY_IMPL = Type.getType(lucee.runtime.type.ArrayImpl.class);

	public static final Type BYTE = Type.getType(Byte.class);
	public static final Type BYTE_VALUE = Type.getType(byte.class);
	public static final Type BYTE_ARRAY = Type.getType(Byte[].class);
	public static final Type BYTE_VALUE_ARRAY = Type.getType(byte[].class);

	public static final Type BOOLEAN = Type.getType(Boolean.class);
	public static final Type BOOLEAN_VALUE = Type.getType(boolean.class);

	public static final Type CHAR = Type.getType(char.class);
	public static final Type CHARACTER = Type.getType(Character.class);

	public static final Type DOUBLE = Type.getType(Double.class);
	public static final Type DOUBLE_VALUE = Type.getType(double.class);

	public static final Type FLOAT = Type.getType(Float.class);
	public static final Type FLOAT_VALUE = Type.getType(float.class);

	// public static final Type IMAGE = Type.getType(Image.class);
	public static final Type INTEGER = Type.getType(Integer.class);
	public static final Type INT_VALUE = Type.getType(int.class);

	public static final Type LONG = Type.getType(Long.class);
	public static final Type LOCALE = Type.getType(Locale.class);
	public static final Type LONG_VALUE = Type.getType(long.class);

	public static final Type SHORT = Type.getType(Short.class);
	public static final Type SHORT_VALUE = Type.getType(short.class);
	public static final Type NUMBER = Type.getType(Number.class);

	public static final Type COMPONENT = Type.getType(lucee.runtime.Component.class);

	public final static Type PAGE = Type.getType(Page.class);
	public final static Type PAGE_IMPL = Type.getType(PageImpl.class);
	public final static Type PAGE_SOURCE = Type.getType(PageSource.class);
	public static final Type COMPONENT_PAGE_IMPL = Type.getType(lucee.runtime.ComponentPageImpl.class);
	public static final Type INTERFACE_PAGE_IMPL = Type.getType(InterfacePageImpl.class);

	public static final Type COMPONENT_IMPL = Type.getType(lucee.runtime.ComponentImpl.class);
	public static final Type INTERFACE_IMPL = Type.getType(lucee.runtime.InterfaceImpl.class);

	public static final Type DATE_TIME = Type.getType(lucee.runtime.type.dt.DateTime.class);

	public static final Type DATE = Type.getType(java.util.Date.class);

	public static final Type FILE = Type.getType(java.io.File.class);
	// public static final Type EXCEL=Type.getType(Excel.class);
	// public static final Type EXCEL_UTIL=Type.getType(ExcelUtil.class);

	public static final Type RESOURCE = Type.getType(Resource.class);

	public static final Type FUNCTION_VALUE = Type.getType(FunctionValue.class);

	public static final Type ITERATOR = Type.getType(Iterator.class);
	public static final Type ITERATORABLE = Type.getType(Iteratorable.class);

	public static final Type NODE = Type.getType(org.w3c.dom.Node.class);

	public static final Type OBJECT = Type.getType(Object.class);

	public static final Type OBJECT_ARRAY = Type.getType(Object[].class);

	public static final Type PAGE_CONTEXT = Type.getType(PageContext.class);
	public static final Type PAGE_CONTEXT_IMPL = Type.getType(PageContextImpl.class);
	public static final Type PAGE_CONTEXT_UTIL = Type.getType(PageContextUtil.class);

	public final static Type QUERY = Type.getType(lucee.runtime.type.Query.class);
	public final static Type QUERY_COLUMN = Type.getType(lucee.runtime.type.QueryColumn.class);

	public final static Type PAGE_EXCEPTION = Type.getType(PageException.class);

	public final static Type REFERENCE = Type.getType(Reference.class);

	public static final Type CASTER = Type.getType(Caster.class);

	public static final Type COLLECTION = Type.getType(Collection.class);

	public static final Type STRING = Type.getType(String.class);
	public static final Type STRING_ARRAY = Type.getType(String[].class);
	public static final Type STRING_UTIL = Type.getType(StringUtil.class);

	public static final Type STRUCT = Type.getType(lucee.runtime.type.Struct.class);
	public static final Type STRUCT_IMPL = Type.getType(lucee.runtime.type.StructImpl.class);

	public static final Type OP_UTIL = Type.getType(OpUtil.class);
	public static final Type CONFIG = Type.getType(Config.class);
	public static final Type CONFIG_WEB = Type.getType(ConfigWeb.class);

	public static final Type SCOPE = Type.getType(Scope.class);
	public static final Type VARIABLES = Type.getType(Variables.class);

	public static final Type TIMESPAN = Type.getType(lucee.runtime.type.dt.TimeSpan.class);

	public static final Type THROWABLE = Type.getType(Throwable.class);
	public static final Type EXCEPTION = Type.getType(Exception.class);

	public static final Type VOID = Type.VOID_TYPE;

	public static final Type LIST_UTIL = Type.getType(ListUtil.class);
	public static final Type VARIABLE_INTERPRETER = Type.getType(VariableInterpreter.class);
	public static final Type VARIABLE_REFERENCE = Type.getType(VariableReference.class);
	public static final Type JSP_WRITER = Type.getType(JspWriter.class);
	public static final Type TAG = Type.getType(Tag.class);
	public static final Type NUMBER_RANGE = Type.getType(NumberRange.class);
	public static final Type NULL_SUPPORT_HELPER = Type.getType(NullSupportHelper.class);

	public static final Type SECURITY_MANAGER = Type.getType(SecurityManager.class);
	public static final Type READER = Type.getType(Reader.class);
	public static final Type BUFFERED_READER = Type.getType(BufferedReader.class);
	public static final Type ARRAY_UTIL = Type.getType(ArrayUtil.class);
	public static final Type EXCEPTION_HANDLER = Type.getType(ExceptionHandler.class);
	// public static final Type RETURN_ EXCEPTION = Type.getType(ReturnException.class);
	public static final Type TIMEZONE = Type.getType(java.util.TimeZone.class);
	public static final Type STRING_BUFFER = Type.getType(StringBuffer.class);
	public static final Type STRING_BUILDER = Type.getType(StringBuilder.class);
	public static final Type MEMBER = Type.getType(Member.class);
	public static final Type UDF = Type.getType(UDF.class);
	public static final Type UDF_PROPERTIES = Type.getType(UDFProperties.class);
	public static final Type UDF_PROPERTIES_IMPL = Type.getType(UDFPropertiesImpl.class);
	public static final Type UDF_IMPL = Type.getType(UDFImpl.class);
	public static final Type CLOSURE = Type.getType(Closure.class);
	public static final Type LAMBDA = Type.getType(Lambda.class);
	public static final Type UDF_PROPERTIES_ARRAY = Type.getType(UDFProperties[].class);
	// public static final Type UDF_IMPL_ARRAY = Type.getType(UDFImpl[].class);
	public static final Type KEY_CONSTANTS = Type.getType(KeyConstants.class);
	public static final Type COLLECTION_KEY = Type.getType(Collection.Key.class);
	public static final Type COLLECTION_KEY_ARRAY = Type.getType(Collection.Key[].class);
	public static final Type UNDEFINED = Type.getType(Undefined.class);
	public static final Type MAP = Type.getType(Map.class);
	public static final Type MAP_ENTRY = Type.getType(Map.Entry.class);
	public static final Type CHAR_ARRAY = Type.getType(char[].class);
	public static final Type IOUTIL = Type.getType(IOUtil.class);
	public static final Type BODY_CONTENT = Type.getType(BodyContent.class);
	public static final Type BODY_CONTENT_UTIL = Type.getType(BodyContentUtil.class);
	public static final Type IMPORT_DEFINITIONS = Type.getType(ImportDefintion.class);
	public static final Type IMPORT_DEFINITIONS_IMPL = Type.getType(ImportDefintionImpl.class);
	public static final Type IMPORT_DEFINITIONS_ARRAY = Type.getType(ImportDefintion[].class);
	public static final Type CI_PAGE = Type.getType(CIPage.class);
	public static final Type CI_PAGE_ARRAY = Type.getType(CIPage[].class);
	public static final Type CLASS = Type.getType(Class.class);
	public static final Type CLASS_ARRAY = Type.getType(Class[].class);
	public static final Type CLASS_LOADER = Type.getType(ClassLoader.class);
	public static final Type BIG_DECIMAL = Type.getType(BigDecimal.class);

	public static final Type FUNCTION_VALUE_IMPL = Type.getType(FunctionValueImpl.class);
	public static final Type CALLER_UTIL = Type.getType(CallerUtil.class);
	public static final Type VARIABLE_UTIL_IMPL = Type.getType(VariableUtilImpl.class);
	public static final Type CONSTANTS = Type.getType(Constants.class);
	public static final Type CONSTANTS_DOUBLE = Type.getType(ConstantsDouble.class);
	public static final Type BODY_TAG = Type.getType(BodyTag.class);
	public static final Type DYNAMIC_ATTRIBUTES = Type.getType(DynamicAttributes.class);
	public static final Type IDENTIFICATION = Type.getType(Identification.class);
	public static final Type TAG_UTIL = Type.getType(TagUtil.class);
	public static final Type FUNCTION_HANDLER_POOL = Type.getType(FunctionHandlerPool.class);
	public static final Type BIF = Type.getType(lucee.runtime.ext.function.BIF.class);
	public static final Type DATA_MEMBER = Type.getType(lucee.runtime.component.DataMember.class);
	public static final Type EXPRESSION_EXCEPTION = Type.getType(ExpressionException.class);
	public static final Type STATIC_STRUCT = Type.getType(StaticStruct.class);

	/**
	 * translate sString classname to a real type
	 * 
	 * @param type
	 * @return
	 * @throws lucee.runtime.exp.TemplateExceptionption
	 */
	public static Type toType(String type) throws TransformerException {
		if (type == null) return OBJECT;
		type = type.trim();
		String lcType = StringUtil.toLowerCase(type);
		char first = lcType.charAt(0);

		switch (first) {
		case 'a':
			if ("any".equals(lcType)) return OBJECT;
			if ("array".equals(lcType)) return ARRAY;
			break;
		case 'b':
			if ("base64".equals(lcType)) return STRING;
			if ("binary".equals(lcType)) return BYTE_VALUE_ARRAY;
			if ("bool".equals(lcType) || "boolean".equals(type)) return BOOLEAN_VALUE;
			if ("boolean".equals(lcType)) return BOOLEAN;
			if ("byte".equals(type)) return BYTE_VALUE;
			if ("byte".equals(lcType)) return BYTE;
			break;
		case 'c':
			if ("char".equals(lcType)) return CHAR;
			if ("character".equals(lcType)) return CHARACTER;
			if ("collection".equals(lcType)) return BYTE_VALUE_ARRAY;
			if ("component".equals(lcType)) return COMPONENT;
			if ("class".equals(lcType)) return COMPONENT;
			break;
		case 'd':
			if ("date".equals(lcType) || "datetime".equals(lcType)) return DATE_TIME;
			if ("decimal".equals(lcType)) return STRING;
			if ("double".equals(type)) return DOUBLE_VALUE;
			if ("double".equals(lcType)) return DOUBLE;
			break;
		case 'e':
			// if("excel".equals(lcType)) return EXCEL;
			break;
		case 'f':
			if ("file".equals(lcType)) return FILE;
			if ("float".equals(type)) return FLOAT_VALUE;
			if ("float".equals(lcType)) return FLOAT;
			if ("function".equals(lcType)) return UDF;
			break;
		case 'i':
			if ("int".equals(lcType)) return INT_VALUE;
			else if ("integer".equals(lcType)) return INTEGER;
			// ext.img else if("image".equals(lcType)) return ImageUtil.getImageType();
			break;
		case 'j':
			if ("java.lang.boolean".equals(lcType)) return BOOLEAN;
			if ("java.lang.byte".equals(lcType)) return BYTE;
			if ("java.lang.character".equals(lcType)) return CHARACTER;
			if ("java.lang.short".equals(lcType)) return SHORT;
			if ("java.lang.integer".equals(lcType)) return INTEGER;
			if ("java.lang.long".equals(lcType)) return LONG;
			if ("java.lang.float".equals(lcType)) return FLOAT;
			if ("java.lang.double".equals(lcType)) return DOUBLE;
			if ("java.io.file".equals(lcType)) return FILE;
			if ("java.lang.string".equals(lcType)) return STRING;
			if ("java.lang.string[]".equals(lcType)) return STRING_ARRAY;
			if ("java.util.date".equals(lcType)) return DATE;
			if ("java.lang.object".equals(lcType)) return OBJECT;
			break;
		case 'l':
			if ("long".equals(type)) return LONG_VALUE;
			if ("long".equals(lcType)) return LONG;
			if ("locale".equals(lcType)) return LOCALE;
			if ("lucee.runtime.type.Collection$Key".equals(type)) return COLLECTION_KEY;
			break;
		case 'n':
			if ("node".equals(lcType)) return NODE;
			if ("number".equals(lcType)) return DOUBLE_VALUE;
			if ("numeric".equals(lcType)) return DOUBLE_VALUE;
			break;
		case 'o':
			if ("object".equals(lcType)) return OBJECT;
			break;
		case 'q':
			if ("query".equals(lcType)) return QUERY;
			if ("querycolumn".equals(lcType)) return QUERY_COLUMN;
			break;
		case 's':
			if ("string".equals(lcType)) return STRING;
			if ("struct".equals(lcType)) return STRUCT;
			if ("short".equals(type)) return SHORT_VALUE;
			if ("short".equals(lcType)) return SHORT;
			break;
		case 't':
			if ("timezone".equals(lcType)) return TIMEZONE;
			if ("timespan".equals(lcType)) return TIMESPAN;
			break;
		case 'u':
			if ("udf".equals(lcType)) return UDF;
			break;
		case 'v':
			if ("void".equals(lcType)) return VOID;
			if ("variablestring".equals(lcType)) return STRING;
			if ("variable_string".equals(lcType)) return STRING;
			break;
		case 'x':
			if ("xml".equals(lcType)) return NODE;
			break;
		case '[':
			if ("[Ljava.lang.String;".equals(lcType)) return STRING_ARRAY;
			break;

		}
		// TODO Array als Lbyte und auch byte[]

		try {
			return Type.getType(ClassUtil.loadClass(type));
		}
		catch (ClassException e) {
			throw new TransformerException(e, null);
		}
	}

	/**
	 * returns if given type is a "primitve" type or in other words a value type (no reference type, no
	 * object)
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isPrimitiveType(int type) {
		return type != _OBJECT && type != _STRING;
	}

	/**
	 * returns if given type is a "primitve" type or in other words a value type (no reference type, no
	 * object)
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isPrimitiveType(Type type) {
		String className = type.getClassName();
		if (className.indexOf('.') != -1) return false;

		if ("boolean".equals(className)) return true;
		if ("short".equals(className)) return true;
		if ("float".equals(className)) return true;
		if ("long".equals(className)) return true;
		if ("double".equals(className)) return true;
		if ("char".equals(className)) return true;
		if ("int".equals(className)) return true;
		if ("byte".equals(className)) return true;

		return false;
	}

	public static int getType(Type type) {
		String className = type.getClassName();

		// if (!className.equals("java.lang.Object")) print.e(" ---> " + className);

		if (className.indexOf('.') != -1) {
			if ("java.lang.String".equalsIgnoreCase(className)) return _STRING;
			return _OBJECT;
		}

		if ("boolean".equals(className)) return _BOOLEAN;
		if ("short".equals(className)) return _SHORT;
		if ("float".equals(className)) return _FLOAT;
		if ("long".equals(className)) return _LONG;
		if ("double".equals(className)) return _DOUBLE;
		if ("char".equals(className)) return _CHAR;
		if ("int".equals(className)) return _INT;
		if ("byte".equals(className)) return _BYTE;

		return _OBJECT;
	}

	public static Type toRefType(Type type) {
		String className = type.getClassName();
		if (className.indexOf('.') != -1) return type;

		if ("boolean".equals(className)) return BOOLEAN;
		if ("short".equals(className)) return SHORT;
		if ("float".equals(className)) return FLOAT;
		if ("long".equals(className)) return LONG;
		if ("double".equals(className)) return DOUBLE;
		if ("char".equals(className)) return CHARACTER;
		if ("int".equals(className)) return INT_VALUE;
		if ("byte".equals(className)) return BYTE;
		return type;
	}

	public static Class toClass(Type type) throws ClassException {
		if (Types.STRING.equals(type)) return String.class;
		if (Types.BOOLEAN_VALUE.equals(type)) return boolean.class;
		if (Types.DOUBLE_VALUE.equals(type)) return double.class;
		if (Types.PAGE_CONTEXT.equals(type)) return PageContext.class;
		if (Types.OBJECT.equals(type)) return Object.class;
		if (Types.STRUCT.equals(type)) return Struct.class;
		if (Types.ARRAY.equals(type)) return Array.class;
		if (Types.COLLECTION_KEY.equals(type)) return Collection.Key.class;
		if (Types.COLLECTION_KEY_ARRAY.equals(type)) return Collection.Key[].class;
		if (Types.QUERY.equals(type)) return Query.class;
		if (Types.DATE_TIME.equals(type)) return lucee.runtime.type.dt.DateTime.class;
		if (Types.TIMESPAN.equals(type)) return TimeSpan.class;
		if (Types.QUERY_COLUMN.equals(type)) return QueryColumn.class;
		if (Types.NODE.equals(type)) return Node.class;
		if (Types.TIMEZONE.equals(type)) return TimeZone.class;
		if (Types.LOCALE.equals(type)) return Locale.class;
		if (Types.UDF.equals(type)) return UDF.class;
		/*
		 * if(Types.IMAGE.equals(type)) { Class clazz = ImageUtil.getImageClass(); if(clazz!=null) return
		 * clazz; throw new
		 * PageRuntimeException("Cannot provide Image class, you neeed to install the Image Extension to do so."
		 * ); }
		 */
		if (Types.BYTE_VALUE_ARRAY.equals(type)) return byte[].class;
		return ClassUtil.toClass(type.getClassName());
	}

}