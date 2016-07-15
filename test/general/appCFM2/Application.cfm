<cfapplication caches="#{
	object:{
		class="org.lucee.extension.cache.eh.EHCache"
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