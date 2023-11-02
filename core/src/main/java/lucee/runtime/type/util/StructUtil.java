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
package lucee.runtime.type.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.map.ReferenceMap;

import lucee.commons.digest.HashUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.comparator.TextComparator;

/**
 * 
 */
public final class StructUtil {

	/**
	 * copy data from source struct to target struct
	 * 
	 * @param source
	 * @param target
	 * @param overwrite overwrite data if exist in target
	 */
	public static void copy(Struct source, Struct target, boolean overwrite) {
		Iterator<Entry<Key, Object>> it = source.entryIterator();
		Entry<Key, Object> e;
		while (it.hasNext()) {
			e = it.next();
			if (overwrite || !target.containsKey(e.getKey())) target.setEL(e.getKey(), e.getValue());
		}
	}

	public static lucee.runtime.type.Collection.Key[] toCollectionKeys(String[] skeys) {
		lucee.runtime.type.Collection.Key[] keys = new lucee.runtime.type.Collection.Key[skeys.length];
		for (int i = 0; i < keys.length; i++) {
			keys[i] = KeyImpl.init(skeys[i]);
		}
		return keys;
	}

	/**
	 * @param sct
	 * @return
	 */
	public static Struct duplicate(Struct sct, boolean deepCopy) {

		Struct rtn = new StructImpl();
		// lucee.runtime.type.Collection.Key[] keys=sct.keys();
		// lucee.runtime.type.Collection.Key key;
		Iterator<Entry<Key, Object>> it = sct.entryIterator();
		Entry<Key, Object> e;
		while (it.hasNext()) {
			e = it.next();
			rtn.setEL(e.getKey(), Duplicator.duplicate(e.getValue(), deepCopy));
		}
		return rtn;
	}

	public static void putAll(Struct struct, Map map) {
		Iterator<Entry> it = map.entrySet().iterator();
		Map.Entry entry;
		while (it.hasNext()) {
			entry = it.next();
			struct.setEL(KeyImpl.toKey(entry.getKey(), null), entry.getValue());
		}
	}

	public static Set<Entry<String, Object>> entrySet(Struct sct) {
		boolean linked = sct instanceof StructImpl && ((StructImpl) sct).getType() == Struct.TYPE_LINKED;
		Iterator<Entry<Key, Object>> it = sct.entryIterator();
		Entry<Key, Object> e;
		Set<Entry<String, Object>> set = linked ? new LinkedHashSet<Entry<String, Object>>() : new HashSet<Entry<String, Object>>();
		while (it.hasNext()) {
			e = it.next();
			set.add(new StructMapEntry(sct, e.getKey(), e.getValue()));
		}
		return set;
	}

	public static Set<String> keySet(Struct sct) {
		boolean linked = sct instanceof StructSupport && ((StructSupport) sct).getType() == Struct.TYPE_LINKED;

		Iterator<Key> it = sct.keyIterator();
		Set<String> set = linked ? new LinkedHashSet<String>() : new HashSet<String>();
		while (it.hasNext()) {
			set.add(it.next().getString());
		}
		return set;
	}

	public static DumpTable toDumpTable(Struct sct, String title, PageContext pageContext, int maxlevel, DumpProperties dp) {
		Key[] keys = CollectionUtil.keys(sct);
		if (!(sct instanceof StructSupport) || ((StructSupport) sct).getType() != Struct.TYPE_LINKED) keys = order(sct, CollectionUtil.keys(sct));
		DumpTable table = new DumpTable("struct", "#9999ff", "#ccccff", "#000000");// "#9999ff","#ccccff","#000000"

		int maxkeys = dp.getMaxKeys();
		if (maxkeys < sct.size()) {
			table.setComment("Entries: " + sct.size() + " (showing top " + maxkeys + ")");
		}
		else if (sct.size() > 10 && dp.getMetainfo()) {
			table.setComment("Entries: " + sct.size());
		}

		// advanced
		/*
		 * Map<Key, FunctionLibFunction> members = MemberUtil.getMembers(pageContext, CFTypes.TYPE_STRUCT);
		 * if(members!=null) { StringBuilder sb=new
		 * StringBuilder("This Struct is supporting the following Object functions:"); Iterator<Entry<Key,
		 * FunctionLibFunction>> it = members.entrySet().iterator(); Entry<Key, FunctionLibFunction> e;
		 * while(it.hasNext()){ e = it.next(); sb.append("\n	.") .append(e.getKey()) .append('(');
		 * 
		 * 
		 * ArrayList<FunctionLibFunctionArg> args = e.getValue().getArg(); int optionals = 0; for(int
		 * i=1;i<args.size();i++) { FunctionLibFunctionArg arg=args.get(i); if(i!=0)sb.append(", ");
		 * if(!arg.getRequired()) { sb.append("["); optionals++; } sb.append(arg.getName()); sb.append(":");
		 * sb.append(arg.getTypeAsString()); } for(int i=0;i<optionals;i++) sb.append("]");
		 * sb.append("):"+e.getValue().getReturnTypeAsString());
		 * 
		 * 
		 * } table.setComment(sb.toString()); }
		 */

		if (!StringUtil.isEmpty(title)) table.setTitle(title);
		maxlevel--;
		int index = 0;
		for (int i = 0; i < keys.length; i++) {
			if (DumpUtil.keyValid(dp, maxlevel, keys[i])) {
				if (maxkeys <= index++) break;
				table.appendRow(1, new SimpleDumpData(keys[i].toString()), DumpUtil.toDumpData(sct.get(keys[i], null), pageContext, maxlevel, dp));
			}
		}
		return table;
	}

	private static Key[] order(Struct sct, Key[] keys) {
		if (sct instanceof StructImpl && ((StructImpl) sct).getType() == Struct.TYPE_LINKED) return keys;

		TextComparator comp = new TextComparator(true, true);
		Arrays.sort(keys, comp);
		return keys;
	}

	/**
	 * create a value return value out of a struct
	 * 
	 * @param sct
	 * @return
	 */
	public static java.util.Collection<?> values(Struct sct) {
		ArrayList<Object> arr = new ArrayList<Object>();
		// Key[] keys = sct.keys();
		Iterator<Object> it = sct.valueIterator();
		while (it.hasNext()) {
			arr.add(it.next());
		}
		return arr;
	}

	public static Struct copyToStruct(Map map) throws PageException {
		Struct sct = new StructImpl();
		Iterator it = map.entrySet().iterator();
		Map.Entry entry;
		while (it.hasNext()) {
			entry = (Entry) it.next();
			sct.setEL(Caster.toString(entry.getKey()), entry.getValue());
		}
		return sct;
	}

	public static void setELIgnoreWhenNull(Struct sct, String key, Object value) {
		setELIgnoreWhenNull(sct, KeyImpl.init(key), value);
	}

	public static void setELIgnoreWhenNull(Struct sct, Collection.Key key, Object value) {
		if (value != null) sct.setEL(key, value);
	}

	/**
	 * remove every entry hat has this value
	 * 
	 * @param map
	 * @param obj
	 */
	public static void removeValue(Map map, Object value) {
		Iterator it = map.entrySet().iterator();
		Map.Entry entry;
		while (it.hasNext()) {
			entry = (Entry) it.next();
			if (entry.getValue() == value) it.remove();
		}
	}

	public static Struct merge(Struct[] scts) {
		Struct sct = new StructImpl();

		for (int i = scts.length - 1; i >= 0; i--) {
			Iterator<Entry<Key, Object>> it = scts[i].entryIterator();
			Entry<Key, Object> e;
			while (it.hasNext()) {
				e = it.next();
				sct.setEL(e.getKey(), e.getValue());
			}
		}
		return sct;
	}

	public static int getType(Map m) {
		if (m instanceof LinkedHashMap) return Struct.TYPE_LINKED;
		if (m instanceof WeakHashMap) return Struct.TYPE_WEAKED;
		if (m instanceof ConcurrentHashMap) return Struct.TYPE_SYNC;
		if (m instanceof ReferenceMap) return Struct.TYPE_SOFT;

		return Struct.TYPE_REGULAR;
	}

	public static String toType(int type, String defaultValue) {
		if (Struct.TYPE_LINKED == type) return "ordered";
		if (Struct.TYPE_WEAKED == type) return "weak";
		if (Struct.TYPE_REGULAR == type) return "regular";
		if (Struct.TYPE_REGULAR == type) return "regular";
		if (Struct.TYPE_SOFT == type) return "soft";
		if (Struct.TYPE_SYNC == type) return "synchronized";
		if (Struct.TYPE_UNDEFINED == type) return "undefined";

		return defaultValue;
	}

	/**
	 * creates a hash based on the keys of the Map/Struct
	 * 
	 * @param map
	 * @return
	 */
	public static String keyHash(Struct sct) {
		Key[] keys;
		Arrays.sort(keys = CollectionUtil.keys(sct));

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < keys.length; i++) {
			sb.append(keys[i].getString()).append(';');
		}
		return Long.toString(HashUtil.create64BitHash(sb), Character.MAX_RADIX);
	}
}