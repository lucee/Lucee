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
 * Die Klasse EvaluatorSupport hat die Aufgabe, Zugriffe auf die CFXD zu vereinfachen. Dazu stellt
 * die Klasse mehrere Methoden zur Verfuegung die verschiedene, immer wieder verwendete Abfragen
 * abbilden. Die Klasse implementiert das Interface Evaluator. Desweiteren splittet diese Klasse
 * auch die Methode evaluate in drei Methoden auf so, das man eine hoehere flexibilitaet beim
 * Einstiegspunkt einer konkreten Implementation hat.
 * 
 */
public class EvaluatorSupport implements TagEvaluator {

	/**
	 * Die Methode execute wird aufgerufen, wenn der Context eines Tags geprueft werden soll. Diese
	 * Methode ueberschreibt, jene des Interface Evaluator. Falls diese Methode durch eine
	 * Implementation nicht ueberschrieben wird, ruft sie wiederere, allenfalls implementierte evaluate
	 * Methoden auf. Mit Hilfe dieses Konstrukt ist es moeglich drei evaluate methoden anzubieten.
	 * 
	 * @param cfxdTag Das konkrete Tag innerhalb der kompletten CFXD.
	 * @param libTag Die Definition des Tag aus der TLD.
	 * @param flibs Saemtliche Function Library Deskriptoren des aktuellen Tag Libray Deskriptors.
	 * @param srcCode
	 * @return TagLib
	 * @throws TemplateException
	 */
	@Override
	public TagLib execute(Config config, Tag tag, TagLibTag libTag, FunctionLib[] flibs, Data data) throws TemplateException {

		return null;
	}

	/**
	 * Die Methode evaluate wird aufgerufen, wenn der Context eines Tags geprueft werden soll. Diese
	 * Methode ueberschreibt, jene des Interface Evaluator. Falls diese Methode durch eine
	 * Implementation nicht ueberschrieben wird, ruft sie wiederere, allenfalls implementierte evaluate
	 * Methoden auf. Mit Hilfe dieses Konstrukt ist es moeglich drei evaluate methoden anzubieten.
	 * 
	 * @param cfxdTag Das konkrete Tag innerhalb der kompletten CFXD.
	 * @param libTag Die Definition des Tag aus der TLD.
	 * @param flibs Saemtliche Function Library Deskriptoren des aktuellen Tag Libray Deskriptors.
	 * @throws EvaluatorException
	 */
	@Override
	public void evaluate(Tag tag, TagLibTag libTag, FunctionLib[] flibs) throws EvaluatorException {
		evaluate(tag);
		evaluate(tag, libTag);
	}

	/**
	 * ueberladene evaluate Methode nur mit einem CFXD Element.
	 * 
	 * @param cfxdTag
	 * @throws EvaluatorException
	 */
	public void evaluate(Tag tag) throws EvaluatorException {

	}

	/**
	 * ueberladene evaluate Methode mit einem CFXD Element und einem TagLibTag.
	 * 
	 * @param cfxdTag
	 * @param libTag
	 * @throws EvaluatorException
	 */
	public void evaluate(Tag tag, TagLibTag libTag) throws EvaluatorException {
	}

}