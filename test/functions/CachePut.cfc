/*
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
 */
 component extends="org.lucee.cfml.test.LuceeTestCase" labels="cache,ehCache" {
 	
	variables.cacheName="Test"&ListFirst(ListLast(getCurrentTemplatePath(),"\/"),".");
	
	public function testCachePutEHCache() {
		if(!isNull(request.testJBossExtension) and request.testJBossExtension) {
			createEHCache();
			testCachePut();
			deleteCache();
		}
	}

	public function testCachePutRAMCache() {
		if(!isNull(request.testJBossExtension) and request.testJBossExtension) {
			createRAMCache();
			testCachePut();
			deleteCache();
		}
	}

	public function testCachePutJBossCache() {
		if(!isNull(request.testJBossExtension) and request.testJBossExtension) {
			createJBossCache();
			testCachePut();
			deleteCache();
		}
	}

	
	private function testCachePut() localMode="modern" {
		server.enableCache=true;

		lock scope="server" timeout="10" {
			prefix=getTickCount();
			cachePut(prefix&'abc','123',CreateTimeSpan(0,0,0,1));
			cachePut(prefix&'def','123',CreateTimeSpan(0,0,0,2),CreateTimeSpan(0,0,0,1));
			cachePut(prefix&'ghi','123',CreateTimeSpan(0,0,0,0),CreateTimeSpan(0,0,0,0));

			sct={};
    		sct.a=cacheGet(prefix&'abc');
    		sct.b=cacheGet(prefix&'def');
    		sct.c=cacheGet(prefix&'ghi');
    		
    		assertEquals(true,structKeyExists(sct,'a'));
    		assertEquals(true, structKeyExists(sct,'b'));
    		assertEquals(true, structKeyExists(sct,'c'));

    		sleep(1200);
    		sct.d=cacheGet(prefix&'abc');
    		sct.e=cacheGet(prefix&'def');
    		sct.f=cacheGet(prefix&'ghi');

    		assertEquals(false,structKeyExists(sct,'d'));
    		assertEquals(false,structKeyExists(sct,'e'));
    		assertEquals(true,structKeyExists(sct,'f'));
    		
    		cachePut(prefix&'def','123',CreateTimeSpan(0,0,0,2),CreateTimeSpan(0,0,0,1),cacheName);

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