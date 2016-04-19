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

	

	public void function testTagAttrApplication_cfc1(){
		local.uri=createURI("Jira2763/index.cfm");
		local.result=_InternalRequest(template=uri,urls={trim=true});
		assertEquals("-a-",trim(result.filecontent));
	}
	public void function testTagAttrApplication_cfc2(){
		local.uri=createURI("Jira2763/index.cfm");
		local.result=_InternalRequest(template=uri,urls={trim=false});
		assertEquals("- a -",trim(result.filecontent));
	}
	
	public void function testTagAttrCFApplication1(){
		application action="update" tag="#{savecontent:{trim:false}}#";
		savecontent variable="local.c" {
			echo(" a ");
		}
		assertEquals(" a ",c);
	}
	
	public void function testTagAttrCFApplication2(){
		application action="update" tag="#{savecontent:{trim:true}}#";
		savecontent variable="local.c" {
			echo(" a ");
		}
		assertEquals("a",c);
	}
	
	public void function testTagAttrCFApplication3(){
		application action="update" tag="#{cfsavecontent:{trim:true}}#";
		savecontent variable="local.c" {
			echo(" a ");
		}
		assertEquals("a",c);
	}
	
	public void function testTagAttrCFApplication4(){
		application action="update" tag="#{cfsavecontent:{trim:false}}#";
		savecontent variable="local.c" {
			echo(" a ");
		}
		assertEquals(" a ",c);
	}
	
	
	
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
	
} 
</cfscript>