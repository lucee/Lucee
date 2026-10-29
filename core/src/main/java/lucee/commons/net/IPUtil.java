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
package lucee.commons.net;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ListUtil;

public final class IPUtil {

	private static boolean isCacheEnabled = false;
	private static boolean isCacheValid = false;
	private static List<String> cachedLocalIPs = null;

	static {

		long tc = System.currentTimeMillis();

		List<String> localIPs = getLocalIPs(true);

		isCacheEnabled = System.currentTimeMillis() > tc;

		if (isCacheEnabled) {

			cachedLocalIPs = localIPs;
			isCacheValid = true;
		}
	}

	public static boolean isIPv4(String ip) {
		String[] arr = ListUtil.trimItems(ListUtil.trim(ListUtil.listToStringArray(ip, '.')));
		if (arr.length != 4) return false;

		int tmp;
		for (int i = 0; i < arr.length; i++) {
			tmp = Caster.toIntValue(arr[i], -1);
			if (tmp < 0 || tmp > 255) return false;
		}
		return true;
	}

	/**
	 * strict check if a string is an IPv4 address in dotted quad notation (a.b.c.d), every part is
	 * 0-255, written with 1-3 ASCII digits and without leading zeros (so "01" is not valid, because it
	 * is ambiguous: decimal or octal). This is a pure format check, nothing gets resolved.
	 */
	public static boolean isValidIPv4(String ip) {
		if (ip == null) return false;
		final int len = ip.length();
		if (len < 7 || len > 15) return false;
		int parts = 0;
		int i = 0;
		while (i < len) {
			int start = i;
			int value = 0;
			while (i < len && ip.charAt(i) >= '0' && ip.charAt(i) <= '9') {
				value = value * 10 + (ip.charAt(i) - '0');
				i++;
			}
			int digits = i - start;
			if (digits < 1 || digits > 3) return false;
			if (digits > 1 && ip.charAt(start) == '0') return false; // leading zero
			if (value > 255) return false;
			parts++;
			if (i < len) {
				if (ip.charAt(i) != '.') return false;
				i++;
				if (i == len) return false; // trailing dot
			}
		}
		return parts == 4;
	}

	/**
	 * strict check if a string is an IPv6 address in the text format of RFC 4291 section 2.2: groups of
	 * 1-4 hex digits separated by ":", one "::" compression for one or more groups of zeros, and an
	 * IPv4 address (strict, see isValidIPv4) as the last 32 bits (like ::ffff:1.2.3.4). A zone id
	 * ("fe80::1%eth0") is not accepted, it is not part of the address. This is a pure format check,
	 * nothing gets resolved.
	 */
	public static boolean isValidIPv6(String ip) {
		if (ip == null) return false;
		String str = ip;
		int len = str.length();
		if (len < 2 || len > 45) return false;

		// IPv4 in the last 32 bits, replace it with 2 groups
		if (str.indexOf('.') != -1) {
			int lastColon = str.lastIndexOf(':');
			if (lastColon == -1 || !isValidIPv4(str.substring(lastColon + 1))) return false;
			str = str.substring(0, lastColon + 1) + "0:0";
			len = str.length();
		}

		int groups = 0;
		boolean compressed = false;
		int i = 0;
		if (str.charAt(0) == ':') {
			if (str.charAt(1) != ':') return false;
			compressed = true;
			i = 2;
			if (i == len) return true; // "::"
		}
		while (i < len) {
			int start = i;
			while (i < len && isHex(str.charAt(i)))
				i++;
			int digits = i - start;
			if (digits < 1 || digits > 4) return false;
			groups++;
			if (i == len) break;
			if (str.charAt(i) != ':') return false;
			i++;
			if (i == len) return false; // single trailing colon
			if (str.charAt(i) == ':') {
				if (compressed) return false; // only one "::"
				compressed = true;
				i++;
			}
		}
		// the compression stands for at least one group
		return compressed ? groups <= 7 : groups == 8;
	}

	private static boolean isHex(char c) {
		return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
	}

	public static boolean isIPv62(String ip) {
		if (ip.indexOf(':') == -1) return false;
		String[] arr = ListUtil.trimItems(ListUtil.trim(ListUtil.listToStringArray(ip, ':')));
		if (arr.length != 8) return false;
		String str;
		int _int;
		for (int i = 0; i < arr.length; i++) {
			str = arr[i];
			if (!StringUtil.isEmpty(str)) {
				try {
					_int = Integer.parseInt(str, 16);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					_int = -1;
				}
				if (_int < 0 || _int > 65535) return false;
			}
		}
		return true;
	}

	public static boolean isIPv4(InetAddress addr) {
		return addr.getAddress().length == 4;
	}

	public static boolean isIPv6(InetAddress addr) {
		return !isIPv4(addr);
	}

	public static List<String> getLocalIPs(boolean refresh) {

		if (isCacheEnabled && isCacheValid && !refresh) {

			return new ArrayList<String>(cachedLocalIPs);
		}

		List<String> result = new ArrayList();

		try {

			Enumeration<NetworkInterface> eNics = NetworkInterface.getNetworkInterfaces();

			while (eNics.hasMoreElements()) {

				NetworkInterface nic = eNics.nextElement();

				if (nic.isUp()) {

					Enumeration<InetAddress> eAddr = nic.getInetAddresses();

					while (eAddr.hasMoreElements()) {

						InetAddress inaddr = eAddr.nextElement();

						String addr = inaddr.toString();

						if (addr.startsWith("/")) addr = addr.substring(1);

						if (addr.indexOf('%') > -1) addr = addr.substring(0, addr.indexOf('%')); // internal zone in some IPv6;
						// http://en.wikipedia.org/wiki/IPv6_Addresses#Link-local%5Faddresses%5Fand%5Fzone%5Findices

						result.add(addr);
					}
				}
			}
		}
		catch (SocketException e) {

			result.add("127.0.0.1");
			result.add("0:0:0:0:0:0:0:1");
		}

		if (isCacheEnabled) {

			cachedLocalIPs = result;
			isCacheValid = true;
		}

		return result;
	}

	/**
	 * this method can be called from Controller periodically, or from Admin if user clicks to
	 * invalidate the cache
	 */
	public void invalidateCache() {

		isCacheValid = false;
	}

	/** returns true if cache is used */
	public boolean isCacheEnabled() {

		return isCacheEnabled;
	}

}