package lucee.runtime.functions.query;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import lucee.commons.db.DBUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.PageContext;
import lucee.runtime.config.Constants;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DataSourceUtil;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.db.DatasourceManagerImpl;
import lucee.runtime.db.SQL;
import lucee.runtime.db.SQLCaster;
import lucee.runtime.db.SQLImpl;
import lucee.runtime.db.SQLItem;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.query.caster.Cast;
import lucee.runtime.tag.Query;
import lucee.runtime.tag.util.QueryParamConverter;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.dt.TimeSpanImpl;
import lucee.runtime.type.query.SimpleQuery;
import lucee.runtime.type.scope.Argument;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.QueryUtil;

public class QueryLazy extends BIF {

	private static int RETURN_TYPE_QUERY = 1;
	private static int RETURN_TYPE_ARRAY = 2;
	private static int RETURN_TYPE_STRUCT = 3;

	private static final long serialVersionUID = 2886504786460447165L;
	private static final Key BLOCKFACTOR = KeyImpl.getInstance("blockfactor");
	private static final Key MAXROWS = KeyImpl.getInstance("maxrows");
	private static final Key COLUMNKEY = KeyImpl.getInstance("columnkey");

	public static String call(PageContext pc, String sql, UDF listener) throws PageException {
		return call(pc, sql, listener, null, null);
	}

	public static String call(PageContext pc, String sql, UDF listener, Object params) throws PageException {
		return call(pc, sql, listener, params, null);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length < 2 || args.length > 4) throw new FunctionException(pc, "QueryLazy", 2, 4, args.length);

		if (args.length == 4) return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), args[2], Caster.toStruct(args[3]));
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), args[2]);
		return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]));
	}

	// name is set by evaluator
	public static String call(PageContext pc, String strSQL, UDF listener, Object params, Struct options) throws PageException {
		DataSource ds = getDatasource(pc, options);
		// credentials
		String user = getString(pc, options, KeyConstants._username, null);
		String pass = getString(pc, options, KeyConstants._password, null);
		int returntype = getReturntype(pc, options);
		Collection.Key columnKey = null;
		if (returntype == RETURN_TYPE_STRUCT) {
			columnKey = getKey(pc, options, COLUMNKEY, null);
			if (StringUtil.isEmpty(columnKey)) throw new ApplicationException("attribute columnKey is required when return type is set to struct");
		}
		else {

		}

		int maxrows = getInt(pc, options, MAXROWS, Integer.MIN_VALUE);
		int blockfactor = getInt(pc, options, BLOCKFACTOR, Integer.MIN_VALUE);

		if (user == null) pass = null;

		SQL sql = getSQL(pc, strSQL, params);
		TimeSpan timeout = getTimeout(pc, options);
		TimeZone tz = getTimeZone(pc, options, ds);

		DatasourceManagerImpl manager = (DatasourceManagerImpl) pc.getDataSourceManager();
		DatasourceConnection dc = manager.getConnection(pc, ds, user, pass); // TODO username and password
		boolean isMySQL = DataSourceUtil.isMySQL(dc);
		// check SQL Restrictions
		if (dc.getDatasource().hasSQLRestriction()) { // deprecated
			QueryUtil.checkSQLRestriction(dc, sql);
		}

		// execute
		Statement stat = null;
		ResultSet res = null;
		boolean hasResult = false;
		try {
			SQLItem[] items = sql.getItems();
			if (items.length == 0) {
				stat = dc.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				setAttributes(stat, maxrows, blockfactor, timeout, isMySQL);
				// some driver do not support second argument
				hasResult = stat.execute(sql.getSQLString());
			}
			else {
				// some driver do not support second argument
				PreparedStatement preStat = dc.getPreparedStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				stat = preStat;
				setAttributes(preStat, maxrows, blockfactor, timeout, isMySQL);
				setItems(pc, tz, preStat, items);
				hasResult = preStat.execute();
			}

			res = null;
			do {
				if (hasResult) {
					res = stat.getResultSet();
					exe(pc, res, tz, listener, blockfactor, returntype, columnKey);
					break;
				}
				throw new ApplicationException("the function QueryLazy can only be used for queries returning a resultset");
			}
			while (true);
		}
		catch (SQLException e) {
			throw new DatabaseException(e, sql, dc);
		}
		catch (Throwable e) {
			ExceptionUtil.rethrowIfNecessary(e);
			throw Caster.toPageException(e);
		}
		finally {
			DBUtil.closeEL(res);
			DBUtil.closeEL(stat);
			manager.releaseConnection(pc, dc);
		}

		return null;
	}

	private static void exe(PageContext pc, ResultSet res, TimeZone tz, UDF listener, int blockfactor, int returntype, Collection.Key columnKey)
			throws SQLException, PageException, IOException {
		ResultSetMetaData meta = res.getMetaData();

		// init columns
		int columncount = meta.getColumnCount();
		List<Key> tmpKeys = new ArrayList<Key>();
		// List<Integer> tmpTypes=new ArrayList<Integer>();
		// int count=0;
		Collection.Key key;
		String columnName;
		int type;
		boolean fns = NullSupportHelper.full(pc);
		Col[] columns = new Col[columncount];
		for (int i = 0; i < columncount; i++) {

			columnName = QueryUtil.getColumnName(meta, i + 1);
			if (StringUtil.isEmpty(columnName)) columnName = "column_" + i;
			type = meta.getColumnType(i + 1);
			key = KeyImpl.init(columnName);
			int index = tmpKeys.indexOf(key);
			if (index == -1) {
				// mappings.put(key.getLowerString(), Caster.toInteger(i+1));
				tmpKeys.add(key);
				// tmpTypes.add(type);
				columns[i] = new Col(res, key, type, i + 1, tz, fns);
			}
		}

		// loop data
		boolean blocks = blockfactor > 1;
		boolean isQuery = false;
		boolean isArray = false;
		boolean isStruct = false;
		Struct row;

		Array _arrRows = null;
		Struct sctRows = null;
		QueryImpl qryRows = null;
		if (blocks) {
			if (returntype == RETURN_TYPE_ARRAY) {
				_arrRows = new ArrayImpl();
				isArray = true;
			}
			else if (returntype == RETURN_TYPE_STRUCT) {
				sctRows = new StructImpl();
				isStruct = true;
			}
			else {
				qryRows = new QueryImpl(tmpKeys.toArray(new Collection.Key[tmpKeys.size()]), blockfactor, "queryLazy");
				isQuery = true;
			}

		}
		int rownbr = 0;
		while (res.next()) {
			rownbr++;
			// create row
			row = new StructImpl();
			for (Col col: columns) {
				if (isQuery) col.set(qryRows, rownbr);
				else col.set(row);
			}
			if (blocks) {

				if (isArray) {
					_arrRows.appendEL(row);
					if (blockfactor == rownbr) {
						if (!Caster.toBooleanValue(listener.call(pc, new Object[] { _arrRows }, true), true)) {
							rownbr = 0;
							break;
						}
						_arrRows = new ArrayImpl();
						rownbr = 0;
					}
				}
				else if (isStruct) {
					sctRows.set(KeyImpl.toKey(row.get(columnKey)), row);
					if (blockfactor == rownbr) {
						if (!Caster.toBooleanValue(listener.call(pc, new Object[] { sctRows }, true), true)) {
							rownbr = 0;
							break;
						}
						sctRows = new StructImpl();
						rownbr = 0;
					}
				}
				else if (isQuery) {
					if (blockfactor == rownbr) {
						if (!Caster.toBooleanValue(listener.call(pc, new Object[] { qryRows }, true), true)) {
							rownbr = 0;
							break;
						}
						qryRows = new QueryImpl(tmpKeys.toArray(new Collection.Key[tmpKeys.size()]), blockfactor, "queryLazy");
						rownbr = 0;
					}
				}
			}
			else {
				if (!Caster.toBooleanValue(listener.call(pc, new Object[] { row }, true), true)) break;
			}

		}
		// send the remaing to the UDF
		if (blocks && rownbr > 0) {
			if (isArray) {
				listener.call(pc, new Object[] { _arrRows }, true);
			}
			else if (isStruct) {
				listener.call(pc, new Object[] { sctRows }, true);
			}
			else if (isQuery) {
				if (rownbr < blockfactor) {
					// shrink
					qryRows.removeRows(rownbr, (blockfactor - rownbr));
				}
				listener.call(pc, new Object[] { qryRows }, true);
			}
		}
	}

	private static void setAttributes(Statement stat, int maxrow, int fetchsize, TimeSpan timeout, boolean isMySQL) throws SQLException {
		if (maxrow > -1) stat.setMaxRows(maxrow);

		if (isMySQL) stat.setFetchSize(Integer.MIN_VALUE); // this is necessary for mysql otherwise all data are loaded into memory
		else if (fetchsize > 0) stat.setFetchSize(fetchsize);
		int to = QueryImpl.getSeconds(timeout);
		if (to > 0) DataSourceUtil.setQueryTimeoutSilent(stat, to);
	}

	private static void setItems(PageContext pc, TimeZone tz, PreparedStatement preStat, SQLItem[] items) throws DatabaseException, PageException, SQLException {
		for (int i = 0; i < items.length; i++) {
			SQLCaster.setValue(pc, tz, preStat, i + 1, items[i]);
		}
	}

	private static String getString(PageContext pc, Struct options, Collection.Key key, String defaultValue) throws PageException {
		if (options == null) return defaultValue;
		String str = Caster.toString(options.get(key, null), null);
		if (StringUtil.isEmpty(str)) return defaultValue;
		return str;
	}

	private static Collection.Key getKey(PageContext pc, Struct options, Collection.Key key, Collection.Key defaultValue) throws PageException {
		if (options == null) return defaultValue;
		Collection.Key str = Caster.toKey(options.get(key, null), null);
		if (StringUtil.isEmpty(str)) return defaultValue;
		return str;
	}

	private static int getInt(PageContext pc, Struct options, Collection.Key key, int defaultValue) {
		if (options == null) return defaultValue;
		return Caster.toIntValue(options.get(key, null), defaultValue);
	}

	public static int getReturntype(PageContext pc, Struct options) throws PageException {
		String strReturntype = getString(pc, options, KeyConstants._returntype, null);

		if (StringUtil.isEmpty(strReturntype)) return RETURN_TYPE_QUERY;
		strReturntype = strReturntype.toLowerCase().trim();

		if (strReturntype.equals("query")) return RETURN_TYPE_QUERY;
		else if (strReturntype.equals("struct")) return RETURN_TYPE_STRUCT;
		else if (strReturntype.equals("array") || strReturntype.equals("array_of_struct") || strReturntype.equals("array-of-struct") || strReturntype.equals("arrayofstruct")
				|| strReturntype.equals("array_of_entity") || strReturntype.equals("array-of-entity") || strReturntype.equals("arrayofentities")
				|| strReturntype.equals("array_of_entities") || strReturntype.equals("array-of-entities") || strReturntype.equals("arrayofentities"))
			return RETURN_TYPE_ARRAY;

		else throw new ApplicationException("option returntype for function QueryLazy invalid value",
				"valid values are [query,array,struct] but value is now [" + strReturntype + "]");
	}

	private static TimeZone getTimeZone(PageContext pc, Struct options, DataSource ds) throws PageException {
		Object obj = options == null ? null : options.get(KeyConstants._timezone, null);
		if (StringUtil.isEmpty(obj)) obj = null;

		if (obj != null) return Caster.toTimeZone(obj);
		else if (ds.getTimeZone() != null) return ds.getTimeZone();
		return pc.getTimeZone();
	}

	public static TimeSpan getTimeout(PageContext pc, Struct options) throws PageException {
		Object obj = options == null ? null : options.get(KeyConstants._timeout, null);
		if (obj == null || StringUtil.isEmpty(obj)) return null;

		if (obj instanceof TimeSpan) return (TimeSpan) obj;
		// seconds
		else {
			int i = Caster.toIntValue(obj);
			if (i < 0) throw new ApplicationException("invalid value [" + i + "] for attribute timeout, value must be a positive integer greater or equal than 0");

			return new TimeSpanImpl(0, 0, 0, i);
		}
	}

	private static SQL getSQL(PageContext pc, String strSQL, Object params) throws PageException {
		if (params != null) {
			if (params instanceof Argument) return QueryParamConverter.convert(strSQL, (Argument) params);
			if (Decision.isArray(params)) return QueryParamConverter.convert(strSQL, Caster.toArray(params));
			if (Decision.isStruct(params)) return QueryParamConverter.convert(strSQL, Caster.toStruct(params));
			throw new DatabaseException("value of the attribute [params] has to be a struct or an array", null, null, null);
		}
		return new SQLImpl(strSQL);
	}

	private static DataSource getDatasource(PageContext pc, Struct options) throws PageException {
		DataSource ds = null;
		Object obj = options == null ? null : options.get(KeyConstants._datasource, null);
		if (obj != null) ds = Query.toDatasource(pc, obj);

		// no datasource definition
		if (ds == null) {
			obj = pc.getApplicationContext().getDefDataSource();
			if (StringUtil.isEmpty(obj)) {
				boolean isCFML = pc.getRequestDialect() == CFMLEngine.DIALECT_CFML;
				throw new ApplicationException("option [datasource] is required when option [dbtype] is not [query] and no default datasource is defined",
						"you can define a default datasource as attribute [defaultdatasource] of the tag "
								+ (isCFML ? Constants.CFML_APPLICATION_TAG_NAME : Constants.LUCEE_APPLICATION_TAG_NAME) + " or as data member of the "
								+ (isCFML ? Constants.CFML_APPLICATION_EVENT_HANDLER : Constants.LUCEE_APPLICATION_EVENT_HANDLER) + " (this.defaultdatasource=\"mydatasource\";)");
			}
			ds = obj instanceof DataSource ? (DataSource) obj : Query.toDatasource(pc, obj);
		}
		return ds;
	}

	public static class Col {

		private ResultSet res;
		private Key key;
		private int index;
		private Cast cast;
		private TimeZone tz;
		private boolean fullNullSupport;

		public Col(ResultSet res, Key key, int type, int index, TimeZone tz, boolean fullNullSupport) {
			this.res = res;
			this.key = key;
			this.index = index;
			this.tz = tz;
			this.fullNullSupport = fullNullSupport;

			try {
				cast = QueryUtil.toCast(res, type);
			}
			catch (Exception e) {
				throw SimpleQuery.toRuntimeExc(e);
			}
		}

		public Object get() throws SQLException, IOException {
			return cast.toCFType(tz, res, index);
		}

		public void set(Struct sct) throws SQLException, IOException, PageException {
			sct.set(key, fullNullSupport ? cast.toCFType(tz, res, index) : emptyIfNull(cast.toCFType(tz, res, index)));
		}

		public void set(QueryImpl qry, int row) throws PageException, SQLException, IOException {
			qry.setAt(key, row, fullNullSupport ? cast.toCFType(tz, res, index) : emptyIfNull(cast.toCFType(tz, res, index)));
		}

		public static Object emptyIfNull(Object obj) {
			if (obj == null) return "";
			return obj;
		}
	}
}
