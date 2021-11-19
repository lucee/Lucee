package lucee.commons.io.cache.complex;

import java.util.Date;

import lucee.commons.io.cache.CacheEntry;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTimeImpl;

public class CacheComplexEntry implements CacheEntry {

	private CacheEntry entry;
	private CacheComplex cache;
	private CacheComplexData data;
	private Object value;

	public CacheComplexEntry(CacheComplex cache, CacheEntry entry) {
		this.cache = cache;
		this.entry = entry;
	}

	@Override
	public Date created() {
		return lastModified();
	}

	@Override
	public Struct getCustomInfo() {
		return CacheUtil.getInfo(entry.getCustomInfo(), this);
	}

	@Override
	public String getKey() {
		return entry.getKey();
	}

	@Override
	public Object getValue() {
		getData();
		return value;
	}

	public CacheComplexData getData() {
		if (data != null) return data;
		Object v = entry.getValue();
		if (v instanceof CacheComplexData) {
			data = (CacheComplexData) v;
			value = data.value;
		}
		else if (v != null) {
			value = v;
		}
		return null;
	}

	@Override
	public int hitCount() {
		CacheComplexData d = getData();
		if (d != null) return d.hitCount;
		return 0;
	}

	@Override
	public long idleTimeSpan() {
		long i = entry.idleTimeSpan();
		if (i > 0) return i;

		CacheComplexData d = getData();
		if (d != null && d.idle != null && d.idle.longValue() > 0) return d.idle.longValue();

		return 0;
	}

	@Override
	public long liveTimeSpan() {
		long l = entry.liveTimeSpan();
		if (l > 0) return l;

		CacheComplexData d = getData();
		if (d != null && d.until != null && d.until.longValue() > 0) return d.until.longValue();

		return 0;
	}

	@Override
	public Date lastHit() {
		Date d = entry.lastHit();
		if (d != null) return d;

		return lastModified();

	}

	@Override
	public Date lastModified() {
		Date d = entry.lastModified();
		if (d != null) return d;

		CacheComplexData ccd = getData();
		if (ccd != null && ccd.lastModified > 0) return new DateTimeImpl(ccd.lastModified, false);

		return new DateTimeImpl(0, false);
	}

	@Override
	public long size() {
		long s = entry.size();
		if (s > 0) return s;

		Object v = getValue();
		if (v != null && Decision.isSimpleValue(v)) return Caster.toString(v, "").length();
		return 0;
	}

}
