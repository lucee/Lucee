package lucee.runtime.tag.query;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lucee.commons.io.DevNullOutputStream;
import lucee.commons.io.SystemUtil.TemplateLine;
import lucee.commons.io.log.Log;
import lucee.commons.lang.Pair;
import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.MappingImpl.SerMapping;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.engine.ThreadLocalConfig;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.Abort;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.http.HttpServletResponseDummy;
import lucee.runtime.net.http.HttpUtil;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.spooler.ExecutionPlan;
import lucee.runtime.spooler.SpoolerTaskSupport;
import lucee.runtime.tag.Query;
import lucee.runtime.thread.SerializableCookie;
import lucee.runtime.thread.ThreadUtil;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDFPropertiesImpl;

public final class QuerySpoolerTask extends SpoolerTaskSupport {

	private static final long serialVersionUID = 2450199479366505177L;

	// Async queries should execute immediately with no retry plans
	private static final ExecutionPlan[] EXECUTION_PLANS = null;

	private transient PageContextImpl pc;
	private String serverName;
	private String queryString;
	private SerializableCookie[] cookies;
	private Pair<String, String>[] parameters;
	private String requestURI;
	private Pair<String, String>[] headers;
	private Struct attributes;
	private long requestTimeout;
	private QueryBean data;
	private String sql;
	private TemplateLine tl;
	private String relPath;
	private String relPathwV;

	private SerMapping mapping;
	// private String absPath;

	public QuerySpoolerTask(PageContext parent, QueryBean data, String sql, TemplateLine tl, PageSource ps) {
		super(EXECUTION_PLANS);
		this.data = data;
		this.sql = sql;
		this.tl = tl;
		this.relPath = ps.getRealpath();
		this.relPathwV = ps.getRealpathWithVirtual();
		Mapping m = ps.getMapping();
		this.mapping = m instanceof MappingImpl ? ((MappingImpl) m).toSerMapping() : null;
		HttpServletRequest req = parent.getHttpServletRequest();
		serverName = req.getServerName();
		queryString = ReqRspUtil.getQueryString(req);
		cookies = SerializableCookie.toSerializableCookie(ReqRspUtil.getCookies(req, parent.getWebCharset()));
		parameters = HttpUtil.cloneParameters(parent, req);
		requestURI = req.getRequestURI();
		headers = HttpUtil.cloneHeaders(req);
		attributes = HttpUtil.getAttributesAsStruct(req);
		requestTimeout = parent.getRequestTimeout();
	}

	@Override
	public Struct detail() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object execute(Config config) throws PageException {
		PageContext oldPc = ThreadLocalPageContext.get();
		PageContextImpl pc = null;

		// daemon
		if (this.pc != null) {
			pc = this.pc;
		}
		// task
		else {
			ConfigWebPro cwi = (ConfigWebPro) config;
			HttpSession session = oldPc != null && oldPc.getSessionType() == Config.SESSION_TYPE_JEE ? oldPc.getSession() : null;
			DevNullOutputStream os = DevNullOutputStream.DEV_NULL_OUTPUT_STREAM;
			pc = ThreadUtil.createPageContext(cwi, os, serverName, requestURI, queryString, SerializableCookie.toCookies(cookies), headers, null, parameters, attributes, true,
					-1, session, null);
			pc.setRequestTimeout(requestTimeout);
			PageSource ps = UDFPropertiesImpl.toPageSource(pc, cwi, mapping == null ? null : mapping.toMapping(cwi), relPath, relPathwV);
			pc.addPageSource(ps, true);
		}

		// Java 25: Establish ScopedValue scope for query execution
		final PageContextImpl fpc = pc;
		try {
			return ScopedValue.where( ThreadLocalPageContext.CURRENT, fpc )
					.where( ThreadLocalConfig.CURRENT, fpc.getConfig() )
					.call( () -> executeWithScope( fpc, oldPc ) );
		}
		catch (PageException e) {
			throw e;
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private Object executeWithScope(PageContextImpl pc, PageContext oldPc) throws PageException {
		try {

			try {
				Query._doEndTag(pc, data, sql, tl, false);
			}
			catch (Exception e) {
				if (!Abort.isSilentAbort(e)) {
					ConfigWeb c = pc.getConfig();
					Log log = ThreadLocalPageContext.getLog(pc, "exception");
					if (log != null) log.log(Log.LEVEL_ERROR, "async-query", e);
					// Don't throw - we're in a background thread, just log the error
					// Async queries are fire-and-forget, so this is the only way to report failures
				}
			}
			finally {
				if (pc.getHttpServletResponse() instanceof HttpServletResponseDummy) {
					// HttpServletResponseDummy rsp=(HttpServletResponseDummy) pc.getHttpServletResponse();
					pc.flush();
					/*
					 * contentType=rsp.getContentType(); Pair<String,Object>[] _headers = rsp.getHeaders();
					 * if(_headers!=null)for(int i=0;i<_headers.length;i++){
					 * if(_headers[i].getName().equalsIgnoreCase("Content-Encoding"))
					 * contentEncoding=Caster.toString(_headers[i].getValue(),null); }
					 */
				}
			}
		}
		finally {
			pc.getConfig().getFactory().releaseLuceePageContext(pc, true);
			// Note: oldPc restore not needed - ScopedValue scope handles cleanup automatically
		}
		return null;
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String subject() {
		// TODO Auto-generated method stub
		return null;
	}

}
