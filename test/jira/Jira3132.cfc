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
 ---><cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase"	{

	//public function setUp(){}

	// calling via http is necessary because we have a template exception
	public void function testTagTryCatchFinally(){
		local.uri=createURI("Jira3132/index.cfm");
		local.result=_InternalRequest(template=uri,urls={tagtcf=true});
		assertEquals("tag:in finally;",trim(result.filecontent));
	}
	public void function testTagTryFinally(){
		local.uri=createURI("Jira3132/index.cfm");
		local.result=_InternalRequest(template=uri,urls={tagtf=true});
		assertEquals("tag:in finally;",trim(result.filecontent));
	}
	public void function testScriptTryCatchFinally(){
		local.uri=createURI("Jira3132/index.cfm");
		local.result=_InternalRequest(template=uri,urls={scripttcf=true});
		assertEquals("script:in finally;",trim(result.filecontent));
	}
	public void function testScriptTryFinally(){
		local.uri=createURI("Jira3132/index.cfm");
		local.result=_InternalRequest(template=uri,urls={scripttf=true});
		assertEquals("script:in finally;",trim(result.filecontent));
	}
	
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
	
} 
</cfscript>