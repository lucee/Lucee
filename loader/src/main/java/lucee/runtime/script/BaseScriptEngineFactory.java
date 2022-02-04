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
package lucee.runtime.script;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.servlet.ServletException;

import lucee.cli.servlet.ServletConfigImpl;
import lucee.cli.servlet.ServletContextImpl;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;

public abstract class BaseScriptEngineFactory implements ScriptEngineFactory {

	private final ScriptEngineFactory factory;

	public BaseScriptEngineFactory(final boolean tag, final int dialect) throws ServletException {
		try {

			System.setProperty("lucee.cli.call", "true");

			// returns null when not used within Lucee
			CFMLEngine engine = null;
			try {
				engine = CFMLEngineFactory.getInstance();
			}
			catch (final RuntimeException re) {}

			// create Engine
			if (engine == null) {
				final String servletName = "";
				final Map<String, Object> attributes = new HashMap<String, Object>();
				final Map<String, String> initParams = new HashMap<String, String>();
				
				// Allow override of context root
				String rootPath = System.getProperty("lucee.cli.contextRoot");
				if( rootPath == null ) {
					// working directory that the java command was called from
					rootPath = ".";
				}
				final File root = new File(rootPath); 

				final ServletContextImpl servletContext = new ServletContextImpl(root, attributes, initParams, 1, 0);
				final ServletConfigImpl servletConfig = new ServletConfigImpl(servletContext, servletName);
				engine = CFMLEngineFactory.getInstance(servletConfig);
				servletContext.setLogger(engine.getCFMLEngineFactory().getLogger());
			}

			factory = tag ? CFMLEngineFactory.getInstance().getTagEngineFactory(dialect) : CFMLEngineFactory.getInstance().getScriptEngineFactory(dialect);

		}
		catch (ServletException se) {
			se.printStackTrace();
			throw se;
		}
		catch (RuntimeException re) {
			re.printStackTrace();
			throw re;
		}
	}

	@Override
	public String getEngineName() {
		return factory.getEngineName();
	}

	@Override
	public String getEngineVersion() {
		return factory.getEngineVersion();
	}

	@Override
	public List<String> getExtensions() {
		return factory.getExtensions();
	}

	@Override
	public List<String> getMimeTypes() {
		return factory.getMimeTypes();
	}

	@Override
	public List<String> getNames() {
		return factory.getNames();
	}

	@Override
	public String getLanguageName() {
		return factory.getLanguageName();
	}

	@Override
	public String getLanguageVersion() {
		return factory.getLanguageVersion();
	}

	@Override
	public Object getParameter(final String key) {
		return factory.getParameter(key);
	}

	@Override
	public String getMethodCallSyntax(final String obj, final String m, final String... args) {
		return factory.getMethodCallSyntax(obj, m, args);
	}

	@Override
	public String getOutputStatement(final String toDisplay) {
		return factory.getOutputStatement(toDisplay);
	}

	@Override
	public String getProgram(final String... statements) {
		return factory.getProgram(statements);
	}

	@Override
	public ScriptEngine getScriptEngine() {
		return factory.getScriptEngine();
	}
}