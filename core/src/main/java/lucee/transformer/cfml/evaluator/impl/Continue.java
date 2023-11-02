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
import lucee.transformer.bytecode.statement.tag.Attribute;
import lucee.transformer.bytecode.statement.tag.Tag;
import lucee.transformer.bytecode.statement.tag.TagContinue;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.EvaluatorSupport;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.library.tag.TagLibTag;

/**
 * Prueft den Kontext des Tag continue. Das Tag <code>break</code> darf nur innerhalb des Tag
 * <code>loop, while, foreach</code> liegen.
 */
public final class Continue extends EvaluatorSupport {

	@Override
	public void evaluate(Tag tag, TagLibTag libTag) throws EvaluatorException {
		String ns = libTag.getTagLib().getNameSpaceAndSeparator();
		String loopName = ns + "loop";
		String whileName = ns + "while";

		// label
		String label = null;
		Attribute attrLabel = tag.getAttribute("label");
		if (attrLabel != null) {
			TagContinue tc = (TagContinue) tag;
			label = Break.variableToString(tag, attrLabel, null);
			if (label != null) {
				tc.setLabel(label = label.trim());
				tag.removeAttribute("label");
			}
			else if (ASMUtil.isLiteralAttribute(tag, attrLabel, ASMUtil.TYPE_STRING, false, true)) {
				LitString ls = (LitString) tag.getFactory().toExprString(tag.getAttribute("label").getValue());
				label = ls.getString();
				if (!StringUtil.isEmpty(label, true)) {
					tc.setLabel(label = label.trim());
					tag.removeAttribute("label");
				}
				else label = null;
			}
		}

		if (ASMUtil.isLiteralAttribute(tag, "label", ASMUtil.TYPE_STRING, false, true)) {
			LitString ls = (LitString) tag.getFactory().toExprString(tag.getAttribute("label").getValue());
			TagContinue tc = (TagContinue) tag;
			label = ls.getString();
			if (!StringUtil.isEmpty(label, true)) {
				tc.setLabel(label = label.trim());
				tag.removeAttribute("label");
			}
			else label = null;
		}

		if (!ASMUtil.hasAncestorContinueFCStatement(tag, label)) {
			if (tag.isScriptBase()) {
				if (StringUtil.isEmpty(label)) throw new EvaluatorException("Wrong Context, [" + libTag.getName() + "] must be inside a loop (for,while,loop ...)");
				throw new EvaluatorException("Wrong Context, [" + libTag.getName() + "] must be inside a loop (for,while,loop ...) with the label [" + label + "]");

			}
			if (StringUtil.isEmpty(label))
				throw new EvaluatorException("Wrong Context, tag [" + libTag.getFullName() + "] must be inside a [" + loopName + "] or [" + whileName + "] tag");
			throw new EvaluatorException(
					"Wrong Context, tag [" + libTag.getFullName() + "] must be inside a [" + loopName + "] or [" + whileName + "] tag with the label [" + label + "]");

		}
	}
}