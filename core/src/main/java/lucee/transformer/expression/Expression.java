/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
 */
package lucee.transformer.expression;

import lucee.runtime.exp.TemplateException;
import lucee.transformer.Context;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;

/**
 * An Expression (Operation, Literal aso.)
 */
public interface Expression {

	/**
	 * Field <code>MODE_REF</code>
	 */
	public static final int MODE_REF = 0;
	/**
	 * Field <code>MODE_VALUE</code>
	 */
	public static final int MODE_VALUE = 1;

	/**
	 * write out the stament to adapter
	 * 
	 * @param adapter
	 * @param mode
	 * @return return Type of expression
	 * @throws TemplateException
	 */
	public Class<?> writeOut(Context bc, int mode) throws TransformerException;

	public Position getStart();

	public Position getEnd();

	public void setStart(Position start);

	public void setEnd(Position end);

	public Factory getFactory();
}