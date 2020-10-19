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
package lucee.runtime.tag;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.osgi.framework.BundleException;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.SystemUtil.TemplateLine;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.cache.tag.CacheHandler;
import lucee.runtime.cache.tag.CacheHandlerCollectionImpl;
import lucee.runtime.cache.tag.CacheHandlerPro;
import lucee.runtime.cache.tag.CacheItem;
import lucee.runtime.cache.tag.query.QueryResultCacheItem;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.config.Constants;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DataSourceImpl;
import lucee.runtime.db.DataSourceSupport;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.db.DatasourceManagerImpl;
import lucee.runtime.db.HSQLDBHandler;
import lucee.runtime.db.SQL;
import lucee.runtime.db.SQLImpl;
import lucee.runtime.db.SQLItem;
import lucee.runtime.db.SQLItemImpl;
import lucee.runtime.debug.DebuggerImpl;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.CatchBlockImpl;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.BodyTagTryCatchFinallyImpl;
import lucee.runtime.functions.displayFormatting.DecimalFormat;
import lucee.runtime.listener.AppListenerUtil;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.listener.ApplicationContextSupport;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.orm.ORMSession;
import lucee.runtime.orm.ORMUtil;
import lucee.runtime.tag.listener.ComponentTagListener;
import lucee.runtime.tag.listener.TagListener;
import lucee.runtime.tag.listener.UDFTagListener;
import lucee.runtime.tag.query.QueryBean;
import lucee.runtime.tag.query.QuerySpoolerTask;
import lucee.runtime.tag.util.QueryParamConverter;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.QueryColumn;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.dt.TimeSpanImpl;
import lucee.runtime.type.query.QueryArray;
import lucee.runtime.type.query.QueryResult;
import lucee.runtime.type.query.QueryStruct;
import lucee.runtime.type.query.SimpleQuery;
import lucee.runtime.type.scope.Argument;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.util.PageContextUtil;

/**
 * Passes SQL statements to a data source. Not limited to queries.
 **/
public final class Query extends BodyTagTryCatchFinallyImpl {

	private static final Collection.Key SQL_PARAMETERS = KeyImpl.getInstance("sqlparameters");
	private static final Collection.Key CFQUERY = KeyImpl.getInstance("cfquery");
	private static final Collection.Key GENERATEDKEY = KeyImpl.getInstance("generatedKey");
	private static final Collection.Key MAX_RESULTS = KeyImpl.getInstance("maxResults");
	private static final Collection.Key TIMEOUT = KeyConstants._timeout;

	public static final int RETURN_TYPE_UNDEFINED = 0;
	public static final int RETURN_TYPE_QUERY = 1;
	public static final int RETURN_TYPE_ARRAY = 2;
	public static final int RETURN_TYPE_STRUCT = 3;
	public static final int RETURN_TYPE_STORED_PROC = 4;

	public boolean orgPSQ;
	public boolean hasChangedPSQ;

	private QueryBean data = new QueryBean();

	private static class ResMeta {
		public Object res;
		public Object meta;

		public QueryResult asQueryResult() {
			if (res instanceof QueryResult) return (QueryResult) res;
			return null;
		}
	}

	@Override
	public void release() {
		super.release();

		if (data.async) data = new QueryBean();
		else data.release();

		orgPSQ = false;
		hasChangedPSQ = false;
	}

	public void setTags(Object oTags) throws PageException {
		if (StringUtil.isEmpty(oTags)) return;
		// to Array
		Array arr;
		if (Decision.isArray(oTags)) arr = Caster.toArray(oTags);
		else arr = ListUtil.listToArrayRemoveEmpty(Caster.toString(oTags), ',');

		// to String[]
		Iterator<Object> it = arr.valueIterator();
		List<String> list = new ArrayList<String>();
		String str;
		while (it.hasNext()) {
			str = Caster.toString(it.next());
			if (!StringUtil.isEmpty(str)) list.add(str);
		}

		data.tags = list.toArray(new String[list.size()]);
	}

	public void setOrmoptions(Struct ormoptions) {
		data.ormoptions = ormoptions;
	}

	public void setIndexname(String indexName) throws CasterException {
		data.indexName = KeyImpl.toKey(indexName);
	}

	public void setReturntype(String strReturntype) throws ApplicationException {
		if (StringUtil.isEmpty(strReturntype)) return;
		strReturntype = strReturntype.toLowerCase().trim();

		if (strReturntype.equals("query")) data.returntype = RETURN_TYPE_QUERY;
		// mail.setType(lucee.runtime.mail.Mail.TYPE_TEXT);
		else if (strReturntype.equals("struct")) data.returntype = RETURN_TYPE_STRUCT;
		else if (strReturntype.equals("array") || strReturntype.equals("array_of_struct") || strReturntype.equals("array-of-struct") || strReturntype.equals("arrayofstruct")
				|| strReturntype.equals("array_of_entity") || strReturntype.equals("array-of-entity") || strReturntype.equals("arrayofentities")
				|| strReturntype.equals("array_of_entities") || strReturntype.equals("array-of-entities") || strReturntype.equals("arrayofentities"))
			data.returntype = RETURN_TYPE_ARRAY;

		else throw new ApplicationException("attribute returntype of tag query has an invalid value", "valid values are [query,array] but value is now [" + strReturntype + "]");
	}

	public void setUnique(boolean unique) {
		data.unique = unique;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(String result) {
		data.result = result;
	}

	/**
	 * @param psq set preserver single quote
	 */
	public void setPsq(boolean psq) {
		orgPSQ = pageContext.getPsq();
		if (orgPSQ != psq) {
			pageContext.setPsq(psq);
			hasChangedPSQ = true;
		}
	}

	/**
	 * set the value password If specified, password overrides the password value specified in the data
	 * source setup.
	 * 
	 * @param password value to set
	 **/
	public void setPassword(String password) {
		data.password = password;
	}

	/**
	 * set the value datasource The name of the data source from which this query should retrieve data.
	 * 
	 * @param datasource value to set
	 * @throws ClassException
	 * @throws BundleException
	 **/

	public void setDatasource(Object datasource) throws PageException, ClassException, BundleException {
		if (datasource == null) return;
		data.rawDatasource = datasource;
		data.datasource = toDatasource(pageContext, datasource);
	}

	public static DataSource toDatasource(PageContext pageContext, Object datasource) throws PageException {
		if (Decision.isStruct(datasource)) {
			return AppListenerUtil.toDataSource(pageContext.getConfig(), "__temp__", Caster.toStruct(datasource), pageContext.getConfig().getLog("application"));
		}
		else if (Decision.isString(datasource)) {
			return pageContext.getDataSource(Caster.toString(datasource));
		}
		else {
			throw new ApplicationException("attribute [datasource] must be datasource name or a datasource definition(struct)");

		}
	}

	/**
	 * set the value timeout The maximum number of milliseconds for the query to execute before
	 * returning an error indicating that the query has timed-out. This attribute is not supported by
	 * most ODBC drivers. timeout is supported by the SQL Server 6.x or above driver. The minimum and
	 * maximum allowable values vary, depending on the driver.
	 * 
	 * @param timeout value to set
	 * @throws PageException
	 **/
	public void setTimeout(Object timeout) throws PageException {
		if (timeout instanceof TimeSpan) data.timeout = (TimeSpan) timeout;
		// seconds
		else {
			int i = Caster.toIntValue(timeout);
			if (i < 0) throw new ApplicationException("invalid value [" + i + "] for attribute timeout, value must be a positive integer greater or equal than 0");

			data.timeout = new TimeSpanImpl(0, 0, 0, i);
		}
	}

	/**
	 * set the value cachedafter This is the age of which the query data can be
	 * 
	 * @param cachedafter value to set
	 **/
	public void setCachedafter(DateTime cachedafter) {
		data.cachedAfter = cachedafter;
	}

	/**
	 * set the value cachename This is specific to JTags, and allows you to give the cache a specific
	 * name
	 * 
	 * @param cachename value to set
	 **/
	public void setCachename(String cachename) {
		// DeprecatedUtil.tagAttribute(pageContext,"query", "cachename");
		// this.cachename=cachename;
	}

	public void setColumnkey(String columnKey) {
		if (StringUtil.isEmpty(columnKey, true)) return;
		data.columnName = KeyImpl.init(columnKey);
	}

	public void setCachedwithin(Object cachedwithin) {

		if (StringUtil.isEmpty(cachedwithin)) return;

		data.cachedWithin = cachedwithin;
	}

	public void setLazy(boolean lazy) {
		data.lazy = lazy;
	}

	/**
	 * set the value providerdsn Data source name for the COM provider, OLE-DB only.
	 * 
	 * @param providerdsn value to set
	 * @throws ApplicationException
	 **/
	public void setProviderdsn(String providerdsn) throws ApplicationException {
		// DeprecatedUtil.tagAttribute(pageContext,"Query", "providerdsn");
	}

	/**
	 * set the value connectstring
	 * 
	 * @param connectstring value to set
	 * @throws ApplicationException
	 **/
	public void setConnectstring(String connectstring) throws ApplicationException {
		// DeprecatedUtil.tagAttribute(pageContext,"Query", "connectstring");
	}

	public void setTimezone(TimeZone tz) {
		if (tz == null) return;
		data.timezone = tz;
	}

	/**
	 * set the value blockfactor Specifies the maximum number of rows to fetch at a time from the
	 * server. The range is 1, default to 100. This parameter applies to ORACLE native database drivers
	 * and to ODBC drivers. Certain ODBC drivers may dynamically reduce the block factor at runtime.
	 * 
	 * @param blockfactor value to set
	 **/
	public void setBlockfactor(double blockfactor) {
		data.blockfactor = (int) blockfactor;
	}

	/**
	 * set the value dbtype The database driver type.
	 * 
	 * @param dbtype value to set
	 **/
	public void setDbtype(String dbtype) {
		data.dbtype = dbtype.toLowerCase();
	}

	/**
	 * set the value debug Used for debugging queries. Specifying this attribute causes the SQL
	 * statement submitted to the data source and the number of records returned from the query to be
	 * returned.
	 * 
	 * @param debug value to set
	 **/
	public void setDebug(boolean debug) {
		data.debug = debug;
	}

	/**
	 * set the value dbname The database name, Sybase System 11 driver and SQLOLEDB provider only. If
	 * specified, dbName overrides the default database specified in the data source.
	 * 
	 * @param dbname value to set
	 * @throws ApplicationException
	 **/
	public void setDbname(String dbname) {
		// DeprecatedUtil.tagAttribute(pageContext,"Query", "dbname");
	}

	/**
	 * set the value maxrows Specifies the maximum number of rows to return in the record set.
	 * 
	 * @param maxrows value to set
	 **/
	public void setMaxrows(double maxrows) {
		data.maxrows = (int) maxrows;
	}

	/**
	 * set the value username If specified, username overrides the username value specified in the data
	 * source setup.
	 * 
	 * @param username value to set
	 **/
	public void setUsername(String username) {
		if (!StringUtil.isEmpty(username)) data.username = username;
	}

	/**
	 * set the value provider COM provider, OLE-DB only.
	 * 
	 * @param provider value to set
	 * @throws ApplicationException
	 **/
	public void setProvider(String provider) {
		// DeprecatedUtil.tagAttribute(pageContext,"Query", "provider");
	}

	/**
	 * set the value dbserver For native database drivers and the SQLOLEDB provider, specifies the name
	 * of the database server computer. If specified, dbServer overrides the server specified in the
	 * data source.
	 * 
	 * @param dbserver value to set
	 * @throws ApplicationException
	 **/
	public void setDbserver(String dbserver) {
		// DeprecatedUtil.tagAttribute(pageContext,"Query", "dbserver");
	}

	/**
	 * set the value name The name query. Must begin with a letter and may consist of letters, numbers,
	 * and the underscore character, spaces are not allowed. The query name is used later in the page to
	 * reference the query's record set.
	 * 
	 * @param name value to set
	 **/
	public void setName(String name) {
		data.name = name;
	}

	public String getName() {
		return data.name == null ? "query" : data.name;
	}

	private static String getName(QueryBean data) {
		return data.name == null ? "query" : data.name;
	}

	/**
	 * @param item
	 */
	public void setParam(SQLItem item) {
		data.items.add(item);
	}

	public void setParams(Object params) {
		data.params = params;
	}

	public void setNestinglevel(double nestingLevel) {
		data.nestingLevel = (int) nestingLevel;
	}

	public void setListener(Object listener) throws ApplicationException {
		if (listener == null) return;

		data.listener = toTagListener(listener);
	}

	public void setAsync(boolean async) {
		data.async = async;
	}

	public void setSql(String sql) {
		data.sql = sql;
	}

	@Override
	public int doStartTag() throws PageException {

		// default datasource
		if (data.datasource == null && (data.dbtype == null || !data.dbtype.equals("query"))) {
			Object obj = pageContext.getApplicationContext().getDefDataSource();
			if (StringUtil.isEmpty(obj)) {
				boolean isCFML = pageContext.getRequestDialect() == CFMLEngine.DIALECT_CFML;
				throw new ApplicationException("attribute [datasource] is required when attribute [dbtype] is not [query] and no default datasource is defined",
						"you can define a default datasource as attribute [defaultdatasource] of the tag "
								+ (isCFML ? Constants.CFML_APPLICATION_TAG_NAME : Constants.LUCEE_APPLICATION_TAG_NAME) + " or as data member of the "
								+ (isCFML ? Constants.CFML_APPLICATION_EVENT_HANDLER : Constants.LUCEE_APPLICATION_EVENT_HANDLER) + " (this.defaultdatasource=\"mydatasource\";)");
			}
			data.datasource = obj instanceof DataSource ? (DataSource) obj : pageContext.getDataSource(Caster.toString(obj));
		}
		// timeout
		if (data.datasource instanceof DataSourceImpl && ((DataSourceImpl) data.datasource).getAlwaysSetTimeout()) {
			TimeSpan remaining = PageContextUtil.remainingTime(pageContext, true);
			if (data.timeout == null || ((int) data.timeout.getSeconds()) <= 0 || data.timeout.getSeconds() > remaining.getSeconds()) { // not set
				data.timeout = remaining;
			}
		}

		// timezone
		if (data.timezone != null || (data.datasource != null && (data.timezone = data.datasource.getTimeZone()) != null)) {
			data.tmpTZ = pageContext.getTimeZone();
			pageContext.setTimeZone(data.timezone);
		}

		PageContextImpl pci = ((PageContextImpl) pageContext);

		// cache within
		if (StringUtil.isEmpty(data.cachedWithin)) {

			Object tmp = (pageContext).getCachedWithin(ConfigWeb.CACHEDWITHIN_QUERY);
			if (tmp != null) setCachedwithin(tmp);
		}

		// literal timestamp with TSOffset
		if (data.datasource instanceof DataSourceSupport) data.literalTimestampWithTSOffset = ((DataSourceSupport) data.datasource).getLiteralTimestampWithTSOffset();
		else data.literalTimestampWithTSOffset = false;

		data.previousLiteralTimestampWithTSOffset = pci.getTimestampWithTSOffset();
		pci.setTimestampWithTSOffset(data.literalTimestampWithTSOffset);

		return EVAL_BODY_BUFFERED;
	}

	@Override
	public int doEndTag() throws PageException {
		if (hasChangedPSQ) pageContext.setPsq(orgPSQ);

		// listener
		if (data.listener == null && data.datasource != null) { // if no datasource we have dbtype query, otherwise we
			// would have an exception in doStartTag
			// does the datasource define a listener?
			TagListener listener = ((DataSourceSupport) data.datasource).getListener();
			if (listener != null) {
				data.listener = listener;
			}
			else {
				ApplicationContext ac = pageContext.getApplicationContext();
				if (ac instanceof ApplicationContextSupport) {
					ApplicationContextSupport acs = (ApplicationContextSupport) ac;
					listener = acs.getQueryListener();
					if (listener != null) {
						data.listener = listener;
					}
				}
			}
		}

		String strSQL;
		if (data.hasBody && !StringUtil.isEmpty(strSQL = bodyContent.getString().trim(), true)) { // we have a body
			if (!StringUtil.isEmpty(data.sql, true)) { // sql in attr and body
				if (!strSQL.equals(data.sql.trim())) // unless they are equal
					throw new DatabaseException("you cannot define SQL in the body and as an attribute at the same time [" + strSQL + "," + data.sql + "]", null, null, null);
			}
		}
		else {
			if (StringUtil.isEmpty(data.sql, true))
				throw new DatabaseException("the required sql string is not defined in the body of the query tag, and not in a sql attribute", null, null, null);
			strSQL = data.sql.trim();
		}

		if (!data.items.isEmpty() && data.params != null)
			throw new DatabaseException("you cannot use the attribute params and sub tags queryparam at the same time", null, null, null);

		if (data.async) {
			PageSource ps = getPageSource();
			((ConfigPro) pageContext.getConfig()).getSpoolerEngine()
					.add(new QuerySpoolerTask(pageContext, data, strSQL, toTemplateLine(pageContext.getConfig(), sourceTemplate, ps), ps));
		}
		else {
			_doEndTag(pageContext, data, strSQL, toTemplateLine(pageContext.getConfig(), sourceTemplate, getPageSource()), true); // when
			// sourceTemplate
			// exists
			// getPageSource
			// call was not
			// necessary
		}
		return EVAL_PAGE;
	}

	public static int _doEndTag(PageContext pageContext, QueryBean data, String strSQL, TemplateLine tl, boolean setVars) throws PageException {

		// listener before
		if (data.listener != null) {
			String res = writeBackArgs(pageContext, data, data.listener.before(pageContext, createArgStruct(data, strSQL, tl)));
			if (!StringUtil.isEmpty(res)) strSQL = res;
		}

		SQL sqlQuery = null;
		long exe = 0;
		try {
			// cannot use attribute params and queryparam tag

			// create SQL
			if (data.params != null) {
				if (data.params instanceof Argument) sqlQuery = QueryParamConverter.convert(strSQL, (Argument) data.params);
				else if (Decision.isArray(data.params)) sqlQuery = QueryParamConverter.convert(strSQL, Caster.toArray(data.params));
				else if (Decision.isStruct(data.params)) sqlQuery = QueryParamConverter.convert(strSQL, Caster.toStruct(data.params));
				else throw new DatabaseException("value of the attribute [params] has to be a struct or an array", null, null, null);
			}
			else {
				sqlQuery = data.items.isEmpty() ? new SQLImpl(strSQL) : new SQLImpl(strSQL, data.items.toArray(new SQLItem[data.items.size()]));
			}
			validate(sqlQuery);

			// lucee.runtime.type.Query query=null;
			QueryResult queryResult = null;
			String cacheHandlerId = null;
			String cacheId = null;

			final long now = System.currentTimeMillis();

			if (data.cachedAfter != null) {
				// not yet
				if (data.cachedAfter.getTime() > now) data.cachedWithin = null;
				// no time range set
				else if (data.cachedWithin == null) {
					data.cachedWithin = ((PageContextImpl) pageContext).getCachedAfterTimeRange();
				}
			}

			boolean useCache = (data.cachedWithin != null) || (data.cachedAfter != null);
			CacheHandler cacheHandler = null;

			if (useCache) {

				cacheId = CacheHandlerCollectionImpl.createId(sqlQuery, data.datasource != null ? data.datasource.getName() : null, data.username, data.password, data.returntype);

				CacheHandlerCollectionImpl coll = (CacheHandlerCollectionImpl) pageContext.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_QUERY, null);
				cacheHandler = coll.getInstanceMatchingObject(data.cachedWithin, null);

				if (cacheHandler == null && data.cachedAfter != null) cacheHandler = coll.getTimespanInstance(null);

				if (cacheHandler != null) {
					cacheHandlerId = cacheHandler.id(); // cacheHandlerId specifies to queryResult the cacheType and
					// therefore whether the query is cached or not
					if (cacheHandler instanceof CacheHandlerPro) {
						CacheItem cacheItem;
						try {
							cacheItem = ((CacheHandlerPro) cacheHandler).get(pageContext, cacheId, (data.cachedWithin != null) ? data.cachedWithin : data.cachedAfter);
						}
						catch (PageException pe) {
							cacheItem = null;
							LogUtil.log(pageContext.getConfig(), "query", pe);
						}
						if (cacheItem instanceof QueryResultCacheItem) queryResult = ((QueryResultCacheItem) cacheItem).getQueryResult();
					}
					else { // FUTURE this else block can be removed when all cache handlers implement
							// CacheHandlerPro
						CacheItem cacheItem;
						try {
							cacheItem = cacheHandler.get(pageContext, cacheId);
						}
						catch (PageException pe) {
							cacheItem = null;
							LogUtil.log(pageContext.getConfig(), "query", pe);
						}

						if (cacheItem instanceof QueryResultCacheItem) {

							QueryResultCacheItem queryCachedItem = (QueryResultCacheItem) cacheItem;

							Date cacheLimit = data.cachedAfter;
							/*
							 * if(cacheLimit == null && cacheHandler in) { TimeSpan ts = Caster.toTimespan(cachedWithin,null);
							 * cacheLimit = new Date(System.currentTimeMillis() - Caster.toTimeSpan(cachedWithin).getMillis());
							 * }
							 */

							if (cacheLimit == null || queryCachedItem.isCachedAfter(cacheLimit)) queryResult = queryCachedItem.getQueryResult();
						}
					}
				}
				else {
					List<String> patterns = pageContext.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_QUERY, null).getPatterns();
					throw new ApplicationException(
							"cachedwithin value [" + data.cachedWithin + "] is invalid, valid values are for example [" + ListUtil.listToList(patterns, ", ") + "]");
				}
				// query=pageContext.getQueryCache().getQuery(pageContext,sql,datasource!=null?datasource.getName():null,username,password,cachedafter);
			}

			// cache not found, process and cache result if needed
			if (queryResult == null) {
				// QoQ
				if ("query".equals(data.dbtype)) {
					QueryImpl q = executeQoQ(pageContext, data, sqlQuery, tl);
					q.setTemplateLine(tl);
					if (data.returntype == RETURN_TYPE_ARRAY) queryResult = QueryArray.toQueryArray(q); // TODO this should be done in queryExecute
																										// itself so
					// we not have to convert afterwards
					else if (data.returntype == RETURN_TYPE_STRUCT) {
						if (data.columnName == null) throw new ApplicationException("attribute columnKey is required when return type is set to struct");
						queryResult = QueryStruct.toQueryStruct(q, data.columnName); // TODO this should be done in
						// queryExecute itself so we not
						// have to convert // afterwards
					}
					else queryResult = q;
				}
				// ORM and Datasource
				else {
					long start = System.nanoTime();
					Object obj;

					if ("orm".equals(data.dbtype) || "hql".equals(data.dbtype)) obj = executeORM(pageContext, data, sqlQuery, data.returntype, data.ormoptions);
					else obj = executeDatasoure(pageContext, data, sqlQuery, data.result != null, pageContext.getTimeZone(), tl);

					if (obj instanceof QueryResult) {
						queryResult = (QueryResult) obj;
					}
					else {
						if (data.setReturnVariable) {
							data.rtn = obj;
						}
						else if (!StringUtil.isEmpty(data.name)) {
							if (setVars) pageContext.setVariable(data.name, obj);
						}
						if (data.result != null) {
							long time = System.nanoTime() - start;
							Struct sct = new StructImpl();
							sct.setEL(KeyConstants._cached, Boolean.FALSE);
							sct.setEL(KeyConstants._executionTime, Caster.toDouble(time / 1000000));
							sct.setEL(KeyConstants._executionTimeNano, Caster.toDouble(time));
							sct.setEL(KeyConstants._SQL, sqlQuery.getSQLString());

							if (!Decision.isArray(obj)) sct.setEL(KeyConstants._RECORDCOUNT, Caster.toDouble(1));

							if (setVars) pageContext.setVariable(data.result, sct);
						}
						else setExecutionTime(pageContext, (System.nanoTime() - start) / 1000000);

						return EVAL_PAGE;
					}
				}
				// else query=executeDatasoure(sql,result!=null,pageContext.getTimeZone());

				if (data.cachedWithin != null && (data.cachedAfter == null || data.cachedAfter.getTime() <= now)) {
					CacheItem cacheItem = QueryResultCacheItem.newInstance(queryResult, data.tags, data.datasource, null);
					if (cacheItem != null) {
						try {
							cacheHandler.set(pageContext, cacheId, data.cachedWithin, cacheItem);
						}
						catch (PageException pe) {
							LogUtil.log(pageContext.getConfig(), "query", pe);
						}
					}
				}
				exe = queryResult.getExecutionTime();
			}
			else {
				queryResult.setCacheType(cacheHandlerId);
			}

			if (pageContext.getConfig().debug() && data.debug) {
				DebuggerImpl di = (DebuggerImpl) pageContext.getDebugger();
				boolean logdb = ((ConfigPro) pageContext.getConfig()).hasDebugOptions(ConfigPro.DEBUG_DATABASE);
				if (logdb) {
					boolean debugUsage = DebuggerImpl.debugQueryUsage(pageContext, queryResult);
					di.addQuery(debugUsage ? queryResult : null, data.datasource != null ? data.datasource.getName() : null, data.name, sqlQuery, queryResult.getRecordcount(), tl,
							exe);
				}
				else {
					di.addQuery(exe);
				}
			}
			boolean setResult = false;
			if (data.setReturnVariable) {
				data.rtn = queryResult;
			}
			else if ((queryResult.getColumncount() + queryResult.getRecordcount()) > 0 && !StringUtil.isEmpty(data.name)) {
				if (setVars) pageContext.setVariable(data.name, queryResult);
				setResult = true;
			}

			// Result
			Struct meta = createMetaData(pageContext, data, queryResult, sqlQuery, setVars, exe);

			// listener
			((ConfigWebPro) pageContext.getConfig()).getActionMonitorCollector().log(pageContext, "query", "Query", exe, queryResult);
			if (data.listener != null) {
				callAfter(pageContext, data, strSQL, tl, setResult, queryResult, meta, setVars);
			}

			// log
			Log log = pageContext.getConfig().getLog("datasource");
			if (log.getLogLevel() >= Log.LEVEL_INFO) {
				log.info("query tag", "executed [" + sqlQuery.toString().trim() + "] in " + DecimalFormat.call(pageContext, exe / 1000000D) + " ms");
			}
		}
		catch (PageException pe) {
			if (data.listener != null && data.listener.hasError()) {
				long addExe = System.nanoTime();
				Struct args = createArgStruct(data, strSQL, tl);
				args.set(KeyConstants._exception, new CatchBlockImpl(pe));
				ResMeta rm = writeBackResult(pageContext, data, data.listener.error(pageContext, args), setVars);
				if (data.result == null || (rm.meta == null && rm.asQueryResult() != null))
					rm.meta = createMetaData(pageContext, data, rm.asQueryResult(), null, setVars, exe + (System.nanoTime() - addExe));
				callAfter(pageContext, data, strSQL, tl, true, rm.res, rm.meta, setVars);
			}
			else throw pe;
		}
		finally {
			((PageContextImpl) pageContext).setTimestampWithTSOffset(data.previousLiteralTimestampWithTSOffset);
			if (data.tmpTZ != null) {
				pageContext.setTimeZone(data.tmpTZ);
			}
		}
		return EVAL_PAGE;
	}

	private static void validate(SQL sql) throws PageException {
		SQLItem[] items = sql.getItems();
		if (items == null) return;
		for (SQLItem item: items) {
			SQLItemImpl _item = (SQLItemImpl) item;
			QueryParam.check(item.getValue(), item.getType(), _item.getMaxlength(), _item.getCharset());
		}
	}

	private static Struct createMetaData(PageContext pageContext, QueryBean data, QueryResult queryResult, SQL sqlQuery, boolean setVars, long exe) throws PageException {
		Struct meta;
		if (data.result != null && queryResult != null) {
			meta = new StructImpl();
			meta.setEL(KeyConstants._cached, Caster.toBoolean(queryResult.isCached()));
			if ((queryResult.getColumncount() + queryResult.getRecordcount()) > 0) {
				String list = ListUtil.arrayToList(queryResult instanceof lucee.runtime.type.Query ? ((lucee.runtime.type.Query) queryResult).getColumnNamesAsString()
						: CollectionUtil.toString(queryResult.getColumnNames(), false), ",");
				meta.setEL(KeyConstants._COLUMNLIST, list);
			}
			int rc = queryResult.getRecordcount();
			if (rc == 0) rc = queryResult.getUpdateCount();
			meta.setEL(KeyConstants._RECORDCOUNT, Caster.toDouble(rc));
			meta.setEL(KeyConstants._executionTime, Caster.toDouble(queryResult.getExecutionTime() / 1000000));
			meta.setEL(KeyConstants._executionTimeNano, Caster.toDouble(queryResult.getExecutionTime()));

			if (sqlQuery != null) meta.setEL(KeyConstants._SQL, sqlQuery.getSQLString());

			// GENERATED KEYS
			lucee.runtime.type.Query qi = Caster.toQuery(queryResult, null);
			if (qi != null) {
				lucee.runtime.type.Query qryKeys = qi.getGeneratedKeys();
				if (qryKeys != null) {
					StringBuilder generatedKey = new StringBuilder(), sb;
					Collection.Key[] columnNames = qryKeys.getColumnNames();
					QueryColumn column;
					for (int c = 0; c < columnNames.length; c++) {
						column = qryKeys.getColumn(columnNames[c]);
						sb = new StringBuilder();
						int size = column.size();
						for (int row = 1; row <= size; row++) {
							if (row > 1) sb.append(',');
							sb.append(Caster.toString(column.get(row, null)));
						}
						if (sb.length() > 0) {
							meta.setEL(columnNames[c], sb.toString());
							if (generatedKey.length() > 0) generatedKey.append(',');
							generatedKey.append(sb);
						}
					}
					if (generatedKey.length() > 0) meta.setEL(GENERATEDKEY, generatedKey.toString());
				}
			}

			// sqlparameters
			if (sqlQuery != null) {
				SQLItem[] params = sqlQuery.getItems();
				if (params != null && params.length > 0) {
					Array arr = new ArrayImpl();
					meta.setEL(SQL_PARAMETERS, arr);
					for (int i = 0; i < params.length; i++) {
						arr.append(params[i].getValue());
					}
				}
			}

			if (setVars) pageContext.setVariable(data.result, meta);
		}
		// cfquery.executiontime
		else {
			meta = setExecutionTime(pageContext, exe / 1000000);
		}
		return meta;
	}

	private static void callAfter(PageContext pc, QueryBean data, String strSQL, TemplateLine tl, boolean setResult, Object queryResult, Object meta, boolean setVars)
			throws PageException {
		Struct args = createArgStruct(data, strSQL, tl);
		if (setResult && queryResult != null) args.set(KeyConstants._result, queryResult);
		if (meta != null) args.set(KeyConstants._meta, meta);
		writeBackResult(pc, data, data.listener.after(pc, args), setVars);
	}

	private static Struct createArgStruct(QueryBean data, String strSQL, TemplateLine tl) throws PageException {
		Struct rtn = new StructImpl(Struct.TYPE_LINKED);
		Struct args = new StructImpl(Struct.TYPE_LINKED);

		// TODO add missing attrs
		/*
		 * TagLibTag tlt = TagUtil.getTagLibTag(pageContext, CFMLEngine.DIALECT_CFML, "cf", "query");
		 * Iterator<Entry<String, TagLibTagAttr>> it = tlt.getAttributes().entrySet().iterator();
		 * Entry<String, TagLibTagAttr> e; while(it.hasNext()) { e=it.next(); e.getValue().get(this); }
		 */

		set(args, "cachedAfter", data.cachedAfter);
		set(args, "cachedWithin", data.cachedWithin);
		if (data.columnName != null) set(args, "columnName", data.columnName.getString());
		set(args, KeyConstants._datasource, data.rawDatasource);
		set(args, "dbtype", data.dbtype);
		set(args, KeyConstants._debug, data.debug);
		set(args, "lazy", data.lazy);
		if (data.maxrows >= 0) set(args, "maxrows", data.maxrows);
		set(args, KeyConstants._name, data.name);
		set(args, "ormoptions", data.ormoptions);
		set(args, KeyConstants._username, data.username);
		set(args, KeyConstants._password, data.password);
		set(args, KeyConstants._result, data.result);
		set(args, KeyConstants._returntype, data.returntype);
		set(args, KeyConstants._timeout, data.timeout);
		set(args, KeyConstants._timezone, data.timezone);
		set(args, "unique", data.unique);
		set(args, KeyConstants._sql, strSQL);
		rtn.setEL(KeyConstants._args, args);

		// params
		if (data.params != null) {
			set(args, "params", data.params);
		}
		else if (data.items != null) {
			Array params = new ArrayImpl();
			Iterator<SQLItem> it = data.items.iterator();
			SQLItem item;
			while (it.hasNext()) {
				item = it.next();
				params.appendEL(QueryParamConverter.toStruct(item));
			}
			set(args, KeyConstants._params, params);
		}

		rtn.setEL(KeyConstants._caller, tl.toStruct());

		return rtn;
	}

	private static String writeBackArgs(PageContext pageContext, QueryBean data, Struct args) throws PageException {
		if (args == null) return null;

		// maybe they send the hole arguments scope, we handle this here
		if (args.size() == 2 && args.containsKey("caller") && args.containsKey("args")) args = Caster.toStruct(args.get("args"));

		// cachedAfter
		DateTime dt = Caster.toDate(args.get("cachedAfter", null), true, pageContext.getTimeZone(), null);
		if (dt != null && dt != data.cachedAfter) data.cachedAfter = dt;

		// cachedWithin
		Object obj = args.get("cachedWithin", null);
		if (obj != null && obj != data.cachedWithin) data.cachedWithin = obj;

		// columnName
		Key k = Caster.toKey(args.get("columnName", null), null);
		if (k != null && k != data.columnName) data.columnName = k;

		// datasource
		obj = args.get("datasource", null);
		if (obj != null && obj != data.datasource) {
			data.rawDatasource = obj;
			data.datasource = toDatasource(pageContext, obj);
		}

		// dbtype
		String str = Caster.toString(args.get("dbtype", null), null);
		if (str != null && str != data.dbtype && !StringUtil.isEmpty(str)) data.dbtype = str;

		// debug
		Boolean b = Caster.toBoolean(args.get("debug", null), null);
		if (b != null && b != data.debug) data.debug = b.booleanValue();

		// lazy
		b = Caster.toBoolean(args.get("lazy", null), null);
		if (b != null && b != data.lazy) data.lazy = b.booleanValue();

		// maxrows
		Integer i = Caster.toInteger(args.get("maxrows", null), null);
		if (i != null && i != data.maxrows) {
			if (i.intValue() >= 0) data.maxrows = i.intValue();
		}

		// name
		str = Caster.toString(args.get("name", null), null);
		if (str != null && str != data.name && !str.equals(data.name) && !StringUtil.isEmpty(str)) data.name = str;

		// ormoptions
		Struct sct = Caster.toStruct(args.get("ormoptions", null), null);
		if (sct != null && sct != data.ormoptions) data.ormoptions = sct;

		// username
		str = Caster.toString(args.get("username", null), null);
		if (str != null && str != data.username && !StringUtil.isEmpty(str)) data.username = str;

		// password
		str = Caster.toString(args.get("password", null), null);
		if (str != null && str != data.password && !StringUtil.isEmpty(str)) data.password = str;

		// result
		str = Caster.toString(args.get("result", null), null);
		if (str != null && str != data.result && !StringUtil.isEmpty(str)) data.result = str;

		// returntype
		i = Caster.toInteger(args.get("returntype", null), null);
		if (i != null && i != data.returntype) data.returntype = i.intValue();

		// timeout
		TimeSpan ts = Caster.toTimespan(args.get("timeout", null), null);
		if (ts != null && ts != data.timeout) data.timeout = ts;

		// timezone
		TimeZone tz = Caster.toTimeZone(args.get("timezone", null), null);
		if (tz != null && tz != data.timeout) data.timezone = tz;

		// params
		obj = args.get("params", null);
		if (obj != null && obj != data.params) {
			data.params = obj;
			data.items.clear();
		}

		// sql
		String sql = null;
		str = Caster.toString(args.get("sql", null), null);
		if (str != null && !StringUtil.isEmpty(str)) sql = str;
		return sql;
	}

	private static ResMeta writeBackResult(PageContext pageContext, QueryBean data, Struct args, boolean setVars) throws PageException {
		ResMeta rm = new ResMeta();
		if (args == null) return rm;

		// result
		rm.res = args.get(KeyConstants._result, null);
		if (rm.res != null) {
			if (!StringUtil.isEmpty(data.name) && setVars) pageContext.setVariable(data.name, rm.res);
		}
		// meta
		rm.meta = args.get(KeyConstants._meta, null);
		if (rm.meta != null) {
			if (StringUtil.isEmpty(data.result)) pageContext.undefinedScope().setEL(CFQUERY, rm.meta);
			else {
				if (setVars) pageContext.setVariable(data.result, rm.meta);
			}
		}
		return rm;
	}

	private static void set(Struct args, String name, Object value) throws PageException {
		if (value != null) args.set(name, value);
	}

	private static void set(Struct args, Key name, Object value) throws PageException {
		if (value != null) args.set(name, value);
	}

	private PageSource getPageSource() {
		if (data.nestingLevel > 0) {
			PageContextImpl pci = (PageContextImpl) pageContext;
			List<PageSource> list = pci.getPageSourceList();
			int index = list.size() - 1 - data.nestingLevel;
			if (index >= 0) return list.get(index);
		}
		return pageContext.getCurrentPageSource();
	}

	private static Struct setExecutionTime(PageContext pc, long exe) {
		Struct sct = new StructImpl();
		sct.setEL(KeyConstants._executionTime, new Double(exe));
		pc.undefinedScope().setEL(CFQUERY, sct);
		return sct;
	}

	private static Object executeORM(PageContext pageContext, QueryBean data, SQL sql, int returnType, Struct ormoptions) throws PageException {
		ORMSession session = ORMUtil.getSession(pageContext);
		if (ormoptions == null) ormoptions = new StructImpl();
		String dsn = null;
		if (ormoptions != null) dsn = Caster.toString(ormoptions.get(KeyConstants._datasource, null), null);
		if (StringUtil.isEmpty(dsn, true)) dsn = ORMUtil.getDefaultDataSource(pageContext).getName();

		// params
		SQLItem[] _items = sql.getItems();
		Array params = new ArrayImpl();
		for (int i = 0; i < _items.length; i++) {
			params.appendEL(_items[i]);
		}

		// query options
		if (data.maxrows != -1 && !ormoptions.containsKey(MAX_RESULTS)) ormoptions.setEL(MAX_RESULTS, new Double(data.maxrows));
		if (data.timeout != null && ((int) data.timeout.getSeconds()) > 0 && !ormoptions.containsKey(TIMEOUT)) ormoptions.setEL(TIMEOUT, new Double(data.timeout.getSeconds()));
		/*
		 * MUST offset: Specifies the start index of the resultset from where it has to start the retrieval.
		 * cacheable: Whether the result of this query is to be cached in the secondary cache. Default is
		 * false. cachename: Name of the cache in secondary cache.
		 */
		Object res = session.executeQuery(pageContext, dsn, sql.getSQLString(), params, data.unique, ormoptions);
		if (returnType == RETURN_TYPE_ARRAY || returnType == RETURN_TYPE_UNDEFINED) return res;
		return session.toQuery(pageContext, res, null);

	}

	public static Object _call(PageContext pc, String hql, Object params, boolean unique, Struct queryOptions) throws PageException {
		ORMSession session = ORMUtil.getSession(pc);
		String dsn = Caster.toString(queryOptions.get(KeyConstants._datasource, null), null);
		if (StringUtil.isEmpty(dsn, true)) dsn = ORMUtil.getDefaultDataSource(pc).getName();

		if (Decision.isCastableToArray(params)) return session.executeQuery(pc, dsn, hql, Caster.toArray(params), unique, queryOptions);
		else if (Decision.isCastableToStruct(params)) return session.executeQuery(pc, dsn, hql, Caster.toStruct(params), unique, queryOptions);
		else return session.executeQuery(pc, dsn, hql, (Array) params, unique, queryOptions);
	}

	private static lucee.runtime.type.QueryImpl executeQoQ(PageContext pc, QueryBean data, SQL sql, TemplateLine tl) throws PageException {
		try {
			return new HSQLDBHandler().execute(pc, sql, data.maxrows, data.blockfactor, data.timeout);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private static QueryResult executeDatasoure(PageContext pageContext, QueryBean data, SQL sql, boolean createUpdateData, TimeZone tz, TemplateLine tl) throws PageException {
		DatasourceManagerImpl manager = (DatasourceManagerImpl) pageContext.getDataSourceManager();
		DatasourceConnection dc = manager.getConnection(pageContext, data.datasource, data.username, data.password);

		try {
			if (data.lazy && !createUpdateData && data.cachedWithin == null && data.cachedAfter == null && data.result == null) {
				if (data.returntype != RETURN_TYPE_QUERY && data.returntype != RETURN_TYPE_UNDEFINED)

					throw new DatabaseException("only return type query is allowed when lazy is set to true", null, sql, dc);

				return new SimpleQuery(pageContext, dc, sql, data.maxrows, data.blockfactor, data.timeout, getName(data), tl, tz);
			}
			if (data.returntype == RETURN_TYPE_ARRAY)
				return QueryImpl.toArray(pageContext, dc, sql, data.maxrows, data.blockfactor, data.timeout, getName(data), tl, createUpdateData, true);
			if (data.returntype == RETURN_TYPE_STRUCT) {
				if (data.columnName == null) throw new ApplicationException("attribute columnKey is required when return type is set to struct");

				return QueryImpl.toStruct(pageContext, dc, sql, data.columnName, data.maxrows, data.blockfactor, data.timeout, getName(data), tl, createUpdateData, true);
			}
			return new QueryImpl(pageContext, dc, sql, data.maxrows, data.blockfactor, data.timeout, getName(data), tl, createUpdateData, true, data.indexName);
		}
		finally {
			manager.releaseConnection(pageContext, dc);
		}
	}

	@Override
	public void doInitBody() {

	}

	@Override
	public int doAfterBody() {
		return SKIP_BODY;
	}

	public void setReturnVariable(boolean setReturnVariable) {
		data.setReturnVariable = setReturnVariable;

	}

	public Object getReturnVariable() {
		return data.rtn;

	}

	/**
	 * sets if tag has a body or not
	 * 
	 * @param hasBody
	 */
	public void hasBody(boolean hasBody) {
		data.hasBody = hasBody;
	}

	public static TagListener toTagListener(Object listener, TagListener defaultValue) {
		if (listener instanceof TagListener) return (TagListener) listener;
		if (listener instanceof Component) return new ComponentTagListener((Component) listener);

		if (listener instanceof UDF) return new UDFTagListener(null, (UDF) listener, null);

		if (listener instanceof Struct) {
			UDF before = Caster.toFunction(((Struct) listener).get("before", null), null);
			UDF after = Caster.toFunction(((Struct) listener).get("after", null), null);
			UDF error = Caster.toFunction(((Struct) listener).get("error", null), null);
			return new UDFTagListener(before, after, error);
		}
		return defaultValue;
	}

	public static TemplateLine toTemplateLine(Config config, String sourceTemplate, PageSource ps) {
		if (!StringUtil.isEmpty(sourceTemplate)) {
			return new TemplateLine(sourceTemplate);
		}

		if (config.debug()) return SystemUtil.getCurrentContext(null);
		return new TemplateLine(ps.getDisplayPath());
	}

	public static TagListener toTagListener(Object listener) throws ApplicationException {
		TagListener ql = toTagListener(listener, null);
		if (ql != null) return ql;
		throw new ApplicationException("cannot convert [" + Caster.toTypeName(listener) + "] to a listener");
	}
}