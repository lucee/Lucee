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

import lucee.runtime.exp.TemplateException;
import lucee.transformer.Factory;
import lucee.transformer.bytecode.Body;
import lucee.transformer.bytecode.Root;
import lucee.transformer.cfml.TransfomerSettings;
import lucee.transformer.cfml.evaluator.EvaluatorPool;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.util.SourceCode;

/**
 * Interface zum implementieren von individullen Parsersn fuer einezelne Tags (cfscript)
 */
public interface TagDependentBodyTransformer {
	
	/**
	 * @param parent
	 * @param flibs
	 * @param cfxdTag
	 * @param tagLibTag
	 * @param cfml
	 * @throws TemplateException
	 */
	public Body transform(Factory factory,Root root,EvaluatorPool ep,TagLib[][] tlibs, FunctionLib[] flibs, String surroundingTagName, TagLibTag[] scriptTags, SourceCode cfml,TransfomerSettings setting)
		throws TemplateException;

}