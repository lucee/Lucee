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

// FUTURE add to extended interface and delete this interface

import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.config.Config;
import lucee.runtime.db.SQL;
import lucee.runtime.type.Query;

public interface DebuggerPro extends Debugger {
	
	
	

    /**
     * add new query execution time
     * @param query 
     * @param datasource 
     * @param name
     * @param sql
     * @param recordcount
     * @param src
     * @param time 
     * @deprecated use instead <code>addQuery(Query query,String datasource,String name,SQL sql, int recordcount, PageSource src,long time)</code>
     */
    @Deprecated
	@Override
	public void addQuery(Query query,String datasource,String name,SQL sql, int recordcount, PageSource src,int time);
    
    /**
     * add new query execution time
     * @param query 
     * @param datasource 
     * @param name
     * @param sql
     * @param recordcount
     * @param src
     * @param time 
     */
    @Override
	public void addQuery(Query query,String datasource,String name,SQL sql, int recordcount, PageSource src,long time);
    
    @Override
	public DebugTrace[] getTraces(PageContext pc);
    
    public DebugDump addDump(PageSource ps,String dump);
	
    
    
    
    
    
    public void setOutputLog(DebugOutputLog outputLog);
    
    public void init(Config config);
}