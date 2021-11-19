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
package lucee.transformer.cfml.attributes.impl;

import lucee.transformer.bytecode.statement.tag.Attribute;
import lucee.transformer.bytecode.statement.tag.Tag;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.cfml.attributes.AttributeEvaluator;
import lucee.transformer.cfml.attributes.AttributeEvaluatorException;
import lucee.transformer.library.tag.TagLibTag;

public class Transaction implements AttributeEvaluator {

	@Override
	public TagLibTag evaluate(TagLibTag tagLibTag, Tag tag) throws AttributeEvaluatorException {
		Attribute action = tag.getAttribute("action");

		if (action != null) {
			Tag parent = ASMUtil.getAncestorTag(tag, tag.getFullname());
			if (parent != null) {
				tagLibTag = tagLibTag.duplicate(false);
				tagLibTag.setBodyContent("empty");
			}
		}

		return tagLibTag;
	}

}