package lucee.transformer.direct;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Queue;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.el.ELContext;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.Tag;

import lucee.commons.io.res.Resource;
import lucee.runtime.CFMLFactory;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DataSourceManager;
import lucee.runtime.debug.Debugger;
import lucee.runtime.err.ErrorPage;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.orm.ORMSession;
import lucee.runtime.security.Credential;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Iterator;
import lucee.runtime.type.Query;
import lucee.runtime.type.UDF;
import lucee.runtime.type.ref.Reference;
import lucee.runtime.type.scope.Application;
import lucee.runtime.type.scope.Argument;
import lucee.runtime.type.scope.CGI;
import lucee.runtime.type.scope.Client;
import lucee.runtime.type.scope.Cluster;
import lucee.runtime.type.scope.Cookie;
import lucee.runtime.type.scope.Form;
import lucee.runtime.type.scope.Local;
import lucee.runtime.type.scope.Request;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.scope.Server;
import lucee.runtime.type.scope.Session;
import lucee.runtime.type.scope.Threads;
import lucee.runtime.type.scope.URL;
import lucee.runtime.type.scope.URLForm;
import lucee.runtime.type.scope.Undefined;
import lucee.runtime.type.scope.Variables;
import lucee.runtime.util.VariableUtil;

public class PageContextDummy extends PageContext {

	private static final int MAX_CAPACITY = 1000;
	private static Queue<PageContextDummy> dummies = new ConcurrentLinkedDeque<>();

	public static PageContextDummy getDummy(Object obj) {
		PageContextDummy dummy = dummies.poll();
		if (dummy != null) return dummy.set(obj);
		return new PageContextDummy(obj);
	}

	public static void returnDummy(PageContextDummy dummy) {
		if (dummies.size() < MAX_CAPACITY) dummies.add(dummy);
	}

	private Object object;

	public PageContextDummy(Object object) {
		this.object = object;
	}

	private PageContextDummy set(Object object) {
		this.object = object;
		return this;
	}

	@Override
	public Object getPage() {
		return object;
	}

	@Override
	public void addPageSource(PageSource arg0, boolean arg1) {

	}

	@Override
	public Application applicationScope() throws PageException {

		return null;
	}

	@Override
	public Argument argumentsScope() {

		return null;
	}

	@Override
	public Argument argumentsScope(boolean arg0) {

		return null;
	}

	@Override
	public CGI cgiScope() {

		return null;
	}

	@Override
	public void clear() {

	}

	@Override
	public void clearCatch() {

	}

	@Override
	public void clearRemoteUser() {

	}

	@Override
	public Client clientScope() throws PageException {

		return null;
	}

	@Override
	public Client clientScopeEL() {

		return null;
	}

	@Override
	public void close() {

	}

	@Override
	public Cluster clusterScope() throws PageException {

		return null;
	}

	@Override
	public Cluster clusterScope(boolean arg0) throws PageException {

		return null;
	}

	@Override
	public void compile(String arg0) throws PageException {

	}

	@Override
	public void compile(PageSource arg0) throws PageException {

	}

	@Override
	public Cookie cookieScope() {

		return null;
	}

	@Override
	public void doInclude(String arg0) throws PageException {

	}

	@Override
	public void doInclude(PageSource[] arg0, boolean arg1) throws PageException {

	}

	@Override
	public void doInclude(String arg0, boolean arg1) throws PageException {

	}

	@Override
	public Object evaluate(String arg0) throws PageException {

		return null;
	}

	@Override
	public void exeLogEnd(int arg0, String arg1) {

	}

	@Override
	public void exeLogStart(int arg0, String arg1) {

	}

	@Override
	public void execute(String arg0, boolean arg1, boolean arg2) throws PageException {

	}

	@Override
	public void executeCFML(String arg0, boolean arg1, boolean arg2) throws PageException {

	}

	@Override
	public void executeRest(String arg0, boolean arg1) throws PageException {

	}

	@Override
	public void flush() {

	}

	@Override
	public void forceWrite(String arg0) throws IOException {

	}

	@Override
	public Form formScope() {

		return null;
	}

	@Override
	public Object get(Object arg0, String arg1) throws PageException {

		return null;
	}

	@Override
	public Object get(Object arg0, Key arg1) throws PageException {

		return null;
	}

	@Override
	public Object get(Object arg0, String arg1, Object arg2) {

		return null;
	}

	@Override
	public Object get(Object arg0, Key arg1, Object arg2) {

		return null;
	}

	@Override
	public Component getActiveComponent() {

		return null;
	}

	@Override
	public UDF getActiveUDF() {

		return null;
	}

	@Override
	public ApplicationContext getApplicationContext() {

		return null;
	}

	@Override
	public PageSource getBasePageSource() {

		return null;
	}

	@Override
	public String getCFID() {

		return null;
	}

	@Override
	public CFMLFactory getCFMLFactory() {

		return null;
	}

	@Override
	public String getCFToken() {

		return null;
	}

	@Override
	public Object getCachedWithin(int arg0) {

		return null;
	}

	@Override
	public PageException getCatch() {

		return null;
	}

	@Override
	public Object getCollection(Object arg0, String arg1) throws PageException {

		return null;
	}

	@Override
	public Object getCollection(Object arg0, Key arg1) throws PageException {

		return null;
	}

	@Override
	public Object getCollection(Object arg0, String arg1, Object arg2) {

		return null;
	}

	@Override
	public Object getCollection(Object arg0, Key arg1, Object arg2) {

		return null;
	}

	@Override
	public ConfigWeb getConfig() {

		return null;
	}

	@Override
	public int getCurrentLevel() {

		return 0;
	}

	@Override
	public PageSource getCurrentPageSource() {

		return null;
	}

	@Override
	public PageSource getCurrentPageSource(PageSource arg0) {

		return null;
	}

	@Override
	public Tag getCurrentTag() {

		return null;
	}

	@Override
	public int getCurrentTemplateDialect() {

		return 0;
	}

	@Override
	public PageSource getCurrentTemplatePageSource() {

		return null;
	}

	@Override
	public DataSource getDataSource(String arg0) throws PageException {

		return null;
	}

	@Override
	public DataSource getDataSource(String arg0, DataSource arg1) {

		return null;
	}

	@Override
	public DataSourceManager getDataSourceManager() {

		return null;
	}

	@Override
	public Debugger getDebugger() {

		return null;
	}

	@Override
	public long getExecutionTime() {

		return 0;
	}

	@Override
	public Object getFunction(Object arg0, String arg1, Object[] arg2) throws PageException {

		return null;
	}

	@Override
	public Object getFunction(Object arg0, Key arg1, Object[] arg2) throws PageException {

		return null;
	}

	@Override
	public Object getFunctionWithNamedValues(Object arg0, String arg1, Object[] arg2) throws PageException {

		return null;
	}

	@Override
	public Object getFunctionWithNamedValues(Object arg0, Key arg1, Object[] arg2) throws PageException {

		return null;
	}

	@Override
	public HttpServletRequest getHttpServletRequest() {

		return null;
	}

	@Override
	public HttpServletResponse getHttpServletResponse() {

		return null;
	}

	@Override
	public int getId() {

		return 0;
	}

	@Override
	public Iterator getIterator(String arg0) throws PageException {

		return null;
	}

	@Override
	public String getJSessionId() {

		return null;
	}

	@Override
	public Locale getLocale() {

		return null;
	}

	@Override
	public ORMSession getORMSession(boolean arg0) throws PageException {

		return null;
	}

	@Override
	public PageContext getParentPageContext() {

		return null;
	}

	@Override
	public boolean getPsq() {

		return false;
	}

	@Override
	public Query getQuery(String arg0) throws PageException {

		return null;
	}

	@Override
	public Query getQuery(Object arg0) throws PageException {

		return null;
	}

	@Override
	public Reference getReference(Object arg0, String arg1) throws PageException {

		return null;
	}

	@Override
	public Reference getReference(Object arg0, Key arg1) throws PageException {

		return null;
	}

	@Override
	public Credential getRemoteUser() throws PageException {

		return null;
	}

	@Override
	public int getRequestDialect() {

		return 0;
	}

	@Override
	public long getRequestTimeout() {

		return 0;
	}

	@Override
	public Throwable getRequestTimeoutException() {

		return null;
	}

	@Override
	public Charset getResourceCharset() {

		return null;
	}

	@Override
	public OutputStream getResponseStream() throws IOException {

		return null;
	}

	@Override
	public Resource getRootTemplateDirectory() {

		return null;
	}

	@Override
	public JspWriter getRootWriter() {

		return null;
	}

	@Override
	public short getSessionType() {

		return 0;
	}

	@Override
	public long getStartTime() {

		return 0;
	}

	@Override
	public Array getTemplatePath() throws PageException {

		return null;
	}

	@Override
	public Thread getThread() {

		return null;
	}

	@Override
	public Threads getThreadScope(String arg0) {

		return null;
	}

	@Override
	public Threads getThreadScope(Key arg0) {

		return null;
	}

	@Override
	public String[] getThreadScopeNames() {

		return null;
	}

	@Override
	public TimeZone getTimeZone() {

		return null;
	}

	@Override
	public String getURLToken() {

		return null;
	}

	@Override
	public Object getVariable(String arg0) throws PageException {

		return null;
	}

	@Override
	public VariableUtil getVariableUtil() {

		return null;
	}

	@Override
	public Charset getWebCharset() {

		return null;
	}

	@Override
	public void handlePageException(PageException arg0) {

	}

	@Override
	public boolean hasFamily() {

		return false;
	}

	@Override
	public boolean ignoreScopes() {

		return false;
	}

	@Override
	public void initBody(BodyTag arg0, int arg1) throws JspException {

	}

	@Override
	public Component loadComponent(String arg0) throws PageException {

		return null;
	}

	@Override
	public Object localGet() throws PageException {

		return null;
	}

	@Override
	public Object localGet(boolean arg0) throws PageException {

		return null;
	}

	@Override
	public Local localScope() {

		return null;
	}

	@Override
	public Local localScope(boolean arg0) {

		return null;
	}

	@Override
	public Object localTouch() throws PageException {

		return null;
	}

	@Override
	public Object localTouch(boolean arg0) throws PageException {

		return null;
	}

	@Override
	public void outputEnd() {

	}

	@Override
	public void outputStart() {

	}

	@Override
	public void param(String arg0, String arg1, Object arg2) throws PageException {

	}

	@Override
	public void param(String arg0, String arg1, Object arg2, int arg3) throws PageException {

	}

	@Override
	public void param(String arg0, String arg1, Object arg2, String arg3) throws PageException {

	}

	@Override
	public void param(String arg0, String arg1, Object arg2, double arg3, double arg4) throws PageException {

	}

	@Override
	public void releaseBody(BodyTag arg0, int arg1) {

	}

	@Override
	public void removeLastPageSource(boolean arg0) {

	}

	@Override
	public Object removeVariable(String arg0) throws PageException {

		return null;
	}

	@Override
	public Request requestScope() {

		return null;
	}

	@Override
	public Scope scope(int arg0) throws PageException {

		return null;
	}

	@Override
	public String serialize(Object arg0) throws PageException {

		return null;
	}

	@Override
	public Server serverScope() throws PageException {

		return null;
	}

	@Override
	public Session sessionScope() throws PageException {

		return null;
	}

	@Override
	public Object set(Object arg0, Key arg1, Object arg2) throws PageException {

		return null;
	}

	@Override
	public void setApplicationContext(ApplicationContext arg0) {

	}

	@Override
	public void setCFOutputOnly(boolean arg0) {

	}

	@Override
	public void setCFOutputOnly(short arg0) {

	}

	@Override
	public PageException setCatch(Throwable arg0) {

		return null;
	}

	@Override
	public void setCatch(PageException arg0) {

	}

	@Override
	public void setCatch(PageException arg0, boolean arg1, boolean arg2) {

	}

	@Override
	public void setErrorPage(ErrorPage arg0) {

	}

	@Override
	public void setExecutionTime(long arg0) {

	}

	@Override
	public void setFunctionScopes(Local arg0, Argument arg1) {

	}

	@Override
	public void setHeader(String arg0, String arg1) {

	}

	@Override
	public void setLocale(Locale arg0) {

	}

	@Override
	public void setLocale(String arg0) throws PageException {

	}

	@Override
	public void setPsq(boolean arg0) {

	}

	@Override
	public void setRemoteUser(Credential arg0) {

	}

	@Override
	public void setRequestTimeout(long arg0) {

	}

	@Override
	public boolean setSilent() {

		return false;
	}

	@Override
	public void setThreadScope(String arg0, Threads arg1) {

	}

	@Override
	public void setThreadScope(Key arg0, Threads arg1) {

	}

	@Override
	public void setTimeZone(TimeZone arg0) {

	}

	@Override
	public Object setVariable(String arg0, Object arg1) throws PageException {

		return null;
	}

	@Override
	public void setVariablesScope(Variables arg0) {

	}

	@Override
	public void throwCatch() throws PageException {

	}

	@Override
	public PageSource toPageSource(Resource arg0, PageSource arg1) {

		return null;
	}

	@Override
	public Object touch(Object arg0, Key arg1) throws PageException {

		return null;
	}

	@Override
	public Undefined undefinedScope() {

		return null;
	}

	@Override
	public boolean unsetSilent() {

		return false;
	}

	@Override
	public URLForm urlFormScope() {

		return null;
	}

	@Override
	public URL urlScope() {

		return null;
	}

	@Override
	public Undefined us() {

		return null;
	}

	@Override
	public Variables variablesScope() {

		return null;
	}

	@Override
	public void write(String arg0) throws IOException {

	}

	@Override
	public void writePSQ(Object arg0) throws IOException, PageException {

	}

	@Override
	public void initialize(Servlet servlet, ServletRequest request, ServletResponse response, String errorPageURL, boolean needsSession, int bufferSize, boolean autoFlush)
			throws IOException, IllegalStateException, IllegalArgumentException {

	}

	@Override
	public void release() {

	}

	@Override
	public HttpSession getSession() {

		return null;
	}

	@Override
	public ServletRequest getRequest() {

		return null;
	}

	@Override
	public ServletResponse getResponse() {

		return null;
	}

	@Override
	public Exception getException() {

		return null;
	}

	@Override
	public ServletConfig getServletConfig() {

		return null;
	}

	@Override
	public ServletContext getServletContext() {

		return null;
	}

	@Override
	public void forward(String relativeUrlPath) throws ServletException, IOException {

	}

	@Override
	public void include(String relativeUrlPath) throws ServletException, IOException {

	}

	@Override
	public void include(String relativeUrlPath, boolean flush) throws ServletException, IOException {

	}

	@Override
	public void handlePageException(Exception e) throws ServletException, IOException {

	}

	@Override
	public void handlePageException(Throwable t) throws ServletException, IOException {

	}

	@Override
	public void setAttribute(String name, Object value) {

	}

	@Override
	public void setAttribute(String name, Object value, int scope) {

	}

	@Override
	public Object getAttribute(String name) {

		return null;
	}

	@Override
	public Object getAttribute(String name, int scope) {

		return null;
	}

	@Override
	public Object findAttribute(String name) {

		return null;
	}

	@Override
	public void removeAttribute(String name) {

	}

	@Override
	public void removeAttribute(String name, int scope) {

	}

	@Override
	public int getAttributesScope(String name) {

		return 0;
	}

	@Override
	public Enumeration<String> getAttributeNamesInScope(int scope) {

		return null;
	}

	@Override
	public JspWriter getOut() {

		return null;
	}

	@Override
	public ExpressionEvaluator getExpressionEvaluator() {

		return null;
	}

	@Override
	public VariableResolver getVariableResolver() {

		return null;
	}

	@Override
	public ELContext getELContext() {
		return null;
	}

}
