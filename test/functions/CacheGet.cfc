component extends="org.lucee.cfml.test.LuceeTestCase" labels="cache,ehCache" {
	function run( testResults , testBox ) {
		describe( title="Test suite for CacheGet()", body=function() {
			variables.cacheName="Test"&ListFirst(ListLast(getCurrentTemplatePath(),"\/"),".");
			afterEach(function( currentSpec ){
				testCacheGet();
				deleteCache();
			});
			it(title="Checking testCacheGetEHCache()", body = function( currentSpec ) {
				createEHCache();
			});
			it(title="Checking testCacheGetJBossCache()", body = function( currentSpec ) {
				if(!isNull(request.testJBossExtension) and request.testJBossExtension){
					createJBossCache();
				}
			});
			it(title="Checking testCacheGetRAMCache()", body = function( currentSpec ) {
				createRAMCache();
			});
		});
	}

	private function testCacheGet(){
		lock timeout="1" scope="server" { 
			cacheRemove(arrayToList(cacheGetAllIds()));
			cachePut('abc','123');

			assertEquals("123",cacheGet('abc'));
			cacheGetKey=cacheGet('def');

			assertEquals("false",structKeyExists(variables,'cacheGetKey') and !isNull(variables.cacheGetKey));

			try{
		        cacheGet('def',true);
		        fail("must throw:there is no entry in cache with key [DEF]");
			} catch(any e){}
			assertEquals("123",cacheGet('abc',false));
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