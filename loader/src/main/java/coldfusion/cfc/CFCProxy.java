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
package coldfusion.cfc;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.util.Cast;
import lucee.runtime.util.Creation;

public class CFCProxy {

	private final CFMLEngine engine;
	private final Cast caster;
	private final Creation creator;

	private Component cfc = null;
	private final String path;
	private Map thisData;
	private boolean invokeDirectly = true;
	private boolean autoFlush;

	public CFCProxy(final String path) throws Throwable {
		this(path, null, true);
	}

	public CFCProxy(final String path, final boolean invokeDirectly) throws Throwable {
		this(path, null, invokeDirectly);
	}

	public CFCProxy(final String path, final Map initialThis) throws Throwable {
		this(path, initialThis, true);
	}

	public CFCProxy(final String path, final Map initialThis, final boolean invokeDirectly) throws Throwable {
		engine = CFMLEngineFactory.getInstance();
		caster = engine.getCastUtil();
		creator = engine.getCreationUtil();

		this.path = path;
		this.invokeDirectly = invokeDirectly;
		setThisScope(initialThis);
	}

	private void initCFC(PageContext pc) {
		if (cfc == null && (invokeDirectly || pc != null)) try {
			if (pc == null) pc = engine.getThreadPageContext();
			cfc = engine.getCreationUtil().createComponentFromPath(pc, path);
		}
		catch (final PageException pe) {}
	}

	@SuppressWarnings("rawtypes")
	public void setThisScope(final Map data) {
		if (data != null) {
			if (thisData == null) this.thisData = new HashMap();

			final Iterator<Entry> it = data.entrySet().iterator();
			Entry entry;
			while (it.hasNext()) {
				entry = it.next();
				thisData.put(entry.getKey(), entry.getValue());
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public Map getThisScope() {
		initCFC(null);
		if (cfc == null) return null;

		final Struct rtn = creator.createStruct();
		final Iterator<Entry<Key, Object>> it = cfc.entryIterator();
		Entry<Key, Object> entry;
		while (it.hasNext()) {
			entry = it.next();
			rtn.setEL(entry.getKey(), entry.getValue());
		}
		return rtn;
	}

	public final Object invoke(final String methodName, final Object args[]) throws Throwable {
		if (invokeDirectly) return _invoke(methodName, args);
		return _invoke(methodName, args, null, null, null);
	}

	public final Object invoke(final String methodName, final Object args[], final HttpServletRequest request, final HttpServletResponse response) throws Throwable {
		if (invokeDirectly) return _invoke(methodName, args);
		return _invoke(methodName, args, request, response, null);
	}

	public final Object invoke(final String methodName, final Object args[], final HttpServletRequest request, final HttpServletResponse response, final OutputStream out)
			throws Throwable {
		if (invokeDirectly) return _invoke(methodName, args);
		return _invoke(methodName, args, request, response, out);
	}

	public static boolean inInvoke() {
		return false;
	}

	private Object _invoke(final String methodName, final Object[] args) throws PageException {
		final CFMLEngine engine = CFMLEngineFactory.getInstance();
		final PageContext pc = engine.getThreadPageContext();
		initCFC(pc);
		return cfc.call(pc, methodName, args);
	}

	private Object _invoke(final String methodName, final Object[] args, HttpServletRequest req, HttpServletResponse rsp, OutputStream out) throws PageException {
		final CFMLEngine engine = CFMLEngineFactory.getInstance();
		final Creation creator = engine.getCreationUtil();
		final PageContext originalPC = engine.getThreadPageContext();

		// no OutputStream
		if (out == null) out = DevNullOutputStream.DEV_NULL_OUTPUT_STREAM;

		// no Request
		if (req == null)
			// TODO new File
			req = creator.createHttpServletRequest(new File("."), "Lucee", "/", "", null, null, null, null, null);
		// noRespone
		if (rsp == null) rsp = creator.createHttpServletResponse(out);

		final PageContext pc = creator.createPageContext(req, rsp, out);
		try {
			engine.registerThreadPageContext(pc);
			initCFC(pc);
			return cfc.call(pc, methodName, args);
		}
		finally {
			if (autoFlush) try {
				pc.getRootWriter().flush();
			}
			catch (final Throwable t) {}
			engine.registerThreadPageContext(originalPC);
		}
	}

	public void flush() throws IOException {
		final CFMLEngine engine = CFMLEngineFactory.getInstance();
		final PageContext pc = engine.getThreadPageContext();
		pc.getRootWriter().flush();
	}

	public void setAutoFlush(final boolean autoFlush) {
		this.autoFlush = autoFlush;
	}

	public void setApplicationExecution(final boolean doApp) {
		// executeApplication = doApp;
	}

}

final class DevNullOutputStream extends OutputStream implements Serializable {

	private static final long serialVersionUID = -4707810151743493285L;

	public static final DevNullOutputStream DEV_NULL_OUTPUT_STREAM = new DevNullOutputStream();

	/**
	 * Constructor of the class
	 */
	private DevNullOutputStream() {}

	/**
	 * @see java.io.OutputStream#close()
	 */
	@Override
	public void close() {}

	/**
	 * @see java.io.OutputStream#flush()
	 */
	@Override
	public void flush() {}

	/**
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
	public void write(final byte[] b, final int off, final int len) {}

	/**
	 * @see java.io.OutputStream#write(byte[])
	 */
	@Override
	public void write(final byte[] b) {}

	/**
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(final int b) {}

}