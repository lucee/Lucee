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

import lucee.transformer.bytecode.Body;
import lucee.transformer.bytecode.BodyBase;
import lucee.transformer.bytecode.statement.tag.Attribute;
import lucee.transformer.bytecode.statement.tag.Tag;
import lucee.transformer.bytecode.statement.tag.TagOutput;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.EvaluatorSupport;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibTag;

/**
 * Prueft den Kontext des Tag Mail.
 * 
 */
public final class Mail extends EvaluatorSupport {

	@Override
	public void evaluate(Tag tag, TagLibTag libTag) throws EvaluatorException {
		if (tag.containsAttribute("query")) {

			TagLib lib = libTag.getTagLib();
			TagLibTag outputTag = lib.getTag("output");

			TagOutput output = new TagOutput(tag.getFactory(), tag.getStart(), null);
			output.setFullname(outputTag.getFullName());
			output.setTagLibTag(outputTag);
			output.addAttribute(new Attribute(false, "output", tag.getFactory().TRUE(), "boolean"));
			output.addAttribute(new Attribute(false, "formail", tag.getFactory().TRUE(), "boolean"));

			Body body = new BodyBase(tag.getFactory());// output.getBody();
			output.setBody(body);

			ASMUtil.replace(tag, output, false);
			body.addStatement(tag);

			output.addAttribute(tag.removeAttribute("query"));
			if (tag.containsAttribute("group")) output.addAttribute(tag.removeAttribute("group"));
			if (tag.containsAttribute("groupcasesensitive")) output.addAttribute(tag.removeAttribute("groupcasesensitive"));
			if (tag.containsAttribute("startrow")) output.addAttribute(tag.removeAttribute("startrow"));
			if (tag.containsAttribute("maxrows")) output.addAttribute(tag.removeAttribute("maxrows"));

			new Output().evaluate(output, outputTag);
		}
	}
}