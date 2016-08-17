package lucee.runtime.monitor;

import java.io.IOException;

import lucee.runtime.PageContext;

public interface RequestMonitorPro extends RequestMonitor {

	public void init(PageContext pc) throws IOException;

}