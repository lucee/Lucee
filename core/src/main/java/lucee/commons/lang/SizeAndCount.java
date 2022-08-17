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
package lucee.commons.lang;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lucee.commons.io.IOUtil;
import lucee.commons.io.log.LogUtil;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Query;
import lucee.runtime.util.Creation;

public class SizeAndCount {

	private static final int OBJECT_GRANULARITY_IN_BYTES = 8;
	private static final int WORD_SIZE = Arch.getVMArchitecture().getWordSize();
	private static final int HEADER_SIZE = 2 * WORD_SIZE;

	private static final int DOUBLE_SIZE = 8;
	private static final int FLOAT_SIZE = 4;
	private static final int LONG_SIZE = 8;
	private static final int INT_SIZE = 4;
	private static final int SHORT_SIZE = 2;
	private static final int BYTE_SIZE = 1;
	private static final int BOOLEAN_SIZE = 1;
	private static final int CHAR_SIZE = 2;
	private static final int REF_SIZE = WORD_SIZE;

	public static Size sizeOf(Object obj) throws PageException {
		Creation creator = CFMLEngineFactory.getInstance().getCreationUtil();
		Size size = new Size(0, 0);
		sizeOf(creator, size, obj, new HashSet<Object>());
		return size;
	}

	private static void sizeOf(Creation creator, Size size, Object obj, Set<Object> parents) throws PageException {
		if (obj == null) return;
		Object raw = obj;

		// TODO this is just a patch solution, find a better way to handle this kind of situation (Wrapper
		// classes)
		if (isInstaneOf(obj.getClass(), "lucee.runtime.text.xml.struct.XMLStruct")) {
			try {
				Method toNode = raw.getClass().getMethod("toNode", new Class[0]);
				raw = toNode.invoke(obj, new Object[0]);
			}
			catch (Exception e) {
				LogUtil.log("lang", e);
			}
		}

		if (parents.contains(raw)) return;
		parents.add(raw);
		try {
			if (obj instanceof Collection) {
				if (obj instanceof Query) sizeOf(creator, size, (Query) obj, parents);
				else sizeOf(creator, size, ((Collection) obj).valueIterator(), parents);
				return;
			}
			// Map
			else if (obj instanceof Map) {
				sizeOf(creator, size, ((Map) obj).values().iterator(), parents);
				return;
			}
			// List
			else if (obj instanceof List) {
				sizeOf(creator, size, ((List) obj).iterator(), parents);
				return;
			}

			// String
			else if (obj instanceof String) {
				size.size += (CHAR_SIZE * ((String) obj).length()) + REF_SIZE;
			}
			// Number
			else if (obj instanceof Number) {
				if (obj instanceof Double) size.size += DOUBLE_SIZE + REF_SIZE;
				else if (obj instanceof Float) size.size += FLOAT_SIZE + REF_SIZE;
				else if (obj instanceof Long) size.size += LONG_SIZE + REF_SIZE;
				else if (obj instanceof Integer) size.size += INT_SIZE + REF_SIZE;
				else if (obj instanceof Short) size.size += SHORT_SIZE + REF_SIZE;
				else if (obj instanceof Byte) size.size += BYTE_SIZE + REF_SIZE;
			}

			else if (obj instanceof Boolean) size.size += REF_SIZE + BOOLEAN_SIZE;
			else if (obj instanceof Character) size.size += REF_SIZE + CHAR_SIZE;

			else size.size += _sizeOf(obj);

			size.count++;
		}
		finally {
			// parents.remove(raw);// TODO should we not remove, to see if sister is me.
		}
	}

	private static void sizeOf(Creation creator, Size size, Iterator it, Set<Object> parents) throws PageException {
		size.count++;
		size.size += REF_SIZE;
		while (it.hasNext()) {
			sizeOf(creator, size, it.next(), parents);
		}
	}

	private static void sizeOf(Creation creator, Size size, Query qry, Set<Object> parents) throws PageException {
		size.count++;
		size.size += REF_SIZE;
		int rows = qry.getRecordcount();
		String[] strColumns = qry.getColumns();
		Collection.Key[] columns = new Collection.Key[strColumns.length];
		for (int col = 0; col < columns.length; col++) {
			columns[col] = creator.createKey(strColumns[col]);
		}

		for (int row = 1; row <= rows; row++) {
			for (int col = 0; col < columns.length; col++) {
				sizeOf(creator, size, qry.getAt(columns[col], row), parents);
			}
		}
	}

	public static boolean isInstaneOf(Class src, String className) {
		if (src == null) return false;
		if (className.equals(src.getName())) return true;

		// interfaces
		Class[] interfaces = src.getInterfaces();
		for (int i = 0; i < interfaces.length; i++) {
			if (isInstaneOf(interfaces[i], className)) return true;
		}
		return isInstaneOf(src.getSuperclass(), className);
	}

	public static int _sizeOf(Object o) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(os);
			oos.writeObject(o);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		finally {
			IOUtil.closeEL(oos);
		}
		return os.toByteArray().length;
	}

	public static class Size {

		public int count;
		public int size;

		public Size(int count, int size) {
			this.count = count;
			this.size = size;
		}

	}

}

class Arch {

	private static final Arch ARCH_32_BITS = new Arch(32, 4);
	private static final Arch ARCH_64_BITS = new Arch(64, 8);
	private static final Arch ARCH_UNKNOWN = new Arch(32, 4);

	private int bits;
	private int wordSize;

	private Arch(int bits, int wordSize) {
		this.bits = bits;
		this.wordSize = wordSize;
	}

	public int getBits() {
		return bits;
	}

	public int getWordSize() {
		return wordSize;
	}

	public static Arch getVMArchitecture() {
		String archString = System.getProperty("sun.arch.data.model");
		if (archString != null) {
			if (archString.equals("32")) {
				return ARCH_32_BITS;
			}
			else if (archString.equals("64")) {
				return ARCH_64_BITS;
			}
		}
		return ARCH_UNKNOWN;
	}

}