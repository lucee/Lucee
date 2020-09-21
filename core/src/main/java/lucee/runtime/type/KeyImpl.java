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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

import lucee.commons.digest.WangJenkins;
import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Castable;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Operator;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.dt.DateTime;

public class KeyImpl implements Collection.Key, Castable, Comparable, Externalizable, WangJenkins, CharSequence {

	private static final long serialVersionUID = -8864844181140115609L; // do not change

	private static final long[] byteTable = createLookupTable();
	private static final long HSTART = 0xBB40E64DA205B064L;
	private static final long HMULT = 7664345821815920749L;

	// private boolean intern;
	private String key;
	private transient String lcKey;
	private transient String ucKey;
	private transient int wjh;
	private transient int sfm = -1;
	private transient long h64;

	public KeyImpl() {
		// DO NOT USE, JUST FOR UNSERIALIZE
	}

	private static final long[] createLookupTable() {
		long[] _byteTable = new long[256];
		long h = 0x544B2FBACAAF1684L;
		for (int i = 0; i < 256; i++) {
			for (int j = 0; j < 31; j++) {
				h = (h >>> 7) ^ h;
				h = (h << 11) ^ h;
				h = (h >>> 10) ^ h;
			}
			_byteTable[i] = h;
		}
		return _byteTable;
	}

	public static final long createHash64(CharSequence cs) {
		long h = HSTART;
		final long hmult = HMULT;
		final long[] ht = byteTable;
		final int len = cs.length();
		for (int i = 0; i < len; i++) {
			char ch = cs.charAt(i);
			h = (h * hmult) ^ ht[ch & 0xff];
			h = (h * hmult) ^ ht[(ch >>> 8) & 0xff];
		}
		return h;
	}

	@Override
	public int wangJenkinsHash() {
		if (wjh == 0) {
			int h = hashCode();
			h += (h << 15) ^ 0xffffcd7d;
			h ^= (h >>> 10);
			h += (h << 3);
			h ^= (h >>> 6);
			h += (h << 2) + (h << 14);
			wjh = h ^ (h >>> 16);
		}
		return wjh;
	}

	public int slotForMap() {
		if (sfm == -1) {
			int h = 0;
			h ^= hashCode();
			h ^= (h >>> 20) ^ (h >>> 12);
			sfm = h ^ (h >>> 7) ^ (h >>> 4);
		}
		return sfm;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(key);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		key = (String) in.readObject();
		ucKey = key.toUpperCase();
		h64 = createHash64(ucKey);
	}

	public KeyImpl(String key) {
		this.key = key;
		this.ucKey = key.toUpperCase();
		h64 = createHash64(ucKey);
	}

	/**
	 * for dynamic loading of key objects
	 * 
	 * @param string
	 * @return
	 */
	public static Collection.Key init(String key) {
		return new KeyImpl(key);
	}

	public static Collection.Key _const(String key) {
		return new KeyImpl(key);
	}

	public static Collection.Key getInstance(String key) {
		return new KeyImpl(key);
	}

	public static Collection.Key intern(String key) {
		return new KeyImpl(key);
	}

	@Override
	public char charAt(int index) {
		return key.charAt(index);
	}

	@Override
	public char lowerCharAt(int index) {
		return getLowerString().charAt(index);
	}

	@Override
	public char upperCharAt(int index) {
		return ucKey.charAt(index);
	}

	@Override
	public String getLowerString() {
		if (lcKey == null) lcKey = StringUtil.toLowerCase(key);
		return lcKey;
	}

	@Override
	public String getUpperString() {
		return ucKey;
	}

	@Override
	public String toString() {
		return key;
	}

	@Override
	public String getString() {
		return key;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (other instanceof KeyImpl) {
			return hash() == ((KeyImpl) other).hash();
		}
		if (other instanceof String) {
			return key.equalsIgnoreCase((String) other);
		}
		if (other instanceof Key) {
			// Both strings are guaranteed to be upper case
			return ucKey.equals(((Key) other).getUpperString());
		}
		return false;
	}

	@Override
	public boolean equalsIgnoreCase(Key other) {
		if (this == other) return true;
		if (other instanceof KeyImpl) {
			return h64 == ((KeyImpl) other).h64;// return lcKey.equals((((KeyImpl)other).lcKey));
		}
		return ucKey.equalsIgnoreCase(other.getLowerString());
	}

	@Override
	public int hashCode() {
		return ucKey.hashCode();
	}

	@Override
	public long hash() {
		return h64;
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return Caster.toBooleanValue(key);
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return Caster.toBoolean(key, defaultValue);
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return Caster.toDatetime(key, null);
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return DateCaster.toDateAdvanced(key, DateCaster.CONVERTING_TYPE_OFFSET, null, defaultValue);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return Caster.toDoubleValue(key);
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return Caster.toDoubleValue(key, defaultValue);
	}

	@Override
	public String castToString() throws PageException {
		return key;
	}

	@Override
	public String castToString(String defaultValue) {
		return key;
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		return Operator.compare(key, b);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return Operator.compare(key, (Date) dt);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return Operator.compare(key, d);
	}

	@Override
	public int compareTo(String str) throws PageException {
		return Operator.compare(key, str);
	}

	@Override
	public int compareTo(Object o) {
		try {
			return Operator.compare(key, o);
		}
		catch (PageException e) {
			ClassCastException cce = new ClassCastException(e.getMessage());
			cce.setStackTrace(e.getStackTrace());
			throw cce;

		}
	}

	public static Array toUpperCaseArray(Key[] keys) {
		ArrayImpl arr = new ArrayImpl();
		for (int i = 0; i < keys.length; i++) {
			arr.appendEL(((KeyImpl) keys[i]).getUpperString());
		}
		return arr;
	}

	public static Array toLowerCaseArray(Key[] keys) {
		ArrayImpl arr = new ArrayImpl();
		for (int i = 0; i < keys.length; i++) {
			arr.appendEL(((KeyImpl) keys[i]).getLowerString());
		}
		return arr;
	}

	public static Array toArray(Key[] keys) {
		ArrayImpl arr = new ArrayImpl();
		for (int i = 0; i < keys.length; i++) {
			arr.appendEL(((KeyImpl) keys[i]).getString());
		}
		return arr;
	}

	public static String toUpperCaseList(Key[] array, String delimiter) {
		if (array.length == 0) return "";
		StringBuffer sb = new StringBuffer(((KeyImpl) array[0]).getUpperString());

		if (delimiter.length() == 1) {
			char c = delimiter.charAt(0);
			for (int i = 1; i < array.length; i++) {
				sb.append(c);
				sb.append(((KeyImpl) array[i]).getUpperString());
			}
		}
		else {
			for (int i = 1; i < array.length; i++) {
				sb.append(delimiter);
				sb.append(((KeyImpl) array[i]).getUpperString());
			}
		}
		return sb.toString();
	}

	public static String toList(Key[] array, String delimiter) {
		if (array.length == 0) return "";
		StringBuilder sb = new StringBuilder(((KeyImpl) array[0]).getString());

		if (delimiter.length() == 1) {
			char c = delimiter.charAt(0);
			for (int i = 1; i < array.length; i++) {
				sb.append(c);
				sb.append((array[i]).getString());
			}
		}
		else {
			for (int i = 1; i < array.length; i++) {
				sb.append(delimiter);
				sb.append((array[i]).getString());
			}
		}
		return sb.toString();
	}

	public static String toLowerCaseList(Key[] array, String delimiter) {
		if (array.length == 0) return "";
		StringBuffer sb = new StringBuffer(((KeyImpl) array[0]).getLowerString());

		if (delimiter.length() == 1) {
			char c = delimiter.charAt(0);
			for (int i = 1; i < array.length; i++) {
				sb.append(c);
				sb.append(((KeyImpl) array[i]).getLowerString());
			}
		}
		else {
			for (int i = 1; i < array.length; i++) {
				sb.append(delimiter);
				sb.append(((KeyImpl) array[i]).getLowerString());
			}
		}
		return sb.toString();
	}

	public static Collection.Key toKey(Object obj, Collection.Key defaultValue) {
		if (obj instanceof Collection.Key) return (Collection.Key) obj;
		String str = Caster.toString(obj, null);
		if (str == null) return defaultValue;
		return init(str);
	}

	public static Collection.Key toKey(Object obj) throws CasterException {
		if (obj instanceof Collection.Key) return (Collection.Key) obj;
		String str = Caster.toString(obj, null);
		if (str == null) throw new CasterException(obj, Collection.Key.class);
		return init(str);
	}

	public static Collection.Key toKey(int i) {
		return init(Caster.toString(i));
	}

	@Override
	public int length() {
		return key.length();
	}

	public static Key[] toKeyArray(String[] arr) {
		if (arr == null) return null;

		Key[] keys = new Key[arr.length];
		for (int i = 0; i < keys.length; i++) {
			keys[i] = init(arr[i]);
		}
		return keys;
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return getString().subSequence(start, end);
	}
}