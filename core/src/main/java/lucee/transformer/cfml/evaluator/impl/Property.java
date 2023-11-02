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

import lucee.loader.engine.CFMLEngine;
import lucee.runtime.config.Constants;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.Page;
import lucee.transformer.bytecode.statement.tag.Tag;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.EvaluatorSupport;
import lucee.transformer.library.tag.TagLibTag;

/**
 * Prueft den Kontext des Tag mailparam. Das Tag <code>mailParam</code> darf nur innerhalb des Tag
 * <code>mail</code> liegen.
 */
public final class Property extends EvaluatorSupport {

	@Override
	public void evaluate(Tag tag, TagLibTag libTag) throws EvaluatorException {
		// get component name
		String compName = getComponentName(tag);

		if (!ASMUtil.isParentTag(tag, compName)) throw new EvaluatorException("Wrong Context, tag [" + libTag.getFullName() + "] must be inside [" + compName + "] tag");
	}

	public static String getComponentName(Tag tag) throws EvaluatorException {
		Page page;
		try {
			page = ASMUtil.getAncestorPage(tag);
		}
		catch (TransformerException e) {
			throw new EvaluatorException(e.getMessage());
		}

		String ns = tag.getTagLibTag().getTagLib().getNameSpaceAndSeparator();
		String compName = ns + (page.getSourceCode().getDialect() == CFMLEngine.DIALECT_CFML ? Constants.CFML_COMPONENT_TAG_NAME : Constants.LUCEE_COMPONENT_TAG_NAME);

		return compName;
	}
}