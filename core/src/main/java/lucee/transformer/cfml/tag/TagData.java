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
package lucee.transformer.cfml.tag;

import lucee.loader.engine.CFMLEngine;
import lucee.transformer.Factory;
import lucee.transformer.bytecode.Root;
import lucee.transformer.cfml.Data;
import lucee.transformer.cfml.TransfomerSettings;
import lucee.transformer.cfml.evaluator.EvaluatorPool;
import lucee.transformer.cfml.expression.SimpleExprTransformer;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.util.SourceCode;

public class TagData extends Data {
	
	private SimpleExprTransformer set;
	
    public TagData(Factory factory,TagLib[][] tlibs, FunctionLib[] flibs,TagLibTag[] scriptTags, SourceCode cfml,
    		TransfomerSettings settings,Root root) {
		super(factory,root,cfml,new EvaluatorPool(),settings,tlibs,flibs,scriptTags);
		
	}
    public TagData(Factory factory,TagLib[][] tlibs, FunctionLib[] flibs,TagLibTag[] scriptTags, SourceCode sc,Root root, boolean dotNotionUpperCase,boolean ignoreScopes) {
		super(factory,root,sc,new EvaluatorPool(),new TransfomerSettings(dotNotionUpperCase,
				sc.getDialect()==CFMLEngine.DIALECT_CFML && factory.getConfig().getHandleUnQuotedAttrValueAsString(),ignoreScopes),tlibs,flibs,scriptTags);
	}
        
	
	public SimpleExprTransformer getSimpleExprTransformer() {
		return set;
	}

	public void setSimpleExprTransformer(SimpleExprTransformer set) {
		this.set = set;
	}
}