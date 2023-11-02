/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.interpreter.ref.var;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.InterpreterException;
import lucee.runtime.interpreter.ref.Ref;
import lucee.runtime.interpreter.ref.RefSupport;
import lucee.runtime.interpreter.ref.Set;
import lucee.runtime.interpreter.ref.literal.LString;
import lucee.runtime.op.Caster;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.StructImpl;

/**
 * 
 */
public final class Variable extends RefSupport implements Set {

	private String key;
	private Ref parent;
	private Ref refKey;
	private boolean limited;

	/**
	 * @param pc
	 * @param parent
	 * @param key
	 */
	public Variable(Ref parent, String key, boolean limited) {
		this.parent = parent;
		this.key = key;
		this.limited = limited;
	}

	/**
	 * @param pc
	 * @param parent
	 * @param refKey
	 */
	public Variable(Ref parent, Ref refKey, boolean limited) {
		this.parent = parent;
		this.refKey = refKey;
		this.limited = limited;
	}

	@Override
	public Object getValue(PageContext pc) throws PageException {
		if (limited) throw new InterpreterException("invalid syntax, variables are not supported in a json string.");
		return pc.get(parent.getCollection(pc), KeyImpl.init(getKeyAsString(pc)));
	}

	@Override
	public Object touchValue(PageContext pc) throws PageException {
		if (limited) throw new InterpreterException("invalid syntax, variables are not supported in a json string.");
		Object p = parent.touchValue(pc);
		if (p instanceof Query) {
			Object o = ((Query) p).getColumn(KeyImpl.init(getKeyAsString(pc)), null);
			if (o != null) return o;
			return setValue(pc, new StructImpl());
		}

		return pc.touch(p, KeyImpl.init(getKeyAsString(pc)));
	}

	@Override
	public Object getCollection(PageContext pc) throws PageException {
		if (limited) throw new InterpreterException("invalid syntax, variables are not supported in a json string.");
		Object p = parent.getValue(pc);
		if (p instanceof Query) {
			return ((Query) p).getColumn(KeyImpl.init(getKeyAsString(pc)));
		}
		return pc.get(p, KeyImpl.init(getKeyAsString(pc)));
	}

	@Override
	public Object setValue(PageContext pc, Object obj) throws PageException {
		if (limited) throw new InterpreterException("invalid syntax, variables are not supported in a json string.");
		return pc.set(parent.touchValue(pc), KeyImpl.init(getKeyAsString(pc)), obj);
	}

	@Override
	public String getTypeName() {
		return "variable";
	}

	@Override
	public Ref getKey(PageContext pc) throws PageException {
		if (key == null) return refKey;
		return new LString(key);
	}

	@Override
	public String getKeyAsString(PageContext pc) throws PageException {
		if (key == null) key = Caster.toString(refKey.getValue(pc));
		return key;
	}

	@Override
	public Ref getParent(PageContext pc) throws PageException {
		return parent;
	}
}