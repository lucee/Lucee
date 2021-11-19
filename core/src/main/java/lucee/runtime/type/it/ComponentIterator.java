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
package lucee.runtime.type.it;

import java.util.Iterator;

import lucee.runtime.ComponentImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Resetable;
import lucee.runtime.type.util.KeyConstants;

public class ComponentIterator implements Iterator, Resetable {

	private static final Object[] EMPTY = new Object[0];

	private ComponentImpl cfc;

	public ComponentIterator(ComponentImpl cfc) {
		this.cfc = cfc;
	}

	@Override
	public boolean hasNext() {
		try {
			return Caster.toBooleanValue(cfc.call(ThreadLocalPageContext.get(), KeyConstants.__hasNext, EMPTY));
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	@Override
	public Object next() {
		try {
			return cfc.call(ThreadLocalPageContext.get(), KeyConstants.__next, EMPTY);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	@Override
	public void remove() {
		try {
			cfc.call(ThreadLocalPageContext.get(), KeyConstants.__remove, EMPTY);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	@Override
	public void reset() {
		try {
			cfc.call(ThreadLocalPageContext.get(), KeyConstants.__reset, EMPTY);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}
}