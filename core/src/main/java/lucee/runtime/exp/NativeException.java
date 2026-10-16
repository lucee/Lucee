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
package lucee.runtime.exp;

import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.Constants;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.op.Caster;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.type.Collection;

/**
 * Box a Native Exception, Native = !PageException
 */
public class NativeException extends PageExceptionImpl {

	private static final long serialVersionUID = 6221156691846424801L;

	private Throwable t;

	/**
	 * Standart constructor for native Exception class
	 * 
	 * @param t Throwable
	 */
	protected NativeException(Throwable t) {
		super(t, t.getClass().getName());
		this.t = t;
		// set stacktrace

		/*
		 * StackTraceElement[] st = getRootCause(t).getStackTrace();
		 * if(hasLuceeRuntime(st))setStackTrace(st); else { StackTraceElement[] cst = new
		 * Exception().getStackTrace(); if(hasLuceeRuntime(cst)){ StackTraceElement[] mst=new
		 * StackTraceElement[st.length+cst.length-1]; System.arraycopy(st, 0, mst, 0, st.length);
		 * System.arraycopy(cst, 1, mst, st.length, cst.length-1);
		 * 
		 * setStackTrace(mst); } else setStackTrace(st); }
		 */
	}

	public static NativeException getInstance(Throwable t) {
		return getInstance(t, true);
	}

	public static NativeException getInstance(Throwable t, boolean rethrowIfNecessary) {
		if (rethrowIfNecessary) ExceptionUtil.rethrowIfNecessary(t);
		return new NativeException(wrapIfNeeded(t));
	}

	private static Throwable wrapIfNeeded(Throwable t) {
		if (t == null) return t;

		String message = t.getMessage();
		if (StringUtil.isEmpty(message, true)) return t;

		// Handle NoClassDefFoundError (e.g., "javax/servlet/jsp/tagext/TryCatchFinally")
		if (t instanceof NoClassDefFoundError) {
			String className = extractClassName(message);
			if (isServletClass(className)) {
				return new JavaxNeededException(t, className);
			}
		}

		// Handle ClassNotFoundException (e.g., "javax.servlet.jsp.tagext.TryCatchFinally not found by
		// redis.extension [98]")
		if (t instanceof ClassNotFoundException) {
			String className = extractClassNameFromCNFE(message);
			if (isServletClass(className)) {
				return new JavaxNeededException(t, className);
			}
		}

		// Recursively check the cause
		Throwable cause = t.getCause();
		if (cause != null && cause != t) {
			Throwable wrappedCause = wrapIfNeeded(cause);
			if (wrappedCause != cause) {
				return wrappedCause;
			}
		}

		return t;
	}

	private static boolean isServletClass(String className) {
		return className != null && (className.startsWith("javax.servlet.") || className.startsWith("jakarta.servlet."));
	}

	private static String extractClassName(String message) {
		if (message == null) return null;

		// NoClassDefFoundError format: "javax/servlet/jsp/tagext/TryCatchFinally"
		// Convert slashes to dots
		String className = message.replace('/', '.');

		// Remove any trailing text (sometimes there's extra info)
		int spaceIdx = className.indexOf(' ');
		if (spaceIdx > 0) {
			className = className.substring(0, spaceIdx);
		}

		return className.trim();
	}

	private static String extractClassNameFromCNFE(String message) {
		if (message == null) return null;

		// ClassNotFoundException format: "javax.servlet.jsp.tagext.TryCatchFinally not found by
		// redis.extension [98]"
		// Extract the class name before "not found" or similar phrases
		String[] stopPhrases = { " not found", " cannot be found", " could not be found" };

		String className = message;
		for (String phrase: stopPhrases) {
			int idx = message.indexOf(phrase);
			if (idx > 0) {
				className = message.substring(0, idx).trim();
				break;
			}
		}

		return className;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		DumpData data = super.toDumpData(pageContext, maxlevel, dp);
		if (data instanceof DumpTable) ((DumpTable) data)
				.setTitle(Constants.NAME + " [" + pageContext.getConfig().getFactory().getEngine().getInfo().getVersion() + "] - Error (" + Caster.toClassName(t) + ")");

		return data;
	}

	@Override
	public boolean typeEqual(String type) {
		if (super.typeEqual(type)) return true;
		return Reflector.isInstaneOfIgnoreCase(t.getClass(), type);
	}

	@Override
	public void setAdditional(Collection.Key key, Object value) {
		super.setAdditional(key, value);
	}

	public Throwable getException() {
		return t;
	}
}