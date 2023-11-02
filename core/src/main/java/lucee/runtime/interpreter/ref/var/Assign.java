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
package lucee.runtime.interpreter.ref.var;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.InterpreterException;
import lucee.runtime.interpreter.ref.Ref;
import lucee.runtime.interpreter.ref.RefSupport;
import lucee.runtime.interpreter.ref.Set;

public final class Assign extends RefSupport implements Ref {

	private Ref value;
	private Set coll;
	private final boolean limited;

	public Assign(Ref coll, Ref value, boolean limited) throws ExpressionException {
		if (!(coll instanceof Set)) throw new InterpreterException("Invalid assignment left-hand side [" + coll.getTypeName() + "]");
		this.coll = (Set) coll;
		this.value = value;
		this.limited = limited;
	}

	public Assign(Set coll, Ref value, boolean limited) {
		this.coll = coll;
		this.value = value;
		this.limited = limited;
	}

	@Override
	public Object getValue(PageContext pc) throws PageException {
		if (limited) throw new InterpreterException("Invalid syntax, variables are not supported in a JSON string.");
		return coll.setValue(pc, value.getValue(pc));
	}

	@Override
	public String getTypeName() {
		return "operation";
	}
}
