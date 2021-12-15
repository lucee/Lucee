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
package lucee.runtime.jsr223;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.runtime.PageContext;
import lucee.runtime.compiler.Renderer;
import lucee.runtime.compiler.Renderer.Result;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.scope.Variables;
import lucee.runtime.util.PageContextUtil;

public class ScriptEngineImpl implements ScriptEngine {

	private ScriptEngineFactoryImpl factory;
	private ScriptContext context;
	private PageContext pc;

	public ScriptEngineImpl(ScriptEngineFactoryImpl factory) {
		this.factory = factory;
		pc = createPageContext();

	}

	@Override
	public Object eval(String script, ScriptContext context) throws ScriptException {
		if (context == null) context = getContext();

		PageContext oldPC = ThreadLocalPageContext.get();
		PageContext pc = getPageContext(context);
		try {
			Result res = factory.tag ? Renderer.tag(pc, script, factory.dialect, false, true) : Renderer.script(pc, script, factory.dialect, false, true);
			return res.getValue();
		}
		catch (PageException pe) {
			throw toScriptException(pe);
		}
		finally {
			releasePageContext(pc, oldPC);
		}
	}

	@Override
	public void put(String key, Object value) {
		PageContext oldPC = ThreadLocalPageContext.get();
		PageContext pc = getPageContext(getContext());
		try {
			pc.undefinedScope().set(KeyImpl.init(key), value);
		}
		catch (PageException e) {
			// ignored
		}
		finally {
			releasePageContext(pc, oldPC);
		}

	}

	@Override
	public Object get(String key) {
		PageContext oldPC = ThreadLocalPageContext.get();
		PageContext pc = getPageContext(getContext());
		try {
			return pc.undefinedScope().get(KeyImpl.init(key), null);
		}
		finally {
			releasePageContext(pc, oldPC);
		}
	}

	@Override
	public Bindings getBindings(int scope) {
		return getContext().getBindings(scope);
	}

	@Override
	public void setBindings(Bindings bindings, int scope) {
		getContext().setBindings(bindings, scope);
	}

	private ScriptException toScriptException(Exception e) {
		ScriptException se = new ScriptException(e);
		se.setStackTrace(e.getStackTrace());
		return se;
	}

	@Override
	public ScriptContext getContext() {
		if (context == null) {
			context = new SimpleScriptContext();
			context.setBindings(new VariablesBinding(), ScriptContext.ENGINE_SCOPE); // we do our own
		}
		return context;
	}

	private ScriptContext getContext(Bindings b) {
		ScriptContext def = getContext();
		SimpleScriptContext custom = new SimpleScriptContext();
		Bindings gs = getBindings(ScriptContext.GLOBAL_SCOPE);
		if (gs != null) custom.setBindings(gs, ScriptContext.GLOBAL_SCOPE);

		custom.setBindings(b, ScriptContext.ENGINE_SCOPE);
		custom.setReader(def.getReader());
		custom.setWriter(def.getWriter());
		custom.setErrorWriter(def.getErrorWriter());
		return custom;
	}

	@Override
	public void setContext(ScriptContext context) {
		this.context = context;
	}

	@Override
	public Bindings createBindings() {
		return new VariablesBinding();
	}

	private PageContext getPageContext(ScriptContext context) {
		pc.setVariablesScope(toVariables(context.getBindings(ScriptContext.ENGINE_SCOPE)));
		ThreadLocalPageContext.register(pc);
		return pc;
	}

	private void releasePageContext(PageContext pc, PageContext oldPC) {
		pc.flush();
		ThreadLocalPageContext.release();
		if (oldPC != null) ThreadLocalPageContext.register(oldPC);
	}

	private Variables toVariables(Bindings bindings) {
		if (bindings instanceof VariablesBinding) return ((VariablesBinding) bindings).getVaraibles();
		RuntimeException t = new RuntimeException("not supported! " + bindings.getClass().getName());
		throw t;
		// return new BindingsAsVariables(bindings);
	}

	private PageContext createPageContext() {
		try {
			File root = new File(factory.engine.getCFMLEngineFactory().getResourceRoot(), "jsr223-webroot");
			return PageContextUtil.getPageContext(null, null, root, "localhost", "/index.cfm", "", null, null, null, null, CFMLEngineImpl.CONSOLE_OUT, false, Long.MAX_VALUE,
					Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.ignore.scopes", null), false));
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	///////////// calling other methods of the same class /////////////////

	@Override
	public Object eval(Reader reader, ScriptContext context) throws ScriptException {
		try {
			return eval(IOUtil.toString(reader), context);
		}
		catch (IOException ioe) {
			throw toScriptException(ioe);
		}
	}

	@Override
	public Object eval(String script) throws ScriptException {
		return eval(script, getContext());
	}

	@Override
	public Object eval(Reader reader) throws ScriptException {
		return eval(reader, getContext());
	}

	@Override
	public Object eval(String script, Bindings b) throws ScriptException { // TODO
		return eval(script, getContext(b));
	}

	@Override
	public Object eval(Reader reader, Bindings b) throws ScriptException {// TODO
		return eval(reader, getContext(b));
	}

	@Override
	public ScriptEngineFactory getFactory() {
		return factory;
	}
}