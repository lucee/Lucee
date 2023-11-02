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

import java.util.List;

import lucee.transformer.Factory;
import lucee.transformer.Position;

/**
 * Body tag (Statement collector)
 */
public interface Body extends Statement {

	/**
	 * adds a statement to the Page
	 * 
	 * @param statement
	 */
	public abstract void addFirst(Statement statement);

	public abstract void addStatement(Statement statement);

	/**
	 * returns all statements
	 * 
	 * @return the statements
	 */
	public abstract boolean hasStatements();

	public abstract List<Statement> getStatements();

	/**
	 * move all statements to target body
	 * 
	 * @param trg
	 */
	public abstract void moveStatmentsTo(Body trg);

	/**
	 * returns if body has content or not
	 * 
	 * @return is empty
	 */
	public abstract boolean isEmpty();

	public void addPrintOut(Factory f, String str, Position start, Position end);

	public void remove(Statement stat);
}