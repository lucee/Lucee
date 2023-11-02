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

import lucee.commons.lang.StringUtil;
import lucee.transformer.bytecode.statement.tag.Tag;
import lucee.transformer.bytecode.statement.tag.TagWhile;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.EvaluatorSupport;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.tag.TagLibTag;

public final class While extends EvaluatorSupport {
	@Override
	public void evaluate(Tag tag, TagLibTag tagLibTag, FunctionLib[] flibs) throws EvaluatorException {
		TagWhile whil = (TagWhile) tag;

		// label
		if (ASMUtil.isLiteralAttribute(tag, "label", ASMUtil.TYPE_STRING, false, true)) {
			LitString ls = (LitString) tag.getFactory().toExprString(tag.getAttribute("label").getValue());
			String l = ls.getString();
			if (!StringUtil.isEmpty(l, true)) {
				whil.setLabel(l.trim());
				tag.removeAttribute("label");
			}
		}
	}
}