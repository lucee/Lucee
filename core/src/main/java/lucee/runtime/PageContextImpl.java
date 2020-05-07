/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package lucee.runtime;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.TimeZone;

import javax.el.ELContext;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TryCatchFinally;

import lucee.commons.db.DBUtil;
import lucee.commons.io.BodyContentStack;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.cache.exp.CacheException;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceClassLoader;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.PhysicalClassLoader;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.mimetype.MimeType;
import lucee.commons.lang.types.RefBoolean;
import lucee.commons.lang.types.RefBooleanImpl;
import lucee.commons.lock.KeyLock;
import lucee.commons.lock.Lock;
import lucee.commons.net.HTTPUtil;
import lucee.intergral.fusiondebug.server.FDSignal;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.cache.CacheConnection;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.cache.tag.CacheHandler;
import lucee.runtime.cache.tag.CacheHandlerCollectionImpl;
import lucee.runtime.cache.tag.CacheHandlerPro;
import lucee.runtime.cache.tag.CacheItem;
import lucee.runtime.cache.tag.include.IncludeCacheItem;
import lucee.runtime.component.ComponentLoader;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.config.Constants;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.config.Password;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DataSourceManager;
import lucee.runtime.db.DatasourceManagerImpl;
import lucee.runtime.debug.ActiveLock;
import lucee.runtime.debug.ActiveQuery;
import lucee.runtime.debug.DebugCFMLWriter;
import lucee.runtime.debug.DebugEntryTemplate;
import lucee.runtime.debug.Debugger;
import lucee.runtime.debug.DebuggerImpl;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.dump.DumpWriter;
import lucee.runtime.engine.ExecutionLog;
import lucee.runtime.err.ErrorPage;
import lucee.runtime.err.ErrorPageImpl;
import lucee.runtime.err.ErrorPagePool;
import lucee.runtime.esapi.ESAPIUtil;
import lucee.runtime.exp.Abort;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.ExceptionHandler;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.MissingIncludeException;
import lucee.runtime.exp.NoLongerSupported;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageExceptionBox;
import lucee.runtime.exp.RequestTimeoutException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.functions.dynamicEvaluation.Serialize;
import lucee.runtime.interpreter.CFMLExpressionInterpreter;
import lucee.runtime.interpreter.VariableInterpreter;
import lucee.runtime.listener.AppListenerSupport;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.listener.ApplicationContextSupport;
import lucee.runtime.listener.ApplicationListener;
import lucee.runtime.listener.ClassicApplicationContext;
import lucee.runtime.listener.JavaSettingsImpl;
import lucee.runtime.listener.ModernAppListener;
import lucee.runtime.listener.ModernAppListenerException;
import lucee.runtime.listener.SessionCookieData;
import lucee.runtime.listener.SessionCookieDataImpl;
import lucee.runtime.monitor.RequestMonitor;
import lucee.runtime.monitor.RequestMonitorPro;
import lucee.runtime.net.ftp.FTPPoolImpl;
import lucee.runtime.net.http.HTTPServletRequestWrap;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.net.mail.ServerImpl;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.op.Operator;
import lucee.runtime.orm.ORMConfiguration;
import lucee.runtime.orm.ORMEngine;
import lucee.runtime.orm.ORMSession;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.regex.Perl5Util;
import lucee.runtime.rest.RestRequestListener;
import lucee.runtime.rest.RestUtil;
import lucee.runtime.security.Credential;
import lucee.runtime.security.CredentialImpl;
import lucee.runtime.security.ScriptProtect;
import lucee.runtime.tag.Login;
import lucee.runtime.tag.TagHandlerPool;
import lucee.runtime.tag.TagUtil;
import lucee.runtime.thread.ThreadsImpl;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Iterator;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.SVArray;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.it.ItAsEnum;
import lucee.runtime.type.ref.Reference;
import lucee.runtime.type.ref.VariableReference;
import lucee.runtime.type.scope.Application;
import lucee.runtime.type.scope.Argument;
import lucee.runtime.type.scope.ArgumentImpl;
import lucee.runtime.type.scope.CFThread;
import lucee.runtime.type.scope.CGI;
import lucee.runtime.type.scope.CGIImpl;
import lucee.runtime.type.scope.CGIImplReadOnly;
import lucee.runtime.type.scope.Client;
import lucee.runtime.type.scope.ClosureScope;
import lucee.runtime.type.scope.Cluster;
import lucee.runtime.type.scope.Cookie;
import lucee.runtime.type.scope.CookieImpl;
import lucee.runtime.type.scope.Form;
import lucee.runtime.type.scope.FormImpl;
import lucee.runtime.type.scope.Local;
import lucee.runtime.type.scope.LocalNotSupportedScope;
import lucee.runtime.type.scope.Request;
import lucee.runtime.type.scope.RequestImpl;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.scope.ScopeContext;
import lucee.runtime.type.scope.ScopeFactory;
import lucee.runtime.type.scope.ScopeSupport;
import lucee.runtime.type.scope.Server;
import lucee.runtime.type.scope.Session;
import lucee.runtime.type.scope.Threads;
import lucee.runtime.type.scope.URL;
import lucee.runtime.type.scope.URLForm;
import lucee.runtime.type.scope.URLImpl;
import lucee.runtime.type.scope.Undefined;
import lucee.runtime.type.scope.UndefinedImpl;
import lucee.runtime.type.scope.UrlFormImpl;
import lucee.runtime.type.scope.Variables;
import lucee.runtime.type.scope.VariablesImpl;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.util.PageContextUtil;
import lucee.runtime.util.VariableUtil;
import lucee.runtime.util.VariableUtilImpl;
import lucee.runtime.writer.BodyContentUtil;
import lucee.runtime.writer.CFMLWriter;
import lucee.runtime.writer.DevNullBodyContent;

/**
 * page context for every page object. the PageContext is a jsp page context expanded by CFML
 * functionality. for example you have the method getSession to get jsp combatible session object
 * (HTTPSession) and with sessionScope() you get CFML combatible session object (Struct,Scope).
 */
public final class PageContextImpl extends PageContext {

	private static final RefBoolean DUMMY_BOOL = new RefBooleanImpl(false);

	private static int counter = 0;

	/**
	 * Field <code>pathList</code>
	 */
	private LinkedList<UDF> udfs = new LinkedList<UDF>();
	private LinkedList<PageSource> pathList = new LinkedList<PageSource>();
	private LinkedList<PageSource> includePathList = new LinkedList<PageSource>();
	private Set<PageSource> includeOnce = new HashSet<PageSource>();

	/**
	 * Field <code>executionTime</code>
	 */
	protected long executionTime = 0;

	private HTTPServletRequestWrap req;
	private HttpServletResponse rsp;
	private HttpServlet servlet;

	private JspWriter writer;
	private JspWriter forceWriter;
	private BodyContentStack bodyContentStack;
	private DevNullBodyContent devNull;

	private ConfigWebImpl config;
	// private DataSourceManager manager;
	// private CFMLCompilerImpl compiler;

	// Scopes
	private ScopeContext scopeContext;
	private Variables variablesRoot = new VariablesImpl();// ScopeSupport(false,"variables",Scope.SCOPE_VARIABLES);
	private Variables variables = variablesRoot;// new ScopeSupport("variables",Scope.SCOPE_VARIABLES);
	private Undefined undefined;

	private URLImpl _url = new URLImpl();
	private FormImpl _form = new FormImpl();

	private URLForm urlForm = new UrlFormImpl(_form, _url);
	private URL url;
	private Form form;

	private RequestImpl request = new RequestImpl();
	private CGIImplReadOnly cgiR = new CGIImplReadOnly();
	private CGIImpl cgiRW = new CGIImpl();
	private Argument argument = new ArgumentImpl();
	private static LocalNotSupportedScope localUnsupportedScope = LocalNotSupportedScope.getInstance();
	private Local local = localUnsupportedScope;
	private Session session;
	private Server server;
	private Cluster cluster;
	private CookieImpl cookie = new CookieImpl();
	private Client client;
	private Application application;

	private DebuggerImpl debugger = new DebuggerImpl();
	private long requestTimeout = -1;
	private short enablecfoutputonly = 0;
	private int outputState;
	private String cfid;
	private String cftoken;

	private int id;
	private int requestId;

	private Boolean _psq;
	private Locale locale;
	private TimeZone timeZone;

	// Pools
	private ErrorPagePool errorPagePool = new ErrorPagePool();
	private TagHandlerPool tagHandlerPool;
	private FTPPoolImpl ftpPool = new FTPPoolImpl();

	private Component activeComponent;
	private UDF activeUDF;
	private Collection.Key activeUDFCalledName;
	// private ComponentScope componentScope=new ComponentScope(this);

	private Credential remoteUser;

	protected VariableUtilImpl variableUtil = new VariableUtilImpl();

	private PageException exception;
	private PageSource base;

	private ApplicationContextSupport applicationContext;
	private final ApplicationContextSupport defaultApplicationContext;

	private ScopeFactory scopeFactory = new ScopeFactory();

	private Tag parentTag = null;
	private Tag currentTag = null;
	private Thread thread;
	private long startTime;

	private DatasourceManagerImpl manager;
	private CFThread threads;
	private Map<Key, Threads> allThreads;
	private boolean hasFamily = false;
	private PageContextImpl parent = null;
	private PageContextImpl root = null;

	private List<String> parentTags;
	private List<PageContext> children = null;
	private List<Statement> lazyStats;
	private boolean fdEnabled;
	private ExecutionLog execLog;
	private boolean useSpecialMappings;

	private ORMSession ormSession;
	private boolean isChild;
	private boolean gatewayContext;
	private Password serverPassword;

	private PageException pe;
	// private Throwable requestTimeoutException;

	private int currentTemplateDialect = CFMLEngine.DIALECT_CFML;
	private int requestDialect = CFMLEngine.DIALECT_CFML;
	private boolean ignoreScopes = false;

	private int appListenerType = ApplicationListener.TYPE_NONE;

	private ThreadsImpl currentThread = null;

	private StackTraceElement[] timeoutStacktrace;

	private boolean fullNullSupport;

	/**
	 * default Constructor
	 * 
	 * @param scopeContext
	 * @param config Configuration of the CFML Container
	 * @param queryCache Query Cache Object
	 * @param id identity of the pageContext
	 * @param servlet
	 */
	public PageContextImpl(ScopeContext scopeContext, ConfigWebImpl config, int id, HttpServlet servlet, boolean jsr223) {
		// must be first because is used after
		tagHandlerPool = config.getTagHandlerPool();
		this.servlet = servlet;

		bodyContentStack = new BodyContentStack();
		devNull = bodyContentStack.getDevNullBodyContent();

		this.config = config;
		manager = new DatasourceManagerImpl(config);

		this.scopeContext = scopeContext;
		undefined = new UndefinedImpl(this, getScopeCascadingType());
		server = ScopeContext.getServerScope(this, jsr223);
		defaultApplicationContext = new ClassicApplicationContext(config, "", true, null);

		this.id = id;
	}

	public boolean isInitialized() {
		return rsp != null;
	}

	/**
	 * return if the PageContext is from a stopped thread, if so it should no longer be used!
	 * 
	 * @return
	 */
	@Override
	public Throwable getRequestTimeoutException() {
		throw new RuntimeException("method no longer supported");
		// return requestTimeoutException;
	}

	/*
	 * public void setRequestTimeoutException(Throwable requestTimeoutException) {
	 * this.requestTimeoutException=requestTimeoutException;
	 * 
	 * }
	 */
	public StackTraceElement[] getTimeoutStackTrace() {
		return timeoutStacktrace;
	}

	public void setTimeoutStackTrace() {
		this.timeoutStacktrace = thread.getStackTrace();
	}

	@Override
	public void initialize(Servlet servlet, ServletRequest req, ServletResponse rsp, String errorPageURL, boolean needsSession, int bufferSize, boolean autoFlush)
			throws IOException, IllegalStateException, IllegalArgumentException {
		initialize((HttpServlet) servlet, (HttpServletRequest) req, (HttpServletResponse) rsp, errorPageURL, needsSession, bufferSize, autoFlush, false, false);
	}

	/**
	 * initialize an existing page context
	 * 
	 * @param servlet
	 * @param req
	 * @param rsp
	 * @param errorPageURL
	 * @param needsSession
	 * @param bufferSize
	 * @param autoFlush
	 */
	public PageContextImpl initialize(HttpServlet servlet, HttpServletRequest req, HttpServletResponse rsp, String errorPageURL, boolean needsSession, int bufferSize,
			boolean autoFlush, boolean isChild, boolean ignoreScopes) {
		parent = null;
		root = null;
		requestId = counter++;

		appListenerType = ApplicationListener.TYPE_NONE;
		this.ignoreScopes = ignoreScopes;

		ReqRspUtil.setContentType(rsp, "text/html; charset=" + config.getWebCharset().name());
		this.isChild = isChild;

		applicationContext = defaultApplicationContext;
		setFullNullSupport();

		startTime = System.currentTimeMillis();
		thread = Thread.currentThread();

		this.req = new HTTPServletRequestWrap(req);
		this.rsp = rsp;
		this.servlet = servlet;

		// Writers
		if (config.debugLogOutput()) {
			CFMLWriter w = config.getCFMLWriter(this, req, rsp);
			w.setAllowCompression(false);
			DebugCFMLWriter dcw = new DebugCFMLWriter(w);
			bodyContentStack.init(dcw);
			debugger.setOutputLog(dcw);
		}
		else {
			bodyContentStack.init(config.getCFMLWriter(this, req, rsp));
		}

		writer = bodyContentStack.getWriter();
		forceWriter = writer;

		// Scopes
		server = ScopeContext.getServerScope(this, ignoreScopes);
		if (hasFamily) {
			variablesRoot = new VariablesImpl();
			variables = variablesRoot;
			request = new RequestImpl();
			_url = new URLImpl();
			_form = new FormImpl();
			urlForm = new UrlFormImpl(_form, _url);
			undefined = new UndefinedImpl(this, getScopeCascadingType());

			hasFamily = false;
		}
		else if (variables == null) {
			variablesRoot = new VariablesImpl();
			variables = variablesRoot;
		}
		request.initialize(this);

		if (config.mergeFormAndURL()) {
			url = urlForm;
			form = urlForm;
		}
		else {
			url = _url;
			form = _form;
		}
		// url.initialize(this);
		// form.initialize(this);
		// undefined.initialize(this);

		_psq = null;

		fdEnabled = !config.allowRequestTimeout();

		if (config.getExecutionLogEnabled()) this.execLog = config.getExecutionLogFactory().getInstance(this);
		if (debugger != null) debugger.init(config);

		undefined.initialize(this);
		timeoutStacktrace = null;
		return this;
	}

	@Override
	public void release() {
		config.releaseCacheHandlers(this);

		if (config.getExecutionLogEnabled()) {
			execLog.release();
			execLog = null;
		}

		if (config.debug()) {
			if (!gatewayContext && !isChild) config.getDebuggerPool().store(this, debugger);
			debugger.reset();
		}
		else debugger.resetTraces(); // traces can alo be used when debugging is off

		this.serverPassword = null;

		// boolean isChild=parent!=null; // isChild is defined in the class outside this method
		parent = null;
		root = null;
		// Attention have to be before close
		if (client != null) {
			client.touchAfterRequest(this);
			client = null;
		}

		if (session != null) {
			session.touchAfterRequest(this);
			session = null;
		}

		// ORM
		// if(ormSession!=null)releaseORM();

		// Scopes
		if (hasFamily) {
			if (hasFamily && !isChild) {
				req.disconnect(this);
			}
			close();
			base = null;
			if (children != null) children.clear();

			request = null;
			_url = null;
			_form = null;
			urlForm = null;
			undefined = null;
			variables = null;
			variablesRoot = null;
			// if(threads!=null && threads.size()>0) threads.clear();
			threads = null;
			allThreads = null;
			currentThread = null;
		}
		else {
			close();
			base = null;
			if (variables.isBind()) {
				variables = null;
				variablesRoot = null;
			}
			else {
				variables = variablesRoot;
				variables.release(this);
			}
			undefined.release(this);
			urlForm.release(this);
			request.release(this);
		}
		cgiR.release(this);
		cgiRW.release(this);
		argument.release(this);
		local = localUnsupportedScope;

		cookie.release(this);
		application = null;// not needed at the moment -> application.releaseAfterRequest();
		applicationContext = null;

		// Properties
		requestTimeout = -1;
		outputState = 0;
		cfid = null;
		cftoken = null;
		locale = null;
		timeZone = null;
		url = null;
		form = null;
		currentTemplateDialect = CFMLEngine.DIALECT_LUCEE;
		requestDialect = CFMLEngine.DIALECT_LUCEE;

		// Pools
		errorPagePool.clear();

		// lazy statements
		if (lazyStats != null && !lazyStats.isEmpty()) {
			java.util.Iterator<Statement> it = lazyStats.iterator();
			while (it.hasNext()) {
				DBUtil.closeEL(it.next());
			}
			lazyStats.clear();
			lazyStats = null;
		}

		pathList.clear();
		includePathList.clear();
		executionTime = 0;

		bodyContentStack.release();

		// activeComponent=null;
		remoteUser = null;
		exception = null;
		ftpPool.clear();
		parentTag = null;
		currentTag = null;

		// Req/Rsp
		req = null;
		rsp = null;
		servlet = null;

		// Writer
		writer = null;
		forceWriter = null;
		if (pagesUsed.size() > 0) pagesUsed.clear();

		activeComponent = null;
		activeUDF = null;

		gatewayContext = false;

		manager.release();
		includeOnce.clear();
		pe = null;
		this.literalTimestampWithTSOffset = false;
		thread = null;
		tagName = null;
		parentTags = null;
		_psq = null;
		listenSettings = false;
	}

	private void releaseORM() throws PageException {
		try {
			// flush orm session
			ORMEngine engine = ormSession.getEngine();
			ORMConfiguration config = engine.getConfiguration(this);
			if (config == null || (config.flushAtRequestEnd() && config.autoManageSession())) {
				ormSession.flushAll(this);
			}
			ormSession.closeAll(this);
			manager.releaseORM();
		}
		finally {
			ormSession = null;
		}
	}

	@Override
	public void write(String str) throws IOException {
		writer.write(str);
	}

	@Override
	public void forceWrite(String str) throws IOException {
		forceWriter.write(str);
	}

	@Override
	public void writePSQ(Object o) throws IOException, PageException {
		// is var usage allowed?
		if (applicationContext != null && applicationContext.getQueryVarUsage() != ConfigImpl.QUERY_VAR_USAGE_IGNORE) {
			// Warning
			if (applicationContext.getQueryVarUsage() == ConfigImpl.QUERY_VAR_USAGE_WARN) {
				DebuggerImpl.deprecated(this, "query.variableUsage",
						"Please do not use variables within the cfquery tag, instead use the tag \"cfqueryparam\" or the attribute \"params\"");

			}
			// Error
			else if (applicationContext.getQueryVarUsage() == ConfigImpl.QUERY_VAR_USAGE_ERROR) {
				throw new ApplicationException("Variables are not allowed within cfquery, please use the tag <cfqueryparam> or the attribute \"params\" instead.");
			}
		}

		// preserve single quote
		if (o instanceof Date || Decision.isDate(o, false)) {
			writer.write(Caster.toString(o));
		}
		else {
			writer.write(getPsq() ? Caster.toString(o) : StringUtil.replace(Caster.toString(o), "'", "''", false));
		}
	}

	// FUTURE add both method to interface
	public void writeEncodeFor(String value, String encodeType) throws IOException, PageException { // FUTURE keyword:encodefore add to interface
		write(ESAPIUtil.esapiEncode(this, encodeType, value));
	}

	/*
	 * public void writeEncodeFor(String value, short encodeType) throws IOException, PageException { //
	 * FUTURE keyword:encodefore add to interface write(ESAPIUtil.esapiEncode(this,value, encodeType));
	 * }
	 */

	@Override
	public void flush() {
		try {
			getOut().flush();
		}
		catch (IOException e) {
		}
	}

	@Override
	public void close() {
		IOUtil.closeEL(getOut());
	}

	public PageSource getRelativePageSource(String realPath) {
		LogUtil.log(config, Log.LEVEL_INFO, PageContextImpl.class.getName(), "method getRelativePageSource is deprecated");
		if (StringUtil.startsWith(realPath, '/')) return PageSourceImpl.best(getPageSources(realPath));
		if (pathList.size() == 0) return null;
		return pathList.getLast().getRealPage(realPath);
	}

	public PageSource getRelativePageSourceExisting(String realPath) {
		if (StringUtil.startsWith(realPath, '/')) return getPageSourceExisting(realPath);
		if (pathList.size() == 0) return null;
		PageSource ps = pathList.getLast().getRealPage(realPath);
		if (PageSourceImpl.pageExist(ps)) return ps;
		return null;
	}

	/**
	 * 
	 * @param realPath
	 * @param previous relative not to the caller, relative to the callers caller
	 * @return
	 */
	public PageSource getRelativePageSourceExisting(String realPath, boolean previous) {
		if (StringUtil.startsWith(realPath, '/')) return getPageSourceExisting(realPath);
		if (pathList.size() == 0) return null;

		PageSource ps = null, tmp = null;
		if (previous) {
			boolean valid = false;
			ps = pathList.getLast();
			for (int i = pathList.size() - 2; i >= 0; i--) {
				tmp = pathList.get(i);
				if (tmp != ps) {
					ps = tmp;
					valid = true;
					break;
				}
			}
			if (!valid) return null;
		}
		else ps = pathList.getLast();

		ps = ps.getRealPage(realPath);
		if (PageSourceImpl.pageExist(ps)) return ps;
		return null;
	}

	public PageSource[] getRelativePageSources(String realPath) {
		if (StringUtil.startsWith(realPath, '/')) return getPageSources(realPath);
		if (pathList.size() == 0) return null;
		return new PageSource[] { pathList.getLast().getRealPage(realPath) };
	}

	public PageSource getPageSource(String realPath) {
		return PageSourceImpl.best(config.getPageSources(this, applicationContext.getMappings(), realPath, false, useSpecialMappings, true));
	}

	public PageSource[] getPageSources(String realPath) { // to not change, this is used in the flex extension
		return config.getPageSources(this, applicationContext.getMappings(), realPath, false, useSpecialMappings, true);
	}

	public PageSource getPageSourceExisting(String realPath) { // do not change, this method is used in flex extension
		return config.getPageSourceExisting(this, applicationContext.getMappings(), realPath, false, useSpecialMappings, true, false);
	}

	public boolean useSpecialMappings(boolean useTagMappings) {
		boolean b = this.useSpecialMappings;
		this.useSpecialMappings = useTagMappings;
		return b;
	}

	public boolean useSpecialMappings() {
		return useSpecialMappings;
	}

	public Resource getPhysical(String realPath, boolean alsoDefaultMapping) {
		return config.getPhysical(applicationContext.getMappings(), realPath, alsoDefaultMapping);
	}

	@Override
	public PageSource toPageSource(Resource res, PageSource defaultValue) {
		return config.toPageSource(applicationContext.getMappings(), res, defaultValue);
	}

	@Override
	public void doInclude(String realPath) throws PageException {
		_doInclude(getRelativePageSources(realPath), false, getCachedWithin(ConfigWeb.CACHEDWITHIN_INCLUDE));
	}

	@Override
	public void doInclude(String realPath, boolean runOnce) throws PageException {
		_doInclude(getRelativePageSources(realPath), runOnce, getCachedWithin(ConfigWeb.CACHEDWITHIN_INCLUDE));
	}

	// used by the transformer
	public void doInclude(String realPath, boolean runOnce, Object cachedWithin) throws PageException {
		if (cachedWithin == null) cachedWithin = getCachedWithin(ConfigWeb.CACHEDWITHIN_INCLUDE);
		_doInclude(getRelativePageSources(realPath), runOnce, cachedWithin);
	}

	@Override
	public void doInclude(PageSource[] sources, boolean runOnce) throws PageException {
		_doInclude(sources, runOnce, getCachedWithin(ConfigWeb.CACHEDWITHIN_INCLUDE));
	}

	// IMPORTANT!!! we do not getCachedWithin in this method, because Modern|ClassicAppListener is
	// calling this method and in this case it should not be used
	public void _doInclude(PageSource[] sources, boolean runOnce, Object cachedWithin) throws PageException {
		PageContextUtil.checkRequestTimeout(this);
		if (cachedWithin == null) {
			_doInclude(sources, runOnce);
			return;
		}

		// ignore call when runonce an it is not first call
		if (runOnce) {
			Page currentPage = PageSourceImpl.loadPage(this, sources);
			if (runOnce && includeOnce.contains(currentPage.getPageSource())) return;
		}

		// get cached data
		String cacheId = CacheHandlerCollectionImpl.createId(sources);
		CacheHandler cacheHandler = config.getCacheHandlerCollection(Config.CACHE_TYPE_INCLUDE, null).getInstanceMatchingObject(cachedWithin, null);

		if (cacheHandler instanceof CacheHandlerPro) {

			CacheItem cacheItem = ((CacheHandlerPro) cacheHandler).get(this, cacheId, cachedWithin);

			if (cacheItem instanceof IncludeCacheItem) {
				try {
					write(((IncludeCacheItem) cacheItem).getOutput());
					return;
				}
				catch (IOException e) {
					throw Caster.toPageException(e);
				}
			}
		}
		else if (cacheHandler != null) { // TODO this else block can be removed when all cache handlers implement CacheHandlerPro

			CacheItem cacheItem = cacheHandler.get(this, cacheId);

			if (cacheItem instanceof IncludeCacheItem) {
				try {
					write(((IncludeCacheItem) cacheItem).getOutput());
					return;
				}
				catch (IOException e) {
					throw Caster.toPageException(e);
				}
			}
		}

		// cached item not found, process and cache result if needed
		long start = System.nanoTime();
		BodyContent bc = pushBody();

		try {
			_doInclude(sources, runOnce);
			String out = bc.getString();

			if (cacheHandler != null) {
				CacheItem cacheItem = new IncludeCacheItem(out, ArrayUtil.isEmpty(sources) ? null : sources[0], System.nanoTime() - start);
				cacheHandler.set(this, cacheId, cachedWithin, cacheItem);
				return;
			}
		}
		finally {
			BodyContentUtil.flushAndPop(this, bc);
		}
	}

	private void _doInclude(PageSource[] sources, boolean runOnce) throws PageException {
		// debug
		if (!gatewayContext && config.debug()) {
			long currTime = executionTime;
			long exeTime = 0;
			long time = System.nanoTime();

			Page currentPage = PageSourceImpl.loadPage(this, sources);
			notSupported(config, currentPage.getPageSource());
			if (runOnce && includeOnce.contains(currentPage.getPageSource())) return;
			DebugEntryTemplate debugEntry = debugger.getEntry(this, currentPage.getPageSource());
			try {
				addPageSource(currentPage.getPageSource(), true);
				debugEntry.updateFileLoadTime((System.nanoTime() - time));
				exeTime = System.nanoTime();
				currentPage.call(this);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				PageException pe = Caster.toPageException(t);
				if (Abort.isAbort(pe)) {
					if (Abort.isAbort(pe, Abort.SCOPE_REQUEST)) throw pe;
				}
				else {
					if (fdEnabled) {
						FDSignal.signal(pe, false);
					}
					pe.addContext(currentPage.getPageSource(), -187, -187, null);// TODO was soll das 187
					throw pe;
				}
			}
			finally {
				includeOnce.add(currentPage.getPageSource());
				long diff = ((System.nanoTime() - exeTime) - (executionTime - currTime));
				executionTime += (System.nanoTime() - time);
				debugEntry.updateExeTime(diff);
				removeLastPageSource(true);
			}
		}
		// no debug
		else {
			Page currentPage = PageSourceImpl.loadPage(this, sources);
			notSupported(config, currentPage.getPageSource());
			if (runOnce && includeOnce.contains(currentPage.getPageSource())) return;
			try {
				addPageSource(currentPage.getPageSource(), true);
				currentPage.call(this);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				PageException pe = Caster.toPageException(t);
				if (Abort.isAbort(pe)) {
					if (Abort.isAbort(pe, Abort.SCOPE_REQUEST)) throw pe;
				}
				else {
					pe.addContext(currentPage.getPageSource(), -187, -187, null);
					throw pe;
				}
			}
			finally {
				includeOnce.add(currentPage.getPageSource());
				removeLastPageSource(true);
			}
		}
	}

	public static void notSupported(Config config, PageSource ps) throws ApplicationException {
		if (ps.getDialect() == CFMLEngine.DIALECT_LUCEE && config instanceof ConfigImpl && !((ConfigImpl) config).allowLuceeDialect()) notSupported();
	}

	public static void notSupported() throws ApplicationException {
		throw new ApplicationException(
				"The Lucee dialect is disabled, to enable the dialect set the environment variable or system property \"lucee.enable.dialect\" to \"true\" or set the attribute \"allow-lucee-dialect\" to \"true\" with the \"compiler\" tag inside the lucee-server.xml.");
	}

	@Override
	public Array getTemplatePath() throws PageException {
		int len = includePathList.size();
		SVArray sva = new SVArray();
		PageSource ps, bps;
		for (int i = 0; i < len; i++) {
			ps = includePathList.get(i);
			if (i == 0) {
				bps = getBasePageSource();
				if (bps != null && !ps.equals(bps)) sva.append(bps.getResourceTranslated(this).getAbsolutePath());
			}
			sva.append(ps.getResourceTranslated(this).getAbsolutePath());
		}
		return sva;
	}

	public List<PageSource> getPageSourceList() {
		return (List<PageSource>) pathList.clone();
	}

	public PageSource getPageSource(int index) {
		return includePathList.get(index - 1);
	}

	public synchronized void copyStateTo(PageContextImpl other) {
		// cfid (we do this that way, otherwise we only have the same cfid if the current pc has defined
		// cfid in cookie or url)
		getCFID();
		other.cfid = cfid;
		other.cftoken = cftoken;

		// private Debugger debugger=new DebuggerImpl();
		other.requestTimeout = requestTimeout;
		other.locale = locale;
		other.timeZone = timeZone;
		other.fdEnabled = fdEnabled;
		other.useSpecialMappings = useSpecialMappings;
		other.serverPassword = serverPassword;
		other.requestDialect = requestDialect;
		other.currentTemplateDialect = currentTemplateDialect;

		hasFamily = true;
		other.hasFamily = true;
		other.parent = this;
		other.root = root == null ? this : root;
		other.tagName = tagName;
		other.parentTags = parentTags == null ? null : (List) ((ArrayList) parentTags).clone();
		/*
		 * if (!StringUtil.isEmpty(tagName)) { if (other.parentTags == null) other.parentTags = new
		 * ArrayList<String>(); other.parentTags.add(tagName); }
		 */
		if (children == null) children = new ArrayList<PageContext>();
		children.add(other);
		other.applicationContext = applicationContext;
		other.setFullNullSupport();
		other.thread = Thread.currentThread();
		other.startTime = System.currentTimeMillis();

		// path
		other.base = base;
		java.util.Iterator<PageSource> it = includePathList.iterator();
		while (it.hasNext()) {
			other.includePathList.add(it.next());
		}
		it = pathList.iterator();
		while (it.hasNext()) {
			other.pathList.add(it.next());
		}

		// scopes
		other.req = req;
		other.request = request;
		other.form = form;
		other.url = url;
		other.urlForm = urlForm;
		other._url = _url;
		other._form = _form;
		other.variables = variables;
		other.undefined = new UndefinedImpl(other, (short) other.undefined.getType());

		// writers
		other.bodyContentStack.init(config.getCFMLWriter(this, other.req, other.rsp));
		// other.bodyContentStack.init(other.req,other.rsp,other.config.isSuppressWhitespace(),other.config.closeConnection(),
		// other.config.isShowVersion(),config.contentLength(),config.allowCompression());
		other.writer = other.bodyContentStack.getWriter();
		other.forceWriter = other.writer;

		other._psq = _psq;
		other.gatewayContext = gatewayContext;

		// initialize stuff
		other.undefined.initialize(other);
	}

	@Override
	public int getCurrentLevel() {
		return includePathList.size() + 1;
	}

	@Override
	public PageSource getCurrentPageSource() {
		if (pathList.isEmpty()) return null;
		return pathList.getLast();
	}

	@Override
	public PageSource getCurrentPageSource(PageSource defaultvalue) {
		if (pathList.isEmpty()) return defaultvalue;
		return pathList.getLast();
	}

	/**
	 * @return the current template PageSource
	 */
	@Override
	public PageSource getCurrentTemplatePageSource() {
		if (includePathList.isEmpty()) return null;
		return includePathList.getLast();
	}

	/**
	 * @return base template file
	 */
	@Override
	public PageSource getBasePageSource() {
		return base;
	}

	@Override
	public Resource getRootTemplateDirectory() {
		return config.getResource(ReqRspUtil.getRootPath(servlet.getServletContext()));
	}

	@Override
	public Scope scope(int type) throws PageException {
		switch (type) {
		case Scope.SCOPE_UNDEFINED:
			return undefinedScope();
		case Scope.SCOPE_URL:
			return urlScope();
		case Scope.SCOPE_FORM:
			return formScope();
		case Scope.SCOPE_VARIABLES:
			return variablesScope();
		case Scope.SCOPE_REQUEST:
			return requestScope();
		case Scope.SCOPE_CGI:
			return cgiScope();
		case Scope.SCOPE_APPLICATION:
			return applicationScope();
		case Scope.SCOPE_ARGUMENTS:
			return argumentsScope();
		case Scope.SCOPE_SESSION:
			return sessionScope();
		case Scope.SCOPE_SERVER:
			return serverScope();
		case Scope.SCOPE_COOKIE:
			return cookieScope();
		case Scope.SCOPE_CLIENT:
			return clientScope();
		case Scope.SCOPE_LOCAL:
		case ScopeSupport.SCOPE_VAR:
			return localScope();
		case Scope.SCOPE_CLUSTER:
			return clusterScope();
		}
		return variables;
	}

	public Scope scope(String strScope, Scope defaultValue) throws PageException {
		if (strScope == null) return defaultValue;
		if (ignoreScopes()) {
			if ("arguments".equals(strScope)) return argumentsScope();
			if ("local".equals(strScope)) return localScope();
			if ("request".equals(strScope)) return requestScope();
			if ("variables".equals(strScope)) return variablesScope();
			if ("server".equals(strScope)) return serverScope();
			return defaultValue;
		}

		strScope = strScope.toLowerCase().trim();
		if ("variables".equals(strScope)) return variablesScope();
		if ("url".equals(strScope)) return urlScope();
		if ("form".equals(strScope)) return formScope();
		if ("request".equals(strScope)) return requestScope();
		if ("cgi".equals(strScope)) return cgiScope();
		if ("application".equals(strScope)) return applicationScope();
		if ("arguments".equals(strScope)) return argumentsScope();
		if ("session".equals(strScope)) return sessionScope();
		if ("server".equals(strScope)) return serverScope();
		if ("cookie".equals(strScope)) return cookieScope();
		if ("client".equals(strScope)) return clientScope();
		if ("local".equals(strScope)) return localScope();
		if ("cluster".equals(strScope)) return clusterScope();

		return defaultValue;
	}

	@Override
	public Undefined undefinedScope() {
		if (!undefined.isInitalized()) undefined.initialize(this);
		return undefined;
	}

	/**
	 * @return undefined scope, undefined scope is a placeholder for the scopecascading
	 */
	@Override
	public Undefined us() {
		if (!undefined.isInitalized()) undefined.initialize(this);
		return undefined;
	}

	public Scope usl() {
		if (!undefined.isInitalized()) undefined.initialize(this);
		if (undefined.getCheckArguments()) return undefined.localScope();
		return undefined;
	}

	@Override
	public Variables variablesScope() {
		return variables;
	}

	@Override
	public URL urlScope() {
		if (!url.isInitalized()) url.initialize(this);
		return url;
	}

	@Override
	public Form formScope() {
		if (!form.isInitalized()) form.initialize(this);
		return form;
	}

	@Override
	public URLForm urlFormScope() {
		if (!urlForm.isInitalized()) urlForm.initialize(this);
		return urlForm;
	}

	@Override
	public Request requestScope() {
		return request;
	}

	@Override
	public CGI cgiScope() {
		CGI cgi = applicationContext == null || applicationContext.getCGIScopeReadonly() ? cgiR : cgiRW;
		if (!cgi.isInitalized()) cgi.initialize(this);
		return cgi;
	}

	@Override
	public Application applicationScope() throws PageException {
		if (application == null) {
			if (!applicationContext.hasName())
				throw new ExpressionException("there is no application context defined for this application", hintAplication("you can define an application context"));
			application = scopeContext.getApplicationScope(this, DUMMY_BOOL);
		}
		return application;
	}

	private String hintAplication(String prefix) {
		boolean isCFML = getRequestDialect() == CFMLEngine.DIALECT_CFML;
		return prefix + " with the tag " + (isCFML ? lucee.runtime.config.Constants.CFML_APPLICATION_TAG_NAME : lucee.runtime.config.Constants.LUCEE_APPLICATION_TAG_NAME)
				+ "or with the " + (isCFML ? lucee.runtime.config.Constants.CFML_APPLICATION_EVENT_HANDLER : lucee.runtime.config.Constants.LUCEE_APPLICATION_EVENT_HANDLER);

	}

	@Override
	public Argument argumentsScope() {
		return argument;
	}

	@Override
	public Argument argumentsScope(boolean bind) {
		// Argument a=argumentsScope();
		if (bind) argument.setBind(true);
		return argument;
	}

	@Override
	public Local localScope() {
		// if(local==localUnsupportedScope)
		// throw new PageRuntimeException(new ExpressionException("Unsupported Context for Local Scope"));
		return local;
	}

	@Override
	public Local localScope(boolean bind) {
		if (bind) local.setBind(true);
		// if(local==localUnsupportedScope)
		// throw new PageRuntimeException(new ExpressionException("Unsupported Context for Local Scope"));
		return local;
	}

	@Override
	public Object localGet() throws PageException {
		return localGet(false);
	}

	public Object localGet(boolean bind, Object defaultValue) {
		if (undefined.getCheckArguments()) {
			return localScope(bind);
		}
		return undefinedScope().get(KeyConstants._local, defaultValue);
	}

	@Override
	public Object localGet(boolean bind) throws PageException {
		// inside a local supported block
		if (undefined.getCheckArguments()) {
			return localScope(bind);
		}
		return undefinedScope().get(KeyConstants._local);
	}

	@Override
	public Object localTouch() throws PageException {
		return localTouch(false);
	}

	@Override
	public Object localTouch(boolean bind) throws PageException {
		// inside a local supported block
		if (undefined.getCheckArguments()) {
			return localScope(bind);
		}
		return touch(undefinedScope(), KeyConstants._local);
		// return undefinedScope().get(LOCAL);
	}

	public Object thisGet() throws PageException {
		return thisTouch();
	}

	public Object thisTouch() throws PageException {
		// inside a component
		if (undefined.variablesScope() instanceof ComponentScope) {
			return ((ComponentScope) undefined.variablesScope()).getComponent();
		}
		return undefinedScope().get(KeyConstants._THIS);
	}

	public Object thisGet(Object defaultValue) {
		return thisTouch(defaultValue);
	}

	public Object thisTouch(Object defaultValue) {
		// inside a component
		if (undefined.variablesScope() instanceof ComponentScope) {
			return ((ComponentScope) undefined.variablesScope()).getComponent();
		}
		return undefinedScope().get(KeyConstants._THIS, defaultValue);
	}

	public Object staticGet() throws PageException {
		return staticTouch();
	}

	public Object staticTouch() throws PageException {
		// inside a component
		if (undefined.variablesScope() instanceof ComponentScope) {
			return getStatic(undefined);
		}
		return undefinedScope().get(KeyConstants._STATIC);
	}

	public Object staticGet(Object defaultValue) {
		return staticTouch(defaultValue);
	}

	public Object staticTouch(Object defaultValue) {
		// inside a component
		if (undefined.variablesScope() instanceof ComponentScope) {
			return getStatic(undefined);
		}
		return undefinedScope().get(KeyConstants._STATIC, defaultValue);
	}

	private Scope getStatic(Undefined undefined) {
		return ((ComponentScope) undefined.variablesScope()).getComponent().staticScope();
	}

	/**
	 * @param local sets the current local scope
	 * @param argument sets the current argument scope
	 */
	@Override
	public void setFunctionScopes(Local local, Argument argument) {
		this.argument = argument;
		this.local = local;
		undefined.setFunctionScopes(local, argument);
	}

	@Override
	public Session sessionScope() throws PageException {
		return sessionScope(true);
	}

	public Session sessionScope(boolean checkExpires) throws PageException {
		if (session == null) {
			checkSessionContext();
			session = scopeContext.getSessionScope(this, DUMMY_BOOL);
		}
		return session;
	}

	public void invalidateUserScopes(boolean migrateSessionData, boolean migrateClientData) throws PageException {
		checkSessionContext();
		scopeContext.invalidateUserScope(this, migrateSessionData, migrateClientData);
	}

	private void checkSessionContext() throws ExpressionException {
		if (!applicationContext.hasName())
			throw new ExpressionException("there is no session context defined for this application", hintAplication("you can define a session context"));
		if (!applicationContext.isSetSessionManagement()) throw new ExpressionException("session scope is not enabled", hintAplication("you can enable session scope"));
	}

	@Override
	public Server serverScope() {
		// if(!server.isInitalized()) server.initialize(this);
		return server;
	}

	public void reset() {
		server = ScopeContext.getServerScope(this, ignoreScopes());
	}

	@Override
	public Cluster clusterScope() throws PageException {
		return clusterScope(true);
	}

	@Override
	public Cluster clusterScope(boolean create) throws PageException {
		if (cluster == null && create) {
			cluster = ScopeContext.getClusterScope(config, create);
			// cluster.initialize(this);
		}
		// else if(!cluster.isInitalized()) cluster.initialize(this);
		return cluster;
	}

	@Override
	public Cookie cookieScope() {
		if (!cookie.isInitalized()) cookie.initialize(this);
		return cookie;
	}

	@Override
	public Client clientScope() throws PageException {
		if (client == null) {
			if (!applicationContext.hasName())
				throw new ExpressionException("there is no client context defined for this application", hintAplication("you can define a client context"));
			if (!applicationContext.isSetClientManagement()) throw new ExpressionException("client scope is not enabled", hintAplication("you can enable client scope"));

			client = scopeContext.getClientScope(this);
		}
		return client;
	}

	@Override
	public Client clientScopeEL() {
		if (client == null) {
			if (applicationContext == null || !applicationContext.hasName()) return null;
			if (!applicationContext.isSetClientManagement()) return null;
			client = scopeContext.getClientScopeEL(this);
		}
		return client;
	}

	public Object set(Object coll, String key, Object value) {
		throw new NoLongerSupported();
		// return variableUtil.set(this,coll,key,value);
	}

	@Override
	public Object set(Object coll, Collection.Key key, Object value) throws PageException {
		return variableUtil.set(this, coll, key, value);
	}

	/*
	 * public Object touch(Object coll, String key) throws PageException { Object
	 * o=getCollection(coll,key,null); if(o!=null) return o; return set(coll,key,new StructImpl()); }
	 */

	@Override
	public Object touch(Object coll, Collection.Key key) throws PageException {
		Object o = getCollection(coll, key, null);
		if (o != null) return o;
		return set(coll, key, new StructImpl());
	}

	/*
	 * private Object _touch(Scope scope, String key) throws PageException { Object
	 * o=scope.get(key,null); if(o!=null) return o; return scope.set(key, new StructImpl()); }
	 */

	@Override
	public Object getCollection(Object coll, String key) throws PageException {
		return variableUtil.getCollection(this, coll, key);
	}

	@Override
	public Object getCollection(Object coll, Collection.Key key) throws PageException {
		return variableUtil.getCollection(this, coll, key);
	}

	@Override
	public Object getCollection(Object coll, String key, Object defaultValue) {
		return variableUtil.getCollection(this, coll, key, defaultValue);
	}

	@Override
	public Object getCollection(Object coll, Collection.Key key, Object defaultValue) {
		return variableUtil.getCollection(this, coll, key, defaultValue);
	}

	@Override
	public Object get(Object coll, String key) throws PageException {
		return variableUtil.get(this, coll, key);
	}

	@Override
	public Object get(Object coll, Collection.Key key) throws PageException {
		return variableUtil.get(this, coll, key);
	}

	@Override
	public Reference getReference(Object coll, String key) throws PageException {
		return new VariableReference(coll, key);
	}

	@Override
	public Reference getReference(Object coll, Collection.Key key) throws PageException {
		return new VariableReference(coll, key);
	}

	@Override
	public Object get(Object coll, String key, Object defaultValue) {
		return variableUtil.get(this, coll, key, defaultValue);
	}

	@Override
	public Object get(Object coll, Collection.Key key, Object defaultValue) {
		return variableUtil.get(this, coll, key, defaultValue);
	}

	@Override
	public Object setVariable(String var, Object value) throws PageException {
		// return new CFMLExprInterpreter().interpretReference(this,new ParserString(var)).set(value);
		return VariableInterpreter.setVariable(this, var, value);
	}

	@Override
	public Object getVariable(String var) throws PageException {
		return VariableInterpreter.getVariable(this, var);
	}

	@Override
	public void param(String type, String name, Object defaultValue, String regex) throws PageException {
		_param(type, name, defaultValue, Double.NaN, Double.NaN, regex, -1);
	}

	@Override
	public void param(String type, String name, Object defaultValue, double min, double max) throws PageException {
		_param(type, name, defaultValue, min, max, null, -1);
	}

	@Override
	public void param(String type, String name, Object defaultValue, int maxLength) throws PageException {
		_param(type, name, defaultValue, Double.NaN, Double.NaN, null, maxLength);
	}

	@Override
	public void param(String type, String name, Object defaultValue) throws PageException {
		_param(type, name, defaultValue, Double.NaN, Double.NaN, null, -1);
	}

	// used by generated code FUTURE add to interface
	public void subparam(String type, String name, final Object value, double min, double max, String strPattern, int maxLength, final boolean isNew) throws PageException {

		// check attributes type
		if (type == null) type = "any";
		else type = type.trim().toLowerCase();

		// cast and set value
		if (!"any".equals(type)) {
			// range
			if ("range".equals(type)) {
				boolean hasMin = Decision.isValid(min);
				boolean hasMax = Decision.isValid(max);
				double number = Caster.toDoubleValue(value);

				if (!hasMin && !hasMax) throw new ExpressionException("you need to define one of the following attributes [min,max], when type is set to [range]");

				if (hasMin && number < min)
					throw new ExpressionException("The number [" + Caster.toString(number) + "] is too small, the number must be at least [" + Caster.toString(min) + "]");

				if (hasMax && number > max)
					throw new ExpressionException("The number [" + Caster.toString(number) + "] is too big, the number cannot be bigger than [" + Caster.toString(max) + "]");

				setVariable(name, Caster.toDouble(number));
			}
			// regex
			else if ("regex".equals(type) || "regular_expression".equals(type)) {
				String str = Caster.toString(value);

				if (strPattern == null) throw new ExpressionException("Missing attribute [pattern]");

				if (!Perl5Util.matches(strPattern, str)) throw new ExpressionException("The value [" + str + "] doesn't match the provided pattern [" + strPattern + "]");
				setVariable(name, str);
			}
			else if (type.equals("int") || type.equals("integer")) {

				if (!Decision.isInteger(value)) throw new ExpressionException("The value [" + value + "] is not a valid integer");

				setVariable(name, value);
			}
			else {
				if (!Decision.isCastableTo(type, value, true, true, maxLength)) {
					if (maxLength > -1 && ("email".equalsIgnoreCase(type) || "url".equalsIgnoreCase(type) || "string".equalsIgnoreCase(type))) {
						StringBuilder msg = new StringBuilder(CasterException.createMessage(value, type));
						msg.append(" with a maximum length of " + maxLength + " characters");
						throw new CasterException(msg.toString());
					}
					throw new CasterException(value, type);
				}

				setVariable(name, value);
				// REALCAST setVariable(name,Caster.castTo(this,type,value,true));
			}
		}
		else if (isNew) setVariable(name, value);
	}

	private void _param(String type, String name, Object defaultValue, double min, double max, String strPattern, int maxLength) throws PageException {

		// check attributes name
		if (StringUtil.isEmpty(name)) throw new ExpressionException("The attribute [name] is required");

		Object value = null;
		boolean isNew = false;

		Object _null = NullSupportHelper.NULL(this);
		// get value
		value = VariableInterpreter.getVariableEL(this, name, _null);
		if (_null == value) {
			if (defaultValue == null) throw new ExpressionException("The required parameter [" + name + "] was not provided.");
			value = defaultValue;
			isNew = true;
		}

		subparam(type, name, value, min, max, strPattern, maxLength, isNew);

	}

	/*
	 * private void paramX(String type, String name, Object defaultValue, double min,double max, String
	 * strPattern, int maxLength) throws PageException {
	 * 
	 * // check attributes type if(type==null)type="any"; else type=type.trim().toLowerCase();
	 * 
	 * // check attributes name if(StringUtil.isEmpty(name)) throw new
	 * ExpressionException("The attribute name is required");
	 * 
	 * Object value=null; boolean isNew=false;
	 * 
	 * // get value value=VariableInterpreter.getVariableEL(this,name,NullSupportHelper.NULL(this));
	 * if(NullSupportHelper.NULL(this)==value) { if(defaultValue==null) throw new
	 * ExpressionException("The required parameter ["+name+"] was not provided."); value=defaultValue;
	 * isNew=true; }
	 * 
	 * // cast and set value if(!"any".equals(type)) { // range if("range".equals(type)) { boolean
	 * hasMin=Decision.isValid(min); boolean hasMax=Decision.isValid(max); double number =
	 * Caster.toDoubleValue(value);
	 * 
	 * if(!hasMin && !hasMax) throw new
	 * ExpressionException("you need to define one of the following attributes [min,max], when type is set to [range]"
	 * );
	 * 
	 * if(hasMin && number<min) throw new ExpressionException("The number ["+Caster.toString(number)
	 * +"] is to small, the number must be at least ["+Caster.toString(min)+"]");
	 * 
	 * if(hasMax && number>max) throw new ExpressionException("The number ["+Caster.toString(number)
	 * +"] is to big, the number cannot be bigger than ["+Caster.toString(max)+"]");
	 * 
	 * setVariable(name,Caster.toDouble(number)); } // regex else if("regex".equals(type) ||
	 * "regular_expression".equals(type)) { String str=Caster.toString(value);
	 * 
	 * if(strPattern==null) throw new ExpressionException("Missing attribute [pattern]");
	 * 
	 * if(!Perl5Util.matches(strPattern, str)) throw new
	 * ExpressionException("The value ["+str+"] doesn't match the provided pattern ["+strPattern+"]");
	 * setVariable(name,str); } else if ( type.equals( "int" ) || type.equals( "integer" ) ) {
	 * 
	 * if ( !Decision.isInteger( value ) ) throw new ExpressionException( "The value [" + value +
	 * "] is not a valid integer" );
	 * 
	 * setVariable( name, value ); } else { if(!Decision.isCastableTo(type,value,true,true,maxLength)) {
	 * if(maxLength>-1 && ("email".equalsIgnoreCase(type) || "url".equalsIgnoreCase(type) ||
	 * "string".equalsIgnoreCase(type))) { StringBuilder msg=new
	 * StringBuilder(CasterException.createMessage(value, type));
	 * msg.append(" with a maximum length of "+maxLength+" characters"); throw new
	 * CasterException(msg.toString()); } throw new CasterException(value,type); }
	 * 
	 * setVariable(name,value); //REALCAST setVariable(name,Caster.castTo(this,type,value,true)); } }
	 * else if(isNew) setVariable(name,value); }
	 */

	@Override
	public Object removeVariable(String var) throws PageException {
		return VariableInterpreter.removeVariable(this, var);
	}

	/**
	 * a variable reference, references to variable, to modifed it, with global effect.
	 * 
	 * @param var variable name to get
	 * @return return a variable reference by string syntax ("scopename.key.key" -> "url.name")
	 * @throws PageException
	 */
	public VariableReference getVariableReference(String var) throws PageException {
		return VariableInterpreter.getVariableReference(this, var);
	}

	@Override
	public Object getFunction(Object coll, String key, Object[] args) throws PageException {
		return variableUtil.callFunctionWithoutNamedValues(this, coll, key, args);
	}

	@Override
	public Object getFunction(Object coll, Key key, Object[] args) throws PageException {
		return variableUtil.callFunctionWithoutNamedValues(this, coll, key, args);
	}

	// FUTURE add to interface
	public Object getFunction(Object coll, Key key, Object[] args, Object defaultValue) {
		return variableUtil.callFunctionWithoutNamedValues(this, coll, key, args, false, defaultValue);
	}

	public Object getFunction2(Object coll, Key key, Object[] args, Object defaultValue) {
		return variableUtil.callFunctionWithoutNamedValues(this, coll, key, args, true, defaultValue);
	}

	@Override
	public Object getFunctionWithNamedValues(Object coll, String key, Object[] args) throws PageException {
		return variableUtil.callFunctionWithNamedValues(this, coll, key, args);
	}

	@Override
	public Object getFunctionWithNamedValues(Object coll, Key key, Object[] args) throws PageException {
		return variableUtil.callFunctionWithNamedValues(this, coll, key, args);
	}

	// FUTURE add to interface
	public Object getFunctionWithNamedValues(Object coll, Key key, Object[] args, Object defaultValue) {
		return variableUtil.callFunctionWithNamedValues(this, coll, key, args, false, defaultValue);
	}

	public Object getFunctionWithNamedValues2(Object coll, Key key, Object[] args, Object defaultValue) {
		return variableUtil.callFunctionWithNamedValues(this, coll, key, args, true, defaultValue);
	}

	@Override
	public ConfigWeb getConfig() {
		return config;
	}

	@Override
	public Iterator getIterator(String key) throws PageException {
		Object o = VariableInterpreter.getVariable(this, key);
		if (o instanceof Iterator) return (Iterator) o;
		throw new ExpressionException("[" + key + "] is not an iterator object");
	}

	@Override
	public Query getQuery(String key) throws PageException {
		Object value = VariableInterpreter.getVariable(this, key);
		if (Decision.isQuery(value)) return Caster.toQuery(value);
		throw new CasterException(value, Query.class);/// ("["+key+"] is not a query object, object is from type ");
	}

	@Override
	public Query getQuery(Object value) throws PageException {
		if (Decision.isQuery(value)) return Caster.toQuery(value);
		value = VariableInterpreter.getVariable(this, Caster.toString(value));
		if (Decision.isQuery(value)) return Caster.toQuery(value);
		throw new CasterException(value, Query.class);
	}

	@Override
	public void setAttribute(String name, Object value) {
		try {
			if (value == null) removeVariable(name);
			else setVariable(name, value);
		}
		catch (PageException e) {
		}
	}

	@Override
	public void setAttribute(String name, Object value, int scope) {
		switch (scope) {
		case javax.servlet.jsp.PageContext.APPLICATION_SCOPE:
			if (value == null) getServletContext().removeAttribute(name);
			else getServletContext().setAttribute(name, value);
			break;
		case javax.servlet.jsp.PageContext.PAGE_SCOPE:
			setAttribute(name, value);
			break;
		case javax.servlet.jsp.PageContext.REQUEST_SCOPE:
			if (value == null) req.removeAttribute(name);
			else setAttribute(name, value);
			break;
		case javax.servlet.jsp.PageContext.SESSION_SCOPE:
			HttpSession s = req.getSession(true);
			if (value == null) s.removeAttribute(name);
			else s.setAttribute(name, value);
			break;
		}
	}

	@Override
	public Object getAttribute(String name) {
		try {
			return getVariable(name);
		}
		catch (PageException e) {
			return null;
		}
	}

	@Override
	public Object getAttribute(String name, int scope) {
		switch (scope) {
		case javax.servlet.jsp.PageContext.APPLICATION_SCOPE:
			return getServletContext().getAttribute(name);
		case javax.servlet.jsp.PageContext.PAGE_SCOPE:
			return getAttribute(name);
		case javax.servlet.jsp.PageContext.REQUEST_SCOPE:
			return req.getAttribute(name);
		case javax.servlet.jsp.PageContext.SESSION_SCOPE:
			HttpSession s = req.getSession();
			if (s != null) return s.getAttribute(name);
			break;
		}
		return null;
	}

	@Override
	public Object findAttribute(String name) {
		// page
		Object value = getAttribute(name);
		if (value != null) return value;
		// request
		value = req.getAttribute(name);
		if (value != null) return value;
		// session
		HttpSession s = req.getSession();
		value = s != null ? s.getAttribute(name) : null;
		if (value != null) return value;
		// application
		value = getServletContext().getAttribute(name);
		if (value != null) return value;

		return null;
	}

	@Override
	public void removeAttribute(String name) {
		setAttribute(name, null);
	}

	@Override
	public void removeAttribute(String name, int scope) {
		setAttribute(name, null, scope);
	}

	@Override
	public int getAttributesScope(String name) {
		// page
		if (getAttribute(name) != null) return PageContext.PAGE_SCOPE;
		// request
		if (req.getAttribute(name) != null) return PageContext.REQUEST_SCOPE;
		// session
		HttpSession s = req.getSession();
		if (s != null && s.getAttribute(name) != null) return PageContext.SESSION_SCOPE;
		// application
		if (getServletContext().getAttribute(name) != null) return PageContext.APPLICATION_SCOPE;

		return 0;
	}

	@Override
	public Enumeration<String> getAttributeNamesInScope(int scope) {

		switch (scope) {
		case javax.servlet.jsp.PageContext.APPLICATION_SCOPE:
			return getServletContext().getAttributeNames();
		case javax.servlet.jsp.PageContext.PAGE_SCOPE:
			return ItAsEnum.toStringEnumeration(variablesScope().keyIterator());
		case javax.servlet.jsp.PageContext.REQUEST_SCOPE:
			return req.getAttributeNames();
		case javax.servlet.jsp.PageContext.SESSION_SCOPE:
			return req.getSession(true).getAttributeNames();
		}
		return null;
	}

	@Override
	public JspWriter getOut() {
		return forceWriter;
	}

	@Override
	public HttpSession getSession() {
		return getHttpServletRequest().getSession();
	}

	@Override
	public Object getPage() {
		return variablesScope();
	}

	@Override
	public ServletRequest getRequest() {
		return getHttpServletRequest();
	}

	@Override
	public HttpServletRequest getHttpServletRequest() {
		return req;
	}

	@Override
	public ServletResponse getResponse() {
		return rsp;
	}

	@Override
	public HttpServletResponse getHttpServletResponse() {
		return rsp;
	}

	@Override
	public OutputStream getResponseStream() throws IOException {
		return getRootOut().getResponseStream();
	}

	@Override
	public Exception getException() {
		// TODO impl
		return exception;
	}

	@Override
	public ServletConfig getServletConfig() {
		return config;
	}

	@Override
	public ServletContext getServletContext() {
		return servlet.getServletContext();
	}

	private static String repl(String haystack, String needle, String replacement) {
		StringBuilder regex = new StringBuilder("#[\\s]*error[\\s]*\\.[\\s]*");

		char[] carr = needle.toCharArray();
		for (int i = 0; i < carr.length; i++) {
			regex.append("[");
			regex.append(Character.toLowerCase(carr[i]));
			regex.append(Character.toUpperCase(carr[i]));
			regex.append("]");
		}

		regex.append("[\\s]*#");
		// print.o(regex);

		haystack = haystack.replaceAll(regex.toString(), replacement);
		// print.o(haystack);
		return haystack;
	}

	@Override
	public void handlePageException(PageException pe) {
		handlePageException(pe, true);
	}

	public void handlePageException(PageException pe, boolean setHeader) {
		if (!Abort.isSilentAbort(pe)) {
			// if(requestTimeoutException!=null)
			// pe=Caster.toPageException(requestTimeoutException);

			int statusCode = getStatusCode(pe);

			// prepare response
			if (rsp != null) {
				// content-type
				Charset cs = ReqRspUtil.getCharacterEncoding(this, rsp);
				if (cs == null) ReqRspUtil.setContentType(rsp, "text/html");
				else ReqRspUtil.setContentType(rsp, "text/html; charset=" + cs.name());

				// expose error message in header
				if (rsp != null && pe.getExposeMessage()) rsp.setHeader("exception-message", StringUtil.emptyIfNull(pe.getMessage()).replace('\n', ' '));

				// status code
				if (getConfig().getErrorStatusCode()) rsp.setStatus(statusCode);
			}

			ErrorPage ep = errorPagePool.getErrorPage(pe, ErrorPageImpl.TYPE_EXCEPTION);

			// ExceptionHandler.printStackTrace(this,pe);
			ExceptionHandler.log(getConfig(), pe);

			// error page exception
			if (ep != null) {
				try {
					Struct sct = pe.getErrorBlock(this, ep);
					variablesScope().setEL(KeyConstants._error, sct);
					variablesScope().setEL(KeyConstants._cferror, sct);

					doInclude(new PageSource[] { ep.getTemplate() }, false);
					return;
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					if (Abort.isSilentAbort(t)) return;
					pe = Caster.toPageException(t);
				}
			}

			// error page request
			ep = errorPagePool.getErrorPage(pe, ErrorPageImpl.TYPE_REQUEST);
			if (ep != null) {
				PageSource ps = ep.getTemplate();
				if (ps.physcalExists()) {
					Resource res = ps.getResource();
					try {
						String content = IOUtil.toString(res, getConfig().getTemplateCharset());
						Struct sct = pe.getErrorBlock(this, ep);
						java.util.Iterator<Entry<Key, Object>> it = sct.entryIterator();
						Entry<Key, Object> e;
						String v;
						while (it.hasNext()) {
							e = it.next();
							v = Caster.toString(e.getValue(), null);
							if (v != null) content = repl(content, e.getKey().getString(), v);
						}

						write(content);
						return;
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						pe = Caster.toPageException(t);
					}
				}
				else pe = new ApplicationException("The error page template for type request only works if the actual source file also exists. If the exception file is in an "
						+ Constants.NAME + " archive (.lar), you need to use type exception instead.");
			}

			try {

				String template = getConfig().getErrorTemplate(statusCode);
				if (!StringUtil.isEmpty(template)) {
					try {
						Struct catchBlock = pe.getCatchBlock(getConfig());
						variablesScope().setEL(KeyConstants._cfcatch, catchBlock);
						variablesScope().setEL(KeyConstants._catch, catchBlock);
						doInclude(template, false);
						return;
					}
					catch (PageException e) {
						pe = e;
					}
				}
				if (!Abort.isSilentAbort(pe))
					forceWrite(getConfig().getDefaultDumpWriter(DumpWriter.DEFAULT_RICH).toString(this, pe.toDumpData(this, 9999, DumpUtil.toDumpProperties()), true));
			}
			catch (Exception e) {}
		}
	}

	private int getStatusCode(PageException pe) {
		int statusCode = 500;
		int maxDeepFor404 = 0;
		if (pe instanceof ModernAppListenerException) {
			pe = ((ModernAppListenerException) pe).getPageException();
			maxDeepFor404 = 1;
		}
		else if (pe instanceof PageExceptionBox) pe = ((PageExceptionBox) pe).getPageException();

		if (pe instanceof MissingIncludeException) {
			MissingIncludeException mie = (MissingIncludeException) pe;
			if (mie.getPageDeep() <= maxDeepFor404) statusCode = 404;
		}
		return statusCode;
	}

	@Override
	public void handlePageException(Exception e) {
		handlePageException(Caster.toPageException(e));
	}

	@Override
	public void handlePageException(Throwable t) {
		handlePageException(Caster.toPageException(t));
	}

	@Override
	public void setHeader(String name, String value) {
		rsp.setHeader(name, value);
	}

	@Override
	public BodyContent pushBody() {
		forceWriter = bodyContentStack.push();
		if (enablecfoutputonly > 0 && outputState == 0) {
			writer = devNull;
		}
		else writer = forceWriter;
		return (BodyContent) forceWriter;
	}

	@Override
	public JspWriter popBody() {
		forceWriter = bodyContentStack.pop();
		if (enablecfoutputonly > 0 && outputState == 0) {
			writer = devNull;
		}
		else writer = forceWriter;
		return forceWriter;
	}

	@Override
	public void outputStart() {
		outputState++;
		if (enablecfoutputonly > 0 && outputState == 1) writer = forceWriter;
		// if(enablecfoutputonly && outputState>0) unsetDevNull();
	}

	@Override
	public void outputEnd() {
		outputState--;
		if (enablecfoutputonly > 0 && outputState == 0) writer = devNull;
	}

	@Override
	public void setCFOutputOnly(boolean boolEnablecfoutputonly) {
		if (boolEnablecfoutputonly) this.enablecfoutputonly++;
		else if (this.enablecfoutputonly > 0) this.enablecfoutputonly--;
		setCFOutputOnly(enablecfoutputonly);
		// if(!boolEnablecfoutputonly)setCFOutputOnly(enablecfoutputonly=0);
	}

	@Override
	public void setCFOutputOnly(short enablecfoutputonly) {
		this.enablecfoutputonly = enablecfoutputonly;
		if (enablecfoutputonly > 0) {
			if (outputState == 0) writer = devNull;
		}
		else {
			writer = forceWriter;
		}
	}

	public short getCFOutputOnly() {
		return enablecfoutputonly;
	}

	/**
	 * FUTURE - add to interface
	 *
	 * @return true if the Request is in silent mode via cfslient
	 */
	public boolean isSilent() {
		return bodyContentStack.getDevNull();
	}

	@Override
	public boolean setSilent() {
		boolean before = bodyContentStack.getDevNull();
		bodyContentStack.setDevNull(true);

		forceWriter = bodyContentStack.getWriter();
		writer = forceWriter;
		return before;
	}

	@Override
	public boolean unsetSilent() {
		boolean before = bodyContentStack.getDevNull();
		bodyContentStack.setDevNull(false);

		forceWriter = bodyContentStack.getWriter();
		if (enablecfoutputonly > 0 && outputState == 0) {
			writer = devNull;
		}
		else writer = forceWriter;
		return before;
	}

	@Override
	public Debugger getDebugger() {
		return debugger;
	}

	@Override
	public void executeRest(String realPath, boolean throwExcpetion) throws PageException {
		initallog();

		ApplicationListener listener = null;// config.get ApplicationListener();
		try {
			String pathInfo = req.getPathInfo();

			// charset
			try {
				String charset = HTTPUtil.splitMimeTypeAndCharset(req.getContentType(), new String[] { "", "" })[1];
				if (StringUtil.isEmpty(charset)) charset = getWebCharset().name();
				java.net.URL reqURL = new java.net.URL(req.getRequestURL().toString());
				String path = ReqRspUtil.decode(reqURL.getPath(), charset, true);
				String srvPath = req.getServletPath();
				if (path.startsWith(srvPath)) {
					pathInfo = path.substring(srvPath.length());
				}
			}
			catch (Exception e) {}

			// Service mapping
			if (StringUtil.isEmpty(pathInfo) || pathInfo.equals("/")) {// ToDo
				// list available services (if enabled in admin)
				if (config.getRestList()) {
					try {
						HttpServletRequest _req = getHttpServletRequest();
						write("Available sevice mappings are:<ul>");
						lucee.runtime.rest.Mapping[] mappings = config.getRestMappings();
						lucee.runtime.rest.Mapping _mapping;
						String path;
						for (int i = 0; i < mappings.length; i++) {
							_mapping = mappings[i];
							Resource p = _mapping.getPhysical();
							path = _req.getContextPath() + ReqRspUtil.getScriptName(this, _req) + _mapping.getVirtual();
							write("<li " + (p == null || !p.isDirectory() ? " style=\"color:red\"" : "") + ">" + path + "</li>");

						}
						write("</ul>");

					}
					catch (IOException e) {
						throw Caster.toPageException(e);
					}
				}
				else RestUtil.setStatus(this, 404, null);
				return;
			}

			// check for matrix
			int index;
			String entry;
			Struct matrix = new StructImpl();
			while ((index = pathInfo.lastIndexOf(';')) != -1) {
				entry = pathInfo.substring(index + 1);
				pathInfo = pathInfo.substring(0, index);
				if (StringUtil.isEmpty(entry, true)) continue;

				index = entry.indexOf('=');
				if (index != -1) matrix.setEL(KeyImpl.init(entry.substring(0, index).trim()), entry.substring(index + 1).trim());
				else matrix.setEL(KeyImpl.init(entry.trim()), "");
			}

			// get accept
			List<MimeType> accept = ReqRspUtil.getAccept(this);
			MimeType contentType = ReqRspUtil.getContentType(this);

			// check for format extension
			// int format = getApplicationContext().getRestSettings().getReturnFormat();
			int format;
			boolean hasFormatExtension = false;
			if (StringUtil.endsWithIgnoreCase(pathInfo, ".json")) {
				pathInfo = pathInfo.substring(0, pathInfo.length() - 5);
				format = UDF.RETURN_FORMAT_JSON;
				accept.clear();
				accept.add(MimeType.APPLICATION_JSON);
				hasFormatExtension = true;
			}
			else if (StringUtil.endsWithIgnoreCase(pathInfo, ".wddx")) {
				pathInfo = pathInfo.substring(0, pathInfo.length() - 5);
				format = UDF.RETURN_FORMAT_WDDX;
				accept.clear();
				accept.add(MimeType.APPLICATION_WDDX);
				hasFormatExtension = true;
			}
			else if (StringUtil.endsWithIgnoreCase(pathInfo, ".cfml")) {
				pathInfo = pathInfo.substring(0, pathInfo.length() - 5);
				format = UDF.RETURN_FORMAT_SERIALIZE;
				accept.clear();
				accept.add(MimeType.APPLICATION_CFML);
				hasFormatExtension = true;
			}
			else if (StringUtil.endsWithIgnoreCase(pathInfo, ".serialize")) {
				pathInfo = pathInfo.substring(0, pathInfo.length() - 10);
				format = UDF.RETURN_FORMAT_SERIALIZE;
				accept.clear();
				accept.add(MimeType.APPLICATION_CFML);
				hasFormatExtension = true;
			}
			else if (StringUtil.endsWithIgnoreCase(pathInfo, ".xml")) {
				pathInfo = pathInfo.substring(0, pathInfo.length() - 4);
				format = UDF.RETURN_FORMAT_XML;
				accept.clear();
				accept.add(MimeType.APPLICATION_XML);
				hasFormatExtension = true;
			}
			else if (StringUtil.endsWithIgnoreCase(pathInfo, ".java")) {
				pathInfo = pathInfo.substring(0, pathInfo.length() - 5);
				format = UDF.RETURN_FORMAT_JAVA;
				accept.clear();
				accept.add(MimeType.APPLICATION_JAVA);
				hasFormatExtension = true;
			}
			else {
				format = getApplicationContext() == null ? null : getApplicationContext().getRestSettings().getReturnFormat();
				// MimeType mt=MimeType.toMimetype(format);
				// if(mt!=null)accept.add(mt);
			}

			if (accept.size() == 0) accept.add(MimeType.ALL);

			// loop all mappings
			// lucee.runtime.rest.Result result = null;//config.getRestSource(pathInfo, null);
			RestRequestListener rl = null;
			lucee.runtime.rest.Mapping[] restMappings = config.getRestMappings();
			lucee.runtime.rest.Mapping m, mapping = null, defaultMapping = null;
			// String callerPath=null;
			if (restMappings != null) for (int i = 0; i < restMappings.length; i++) {
				m = restMappings[i];
				if (m.isDefault()) defaultMapping = m;
				if (pathInfo.startsWith(m.getVirtualWithSlash(), 0) && m.getPhysical() != null) {
					mapping = m;
					// result =
					// m.getResult(this,callerPath=pathInfo.substring(m.getVirtual().length()),format,matrix,null);
					rl = new RestRequestListener(m, pathInfo.substring(m.getVirtual().length()), matrix, format, hasFormatExtension, accept, contentType, null);
					break;
				}
			}

			// default mapping
			if (mapping == null && defaultMapping != null && defaultMapping.getPhysical() != null) {
				mapping = defaultMapping;
				// result = mapping.getResult(this,callerPath=pathInfo,format,matrix,null);
				rl = new RestRequestListener(mapping, pathInfo, matrix, format, hasFormatExtension, accept, contentType, null);
			}

			// base = PageSourceImpl.best(config.getPageSources(this,null,realPath,true,false,true));

			if (mapping == null || mapping.getPhysical() == null) {
				RestUtil.setStatus(this, 404, "no rest service for [" + pathInfo + "] found");
				getConfig().getLog("rest").error("REST", "no rest service for [" + pathInfo + "] found");
			}
			else {
				base = config.toPageSource(null, mapping.getPhysical(), null);
				listener = ((MappingImpl) base.getMapping()).getApplicationListener();
				listener.onRequest(this, base, rl);
			}

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			PageException pe = Caster.toPageException(t);
			if (!Abort.isSilentAbort(pe)) {
				log(true);
				if (fdEnabled) {
					FDSignal.signal(pe, false);
				}
				if (listener == null) {
					if (base == null) listener = config.getApplicationListener();
					else listener = ((MappingImpl) base.getMapping()).getApplicationListener();
				}
				listener.onError(this, pe);
			}
			else log(false);

			if (throwExcpetion) throw pe;
		}
		finally {
			if (enablecfoutputonly > 0) {
				setCFOutputOnly((short) 0);
			}
			base = null;
		}
	}

	@Override
	public final void execute(String realPath, boolean throwExcpetion, boolean onlyTopLevel) throws PageException {
		requestDialect = currentTemplateDialect = CFMLEngine.DIALECT_LUCEE;
		setFullNullSupport();
		_execute(realPath, throwExcpetion, onlyTopLevel);
	}

	@Override
	public final void executeCFML(String realPath, boolean throwExcpetion, boolean onlyTopLevel) throws PageException {
		requestDialect = currentTemplateDialect = CFMLEngine.DIALECT_CFML;
		setFullNullSupport();
		_execute(realPath, throwExcpetion, onlyTopLevel);
	}

	private final void _execute(String realPath, boolean throwExcpetion, boolean onlyTopLevel) throws PageException {
		if ((config.getScriptProtect() & ApplicationContext.SCRIPT_PROTECT_URL) > 0) {
			realPath = ScriptProtect.translate(realPath);
		}

		// convert realpath to a PageSource
		if (realPath.startsWith("/mapping-")) {
			base = null;
			int index = realPath.indexOf('/', 9);
			if (index > -1) {
				String type = realPath.substring(9, index);
				if (type.equalsIgnoreCase("tag")) {
					base = getPageSource(new Mapping[] { config.getDefaultTagMapping(), config.getDefaultServerTagMapping() }, realPath.substring(index));
				}
				else if (type.equalsIgnoreCase("customtag")) {
					base = getPageSource(config.getCustomTagMappings(), realPath.substring(index));
				}
			}
			if (base == null) base = PageSourceImpl.best(config.getPageSources(this, null, realPath, onlyTopLevel, false, true));
		}
		else base = PageSourceImpl.best(config.getPageSources(this, null, realPath, onlyTopLevel, false, true));

		execute(base, throwExcpetion, onlyTopLevel);
	}

	private final void execute(PageSource ps, boolean throwExcpetion, boolean onlyTopLevel) throws PageException {
		ApplicationListener listener = getRequestDialect() == CFMLEngine.DIALECT_CFML
				? (gatewayContext ? config.getApplicationListener() : ((MappingImpl) ps.getMapping()).getApplicationListener())
				: ModernAppListener.getInstance();
		Throwable _t = null;
		try {
			initallog();
			listener.onRequest(this, ps, null);
			if (ormSession != null) {
				releaseORM();
				removeLastPageSource(true);
			}
			log(false);
		}
		catch (Throwable t) {
			PageException pe;
			if (t instanceof ThreadDeath && getTimeoutStackTrace() != null) {
				t = pe = new RequestTimeoutException(this, (ThreadDeath) t);
			}
			else pe = Caster.toPageException(t, false);
			_t = t;
			if (!Abort.isSilentAbort(pe)) {
				this.pe = pe;
				log(true);
				if (fdEnabled) {
					FDSignal.signal(pe, false);
				}
				listener.onError(this, pe);
			}
			else log(false);

			if (throwExcpetion) {
				ExceptionUtil.rethrowIfNecessary(t);
				throw pe;
			}
		}
		finally {
			if (enablecfoutputonly > 0) {
				setCFOutputOnly((short) 0);
			}
			if (!gatewayContext && getConfig().debug()) {
				try {
					listener.onDebug(this);
				}
				catch (Exception e) {
					pe = Caster.toPageException(e);
					if (!Abort.isSilentAbort(pe)) listener.onError(this, pe);
					ExceptionUtil.rethrowIfNecessary(e);
				}
			}
			ps = null;
			if (_t != null) ExceptionUtil.rethrowIfNecessary(_t);
		}
	}

	private void initallog() {
		if (!isGatewayContext() && config.isMonitoringEnabled()) {
			RequestMonitor[] monitors = config.getRequestMonitors();
			if (monitors != null) for (int i = 0; i < monitors.length; i++) {
				if (monitors[i].isLogEnabled()) {
					try {
						((RequestMonitorPro) monitors[i]).init(this);
					}
					catch (Throwable e) {
						ExceptionUtil.rethrowIfNecessary(e);
					}
				}
			}
		}
	}

	private void log(boolean error) {
		if (!isGatewayContext() && config.isMonitoringEnabled()) {
			RequestMonitor[] monitors = config.getRequestMonitors();
			if (monitors != null) for (int i = 0; i < monitors.length; i++) {
				if (monitors[i].isLogEnabled()) {
					try {
						monitors[i].log(this, error);
					}
					catch (Throwable e) {
						ExceptionUtil.rethrowIfNecessary(e);
					}
				}
			}
		}
	}

	private PageSource getPageSource(Mapping[] mappings, String realPath) {
		PageSource ps;
		// print.err(mappings.length);
		for (int i = 0; i < mappings.length; i++) {
			ps = mappings[i].getPageSource(realPath);
			// print.err(ps.getDisplayPath());
			if (ps.exists()) return ps;

		}
		return null;
	}

	@Override
	public void include(String realPath) throws ServletException, IOException {
		HTTPUtil.include(this, realPath);
	}

	@Override
	public void forward(String realPath) throws ServletException, IOException {
		HTTPUtil.forward(this, realPath);
	}

	@Override
	public void clear() {
		try {
			// print.o(getOut().getClass().getName());
			getOut().clear();
		}
		catch (IOException e) {
		}
	}

	@Override
	public long getRequestTimeout() {
		if (requestTimeout == -1) {
			if (applicationContext != null) {
				return applicationContext.getRequestTimeout().getMillis();
			}
			requestTimeout = config.getRequestTimeout().getMillis();
		}
		return requestTimeout;
	}

	@Override
	public void setRequestTimeout(long requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	@Override
	public String getCFID() {
		if (cfid == null) initIdAndToken();
		return cfid;
	}

	@Override
	public String getCFToken() {
		if (cftoken == null) initIdAndToken();
		return cftoken;
	}

	@Override
	public String getURLToken() {
		if (getConfig().getSessionType() == Config.SESSION_TYPE_JEE) {
			HttpSession s = getSession();
			return "CFID=" + getCFID() + "&CFTOKEN=" + getCFToken() + "&jsessionid=" + (s != null ? getSession().getId() : "");
		}
		return "CFID=" + getCFID() + "&CFTOKEN=" + getCFToken();
	}

	@Override
	public String getJSessionId() {
		if (getConfig().getSessionType() == Config.SESSION_TYPE_JEE) {
			return getSession().getId();
		}
		return null;
	}

	/**
	 * initialize the cfid and the cftoken
	 */
	private void initIdAndToken() {
		boolean setCookie = true;
		// From URL
		Object oCfid = urlScope().get(KeyConstants._cfid, null);
		Object oCftoken = urlScope().get(KeyConstants._cftoken, null);

		// if CFID comes from URL, we only accept if already exists
		if (oCfid != null) {
			if (Decision.isGUIdSimple(oCfid)) {
				if (!scopeContext.hasExistingCFID(this, Caster.toString(oCfid, null))) {
					oCfid = null;
					oCftoken = null;
				}
			}
			else {
				oCfid = null;
				oCftoken = null;
			}
		}

		// Cookie
		if (oCfid == null) {
			setCookie = false;
			oCfid = cookieScope().get(KeyConstants._cfid, null);
			oCftoken = cookieScope().get(KeyConstants._cftoken, null);
		}

		// check cookie value
		if (oCfid != null) {
			// cookie value is invalid, maybe from ACF
			if (!Decision.isGUIdSimple(oCfid)) {
				oCfid = null;
				oCftoken = null;
				Charset charset = getWebCharset();

				// check if we have multiple cookies with the name "cfid" and another one is valid
				javax.servlet.http.Cookie[] cookies = getHttpServletRequest().getCookies();
				String name, value;
				if (cookies != null) {
					for (int i = 0; i < cookies.length; i++) {
						name = ReqRspUtil.decode(cookies[i].getName(), charset.name(), false);
						// CFID
						if ("cfid".equalsIgnoreCase(name)) {
							value = ReqRspUtil.decode(cookies[i].getValue(), charset.name(), false);
							if (Decision.isGUIdSimple(value)) oCfid = value;
							ReqRspUtil.removeCookie(getHttpServletResponse(), name);
						}
						// CFToken
						else if ("cftoken".equalsIgnoreCase(name)) {
							value = ReqRspUtil.decode(cookies[i].getValue(), charset.name(), false);
							if (isValidCfToken(value)) oCftoken = value;
							ReqRspUtil.removeCookie(getHttpServletResponse(), name);
						}
					}
				}

				if (oCfid != null) {
					setCookie = true;
					if (oCftoken == null) oCftoken = "0";
				}
			}
		}
		// New One
		if (oCfid == null || oCftoken == null) {
			setCookie = true;
			cfid = ScopeContext.getNewCFId();
			cftoken = ScopeContext.getNewCFToken();
		}
		else {
			cfid = Caster.toString(oCfid, null);
			cftoken = Caster.toString(oCftoken, "0");
		}

		if (setCookie && applicationContext.isSetClientCookies()) setClientCookies();
	}

	private boolean isValidCfToken(String value) {
		return Operator.compare(value, "0") == 0;
	}

	public void resetIdAndToken() {
		cfid = ScopeContext.getNewCFId();
		cftoken = ScopeContext.getNewCFToken();

		if (applicationContext.isSetClientCookies()) setClientCookies();
	}

	private void setClientCookies() {
		TimeSpan tsExpires = SessionCookieDataImpl.DEFAULT.getTimeout();
		String domain = PageContextUtil.getCookieDomain(this);
		boolean httpOnly = SessionCookieDataImpl.DEFAULT.isHttpOnly();
		boolean secure = SessionCookieDataImpl.DEFAULT.isSecure();
		String samesite = SessionCookieDataImpl.DEFAULT.getSamesite();

		ApplicationContext ac = getApplicationContext();

		if (ac instanceof ApplicationContextSupport) {
			ApplicationContextSupport acs = (ApplicationContextSupport) ac;
			SessionCookieData data = acs.getSessionCookie();
			if (data != null) {
				// expires
				TimeSpan ts = data.getTimeout();
				if (ts != null) tsExpires = ts;
				// httpOnly
				httpOnly = data.isHttpOnly();
				// secure
				secure = data.isSecure();
				// domain
				String tmp = data.getDomain();
				if (!StringUtil.isEmpty(tmp, true)) domain = tmp.trim();
				// samesite
				samesite = data.getSamesite();
			}
		}
		int expires;
		long tmp = tsExpires.getSeconds();
		if (Integer.MAX_VALUE < tmp) expires = Integer.MAX_VALUE;
		else expires = (int) tmp;

		cookieScope().setCookieEL(KeyConstants._cfid, cfid, expires, secure, "/", domain, httpOnly, true, false, samesite);
		cookieScope().setCookieEL(KeyConstants._cftoken, cftoken, expires, secure, "/", domain, httpOnly, true, false, samesite);

	}

	@Override
	public int getId() {
		return id;
	}

	/**
	 * @return returns the root JSP Writer
	 * 
	 */
	public CFMLWriter getRootOut() {// used in extension PDF
		return bodyContentStack.getBase();
	}

	@Override
	public JspWriter getRootWriter() {
		return bodyContentStack.getBase();
	}

	@Override
	public void setPsq(boolean psq) {
		this._psq = psq;
	}

	@Override
	public boolean getPsq() {
		if (_psq != null) return _psq.booleanValue();

		if (applicationContext != null) {
			return applicationContext.getQueryPSQ();
		}
		return config.getPSQL();
	}

	@Override
	public Locale getLocale() {
		Locale l = getApplicationContext() == null ? null : getApplicationContext().getLocale();
		if (l != null) return l;
		if (locale != null) return locale;
		return config.getLocale();
	}

	@Override
	public void setLocale(Locale locale) {
		if (getApplicationContext() != null) getApplicationContext().setLocale(locale);
		this.locale = locale;
		HttpServletResponse rsp = getHttpServletResponse();

		Charset charEnc = ReqRspUtil.getCharacterEncoding(this, rsp);
		rsp.setLocale(locale);
		if (charEnc.equals(CharsetUtil.UTF8)) {
			ReqRspUtil.setContentType(rsp, "text/html; charset=UTF-8");
		}
		else if (!charEnc.equals(ReqRspUtil.getCharacterEncoding(this, rsp))) {
			ReqRspUtil.setContentType(rsp, "text/html; charset=" + charEnc);
		}
	}

	@Override
	public void setLocale(String strLocale) throws ExpressionException {
		setLocale(Caster.toLocale(strLocale));
	}

	@Override
	public void setErrorPage(ErrorPage ep) {
		errorPagePool.setErrorPage(ep);
	}

	// called by generated bytecode
	public Tag use(String tagClassName, String fullname, int attrType) throws PageException {
		return use(tagClassName, null, null, fullname, attrType, null);
	}

	public Tag use(String tagClassName, String fullname, int attrType, String template) throws PageException {
		return use(tagClassName, null, null, fullname, attrType, template);
	}

	// called by generated bytecode
	public Tag use(String tagClassName, String tagBundleName, String tagBundleVersion, String fullname, int attrType) throws PageException {
		return use(tagClassName, tagBundleName, tagBundleVersion, fullname, attrType, null);
	}

	public Tag use(String tagClassName, String tagBundleName, String tagBundleVersion, String fullname, int attrType, String template) throws PageException {
		parentTag = currentTag;
		currentTag = tagHandlerPool.use(tagClassName, tagBundleName, tagBundleVersion, getConfig().getIdentification());
		if (currentTag == parentTag) throw new ApplicationException("");
		currentTag.setPageContext(this);
		currentTag.setParent(parentTag);
		if (currentTag instanceof TagImpl) ((TagImpl) currentTag).setSourceTemplate(template);

		if (attrType >= 0 && fullname != null) {
			Map<Collection.Key, Object> attrs = applicationContext.getTagAttributeDefaultValues(this, fullname);
			if (attrs != null) {
				TagUtil.setAttributes(this, currentTag, attrs, attrType);
			}
		}
		return currentTag;
	}

	public Object useJavaFunction(Page page, String className) throws ClassException, ClassNotFoundException, IOException {
		JF jf = (JF) ClassUtil.loadInstance(page.getPageSource().getMapping().getPhysicalClass(className));
		jf.setPageSource(page.getPageSource());
		return jf;
	}

	public void reuse(Tag tag) {
		currentTag = tag.getParent();
		tagHandlerPool.reuse(tag);
	}

	public void reuse(Tag tag, String tagBundleName, String tagBundleVersion) {
		currentTag = tag.getParent();
		tagHandlerPool.reuse(tag, tagBundleName, tagBundleVersion);
	}

	@Override
	public void initBody(BodyTag bodyTag, int state) throws JspException {
		if (state != Tag.EVAL_BODY_INCLUDE) {
			bodyTag.setBodyContent(pushBody());
			bodyTag.doInitBody();
		}
	}

	@Override
	public void releaseBody(BodyTag bodyTag, int state) {
		if (bodyTag instanceof TryCatchFinally) {
			((TryCatchFinally) bodyTag).doFinally();
		}
		if (state != Tag.EVAL_BODY_INCLUDE) popBody();
	}

	/*
	 * *
	 * 
	 * @return returns the cfml compiler / public CFMLCompiler getCompiler() { return compiler; }
	 */

	@Override
	public void setVariablesScope(Variables variables) {
		this.variables = variables;
		undefinedScope().setVariableScope(variables);

		if (variables instanceof ClosureScope) {
			variables = ((ClosureScope) variables).getVariables();
		}
		if (variables instanceof StaticScope) {
			activeComponent = ((StaticScope) variables).getComponent();
		}
		else if (variables instanceof ComponentScope) {
			activeComponent = ((ComponentScope) variables).getComponent();
		}
		else {
			activeComponent = null;
		}
	}

	@Override
	public Component getActiveComponent() {
		return activeComponent;
	}

	@Override
	public Credential getRemoteUser() throws PageException {
		if (remoteUser == null) {
			Key name = KeyImpl.init(Login.getApplicationName(applicationContext));
			Resource roles = config.getConfigDir().getRealResource("roles");

			if (applicationContext.getLoginStorage() == Scope.SCOPE_SESSION) {
				Object auth = sessionScope().get(name, null);
				if (auth != null) {
					remoteUser = CredentialImpl.decode(auth, roles);
				}
			}
			else if (applicationContext.getLoginStorage() == Scope.SCOPE_COOKIE) {
				Object auth = cookieScope().get(name, null);
				if (auth != null) {
					remoteUser = CredentialImpl.decode(auth, roles);
				}
			}
		}
		return remoteUser;
	}

	@Override
	public void clearRemoteUser() {
		if (remoteUser != null) remoteUser = null;
		String name = Login.getApplicationName(applicationContext);

		cookieScope().removeEL(KeyImpl.init(name));
		try {
			sessionScope().removeEL(KeyImpl.init(name));
		}
		catch (PageException e) {}

	}

	@Override
	public void setRemoteUser(Credential remoteUser) {
		this.remoteUser = remoteUser;
	}

	@Override
	public VariableUtil getVariableUtil() {
		return variableUtil;
	}

	@Override
	public void throwCatch() throws PageException {
		if (exception != null) throw exception;
		throw new ApplicationException("invalid context for tag/script expression rethow");
	}

	@Override
	public PageException setCatch(Throwable t) {
		PageException pe = t == null ? null : Caster.toPageException(t);
		_setCatch(pe, null, false, true, false);
		return pe;
	}

	@Override
	public void setCatch(PageException pe) {
		_setCatch(pe, null, false, true, false);
	}

	@Override
	public void setCatch(PageException pe, boolean caught, boolean store) {
		_setCatch(pe, null, caught, store, true);
	}

	public void setCatch(PageException pe, String name, boolean caught, boolean store) {
		_setCatch(pe, name, caught, store, true);
	}

	public void _setCatch(PageException pe, String name, boolean caught, boolean store, boolean signal) {
		if (signal && fdEnabled) {
			FDSignal.signal(pe, caught);
		}
		// boolean outer = exception != null && exception == pe;
		exception = pe;
		if (store) {
			Undefined u = undefinedScope();
			if (pe == null) {
				(u.getCheckArguments() ? u.localScope() : u).removeEL(KeyConstants._cfcatch);
				if (name != null && !StringUtil.isEmpty(name, true)) (u.getCheckArguments() ? u.localScope() : u).removeEL(KeyImpl.getInstance(name.trim()));
			}
			else {
				(u.getCheckArguments() ? u.localScope() : u).setEL(KeyConstants._cfcatch, pe.getCatchBlock(config));
				if (name != null && !StringUtil.isEmpty(name, true)) (u.getCheckArguments() ? u.localScope() : u).setEL(KeyImpl.getInstance(name.trim()), pe.getCatchBlock(config));
				if (!gatewayContext && config.debug() && config.hasDebugOptions(ConfigImpl.DEBUG_EXCEPTION) && caught) {
					/*
					 * print.e("-----------------------"); print.e("msg:" + pe.getMessage()); print.e("caught:" +
					 * caught); print.e("store:" + store); print.e("signal:" + signal); print.e("outer:" + outer);
					 */
					debugger.addException(config, exception);
				}
			}
		}
	}

	/**
	 * @return return current catch
	 */
	@Override
	public PageException getCatch() {
		return exception;
	}

	@Override
	public void clearCatch() {
		exception = null;

		Undefined u = undefinedScope();
		(u.getCheckArguments() ? u.localScope() : u).removeEL(KeyConstants._cfcatch);
	}

	@Override
	public void addPageSource(PageSource ps, boolean alsoInclude) {
		currentTemplateDialect = ps.getDialect();
		setFullNullSupport();
		pathList.add(ps);
		if (alsoInclude) includePathList.add(ps);
	}

	public void addPageSource(PageSource ps, PageSource psInc) {
		currentTemplateDialect = ps.getDialect();
		setFullNullSupport();
		pathList.add(ps);
		if (psInc != null) includePathList.add(psInc);
	}

	@Override
	public void removeLastPageSource(boolean alsoInclude) {
		if (!pathList.isEmpty()) pathList.removeLast();
		if (!pathList.isEmpty()) {
			currentTemplateDialect = pathList.getLast().getDialect();
			setFullNullSupport();
		}
		if (alsoInclude && !includePathList.isEmpty()) includePathList.removeLast();
	}

	public UDF[] getUDFs() {
		return udfs.toArray(new UDF[udfs.size()]);
	}

	public void addUDF(UDF udf) {
		udfs.add(udf);
	}

	public void removeUDF() {
		if (!udfs.isEmpty()) udfs.removeLast();
	}

	public FTPPoolImpl getFTPPool() {
		return ftpPool;
	}

	/*
	 * *
	 * 
	 * @return Returns the manager. / public DataSourceManager getManager() { return manager; }
	 */

	@Override
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {

		session = null;
		application = null;
		client = null;
		this.applicationContext = (ApplicationContextSupport) applicationContext;
		setFullNullSupport();
		int scriptProtect = applicationContext.getScriptProtect();

		// ScriptProtecting
		if (config.mergeFormAndURL()) {
			form.setScriptProtecting(applicationContext,
					(scriptProtect & ApplicationContext.SCRIPT_PROTECT_FORM) > 0 || (scriptProtect & ApplicationContext.SCRIPT_PROTECT_URL) > 0);
		}
		else {
			form.setScriptProtecting(applicationContext, (scriptProtect & ApplicationContext.SCRIPT_PROTECT_FORM) > 0);
			url.setScriptProtecting(applicationContext, (scriptProtect & ApplicationContext.SCRIPT_PROTECT_URL) > 0);
		}
		cookie.setScriptProtecting(applicationContext, (scriptProtect & ApplicationContext.SCRIPT_PROTECT_COOKIE) > 0);
		// CGI
		cgiR.setScriptProtecting(applicationContext, (scriptProtect & ApplicationContext.SCRIPT_PROTECT_CGI) > 0);
		cgiRW.setScriptProtecting(applicationContext, (scriptProtect & ApplicationContext.SCRIPT_PROTECT_CGI) > 0);
		undefined.reinitialize(this);
	}

	/**
	 * @return return value of method "onApplicationStart" or true
	 * @throws PageException
	 */
	public boolean initApplicationContext(ApplicationListener listener) throws PageException {
		boolean initSession = false;
		// AppListenerSupport listener = (AppListenerSupport) config.get ApplicationListener();
		KeyLock<String> lock = config.getContextLock();
		String name = StringUtil.emptyIfNull(applicationContext.getName());
		String token = name + ":" + getCFID();

		Lock tokenLock = lock.lock(token, getRequestTimeout());
		// print.o("outer-lock :"+token);
		try {
			// check session before executing any code
			initSession = applicationContext.isSetSessionManagement() && listener.hasOnSessionStart(this) && !scopeContext.hasExistingSessionScope(this);

			// init application

			Lock nameLock = lock.lock(name, getRequestTimeout());
			// print.o("inner-lock :"+token);
			try {
				RefBoolean isNew = new RefBooleanImpl(false);
				application = scopeContext.getApplicationScope(this, isNew);// this is needed that the application scope is initilized
				if (isNew.toBooleanValue()) {
					try {
						if (!((AppListenerSupport) listener).onApplicationStart(this, application)) {
							scopeContext.removeApplicationScope(this);
							return false;
						}
					}
					catch (PageException pe) {
						scopeContext.removeApplicationScope(this);
						throw pe;
					}
				}
			}
			finally {
				// print.o("inner-unlock:"+token);
				lock.unlock(nameLock);
			}

			// init session
			if (initSession) {
				// session must be initlaized here
				((AppListenerSupport) listener).onSessionStart(this, scopeContext.getSessionScope(this, DUMMY_BOOL));
			}
		}
		finally {
			// print.o("outer-unlock:"+token);
			lock.unlock(tokenLock);
		}
		return true;
	}

	/**
	 * @return the scope factory
	 */
	public ScopeFactory getScopeFactory() {
		return scopeFactory;
	}

	@Override
	public Tag getCurrentTag() {
		return currentTag;
	}

	@Override
	public long getStartTime() {
		return startTime;
	}

	@Override
	public Thread getThread() {
		return thread;
	}

	public void setThread(Thread thread) {
		this.thread = thread;
	}

	@Override
	public long getExecutionTime() {
		return executionTime;
	}

	@Override
	public void setExecutionTime(long executionTime) {
		this.executionTime = executionTime;
	}

	@Override
	public void compile(PageSource pageSource) throws PageException {
		Resource classRootDir = pageSource.getMapping().getClassRootDirectory();
		int dialect = getCurrentTemplateDialect();

		try {
			config.getCompiler().compile(config, pageSource, config.getTLDs(dialect), config.getFLDs(dialect), classRootDir, false, ignoreScopes());
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public void compile(String realPath) throws PageException {
		LogUtil.log(config, Log.LEVEL_INFO, PageContextImpl.class.getName(), "method PageContext.compile(String) should no longer be used!");
		compile(PageSourceImpl.best(getRelativePageSources(realPath)));
	}

	public HttpServlet getServlet() {
		return servlet;
	}

	@Override
	public lucee.runtime.Component loadComponent(String compPath) throws PageException {
		return ComponentLoader.searchComponent(this, null, compPath, null, null, false);
	}

	/**
	 * @return the base
	 */
	public PageSource getBase() {
		return base;
	}

	/**
	 * @param base the base to set
	 */
	public void setBase(PageSource base) {
		this.base = base;
	}

	@Override
	public DataSourceManager getDataSourceManager() {
		return manager;
	}

	@Override
	public Object evaluate(String expression) throws PageException {
		return new CFMLExpressionInterpreter(false).interpret(this, expression);
	}

	@Override
	public String serialize(Object expression) throws PageException {
		return Serialize.call(this, expression);
	}

	/**
	 * @return the activeUDF
	 */
	@Override
	public UDF getActiveUDF() {
		return activeUDF;
	}

	/**
	 * @param activeUDF the activeUDF to set
	 */
	public void setActiveUDF(UDF activeUDF) {
		this.activeUDF = activeUDF;
	}

	public Collection.Key getActiveUDFCalledName() {
		return activeUDFCalledName;
	}

	public void setActiveUDFCalledName(Collection.Key activeUDFCalledName) {
		this.activeUDFCalledName = activeUDFCalledName;
	}

	@Override
	public CFMLFactory getCFMLFactory() {
		return config.getFactory();
	}

	@Override
	public PageContext getParentPageContext() {
		// DebuggerImpl.deprecated(this, "PageContext.getParentPageContext", "the method
		// PageContext.getParentPageContext should no longer be used");

		return parent;
	}

	public PageContext getRootPageContext() {
		return root;
	}

	public List<PageContext> getChildPageContexts() {
		return children;
	}

	@Override
	public String[] getThreadScopeNames() {
		if (threads == null) return new String[0];
		return CollectionUtil.keysAsString(threads);
	}

	@Override
	public Threads getThreadScope(String name) {
		return getThreadScope(KeyImpl.init(name));
	}

	@Override
	public Threads getThreadScope(Collection.Key name) {// MUST who uses this? is cfthread/thread handling necessary
		if (threads == null) threads = new CFThread();
		Object obj = threads.get(name, null);
		if (obj instanceof Threads) return (Threads) obj;
		return null;
	}

	public Object getThreadScope(Collection.Key name, Object defaultValue) {
		if (threads == null) threads = new CFThread();
		if (name.equalsIgnoreCase(KeyConstants._cfthread)) return threads; // do not change this, this is used!
		if (name.equalsIgnoreCase(KeyConstants._thread)) {
			ThreadsImpl curr = getCurrentThreadScope();
			if (curr != null) return curr;
		}
		return threads.get(name, defaultValue);
	}

	public Struct getCFThreadScope() {
		if (threads == null) threads = new CFThread();
		return threads;
	}

	public boolean isThreads(Object obj) {
		return threads == obj;
	}

	public void setCurrentThreadScope(ThreadsImpl thread) {
		currentThread = thread;
	}

	public ThreadsImpl getCurrentThreadScope() {
		return currentThread;
	}

	@Override
	public void setThreadScope(String name, Threads ct) {
		setThreadScope(KeyImpl.init(name), ct);
	}

	@Override
	public void setThreadScope(Collection.Key name, Threads ct) {
		hasFamily = true;
		if (threads == null) threads = new CFThread();
		threads.setEL(name, ct);
	}

	/**
	 * 
	 * @param name
	 * @param ct
	 */
	public void setAllThreadScope(Collection.Key name, Threads ct) {
		hasFamily = true;
		if (allThreads == null) allThreads = new HashMap<Collection.Key, Threads>();
		allThreads.put(name, ct);
	}

	public Map<Collection.Key, Threads> getAllThreadScope() {
		return allThreads;
	}

	@Override
	public boolean hasFamily() {
		return hasFamily;
	}

	@Override
	public TimeZone getTimeZone() {
		TimeZone tz = getApplicationContext() == null ? null : getApplicationContext().getTimeZone();
		if (tz != null) return tz;
		if (timeZone != null) return timeZone;
		return config.getTimeZone();
	}

	@Override
	public void setTimeZone(TimeZone timeZone) {
		if (getApplicationContext() != null) getApplicationContext().setTimeZone(timeZone);
		this.timeZone = timeZone;
	}

	/**
	 * @return the requestId
	 */
	public int getRequestId() {
		return requestId;
	}

	private Set<String> pagesUsed = new HashSet<String>();

	private Stack<ActiveQuery> activeQueries = new Stack<ActiveQuery>();
	private Stack<ActiveLock> activeLocks = new Stack<ActiveLock>();

	private boolean literalTimestampWithTSOffset;

	private String tagName;

	private boolean listenSettings;

	public boolean isTrusted(Page page) {
		if (page == null) return false;

		short it = ((MappingImpl) page.getPageSource().getMapping()).getInspectTemplate();
		if (it == ConfigImpl.INSPECT_NEVER) return true;
		if (it == ConfigImpl.INSPECT_ALWAYS) return false;

		return pagesUsed.contains("" + page.hashCode());
	}

	public void setPageUsed(Page page) {
		pagesUsed.add("" + page.hashCode());
	}

	@Override
	public void exeLogStart(int position, String id) {
		if (execLog != null) execLog.start(position, id);
	}

	@Override
	public void exeLogEnd(int position, String id) {
		if (execLog != null) execLog.end(position, id);
	}

	@Override
	public ORMSession getORMSession(boolean create) throws PageException {
		if (ormSession == null || !ormSession.isValid()) {
			if (!create) return null;
			ormSession = config.getORMEngine(this).createSession(this);
		}
		DatasourceManagerImpl manager = (DatasourceManagerImpl) getDataSourceManager();
		manager.add(this, ormSession);

		return ormSession;
	}

	public ClassLoader getClassLoader() throws IOException {
		return getResourceClassLoader();
	}

	public ClassLoader getClassLoader(Resource[] reses) throws IOException {

		ResourceClassLoader rcl = getResourceClassLoader();
		return rcl.getCustomResourceClassLoader(reses);
	}

	private ResourceClassLoader getResourceClassLoader() throws IOException {

		JavaSettingsImpl js = (JavaSettingsImpl) applicationContext.getJavaSettings();

		if (js != null) {
			Resource[] jars = OSGiUtil.extractAndLoadBundles(this, js.getResourcesTranslated());
			if (jars.length > 0) return config.getResourceClassLoader().getCustomResourceClassLoader(jars);
		}
		return config.getResourceClassLoader();
	}

	public ClassLoader getRPCClassLoader(boolean reload) throws IOException {
		return getRPCClassLoader(reload, null);
	}

	public ClassLoader getRPCClassLoader(boolean reload, ClassLoader[] parents) throws IOException {
		JavaSettingsImpl js = (JavaSettingsImpl) applicationContext.getJavaSettings();
		ClassLoader cl = config.getRPCClassLoader(reload, parents);
		if (js != null) {
			Resource[] jars = OSGiUtil.extractAndLoadBundles(this, js.getResourcesTranslated());
			if (jars.length > 0) return ((PhysicalClassLoader) cl).getCustomClassLoader(jars, reload);
		}
		return cl;
	}

	public void resetSession() {
		this.session = null;
	}

	public void resetClient() {
		this.client = null;
	}

	/**
	 * @return the gatewayContext
	 */
	public boolean isGatewayContext() {
		return gatewayContext;
	}

	/**
	 * @param gatewayContext the gatewayContext to set
	 */
	public void setGatewayContext(boolean gatewayContext) {
		this.gatewayContext = gatewayContext;
	}

	public void setServerPassword(Password serverPassword) {
		this.serverPassword = serverPassword;
	}

	public Password getServerPassword() {
		return serverPassword;
	}

	@Override
	public short getSessionType() {
		if (isGatewayContext()) return Config.SESSION_TYPE_APPLICATION;
		return applicationContext.getSessionType();
	}

	// this is just a wrapper method for ACF
	public Scope SymTab_findBuiltinScope(String name) throws PageException {
		return scope(name, null);
	}

	@Override
	public DataSource getDataSource(String datasource) throws PageException {

		DataSource ds = getApplicationContext() == null ? null : getApplicationContext().getDataSource(datasource, null);
		if (ds != null) return ds;
		ds = getConfig().getDataSource(datasource, null);
		if (ds != null) return ds;

		throw DatabaseException.notFoundException(this, datasource);
	}

	@Override
	public DataSource getDataSource(String datasource, DataSource defaultValue) {
		DataSource ds = getApplicationContext() == null ? null : getApplicationContext().getDataSource(datasource, null);
		if (ds == null) ds = getConfig().getDataSource(datasource, defaultValue);
		return ds;
	}

	public CacheConnection getCacheConnection(String cacheName, CacheConnection defaultValue) {
		cacheName = cacheName.toLowerCase().trim();

		CacheConnection cc = null;
		if (getApplicationContext() != null) cc = ((ApplicationContextSupport) getApplicationContext()).getCacheConnection(cacheName, null);
		if (cc == null) cc = config.getCacheConnections().get(cacheName);
		if (cc == null) return defaultValue;

		return cc;
	}

	public CacheConnection getCacheConnection(String cacheName) throws CacheException {
		cacheName = cacheName.toLowerCase().trim();

		CacheConnection cc = null;
		if (getApplicationContext() != null) cc = ((ApplicationContextSupport) getApplicationContext()).getCacheConnection(cacheName, null);
		if (cc == null) cc = config.getCacheConnections().get(cacheName);
		if (cc == null) throw CacheUtil.noCache(config, cacheName);

		return cc;
	}

	public void setActiveQuery(ActiveQuery activeQuery) {
		this.activeQueries.add(activeQuery);
	}

	public ActiveQuery[] getActiveQueries() {
		return activeQueries.toArray(new ActiveQuery[activeQueries.size()]);
	}

	public ActiveQuery releaseActiveQuery() {
		return activeQueries.pop();
	}

	public void setActiveLock(ActiveLock activeLock) {
		this.activeLocks.add(activeLock);
	}

	public ActiveLock[] getActiveLocks() {
		return activeLocks.toArray(new ActiveLock[activeLocks.size()]);
	}

	public ActiveLock releaseActiveLock() {
		return activeLocks.pop();
	}

	public PageException getPageException() {
		return pe;
	}

	@Override
	public Charset getResourceCharset() {
		Charset cs = getApplicationContext() == null ? null : getApplicationContext().getResourceCharset();
		if (cs != null) return cs;
		return config.getResourceCharset();
	}

	@Override
	public Charset getWebCharset() {
		Charset cs = getApplicationContext() == null ? null : getApplicationContext().getWebCharset();
		if (cs != null) return cs;
		return config.getWebCharset();
	}

	public short getScopeCascadingType() {
		if (applicationContext == null) return config.getScopeCascadingType();
		return applicationContext.getScopeCascading();
	}

	public boolean getTypeChecking() {
		if (applicationContext == null) return config.getTypeChecking();
		return applicationContext.getTypeChecking();
	}

	public boolean getAllowCompression() {
		if (applicationContext == null) return config.allowCompression();
		return applicationContext.getAllowCompression();
	}

	public boolean getSuppressContent() {
		if (applicationContext == null) return config.isSuppressContent();
		return applicationContext.getSuppressContent();
	}

	@Override
	public Object getCachedWithin(int type) {
		if (applicationContext == null) return config.getCachedWithin(type);
		return applicationContext.getCachedWithin(type);
	}

	// FUTURE add to interface
	public lucee.runtime.net.mail.Server[] getMailServers() {
		if (applicationContext != null) {
			lucee.runtime.net.mail.Server[] appms = applicationContext.getMailServers();
			if (ArrayUtil.isEmpty(appms)) return config.getMailServers();

			lucee.runtime.net.mail.Server[] cms = config.getMailServers();
			if (ArrayUtil.isEmpty(cms)) return appms;

			lucee.runtime.net.mail.Server[] arr = ServerImpl.merge(appms, cms);
			return arr;
		}
		return config.getMailServers();
	}

	// FUTURE add to interface
	public boolean getFullNullSupport() {
		return fullNullSupport;
	}

	private void setFullNullSupport() {
		fullNullSupport = currentTemplateDialect != CFMLEngine.DIALECT_CFML || (applicationContext != null && applicationContext.getFullNullSupport());
	}

	public void registerLazyStatement(Statement s) {
		if (lazyStats == null) lazyStats = new ArrayList<Statement>();
		lazyStats.add(s);
	}

	@Override
	public int getCurrentTemplateDialect() {
		return currentTemplateDialect;
	}

	@Override
	public int getRequestDialect() {
		return requestDialect;
	}

	@Override
	public void include(String realPath, boolean flush) throws ServletException, IOException {
		include(realPath);
		if (flush) flush();
	}

	@Override
	public ExpressionEvaluator getExpressionEvaluator() {
		throw new RuntimeException("not supported!");
	}

	@Override
	public VariableResolver getVariableResolver() {
		throw new RuntimeException("not supported!");
	}

	@Override
	public ELContext getELContext() {
		throw new RuntimeException("not supported!");
	}

	@Override
	public boolean ignoreScopes() {
		return ignoreScopes;
	}

	public void setIgnoreScopes(boolean ignoreScopes) {
		this.ignoreScopes = ignoreScopes;
	}

	public void setAppListenerType(int appListenerType) {
		this.appListenerType = appListenerType;
	}

	public int getAppListenerType() {
		return appListenerType;
	}

	public Log getLog(String name) {
		return config.getLog(name);
	}

	public Log getLog(String name, boolean createIfNecessary) {
		if (applicationContext != null) {
			Log log = applicationContext.getLog(name);
			if (log != null) return log;
		}
		return config.getLog(name, createIfNecessary);
	}

	public java.util.Collection<String> getLogNames() {
		java.util.Collection<String> cnames = config.getLoggers().keySet();
		if (applicationContext != null) {
			java.util.Collection<Collection.Key> anames = applicationContext.getLogNames();

			java.util.Collection<String> names = new HashSet<String>();

			copy(cnames, names);
			copy(anames, names);
			return names;

		}
		return cnames;
	}

	private void copy(java.util.Collection src, java.util.Collection<String> trg) {
		java.util.Iterator it = src.iterator();
		while (it.hasNext()) {
			trg.add(it.next().toString());
		}
	}

	public void setTimestampWithTSOffset(boolean literalTimestampWithTSOffset) {
		this.literalTimestampWithTSOffset = literalTimestampWithTSOffset;
	}

	public boolean getTimestampWithTSOffset() {
		return literalTimestampWithTSOffset;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public String getTagName() {
		return this.tagName;
	}

	public List<String> getParentTagNames() {
		return this.parentTags;
	}

	public void addParentTag(String tagName) {
		if (!StringUtil.isEmpty(tagName)) {
			if (parentTags == null) parentTags = new ArrayList<String>();
			parentTags.add(tagName);
		}
	}

	public TimeSpan getCachedAfterTimeRange() { // FUTURE add to interface
		if (applicationContext != null) {
			return applicationContext.getQueryCachedAfter();
		}
		return config.getCachedAfterTimeRange();
	}

	public ProxyData getProxyData() {
		if (applicationContext != null) {
			ProxyData pd = applicationContext.getProxyData();
			if (pd != null) return pd;
		}
		// TODO check application context
		return config.getProxyData();
	}

	public void setListenSettings(boolean listenSettings) {
		this.listenSettings = listenSettings;
	}

	public boolean getListenSettings() {
		return listenSettings;
	}
}
