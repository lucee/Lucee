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
	variables.name='testApplicationListener';
	public void function testApplicationListener(){
		uri=createURI("ApplicationListener/index.cfm");
		local.res=_InternalRequest(
			template:uri,
			urls:{name:variables.name}
		);
		// first request everything is triggered
		assertEquals("-onApplicationStart--onSessionStart-index.cfm",res.filecontent.trim());

		local.res=_InternalRequest(
			template:uri,
			urls:{name:variables.name},
			addToken:true
		);
		// because we pass non cfif/cftoken and did not before a new session context is generated
		assertEquals("-onSessionStart-index.cfm",res.filecontent.trim());


		local.res=_InternalRequest(
			template:uri,
			urls:{name:variables.name},
			addToken:true
		);
		// again the same cfid
		assertEquals("index.cfm",res.filecontent.trim());
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

} 
</cfscript>




