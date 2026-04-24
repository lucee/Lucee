package lucee.runtime.type.query;

import java.util.Iterator;
import java.util.Map.Entry;

import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.CastablePro;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.dt.DateTime;

public class CurrentRow implements Collection, CastablePro {

	private Query qry;
	private int row;
	private boolean createColumnIfMissing;

	public CurrentRow(Query qry, int row, boolean createColumnIfMissing) {
		this.qry = qry;
		this.row = row;
		this.createColumnIfMissing = createColumnIfMissing;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {
		return qry.toDumpData(pageContext, maxlevel, properties);
	}

	@Override
	public Iterator<Key> keyIterator() {
		return qry.keyIterator();
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return qry.keysAsStringIterator();
	}

	@Override
	public Iterator<Object> valueIterator() {
		return qry.valueIterator();
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return qry.entryIterator();
	}

	@Override
	public String castToString() throws PageException {
		return qry.castToString();
	}

	@Override
	public String castToString(String defaultValue) {
		return qry.castToString(defaultValue);
	}

	@Override
	public String castToString(PageContext pc) throws PageException {
		if (qry instanceof CastablePro) {
			return ((CastablePro) qry).castToString(pc);
		}
		return qry.castToString();
	}

	@Override
	public String castToString(PageContext pc, String defaultValue) {
		if (qry instanceof CastablePro) {
			return ((CastablePro) qry).castToString(pc, defaultValue);
		}
		return qry.castToString(defaultValue);
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return qry.castToBooleanValue();
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return qry.castToBoolean(defaultValue);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return qry.castToDoubleValue();
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return qry.castToDoubleValue(defaultValue);
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return qry.castToDateTime();
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return qry.castToDateTime(defaultValue);
	}

	@Override
	public int compareTo(String str) throws PageException {
		return qry.compareTo(str);
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		return qry.compareTo(b);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return qry.compareTo(d);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return qry.compareTo(dt);
	}

	@Override
	public Iterator<?> getIterator() {
		return qry.getIterator();
	}

	@Override
	public int size() {
		return qry.size();
	}

	@Override
	public Key[] keys() {
		return qry.keys();
	}

	@Override
	public Object remove(Key key) throws PageException {
		// TODO removeAT
		return qry.remove(key);
	}

	@Override
	public Object removeEL(Key key) {
		// TODO removeATEL
		return qry.removeEL(key);
	}

	@Override
	public Object remove(Key key, Object defaultValue) {
		// TODO removeAT
		return qry.remove(key, defaultValue);
	}

	@Override
	public void clear() {
		qry.clear();
	}

	@Override
	public Object get(String key) throws PageException {
		return qry.getAt(key, row);
	}

	@Override
	public Object get(Key key) throws PageException {
		return qry.getAt(key, row);
	}

	@Override
	public Object get(String key, Object defaultValue) {
		return qry.getAt(key, row, defaultValue);
	}

	@Override
	public Object get(Key key, Object defaultValue) {
		return qry.getAt(key, row, defaultValue);
	}

	@Override
	public Object set(String key, Object value) throws PageException {
		if (createColumnIfMissing) createColumnIfMissing(KeyImpl.init(key));
		return qry.setAt(key, row, value);
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		if (createColumnIfMissing) createColumnIfMissing(key);
		return qry.setAt(key, row, value);
	}

	@Override
	public Object setEL(String key, Object value) {
		return setEL(KeyImpl.init(key), value);
	}

	@Override
	public Object setEL(Key key, Object value) {
		if (createColumnIfMissing) {
			try {
				createColumnIfMissing(key);
			}
			catch (Exception e) {}
		}
		return qry.setAtEL(key, row, value);
	}

	public void createColumnIfMissing(Key key) throws PageException {
		if (qry.getColumn(key, null) != null) return;
		qry.addColumn(key, new ArrayImpl());
	}

	@Override
	public boolean containsKey(String key) {
		// TODO containsKeyAat
		return qry.containsKey(key);
	}

	@Override
	public boolean containsKey(Key key) {
		// TODO containsKeyAat
		return qry.containsKey(key);
	}

	@Override
	public boolean containsKey(PageContext pc, Key key) {
		// TODO containsKeyAat
		return qry.containsKey(pc, key);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return new CurrentRow((Query) qry.duplicate(deepCopy), row, createColumnIfMissing);
	}

	@Override
	public Object clone() {
		return new CurrentRow((Query) qry.clone(), row, createColumnIfMissing);
	}

}
