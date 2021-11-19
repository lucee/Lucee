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
package lucee.transformer.cfml.script;

import lucee.runtime.exp.TemplateException;
import lucee.transformer.bytecode.Body;
import lucee.transformer.bytecode.Page;
import lucee.transformer.cfml.Data;
import lucee.transformer.cfml.tag.TagDependentBodyTransformer;
import lucee.transformer.expression.Expression;

public class CFMLScriptTransformer extends AbstrCFMLScriptTransformer implements TagDependentBodyTransformer {
	@Override
	public Body transform(Data data, String surroundingTagName) throws TemplateException {

		boolean isCFC = data.root instanceof Page && ((Page) data.root).isComponent();
		boolean isInterface = data.root instanceof Page && ((Page) data.root).isInterface();

		Data ed = init(data);

		boolean oldAllowLowerThan = ed.allowLowerThan;
		boolean oldInsideFunction = ed.insideFunction = false;
		String oldTagName = ed.tagName;
		boolean oldIsCFC = ed.isCFC;
		boolean oldIsInterface = ed.isInterface;

		ed.allowLowerThan = true;
		ed.insideFunction = false;
		ed.tagName = surroundingTagName;
		ed.isCFC = isCFC;
		ed.isInterface = isInterface;
		try {
			return statements(ed);
		}
		finally {
			ed.allowLowerThan = oldAllowLowerThan;
			ed.insideFunction = oldInsideFunction;
			ed.tagName = oldTagName;
			ed.isCFC = oldIsCFC;
			ed.isInterface = oldIsInterface;

		}
	}

	@Override
	public final Expression expression(Data data) throws TemplateException {
		Expression expr;
		expr = super.expression(data);
		comments(data);
		return expr;
	}
}