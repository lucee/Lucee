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

	public static NativeException newInstance(Throwable t) {
		return newInstance(t, true);
	}

	public static NativeException newInstance(Throwable t, boolean rethrowIfNecessary) {
		if (rethrowIfNecessary && t instanceof ThreadDeath) // never ever catch this
			throw (ThreadDeath) t;
		return new NativeException(t);
	}

	private static Throwable getRootCause(Throwable t) {
		Throwable c;
		do {
			c = t.getCause();
			if (c == null || c == t) return t;
			t = c;

		}
		while (true);
	}

	private boolean hasLuceeRuntime(StackTraceElement[] st) {
		if (st != null) for (int i = 0; i < st.length; i++) {
			if (st[i].getClassName().indexOf("lucee.runtime") != -1) return true;
		}
		return false;
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