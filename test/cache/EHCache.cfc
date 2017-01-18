<!--- 
 *
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.*
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
 ---><cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	
	//public function afterTests(){}
	
	public function setUp(){
		defineCache();
	}

	public void function test(){
		cachePut(id:'abc', value:'AAA', cacheName:'ehcache');
		var val=cacheget(id:'abc', cacheName:'ehcache');
		assertEquals("AAA",val);
		
	}

	public void function testTimespan() {
		
		var rightNow = Now();
		var testData = {"time": rightNow};
		var cacheId='jkijhiiuhkj';
		var cacheName='ehcache';

		// first we store the data
		cachePut(id=cacheId, value=testData, cacheName=cacheName);

		// getting back without waiting on it
		theValue = cacheGet(id=cacheId, cacheName=cacheName);
		wasFound = !isNull(theValue);
		assertTrue(wasFound);

		// getting back after at least a second
		sleep(1500); // take a nap
		theValue = cacheGet(id=cacheId, cacheName=cacheName);
		wasFound = !isNull(theValue);
		assertFalse(wasFound);		
	}

	private string function defineCache(){
		application action="update" 
			caches="#{ehcache: {
	  class: 'org.lucee.extension.cache.eh.EHCache'
	, bundleName: 'ehcache.extension'
	, storage: false
	, custom: {"bootstrapAsynchronously":"true","replicatePuts":"true","automatic_hostName":"",
		"bootstrapType":"on","maxelementsinmemory":"10000","manual_rmiUrls":"","distributed":"off",
		"automatic_multicastGroupAddress":"230.0.0.1","memoryevictionpolicy":"LRU","replicatePutsViaCopy":"true",
		"timeToIdleSeconds":"1","timeToLiveSeconds":"1","maximumChunkSizeBytes":"5000000","automatic_multicastGroupPort":"4446",
		"listener_socketTimeoutMillis":"120000",
		"diskpersistent":"true","manual_addional":"","replicateRemovals":"true",
		"replicateUpdatesViaCopy":"true","automatic_addional":"","overflowtodisk":"true","replicateAsynchronously":"true",
		"maxelementsondisk":"10000000","listener_remoteObjectPort":"","asynchronousReplicationIntervalMillis":"1000",
		"listener_hostName":"","replicateUpdates":"true","manual_hostName":"","automatic_timeToLive":"unrestricted","listener_port":""
	}
	, default: ''
}}#";
	
	return true;
	}

} 
</cfscript>