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
package lucee.commons.net.http.httpclient3;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lucee.commons.io.TemporaryStream;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.Entity;
import lucee.commons.net.http.HTTPEngine;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.Header;
import lucee.commons.net.http.httpclient3.entity.EmptyRequestEntity;
import lucee.commons.net.http.httpclient3.entity.ResourceRequestEntity;
import lucee.commons.net.http.httpclient3.entity.TemporaryStreamRequestEntity;
import lucee.commons.net.http.httpclient3.entity._ByteArrayRequestEntity;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.util.CollectionUtil;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.http.entity.ContentType;

/**
 * 
 */
public final class HTTPEngine3Impl {
	

    
    public static HTTPResponse get(URL url, String username, String password, long timeout, int maxRedirect,
        String charset, String useragent,ProxyData proxy, Header[] headers) throws IOException {
    	return _invoke(new GetMethod(url.toExternalForm()), url, username, password, timeout,maxRedirect, null,charset, useragent, proxy, headers,null,null);
    }
    
    public static HTTPResponse head(URL url, String username, String password, int timeout, int maxRedirect,
        String charset, String useragent,ProxyData proxy, Header[] headers) throws IOException {
		return _invoke(new HeadMethod(url.toExternalForm()), url, username, password, timeout,maxRedirect, null, charset, useragent, proxy, headers,null,null);
	}
    
    public static HTTPResponse post(URL url, String username, String password, long timeout, int maxRedirect,
        String charset, String useragent, ProxyData proxy, Header[] headers, Map<String,String> params) throws IOException {
    	return _invoke(new PostMethod(url.toExternalForm()), url, username, password, timeout,maxRedirect, null,charset, useragent, proxy, headers,params,null);
    }
    
	public static HTTPResponse put(URL url, String username, String password, int timeout, int maxRedirect,
        String mimetype, String charset, String useragent,ProxyData proxy, Header[] headers, Object body) throws IOException {
		return _invoke(new PutMethod(url.toExternalForm()), url, username, password, timeout,maxRedirect, mimetype,charset, useragent, proxy, headers,null,body);     
	}
    
    public static HTTPResponse delete(URL url, String username, String password, int timeout, int maxRedirect,
        String charset, String useragent,ProxyData proxy, Header[] headers) throws IOException {
    	return _invoke(new DeleteMethod(url.toExternalForm()), url, username, password, timeout,maxRedirect, null, charset, useragent, proxy, headers,null,null);
	}
    

	private static HTTPResponse _invoke(HttpMethod httpMethod, URL url, String username, String password, long timeout, int maxRedirect,
            String mimetype, String charset, String useragent, ProxyData proxy, Header[] headers, Map<String,String> params, Object body) throws IOException {

        HttpClient client = new HttpClient();
        HostConfiguration config = client.getHostConfiguration();
        HttpState state = client.getState();
        
        setHeader(httpMethod,headers);
        if(CollectionUtil.isEmpty(params))setContentType(httpMethod,charset);
        setUserAgent(httpMethod,useragent);
        setTimeout(client,timeout);
        setParams(httpMethod,params);
        setCredentials(client,httpMethod,username,password);  
        setProxy(config,state,proxy);
        if(body!=null && httpMethod instanceof EntityEnclosingMethod)setBody((EntityEnclosingMethod)httpMethod,body,mimetype,charset);
        return new HTTPResponse3Impl(execute(client,httpMethod,maxRedirect),url);
    }

	/**
     * Execute a HTTTP Client and follow redirect over different hosts
     * @param client
     * @param method
     * @param doRedirect
     * @return
     * @throws IOException
     * @throws HttpException
     */
    public static HttpMethod execute(HttpClient client, HttpMethod method, int maxRedirect) throws HttpException, IOException {
    	short count=0;
        method.setFollowRedirects(false);
        
        while(isRedirect(client.executeMethod(method)) && count++ < maxRedirect) {
        	method=rewrite(method);
        }
        return method;
    }

    /**
     * rewrite request method
     * @param method
     * @return
     * @throws MalformedURLException
     */
    private static HttpMethod rewrite(HttpMethod method) throws MalformedURLException {
        org.apache.commons.httpclient.Header location = method.getResponseHeader("location");
        if(location==null) return method;

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
        
        method= clone(method,url);
        
        return method;
    }

    /**
     * FUNKTIONIERT NICHT, HOST WIRD NICHT UEBERNOMMEN
     * Clones a http method and sets a new url
     * @param src
     * @param url
     * @return
     */
    private static HttpMethod clone(HttpMethod src, URL url) {
        HttpMethod trg = HttpMethodCloner.clone(src);
        HostConfiguration trgConfig = trg.getHostConfiguration();
        trgConfig.setHost(url.getHost(),url.getPort(),url.getProtocol());
        trg.setPath(url.getPath());
        trg.setQueryString(url.getQuery());
        
        return trg;
    }
    
    /**
     * merge to pathes to one
     * @param current
     * @param realPath
     * @return
     * @throws MalformedURLException
     */
    private static String mergePath(String current, String realPath) throws MalformedURLException {
        
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

    /**
     * checks if status code is a redirect
     * @param status
     * @return is redirect
     */
    private static boolean isRedirect(int status) {
        return 
        	status==HTTPEngine.STATUS_REDIRECT_FOUND || 
        	status==HTTPEngine.STATUS_REDIRECT_MOVED_PERMANENTLY ||
        	status==HTTPEngine.STATUS_REDIRECT_SEE_OTHER;
    }

    

    private static void setBody(EntityEnclosingMethod httpMethod, Object body, String mimetype,String charset) throws IOException {
        if(body!=null)
			try {
				httpMethod.setRequestEntity(toRequestEntity(body,mimetype,charset));
			} catch (PageException e) {
				throw ExceptionUtil.toIOException(e);
			}
	}

	private static void setProxy(HostConfiguration config, HttpState state, ProxyData data) {
        // set Proxy
            if(ProxyDataImpl.isValid(data)) {
                config.setProxy(data.getServer(),data.getPort()<=0?80:data.getPort());
                if(ProxyDataImpl.hasCredentials(data)) {
                    state.setProxyCredentials(null,null,new UsernamePasswordCredentials(data.getUsername(),StringUtil.emptyIfNull(data.getPassword())));
                }
            } 
	}

	private static void setCredentials(HttpClient client, HttpMethod httpMethod, String username,String password) {
        // set Username and Password
            if(username!=null) {
                if(password==null)password="";
                client.getState().setCredentials(null,null,new UsernamePasswordCredentials(username, password));
                httpMethod.setDoAuthentication( true );
            }
	}

	private static void setTimeout(HttpClient client, long timeout) {
        if(timeout>0){
        	client.setConnectionTimeout((int)timeout);
        	client.setTimeout((int)timeout);
        }
	}

	private static void setUserAgent(HttpMethod httpMethod, String useragent) {
        if(useragent!=null)httpMethod.setRequestHeader("User-Agent",useragent);
	}

	private static void setContentType(HttpMethod httpMethod, String charset) {
    	if(charset!=null)httpMethod.addRequestHeader("Content-type", "text/html; charset="+charset );
	}

	private static void setHeader(HttpMethod httpMethod,Header[] headers) {
    	if(headers!=null) {
        	for(int i=0;i<headers.length;i++)
        		httpMethod.addRequestHeader(headers[i].getName(), headers[i].getValue());
        }
	}
	
    private static void setParams(HttpMethod httpMethod, Map<String, String> params) {
		if(params==null || !(httpMethod instanceof PostMethod)) return;
    	PostMethod post=(PostMethod) httpMethod;
    	Iterator<Entry<String, String>> it = params.entrySet().iterator();
    	Entry<String, String> e;
    	while(it.hasNext()){
    		e = it.next();
    		post.addParameter(new NameValuePair(e.getKey(),e.getValue()));
    	}
	}

	private static RequestEntity toRequestEntity(Object value, String mimetype, String charset) throws PageException {
    	if(value instanceof RequestEntity) return (RequestEntity) value;
    	
    	ContentType ct=HTTPEngine.toContentType(mimetype, charset);
    	
    	if(value instanceof InputStream) {
    		if(ct==null) ct=ContentType.APPLICATION_OCTET_STREAM;
			return new InputStreamRequestEntity((InputStream)value,ct.toString());
		}
		else if(Decision.isCastableToBinary(value,false)){
			if(ct==null)
				return new ByteArrayRequestEntity(Caster.toBinary(value));
			return new ByteArrayRequestEntity(Caster.toBinary(value),ct.toString());
		}
		else {
			if(ct==null)
				ct=ContentType.APPLICATION_OCTET_STREAM;
			String str = Caster.toString(value);
			if(str.equals("<empty>")) {
				//String contentType=str.substring(7, str.length()-1);
				return new EmptyRequestEntity(ct.toString());
			}
			try {
				return new StringRequestEntity(str,ct.getMimeType(),ct.getCharset()==null?null:ct.getCharset().name());
			}
			catch(UnsupportedEncodingException e) {
				throw Caster.toPageException(e);
			}
		}
    }
	

	public static Header header(String name, String value) {
		return new HeaderImpl(name, value);
	}

	public static Entity getEmptyEntity(String contentType) {
		return new EmptyRequestEntity(contentType);
	}
		
	public static Entity getByteArrayEntity(byte[] barr, String contentType) {
		return new _ByteArrayRequestEntity(barr, contentType);
	}
	
	public static Entity getTemporaryStreamEntity(TemporaryStream ts, String contentType) {
		return new TemporaryStreamRequestEntity(ts,contentType);
	}
	
	public static Entity getResourceEntity(Resource res, String contentType) {
		return new ResourceRequestEntity(res,contentType);
	}
}