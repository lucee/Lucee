package lucee.commons.cpu;

import java.util.Iterator;
import java.util.List;

import lucee.commons.cpu.CPULogger.StaticData;
import lucee.commons.io.log.Log;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.engine.ThreadLocalPageContext;

public class LogListener implements Listener {

	private Log log;
	private long index;

	public LogListener(Config config, String logName) {
		this.log = ThreadLocalPageContext.getLog(config, logName);
	}

	public LogListener(Log log) {
		this.log = log;
	}

	@Override
	public void listen(List<StaticData> staticData) {
		Iterator<StaticData> it = staticData.iterator();
		StaticData data;
		index++;
		if (index < 0) index = 1;

		StringBuilder sb;
		while (it.hasNext()) {
			data = it.next();
			sb = new StringBuilder();

			sb.append("{'id':").append(index);
			sb.append(",'name':").append(StringUtil.escapeJS(data.name, '"'));
			sb.append(",'percentage':").append(data.getPercentage());
			sb.append(",'stacktrace':").append(data.getStacktrace());
			sb.append("}");
			log.info("cpu", sb.toString());
		}
	}

}
