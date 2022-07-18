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
package lucee.runtime.util;

import java.io.IOException;

import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.db.SQL;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;

/**
 * class to get exceptions of different types
 */
public interface Excepton {

	/**
	 * Field <code>TYPE_ABORT</code>
	 */
	public static final int TYPE_ABORT = 0;
	/**
	 * Field <code>TYPE_ABORT_EXP</code>
	 */
	public static final int TYPE_ABORT_EXP = 1;
	/**
	 * Field <code>TYPE_APPLICATION_EXP</code>
	 */
	public static final int TYPE_APPLICATION_EXP = 2;
	/**
	 * Field <code>TYPE_CASTER_EXP</code>
	 */
	public static final int TYPE_CASTER_EXP = 3;
	/**
	 * Field <code>TYPE_CUSTOM_TYPE_EXP</code>
	 */
	public static final int TYPE_CUSTOM_TYPE_EXP = 4;
	/**
	 * Field <code>TYPE_DATABASE_EXP</code>
	 */
	public static final int TYPE_DATABASE_EXP = 5;
	/**
	 * Field <code>TYPE_EXPRESSION_EXP</code>
	 */
	public static final int TYPE_EXPRESSION_EXP = 6;
	/**
	 * Field <code>TYPE_FUNCTION_EXP</code>
	 */
	public static final int TYPE_FUNCTION_EXP = 7;
	/**
	 * Field <code>TYPE_LOCK_EXP</code>
	 */
	public static final int TYPE_LOCK_EXP = 8;
	/**
	 * Field <code>TYPE_MISSING_INCLUDE_EXP</code>
	 */
	public static final int TYPE_MISSING_INCLUDE_EXP = 9;
	/**
	 * Field <code>TYPE_NATIVE_EXP</code>
	 */
	public static final int TYPE_NATIVE_EXP = 10;
	/**
	 * Field <code>TYPE_SECURITY_EXP</code>
	 */
	public static final int TYPE_SECURITY_EXP = 11;
	/**
	 * Field <code>TYPE_TEMPLATE_EXP</code>
	 */
	public static final int TYPE_TEMPLATE_EXP = 12;
	/**
	 * Field <code>TYPE_XML_EXP</code>
	 */
	public static final int TYPE_XML_EXP = 13;

	/**
	 * create exception "Abort"
	 * 
	 * @return Abort
	 */
	public PageException createAbort();

	/**
	 * create exception "AbortException"
	 * 
	 * @param showError show error
	 * @return AbortException
	 */
	public PageException createAbortException(String showError);

	/**
	 * create exception "ApplicationException"
	 * 
	 * @param message Message
	 * @return ApplicationException
	 */
	public PageException createApplicationException(String message);

	/**
	 * create exception "ApplicationException"
	 * 
	 * @param message Message
	 * @param detail Detail
	 * @return ApplicationException
	 */
	public PageException createApplicationException(String message, String detail);

	/**
	 * create exception "CasterException"
	 * 
	 * @param message Message
	 * @return CasterException
	 */
	public PageException createCasterException(String message);

	public PageException createCasterException(Object obj, String className);

	public PageException createCasterException(Object obj, Class clazz);

	/**
	 * create exception "CustomTypeException"
	 * 
	 * @param message Message
	 * @param detail Detail
	 * @param errorcode Error Code
	 * @param customType Custom Type
	 * @return CustomTypeException
	 * @deprecated use instead
	 *             <code>createCustomTypeException(String message, String detail, String errorcode, String customType, String extendedInfo);</code>
	 */
	@Deprecated
	public PageException createCustomTypeException(String message, String detail, String errorcode, String customType);

	public PageException createCustomTypeException(String message, String detail, String errorcode, String customType, String extendedInfo);

	/**
	 * create exception "DatabaseException"
	 * 
	 * @param message Message
	 * @return DatabaseException
	 */
	public PageException createDatabaseException(String message);

	/**
	 * create exception "DatabaseException"
	 * 
	 * @param message Message
	 * @param detail Detail
	 * @return DatabaseException
	 */
	public PageException createDatabaseException(String message, String detail);

	/**
	 * create exception "DatabaseException"
	 * 
	 * @param message Message
	 * @param sql SQL
	 * @return DatabaseException
	 */
	public PageException createDatabaseException(String message, SQL sql);

	/**
	 * create exception "ExpressionException"
	 * 
	 * @param message Message
	 * @return ExpressionException
	 */
	public PageException createExpressionException(String message);

	/**
	 * create exception "ExpressionException"
	 * 
	 * @param message Message
	 * @param detail Detail
	 * @return ExpressionException
	 */
	public PageException createExpressionException(String message, String detail);

	/**
	 * create exception "FunctionException"
	 * 
	 * @param pc Page Context
	 * @param functionName Function Name
	 * @param badArgumentPosition Bad Argument Position
	 * @param badArgumentName Bad Argument Name
	 * @param message Message
	 * @return FunctionException
	 * @deprecated use instead
	 *             <code>createFunctionException(PageContext pc,String functionName, int badArgumentPosition, String badArgumentName, String message, String detail))</code>
	 */
	@Deprecated
	public PageException createFunctionException(PageContext pc, String functionName, String badArgumentPosition, String badArgumentName, String message);

	/**
	 * create exception "FunctionException"
	 * 
	 * @param pc Page Context
	 * @param functionName Function Name
	 * @param badArgumentPosition Bad Argument Position
	 * @param badArgumentName Bad Argument Name
	 * @param message Message
	 * @param detail Detail
	 * @return FunctionException
	 */
	public PageException createFunctionException(PageContext pc, String functionName, int badArgumentPosition, String badArgumentName, String message, String detail);

	/**
	 * create exception "LockException"
	 * 
	 * @param operation operation
	 * @param name name
	 * @param message Message
	 * @return LockException
	 */
	public PageException createLockException(String operation, String name, String message);

	/**
	 * create exception "LockException"
	 * 
	 * @param ps Page Source
	 * @return LockException
	 */
	public PageException createMissingIncludeException(PageSource ps);

	/**
	 * create exception "NativeException"
	 * 
	 * @param t Throwable
	 * @return NativeException
	 */
	public PageException createNativeException(Throwable t);

	/**
	 * create exception "SecurityException"
	 * 
	 * @param message Message
	 * @return SecurityException
	 */
	public PageException createSecurityException(String message);

	/**
	 * create exception "SecurityException"
	 * 
	 * @param message Message
	 * @param detail Detail
	 * @return SecurityException
	 */
	public PageException createSecurityException(String message, String detail);

	/**
	 * create exception "TemplateException"
	 * 
	 * @param message Message
	 * @return TemplateException
	 */
	public PageException createTemplateException(String message);

	/**
	 * create exception "TemplateException"
	 * 
	 * @param message Message
	 * @param detail Detail
	 * @return TemplateException
	 */
	public PageException createTemplateException(String message, String detail);

	/**
	 * create exception "XMLException"
	 * 
	 * @param message Message
	 * @return XMLException
	 */
	public PageException createXMLException(String message);

	/**
	 * create exception "XMLException"
	 * 
	 * @param message Message
	 * @param detail Detail
	 * @return XMLException
	 */
	public PageException createXMLException(String message, String detail);

	/**
	 * check if exception is of given type
	 * 
	 * @param type type to check
	 * @param t exception to check
	 * @return is of type
	 */
	public boolean isOfType(int type, Throwable t);

	public String similarKeyMessage(Collection.Key[] keys, String keySearched, String keyLabel, String keyLabels, String in, boolean listAll);

	public RuntimeException createPageRuntimeException(PageException pe);

	public PageException createFunctionException(PageContext pc, String functionName, int min, int max, int actual);

	public IOException toIOException(Throwable t);

}