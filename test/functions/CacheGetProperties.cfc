component extends="org.lucee.cfml.test.LuceeTestCase" labels="cache,ehCache" {
	function run( testResults , testBox ) {
		describe( title="Test suite for CacheGetProperties()", body=function() {
			variables.cacheName="Test"&ListFirst(ListLast(getCurrentTemplatePath(),"\/"),".");
			afterEach(function( currentSpec ){
				if(currentSpec != "Checking testCacheGetPropertiesJBossCache()") {
					testCacheGetProperties();
					deleteCache();
				}
			});
			it(title="Checking testCacheGetPropertiesEHCache()", body = function( currentSpec ) {
				createEHCache();
			});
			it(title="Checking testCacheGetPropertiesJBossCache()", body = function( currentSpec ) {
				if(!isNull(request.testJBossExtension) and request.testJBossExtension){
					createJBossCache();
					testCacheGetProperties();
					deleteCache();
				}
			});
			it(title="Checking testCacheGetPropertiesRAMCache()", body = function( currentSpec ) {
				createRAMCache();
			});
		});
	}

	private function testCacheGetProperties(){
		lock timeout="1" scope="server" { 
			cacheRemove(arrayToList(cacheGetAllIds()));
			cachePut('abc','123');
			cacheGetProperties();
			cacheGetProperties('object');
			cacheGetProperties(cacheName);
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