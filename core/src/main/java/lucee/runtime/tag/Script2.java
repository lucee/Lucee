/**
 *
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
package lucee.runtime.tag;

import java.util.Iterator;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.BodyTagTryCatchFinallyImpl;

/**
 * Saves the generated content inside the tag body in a variable.
 *
 *
 *
 **/
public final class Script2 extends BodyTagTryCatchFinallyImpl {

	/** The name of the variable in which to save the generated content inside the tag. */
	private String language;
	private final CFMLEngine engine;
	private String script;

	public Script2() {
		engine = CFMLEngineFactory.getInstance();
	}

	@Override
	public void release() {
		super.release();
		language = null;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	@Override
	public int doStartTag() {

		return EVAL_BODY_BUFFERED;
	}

	@Override
	public int doAfterBody() throws PageException {
		script = bodyContent.getString();
		bodyContent.clearBody();
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() throws PageException {
		ScriptEngine engine = getScriptEngine();
		Bindings bindings = engine.createBindings();
		try {
			bindings.put("pageContext", pageContext);
			bindings.put("application", pageContext.applicationScope());
			bindings.put("session", pageContext.sessionScope());
			bindings.put("request", pageContext.requestScope());
			bindings.put("variables", pageContext.variablesScope());

			bindings.put("caster", this.engine.getCastUtil());
			// TODO more

			engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
			engine.eval(script);

		}
		catch (ScriptException e) {
			throw this.engine.getCastUtil().toPageException(e);
		}
		// remove all presets
		bindings.remove("pageContext");
		bindings.remove("application");
		bindings.remove("session");
		bindings.remove("request");
		bindings.remove("variables");

		bindings.remove("caster");

		pageContext.setVariable("cfscript", bindings);

		return EVAL_PAGE;
	}

	public ScriptEngine getScriptEngine() throws PageException {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName(language);

		// get engine by engine name
		if (engine == null) {
			Iterator<ScriptEngineFactory> it = manager.getEngineFactories().iterator();
			ScriptEngineFactory factory;
			while (it.hasNext()) {
				factory = it.next();
				if (language.equalsIgnoreCase(factory.getEngineName())) {
					engine = factory.getScriptEngine();
					break;
				}
			}
		}

		// get engine by language name
		if (engine == null) {
			Iterator<ScriptEngineFactory> it = manager.getEngineFactories().iterator();
			ScriptEngineFactory factory;
			while (it.hasNext()) {
				factory = it.next();
				if (language.equalsIgnoreCase(factory.getEngineName())) {
					engine = factory.getScriptEngine();
					break;
				}
			}
		}
		if (engine == null) {
			Iterator<ScriptEngineFactory> it = manager.getEngineFactories().iterator();
			ScriptEngineFactory factory;
			StringBuilder sb = new StringBuilder();
			while (it.hasNext()) {
				factory = it.next();
				if (sb.length() > 0) sb.append(',');
				sb.append(factory.getEngineName());
			}
			throw this.engine.getExceptionUtil().createApplicationException("language [" + language + "] is not supported, supported languages are [" + sb + "]");
		}
		return engine;
	}
}