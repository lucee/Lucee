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
package lucee.transformer.bytecode.statement;

import org.objectweb.asm.Label;

import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.visitor.OnFinally;

/**
 * finally code of a statement (try, for-in, ...) that a break, continue or retry has to pass on its
 * way out
 */
public interface FlowControlFinal {

	/**
	 * returns the label to jump to, to execute the finally code and then go on to the given label.
	 * every distinct target gets its own entry, a break and a continue inside the same statement must
	 * not share one (LDEV-6510)
	 * 
	 * @param afterFinalGOTOLabel label to go to after the finally code
	 * @return entry label
	 */
	public Label getFinalEntryLabel(Label afterFinalGOTOLabel);

	/**
	 * writes the finally code (see setOnFinally) once for every entry requested with
	 * getFinalEntryLabel, each followed by a jump to its own target
	 * 
	 * @param bc
	 * @throws TransformerException
	 */
	public void writeOutFinalEntries(BytecodeContext bc) throws TransformerException;

	/**
	 * set the finally code that belongs to this statement
	 * 
	 * @param onFinally
	 */
	public void setOnFinally(OnFinally onFinally);

	/**
	 * the finally code that belongs to this statement, can be null
	 * 
	 * @return finally code
	 */
	public OnFinally getOnFinally();
}