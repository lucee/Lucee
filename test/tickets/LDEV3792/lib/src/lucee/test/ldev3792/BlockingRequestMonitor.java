package lucee.test.ldev3792;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigServer;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.exp.Abort;
import lucee.runtime.exp.PageException;
import lucee.runtime.monitor.RequestMonitor;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;

public class BlockingRequestMonitor implements RequestMonitor {

	private String name;
	private boolean logEnabled;

	public void init(PageContext pc) throws IOException, PageException {
		HttpServletRequest req = pc.getHttpServletRequest();
		if (req.getRequestURI().indexOf("/LDEV3792/") == -1 || !"true".equals(String.valueOf(pc.urlScope().get(KeyImpl.init("block"), null)))) return;

		HttpServletResponse rsp = pc.getHttpServletResponse();
		rsp.setStatus(403);
		rsp.setContentType("text/plain");
		pc.write("blocked by request monitor");
		throw new Abort(Abort.SCOPE_REQUEST);
	}

	@Override
	public void init(ConfigServer configServer, String name, boolean logEnabled) {
		this.name = name;
		this.logEnabled = logEnabled;
	}

	@Override
	public void log(PageContext pc, boolean error) throws IOException {
	}

	@Override
	public Query getData(ConfigWeb config, Map<String, Object> arguments) throws PageException {
		return null;
	}

	@Override
	public short getType() {
		return TYPE_REQUEST;
	}

	@Override
	public String getName() {
		return name;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getClazz() {
		return getClass();
	}

	@Override
	public boolean isLogEnabled() {
		return logEnabled;
	}
}
