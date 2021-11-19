package lucee.runtime.type.scope.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

import lucee.commons.io.IOUtil;
import lucee.commons.lang.NumberUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.util.KeyConstants;

public class IKStorageValue implements Serializable {

	private static final long serialVersionUID = 2728185742217909233L;
	private static final byte[] EMPTY = new byte[0];

	transient Map<Collection.Key, IKStorageScopeItem> value;
	final byte[] barr;
	private long _lastModified;
	private long _timecreated;
	private long _lastVisit;

	public IKStorageValue(Map<Collection.Key, IKStorageScopeItem> value) throws PageException {
		this.value = value;
		this._lastModified = System.currentTimeMillis();

		// keep this, because sserialize will remove the data
		getLastVisit();
		getTimeCreated();
		this.barr = serialize(value);
	}

	public IKStorageValue(byte[][] barrr) throws PageException {
		this.barr = barrr[0];
		this._lastModified = toLong(barrr[1]);
		this._timecreated = toLong(barrr[2]);
		this._lastVisit = toLong(barrr[3]);
	}

	public static byte[][] toByteRepresentation(Map<Collection.Key, IKStorageScopeItem> value) throws PageException {
		// this needs to be done BEFORE serialize is called!
		long tc = toLong(value, KeyConstants._timecreated);
		long lv = toLong(value, KeyConstants._lastvisit);
		return new byte[][] { serialize(value), NumberUtil.longToByteArray(System.currentTimeMillis()), NumberUtil.longToByteArray(tc), NumberUtil.longToByteArray(lv) };
	}

	public static byte[][] toByteRepresentation(IKStorageValue val) throws PageException {
		return new byte[][] { val.barr, NumberUtil.longToByteArray(val.getLastModified()), NumberUtil.longToByteArray(val.getTimeCreated()),
				NumberUtil.longToByteArray(val.getLastVisit()) };
	}

	public static long toLong(Map<Key, IKStorageScopeItem> value, Key key) {
		if (value == null) return 0;
		return toLong(value.get(key));
	}

	public static long toLong(IKStorageScopeItem item) {
		if (item == null) return 0;
		DateTime date = Caster.toDate(item.getValue(), false, null, null);
		if (date == null) return 0;
		return date.getTime();
	}

	public static long toLong(byte[] barr) {
		return NumberUtil.byteArrayToLong(barr);
	}

	private static byte[] toByteArray(Map<Key, IKStorageScopeItem> value, Key key) {
		return NumberUtil.longToByteArray(toLong(value, key));
	}

	public long getLastModified() {
		return _lastModified;
	}

	public long getTimeCreated() {
		if (_timecreated == 0 && value != null) {
			IKStorageScopeItem item;
			if ((item = value.get(KeyConstants._timecreated)) != null) {
				_timecreated = toLong(item);
			}
		}
		return _timecreated;
	}

	public long getLastVisit() {
		if (_lastVisit == 0 && value != null) {
			IKStorageScopeItem item;
			if ((item = value.get(KeyConstants._lastvisit)) != null) {
				_lastVisit = toLong(item);
			}
		}
		return _lastVisit;
	}

	public Map<Collection.Key, IKStorageScopeItem> getValue() throws PageException {

		if (value == null) {
			if (barr.length == 0) return null;
			value = deserialize(barr, getTimeCreated(), getLastVisit()); // timecreated, long lastvisit
		}
		return value;
	}

	public static Map<Collection.Key, IKStorageScopeItem> deserialize(byte[] barr, long timecreated, long lastvisit) throws PageException {
		if (barr == null || barr.length == 0) return null;
		ObjectInputStream ois = null;
		Map<Collection.Key, IKStorageScopeItem> data = null;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(barr));
			data = (Map<Collection.Key, IKStorageScopeItem>) ois.readObject();
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		finally {
			try {
				IOUtil.close(ois);
			}
			catch (IOException e) {
				throw Caster.toPageException(e);
			}
		}

		IKStorageScopeItem cfid = data.get(KeyConstants._cfid);
		if (cfid == null) return data;

		if (timecreated > 0) data.put(KeyConstants._timecreated, new IKStorageScopeItem(new DateTimeImpl(timecreated, false), cfid.lastModified()));
		if (lastvisit > 0) data.put(KeyConstants._lastvisit, new IKStorageScopeItem(new DateTimeImpl(lastvisit, false), cfid.lastModified()));

		data.put(KeyConstants._urltoken, new IKStorageScopeItem("CFID=" + cfid.getValue() + "&CFTOKEN=0", lastvisit));
		data.put(KeyConstants._cftoken, new IKStorageScopeItem("0", lastvisit));
		return data;
	}

	static byte[] serialize(Map<Collection.Key, IKStorageScopeItem> data) throws PageException {
		if (data == null) return EMPTY;

		data.remove(KeyConstants._timecreated);
		data.remove(KeyConstants._lastvisit);
		data.remove(KeyConstants._urltoken); // CFID=5d2d23e4-c03c-46e3-b924-e0425b913cb5&CFTOKEN=0
		data.remove(KeyConstants._cftoken);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(os);
			oos.writeObject(data);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		finally {
			try {
				IOUtil.close(oos);
			}
			catch (IOException e) {
				throw Caster.toPageException(e);
			}
		}
		return os.toByteArray();

		// NumberUtil.longToByteArray(Caster.toLongValue(value.get(KeyConstants._timecreated), 0)),
		// NumberUtil.longToByteArray(Caster.toLongValue(value.get(KeyConstants._lastvisit), 0)) };
	}
}
