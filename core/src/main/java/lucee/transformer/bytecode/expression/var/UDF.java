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
package lucee.transformer.bytecode.expression.var;

import lucee.transformer.bytecode.Page;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;

public final class UDF extends FunctionMember {

	private ExprString name;

	public UDF(Expression name) {
		this.name = name.getFactory().toExprString(name);
	}

	public UDF(Page page, String name) {
		this.name = page.getFactory().createLitString(name);
	}

	/**
	 * @return the name
	 */
	public ExprString getName() {
		return name;
	}
}