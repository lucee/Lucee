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

import lucee.transformer.bytecode.statement.tag.Tag;
import lucee.transformer.bytecode.statement.tag.TagIf;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.EvaluatorSupport;
import lucee.transformer.library.tag.TagLibTag;

/**
 *
 * Prueft den Kontext des Tag else. Das Tag <code>else</code> darf nur direkt innerhalb des Tag
 * <code>if</code> liegen. Dem Tag <code>else</code> darf, innerhalb des Tag <code>if</code>, kein
 * Tag <code>if</code> nachgestellt sein. Das Tag darf auch nur einmal vorkommen innerhalb des Tag
 * if.
 */
public final class Else extends EvaluatorSupport {

	@Override
	public void evaluate(Tag tag, TagLibTag libTag) throws EvaluatorException {
		String ns = libTag.getTagLib().getNameSpaceAndSeparator();
		String ifName = ns + "if";

		// check if tag is direct inside if
		if (!ASMUtil.isParentTag(tag, TagIf.class)) {
			throw new EvaluatorException("Wrong Context, tag [" + libTag.getFullName() + "] must be direct inside a [" + ifName + "] tag");
		}

		// check if is there an elseif tag after this tag
		if (ASMUtil.hasSisterTagAfter(tag, "elseif")) throw new EvaluatorException("Wrong Context, tag [cfelseif] can't be after tag [else]");
		// check if tag else is unique
		if (ASMUtil.hasSisterTagWithSameName(tag)) throw new EvaluatorException("Wrong Context, tag [else] must be once inside the tag [if]");

	}

}