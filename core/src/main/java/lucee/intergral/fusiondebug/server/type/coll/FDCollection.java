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
package lucee.intergral.fusiondebug.server.type.coll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.intergral.fusiondebug.server.FDLanguageException;
import com.intergral.fusiondebug.server.FDMutabilityException;
import com.intergral.fusiondebug.server.IFDStackFrame;

import lucee.intergral.fusiondebug.server.type.FDValueSupport;
import lucee.intergral.fusiondebug.server.type.FDVariable;
import lucee.intergral.fusiondebug.server.util.FDCaster;
import lucee.runtime.Component;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.comparator.TextComparator;
import lucee.runtime.type.util.CollectionUtil;

public class FDCollection extends FDValueSupport {

	private static final int INTERVAL = 10;
	private ArrayList children;
	private Collection coll;
	private String name;
	private Key[] keys;

	/**
	 * Constructor of the class
	 * 
	 * @param frame
	 * @param name
	 * @param name
	 * @param coll
	 */

	public FDCollection(IFDStackFrame frame, String name, Collection coll) {
		this(frame, name, coll, keys(coll));
	}

	public FDCollection(IFDStackFrame frame, String name, Collection coll, Key[] keys) {

		this.name = name;
		this.coll = coll;
		this.keys = keys;
		// Key[] keys = coll.keys();
		children = new ArrayList();

		int interval = INTERVAL;
		while (interval * interval < keys.length)
			interval *= interval;

		if (keys.length > interval) {
			FDCollection node;

			int len = keys.length;

			int max;
			for (int i = 0; i < len; i += interval) {
				max = (i + (interval)) < len ? (interval) : len - i;
				Key[] skeys = new Key[max];
				for (int y = 0; y < max; y++) {
					skeys[y] = keys[i + y];
				}
				node = new FDCollection(frame, "Rows", coll, skeys);
				children.add(new FDVariable(frame, node.getName(), node));
			}
		}
		else {
			FDCollectionNode node;
			for (int i = 0; i < keys.length; i++) {
				node = new FDCollectionNode(frame, coll, keys[i]);
				children.add(new FDVariable(frame, node.getName(), node));
			}
		}
	}

	private static Key[] keys(Collection coll) {
		Key[] keys = CollectionUtil.keys(coll);
		if (coll instanceof Array) return keys;
		TextComparator comp = new TextComparator(true, true);
		Arrays.sort(keys, comp);
		return keys;
	}

	@Override
	public List getChildren() {
		return children;
	}

	public IFDStackFrame getStackFrame() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public void set(String arg0) throws FDMutabilityException, FDLanguageException {
		throw new FDMutabilityException();
	}

	@Override
	public boolean hasChildren() {
		return true;
	}

	@Override
	public String toString() {
		if (coll instanceof Array) return "[" + fromto() + "]";
		if (coll instanceof Component) {
			Component c = (Component) coll;
			return "Component " + c.getName() + "(" + c.getPageSource().getDisplayPath() + ")";
		}
		if (coll instanceof Struct) return "{" + fromto() + "}";
		return FDCaster.serialize(coll);
	}

	private String fromto() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < keys.length; i++) {
			if (i != 0) sb.append(",");
			sb.append(keys[i].toString());
		}
		return keys[0] + " ... " + keys[keys.length - 1];
	}

	@Override
	public String getName() {
		return name;
	}
}