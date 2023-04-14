<!--- 
 *
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.*
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

	public void function testNoHTML(){
		local.uri=createURI("Issue0214/noHTML.cfm");
		local.result=_InternalRequest(uri);
		assertEquals("For-HeaderHello World",result.filecontent.trim());
	}

	public void function testNoHead(){
		local.uri=createURI("Issue0214/noHead.cfm");
		local.result=_InternalRequest(uri);
		assertEquals("For-Header<html>Hello World</html>",result.filecontent.trim());
	}

	public void function testWithHead(){
		local.uri=createURI("Issue0214/withHead.cfm");
		local.result=_InternalRequest(uri);
		assertEquals("<html><head>For-Header</head>Hello World</html>",result.filecontent.trim());
	}
	
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
	
} 
</cfscript>