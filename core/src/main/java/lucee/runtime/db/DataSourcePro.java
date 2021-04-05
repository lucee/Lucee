package lucee.runtime.db;

import lucee.runtime.tag.listener.TagListener;

// FUTURE move content to loader
public interface DataSourcePro extends DataSource {
	/**
	 * should connections produced from this datasource be exclusive to a request or not?
	 * 
	 * @return
	 */
	public boolean isRequestExclusive();

	public boolean isAlwaysResetConnections();

	public int getDefaultTransactionIsolation();

	public TagListener getListener();

	public int getIdleTimeout();

	public int getLiveTimeout();

	public int getMinIdle();

	public int getMaxIdle();

	public int getMaxTotal();
}
