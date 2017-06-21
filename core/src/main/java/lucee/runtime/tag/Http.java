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
package lucee.runtime.tag;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.PageContextThread;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.mimetype.ContentType;
import lucee.commons.net.HTTPUtil;
import lucee.commons.net.URLEncoder;
import lucee.commons.net.http.Header;
import lucee.commons.net.http.httpclient.CachingGZIPInputStream;
import lucee.commons.net.http.httpclient.HTTPEngine4Impl;
import lucee.commons.net.http.httpclient.HTTPPatchFactory;
import lucee.commons.net.http.httpclient.HTTPResponse4Impl;
import lucee.commons.net.http.httpclient.ResourceBody;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.cache.tag.CacheHandler;
import lucee.runtime.cache.tag.CacheHandlerCollectionImpl;
import lucee.runtime.cache.tag.CacheItem;
import lucee.runtime.cache.tag.http.HTTPCacheItem;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.Constants;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.HTTPException;
import lucee.runtime.exp.NativeException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.RequestTimeoutException;
import lucee.runtime.ext.tag.BodyTagImpl;
import lucee.runtime.net.http.MultiPartResponseUtils;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.net.http.sni.DefaultHostnameVerifierImpl;
import lucee.runtime.net.http.sni.DefaultHttpClientConnectionOperatorImpl;
import lucee.runtime.net.http.sni.SSLConnectionSocketFactoryImpl;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.op.Caster;
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
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.util.PageContextUtil;
import lucee.runtime.util.URLResolver;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Lookup;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;

// MUST change behavor of mltiple headers now is a array, it das so?

/**
* Lets you execute HTTP POST and GET operations on files. Using cfhttp, you can execute standard
*   GET operations and create a query object from a text file. POST operations lets you upload MIME file
*   types to a server, or post cookie, formfield, URL, file, or CGI variables directly to a specified server.
*
*
*
*
**/
public final class Http extends BodyTagImpl {

	public static final String MULTIPART_RELATED = "multipart/related";
	public static final String MULTIPART_FORM_DATA = "multipart/form-data";



    /**
     * Maximum redirect count (5)
     */
    public static final short MAX_REDIRECT=15;

    /**
     * Constant value for HTTP Status Code "moved Permanently 301"
     */
    public static final int STATUS_REDIRECT_MOVED_PERMANENTLY=301;
    /**
     * Constant value for HTTP Status Code "Found 302"
     */
    public static final int STATUS_REDIRECT_FOUND=302;
    /**
     * Constant value for HTTP Status Code "see other 303"
     */
    public static final int STATUS_REDIRECT_SEE_OTHER=303;


    public static final int STATUS_REDIRECT_TEMPORARY_REDIRECT = 307;






	private static final short METHOD_GET=0;
	private static final short METHOD_POST=1;
	private static final short METHOD_HEAD=2;
	private static final short METHOD_PUT=3;
	private static final short METHOD_DELETE=4;
	private static final short METHOD_OPTIONS=5;
	private static final short METHOD_TRACE=6;
	private static final short METHOD_PATCH=7;

	private static final String NO_MIMETYPE="Unable to determine MIME type of file.";

	private static final short GET_AS_BINARY_NO=0;
	private static final short GET_AS_BINARY_YES=1;
	private static final short GET_AS_BINARY_AUTO=2;

	private static final Key STATUSCODE = KeyConstants._statuscode;
	private static final Key CHARSET = KeyConstants._charset;

	private static final Key ERROR_DETAIL = KeyImpl.intern("errordetail");
	private static final Key STATUS_CODE = KeyImpl.intern("status_code");
	private static final Key STATUS_TEXT = KeyImpl.intern("status_text");
	private static final Key HTTP_VERSION = KeyImpl.intern("http_version");


	private static final Key EXPLANATION = KeyImpl.intern("explanation");
	private static final Key RESPONSEHEADER = KeyImpl.intern("responseheader");
	private static final Key SET_COOKIE = KeyImpl.intern("set-cookie");

	private static final short AUTH_TYPE_BASIC = 0;
	private static final short AUTH_TYPE_NTLM = 1;




	static {
	    //Protocol myhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 443);
	    //Protocol.registerProtocol("https", new Protocol("https", new EasySSLProtocolSocketFactory(), 443));
	}


    private ArrayList<HttpParamBean> params=new ArrayList<HttpParamBean>();


	/** When required by a server, a valid password. */
	private String password;

	/** Required for creating a query. Options are a tab or comma. Default is a comma. */
	private char delimiter=',';

	/** Yes or No. Default is No. For GET and POST operations, if Yes, page reference returned into the
	** 	fileContent internal variable has its internal URLs fully resolved, including port number, so that
	** 	links remain intact. */
	private boolean resolveurl;

	/** A value, in seconds. When a URL timeout is specified in the browser */
	private TimeSpan timeout=null;

	/** Host name or IP address of a proxy server. */
	private String proxyserver;

	/** The filename to be used for the file that is accessed. For GET operations, defaults to the name
	** 	pecified in url. Enter path information in the path attribute. */
	private String strFile;

	/** The path to the directory in which a file is to be stored. If a path is not specified in a POST
	** 	or GET operation, a variable is created (cfhttp.fileContent) that you can use to display the results
	** 	of the POST operation in a cfoutput. */
	private String strPath;

	/** Boolean indicating whether to throw an exception that can be caught by using the cftry and
	** 	cfcatch tags. The default is NO. */
	private boolean throwonerror;

	/** set the charset for the call. */
	private String charset=null;

	/** The port number on the proxy server from which the object is requested. Default is 80. When
	** 	used with resolveURL, the URLs of retrieved documents that specify a port number are automatically
	** 	resolved to preserve links in the retrieved document. */
	private int proxyport=80;

	/** Specifies the column names for a query when creating a query as a result of a cfhttp GET. */
	private String[] columns;

	/** The port number on the server from which the object is requested. Default is 80. When used with
	** 	resolveURL, the URLs of retrieved documents that specify a port number are automatically resolved to
	** 	preserve links in the retrieved document. If a port number is specified in the url attribute, the port
	** 	value overrides the value of the port attribute. */
	private int port=-1;

	/** User agent request header. */
	private String useragent=Constants.NAME+" (CFML Engine)";

	/** Required for creating a query. Indicates the start and finish of a column. Should be
	** 	appropriately escaped when embedded in a column. For example, if the qualifier is a double quotation
	** 	mark, it should be escaped as """". If there is no text qualifier in the file, specify it as " ".
	** 	Default is the double quotation mark ("). */
	private char textqualifier='"';

	/** When required by a server, a valid username. */
	private String username;

	/** Full URL of the host name or IP address of the server on which the file resides. The URL must be
	** 	an absolute URL, including the protocol (http or https) and hostname. It may optionally contain a port
	** 	number. Port numbers specified in the url attribute override the port attribute. */
	private String url;

	/** Boolean indicating whether to redirect execution or stop execution.*/
	private boolean redirect=true;


	/** The name to assign to a query if the a query is constructed from a file. */
	private String name;

	/** GET or POST. Use GET to download a text or binary file or to create a query from the contents
	** 	of a text file. Use POST to send information to a server page or a CGI program for processing. POST
	** 	requires the use of a cfhttpparam tag. */
	private short method=METHOD_GET;

	//private boolean hasBody=false;

	private boolean firstrowasheaders=true;

	private String proxyuser=null;
	private String proxypassword="";
	private boolean multiPart=false;
	private String multiPartType=MULTIPART_FORM_DATA;

	private short getAsBinary=GET_AS_BINARY_NO;
    private String result="cfhttp";

    private boolean addtoken=false;

    private short authType=AUTH_TYPE_BASIC;
    private String workStation=null;
    private String domain=null;
	private boolean preauth=true;
	private boolean encoded=true;

	private boolean compression=true;

	private Object cachedWithin;

	/** The full path to a PKCS12 format file that contains the client certificate for the request. */
	private String clientCert;
	/** Password used to decrypt the client certificate. */
	private String clientCertPassword;

	@Override
	public void release()	{
		super.release();
	    params.clear();
		password=null;
		delimiter=',';
		resolveurl=false;
		timeout=null;
		proxyserver=null;
		proxyport=80;
		proxyuser=null;
		proxypassword="";
		strFile=null;
		throwonerror=false;
		charset=null;
		columns=null;
		port=-1;
		useragent=Constants.NAME+" (CFML Engine)";
		textqualifier='"';
		username=null;
		url=null;
		redirect=true;
		strPath=null;
		name=null;
		method=METHOD_GET;
		//hasBody=false;
		firstrowasheaders=true;

		getAsBinary=GET_AS_BINARY_NO;
		multiPart=false;
		multiPartType=MULTIPART_FORM_DATA;
        result="cfhttp";
        addtoken=false;

        authType=AUTH_TYPE_BASIC;
        workStation=null;
        domain=null;
        preauth=true;
        encoded=true;
        compression=true;
        clientCert=null;
        clientCertPassword=null;
        cachedWithin=null;
	}

	/**
	 * @param firstrowasheaders
	 */
	public void setFirstrowasheaders(boolean firstrowasheaders)	{
		this.firstrowasheaders=firstrowasheaders;
	}


	public void setEncodeurl(boolean encoded)	{
		this.encoded=encoded;
	}


	/** set the value password
	*  When required by a server, a valid password.
	* @param password value to set
	**/
	public void setPassword(String password)	{
		this.password=password;
	}
	/** set the value password
	*  When required by a proxy server, a valid password.
	* @param proxypassword value to set
	**/
	public void setProxypassword(String proxypassword)	{
		this.proxypassword=proxypassword;
	}

	/** set the value delimiter
	*  Required for creating a query. Options are a tab or comma. Default is a comma.
	* @param delimiter value to set
	**/
	public void setDelimiter(String delimiter)	{
		this.delimiter=delimiter.length()==0?',':delimiter.charAt(0);
	}

	/** set the value resolveurl
	*  Yes or No. Default is No. For GET and POST operations, if Yes, page reference returned into the
	* 	fileContent internal variable has its internal URLs fully resolved, including port number, so that
	* 	links remain intact.
	* @param resolveurl value to set
	**/
	public void setResolveurl(boolean resolveurl)	{
		this.resolveurl=resolveurl;
	}

	public void setPreauth(boolean preauth)	{
		this.preauth=preauth;
	}



	/** set the value timeout
	* @param timeout value to set
	 * @throws ExpressionException
	**/
	public void setTimeout(Object timeout) throws PageException	{
		if(timeout instanceof TimeSpan)
			this.timeout=(TimeSpan) timeout;
		// seconds
		else {
			int i = Caster.toIntValue(timeout);
			if(i<0)
				throw new ApplicationException("invalid value ["+i+"] for attribute timeout, value must be a positive integer greater or equal than 0");

			this.timeout=new TimeSpanImpl(0, 0, 0, i);
		}
	}

	/** set the value proxyserver
	*  Host name or IP address of a proxy server.
	* @param proxyserver value to set
	**/
	public void setProxyserver(String proxyserver)	{
		this.proxyserver=proxyserver;
	}

	/** set the value proxyport
	*  The port number on the proxy server from which the object is requested. Default is 80. When
	* 	used with resolveURL, the URLs of retrieved documents that specify a port number are automatically
	* 	resolved to preserve links in the retrieved document.
	* @param proxyport value to set
	**/
	public void setProxyport(double proxyport)	{
		this.proxyport=(int)proxyport;
	}

	/** set the value file
	*  The filename to be used for the file that is accessed. For GET operations, defaults to the name
	* 	pecified in url. Enter path information in the path attribute.
	* @param file value to set
	**/
	public void setFile(String file)	{
		this.strFile=file;
	}

	/** set the value throwonerror
	*  Boolean indicating whether to throw an exception that can be caught by using the cftry and
	* 	cfcatch tags. The default is NO.
	* @param throwonerror value to set
	**/
	public void setThrowonerror(boolean throwonerror)	{
		this.throwonerror=throwonerror;
	}

	/** set the value charset
	*  set the charset for the call.
	* @param charset value to set
	**/
	public void setCharset(String charset)	{
		this.charset=charset;
	}

	/** set the value columns
	* @param columns value to set
	 * @throws PageException
	**/
	public void setColumns(String columns) throws PageException	{
		this.columns=ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(columns,","));
	}

	/** set the value port
	*  The port number on the server from which the object is requested. Default is 80. When used with
	* 	resolveURL, the URLs of retrieved documents that specify a port number are automatically resolved to
	* 	preserve links in the retrieved document. If a port number is specified in the url attribute, the port
	* 	value overrides the value of the port attribute.
	* @param port value to set
	**/
	public void setPort(double port)	{
		this.port=(int) port;
	}

	/** set the value useragent
	*  User agent request header.
	* @param useragent value to set
	**/
	public void setUseragent(String useragent)	{
		this.useragent=useragent;
	}

	/** set the value textqualifier
	*  Required for creating a query. Indicates the start and finish of a column. Should be
	* 	appropriately escaped when embedded in a column. For example, if the qualifier is a double quotation
	* 	mark, it should be escaped as """". If there is no text qualifier in the file, specify it as " ".
	* 	Default is the double quotation mark (").
	* @param textqualifier value to set
	**/
	public void setTextqualifier(String textqualifier)	{
		this.textqualifier=textqualifier.length()==0?'"':textqualifier.charAt(0);
	}

	/** set the value username
	*  When required by a proxy server, a valid username.
	* @param proxyuser value to set
	**/
	public void setProxyuser(String proxyuser)	{
		this.proxyuser=proxyuser;
	}

	/** set the value username
	*  When required by a server, a valid username.
	* @param username value to set
	**/
	public void setUsername(String username)	{
		this.username=username;
	}

	/** set the value url
	*  Full URL of the host name or IP address of the server on which the file resides. The URL must be
	* 	an absolute URL, including the protocol (http or https) and hostname. It may optionally contain a port
	* 	number. Port numbers specified in the url attribute override the port attribute.
	* @param url value to set
	**/
	public void setUrl(String url)	{
		this.url=url;
	}

	/** set the value redirect
	* @param redirect value to set
	**/
	public void setRedirect(boolean redirect)	{
		this.redirect=redirect;
	}

	/** set the value path
	*  The path to the directory in which a file is to be stored. If a path is not specified in a POST
	* 	or GET operation, a variable is created (cfhttp.fileContent) that you can use to display the results
	* 	of the POST operation in a cfoutput.
	* @param path value to set
	**/
	public void setPath(String path)	{
		this.strPath=path;
	}

	/** set the value name
	*  The name to assign to a query if the a query is constructed from a file.
	* @param name value to set
	**/
	public void setName(String name)	{
		this.name=name;
	}

	public void setAuthtype(String strAuthType) throws ExpressionException{
		if(StringUtil.isEmpty(strAuthType,true)) return;
		strAuthType=strAuthType.trim();
		if("basic".equalsIgnoreCase(strAuthType)) authType=AUTH_TYPE_BASIC;
		else if("ntlm".equalsIgnoreCase(strAuthType)) authType=AUTH_TYPE_NTLM;
		else
			throw new ExpressionException("invalid value ["+strAuthType+"] for attribute authType, value must be one of the following [basic,ntlm]");
	}

	public void setWorkstation(String workStation)	{
		this.workStation=workStation;
	}

	public void setDomain(String domain)	{
		this.domain=domain;
	}

	/** set the value method
	*  GET or POST. Use GET to download a text or binary file or to create a query from the contents
	* 	of a text file. Use POST to send information to a server page or a CGI program for processing. POST
	* 	requires the use of a cfhttpparam tag.
	* @param method value to set
	 * @throws ApplicationException
	**/
	public void setMethod(String method) throws ApplicationException	{
	    method=method.toLowerCase().trim();
	    if(method.equals("post")) this.method=METHOD_POST;
	    else if(method.equals("get")) this.method=METHOD_GET;
	    else if(method.equals("head")) this.method=METHOD_HEAD;
	    else if(method.equals("delete")) this.method=METHOD_DELETE;
	    else if(method.equals("put")) this.method=METHOD_PUT;
	    else if(method.equals("trace")) this.method=METHOD_TRACE;
	    else if(method.equals("options")) this.method=METHOD_OPTIONS;
	    else if(method.equals("patch")) this.method=METHOD_PATCH;
	    else throw new ApplicationException("invalid method type ["+(method.toUpperCase())+"], valid types are POST,GET,HEAD,DELETE,PUT,TRACE,OPTIONS,PATCH");
	}

	public void setCompression(String strCompression) throws ApplicationException {
		if(StringUtil.isEmpty(strCompression,true)) return;
		Boolean b = Caster.toBoolean(strCompression,null);

		if(b!=null) compression=b.booleanValue();
		else if(strCompression.trim().equalsIgnoreCase("none")) compression=false;
	    else throw new ApplicationException("invalid value for attribute compression ["+strCompression+"], valid values are: true,false or none");

	}

	public void setCachedwithin(Object cachedwithin)	{
		if(StringUtil.isEmpty(cachedwithin)) return;
		this.cachedWithin=cachedwithin;
	}


	@Override
	public int doStartTag() throws PageException	{
		if(addtoken) {
			setParam(HttpParamBean.TYPE_COOKIE,"cfid",pageContext.getCFID());
			setParam(HttpParamBean.TYPE_COOKIE,"cftoken",pageContext.getCFToken());
			String jsessionid = pageContext.getJSessionId();
			if(jsessionid!=null)setParam(HttpParamBean.TYPE_COOKIE,"jsessionid",jsessionid);
		}

		// cache within
		if(StringUtil.isEmpty(cachedWithin)){
			Object tmp = ((PageContextImpl)pageContext).getCachedWithin(ConfigWeb.CACHEDWITHIN_HTTP);
			if(tmp!=null)setCachedwithin(tmp);
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

		// because commons
		PrintStream out = System.out;
        try {
        	//System.setOut(new PrintStream(DevNullOutputStream.DEV_NULL_OUTPUT_STREAM));
             _doEndTag();
             return EVAL_PAGE;
        }
        catch (IOException e) {
            throw Caster.toPageException(e);
        }
        finally {
        	System.setOut(out);
        }

	}



	private void _doEndTag() throws PageException, IOException	{
		long start=System.nanoTime();
		HttpClientBuilder builder = HttpClients.custom();
		ssl(builder);

    	// redirect
    	if(redirect)  builder.setRedirectStrategy(new DefaultRedirectStrategy());
    	else builder.disableRedirectHandling();

    	// cookies
    	BasicCookieStore cookieStore = new BasicCookieStore();
    	builder.setDefaultCookieStore(cookieStore);

    	ConfigWeb cw = pageContext.getConfig();
    	HttpRequestBase req=null;
    	HttpContext httpContext=null;
		//HttpRequestBase req = init(pageContext.getConfig(),this,client,params,url,port);
    	{
    		if(StringUtil.isEmpty(charset,true)) charset=((PageContextImpl)pageContext).getWebCharset().name();
    		else charset=charset.trim();


    	// check if has fileUploads
    		boolean doUploadFile=false;
    		for(int i=0;i<this.params.size();i++) {
    			if((this.params.get(i)).getType()==HttpParamBean.TYPE_FILE) {
    				doUploadFile=true;
    				break;
    			}
    		}

    	// parse url (also query string)
    		int len=this.params.size();
    		StringBuilder sbQS=new StringBuilder();
    		for(int i=0;i<len;i++) {
    			HttpParamBean param=this.params.get(i);
    			int type=param.getType();
    		// URL
    			if(type==HttpParamBean.TYPE_URL) {
    				if(sbQS.length()>0)sbQS.append('&');
    				sbQS.append(param.getEncoded()?urlenc(param.getName(),charset):param.getName());
    				sbQS.append('=');
    				sbQS.append(param.getEncoded()?urlenc(param.getValueAsString(), charset):param.getValueAsString());
    			}
    		}
    		String host=null;
    		HttpHost httpHost;
    		try {
    			URL _url = HTTPUtil.toURL(url,port,encoded);
    			httpHost = new HttpHost(_url.getHost(),_url.getPort());
    			host=_url.getHost();
    			url=_url.toExternalForm();
    			if(sbQS.length()>0){
    				// no existing QS
    				if(StringUtil.isEmpty(_url.getQuery())) {
    					url+="?"+sbQS;
    				}
    				else {
    					url+="&"+sbQS;
    				}
    			}
    		}
    		catch (MalformedURLException mue) {
    			throw Caster.toPageException(mue);
    		}

    	// cache
    		if(cachedWithin!=null) {
    			CacheHandler ch = pageContext.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_HTTP,null).getInstanceMatchingObject(cachedWithin,null);
    			if(ch!=null) {
	    			CacheItem ci = ch.get(pageContext, createId());
	    			if(ci instanceof HTTPCacheItem) {
	    				pageContext.setVariable(result,((HTTPCacheItem)ci).getData());
	    				return;
	    			}
    			}
    		}

    	// select best matching method (get,post, post multpart (file))

    		boolean isBinary = false;
    		boolean doMultiPart=doUploadFile || this.multiPart;
    		HttpEntityEnclosingRequest post=null;
    		HttpEntityEnclosingRequest eem=null;


    		if(this.method==METHOD_GET) {
    			req=new HttpGet(url);
    		}
    		else if(this.method==METHOD_HEAD) {
    		    req=new HttpHead(url);
    		}
    		else if(this.method==METHOD_DELETE) {
    			isBinary=true;
    		    req=new HttpDelete(url);
    		}
    		else if(this.method==METHOD_PUT) {
    			isBinary=true;
    			HttpPut put = new HttpPut(url);
    			post=put;
    		    req=put;
    		    eem=put;

    		}
    		else if(this.method==METHOD_TRACE) {
    			isBinary=true;
    		    req=new HttpTrace(url);
    		}
    		else if(this.method==METHOD_OPTIONS) {
    			isBinary=true;
    		    req=new HttpOptions(url);
    		}
    		else if(this.method==METHOD_PATCH) {
    			isBinary=true;
    			eem = HTTPPatchFactory.getHTTPPatch(url);
    		    req=(HttpRequestBase) eem;
    		}
    		else {
    			isBinary=true;
    			post=new HttpPost(url);
    			req=(HttpPost)post;
    			eem=post;
    		}

    		boolean hasForm=false;
    		boolean hasBody=false;
    		boolean hasContentType=false;
    	// Set http params
    		ArrayList<FormBodyPart> parts=new ArrayList<FormBodyPart>();

    		StringBuilder acceptEncoding=new StringBuilder();
    		java.util.List<NameValuePair> postParam = post!=null?new ArrayList <NameValuePair>():null;

    		for(int i=0;i<len;i++) {
    			HttpParamBean param=this.params.get(i);
    			int type=param.getType();

    		// URL
    			if(type==HttpParamBean.TYPE_URL) {
    				//listQS.add(new BasicNameValuePair(translateEncoding(param.getName(), http.charset),translateEncoding(param.getValueAsString(), http.charset)));
    			}
    		// Form
    			else if(type==HttpParamBean.TYPE_FORM) {
    				hasForm=true;
    				if(this.method==METHOD_GET) throw new ApplicationException("httpparam with type formfield can only be used when the method attribute of the parent http tag is set to post");
    				if(post!=null){
    					if(doMultiPart)	{
    						parts.add(
    							new FormBodyPart(
    								param.getName(),
    								new StringBody(
    										param.getValueAsString(),
    										CharsetUtil.toCharset(charset)
    								)
    							)
    						);
    					}
    					else {
    						postParam.add(new BasicNameValuePair(param.getName(),param.getValueAsString()));
    					}
    				}
    				//else if(multi!=null)multi.addParameter(param.getName(),param.getValueAsString());
    			}
    		// CGI
    			else if(type==HttpParamBean.TYPE_CGI) {
    				if(param.getEncoded())
    					req.addHeader(
    							urlenc(param.getName(),charset),
    							urlenc(param.getValueAsString(),charset));
                    else
                        req.addHeader(param.getName(),param.getValueAsString());
    			}
            // Header
                else if(type==HttpParamBean.TYPE_HEADER) {
                	if(param.getName().equalsIgnoreCase("content-type")) hasContentType=true;

                	if(param.getName().equalsIgnoreCase("Content-Length")) {}
                	else if(param.getName().equalsIgnoreCase("Accept-Encoding")) {
                		acceptEncoding.append(headerValue(param.getValueAsString()));
                		acceptEncoding.append(", ");
                	}
                	else req.addHeader(param.getName(),headerValue(param.getValueAsString()));
                }
    		// Cookie
    			else if(type==HttpParamBean.TYPE_COOKIE) {
    				HTTPEngine4Impl.addCookie(cookieStore,host,param.getName(),param.getValueAsString(),"/",charset);
    			}
    		// File
    			else if(type==HttpParamBean.TYPE_FILE) {
    				hasForm=true;
    				if(this.method==METHOD_GET) throw new ApplicationException("httpparam type file can't only be used, when method of the tag http equal post");
    				//if(param.getFile()==null) throw new ApplicationException("httpparam type file can't only be used, when method of the tag http equal post");
    				String strCT = getContentType(param);
    				ContentType ct = HTTPUtil.toContentType(strCT,null);

    				String mt="text/xml";
    				if(ct!=null && !StringUtil.isEmpty(ct.getMimeType(),true)) mt=ct.getMimeType();

    				String cs=charset;
    				if(ct!=null && !StringUtil.isEmpty(ct.getCharset(),true)) cs=ct.getCharset();


    				if(doMultiPart) {
    					try {
    						Resource res = param.getFile();
    						parts.add(new FormBodyPart(
    								param.getName(),
    								new ResourceBody(res, mt, res.getName(), cs)
    						));
    						//parts.add(new ResourcePart(param.getName(),new ResourcePartSource(param.getFile()),getContentType(param),_charset));
    					}
    					catch (FileNotFoundException e) {
    						throw new ApplicationException("can't upload file, path is invalid",e.getMessage());
    					}
    				}
    			}
    		// XML
    			else if(type==HttpParamBean.TYPE_XML) {
    				ContentType ct = HTTPUtil.toContentType(param.getMimeType(),null);

    				String mt="text/xml";
    				if(ct!=null && !StringUtil.isEmpty(ct.getMimeType(),true)) mt=ct.getMimeType();

    				String cs=charset;
    				if(ct!=null && !StringUtil.isEmpty(ct.getCharset(),true)) cs=ct.getCharset();

    				hasBody=true;
    				hasContentType=true;
    				req.addHeader("Content-type", mt+"; charset="+cs);
    			    if(eem==null)throw new ApplicationException("type xml is only supported for type post and put");
    			    HTTPEngine4Impl.setBody(eem, param.getValueAsString(),mt,cs);
    			}
    		// Body
    			else if(type==HttpParamBean.TYPE_BODY) {
    				ContentType ct = HTTPUtil.toContentType(param.getMimeType(),null);

    				String mt=null;
    				if(ct!=null && !StringUtil.isEmpty(ct.getMimeType(),true)) mt=ct.getMimeType();

    				String cs=charset;
    				if(ct!=null && !StringUtil.isEmpty(ct.getCharset(),true)) cs=ct.getCharset();


    				hasBody=true;
    				if(eem==null)throw new ApplicationException("type body is only supported for type post and put");
    				HTTPEngine4Impl.setBody(eem, param.getValue(),mt,cs);

    			}
                else {
                    throw new ApplicationException("invalid type ["+type+"]");
                }

    		}

    		// post params
    		if(postParam!=null && postParam.size()>0)
    			post.setEntity(new org.apache.http.client.entity.UrlEncodedFormEntity(postParam,charset));

    		if(compression){
    			acceptEncoding.append("gzip");
    		}
    		else {
    			acceptEncoding.append("deflate;q=0");
    			req.setHeader("TE", "deflate;q=0");
    		}
			req.setHeader("Accept-Encoding",acceptEncoding.toString());



    		// multipart
    		if(doMultiPart && eem!=null) {
    			hasContentType=true;
    			boolean doIt=true;
    			if(!this.multiPart && parts.size()==1){
    				ContentBody body = parts.get(0).getBody();
    				if(body instanceof StringBody){
    					StringBody sb=(StringBody)body;
    					try {
    						org.apache.http.entity.ContentType ct=org.apache.http.entity.ContentType.create(sb.getMimeType(),sb.getCharset());
    						String str = IOUtil.toString(sb.getReader());
    						StringEntity entity = new StringEntity(str,ct);
    						eem.setEntity(entity);

    					} catch (IOException e) {
    						throw Caster.toPageException(e);
    					}
    					doIt=false;
    				}
    			}
    			if(doIt) {
    				MultipartEntity mpe = new MultipartEntity(HttpMultipartMode.STRICT);
    				Iterator<FormBodyPart> it = parts.iterator();
    				while(it.hasNext()) {
    					FormBodyPart part = it.next();
    					mpe.addPart(part.getName(),part.getBody());
    				}
    				eem.setEntity(mpe);
    			}
    				//eem.setRequestEntity(new MultipartRequestEntityFlex(parts.toArray(new Part[parts.size()]), eem.getParams(),http.multiPartType));
    		}



    		if(hasBody && hasForm)
    			throw new ApplicationException("mixing httpparam  type file/formfield and body/XML is not allowed");

    		if(!hasContentType) {
    			if(isBinary) {
    				if(hasBody) req.addHeader("Content-type", "application/octet-stream");
    				else req.addHeader("Content-type", "application/x-www-form-urlencoded; charset="+charset);
    			}
    			else {
    				if(hasBody)
    					req.addHeader("Content-type", "text/html; charset="+charset );
    			}
    		}


    		// set User Agent
    			if(!hasHeaderIgnoreCase(req,"User-Agent"))
    				req.setHeader("User-Agent",this.useragent);

    	// set timeout
    			setTimeout(builder,checkRemainingTimeout());


    	// set Username and Password
    		if(this.username!=null) {
    			if(this.password==null)this.password="";
    			if(AUTH_TYPE_NTLM==this.authType) {
    				if(StringUtil.isEmpty(this.workStation,true))
    	                throw new ApplicationException("attribute workstation is required when authentication type is [NTLM]");
    				if(StringUtil.isEmpty(this.domain,true))
    	                throw new ApplicationException("attribute domain is required when authentication type is [NTLM]");

    				HTTPEngine4Impl.setNTCredentials(builder, this.username, this.password, this.workStation,this.domain);
    			}
    			else httpContext=HTTPEngine4Impl.setCredentials(builder, httpHost, this.username, this.password,preauth);
    		}

    	// set Proxy
    		ProxyData proxy=null;
    		if(!StringUtil.isEmpty(this.proxyserver)) {
    			proxy=ProxyDataImpl.getInstance(this.proxyserver, this.proxyport, this.proxyuser, this.proxypassword) ;
    		}
    		if(pageContext.getConfig().isProxyEnableFor(host)) {
    			proxy=pageContext.getConfig().getProxyData();
    		}
    		HTTPEngine4Impl.setProxy(builder, req, proxy);

    	}




    	CloseableHttpClient client=null;
    	try {
    	if(httpContext==null)httpContext = new BasicHttpContext();

    	Struct cfhttp=new StructImpl();
		cfhttp.setEL(ERROR_DETAIL,"");
		pageContext.setVariable(result,cfhttp);


/////////////////////////////////////////// EXECUTE /////////////////////////////////////////////////
		client = builder.build();
		Executor4 e = new Executor4(pageContext,this,client,httpContext,req,redirect);
		HTTPResponse4Impl rsp=null;

		if(timeout==null || timeout.getMillis()<=0){
			try{
				rsp = e.execute(httpContext);
			}

			catch(Throwable t){
				ExceptionUtil.rethrowIfNecessary(t);
				if(!throwonerror){
					if(t instanceof SocketTimeoutException)setRequestTimeout(cfhttp);
					else setUnknownHost(cfhttp, t);
					return;
				}
				throw toPageException(t,rsp);

			}
		} else {
			e.start();
			try {
				synchronized(this){//print.err(timeout);
					this.wait(timeout.getMillis());
				}
			} catch (InterruptedException ie) {
				throw Caster.toPageException(ie);
			}
			if(e.t!=null){
				if(!throwonerror){
					setUnknownHost(cfhttp,e.t);
					return;
				}
				throw toPageException(e.t,rsp);
			}

			rsp=e.response;


			if(!e.done){
				req.abort();
				if(throwonerror)
					throw new HTTPException("408 Request Time-out","a timeout occurred in tag http",408,"Time-out",rsp==null?null:rsp.getURL());
				setRequestTimeout(cfhttp);
				return;
				//throw new ApplicationException("timeout");
			}
		}

/////////////////////////////////////////// EXECUTE /////////////////////////////////////////////////
		Charset responseCharset=CharsetUtil.toCharset(rsp.getCharset());
		int statCode=0;
	// Write Response Scope
		//String rawHeader=httpMethod.getStatusLine().toString();
			String mimetype=null;
			String contentEncoding=null;

		// status code
			cfhttp.set(STATUSCODE,((rsp.getStatusCode()+" "+rsp.getStatusText()).trim()));
			cfhttp.set(STATUS_CODE,new Double(statCode=rsp.getStatusCode()));
			cfhttp.set(STATUS_TEXT,(rsp.getStatusText()));
			cfhttp.set(HTTP_VERSION,(rsp.getProtocolVersion()));

		//responseHeader
			lucee.commons.net.http.Header[] headers = rsp.getAllHeaders();
			StringBuffer raw=new StringBuffer(rsp.getStatusLine()+" ");
			Struct responseHeader = new StructImpl();
			Struct cookie;
			Array setCookie = new ArrayImpl();
			Query cookies=new QueryImpl(new String[]{"name","value","path","domain","expires","secure","httpOnly"},0,"cookies");

	        for(int i=0;i<headers.length;i++) {
	        	lucee.commons.net.http.Header header=headers[i];
	        	//print.ln(header);

	        	raw.append(header.toString()+" ");
	        	if(header.getName().equalsIgnoreCase("Set-Cookie")) {
	        		setCookie.append(header.getValue());
	        		parseCookie(cookies,header.getValue());
	        	}
	        	else {
	        	    //print.ln(header.getName()+"-"+header.getValue());
	        		Object value=responseHeader.get(KeyImpl.getInstance(header.getName()),null);
	        		if(value==null) responseHeader.set(KeyImpl.getInstance(header.getName()),header.getValue());
	        		else {
	        		    Array arr=null;
	        		    if(value instanceof Array) {
	        		        arr=(Array) value;
	        		    }
	        		    else {
	        		        arr=new ArrayImpl();
	        		        responseHeader.set(KeyImpl.getInstance(header.getName()),arr);
	        		        arr.appendEL(value);
	        		    }
	        		    arr.appendEL(header.getValue());
	        		}
	        	}

	        	// Content-Type
	        	if(header.getName().equalsIgnoreCase("Content-Type")) {
	        		mimetype=header.getValue();
		    	    if(mimetype==null)mimetype=NO_MIMETYPE;
	        	}

	        	// Content-Encoding
        		if(header.getName().equalsIgnoreCase("Content-Encoding")) {
        			contentEncoding=header.getValue();
        		}

	        }
	        cfhttp.set(RESPONSEHEADER,responseHeader);
	        cfhttp.set(KeyConstants._cookies,cookies);
	        responseHeader.set(STATUS_CODE,new Double(statCode=rsp.getStatusCode()));
	        responseHeader.set(EXPLANATION,(rsp.getStatusText()));
	        if(setCookie.size()>0)responseHeader.set(SET_COOKIE,setCookie);

	    // is text
	        boolean isText=
	        	mimetype == null ||
	        	mimetype == NO_MIMETYPE || HTTPUtil.isTextMimeType(mimetype);

		    // is multipart
	        boolean isMultipart= MultiPartResponseUtils.isMultipart(mimetype);

	        cfhttp.set(KeyConstants._text,Caster.toBoolean(isText));

	    // mimetype charset
	        //boolean responseProvideCharset=false;
	        if(!StringUtil.isEmpty(mimetype,true)){
		        if(isText) {
		        	String[] types=HTTPUtil.splitMimeTypeAndCharset(mimetype,null);
		        	if(types[0]!=null)cfhttp.set(KeyConstants._mimetype,types[0]);
		        	if(types[1]!=null)cfhttp.set(CHARSET,types[1]);

		        }
		        else cfhttp.set(KeyConstants._mimetype,mimetype);
	        }
	        else cfhttp.set(KeyConstants._mimetype,NO_MIMETYPE);

	    // File
	        Resource file=null;

	        if(strFile!=null && strPath!=null) {
	            file=ResourceUtil.toResourceNotExisting(pageContext, strPath).getRealResource(strFile);
	        }
	        else if(strFile!=null) {
	            file=ResourceUtil.toResourceNotExisting(pageContext, strFile);
	        }
	        else if(strPath!=null) {
	            file=ResourceUtil.toResourceNotExisting(pageContext, strPath);
	            //Resource dir = file.getParentResource();
	            if(file.isDirectory()){
	            	file=file.getRealResource(req.getURI().getPath());// TODO was getName() ->http://hc.apache.org/httpclient-3.x/apidocs/org/apache/commons/httpclient/URI.html#getName()
	            }

	        }
	        if(file!=null)pageContext.getConfig().getSecurityManager().checkFileLocation(file);


	        // filecontent
	        InputStream is=null;
		    if(isText && getAsBinary!=GET_AS_BINARY_YES) {
		    	String str;
                try {

                	// read content
                	if(method!=METHOD_HEAD) {
                		is = rsp.getContentAsStream();
	                    if(is!=null &&isGzipEncoded(contentEncoding))
	                    	is = rsp.getStatusCode()!=200? new CachingGZIPInputStream(is):new GZIPInputStream(is);
                	}
                    try {
                    	try{
                    	str = is==null?"":IOUtil.toString(is,responseCharset,checkRemainingTimeout().getMillis());
                    	}
                    	catch(EOFException eof){
                    		if(is instanceof CachingGZIPInputStream) {
                    			str = IOUtil.toString(is=((CachingGZIPInputStream)is).getRawData(),responseCharset,checkRemainingTimeout().getMillis());
                    		}
                    		else throw eof;
                    	}
                    }
                    catch (UnsupportedEncodingException uee) {
                    	str = IOUtil.toString(is,(Charset)null,checkRemainingTimeout().getMillis());
                    }
                }
                catch (IOException ioe) {
                	throw Caster.toPageException(ioe);
                }
                finally {
                	IOUtil.closeEL(is);
                }

                if(str==null)str="";
		        if(resolveurl){
		        	//if(e.redirectURL!=null)url=e.redirectURL.toExternalForm();
		        	str=new URLResolver().transform(str,e.response.getTargetURL(),false);
		        }
		        cfhttp.set(KeyConstants._filecontent,str);
		        try {
		        	if(file!=null){
		        		IOUtil.write(file,str,((PageContextImpl)pageContext).getWebCharset(),false);
                    }
                }
		        catch (IOException e1) {}

		        if(name!=null) {
                    Query qry = CSVParser.toQuery( str, delimiter, textqualifier, columns, firstrowasheaders  );
                    pageContext.setVariable(name,qry);
		        }
		    }
		    // Binary
		    else {
		    	byte[] barr=null;
		        if(isGzipEncoded(contentEncoding)){
		        	if(method!=METHOD_HEAD) {
			        	is=rsp.getContentAsStream();
			        	is = rsp.getStatusCode()!=200?new CachingGZIPInputStream(is) :new GZIPInputStream(is);
		        	}

		        	try {
		        		try{
		        			barr = is==null?new byte[0]: IOUtil.toBytes(is);
		        		}
		        		catch(EOFException eof){
		        			if(is instanceof CachingGZIPInputStream)
		        				barr = IOUtil.toBytes(((CachingGZIPInputStream)is).getRawData());
		        			else throw eof;
		        		}
					}
		        	catch (IOException t) {
		        		throw Caster.toPageException(t);
					}
					finally{
						IOUtil.closeEL(is);
					}
		        }
		        else {
		        	try {
		        		if(method!=METHOD_HEAD) barr = rsp.getContentAsByteArray();
		        		else barr=new byte[0];
					}
		        	catch (IOException t) {
		        		throw Caster.toPageException(t);
					}
		        }
		        //IF Multipart response get file content and parse parts
		        if(barr!=null) {
				    if(isMultipart) {
				    	cfhttp.set(KeyConstants._filecontent,MultiPartResponseUtils.getParts(barr,mimetype));
				    } else {
				    	cfhttp.set(KeyConstants._filecontent,barr);
				    }
		        }
		        else
			    	cfhttp.set(KeyConstants._filecontent,"");


		        if(file!=null) {
		        	try {
		        		if(barr!=null)IOUtil.copy(new ByteArrayInputStream(barr),file,true);
		        	}
		        	catch (IOException ioe) {
                		throw Caster.toPageException(ioe);
		        	}
		        }
		    }

	    // header
	        cfhttp.set(KeyConstants._header,raw.toString());
	        if(!isStatusOK(rsp.getStatusCode())){
	        	String msg=rsp.getStatusCode()+" "+rsp.getStatusText();
	            cfhttp.setEL(ERROR_DETAIL,msg);
	            if(throwonerror){
	            	throw new HTTPException(msg,null,rsp.getStatusCode(),rsp.getStatusText(),rsp.getURL());
	            }
	        }
	     // add to cache
	    	if(cachedWithin!=null && rsp.getStatusCode()==200) {
				String id = createId();
				CacheHandler ch = pageContext.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_HTTP,null).getInstanceMatchingObject(cachedWithin,null);
				if(ch!=null){

					if(statCode>=200 && statCode<300)
						ch.set(pageContext, id,cachedWithin,new HTTPCacheItem(cfhttp,url,System.nanoTime()-start));
				}

			}
    	}
		finally {
			if(client!=null)client.close();
		}
	}

	private void ssl(HttpClientBuilder builder) throws PageException {
		try {
			// SSLContext sslcontext = SSLContexts.createSystemDefault();
			SSLContext sslcontext = SSLContext.getInstance("TLSv1.2");
			if(!StringUtil.isEmpty(this.clientCert)) {
				if(this.clientCertPassword==null)this.clientCertPassword="";
				File ksFile = new File(this.clientCert);
				KeyStore clientStore = KeyStore.getInstance("PKCS12");
				clientStore.load(new FileInputStream(ksFile), this.clientCertPassword.toCharArray());

				KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				kmf.init(clientStore, this.clientCertPassword.toCharArray());

				sslcontext.init(kmf.getKeyManagers(), null, new java.security.SecureRandom());
			} else {
				sslcontext.init(null, null, new java.security.SecureRandom());
			}
			final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactoryImpl(sslcontext,new DefaultHostnameVerifierImpl());
			builder.setSSLSocketFactory(sslsf);
			Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
	        .register("http", PlainConnectionSocketFactory.getSocketFactory())
	        .register("https", sslsf)
	        .build();
			PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(
					new DefaultHttpClientConnectionOperatorImpl(reg), null, -1, TimeUnit.MILLISECONDS); // TODO review -1 setting
			builder.setConnectionManager(cm);
		}
		catch(Exception e){
			throw Caster.toPageException(e);
		}
	}

	private TimeSpan checkRemainingTimeout() throws RequestTimeoutException {
		TimeSpan remaining = PageContextUtil.remainingTime(pageContext,true);
		if(this.timeout==null || ((int)this.timeout.getSeconds())<=0 || timeout.getSeconds()>remaining.getSeconds()) { // not set
			this.timeout=remaining;
		}
		return timeout;
	}


	private String createId() {
		return CacheHandlerCollectionImpl.createId(url,addtoken?pageContext.getURLToken():"",method,params,username,password,this.port
				,proxyserver,proxyport,proxyuser,proxypassword,useragent);
	}

	private void parseCookie(Query cookies,String raw) {
		String[] arr =ListUtil.trimItems(ListUtil.trim(ListUtil.listToStringArray(raw, ';')));
		if(arr.length==0) return;
		int row = cookies.addRow();
		String item;

		int index;
		// name/value
		if(arr.length>0) {
			item=arr[0];
			index=item.indexOf('=');
			if(index==-1)  // only name
				cookies.setAtEL(KeyConstants._name,row, dec(item));
			else { // name and value
				cookies.setAtEL(KeyConstants._name,row, dec(item.substring(0,index)));
				cookies.setAtEL(KeyConstants._value,row, dec(item.substring(index+1)));
			}

		}
		String n,v;
		cookies.setAtEL("secure",row, Boolean.FALSE);
		cookies.setAtEL("httpOnly",row, Boolean.FALSE);
		for(int i=1;i<arr.length;i++){
			item=arr[i];
			index=item.indexOf('=');
			if(index==-1)  // only name
				cookies.setAtEL(dec(item),row, Boolean.TRUE);
			else { // name and value
				n=dec(item.substring(0,index));
				v=dec(item.substring(index+1));
				if(n.equalsIgnoreCase("expires")) {
					DateTime d = Caster.toDate(v, false, null,null);

					if(d!=null) {
						cookies.setAtEL(n,row, d);
						continue;
					}
				}
				cookies.setAtEL(n,row, v);
			}

		}
	}

	public String dec(String str) {
    	return ReqRspUtil.decode(str, charset, false);
	}

	public static boolean isStatusOK(int statusCode) {
		return statusCode>=200 && statusCode<=299;
	}

	private PageException toPageException(Throwable t, HTTPResponse4Impl rsp) {
		if(t instanceof SocketTimeoutException) {
			HTTPException he = new HTTPException("408 Request Time-out","a timeout occurred in tag http",408,"Time-out",rsp==null?null:rsp.getURL());
			List<StackTraceElement> merged = ArrayUtil.merge(t.getStackTrace(), he.getStackTrace());
			StackTraceElement[] traces=new StackTraceElement[merged.size()];
			Iterator<StackTraceElement> it = merged.iterator();
			int index=0;
			while(it.hasNext()){
				traces[index++]=it.next();
			}
			he.setStackTrace(traces);
			return he;
		}
		PageException pe = Caster.toPageException(t);
		if(pe instanceof NativeException) {
			((NativeException) pe).setAdditional(KeyConstants._url, url);
		}
		return pe;
	}







	private void setUnknownHost(Struct cfhttp,Throwable t) {
		cfhttp.setEL(CHARSET,"");
		cfhttp.setEL(ERROR_DETAIL,"Unknown host: "+t.getMessage());
		cfhttp.setEL(KeyConstants._filecontent,"Connection Failure");
		cfhttp.setEL(KeyConstants._header,"");
		cfhttp.setEL(KeyConstants._mimetype,"Unable to determine MIME type of file.");
		cfhttp.setEL(RESPONSEHEADER,new StructImpl());
		cfhttp.setEL(STATUSCODE,"Connection Failure. Status code unavailable.");
		cfhttp.setEL(STATUS_CODE,new Double(0));
        cfhttp.setEL(STATUS_TEXT,"Connection Failure");
		cfhttp.setEL(KeyConstants._text,Boolean.TRUE);
	}

	private void setRequestTimeout(Struct cfhttp) {
		cfhttp.setEL(CHARSET,"");
		cfhttp.setEL(ERROR_DETAIL,"");
		cfhttp.setEL(KeyConstants._filecontent,"Connection Timeout");
		cfhttp.setEL(KeyConstants._header,"");
		cfhttp.setEL(KeyConstants._mimetype,"Unable to determine MIME type of file.");
		cfhttp.setEL(RESPONSEHEADER,new StructImpl());
		cfhttp.setEL(STATUSCODE,"408 Request Time-out");
		cfhttp.setEL(STATUS_CODE,new Double(408));
		cfhttp.setEL(STATUS_TEXT,"Request Time-out");
		cfhttp.setEL(KeyConstants._text,Boolean.TRUE);
	}

	/*private static HttpMethod execute(Http http, HttpClient client, HttpMethod httpMethod, boolean redirect) throws PageException {
		try {
			// Execute Request
			short count=0;
	        URL lu;

	        while(isRedirect(client.executeMethod(httpMethod)) && redirect && count++ < MAX_REDIRECT) {
	        	lu=locationURL(httpMethod);
	        	httpMethod=createMethod(http,client,lu.toExternalForm(),-1);
	        }
        }
		catch (IOException e) {
        	PageException pe = Caster.toPageException(e);
			if(pe instanceof NativeException) {
				((NativeException) pe).setAdditional("url", HTTPUtil.toURL(httpMethod));
			}
			throw pe;
        }
		return httpMethod;
	}*/


	/*static URL locationURL(HttpMethod method) throws MalformedURLException, ExpressionException {
        Header location = method.getResponseHeader("location");

        if(location==null) throw new ExpressionException("missing location header definition");


        HostConfiguration config = method.getHostConfiguration();
        URL url;
        try {
            url = new URL(location.getValue());
        }
        catch (MalformedURLException e) {
            url=new URL(config.getProtocol().getScheme(),
                    config.getHost(),
                    config.getPort(),
                    mergePath(method.getPath(),location.getValue()));
        }

        return url;
    }*/


	/*static HttpRequestBase init(Config cw,Http4 http, DefaultHttpClient client, HttpParams params, String url, int port) throws PageException, IOException {
		String charset=http.charset;
		if(StringUtil.isEmpty(charset,true)) charset=cw.getWebCharset();
		else charset=charset.trim();

		HttpRequestBase req;

	// check if has fileUploads
		boolean doUploadFile=false;
		for(int i=0;i<http.params.size();i++) {
			if((http.params.get(i)).getType().equals("file")) {
				doUploadFile=true;
				break;
			}
		}

	// parse url (also query string)
		int len=http.params.size();
		StringBuilder sbQS=new StringBuilder();
		for(int i=0;i<len;i++) {
			HttpParamBean param=http.params.get(i);
			String type=param.getType();
		// URL
			if(type.equals("url")) {
				if(sbQS.length()>0)sbQS.append('&');
				sbQS.append(translateEncoding(param.getName(), charset));
				sbQS.append('=');
				sbQS.append(translateEncoding(param.getValueAsString(), charset));
			}
		}
		String host=null;
		HttpHost httpHost;
		try {
			URL _url = HTTPUtil.toURL(url,port);
			httpHost = new HttpHost(_url.getHost(),_url.getPort());
			host=_url.getHost();
			url=_url.toExternalForm();
			if(sbQS.length()>0){
				// no existing QS
				if(StringUtil.isEmpty(_url.getQuery())) {
					url+="?"+sbQS;
				}
				else {
					url+="&"+sbQS;
				}

			}


		} catch (MalformedURLException mue) {
			throw Caster.toPageException(mue);
		}

	// select best matching method (get,post, post multpart (file))

		boolean isBinary = false;
		boolean doMultiPart=doUploadFile || http.multiPart;
		HttpPost post=null;
		HttpEntityEnclosingRequest eem=null;


		if(http.method==METHOD_GET) {
			req=new HttpGet(url);
		}
		else if(http.method==METHOD_HEAD) {
		    req=new HttpHead(url);
		}
		else if(http.method==METHOD_DELETE) {
			isBinary=true;
		    req=new HttpDelete(url);
		}
		else if(http.method==METHOD_PUT) {
			isBinary=true;
			HttpPut put = new HttpPut(url);
		    req=put;
		    eem=put;

		}
		else if(http.method==METHOD_TRACE) {
			isBinary=true;
		    req=new HttpTrace(url);
		}
		else if(http.method==METHOD_OPTIONS) {
			isBinary=true;
		    req=new HttpOptions(url);
		}
		else {
			isBinary=true;
			post=new HttpPost(url);
			req=post;
			eem=post;
		}

		boolean hasForm=false;
		boolean hasBody=false;
		boolean hasContentType=false;
	// Set http params
		ArrayList<FormBodyPart> parts=new ArrayList<FormBodyPart>();

		StringBuilder acceptEncoding=new StringBuilder();
		java.util.List<NameValuePair> postParam = post!=null?new ArrayList <NameValuePair>():null;

		for(int i=0;i<len;i++) {
			HttpParamBean param=http.params.get(i);
			String type=param.getType();
		// URL
			if(type.equals("url")) {
				//listQS.add(new BasicNameValuePair(translateEncoding(param.getName(), http.charset),translateEncoding(param.getValueAsString(), http.charset)));
			}
		// Form
			else if(type.equals("formfield") || type.equals("form")) {
				hasForm=true;
				if(http.method==METHOD_GET) throw new ApplicationException("httpparam with type formfield can only be used when the method attribute of the parent http tag is set to post");
				if(post!=null){
					if(doMultiPart){
						parts.add(
							new FormBodyPart(
								param.getName(),
								new StringBody(
										param.getValueAsString(),
										CharsetUtil.toCharset(charset)
								)
							)
						);
					}
					else {
						postParam.add(new BasicNameValuePair(param.getName(),param.getValueAsString()));
					}
				}
				//else if(multi!=null)multi.addParameter(param.getName(),param.getValueAsString());
			}
		// CGI
			else if(type.equals("cgi")) {
				if(param.isEncoded())
				    req.addHeader(
                            translateEncoding(param.getName(),charset),
                            translateEncoding(param.getValueAsString(),charset));
                else
                    req.addHeader(param.getName(),param.getValueAsString());
			}
        // Header
            else if(type.startsWith("head")) {
            	if(param.getName().equalsIgnoreCase("content-type")) hasContentType=true;

            	if(param.getName().equalsIgnoreCase("Accept-Encoding")) {
            		acceptEncoding.append(headerValue(param.getValueAsString()));
            		acceptEncoding.append(", ");
            	}
            	else req.addHeader(param.getName(),headerValue(param.getValueAsString()));
            }
		// Cookie
			else if(type.equals("cookie")) {
				HTTPEngine4Impl.addCookie(client,host,param.getName(),param.getValueAsString(),"/",charset);
			}
		// File
			else if(type.equals("file")) {
				hasForm=true;
				if(http.method==METHOD_GET) throw new ApplicationException("httpparam type file can't only be used, when method of the tag http equal post");
				if(doMultiPart) {
					try {
						Resource res = param.getFile();
						parts.add(new FormBodyPart(
								param.getName(),
								new ResourceBody(res, getContentType(param), res.getName(), charset)
						));
						//parts.add(new ResourcePart(param.getName(),new ResourcePartSource(param.getFile()),getContentType(param),_charset));
					}
					catch (FileNotFoundException e) {
						throw new ApplicationException("can't upload file, path is invalid",e.getMessage());
					}
				}
			}
		// XML
			else if(type.equals("xml")) {
				hasBody=true;
				hasContentType=true;
				req.addHeader("Content-type", "text/xml; charset="+charset);
			    if(eem==null)throw new ApplicationException("type xml is only supported for type post and put");
			    HTTPEngine4Impl.setBody(eem, param.getValueAsString());
			}
		// Body
			else if(type.equals("body")) {
				hasBody=true;
				if(eem==null)throw new ApplicationException("type body is only supported for type post and put");
				HTTPEngine4Impl.setBody(eem, param.getValue());

			}
            else {
                throw new ApplicationException("invalid type ["+type+"]");
            }

		}

		// post params
		if(postParam!=null && postParam.size()>0)
			post.setEntity(new org.apache.http.client.entity.UrlEncodedFormEntity(postParam,charset));

		req.setHeader("Accept-Encoding",acceptEncoding.append("gzip").toString());

		// multipart
		if(doMultiPart && eem!=null) {
			hasContentType=true;
			boolean doIt=true;
			if(!http.multiPart && parts.size()==1){
				ContentBody body = parts.get(0).getBody();
				if(body instanceof StringBody){
					StringBody sb=(StringBody)body;
					try {
						String str = IOUtil.toString(sb.getReader());
						StringEntity entity = new StringEntity(str,sb.getMimeType(),sb.getCharset());
						eem.setEntity(entity);

					} catch (IOException e) {
						throw Caster.toPageException(e);
					}
					doIt=false;
				}
			}
			if(doIt) {
				MultipartEntity mpe = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE,null,CharsetUtil.toCharset(charset));
				Iterator<FormBodyPart> it = parts.iterator();
				while(it.hasNext()) {
					mpe.addPart(it.next());
				}
				eem.setEntity(mpe);
			}
				//eem.setRequestEntity(new MultipartRequestEntityFlex(parts.toArray(new Part[parts.size()]), eem.getParams(),http.multiPartType));
		}



		if(hasBody && hasForm)
			throw new ApplicationException("mixing httpparam  type file/formfield and body/XML is not allowed");

		if(!hasContentType) {
			if(isBinary) {
				if(hasBody) req.addHeader("Content-type", "application/octet-stream");
				else req.addHeader("Content-type", "application/x-www-form-urlencoded; charset="+charset);
			}
			else {
				if(hasBody)
					req.addHeader("Content-type", "text/html; charset="+charset );
			}
		}


		// set User Agent
			if(!hasHeaderIgnoreCase(req,"User-Agent"))
				req.setHeader("User-Agent",http.useragent);

	// set timeout
		if(http.timeout>0L)HTTPEngine4Impl.setTimeout(params, (int)http.timeout);

	// set Username and Password
		BasicHttpContext httpContext=null;
		if(http.username!=null) {
			if(http.password==null)http.password="";
			if(AUTH_TYPE_NTLM==http.authType) {
				if(StringUtil.isEmpty(http.workStation,true))
	                throw new ApplicationException("attribute workstation is required when authentication type is [NTLM]");
				if(StringUtil.isEmpty(http.domain,true))
	                throw new ApplicationException("attribute domain is required when authentication type is [NTLM]");

				HTTPEngine4Impl.setNTCredentials(client, http.username, http.password, http.workStation,http.domain);
			}
			else httpContext=HTTPEngine4Impl.setCredentials(client, httpHost, http.username, http.password);
		}

	// set Proxy
		ProxyData proxy=null;
		if(!StringUtil.isEmpty(http.proxyserver)) {
			proxy=ProxyDataImpl.getInstance(http.proxyserver, http.proxyport, http.proxyuser, http.proxypassword) ;
		}
		if(http.pageContext.getConfig().isProxyEnableFor(host)) {
			proxy=http.pageContext.getConfig().getProxyData();
		}
		HTTPEngine4Impl.setProxy(client, req, proxy);

		return req;
	}*/

	private static boolean hasHeaderIgnoreCase(HttpRequestBase req,String name) {
		org.apache.http.Header[] headers = req.getAllHeaders();
		if(headers==null) return false;
		for(int i=0;i<headers.length;i++){
			if(name.equalsIgnoreCase(headers[i].getName())) return true;
		}
		return false;
	}

	private static String headerValue(String value) {
		if(value==null) return null;
		value=value.trim();
		value=value.replace('\n', ' ');
		value=value.replace('\r', ' ');
		/*int len=value.length();
		char c;
		for(int i=0;i<len;i++){
			c=value.charAt(i);
			if(c=='\n' || c=='\r') return value.substring(0,i);
		}*/
		return value;
	}

	private static String toQueryString(NameValuePair[] qsPairs) {
		StringBuffer sb=new StringBuffer();
        for(int i=0;i<qsPairs.length;i++) {
            if(sb.length()>0)sb.append('&');
            sb.append(qsPairs[i].getName());
            if(qsPairs[i].getValue()!=null){
            	sb.append('=');
            	sb.append(qsPairs[i].getValue());
            }
        }
        return sb.toString();
    }

    private static String urlenc(String str, String charset) throws UnsupportedEncodingException {
    	if(!ReqRspUtil.needEncoding(str,false)) return str;
    	return URLEncoder.encode(str,charset);
    }

    @Override
	public void doInitBody()	{

	}

	@Override
	public int doAfterBody()	{
		return SKIP_BODY;
	}

	/**
	 * sets if has body or not
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
        getAsBinary=getAsBinary.toLowerCase().trim();
        if(getAsBinary.equals("yes") || getAsBinary.equals("true")) 		this.getAsBinary=GET_AS_BINARY_YES;
        else if(getAsBinary.equals("no") || getAsBinary.equals("false")) 	this.getAsBinary=GET_AS_BINARY_NO;
        else if(getAsBinary.equals("auto")) 								this.getAsBinary=GET_AS_BINARY_AUTO;
    }

    /**
     * @param multipart The multipart to set.
     */
    public void setMultipart(boolean multiPart) {
        this.multiPart = multiPart;
    }

    /**
     * @param multipart The multipart to set.
     * @throws ApplicationException
     */
    public void setMultiparttype(String multiPartType) throws ApplicationException {
    	if(StringUtil.isEmpty(multiPartType))return;
    	multiPartType=multiPartType.trim().toLowerCase();

    	if("form-data".equals(multiPartType)) 	this.multiPartType=MULTIPART_FORM_DATA;
    	else
			throw new ApplicationException("invalid value for attribute multiPartType ["+multiPartType+"]",
					"attribute must have one of the following values [form-data]");

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
     * checks if status code is a redirect
     * @param status
     * @return is redirect
     */

	static boolean isRedirect(int status) {
    	return
        	status==STATUS_REDIRECT_FOUND ||
        	status==STATUS_REDIRECT_MOVED_PERMANENTLY ||
        	status==STATUS_REDIRECT_SEE_OTHER ||
        	status==STATUS_REDIRECT_TEMPORARY_REDIRECT;


    }

    /**
     * merge to pathes to one
     * @param current
     * @param realPath
     * @return
     * @throws MalformedURLException
     */
    public static String mergePath(String current, String realPath) throws MalformedURLException {

        // get current directory
        String currDir;
        if(current==null || current.indexOf('/')==-1)currDir="/";
        else if(current.endsWith("/"))currDir=current;
        else currDir=current.substring(0,current.lastIndexOf('/')+1);

        // merge together
        String path;
        if(realPath.startsWith("./"))path=currDir+realPath.substring(2);
        else if(realPath.startsWith("/"))path=realPath;
        else if(!realPath.startsWith("../"))path=currDir+realPath;
        else {
            while(realPath.startsWith("../") || currDir.length()==0) {
                realPath=realPath.substring(3);
                currDir=currDir.substring(0,currDir.length()-1);
                int index = currDir.lastIndexOf('/');
                if(index==-1)throw new MalformedURLException("invalid realpath definition for URL");
                currDir=currDir.substring(0,index+1);
            }
            path=currDir+realPath;
        }

        return path;
    }

	private static String getContentType(HttpParamBean param) {
		String mimeType=param.getMimeType();
		if(StringUtil.isEmpty(mimeType,true)) {
			mimeType=ResourceUtil.getMimeType(param.getFile(), ResourceUtil.MIMETYPE_CHECK_EXTENSION+ResourceUtil.MIMETYPE_CHECK_HEADER, null);
		}
		return mimeType;
	}

	public static boolean isGzipEncoded(String contentEncoding) {
		return !StringUtil.isEmpty(contentEncoding) && StringUtil.indexOfIgnoreCase(contentEncoding, "gzip")!=-1;
	}

	public static Object getOutput(InputStream is, String contentType, String contentEncoding, boolean closeIS) {
		if(StringUtil.isEmpty(contentType))contentType="text/html";

		// Gzip
		if(Http.isGzipEncoded(contentEncoding)){
			try {
				is=new GZIPInputStream(is);
			}
			catch (IOException e) {}
		}

		try {
			// text
			if(HTTPUtil.isTextMimeType(contentType)) {
				String[] tmp = HTTPUtil.splitMimeTypeAndCharset(contentType,null);
				Charset cs=Http.getCharset(tmp[1]);

				try {
					return IOUtil.toString(is, cs);
				} catch (IOException e) {}
			}
			// Binary
			else {
				try {
					return IOUtil.toBytes(is);
				}
				catch (IOException e) {}
			}
		}
		finally{
			if(closeIS)IOUtil.closeEL(is);
		}

		return "";
	}

	public static URL locationURL(HttpUriRequest req, HttpResponse rsp) {
		URL url=null;
		try {
			url = req.getURI().toURL();
		} catch (MalformedURLException e1) {
			return null;
		}

		Header h = HTTPResponse4Impl.getLastHeaderIgnoreCase(rsp, "location");
		if(h!=null) {
			String str = h.getValue();
			try {
				return new URL(str);
			} catch (MalformedURLException e) {
				try {
					return new URL(url.getProtocol(), url.getHost(), url.getPort(), mergePath(url.getFile(), str));

				} catch (MalformedURLException e1) {
					return null;
				}
			}
		}
		return null;
	}

	public static Charset getCharset(String strCharset) {
		if(!StringUtil.isEmpty(strCharset,true))
			return CharsetUtil.toCharset(strCharset);
		return CharsetUtil.getWebCharset();
	}

	public static void setTimeout(HttpClientBuilder builder, TimeSpan timeout) {
		if(timeout==null || timeout.getMillis()<=0) return;

		int ms=(int)timeout.getMillis();
		if(ms<0)ms=Integer.MAX_VALUE;

		//builder.setConnectionTimeToLive(ms, TimeUnit.MILLISECONDS);
    	SocketConfig sc=SocketConfig.custom()
    			.setSoTimeout(ms)
    			.build();
    	builder.setDefaultSocketConfig(sc);
	}

}

class Executor4 extends PageContextThread {

	 final Http http;
	 private final CloseableHttpClient client;
	 final boolean redirect;
	 Throwable t;
	 boolean done;
	//URL redirectURL;
	HTTPResponse4Impl response;
	private HttpRequestBase req;
	private HttpContext context;

	public Executor4(PageContext pc,Http http,CloseableHttpClient client, HttpContext context, HttpRequestBase req, boolean redirect) {
		super(pc);
		this.http=http;
		this.client=client;
		this.context=context;
		this.redirect=redirect;
		this.req=req;
	}

	@Override
	public void run(PageContext pc) {
		try {
			response=execute(context);
			done=true;
		}
		catch(Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			this.t=t;
		}
		finally {
			SystemUtil.notify(http);
		}
	}

	public HTTPResponse4Impl execute(HttpContext context) throws IOException	{
		return response=new HTTPResponse4Impl(null,context,req,client.execute(req,context));
	}


}