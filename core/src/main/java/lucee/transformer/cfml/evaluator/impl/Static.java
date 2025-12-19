/**
 *
 * Copyright (c) 2015, Lucee Association Switzerland
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

import java.util.List;

import lucee.commons.lang.StringUtil;
import lucee.transformer.Body;
import lucee.transformer.bytecode.StaticBody;
import lucee.transformer.bytecode.statement.tag.TagComponent;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.EvaluatorSupport;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.statement.Statement;
import lucee.transformer.statement.tag.Tag;

/**
 * Prueft den Kontext des Tag case. Das Tag <code>httpparam</code> darf nur innerhalb des Tag
 * <code>http</code> liegen.
 */
public final class Static extends EvaluatorSupport {

	@Override
	public void evaluate(Tag tag, TagLibTag libTag) throws EvaluatorException {
		// check parent
		Body body = null;

		String compName = Property.getComponentName(tag);

		boolean isCompChild = false;
		Tag p = ASMUtil.getParentTag(tag);

		if (p != null && (p instanceof TagComponent || getFullname(p, "").equalsIgnoreCase(compName))) {
			isCompChild = true;
			body = p.getBody();
		}

		Tag pp = p != null ? ASMUtil.getParentTag(p) : null;
		if (!isCompChild && pp != null && (p instanceof TagComponent || getFullname(pp, "").equalsIgnoreCase(compName))) {
			isCompChild = true;
			body = pp.getBody();
		}

		if (!isCompChild) {
			throw new EvaluatorException("Wrong Context for the the static constructor, " + "a static constructor must inside a component body.");
		}

		// Body body=(Body) tag.getParent();
		List<Statement> children = tag.getBody().getStatements();

		// remove that tag from parent
		ASMUtil.remove(tag);

		StaticBody sb = createStaticBody(body);
		ASMUtil.addStatements(sb, children);
	}

	private String getFullname(Tag tag, String defaultValue) {
		if (tag != null) {
			String fn = tag.getFullname();
			if (StringUtil.isEmpty(fn)) fn = tag.getTagLibTag().getFullName();
			if (!StringUtil.isEmpty(fn)) return fn;
		}

		return defaultValue;
	}

	/**
	 * Creates a new StaticBody and adds it to the component body.
	 * Each static { } block gets its own StaticBody to preserve AST structure.
	 */
	static StaticBody createStaticBody(Body body) {
		StaticBody sb = new StaticBody(body.getFactory());
		body.addStatement(sb);
		return sb;
	}

	/**
	 * Creates a new StaticBody for static functions.
	 * Each static function gets its own StaticBody to preserve AST structure.
	 */
	static StaticBody getStaticBodyForFunction(Body body) {
		return createStaticBody(body);
	}

}