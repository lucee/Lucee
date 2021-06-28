<!--- 
 *
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.
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
 ---><cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm"	{

	//public function setUp(){}

	public void function testWithDefaultSettings(){
		// explicit and transaction
		local.uri=createURI("LDEV1370/index.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(200,result.status);
		assertEquals("inital:1:1;explicit:1:1:explicit change;transaction:1:1:transaction change;",trim(result.fileContent));

		// after end (second request)
		local.uri=createURI("LDEV1370/result.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(200,result.status);
		assertEquals("end:1:1:request end;",trim(result.fileContent));

	}

	public void function testWithSettingsTrue(){
		// explicit and transaction
		local.uri=createURI("LDEV1370/index.cfm");
		local.result=_InternalRequest(template:uri,urls:{flushAtRequestEnd:1,autoManageSession:1});
		assertEquals(200,result.status);
		assertEquals("inital:1:1;explicit:1:1:explicit change;transaction:1:1:transaction change;",trim(result.fileContent));

		// after end (second request)
		local.uri=createURI("LDEV1370/result.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(200,result.status);
		assertEquals("end:1:1:request end;",trim(result.fileContent));

	}

	public void function testWithSettingsFalse(){
		// explicit and transaction
		local.uri=createURI("LDEV1370/index.cfm");
		local.result=_InternalRequest(template:uri,urls:{flushAtRequestEnd:0,autoManageSession:0});
		assertEquals(200,result.status);
		assertEquals("inital:1:1;explicit:1:1:explicit change;transaction:1:1:transaction change;",trim(result.fileContent));

		// after end (second request)
		local.uri=createURI("LDEV1370/result.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(200,result.status);
		assertEquals("end:1:1:transaction change;",trim(result.fileContent));

	}
	
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
	
} 
</cfscript>