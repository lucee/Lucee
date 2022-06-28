component {
	this.name = 'test';
	this.cache.connections["testEHcache"] = {
	  class: 'org.lucee.extension.cache.eh.EHCache'
	, bundleName: 'ehcache.extension'
	//, bundleVersion: '2.10.0.21'
	, storage: false
	, custom: {
		"bootstrapAsynchronously":"true",
		"replicatePuts":"true",
		"automatic_hostName":"",
		"bootstrapType":"on",
		"maxelementsinmemory":"10000",
		"manual_rmiUrls":"",
		"distributed":"automatic",
		"automatic_multicastGroupAddress":"230.0.0.1",
		"memoryevictionpolicy":"LRU",
		"replicatePutsViaCopy":"true",
		"timeToIdleSeconds":"86400",
		"maximumChunkSizeBytes":"5000000",
		"automatic_multicastGroupPort":"4471",
		"listener_socketTimeoutMillis":"120000",
		"timeToLiveSeconds":"86400",
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
		"listener_hostName":"",
		"replicateUpdates":"true",
		"manual_hostName":"",
		"automatic_timeToLive":"the same subnet",
		"listener_port":""
	}
	, default: 'object'
	};
	this.cache.object = "testEHcache";

	public function onRequestStart() {
		setting requesttimeout=10;
	}
}