/**
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

import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.statement.tag.Tag;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.EvaluatorSupport;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.tag.TagLibTag;

public final class TagThread extends EvaluatorSupport {

	@Override
	public void evaluate(Tag tag, TagLibTag tagLibTag, FunctionLib[] flibs) throws EvaluatorException {
		lucee.transformer.bytecode.statement.tag.TagThread tt = (lucee.transformer.bytecode.statement.tag.TagThread) tag;
		try {
			tt.init();
		}
		catch (TransformerException te) {
			EvaluatorException ee = new EvaluatorException(te.getMessage());
			ee.initCause(te);
			throw ee;
		}
	}
}