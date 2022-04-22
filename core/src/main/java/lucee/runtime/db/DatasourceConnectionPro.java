package lucee.runtime.db;

import java.sql.SQLException;

import lucee.runtime.exp.PageException;

// FUTURE add methods to DatasourceConnection and delete this interface

public interface DatasourceConnectionPro extends DatasourceConnection {

	public boolean isAutoCommit() throws SQLException;

	@Override
	public void setAutoCommit(boolean setting) throws SQLException;

	public int getDefaultTransactionIsolation();

	public DatasourceConnection using() throws PageException;

	public void release();

	public boolean validate();
}