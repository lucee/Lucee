component {

	THIS.name=url.appName;


	variables.conn={
		class: 'org.lucee.extension.cache.eh.EHCache'
		, bundleName: 'ehcache.extension'
		//, bundleVersion: '2.10.0.27-SNAPSHOT'
		, storage: true
		, custom:
			{ "bootstrapAsynchronously":"true", 
				"replicatePuts":"true", 
				"automatic_hostName":"", 
				"bootstrapType":"on", 
				"maxelementsinmemory":"10000", 
				"manual_rmiUrls":"", 
				"distributed":"off", 
				"automatic_multicastGroupAddress":"230.0.0.1", 
				"memoryevictionpolicy":"LRU", 
				"replicatePutsViaCopy":"true", 
				"timeToIdleSeconds":"86400", 
				"timeToLiveSeconds":"86400",
				"maximumChunkSizeBytes":"5000000", 
				"automatic_multicastGroupPort":"4446", 
				"listener_socketTimeoutMillis":"120000",
				"diskpersistent":"true", 
				"manual_addional":"", 
				"replicateRemovals":"true", 
				"replicateUpdatesViaCopy":"true", 
				"automatic_addional":"", 
				"overflowtodisk":"true", 
				"replicateAsynchronously":"true", 
				"maxelementsondisk":"10000000", 
				"listener_remoteObjectPort":"", 
				"asynchronousReplicationIntervalMillis":"1000", 
				"listener_hostName":"", "replicateUpdates":"true", 
				"manual_hostName":"", 
				"automatic_timeToLive":"unrestricted", 
				"listener_port":"" 
		}
		, default: ''
	};
	if(!isNull(url.version)) variables.conn.bundleVersion=url.version;

	this.cache.connections["perAppCache"] = conn;



	THIS.applicationTimeout = createTimeSpan( 60, 0, 0, 0 );
	THIS.sessionManagement = true;
	THIS.sessionCluster=true;
	THIS.sessionType = "cfml";
	THIS.sessionTimeout = createTimeSpan( 0, 0, 2, 0 );
	THIS.sessionStorage = "perAppCache";
	THIS.clientManagement = true;
	THIS.clientCluster = true;
	THIS.clientTimeout = createTimeSpan( 0, 2, 0, 0 );
	THIS.clientStorage = "perAppCache";

	
	public function onRequestStart() {
		setting requesttimeout=10;
	}
}