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
package lucee.runtime.orm;

import java.lang.reflect.Method;

import lucee.commons.lang.ExceptionUtil;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.Component;
import lucee.runtime.db.DataSource;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

public class ORMExceptionUtil {

	private static Method setAdditional;

	public static PageException createException(ORMSession session, Component cfc, Throwable t) {
		PageException pe = Caster.toPageException(t);// CFMLEngineFactory.getInstance().getExceptionUtil().createApplicationException(t.getMessage());
		pe.setStackTrace(t.getStackTrace());
		if (session != null) setAddional(session, pe);
		if (cfc != null) setContext(pe, cfc);
		return pe;
	}

	public static PageException createException(ORMSession session, Component cfc, String message, String detail) {
		PageException pe = CFMLEngineFactory.getInstance().getExceptionUtil().createApplicationException(message);
		if (session != null) setAddional(session, pe);
		if (cfc != null) setContext(pe, cfc);
		return pe;
	}

	private static void setContext(PageException pe, Component cfc) {
		if (cfc != null && getPageDeep(pe) == 0) pe.addContext(cfc.getPageSource(), 1, 1, null);
	}

	private static void setAddional(ORMSession session, PageException pe) {
		String[] names = session.getEntityNames();

		setAdditional(pe, KeyConstants._Entities, ListUtil.arrayToList(names, ", "));
		setAddional(pe, session.getDataSources());
	}

	private static void setAddional(PageException pe, DataSource... sources) {
		if (sources != null && sources.length > 0) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < sources.length; i++) {
				if (i > 0) sb.append(", ");
				sb.append(sources[i].getName());
			}
			setAdditional(pe, KeyConstants._Datasource, sb.toString());
		}
	}

	private static int getPageDeep(PageException pe) {
		StackTraceElement[] traces = getStackTraceElements(pe);

		String template = "", tlast;
		StackTraceElement trace = null;
		int index = 0;
		for (int i = 0; i < traces.length; i++) {
			trace = traces[i];
			tlast = template;
			template = trace.getFileName();
			if (trace.getLineNumber() <= 0 || template == null || CFMLEngineFactory.getInstance().getResourceUtil().getExtension(template, "").equals("java")) continue;
			if (!(tlast == null ? "" : tlast).equals(template)) index++;

		}
		return index;
	}

	private static StackTraceElement[] getStackTraceElements(Throwable t) {
		StackTraceElement[] st = getStackTraceElements(t, true);
		if (st == null) st = getStackTraceElements(t, false);
		return st;
	}

	private static StackTraceElement[] getStackTraceElements(Throwable t, boolean onlyWithCML) {
		StackTraceElement[] st;
		Throwable cause = t.getCause();
		if (cause != null) {
			st = getStackTraceElements(cause, onlyWithCML);
			if (st != null) return st;
		}

		st = t.getStackTrace();
		if (!onlyWithCML || hasCFMLinStacktrace(st)) {
			return st;
		}
		return null;
	}

	private static boolean hasCFMLinStacktrace(StackTraceElement[] traces) {
		for (int i = 0; i < traces.length; i++) {
			if (traces[i].getFileName() != null && !traces[i].getFileName().endsWith(".java")) return true;
		}
		return false;
	}

	public static void setAdditional(PageException pe, Key name, Object value) {
		try {
			if (setAdditional == null || setAdditional.getDeclaringClass() != pe.getClass()) {
				setAdditional = pe.getClass().getMethod("setAdditional", new Class[] { Key.class, Object.class });
			}
			setAdditional.invoke(pe, new Object[] { name, value });
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}
}