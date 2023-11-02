package lucee.commons.io.cache.complex;

import java.io.Serializable;

public class CacheComplexData implements Serializable {

	private static final long serialVersionUID = 6401384011421058561L;

	public final Object value;
	public final long lastModified;
	public final Long idle;
	public final Long until;
	public final int hitCount = 0; // TODO

	public CacheComplexData(Object value, Long idle, Long until) {
		this.value = value;
		this.lastModified = System.currentTimeMillis();
		this.idle = idle;
		this.until = until;
	}

	public String toString() {
		return value.toString();
	}
}
