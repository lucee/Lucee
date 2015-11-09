<!--- 
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.*
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

	public void function testAppCFCWritable(){
		local.result=_InternalRequest(
			template:createURI("LDEV0372/appcfc/index.cfm"),
			urls:{readonly:false});
		assertEquals("Susanne-test-80",result.filecontent.trim());
	}
	public void function testAppCFCReadonly(){
		local.result=_InternalRequest(
			template:createURI("LDEV0372/appcfc/index.cfm"),
			urls:{readonly:true});
		assertEquals("can't set key [susi] to struct, struct is readonly",result.filecontent.trim());
	}

	public void function testAppCFCDefault(){
		local.result=_InternalRequest(
			template:createURI("LDEV0372/appcfc/index.cfm"));
		
		var readOnly=getPageContext().getConfig().getCGIScopeReadOnly();
		assertEquals(
			readOnly?
			"can't set key [susi] to struct, struct is readonly":
			"Susanne-test-80",
			result.filecontent.trim());
	}


	public void function testAppCFMDefault(){
		local.result=_InternalRequest(
			template:createURI("LDEV0372/appcfm/index.cfm"));
		
		var readOnly=getPageContext().getConfig().getCGIScopeReadOnly();

		assertEquals(
			readOnly?
			"can't set key [susi] to struct, struct is readonly":
			"Susanne-test-80",
			result.filecontent.trim());
	}

	public void function testAppCFMWritable(){
		local.result=_InternalRequest(
			template:createURI("LDEV0372/appcfm/index.cfm"),
			urls:{readonly:false});

		assertEquals("Susanne-test-80",result.filecontent.trim());
	}

	public void function testAppCFMReadonly(){
		local.result=_InternalRequest(
			template:createURI("LDEV0372/appcfm/index.cfm"),
			urls:{readonly:true});

		assertEquals("can't set key [susi] to struct, struct is readonly",result.filecontent.trim());
	}
	
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
} 
</cfscript>