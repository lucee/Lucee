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
package lucee.runtime.net.rpc;

import java.lang.reflect.Method;
import java.util.Iterator;

import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Pair;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Pojo;

public class PojoIterator implements Iterator<Pair<Collection.Key, Object>> {

	private static final Object[] EMPTY_ARG = new Object[] {};

	private Pojo pojo;
	private Method[] getters;
	private Class<? extends Pojo> clazz;
	private int index = -1;

	public PojoIterator(Pojo pojo) {
		this.pojo = pojo;
		this.clazz = pojo.getClass();
		getters = Reflector.getGetters(pojo.getClass());
	}

	public int size() {
		return getters.length;
	}

	@Override
	public boolean hasNext() {
		return (index + 1) < getters.length;
	}

	@Override
	public Pair<Collection.Key, Object> next() {
		Method g = getters[++index];
		try {

			return new Pair<Collection.Key, Object>(KeyImpl.init(g.getName().substring(3)), g.invoke(pojo, EMPTY_ARG));
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw new PageRuntimeException(Caster.toPageException(t));
		}
	}

	@Override
	public void remove() {
		throw new RuntimeException("method remove is not supported!");
	}

}