package lucee.commons.io.log.log4j.layout;

import java.sql.Types;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.db.SQLCaster;
import lucee.runtime.db.SQLItemImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.op.Caster;
import lucee.runtime.type.dt.DateTimeImpl;

public class DatasourceLayout extends Layout {

	private final String sqlName;
	private String tableName;
	private String sqlCustom;

	public DatasourceLayout(String name) {
		String tmp = name.length() > 128 ? name.substring(0, 124) + "..." : name;
		this.sqlName = SQLCaster.toString(new SQLItemImpl(tmp, Types.VARCHAR));

	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setCustom(String custom) {
		this.sqlCustom = SQLCaster.toString(new SQLItemImpl(custom == null ? "" : custom, Types.VARCHAR));
		if (sqlCustom.length() > 2048) this.sqlCustom = "";
	}

	@Override
	public String getHeader() {
		return super.getHeader();
	}

	@Override
	public void activateOptions() {
	}

	@Override
	public String format(LoggingEvent event) {
		// ththreadId=
		String threadId = event.getThreadName();
		if (threadId.length() > 64) threadId = threadId.substring(0, 63);

		// split application->message
		String application;
		String msg = Caster.toString(event.getMessage(), null);
		int index = msg.indexOf("->");
		if (index > -1) {
			application = msg.substring(0, index);
			if (application.length() > 64) application = application.substring(0, 63);
			msg = msg.substring(index + 2);
		}
		else application = "";

		if (msg.length() > 512) msg = msg.substring(0, 508) + "...";

		// get Exception
		String exception = "";
		ThrowableInformation ti = event.getThrowableInformation();
		if (ti != null) {
			Throwable t = ti.getThrowable();
			String em = ExceptionUtil.getMessage(t);
			if (StringUtil.isEmpty(msg)) msg = em;
			else msg += ";" + em;

			exception = ExceptionUtil.getStacktrace(t, false);
			if (exception == null) exception = "";
			else if (exception.length() > 2048) exception = exception.substring(0, 2044) + "...";
		}

		// id
		String id = "";
		Config c = ThreadLocalPageContext.getConfig();
		if (c != null) {
			if (c instanceof ConfigWeb) id = ((ConfigWeb) c).getLabel();

			else id = c.getIdentification().getId();
		}

		/*
		 * if(StringUtil.isEmpty(tableName)) { Enumeration e = event.getLogger().getAllAppenders(); Object
		 * o; while(e.hasMoreElements()) { o = e.nextElement(); if(o instanceof DatasourceAppender) {
		 * tableName=((DatasourceAppender)o).getTableName(); break; } } }
		 */

		String sql = "INSERT INTO " + tableName + " (id,name,severity,threadid,time,application,message,exception,custom) VALUES("
				+ SQLCaster.toString(new SQLItemImpl(id, Types.VARCHAR)) + "," + sqlName + "," + SQLCaster.toString(new SQLItemImpl(event.getLevel().toString(), Types.VARCHAR))
				+ "," + SQLCaster.toString(new SQLItemImpl(threadId, Types.VARCHAR)) + "," + new DateTimeImpl(event.getTimeStamp(), false) + ","
				+ SQLCaster.toString(new SQLItemImpl(application, Types.VARCHAR)) + "," + SQLCaster.toString(new SQLItemImpl(msg, Types.VARCHAR)) + ","
				+ SQLCaster.toString(new SQLItemImpl(exception, Types.VARCHAR)) + "," + sqlCustom + ")";
		return sql;
	}

	@Override
	public boolean ignoresThrowable() {
		return false;
	}
}