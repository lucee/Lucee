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
	
	<cffunction name="testCachePutEHCache" localMode="modern">
		<cfset createEHCache()>
		<cfset testCachePut()>
		<cfset deleteCache()>
	</cffunction>

	<cffunction name="testCachePutRAMCache" localMode="modern">
		<cfset createRAMCache()>
		<cfset testCachePut()>
		<cfset deleteCache()>
	</cffunction>

	<cffunction name="testCachePutJBossCache" localMode="modern">
		<cfif !isNull(request.testJBossExtension) and request.testJBossExtension>
			<cfset createJBossCache()>
			<cfset testCachePut()>
			<cfset deleteCache()>
		</cfif>
	</cffunction>

	
	<cffunction access="private" name="testCachePut" localMode="modern">

<!--- begin old test code --->
<cfset server.enableCache=true>

<cflock scope="server" timeout="10">
	<!--- <cfset cacheRemove(arrayToList(cacheGetAllIds()))> --->
	<cfset prefix=getTickCount()>

	<cfset cachePut(prefix&'abc','123',CreateTimeSpan(0,0,0,1))>
	<cfset cachePut(prefix&'def','123',CreateTimeSpan(0,0,0,2),CreateTimeSpan(0,0,0,1))>
	<cfset cachePut(prefix&'ghi','123',CreateTimeSpan(0,0,0,0),CreateTimeSpan(0,0,0,0))>
    
	<cfset sct={}>
    <cfset sct.a=cacheGet(prefix&'abc')>
    <cfset sct.b=cacheGet(prefix&'def')>
    <cfset sct.c=cacheGet(prefix&'ghi')>
    
    <cfset valueEquals(left="#structKeyExists(sct,'a')#", right="true")>
    <cfset valueEquals(left="#structKeyExists(sct,'b')#", right="true")>
    <cfset valueEquals(left="#structKeyExists(sct,'c')#", right="true")>
    <cfset sleep(1200)>
    <cfset sct.d=cacheGet(prefix&'abc')>
    <cfset sct.e=cacheGet(prefix&'def')>
    <cfset sct.f=cacheGet(prefix&'ghi')>
    <cfset valueEquals(left="#structKeyExists(sct,'d')#", right="false")>
    <cfset valueEquals(left="#structKeyExists(sct,'e')#", right="false")>
    <cfset valueEquals(left="#structKeyExists(sct,'f')#", right="true")>
    
<cfif server.ColdFusion.ProductName EQ "lucee">    
	<cfset cachePut(prefix&'def','123',CreateTimeSpan(0,0,0,2),CreateTimeSpan(0,0,0,1),cacheName)>
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


		
				
	private function deleteCache(){
		admin 
			action="removeCacheConnection"
			type="web"
			password="#request.webadminpassword#"
			name="#cacheName#";
						
	}
</cfscript>	
</cfcomponent>