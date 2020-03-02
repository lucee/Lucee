package lucee.runtime.db;

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
}
