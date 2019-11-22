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

import java.lang.ref.SoftReference;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.servlet.jsp.JspException;

import lucee.commons.io.IOUtil;
import lucee.commons.io.log.Log;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.sql.SQLUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.PageContextImpl;
import lucee.runtime.cache.tag.CacheHandler;
import lucee.runtime.cache.tag.CacheHandlerCollectionImpl;
import lucee.runtime.cache.tag.CacheHandlerPro;
import lucee.runtime.cache.tag.CacheItem;
import lucee.runtime.cache.tag.query.StoredProcCacheItem;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.Constants;
import lucee.runtime.db.CFTypes;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DataSourceManager;
import lucee.runtime.db.DataSourceSupport;
import lucee.runtime.db.DataSourceUtil;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.db.ProcMeta;
import lucee.runtime.db.ProcMetaCollection;
import lucee.runtime.db.SQLCaster;
import lucee.runtime.db.SQLImpl;
import lucee.runtime.db.SQLItemImpl;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.BodyTagTryCatchFinallySupport;
import lucee.runtime.functions.displayFormatting.DecimalFormat;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.util.KeyConstants;

public class StoredProc extends BodyTagTryCatchFinallySupport {
	// private static final int PROCEDURE_CAT=1;
	// private static final int PROCEDURE_SCHEM=2;
	// private static final int PROCEDURE_NAME=3;
	// private static final int COLUMN_NAME=4;
	private static final int COLUMN_TYPE = 5;
	private static final int DATA_TYPE = 6;
	private static final int TYPE_NAME = 7;
	// |PRECISION|LENGTH|SCALE|RADIX|NULLABLE|REMARKS|SEQUENCE|OVERLOAD|DEFAULT_VALUE

	private static final lucee.runtime.type.Collection.Key KEY_SC = KeyImpl.intern("StatusCode");

	private static final lucee.runtime.type.Collection.Key COUNT = KeyImpl.intern("count_afsdsfgdfgdsfsdfsgsdgsgsdgsasegfwef");

	private static final ProcParamBean STATUS_CODE;
	private static final lucee.runtime.type.Collection.Key STATUSCODE = KeyImpl.intern("StatusCode");

	static {
		STATUS_CODE = new ProcParamBean();
		STATUS_CODE.setType(Types.INTEGER);
		STATUS_CODE.setDirection(ProcParamBean.DIRECTION_OUT);
		STATUS_CODE.setVariable("cfstoredproc.statusCode");
	}

	private List<ProcParamBean> params = new ArrayList<ProcParamBean>();
	private Array results = new ArrayImpl();

	private String procedure;
	private String datasource = null;
	private String username;
	private String password;
	private int blockfactor = -1;
	private int timeout = -1;
	private boolean debug = true;
	private boolean returncode;
	private String result = "cfstoredproc";

	private DateTime cachedafter;
	private ProcParamBean returnValue = null;
	private Object cachedWithin;
	// private Map<String,ProcMetaCollection> procedureColumnCache;

	@Override
	public void release() {
		params.clear();
		results.clear();
		returnValue = null;
		procedure = null;
		datasource = null;
		username = null;
		password = null;
		blockfactor = -1;
		timeout = -1;
		debug = true;
		returncode = false;
		result = "cfstoredproc";

		cachedWithin = null;
		cachedafter = null;
		// cachename="";

		super.release();
	}

	/**
	 * set the value cachedafter This is the age of which the query data can be
	 *
	 * @param cachedafter
	 *            value to set
	 **/
	public void setCachedafter(DateTime cachedafter) {
		// lucee.print.ln("cachedafter:"+cachedafter);
		this.cachedafter = cachedafter;
	}

	/**
	 * set the value cachename This is specific to JTags, and allows you to give the cache a specific name
	 *
	 * @param cachename
	 *            value to set
	 **/
	public void setCachename(String cachename) {
		// DeprecatedUtil.tagAttribute(pageContext,"StoredProc", "cachename");
	}

	/**
	 * set the value cachedwithin
	 *
	 * @param cachedwithin
	 *            value to set
	 **/
	public void setCachedwithin(Object cachedwithin) {
		if(StringUtil.isEmpty(cachedwithin))
			return;
		this.cachedWithin = cachedwithin;
	}

	/**
	 * @param blockfactor
	 *            The blockfactor to set.
	 */
	public void setBlockfactor(double blockfactor) {
		this.blockfactor = (int)blockfactor;
	}

	/**
	 * @param blockfactor
	 * @deprecated replaced with setBlockfactor(double)
	 */
	@Deprecated
	public void setBlockfactor(int blockfactor) {
		// DeprecatedUtil.tagAttribute(pageContext,"storedproc","blockfactor");
		this.blockfactor = blockfactor;
	}

	/**
	 * @param datasource
	 *            The datasource to set.
	 */
	public void setDatasource(String datasource) {
		this.datasource = datasource;
	}

	/**
	 * @param username
	 *            The username to set.
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @param password
	 *            The password to set.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @param debug
	 *            The debug to set.
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * @param procedure
	 *            The procedure to set.
	 */
	public void setProcedure(String procedure) {
		this.procedure = procedure;
	}

	/**
	 * @param result
	 *            The result to set.
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * @param returncode
	 *            The returncode to set.
	 */
	public void setReturncode(boolean returncode) {
		this.returncode = returncode;
	}

	/**
	 * @param dbvarname
	 *            the dbvarname to set
	 */
	public void setDbvarname(String dbvarname) {
		// DeprecatedUtil.tagAttribute(pageContext,"storedproc","dbvarname");
	}

	public void setDbtype(String dbtype) {
		// DeprecatedUtil.tagAttribute(pageContext,"storedproc","dbtype");
	}

	public void addProcParam(ProcParamBean param) {
		Log log = pageContext.getConfig().getLog("datasource");
		if(log.getLogLevel() >= Log.LEVEL_DEBUG)	// log entry added to troubleshoot LDEV-1147
			log.debug("LDEV1147", String.format("  param %s = %s", param.getVariable(), param.getValue()));

		params.add(param);
	}

	public void addProcResult(ProcResultBean result) {
		results.setEL(result.getResultset(), result);
	}

	@Override
	public int doStartTag() throws JspException {

		// cache within
		if(StringUtil.isEmpty(cachedWithin)) {
			Object tmp = ((PageContextImpl)pageContext).getCachedWithin(ConfigWeb.CACHEDWITHIN_QUERY);
			if(tmp != null)
				setCachedwithin(tmp);
		}

		return EVAL_BODY_INCLUDE;
	}

	private void returnValue(DatasourceConnection dc) throws PageException {

		Connection conn = dc.getConnection();

		if(SQLUtil.isOracle(conn)) {
			String name = this.procedure.toUpperCase().trim();

			// split procedure definition
			String catalog = null, schema = null;
			{
				int index = name.lastIndexOf('.');
				if(index != -1) {
					catalog = name.substring(0, index).trim();
					name = name.substring(index + 1).trim();

					index = catalog.lastIndexOf('.');
					if(index != -1) {
						schema = catalog.substring(0, index).trim();
						catalog = catalog.substring(index + 1).trim();
						// schema=catalog.substring(index+1);
						// catalog=catalog.substring(0,index);
					}
				}
				if(StringUtil.isEmpty(schema))
					schema = null;
				if(StringUtil.isEmpty(catalog))
					catalog = null;
			}

			try {
				// if(procParamsCache==null)procParamsCache=new ReferenceMap();
				// ProcMetaCollection procParams=procParamsCache.get(procedure);
				DataSourceSupport ds = ((DataSourceSupport)dc.getDatasource());
				long cacheTimeout = ds.getMetaCacheTimeout();
				Map<String, ProcMetaCollection> procParamsCache = ds.getProcedureColumnCache();
				String cacheId = procedure.toLowerCase();
				ProcMetaCollection procParams = procParamsCache.get(cacheId);

				if(procParams == null || (cacheTimeout >= 0 && (procParams.created + cacheTimeout) < System.currentTimeMillis())) {

					// get PROC information and resolve synonym if needed per LDEV-1147
					String sql =
							"SELECT  PROC.OWNER, PROC.OBJECT_NAME, PROC.PROCEDURE_NAME, PROC.OBJECT_TYPE \n" +
							"FROM    ALL_PROCEDURES PROC \n" +
							"    LEFT JOIN ALL_SYNONYMS SYNO \n" +
							"       ON PROC.OBJECT_NAME = SYNO.TABLE_NAME AND PROC.OWNER = SYNO.TABLE_OWNER \n" +
							"WHERE   (PROC.PROCEDURE_NAME = ? OR (PROC.OBJECT_NAME = ? AND PROC.OBJECT_TYPE = 'PROCEDURE')) \n";

					if (catalog != null)
						sql += "    AND (PROC.OBJECT_NAME = ? OR SYNO.SYNONYM_NAME = ?)";

					PreparedStatement preparedStatement = conn.prepareStatement(sql);
					preparedStatement.setString(1, name);
					preparedStatement.setString(2, name);

					if (catalog != null) {
						preparedStatement.setString(3, catalog);
						preparedStatement.setString(4, catalog);
					}

					ResultSet resultSet = preparedStatement.executeQuery();

					if(resultSet.next()) {
						String _schema  = resultSet.getString(1);	// OWNER
						String _catalog = resultSet.getString(2);	// OBJECT_NAME
						String _name    = resultSet.getString(3);	// PROCEDURE_NAME

						if (_name == null && _catalog != null){
							// when the PROC is not scoped the PROCEDURE_NAME is actually the OBJECT_NAME, see LDEV-1833
							_name = _catalog;
							_catalog = null;
						}

						ResultSet procColumns = conn.getMetaData().getProcedureColumns(_catalog, _schema, _name, "%");
						procParams = createProcMetaCollection(procColumns);
						procParamsCache.put(cacheId, procParams);
					}
					else {
						Log log = pageContext.getConfig().getLog("datasource");
						if(log.getLogLevel() >= Log.LEVEL_INFO)
							log.info(StoredProc.class.getSimpleName(), "procedure " + procedure + " not found in view ALL_PROCEDURES");
					}
				}

				int ct, index = -1;
				if(procParams != null) {
					Iterator<ProcMeta> it = procParams.metas.iterator();
					ProcMeta pm;
					while(it.hasNext()) {
						index++;
						pm = it.next();
						ct = pm.columnType;

						// Return
						if(ct == DatabaseMetaData.procedureColumnReturn) {
							index--;
							ProcResultBean result = getFirstResult();
							ProcParamBean param = new ProcParamBean();

							param.setType(pm.dataType);
							param.setDirection(ProcParamBean.DIRECTION_OUT);
							if(result != null)
								param.setVariable(result.getName());
							returnValue = param;
						}
						else if(ct == DatabaseMetaData.procedureColumnOut || ct == DatabaseMetaData.procedureColumnInOut) {
							// review of the code: seems to add an addional column in this case
							if(pm.dataType == CFTypes.CURSOR) {
								ProcResultBean result = getFirstResult();
								ProcParamBean param = new ProcParamBean();

								param.setType(pm.dataType);
								param.setDirection(ProcParamBean.DIRECTION_OUT);
								if(result != null)
									param.setVariable(result.getName());

								if(params.size() < index)
									throw new DatabaseException(
											"you have only defined [" + params.size() + "] procparam tags, but the procedure/function called is expecting more",
											null, null, dc);
								else if(params.size() == index)
									params.add(param);
								else
									params.add(index, param);
							}
							else {
								ProcParamBean param = null;
								if (params.size() > index)
									param = params.get(index);

								if(param != null && pm.dataType != Types.OTHER && pm.dataType != param.getType()) {
									param.setType(pm.dataType);
								}
							}
						}
						else if(ct == DatabaseMetaData.procedureColumnIn) {
							ProcParamBean param = get(params, index);
							if(param != null && pm.dataType != Types.OTHER && pm.dataType != param.getType()) {
								param.setType(pm.dataType);
							}
						}
					}
				}

				contractTo(params, index + 1);
				// if(res!=null)print.out(new QueryImpl(res,"columns").toString());
			}
			catch (SQLException e) {
				throw new DatabaseException(e, dc);
			}
		}

		// return code
		if(returncode) {
			returnValue = STATUS_CODE;
		}
	}

	private static ProcParamBean get(List<ProcParamBean> params, int index) {
		try {
			return params.get(index);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return null;
		}
	}

	private void contractTo(List<ProcParamBean> params, int paramCount) {
		if(params.size() > paramCount) {
			for (int i = params.size() - 1; i >= paramCount; i--) {
				params.remove(i);
			}
		}
	}

	private ProcMetaCollection createProcMetaCollection(ResultSet res) throws SQLException {
		/*
		 * try { print.out(new QueryImpl(res,"qry")); } catch (PageException e) {}
		 */
		ArrayList<ProcMeta> list = new ArrayList<ProcMeta>();
		try {
			while(res.next()) {
				list.add(new ProcMeta(res.getInt(COLUMN_TYPE), getDataType(res)));
			}
		} finally {
			IOUtil.closeEL(res);
		}
		return new ProcMetaCollection(list);
	}

	private int getDataType(ResultSet res) throws SQLException {
		int dataType = res.getInt(DATA_TYPE);
		if(dataType == Types.OTHER) {
			String strDataType = res.getString(TYPE_NAME);
			if("REF CURSOR".equalsIgnoreCase(strDataType))
				dataType = CFTypes.CURSOR;
			if("CLOB".equalsIgnoreCase(strDataType))
				dataType = Types.CLOB;
			if("BLOB".equalsIgnoreCase(strDataType))
				dataType = Types.BLOB;
		}
		return dataType;
	}

	private ProcResultBean getFirstResult() {
		Iterator<Key> it = results.keyIterator();
		if(!it.hasNext())
			return null;

		return (ProcResultBean)results.removeEL(it.next());
	}

	@Override
	public int doEndTag() throws PageException {
		long startNS = System.nanoTime();

		Object ds = datasource;
		if(StringUtil.isEmpty(datasource)) {
			ds = pageContext.getApplicationContext().getDefDataSource();
			if(StringUtil.isEmpty(ds)) {
				boolean isCFML = pageContext.getRequestDialect() == CFMLEngine.DIALECT_CFML;
				throw new ApplicationException("attribute [datasource] is required, when no default datasource is defined",
						"you can define a default datasource as attribute [defaultdatasource] of the tag "
								+ (isCFML ? Constants.CFML_APPLICATION_TAG_NAME : Constants.LUCEE_APPLICATION_TAG_NAME) + " or as data member of the "
								+ (isCFML ? Constants.CFML_APPLICATION_EVENT_HANDLER : Constants.LUCEE_APPLICATION_EVENT_HANDLER)
								+ " (this.defaultdatasource=\"mydatasource\";)");
			}
		}

		Struct res = new StructImpl();
		DataSourceManager manager = pageContext.getDataSourceManager();
		DatasourceConnection dc = ds instanceof DataSource ? manager.getConnection(pageContext, (DataSource)ds, username, password)
				: manager.getConnection(pageContext, Caster.toString(ds), username, password);

		// create returnValue
		returnValue(dc);

		String sql = createSQL();

		// add returnValue to params
		if(returnValue != null) {
			params.add(0, returnValue);
		}

		Log log = pageContext.getConfig().getLog("datasource");

		SQLImpl _sql = new SQLImpl(sql);
		CallableStatement callStat = null;
		try {

			if(log.getLogLevel() >= Log.LEVEL_DEBUG)	// log entry added to troubleshoot LDEV-1147
				log.debug("LDEV1147", sql + " [" + params.size() + " params]");

			callStat = dc.getConnection().prepareCall(sql);
			if(blockfactor > 0)
				callStat.setFetchSize(blockfactor);
			if(timeout > 0)
				DataSourceUtil.setQueryTimeoutSilent(callStat, timeout);

			// set IN register OUT
			Iterator<ProcParamBean> it = params.iterator();
			ProcParamBean param;
			int index = 1;
			while(it.hasNext()) {
				param = it.next();
				param.setIndex(index);
				_sql.addItems(new SQLItemImpl(param.getValue()));
				if(param.getDirection() != ProcParamBean.DIRECTION_OUT) {
					SQLCaster.setValue(pageContext, pageContext.getTimeZone(), callStat, index, param);
				}
				if(param.getDirection() != ProcParamBean.DIRECTION_IN) {
					registerOutParameter(callStat, param);
				}
				index++;
			}

			String dsn = (ds instanceof DataSource) ? ((DataSource)ds).getName() : Caster.toString(ds);

			// cache
			boolean isFromCache = false;
			Object cacheValue = null;
			boolean useCache = (cachedWithin != null) || (cachedafter != null);
			String cacheId = null;
			CacheHandler cacheHandler = null;

			if(useCache) {

				cacheId = CacheHandlerCollectionImpl.createId(_sql, dsn, username, password, Query.RETURN_TYPE_STORED_PROC);
				cacheHandler = pageContext.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_QUERY, null).getInstanceMatchingObject(cachedWithin, null);

				if(cacheHandler instanceof CacheHandlerPro) {

					CacheItem cacheItem = ((CacheHandlerPro)cacheHandler).get(pageContext, cacheId, cachedWithin);

					if(cacheItem != null)
						cacheValue = ((StoredProcCacheItem)cacheItem).getStruct();
				}
				else if(cacheHandler != null) { // TODO this else block can be removed when all cache handlers implement CacheHandlerPro

					CacheItem cacheItem = cacheHandler.get(pageContext, cacheId);

					if(cacheItem != null)
						cacheValue = ((StoredProcCacheItem)cacheItem).getStruct();
					// cacheValue = pageContext.getQueryCache().get(pageContext,_sql,dsn,username,password,cachedafter);
				}
			}

			int count = 0;
			long start = System.currentTimeMillis();
			if(cacheValue == null) {
				// execute
				boolean isResult = callStat.execute();

				Struct cacheStruct = useCache ? new StructImpl() : null;

				// resultsets
				ProcResultBean result;

				index = 1;
				do {
					if(isResult) {
						ResultSet rs = callStat.getResultSet();
						if(rs != null) {
							try {
								result = (ProcResultBean)results.get(index++, null);
								if(result != null) {
									lucee.runtime.type.Query q = new QueryImpl(rs, result.getMaxrows(), result.getName(), pageContext.getTimeZone());
									count += q.getRecordcount();
									setVariable(result.getName(), q);

									if(useCache)
										cacheStruct.set(KeyImpl.getInstance(result.getName()), q);
								}
							} finally {
								IOUtil.closeEL(rs);
							}
						}
					}
				} while((isResult = callStat.getMoreResults()) || (callStat.getUpdateCount() != -1));

				// params
				it = params.iterator();
				while(it.hasNext()) {
					param = it.next();
					if(param.getDirection() != ProcParamBean.DIRECTION_IN) {
						Object value = null;
						if(!StringUtil.isEmpty(param.getVariable())) {
							try {
								value = SQLCaster.toCFType(callStat.getObject(param.getIndex()));
							}
							catch (Throwable t) {
								ExceptionUtil.rethrowIfNecessary(t);
							}
							value = emptyIfNull(value);

							if(param == STATUS_CODE)
								res.set(STATUSCODE, value);
							else
								setVariable(param.getVariable(), value);

							if(useCache)
								cacheStruct.set(KeyImpl.getInstance(param.getVariable()), value);
						}
					}
				}

				if(cacheHandler != null) {

					cacheStruct.set(COUNT, Caster.toDouble(count));
					cacheHandler.set(pageContext, cacheId, cachedWithin, new StoredProcCacheItem(cacheStruct, procedure, System.currentTimeMillis() - start));
					// pageContext.getQueryCache().set(pageContext,_sql,dsn,username,password,cache,cachedbefore);
				}
			}
			else if(cacheValue instanceof Struct) {
				Struct sctCache = (Struct)cacheValue;
				count = Caster.toIntValue(sctCache.removeEL(COUNT), 0);

				Iterator<Entry<Key, Object>> cit = sctCache.entryIterator();
				Entry<Key, Object> ce;
				while(cit.hasNext()) {
					ce = cit.next();
					if(STATUS_CODE.getVariable().equals(ce.getKey().getString()))
						res.set(KEY_SC, ce.getValue());
					else
						setVariable(ce.getKey().getString(), ce.getValue());
				}
				isFromCache = true;
			}
			// result
			long exe;

			setVariable(this.result, res);
			res.set(KeyConstants._executionTime, Caster.toDouble(exe = (System.nanoTime() - startNS)));
			res.set(KeyConstants._cached, Caster.toBoolean(isFromCache));

			if(pageContext.getConfig().debug() && debug) {
				boolean logdb = ((ConfigImpl)pageContext.getConfig()).hasDebugOptions(ConfigImpl.DEBUG_DATABASE);
				if(logdb)
					pageContext.getDebugger().addQuery(null, dsn, procedure, _sql, count, pageContext.getCurrentPageSource(), (int)exe);
			}

			// log
			if(log.getLogLevel() >= Log.LEVEL_INFO) {
				log.info(StoredProc.class.getSimpleName(), "executed [" + sql.trim() + "] in " + DecimalFormat.call(pageContext, exe / 1000000D) + " ms");
			}
		}
		catch (SQLException e) {
			// log
			log.error(StoredProc.class.getSimpleName(), e);
			throw new DatabaseException(e, new SQLImpl(sql), dc);
		}
		catch (PageException pe) {
			// log
			log.error(StoredProc.class.getSimpleName(), pe);
			throw pe;
		} finally {
			if(callStat != null) {
				try {
					callStat.close();
				}
				catch (SQLException e) {
				}
			}
			manager.releaseConnection(pageContext, dc);
		}
		return EVAL_PAGE;
	}

	private void setVariable(String name, Object value) throws PageException {
		pageContext.setVariable(name, value);
	}

	private String createSQL() {
		StringBuilder sb = new StringBuilder();

		if(returnValue != null)
			sb.append("{? = call ");
		else
			sb.append("{ call ");

		sb.append(procedure);
		sb.append('(');

		int incount = params.size();
		for (int i = 0; i < incount; i++) {
			if(i == 0)
				sb.append('?');
			else
				sb.append(",?");
		}
		sb.append(") }");

		return sb.toString();
	}

	private Object emptyIfNull(Object object) {
		if(object == null)
			return "";
		return object;
	}

	private void registerOutParameter(CallableStatement proc, ProcParamBean param) throws SQLException {
		if(param.getScale() == -1)
			proc.registerOutParameter(param.getIndex(), param.getType());
		else
			proc.registerOutParameter(param.getIndex(), param.getType(), param.getScale());
	}

	/**
	 * @param b
	 */
	public void hasBody(boolean b) {

	}

	/**
	 * @param timeout
	 *            the timeout to set
	 */
	public void setTimeout(double timeout) {
		this.timeout = (int)timeout;
	}
}