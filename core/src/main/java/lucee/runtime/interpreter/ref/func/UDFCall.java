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
package lucee.runtime.interpreter.ref.func;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.ref.Ref;
import lucee.runtime.interpreter.ref.RefSupport;
import lucee.runtime.interpreter.ref.util.RefUtil;
import lucee.runtime.op.Caster;

/**
 * call of a User defined Function
 */
public final class UDFCall extends RefSupport implements Ref {

	private Ref[] arguments;
	private String name;
	private Ref parent;
	private Ref refName;

	/**
	 * @param pc
	 * @param parent
	 * @param name
	 * @param arguments
	 */
	public UDFCall(Ref parent, String name, Ref[] arguments) {
		this.parent = parent;
		this.name = name;
		this.arguments = arguments;
	}

	/**
	 * @param pc
	 * @param parent
	 * @param refName
	 * @param arguments
	 */
	public UDFCall(Ref parent, Ref refName, Ref[] arguments) {
		this.parent = parent;
		this.refName = refName;
		this.arguments = arguments;
	}

	@Override
	public Object getValue(PageContext pc) throws PageException {
		return pc.getVariableUtil().callFunction(pc, parent.getValue(pc), getName(pc), RefUtil.getValue(pc, arguments));
	}

	private String getName(PageContext pc) throws PageException {
		if (name != null) return name;
		return Caster.toString(refName.getValue(pc));
	}

	@Override
	public String getTypeName() {
		return "user defined function";
	}
}