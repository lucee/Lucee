package lucee.runtime.pool;

public interface PoolItem {
	public void start() throws Exception;

	public boolean isValid();

	public void end() throws Exception;
}
