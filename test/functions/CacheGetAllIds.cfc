component extends="org.lucee.cfml.test.LuceeTestCase" labels="cache,ehCache" {
	function run( testResults , testBox ) {
		describe( title="Test suite for CacheGetAllIds()", body=function() {
			variables.cacheName="Test"&ListFirst(ListLast(getCurrentTemplatePath(),"\/"),".");
			afterEach(function( currentSpec ){
				testCacheGetAllIds();
				deleteCache();
			});
			it(title="Checking testCacheGetAllIdsEHCache()", body = function( currentSpec ) {
				createEHCache();
			});
			it(title="Checking testCacheGetAllIdsJBossCache()", body = function( currentSpec ) {
				if(!isNull(request.testJBossExtension) and request.testJBossExtension){
					createJBossCache();
				}
			});
			it(title="Checking testCacheGetAllIdsRAMCache()", body = function( currentSpec ) {
				createRAMCache();
			});
		});
	}

	private function testCacheGetAllIds(){
		server.enableCache=true ;
		lock timeout="1" scope="server" { 
			cacheRemove(arrayToList(cacheGetAllIds()));
			cachePut('abc','123');
			cachePut('def','123');
		    assertEquals("ABC,DEF",ListSort(arrayToList(cacheGetAllIds()),'textnocase'));
			cacheClear();
			cachePut('abc','123');
			cachePut('abd','123');
			cachePut('def','123');
		    assertEquals("ABC,ABD",ListSort(arrayToList(cacheGetAllIds("ab*")),'textnocase'));
		    assertEquals("ABC,ABD",ListSort(arrayToList(cacheGetAllIds("ab*")),'textnocase'));
		}
	}

	private function createRAMCache(){
		admin 
			action="updateCacheConnection"
			type="web"
			password="#request.webadminpassword#"
			
			
			name="#cacheName#" 
			class="lucee.runtime.cache.ram.RamCache" 
			storage="false"
			default="object" 
			custom="#{timeToLiveSeconds:86400
				,timeToIdleSeconds:86400}#";
	}
	
	private function createEHCache() {
		admin 
			action="updateCacheConnection"
			type="web"
			password="#request.webadminpassword#"
			
			
			name="#cacheName#" 
			class="org.lucee.extension.cache.eh.EHCache" 
			storage="false"
			default="object" 
			custom="#{timeToLiveSeconds:86400
				,maxelementsondisk:10000000
				,distributed:"off"
				,overflowtodisk:true
				,maximumChunkSizeBytes:5000000
				,timeToIdleSeconds:86400
				,maxelementsinmemory:10000
				,asynchronousReplicationIntervalMillis:1000
				,diskpersistent:true
				,memoryevictionpolicy:"LRU"}#";
	}
		
	private function createJBossCache() {
		admin 
			action="updateCacheConnection"
			type="web"
			password="#request.webadminpassword#"
			
			default="object"
			name="#cacheName#" 
			class="lucee.extension.cache.jboss.JBossCache" 
			storage="false"
			custom="#{timeToLiveSeconds:86400.0
				,minTimeToLiveSeconds:0
				,minElementsInMemory:0
				,memoryEvictionPolicy:"LRU"
				,timeToIdleSeconds:86400
				,maxElementsInMemory:10000}#";
	}
				
	private function deleteCache(){
		admin 
			action="removeCacheConnection"
			type="web"
			password="#request.webadminpassword#"
			name="#cacheName#";
						
	}
}