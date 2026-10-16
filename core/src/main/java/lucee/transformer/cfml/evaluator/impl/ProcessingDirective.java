/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
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
package lucee.transformer.cfml.evaluator.impl;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import lucee.commons.io.CharsetUtil;
import lucee.runtime.config.Config;
import lucee.runtime.exp.TemplateException;
import lucee.runtime.op.Caster;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.cfml.Data;
import lucee.transformer.cfml.evaluator.EvaluatorSupport;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.statement.tag.Tag;
import lucee.transformer.util.PageSourceCode;

/**
 * Prueft den Kontext des Tag <code>catch</code>. Das Tag darf sich nur direkt innerhalb des Tag
 * <code>try</code> befinden.
 */
public final class ProcessingDirective extends EvaluatorSupport {

	private static final String GENERAL_EXPLANATIONX = " The [processingdirective] tag provides instructions to the CFML compiler and affects the entire template. ";

	@Override
	public TagLib execute(Config config, Tag tag, TagLibTag libTag, FunctionLib flibs, Data data) throws TemplateException {

		// dot notation
		Boolean dotNotationUpperCase = null;
		if (tag.containsAttribute("preservecase")) {
			Boolean preservecase = ASMUtil.getAttributeBoolean(tag, "preservecase", null);
			if (preservecase == null) throw new TemplateException(data.srcCode,
					"The attribute [preserveCase] from the tag [processingdirective] must be a constant boolean value (true or false). "
							+ "Dynamic expressions like #variables# or function calls are not allowed. "
							+ "Examples of valid usage: preserveCase=\"true\" or preserveCase=\"false\"." + GENERAL_EXPLANATIONX);
			dotNotationUpperCase = preservecase.booleanValue() ? Boolean.FALSE : Boolean.TRUE;

			if (dotNotationUpperCase == data.settings.dotNotationUpper) dotNotationUpperCase = null;

		}

		// page encoding
		Charset cs = null;
		if (tag.containsAttribute("pageencoding")) {
			String str = ASMUtil.getAttributeString(tag, "pageencoding", null);
			if (str == null) throw new TemplateException(data.srcCode,
					"The attribute [pageEncoding] from the tag [processingdirective] must be a constant string value. "
							+ "Dynamic expressions like #variables# or function calls are not allowed. "
							+ "Examples of valid usage: pageEncoding=\"UTF-8\" or pageEncoding=\"ISO-8859-1\"." + GENERAL_EXPLANATIONX);

			cs = CharsetUtil.toCharset(str);

			PageSourceCode psc = data.srcCode instanceof PageSourceCode ? (PageSourceCode) data.srcCode : null;
			if (psc == null || cs.equals(psc.getCharset())) {
				cs = null;
			}
		}

		// execution log
		Boolean exeLog = null;
		if (tag.containsAttribute("executionlog")) {
			String strExeLog = ASMUtil.getAttributeString(tag, "executionlog", null);
			exeLog = Caster.toBoolean(strExeLog, null);
			if (exeLog == null) throw new TemplateException(data.srcCode,
					"The attribute [executionlog] from the tag [processingdirective] must be a constant boolean value (true or false). "
							+ "Dynamic expressions like #variables# or function calls are not allowed. "
							+ "Examples of valid usage: executionLog=\"true\" or executionLog=\"false\"." + GENERAL_EXPLANATIONX);
			if (exeLog.booleanValue() == data.srcCode.getWriteLog()) exeLog = null;
		}

		if (cs != null || exeLog != null || dotNotationUpperCase != null) {
			Charset currCS = data.srcCode instanceof PageSourceCode ? ((PageSourceCode) data.srcCode).getCharset() : StandardCharsets.UTF_8;

			// throw an exception when already done
			if (data.hasCharset && cs != null) throw new TemplateException(data.srcCode,
					"The attribute [pageEncoding] from the tag [processingdirective] was already set with a previous tag and cannot be set again." + GENERAL_EXPLANATIONX);
			if (data.hasUpper && dotNotationUpperCase != null) throw new TemplateException(data.srcCode,
					"The attribute [preserveCase] from the tag [processingdirective] was already set with a previous tag and cannot be set again." + GENERAL_EXPLANATIONX);
			if (data.hasWriteLog && exeLog != null) throw new TemplateException(data.srcCode,
					"The attribute [executionlog] from the tag [processingdirective] was already set with a previous tag and cannot be set again." + GENERAL_EXPLANATIONX);

			// ignore in case they are the same
			if (cs != null && cs.equals(currCS)) cs = null;
			if (dotNotationUpperCase != null && dotNotationUpperCase.equals(data.settings.dotNotationUpper)) dotNotationUpperCase = null;
			if (exeLog != null && exeLog.equals(data.srcCode.getWriteLog())) exeLog = null;

			// do we have changes?
			if (cs != null || exeLog != null || dotNotationUpperCase != null) {
				throw new ProcessingDirectiveException(data.srcCode, tag.getStart(), tag.getEnd(), cs, dotNotationUpperCase, exeLog);
			}
		}

		return null;
	}
	/*
	 * FUTURE add this restriction
	 * 
	 * @Override public void evaluate(Tag tag, TagLibTag libTag) throws EvaluatorException { if
	 * (!ASMUtil.inRoot(tag, true)) { throw new
	 * EvaluatorException("The tag [processingdirective] must be placed at the top level of your template (not nested within any other tags or statement blocks). "
	 * +
	 * "Since it applies compiler settings for the entire file, placing it within conditional blocks or other tags would create ambiguity."
	 * + GENERAL_EXPLANATIONX); }
	 * 
	 * }
	 */
}