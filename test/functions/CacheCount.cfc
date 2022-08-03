component extends="org.lucee.cfml.test.LuceeTestCase" labels="cache,ehCache" {
	function run( testResults , testBox ) {
		describe( title="Test suite for CacheCount()", body=function() {
			variables.cacheName="Test"&ListFirst(ListLast(getCurrentTemplatePath(),"\/"),".");
			afterEach(function( currentSpec ){
				testCacheCount();
				deleteCache();
			});
			it(title="Checking testCacheCountEHCache()", body = function( currentSpec ) {
				createEHCache();
			});
			it(title="Checking testCacheCountJBossCache()", body = function( currentSpec ) {
				if(!isNull(request.testJBossExtension) and request.testJBossExtension){
					createJBossCache();
				}
			});
			it(title="Checking testCacheCountRAMCache()", body = function( currentSpec ) {
				createRAMCache();
			});
		});
	}

	private function testCacheCount(){
		lock timeout="1" scope="server" { 
			cacheClear(); 
			cachePut('abc','123');
			assertEquals("1",cacheCount());
			cacheClear();
			assertEquals("0",cacheCount());
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