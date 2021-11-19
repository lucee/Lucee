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
package lucee.transformer.cfml.expression;

import lucee.runtime.exp.TemplateException;
import lucee.transformer.cfml.Data;
import lucee.transformer.cfml.ExprTransformer;
import lucee.transformer.cfml.script.AbstrCFMLScriptTransformer;
import lucee.transformer.expression.Expression;

public class CFMLExprTransformer extends AbstrCFMLScriptTransformer implements ExprTransformer {

	@Override
	public Expression transformAsString(Data data) throws TemplateException {
		boolean alt = data.allowLowerThan;
		data.allowLowerThan = false;
		Data ed = init(data);
		try {
			return transformAsString(ed, new String[] { " ", ">", "/>" });
		}
		finally {
			data.allowLowerThan = alt;
		}
	}

	@Override
	public Expression transform(Data data) throws TemplateException {
		boolean alt = data.allowLowerThan;
		data.allowLowerThan = false;
		Data ed = init(data);
		try {
			comments(ed);
			return assignOp(ed);
		}
		finally {
			data.allowLowerThan = alt;
		}
	}
}