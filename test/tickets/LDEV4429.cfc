component extends="org.lucee.cfml.test.LuceeTestCase" labels="cache,ehCache" skip=true {
 	
	function beforeAll() {
		variables.postgres = server.getDatasource("postgres");

		if( structCount(postgres) ) {
			// define datasource
			application name="LDEV-4429" action="update"  datasource=postgres;
		}
	}

	public function testCachePutEHCache() {
		createEHCache();
		testCachePut();
	}

	private function testCachePut() localMode="modern" {
		var res =queryExecute('SELECT ''{"a" : "aab"}''::jsonb AS result');
		var jsonbColl = res.result[1];
		cachePut("def",jsonbColl,createTimespan(0,0,0,30),createTimespan(0,0,0,30),"testCache4429")
		var cachedval = cacheGet(id ="def", region="testCache4429")
		
		expect(isInstanceOf(cachedval, "java.lang.Object")).toBe("true");
	}

	private function createEHCache() {
		var cacheConn = {
			class: 'org.lucee.extension.cache.eh.EHCache'
		  , bundleName: 'ehcache.extension'
		  , bundleVersion: '2.10.9.2-SNAPSHOT'
		  , storage: false
		  , custom: {
			  "bootstrapAsynchronously":"true",
			  "automatic_hostName":"",
			  "bootstrapType":"on",
			  "maxelementsinmemory":"10000",
			  "manual_rmiUrls":"",
			  "distributed":"automatic",
			  "automatic_multicastGroupAddress":"230.0.0.1",
			  "memoryevictionpolicy":"LRU",
			  "timeToIdleSeconds":"86400",
			  "maximumChunkSizeBytes":"5000000",
			  "automatic_multicastGroupPort":"4446",
			  "listener_socketTimeoutMillis":"120000",
			  "timeToLiveSeconds":"86400",
			  "diskpersistent":"true",
			  "manual_addional":"",
			  "replicateRemovals":"true",
			  "automatic_addional":"",
			  "overflowtodisk":"true",
			  "replicateAsynchronously":"true",
			  "maxelementsondisk":"10000000",
			  "listener_remoteObjectPort":"",
			  "asynchronousReplicationIntervalMillis":"1000",
			  "listener_hostName":"",
			  "replicateUpdates":"true",
			  "manual_hostName":"",
			  "automatic_timeToLive":"unrestricted",
			  "listener_port":""
		  }
		  , default: ''
		};
		application name="LDEV-4429" action="update"  caches={"testCache4429":cacheConn};
	}
}