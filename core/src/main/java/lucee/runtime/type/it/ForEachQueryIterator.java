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
package lucee.runtime.type.it;

import java.sql.SQLException;
import java.util.Iterator;

import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;
import lucee.runtime.type.Resetable;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.query.SimpleQuery;

public class ForEachQueryIterator implements Iterator, Resetable {

	private Query qry;
	private int pid;
	private int start, current = 0;
	private Key[] names;
	private PageContext pcMayNull;

	public ForEachQueryIterator(PageContext pc, Query qry, int pid) {
		this.pcMayNull = pc;
		this.qry = qry;
		this.pid = pid;
		this.start = qry.getCurrentrow(pid);
		this.names = qry.getColumnNames();
	}

	@Override
	public boolean hasNext() {
		return current < qry.getRecordcount();
	}

	@Override
	public Object next() {
		try {
			if (qry.go(++current, pid)) {
				Struct sct = new StructImpl();
				Object empty = NullSupportHelper.full(pcMayNull) ? null : "";
				for (int i = 0; i < names.length; i++) {
					sct.setEL(names[i], qry.get(names[i], empty));
				}
				return sct;
			}
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
		return null;
	}

	@Override
	public void remove() {
		try {
			qry.removeRow(current);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	@Override
	public void reset() throws PageException {
		qry.go(start, pid);
		if (qry instanceof SimpleQuery) {
			SimpleQuery sq = (SimpleQuery) qry;
			try {
				if (!sq.isClosed()) {
					sq.close();
				}
			}
			catch (SQLException e) {
				throw new DatabaseException(e, sq.getDc());
			}

		}
	}

}