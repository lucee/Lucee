<!--- 
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function test() localmode=true {
		cacheName="ldev1280";
		
		qry=exeQuery();
		assertFalse(qry.isCached());
		
		qry=exeQuery();
		assertTrue(qry.isCached());

		cacheClear(tags:['peter','ueli'],cacheName:cacheName); // not matching tags
		qry=exeQuery();
		assertTrue(qry.isCached());
		
		cacheClear(tags:[],cacheName:cacheName); // not tags
		qry=exeQuery();
		assertTrue(qry.isCached());
		
		cacheClear(tags:['peter','urs'],cacheName:cacheName); // matching tags
		qry=exeQuery();
		assertFalse(qry.isCached());
	}

	private function exeQuery() localmode=true {
		q=query(a:[1,2,3,4]);
		query name="qry" cachedwithin=createTimespan(0,0,0,10) dbtype="query" tags=['susi','urs'] {
			echo('select * from q');
		}
		return qry;
	}

	public function setUp() {
		application action="update" 
			caches="#{

			ldev1280:{
			  class: 'lucee.runtime.cache.ram.RamCache'
			, storage: false
			, custom: {"timeToIdleSeconds":"10","timeToLiveSeconds":"10"}
			, default: 'query'
		}}#";
	
	//return true;
	}
} 
</cfscript>