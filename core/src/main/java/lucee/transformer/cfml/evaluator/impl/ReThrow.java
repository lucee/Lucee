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
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.EvaluatorSupport;
import lucee.transformer.library.tag.TagLibTag;

/**
 * Prueft den Kontext des Tag rethrow. Das Tag <code>rethrow</code> darf nur innerhalb des Tag
 * <code>throw</code> liegen.
 */
public final class ReThrow extends EvaluatorSupport {

	@Override
	public void evaluate(Tag tag, TagLibTag libTag) throws EvaluatorException {
		String ns = libTag.getTagLib().getNameSpaceAndSeparator();
		String queryName = ns + "catch";

		if (!ASMUtil.hasAncestorTryStatement(tag)) {
			if (tag.isScriptBase()) throw new EvaluatorException("Wrong Context, statement [" + libTag.getName() + "] must be inside a [" + queryName + "] tag or catch statement");
			throw new EvaluatorException("Wrong Context, tag [" + libTag.getFullName() + "] must be inside a [" + queryName + "] tag");

		}
		// ASMUtil.replace(tag,new TagReThrow(tag));
	}

}