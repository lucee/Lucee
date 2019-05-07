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
package lucee.runtime.interpreter.ref.literal;

import java.util.ArrayList;
import java.util.List;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.InterpreterException;
import lucee.runtime.interpreter.ref.Ref;
import lucee.runtime.interpreter.ref.RefSupport;
import lucee.runtime.interpreter.ref.Set;
import lucee.runtime.interpreter.ref.var.Variable;
import lucee.runtime.type.FunctionValueImpl;

/**
 * ref for a functionValue
 */
public final class LFunctionValue extends RefSupport implements Ref {

	private Ref name;
	private Ref refValue;
	private Object objValue;

	/**
	 * constructor of the class
	 * 
	 * @param name
	 * @param value
	 */
	public LFunctionValue(Ref name, Ref value) {
		this.name = name;
		this.refValue = value;
	}

	public LFunctionValue(Ref name, Object value) {
		this.name = name;
		this.objValue = value;
	}

	@Override
	public Object getValue(PageContext pc) throws PageException {

		if (name instanceof Variable) {
			return new FunctionValueImpl(toStringArray(pc, (Set) name), refValue == null ? objValue : refValue.getValue(pc));
		}
		if (name instanceof Literal) {
			return new FunctionValueImpl(((Literal) name).getString(pc), refValue == null ? objValue : refValue.getValue(pc));
		}

		// TODO no idea if this is ever used
		if (name instanceof Set) {
			return new FunctionValueImpl(lucee.runtime.type.util.ListUtil.arrayToList(toStringArray(pc, (Set) name), "."), refValue == null ? objValue : refValue.getValue(pc));
		}
		throw new InterpreterException("invalid syntax in named argument");
		// return new FunctionValueImpl(key,value.getValue());
	}

	public static String[] toStringArray(PageContext pc, Set set) throws PageException {
		Ref ref = set;
		String str;
		List<String> arr = new ArrayList<String>();
		do {
			set = (Set) ref;
			str = set.getKeyAsString(pc);
			if (str != null) arr.add(0, str);
			else break;
			ref = set.getParent(pc);
		}
		while (ref instanceof Set);
		return arr.toArray(new String[arr.size()]);
	}

	@Override
	public String getTypeName() {
		return "function value";
	}

}