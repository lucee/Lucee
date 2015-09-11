/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.runtime.debug;

import lucee.runtime.db.SQL;
import lucee.runtime.type.Query;

/**
 * 
 */
public final class QueryEntryImpl implements QueryEntry {

	private static final long serialVersionUID = 8655915268130645466L;
	
	private final String src;
	private final SQL sql;
	private final long exe;
    private final String name;
    private final int recordcount;
    private final String datasource;
	private final Query qry;
	private final long startTime;
	
	/**
	 * constructor of the class
	 * @param recordcount
	 * @param query
	 * @param src
	 * @param exe
	 */
	public QueryEntryImpl(Query qry,String datasource, String name,SQL sql,int recordcount, String src, long exe) {
		this.startTime=System.currentTimeMillis()-(exe/1000000);
		this.datasource=datasource;
        this.recordcount=recordcount;
        this.name=name;
	    this.src=src;
		this.sql=sql;
		this.exe=exe;
		this.qry=qry;
	}
	
	@Override
	public Query getQry() {
		return qry;
	}
	
	@Override
	public int getExe() {
		return (int)getExecutionTime();
	}
	
	@Override
	public long getExecutionTime() {
		return exe;
	}
	
	
	@Override
	public SQL getSQL() {
		return sql;
	}
	@Override
	public String getSrc() {
		return src;
	}
	@Override
	public String getName() {
        return name;
    }
	@Override
	public int getRecordcount() {
        return recordcount;
    }
	@Override
	public String getDatasource() {
        return datasource;
    }
	
	@Override
	public long getStartTime() {
        return startTime;
    }

	@Override
	public String getCacheType() {
		return qry.getCacheType();
	}
}