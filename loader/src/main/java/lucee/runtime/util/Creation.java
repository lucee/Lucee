/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.runtime.util;

import java.io.File;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.types.RefBoolean;
import lucee.commons.lang.types.RefDouble;
import lucee.commons.lang.types.RefInteger;
import lucee.commons.lang.types.RefLong;
import lucee.commons.lang.types.RefString;
import lucee.commons.lock.KeyLock;
import lucee.runtime.Component;
import lucee.runtime.Mapping;
import lucee.runtime.PageContext;
import lucee.runtime.component.Property;
import lucee.runtime.config.Config;
import lucee.runtime.config.RemoteClient;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.db.SQL;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.ApplicationListener;
import lucee.runtime.spooler.ExecutionPlan;
import lucee.runtime.spooler.SpoolerTask;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.Date;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.Time;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.scope.ClusterEntry;

/**
 * Creation of different Objects
 */
public interface Creation {

	/**
	 * creates and returns an array instance
	 * 
	 * @return array
	 */
	public abstract Array createArray();

	/**
	 * creates and returns an array based on a string list
	 * 
	 * @return array
	 */
	public abstract Array createArray(String list, String delimiter, boolean removeEmptyItem, boolean trim);

	/**
	 * creates and returns a DateTime instance
	 * 
	 * @param time
	 * @return DateTime
	 */
	public abstract DateTime createDateTime(long time);

	/**
	 * creates and returns a DateTime instance
	 * 
	 * @param year
	 * @param month
	 * @param day
	 * @param hour
	 * @param minute
	 * @param seond
	 * @param millis
	 * @return DateTime
	 */
	public abstract DateTime createDateTime(int year, int month, int day, int hour, int minute, int seond, int millis) throws PageException;

	/**
	 * creates and returns a Date instance
	 * 
	 * @param time
	 * @return DateTime
	 */
	public abstract Date createDate(long time);

	/**
	 * creates and returns a Date instance
	 * 
	 * @param year
	 * @param month
	 * @param day
	 * @return DateTime
	 */
	public abstract Date createDate(int year, int month, int day) throws PageException;

	/**
	 * creates and returns a Time instance
	 * 
	 * @param time
	 * @return DateTime
	 */
	public abstract Time createTime(long time);

	/**
	 * creates and returns a Time instance
	 * 
	 * @param hour
	 * @param minute
	 * @param second
	 * @param millis
	 * @return DateTime
	 */
	public abstract Time createTime(int hour, int minute, int second, int millis);

	/**
	 * creates and returns a TimeSpan instance
	 * 
	 * @param day
	 * @param hour
	 * @param minute
	 * @param second
	 * @return TimeSpan
	 */
	public abstract TimeSpan createTimeSpan(int day, int hour, int minute, int second);

	/**
	 * creates and returns an array instance
	 * 
	 * @param dimension
	 * @return array
	 * @throws PageException
	 */
	public abstract Array createArray(int dimension) throws PageException;

	/**
	 * creates and returns a struct instance
	 * 
	 * @return struct
	 */
	public abstract Struct createStruct();

	public abstract Struct createStruct(int type);

	@Deprecated
	public abstract Struct createStruct(String type) throws PageException;

	public abstract Struct createCastableStruct(Object value);

	public abstract Struct createCastableStruct(Object value, int type);

	/**
	 * creates a query object with given data
	 * 
	 * @param columns
	 * @param rows
	 * @param name
	 * @return created query Object
	 * @deprecated use instead <code>createQuery(Collection.Key[] columns, int rows, String name)</code>
	 */
	@Deprecated
	public abstract Query createQuery(String[] columns, int rows, String name);

	/**
	 * creates a query object with given data
	 * 
	 * @param columns
	 * @param rows
	 * @param name
	 * @return created query Object
	 */
	public abstract Query createQuery(Collection.Key[] columns, int rows, String name) throws PageException;

	/**
	 * creates a query object with given data
	 * 
	 * @param columns
	 * @param rows
	 * @param name
	 * @return created query Object
	 * @deprecated use instead
	 *             <code>createQuery(Collection.Key[] columns, String[] types, int rows, String name)</code>
	 */
	@Deprecated
	public abstract Query createQuery(String[] columns, String[] types, int rows, String name) throws PageException;

	/**
	 * creates a query object with given data
	 * 
	 * @param columns
	 * @param rows
	 * @param name
	 * @return created query Object
	 */
	public abstract Query createQuery(Collection.Key[] columns, String[] types, int rows, String name) throws PageException;

	/**
	 * @param dc Connection to a database
	 * @param sql sql to execute
	 * @param maxrow maxrow for the resultset
	 * @param fetchsize
	 * @param timeout in seconds
	 * @param name
	 * @return created Query
	 * @throws PageException
	 */
	public abstract Query createQuery(DatasourceConnection dc, SQL sql, int maxrow, int fetchsize, int timeout, String name) throws PageException;

	/**
	 * creates a collection Key out of a String
	 * 
	 * @param key
	 */
	public abstract Collection.Key createKey(String key);

	public SpoolerTask createRemoteClientTask(ExecutionPlan[] plans, RemoteClient remoteClient, Struct attrColl, String callerId, String type);

	public ClusterEntry createClusterEntry(Key key, Serializable value, int offset);

	public Resource createResource(String path, boolean existing) throws PageException;

	public abstract HttpServletRequest createHttpServletRequest(File contextRoot, String serverName, String scriptName, String queryString, Cookie[] cookies,
			Map<String, Object> headers, Map<String, String> parameters, Map<String, Object> attributes, HttpSession session);

	public abstract HttpServletResponse createHttpServletResponse(OutputStream io);

	public abstract PageContext createPageContext(HttpServletRequest req, HttpServletResponse rsp, OutputStream out);

	// FUTURE public ServletConfig createServletConfig(File root, Map<String, Object> attributes,
	// Map<String, String> params)

	/**
	 * creates a component object from (Full)Name, for example lucee.extensions.net.HTTPUtil
	 * 
	 * @param pc Pagecontext for loading the CFC
	 * @param fullName full name of the cfc example:lucee.extensions.net.HTTPUtil
	 * @return loaded cfc
	 * @throws PageException
	 */
	public abstract Component createComponentFromName(PageContext pc, String fullName) throws PageException;

	/**
	 * creates a component object from an absolute local path, for example
	 * /Users/susi/Projects/Sorglos/wwwrooot/lucee/extensions/net/HTTPUtil.cfc
	 * 
	 * @param pc Pagecontext for loading the CFC
	 * @param path path of the cfc example:/Users/susi/Projects/Sorglos/wwwrooot/
	 *            lucee/extensions/net/HTTPUtil.cfc
	 * @return loaded cfc
	 * @throws PageException
	 */
	public abstract Component createComponentFromPath(PageContext pc, String path) throws PageException;

	public abstract RefBoolean createRefBoolean(boolean b);

	public abstract RefInteger createRefInteger(int i);

	public abstract RefLong createRefLong(long l);

	public abstract RefDouble createRefDouble(long d);

	public abstract String createUUID();

	public abstract String createGUID();

	public abstract Property createProperty(String name, String type);

	public abstract Mapping createMapping(Config config, String virtual, String strPhysical, String strArchive, short inspect, boolean physicalFirst, boolean hidden,
			boolean readonly, boolean topLevel, boolean appMapping, boolean ignoreVirtual, ApplicationListener appListener, int listenerMode, int listenerType);

	public abstract DateTime now();

	public abstract <K> KeyLock<K> createKeyLock();

	public abstract RefString createRefString(String value);

}