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
package lucee.runtime.cfx.customtag;

import java.util.Map;
import java.util.Set;

import com.allaire.cfx.CustomTag;

import lucee.commons.collection.MapFactory;
import lucee.runtime.cfx.CFXTagException;
import lucee.runtime.cfx.CFXTagPool;
import lucee.runtime.config.Config;
import lucee.runtime.type.util.ListUtil;

/**
 * 
 */
public final class CFXTagPoolImpl implements CFXTagPool {

	Config config;
	Map<String, CFXTagClass> classes;
	Map<String, CFXTagClass> objects = MapFactory.<String, CFXTagClass>getConcurrentMap();

	/**
	 * constructor of the class
	 * 
	 * @param classes
	 */
	public CFXTagPoolImpl(Map<String, CFXTagClass> classes) {
		this.classes = classes;
	}

	@Override
	public Map<String, CFXTagClass> getClasses() {
		return classes;
	}

	@Override
	public synchronized CustomTag getCustomTag(String name) throws CFXTagException {
		name = name.toLowerCase();

		Object o = classes.get(name);
		if (o == null) {
			Set<String> set = classes.keySet();
			String names = ListUtil.arrayToList(set.toArray(new String[set.size()]), ",");

			throw new CFXTagException("there is no Custom Tag (CFX) with name [" + name + "], available Custom Tags are [" + names + "]");
		}
		CFXTagClass ctc = (CFXTagClass) o;
		CustomTag ct = ctc.newInstance();
		// if(!(o instanceof CustomTag))throw new CFXTagException("["+name+"] is not of type
		// ["+CustomTag.class.getName()+"]");
		return ct;
	}

	@Override
	public synchronized CFXTagClass getCFXTagClass(String name) throws CFXTagException {
		name = name.toLowerCase();
		CFXTagClass ctc = classes.get(name);
		if (ctc == null) throw new CFXTagException("there is not Custom Tag (CFX) with name [" + name + "]");
		return ctc;
	}

	@Override
	public void releaseCustomTag(CustomTag ct) {
		// table.put(ct.getClass().toString(),ct);
	}

	@Override
	public void releaseTag(Object tag) {
		// table.put(ct.getClass().toString(),ct);
	}
}