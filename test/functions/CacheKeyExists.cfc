component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for cacheKeyExists()", body=function() {
			variables.cacheName="Test"&ListFirst(ListLast(getCurrentTemplatePath(),"\/"),".");
			afterEach(function( currentSpec ){
				if(currentSpec != 'checking testCacheKeyExistsJBossCache()'){
					testCacheKeyExists();
					deleteCache();
				}
			});
			it(title="Checking testCacheKeyExistsEHCache()", body = function( currentSpec ) {
				createEHCache();
			});
			it(title="Checking testCacheKeyExistsJBossCache()", body = function( currentSpec ) {
				if(!isNull(request.testJBossExtension) and request.testJBossExtension){
					createJBossCache();
					testCacheKeyExists();
					deleteCache();
				}
			});
			it(title="Checking testCacheKeyExistsRAMCache()", body = function( currentSpec ) {
				createRAMCache();
			});
		});
	}

	private function testCacheKeyExists(){
		lock timeout="1" scope="server" { 
			cacheClear();
			cachePut('abc','123');
		    valueEquals("true","#cacheKeyExists('abc')#");
		    valueEquals("false","#cacheKeyExists('def')#");
		    valueEquals("false","#cacheKeyExists('def',cacheName)#");
		}
	}
	
	<cffunction access="private" name="testCacheKeyExists" localMode="modern">

<!--- begin old test code --->
<cfif server.ColdFusion.ProductName EQ "lucee">
<cflock scope="server" timeout="1">
	<cfset cacheClear()>
	
	<cfset cachePut('abc','123')>
    <cfset valueEquals(left="#cacheKeyExists('abc')#", right="true")>
    <cfset valueEquals(left="#cacheKeyExists('def')#", right="false")>
    <cfset valueEquals(left="#cacheKeyExists('def',cacheName)#", right="false")>
</cflock>

</cfif>

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
<cfscript>
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
</cfscript>	
</cfcomponent>