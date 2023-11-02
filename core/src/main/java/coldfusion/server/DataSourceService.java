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
package coldfusion.server;

import java.io.IOException;
import java.sql.SQLException;

import coldfusion.sql.DataSource;
import lucee.runtime.exp.SecurityException;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;

public interface DataSourceService extends Service {

	/*
	 * TODO impl public abstract Query executeQuery(Connection arg0, String arg1, ParameterList arg2,
	 * Integer arg3, Integer arg4, Integer arg5, int[] arg6, int arg7, int arg8, boolean arg9, boolean
	 * arg10) throws SQLException;
	 * 
	 * public abstract Query executeQuery(Connection arg0, String arg1, ParameterList arg2, Integer
	 * arg3, Integer arg4, Integer arg5, int[] arg6, String arg7) throws SQLException;
	 * 
	 * public abstract Query executeQuery(Connection arg0, String arg1, ParameterList arg2, Integer
	 * arg3, Integer arg4, Integer arg5, int[] arg6, DataSourceDef arg7) throws SQLException;
	 * 
	 * public abstract Query executeQuery(Connection arg0, String arg1, ParameterList arg2, Integer
	 * arg3, Integer arg4, Integer arg5, int[] arg6, Object arg7) throws SQLException;
	 * 
	 * public abstract Query executeCall(Connection arg0, String arg1, ParameterList arg2, int[] arg3,
	 * Integer arg4, Integer arg5, int[] arg6, int arg7, int arg8, boolean arg9, boolean arg10) throws
	 * SQLException;
	 * 
	 * public abstract Query executeCall(Connection arg0, String arg1, ParameterList arg2, int[] arg3,
	 * Integer arg4, Integer arg5, int[] arg6, String arg7) throws SQLException;
	 * 
	 * public abstract Query executeCall(Connection arg0, String arg1, ParameterList arg2, int[] arg3,
	 * Integer arg4, Integer arg5, int[] arg6, DataSourceDef arg7) throws SQLException;
	 * 
	 * public abstract Query executeCall(Connection arg0, String arg1, ParameterList arg2, int[] arg3,
	 * Integer arg4, Integer arg5, int[] arg6, Object arg7) throws SQLException;
	 */
	public abstract Struct getDatasources() throws SecurityException;

	public abstract Struct getDrivers() throws ServiceException, SecurityException;

	public abstract Array getNames() throws SecurityException;

	public abstract Struct getDefaults();

	public abstract Number getMaxQueryCount();

	public abstract void setMaxQueryCount(Number arg0);

	public abstract String encryptPassword(String arg0);

	public abstract boolean verifyDatasource(String arg0) throws SQLException, SecurityException;

	public abstract DataSource getDatasource(String arg0) throws SQLException, SecurityException;

	public abstract String getDbdir();

	public abstract Object getCachedQuery(String arg0);

	public abstract void setCachedQuery(String arg0, Object arg1);

	public abstract void purgeQueryCache() throws IOException;

	public abstract boolean disableConnection(String arg0);

	public abstract boolean isJadoZoomLoaded();

	public abstract void removeDatasource(String arg0) throws SQLException, SecurityException;

}