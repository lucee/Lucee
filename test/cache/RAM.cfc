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
		cachePut(id:'abc', value:'AAA', cacheName:'susi');
		var val=cacheget(id:'abc', cacheName:'susi');
		assertEquals("AAA",val);
		
	}

	private string function defineCache(){
		application action="update" 
			caches="#{susi:{
		  class: 'lucee.runtime.cache.ram.RamCache'
		, storage: false
		, custom: {"timeToIdleSeconds":"0","timeToLiveSeconds":"0"}
		, default: 'function'
	}}#";
	
	return true;
	}

} 
</cfscript>