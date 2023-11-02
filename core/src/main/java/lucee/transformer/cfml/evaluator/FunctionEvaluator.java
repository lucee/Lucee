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

import lucee.runtime.exp.TemplateException;
import lucee.transformer.bytecode.expression.var.BIF;
import lucee.transformer.library.function.FunctionLibFunction;

public interface FunctionEvaluator {

	public FunctionLibFunction pre(BIF bif, FunctionLibFunction flf) throws TemplateException;

	/**
	 * this method is executed to check the tag itself, the method is invoked after Lucee has read that
	 * function, but before reading the rest. so you have not the complete environment of the function.
	 */
	public void execute(BIF bif, FunctionLibFunction flf) throws TemplateException;

	/**
	 * This method is invoked to check the environment of a function, the method is invoked AFTER the
	 * parser has read the complete template, so you have the full environment.
	 * 
	 * @param bif
	 * @param flf the definition of the function from the fld file
	 * @throws TemplateException
	 */
	public void evaluate(BIF bif, FunctionLibFunction flf) throws EvaluatorException;

}