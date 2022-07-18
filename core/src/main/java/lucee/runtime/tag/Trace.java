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
package lucee.runtime.tag;

import java.io.IOException;

import lucee.commons.io.log.Log;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageSource;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.ScriptConverter;
import lucee.runtime.debug.DebugTrace;
import lucee.runtime.debug.DebugTraceImpl;
import lucee.runtime.debug.DebuggerImpl;
import lucee.runtime.exp.Abort;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.BodyTagImpl;
import lucee.runtime.functions.other.Dump;
import lucee.runtime.interpreter.VariableInterpreter;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.trace.TraceObjectSupport;

public final class Trace extends BodyTagImpl {

	private boolean abort = false;
	private boolean follow = false;
	private String category;
	private boolean inline = false;
	private String text;
	private int type = Log.LEVEL_INFO;
	private String var;
	private Struct caller;

	@Override
	public void release() {
		super.release();
		abort = false;
		category = null;
		inline = false;
		text = null;
		type = Log.LEVEL_INFO;
		var = null;
		caller = null;
		follow = false;
	}

	/**
	 * @param abort the abort to set
	 */
	public void setAbort(boolean abort) {
		this.abort = abort;
	}

	public void setFollow(boolean follow) {
		this.follow = follow;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @param inline the inline to set
	 */
	public void setInline(boolean inline) {
		this.inline = inline;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @param type the type to set
	 * @throws ApplicationException
	 */
	public void setType(String strType) throws ApplicationException {
		strType = strType.toLowerCase().trim();
		if ("info".equals(strType)) type = Log.LEVEL_INFO;
		if ("information".equals(strType)) type = Log.LEVEL_INFO;
		else if ("warn".equals(strType)) type = Log.LEVEL_WARN;
		else if ("warning".equals(strType)) type = Log.LEVEL_WARN;
		else if ("error".equals(strType)) type = Log.LEVEL_ERROR;
		else if ("fatal information".equals(strType)) type = Log.LEVEL_FATAL;
		else if ("fatal-information".equals(strType)) type = Log.LEVEL_FATAL;
		else if ("fatal_information".equals(strType)) type = Log.LEVEL_FATAL;
		else if ("fatalinformation".equals(strType)) type = Log.LEVEL_FATAL;
		else if ("fatal info".equals(strType)) type = Log.LEVEL_FATAL;
		else if ("fatal-info".equals(strType)) type = Log.LEVEL_FATAL;
		else if ("fatal_info".equals(strType)) type = Log.LEVEL_FATAL;
		else if ("fatalinfo".equals(strType)) type = Log.LEVEL_FATAL;
		else if ("fatal".equals(strType)) type = Log.LEVEL_FATAL;
		else if ("debug".equals(strType)) type = Log.LEVEL_DEBUG;
		else if ("debugging".equals(strType)) type = Log.LEVEL_DEBUG;
		else if ("debuging".equals(strType)) type = Log.LEVEL_DEBUG;
		else if ("trace".equals(strType)) type = Log.LEVEL_TRACE;
		else throw new ApplicationException("invalid value [" + strType + "] for attribute [type], valid values are [Debug, Information, Warning, Error, Fatal Information]");
	}

	/**
	 * @param var the var to set
	 */
	public void setVar(String var) {
		this.var = var;
	}

	public void setCaller(Struct caller) {
		this.caller = caller;
	}

	/**
	 * @param var the var to set
	 */
	public void setVariable(String var) {
		this.var = var;
	}

	@Override
	public int doStartTag() {
		return EVAL_BODY_INCLUDE;
	}

	@Override
	public int doEndTag() throws PageException {
		try {
			_doEndTag();
		}
		catch (IOException e) {
		}
		return EVAL_PAGE;
	}

	public void _doEndTag() throws IOException, PageException {

		PageSource ps = pageContext.getCurrentTemplatePageSource();

		// var
		String varValue = null;
		Object value = null, traceValue = null;
		if (!StringUtil.isEmpty(var)) {

			try {
				if (caller instanceof Scope) value = VariableInterpreter.getVariable(pageContext, var, (Scope) caller);
				else value = pageContext.getVariable(var);
			}
			catch (PageException e) {
				varValue = "(undefined)";
				follow = false;
			}

			if (follow) {
				// print.o(1);
				if (StringUtil.isEmpty(text, true)) text = var;
				// print.o(2);
				traceValue = TraceObjectSupport.toTraceObject(pageContext.getDebugger(), value, type, category, text);

				if (caller instanceof Scope) VariableInterpreter.setVariable(pageContext, var, traceValue, (Scope) caller);
				else pageContext.setVariable(var, traceValue);
			}

			try {
				varValue = new ScriptConverter().serialize(value);
			}
			catch (ConverterException e) {
				if (value != null) varValue = "(" + Caster.toTypeName(value) + ")";
			}

		}
		DebugTrace trace = ((DebuggerImpl) pageContext.getDebugger()).addTrace(type, category, text, ps, var, varValue);
		DebugTrace[] traces = pageContext.getDebugger().getTraces(pageContext);

		String total = "(1st trace)";
		if (traces.length > 1) {
			long t = 0;
			for (int i = 0; i < traces.length; i++) {
				t += traces[i].getTime();
			}
			total = "(" + t + ")";
		}

		boolean hasCat = !StringUtil.isEmpty(trace.getCategory());
		boolean hasText = !StringUtil.isEmpty(trace.getText());
		boolean hasVar = !StringUtil.isEmpty(var);

		// inline
		if (inline) {
			lucee.runtime.format.TimeFormat tf = new lucee.runtime.format.TimeFormat(pageContext.getConfig().getLocale());
			StringBuffer sb = new StringBuffer();
			sb.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" bgcolor=\"white\">");
			sb.append("<tr>");
			// sb.append("<td><img src=\"/CFIDE/debug/images/Error_16x16.gif\" alt=\"Error type\">");
			sb.append("<td>");
			sb.append("<font color=\"orange\">");
			sb.append("<b>");
			sb.append(DebugTraceImpl.toType(trace.getType(), "INFO") + " - ");
			sb.append("[CFTRACE " + tf.format(new DateTimeImpl(pageContext.getConfig()), "hh:mm:ss:l") + "]");
			sb.append("[" + trace.getTime() + " ms " + total + "]");
			sb.append("[" + trace.getTemplate() + " @ line: " + trace.getLine() + "]");
			if (hasCat || hasText) sb.append(" -");
			if (hasCat) sb.append("  [" + trace.getCategory() + "]");
			if (hasText) sb.append(" <i>" + trace.getText() + "&nbsp;</i>");
			sb.append("</b>");
			sb.append("</font>");
			sb.append("</td>");
			sb.append("</tr>");
			sb.append("</table>");
			pageContext.forceWrite(sb.toString());

			if (hasVar) Dump.call(pageContext, value, var);

		}

		// log
		Log log = pageContext.getConfig().getLog("trace");
		StringBuffer msg = new StringBuffer();
		msg.append("[" + trace.getTime() + " ms " + total + "] ");
		msg.append("[" + trace.getTemplate() + " @ line: " + trace.getLine() + "]");
		if (hasCat || hasText || hasVar) msg.append("- ");
		if (hasCat) msg.append("[" + trace.getCategory() + "] ");
		if (hasVar) msg.append("[" + var + "=" + varValue + "] ");
		if (hasText) msg.append(" " + trace.getText() + " ");
		log.log(trace.getType(), "cftrace", msg.toString());

		// abort
		if (abort) throw new Abort(Abort.SCOPE_REQUEST);

	}

	@Override
	public void doInitBody() {

	}

	@Override
	public int doAfterBody() {
		return SKIP_BODY;
	}

	/**
	 * sets if has body or not
	 * 
	 * @param hasBody
	 */
	public void hasBody(boolean hasBody) {

	}
}