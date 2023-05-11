package lucee.runtime.tag.query;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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

public class QuerySpoolerTask extends SpoolerTaskSupport {

	private static final long serialVersionUID = 2450199479366505177L;

	private static final ExecutionPlan[] EXECUTION_PLANS = new ExecutionPlan[] {
			// new ExecutionPlanImpl(1,60),
			// new ExecutionPlanImpl(1,5*60),
			// new ExecutionPlanImpl(1,3600),
			// new ExecutionPlanImpl(2,24*3600),
	};

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
		parameters = HttpUtil.cloneParameters(req);
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
		try {
			// daemon
			if (this.pc != null) {
				pc = this.pc;
				ThreadLocalPageContext.register(pc);
			}
			// task
			else {
				ConfigWebPro cwi = (ConfigWebPro) config;
				HttpSession session = oldPc != null && oldPc.getSessionType() == Config.SESSION_TYPE_JEE ? oldPc.getSession() : null;
				DevNullOutputStream os = DevNullOutputStream.DEV_NULL_OUTPUT_STREAM;
				pc = ThreadUtil.createPageContext(cwi, os, serverName, requestURI, queryString, SerializableCookie.toCookies(cookies), headers, null, parameters, attributes, true,
						-1, session);
				pc.setRequestTimeout(requestTimeout);
				PageSource ps = UDFPropertiesImpl.toPageSource(pc, cwi, mapping == null ? null : mapping.toMapping(), relPath, relPathwV);
				pc.addPageSource(ps, true);
			}

			try {
				Query._doEndTag(pc, data, sql, tl, false);
			}
			catch (Exception e) {
				if (!Abort.isSilentAbort(e)) {
					ConfigWeb c = pc.getConfig();
					Log log = ThreadLocalPageContext.getLog(pc, "application");
					if (log != null) log.log(Log.LEVEL_ERROR, "query", e);
					PageException pe = Caster.toPageException(e);
					// if(!serializable)catchBlock=pe.getCatchBlock(pc.getConfig());
					return pe;
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
			pc = null;
			if (oldPc != null) ThreadLocalPageContext.register(oldPc);
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
