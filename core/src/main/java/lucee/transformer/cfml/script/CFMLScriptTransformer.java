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
import lucee.transformer.Factory;
import lucee.transformer.bytecode.Body;
import lucee.transformer.bytecode.Page;
import lucee.transformer.bytecode.Root;
import lucee.transformer.cfml.TransfomerSettings;
import lucee.transformer.cfml.evaluator.EvaluatorPool;
import lucee.transformer.cfml.tag.TagDependentBodyTransformer;
import lucee.transformer.expression.Expression;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.util.SourceCode;

public class CFMLScriptTransformer extends AbstrCFMLScriptTransformer implements TagDependentBodyTransformer {
	@Override
	public Body transform(Factory factory,Root root,EvaluatorPool ep
			,TagLib[][] tlibs, FunctionLib[] fld
			, String surroundingTagName,TagLibTag[] scriptTags
			, SourceCode cfml,TransfomerSettings settings) throws TemplateException	{
		//Page page = ASMUtil.getAncestorPage(tag);
		boolean isCFC= root instanceof Page && ((Page)root).isComponent();
		boolean isInterface=  root instanceof Page && ((Page)root).isInterface();
		
		ExprData data = init(factory,root,ep,tlibs,fld,scriptTags,cfml,settings,true);

		data.insideFunction=false; 
		data.tagName=surroundingTagName;
		data.isCFC=isCFC;
		data.isInterface=isInterface;
		//data.scriptTags=((ConfigImpl) config).getCoreTagLib().getScriptTags();

		//tag.setBody(statements(data));
		return statements(data);
	}

	@Override
	public final Expression expression(ExprData data) throws TemplateException {
		Expression expr;
		expr = super.expression(data);
		comments(data);
		return expr;
	}
}