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

import lucee.commons.io.SystemUtil.TemplateLine;
import lucee.runtime.db.SQL;
import lucee.runtime.type.Query;
import lucee.runtime.type.query.QueryResult;

/**
 * 
 */
public final class QueryResultEntryImpl implements QueryEntry {

	private static final long serialVersionUID = 8655915268130645466L;

	private final SQL sql;
	private final long exe;
	private final String name;
	private final int recordcount;
	private final String datasource;
	private final QueryResult qr;
	private final long startTime;
	private TemplateLine tl;

	/**
	 * constructor of the class
	 * 
	 * @param recordcount
	 * @param query
	 * @param src
	 * @param exe
	 */
	public QueryResultEntryImpl(QueryResult qr, String datasource, String name, SQL sql, int recordcount, TemplateLine tl, long exe) {
		this.startTime = System.currentTimeMillis() - (exe / 1000000);
		this.datasource = datasource;
		this.recordcount = recordcount;
		this.name = name;
		this.tl = tl;
		this.sql = sql;
		this.exe = exe;
		this.qr = qr;
	}

	@Override
	public Query getQry() { // FUTURE deprecate
		if (qr instanceof Query) return (Query) qr;
		return null;
	}

	public QueryResult getQueryResult() {
		return qr;
	}

	@Override
	public int getExe() {
		return (int) getExecutionTime();
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
		return tl == null ? "" : tl.template;
	}

	public TemplateLine getTemplateLine() {
		return tl;
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
		return qr == null ? null : qr.getCacheType();
	}
}