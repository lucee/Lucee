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
package lucee.transformer.cfml;

import lucee.runtime.exp.TemplateException;
import lucee.transformer.expression.Expression;

/**
 * Innerhalb einer TLD (Tag Library Descriptor) kann eine Klasse angemeldet werden, welche das
 * Interface ExprTransfomer implementiert, um Ausdruecke die innerhalb von Attributen und dem Body
 * von Tags vorkommen zu transformieren. Die Idee dieses Interface ist es die Moeglichkeit zu
 * bieten, weitere ExprTransfomer zu erstellen zu koennen, um fuer verschiedene TLD, verschiedene
 * Ausdrucksarten zu bieten.
 *
 */
public interface ExprTransformer {

	/**
	 * Wird aufgerufen um aus dem uebergebenen CFMLString einen Ausdruck auszulesen und diesen in ein
	 * CFXD Element zu uebersetzten. <br>
	 * Beispiel eines uebergebenen String:<br>
	 * "session.firstName" oder "trim(left('test'&amp;var1,3))"
	 * 
	 * @param data
	 * @return Element CFXD Element
	 * @throws lucee.runtime.exp.TemplateException
	 * @throws TemplateException
	 */
	public Expression transform(Data data) throws TemplateException;

	/**
	 * Wird aufgerufen um aus dem uebergebenen CFMLString einen Ausdruck auszulesen und diesen in ein
	 * CFXD Element zu uebersetzten. Es wird aber davon ausgegangen das es sich um einen String handelt.
	 * <br>
	 * Beispiel eines uebergebenen String:<br>
	 * "session.firstName" oder "trim(left('test'&amp;var1,3))"
	 * 
	 * @param data
	 * @return Element CFXD Element
	 * @throws TemplateException
	 */
	public Expression transformAsString(Data data) throws TemplateException;

}