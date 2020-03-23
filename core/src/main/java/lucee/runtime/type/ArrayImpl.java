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
package lucee.runtime.type;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import edu.emory.mathcs.backport.java.util.Collections;
import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Duplicator;
import lucee.runtime.op.ThreadLocalDuplication;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.wrap.ListAsArray;

/**
 * CFML array object implements Array,List,Objects
 */
public class ArrayImpl extends ListAsArray {

	private static final long serialVersionUID = -6187994169003839005L;
	public static final int DEFAULT_CAP = 32;

	public ArrayImpl() {
		this(DEFAULT_CAP, true);
	}

	public ArrayImpl(int initalCap) {
		this(initalCap, true);
	}

	public ArrayImpl(int initalCap, boolean sync) {
		super(sync ? Collections.synchronizedList(new ArrayList(initalCap)) : new ArrayList(initalCap));
	}

	public ArrayImpl(Object[] objects) {
		this(ArrayUtil.isEmpty(objects) ? 32 : objects.length);

		for (int i = 0; i < objects.length; i++) {
			setEL(i + 1, objects[i]);
		}
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return duplicate(new ArrayImpl(), deepCopy);
	}

	protected Collection duplicate(ArrayImpl arr, boolean deepCopy) {
		Iterator<Entry<Key, Object>> it = entryIterator();
		boolean inside = deepCopy ? ThreadLocalDuplication.set(this, arr) : true;
		Entry<Key, Object> e;
		try {
			while (it.hasNext()) {
				e = it.next();
				if (deepCopy) arr.set(e.getKey(), Duplicator.duplicate(e.getValue(), deepCopy));
				else arr.set(e.getKey(), e.getValue());
			}
		}
		catch (PageException ee) {} // MUST habdle this
		finally {
			if (!inside) ThreadLocalDuplication.reset();
		}

		return arr;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		DumpTable table = new DumpTable("array", "#99cc33", "#ccff33", "#000000");
		table.setTitle("Array");

		int top = dp.getMaxlevel();

		if (size() > top) table.setComment("Rows: " + size() + " (showing top " + top + ")");
		else if (size() > 10 && dp.getMetainfo()) table.setComment("Rows: " + size());

		int length = size();

		for (int i = 1; i <= length; i++) {
			Object o = null;
			try {
				o = getE(i);
			}
			catch (Exception e) {}

			table.appendRow(1, new SimpleDumpData(i), DumpUtil.toDumpData(o, pageContext, maxlevel, dp));

			if (i == top) break;
		}

		return table;
	}
}