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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.SerializableObject;
import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.DeprecatedException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Duplicator;
import lucee.runtime.op.Operator;
import lucee.runtime.op.ThreadLocalDuplication;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.reflection.pairs.MethodInstance;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.it.EntryIterator;
import lucee.runtime.type.it.KeyIterator;
import lucee.runtime.type.it.StringIterator;
import lucee.runtime.type.scope.Undefined;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.QueryUtil;
import lucee.runtime.util.ArrayIterator;

/**
 * implementation of the query column
 */
public class QueryColumnImpl implements QueryColumnPro, Objects {

	private static final long serialVersionUID = -5544446523204021493L;
	private static final int CAPACITY = 32;

	protected int type;
	protected int size;
	protected Object[] data;

	protected boolean typeChecked = false;
	protected QueryImpl query;
	protected Collection.Key key;
	private final Object sync = new SerializableObject();

	/**
	 * constructor with type
	 * 
	 * @param query
	 * @param key
	 * @param type
	 */
	public QueryColumnImpl(QueryImpl query, Collection.Key key, int type) {
		this.data = new Object[CAPACITY];
		this.type = type;
		this.key = key;
		this.query = query;
	}

	/**
	 * constructor with array
	 * 
	 * @param query
	 * @param array
	 * @param type
	 */
	public QueryColumnImpl(QueryImpl query, Collection.Key key, Array array, int type) {
		data = array.toArray();
		size = array.size();
		this.type = type;
		this.query = query;
		this.key = key;
	}

	/**
	 * @param query
	 * @param type type as (java.sql.Types.XYZ) int
	 * @param size
	 */
	public QueryColumnImpl(QueryImpl query, Collection.Key key, int type, int size) {
		this.data = new Object[size];
		this.type = type;
		this.size = size;
		this.query = query;
		this.key = key;
	}

	/**
	 * Constructor of the class for internal usage only
	 */
	public QueryColumnImpl() {
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public Collection.Key[] keys() {
		Collection.Key[] k = new Collection.Key[size()];
		int len = k.length;
		for (int i = 1; i <= len; i++) {
			k[i - 1] = KeyImpl.init(Caster.toString(i));
		}
		return k;
	}

	@Override
	public Object remove(Collection.Key key) throws PageException {
		resetTypeSync();
		return set(Caster.toIntValue(key.getString()), "");
	}

	@Override
	public Object remove(Collection.Key key, Object defaultValue) {
		resetTypeSync();
		try {
			return set(Caster.toIntValue(key.getString()), "");
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	@Override
	public Object remove(int row) throws DatabaseException {
		query.disableIndex();
		// query.disconnectCache();
		resetTypeSync();
		return set(row, "");
	}

	@Override
	public Object removeEL(Collection.Key key) {
		query.disableIndex();
		// query.disconnectCache();
		resetTypeSync();
		return setEL(Caster.toIntValue(key.getString(), -1), "");
	}

	@Override
	public Object removeEL(int row) {
		query.disableIndex();
		// query.disconnectCache();
		resetTypeSync();
		return setEL(row, "");
	}

	@Override
	public void clear() {
		query.disableIndex();
		synchronized (sync) {
			resetType();
			data = new Object[CAPACITY];
			size = 0;
		}
	}

	@Override
	public Object remove(PageContext pc) throws PageException {
		return remove(query.getCurrentrow(pc.getId()));
	}

	@Override
	public Object removeEL(PageContext pc) {
		return removeEL(query.getCurrentrow(pc.getId()));
	}

	@Override
	public Object get(String key) throws PageException {
		return get(KeyImpl.init(key));
	}

	@Override
	public Object get(Key key) throws PageException {
		return get((PageContext) null, key);
	}

	@Override
	public Object get(PageContext pc, Collection.Key key) throws PageException {
		int row = Caster.toIntValue(key.getString(), Integer.MIN_VALUE);
		if (row == Integer.MIN_VALUE) {
			Object _null = NullSupportHelper.NULL(pc);
			Object child = getChildElement(pc, key, _null);
			if (child != _null) return child;
			throw new DatabaseException("key [" + key + "] not found", null, null, null);
		}
		return QueryUtil.getValue(pc, this, row);
	}

	private Object getChildElement(PageContext pc, Key key, Object defaultValue) {// pc maybe null

		Collection coll = Caster.toCollection(QueryUtil.getValue(this, query.getCurrentrow(pc.getId())), null);
		if (coll != null) {
			Object res = coll.get(key, CollectionUtil.NULL);
			if (res != CollectionUtil.NULL) return res;
		}

		// column and query has same name
		if (key.equals(this.key)) {
			return query.get(key, defaultValue);
		}

		// get it from undefined scope
		pc = ThreadLocalPageContext.get(pc);
		if (pc != null) {
			Object _null = NullSupportHelper.NULL(pc);
			Undefined undefined = pc.undefinedScope();
			boolean old = undefined.setAllowImplicidQueryCall(false);
			Object sister = undefined.get(this.key, _null);
			undefined.setAllowImplicidQueryCall(old);
			if (sister != _null) {
				try {
					return pc.get(sister, key);
				}
				catch (PageException e) {
					return defaultValue;
				}
			}
		}
		return defaultValue;
	}

	/**
	 * touch the given line on the column at given row
	 * 
	 * @param row
	 * @return new row or existing
	 * @throws DatabaseException
	 */
	public Object touch(int row) {
		if (row < 1 || row > size) return NullSupportHelper.full() ? null : "";
		Object o = data[row - 1];
		if (o != null) return o;
		return setEL(row, new StructImpl());
	}

	/**
	 * touch the given line on the column at given row
	 * 
	 * @param row
	 * @return new row or existing
	 * @throws DatabaseException
	 */
	public Object touchEL(int row) {
		return touch(row);
	}

	@Override
	public Object get(Key key, Object defaultValue) {
		return get(null, key, defaultValue);
	}

	@Override
	public Object get(PageContext pc, Collection.Key key, Object defaultValue) {// pc maybe null
		int row = Caster.toIntValue(key.getString(), Integer.MIN_VALUE);
		if (row == Integer.MIN_VALUE) {
			return getChildElement(pc, key, defaultValue);
		}
		return get(row, defaultValue);
	}

	@Override
	public Object get(String key, Object defaultValue) {
		return get(KeyImpl.init(key), defaultValue);
	}

	@Override
	public Object get(int row) throws DeprecatedException {
		throw new DeprecatedException("this method is no longer supported, use instead get(int,Object)");
		// return QueryUtil.getValue(this,row);
	}

	@Override
	public Object get(int row, Object emptyValue) {
		if (row < 1 || row > size) return emptyValue;
		return data[row - 1] == null ? emptyValue : data[row - 1];
	}

	@Override
	public Object set(String key, Object value) throws PageException {
		int row = Caster.toIntValue(key, Integer.MIN_VALUE);
		if (row == Integer.MIN_VALUE) return query.set(key, value);
		return set(row, value);
	}

	@Override
	public Object set(Collection.Key key, Object value) throws PageException {
		int row = Caster.toIntValue(key.getString(), Integer.MIN_VALUE);
		if (row == Integer.MIN_VALUE) return query.set(key, value);
		return set(row, value);
	}

	@Override
	public Object set(int row, Object value) throws DatabaseException {
		return set(row, value, false);
	}

	// Pass trustType=true to optimize operations such as QoQ where lots of values are being moved
	// around between query objects but we know the types are already fine and don't need to
	// redefine them every time
	public Object set(int row, Object value, boolean trustType) throws DatabaseException {
		query.disableIndex();
		// query.disconnectCache();
		if (row < 1) throw new DatabaseException("invalid row number [" + row + "]", "valid row numbers a greater or equal to one", null, null);
		if (row > size) {
			if (size == 0) throw new DatabaseException("cannot set a value to an empty query, you first have to add a row", null, null, null);
			throw new DatabaseException("invalid row number [" + row + "]", "valid row numbers goes from 1 to " + size, null, null);
		}
		synchronized (sync) {
			if (!trustType) value = reDefineType(value);
			data[row - 1] = value;
			return value;
		}
	}

	@Override
	public Object setEL(String key, Object value) {
		int index = Caster.toIntValue(key, Integer.MIN_VALUE);
		if (index == Integer.MIN_VALUE) query.setEL(key, value);
		return setEL(index, value);

	}

	@Override
	public Object setEL(Collection.Key key, Object value) {
		int index = Caster.toIntValue(key.getString(), Integer.MIN_VALUE);
		if (index == Integer.MIN_VALUE) query.setEL(key, value);
		return setEL(index, value);
	}

	@Override
	public Object setEL(int row, Object value) {
		query.disableIndex();
		// query.disconnectCache();
		if (row < 1 || row > size) return value;
		synchronized (sync) {
			value = reDefineType(value);
			data[row - 1] = value;
		}
		return value;
	}

	@Override
	public void add(Object value) {
		query.disableIndex();
		synchronized (sync) {
			// query.disconnectCache();
			if (data.length <= size) growTo(size);
			data[size++] = value;
		}
	}

	@Override
	public void cutRowsTo(int maxrows) {
		synchronized (sync) {
			if (maxrows > -1 && maxrows < size) size = maxrows;
		}
	}

	@Override
	public void addRow(int count) {
		query.disableIndex();
		synchronized (sync) {
			if (data.length < (size + count)) growTo(size + count);
			for (int i = 0; i < count; i++)
				size++;
		}
	}

	@Override
	public Object removeRow(int row) throws DatabaseException {
		query.disableIndex();
		// query.disconnectCache();
		if (row < 1 || row > size) throw new DatabaseException("invalid row number [" + row + "]", "valid rows goes from 1 to " + size, null, null);
		synchronized (sync) {
			Object o = data[row - 1];
			for (int i = row; i < size; i++) {
				data[i - 1] = data[i];
			}
			size--;
			if (NullSupportHelper.full()) return o;
			return o == null ? "" : o;
		}
	}

	@Override
	public int getType() {
		reOrganizeType();
		return type;
	}

	@Override
	public String getTypeAsString() {
		return QueryImpl.getColumTypeName(getType());
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return DumpUtil.toDumpData(QueryUtil.getValue(this, query.getCurrentrow(pageContext.getId())), pageContext, maxlevel, dp);
	}

	private void growTo(int row) {

		int newSize = (data.length + 1) * 2;
		while (newSize <= row) {
			// print.ln(newSize+"<="+row);
			newSize *= 2;
		}

		Object[] newData = new Object[newSize];
		for (int i = 0; i < data.length; i++) {
			newData[i] = data[i];
		}
		data = newData;
	}

	private Object reDefineType(Object value) {
		return QueryColumnUtil.reDefineType(this, value);
	}

	private void resetTypeSync() {
		synchronized (sync) {
			QueryColumnUtil.resetType(this);
		}
	}

	private void resetType() {
		QueryColumnUtil.resetType(this);
	}

	private void reOrganizeType() {
		synchronized (sync) {
			QueryColumnUtil.reOrganizeType(this);
		}
	}

	@Override
	public Collection.Key getKey() {
		return key;
	}

	@Override
	public void setKey(Collection.Key key) {
		query.disableIndex();
		this.key = key;
	}

	@Override
	public String getKeyAsString() throws PageException {
		return key.getLowerString();// TODO ist das OK?
	}

	@Override
	public Object get(PageContext pc) {
		return QueryUtil.getValue(this, query.getCurrentrow(pc.getId()));
	}

	@Override
	public Object get(PageContext pc, Object defaultValue) {
		return get(query.getCurrentrow(pc.getId()), defaultValue);
	}

	@Override
	public Object touch(PageContext pc) throws PageException {
		return touch(query.getCurrentrow(pc.getId()));
	}

	@Override
	public Object touchEL(PageContext pc) {
		return touchEL(query.getCurrentrow(pc.getId()));
	}

	@Override
	public Object set(PageContext pc, Object value) throws PageException {
		return set(query.getCurrentrow(pc.getId()), value);
	}

	@Override
	public Object setEL(PageContext pc, Object value) {
		return setEL(query.getCurrentrow(pc.getId()), value);
	}

	@Override
	public Object getParent() {
		return query;
	}

	@Override
	public String castToString() throws PageException {
		return Caster.toString(get(query.getCurrentrow(ThreadLocalPageContext.get().getId()), null));
	}

	@Override
	public String castToString(String defaultValue) {
		Object _null = NullSupportHelper.NULL();
		Object value = get(query.getCurrentrow(ThreadLocalPageContext.get().getId()), _null);
		if (value == _null) return defaultValue;
		return Caster.toString(value, defaultValue);
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return Caster.toBooleanValue(get(query.getCurrentrow(ThreadLocalPageContext.get().getId()), null));
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		Object _null = NullSupportHelper.NULL();
		Object value = get(query.getCurrentrow(ThreadLocalPageContext.get().getId()), _null);
		if (value == _null) return defaultValue;
		return Caster.toBoolean(value, defaultValue);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return Caster.toDoubleValue(get(query.getCurrentrow(ThreadLocalPageContext.get().getId()), null));
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		Object _null = NullSupportHelper.NULL();
		Object value = get(query.getCurrentrow(ThreadLocalPageContext.get().getId()), _null);
		if (value == _null) return defaultValue;
		return Caster.toDoubleValue(value, true, defaultValue);
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return DateCaster.toDateAdvanced(get(query.getCurrentrow(ThreadLocalPageContext.get().getId()), null), null);
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		Object _null = NullSupportHelper.NULL();
		Object value = get(query.getCurrentrow(ThreadLocalPageContext.get().getId()), _null);
		if (value == _null) return defaultValue;
		return DateCaster.toDateAdvanced(value, DateCaster.CONVERTING_TYPE_OFFSET, null, defaultValue);
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		return Operator.compare(castToBooleanValue(), b);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return Operator.compare((Date) castToDateTime(), (Date) dt);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return Operator.compare(castToDoubleValue(), d);
	}

	@Override
	public int compareTo(String str) throws PageException {
		return Operator.compare(castToString(), str);
	}

	@Override
	public Object clone() {
		return cloneColumnImpl(true);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return cloneColumnImpl(deepCopy);
	}

	@Override
	public QueryColumnPro cloneColumn(boolean deepCopy) {
		return cloneColumnImpl(deepCopy);
	}

	public QueryColumnImpl cloneColumnImpl(boolean deepCopy) {
		QueryColumnImpl clone = new QueryColumnImpl();
		populate(clone, deepCopy);
		return clone;
	}

	protected void populate(QueryColumnImpl trg, boolean deepCopy) {

		boolean inside = ThreadLocalDuplication.set(this, trg);
		try {
			trg.key = this.key;
			trg.query = this.query;
			trg.size = this.size;
			trg.type = this.type;
			trg.key = this.key;
			if (trg.query != null) trg.query.disableIndex();

			// we first get data local, because length of the object cannot be changed, the safes us
			// from
			// modifications from outside
			Object[] data = this.data;
			trg.data = new Object[data.length];
			for (int i = 0; i < data.length; i++) {
				trg.data[i] = deepCopy ? Duplicator.duplicate(data[i], true) : data[i];
			}
		}
		finally {
			if (!inside) ThreadLocalDuplication.reset();
		}
	}

	@Override
	public String toString() {
		try {
			return Caster.toString(get(query.getCurrentrow(ThreadLocalPageContext.get().getId()), null));
		}
		catch (PageException e) {
			return super.toString();
		}
	}

	@Override
	public boolean containsKey(String key) {
		return containsKey(KeyImpl.init(key));
	}

	@Override
	public boolean containsKey(Collection.Key key) {
		Object _null = NullSupportHelper.NULL();
		return get(key, _null) != _null;
	}

	public Iterator iterator() {
		return keyIterator();
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return new KeyIterator(keys());
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return new StringIterator(keys());
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return new EntryIterator(this, keys());
	}

	@Override
	public Iterator<Object> valueIterator() {
		return new ArrayIterator(data, 0, size);
	}

	@Override
	public Object callWithNamedValues(PageContext pc, Key methodName, Struct args) throws PageException {

		throw new ExpressionException("No matching Method/Function [" + methodName + "] for call with named arguments found");
		// return pc.getFunctionWithNamedValues(get(query.getCurrentrow()), methodName,
		// Caster.toFunctionValues(args));
	}

	@Override
	public Object call(PageContext pc, Key methodName, Object[] arguments) throws PageException {
		MethodInstance mi = Reflector.getMethodInstanceEL(this, this.getClass(), methodName, arguments);
		if (mi != null) {
			try {
				return mi.invoke(this);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				try {
					return pc.getFunction(QueryUtil.getValue(this, query.getCurrentrow(pc.getId())), methodName, arguments);
				}
				catch (PageException pe) {
					throw Caster.toPageException(t);
				}
			}
		}
		return pc.getFunction(QueryUtil.getValue(this, query.getCurrentrow(pc.getId())), methodName, arguments);
	}

	@Override
	public Object set(PageContext pc, Key propertyName, Object value) throws PageException {
		return set(propertyName, value);
	}

	@Override
	public Object setEL(PageContext pc, Key propertyName, Object value) {
		return setEL(propertyName, value);
	}

	public void add(int index, Object element) {
		throwNotAllowedToAlter();
		// setEL(index+1, element);
	}

	private void throwNotAllowedToAlter() {
		throw new PageRuntimeException(new DatabaseException("Query columns do not support methods that would alter the structure of a query column",
				"you must use an analogous method on the query", null, null));

	}

	public boolean addAll(java.util.Collection<? extends Object> c) {
		throwNotAllowedToAlter();
		return false;
		/*
		 * Iterator<? extends Object> it = c.iterator(); while(it.hasNext()){ add(it.next()); } return true;
		 */
	}

	public boolean addAll(int index, java.util.Collection<? extends Object> c) {
		throwNotAllowedToAlter();
		return false;
		/*
		 * Iterator<? extends Object> it = c.iterator(); while(it.hasNext()){ setEL(++index,it.next()); }
		 * return true;
		 */
	}

	public boolean contains(Object o) {
		return indexOf(o) != -1;
	}

	public boolean containsAll(java.util.Collection<?> c) {
		Iterator<? extends Object> it = c.iterator();
		while (it.hasNext()) {
			if (indexOf(it.next()) == -1) return false;
		}
		return true;
	}

	public int indexOf(Object o) {
		for (int i = 0; i < size; i++) {
			try {
				if (Operator.compare(o, data[i]) == 0) return i;
			}
			catch (PageException e) {
			}
		}
		return -1;
	}

	public int lastIndexOf(Object o) {
		for (int i = size - 1; i >= 0; i--) {
			try {
				if (Operator.compare(o, data[i]) == 0) return i;
			}
			catch (PageException e) {
			}
		}
		return -1;
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public boolean removeAll(java.util.Collection<?> c) {
		throwNotAllowedToAlter();
		return false;
		/*
		 * boolean hasChanged=false; Iterator<? extends Object> it = c.iterator(); while(it.hasNext()){
		 * if(remove(it.next())) { hasChanged=true; } } return hasChanged;
		 */
	}

	public boolean retainAll(java.util.Collection<?> c) {
		throwNotAllowedToAlter();
		return false;
		/*
		 * boolean hasChanged=false; Iterator it = valueIterator(); while(it.hasNext()){
		 * if(!c.contains(it.next())){ hasChanged=true; it.remove(); } } return hasChanged;
		 */
	}

	public List<Object> subList(int fromIndex, int toIndex) {
		ArrayList<Object> list = new ArrayList<Object>();
		for (int i = fromIndex; i < toIndex; i++) {
			list.add(data[i]);
		}
		return list;
	}

	@Override
	public Object[] toArray() {
		return toArray(new Object[size()]);
	}

	public Object[] toArray(Object[] trg) {
		System.arraycopy(data, 0, trg, 0, data.length > trg.length ? trg.length : data.length);
		return trg;
	}

	@Override
	public QueryColumnPro toDebugColumn() {
		return _toDebugColumn();
	}

	public DebugQueryColumn _toDebugColumn() {
		return new DebugQueryColumn(data, key, query, size, type, typeChecked);
	}

	@Override
	public java.util.Iterator<String> getIterator() {
		return keysAsStringIterator();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Collection)) return false;
		return CollectionUtil.equals(this, (Collection) obj);
	}

	@Override
	public int getDimension() {
		return 1;
	}

	@Override
	public Object getE(int row) throws PageException {
		return get(row);
	}

	@Override
	public Object setE(int key, Object value) throws PageException {
		return set(key, value);
	}

	@Override
	public int[] intKeys() {
		int[] keys = new int[size()];
		int len = keys.length;
		for (int i = 1; i <= len; i++) {
			keys[i - 1] = i;
		}
		return keys;
	}

	@Override
	public boolean insert(int key, Object value) throws PageException {
		throwNotAllowedToAlter();
		return false;
	}

	@Override
	public Object append(Object o) throws PageException {
		throwNotAllowedToAlter();
		return o;
	}

	@Override
	public Object appendEL(Object o) {
		throwNotAllowedToAlter();
		return o;
	}

	@Override
	public Object prepend(Object o) throws PageException {
		throwNotAllowedToAlter();
		return o;
	}

	@Override
	public void resize(int to) throws PageException {
		throwNotAllowedToAlter();
	}

	@Override
	public void sort(String sortType, String sortOrder) throws PageException {
		throwNotAllowedToAlter();
	}

	protected void sort(int[] rows) throws PageException {
		query.disableIndex();
		Object[] tmp = new Object[data.length];
		for (int i = 0; i < size; i++) {
			tmp[i] = data[rows[i] - 1];
		}
		data = tmp;
	}

	@Override
	public void sortIt(Comparator comp) {
		throwNotAllowedToAlter();
	}

	@Override
	public List toList() {
		Iterator<Object> it = valueIterator();
		ArrayList list = new ArrayList();
		while (it.hasNext()) {
			list.add(it.next());
		}
		return list;
	}

	@Override
	public Object removeE(int key) throws PageException {
		throwNotAllowedToAlter();
		return null;
	}

	@Override
	public boolean containsKey(int key) {
		Object _null = NullSupportHelper.NULL();
		return get(key, _null) != _null;
	}

	/*
	 * @Override public int hashCode() { return CollectionUtil.hashCode(this); }
	 */
}