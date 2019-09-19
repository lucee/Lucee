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
package lucee.runtime.type.ref;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;

public class SimpleVarRef implements Reference {

	// private PageContextImpl pc;

	public SimpleVarRef(PageContextImpl pc, String key) {
		// this.pc=pc;
	}

	@Override
	public Object get(PageContext pc) throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object get(PageContext pc, Object defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection.Key getKey() throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getKeyAsString() throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object remove(PageContext pc) throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object removeEL(PageContext pc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object set(PageContext pc, Object value) throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object setEL(PageContext pc, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object touch(PageContext pc) throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object touchEL(PageContext pc) {
		// TODO Auto-generated method stub
		return null;
	}

}