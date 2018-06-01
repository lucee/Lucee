package lucee.runtime.tag.query;

import java.io.ByteArrayOutputStream;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import lucee.commons.io.DevNullOutputStream;
import lucee.commons.io.SystemUtil.TemplateLine;
import lucee.commons.io.log.Log;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Pair;
import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebImpl;
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

public class QuerySpoolerTask extends SpoolerTaskSupport {
	private static final ExecutionPlan[] EXECUTION_PLANS = new ExecutionPlan[]{
			//new ExecutionPlanImpl(1,60),
			//new ExecutionPlanImpl(1,5*60),
			//new ExecutionPlanImpl(1,3600),
			//new ExecutionPlanImpl(2,24*3600),
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

	public QuerySpoolerTask(PageContext parent, QueryBean data, String sql, TemplateLine tl,String relPath) {
		super(EXECUTION_PLANS);
		this.data=data;
		this.sql=sql;
		this.tl=tl;
		this.relPath=relPath;
		//this.template=page.getPageSource().getRealpathWithVirtual();
		HttpServletRequest req = parent.getHttpServletRequest();
		serverName=req.getServerName();
		queryString=ReqRspUtil.getQueryString(req);
		cookies=SerializableCookie.toSerializableCookie(ReqRspUtil.getCookies(req,parent.getWebCharset()));
		parameters=HttpUtil.cloneParameters(req);
		requestURI=req.getRequestURI();
		headers=HttpUtil.cloneHeaders(req);
		attributes=HttpUtil.getAttributesAsStruct(req);
		requestTimeout=parent.getRequestTimeout();
	}
	
	
	
	@Override
	public Struct detail() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object execute(Config config) throws PageException {
		PageContext oldPc = ThreadLocalPageContext.get();
		PageContextImpl pc=null;
		try {
			// deamon
			if(this.pc!=null){
				pc=this.pc;
				ThreadLocalPageContext.register(pc);
			}
			// task
			else {
				Page p;
				ConfigWebImpl cwi;
				try {
					cwi = (ConfigWebImpl)config;
					DevNullOutputStream os = DevNullOutputStream.DEV_NULL_OUTPUT_STREAM;
					pc=ThreadUtil.createPageContext(cwi, os, serverName, requestURI, queryString, 
							SerializableCookie.toCookies(cookies), headers, null, parameters, attributes,true,-1);
					pc.setRequestTimeout(requestTimeout);
					p=PageSourceImpl.loadPage(pc, cwi.getPageSources(oldPc==null?pc:oldPc,null, relPath, false,false,true));
					//p=cwi.getPageSources(oldPc,null, template, false,false,true).loadPage(cwi);
				} 
				catch (PageException e) {
					return e;
				}
				pc.addPageSource(p.getPageSource(), true);
			}
			
			try {
				Query._doEndTag(pc, data, sql, tl, false);
			}
			catch(Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				if(!Abort.isSilentAbort(t)) {
					ConfigWeb c = pc.getConfig();
					if(c instanceof ConfigImpl) {
						ConfigImpl ci=(ConfigImpl) c;
						Log log = ci.getLog("application");
						if(log!=null)log.log(Log.LEVEL_ERROR,"query", t);
					}
					PageException pe = Caster.toPageException(t);
					//if(!serializable)catchBlock=pe.getCatchBlock(pc.getConfig());
					return pe;
				}
			}
			finally {
				 
	            if(pc.getHttpServletResponse() instanceof HttpServletResponseDummy) {
		            HttpServletResponseDummy rsp=(HttpServletResponseDummy) pc.getHttpServletResponse();
		            pc.flush();
		            /*contentType=rsp.getContentType();
		            Pair<String,Object>[] _headers = rsp.getHeaders();
		            if(_headers!=null)for(int i=0;i<_headers.length;i++){
		            	if(_headers[i].getName().equalsIgnoreCase("Content-Encoding"))
		            		contentEncoding=Caster.toString(_headers[i].getValue(),null);
		            }*/
	            }
			}
		}
		finally {
			pc.getConfig().getFactory().releaseLuceePageContext(pc,true);
			pc=null;
			if(oldPc!=null)ThreadLocalPageContext.register(oldPc);
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
