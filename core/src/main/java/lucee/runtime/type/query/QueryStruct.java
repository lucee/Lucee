package lucee.runtime.type.query;

import lucee.commons.io.SystemUtil.TemplateLine;
import lucee.commons.lang.FormatUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.db.SQL;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.StructSupport;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public final class QueryStruct extends StructImpl implements QueryResult {

	private static final long serialVersionUID = -2123873025169506446L;

	private final SQL sql;
	private long executionTime;
	private final TemplateLine templateLine;
	private final String name;

	private String cacheId;
	private String cacheType;
	private int updateCount;
	private Key[] columnNames;
	private boolean isSingleRecord = false;
	private String datasourceName;

	public QueryStruct(String name, SQL sql, TemplateLine templateLine) {
		super(Struct.TYPE_LINKED);
		this.name = name;
		this.sql = sql;
		this.templateLine = templateLine;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		DumpTable dt = (DumpTable) super.toDumpData(pageContext, maxlevel, dp);

		StringBuilder comment = new StringBuilder();

		// table.appendRow(1, new SimpleDumpData("SQL"), new SimpleDumpData(sql.toString()));
		TemplateLine tl = getTemplateLine();
		if (tl != null) comment.append("Template: ").append(tl.toString(pageContext, true)).append("\n");

		int top = dp.getMaxlevel();

		comment.append("Execution Time: ").append(Caster.toString(FormatUtil.formatNSAsMSDouble(getExecutionTime()))).append(" ms \n");
		comment.append("Record Count: ").append(Caster.toString(getRecordcount()));
		if (getRecordcount() > top) comment.append(" (showing top ").append(Caster.toString(top)).append(")");
		comment.append("\n");
		comment.append("Cached: ").append(isCached() ? "Yes\n" : "No\n");
		if (isCached()) {
			String ct = getCacheType();
			comment.append("Cache Type: ").append(ct).append("\n");
		}

		String datasourceName = getDatasourceName();
		if (datasourceName != null) comment.append("Datasource: ").append(datasourceName).append("\n");

		SQL sql = getSql();
		if (sql != null) comment.append("SQL: ").append("\n").append(StringUtil.suppressWhiteSpace(sql.toString().trim())).append("\n");

		dt.setTitle("Struct (from Query)");
		if (dp.getMetainfo()) dt.setComment(comment.toString());

		return dt;
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		QueryStruct qa = new QueryStruct(name, sql, templateLine);
		qa.cacheId = cacheId;
		qa.cacheType = cacheType;
		qa.columnNames = columnNames;
		qa.executionTime = executionTime;
		qa.updateCount = updateCount;
		qa.datasourceName = datasourceName;
		copy(this, qa, deepCopy);
		return qa;
	}

	@Override
	public SQL getSql() {
		return sql;
	}

	@Override
	public void setCacheId(String cacheId) {
		this.cacheId = cacheId;
	}

	@Override
	public String getCacheId(Collection arguments, String defaultValue) {
		// for query we need no arguments for the cache
		return cacheId;
	}

	@Override
	public void setCacheType(String cacheType) {
		this.cacheType = cacheType;
	}

	@Override
	public String getCacheType() {
		return cacheType;
	}

	@Override
	public boolean isCached() {
		return cacheType != null;
	}

	public String getDatasourceName() {
		return datasourceName;
	}

	public void setDatasourceName(String datasourceName) {
		this.datasourceName = datasourceName;
	}

	@Override
	public long getExecutionTime() {
		return executionTime;
	}

	@Override
	public void setExecutionTime(long executionTime) {
		this.executionTime = executionTime;
	}

	@Override
	public String getTemplate() {
		return templateLine == null ? null : templateLine.template;
	}

	@Override
	public TemplateLine getTemplateLine() {
		return templateLine;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getRecordcount() {
		if (isSingleRecord) {
			return (size() > 0) ? 1 : 0;
		}
		return size();
	}

	@Override
	public int getColumncount() {
		return columnNames == null ? 0 : columnNames.length;
	}

	@Override
	public int getUpdateCount() {
		return updateCount;
	}

	@Override
	public void setUpdateCount(int updateCount) {
		this.updateCount = updateCount;
	}

	@Override
	public Key[] getColumnNames() {
		return columnNames;
	}

	@Override
	public void setColumnNames(Key[] columnNames) throws PageException {
		this.columnNames = columnNames;
	}

	public void setSingleRecord(boolean value) {
		this.isSingleRecord = value;
	}

	/**
	 * converts the query to a struct
	 * 
	 * @param q the query
	 * @param columnNames the column(s) whose values are used as keys. With more than one column the
	 *            result is a struct of structs (one level per column) of row structs. If more than one
	 *            row has the same combination of values, the last row wins. If null, the query must have
	 *            no more than one record and the struct is that record.
	 */
	public static QueryStruct toQueryStruct(QueryImpl q, Key[] columnNames) throws PageException {
		QueryStruct qs = new QueryStruct(q.getName(), q.getSql(), q.getTemplateLine());
		qs.setCacheType(q.getCacheType());
		qs.setCacheId(q.getCacheId(null, null));
		qs.setColumnNames(q.getColumnNames());
		qs.setExecutionTime(q.getExecutionTime());
		qs.setUpdateCount(q.getUpdateCount());

		int rows = q.getRecordcount();
		if (rows == 0) return qs;
		Key[] columns = q.getColumnNames();
		int colCount = StructImpl.optimalCapacity(columns.length, 4);

		// no column key, the query must have a single record, the struct is that record (same as cfquery with a datasource)
		if (columnNames == null || columnNames.length == 0) {
			if (rows > 1) throw new ApplicationException("Attribute [columnKey] is required when return type is set to struct and more than one record is returned");
			qs.setSingleRecord(true);
			for (Key c: columns) {
				qs.set(c, q.getAt(c, 1, null));
			}
			return qs;
		}

		for (Key columnName: columnNames) {
			if (q.getColumn(columnName, null) == null) {
				Struct keys = new StructImpl();
				for (Key c: columns) {
					keys.set(c, "");
				}
				throw StructSupport.invalidKey(null, keys, columnName, "query");
			}
		}

		Struct tmp;
		for (int r = 1; r <= rows; r++) {
			tmp = new StructImpl(Struct.TYPE_LINKED, colCount);
			for (Key c: columns) {
				tmp.setEL(c, q.getAt(c, r, null));
			}
			put(qs, columnNames, tmp, columns, "query");
		}
		return qs;
	}

	/**
	 * adds a row to the target struct. The key is the value of the column(s) defined by keyColumns in
	 * that row, with more than one column the row is added to nested structs, one level per column. If
	 * the key (combination) already exists the row replaces the existing one.
	 * 
	 * @param target the struct to add the row to
	 * @param keyColumns the columns whose values are used as keys
	 * @param row the row
	 * @param columnNames all column names, used for the exception if a key column does not exist
	 * @param source describes the source of the data, used for the exception if a key column does not
	 *            exist
	 */
	public static void put(Struct target, Key[] keyColumns, Struct row, Key[] columnNames, String source) throws PageException {
		Struct current = target;
		Object value, sub;
		Key key;
		int last = keyColumns.length - 1;
		for (int i = 0; i <= last; i++) {
			value = row.get(keyColumns[i], CollectionUtil.NULL);
			if (value == CollectionUtil.NULL) {
				Struct keys = new StructImpl();
				if (columnNames != null) {
					for (Key c: columnNames) {
						keys.set(c, "");
					}
				}
				throw StructSupport.invalidKey(null, keys, keyColumns[i], source);
			}
			key = value == null ? KeyImpl.init("") : Caster.toKey(value);
			if (i == last) {
				current.set(key, row);
			}
			else {
				sub = current.get(key, null);
				if (!(sub instanceof Struct)) {
					sub = new StructImpl(Struct.TYPE_LINKED);
					current.set(key, sub);
				}
				current = (Struct) sub;
			}
		}
	}

	@Override
	public int getCachetype() {
		return Config.CACHE_TYPE_QUERY;
	}
}