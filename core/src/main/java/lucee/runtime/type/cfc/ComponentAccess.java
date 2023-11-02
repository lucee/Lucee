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
package lucee.runtime.type.cfc;

import java.util.Iterator;
import java.util.Set;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.component.Member;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;

public interface ComponentAccess extends Component {

	@Override
	public boolean isPersistent();

	@Override
	public Object getMetaStructItem(Collection.Key name);

	@Override
	Set<Key> keySet(int access);

	@Override
	Object call(PageContext pc, int access, Collection.Key name, Object[] args) throws PageException;

	@Override
	Object callWithNamedValues(PageContext pc, int access, Collection.Key name, Struct args) throws PageException;

	@Override
	int size(int access);

	@Override
	Collection.Key[] keys(int access);

	@Override
	Iterator<Collection.Key> keyIterator(int access);

	@Override
	Iterator<String> keysAsStringIterator(int access);

	@Override
	Iterator<Entry<Key, Object>> entryIterator(int access);

	@Override
	Iterator<Object> valueIterator(int access);

	@Override
	Object get(int access, Collection.Key key) throws PageException;

	@Override
	Object get(int access, Collection.Key key, Object defaultValue);

	@Override
	DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp, int access);

	@Override
	boolean contains(int access, Key name);

	@Override
	Member getMember(int access, Collection.Key key, boolean dataMember, boolean superAccess);

	public ComponentAccess _base();// TODO do better impl
	// public boolean isRest();

	@Override
	public void setEntity(boolean entity);

	@Override
	public boolean isEntity();

}