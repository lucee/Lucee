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


	public void function test1(){
		if(!hasCredentials()) return;
		local.uri=createURI("LDEV0078/test1.cfm");
		local.result=_InternalRequest(uri);
		assertEquals("true",trim(result.filecontent));
	}

	public void function test2(){
		if(!hasCredentials()) return;
		local.uri=createURI("LDEV0078/test2.cfm");
		local.result=_InternalRequest(uri);
		assertEquals("true",trim(result.filecontent));
	}

	public void function test3(){
		if(!hasCredentials()) return;
		local.uri=createURI("LDEV0078/test3.cfm");
		local.result=_InternalRequest(uri);
		assertEquals("true",trim(result.filecontent));
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private boolean function hasCredentials() {
		return structCount(server.getDatasource("mysql"));
	}
	
} 
</cfscript>