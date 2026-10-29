package lucee.runtime.monitor;

import java.io.IOException;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

public interface RequestMonitorPro extends RequestMonitor {

	/**
	 * Called at the start of a request, before the Application listener runs. A silent Abort (for
	 * example lucee.runtime.exp.Abort with scope request) thrown from here aborts the request.
	 */
	public void init(PageContext pc) throws IOException, PageException;

}