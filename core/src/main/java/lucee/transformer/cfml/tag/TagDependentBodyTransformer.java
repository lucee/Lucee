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
import lucee.transformer.bytecode.Body;
import lucee.transformer.cfml.Data;

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
	// public Body transform(Factory factory,Root root,EvaluatorPool ep,TagLib[][] tlibs, FunctionLib[]
	// flibs, String surroundingTagName,
	// TagLibTag[] scriptTags, SourceCode cfml,TransfomerSettings setting)
	// throws TemplateException;

	public Body transform(Data data, String surroundingTagName) throws TemplateException;

}