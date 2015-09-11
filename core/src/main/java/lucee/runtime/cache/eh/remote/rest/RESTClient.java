/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 **/
package lucee.runtime.cache.eh.remote.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import lucee.commons.io.IOUtil;
import lucee.commons.io.cache.CacheEntry;
import lucee.runtime.cache.eh.remote.Converter;
import lucee.runtime.cache.eh.remote.rest.sax.CacheFactory;
import lucee.runtime.cache.eh.remote.rest.sax.CacheMeta;

import org.xml.sax.SAXException;

public class RESTClient {
	
	private URL url;
	private String strUrl;
	
	public RESTClient(URL url) {
		this.url=url;
		this.strUrl=url.toExternalForm();
		if(!strUrl.endsWith("/")){
			strUrl+="/";
			try {
				url=new URL(strUrl);
			} catch (MalformedURLException e) {}
		}
		
	}
	
	public Object getMetaRaw(String cacheName) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) toURL(cacheName).openConnection();
	    connection.setRequestMethod("GET");
	    connection.setRequestProperty("id", "getKeysWithExpiryCheck");
	    connection.connect();
	    try	{
	    	return getContent(connection);
		} 
    	finally	{
    		disconnectEL(connection);
    	}
	}	
	
	public CacheMeta getMeta(String cacheName) throws IOException, SAXException {
		HttpURLConnection connection = (HttpURLConnection) toURL(cacheName).openConnection();
	    connection.setRequestMethod("GET");
	    connection.setRequestProperty("id", "getKeysWithExpiryCheck");
	    connection.connect();
	    InputStream is=null;
	    try	{
	    	is=connection.getInputStream();
	    	return new CacheFactory(is).getMeta();
		} 
    	finally	{
    		IOUtil.closeEL(is);
    		disconnectEL(connection);
    	}
	}	
	
	public CacheEntry getMeta2(String cacheName) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) toURL(cacheName).openConnection();
	    connection.setRequestMethod("HEAD");
	    connection.connect();
	    InputStream is=null;
	    try	{
	    	//is=connection.getInputStream();
	    	//obj=getContent(connection);
	    	//CacheFactory cf = new CacheFactory(is);
	    	
		} 
    	finally	{
    		IOUtil.closeEL(is);
    		disconnectEL(connection);
    	}
    	return null;
	}	

	/*private Object listCachesRaw() throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		Object obj;
        conn.setRequestMethod("GET");
        try{
        	obj = getContent(conn);
        }
        finally{
        	disconnectEL(conn);
        }
        return obj;
	}*/
	
	/*private Object featuresRaw() throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		Object obj;
        conn.setRequestMethod("OPTIONS");
        try{
        	obj = getContent(conn);
        }
        finally{
        	disconnectEL(conn);
        }
        return obj;
	}*/
	
	/*private void createCache(String cacheName) throws IOException {
		HttpURLConnection urlConnection = (HttpURLConnection) toURL(cacheName).openConnection();
        urlConnection.setRequestMethod("PUT");

        urlConnection.getResponseCode();
        urlConnection.disconnect();
	}*/
	
	private void createEntry(String cacheName,String key, String value) throws IOException {

	    HttpURLConnection connection = (HttpURLConnection) toURL(cacheName,key).openConnection();
	    connection.setRequestProperty("Content-Type", "application/x-java-serialized-object");
	    connection.setDoOutput(true);
	    connection.setRequestMethod("PUT");
	    connection.connect();
	    
	 // Write the message to the servlet
	    
	      OutputStream os = connection.getOutputStream(); // returns
	      ObjectOutputStream oos = new ObjectOutputStream(os);
	      oos.writeObject(value);
	      oos.flush();
	      
	    connection.disconnect();
	}

	/*private void removeEntry(String cacheName,String key) throws IOException {
		
	    HttpURLConnection connection = (HttpURLConnection) toURL(cacheName,key).openConnection();
	    connection.setRequestProperty("Content-Type", "application/x-java-serialized-object");
	    connection.setDoOutput(true);
	    connection.setRequestMethod("DELETE");
	    connection.connect();
	    
	    disconnectEL(connection);
	    
	}*/

	

	public boolean contains(String cacheName,String key) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) toURL(cacheName, key).openConnection();
	    connection.setRequestMethod("HEAD");
	    connection.connect();
	    try{
	    	return connection.getResponseCode()==200;
	    }
	    finally{
	    	disconnectEL(connection);
	    }
	}
	
	
	public Object getValue(String cacheName,String key) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) toURL(cacheName, key).openConnection();
	    connection.setRequestMethod("GET");
	    connection.connect();
	    try{
	    return getContent(connection);
	    }
	    finally{
	    	disconnectEL(connection);
	    }
	}
	
	


	public CacheEntry getEntry(String cacheName,String key) throws IOException {
		Object obj=null;
		HttpURLConnection connection = (HttpURLConnection) toURL(cacheName, key).openConnection();
	    connection.setRequestMethod("GET");
	    connection.connect();
	    try	{
	    	connection.getContentLength();
	    	connection.getHeaderField("Expires");
	    	connection.getHeaderField("Last-Modified");
	    	
	    	obj=getContent(connection);
	    	
		}
    	finally	{
    		disconnectEL(connection);
    	}
	    return new RESTCacheEntry(key,obj);
	}	

	
	private static Object getContent(HttpURLConnection conn) {
		InputStream is=null;
		try	{
			return Converter.toObject(conn.getContentType(),conn.getInputStream());
		}
	    catch(Exception e){
	    	return null;
	    }
    	finally	{
    		IOUtil.closeEL(is);
    	}
		
	}
	
	
	private static void disconnectEL(HttpURLConnection connection) {
		if(connection!=null){
			connection.disconnect();
		}
	}


	private URL toURL(String cacheName, String key) {
		try{
			return new URL(strUrl+cacheName+"/"+key);
		}
		catch(MalformedURLException e){
			return null;
		}
	}


	private URL toURL(String cacheName) {
		try{
			return new URL(strUrl+cacheName);
		}
		catch(MalformedURLException e){
			return null;
		}
	}
	
	public static void main(String[] args) throws Exception {
		RESTClient client81 = new RESTClient(new URL("http://localhost:8181/rest/"));
		//client81.createCache("sample");
		client81.createEntry("sample", "mx81","81 "+new Date());
		
		RESTClient client82 = new RESTClient(new URL("http://localhost:8282/rest/"));
		//client82.createCache("sample");
		client82.createEntry("sample", "mx82","82 "+new Date());
		
		
		/*
		RESTClient client82 = new RESTClient(new URL("http://localhost:8282/rest/"));
		client82.createCache("sample");
		client82.createEntry("sample", "resti","RESTFull82");
		*/
		
		
		
		
	}

}