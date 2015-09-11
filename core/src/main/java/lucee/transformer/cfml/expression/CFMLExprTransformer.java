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
import lucee.transformer.Factory;
import lucee.transformer.bytecode.Root;
import lucee.transformer.cfml.ExprTransformer;
import lucee.transformer.cfml.TransfomerSettings;
import lucee.transformer.cfml.evaluator.EvaluatorPool;
import lucee.transformer.cfml.script.AbstrCFMLScriptTransformer;
import lucee.transformer.expression.Expression;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.util.SourceCode;

public class CFMLExprTransformer extends AbstrCFMLScriptTransformer implements ExprTransformer {

	@Override

	public Expression transformAsString(Factory factory,Root root,EvaluatorPool ep,TagLib[][] tld, FunctionLib[] fld,TagLibTag[] scriptTags, SourceCode cfml, TransfomerSettings settings, boolean allowLowerThan) throws TemplateException {
		return transformAsString(init(factory,root,ep,tld,fld,scriptTags, cfml,settings,allowLowerThan),new String[]{" ", ">", "/>"});
	}
	
	@Override
	public Expression transform(Factory factory,Root root,EvaluatorPool ep,TagLib[][] tld, FunctionLib[] fld,TagLibTag[] scriptTags, SourceCode cfml, TransfomerSettings settings) throws TemplateException {
		ExprData data = init(factory,root,ep,tld,fld,scriptTags, cfml,settings,false);
		comments(data);
		return assignOp(data);
	}
	
}