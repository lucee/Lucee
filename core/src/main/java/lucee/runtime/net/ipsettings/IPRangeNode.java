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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Comparator;
import java.util.List;

import lucee.commons.lang.ExceptionUtil;

public class IPRangeNode<T> implements Comparable<IPRangeNode>, Comparator<IPRangeNode> {

	private InetAddress lower;
	private InetAddress upper;
	private boolean isSingle;

	private T data;
	private IPRangeCollection children;

	public IPRangeNode(InetAddress lower, InetAddress upper) {

		int c = comparerIAddr.compare(lower, upper);

		if (c <= 0) {
			this.lower = lower;
			this.upper = upper;
		}
		else {
			this.lower = upper;
			this.upper = lower;
		}

		this.isSingle = (c == 0);
		this.children = new IPRangeCollection();
	}

	public IPRangeNode(String lower, String upper) throws UnknownHostException {

		this(InetAddress.getByName(lower), InetAddress.getByName(upper));
	}

	public IPRangeNode(String addr) throws UnknownHostException {

		this(addr, addr);
	}

	public boolean isSingleAddress() {

		return isSingle;
	}

	public boolean isInRange(InetAddress addr) {

		if (this.isV4() != IPSettings.isV4(addr)) return false;

		return comparerIAddr.compare(lower, addr) <= 0 && comparerIAddr.compare(upper, addr) >= 0;
	}

	public boolean containsRange(IPRangeNode other) {

		if (this.isV4() != other.isV4()) return false;

		return this.isInRange(other.lower) && this.isInRange(other.upper);
	}

	/**
	 *
	 * @param child
	 * @param doCheck - passing false will avoid searching for a "better" parent, for a more efficient
	 *            insert in large data sets (e.g. Country Codes of all known ranges)
	 * @return - true if the child was added
	 */
	synchronized boolean addChild(IPRangeNode child, boolean doCheck) {

		if (!this.containsRange(child)) return false;

		IPRangeNode parent = this;

		if (doCheck) parent = findRange(child);

		// TODO: check for eqaulity of new child and found parent

		parent.children.add(child, doCheck);
		return true;
	}

	/** calls addChild( child, true ) */
	public boolean addChild(IPRangeNode child) {

		return addChild(child, true);
	}

	public T getData() {

		return data;
	}

	public void setData(T data) {

		this.data = data;
	}

	public IPRangeNode findRange(IPRangeNode child) {

		IPRangeNode result = null;

		if (this.containsRange(child)) {

			result = this;

			IPRangeNode temp = this.children.findRange(child);
			if (temp != null) result = temp;
		}

		return result;
	}

	public IPRangeNode findAddr(InetAddress iaddr) {

		IPRangeNode result = null;

		if (this.isInRange(iaddr)) {

			result = this;

			if (this.hasChildren()) {

				IPRangeNode temp = children.findAddr(iaddr);
				if (temp != null) result = temp;
			}
		}

		return result;
	}

	public IPRangeNode findAddr(String addr) {

		try {

			return findAddr(InetAddress.getByName(addr));
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}

		return null;
	}

	public IPRangeNode findFast(InetAddress iaddr, List<IPRangeNode> parents) {

		IPRangeNode result = null;

		if (this.isInRange(iaddr)) {

			result = this;
			if (parents != null) parents.add(result);

			if (this.hasChildren()) {

				IPRangeNode temp = children.findFast(iaddr, parents);

				if (temp != null) result = temp;
			}
		}

		return result;
	}

	public IPRangeNode findFast(InetAddress iaddr) {

		return findFast(iaddr, null);
	}

	/*
	 * / works public IPRangeNode findFast(InetAddress iaddr) {
	 * 
	 * IPRangeNode result = null;
	 * 
	 * if ( this.isInRange(iaddr) ) {
	 * 
	 * result = this;
	 * 
	 * if ( this.hasChildren() ) {
	 * 
	 * IPRangeNode temp = children.findFast( iaddr ); if ( temp != null ) result = temp; } }
	 * 
	 * return result; } //
	 */

	public IPRangeNode findFast(String addr) {

		try {

			return findFast(InetAddress.getByName(addr));
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}

		return null;
	}

	IPRangeCollection getChildren() {

		return children;
	}

	boolean hasChildren() {

		return children.size() > 0;
	}

	@Override
	public int compareTo(IPRangeNode other) {

		int c = comparerIAddr.compare(this.lower, other.lower);

		if (c != 0) return c;

		c = comparerIAddr.compare(this.upper, other.upper);

		return c;
	}

	@Override
	public int compare(IPRangeNode lhs, IPRangeNode rhs) {

		return lhs.compareTo(rhs);
	}

	@Override
	public boolean equals(Object o) {

		if (o instanceof IPRangeNode) {

			return this.compareTo((IPRangeNode) o) == 0;
		}

		return false;
	}

	@Override
	public int hashCode() {

		return this.lower.hashCode();
	}

	@Override
	public String toString() {

		if (isSingle) return this.lower.toString().substring(1) + String.format(" (%d)", this.children.size());

		return this.lower.toString().substring(1) + " - " + this.upper.toString().substring(1) + String.format(" (%d)", this.children.size());
	}

	public boolean isV4() {

		return IPSettings.isV4(this.lower);
	}

	public boolean isV6() {

		return IPSettings.isV6(this.lower);
	}

	public static final Comparator<IPRangeNode> comparerRange = new Comparator<IPRangeNode>() {

		@Override
		public int compare(IPRangeNode lhs, IPRangeNode rhs) {

			return lhs.compareTo(rhs);
		}
	};

	public static final Comparator<InetAddress> comparerIAddr = new Comparator<InetAddress>() {

		@Override
		public int compare(InetAddress lhs, InetAddress rhs) {

			if ((lhs instanceof Inet4Address) != (rhs instanceof Inet4Address)) throw new IllegalArgumentException("Both arguments must be of the same IP Version");

			byte[] barrLhs = lhs.getAddress();
			byte[] barrRhs = rhs.getAddress();

			for (int i = 0; i < barrLhs.length; i++) {

				int l = barrLhs[i] & 0xff; // fix signed bit in byte
				int r = barrRhs[i] & 0xff;

				if (l < r) return -1;
				else if (l > r) return 1;
			}

			return 0; // equal
		}
	};

}