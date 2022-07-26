component extends="org.lucee.cfml.test.LuceeTestCase" labels="cache,ehCache" {
	function beforeAll(){
		createEHCache('defaultCache');
		createEHCache('testEHcache');
	}

	function afterAll(){
		removeEHCache('defaultCache');
		removeEHCache('testEHcache');
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1303", function() {
			it(title="checking EHCache with application.cfc, with distribution mode = 'auto' ", body = function( currentSpec ) {
				var uri=createURI("LDEV1303/test.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe("test");
			});

			it(title="checking EHCache create via admin tag, with distribution mode = 'auto' ", body = function( currentSpec ) {
				result1 = verifyCacheConnection('defaultCache');
				result2 = verifyCacheConnection('testEHcache');
				expect(result1).toBe("false");
				expect(result2).toBe("false");
			});
		});
	}
	// private Function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private function createEHCache(string name){
		admin
			action="updateCacheConnection"
			type="web"
			password=request.webadminpassword
			name="#arguments.name#"
			class="org.lucee.extension.cache.eh.EHCache"
			storage="false"
			default="object"
			custom='#{ "bootstrapAsynchronously":"true", "replicatePuts":"true", "automatic_hostName":"", "bootstrapType":"on", "maxelementsinmemory":"10000", "manual_rmiUrls":"", "distributed":"automatic", "automatic_multicastGroupAddress":"224.0.0.0", "memoryevictionpolicy":"LRU", "replicatePutsViaCopy":"true", "timeToIdleSeconds":"86400", "maximumChunkSizeBytes":"5000000", "automatic_multicastGroupPort":"4446", "listener_socketTimeoutMillis":"120000", "timeToLiveSeconds":"86400", "diskpersistent":"true", "manual_addional":"", "replicateRemovals":"true", "replicateUpdatesViaCopy":"true", "automatic_addional":"", "overflowtodisk":"true", "replicateAsynchronously":"true", "maxelementsondisk":"10000000", "listener_remoteObjectPort":"", "asynchronousReplicationIntervalMillis":"1000", "listener_hostName":"", "replicateUpdates":"true", "manual_hostName":"", "automatic_timeToLive":"the same subnet", "listener_port":"" }#';
	}

	private string function verifyCacheConnection(string name){
		try {
			admin
				action="verifyCacheConnection"
				type="web"
				password=request.webadminpassword
				name="#arguments.name#";
			hasError = "false"
		} catch( any e ){
			hasError = e.message;
		}
		return hasError;
	}

	private function removeEHCache(string name){
		admin
			action="removeCacheConnection"
			type="web"
			password=request.webadminpassword
			name="#arguments.name#";
	}
}