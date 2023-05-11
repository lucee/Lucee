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
import java.util.Iterator;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.ref.Ref;
import lucee.runtime.interpreter.ref.RefSupport;
import lucee.runtime.interpreter.ref.util.RefUtil;
import lucee.runtime.op.Caster;

/**
 * Literal String
 *
 */
public final class LStringBuffer extends RefSupport implements Literal {

	private ArrayList refs = new ArrayList();
	private StringBuffer sb = new StringBuffer();

	/**
	 * constructor of the class
	 * 
	 * @param str
	 */
	public LStringBuffer(String str) {
		sb.append(str);
	}

	/**
	 * constructor of the class
	 * 
	 * @param str
	 */
	public LStringBuffer() {
	}

	@Override
	public Object getValue(PageContext pc) throws PageException {
		if (refs.size() == 0) return sb.toString();

		StringBuffer tmp = new StringBuffer();
		Iterator it = refs.iterator();
		while (it.hasNext()) {
			tmp.append(Caster.toString(((Ref) it.next()).getValue(pc)));
		}
		if (sb.length() > 0) tmp.append(sb);

		return tmp.toString();
	}

	public void append(Ref ref) {
		if (sb.length() > 0) {
			refs.add(new LString(sb.toString()));
			sb = new StringBuffer();
		}
		refs.add(ref);
	}

	public void append(char c) {
		sb.append(c);
	}

	public void append(String str) {
		sb.append(str);
	}

	public boolean isEmpty() {
		return sb.length() + refs.size() == 0;
	}

	@Override
	public String getTypeName() {
		return "literal";
	}

	@Override
	public String getString(PageContext pc) throws PageException {
		return (String) getValue(pc);
	}

	@Override
	public boolean eeq(PageContext pc, Ref other) throws PageException {
		return RefUtil.eeq(pc, this, other);
	}
}