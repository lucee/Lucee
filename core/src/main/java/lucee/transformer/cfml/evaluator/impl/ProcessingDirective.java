/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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

import lucee.commons.io.CharsetUtil;
import lucee.runtime.config.Config;
import lucee.runtime.exp.TemplateException;
import lucee.runtime.op.Caster;
import lucee.transformer.bytecode.statement.tag.Tag;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.cfml.Data;
import lucee.transformer.cfml.evaluator.EvaluatorSupport;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.util.PageSourceCode;

/**
 * Prueft den Kontext des Tag <code>catch</code>. Das Tag darf sich nur direkt innerhalb des Tag
 * <code>try</code> befinden.
 */
public final class ProcessingDirective extends EvaluatorSupport {

	@Override
	public TagLib execute(Config config, Tag tag, TagLibTag libTag, FunctionLib[] flibs, Data data) throws TemplateException {
		// dot notation
		Boolean dotNotationUpperCase = null;
		if (tag.containsAttribute("preservecase")) {
			Boolean preservecase = ASMUtil.getAttributeBoolean(tag, "preservecase", null);
			if (preservecase == null) throw new TemplateException(data.srcCode, "attribute [preserveCase] of the tag [processingdirective] must be a constant boolean value");
			dotNotationUpperCase = preservecase.booleanValue() ? Boolean.FALSE : Boolean.TRUE;

			if (dotNotationUpperCase == data.settings.dotNotationUpper) dotNotationUpperCase = null;

		}

		// page encoding
		Charset cs = null;
		if (tag.containsAttribute("pageencoding")) {
			String str = ASMUtil.getAttributeString(tag, "pageencoding", null);
			if (str == null) throw new TemplateException(data.srcCode, "attribute [pageencoding] of the tag [processingdirective] must be a constant value");

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
			if (exeLog == null) throw new TemplateException(data.srcCode, "attribute [executionlog] of the tag [processingdirective] must be a constant boolean value");
			if (exeLog.booleanValue() == data.srcCode.getWriteLog()) exeLog = null;
		}

		if (cs != null || exeLog != null || dotNotationUpperCase != null) {
			if (cs == null) {
				if (data.srcCode instanceof PageSourceCode) cs = ((PageSourceCode) data.srcCode).getCharset();
				else cs = CharsetUtil.UTF8;
			}
			if (exeLog == null) exeLog = data.srcCode.getWriteLog() ? Boolean.TRUE : Boolean.FALSE;
			if (dotNotationUpperCase == null) dotNotationUpperCase = data.settings.dotNotationUpper;
			throw new ProcessingDirectiveException(data.srcCode, cs, dotNotationUpperCase, exeLog);
		}

		return null;
	}
}