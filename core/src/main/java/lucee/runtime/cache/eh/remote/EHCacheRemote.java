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
package lucee.runtime.cache.eh.remote;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.rpc.ServiceException;

import lucee.commons.io.cache.CacheEntry;
import lucee.commons.io.cache.CachePro;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.cache.CacheSupport;
import lucee.runtime.cache.eh.remote.rest.RESTClient;
import lucee.runtime.cache.eh.remote.rest.sax.CacheConfiguration;
import lucee.runtime.cache.eh.remote.soap.Element;
import lucee.runtime.cache.eh.remote.soap.SoapClient;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.util.Cast;

public class EHCacheRemote extends CacheSupport {

	private URL url;
	private String name;
	private RESTClient rest;
	private SoapClient soap;


	public EHCacheRemote() {	
	}
	
	public static void init(ConfigWeb config,String[] cacheNames,Struct[] arguments) {
		
	}

	@Override
	public void init(Config config,String name, Struct arguments) throws IOException {
		Cast caster = CFMLEngineFactory.getInstance().getCastUtil();
		String strUrl=null;
		
		try {
			strUrl=caster.toString(arguments.get("url"));
			this.name=caster.toString(arguments.get("remoteCacheName"));
			
		} catch (PageException e) {
			throw new IOException(e.getMessage());
		}
		if(!strUrl.endsWith("/")){
			strUrl=strUrl+"/";
		}
		this.url=new URL(strUrl);
		
		
		
		
		this.rest=new RESTClient(new URL(url.toExternalForm()+"rest/"));
		this.soap=new SoapClient(new URL(url.toExternalForm()+"soap/EhcacheWebServiceEndpoint?wsdl"));
	}

	@Override
	public boolean contains(String key) {
		try {
			return rest.contains(name, key);
		} catch (IOException e) {
			return false;
		}
	}
	

	@Override
	public CachePro decouple() {
		// is already decoupled by default
		return this;
	}
	
	
	@Override
	public List keys() {
		try {
			return soap.getKeysWithExpiryCheck(name);
		} 
		catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	@Override
	public CacheEntry getQuiet(String key) throws IOException {
		try {
			return soap.getQuiet(name, key);
		} 
		catch (ServiceException e) {
			throw new IOException(e.getMessage());
		}
	}
	

	@Override
	public CacheEntry getQuiet(String key,CacheEntry defaultValue) {
		try {
			return soap.getQuiet(name, key);
		} 
		catch (Throwable t) {
			return defaultValue;
		}
	}

	@Override
	public CacheEntry getCacheEntry(String key) throws IOException {
		try {
			return soap.get(name, key);
		} 
		catch (ServiceException e) {
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public CacheEntry getCacheEntry(String key,CacheEntry defaultValue) {
		try {
			return soap.get(name, key);
		} 
		catch (Throwable t) {
			return defaultValue;
		}
	}

	

	@Override
	public Struct getCustomInfo() {
		Struct info=super.getCustomInfo();
		try	{
			CacheConfiguration conf = rest.getMeta(name).getCacheConfiguration();
			
			info.setEL("disk_expiry_thread_interval", new Double(conf.getDiskExpiryThreadIntervalSeconds()));
			info.setEL("disk_spool_buffer_size", new Double(conf.getDiskSpoolBufferSize()));
			info.setEL("max_elements_in_memory", new Double(conf.getMaxElementsInMemory()));
			info.setEL("max_elements_on_disk", new Double(conf.getMaxElementsOnDisk()));
			info.setEL("time_to_idle", new Double(conf.getTimeToIdleSeconds()));
			info.setEL("time_to_live", new Double(conf.getTimeToLiveSeconds()));
			info.setEL(KeyConstants._name, conf.getName());
		}
		catch(Throwable t){
			//print.printST(t);
		}
		
		return info;
	}


	@Override
	public long hitCount() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public long missCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void put(String key, Object value, Long idleTime, Long liveTime) {
		Boolean eternal = idleTime==null && liveTime==null?Boolean.TRUE:Boolean.FALSE;
		Integer idle = idleTime==null?null:new Integer((int)idleTime.longValue()/1000);
		Integer live = liveTime==null?null:new Integer((int)liveTime.longValue()/1000);
		try {
			Element el = new Element();
			el.setKey(key);
			// TODO make text/plain for string
			el.setMimeType("application/x-java-serialized-object");
			el.setValue(Converter.toBytes(value));
			el.setEternal(eternal);
			el.setTimeToIdleSeconds(idle);
			el.setTimeToLiveSeconds(live);
		
			soap.put(name,el);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
		
	}

	@Override
	public boolean remove(String key) {
		try {
			return soap.remove(name, key);
		} 
		catch (Exception e) {
			return false;
		} 
	}
	
	@Override
	public int clear() throws IOException {
			return soap.clear(name);
	}


}