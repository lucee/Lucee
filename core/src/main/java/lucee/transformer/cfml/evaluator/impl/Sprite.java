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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lucee.commons.digest.HashUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.IDGenerator;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.transformer.bytecode.Page;
import lucee.transformer.bytecode.statement.tag.Attribute;
import lucee.transformer.bytecode.statement.tag.Tag;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.EvaluatorSupport;
import lucee.transformer.expression.Expression;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.util.SourceCode;

public final class Sprite extends EvaluatorSupport {

	// private static final Expression DELIMITER = LitString.toExprString(",");
	private static Map<String, Previous> sprites = new HashMap<String, Previous>();

	@Override
	public void evaluate(Tag tag, TagLibTag tagLibTag, FunctionLib[] flibs) throws EvaluatorException {
		String id = "sprite_" + IDGenerator.intId();
		try {
			Page page = ASMUtil.getAncestorPage(tag);

			SourceCode sc = page.getSourceCode();

			String key = sc.id();
			key = HashUtil.create64BitHashAsString(Thread.currentThread().getId() + ":" + key);

			Expression src = tag.getAttribute("src").getValue();

			// get data from previous sprites
			Previous previous = sprites.get(key);
			if (previous != null) {
				previous.tag.removeAttribute("_ids");
				previous.tag.removeAttribute("_srcs");
				previous.tag = tag;
			}
			else {
				sprites.put(key, previous = new Previous(tag));
			}

			previous.ids.add(id);
			if (previous.src == null) previous.src = src;
			else {
				previous.src = tag.getFactory().opString(previous.src, tag.getFactory().createLitString(","));
				previous.src = tag.getFactory().opString(previous.src, src);
			}

			tag.addAttribute(new Attribute(false, "_id", tag.getFactory().createLitString(id), "string"));
			tag.addAttribute(new Attribute(false, "_ids", tag.getFactory().createLitString(lucee.runtime.type.util.ListUtil.listToList(previous.ids, ",")), "string"));

			tag.addAttribute(new Attribute(false, "_srcs", previous.src, "string"));

		}
		catch (Throwable e) {// TODO handle Excpetion much more precise
			ExceptionUtil.rethrowIfNecessary(e);
			throw new PageRuntimeException(Caster.toPageException(e));
		}

	}

	private static class Previous {
		public Previous(Tag tag) {
			this.tag = tag;
		}

		private List<String> ids = new ArrayList<String>();
		private Expression src = null;
		private Tag tag;

	}

}