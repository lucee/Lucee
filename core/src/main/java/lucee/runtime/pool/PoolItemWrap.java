package lucee.runtime.pool;

public class PoolItemWrap {

	private PoolItem value;
	private long last;

	public PoolItemWrap(PoolItem value) {
		this.value = value;
		this.last = System.currentTimeMillis();
	}

	public PoolItemWrap setLastAccess(long last) {
		this.last = last;
		return this;
	}

	public long lastAccess() {
		return last;
	}

	public PoolItem getValue() {
		return value;
	}

	public void start() throws Exception {
		value.start();
	}

	public void end() throws Exception {
		value.end();
	}
}
