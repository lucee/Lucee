package lucee.runtime.type.query;

import lucee.runtime.db.SQL;
import lucee.runtime.dump.Dumpable;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Duplicable;

public interface QueryResult extends Dumpable,Duplicable {

	public SQL getSql();

	public boolean isCached();
	
	public void setCacheType(String cacheType);

    public String getCacheType();

	public long getExecutionTime();
	public void setExecutionTime(long time);
	
	public String getTemplate();
	
	public String getName();
	
	public boolean isEmpty();
	
	public int getRecordcount();

	public int getUpdateCount();
	public void setUpdateCount(int updateCount);
	
	public Key[] getColumnNames();
	public void setColumnNames(Key[] columnNames) throws PageException;

}
