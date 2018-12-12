<!--- 
 *
 * Copyright (c) 2014, the Railo Company LLC. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 ---><cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfset variables.cacheName="Test"&ListFirst(ListLast(getCurrentTemplatePath(),"\/"),".")>
	
	<cffunction name="testCacheGetPropertiesEHCache" localMode="modern">
		<cfset createEHCache()>
		<cfset testCacheGetProperties()>
		<cfset deleteCache()>
	</cffunction>
	<cffunction name="testCacheGetPropertiesJBossCache" localMode="modern">
		<cfif !isNull(request.testJBossExtension) and request.testJBossExtension>
			<cfset createJBossCache()>
			<cfset testCacheGetProperties()>
			<cfset deleteCache()>
		</cfif>
	</cffunction>
	<cffunction name="testCacheGetPropertiesRedisCache" localMode="modern">
		<cfif !variables.hasRedis><cfreturn></cfif>
		<cfset createRedisCache()>
		<cfset testCacheGetProperties()>
		<cfset deleteCache()>
	</cffunction>
	<cffunction name="testCacheGetPropertiesRAMCache" localMode="modern">
		<cfset createRAMCache()>
		<cfset testCacheGetProperties()>
		<cfset deleteCache()>
	</cffunction>
	
	<cffunction access="private" name="testCacheGetProperties" localMode="modern">

<!--- begin old test code --->
<cflock scope="server" timeout="1">


	<cfset cacheRemove(arrayToList(cacheGetAllIds()))>
	
	<cfset cachePut('abc','123')>
	<cfset cacheGetProperties()>
	<cfset cacheGetProperties('object')>
	
	<cfif server.ColdFusion.ProductName EQ "lucee"> 
		<cfset cacheGetProperties(cacheName)>
    </cfif>
</cflock>

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

		
	private function createRedisCache() {
		admin 
				action="updateCacheConnection"
				type="web"
				password="#request.webadminpassword#"
				
				default="object"
				name="#cacheName#" 
				class="lucee.extension.io.cache.redis.simple.RedisCache" 
				bundleName= 'redis.extension'
				bundleVersion= '2.9.0.2-BETA'
				storage="false"
				custom='#{"minIdle":"0",
				"maxTotal":"8",
				"maxIdle":"10",
				"host":"localhost",
				"password":variables.redis.password, 
				"port":variables.redis.port,
				"timeout":"2000",
				"timeToLiveSeconds":"60"}#';
	}

	private struct function getRedisCredencials() {
		// getting the credetials from the enviroment variables
		var sct={};

		if(
			!isNull(server.system.environment.REDIS_PORT) && 
			!isNull(server.system.environment.REDIS_PASSWORD)) {
			sct.port=server.system.environment.REDIS_PORT;
			sct.password=server.system.environment.REDIS_PASSWORD;
		}
		// getting the credetials from the system variables
		else if(
			!isNull(server.system.properties.REDIS_PORT) && 
			!isNull(server.system.properties.REDIS_PASSWORD)) {
			sct.port=server.system.properties.REDIS_PORT;
			sct.password=server.system.properties.REDIS_PASSWORD;
		}
		return sct;
	}

	public function setUp(){
		variables.redis=getRedisCredencials();	
		variables.hasRedis=structCount(variables.redis);		
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