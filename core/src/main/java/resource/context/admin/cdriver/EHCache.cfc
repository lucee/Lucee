<cfcomponent extends="Cache">


	
    <cfset fields=array(
		field("Eternal","eternal","false",true,"Sets whether elements are eternal. If eternal, timeouts are ignored and the element is never expired","checkbox","true"),
		field("Maximum elements in memory","maxelementsinmemory","10000",true,"Sets the maximum number of objects to be held in memory","text"),
		field("Memory Store Eviction Policy","memoryevictionpolicy","LRU,LFU,FIFO",true,"The algorithm to used to evict old entries when maximum limit is reached, such as LRU (least recently used), LFU (least frequently used) or FIFO (first in first out).","select"),
		field("Time to idle in seconds","timeToIdleSeconds","86400",true,"Sets the time to idle for an element before it expires. Is only used if the element is not eternal","time"),
		field("Time to live in seconds","timeToLiveSeconds","86400",true,"Sets the timeout to live for an element before it expires. Is only used if the element is not eternal","time"),
		
		//group("Disk","Hard disk specific settings"),
		field("Disk persistent","diskpersistent","true",true,"for caches that overflow to disk, whether the disk store persists between restarts of the Engine.","checkbox","true"),
		field("Overflow to disk","overflowtodisk","true",true,"for caches that overflow to disk, the disk cache persist between CacheManager instances","checkbox","true"),
		field("Maximum elements on disk","maxelementsondisk","10000000",true,"Sets the maximum number of elements on Disk. 0 means unlimited","text"),
		
		
		group("Distributed","Ehcache comes with a built-in RMI-based distribution system"),
		field("Distribution Type","distributed","off",true,
			struct(
			_top:"select how EHCache distribute the data in the cluster",
			off:" distribution is disabled",
			automatic:"using a multicast group. This one automatically discovers peers and detects changes such as peers entering and leaving the group",
    		manual:"using manual rmiURL configuration. A hardcoded list of peers is provided at configuration time"),
			"radio","off,automatic,manual"),
		
		
		group("Automatic Discovery" ,"Settings for automatic discovery. only used when ""Distribution Type"" above is set to ""automatic""",3),
		
		
		
		
		field("Multicast Group Address","automatic_multicastGroupAddress","230.0.0.1",false,"specify a valid multicast group address","text"),
		field("Multicast Group Port","automatic_multicastGroupPort","4446",false,"specify a dedicated port for the multicast heartbeat traffic","text"),
		field("Time to Live","automatic_timeToLive","unrestricted",false,struct(
		_top:'select a value which determines how far the packets will propagate.<br>By convention, the restrictions are:'
		),"radio",'the same host,the same subnet,the same site,the same region,the same continent,unrestricted'),
		
		field("Host Name","automatic_hostName","",false,"the hostname or IP of the interface to be used for sending and receiving multicast packets
       (relevant to mulithomed hosts only)","text"),
		
		field("Addional","automatic_addional","",true,"addional properties","textarea","true"),
		
		group("Manual Discovery","Settings for manual discovery. only used when ""Distribution Type"" above is set to ""manual""",3),
		
		field("RMI Urls","manual_rmiUrls","",false,"specify a pipe separated list of rmiUrls, in the form ""//hostname:port""","textarea"),
		field("Host Name","manual_hostName","",false,"the hostname is the hostname of the remote CacheManager peer. The port is the listening
      port of the RMICacheManagerPeerListener of the remote CacheManager peer","text"),
		field("Addional","manual_addional","",true,"addional properties","textarea"),
		
		group("Listener","",3),
		
		field("Host Name","listener_hostName","",false,"the hostName of the host the listener is running on. Specify
      where the host is multihomed and you want to control the interface over which cluster
      messages are received. Defaults to the host name of the default interface if not
      specified.","text"),
		field("Port","listener_port","",false,"the port the RMI Registry listener listens on. This defaults to a free port if not specified.","text"),
		field("Remote Object Port","listener_remoteObjectPort","",false,"the port number on which the remote objects bound in the registry receive calls.
                         This defaults to a free port if not specified.","text"),
		field("Socket Timeout (ms)","listener_socketTimeoutMillis","120000",false,"the number of ms client sockets will stay open when sending
      messages to the listener. This should be long enough for the slowest message.","text")
			
		
		
		,group("Bootstrap","When you startup the Lucee server and EHCache is to be synchronized with another instance, the local EHCache will fetch the data from the other EHCache instance. (these settings has no impact when Distribution type is set to ""off"")",3)
		
		
		
		,field("Bootstrap Type","bootstrapType","on",true,
			struct(
			_top:"select how EHCache bootstrap the data from the cluster",
			off:"bootstrap is disabled",
			on:"bootstrap is enabled "
    		),
			"radio","off,on"),
		
		
		
		
		field("Bootstrap Asynchronously","bootstrapAsynchronously","true",true,"whether the bootstrap happens in the background
      after the cache has started. If checkbox is unchecked, bootstrapping must complete before the cache is
      made available.","checkbox",'true')
		,field("Maximum Chunk Size (bytes)","maximumChunkSizeBytes","5000000",true,"Caches can potentially be very large, larger than the
      memory limits of the VM. This property allows the bootstraper to fetched elements in
      chunks.","text",'true')
		
		
		,group("Replication","Different settings to define how replication works (these settings has no impact when Distribution type is set to ""off"")",3)
		
		,field("Replicate Asynchronously","replicateAsynchronously","true",true,"whether replications are
      asynchronous (checked) or synchronous (unchecked), .","checkbox",'true')
		
		,field("Replicate Puts","replicatePuts","true",true,"whether new elements placed in a cache are replicated to others.","checkbox",'true')
		,field("Replicate Puts Via Copy","replicatePutsViaCopy","true",true,"whether the new elements are copied to other caches (checked), or whether a remove message is sent.","checkbox",'true')
		,field("Replicate Updates","replicateUpdates","true",true,"whether new elements which override an element already existing with the same key are replicated","checkbox",'true')
		,field("Replicate Updates Via Copy","replicateUpdatesViaCopy","true",true,"whether the new elements are copied to other caches (checked), or whether a remove message is sent.","checkbox",'true')
		,field("Replicate Removals","replicateRemovals","true",true,"whether element removals are replicated.","checkbox",'true')
		,field("Asynchronous Replication Intervall","asynchronousReplicationIntervalMillis","1000",true,"The asynchronous replicator runs at a set interval of milliseconds (has no impact when ""Replicate Asynchronously"" is not checked)","text")
		
		
		
	)>

	<cffunction name="getClass" returntype="string">
    	<cfreturn "lucee.runtime.cache.eh.EHCache">
    </cffunction>
    
	<cffunction name="getLabel" returntype="string" output="no">
    	<cfreturn "EHCache">
    </cffunction>
	<cffunction name="getDescription" returntype="string" output="no">
    	<cfset var c="">
    	<cfsavecontent variable="c">
Ehcache is a widely used java distributed cache for general purpose caching, Java EE and light-weight containers.
<br><br>
It features memory and disk stores, replicate by copy and invalidate, listeners, cache loaders, cache extensions, cache exception handlers, a gzip caching servlet filter, RESTful & SOAP APIs, an implementation of JSR107 and much more...
<br><br>
Ehcache is available under an Apache open source license and is actively developed, maintained and supported.
        </cfsavecontent>
    
    
    	<cfreturn c>
    </cffunction>
</cfcomponent>