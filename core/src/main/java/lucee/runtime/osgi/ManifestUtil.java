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
package lucee.runtime.osgi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.type.util.ListUtil;

public class ManifestUtil {

	private static final int DEFAULT_MAX_LINE_SIZE = 100;
	private static final Set<String> DEFAULT_MAIN_FILTER = new HashSet<String>();
	static {
		DEFAULT_MAIN_FILTER.add("Manifest-Version");
	}
	private static final Set<String> DEFAULT_INDIVIDUAL_FILTER = new HashSet<String>();
	static {
		DEFAULT_INDIVIDUAL_FILTER.add("Name");
	}

	public static String toString(Manifest manifest, int maxLineSize, Set<String> mainSectionIgnore, Set<String> individualSectionIgnore) {
		if (maxLineSize < 0) maxLineSize = DEFAULT_MAX_LINE_SIZE;
		StringBuilder msb = new StringBuilder();
		Attributes main = manifest.getMainAttributes();

		// prepare ignores
		if (mainSectionIgnore == null) mainSectionIgnore = DEFAULT_MAIN_FILTER;
		else mainSectionIgnore.addAll(DEFAULT_MAIN_FILTER);
		if (individualSectionIgnore == null) individualSectionIgnore = DEFAULT_INDIVIDUAL_FILTER;
		else individualSectionIgnore.addAll(DEFAULT_INDIVIDUAL_FILTER);

		// Manifest-Version comes first
		add(msb, "Manifest-Version", main.getValue("Manifest-Version"), "1.0");
		// all other main attributes
		printSection(msb, main, maxLineSize, mainSectionIgnore);

		// individual entries

		Map<String, Attributes> entries = manifest.getEntries();
		if (entries != null && entries.size() > 0) {
			Iterator<Entry<String, Attributes>> it = entries.entrySet().iterator();
			Entry<String, Attributes> e;
			StringBuilder sb;
			while (it.hasNext()) {
				e = it.next();
				sb = new StringBuilder();
				printSection(sb, e.getValue(), maxLineSize, individualSectionIgnore);
				if (sb.length() > 0) {
					msb.append('\n'); // new section need an empty line
					add(msb, "Name", e.getKey(), null);
					msb.append(sb);
				}
			}
		}

		return msb.toString();
	}

	private static void printSection(StringBuilder sb, Attributes attrs, int maxLineSize, Set<String> ignore) {
		Iterator<Entry<Object, Object>> it = attrs.entrySet().iterator();
		Entry<Object, Object> e;
		String name;
		String value;
		while (it.hasNext()) {
			e = it.next();
			name = ((Name) e.getKey()).toString();
			value = (String) e.getValue();
			if (StringUtil.isEmpty(value)) continue;
			if ("Import-Package".equals(name) || "Export-Package".equals(name) || "Require-Bundle".equals(name)) {
				value = splitByComma(value);

			}
			else if (value.length() > maxLineSize) value = split(value, maxLineSize);

			if (ignore != null && ignore.contains(name)) continue;
			add(sb, name, value, null);
		}
	}

	private static String splitByComma(String value) {
		StringTokenizer st = new StringTokenizer(value.trim(), ",");
		StringBuilder sb = new StringBuilder();
		while (st.hasMoreTokens()) {
			if (sb.length() > 0) sb.append(",\n ");
			sb.append(st.nextToken().trim());
		}
		return sb.toString();
	}

	private static String split(String value, int max) {
		StringTokenizer st = new StringTokenizer(value, "\n");
		StringBuilder sb = new StringBuilder();
		while (st.hasMoreTokens()) {
			_split(sb, st.nextToken(), max);
		}
		return sb.toString();
	}

	private static void _split(StringBuilder sb, String value, int max) {
		int index = 0;
		while (index + max <= value.length()) {
			if (sb.length() > 0) sb.append("\n ");
			sb.append(value.substring(index, index + max));
			index = index + max;
		}
		if (index < value.length()) {
			if (sb.length() > 0) sb.append("\n ");
			sb.append(value.substring(index, value.length()));
		}
	}

	private static void add(StringBuilder sb, String name, String value, String defaultValue) {
		if (value == null) {
			if (defaultValue == null) return;
			value = defaultValue;
		}
		sb.append(name).append(": ").append(value).append('\n');
	}

	public static void removeFromList(Attributes attrs, String key, String valueToRemove) {
		String val = attrs.getValue(key);
		if (StringUtil.isEmpty(val)) return;
		StringBuilder sb = new StringBuilder();
		boolean removed = false;

		boolean wildcard = false;
		if (valueToRemove.endsWith(".*")) {
			wildcard = true;
			valueToRemove = valueToRemove.substring(0, valueToRemove.length() - 1);
		}

		try {

			Iterator<String> it = toList(val).iterator();// ListUtil.toStringArray(ListUtil.listToArrayTrim(val, ','));
			String str;
			while (it.hasNext()) {
				str = it.next();
				str = str.trim();
				// print.e("=="+str);

				if (wildcard ? str.startsWith(valueToRemove) : (str.equals(valueToRemove) || ListUtil.first(str, ";").trim().equals(valueToRemove))) {
					removed = true;
					continue;
				}

				if (sb.length() > 0) sb.append(",\n ");
				sb.append(str);
			}
		}
		catch (Throwable e) {
			ExceptionUtil.rethrowIfNecessary(e);
		}
		if (removed) {
			if (sb.length() > 0) attrs.putValue(key, sb.toString());
			else attrs.remove(key);
		}

	}

	public static void removeOptional(Attributes attrs, String key) {
		String val = attrs.getValue(key);
		if (StringUtil.isEmpty(val)) return;
		StringBuilder sb = new StringBuilder();
		boolean removed = false;

		try {

			Iterator<String> it = toList(val).iterator();// ListUtil.toStringArray(ListUtil.listToArrayTrim(val, ','));
			String str;
			while (it.hasNext()) {
				str = it.next();
				str = str.trim();
				// print.e("=="+str);

				if (str.indexOf("resolution:=optional") != -1) {
					removed = true;
					continue;
				}

				if (sb.length() > 0) sb.append(",\n ");
				sb.append(str);
			}
		}
		catch (Throwable e) {
			ExceptionUtil.rethrowIfNecessary(e);
		}
		if (removed) attrs.putValue(key, sb.toString());

	}

	private static List<String> toList(String val) {
		List<String> list = new ArrayList<String>();
		int len = val.length();
		int inside = 0;
		char c;
		int begin = 0;
		for (int i = 0; i < len; i++) {
			c = val.charAt(i);
			if (c == '"') {
				if (inside == '"') inside = 0;
				else if (inside == 0) inside = '"';
			}
			else if (c == '\'') {
				if (inside == '\'') inside = 0;
				else if (inside == 0) inside = '\'';
			}
			else if (c == ',' && inside == 0) {
				if (begin < i) list.add(val.substring(begin, i));
				begin = i + 1;
			}
		}
		if (begin < len) list.add(val.substring(begin));

		return list;
	}
}