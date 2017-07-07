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
component extends="org.lucee.cfml.test.LuceeTestCase"	{



	//public function afterTests(){}
	
	public function setUp(){
		defineDatasource();
	}

	public void function testRequestCacheHandler(){
		
		query name="local.qry" cachedWithin="request" result="res" {
			echo("SELECT * FROM INFORMATION_SCHEMA.SYSTEM_USERS");
		}
		assertFalse(res.cached);

		query name="local.qry" cachedWithin="request" result="res" {
			echo("SELECT * FROM INFORMATION_SCHEMA.SYSTEM_USERS");
		}
		assertTrue(res.cached);
	}

	public void function testTimespanCacheHandler(){
		
		query name="local.qry" cachedWithin="#createTimespan(0,0,1,0)#" result="res" {
			echo("SELECT * FROM INFORMATION_SCHEMA.SYSTEM_USERS");
		}
		assertFalse(res.cached);

		query name="local.qry" cachedWithin="#createTimespan(0,0,1,0)#" result="res" {
			echo("SELECT * FROM INFORMATION_SCHEMA.SYSTEM_USERS");
		}
		assertTrue(res.cached);
	}

	private void function defineDatasource(){
		application action="update" 
			datasource="#{
	  		class: 'org.hsqldb.jdbcDriver'
			, bundleName: 'org.hsqldb.hsqldb'
			, bundleVersion: '2.3.2'
			, connectionString: 'jdbc:hsqldb:file:#getDirectoryFromPath(getCurrentTemplatePath())#/cacheHandler/db'
		}#";
	}




	public void function testApplicationCFM2(){
		uri=createURI("appCFM2/index.cfm");
		local.res=_InternalRequest(template:uri);
		assertEquals("12",res.filecontent.trim());

		local.res=_InternalRequest(template:uri);
		assertEquals("12",res.filecontent.trim());
	}


	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

} 
</cfscript>