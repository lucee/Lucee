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
import lucee.transformer.cfml.evaluator.ChildEvaluator;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.library.tag.TagLibTag;

/**
 * Prueft den Kontext des Tag mailparam. Das Tag <code>mailParam</code> darf nur innerhalb des Tag
 * <code>mail</code> liegen.
 */
public final class MailParam extends ChildEvaluator {

	public MailParam() {
		super("mail");
	}

	@Override
	public void evaluate(Tag tag, TagLibTag libTag) throws EvaluatorException {

	}
}