component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	variables.cacheName="Test"&ListFirst(ListLast(getCurrentTemplatePath(),"\/"),".");

	variables.parentFolder=getDirectoryFromPath(getCurrentTemplatePath())&"/datasource/";
	variables.datasourceFolder=variables.parentFolder&"cacheClear/";

	private void function defineDatasource(id){
		admin 
			action="updateCacheConnection"
			type="web"
			password="#request.webadminpassword#"
			name="_cacheClear"&id 
			class="lucee.runtime.cache.ram.RamCache" 
			storage="false"
			default="object" 
			custom="#{timeToLiveSeconds:86400,timeToIdleSeconds:86400}#";

		if(!directoryExists(variables.datasourceFolder)) directoryCreate(variables.datasourceFolder);
		
		application 
			action="update" 
			cache={
				query:"_cacheClear"&id
			}
			
		datasources="#{
			'cacheClear_1':{
		  		class: 'org.hsqldb.jdbcDriver'
				, bundleName: 'org.hsqldb.hsqldb'
				, bundleVersion: '2.3.2'
				, connectionString: 'jdbc:hsqldb:file:#variables.datasourceFolder#/cacheClear_1'&id
			}
			,'cacheClear_2':{
		  		class: 'org.hsqldb.jdbcDriver'
				, bundleName: 'org.hsqldb.hsqldb'
				, bundleVersion: '2.3.2'
				, connectionString: 'jdbc:hsqldb:file:#variables.datasourceFolder#/cacheClear_2'&id
			}
		}#";
	}

	function testCacheClearTags() localmode=true {
		var id=createUniqueId();
		try {
			defineDatasource(id);
			
			idsBefore=cacheGetAllIds(cacheName:"_cacheClear"&id);
			before=arrayLen(idsBefore);
			assertEquals(0,before);
			
			
			query cachedwithin=createTimeSpan(0,0,1,0) name="qry1" datasource="cacheClear_1" tags=['tables'] {
				echo('SELECT top 1 TABLE_NAME as tn,''cacheClear_1'' as ds FROM  INFORMATION_SCHEMA.SYSTEM_TABLES');
			}
			query cachedwithin=createTimeSpan(0,0,1,0) name="qry1" datasource="cacheClear_2" tags=['tables'] {
				echo('SELECT top 1 TABLE_NAME as tn,''cacheClear_2'' as ds FROM  INFORMATION_SCHEMA.SYSTEM_TABLES');
			}

			query cachedwithin=createTimeSpan(0,0,1,0) name="qry1" datasource="cacheClear_1" tags=['tables2'] {
				echo('SELECT top 1 TABLE_NAME as tn,''cacheClear_111'' as ds FROM  INFORMATION_SCHEMA.SYSTEM_TABLES');
			}
			query cachedwithin=createTimeSpan(0,0,1,0) name="qry1" datasource="cacheClear_2" tags=['tables2'] {
				echo('SELECT top 1 TABLE_NAME as tn,''cacheClear_222'' as ds FROM  INFORMATION_SCHEMA.SYSTEM_TABLES');
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