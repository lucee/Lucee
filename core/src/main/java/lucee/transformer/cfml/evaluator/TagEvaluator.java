/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 **/
package lucee.transformer.cfml.evaluator;

import lucee.runtime.config.Config;
import lucee.runtime.exp.TemplateException;
import lucee.transformer.bytecode.statement.tag.Tag;
import lucee.transformer.cfml.Data;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibTag;

/**
 * evaluator interface for tags, this allows tags to check their environment and themself.
 */
public interface TagEvaluator {

	/**
	 * this method is executed to check the tag itself, the method is invoked after Lucee has read that
	 * tag, but before reading following tags. so you have not the complete environment of the tag.
	 * 
	 * @param config
	 * @param tag the tag to check
	 * @param libTag the definition of the tag from the tld file
	 * @param flibs all fld libraries.
	 * @param data data object of the running parser
	 * @throws TemplateException
	 */
	public TagLib execute(Config config, Tag tag, TagLibTag libTag, FunctionLib[] flibs, Data data) throws TemplateException;

	/**
	 * This method is invoked to check the environment of a tag, the method is invoked AFTER the parser
	 * has read the complete template, so you have the full environment.
	 * 
	 * @param tag the tag to check
	 * @param libTag the definition of the tag from the tld file
	 * @param flibs all fld libraries.
	 * @throws EvaluatorException
	 */
	public void evaluate(Tag tag, TagLibTag libTag, FunctionLib[] flibs) throws EvaluatorException;

}