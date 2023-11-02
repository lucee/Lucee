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
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lucee.commons.lang.ExceptionUtil;

/**
 * an efficient data structure for IP-range based settings
 */
public class IPSettings {

	public static final Map EMPTY = Collections.EMPTY_MAP;

	private IPRangeNode<Map> root, ipv4, ipv6;
	private boolean isSorted;
	private int version;

	public IPSettings() {

		try {

			root = new IPRangeNodeRoot();

			root.addChild(ipv4 = new IPRangeNode("0.0.0.0", "255.255.255.255"));
			root.addChild(ipv6 = new IPRangeNode("0:0:0:0:0:0:0:0", "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"));
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		} // all valid addresses, should never happen
	}

	/**
	 * all added data should go through this method
	 *
	 * @param ipr
	 * @param doCheck
	 */
	public synchronized void put(IPRangeNode<Map> ipr, boolean doCheck) {
		IPRangeNode parent = ipr.isV4() ? ipv4 : ipv6;
		parent.addChild(ipr, doCheck);
		version++;
		isSorted = false;
	}

	/** calls put( IPRangeNode ipr ) */
	public void put(IPRangeNode<Map> ipr) {

		this.put(ipr, true);
	}

	/**
	 * puts all the children at the IPv4 or IPv6 nodes for fast insertion. this method does not look for
	 * a more accurate insertion point and is useful when adding many items at once, e.g. for Country
	 * Codes of all known IP ranges
	 *
	 * @param children
	 */
	public void putAll(List<IPRangeNode<Map>> children) {

		for (IPRangeNode child: children) {

			this.put(child, false); // pass false for optimized insertion performance
		}
	}

	public void putSettings(String lower, String upper, Map settings) throws UnknownHostException {

		IPRangeNode<Map> ipr = new IPRangeNode(lower, upper);
		ipr.setData(settings);

		this.put(ipr);
	}

	public void putSettings(String addr, Map settings) throws UnknownHostException {

		if (addr.equals("*")) {

			root.setData(settings);
			return;
		}

		IPRangeNode<Map> ipr = new IPRangeNode(addr);
		ipr.setData(settings);

		this.put(ipr);
	}

	/**
	 * returns a single, best matching node for the given address
	 *
	 * @param addr
	 * @return
	 */
	public IPRangeNode get(InetAddress addr) {

		if (version == 0) // no data was added
			return null;

		IPRangeNode node = isV4(addr) ? ipv4 : ipv6;

		if (!this.isSorted) this.optimize();

		return node.findFast(addr);
	}

	/**
	 * returns a List of all the nodes (from root to best matching) for the given address
	 *
	 * @param iaddr
	 * @return
	 */
	public List<IPRangeNode> getChain(InetAddress iaddr) {

		List<IPRangeNode> result = new ArrayList();

		result.add(root);

		IPRangeNode node = isV4(iaddr) ? ipv4 : ipv6;
		node.findFast(iaddr, result);

		return result;
	}

	/**
	 * returns the cumulative settings for a given address
	 *
	 * @param iaddr
	 * @return
	 */
	public Map getSettings(InetAddress iaddr) {

		Map result = new TreeMap(String.CASE_INSENSITIVE_ORDER);

		List<IPRangeNode> chain = getChain(iaddr);

		for (IPRangeNode<Map> ipr: chain) {

			Map m = ipr.getData();
			if (m != null) result.putAll(m);
		}

		return result;
	}

	/**
	 * returns the cumulative settings for a given address
	 *
	 * @param addr
	 * @return
	 */
	public Map getSettings(String addr) {

		try {

			return this.getSettings(InetAddress.getByName(addr));
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}

		return EMPTY;
	}

	/**
	 * returns the settings for a single (non-cumulative) node that best matches the given address
	 *
	 * @param addr
	 * @return
	 */
	public Map getNodeSettings(InetAddress addr) {

		IPRangeNode<Map> ipr = this.get(addr);

		if (ipr != null) {

			Map result = ipr.getData();

			if (result != null) return result;
		}

		return EMPTY;
	}

	/**
	 * returns the settings for a single (non-cumulative) node that best matches the given address
	 *
	 * @param addr
	 * @return
	 */
	public Map getNodeSettings(String addr) {

		try {

			return this.getNodeSettings(InetAddress.getByName(addr));
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}

		return EMPTY;
	}

	public int getVersion() {

		return version;
	}

	/** sorts the data for fast binary search */
	private void optimize() {

		root.getChildren().sortChildren();

		isSorted = true;
	}

	/** returns true if the value is an IPv4 address */
	public static boolean isV4(InetAddress addr) {

		return addr instanceof Inet4Address;
	}

	/** returns true if the value is an IPv6 address */
	public static boolean isV6(InetAddress addr) {

		return addr instanceof Inet6Address;
	}

}