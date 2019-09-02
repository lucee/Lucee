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
package lucee.transformer.bytecode;

import lucee.runtime.exp.TemplateException;
import lucee.transformer.Context;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.statement.FlowControlFinal;

/**
 * A single Statement
 */
public interface Statement {

	/**
	 * sets parent statement to statement
	 * 
	 * @param parent
	 */
	public void setParent(Statement parent);

	public boolean hasFlowController();

	public void setHasFlowController(boolean has);

	/**
	 * @return returns the parent statement
	 */
	public Statement getParent();

	/**
	 * write out the statement to adapter
	 * 
	 * @param c
	 * @throws TemplateException
	 */
	public void writeOut(Context c) throws TransformerException;

	/**
	 * sets the line value.
	 * 
	 * @param line The line to set.
	 */
	public void setStart(Position startLine);

	/**
	 * sets the line value.
	 * 
	 * @param line The line to set.
	 */
	public void setEnd(Position endLine);

	/**
	 * @return the startLine
	 */
	public Position getStart();

	/**
	 * @return the endLine
	 */
	public Position getEnd();

	/**
	 * @return return the label where the finally block of this tags starts, IF there is a finally
	 *         block, otherwise return null;
	 */
	public FlowControlFinal getFlowControlFinal();

	public Factory getFactory();
}