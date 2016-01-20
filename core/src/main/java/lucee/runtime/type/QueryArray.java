package lucee.runtime.type;

import lucee.commons.lang.FormatUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.db.SQL;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.op.Caster;

public class QueryArray extends ArrayImpl {
	
	private static final long serialVersionUID = -2123873025169506446L;
	
	private final SQL sql;
	private long executionTime;
	private final String template;
	private final String name;

	public QueryArray(String name, SQL sql, String template) {
		this.name=name;
		this.sql=sql;
		this.template=template;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		DumpTable dt= (DumpTable) super.toDumpData(pageContext, maxlevel, dp);


		StringBuilder comment=new StringBuilder(); 
		
		//table.appendRow(1, new SimpleDumpData("SQL"), new SimpleDumpData(sql.toString()));
		String template=getTemplate();
		if(!StringUtil.isEmpty(template))
			comment.append("Template: ").append(template).append("\n");

		int top = dp.getMaxlevel();        

		comment.append("Execution Time: ").append(Caster.toString(FormatUtil.formatNSAsMSDouble(getExecutionTime()))).append(" ms \n");
		comment.append("Record Count: ").append(Caster.toString(size()));
		if ( size() > top )
			comment.append( " (showing top " ).append( Caster.toString( top ) ).append( ")" );
		comment.append("\n");
		comment.append("Cached: ").append(isCached()?"Yes\n":"No\n");
		/* TODO if(isCached() && query instanceof QueryImpl) {
			String ct=((QueryImpl)query).getCacheType();
			comment.append("Cache Type: ").append(ct).append("\n");
		}*/
		
		SQL sql=getSql();
		if(sql!=null)
			comment.append("SQL: ").append("\n").append(StringUtil.suppressWhiteSpace(sql.toString().trim())).append("\n");
		
		dt.setTitle("Array (from Query)");
		if(dp.getMetainfo())dt.setComment(comment.toString());
		return dt;
		
	}

	private SQL getSql() {
		return sql;
	}

	private boolean isCached() {
		// TODO 
		return false;
	}

	public long getExecutionTime() {
		return executionTime;
	}
	public void setExecutionTime(long executionTime) {
		this.executionTime=executionTime;
	}

	public String getTemplate() {
		return template;
	}
	public String getName() {
		return name;
	}

}