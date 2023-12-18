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

import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.config.Constants;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.type.util.ListUtil;

public class ScriptEngineFactoryImpl implements ScriptEngineFactory {

	final CFMLEngine engine;
	final boolean tag;

	public ScriptEngineFactoryImpl(CFMLEngine engine, boolean tag, int dialect) {
		this.engine = engine;
		this.tag = tag;
	}

	@Override
	public List<String> getExtensions() {
		return ListUtil.arrayToList(Constants.getCFMLExtensions());
	}

	@Override
	public List<String> getMimeTypes() {
		return ListUtil.arrayToList(Constants.CFML_MIMETYPES);
	}

	@Override
	public List<String> getNames() {
		return ListUtil.arrayToList(Constants.CFML_ALIAS_NAMES);
	}

	@Override
	public Object getParameter(String key) {

		if (key.equalsIgnoreCase(ScriptEngine.NAME)) return ConfigWebUtil.toDialect(CFMLEngine.DIALECT_CFML, "");

		if (key.equalsIgnoreCase(ScriptEngine.ENGINE)) return Constants.NAME;

		if (key.equalsIgnoreCase(ScriptEngine.ENGINE_VERSION) || key.equalsIgnoreCase(ScriptEngine.LANGUAGE_VERSION)) return engine.getInfo().getVersion().toString();

		if (key.equalsIgnoreCase(ScriptEngine.LANGUAGE)) return (Constants.CFML_NAME).toLowerCase() + (tag ? "-tag" : "");

		if (key.equalsIgnoreCase("THREADING")) return "THREAD-ISOLATED";
		throw new IllegalArgumentException("Invalid key");
	}

	@Override
	public String getMethodCallSyntax(String obj, String m, String... args) {
		StringBuilder sb = new StringBuilder();
		if (tag) sb.append("<").append(getSetTagName()).append(" ");

		sb.append(obj).append('.').append(m).append('(');

		if (args != null) for (int i = 0; i < args.length; i++) {
			sb.append("'");
			sb.append(escape(args[i]));
			sb.append("'");
			if (i == args.length - 1) sb.append(')');
			else sb.append(',');
		}
		if (tag) sb.append(">");
		else sb.append(";");

		return sb.toString();
	}

	@Override
	public String getOutputStatement(String toDisplay) {
		StringBuilder sb = new StringBuilder();
		if (tag) sb.append("<").append(getSetTagName()).append(" ");

		sb.append("echo(").append("'").append(escape(toDisplay)).append("'").append(")");

		if (tag) sb.append(">");
		else sb.append(";");
		return sb.toString();
	}

	@Override
	public String getProgram(String... statements) {
		// String name=getScriptTagName();

		StringBuilder sb = new StringBuilder()
		/*
		 * .append("<") .append(name) .append(">\n")
		 */;

		int len = statements.length;
		for (int i = 0; i < len; i++) {
			sb.append(statements[i]).append(";\n");
		}
		// sb.append("</").append(name).append(">");
		return sb.toString();
	}

	private String getSetTagName() {
		String prefix = ((ConfigPro) ThreadLocalPageContext.getConfig()).getCoreTagLib().getNameSpaceAndSeparator();
		return prefix + (Constants.CFML_SET_TAG_NAME);

	}

	@Override
	public ScriptEngine getScriptEngine() {
		return new ScriptEngineImpl(this);
	}

	private Object escape(String str) {
		return StringUtil.replace(str, "'", "''", false);
	}

	public String getName() {
		return (String) getParameter(ScriptEngine.NAME);
	}

	@Override
	public String getEngineName() {
		return (String) getParameter(ScriptEngine.ENGINE);
	}

	@Override
	public String getEngineVersion() {
		return (String) getParameter(ScriptEngine.ENGINE_VERSION);
	}

	@Override
	public String getLanguageName() {
		return (String) getParameter(ScriptEngine.LANGUAGE);
	}

	@Override
	public String getLanguageVersion() {
		return (String) getParameter(ScriptEngine.LANGUAGE_VERSION);
	}

}