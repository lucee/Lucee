package lucee.runtime.tag.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TimeZone;

import lucee.runtime.db.DataSource;
import lucee.runtime.db.SQLItem;
import lucee.runtime.tag.Query;
import lucee.runtime.tag.listener.TagListener;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.TimeSpan;

public class QueryBean implements Serializable {

	/** If specified, password overrides the password value specified in the data source setup. */
	public String password;

	/** The name of the data source from which this query should retrieve data. */
	public DataSource datasource = null;

	/**
	 * The maximum number of milliseconds for the query to execute before returning an error indicating
	 * that the query has timed-out. This attribute is not supported by most ODBC drivers. timeout is
	 * supported by the SQL Server 6.x or above driver. The minimum and maximum allowable values vary,
	 * depending on the driver.
	 */
	public TimeSpan timeout = null;

	/** This is the age of which the query data can be */
	public Object cachedWithin;

	/**
	 * Specifies the maximum number of rows to fetch at a time from the server. The range is 1, default
	 * to 100. This parameter applies to ORACLE native database drivers and to ODBC drivers. Certain
	 * ODBC drivers may dynamically reduce the block factor at runtime.
	 */
	public int blockfactor = -1;

	/** The database driver type. */
	public String dbtype;

	/**
	 * Used for debugging queries. Specifying this attribute causes the SQL statement submitted to the
	 * data source and the number of records returned from the query to be returned.
	 */
	public boolean debug = true;

	/* This is specific to JTags, and allows you to give the cache a specific name */
	// public String cachename;

	/** Specifies the maximum number of rows to return in the record set. */
	public int maxrows = -1;

	/** If specified, username overrides the username value specified in the data source setup. */
	public String username;

	/**  */
	public DateTime cachedAfter;

	/**
	 * The name query. Must begin with a letter and may consist of letters, numbers, and the underscore
	 * character, spaces are not allowed. The query name is used later in the page to reference the
	 * query's record set.
	 */
	public String name;

	public String result = null;

	public Collection.Key indexName = null;

	// public static HSQLDBHandler hsql=new HSQLDBHandler();

	public ArrayList<SQLItem> items = new ArrayList<SQLItem>();

	public boolean unique;
	public Struct ormoptions;
	public int returntype = Query.RETURN_TYPE_UNDEFINED;
	public TimeZone timezone;
	public TimeZone tmpTZ;
	public boolean lazy;
	public Object params;
	public int nestingLevel = 0;
	public boolean setReturnVariable = false;
	public Object rtn;
	public Key columnName;
	public boolean literalTimestampWithTSOffset;
	public boolean previousLiteralTimestampWithTSOffset;
	public String[] tags = null;
	public String sql;
	public boolean hasBody;
	public TagListener listener;
	public Object rawDatasource;
	public boolean async;

	public void release() {
		items.clear();
		password = null;
		datasource = null;
		rawDatasource = null;
		timeout = null;
		cachedWithin = null;
		cachedAfter = null;
		// cachename="";
		blockfactor = -1;
		dbtype = null;
		debug = true;
		maxrows = -1;
		username = null;
		name = "";
		result = null;

		unique = false;

		ormoptions = null;
		returntype = Query.RETURN_TYPE_UNDEFINED;
		timezone = null;
		tmpTZ = null;
		lazy = false;
		params = null;
		nestingLevel = 0;
		rtn = null;
		setReturnVariable = false;
		columnName = null;
		literalTimestampWithTSOffset = false;
		previousLiteralTimestampWithTSOffset = false;
		tags = null;
		sql = null;
		hasBody = false;
		listener = null;
		async = false;
		indexName = null;
	}

}
