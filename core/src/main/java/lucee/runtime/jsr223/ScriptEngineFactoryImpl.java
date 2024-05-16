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
	final int dialect;
	final boolean isCFML;

	public ScriptEngineFactoryImpl(CFMLEngine engine, boolean tag, int dialect) {
		this.engine = engine;
		this.tag = tag;
		this.dialect = dialect;
		this.isCFML = dialect == CFMLEngine.DIALECT_CFML;
	}

	@Override
	public List<String> getExtensions() {
		return ListUtil.arrayToList(isCFML ? Constants.getCFMLExtensions() : Constants.getLuceeExtensions());
	}

	@Override
	public List<String> getMimeTypes() {
		return ListUtil.arrayToList(isCFML ? Constants.CFML_MIMETYPES : Constants.LUCEE_MIMETYPES);
	}

	@Override
	public List<String> getNames() {
		return ListUtil.arrayToList(dialect == CFMLEngine.DIALECT_CFML ? Constants.CFML_ALIAS_NAMES : Constants.LUCEE_ALIAS_NAMES);
	}

	@Override
	public Object getParameter(String key) {

		if (key.equalsIgnoreCase(ScriptEngine.NAME)) return ConfigWebUtil.toDialect(dialect, "");

		if (key.equalsIgnoreCase(ScriptEngine.ENGINE)) return Constants.NAME + " (dialect:" + ConfigWebUtil.toDialect(dialect, "") + ")";

		if (key.equalsIgnoreCase(ScriptEngine.ENGINE_VERSION) || key.equalsIgnoreCase(ScriptEngine.LANGUAGE_VERSION)) return engine.getInfo().getVersion().toString();

		if (key.equalsIgnoreCase(ScriptEngine.LANGUAGE)) return (isCFML ? Constants.CFML_NAME : Constants.LUCEE_NAME).toLowerCase() + (tag ? "-tag" : "");

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

	private String getScriptTagName() {
		String prefix = ((ConfigPro) ThreadLocalPageContext.getConfig()).getCoreTagLib(dialect).getNameSpaceAndSeparator();
		return prefix + (dialect == CFMLEngine.DIALECT_CFML ? Constants.CFML_SCRIPT_TAG_NAME : Constants.LUCEE_SCRIPT_TAG_NAME);

	}

	private String getSetTagName() {
		String prefix = ((ConfigPro) ThreadLocalPageContext.getConfig()).getCoreTagLib(dialect).getNameSpaceAndSeparator();
		return prefix + (dialect == CFMLEngine.DIALECT_CFML ? Constants.CFML_SET_TAG_NAME : Constants.LUCEE_SET_TAG_NAME);

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