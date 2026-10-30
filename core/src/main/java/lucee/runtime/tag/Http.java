/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
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
package lucee.runtime.tag;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;


import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.SystemUtil.TemplateLine;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.PageContextThread;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.mimetype.ContentType;
import lucee.commons.net.HTTPUtil;
import lucee.commons.net.URLEncoder;
import lucee.commons.net.http.HTTPEngine;
import lucee.commons.net.http.HTTPFeatureNotSupportedException;
import lucee.commons.net.http.HTTPRequest;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.Header;
import lucee.commons.net.http.CachingGZIPInputStream;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.cache.tag.CacheHandler;
import lucee.runtime.cache.tag.CacheHandlerCollectionImpl;
import lucee.runtime.cache.tag.CacheHandlerPro;
import lucee.runtime.cache.tag.CacheItem;
import lucee.runtime.cache.tag.http.HTTPCacheItem;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.Constants;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.AbortException;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.HTTPException;
import lucee.runtime.exp.NativeException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.RequestTimeoutException;
import lucee.runtime.ext.tag.BodyTagImpl;
import lucee.runtime.functions.file.GetFileFromPath;
import lucee.runtime.functions.system.CallStackGet;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.listener.ApplicationContextSupport;
import lucee.runtime.net.http.CertificateInstaller;
import lucee.runtime.net.http.MultiPartResponseUtils;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.net.http.SSLUtil;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.security.SecurityManagerImpl;
import lucee.runtime.tag.listener.TagListener;
import lucee.runtime.text.csv.CSVParser;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.dt.TimeSpanImpl;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.HTTPStruct;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.util.PageContextUtil;
import lucee.runtime.util.URLResolver;

// MUST change behavor of mltiple headers now is an array, it das so?

/**
 * Lets you execute HTTP POST and GET operations on files. Using cfhttp, you can execute standard
 * GET operations and create a query object from a text file. POST operations lets you upload MIME
 * file types to a server, or post cookie, formfield, URL, file, or CGI variables directly to a
 * specified server.
 *
 *
 *
 *
 **/
public final class Http extends BodyTagImpl {

	private static final TimeSpan MAX_CONN_TIMEOUT_MS = TimeSpanImpl.fromMillis(20000L);
	public static final String MULTIPART_RELATED = "multipart/related";
	public static final String MULTIPART_FORM_DATA = "multipart/form-data";

	/**
	 * Maximum redirect count (5)
	 */
	public static final short MAX_REDIRECT = 15;

	/**
	 * Constant value for HTTP Status Code "moved Permanently 301"
	 */
	public static final int STATUS_REDIRECT_MOVED_PERMANENTLY = 301;
	/**
	 * Constant value for HTTP Status Code "Found 302"
	 */
	public static final int STATUS_REDIRECT_FOUND = 302;
	/**
	 * Constant value for HTTP Status Code "see other 303"
	 */
	public static final int STATUS_REDIRECT_SEE_OTHER = 303;

	public static final int STATUS_REDIRECT_TEMPORARY_REDIRECT = 307;

	private static final short METHOD_GET = 0;
	private static final short METHOD_POST = 1;
	private static final short METHOD_HEAD = 2;
	private static final short METHOD_PUT = 3;
	private static final short METHOD_DELETE = 4;
	private static final short METHOD_OPTIONS = 5;
	private static final short METHOD_TRACE = 6;
	private static final short METHOD_PATCH = 7;

	private static final Pattern SENSITIVE_NAME = Pattern.compile("authorization|cookie|passw(or)?d|secret|token|api[-_]?key", Pattern.CASE_INSENSITIVE);
	private static final String MASK = "***";

	private static final List<String> methods = Arrays.asList(new String[] { "GET", "POST", "HEAD", "PUT", "DELETE", "OPTIONS", "TRACE", "PATCH", "QUERY" });

	private static final String NO_MIMETYPE = "Unable to determine MIME type of file.";

	private static final short GET_AS_BINARY_NO = 0;
	private static final short GET_AS_BINARY_YES = 1;
	private static final short GET_AS_BINARY_AUTO = 2;

	private static final Key SET_COOKIE = KeyImpl.getInstance("set-cookie");

	private static final short AUTH_TYPE_BASIC = 0;
	private static final short AUTH_TYPE_NTLM = 1;

	public static final short ENCODED_AUTO = HTTPUtil.ENCODED_AUTO;
	public static final short ENCODED_YES = HTTPUtil.ENCODED_YES;
	public static final short ENCODED_NO = HTTPUtil.ENCODED_NO;

	private ArrayList<HttpParamBean> params = new ArrayList<HttpParamBean>();

	/** When required by a server, a valid password. */
	private String password;

	/** Required for creating a query. Options are a tab or comma. Default is a comma. */
	private char delimiter = ',';

	/**
	 * Yes or No. Default is No. For GET and POST operations, if Yes, page reference returned into the
	 * fileContent internal variable has its internal URLs fully resolved, including port number, so
	 * that links remain intact.
	 */
	private boolean resolveurl;

	/** A value, in seconds. When a URL timeout is specified in the browser */
	private TimeSpan timeout = null;
	private TimeSpan socketTimeout = null;
	private TimeSpan connectionTimeout = null;

	/** Host name or IP address of a proxy server. */
	private String proxyserver;

	/**
	 * The filename to be used for the file that is accessed. For GET operations, defaults to the name
	 * pecified in url. Enter path information in the path attribute.
	 */
	private String strFile;

	/**
	 * The path to the directory in which a file is to be stored. If a path is not specified in a POST
	 * or GET operation, a variable is created (cfhttp.fileContent) that you can use to display the
	 * results of the POST operation in a cfoutput.
	 */
	private String strPath;

	/**
	 * Boolean indicating whether to throw an exception that can be caught by using the cftry and
	 * cfcatch tags. The default is NO.
	 */
	private boolean throwonerror;

	/** set the charset for the call. */
	private String charset = null;

	/**
	 * The port number on the proxy server from which the object is requested. Default is 80. When used
	 * with resolveURL, the URLs of retrieved documents that specify a port number are automatically
	 * resolved to preserve links in the retrieved document.
	 */
	private int proxyport = 80;

	/** Specifies the column names for a query when creating a query as a result of a cfhttp GET. */
	private String[] columns;

	/**
	 * The port number on the server from which the object is requested. Default is 80. When used with
	 * resolveURL, the URLs of retrieved documents that specify a port number are automatically resolved
	 * to preserve links in the retrieved document. If a port number is specified in the url attribute,
	 * the port value overrides the value of the port attribute.
	 */
	private int port = -1;

	/** User agent request header. */
	private String useragent = Constants.NAME + " (CFML Engine)";

	/**
	 * Required for creating a query. Indicates the start and finish of a column. Should be
	 * appropriately escaped when embedded in a column. For example, if the qualifier is a double
	 * quotation mark, it should be escaped as """". If there is no text qualifier in the file, specify
	 * it as " ". Default is the double quotation mark (").
	 */
	private char textqualifier = '"';

	/** When required by a server, a valid username. */
	private String username;

	/**
	 * Full URL of the host name or IP address of the server on which the file resides. The URL must be
	 * an absolute URL, including the protocol (http or https) and hostname. It may optionally contain a
	 * port number. Port numbers specified in the url attribute override the port attribute.
	 */
	private String url;

	/** Boolean or Lax indicating whether to redirect execution or stop execution. */
	private String redirect = "true";

	/** The name to assign to a query if the a query is constructed from a file. */
	private String name;

	/**
	 * GET or POST. Use GET to download a text or binary file or to create a query from the contents of
	 * a text file. Use POST to send information to a server page or a CGI program for processing. POST
	 * requires the use of a cfhttpparam tag.
	 */
	private short method = METHOD_GET;

	// private boolean hasBody=false;

	private boolean firstrowasheaders = true;

	private String proxyuser = null;
	private String proxypassword = "";
	private boolean multiPart = false;
	private String multiPartType = MULTIPART_FORM_DATA;

	private short getAsBinary = GET_AS_BINARY_NO;
	private String result = "cfhttp";

	private boolean addtoken = false;
	private boolean encode = true;

	private short authType = AUTH_TYPE_BASIC;
	private String workStation = null;
	private String domain = null;
	private boolean preauth = true;
	private short encoded = ENCODED_AUTO;

	private boolean compression = true;

	private Object cachedWithin;

	private boolean usePool = true;

	/** The full path to a PKCS12 format file that contains the client certificate for the request. */
	private String clientCert;
	/** Password used to decrypt the client certificate. */
	private String clientCertPassword;
	private boolean autoCert = false;

	/** Path to a custom trust store (JKS or PKCS12) for SSL certificate validation. */
	private String trustStore;
	/** Password for the custom trust store. */
	private String trustStorePassword;
	/** When false, disables SSL certificate and hostname verification (like curl -k). */
	private boolean sslVerify = true;

	/** Listener (before, after, error) for this tag, overrides the listener defined in the application. */
	private TagListener listener;

	@Override
	public void release() {
		super.release();
		params.clear();
		password = null;
		delimiter = ',';
		resolveurl = false;
		timeout = null;
		connectionTimeout = null;
		socketTimeout = null;
		proxyserver = null;
		proxyport = 80;
		proxyuser = null;
		proxypassword = "";
		strFile = null;
		throwonerror = false;
		charset = null;
		columns = null;
		port = -1;
		useragent = Constants.NAME + " (CFML Engine)";
		textqualifier = '"';
		username = null;
		url = null;
		redirect = "true";
		strPath = null;
		name = null;
		method = METHOD_GET;
		// hasBody=false;
		firstrowasheaders = true;

		getAsBinary = GET_AS_BINARY_NO;
		multiPart = false;
		multiPartType = MULTIPART_FORM_DATA;
		result = "cfhttp";
		addtoken = false;
		encode = true;

		authType = AUTH_TYPE_BASIC;
		workStation = null;
		domain = null;
		preauth = true;
		encoded = ENCODED_AUTO;
		compression = true;
		clientCert = null;
		clientCertPassword = null;
		cachedWithin = null;
		usePool = true;
		autoCert = false;
		trustStore = null;
		trustStorePassword = null;
		sslVerify = true;
		listener = null;
	}

	public void setListener(Object listener) throws ApplicationException {
		if (listener == null) return;
		this.listener = lucee.runtime.tag.Query.toTagListener(listener);
	}

	/**
	 * @param firstrowasheaders
	 */
	public void setFirstrowasheaders(boolean firstrowasheaders) {
		this.firstrowasheaders = firstrowasheaders;
	}

	public void setEncodeurl(boolean encoded) {
		this.encoded = encoded ? ENCODED_YES : ENCODED_NO;
	}

	/**
	 * set the value password When required by a server, a valid password.
	 * 
	 * @param password value to set
	 **/
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * set the value password When required by a proxy server, a valid password.
	 * 
	 * @param proxypassword value to set
	 **/
	public void setProxypassword(String proxypassword) {
		this.proxypassword = proxypassword;
	}

	/**
	 * set the value delimiter Required for creating a query. Options are a tab or comma. Default is a
	 * comma.
	 * 
	 * @param delimiter value to set
	 **/
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter.length() == 0 ? ',' : delimiter.charAt(0);
	}

	/**
	 * set the value resolveurl Yes or No. Default is No. For GET and POST operations, if Yes, page
	 * reference returned into the fileContent internal variable has its internal URLs fully resolved,
	 * including port number, so that links remain intact.
	 * 
	 * @param resolveurl value to set
	 **/
	public void setResolveurl(boolean resolveurl) {
		this.resolveurl = resolveurl;
	}

	public void setPreauth(boolean preauth) {
		this.preauth = preauth;
	}

	/**
	 * set the value timeout
	 * 
	 * @param timeout value to set
	 * @throws ExpressionException
	 **/
	public void setTimeout(Object timeout) throws PageException {
		this.timeout = toTimeout(timeout, "timeout");
	}

	public void setConnectiontimeout(Object timeout) throws PageException {
		this.connectionTimeout = toTimeout(timeout, "connectionTimeout");
	}

	public void setSockettimeout(Object timeout) throws PageException {
		this.socketTimeout = toTimeout(timeout, "socketTimeout");
	}

	private static TimeSpan toTimeout(Object timeout, String label) throws PageException {
		if (StringUtil.isEmpty(timeout)) return null;
		if (timeout instanceof TimeSpan) return (TimeSpan) timeout;
		int i = Caster.toIntValue(timeout);
		if (i < 0) throw new ApplicationException("invalid value [" + i + "] for attribute " + label + ", value must be a positive integer greater or equal than 0");

		return new TimeSpanImpl(0, 0, 0, i);

	}

	/**
	 * set the value proxyserver Host name or IP address of a proxy server.
	 * 
	 * @param proxyserver value to set
	 **/
	public void setProxyserver(String proxyserver) {
		this.proxyserver = proxyserver;
	}

	/**
	 * set the value proxyport The port number on the proxy server from which the object is requested.
	 * Default is 80. When used with resolveURL, the URLs of retrieved documents that specify a port
	 * number are automatically resolved to preserve links in the retrieved document.
	 * 
	 * @param proxyport value to set
	 **/
	public void setProxyport(double proxyport) {
		this.proxyport = (int) proxyport;
	}

	/**
	 * set the value file The filename to be used for the file that is accessed. For GET operations,
	 * defaults to the name pecified in url. Enter path information in the path attribute.
	 * 
	 * @param file value to set
	 **/
	public void setFile(String file) {
		this.strFile = file;
	}

	/**
	 * set the value throwonerror Boolean indicating whether to throw an exception that can be caught by
	 * using the cftry and cfcatch tags. The default is NO.
	 * 
	 * @param throwonerror value to set
	 **/
	public void setThrowonerror(boolean throwonerror) {
		this.throwonerror = throwonerror;
	}

	/**
	 * set the value charset set the charset for the call.
	 * 
	 * @param charset value to set
	 **/
	public void setCharset(String charset) {
		this.charset = charset;
	}

	/**
	 * set the autoCert value
	 * 
	 * @param autoCert value to set
	 **/
	public void setAutocert(boolean autoCert) {
		this.autoCert = autoCert;
	}

	/**
	 * set the value columns
	 * 
	 * @param columns value to set
	 * @throws PageException
	 **/
	public void setColumns(String columns) throws PageException {
		this.columns = ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(columns, ","));
	}

	/**
	 * set the value port The port number on the server from which the object is requested. Default is
	 * 80. When used with resolveURL, the URLs of retrieved documents that specify a port number are
	 * automatically resolved to preserve links in the retrieved document. If a port number is specified
	 * in the url attribute, the port value overrides the value of the port attribute.
	 * 
	 * @param port value to set
	 **/
	public void setPort(double port) {
		this.port = (int) port;
	}

	/**
	 * set the value useragent User agent request header.
	 * 
	 * @param useragent value to set
	 **/
	public void setUseragent(String useragent) {
		this.useragent = useragent;
	}

	/**
	 * set the value textqualifier Required for creating a query. Indicates the start and finish of a
	 * column. Should be appropriately escaped when embedded in a column. For example, if the qualifier
	 * is a double quotation mark, it should be escaped as """". If there is no text qualifier in the
	 * file, specify it as " ". Default is the double quotation mark (").
	 * 
	 * @param textqualifier value to set
	 **/
	public void setTextqualifier(String textqualifier) {
		this.textqualifier = textqualifier.length() == 0 ? '"' : textqualifier.charAt(0);
	}

	/**
	 * set the value username When required by a proxy server, a valid username.
	 * 
	 * @param proxyuser value to set
	 **/
	public void setProxyuser(String proxyuser) {
		this.proxyuser = proxyuser;
	}

	/**
	 * set the value username When required by a server, a valid username.
	 * 
	 * @param username value to set
	 **/
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * set the value url Full URL of the host name or IP address of the server on which the file
	 * resides. The URL must be an absolute URL, including the protocol (http or https) and hostname. It
	 * may optionally contain a port number. Port numbers specified in the url attribute override the
	 * port attribute.
	 * 
	 * @param url value to set
	 **/
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * set the value redirect, boolean or lax
	 * 
	 * @param redirect value to set
	 **/
	public void setRedirect(String redirect) throws ExpressionException {
		if ("lax".equalsIgnoreCase(redirect)) this.redirect = redirect;
		else if (Decision.isBoolean(redirect)) this.redirect = redirect;
		else throw new ExpressionException("Invalid value [" + redirect + "] for attribute [redirect], value must be one of the following [ true, false, lax ]");
	}

	/**
	 * set the value path The path to the directory in which a file is to be stored. If a path is not
	 * specified in a POST or GET operation, a variable is created (cfhttp.fileContent) that you can use
	 * to display the results of the POST operation in a cfoutput.
	 * 
	 * @param path value to set
	 **/
	public void setPath(String path) {
		this.strPath = path;
	}

	/**
	 * set the value name The name to assign to a query if the a query is constructed from a file.
	 * 
	 * @param name value to set
	 **/
	public void setName(String name) {
		this.name = name;
	}

	public void setAuthtype(String strAuthType) throws ExpressionException {
		if (StringUtil.isEmpty(strAuthType, true)) return;
		strAuthType = strAuthType.trim();
		if ("basic".equalsIgnoreCase(strAuthType)) authType = AUTH_TYPE_BASIC;
		else if ("ntlm".equalsIgnoreCase(strAuthType)) authType = AUTH_TYPE_NTLM;
		else throw new ExpressionException("invalid value [" + strAuthType + "] for attribute [authType], value must be one of the following [ basic, ntlm ]");
	}

	public void setWorkstation(String workStation) {
		this.workStation = workStation;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * set the value method GET or POST. Use GET to download a text or binary file or to create a query
	 * from the contents of a text file. Use POST to send information to a server page or a CGI program
	 * for processing. POST requires the use of a cfhttpparam tag.
	 * 
	 * @param method value to set
	 * @throws ApplicationException
	 **/
	public void setMethod(String method) throws ApplicationException {
		method = method.toUpperCase().trim();
		short idx = (short) methods.indexOf(method);
		if (idx < 0) throw new ApplicationException("invalid method type [" + method + "], valid types are [" + ListUtil.arrayToList(methods.toArray(new String[0]), ",") + "]");
		this.method = idx;
	}

	private static String getMethodAsVerb(short method) throws ApplicationException {
		if (method < 0 || method > methods.size() - 1)
			throw new ApplicationException("invalid method [" + method + "], valid types are [" + ListUtil.arrayToList(methods.toArray(new String[0]), ",") + "]"); // never will
																																									// reach this,
																																									// due to above

		return methods.get(method);
	}

	public void setCompression(String strCompression) throws ApplicationException {
		if (StringUtil.isEmpty(strCompression, true)) return;
		Boolean b = Caster.toBoolean(strCompression, null);

		if (b != null) compression = b.booleanValue();
		else if (strCompression.trim().equalsIgnoreCase("none")) compression = false;
		else throw new ApplicationException("invalid value for attribute compression [" + strCompression + "], valid values are: [true, false or none]");

	}

	public void setCachedwithin(Object cachedwithin) {
		if (StringUtil.isEmpty(cachedwithin)) return;
		this.cachedWithin = cachedwithin;
	}

	public void setPooling(boolean usePool) {
		this.usePool = usePool;
	}

	@Override
	public int doStartTag() throws PageException {
		if (addtoken) {
			setParam(HttpParamBean.TYPE_COOKIE, "cfid", pageContext.getCFID());
			setParam(HttpParamBean.TYPE_COOKIE, "cftoken", pageContext.getCFToken());
			String jsessionid = pageContext.getJSessionId();
			if (jsessionid != null) setParam(HttpParamBean.TYPE_COOKIE, "jsessionid", jsessionid);
		}

		// cache within
		if (StringUtil.isEmpty(cachedWithin)) {
			Object tmp = ((PageContextImpl) pageContext).getCachedWithin(ConfigWeb.CACHEDWITHIN_HTTP);
			if (tmp != null) setCachedwithin(tmp);
		}

		return EVAL_BODY_INCLUDE;
	}

	private void setParam(int type, String name, String value) {
		HttpParamBean hpb = new HttpParamBean();
		hpb.setType(type);
		hpb.setName(name);
		hpb.setValue(value);
		setParam(hpb);
	}

	@Override
	public int doEndTag() throws PageException {
		try {
			TagListener tagListener = getTagListener();
			if (tagListener == null) _doEndTag();
			else _doEndTag(tagListener);
			return EVAL_PAGE;
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private TagListener getTagListener() {
		if (listener != null) return listener;
		ApplicationContext ac = pageContext.getApplicationContext();
		if (ac instanceof ApplicationContextSupport) return ((ApplicationContextSupport) ac).getHttpListener();
		return null;
	}

	private void _doEndTag(TagListener tagListener) throws PageException, IOException, GeneralSecurityException {
		TemplateLine tl = lucee.runtime.tag.Query.toTemplateLine(pageContext, null, ((PageContextImpl) pageContext).getCurrentPageSource(null));

		// before
		Struct rtn = tagListener.before(pageContext, createListenerArgs(tl));
		Object response = writeBackListenerArgs(rtn);
		boolean safeToMemory = !StringUtil.isEmpty(result, true);
		Struct args = createListenerArgs(tl);

		try {
			// the listener answered the request itself, no request is made
			if (response != null) {
				if (safeToMemory) pageContext.setVariable(result, response);
			}
			else _doEndTag();
		}
		catch (Exception e) {
			PageException pe = Caster.toPageException(e);
			if (pe instanceof AbortException || !tagListener.hasError()) throw pe;

			args.set(KeyConstants._exception, pe.getCatchBlock(pageContext.getConfig()));
			Struct errRtn = tagListener.error(pageContext, args);
			args.removeEL(KeyConstants._exception);
			response = errRtn == null ? null : errRtn.get(KeyConstants._result, null);
			// without a result from the error listener the error is not handled
			if (response == null) throw pe;
			if (safeToMemory) pageContext.setVariable(result, response);
		}

		// after
		if (safeToMemory) args.set(KeyConstants._result, pageContext.getVariable(result));
		Struct afterRtn = tagListener.after(pageContext, args);
		if (afterRtn != null && safeToMemory) {
			Object newResult = afterRtn.get(KeyConstants._result, null);
			if (newResult != null) pageContext.setVariable(result, newResult);
		}
	}

	private Struct createListenerArgs(TemplateLine tl) throws PageException {
		Struct rtn = new StructImpl(Struct.TYPE_LINKED, 4);
		Struct args = new StructImpl(Struct.TYPE_LINKED, 24);

		setArg(args, KeyConstants._url, url);
		setArg(args, KeyConstants._method, getMethodAsVerb(method));
		setArg(args, KeyConstants._charset, charset);
		setArg(args, KeyConstants._timeout, timeout);
		setArg(args, "connectionTimeout", connectionTimeout);
		setArg(args, "socketTimeout", socketTimeout);
		setArg(args, "redirect", redirect);
		setArg(args, "userAgent", useragent);
		setArg(args, KeyConstants._username, username);
		setArg(args, KeyConstants._password, password);
		setArg(args, "throwOnError", Boolean.valueOf(throwonerror));
		setArg(args, KeyConstants._result, result);
		setArg(args, "cachedWithin", cachedWithin);

		Array arrParams = new ArrayImpl(params.size());
		for (HttpParamBean param: params) {
			Struct sct = new StructImpl(Struct.TYPE_LINKED, 6);
			sct.set(KeyConstants._type, paramTypeAsString(param.getType()));
			setArg(sct, KeyConstants._name, param.getName());
			setArg(sct, KeyConstants._value, param.getValue());
			if (param.getFile() != null) sct.set(KeyConstants._file, param.getFile().getAbsolutePath());
			if (!StringUtil.isEmpty(param.getMimeType(), true)) sct.set(KeyConstants._mimetype, param.getMimeType());
			if (param.getEncoded() != ENCODED_AUTO) sct.set(KeyConstants._encoded, Boolean.valueOf(param.getEncoded() == ENCODED_YES));
			arrParams.appendEL(sct);
		}
		args.set(KeyConstants._params, arrParams);

		rtn.setEL(KeyConstants._args, args);
		rtn.setEL(KeyConstants._caller, tl.toStruct());
		return rtn;
	}

	private static void setArg(Struct args, String name, Object value) throws PageException {
		if (value != null) args.set(name, value);
	}

	private static void setArg(Struct args, Key name, Object value) throws PageException {
		if (value != null) args.set(name, value);
	}

	/**
	 * apply the values a before listener returned to the tag
	 * 
	 * @return the response of the request in case the listener provided one, otherwise null
	 */
	private Object writeBackListenerArgs(Struct rtn) throws PageException {
		if (rtn == null) return null;

		// the listener can return the arguments it got (args and caller) or only the arguments
		Struct args = rtn;
		Object obj = rtn.get(KeyConstants._args, null);
		if (obj instanceof Struct) args = (Struct) obj;

		String str = Caster.toString(args.get(KeyConstants._url, null), null);
		if (!StringUtil.isEmpty(str, true)) this.url = str.trim();

		str = Caster.toString(args.get(KeyConstants._method, null), null);
		if (!StringUtil.isEmpty(str, true)) setMethod(str);

		str = Caster.toString(args.get(KeyConstants._charset, null), null);
		if (!StringUtil.isEmpty(str, true)) this.charset = str.trim();

		TimeSpan ts = Caster.toTimespan(args.get(KeyConstants._timeout, null), null);
		if (ts != null) this.timeout = ts;
		ts = Caster.toTimespan(args.get("connectionTimeout", null), null);
		if (ts != null) this.connectionTimeout = ts;
		ts = Caster.toTimespan(args.get("socketTimeout", null), null);
		if (ts != null) this.socketTimeout = ts;

		str = Caster.toString(args.get("redirect", null), null);
		if (!StringUtil.isEmpty(str, true)) setRedirect(str.trim());

		str = Caster.toString(args.get("userAgent", null), null);
		if (!StringUtil.isEmpty(str, true)) this.useragent = str;

		str = Caster.toString(args.get(KeyConstants._username, null), null);
		if (!StringUtil.isEmpty(str, true)) this.username = str;

		str = Caster.toString(args.get(KeyConstants._password, null), null);
		if (!StringUtil.isEmpty(str, true)) this.password = str;

		Boolean b = Caster.toBoolean(args.get("throwOnError", null), null);
		if (b != null) this.throwonerror = b.booleanValue();

		// a string is the name of the result variable
		Object res = args.get(KeyConstants._result, null);
		if (res instanceof CharSequence && !StringUtil.isEmpty(res.toString(), true)) this.result = res.toString().trim();

		obj = args.get(KeyConstants._params, null);
		if (obj != null) {
			Array arr = Caster.toArray(obj);
			ArrayList<HttpParamBean> list = new ArrayList<HttpParamBean>(arr.size());
			Iterator<Object> it = arr.valueIterator();
			while (it.hasNext()) {
				list.add(toParamBean(Caster.toStruct(it.next())));
			}
			this.params.clear();
			this.params.addAll(list);
		}

		// a struct as result is the response of the request, in that case no request is made
		Object top = rtn.get(KeyConstants._result, null);
		if (top instanceof Struct) return top;
		if (res instanceof Struct) return res;
		return null;
	}

	private HttpParamBean toParamBean(Struct sct) throws PageException {
		HttpParamBean param = new HttpParamBean();
		param.setType(paramTypeFromString(Caster.toString(sct.get(KeyConstants._type, null), null)));
		param.setName(Caster.toString(sct.get(KeyConstants._name, null), null));
		param.setValue(sct.get(KeyConstants._value, null));
		String file = Caster.toString(sct.get(KeyConstants._file, null), null);
		if (!StringUtil.isEmpty(file, true)) param.setFile(ResourceUtil.toResourceNotExisting(pageContext, file));
		String mt = Caster.toString(sct.get(KeyConstants._mimetype, null), null);
		if (mt != null) param.setMimeType(mt);
		Boolean enc = Caster.toBoolean(sct.get(KeyConstants._encoded, null), null);
		if (enc != null) param.setEncoded(enc.booleanValue() ? ENCODED_YES : ENCODED_NO);

		if (StringUtil.isEmpty(param.getName()) && HttpParamBean.TYPE_BODY != param.getType() && HttpParamBean.TYPE_XML != param.getType())
			throw new ApplicationException("http listener returned a param of type [" + paramTypeAsString(param.getType()) + "] without a name");
		if (HttpParamBean.TYPE_FILE == param.getType() && param.getFile() == null)
			throw new ApplicationException("http listener returned a param of type [file] without the key [file]");
		return param;
	}

	private static String paramTypeAsString(int type) {
		switch (type) {
		case HttpParamBean.TYPE_URL:
			return "url";
		case HttpParamBean.TYPE_FORM:
			return "formfield";
		case HttpParamBean.TYPE_CGI:
			return "cgi";
		case HttpParamBean.TYPE_HEADER:
			return "header";
		case HttpParamBean.TYPE_COOKIE:
			return "cookie";
		case HttpParamBean.TYPE_FILE:
			return "file";
		case HttpParamBean.TYPE_XML:
			return "xml";
		default:
			return "body";
		}
	}

	private static int paramTypeFromString(String type) throws ApplicationException {
		String t = StringUtil.isEmpty(type, true) ? "" : type.trim().toLowerCase();
		if (t.equals("url")) return HttpParamBean.TYPE_URL;
		if (t.equals("formfield") || t.equals("form")) return HttpParamBean.TYPE_FORM;
		if (t.equals("cgi")) return HttpParamBean.TYPE_CGI;
		if (t.startsWith("head")) return HttpParamBean.TYPE_HEADER;
		if (t.equals("cookie")) return HttpParamBean.TYPE_COOKIE;
		if (t.equals("file")) return HttpParamBean.TYPE_FILE;
		if (t.equals("xml")) return HttpParamBean.TYPE_XML;
		if (t.equals("body")) return HttpParamBean.TYPE_BODY;
		throw new ApplicationException("http listener returned a param with invalid type [" + type
				+ "], valid types are [url, formfield, cgi, header, cookie, file, xml, body]");
	}

	private void _doEndTag() throws PageException, IOException, GeneralSecurityException {

		long start = System.nanoTime();
		boolean safeToMemory = !StringUtil.isEmpty(result, true);

		ConfigWeb cw = pageContext.getConfig();
		HTTPRequest req = new HTTPRequest();

		CacheHandler cacheHandler = null;
		String cacheId = null;

		// HttpRequestBase req = init(pageContext.getConfig(),this,client,params,url,port);
		{
			if (StringUtil.isEmpty(charset, true)) charset = ((PageContextImpl) pageContext).getWebCharset().name();
			else charset = charset.trim();

			// check if has fileUploads
			boolean doUploadFile = false;
			for (int i = 0; i < this.params.size(); i++) {
				if ((this.params.get(i)).getType() == HttpParamBean.TYPE_FILE) {
					doUploadFile = true;
					break;
				}
			}

			// parse url (also query string)
			int len = this.params.size();
			StringBuilder sbQS = new StringBuilder();
			for (int i = 0; i < len; i++) {
				HttpParamBean param = this.params.get(i);
				int type = param.getType();
				// URL
				if (type == HttpParamBean.TYPE_URL) {
					short enc = param.getEncoded();
					if (sbQS.length() > 0) sbQS.append('&');
					sbQS.append(enc != ENCODED_NO ? urlenc(param.getName(), charset, enc == ENCODED_AUTO) : param.getName());
					sbQS.append('=');
					sbQS.append(param.getEncoded() != ENCODED_NO ? urlenc(param.getValueAsString(), charset, enc == ENCODED_AUTO) : param.getValueAsString());
				}
			}

			String host = null;

			try {
				URL _url = HTTPUtil.toURL(url, port, encoded);
				host = _url.getHost();
				url = _url.toExternalForm();
				if (sbQS.length() > 0) {
					// no existing QS
					if (StringUtil.isEmpty(_url.getQuery())) {
						url += "?" + sbQS;
					}
					else {
						url += "&" + sbQS;
					}
				}
				req.setURL(new URL(url));

				// check certificate
				if (autoCert && "https".equalsIgnoreCase(_url.getProtocol())) {
					int port = _url.getPort();

					Resource cacerts = pageContext.getConfig().getSecurityDirectory();

					CertificateInstaller installer = new CertificateInstaller(cacerts, _url.getHost(), port < 1 ? 443 : port);
					installer.installAll(false);
				}

			}
			catch (MalformedURLException mue) {
				throw Caster.toPageException(mue);
			}

			// cache
			if (cachedWithin != null) {

				cacheId = createCacheId();

				cacheHandler = pageContext.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_HTTP, null).getInstanceMatchingObject(cachedWithin, null);

				if (cacheHandler instanceof CacheHandlerPro) {

					CacheItem cacheItem = ((CacheHandlerPro) cacheHandler).get(pageContext, cacheId, cachedWithin);

					if (cacheItem instanceof HTTPCacheItem) {
						logHttpRequest(pageContext, ((HTTPCacheItem) cacheItem).getData(), url, getMethodAsVerb(method), System.nanoTime() - start, true, null);
						pageContext.setVariable(result, ((HTTPCacheItem) cacheItem).getData());
						return;
					}
				}
				else if (cacheHandler != null) { // TODO this else block can be removed when all cache handlers implement CacheHandlerPro

					CacheItem cacheItem = cacheHandler.get(pageContext, cacheId);

					if (cacheItem instanceof HTTPCacheItem) {
						logHttpRequest(pageContext, ((HTTPCacheItem) cacheItem).getData(), url, getMethodAsVerb(method), System.nanoTime() - start, true, null);
						pageContext.setVariable(result, ((HTTPCacheItem) cacheItem).getData());
						return;
					}
				}
			}

			// cache not found, process and cache result if needed

			// select best matching method (get,post, post multpart (file))

			boolean isBinary = false;
			boolean doMultiPart = doUploadFile || this.multiPart;

			String verb = getMethodAsVerb(method);
			req.setMethod(verb);
			// the methods that can have a body, and the ones that can have form fields
			boolean bodyAllowed = this.method != METHOD_HEAD && this.method != METHOD_TRACE && this.method != METHOD_OPTIONS;
			boolean formAllowed = this.method != METHOD_GET && this.method != METHOD_HEAD && this.method != METHOD_DELETE && this.method != METHOD_OPTIONS && this.method != METHOD_TRACE
					&& this.method != METHOD_PATCH;
			isBinary = this.method != METHOD_GET && this.method != METHOD_HEAD;

			boolean hasForm = false;
			boolean hasBody = false;
			boolean hasContentType = false;
			// Set http params
			ArrayList<HTTPRequest.Part> parts = new ArrayList<HTTPRequest.Part>();

			StringBuilder acceptEncoding = new StringBuilder();
			java.util.List<String[]> postParam = formAllowed ? new ArrayList<String[]>() : null;

			for (int i = 0; i < len; i++) {
				HttpParamBean param = this.params.get(i);
				int type = param.getType();

				// URL
				if (type == HttpParamBean.TYPE_URL) {
					// listQS.add(new BasicNameValuePair(translateEncoding(param.getName(),
					// http.charset),translateEncoding(param.getValueAsString(),
					// http.charset)));
				}
				// Form
				else if (type == HttpParamBean.TYPE_FORM) {
					hasForm = true;
					if (this.method == METHOD_GET)
						throw new ApplicationException("httpparam with type formfield can only be used when the method attribute of the parent http tag is set to post");
					if (formAllowed) {
						if (doMultiPart) {
							parts.add(HTTPRequest.Part.text(param.getName(), param.getValueAsString(), charset));
						}
						else {
							postParam.add(new String[] { param.getName(), param.getValueAsString() });
						}
					}
					// else if(multi!=null)multi.addParameter(param.getName(),param.getValueAsString());
				}
				// CGI
				else if (type == HttpParamBean.TYPE_CGI) {
					if (param.getEncoded() != ENCODED_NO) {
						boolean isAuto = param.getEncoded() == ENCODED_AUTO;
						req.addHeader(urlenc(param.getName(), charset, isAuto), urlenc(param.getValueAsString(), charset, isAuto));
					}
					else req.addHeader(param.getName(), param.getValueAsString());
				}
				// Header
				else if (type == HttpParamBean.TYPE_HEADER) {
					if (param.getName().equalsIgnoreCase("content-type")) hasContentType = true;

					if (param.getName().equalsIgnoreCase("Content-Length")) {}
					else if (param.getName().equalsIgnoreCase("Accept-Encoding")) {
						acceptEncoding.append(headerValue(param.getValueAsString()));
						acceptEncoding.append(", ");
					}
					else req.addHeader(param.getName(), headerValue(param.getValueAsString()));
				}
				// Cookie
				else if (type == HttpParamBean.TYPE_COOKIE) {
					req.addCookie(param.getName(), param.getValueAsString(), host, "/");
				}
				// File
				else if (type == HttpParamBean.TYPE_FILE) {
					hasForm = true;
					if (this.method == METHOD_GET) throw new ApplicationException("httpparam type file can't only be used, when method of the tag http equal post");
					// if(param.getFile()==null) throw new ApplicationException("httpparam type file can't only be used,
					// when method of the tag http equal
					// post");
					String strCT = getContentType(param);
					ContentType ct = HTTPUtil.toContentType(strCT, null);

					String mt = "text/xml";
					if (ct != null && !StringUtil.isEmpty(ct.getMimeType(), true)) mt = ct.getMimeType();

					String cs = charset;
					if (ct != null && !StringUtil.isEmpty(ct.getCharset(), true)) cs = ct.getCharset();

					if (doMultiPart) {
						Resource res = param.getFile();
						if (!res.isFile()) throw new ApplicationException("can't upload file, path is invalid", "File is not a normal file.");
						if (!res.isReadable()) throw new ApplicationException("can't upload file, path is invalid", "File is not readable.");
						parts.add(HTTPRequest.Part.file(param.getName(), res, mt, cs));
					}
				}
				// XML
				else if (type == HttpParamBean.TYPE_XML) {
					ContentType ct = HTTPUtil.toContentType(param.getMimeType(), null);

					String mt = "text/xml";
					if (ct != null && !StringUtil.isEmpty(ct.getMimeType(), true)) mt = ct.getMimeType();

					String cs = charset;
					if (ct != null && !StringUtil.isEmpty(ct.getCharset(), true)) cs = ct.getCharset();

					hasBody = true;
					hasContentType = true;
					req.addHeader("Content-type", mt + "; charset=" + cs);
					if (!bodyAllowed) throw new ApplicationException("type xml is only supported for methods get, delete, post, and put");
					req.setBody(param.getValueAsString(), mt, cs);
				}
				// Body
				else if (type == HttpParamBean.TYPE_BODY) {
					ContentType ct = HTTPUtil.toContentType(param.getMimeType(), null);

					String mt = null;
					if (ct != null && !StringUtil.isEmpty(ct.getMimeType(), true)) mt = ct.getMimeType();

					String cs = charset;
					if (ct != null && !StringUtil.isEmpty(ct.getCharset(), true)) cs = ct.getCharset();

					hasBody = true;
					if (!bodyAllowed) throw new ApplicationException("type body is only supported for methods get, delete, post, and put");
					req.setBody(param.getValue(), mt, cs);

				}
				else {
					throw new ApplicationException("invalid type [" + type + "]");
				}

			}

			// post params
			if (postParam != null && postParam.size() > 0) {
				for (String[] field: postParam) {
					req.addFormField(field[0], field[1]);
				}
			}

			if (compression) {
				acceptEncoding.append("gzip");
			}
			else {
				acceptEncoding.append("deflate;q=0");
				req.setHeader("TE", "deflate;q=0");
			}
			req.setHeader("Accept-Encoding", acceptEncoding.toString());

			// multipart
			if (doMultiPart && bodyAllowed) {
				hasContentType = true;
				boolean doIt = true;
				if (!this.multiPart && parts.size() == 1 && parts.get(0).getResource() == null) {
					// a single text part is sent as plain text
					req.setBody(parts.get(0).getValue(), "text/plain", parts.get(0).getCharset());
					doIt = false;
				}

				if (doIt) {
					// the fields of a multipart request are parts
					for (HTTPRequest.Part part: parts) {
						req.addPart(part);
					}
				}
			}

			if (hasBody && hasForm) throw new ApplicationException("mixing httpparam  type file/formfield and body/XML is not allowed");

			if (!hasContentType) {
				if (isBinary) {
					if (hasBody) req.addHeader("Content-type", "application/octet-stream");
					else req.addHeader("Content-type", "application/x-www-form-urlencoded; charset=" + charset);
				}
				else {
					if (hasBody) req.addHeader("Content-type", "text/html; charset=" + charset);
				}
			}

			// set User Agent
			if (!req.hasHeader("User-Agent")) req.setHeader("User-Agent", this.useragent);

			// set timeout
			setTimeout(req, Http.checkRemainingTimeout(pageContext, this.timeout), connectionTimeout, socketTimeout);
			// set Username and Password
			if (this.username != null) {

				if (this.password == null) this.password = "";

				if (AUTH_TYPE_NTLM == this.authType) {
					if (StringUtil.isEmpty(this.workStation, true)) throw new ApplicationException("attribute workstation is required when authentication type is [NTLM]");
					if (StringUtil.isEmpty(this.domain, true)) throw new ApplicationException("attribute domain is required when authentication type is [NTLM]");

					req.setNTCredentials(this.username, this.password, this.workStation, this.domain);
				}
				else {
					req.setCredentials(this.username, this.password, preauth);
				}
			}

			// set Proxy
			ProxyData proxy = null;
			if (!StringUtil.isEmpty(this.proxyserver)) {
				proxy = ProxyDataImpl.validate(ProxyDataImpl.getInstance(this.proxyserver, this.proxyport, this.proxyuser, this.proxypassword), host);
			}

			if (proxy == null) {
				proxy = ProxyDataImpl.validate(((PageContextImpl) pageContext).getProxyData(), host);

			}
			req.setProxy(proxy);

			req.setRedirect(this.redirect).setPooling(this.usePool).setClientCert(this.clientCert, this.clientCertPassword).setTrustStore(this.trustStore, this.trustStorePassword)
					.setSslVerify(this.sslVerify).setCharset(charset);

			// an invalid trust store is an error in the attributes of the tag, it must throw and not end up as a "Connection Failure"
			// inside the execution below, that is also the case with throwOnError=false (LDEV-6005)
			if (!StringUtil.isEmpty(this.trustStore, true)) {
				try {
					SSLUtil.validateTrustStore(Paths.get(this.trustStore.trim()), this.trustStorePassword == null ? null : this.trustStorePassword.toCharArray());
				}
				catch (Exception ex) {
					throw new ApplicationException("invalid value for attribute [trustStore] [" + this.trustStore + "]: " + ex.getMessage());
				}
			}

		}
		HTTPResponse rsp = null;
		try {
			HTTPStruct cfhttp = new HTTPStruct(cacheId);
			cfhttp.setEL(KeyConstants._errordetail, "");
			Struct requestInfo = toRequestStruct(req);
			cfhttp.setEL(KeyConstants._request, requestInfo);
			if (safeToMemory) pageContext.setVariable(result, cfhttp);

			/////////////////////////////////////////// EXECUTE
			/////////////////////////////////////////// /////////////////////////////////////////////////
			HttpExecutor e = new HttpExecutor(pageContext, this, req);

			if (socketTimeout == null || socketTimeout.getMillis() <= 0) {
				try {
					rsp = e.execute();
				}

				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					// the request asks for something the engine cannot do, that is not a connection failure (LDEV-6676)
					if (t instanceof HTTPFeatureNotSupportedException) throw new ApplicationException(t.getMessage());
					if (!throwonerror) {
						if (t instanceof SocketTimeoutException) setRequestTimeout(cfhttp);
						else setUnknownHost(cfhttp, t);
						logHttpRequest(pageContext, cfhttp, url, req.getMethod(), System.nanoTime() - start, false, t);
						return;
					}
					throw toPageException(t, rsp);

				}
			}
			else {
				e.start();
				try {
					// the tag instance is pooled, the executor of an earlier (aborted) request of this instance can still notify it, so a
					// wake up alone does not mean that this request is done, it waits until the executor is finished or the time is up
					long ms = socketTimeout.getMillis();
					long deadline = System.nanoTime() + (ms >= Long.MAX_VALUE / 1000000L ? Long.MAX_VALUE / 2 : ms * 1000000L);
					synchronized (this) {
						while (!e.finished) {
							long left = (deadline - System.nanoTime()) / 1000000L;
							if (left <= 0) break;
							this.wait(left);
						}
					}
				}
				catch (InterruptedException ie) {
					throw Caster.toPageException(ie);
				}
				if (e.t != null) {
					// the request asks for something the engine cannot do, that is not a connection failure (LDEV-6676)
					if (e.t instanceof HTTPFeatureNotSupportedException) throw new ApplicationException(e.t.getMessage());
					if (!throwonerror) {
						if (e.t instanceof SocketTimeoutException) setRequestTimeout(cfhttp);
						else setUnknownHost(cfhttp, e.t);
						logHttpRequest(pageContext, cfhttp, url, req.getMethod(), System.nanoTime() - start, false, e.t);
						return;
					}
					throw toPageException(e.t, rsp);
				}

				rsp = e.response;

				if (!e.done) {
					req.abort();
					if (throwonerror) throw new HTTPException("408 Request Time-out", "a timeout occurred in tag http", 408, "Time-out", rsp == null ? null : rsp.getURL());
					setRequestTimeout(cfhttp);
					logHttpRequest(pageContext, cfhttp, url, req.getMethod(), System.nanoTime() - start, false, null);
					return;
					// throw new ApplicationException("timeout");
				}
			}

			/////////////////////////////////////////// EXECUTE
			/////////////////////////////////////////// /////////////////////////////////////////////////
			int statCode = 0;
			// Write Response Scope
			// String rawHeader=httpMethod.getStatusLine().toString();
			String mimetype = null;
			String contentEncoding = null;
			String rspCharset = null;

			// status code
			cfhttp.set(KeyConstants._statuscode, ((rsp.getStatusCode() + " " + rsp.getStatusText()).trim()));
			cfhttp.set(KeyConstants._status_code, Double.valueOf(statCode = rsp.getStatusCode()));
			cfhttp.set(KeyConstants._status_text, (rsp.getStatusText()));
			cfhttp.set(KeyConstants._http_version, (rsp.getProtocolVersion()));
			Array locations = rsp.getLocations();
			if (locations != null) cfhttp.set(KeyConstants._locations, locations);
			URL targetURL = rsp.getTargetURL();
			if (targetURL != null) requestInfo.setEL(KeyConstants._finalUrl, maskUrl(targetURL.toExternalForm()));
			Struct connection = rsp.getConnectionInfo();
			if (connection != null) cfhttp.set(KeyConstants._connection, connection);

			// responseHeader
			lucee.commons.net.http.Header[] headers = rsp.getAllHeaders();
			StringBuilder raw = new StringBuilder(rsp.getStatusLine() + " ");
			Struct responseHeader = new StructImpl();
			Struct cookie;
			Array setCookie = new ArrayImpl();
			Query cookies = new QueryImpl(new String[] { "name", "value", "path", "domain", "expires", "secure", "httpOnly", "samesite", "partitioned" }, 0, "cookies");

			for (int i = 0; i < headers.length; i++) {
				lucee.commons.net.http.Header header = headers[i];
				// print.ln(header);

				raw.append(header.toString() + " ");
				if (header.getName().equalsIgnoreCase("Set-Cookie")) {
					setCookie.append(header.getValue());
					parseCookie(cookies, header.getValue(), charset);
				}
				else {
					// print.ln(header.getName()+"-"+header.getValue());
					Object value = responseHeader.get(KeyImpl.init(header.getName()), null);
					if (value == null) responseHeader.set(KeyImpl.init(header.getName()), header.getValue());
					else {
						Array arr = null;
						if (value instanceof Array) {
							arr = (Array) value;
						}
						else {
							arr = new ArrayImpl();
							responseHeader.set(KeyImpl.init(header.getName()), arr);
							arr.appendEL(value);
						}
						arr.appendEL(header.getValue());
					}
				}

				// Content-Type
				if (header.getName().equalsIgnoreCase("Content-Type")) {
					mimetype = header.getValue();
					if (mimetype == null) mimetype = NO_MIMETYPE;
				}

				// Content-Encoding
				if (header.getName().equalsIgnoreCase("Content-Encoding")) {
					contentEncoding = header.getValue();
				}

			}

			String[] tmpCharset = HTTPUtil.splitMimeTypeAndCharset(mimetype, null);
			rspCharset = tmpCharset != null ? tmpCharset[1] : null;

			cfhttp.set(KeyConstants._error, Boolean.FALSE); // default
			cfhttp.set(KeyConstants._responseheader, responseHeader);
			cfhttp.set(KeyConstants._cookies, cookies);
			responseHeader.set(KeyConstants._status_code, Double.valueOf(statCode = rsp.getStatusCode()));
			responseHeader.set(KeyConstants._explanation, (rsp.getStatusText()));
			if (setCookie.size() > 0) responseHeader.set(SET_COOKIE, setCookie);

			// is text
			if (mimetype == null || mimetype == NO_MIMETYPE) {

			}

			Boolean _isText = null;
			if (mimetype != null && mimetype != NO_MIMETYPE) {
				_isText = HTTPUtil.isTextMimeType(mimetype);
			}

			// is multipart
			boolean isMultipart = MultiPartResponseUtils.isMultipart(mimetype);

			// we still don't know the mime type
			byte[] barr = null;
			if (Boolean.TRUE != _isText && safeToMemory && !isMultipart) {
				barr = contentAsBinary(rsp, contentEncoding);
				if (_isText == null) {
					String mt = IOUtil.getMimeType(barr, null);
					if (mt != null) {
						_isText = HTTPUtil.isTextMimeType(mt);
						mimetype = mt;
					}
				}
			}

			if (safeToMemory && _isText != null) cfhttp.set(KeyConstants._text, _isText);

			// mimetype charset
			// boolean responseProvideCharset=false;
			if (!StringUtil.isEmpty(mimetype, true)) {
				if (Boolean.TRUE == _isText) {
					String[] types = HTTPUtil.splitMimeTypeAndCharset(mimetype, null);
					if (types[0] != null) cfhttp.set(KeyConstants._mimetype, types[0]);
					if (types[1] != null) rspCharset = types[1]; // only text types have charset
				}
				else cfhttp.set(KeyConstants._mimetype, mimetype);
			}
			else cfhttp.set(KeyConstants._mimetype, NO_MIMETYPE);

			// charset
			cfhttp.set(KeyConstants._charset, rspCharset != null ? rspCharset : "");

			// File
			Resource file = null;

			if (strFile != null && strPath != null) {
				file = ResourceUtil.toResourceNotExisting(pageContext, strPath).getRealResource(strFile);
			}
			else if (strFile != null) {
				file = ResourceUtil.toResourceNotExisting(pageContext, strFile);
			}
			else if (strPath != null) {
				file = ResourceUtil.toResourceNotExisting(pageContext, strPath);
				// Resource dir = file.getParentResource();
				if (file.isDirectory()) {
					file = file.getRealResource(GetFileFromPath.call(pageContext, req.getURL().getPath()));
				}

			}
			if (file != null) {
				SecurityManagerImpl.checkFileLocation(pageContext, file);
			}

			// filecontent

			if (Boolean.TRUE == _isText && getAsBinary != GET_AS_BINARY_YES && safeToMemory) {
				String tmp = rsp.getCharset();
				Charset responseCharset = StringUtil.isEmpty(tmp, true) ? null : CharsetUtil.toCharset(tmp);
				// store to memory
				String str;

				Charset cs = (StringUtil.isEmpty(charset)) ? responseCharset : CharsetUtil.toCharset(charset, responseCharset);
				if (barr == null) str = contentAsString(rsp, cs, contentEncoding, e);
				else str = IOUtil.toString(barr, cs);
				cfhttp.set(KeyConstants._filecontent, str);

				// store to file
				if (file != null) {
					IOUtil.write(file, str, ((PageContextImpl) pageContext).getWebCharset(), false);

				}

				// store to variable
				if (name != null) {
					Query qry = CSVParser.toQuery(str, delimiter, textqualifier, columns, firstrowasheaders);
					pageContext.setVariable(name, qry);
				}
			}
			// Binary
			else {
				if (barr == null && safeToMemory) barr = contentAsBinary(rsp, contentEncoding);

				// IF Multipart response get file content and parse parts
				if (barr != null) {
					if (isMultipart) {
						cfhttp.set(KeyConstants._filecontent, MultiPartResponseUtils.getParts(barr, mimetype));
					}
					else {
						cfhttp.set(KeyConstants._filecontent, barr);
					}
				}

				// store to file
				if (file != null) {
					try {
						if (barr != null) IOUtil.copy(new ByteArrayInputStream(barr), file, true);
						else storeTo(rsp, contentEncoding, file);
					}
					catch (IOException ioe) {
						throw Caster.toPageException(ioe);
					}
				}
			}

			// header
			cfhttp.set(KeyConstants._header, raw.toString());
			if (!isStatusOK(rsp.getStatusCode())) {
				String msg = rsp.getStatusCode() + " " + rsp.getStatusText();
				cfhttp.setEL(KeyConstants._errordetail, msg);
				if (throwonerror) {
					URL url = rsp.getURL();
					String details = getMethodAsVerb(method) + " " + url.toExternalForm();
					throw new HTTPException(msg, details, rsp.getStatusCode(), rsp.getStatusText(), url);
				}
				cfhttp.setEL(KeyConstants._error, Boolean.TRUE);
			}

			// TODO: check if we can use statCode instead of rsp.getStatusCode() everywhere and cleanup the code
			if (cacheHandler != null && rsp.getStatusCode() == 200) {
				// add to cache
				cacheHandler.set(pageContext, cacheId, cachedWithin, new HTTPCacheItem(cfhttp, url, System.nanoTime() - start, cacheId));
			}

			logHttpRequest(pageContext, cfhttp, url, req.getMethod(), System.nanoTime() - start, false, null);
		}
		finally {
			if (rsp != null) rsp.close();
		}
	}

	private byte[] contentAsBinary(HTTPResponse rsp, String contentEncoding) throws PageException, IOException {
		byte[] barr = null;
		InputStream is = null;
		if (isGzipEncoded(contentEncoding)) {
			if (method != METHOD_HEAD) {
				is = rsp.getContentAsStream();
				is = rsp.getStatusCode() != 200 ? new CachingGZIPInputStream(is) : new GZIPInputStream(is);
			}

			try {
				try {
					barr = is == null ? new byte[0] : IOUtil.toBytes(is);
				}
				catch (EOFException eof) {
					if (is instanceof CachingGZIPInputStream) barr = IOUtil.toBytes(((CachingGZIPInputStream) is).getRawData());
					else throw eof;
				}
			}
			catch (IOException t) {
				throw Caster.toPageException(t);
			}
			finally {
				IOUtil.close(is);
			}
		}
		else {
			try {
				if (method != METHOD_HEAD) barr = rsp.getContentAsByteArray();
				else barr = new byte[0];
			}
			catch (IOException t) {
				throw Caster.toPageException(t);
			}
		}
		return barr;
	}

	private void storeTo(HTTPResponse rsp, String contentEncoding, Resource target) throws IOException {
		InputStream is = null;
		if (target != null && isGzipEncoded(contentEncoding)) {
			if (method != METHOD_HEAD) {
				is = rsp.getContentAsStream();
				is = rsp.getStatusCode() != 200 ? new CachingGZIPInputStream(is) : new GZIPInputStream(is);
			}
			try {
				if (is != null) IOUtil.copy(is, target, true);
			}
			catch (EOFException eof) {
				if (is instanceof CachingGZIPInputStream) {
					IOUtil.copy(((CachingGZIPInputStream) is).getRawData(), target, true);
				}
				else throw eof;
			}
		}
		else {
			if (method != METHOD_HEAD) {
				IOUtil.copy(rsp.getContentAsStream(), target, true);
			}
		}
	}

	private String contentAsString(HTTPResponse rsp, Charset responseCharset, String contentEncoding, HttpExecutor e) throws PageException {
		String str;
		InputStream is = null;
		try {

			// read content
			if (method != METHOD_HEAD) {
				is = rsp.getContentAsStream();
				if (is != null && isGzipEncoded(contentEncoding)) is = rsp.getStatusCode() != 200 ? new CachingGZIPInputStream(is) : new GZIPInputStream(is);
			}
			try {
				try {
					str = is == null ? "" : IOUtil.toString(is, responseCharset, Http.checkRemainingTimeout(pageContext, this.socketTimeout).getMillis());
				}
				catch (EOFException eof) {
					if (is instanceof CachingGZIPInputStream) {
						str = IOUtil.toString(is = ((CachingGZIPInputStream) is).getRawData(), responseCharset,
								Http.checkRemainingTimeout(pageContext, this.socketTimeout).getMillis());
					}
					else throw eof;
				}
			}
			catch (UnsupportedEncodingException uee) {
				str = IOUtil.toString(is, (Charset) null, Http.checkRemainingTimeout(pageContext, this.socketTimeout).getMillis());
			}
		}
		catch (IOException ioe) {
			throw Caster.toPageException(ioe);
		}
		finally {
			IOUtil.closeEL(is);
		}

		if (str == null) str = "";
		if (resolveurl) {
			// if(e.redirectURL!=null)url=e.redirectURL.toExternalForm();
			str = new URLResolver().transform(str, e.response.getTargetURL(), false);
		}
		return str;
	}

	public static TimeSpan checkRemainingTimeout(PageContext pc, TimeSpan timeout) throws RequestTimeoutException {
		TimeSpan remaining = PageContextUtil.remainingTime(pc, true);
		if (timeout == null || ((int) timeout.getMillis()) <= 0 || (timeout.getMillis() > remaining.getMillis() && remaining.getMillis() > 0)) { // not set
			return remaining;
		}
		return timeout;
	}

	private String createCacheId() {
		return CacheHandlerCollectionImpl.createId(url, addtoken ? pageContext.getURLToken() : "", method, params, username, password, this.port, proxyserver, proxyport, proxyuser,
				proxypassword, useragent);
	}

	public static void parseCookie(Struct cookies, String raw, String charset) {
		String[] arr = ListUtil.trimItems(ListUtil.trim(ListUtil.listToStringArray(raw, ';')));
		if (arr.length == 0) return;
		String item;

		int index;
		String n, v;
		// name/value
		if (arr.length > 0) {
			item = arr[0];
			index = item.indexOf('=');
			if (index == -1) {
				cookies.setEL(dec(item, charset), "");
			}
			else { // name and value
				cookies.setEL(dec(item.substring(0, index), charset), dec(item.substring(index + 1), charset));
			}

		}
	}

	public static void parseCookie(Query cookies, String raw, String charset) {
		String[] arr = ListUtil.trimItems(ListUtil.trim(ListUtil.listToStringArray(raw, ';')));
		if (arr.length == 0) return;
		int row = cookies.addRow();
		String item;

		int index;
		// name/value
		if (arr.length > 0) {
			item = arr[0];
			index = item.indexOf('=');
			if (index == -1) // only name
				cookies.setAtEL(KeyConstants._name, row, dec(item, charset));
			else { // name and value
				cookies.setAtEL(KeyConstants._name, row, dec(item.substring(0, index), charset));
				cookies.setAtEL(KeyConstants._value, row, dec(item.substring(index + 1), charset));
			}

		}
		String n, v;
		cookies.setAtEL("secure", row, Boolean.FALSE);
		cookies.setAtEL("httpOnly", row, Boolean.FALSE);
		cookies.setAtEL("partitioned", row, Boolean.FALSE);
		for (int i = 1; i < arr.length; i++) {
			item = arr[i];
			index = item.indexOf('=');
			if (index == -1) // only name
				cookies.setAtEL(dec(item, charset), row, Boolean.TRUE);
			else { // name and value
				n = dec(item.substring(0, index), charset);
				v = dec(item.substring(index + 1), charset);
				if (n.equalsIgnoreCase("expires")) {
					DateTime d = Caster.toDate(v, false, null, null);

					if (d != null) {
						cookies.setAtEL(n, row, d);
						continue;
					}
				}
				cookies.setAtEL(n, row, v);
			}
		}
	}

	public String dec(String str) {
		return dec(str, charset);
	}

	public static String dec(String str, String charset) {
		return ReqRspUtil.decode(str, charset, false);
	}

	public static boolean isStatusOK(int statusCode) {
		return statusCode >= 200 && statusCode <= 299;
	}

	private PageException toPageException(Throwable t, HTTPResponse rsp) {
		if (t instanceof SocketTimeoutException) {
			HTTPException he = new HTTPException("408 Request Time-out", "a timeout occurred in tag http", 408, "Time-out", rsp == null ? null : rsp.getURL());
			List<StackTraceElement> merged = ArrayUtil.merge(t.getStackTrace(), he.getStackTrace());
			StackTraceElement[] traces = new StackTraceElement[merged.size()];
			Iterator<StackTraceElement> it = merged.iterator();
			int index = 0;
			while (it.hasNext()) {
				traces[index++] = it.next();
			}
			he.setStackTrace(traces);
			return he;
		}
		PageException pe = Caster.toPageException(new Exception(t));
		if (pe instanceof NativeException) {
			((NativeException) pe).setAdditional(KeyConstants._url, url);
		}
		return pe;
	}

	/**
	 * the request as handed to the HTTP engine, values of sensitive headers and params are masked
	 */
	private static Struct toRequestStruct(HTTPRequest req) {
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._method, req.getMethod());
		String url = maskUrl(req.getURL().toExternalForm());
		sct.setEL(KeyConstants._url, url);
		// replaced with the target of the response, when the engine reports one
		sct.setEL(KeyConstants._finalUrl, url);

		// headers, the engine adds Authorization (preauth), Cookie and the multipart boundary on its own
		Struct headers = new StructImpl(Struct.TYPE_LINKED);
		boolean multipart = req.getParts() != null;
		boolean hasContentType = false;
		boolean hasAuthorization = false;
		boolean hasCookie = false;
		for (lucee.commons.net.http.Header h: req.getHeaders()) {
			String name = h.getName();
			if (name.equalsIgnoreCase("content-type")) {
				hasContentType = true;
				if (multipart) {
					putValue(headers, name, "multipart/form-data");
					continue;
				}
			}
			else if (name.equalsIgnoreCase("authorization")) hasAuthorization = true;
			else if (name.equalsIgnoreCase("cookie")) hasCookie = true;
			putValue(headers, name, SENSITIVE_NAME.matcher(name).find() ? MASK : h.getValue());
		}
		if (multipart && !hasContentType) putValue(headers, "Content-Type", "multipart/form-data");
		if (!hasAuthorization && !StringUtil.isEmpty(req.getUsername(), true) && req.isPreAuth()) putValue(headers, "Authorization", MASK);
		if (!hasCookie && !req.getCookies().isEmpty()) putValue(headers, "Cookie", MASK);
		sct.setEL(KeyConstants._headers, headers);

		// params
		Struct params = new StructImpl(Struct.TYPE_LINKED);
		Struct urlParams = new StructImpl(Struct.TYPE_LINKED);
		String query = req.getURL().getQuery();
		if (!StringUtil.isEmpty(query)) {
			for (String pair: query.split("&")) {
				if (pair.isEmpty()) continue;
				int index = pair.indexOf('=');
				String name = decode(index == -1 ? pair : pair.substring(0, index), req.getCharset());
				String value = index == -1 ? "" : decode(pair.substring(index + 1), req.getCharset());
				putValue(urlParams, name, SENSITIVE_NAME.matcher(name).find() ? MASK : value);
			}
		}
		params.setEL(KeyConstants._url, urlParams);

		Struct form = new StructImpl(Struct.TYPE_LINKED);
		Struct file = new StructImpl(Struct.TYPE_LINKED);
		if (req.getFormFields() != null) {
			for (String[] field: req.getFormFields()) {
				putValue(form, field[0], SENSITIVE_NAME.matcher(field[0]).find() ? MASK : field[1]);
			}
		}
		if (multipart) {
			for (HTTPRequest.Part part: req.getParts()) {
				if (part.getResource() != null) putValue(file, part.getName(), part.getResource().getName());
				else putValue(form, part.getName(), SENSITIVE_NAME.matcher(part.getName()).find() ? MASK : part.getValue());
			}
		}
		params.setEL(KeyConstants._form, form);
		params.setEL(KeyConstants._file, file);

		// the content of a body is not part of the summary
		Struct body = new StructImpl(Struct.TYPE_LINKED);
		Object value = req.getBody();
		if (value != null) {
			if (value instanceof String) {
				body.setEL(KeyConstants._type, "text");
				body.setEL(KeyConstants._length, Double.valueOf(((String) value).length()));
			}
			else if (value instanceof byte[]) {
				body.setEL(KeyConstants._type, "binary");
				body.setEL(KeyConstants._length, Double.valueOf(((byte[]) value).length));
			}
			else if (value instanceof Resource) {
				body.setEL(KeyConstants._type, "file");
				body.setEL(KeyConstants._length, Double.valueOf(((Resource) value).length()));
			}
			else body.setEL(KeyConstants._type, "stream");
			if (!StringUtil.isEmpty(req.getBodyMimeType(), true)) body.setEL(KeyConstants._mimetype, req.getBodyMimeType());
			if (!StringUtil.isEmpty(req.getBodyCharset(), true)) body.setEL(KeyConstants._charset, req.getBodyCharset());
		}
		params.setEL(KeyConstants._body, body);
		sct.setEL(KeyConstants._params, params);
		return sct;
	}

	// a name that is used more than once becomes an array of the values
	private static void putValue(Struct sct, String name, String value) {
		if (value == null) value = "";
		Key key = KeyImpl.init(name);
		Object existing = sct.get(key, null);
		if (existing == null) sct.setEL(key, value);
		else if (existing instanceof Array) ((Array) existing).appendEL(value);
		else {
			Array arr = new ArrayImpl();
			arr.appendEL(existing);
			arr.appendEL(value);
			sct.setEL(key, arr);
		}
	}

	private static String decode(String str, String charset) {
		try {
			return java.net.URLDecoder.decode(str, StringUtil.isEmpty(charset, true) ? "UTF-8" : charset.trim());
		}
		catch (Exception e) {
			return str;
		}
	}

	/**
	 * masks the user info of the url and the values of sensitive query parameters
	 */
	private static String maskUrl(String url) {
		int start = url.indexOf("://");
		if (start != -1) {
			start += 3;
			int end = url.length();
			for (int i = start; i < url.length(); i++) {
				char c = url.charAt(i);
				if (c == '/' || c == '?' || c == '#') {
					end = i;
					break;
				}
			}
			int at = url.lastIndexOf('@', end - 1);
			if (at >= start) url = url.substring(0, start) + MASK + url.substring(at);
		}
		int qs = url.indexOf('?');
		if (qs == -1) return url;
		int hash = url.indexOf('#', qs);
		String query = url.substring(qs + 1, hash == -1 ? url.length() : hash);
		StringBuilder sb = new StringBuilder(url.substring(0, qs + 1));
		boolean first = true;
		for (String pair: query.split("&", -1)) {
			if (!first) sb.append('&');
			first = false;
			int index = pair.indexOf('=');
			if (index != -1 && SENSITIVE_NAME.matcher(decode(pair.substring(0, index), null)).find()) sb.append(pair, 0, index + 1).append(MASK);
			else sb.append(pair);
		}
		if (hash != -1) sb.append(url.substring(hash));
		return sb.toString();
	}

	private void setUnknownHost(Struct cfhttp, Throwable t) {
		cfhttp.setEL(KeyConstants._charset, "");
		cfhttp.setEL(KeyConstants._errordetail, "Unknown host: " + t.getMessage());
		cfhttp.setEL(KeyConstants._filecontent, "Connection Failure");
		cfhttp.setEL(KeyConstants._header, "");
		cfhttp.setEL(KeyConstants._mimetype, "Unable to determine MIME type of file.");
		cfhttp.setEL(KeyConstants._responseheader, new StructImpl());
		cfhttp.setEL(KeyConstants._statuscode, "Connection Failure. Status code unavailable.");
		cfhttp.setEL(KeyConstants._status_code, Double.valueOf(0));
		cfhttp.setEL(KeyConstants._status_text, "Connection Failure");
		cfhttp.setEL(KeyConstants._text, Boolean.TRUE);
		cfhttp.setEL(KeyConstants._error, Boolean.TRUE);
	}

	private void setRequestTimeout(Struct cfhttp) {
		cfhttp.setEL(KeyConstants._charset, "");
		cfhttp.setEL(KeyConstants._errordetail, "");
		cfhttp.setEL(KeyConstants._filecontent, "Connection Timeout");
		cfhttp.setEL(KeyConstants._header, "");
		cfhttp.setEL(KeyConstants._mimetype, "Unable to determine MIME type of file.");
		cfhttp.setEL(KeyConstants._responseheader, new StructImpl());
		cfhttp.setEL(KeyConstants._statuscode, "408 Request Time-out");
		cfhttp.setEL(KeyConstants._status_code, Double.valueOf(408));
		cfhttp.setEL(KeyConstants._status_text, "Request Time-out");
		cfhttp.setEL(KeyConstants._text, Boolean.TRUE);
		cfhttp.setEL(KeyConstants._error, Boolean.TRUE);
	}

	private static String headerValue(String value) {
		if (value == null) return null;
		value = value.trim();
		value = value.replace('\n', ' ');
		value = value.replace('\r', ' ');
		/*
		 * int len=value.length(); char c; for(int i=0;i<len;i++){ c=value.charAt(i); if(c=='\n' || c=='\r')
		 * return value.substring(0,i); }
		 */
		return value;
	}

	private static String urlenc(String str, String charset, boolean checkIfNeeded) throws UnsupportedEncodingException {
		if (checkIfNeeded && !ReqRspUtil.needEncoding(str)) return str;
		return URLEncoder.encode(str, CharsetUtil.toCharset(charset));
	}

	private static void logHttpRequest(PageContext pc, Struct data, String url, String method, long executionTimeNS, boolean cached, Throwable t) throws PageException {
		Log log = ThreadLocalPageContext.getLog(pc, "http");
		if (log == null) log = ThreadLocalPageContext.getLog(pc, "application");
		if (log != null) {
			if (t != null || LogUtil.doesInfo(log)) {
				String msg = "httpRequest [" + method + "] to [" + url + "], returned [" + data.get(KeyConstants._statuscode) + "] in " + (executionTimeNS / 1000000) + "ms, "
						+ (cached ? "(cached response)" : "") + " at " + CallStackGet.call(pc, "text");

				if (t != null) log.error("cfhttp", msg, t);
				else log.info("cfhttp", msg);
			}
		}
	}

	@Override
	public void doInitBody() {

	}

	@Override
	public int doAfterBody() {
		return SKIP_BODY;
	}

	/**
	 * sets if has body or not
	 * 
	 * @param hasBody
	 */
	public void hasBody(boolean hasBody) {

	}

	/**
	 * @param param
	 */
	public void setParam(HttpParamBean param) {
		params.add(param);

	}

	/**
	 * @param getAsBinary The getasbinary to set.
	 */
	public void setGetasbinary(String getAsBinary) {
		// TODO support never, wird das verwendet?
		getAsBinary = getAsBinary.toLowerCase().trim();
		if (getAsBinary.equals("yes") || getAsBinary.equals("true")) this.getAsBinary = GET_AS_BINARY_YES;
		else if (getAsBinary.equals("no") || getAsBinary.equals("false")) this.getAsBinary = GET_AS_BINARY_NO;
		else if (getAsBinary.equals("auto")) this.getAsBinary = GET_AS_BINARY_AUTO;
	}

	/**
	 * @param multiPart The multipart to set.
	 */
	public void setMultipart(boolean multiPart) {
		this.multiPart = multiPart;
	}

	/**
	 * @param multiPartType The multipart to set.
	 * @throws ApplicationException
	 */
	public void setMultiparttype(String multiPartType) throws ApplicationException {
		if (StringUtil.isEmpty(multiPartType)) return;
		multiPartType = multiPartType.trim().toLowerCase();

		if ("form-data".equals(multiPartType)) this.multiPartType = MULTIPART_FORM_DATA;
		else throw new ApplicationException("invalid value for attribute multiPartType [" + multiPartType + "]", "attribute must have one of the following values [form-data]");

	}

	/**
	 * @param result The result to set.
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * @param addtoken the addtoken to set
	 */
	public void setAddtoken(boolean addtoken) {
		this.addtoken = addtoken;
	}

	/**
	 * @param encode encode the URL
	 */
	public void setEncode(boolean encode) {
		this.encode = encode;
	}

	/**
	 * @param clientCert the clientCert to set
	 */
	public void setClientcert(String clientCert) {
		this.clientCert = clientCert;
	}

	/**
	 * @param clientCertPassword the clientCertPassword to set
	 */
	public void setClientcertpassword(String clientCertPassword) {
		this.clientCertPassword = clientCertPassword;
	}

	/**
	 * @param trustStore path to custom trust store (JKS or PKCS12)
	 */
	public void setTruststore(String trustStore) {
		this.trustStore = trustStore;
	}

	/**
	 * @param trustStorePassword password for the custom trust store
	 */
	public void setTruststorepassword(String trustStorePassword) {
		this.trustStorePassword = trustStorePassword;
	}

	/**
	 * @param sslVerify when false, disables SSL certificate verification
	 */
	public void setSslverify(boolean sslVerify) {
		this.sslVerify = sslVerify;
	}

	/**
	 * checks if status code is a redirect
	 * 
	 * @param status
	 * @return is redirect
	 */

	static boolean isRedirect(int status) {
		return status == STATUS_REDIRECT_FOUND || status == STATUS_REDIRECT_MOVED_PERMANENTLY || status == STATUS_REDIRECT_SEE_OTHER
				|| status == STATUS_REDIRECT_TEMPORARY_REDIRECT;

	}

	/**
	 * merge to pathes to one
	 * 
	 * @param current
	 * @param realPath
	 * @return
	 * @throws MalformedURLException
	 */
	public static String mergePath(String current, String realPath) throws MalformedURLException {

		// get current directory
		String currDir;
		if (current == null || current.indexOf('/') == -1) currDir = "/";
		else if (current.endsWith("/")) currDir = current;
		else currDir = current.substring(0, current.lastIndexOf('/') + 1);

		// merge together
		String path;
		if (realPath.startsWith("./")) path = currDir + realPath.substring(2);
		else if (realPath.startsWith("/")) path = realPath;
		else if (!realPath.startsWith("../")) path = currDir + realPath;
		else {
			while (realPath.startsWith("../") || currDir.length() == 0) {
				realPath = realPath.substring(3);
				currDir = currDir.substring(0, currDir.length() - 1);
				int index = currDir.lastIndexOf('/');
				if (index == -1) throw new MalformedURLException("invalid realpath definition for URL");
				currDir = currDir.substring(0, index + 1);
			}
			path = currDir + realPath;
		}

		return path;
	}

	private static String getContentType(HttpParamBean param) {
		String mimeType = param.getMimeType();
		if (StringUtil.isEmpty(mimeType, true)) {
			mimeType = ResourceUtil.getMimeType(param.getFile(), null);
		}
		return mimeType;
	}

	public static boolean isGzipEncoded(String contentEncoding) {
		return !StringUtil.isEmpty(contentEncoding) && StringUtil.indexOfIgnoreCase(contentEncoding, "gzip") != -1;
	}

	public static Object getOutput(InputStream is, String contentType, String contentEncoding, boolean closeIS) {
		if (StringUtil.isEmpty(contentType)) contentType = "text/html";

		// Gzip
		if (Http.isGzipEncoded(contentEncoding)) {
			try {
				is = new GZIPInputStream(is);
			}
			catch (IOException e) {}
		}

		try {
			// text
			if (HTTPUtil.isTextMimeType(contentType) == Boolean.TRUE) {
				String[] tmp = HTTPUtil.splitMimeTypeAndCharset(contentType, null);
				Charset cs = Http.getCharset(tmp[1]);

				try {
					return IOUtil.toString(is, cs);
				}
				catch (IOException e) {}
			}
			// Binary
			else {
				try {
					return IOUtil.toBytes(is);
				}
				catch (IOException e) {}
			}
		}
		finally {
			if (closeIS) IOUtil.closeEL(is);
		}

		return "";
	}

	public static Charset getCharset(String strCharset) {
		if (!StringUtil.isEmpty(strCharset, true)) return CharsetUtil.toCharset(strCharset);
		return CharsetUtil.getWebCharset();
	}

	private void setTimeout(HTTPRequest req, TimeSpan timeout, TimeSpan connectionTimeout, TimeSpan socketTimeout) {

		// . Resolve Connection Timeout
		if (connectionTimeout == null) connectionTimeout = timeout;

		// Apply the ceiling: Use the smaller of the requested timeout or the 20s cap
		if (connectionTimeout == null || connectionTimeout.getMillis() > MAX_CONN_TIMEOUT_MS.getMillis() || connectionTimeout.getMillis() <= 0) {
			connectionTimeout = MAX_CONN_TIMEOUT_MS;
		}

		// 2. Resolve Socket/Read Timeout
		if (socketTimeout == null) socketTimeout = timeout;

		// Update the Http object for logging accuracy
		this.timeout = timeout;
		this.connectionTimeout = connectionTimeout;
		this.socketTimeout = socketTimeout;

		req.setConnectTimeout((int) connectionTimeout.getMillis());
		req.setConnectionRequestTimeout((int) connectionTimeout.getMillis());
		req.setReadTimeout(socketTimeout != null ? (int) socketTimeout.getMillis() : 0);
	}

}

class HttpExecutor extends PageContextThread {

	final Http http;
	volatile Throwable t;
	volatile boolean done;
	volatile boolean finished;
	volatile HTTPResponse response;
	private final HTTPRequest req;

	public HttpExecutor(PageContext pc, Http http, HTTPRequest req) {
		super(pc);
		this.http = http;
		this.req = req;
	}

	@Override
	public void run(PageContext pc) {
		try {
			response = execute();
			done = true;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			this.t = t;
		}
		finally {
			finished = true;
			SystemUtil.notify(http);
		}
	}

	public HTTPResponse execute() throws IOException {
		return response = HTTPEngine.getEngine().execute(req);
	}

}
