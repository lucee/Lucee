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
package lucee.transformer.cfml.evaluator;

import lucee.runtime.config.Config;
import lucee.runtime.exp.TemplateException;
import lucee.transformer.bytecode.statement.tag.Tag;
import lucee.transformer.cfml.Data;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibTag;

/**
 * Jede Klasse die als Evaluator verwendet werden soll, 
 * muss das Interface Evaluator implementieren. 
 * Das Interface Evaluator definiert also die gemeinsame 
 * Schnittstelle fuer alle Evaluatoren. 
 */
public interface Evaluator {

	/**
	 * Die Methode evaluate wird aufgerufen, wenn der Context eines Tags geprueft werden soll.
	 * @param config 
	 * @param cfxdTag Das konkrete Tag innerhalb der kompletten CFXD.
	 * @param libTag Die Definition des Tag aus der TLD.
	 * @param flibs Saemtliche Function Library Deskriptoren des aktuellen Tag Libray Deskriptors.
	 * @param srcCode
	 * @return changed talib
	 * @throws TemplateException
	*/
	public TagLib execute(Config config,Tag tag, TagLibTag libTag, FunctionLib[] flibs,Data data) throws TemplateException;
	
	/**
	 * Die Methode evaluate wird aufgerufen, wenn der Context eines Tags geprueft werden soll,
	 * nachdem die komplette Seite uebersetzt wurde.
	 * @param cfxdTag Das konkrete Tag innerhalb der kompletten CFXD.
	 * @param libTag Die Definition des Tag aus der TLD.
	 * @param flibs Saemtliche Function Library Deskriptoren des aktuellen Tag Libray Deskriptors.
	 * @throws EvaluatorException
	*/
	public void evaluate(Tag tag, TagLibTag libTag, FunctionLib[] flibs) throws EvaluatorException;
	
	
}