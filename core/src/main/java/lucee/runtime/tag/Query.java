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
import java.util.List;
import java.util.TimeZone;

import lucee.commons.io.log.Log;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.cache.tag.CacheHandler;
import lucee.runtime.cache.tag.CacheHandlerCollectionImpl;
import lucee.runtime.cache.tag.CacheItem;
import lucee.runtime.cache.tag.query.QueryResultCacheItem;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.config.Constants;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.db.DatasourceManagerImpl;
import lucee.runtime.db.HSQLDBHandler;
import lucee.runtime.db.SQL;
import lucee.runtime.db.SQLImpl;
import lucee.runtime.db.SQLItem;
import lucee.runtime.debug.DebuggerImpl;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.BodyTagTryCatchFinallyImpl;
import lucee.runtime.functions.displayFormatting.DecimalFormat;
import lucee.runtime.listener.AppListenerUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.orm.ORMSession;
import lucee.runtime.orm.ORMUtil;
import lucee.runtime.tag.util.DeprecatedUtil;
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
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.dt.TimeSpanImpl;
import lucee.runtime.type.query.QueryResult;
import lucee.runtime.type.query.SimpleQuery;
import lucee.runtime.type.scope.Argument;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.util.PageContextUtil;

import org.osgi.framework.BundleException;



/**
* Passes SQL statements to a data source. Not limited to queries.
**/
public final class Query extends BodyTagTryCatchFinallyImpl {

	private static final Collection.Key SQL_PARAMETERS = KeyImpl.intern("sqlparameters");
	private static final Collection.Key CFQUERY = KeyImpl.intern("cfquery");
	private static final Collection.Key GENERATEDKEY = KeyImpl.intern("generatedKey");
	private static final Collection.Key MAX_RESULTS = KeyImpl.intern("maxResults");
	private static final Collection.Key TIMEOUT = KeyConstants._timeout;
	
	private static final int RETURN_TYPE_UNDEFINED = 0;
	private static final int RETURN_TYPE_QUERY = 1;
	private static final int RETURN_TYPE_ARRAY = 2;
	private static final int RETURN_TYPE_STRUCT = 3;
	public static final int RETURN_TYPE_STORED_PROC = 4;


	
	/** If specified, password overrides the password value specified in the data source setup. */
	private String password;

	/** The name of the data source from which this query should retrieve data. */
	private DataSource datasource=null;

	/** The maximum number of milliseconds for the query to execute before returning an error 
	** 		indicating that the query has timed-out. This attribute is not supported by most ODBC drivers. 
	** 		timeout is supported by the SQL Server 6.x or above driver. The minimum and maximum allowable values 
	** 		vary, depending on the driver. */
	private TimeSpan timeout=null;

	/** This is the age of which the query data can be */
	private Object cachedWithin;
	
	/** Specifies the maximum number of rows to fetch at a time from the server. The range is 1, 
	** 		default to 100. This parameter applies to ORACLE native database drivers and to ODBC drivers. 
	** 		Certain ODBC drivers may dynamically reduce the block factor at runtime. */
	private int blockfactor=-1;

	/** The database driver type. */
	private String dbtype;

	/** Used for debugging queries. Specifying this attribute causes the SQL statement submitted to the 
	** 		data source and the number of records returned from the query to be returned. */
	private boolean debug=true;

	/* This is specific to JTags, and allows you to give the cache a specific name */
	//private String cachename;

	/** Specifies the maximum number of rows to return in the record set. */
	private int maxrows=-1;

	/** If specified, username overrides the username value specified in the data source setup. */
	private String username;

	/**  */
	private DateTime cachedAfter;

	/** The name query. Must begin with a letter and may consist of letters, numbers, and the underscore 
	** 		character, spaces are not allowed. The query name is used later in the page to reference the query's 
	** 		record set. */
	private String name;
	
	private String result=null;

	//private static HSQLDBHandler hsql=new HSQLDBHandler();
	
	private boolean orgPSQ;
	private boolean hasChangedPSQ;
	
	ArrayList<SQLItem> items=new ArrayList<SQLItem>();
	
	private boolean unique;
	private Struct ormoptions;
	private int returntype=RETURN_TYPE_UNDEFINED;
	private TimeZone timezone;
	private TimeZone tmpTZ;
	private boolean lazy;
	private Object params;
	private int nestingLevel=0;
	private boolean setReturnVariable=false;
	private Object rtn;
	private Key columnName;
	
	@Override
	public void release()	{
		super.release();
		items.clear();
		password=null;
		datasource=null;
		timeout=null;
		cachedWithin=null;
		cachedAfter=null;
		//cachename="";
		blockfactor=-1;
		dbtype=null;
		debug=true;
		maxrows=-1;
		username=null;
		name="";
		result=null;

		orgPSQ=false;
		hasChangedPSQ=false;
		unique=false;
		
		ormoptions=null;
		returntype=RETURN_TYPE_UNDEFINED;
		timezone=null;
		tmpTZ=null;
		lazy=false;
		params=null;
		nestingLevel=0;
		rtn=null;
		setReturnVariable=false;
		columnName=null;
	}
	
	
	public void setOrmoptions(Struct ormoptions) {
		this.ormoptions = ormoptions;
	}


	public void setReturntype(String strReturntype) throws ApplicationException {
		if(StringUtil.isEmpty(strReturntype)) return;
		strReturntype=strReturntype.toLowerCase().trim();
		
		if(strReturntype.equals("query"))
			returntype=RETURN_TYPE_QUERY;
		    //mail.setType(lucee.runtime.mail.Mail.TYPE_TEXT);
		else if(strReturntype.equals("struct"))
			returntype=RETURN_TYPE_STRUCT;
		else if(strReturntype.equals("array") || 
				strReturntype.equals("array_of_struct") || strReturntype.equals("array-of-struct") || strReturntype.equals("arrayofstruct") ||
				strReturntype.equals("array_of_entity") || strReturntype.equals("array-of-entity") || strReturntype.equals("arrayofentities") ||
				strReturntype.equals("array_of_entities") || strReturntype.equals("array-of-entities") || strReturntype.equals("arrayofentities"))
			returntype=RETURN_TYPE_ARRAY;

		
		else
			throw new ApplicationException("attribute returntype of tag query has an invalid value","valid values are [query,array] but value is now ["+strReturntype+"]");
	}


	public void setUnique(boolean unique) {
		this.unique = unique;
	}
	/**
	 * @param result the result to set
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * @param psq set preserver single quote
	 */
	public void setPsq(boolean psq)	{
		orgPSQ=pageContext.getPsq();
        if(orgPSQ!=psq){
        	pageContext.setPsq(psq);
        	hasChangedPSQ=true;
        }
	}
	
	/** set the value password
	*  If specified, password overrides the password value specified in the data source setup.
	* @param password value to set
	**/
	public void setPassword(String password)	{
		this.password=password;
	}

	/** set the value datasource
	*  The name of the data source from which this query should retrieve data.
	* @param datasource value to set
	 * @throws ClassException 
	 * @throws BundleException 
	**/

	public void setDatasource(Object datasource) throws PageException, ClassException, BundleException	{
		if (Decision.isStruct(datasource)) {
			this.datasource=AppListenerUtil.toDataSource(pageContext.getConfig(),"__temp__", Caster.toStruct(datasource),pageContext.getConfig().getLog("application"));
		} 
		else if (Decision.isString(datasource)) {
			this.datasource=pageContext.getDataSource(Caster.toString(datasource));
		} 
		else {
			throw new ApplicationException("attribute [datasource] must be datasource name or a datasource definition(struct)");
			
		}
	}

	/** set the value timeout
	*  The maximum number of milliseconds for the query to execute before returning an error 
	* 		indicating that the query has timed-out. This attribute is not supported by most ODBC drivers. 
	* 		timeout is supported by the SQL Server 6.x or above driver. The minimum and maximum allowable values 
	* 		vary, depending on the driver.
	* @param timeout value to set
	 * @throws PageException 
	**/
	public void setTimeout(Object timeout) throws PageException	{
		if(timeout instanceof TimeSpan)
			this.timeout=(TimeSpan) timeout;
		// seconds
		else {
			int i = Caster.toIntValue(timeout);
			if(i<0)
				throw new ApplicationException("invalid value ["+i+"] for attribute timeout, value must be a positive integer greater or equal than 0");
			
			this.timeout=new TimeSpanImpl(0, 0, 0, i);
		}
	}

	/** set the value cachedafter
	*  This is the age of which the query data can be
	* @param cachedafter value to set
	**/
	public void setCachedafter(DateTime cachedafter)	{
		//lucee.print.ln("cachedafter:"+cachedafter);
		this.cachedAfter=cachedafter;
	}

	/** set the value cachename
	*  This is specific to JTags, and allows you to give the cache a specific name
	* @param cachename value to set
	**/
	public void setCachename(String cachename)	{
		DeprecatedUtil.tagAttribute(pageContext,"query", "cachename");
		//this.cachename=cachename;
	}
	public void setColumnkey(String columnKey) {
		if(StringUtil.isEmpty(columnKey,true)) return;
		this.columnName=KeyImpl.init(columnKey);
	}
	

	
	public void setCachedwithin(Object cachedwithin)	{
		if(StringUtil.isEmpty(cachedwithin)) return;
		this.cachedWithin=cachedwithin;
	}
	
	
	public void setLazy(boolean lazy)	{
		this.lazy=lazy;
	}

	/** set the value providerdsn
	*  Data source name for the COM provider, OLE-DB only.
	* @param providerdsn value to set
	 * @throws ApplicationException
	**/
	public void setProviderdsn(String providerdsn) throws ApplicationException	{
		DeprecatedUtil.tagAttribute(pageContext,"Query", "providerdsn");
	}

	/** set the value connectstring
	* @param connectstring value to set
	 * @throws ApplicationException
	**/
	public void setConnectstring(String connectstring) throws ApplicationException	{
		DeprecatedUtil.tagAttribute(pageContext,"Query", "connectstring");
	}
	

	public void setTimezone(TimeZone tz)	{
		if(tz==null) return;
	    this.timezone=tz;
	}

	/** set the value blockfactor
	*  Specifies the maximum number of rows to fetch at a time from the server. The range is 1, 
	* 		default to 100. This parameter applies to ORACLE native database drivers and to ODBC drivers. 
	* 		Certain ODBC drivers may dynamically reduce the block factor at runtime.
	* @param blockfactor value to set
	**/
	public void setBlockfactor(double blockfactor)	{
		this.blockfactor=(int) blockfactor;
	}

	/** set the value dbtype
	*  The database driver type.
	* @param dbtype value to set
	**/
	public void setDbtype(String dbtype)	{
		this.dbtype=dbtype.toLowerCase();
	}

	/** set the value debug
	*  Used for debugging queries. Specifying this attribute causes the SQL statement submitted to the 
	* 		data source and the number of records returned from the query to be returned.
	* @param debug value to set
	**/
	public void setDebug(boolean debug)	{
		this.debug=debug;
	}

	/** set the value dbname
	*  The database name, Sybase System 11 driver and SQLOLEDB provider only. If specified, dbName 
	* 		overrides the default database specified in the data source.
	* @param dbname value to set
	 * @throws ApplicationException
	**/
	public void setDbname(String dbname) {
		DeprecatedUtil.tagAttribute(pageContext,"Query", "dbname");
	}

	/** set the value maxrows
	*  Specifies the maximum number of rows to return in the record set.
	* @param maxrows value to set
	**/
	public void setMaxrows(double maxrows)	{
		this.maxrows=(int) maxrows;
	}

	/** set the value username
	*  If specified, username overrides the username value specified in the data source setup.
	* @param username value to set
	**/
	public void setUsername(String username)	{
		if(!StringUtil.isEmpty(username))
			this.username=username;
	}

	/** set the value provider
	*  COM provider, OLE-DB only.
	* @param provider value to set
	 * @throws ApplicationException
	**/
	public void setProvider(String provider) {
		DeprecatedUtil.tagAttribute(pageContext,"Query", "provider");
	}

	/** set the value dbserver
	*  For native database drivers and the SQLOLEDB provider, specifies the name of the database server 
	* 		computer. If specified, dbServer overrides the server specified in the data source.
	* @param dbserver value to set
	 * @throws ApplicationException
	**/
	public void setDbserver(String dbserver) {
		DeprecatedUtil.tagAttribute(pageContext,"Query", "dbserver");
	}

	/** set the value name
	*  The name query. Must begin with a letter and may consist of letters, numbers, and the underscore 
	* 		character, spaces are not allowed. The query name is used later in the page to reference the query's 
	* 		record set.
	* @param name value to set
	**/
	public void setName(String name)	{
		this.name=name;
	}
	
	public String getName()	{
		return name==null? "query":name;
	}
	
	
	


    /**
     * @param item
     */
    public void setParam(SQLItem item) {
        items.add(item);
    }
    
    public void setParams(Object params) {
        this.params=params;
    }
    
    public void setNestinglevel(double nestingLevel) {
        this.nestingLevel=(int)nestingLevel;
    }



	@Override
	public int doStartTag() throws PageException	{
		
		//timeout
		/*TimeSpan remaining = PageContextUtil.remainingTime(pageContext,true);
		if(this.timeout==null || ((int)this.timeout.getSeconds())<=0 || timeout.getSeconds()>remaining.getSeconds()) { // not set
			this.timeout=remaining;
		}*/
		
		
		// default datasource
		if(datasource==null && (dbtype==null || !dbtype.equals("query"))){
			Object obj = pageContext.getApplicationContext().getDefDataSource();
			if(StringUtil.isEmpty(obj)) {
				boolean isCFML=pageContext.getRequestDialect()==CFMLEngine.DIALECT_CFML;
				throw new ApplicationException(
						"attribute [datasource] is required, when attribute [dbtype] has not value [query] and no default datasource is defined",
						"you can define a default datasource as attribute [defaultdatasource] of the tag "
						+(isCFML?Constants.CFML_APPLICATION_TAG_NAME:Constants.LUCEE_APPLICATION_TAG_NAME)
						+" or as data member of the "
						+(isCFML?Constants.CFML_APPLICATION_EVENT_HANDLER:Constants.LUCEE_APPLICATION_EVENT_HANDLER)
						+" (this.defaultdatasource=\"mydatasource\";)");
			}
			datasource=obj instanceof DataSource?(DataSource)obj:pageContext.getDataSource(Caster.toString(obj));
		}
		
		
		// timezone
		if(timezone!=null || (datasource!=null && (timezone=datasource.getTimeZone())!=null)) {
			tmpTZ=pageContext.getTimeZone();
			pageContext.setTimeZone(timezone);
		}
		
		// cache within
		if(StringUtil.isEmpty(cachedWithin)){
			Object tmp = ((PageContextImpl)pageContext).getCachedWithin(ConfigWeb.CACHEDWITHIN_QUERY);
			if(tmp!=null)setCachedwithin(tmp);
		}
		
		
		return EVAL_BODY_BUFFERED;
	}
	
	@Override
	public void doFinally() {
		if(tmpTZ!=null) {
			pageContext.setTimeZone(tmpTZ);
		}
		super.doFinally();
	}

	@Override
	public int doEndTag() throws PageException	{

		
		
		if(hasChangedPSQ)pageContext.setPsq(orgPSQ);
		String strSQL=bodyContent.getString();
		// no SQL String defined
		if(strSQL.length()==0) 
			throw new DatabaseException("no sql string defined, inside query tag",null,null,null);
		
		try{
		
			strSQL=strSQL.trim();
			// cannot use attribute params and queryparam tag
			if(items.size()>0 && params!=null)
				throw new DatabaseException("you cannot use the attribute params and sub tags queryparam at the same time",null,null,null);
			// create SQL
			SQL sql;
			if(params!=null) {
				if(params instanceof Argument)
					sql=QueryParamConverter.convert(strSQL, (Argument) params);
				else if(Decision.isArray(params))
					sql=QueryParamConverter.convert(strSQL, Caster.toArray(params));
				else if(Decision.isStruct(params))
					sql=QueryParamConverter.convert(strSQL, Caster.toStruct(params));
				else
					throw new DatabaseException("value of the attribute [params] has to be a struct or a array",null,null,null);
			}
			else sql=items.size()>0?new SQLImpl(strSQL,items.toArray(new SQLItem[items.size()])):new SQLImpl(strSQL);
			

			QueryResult qr=null;
			
			//lucee.runtime.type.Query query=null;
			long exe=0;
			boolean hasCached=cachedWithin!=null || cachedAfter!=null;
			String cacheId=null;
			if(hasCached) {
				String id = CacheHandlerCollectionImpl.createId(sql,datasource!=null?datasource.getName():null,username,password,returntype);
				CacheHandler ch = pageContext.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_QUERY,null).getInstanceMatchingObject(cachedWithin,null);
				if(ch!=null) {
					cacheId=ch.id();
					CacheItem ci = ch.get(pageContext, id);
					if(ci instanceof QueryResultCacheItem) {
						QueryResultCacheItem ce = (QueryResultCacheItem) ci;
						if(ce.isCachedAfter(cachedAfter))
							qr= ce.getQueryResult();
					}
				}
				else {
					List<String> patterns = pageContext.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_QUERY,null).getPatterns();
					throw new ApplicationException("cachedwithin value ["+cachedWithin+"] is invalid, valid values are for example ["+ListUtil.listToList(patterns, ", ")+"]");
				}
				//query=pageContext.getQueryCache().getQuery(pageContext,sql,datasource!=null?datasource.getName():null,username,password,cachedafter);
			}
			
			if(qr==null) {
				// QoQ
				if("query".equals(dbtype)) 		qr=(QueryResult)executeQoQ(sql);
				// ORM and Datasource
				else  	{ 
					long start=System.nanoTime();
					
					Object obj = 
							("orm".equals(dbtype) || "hql".equals(dbtype))?
									executeORM(sql,returntype,ormoptions):
									executeDatasoure(sql,result!=null,pageContext.getTimeZone());

					
					/*if(obj instanceof lucee.runtime.type.Query)
						qr=query=(lucee.runtime.type.Query) obj;
					else*/ 
					if(obj instanceof QueryResult)
						qr=(QueryResult)obj;
					else {
						if(setReturnVariable){
							rtn=obj;
						}
						else if(!StringUtil.isEmpty(name)) {
							pageContext.setVariable(name,obj);
						}
						if(result!=null){
							Struct sct=new StructImpl();
							sct.setEL(KeyConstants._cached, Boolean.FALSE);
							long time=System.nanoTime()-start;
							sct.setEL(KeyConstants._executionTime, Caster.toDouble(time/1000000));
							sct.setEL(KeyConstants._executionTimeNano, Caster.toDouble(time));
							sct.setEL(KeyConstants._SQL, sql.getSQLString());
							if(Decision.isArray(obj)){
								
							}
							else sct.setEL(KeyConstants._RECORDCOUNT, Caster.toDouble(1));
								
							pageContext.setVariable(result, sct);
						}
						else
							setExecutionTime((System.nanoTime()-start)/1000000);
						return EVAL_PAGE;
					}
				}
				//else query=executeDatasoure(sql,result!=null,pageContext.getTimeZone());
				
				if(cachedWithin!=null) {
					String id = CacheHandlerCollectionImpl.createId(sql,datasource!=null?datasource.getName():null,username,password,returntype);
					CacheHandler ch = pageContext.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_QUERY,null).getInstanceMatchingObject(cachedWithin,null);
					if(ch!=null) {
						CacheItem ci = QueryResultCacheItem.newInstance(qr,null);
						if(ci!=null)ch.set(pageContext, id,cachedWithin,ci);
					}
				}
				exe=qr.getExecutionTime();
			}
			else qr.setCacheType(cacheId);
			
			if(pageContext.getConfig().debug() && debug) {
				boolean logdb=((ConfigImpl)pageContext.getConfig()).hasDebugOptions(ConfigImpl.DEBUG_DATABASE);
				if(logdb && qr instanceof lucee.runtime.type.Query){
					lucee.runtime.type.Query q = (lucee.runtime.type.Query)qr;
					boolean debugUsage=DebuggerImpl.debugQueryUsage(pageContext,q);
					pageContext.getDebugger().addQuery(debugUsage?q:null,datasource!=null?datasource.getName():null,name,sql,qr.getRecordcount(),getPageSource(),exe);
				}
			}
			if(setReturnVariable){
				rtn=qr;
			}
			else if(!qr.isEmpty() && !StringUtil.isEmpty(name)) {
				pageContext.setVariable(name,qr);
			}
			
			// Result
			if(result!=null) {
				
				Struct sct=new StructImpl();
				sct.setEL(KeyConstants._cached, Caster.toBoolean(qr.isCached()));
				if(!qr.isEmpty()){
					String list = ListUtil.arrayToList(
							qr instanceof lucee.runtime.type.Query?
							((lucee.runtime.type.Query)qr).getColumnNamesAsString():
							CollectionUtil.toString(qr.getColumnNames(), false)
							,",");
					sct.setEL(KeyConstants._COLUMNLIST, list);
				}
				int rc=qr.getRecordcount();
				if(rc==0)rc=qr.getUpdateCount();
				sct.setEL(KeyConstants._RECORDCOUNT, Caster.toDouble(rc));
				sct.setEL(KeyConstants._executionTime, Caster.toDouble(qr.getExecutionTime()/1000000));
				sct.setEL(KeyConstants._executionTimeNano, Caster.toDouble(qr.getExecutionTime()));
				
				sct.setEL(KeyConstants._SQL, sql.getSQLString());
				
				// GENERATED KEYS
				lucee.runtime.type.Query qi = Caster.toQuery(qr,null);
				if(qi !=null){
					lucee.runtime.type.Query qryKeys = qi.getGeneratedKeys();
					if(qryKeys!=null){
						StringBuilder generatedKey=new StringBuilder(),sb;
						Collection.Key[] columnNames = qryKeys.getColumnNames();
						QueryColumn column;
						for(int c=0;c<columnNames.length;c++){
							column = qryKeys.getColumn(columnNames[c]);
							sb=new StringBuilder();
							int size=column.size();
							for(int row=1;row<=size;row++) {
								if(row>1)sb.append(',');
								sb.append(Caster.toString(column.get(row,null)));
							}
							if(sb.length()>0){
								sct.setEL(columnNames[c], sb.toString());
								if(generatedKey.length()>0)generatedKey.append(',');
								generatedKey.append(sb);
							}
						}
						if(generatedKey.length()>0)
							sct.setEL(GENERATEDKEY, generatedKey.toString());
					}
				}
				
				// sqlparameters
				SQLItem[] params = sql.getItems();
				if(params!=null && params.length>0) {
					Array arr=new ArrayImpl();
					sct.setEL(SQL_PARAMETERS, arr); 
					for(int i=0;i<params.length;i++) {
						arr.append(params[i].getValue());
						
					}
				}
				pageContext.setVariable(result, sct);
			}
			// cfquery.executiontime
			else {
				setExecutionTime(exe/1000000);
				
			}
			
			
			// listener
			((ConfigWebImpl)pageContext.getConfig()).getActionMonitorCollector()
				.log(pageContext, "query", "Query", exe, qr);
			

			// log
			Log log = pageContext.getConfig().getLog("datasource");
			if(log.getLogLevel()>=Log.LEVEL_INFO) {
				log.info("query tag", "executed ["+sql.toString().trim()+"] in "+DecimalFormat.call(pageContext, exe/1000000D)+" ms");
			}
		}
		catch (PageException pe) {
			// log
			pageContext.getConfig().getLog("datasource").error("query tag", pe);
			throw pe;
		}
		return EVAL_PAGE;
	}

	private PageSource getPageSource() {
		if(nestingLevel>0) {
			PageContextImpl pci=(PageContextImpl) pageContext;
			List<PageSource> list = pci.getPageSourceList();
			int index=list.size()-1-nestingLevel;
			if(index>=0) return list.get(index);
		}
		return pageContext.getCurrentPageSource();
	}


	private void setExecutionTime(long exe) {
		Struct sct=new StructImpl();
		sct.setEL(KeyConstants._executionTime,new Double(exe));
		pageContext.undefinedScope().setEL(CFQUERY,sct);
	}


	private Object executeORM(SQL sql, int returnType, Struct ormoptions) throws PageException {
		ORMSession session=ORMUtil.getSession(pageContext);
		if(ormoptions==null) ormoptions=new StructImpl();
		String dsn = null;
		if (ormoptions!=null) dsn =	Caster.toString(ormoptions.get(KeyConstants._datasource,null),null);
		if(StringUtil.isEmpty(dsn,true)) dsn=ORMUtil.getDefaultDataSource(pageContext).getName();
		
		// params
		SQLItem[] _items = sql.getItems();
		Array params=new ArrayImpl();
		for(int i=0;i<_items.length;i++){
			params.appendEL(_items[i]);
		}
		
		// query options
		if(maxrows!=-1 && !ormoptions.containsKey(MAX_RESULTS)) 
			ormoptions.setEL(MAX_RESULTS, new Double(maxrows));
		if(timeout!=null && ((int)timeout.getSeconds())>0 && !ormoptions.containsKey(TIMEOUT)) 
			ormoptions.setEL(TIMEOUT, new Double(timeout.getSeconds()));
		/* MUST
offset: Specifies the start index of the resultset from where it has to start the retrieval.
cacheable: Whether the result of this query is to be cached in the secondary cache. Default is false.
cachename: Name of the cache in secondary cache.
		 */
		Object res = session.executeQuery(pageContext,dsn,sql.getSQLString(),params,unique,ormoptions);
		if(returnType==RETURN_TYPE_ARRAY || returnType==RETURN_TYPE_UNDEFINED) return res;
		return session.toQuery(pageContext, res, null);
		
	}
	
	public static Object _call(PageContext pc,String hql, Object params, boolean unique, Struct queryOptions) throws PageException {
		ORMSession session=ORMUtil.getSession(pc);
		String dsn = Caster.toString(queryOptions.get(KeyConstants._datasource,null),null);
		if(StringUtil.isEmpty(dsn,true)) dsn=ORMUtil.getDefaultDataSource(pc).getName();
		
		
		if(Decision.isCastableToArray(params))
			return session.executeQuery(pc,dsn,hql,Caster.toArray(params),unique,queryOptions);
		else if(Decision.isCastableToStruct(params))
			return session.executeQuery(pc,dsn,hql,Caster.toStruct(params),unique,queryOptions);
		else
			return session.executeQuery(pc,dsn,hql,(Array)params,unique,queryOptions);
	}
	

	private lucee.runtime.type.Query executeQoQ(SQL sql) throws PageException {
		try {
			return new HSQLDBHandler().execute(pageContext,sql,maxrows,blockfactor,timeout);
		} 
		catch (Exception e) {
			throw Caster.toPageException(e);
		} 
	}
	
	private QueryResult executeDatasoure(SQL sql,boolean createUpdateData,TimeZone tz) throws PageException {
		DatasourceManagerImpl manager = (DatasourceManagerImpl) pageContext.getDataSourceManager();
		DatasourceConnection dc=manager.getConnection(pageContext,datasource, username, password);
		
		try {
			if(lazy && !createUpdateData && cachedWithin==null && cachedAfter==null && result==null) {
				if(returntype!=RETURN_TYPE_QUERY)
					throw new DatabaseException("only return type query is allowed when lazy is set to true", null, sql, dc);

				return new SimpleQuery(pageContext,dc,sql,maxrows,blockfactor,timeout,getName(),getPageSource().getDisplayPath(),tz);
			}
			if(returntype==RETURN_TYPE_ARRAY)
				return QueryImpl.toArray(pageContext,dc,sql,maxrows,blockfactor,timeout,getName(),getPageSource().getDisplayPath(),createUpdateData,true);
			if(returntype==RETURN_TYPE_STRUCT){
				if(columnName==null)
					throw new ApplicationException("attribute columnKey is required when return type is set to struct");

				return QueryImpl.toStruct(pageContext,dc,sql,columnName,maxrows,blockfactor,timeout,getName(),getPageSource().getDisplayPath(),createUpdateData,true);
			}
			return new QueryImpl(pageContext,dc,sql,maxrows,blockfactor,timeout,getName(),getPageSource().getDisplayPath(),createUpdateData,true);
		}
		finally {
			manager.releaseConnection(pageContext,dc);
		}
	}
	

	@Override
	public void doInitBody()	{
		
	}

	@Override
	public int doAfterBody()	{
		return SKIP_BODY;
	}
	

	public void setReturnVariable(boolean setReturnVariable) {
		this.setReturnVariable=setReturnVariable;
		
	}
	public Object getReturnVariable() {
		return rtn;
		
	}
}