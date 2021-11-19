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
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.Tag;

import lucee.commons.io.res.Resource;
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
import lucee.runtime.type.Collection;
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

/**
 * page context for every page object. the PageContext is a jsp page context expanded by CFML
 * functionality. for example you have the method getSession to get jsp compatible session object
 * (HTTPSession) and with sessionScope() you get CFML compatible session object (Struct,Scope).
 */
public abstract class PageContext extends javax.servlet.jsp.PageContext {

	/**
	 * returns matching scope
	 * 
	 * @return scope matching to defined scope definition
	 * @param type type of scope (Scope.xx)
	 * @throws PageException Page Exception
	 */
	public abstract Scope scope(int type) throws PageException;

	/**
	 * @return undefined scope, undefined scope is a placeholder for the scopecascading
	 */
	public abstract Undefined undefinedScope();

	/**
	 * @return variables scope
	 */
	public abstract Variables variablesScope();

	/**
	 * @return url scope
	 */
	public abstract URL urlScope();

	/**
	 * @return form scope
	 */
	public abstract Form formScope();

	/**
	 * @return scope mixed url and scope
	 */
	public abstract URLForm urlFormScope();

	/**
	 * @return request scope
	 */
	public abstract Request requestScope();

	/**
	 * @return request scope
	 */
	public abstract CGI cgiScope();

	/**
	 * @return application scope
	 * @throws PageException Page Exception
	 */
	public abstract Application applicationScope() throws PageException;

	/**
	 * @return arguments scope
	 */
	public abstract Argument argumentsScope();

	/**
	 * return the argument scope
	 * 
	 * @param bind indicate that the Argument Scope is bound for use outside of the udf
	 * @return Argument Scope
	 */
	public abstract Argument argumentsScope(boolean bind);

	/**
	 * @return arguments scope
	 */
	public abstract Local localScope();

	public abstract Local localScope(boolean bind);

	public abstract Object localGet() throws PageException;

	public abstract Object localGet(boolean bind) throws PageException;

	public abstract Object localTouch() throws PageException;

	public abstract Object localTouch(boolean bind) throws PageException;

	/**
	 * @return session scope
	 * @throws PageException Page Exception
	 */
	public abstract Session sessionScope() throws PageException;

	public abstract void setFunctionScopes(Local local, Argument argument);

	/**
	 * @return server scope
	 * @throws PageException Page Exception
	 */
	public abstract Server serverScope() throws PageException;

	/**
	 * @return cookie scope
	 */
	public abstract Cookie cookieScope();

	/**
	 * @return cookie scope
	 * @throws PageException Page Exception
	 */
	public abstract Client clientScope() throws PageException;

	public abstract Client clientScopeEL();

	/**
	 * @return cluster scope
	 * @throws PageException Page Exception
	 */
	public abstract Cluster clusterScope() throws PageException;

	/**
	 * cluster scope
	 * 
	 * @param create return null when false and scope does not exist
	 * @return cluster scope or null
	 * @throws PageException Page Exception
	 */
	public abstract Cluster clusterScope(boolean create) throws PageException;

	/**
	 * set property at a collection object
	 * 
	 * @param coll Collection Object (Collection, HashMap aso.)
	 * @param key key of the new value
	 * @param value new Value
	 * @return value setted
	 * @throws PageException Page Exception
	 */
	public abstract Object set(Object coll, Collection.Key key, Object value) throws PageException;

	/**
	 * touch a new property, if property doesn't existset a Struct, otherwise do nothing
	 * 
	 * @param coll Collection Object
	 * @param key key to touch
	 * @return Property
	 * @throws PageException Page Exception
	 */
	public abstract Object touch(Object coll, Collection.Key key) throws PageException;

	/**
	 * same like getProperty but return a collection object (QueryColumn) if return object is a Query
	 * 
	 * @param coll Collection Object
	 * @param key key to touch
	 * @return Property or QueryColumn
	 * @throws PageException Page Exception
	 * @deprecated use instead
	 *             <code>{@link #getCollection(Object, lucee.runtime.type.Collection.Key, Object)}</code>
	 */
	@Deprecated
	public abstract Object getCollection(Object coll, String key) throws PageException;

	/**
	 * same like getProperty but return a collection object (QueryColumn) if return object is a Query
	 * 
	 * @param coll Collection Object
	 * @param key key to touch
	 * @return Property or QueryColumn
	 * @throws PageException Page Exception
	 */
	public abstract Object getCollection(Object coll, Collection.Key key) throws PageException;

	/**
	 * same like getProperty but return a collection object (QueryColumn) if return object is a Query
	 * 
	 * @param coll Collection Object
	 * @param key key to touch
	 * @param defaultValue default value
	 * @return Property or QueryColumn
	 * @deprecated use instead
	 *             <code>{@link #getCollection(Object, lucee.runtime.type.Collection.Key, Object)}</code>
	 */
	@Deprecated
	public abstract Object getCollection(Object coll, String key, Object defaultValue);

	/**
	 * same like getProperty but return a collection object (QueryColumn) if return object is a Query
	 * 
	 * @param coll Collection Object
	 * @param key key to touch
	 * @param defaultValue default value
	 * @return Property or QueryColumn
	 */
	public abstract Object getCollection(Object coll, Collection.Key key, Object defaultValue);

	/**
	 * 
	 * @param coll Collection to get value
	 * @param key key of the value
	 * @return return value of a Collection, throws Exception if value doesn't exist
	 * @throws PageException Page Exception
	 * @deprecated use instead <code>{@link #get(Object, lucee.runtime.type.Collection.Key)}</code>
	 */
	@Deprecated
	public abstract Object get(Object coll, String key) throws PageException;

	/**
	 * 
	 * @param coll Collection to get value
	 * @param key key of the value
	 * @return return value of a Collection, throws Exception if value doesn't exist
	 * @throws PageException Page Exception
	 */
	public abstract Object get(Object coll, Collection.Key key) throws PageException;

	/**
	 * 
	 * @param coll Collection to get value
	 * @param key key of the value
	 * @return return value of a Collection, throws Exception if value doesn't exist
	 * @throws PageException Page Exception
	 * @deprecated use instead
	 *             <code>{@link #getReference(Object, lucee.runtime.type.Collection.Key)}</code>
	 */
	@Deprecated
	public abstract Reference getReference(Object coll, String key) throws PageException;

	/**
	 * 
	 * @param coll Collection to get value
	 * @param key key of the value
	 * @return return value of a Collection, throws Exception if value doesn't exist
	 * @throws PageException Page Exception
	 */
	public abstract Reference getReference(Object coll, Collection.Key key) throws PageException;

	/*
	 * * get data from a scope
	 * 
	 * @param scope
	 * 
	 * @param key1
	 * 
	 * @param key2
	 * 
	 * @return
	 * 
	 * @throws PageException / public abstract Object get(Scope scope, String key1, String key2) throws
	 * PageException;
	 */
	/*
	 * * get data from a scope
	 * 
	 * @param scope
	 * 
	 * @param key1
	 * 
	 * @param key2
	 * 
	 * @param key3
	 * 
	 * @return
	 * 
	 * @throws PageException / public abstract Object get(Scope scope, String key1, String key2, String
	 * key3) throws PageException;
	 */
	/*
	 * * get data from a scope
	 * 
	 * @param scope
	 * 
	 * @param key1
	 * 
	 * @param key2
	 * 
	 * @param key3
	 * 
	 * @param key4
	 * 
	 * @return
	 * 
	 * @throws PageException / public abstract Object get(Scope scope, String key1, String key2, String
	 * key3, String key4) throws PageException;
	 */
	/*
	 * * get data from a scope
	 * 
	 * @param scope
	 * 
	 * @param key1
	 * 
	 * @param key2
	 * 
	 * @param key3
	 * 
	 * @param key4
	 * 
	 * @param key5
	 * 
	 * @return
	 * 
	 * @throws PageException / public abstract Object get(Scope scope, String key1, String key2, String
	 * key3, String key4, String key5) throws PageException;
	 */
	/*
	 * * get data from a scope
	 * 
	 * @param scope
	 * 
	 * @param key1
	 * 
	 * @param key2
	 * 
	 * @param key3
	 * 
	 * @param key4
	 * 
	 * @param key5
	 * 
	 * @param key6
	 * 
	 * @return
	 * 
	 * @throws PageException / public abstract Object get(Scope scope, String key1, String key2, String
	 * key3, String key4, String key5, String key6) throws PageException;
	 */

	/*
	 * * set data from a scope
	 * 
	 * @param scope
	 * 
	 * @param key1
	 * 
	 * @param key2
	 * 
	 * @return
	 * 
	 * @throws PageException / public abstract Object set(Scope scope, String key1, String key2, Object
	 * value) throws PageException;
	 */
	/*
	 * * set data from a scope
	 * 
	 * @param scope
	 * 
	 * @param key1
	 * 
	 * @param key2
	 * 
	 * @param key3
	 * 
	 * @return
	 * 
	 * @throws PageException / public abstract Object set(Scope scope, String key1, String key2, String
	 * key3, Object value) throws PageException;
	 */
	/*
	 * * set data from a scope
	 * 
	 * @param scope
	 * 
	 * @param key1
	 * 
	 * @param key2
	 * 
	 * @param key3
	 * 
	 * @param key4
	 * 
	 * @return
	 * 
	 * @throws PageException / public abstract Object set(Scope scope, String key1, String key2, String
	 * key3, String key4, Object value) throws PageException;
	 */
	/*
	 * * set data from a scope
	 * 
	 * @param scope
	 * 
	 * @param key1
	 * 
	 * @param key2
	 * 
	 * @param key3
	 * 
	 * @param key4
	 * 
	 * @param key5
	 * 
	 * @return
	 * 
	 * @throws PageException / public abstract Object set(Scope scope, String key1, String key2, String
	 * key3, String key4, String key5, Object value) throws PageException;
	 */
	/*
	 * * set data from a scope
	 * 
	 * @param scope
	 * 
	 * @param key1
	 * 
	 * @param key2
	 * 
	 * @param key3
	 * 
	 * @param key4
	 * 
	 * @param key5
	 * 
	 * @param key6
	 * 
	 * @return
	 * 
	 * @throws PageException / public abstract Object set(Scope scope, String key1, String key2, String
	 * key3, String key4, String key5, String key6, Object value) throws PageException;
	 */

	/**
	 * 
	 * @param coll Collection to get value
	 * @param key key of the value
	 * @param defaultValue default value
	 * @return return value of a Collection, return null if value not exist
	 * @deprecated use instead
	 *             <code>{@link #get(Object, lucee.runtime.type.Collection.Key, Object)}</code>
	 */
	@Deprecated
	public abstract Object get(Object coll, String key, Object defaultValue);

	/**
	 * 
	 * @param coll Collection to get value
	 * @param key key of the value
	 * @param defaultValue default value
	 * @return return value of a Collection, return null if value not exist
	 */
	public abstract Object get(Object coll, Collection.Key key, Object defaultValue);

	/**
	 * sets a value by string syntax ("scopename.key.key" "url.name")
	 * 
	 * @param var Variable String name to set
	 * @param value value to set
	 * @return setted value
	 * @throws PageException Page Exception
	 */
	public abstract Object setVariable(String var, Object value) throws PageException;

	/**
	 * 
	 * @param var variable name to get
	 * @return return a value by string syntax ("scopename.key.key" "url.name")
	 * @throws PageException Page Exception
	 **/
	public abstract Object getVariable(String var) throws PageException;

	/**
	 * evaluate given expression
	 * 
	 * @param expression expression to evaluate
	 * @return return value generated by expression or null
	 * @throws PageException Page Exception
	 **/
	public abstract Object evaluate(String expression) throws PageException;

	public abstract String serialize(Object expression) throws PageException;

	/**
	 * 
	 * @param var variable name to get
	 * @return return a value by string syntax ("scopename.key.key" "url.name")
	 * @throws PageException Page Exception
	 */
	public abstract Object removeVariable(String var) throws PageException;

	/**
	 * get variable from string definition and cast it to a Query Object
	 * 
	 * @param key Variable Name to get
	 * @return Query
	 * @throws PageException Page Exception
	 */
	public abstract Query getQuery(String key) throws PageException;

	public abstract Query getQuery(Object value) throws PageException;

	/**
	 * write a value to the header of the response
	 * 
	 * @param name name of the value to set
	 * @param value value to set
	 */
	public abstract void setHeader(String name, String value);

	/**
	 * @return returns the cfid of the current user
	 */
	public abstract String getCFID();

	/**
	 * @return returns the current cftoken of the user
	 */
	public abstract String getCFToken();

	/**
	 * @return return the session id
	 */
	public abstract String getJSessionId();

	/**
	 * @return returns the urltoken of the current user
	 */
	public abstract String getURLToken();

	/**
	 * @return returns the page context id
	 */
	public abstract int getId();

	public abstract JspWriter getRootWriter();

	/**
	 * @return Returns the locale.
	 */
	public abstract Locale getLocale();

	/**
	 * @param locale The locale to set
	 */
	public abstract void setLocale(Locale locale);

	/**
	 * @param strLocale The locale to set as String.
	 * @throws PageException Page Exception
	 * @deprecated use instead <code>{@link #setLocale(Locale)}</code>
	 */
	@Deprecated
	public abstract void setLocale(String strLocale) throws PageException;

	/**
	 * @return Returns the Config Object of the PageContext.
	 */
	public abstract ConfigWeb getConfig();

	/**
	 * return HttpServletRequest, getRequest only returns ServletRequest
	 * 
	 * @return HttpServletRequest
	 */
	public abstract HttpServletRequest getHttpServletRequest();

	/**
	 * return HttpServletResponse, getResponse only returns ServletResponse
	 * 
	 * @return HttpServletResponse
	 */
	public abstract HttpServletResponse getHttpServletResponse();

	public abstract OutputStream getResponseStream() throws IOException;

	/**
	 * returns the tag that is in use
	 * 
	 * @return Returns the currentTag.
	 */
	public abstract Tag getCurrentTag();

	/**
	 * @return Returns the applicationContext.
	 */
	public abstract ApplicationContext getApplicationContext();

	/**
	 * Writes a String to the Response Buffer
	 * 
	 * @param str string
	 * @throws IOException IO Exception
	 */
	public abstract void write(String str) throws IOException;

	/**
	 * Writes a String to the Response Buffer,also when cfoutputonly is true and execution is outside of
	 * a cfoutput
	 * 
	 * @param str string
	 * @throws IOException IO Exception
	 */
	public abstract void forceWrite(String str) throws IOException;

	/**
	 * Writes a String to the Response Buffer,also when cfoutputonly is true and execution is outside of
	 * a cfoutput
	 * 
	 * @param o object
	 * @throws IOException IO Exception
	 * @throws PageException Page Exception
	 */
	public abstract void writePSQ(Object o) throws IOException, PageException;

	/**
	 * @return the current template PageSource
	 * @deprecated use instead {@link #getCurrentPageSource(PageSource)}
	 */
	@Deprecated
	public abstract PageSource getCurrentPageSource();

	/**
	 * @param defaultValue default value
	 * @return the current template PageSource
	 */
	public abstract PageSource getCurrentPageSource(PageSource defaultValue);

	/**
	 * @return the current template PageSource
	 */
	public abstract PageSource getCurrentTemplatePageSource();

	/**
	 * @return base template file
	 */
	public abstract PageSource getBasePageSource();

	/**
	 * sets the pagecontext silent
	 * 
	 * @return return setting that was before
	 */
	public abstract boolean setSilent();

	/**
	 * unsets the pagecontext silent
	 * 
	 * @return return setting that was before
	 */
	public abstract boolean unsetSilent();

	/**
	 * return debugger of the page Context
	 * 
	 * @return debugger
	 */
	public abstract Debugger getDebugger();

	/**
	 * 
	 * @return Returns the executionTime.
	 */
	public abstract long getExecutionTime();

	/**
	 * @param executionTime The executionTime to set.
	 */
	public abstract void setExecutionTime(long executionTime);

	/**
	 * @return Returns the remoteUser.
	 * @throws PageException Page Exception
	 */
	public abstract Credential getRemoteUser() throws PageException;

	/**
	 * clear the remote user
	 */
	public abstract void clearRemoteUser();

	/**
	 * @param remoteUser The remoteUser to set.
	 */
	public abstract void setRemoteUser(Credential remoteUser);

	/**
	 * array of current template stack
	 * 
	 * @return array
	 * @throws PageException Page Exception
	 */
	public abstract Array getTemplatePath() throws PageException;

	/**
	 * returns the current level, how deep is the page stack
	 * 
	 * @return level
	 */
	public abstract int getCurrentLevel();

	/**
	 * @return Returns the variableUtil.
	 */
	public abstract VariableUtil getVariableUtil();

	/**
	 * @param applicationContext The applicationContext to set.
	 */
	public abstract void setApplicationContext(ApplicationContext applicationContext);

	public abstract PageSource toPageSource(Resource res, PageSource defaultValue);

	/**
	 * set another variable scope
	 * 
	 * @param scope scope
	 */
	public abstract void setVariablesScope(Variables scope);

	/**
	 * includes a path from an absolute path
	 * 
	 * @param source absolute path as file object
	 * @param runOnce include only once per request
	 * @throws PageException Page Exception
	 */
	public abstract void doInclude(PageSource[] source, boolean runOnce) throws PageException;

	/**
	 * includes a path from an absolute path
	 * 
	 * @param source absolute path as file object
	 * @throws PageException Page Exception
	 * @deprecated used <code> doInclude(String source, boolean runOnce)</code> instead. Still used by
	 *             extensions ...
	 */
	@Deprecated
	public abstract void doInclude(String source) throws PageException;

	/**
	 * includes a path from an absolute path
	 * 
	 * @param source absolute path as file object
	 * @param runOnce include only once per request
	 * @throws PageException Page Exception
	 */
	public abstract void doInclude(String source, boolean runOnce) throws PageException;

	/**
	 * clear the current output buffer
	 */
	public abstract void clear();

	/**
	 * @return return the request timeout for this pagecontext in milli seconds
	 */
	public abstract long getRequestTimeout();

	/**
	 * @param requestTimeout The requestTimeout to set.
	 */
	public abstract void setRequestTimeout(long requestTimeout);

	/**
	 * sets state of cfoutput only
	 * 
	 * @param boolEnablecfoutputonly enable cfoutput only
	 */
	public abstract void setCFOutputOnly(boolean boolEnablecfoutputonly);

	/**
	 * returns if single quotes will be preserved inside a query tag (psq=preserve single quote)
	 * 
	 * @return preserve single quote
	 */
	public abstract boolean getPsq();

	/**
	 * Close the response stream.
	 */
	public abstract void close();

	/**
	 * adds a PageSource
	 * 
	 * @param ps page source
	 * @param alsoInclude also include
	 */
	public abstract void addPageSource(PageSource ps, boolean alsoInclude);

	/**
	 * clear all catches
	 */
	public abstract void clearCatch();

	/**
	 * execute a request to the PageConext
	 * 
	 * @param realPath path
	 * @param throwException catch or throw exceptions
	 * @param onlyTopLevel only check top level mappings for the matching realpath
	 * @throws PageException Page Exception
	 */
	public abstract void execute(String realPath, boolean throwException, boolean onlyTopLevel) throws PageException;

	/**
	 * execute a request to the PageConext form CFML
	 * 
	 * @param realPath path
	 * @param throwException catch or throw exceptions
	 * @param onlyTopLevel only check top level mappings for the matching realpath
	 * @throws PageException Page Exception
	 */
	public abstract void executeCFML(String realPath, boolean throwException, boolean onlyTopLevel) throws PageException;

	public abstract void executeRest(String realPath, boolean throwException) throws PageException;

	/**
	 * Flush Content of buffer to the response stream of the Socket.
	 */
	public abstract void flush();

	/**
	 * call a UDF Function and return "return value" of the function
	 * 
	 * @param coll Collection of the UDF Function
	 * @param key name of the function
	 * @param args arguments to call the function
	 * @return return value of the function
	 * @throws PageException Page Exception
	 */
	public abstract Object getFunction(Object coll, String key, Object[] args) throws PageException;

	/**
	 * call a UDF Function and return "return value" of the function
	 * 
	 * @param coll Collection of the UDF Function
	 * @param key name of the function
	 * @param args arguments to call the function
	 * @return return value of the function
	 * @throws PageException Page Exception
	 */
	public abstract Object getFunction(Object coll, Collection.Key key, Object[] args) throws PageException;

	/**
	 * call a UDF Function and return "return value" of the function
	 * 
	 * @param coll Collection of the UDF Function
	 * @param key name of the function
	 * @param args arguments to call the function
	 * @return return value of the function
	 * @throws PageException Page Exception
	 */
	public abstract Object getFunctionWithNamedValues(Object coll, String key, Object[] args) throws PageException;

	/**
	 * call a UDF Function and return "return value" of the function
	 * 
	 * @param coll Collection of the UDF Function
	 * @param key name of the function
	 * @param args arguments to call the function
	 * @return return value of the function
	 * @throws PageException Page Exception
	 */
	public abstract Object getFunctionWithNamedValues(Object coll, Collection.Key key, Object[] args) throws PageException;

	/**
	 * get variable from string definition and cast it to an Iterator Object
	 * 
	 * @param key Variable Name to get
	 * @return Iterator
	 * @throws PageException Page Exception
	 */
	public abstract Iterator getIterator(String key) throws PageException;

	/**
	 * @return directory of root template file
	 */
	public abstract Resource getRootTemplateDirectory();

	/**
	 * @return Returns the startTime.
	 */
	public abstract long getStartTime();

	/**
	 * @return Returns the thread.
	 */
	public abstract Thread getThread();

	/**
	 * specialised method for handlePageException with argument Exception or Throwable
	 * 
	 * @param pe Page Exception
	 */
	public abstract void handlePageException(PageException pe);

	/*
	 * *
	 * 
	 * @param applicationFile
	 * 
	 * @throws ServletException
	 */
	// public abstract void includeOnRequestEnd(PageSource applicationFile) throws ServletException;

	/**
	 * ends a cfoutput block
	 */
	public abstract void outputEnd();

	/**
	 * starts a cfoutput block
	 */
	public abstract void outputStart();

	/**
	 * remove the last PageSource
	 * 
	 * @param alsoInclude also include
	 */
	public abstract void removeLastPageSource(boolean alsoInclude);

	/**
	 * sets an exception
	 * 
	 * @param t throwable
	 * @return PageException
	 */
	public abstract PageException setCatch(Throwable t);

	public abstract PageException getCatch();

	public abstract void setCatch(PageException pe);

	public abstract void setCatch(PageException pe, boolean caught, boolean store);

	public abstract void exeLogStart(int position, String id);

	public abstract void exeLogEnd(int position, String id);

	/**
	 * sets state of cfoutput only
	 * 
	 * @param enablecfoutputonly enable cfoutput flag
	 */
	public abstract void setCFOutputOnly(short enablecfoutputonly);

	/**
	 * sets the error page
	 * 
	 * @param ep error page
	 */
	public abstract void setErrorPage(ErrorPage ep);

	/**
	 * sets if inside a query tag single quote will be preserved (preserve single quote)
	 * 
	 * @param psq sets preserve single quote for query
	 */
	public abstract void setPsq(boolean psq);

	/**
	 * return thrown exception
	 * 
	 * @throws PageException Page Exception
	 */
	public abstract void throwCatch() throws PageException;

	/**
	 * @return undefined scope, undefined scope is a placeholder for the scopecascading
	 */
	public abstract Undefined us();

	/**
	 * compile a CFML Template
	 * 
	 * @param templatePath path
	 * @throws PageException Page Exception
	 * @deprecated use instead <code>compile(PageSource pageSource)</code>
	 */
	@Deprecated
	public abstract void compile(String templatePath) throws PageException;

	/**
	 * compile a CFML Template
	 * 
	 * @param pageSource page source
	 * @throws PageException Page Exception
	 */
	public abstract void compile(PageSource pageSource) throws PageException;

	/**
	 * init body of a tag
	 * 
	 * @param bodyTag body tag
	 * @param state state
	 * @throws JspException JSP Exception
	 */
	public abstract void initBody(BodyTag bodyTag, int state) throws JspException;

	/**
	 * release body of a tag
	 * 
	 * @param bodyTag body tag
	 * @param state state
	 */
	public abstract void releaseBody(BodyTag bodyTag, int state);

	/**
	 * @param type type
	 * @param name name
	 * @param defaultValue default value
	 * @throws PageException Page Exception
	 */
	public abstract void param(String type, String name, Object defaultValue) throws PageException;

	/**
	 * @param type type
	 * @param name name
	 * @param defaultValue default value
	 * @param maxLength max length
	 * @throws PageException Page Exception
	 */
	public abstract void param(String type, String name, Object defaultValue, int maxLength) throws PageException;

	/**
	 * @param type type
	 * @param name name
	 * @param defaultValue default value
	 * @param pattern pattern
	 * @throws PageException Page Exception
	 */
	public abstract void param(String type, String name, Object defaultValue, String pattern) throws PageException;

	/**
	 * @param type type
	 * @param name name
	 * @param defaultValue default value
	 * @param min min value
	 * @param max max value
	 * @throws PageException Page Exception
	 */
	public abstract void param(String type, String name, Object defaultValue, double min, double max) throws PageException;

	// public abstract PageContext clonePageContext();

	// public abstract boolean isCFCRequest();

	public abstract DataSourceManager getDataSourceManager();

	public abstract CFMLFactory getCFMLFactory();

	public abstract PageContext getParentPageContext();

	/**
	 * @param name thread scope name
	 * @return thread scope name
	 * @deprecated use instead <code>setThreadScope(Collection.Key name,Threads t)</code>
	 */
	@Deprecated
	public abstract Threads getThreadScope(String name);

	public abstract Threads getThreadScope(Collection.Key name);

	/**
	 * set a thread to the context
	 * 
	 * @param name thread scope name
	 * @param t threads
	 * @deprecated use instead <code>setThreadScope(Collection.Key name,Threads t)</code>
	 */
	@Deprecated
	public abstract void setThreadScope(String name, Threads t);

	public abstract void setThreadScope(Collection.Key name, Threads t);

	/**
	 * @return return an Array with names off all threads running.
	 */
	public abstract String[] getThreadScopeNames();

	public abstract boolean hasFamily();

	public abstract Component loadComponent(String compPath) throws PageException;

	// public abstract void setActiveComponent(Component component);

	/**
	 * @return Returns the active Component.
	 */
	public abstract Component getActiveComponent();

	public abstract UDF getActiveUDF();

	public abstract TimeZone getTimeZone();

	public abstract void setTimeZone(TimeZone timeZone);

	public abstract short getSessionType();

	public abstract DataSource getDataSource(String datasource) throws PageException;

	public abstract DataSource getDataSource(String datasource, DataSource defaultValue);

	public abstract Charset getResourceCharset();

	public abstract Charset getWebCharset();

	public abstract Object getCachedWithin(int type);

	/**
	 * 
	 * @return get the dialect for the current template
	 */
	public abstract int getCurrentTemplateDialect();

	/**
	 * 
	 * @return get the dialect for the current template
	 */
	public abstract int getRequestDialect();

	/**
	 * @param create if set to true, lucee creates a session when not exist
	 * @return ORM Session
	 * @throws PageException Page Exception
	 */
	public abstract ORMSession getORMSession(boolean create) throws PageException;

	public abstract Throwable getRequestTimeoutException(); // FUTURE deprecate

	/**
	 * if set to true Lucee ignores all scope names and handles them as regular keys for the undefined
	 * scope
	 * 
	 * @return Ignore Scopes
	 */
	public abstract boolean ignoreScopes();
}