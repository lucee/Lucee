<cfapplication caches="#{
	object:{
		class="org.lucee.extension.cache.eh.EHCache"
		,maven="org.lucee:ehcache:2.10.9.3-SNAPSHOT"
		,storage=false
		,custom={timeToLiveSeconds:86400
				,maxelementsondisk:10000000
				,distributed:"off"
				,overflowtodisk:true
				,maximumChunkSizeBytes:5000000
				,timeToIdleSeconds:86400
				,maxelementsinmemory:10000
				,asynchronousReplicationIntervalMillis:1000
				,diskpersistent:true
				,memoryevictionpolicy:"LRU"}
	}	

}#"/>