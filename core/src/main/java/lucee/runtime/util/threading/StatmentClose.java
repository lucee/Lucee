package lucee.runtime.util.threading;

import java.sql.Statement;

import lucee.commons.db.DBUtil;
import lucee.commons.io.log.Log;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.db.DatasourceManagerImpl;
import lucee.runtime.exp.PageException;

public class StatmentClose implements CloserJob {

	private DatasourceManagerImpl manager;
	private DatasourceConnection dc;
	private Statement stat;
	private Log log;

	public StatmentClose(DatasourceManagerImpl manager, DatasourceConnection dc, Statement stat, Log log) {
		this.manager = manager;
		this.dc = dc;
		this.stat = stat;
		this.log = log;
	}

	@Override
	public String getLablel() {
		return "closing datasource connection resultset";
	}

	@Override
	public void execute() throws PageException {
		// TODO add virtual threads
		new Thread(() -> {
			try {
				DBUtil.closeEL(stat);
				manager.releaseConnection(null, dc);
				if (log != null) log.debug("StatmentClose", "sucessfully closed resultset");
			}
			catch (Exception e) {
				if (log != null) log.error("StatmentClose", e);
			}
		}).start();
	}

}