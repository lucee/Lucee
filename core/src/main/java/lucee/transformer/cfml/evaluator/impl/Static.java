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
		Tag containingTag = null; // The tag that directly contains this static in the component body

		if (p != null && (p instanceof TagComponent || getFullname(p, "").equalsIgnoreCase(compName))) {
			isCompChild = true;
			body = p.getBody();
			containingTag = tag; // static tag is directly in component body
		}

		Tag pp = p != null ? ASMUtil.getParentTag(p) : null;
		if (!isCompChild && pp != null && (pp instanceof TagComponent || getFullname(pp, "").equalsIgnoreCase(compName))) {
			isCompChild = true;
			body = pp.getBody();
			containingTag = p; // static is inside another tag (e.g., cfscript) in component body
		}

		if (!isCompChild) {
			throw new EvaluatorException("Wrong Context for the the static constructor, " + "a static constructor must inside a component body.");
		}

		// Body body=(Body) tag.getParent();
		List<Statement> children = tag.getBody().getStatements();

		// Find the index of the containing tag in component body (for AST position preservation)
		// For direct cfstatic, this is the tag itself. For script static { }, this is cfscript.
		int tagIndex = containingTag != null ? body.getStatements().indexOf(containingTag) : -1;

		// remove that tag from parent
		ASMUtil.remove(tag);

		StaticBody sb = createStaticBody(body, tag, tagIndex);
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
	 * Creates a new StaticBody and adds it to the component body at the original tag position.
	 * Each static { } block gets its own StaticBody to preserve AST structure.
	 *
	 * @param body The component body to add the StaticBody to
	 * @param tag The original cfstatic tag (for position info)
	 * @param tagIndex The original index of the tag in the body (-1 to append at end)
	 */
	static StaticBody createStaticBody(Body body, Statement tag, int tagIndex) {
		StaticBody sb = new StaticBody(body.getFactory(), tag != null ? tag.getStart() : null, tag != null ? tag.getEnd() : null);
		if (tagIndex >= 0 && tagIndex < body.getStatements().size()) {
			// Insert at original position
			body.getStatements().add(tagIndex, sb);
			sb.setParent(body);
		}
		else {
			// Fallback: append at end
			body.addStatement(sb);
		}
		return sb;
	}

	/**
	 * Creates a new StaticBody for static functions at the original tag position.
	 * Each static function gets its own StaticBody to preserve AST structure.
	 *
	 * @param body The component body to add the StaticBody to
	 * @param tag The original cffunction tag (for position info)
	 * @param tagIndex The original index of the tag in the body (-1 to append at end)
	 */
	static StaticBody getStaticBodyForFunction(Body body, Statement tag, int tagIndex) {
		return createStaticBody(body, tag, tagIndex);
	}

}