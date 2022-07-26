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


	public void function testTypeCheckingOnAppCFC(){
		local.uri=createURI("Jira1460/index.cfm");
		local.msg="";
		try {
			local.result=_InternalRequest(template:uri,url:{typeChecking:true});
		}
		catch(local.e){
			msg=e.message;
		}
		assertTrue(find("Cannot cast String [susi] to a value of type [boolean]",msg));
	}
	public void function testTypeCheckingOffAppCFC(){
		local.uri=createURI("Jira1460/index.cfm");
		local.result=_InternalRequest(template:uri,url:{typeChecking:false});
		assertEquals(200,result.status);
	}
	
	
	
	public void function testTypeCheckingOffAppTag(){
		var defaultSetting=getApplicationSettings().typeChecking;
		try{
			application action="update" typeChecking="false";
			a("susi");
			b();
		}
		finally {
			application action="update" typeChecking="#defaultSetting#";
		}
		
		//assertEquals(200,result.status_code);
	}
	
	
	public void function testTypeCheckingOnAppTag(){
		var defaultSetting=getApplicationSettings().typeChecking;
		try{
			application action="update" typeChecking="true";
			var err=false;
			try {
				a("susi");
			}
			catch(local.e){err=true;}
			assertEquals(true,err);
			
			var err=false;
			try {
				b();
				fail("must throw:casting exception");
			}
			catch(local.e){err=true;}
			assertEquals(true,err);
			
		}
		finally {
			application action="update" typeChecking="#defaultSetting#";
		}
		
		//assertEquals(200,result.status_code);
	}
	
	private void function a(boolean b){
		
	}
	private void function b() returntype="boolean" {
		return "susi";
	}
	
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
	
} 
</cfscript>