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
package lucee.runtime.type.scope;

import java.io.UnsupportedEncodingException;

import lucee.commons.lang.StringList;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.URLDecoder;
import lucee.commons.net.URLItem;
import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.op.Caster;
import lucee.runtime.security.ScriptProtect;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.CastableStruct;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.type.util.StructUtil;

/**
 * Simple implementation of a Scope, for general use.
 */
public abstract class ScopeSupport extends StructImpl implements Scope {

	private static final long serialVersionUID = -4185219623238374574L;

	private String name;
	private String dspName;
	private static int _id = 0;
	private int id = 0;
	private static final byte[] EMPTY = "".getBytes();

	/**
	 * Field <code>isInit</code>
	 */
	protected boolean isInit;
	private int type;

	/**
	 * constructor for the Simple class
	 * 
	 * @param name name of the scope
	 * @param type scope type (SCOPE_APPLICATION,SCOPE_COOKIE use)
	 */
	private ScopeSupport(String name, int type) {
		this(name, type, Struct.TYPE_LINKED);
	}

	/**
	 * constructor for ScopeSupport
	 * 
	 * @param name name of the scope
	 * @param type scope type (SCOPE_APPLICATION,SCOPE_COOKIE use)
	 * @param mapType mean that the struct has predictable iteration order this make the input order fix
	 */
	public ScopeSupport(String name, int type, int mapType) {
		super(mapType);
		this.name = name;
		this.type = type;

		id = ++_id;
	}

	@Override

	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return toDumpData(pageContext, maxlevel, dp, this, dspName);
	}

	public static DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp, Struct sct, String dspName) {
		if (StringUtil.isEmpty(dspName)) dspName = "Scope";

		return StructUtil.toDumpTable(sct, dspName, pageContext, maxlevel, dp);

	}

	protected ExpressionException invalidKey(String key) {
		return new ExpressionException("variable [" + key + "] doesn't exist in " + StringUtil.ucFirst(name) + " Scope (keys:" + ListUtil.arrayToList(keys(), ",") + ")");
	}

	/**
	 * write parameter defined in a query string (name1=value1&name2=value2) to the scope
	 * 
	 * @param str Query String
	 * @return parsed name value pair
	 */
	protected static URLItem[] setFromQueryString(String str) {
		return setFrom___(str, '&');
	}

	protected static URLItem[] setFromTextPlain(String str) {
		return setFrom___(str, '\n');
	}

	protected static URLItem[] setFrom___(String tp, char delimiter) {
		if (tp == null) return new URLItem[0];
		Array arr = ListUtil.listToArrayRemoveEmpty(tp, delimiter);
		URLItem[] pairs = new URLItem[arr.size()];

		// Array item;
		int index;
		String name;

		for (int i = 1; i <= pairs.length; i++) {
			name = Caster.toString(arr.get(i, ""), "");
			// if(name.length()==0) continue;

			index = name.indexOf('=');
			if (index != -1) pairs[i - 1] = new URLItem(name.substring(0, index), name.substring(index + 1), true);
			else pairs[i - 1] = new URLItem(name, "", true);

		}
		return pairs;
	}

	protected static byte[] getBytes(String str) {
		return str.getBytes();
	}

	protected static byte[] getBytes(String str, String encoding) {
		try {
			return str.getBytes(encoding);
		}
		catch (UnsupportedEncodingException e) {
			return EMPTY;
		}
	}

	protected void fillDecodedEL(URLItem[] raw, String encoding, boolean scriptProteced, boolean sameAsArray) {
		try {
			fillDecoded(raw, encoding, scriptProteced, sameAsArray);
		}
		catch (UnsupportedEncodingException e) {
			try {
				fillDecoded(raw, "iso-8859-1", scriptProteced, sameAsArray);
			}
			catch (UnsupportedEncodingException e1) {
			}
		}
	}

	/**
	 * fill th data from given strut and decode it
	 * 
	 * @param raw
	 * @param encoding
	 * @throws UnsupportedEncodingException
	 */
	protected void fillDecoded(URLItem[] raw, String encoding, boolean scriptProteced, boolean sameAsArray) throws UnsupportedEncodingException {
		clear();
		String name, value;
		// Object curr;
		for (int i = 0; i < raw.length; i++) {
			name = raw[i].getName();
			value = raw[i].getValue();
			if (raw[i].isUrlEncoded()) {
				name = URLDecoder.decode(name, encoding, true);
				value = URLDecoder.decode(value, encoding, true);
			}
			// MUST valueStruct
			if (name.indexOf('.') != -1) {

				StringList list = ListUtil.listToStringListRemoveEmpty(name, '.');
				if (list.size() > 0) {
					Struct parent = this;
					while (list.hasNextNext()) {
						parent = _fill(parent, list.next(), new CastableStruct(Struct.TYPE_LINKED), false, scriptProteced, sameAsArray);
					}
					_fill(parent, list.next(), value, true, scriptProteced, sameAsArray);
				}
			}
			// else
			_fill(this, name, value, true, scriptProteced, sameAsArray);
		}
	}

	private Struct _fill(final Struct parent, String name, Object value, boolean isLast, boolean scriptProteced, boolean sameAsArray) {
		Object curr;
		boolean isArrayDef = false;
		Collection.Key key = KeyImpl.init(name);

		// script protect
		if (scriptProteced && value instanceof String) {
			value = ScriptProtect.translate((String) value);
		}

		if (name.length() > 2 && name.endsWith("[]")) {
			isArrayDef = true;
			name = name.substring(0, name.length() - 2);
			key = KeyImpl.getInstance(name);
			curr = parent.get(key, null);
		}
		else {
			curr = parent.get(key, null);
		}

		if (curr == null) {
			if (isArrayDef) {
				Array arr = new ArrayImpl();
				arr.appendEL(value);
				parent.setEL(key, arr);
			}
			else parent.setEL(key, value);
		}
		else if (curr instanceof Array) {
			Array arr = ((Array) curr);
			arr.appendEL(value);
		}
		else if (curr instanceof CastableStruct) {
			if (isLast) ((CastableStruct) curr).setValue(value);
			else return (Struct) curr;

		}
		else if (curr instanceof Struct) {
			if (isLast) parent.setEL(key, value);
			else return (Struct) curr;
		}
		else if (curr instanceof String) {
			if (isArrayDef) {
				Array arr = new ArrayImpl();
				arr.appendEL(curr);
				arr.appendEL(value);
				parent.setEL(key, arr);
			}
			else if (value instanceof Struct) {
				parent.setEL(key, value);
			}
			else {
				if (sameAsArray) {
					Array arr = new ArrayImpl();
					arr.appendEL(curr);
					arr.appendEL(value);
					parent.setEL(key, arr);
				}
				else {
					String c = Caster.toString(curr, "");
					String v = Caster.toString(value, "");
					if (StringUtil.isEmpty(c)) {
						parent.setEL(key, v);
					}
					else if (!StringUtil.isEmpty(v)) {
						parent.setEL(key, c + ',' + v);
					}
				}
			}
		}
		if (!isLast) {
			return (Struct) value;
		}
		return null;
	}

	/*
	 * private String decode(Object value,String encoding) throws UnsupportedEncodingException { return
	 * URLDecoder.decode(new
	 * String(Caster.toString(value,"").getBytes("ISO-8859-1"),encoding),encoding); }
	 */

	@Override
	public boolean isInitalized() {
		return isInit;
	}

	@Override
	public void initialize(PageContext pc) {
		isInit = true;
	}

	@Override
	public void release(PageContext pc) {
		clear();
		isInit = false;
	}

	/**
	 * @return Returns the id.
	 */
	public int _getId() {
		return id;
	}

	/**
	 * display name for dump
	 * 
	 * @param dspName
	 */
	protected void setDisplayName(String dspName) {
		this.dspName = dspName;
	}

	@Override
	public int getType() {
		return type;
	}

	@Override
	public String getTypeAsString() {
		return name;
	}

}