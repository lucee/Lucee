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

import lucee.transformer.cfml.evaluator.ChildEvaluator;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.statement.tag.Attribute;
import lucee.transformer.statement.tag.Tag;

/**
 * Prueft den Kontext des Tag <code>catch</code>. Das Tag darf sich nur direkt innerhalb des Tag
 * <code>try</code> befinden.
 */
public final class Catch extends ChildEvaluator {

	public Catch() {
		super("try");
	}

	@Override
	public void evaluate(Tag tag, TagLibTag libTag) throws EvaluatorException {
		super.evaluate(tag, libTag);

		// multi catch, type="a" | "b"
		Attribute type = tag.getAttribute("type");
		if (type == null || !type.hasAlternatives()) return;

		if (isAny(type.getValue())) throw new EvaluatorException("invalid multi catch, the type [any] cannot be combined with other types, it already catches all exceptions.");
		for (Expression alt: type.getAlternatives()) {
			if (isAny(alt)) throw new EvaluatorException("invalid multi catch, the type [any] cannot be combined with other types, it already catches all exceptions.");
		}
		if (tag.getAttribute("name") == null) {
			throw new EvaluatorException("invalid multi catch, the attribute [name] is required when [type] has multiple values, for example type=\"a\" | \"b\" name=\"e\".");
		}
	}

	private static boolean isAny(Expression expr) {
		return expr instanceof LitString && "any".equalsIgnoreCase(((LitString) expr).getString());
	}
}