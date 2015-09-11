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
package lucee.transformer.cfml.script;

import lucee.runtime.exp.TemplateException;
import lucee.transformer.Factory;
import lucee.transformer.bytecode.Body;
import lucee.transformer.bytecode.Root;
import lucee.transformer.cfml.TransfomerSettings;
import lucee.transformer.cfml.evaluator.EvaluatorPool;
import lucee.transformer.cfml.tag.TagDependentBodyTransformer;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.util.SourceCode;

public class CFMLOrJavaScriptTransformer implements TagDependentBodyTransformer {

	private JavaScriptTransformer jst=new JavaScriptTransformer();
	private CFMLScriptTransformer cst=new CFMLScriptTransformer();
	
	@Override
	public Body transform(Factory factory,Root root,EvaluatorPool ep,TagLib[][] tlibs, FunctionLib[] flibs, String surroundingTagName ,TagLibTag[] scriptTags, SourceCode cfml,TransfomerSettings settings) 

	throws TemplateException {/*
		Attribute attr = tag.getAttribute("language");
		if(attr!=null) {
			Expression expr = factory.toExprString(attr.getValue());
			if(!(expr instanceof LitString))
				throw new TemplateException(cfml,"Attribute language of the Tag script, must be a literal string value");
			String str = ((LitString)expr).getString().trim();
			if("java".equalsIgnoreCase(str))		return jst.transform(factory,page, ep, tlibs,flibs, tagLibTag,scriptTags, cfml,settings);
			else if("cfml".equalsIgnoreCase(str))	return cst.transform(factory,page, ep, tlibs,flibs,  tagLibTag,scriptTags, cfml,settings);
			else 
				throw new TemplateException(cfml,"invalid value for attribute language from tag script ["+str+"], valid values are [cfml,java]");
		}
		*/
	return null;
	}

}