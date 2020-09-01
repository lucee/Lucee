package lucee.runtime.type.query;

import lucee.commons.io.SystemUtil.TemplateLine;
import lucee.commons.lang.FormatUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.db.SQL;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public class QueryArray extends ArrayImpl implements QueryResult {

	private static final long serialVersionUID = -2123873025169506446L;

	private final SQL sql;
	private long executionTime;
	private final TemplateLine templateLine;
	private final String name;

	private String cacheType;
	private int updateCount;
	private Key[] columnNames;

	public QueryArray(String name, SQL sql, TemplateLine templateLine) {
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
		comment.append("Record Count: ").append(Caster.toString(size()));
		if (size() > top) comment.append(" (showing top ").append(Caster.toString(top)).append(")");
		comment.append("\n");
		comment.append("Cached: ").append(isCached() ? "Yes\n" : "No\n");
		if (isCached()) {
			String ct = getCacheType();
			comment.append("Cache Type: ").append(ct).append("\n");
		}

		SQL sql = getSql();
		if (sql != null) comment.append("SQL: ").append("\n").append(StringUtil.suppressWhiteSpace(sql.toString().trim())).append("\n");

		dt.setTitle("Array (from Query)");
		if (dp.getMetainfo()) dt.setComment(comment.toString());
		return dt;

	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		QueryArray qa = new QueryArray(name, sql, templateLine);
		qa.cacheType = cacheType;
		qa.columnNames = columnNames;
		qa.executionTime = executionTime;
		qa.updateCount = updateCount;
		return duplicate(qa, deepCopy);
	}

	@Override
	public SQL getSql() {
		return sql;
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

	public static QueryArray toQueryArray(lucee.runtime.type.QueryImpl q) throws PageException {

		QueryArray qa = new QueryArray(q.getName(), q.getSql(), q.getTemplateLine());
		qa.setCacheType(q.getCacheType());
		qa.setColumnNames(q.getColumnNames());
		qa.setExecutionTime(q.getExecutionTime());
		qa.setUpdateCount(q.getUpdateCount());

		int rows = q.getRecordcount();
		if (rows == 0) return qa;
		Key[] columns = q.getColumnNames();

		Struct tmp;
		for (int r = 1; r <= rows; r++) {
			qa.add(tmp = new StructImpl());
			for (Key c: columns) {
				tmp.set(c, q.getAt(c, r, null));
			}
		}
		return qa;
	}

}