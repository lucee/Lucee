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
package lucee.runtime.cache.eh;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.cache.CacheEntry;
import lucee.commons.io.cache.exp.CacheException;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ResourceNameFilter;
import lucee.commons.lang.ExceptionUtil;
import lucee.loader.util.Util;
import lucee.runtime.config.Config;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;

public class EHCache extends EHCacheSupport {
	
	private static final boolean DISK_PERSISTENT = true;
	private static final boolean ETERNAL = false;
	private static final int MAX_ELEMENTS_IN_MEMEORY = 10000;
	private static final int MAX_ELEMENTS_ON_DISK = 10000000;
	private static final String MEMORY_EVICTION_POLICY = "LRU";
	private static final boolean OVERFLOW_TO_DISK = true;
	private static final long TIME_TO_IDLE_SECONDS = 86400; 
	private static final long TIME_TO_LIVE_SECONDS = 86400;
	
	private static final boolean REPLICATE_PUTS = true;
	private static final boolean REPLICATE_PUTS_VIA_COPY = true;
	private static final boolean REPLICATE_UPDATES = true;
	private static final boolean REPLICATE_UPDATES_VIA_COPY = true;
	private static final boolean REPLICATE_REMOVALS = true;
	private static final boolean REPLICATE_ASYNC = true;
	private static final int ASYNC_REP_INTERVAL = 1000; 
	//private static Map managers=new HashMap();
	private static Map<String,CacheManagerAndHash> managers=new HashMap<String,CacheManagerAndHash>();
	
	//private net.sf.ehcache.Cache cache;
	private int hits;
	private int misses;
	private String cacheName;
	private CacheManager manager;
	private ClassLoader classLoader;


	public static void init(Config config,String[] cacheNames,Struct[] arguments) throws IOException {
		System.setProperty("net.sf.ehcache.enableShutdownHook", "true");
		Thread.currentThread().setContextClassLoader(config.getClassLoader());

		Resource dir = config.getConfigDir().getRealResource("ehcache"),hashDir;
		if(!dir.isDirectory())dir.createDirectory(true);
		String[] hashArgs=createHash(arguments);
		
		// create all xml
		HashMap<String,String> mapXML = new HashMap<String,String>();
		HashMap<String,CacheManagerAndHash> newManagers = new HashMap<String,CacheManagerAndHash>();
		for(int i=0;i<hashArgs.length;i++){
			if(mapXML.containsKey(hashArgs[i])) continue;
			
			hashDir=dir.getRealResource(hashArgs[i]);
			String xml=createXML(hashDir.getAbsolutePath(), cacheNames,arguments,hashArgs,hashArgs[i]);
			String hash=MD5.getDigestAsString(xml);
			
			CacheManagerAndHash manager= managers.remove(hashArgs[i]);
			if(manager!=null && manager.hash.equals(hash)) {
				newManagers.put(hashArgs[i], manager);
			}	
			else mapXML.put(hashArgs[i], xml);
		}
		
		// shutdown all existing managers that have changed
		{
			Iterator<Entry<String, CacheManagerAndHash>> it = managers.entrySet().iterator();
			Entry<String, CacheManagerAndHash> entry;
			synchronized(managers){
				it = managers.entrySet().iterator();
				while(it.hasNext()){
					entry= it.next();
					if(entry.getKey().toString().startsWith(dir.getAbsolutePath())){
						entry.getValue().manager.shutdown();
					}
					else newManagers.put(entry.getKey(), entry.getValue());
					
				}
				managers=newManagers;
			}
		}
		
		Iterator<Entry<String, String>> it = mapXML.entrySet().iterator();
		Entry<String, String> entry;
		String xml,hashArg,hash;
		while(it.hasNext()){
			entry = it.next();
			hashArg=entry.getKey();
			xml=entry.getValue();
			
			hashDir=dir.getRealResource(hashArg);
			if(!hashDir.isDirectory())hashDir.createDirectory(true);
			
			writeEHCacheXML(hashDir,xml);
			hash=MD5.getDigestAsString(xml);
			
			moveData(dir,hashArg,cacheNames,arguments);
			//print.e(xml);
			Configuration conf = ConfigurationFactory.parseConfiguration(new ByteArrayInputStream(xml.getBytes(CharsetUtil.UTF8)));
			conf.setName("ehcache_"+config.getIdentification().getId());
			CacheManagerAndHash m = new CacheManagerAndHash(CacheManager.newInstance(conf),hash);
			newManagers.put(hashDir.getAbsolutePath(), m);
		}
		
		clean(dir);
	}
	
	public static void flushAllCaches() {
		String[] names;
		Iterator<Entry<String, CacheManagerAndHash>> it = managers.entrySet().iterator();
		Entry<String, CacheManagerAndHash> entry;
		while(it.hasNext()){
			entry = it.next();
			CacheManager manager=entry.getValue().manager;
			names = manager.getCacheNames();
			for(int i=0;i<names.length;i++){
				manager.getCache(names[i]).flush();
			}
		}
	}
	
	private static void clean(Resource dir) {
		Resource[] dirs = dir.listResources();
		Resource[] children;
		
		for(int i=0;i<dirs.length;i++){
			if(dirs[i].isDirectory()){
				//print.out(dirs[i]+":"+pathes.contains(dirs[i].getAbsolutePath()));
				children=dirs[i].listResources();
				if(children!=null && children.length>1)continue;
				clean(children);
				dirs[i].delete();
			}
		}
	}

	private static void clean(Resource[] arr) {
		if(arr!=null)for(int i=0;i<arr.length;i++){
			if(arr[i].isDirectory()){
				clean(arr[i].listResources());
			}
			arr[i].delete();
		}
	}

	private static void moveData(Resource dir, String hash, String[] cacheNames, Struct[] arguments) {
		String h;
		Resource trg = dir.getRealResource(hash);
		deleteData(dir, cacheNames);
		for(int i=0;i<cacheNames.length;i++){
			h=createHash(arguments[i]);
			if(h.equals(hash)){
				moveData(dir,cacheNames[i],trg);
			}
		}
		
	}

	private static void moveData(Resource dir, String cacheName, Resource trg) {
		Resource[] dirs = dir.listResources();
		Resource index,data;
		// move 
		for(int i=0;i<dirs.length;i++){
			if(!dirs[i].equals(trg) && 
				dirs[i].isDirectory() && 
				(data=dirs[i].getRealResource(cacheName+".data")).exists() && 
				(index=dirs[i].getRealResource(cacheName+".index")).exists() ){
				
				try {
					index.moveTo(trg.getRealResource(cacheName+".index"));
					data.moveTo(trg.getRealResource(cacheName+".data"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void deleteData(Resource dir, String[] cacheNames) {
		Set<String> names=new HashSet<String>();
		for(int i=0;i<cacheNames.length;i++){
			names.add(cacheNames[i]);
		}
		
		Resource[] dirs = dir.listResources();
		String name;
		// move 
		for(int i=0;i<dirs.length;i++){
			if(dirs[i].isDirectory()){
				Resource[] datas = dirs[i].listResources(new DataFiter());
				if(datas!=null) for(int y=0;y<datas.length;y++){
					name=datas[y].getName();
					name=name.substring(0,name.length()-5);
					if(!names.contains(name)){
						datas[y].delete();
						dirs[i].getRealResource(name+".index").delete();
					}
						
				}
			}
		}
	}

	private static void writeEHCacheXML(Resource hashDir, String xml) {
		ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes(CharsetUtil.UTF8));
		OutputStream os=null;
		try{
			os = hashDir.getRealResource("ehcache.xml").getOutputStream();
			IOUtil.copy(is, os,false,true);
		}
		catch(IOException ioe){ioe.printStackTrace();}
	}

	private static String createHash(Struct args) {
		
			String dist = args.get("distributed","").toString().trim().toLowerCase();
			try {
				if(dist.equals("off")){
					return MD5.getDigestAsString(dist);
				}
				else if(dist.equals("automatic")){
					return MD5.getDigestAsString(
						dist+
						args.get("automatic_timeToLive","").toString().trim().toLowerCase()+
						args.get("automatic_addional","").toString().trim().toLowerCase()+
						args.get("automatic_multicastGroupPort","").toString().trim().toLowerCase()+
						args.get("automatic_multicastGroupAddress","").toString().trim().toLowerCase()+
						args.get("automatic_hostName","").toString().trim().toLowerCase()
					);
				}
				else {
					 return MD5.getDigestAsString(
						dist+
						args.get("manual_rmiUrls","").toString().trim().toLowerCase()+
						args.get("manual_addional","").toString().trim().toLowerCase()+
						args.get("listener_hostName","").toString().trim().toLowerCase()+
						args.get("listener_port","").toString().trim().toLowerCase()+
						args.get("listener_remoteObjectPort","").toString().trim().toLowerCase()+
						args.get("listener_socketTimeoutMillis","120000").toString().trim().toLowerCase()
					); 
				}
				
			} catch (IOException e) {
				e.printStackTrace();
				return "";
			}
	}
	private static String[] createHash(Struct[] arguments) {
		String[] hashes=new String[arguments.length];
		for(int i=0;i<arguments.length;i++){
			hashes[i]=createHash(arguments[i]);
		}
		return hashes;
	}

	private static String createXML(String path, String[] cacheNames,Struct[] arguments, String[] hashes, String hash) {
		boolean isDistributed=false;
		
		//Cast caster = CFMLEngineFactory.getInstance().getCastUtil();
		Struct global=null;
		for(int i=0;i<hashes.length;i++){
			if(hash.equals(hashes[i])){
				global=arguments[i];
				break;
			}
		}
		
		
		StringBuilder xml=new StringBuilder();
		xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		xml.append("<ehcache xsi:noNamespaceSchemaLocation=\"ehcache.xsd\">\n");
				
		// disk storage
		xml.append("<diskStore path=\"");
		xml.append(path);
		xml.append("\"/>\n");
		xml.append("<cacheManagerEventListenerFactory class=\"\" properties=\"\"/>\n");
		
		
		// RMI
		// Automatic
		if(global!=null && global.get("distributed","").equals("automatic")){
			// provider
			isDistributed=true;
			xml.append("<cacheManagerPeerProviderFactory \n");
			xml.append(" class=\"net.sf.ehcache.distribution.RMICacheManagerPeerProviderFactory\"\n ");
			String add = global.get("automatic_addional","").toString().trim();
			String hostName=global.get("automatic_hostName","").toString().trim().toLowerCase();
			if(!Util.isEmpty(hostName)) add+=",hostName="+hostName;
			if(!Util.isEmpty(add) && !add.startsWith(","))add=","+add;
			add=add.replace('\n', ' ');
			xml.append(" properties=\"peerDiscovery=automatic" +
					", multicastGroupAddress="+global.get("automatic_multicastGroupAddress","").toString().trim().toLowerCase()+
					", multicastGroupPort="+global.get("automatic_multicastGroupPort","").toString().trim().toLowerCase()+
					", timeToLive="+toTimeToLive(global.get("automatic_timeToLive","").toString().trim().toLowerCase())+
					add+" \" />\n");
			
			//hostName=fully_qualified_hostname_or_ip,
			
			// listener
			xml.append("<cacheManagerPeerListenerFactory class=\"net.sf.ehcache.distribution.RMICacheManagerPeerListenerFactory\"/>\n");
			
		}
		// Manual
		else if(global!=null && global.get("distributed","").equals("manual")){
			// provider
			isDistributed=true;
			xml.append("<cacheManagerPeerProviderFactory");
			xml.append(" class=\"net.sf.ehcache.distribution.RMICacheManagerPeerProviderFactory\" ");
			String add = global.get("manual_addional","").toString().trim();
			if(!Util.isEmpty(add) && !add.startsWith(","))add=","+add;
			add=add.replace('\n', ' ');
			xml.append(" properties=\"peerDiscovery=manual, rmiUrls="+global.get("manual_rmiUrls","").toString().trim().toLowerCase().replace('\n', ' ')+
					add+"\"/>\n"); //propertySeparator=\",\" 
		
			// listener
			StringBuilder sb=new StringBuilder();
			
			String hostName=global.get("listener_hostName","").toString().trim().toLowerCase();
			if(!Util.isEmpty(hostName)) add(sb,"hostName="+hostName);
			String port = global.get("listener_port","").toString().trim().toLowerCase();
			if(!Util.isEmpty(port)) add(sb,"port="+port);
			String remoteObjectPort = global.get("listener_remoteObjectPort","").toString().trim().toLowerCase();
			if(!Util.isEmpty(remoteObjectPort)) add(sb,"remoteObjectPort="+remoteObjectPort);
			String socketTimeoutMillis = global.get("listener_socketTimeoutMillis","").toString().trim().toLowerCase();
			if(!Util.isEmpty(socketTimeoutMillis) && !"120000".equals(socketTimeoutMillis)) 
				add(sb,"socketTimeoutMillis="+socketTimeoutMillis);
			
			xml.append("<cacheManagerPeerListenerFactory"); 
			xml.append(" class=\"net.sf.ehcache.distribution.RMICacheManagerPeerListenerFactory\""); 
			if(sb.length()>0)xml.append(" properties=\""+sb+"\""); 
			xml.append("/>\n");
			
			
		}

		
		
		if(isDistributed){
		}
		
        
        

		
		xml.append("<defaultCache \n");
		xml.append("   diskPersistent=\"true\"\n");
		xml.append("   eternal=\"false\"\n");
		xml.append("   maxElementsInMemory=\"10000\"\n");
		xml.append("   maxElementsOnDisk=\"10000000\"\n");
		xml.append("   memoryStoreEvictionPolicy=\"LRU\"\n");
		xml.append("   timeToIdleSeconds=\"86400\"\n");
		xml.append("   timeToLiveSeconds=\"86400\"\n");
		xml.append("   overflowToDisk=\"true\"\n");
		xml.append("   diskSpoolBufferSizeMB=\"30\"\n");
		xml.append("   diskExpiryThreadIntervalSeconds=\"3600\"\n");
		xml.append(" />\n");
		
		// cache
		for(int i=0;i<cacheNames.length && i<arguments.length;i++){
			if(hashes[i].equals(hash))createCacheXml(xml,cacheNames[i],arguments[i],isDistributed);
		}
		
		
		xml.append("</ehcache>\n");
		return xml.toString();
	}
	
	
	
	private static void add(StringBuilder sb,String str) {
		if(sb.length()>0)sb.append(", ");
		sb.append(str);
	}

	private static int toTimeToLive(String str) {
		if(str.indexOf("host")!=-1) return 0;
		if(str.indexOf("site")!=-1) return 1;
		if(str.indexOf("region")!=-1) return 64;
		if(str.indexOf("continent")!=-1) return 128;
		return 255;	
	}

	private static void createCacheXml(StringBuilder xml, String cacheName, Struct arguments, boolean isDistributed) {

		// disk Persistent
		boolean diskPersistent=toBooleanValue(arguments.get("diskpersistent",Boolean.FALSE),DISK_PERSISTENT);
		
		// eternal
		boolean eternal=toBooleanValue(arguments.get("eternal",Boolean.FALSE),ETERNAL);
		
		// max elements in memory
		int maxElementsInMemory=toIntValue(arguments.get("maxelementsinmemory",new Integer(MAX_ELEMENTS_IN_MEMEORY)),MAX_ELEMENTS_IN_MEMEORY);
		
		// max elements on disk
		int maxElementsOnDisk=toIntValue(arguments.get("maxelementsondisk",new Integer(MAX_ELEMENTS_ON_DISK)),MAX_ELEMENTS_ON_DISK);
		
		// memory eviction policy
		String strPolicy=toString(arguments.get("memoryevictionpolicy",MEMORY_EVICTION_POLICY),MEMORY_EVICTION_POLICY);
		String policy = "LRU";
		if("FIFO".equalsIgnoreCase(strPolicy)) policy="FIFO";
		else if("LFU".equalsIgnoreCase(strPolicy)) policy="LFU";
		
		// overflow to disk
		boolean overflowToDisk=toBooleanValue(arguments.get("overflowtodisk",Boolean.FALSE),OVERFLOW_TO_DISK);
		
		// time to idle seconds
		long timeToIdleSeconds=toLongValue(arguments.get("timeToIdleSeconds",new Long(TIME_TO_IDLE_SECONDS)),TIME_TO_IDLE_SECONDS);
		
		// time to live seconds
		long timeToLiveSeconds=toLongValue(arguments.get("timeToLiveSeconds",new Long(TIME_TO_LIVE_SECONDS)),TIME_TO_LIVE_SECONDS);
		
	// REPLICATION
		boolean replicatePuts=toBooleanValue(arguments.get("replicatePuts",Boolean.FALSE),REPLICATE_PUTS);
		boolean replicatePutsViaCopy=toBooleanValue(arguments.get("replicatePutsViaCopy",Boolean.FALSE),REPLICATE_PUTS_VIA_COPY);
		boolean replicateUpdates=toBooleanValue(arguments.get("replicateUpdates",Boolean.FALSE),REPLICATE_UPDATES);
		boolean replicateUpdatesViaCopy=toBooleanValue(arguments.get("replicateUpdatesViaCopy",Boolean.FALSE),REPLICATE_UPDATES_VIA_COPY);
		boolean replicateRemovals=toBooleanValue(arguments.get("replicateRemovals",Boolean.FALSE),REPLICATE_REMOVALS);
		boolean replicateAsynchronously=toBooleanValue(arguments.get("replicateAsynchronously",Boolean.FALSE),REPLICATE_ASYNC);
		int asynchronousReplicationInterval=toIntValue(arguments.get("asynchronousReplicationIntervalMillis",new Integer(ASYNC_REP_INTERVAL)),ASYNC_REP_INTERVAL);
		
		
		
		xml.append("<cache name=\""+cacheName+"\"\n");
		xml.append("   diskPersistent=\""+diskPersistent+"\"\n");
		xml.append("   eternal=\""+eternal+"\"\n");
		xml.append("   maxElementsInMemory=\""+maxElementsInMemory+"\"\n");
		xml.append("   maxElementsOnDisk=\""+maxElementsOnDisk+"\"\n");
		xml.append("   memoryStoreEvictionPolicy=\""+policy+"\"\n");
		xml.append("   timeToIdleSeconds=\""+timeToIdleSeconds+"\"\n");
		xml.append("   timeToLiveSeconds=\""+timeToLiveSeconds+"\"\n");
		xml.append("   overflowToDisk=\""+overflowToDisk+"\"");
		xml.append(">\n");
		if(isDistributed){
			xml.append(" <cacheEventListenerFactory \n");
			//xml.append(" class=\"net.sf.ehcache.distribution.RMICacheReplicatorFactory\" ");
			xml.append(" class=\""+LuceeRMICacheReplicatorFactory.class.getName()+"\" \n");
			
			xml.append(" properties=\"replicateAsynchronously="+replicateAsynchronously+
					", asynchronousReplicationIntervalMillis="+asynchronousReplicationInterval+
					", replicatePuts="+replicatePuts+
					", replicatePutsViaCopy="+replicatePutsViaCopy+
					", replicateUpdates="+replicateUpdates+
					", replicateUpdatesViaCopy="+replicateUpdatesViaCopy+
					", replicateRemovals="+replicateRemovals+" \"");
			xml.append("/>\n");
			

			// BootStrap
			if(toBooleanValue(arguments.get("bootstrapType","false"),false)){
				xml.append("<bootstrapCacheLoaderFactory \n");
				xml.append("	class=\"net.sf.ehcache.distribution.RMIBootstrapCacheLoaderFactory\" \n");
				xml.append("	properties=\"bootstrapAsynchronously="+toBooleanValue(arguments.get("bootstrapAsynchronously","true"),true)+
						", maximumChunkSizeBytes="+toLongValue(arguments.get("maximumChunkSizeBytes","5000000"),5000000L)+"\" \n");
				xml.append("	propertySeparator=\",\" /> \n");
			}
	        
			
		}
		xml.append(" </cache>\n");
	
		
	}



	public void init(String cacheName, Struct arguments) {
		init(ThreadLocalPageContext.getConfig(),cacheName, arguments);
		
	}
	@Override
	public void init(Config config,String cacheName, Struct arguments) {
		this.classLoader=config.getClassLoader();
		this.cacheName=cacheName;
		
		setClassLoader();
		Resource hashDir = config.getConfigDir().getRealResource("ehcache").getRealResource(createHash(arguments));
		manager =managers.get(hashDir.getAbsolutePath()).manager;
	} 

	private void setClassLoader() {
		if(classLoader!=Thread.currentThread().getContextClassLoader())
			Thread.currentThread().setContextClassLoader(classLoader);
	}

	protected net.sf.ehcache.Cache getCache() {
		setClassLoader();
		Cache c = manager.getCache(cacheName);
		if(c==null)
			throw new PageRuntimeException(new ApplicationException(ExceptionUtil.similarKeyMessage(CollectionUtil.toKeys(manager.getCacheNames(),false), cacheName, "cache name", "caches",null, true)));
		return c;
	}

	@Override
	public boolean remove(String key) {
		try	{
			return getCache().remove(key);
		}
		catch(Throwable t){
			return false;
		}
	}

	public CacheEntry getCacheEntry(String key) throws CacheException {
		try {
			misses++;
			Element el = getCache().get(key);
			if(el==null)throw new CacheException("there is no entry in cache with key ["+key+"]");
			hits++;
			misses--;
			return new EHCacheEntry(el);
		}
		catch(IllegalStateException ise) {
			throw new CacheException(ise.getMessage());
		}
		catch(net.sf.ehcache.CacheException ce) {
			throw new CacheException(ce.getMessage());
		}
	}

	public CacheEntry getCacheEntry(String key, CacheEntry defaultValue) {
		try {
			Element el = getCache().get(key);
			if(el!=null){
				hits++;
				return new EHCacheEntry(el);
			}
		}
		catch(Throwable t) {
			misses++;
		}
		return defaultValue;
	}

	@Override
	public Object getValue(String key) throws CacheException {
		try {
			misses++;
			Element el = getCache().get(key);
			if(el==null)throw new CacheException("there is no entry in cache with key ["+key+"]");
			misses--;
			hits++;
			return el.getObjectValue();
		}
		catch(IllegalStateException ise) {
			throw new CacheException(ise.getMessage());
		}
		catch(net.sf.ehcache.CacheException ce) {
			throw new CacheException(ce.getMessage());
		}
	}

	@Override
	public Object getValue(String key, Object defaultValue) {
		try {
			Element el = getCache().get(key);
			if(el!=null){
				hits++;
				return el.getObjectValue();
			}
		}
		catch(Throwable t) {
			misses++;
		}
		return defaultValue;
	}

	@Override
	public long hitCount() {
		return hits;
	}

	@Override
	public long missCount() {
		return misses;
	}

	public void remove() {
		setClassLoader();
		CacheManager singletonManager = CacheManager.getInstance();
		if(singletonManager.cacheExists(cacheName))
			singletonManager.removeCache(cacheName);
		
	}
	
	@Override
	public int clear() throws IOException {
		int size=getCache().getSize();
		getCache().removeAll();
		return size;
	}
	
	

	private static boolean toBooleanValue(Object o, boolean defaultValue) {
		if(o instanceof Boolean) return ((Boolean)o).booleanValue();
        else if(o instanceof Number) return (((Number)o).doubleValue())!=0;
        else if(o instanceof String) {
        	String str = o.toString().trim().toLowerCase();
            if(str.equals("yes") || str.equals("on") || str.equals("true")) return true;
            else if(str.equals("no") || str.equals("false") || str.equals("off")) return false;
        }
        return defaultValue;
	}

	private static String toString(Object o, String defaultValue) {
		if(o instanceof String)return o.toString();
		return defaultValue;
	}

	private static int toIntValue(Object o, int defaultValue) {
		if(o instanceof Number) return ((Number)o).intValue();
		try{
		return Integer.parseInt(o.toString());
		}
		catch(Throwable t){
			return defaultValue;
		}
	}

	private static long toLongValue(Object o, long defaultValue) {
		if(o instanceof Number) return ((Number)o).longValue();
		try{
			return Long.parseLong(o.toString());
		}
		catch(Throwable t){
			return defaultValue;
		}
	}
	

}class CacheManagerAndHash {

		CacheManager manager;
		String hash;

		public CacheManagerAndHash(CacheManager manager, String hash) {
			this.manager=manager;
			
			this.hash=hash;
		}
		
	}

class DataFiter implements ResourceNameFilter {

	@Override
	public boolean accept(Resource parent, String name) {
		return name.endsWith(".data");
	}

}