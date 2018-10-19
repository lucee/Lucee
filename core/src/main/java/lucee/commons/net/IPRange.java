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

import java.io.IOException;
import java.io.Serializable;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import lucee.commons.lang.StringUtil;
import lucee.runtime.type.util.ListUtil;

public class IPRange implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4427999443422764L;
    private static final short N256 = 256;
    private static final int SIZE = 4;

    private Range[] ranges = new Range[SIZE];
    int max = 0;

    private static class Range {
	private final short[] from;
	private final short[] to;
	private final boolean equality;

	private Range(short[] from, short[] to) throws IOException {
	    if (from.length != to.length) throw new IOException("both ip address must be from same type, IPv4 or IPv6");

	    this.from = from;
	    this.to = to;
	    this.equality = equal(from, to);
	}

	private Range(short[] from) {
	    this.from = from;
	    this.to = from;
	    this.equality = true;
	}

	private boolean inRange(short[] sarr) {
	    if (from.length != sarr.length) return false;

	    if (equality) return equal(from, sarr);

	    for (int i = 0; i < from.length; i++) {
		if (from[i] > sarr[i] || to[i] < sarr[i]) return false;
	    }
	    return true;
	}

	@Override
	public String toString() {
	    if (equality) return toString(from);
	    return toString(from) + "-" + toString(to);
	}

	private String toString(short[] sarr) {
	    if (sarr.length == 4) return new StringBuilder().append(sarr[0]).append(".").append(sarr[1]).append(".").append(sarr[2]).append(".").append(sarr[3]).toString();

	    return new StringBuilder().append(toHex(sarr[0], sarr[1], false)).append(":").append(toHex(sarr[2], sarr[3], true)).append(":").append(toHex(sarr[4], sarr[5], true))
		    .append(":").append(toHex(sarr[6], sarr[7], true)).append(":").append(toHex(sarr[8], sarr[9], true)).append(":").append(toHex(sarr[10], sarr[11], true))
		    .append(":").append(toHex(sarr[12], sarr[13], true)).append(":").append(toHex(sarr[14], sarr[15], false)).toString();

	}

	private String toHex(int first, int second, boolean allowEmpty) {
	    String str1 = Integer.toString(first, 16);
	    while (str1.length() < 2)
		str1 = "0" + str1;
	    String str2 = Integer.toString(second, 16);
	    while (str2.length() < 2)
		str2 = "0" + str2;
	    str1 += str2;
	    if (allowEmpty && str1.equals("0000")) return "";

	    while (str1.length() > 1 && str1.charAt(0) == '0')
		str1 = str1.substring(1);

	    return str1;
	}

	private boolean equal(short[] left, short[] right) {
	    for (int i = 0; i < left.length; i++) {
		if (left[i] != right[i]) return false;
	    }
	    return true;
	}

    }

    private void add(String ip) throws IOException {
	ip = ip.trim();
	// no wildcard defined
	if (ip.indexOf('*') == -1) {
	    add(new Range(toShortArray(toInetAddress(ip))));
	    return;
	}

	if ("*".equals(ip)) {
	    add("*.*.*.*");
	    add("*:*:*:*:*:*:*:*");
	    return;
	}

	String from = ip.replace('*', '0');
	String to;
	InetAddress addr1 = toInetAddress(from);
	if (addr1 instanceof Inet6Address) to = StringUtil.replace(ip, "*", "ffff", false);
	else to = StringUtil.replace(ip, "*", "255", false);
	add(new Range(toShortArray(addr1), toShortArray(toInetAddress(to))));
    }

    private void add(String ip1, String ip2) throws IOException {
	add(new Range(toShortArray(toInetAddress(ip1)), toShortArray(toInetAddress(ip2))));
    }

    public static IPRange getInstance(String raw) throws IOException {
	return getInstance(ListUtil.listToStringArray(raw, ','));
    }

    public static IPRange getInstance(String[] raw) throws IOException {
	IPRange range = new IPRange();
	String[] arr = ListUtil.trimItems(ListUtil.trim(raw));
	String str;
	int index;
	for (int i = 0; i < arr.length; i++) {
	    str = arr[i];
	    if (str.length() > 0) {
		index = str.indexOf('-');
		if (index != -1) range.add(str.substring(0, index), str.substring(index + 1));
		else range.add(str);
	    }
	}
	return range;

    }

    private synchronized void add(Range range) {
	if (max >= ranges.length) {
	    Range[] tmp = new Range[ranges.length + SIZE];
	    for (int i = 0; i < ranges.length; i++) {
		tmp[i] = ranges[i];
	    }
	    ranges = tmp;
	}
	ranges[max++] = range;
    }

    public boolean inRange(String ip) throws IOException {
	return inRange(toShortArray(ip));
    }

    public boolean inRange(short[] ip) {
	for (int i = 0; i < max; i++) {
	    if (ranges[i].inRange(ip)) return true;
	}
	return false;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < max; i++) {
	    if (i > 0) sb.append(",");
	    sb.append(ranges[i].toString());
	}
	return sb.toString();
    }

    public static short[] toShortArray(String ip) throws IOException {
	return toShortArray(toInetAddress(ip));
    }

    private static InetAddress toInetAddress(String ip) throws IOException {
	// TODO Auto-generated method stub
	try {
	    return InetAddress.getByName(ip);
	}
	catch (UnknownHostException e) {
	    throw new IOException("cannot parse the ip [" + ip + "]");
	}
    }

    private static short[] toShortArray(InetAddress ia) {
	byte[] addr = ia.getAddress();
	short[] sarr = new short[addr.length];
	for (int i = 0; i < addr.length; i++) {
	    sarr[i] = byte2short(addr[i]);
	}
	return sarr;
    }

    private static short byte2short(byte b) {
	if (b < 0) return (short) (b + N256);
	return b;
    }
}