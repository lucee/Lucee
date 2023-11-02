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
package lucee.runtime.net.ipsettings;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IPRangeCollection {

	private List<IPRangeNode> list = Collections.EMPTY_LIST;

	void add(IPRangeNode child, boolean doCheck) {

		if (list == Collections.EMPTY_LIST) list = new ArrayList();
		else if (doCheck) {

			// scan for previous children in parent that should be moved under the newly added child after this
			// addition

			int listSize = list.size();
			for (int i = 0; i < listSize; i++) {

				IPRangeNode sibling = list.get(i);

				if (child.containsRange(sibling)) { // move sibling under new child

					list.remove(i--); // adjust i and numChildren due to removal
					listSize--;

					child.addChild(sibling);
				}
			}
		}

		list.add(child);
	}

	public void add(IPRangeNode child) {

		this.add(child, true);
	}

	public IPRangeNode findFast(InetAddress iaddr, List<IPRangeNode> parents) {

		IPRangeNode needle, parent;

		needle = new IPRangeNode(iaddr, iaddr);

		int pos = Collections.binarySearch(list, needle, IPRangeNode.comparerRange);

		if (pos > -1) {

			parent = list.get(pos);
			return parent.findFast(iaddr, parents);
		}

		int tests = 2;
		pos = Math.abs(pos);

		pos = Math.max(0, pos - tests);
		int max = Math.min(pos + tests, list.size());

		for (; pos < max; pos++) {

			if (list.get(pos).isInRange(iaddr)) {

				parent = list.get(pos);
				return parent.findFast(iaddr, parents);
			}
		}

		return null;
	}

	/**
	 * performs a binary search over a sorted list
	 *
	 * @param iaddr
	 * @return
	 */
	public IPRangeNode findFast(InetAddress iaddr) {

		IPRangeNode needle, parent;

		needle = new IPRangeNode(iaddr, iaddr);

		int pos = Collections.binarySearch(list, needle, IPRangeNode.comparerRange);

		if (pos > -1) {

			parent = list.get(pos);
			return parent.findFast(iaddr);
		}

		int tests = 2;
		pos = Math.abs(pos);

		pos = Math.max(0, pos - tests);
		int max = Math.min(pos + tests, list.size());

		for (; pos < max; pos++) {

			if (list.get(pos).isInRange(iaddr)) {

				parent = list.get(pos);
				return parent.findFast(iaddr);
			}
		}

		return null;
	}

	/**
	 * performs a binary search over sorted list
	 *
	 * @param addr
	 * @return
	 */
	public IPRangeNode findFast(String addr) {

		InetAddress iaddr;

		try {

			iaddr = InetAddress.getByName(addr);
		}
		catch (UnknownHostException ex) {

			return null;
		}

		return findFast(iaddr);
	}

	/**
	 * performs a linear scan for unsorted lists
	 *
	 * @param addr
	 * @return
	 */
	public IPRangeNode findAddr(InetAddress addr) {

		for (IPRangeNode c: this.list) {

			IPRangeNode result = c.findAddr(addr);

			if (result != null) return result;
		}

		return null;
	}

	/**
	 * performs a linear scan for unsorted lists
	 *
	 * @param child
	 * @return
	 */
	public IPRangeNode findRange(IPRangeNode child) {

		for (IPRangeNode c: this.list) {

			IPRangeNode result = c.findRange(child);

			if (result != null) return result;
		}

		return null;
	}

	public int size() {

		return list.size();
	}

	private void sort() {

		Collections.sort(this.list);
	}

	public void sortChildren() {

		for (IPRangeNode node: this.list) {

			node.getChildren().sort();
		}
	}

}