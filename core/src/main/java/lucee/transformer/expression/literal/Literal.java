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
package lucee.transformer.expression.literal;

import lucee.transformer.expression.Expression;

/**
 * Literal
 */
public interface Literal extends Expression {

	/**
	 * @param defaultValue
	 * @return return value as String (CFML Rules)
	 */
	public String getString();

	/**
	 * @param defaultValue
	 * @return return value as Double Object
	 */
	public Number getNumber(Number defaultValue);

	/**
	 * @param defaultValue
	 * @return return value as a Boolean Object
	 */
	public Boolean getBoolean(Boolean defaultValue);

}