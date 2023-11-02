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

import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import lucee.transformer.bytecode.Body;
import lucee.transformer.bytecode.Statement;
import lucee.transformer.bytecode.statement.tag.Tag;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.EvaluatorSupport;

/**
 * Prueft den Kontext des Tag <code>try</code>. Innerhalb des Tag try muss sich am Schluss 1 bis n
 * Tags vom Typ catch befinden.
 */
public final class Try extends EvaluatorSupport {

	/**
	 * @see lucee.transformer.cfml.evaluator.EvaluatorSupport#evaluate(Element)
	 */
	@Override
	public void evaluate(Tag tag) throws EvaluatorException {
		Body body = tag.getBody();
		int catchCount = 0;
		int noCatchCount = 0;
		int finallyCount = 0;

		// count catch tag and other in body
		if (body != null) {
			List stats = body.getStatements();
			Iterator it = stats.iterator();
			Statement stat;
			Tag t;
			String name;
			while (it.hasNext()) {
				stat = (Statement) it.next();
				if (stat instanceof Tag) {
					t = (Tag) stat;
					name = t.getTagLibTag().getName();
					if (name.equals("finally")) {
						finallyCount++;
						noCatchCount++;
					}
					else if (name.equals("catch")) catchCount++;
					else noCatchCount++;
				}
				else noCatchCount++;
			}
		}
		// check if has Content
		if (catchCount == 0 && finallyCount == 0) throw new EvaluatorException("Wrong Context, tag cftry must have at least one tag cfcatch inside or a cffinally tag.");
		if (finallyCount > 1) throw new EvaluatorException("Wrong Context, tag cftry can have only one tag cffinally inside.");
		// check if no has Content
		if (noCatchCount == 0) {
			ASMUtil.remove(tag);
		}

	}
}