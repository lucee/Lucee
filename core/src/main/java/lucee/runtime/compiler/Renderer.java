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
package lucee.runtime.compiler;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.tagext.BodyContent;

import lucee.commons.digest.HashUtil;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.MemoryClassLoader;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.config.Constants;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.transformer.util.SourceCode;

public class Renderer {

	private static final long MAX_SIZE = 1024 * 1024;
	private static MemoryClassLoader mcl;
	private static Map<String, Page> pages = new HashMap<String, Page>();

	private static Class<? extends Page> loadClass(ConfigWebPro config, String className, String cfml, int dialect, boolean ignoreScopes) throws Exception {

		CFMLCompilerImpl compiler = config.getCompiler();
		// create className based o the content

		Class<? extends Page> clazz = null;
		if (mcl == null) {
			mcl = createMemoryClassLoader(config);
		}
		else clazz = ClassUtil.loadClass(mcl, className, null);

		if (clazz != null) return clazz;

		SourceCode sc = new SourceCode(cfml, false, dialect);

		// compile
		lucee.runtime.compiler.CFMLCompilerImpl.Result result = compiler.compile(config, sc, config.getTLDs(dialect), config.getFLDs(dialect), null, className, true, ignoreScopes);

		// before we add a new class, we make sure we are still in range
		if (mcl.getSize() + result.barr.length > MAX_SIZE) {
			mcl = createMemoryClassLoader(config);
			pages.clear();
		}

		return (Class<? extends Page>) mcl.loadClass(className, result.barr);
	}

	private static MemoryClassLoader createMemoryClassLoader(ConfigWeb cw) throws IOException {
		return new MemoryClassLoader(cw, cw.getClass().getClassLoader());
	}

	private static Page loadPage(ConfigWebPro cw, PageSource ps, String cfml, int dialect, boolean ignoreScopes) throws Exception {
		String className = HashUtil.create64BitHashAsString(cfml);

		// do we already have the page?
		Page p = pages.get(className);
		if (p != null) return p;

		// load class
		Constructor<? extends Page> constr = loadClass(cw, className, cfml, dialect, ignoreScopes).getDeclaredConstructor(PageSource.class);
		p = constr.newInstance(ps);
		pages.put(className, p);
		return p;
	}

	public static Result script(PageContext pc, String cfml, int dialect, boolean catchOutput, boolean ignoreScopes) throws PageException {
		String prefix = ((ConfigPro) pc.getConfig()).getCoreTagLib(dialect).getNameSpaceAndSeparator();
		String name = prefix + (dialect == CFMLEngine.DIALECT_CFML ? Constants.CFML_SCRIPT_TAG_NAME : Constants.LUCEE_SCRIPT_TAG_NAME);
		return tag(pc, "<" + name + ">" + cfml + "</" + name + ">", dialect, catchOutput, ignoreScopes);
	}

	public static Result tag(PageContext pc, String cfml, int dialect, boolean catchOutput, boolean ignoreScopes) throws PageException {
		// execute
		Result res = new Result();
		BodyContent bc = null;
		try {
			if (catchOutput) bc = pc.pushBody();

			res.value = loadPage((ConfigWebPro) pc.getConfig(), null, cfml, dialect, ignoreScopes).call(pc);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}
		finally {
			if (catchOutput) {
				if (bc != null) res.output = bc.getString();
				pc.popBody();
			}
		}
		return res;
	}

	public static class Result {

		private String output;
		private Object value;

		public String getOutput() {
			return output == null ? "" : output;
		}

		public Object getValue() {
			return value;
		}

		@Override
		public String toString() {
			return "output:" + output + ";value:" + value;
		}
	}
}