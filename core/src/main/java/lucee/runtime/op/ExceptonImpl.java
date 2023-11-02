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
package lucee.runtime.op;

import java.io.IOException;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.db.SQL;
import lucee.runtime.exp.Abort;
import lucee.runtime.exp.AbortException;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.CustomTypeException;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.LockException;
import lucee.runtime.exp.MissingIncludeException;
import lucee.runtime.exp.NativeException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.exp.SecurityException;
import lucee.runtime.exp.TemplateException;
import lucee.runtime.exp.XMLException;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.type.Collection;
import lucee.runtime.util.Excepton;

/**
 * Implementation of Exception Util
 */
public final class ExceptonImpl implements Excepton {

	private static Class[] exceptions = new Class[14];

	static {
		exceptions[TYPE_ABORT] = Abort.class;
		exceptions[TYPE_ABORT_EXP] = AbortException.class;
		exceptions[TYPE_APPLICATION_EXP] = ApplicationException.class;
		exceptions[TYPE_CASTER_EXP] = CasterException.class;
		exceptions[TYPE_CUSTOM_TYPE_EXP] = CustomTypeException.class;
		exceptions[TYPE_DATABASE_EXP] = DatabaseException.class;
		exceptions[TYPE_EXPRESSION_EXP] = ExpressionException.class;
		exceptions[TYPE_FUNCTION_EXP] = FunctionException.class;
		exceptions[TYPE_LOCK_EXP] = LockException.class;
		exceptions[TYPE_MISSING_INCLUDE_EXP] = MissingIncludeException.class;
		exceptions[TYPE_NATIVE_EXP] = NativeException.class;
		exceptions[TYPE_SECURITY_EXP] = SecurityException.class;
		exceptions[TYPE_TEMPLATE_EXP] = TemplateException.class;
		exceptions[TYPE_XML_EXP] = XMLException.class;
	}

	private static ExceptonImpl singelton;

	/**
	 * @return singleton instance
	 */
	public static Excepton getInstance() {
		if (singelton == null) singelton = new ExceptonImpl();
		return singelton;
	}

	@Override
	public PageException createAbort() {
		return new Abort(Abort.SCOPE_REQUEST);
	}

	@Override
	public PageException createAbortException(String showError) {
		return new AbortException(showError);
	}

	@Override
	public PageException createApplicationException(String message) {
		return new ApplicationException(message);
	}

	@Override
	public PageException createApplicationException(String message, String detail) {
		return new ApplicationException(message, detail);
	}

	@Override
	public PageException createCasterException(String message) {
		return new CasterException(message);
	}

	@Override
	public PageException createCasterException(Object obj, String className) {
		return new CasterException(obj, className);
	}

	@Override
	public PageException createCasterException(Object obj, Class clazz) {
		return new CasterException(obj, clazz);
	}

	@Override
	public PageException createCustomTypeException(String message, String detail, String errorcode, String customType) {
		return createCustomTypeException(message, detail, errorcode, customType, null);
	}

	@Override
	public PageException createCustomTypeException(String message, String detail, String errorcode, String customType, String extendedInfo) {
		return new CustomTypeException(message, detail, errorcode, customType, extendedInfo);
	}

	@Override
	public PageException createDatabaseException(String message) {
		return new DatabaseException(message, null, null, null);
	}

	@Override
	public PageException createDatabaseException(String message, String detail) {
		return new DatabaseException(message, detail, null, null);
	}

	@Override
	public PageException createDatabaseException(String message, SQL sql) {
		return new DatabaseException(message, null, sql, null);
	}

	@Override
	public PageException createExpressionException(String message) {
		return new ExpressionException(message);
	}

	@Override
	public PageException createExpressionException(String message, String detail) {
		return new ExpressionException(message, detail);
	}

	@Override
	public PageException createFunctionException(PageContext pc, String functionName, String badArgumentPosition, String badArgumentName, String message) {
		return new FunctionException(pc, functionName, badArgumentPosition, badArgumentName, message, null);
	}

	@Override
	public PageException createFunctionException(PageContext pc, String functionName, int badArgumentPosition, String badArgumentName, String message, String detail) {
		return new FunctionException(pc, functionName, badArgumentPosition, badArgumentName, message, detail);
	}

	@Override
	public PageException createFunctionException(PageContext pc, String functionName, int min, int max, int actual) {
		return new FunctionException(pc, functionName, min, max, actual);
	}

	@Override
	public PageException createLockException(String operation, String name, String message) {
		return new LockException(operation, name, message);
	}

	@Override
	public PageException createMissingIncludeException(PageSource ps) {
		return new MissingIncludeException(ps);
	}

	@Override
	public PageException createNativeException(Throwable t) {
		return NativeException.newInstance(t);
	}

	@Override
	public PageException createSecurityException(String message) {
		return new SecurityException(message);
	}

	@Override
	public PageException createSecurityException(String message, String detail) {
		return new SecurityException(message, detail);
	}

	@Override
	public PageException createTemplateException(String message) {
		return new TemplateException(message);
	}

	@Override
	public PageException createTemplateException(String message, String detail) {
		return new TemplateException(message, detail);
	}

	@Override
	public PageException createXMLException(String message) {
		return new XMLException(message);
	}

	@Override
	public PageException createXMLException(String message, String detail) {
		return new XMLException(message, detail);
	}

	@Override

	public boolean isOfType(int type, Throwable t) {
		switch (type) {
		case TYPE_ABORT:
			return Abort.isSilentAbort(t);
		case TYPE_ABORT_EXP:
			return t instanceof AbortException;
		case TYPE_APPLICATION_EXP:
			return t instanceof ApplicationException;
		case TYPE_CASTER_EXP:
			return t instanceof CasterException;
		case TYPE_CUSTOM_TYPE_EXP:
			return t instanceof CustomTypeException;
		case TYPE_DATABASE_EXP:
			return t instanceof DatabaseException;
		case TYPE_EXPRESSION_EXP:
			return t instanceof ExpressionException;
		case TYPE_FUNCTION_EXP:
			return t instanceof FunctionException;
		case TYPE_LOCK_EXP:
			return t instanceof LockException;
		case TYPE_MISSING_INCLUDE_EXP:
			return t instanceof MissingIncludeException;
		case TYPE_NATIVE_EXP:
			return t instanceof NativeException;
		case TYPE_SECURITY_EXP:
			return t instanceof SecurityException;
		case TYPE_TEMPLATE_EXP:
			return t instanceof TemplateException;
		case TYPE_XML_EXP:
			return t instanceof XMLException;
		}
		return Reflector.isInstaneOf(t.getClass(), exceptions[type], false);
	}

	@Override
	public String similarKeyMessage(Collection.Key[] keys, String keySearched, String keyLabel, String keyLabels, String in, boolean listAll) {
		return ExceptionUtil.similarKeyMessage(keys, keySearched, keyLabel, keyLabels, in, listAll);
	}

	@Override
	public RuntimeException createPageRuntimeException(PageException pe) {
		return new PageRuntimeException(pe);
	}

	@Override
	public IOException toIOException(Throwable t) {
		return ExceptionUtil.toIOException(t);
	}
}