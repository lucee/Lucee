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
package lucee.transformer.cfml.evaluator.impl;

import java.nio.charset.Charset;

import lucee.commons.io.CharsetUtil;
import lucee.runtime.config.Config;
import lucee.runtime.exp.TemplateException;
import lucee.transformer.bytecode.statement.tag.Tag;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.cfml.Data;
import lucee.transformer.cfml.evaluator.EvaluatorSupport;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.util.PageSourceCode;

public final class PageEncoding extends EvaluatorSupport {

	@Override
	public TagLib execute(Config config, Tag tag, TagLibTag libTag, FunctionLib[] flibs, Data data) throws TemplateException {

		// encoding
		String str = ASMUtil.getAttributeString(tag, "charset", null);
		if (str == null) throw new TemplateException(data.srcCode, "attribute [pageencoding] of the tag [processingdirective] must be a constant value");

		Charset cs = CharsetUtil.toCharset(str);
		PageSourceCode psc = data.srcCode instanceof PageSourceCode ? (PageSourceCode) data.srcCode : null;
		if (psc == null || cs.equals(psc.getCharset()) || CharsetUtil.UTF8.equals(psc.getCharset())) {
			cs = null;
		}

		//

		if (cs != null) {
			throw new ProcessingDirectiveException(data.srcCode, cs, null, data.srcCode.getWriteLog());
		}

		return null;
	}
}