component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	variables.cacheName="Test"&ListFirst(ListLast(getCurrentTemplatePath(),"\/"),".");

	variables.parentFolder=getDirectoryFromPath(getCurrentTemplatePath())&"/datasource/";
	variables.datasourceFolder=variables.parentFolder&"cacheClear/";


	private struct function getMongoDBCredentials() {
		// getting the credentials from the environment variables
		return server.getDatasource("mongoDB");
	}

	private void function defineDatasource(id, boolean asMongo=false){
		var credentials=getMongoDBCredentials();
		if(asMongo && structCount(credentials)) {
			//systemOutput("testing as mongo",1,1);
			
			admin 
				action="updateCacheConnection"
				type="web"
				password="#request.webadminpassword#"
				name="_cacheClear"&id 
				class= 'org.lucee.mongodb.cache.MongoDBCache'
				bundleName= 'mongodb.extension'
				storage="false"
				default="object" 
				custom="#{
						"collection":"testcacheclear",
						"password":credentials.pass,
						"connectionsPerHost":"10",
						"database":"test",
						"hosts":credentials.server&":"&credentials.port,
						"persist":"true",
						"username":credentials.user
						}#";
		}
		else {
			admin 
				action="updateCacheConnection"
				type="web"
				password="#request.webadminpassword#"
				name="_cacheClear"&id 
				class="lucee.runtime.cache.ram.RamCache" 
				storage="false"
				default="object" 
				custom="#{timeToLiveSeconds:86400,timeToIdleSeconds:86400}#";
		}



		if(!directoryExists(variables.datasourceFolder)) directoryCreate(variables.datasourceFolder);
		application action="update" 
			cache={query:"_cacheClear"&id}
			datasources={
				'cacheClear_1':{
					class: 'org.h2.Driver'
					, bundleName: 'org.h2'
					, bundleVersion: '1.3.172'
					, connectionString: 'jdbc:h2:#variables.datasourceFolder#/cacheClear_1#id#;MODE=MySQL'
					, connectionLimit:100 // default:-1
				}
				,'cacheClear_2':{
					class: 'org.h2.Driver'
					, bundleName: 'org.h2'
					, bundleVersion: '1.3.172'
					, connectionString: 'jdbc:h2:#variables.datasourceFolder#/cacheClear_2#id#;MODE=MySQL'
					, connectionLimit:100 // default:-1
				}
			}
		;
	}


	function testCacheClearTagsRAM() localmode=true {
		testCacheClearTags(false);
	}
	/*function testCacheClearTagsMongo() localmode=true {
		testCacheClearTags(true);
	}*/

	private function testCacheClearTags(boolean asMongo=false) localmode=true {
		var id=createUniqueId();
		try {
			defineDatasource(id,asMongo);
			
			idsBefore=cacheGetAllIds(cacheName:"_cacheClear"&id);
			before=arrayLen(idsBefore);
			
			query cachedwithin=createTimeSpan(0,0,1,0) name="qry1" datasource="cacheClear_1" tags=['tables'] {
				echo('SELECT top 1 TABLE_NAME as tn,''cacheClear_1'' as ds FROM  INFORMATION_SCHEMA.TABLES');
			}
			query cachedwithin=createTimeSpan(0,0,1,0) name="qry1" datasource="cacheClear_2" tags=['tables'] {
				echo('SELECT top 1 TABLE_NAME as tn,''cacheClear_2'' as ds FROM  INFORMATION_SCHEMA.TABLES');
			}

			query cachedwithin=createTimeSpan(0,0,1,0) name="qry1" datasource="cacheClear_1" tags=['tables2'] {
				echo('SELECT top 1 TABLE_NAME as tn,''cacheClear_111'' as ds FROM  INFORMATION_SCHEMA.TABLES');
			}
			query cachedwithin=createTimeSpan(0,0,1,0) name="qry1" datasource="cacheClear_2" tags=['tables2'] {
				echo('SELECT top 1 TABLE_NAME as tn,''cacheClear_222'' as ds FROM  INFORMATION_SCHEMA.TABLES');
			}
			idsAfter.a=cacheGetAllIds(cacheName:"_cacheClear"&id);
			assertEquals(4,arrayLen(idsAfter.a)-before);
			
			cacheClear(['invalid'],"_cacheClear"&id);
			idsAfter.b=cacheGetAllIds(cacheName:"_cacheClear"&id);
			assertEquals(4,arrayLen(idsAfter.b)-before);
			
			cacheClear(['tables2'],"_cacheClear"&id);
			idsAfter.c=cacheGetAllIds(cacheName:"_cacheClear"&id);
			//SystemOutput(idsAfter,1,1);
			assertEquals(2,arrayLen(idsAfter.c)-before);
			

			cacheClear({datasource:"cacheClear_1",tags:['tables']},"_cacheClear"&id);
			idsAfter.d=cacheGetAllIds(cacheName:"_cacheClear"&id);
			assertEquals(1,arrayLen(idsAfter.d)-before);
			
		}
		finally {
			cacheClear(cacheName:"_cacheClear"&id);
			directoryDelete(variables.datasourceFolder,true);
		}
	}



	function testCacheClearEHCache() {
		createEHCache();
		testCacheClear();
		deleteCache();
	}
	function testCacheClearRAMCache() {
		createRAMCache();
		testCacheClear();
		deleteCache();
	}
	function testCacheClearJBossCache() {
		if(!isNull(request.testJBossExtension) and request.testJBossExtension) {
			createJBossCache();
			testCacheClear();
			deleteCache();
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

	private function testCacheClear() localmode=true {
		lock scope="server" timeout="1" {
			cacheClear();
			cachePut('abc','123');
		    assertEquals("#cacheCount()#", "1");
		    cacheClear();
		    assertEquals("#cacheCount()#", "0");
		    
		    cachePut('abc','123');
		    assertEquals("#cacheCount()#", "1");
		    cacheClear("*");
		    assertEquals("#cacheCount()#", "0");
		    
		    cacheClear("",cacheName);
		    cachePut('abc','123',CreateTimeSpan(1,1,1,1),CreateTimeSpan(1,1,1,1),cacheName);
		    assertEquals("#cacheCount(cacheName)#", "1");
		    cacheClear("",cacheName);
		    assertEquals("#cacheCount(cacheName)#", "0");
		    
		    cachePut('abc','123',CreateTimeSpan(1,1,1,1),CreateTimeSpan(1,1,1,1),cacheName);
		    cachePut('abe','456',CreateTimeSpan(1,1,1,1),CreateTimeSpan(1,1,1,1),cacheName);
		    cachePut('afg','789',CreateTimeSpan(1,1,1,1),CreateTimeSpan(1,1,1,1),cacheName);

		    assertEquals("#cacheCount(cacheName)#", "3");
		    cacheClear("ab*",cacheName);
		    assertEquals("#cacheCount(cacheName)#", "1");
    	}
	}
}