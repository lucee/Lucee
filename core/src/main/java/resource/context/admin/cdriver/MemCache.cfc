<cfcomponent extends="Cache">
	
    <cfset fields=array(
		field("Servers","servers","",true,"please define here a list of all servers you would like to connect to, please follow this pattern:<br> Host:Port&lt;new line><br> Host:Port&lt;new line><br>Host:Port","textarea")
		
		,field("Initial connections","initial_connections","1",false,"how many initial connetions are set (initial setting ""1"")","text")
		,field("Minimal spare connections","min_spare_connections","1",false,"minimal amount of connections (initial setting ""1"")","text")
		,field("Maximum spare connections","max_spare_connections","32",false,"maximum amount of connections (initial setting ""32"")","text")
		
		,field("Maximum idle time","max_idle_time","600",false,"maximum idle time for available sockets (initial setting ""10 minutes"")","time")
		,field("Maximum busy time","max_busy_time","30",false,"maximum busy time for available sockets (initial setting ""30 seconds"")","time")
		,field("thread maintenance","maint_thread_sleep","5",false,"maintenance thread sleep time (initial setting ""5 seconds"")","time")
		,field("Socket timeout","socket_timeout","30",false,"default timeout of socket reads (initial setting ""30 seconds"")","time")
		,field("Socket connection timeout","socket_connect_to","3",false,"timeout for socket connections (initial setting ""3 seconds"")","time")
		
		,field("Failover","failover",true,false,"default to failover in event of cache (initial setting ""enabled"")","checkbox","true")
		,field("Failback","failback",true,false,"controls putting a dead server back (initial setting ""enabled"")","checkbox","true")
		,field("Nagle's algorithm","nagle_alg",true,false,"enable/disable Nagle's algorithm (initial setting ""enabled"")","checkbox","true")
		,field("Alive check","alive_check",true,false,"enable/disable health check of socket on checkout (initial setting ""enabled"")","checkbox","true")
		
		,field("Buffer size","buffer_size","1",false,"buffer size in mb (initial setting ""1mb"")","text")
		
	)>
    
	<cffunction name="getClass" returntype="string">
    	<cfreturn "lucee.extension.io.cache.memcache.MemCacheRaw">
    </cffunction>
    
	<cffunction name="getLabel" returntype="string">
    	<cfreturn "MemCache">
    </cffunction>
	<cffunction name="getDescription" returntype="string" output="no">
    	<cfreturn "Memcache connection">
    </cffunction>
    
</cfcomponent>
