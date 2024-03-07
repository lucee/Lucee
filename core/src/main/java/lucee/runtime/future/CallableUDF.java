package lucee.runtime.future;

import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;

import lucee.commons.io.DevNullOutputStream;
import lucee.commons.lang.Pair;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.net.http.HttpUtil;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.thread.SerializableCookie;
import lucee.runtime.thread.ThreadUtil;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;

public class CallableUDF implements Callable<Object> {

	private UDF udf;
	private String serverName;
	private String queryString;
	private SerializableCookie[] cookies;
	private Pair<String, String>[] parameters;
	private String requestURI;
	private Pair<String, String>[] headers;
	private Struct attributes;
	private long requestTimeout;
	private ConfigWeb cw;
	private Object arg;
	private ApplicationContext ac;

	public CallableUDF(PageContext parent, UDF udf, Object arg) {
		// this.template=page.getPageSource().getRealpathWithVirtual();
		HttpServletRequest req = parent.getHttpServletRequest();
		serverName = req.getServerName();
		queryString = ReqRspUtil.getQueryString(req);
		cookies = SerializableCookie.toSerializableCookie(ReqRspUtil.getCookies(req, parent.getWebCharset()));
		parameters = HttpUtil.cloneParameters(req);
		requestURI = req.getRequestURI();
		headers = HttpUtil.cloneHeaders(req);
		attributes = HttpUtil.getAttributesAsStruct(req);
		requestTimeout = parent.getRequestTimeout();
		
		// ApplicationContext
		ac = parent.getApplicationContext();

		cw = parent.getConfig();
		this.udf = udf;
		this.arg = arg;
	}

	@Override
	public Object call() throws Exception {
		PageContext pc = null;
		ThreadLocalPageContext.register(pc);

		DevNullOutputStream os = DevNullOutputStream.DEV_NULL_OUTPUT_STREAM;
		pc = ThreadUtil.createPageContext(cw, os, serverName, requestURI, queryString, SerializableCookie.toCookies(cookies), headers, null, parameters, attributes, true, -1);
		pc.setRequestTimeout(requestTimeout);
		
		pc.setApplicationContext(ac);

		try {
			return udf.call(pc, arg == Future.ARG_NULL ? new Object[] {} : new Object[] { arg }, true);
		}
		finally {
			pc.getConfig().getFactory().releasePageContext(pc);
		}

	}

}
