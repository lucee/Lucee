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

	public function setUp() {
		local.uri=createURI("Jira2588/index.cfm");
		local.result=_InternalRequest(
			template:uri,
			method:"post",
			header:{'content-type':'application/json; charset=utf-8'},
			body:'{"foo":"bar"}');
		//result=httpCall(calledName:"Jira2588/index.cfm",method:"post",body:'{"foo":"bar"}');
		body=evaluate(trim(result.filecontent));
	}

	public void function testHTTPReqData(){
		assertEquals('{"foo":"bar"}',body.HTTPReqData.content);
	}
	
	public void function testForm(){
		assertEquals('',structKeyList(body.FORM));
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
	
} 
</cfscript>