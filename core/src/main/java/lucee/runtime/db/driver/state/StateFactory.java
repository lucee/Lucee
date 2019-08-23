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
package lucee.runtime.db.driver.state;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.Statement;

import lucee.runtime.db.driver.CallableStatementProxy;
import lucee.runtime.db.driver.ConnectionProxy;
import lucee.runtime.db.driver.Factory;
import lucee.runtime.db.driver.PreparedStatementProxy;
import lucee.runtime.db.driver.StatementProxy;

public class StateFactory implements Factory {

	@Override
	public StatementProxy createStatementProxy(ConnectionProxy conn, Statement stat) {
		return new StateStatement(conn, stat);
	}

	@Override
	public PreparedStatementProxy createPreparedStatementProxy(ConnectionProxy conn, PreparedStatement stat, String sql) {
		return new StatePreparedStatement(conn, stat, sql);
	}

	@Override
	public CallableStatementProxy createCallableStatementProxy(ConnectionProxy conn, CallableStatement stat, String sql) {
		return new StateCallableStatement(conn, stat, sql);
	}

}