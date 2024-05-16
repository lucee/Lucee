package lucee.runtime.monitor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigServer;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Query;

public class RequestMonitorProImpl implements RequestMonitorPro {

	private RequestMonitor monitor;// do not change that name, used by Argus Monitor
	private Method init;

	public RequestMonitorProImpl(RequestMonitor monitor) {
		this.monitor = monitor;

		// do we have an init method?
		try {
			init = monitor.getClass().getDeclaredMethod("init", new Class[] { PageContext.class });
		}
		catch (Exception e) {
		}
	}

	@Override
	public Class getClazz() {
		return monitor.getClazz();
	}

	@Override
	public String getName() {
		return monitor.getName();
	}

	@Override
	public short getType() {
		return monitor.getType();
	}

	@Override
	public void init(ConfigServer cs, String name, boolean logEnable) {
		monitor.init(cs, name, logEnable);
	}

	@Override
	public boolean isLogEnabled() {
		return monitor.isLogEnabled();
	}

	@Override
	public Query getData(ConfigWeb config, Map<String, Object> arguments) throws PageException {
		return monitor.getData(config, arguments);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lucee.runtime.monitor.RequestMonitorPro#init(lucee.runtime.PageContext)
	 */
	@Override
	public void init(PageContext pc) throws IOException {
		if (init != null) {
			try {
				init.invoke(monitor, new Object[] { pc });
			}
			catch (Exception e) {
				throw ExceptionUtil.toIOException(e);
			}
		}
	}

	@Override
	public void log(PageContext arg0, boolean arg1) throws IOException {
		monitor.log(arg0, arg1);
	}

}
