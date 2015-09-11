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
package lucee.runtime.cache.eh.remote.soap;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.encoding.TypeMapping;

import lucee.commons.io.cache.CacheEntry;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.cache.eh.remote.Converter;
import lucee.runtime.cache.eh.remote.rest.RESTClient;
import lucee.runtime.net.rpc.TypeMappingUtil;
import lucee.runtime.util.Cast;

import org.apache.axis.Constants;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;

public class SoapClient {
	//private static QName bool2 = Constants.XSD_BOOLEAN;//new QName("http://www.w3.org/2001/XMLSchema", "boolean");
	private static QName string2 = Constants.XSD_STRING;//new QName("http://www.w3.org/2001/XMLSchema", "string");
	
	private static QName element = new QName("http://soap.server.ehcache.sf.net/", "element");
	private static QName cache = new QName("http://soap.server.ehcache.sf.net/", "cache");
	private static QName cacheConfiguration = new QName("http://soap.server.ehcache.sf.net/", "cacheConfiguration");
	
	
	private String endpoint;

	public SoapClient(URL endpoint) {
		this.endpoint=endpoint.toExternalForm();
	}

	public static void main(String [] args) throws Exception {
		 RESTClient.main(null);
		 
		 SoapClient client = new SoapClient(new URL("http://localhost:8181/soap/EhcacheWebServiceEndpoint?wsdl"));
		 
		 Element e = new Element();
		 e.setEternal(Boolean.TRUE);
		 e.setExpirationDate(new Long(new Date().getTime()+1000000));
		 e.setKey("lami");
		 e.setMimeType("application/x-java-serialized-object");
		 e.setValue(Converter.toBytes("Lama"));
		 e.setTimeToIdleSeconds(new Integer(10000));
		 e.setTimeToLiveSeconds(new Integer(10000));
		 //e.setResourceUri(resourceUri);
		 
		 client.put("susi", e);
		 client.putQuiet("susi", e);
		 
	 }
	
	public Cache getCache(String cacheName) throws Exception {
		
       Service  service = new Service();
       TypeMapping tm = service.getTypeMappingRegistry().getDefaultTypeMapping();
       TypeMappingUtil.registerBeanTypeMapping(tm, CacheConfiguration.class, cacheConfiguration);
       TypeMappingUtil.registerBeanTypeMapping(tm, Cache.class, cache);
       
       
      Call     call    = (Call) service.createCall();
       
       
       call.registerTypeMapping(
               Cache.class, 
               cache,
               BeanSerializerFactory.class,
               BeanDeserializerFactory.class);
       
       
        
       call.registerTypeMapping(
               CacheConfiguration.class, 
               cacheConfiguration,
               BeanSerializerFactory.class,
               BeanDeserializerFactory.class);
       
       call.setTargetEndpointAddress( new java.net.URL(endpoint) );
       call.setOperationName(new QName("http://soap.server.ehcache.sf.net/", "getCache"));

       call.addParameter("arg0", Constants.XSD_STRING, String.class, ParameterMode.IN);
       call.setReturnClass(Element.class);
       call.setReturnQName(cache);
       
       
       
       //Object ret =  call.invoke( new Object[] { } );
       return (Cache) call.invoke( new Object[] {cacheName } );
   }
	
	public List getKeysWithExpiryCheck(String cacheName) throws MalformedURLException, RemoteException, ServiceException {
		Object raw=getKeysWithExpiryCheckRaw(cacheName);
		if(raw==null) return new ArrayList();
		else if(raw instanceof String) {
			List list=new ArrayList();
			list.add(raw);
			return list;
		}
		else if(raw instanceof List) {
			return (List) raw;
		}
		
		Cast caster = CFMLEngineFactory.getInstance().getCastUtil();
		return caster.toList(raw,null);
	}
	
	private Object getKeysWithExpiryCheckRaw(String cacheName) throws MalformedURLException, RemoteException, ServiceException {
		Service  service = new Service();
		Call     call    = (Call) service.createCall();
		QName any = new QName("http://www.w3.org/2001/XMLSchema", "anyType[0,unbounded]");
		QName string = new QName("http://www.w3.org/2001/XMLSchema", "string");
       
       
       
       call.setTargetEndpointAddress( new java.net.URL(endpoint) );
       call.setOperationName(new QName("http://soap.server.ehcache.sf.net/", "getKeysWithExpiryCheck"));

       call.addParameter("arg0", string, String.class, ParameterMode.IN);
       call.setReturnType(any);
       return call.invoke( new Object[] {cacheName } );
   }
	

	public CacheEntry get(String cacheName,String key) throws MalformedURLException, RemoteException, ServiceException {
		return _get(cacheName, "get", key);
	}
	
	public CacheEntry getQuiet(String cacheName,String key) throws MalformedURLException, RemoteException, ServiceException {
		return _get(cacheName, "getQuiet", key);
	}
	
	
	private CacheEntry _get(String cacheName,String method,String key) throws ServiceException, MalformedURLException, RemoteException  {
    	Service  service = new Service();
        Call     call    = (Call) service.createCall();
        
        call.registerTypeMapping(
                Element.class, 
                element,
                BeanSerializerFactory.class,
                BeanDeserializerFactory.class);
        
        call.setTargetEndpointAddress( new java.net.URL(endpoint) );
        call.setOperationName(new QName("http://soap.server.ehcache.sf.net/", method));

        call.addParameter("arg0", Constants.XSD_STRING, String.class, ParameterMode.IN);
        call.addParameter("arg1", Constants.XSD_STRING, String.class, ParameterMode.IN);
        call.setReturnClass(Element.class);
        call.setReturnQName(element);
        
        return new SoapCacheEntry((Element) call.invoke( new Object[] {cacheName,key } ));
    }
	
	public boolean remove(String cacheName,String key) throws MalformedURLException, RemoteException, ServiceException {
		return _remove(cacheName, "remove", key);
	}
	
	public boolean removeQuiet(String cacheName,String key) throws MalformedURLException, RemoteException, ServiceException {
		return _remove(cacheName, "removeQuiet", key);
	}
	
	
	private boolean _remove(String cacheName,String method,String key) throws ServiceException, MalformedURLException, RemoteException  {
    	Service  service = new Service();
        Call     call    = (Call) service.createCall();
        
        
        call.registerTypeMapping(
                Element.class, 
                element,
                BeanSerializerFactory.class,
                BeanDeserializerFactory.class);
        
        call.setTargetEndpointAddress( new java.net.URL(endpoint) );
        call.setOperationName(new QName("http://soap.server.ehcache.sf.net/", method));

        call.addParameter("arg0", Constants.XSD_STRING, String.class, ParameterMode.IN);
        call.addParameter("arg1", Constants.XSD_STRING, String.class, ParameterMode.IN);
        call.setReturnClass(boolean.class);
        call.setReturnQName(Constants.XSD_BOOLEAN);
      
        return ((Boolean)call.invoke( new Object[] {cacheName,key } )).booleanValue();

    }
	

	
	public void put(String cacheName,Element element) throws MalformedURLException, RemoteException, ServiceException {
		_put(cacheName, "put", element);
	}
	
	public void putQuiet(String cacheName,Element element) throws MalformedURLException, RemoteException, ServiceException {
		_put(cacheName, "putQuiet", element);
	}
	
	
	private void _put(String cacheName,String method,Element el) throws ServiceException, MalformedURLException, RemoteException  {
    	Service  service = new Service();
        Call     call    = (Call) service.createCall();
        
        el.setResourceUri(endpoint);
        
        call.registerTypeMapping(
                Element.class, 
                element,
                BeanSerializerFactory.class,
                BeanDeserializerFactory.class);
        
        call.setTargetEndpointAddress( new java.net.URL(endpoint) );
        call.setOperationName(new QName("http://soap.server.ehcache.sf.net/", method));
        
        
        call.addParameter("arg0", Constants.XSD_STRING, String.class, ParameterMode.IN);
        call.addParameter("arg1", element, Element.class, ParameterMode.IN);
        call.setReturnType(Constants.XSD_ANYSIMPLETYPE);
        
        call.invoke( new Object[] {cacheName,el } );
        //call.invokeOneWay(new Object[] {cacheName,el } );
    }

	public int clear(String name) {
		// MUST implement
		throw new RuntimeException("not supported!");
	}
	
}