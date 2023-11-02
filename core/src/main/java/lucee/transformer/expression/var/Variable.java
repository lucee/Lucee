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
package lucee.transformer.expression.var;

import lucee.transformer.Context;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.expression.var.Assign;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.Invoker;

public interface Variable extends Expression, Invoker {

	public int getScope();

	/**
	 * @return the first member or null if there no member
	 */
	public Member getFirstMember();

	/**
	 * @return the first member or null if there no member
	 */
	public Member getLastMember();

	public void ignoredFirstMember(boolean b);

	public boolean ignoredFirstMember();

	public void fromHash(boolean fromHash);

	public boolean fromHash();

	public Expression getDefaultValue();

	public void setDefaultValue(Expression defaultValue);

	public Boolean getAsCollection();

	public void setAsCollection(Boolean asCollection);

	public int getCount();

	public Class<?> writeOutCollection(Context c, int mode) throws TransformerException;

	Member removeMember(int index);

	public void assign(Assign assign);

	public Assign assign();

}