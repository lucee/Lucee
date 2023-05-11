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
component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm"	{


	public void function testCompositeId(){
		// first call only initialize the data
		local.uri=createURI("Jira2644/one.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(200,result.status);
		assertEquals('',trim(result.filecontent));
		
		// now get the result
		local.uri=createURI("Jira2644/two.cfm");
		local.result=_InternalRequest(uri);
		local.res=evaluate(trim(result.filecontent));
		
		assertEquals('CA',res.getStateCode()&"");
		assertEquals('US',res.getCountryCode()&"");	
	}
	
	
	public void function testCompositeId2(){
		// first call only initialize the data
		local.uri=createURI("Jira2644/bestseller.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(200,result.status);
	}
	
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
	
} 
</cfscript>