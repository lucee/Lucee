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
 --->
 component extends="org.lucee.cfml.test.LuceeTestCase"	{

	function beforeAll() {
		// stash the existing setting to restore afterwards
		admin 
			action="getScope"
			type="server"
			password="#server.SERVERADMINPASSWORD#"
			returnVariable="local.scope";
		variables.defaultScopeSettings = scope;

		local.result = _internalRequest(template: createURI("cfidStorage/urlCookie/index.cfm"));
		variables.session1 = local.result.session.cfid;
		local.result = _internalRequest(template: createURI("cfidStorage/urlCookie/index.cfm"));
		variables.session2 = local.result.session.cfid;
		systemOutput("", true);
		systemOutput(variables.session1, true);
		systemOutput(variables.session2, true);
	}

	function run( testResults , testBox ) {
		variables.defaultScopeSettings = "";
		
		describe(title="Testing cfid Storage options", asyncAll=false, body=function(){

			it ( "test urlBeforeCookie", function(){
				// lucee should use the url.cfid

				local.urlCFID = variables.session1;
				local.result = _internalRequest(
					template: createURI("cfidStorage/urlCookie/index.cfm"),
					url: {
						cfid: local.urlCFID,
						cftoken: 0
					}
				); // this returns json

				local.status = deserializeJson( local.result.fileContent );

				assertEquals( local.status.cfidStorage, "urlBeforeCookie" );
				assertNotEquals( local.status.sessionCFID, local.urlCFID );

				//just pass in a cookie
				local.result = _internalRequest(
					template: createURI("cfidStorage/urlCookie/index.cfm"),
					cookie: {
						cfid: local.urlCFID,
						cftoken: 0
					}
				); // this returns json

				local.status = deserializeJson( local.result.fileContent );
				systemOutput(status);
				assertEquals( local.status.cfidStorage, "urlBeforeCookie" );
				assertEquals( local.status.sessionCFID, local.urlCFID );
				assertNotEquals( local.status.sessionCFID, local.status.urlCFID );

				// now test with both a url cfid and a cookie cfid, cookie takes precendence 
				local.cookies = {
					cfid: variables.session1,
					cftoken: 0
				};

				local.urlCFID = variables.session2;
				local.result = _internalRequest(
					template: createURI("cfidStorage/urlCookie/index.cfm"),
					cookies: local.cookies,
					url: {
						cfid: local.urlCFID,
						cftoken: 0
					}
				); // this returns json

				local.status = deserializeJson( local.result.fileContent );

				assertEquals( local.status.cfidStorage, "urlBeforeCookie" );
				assertEquals( local.status.sessionCFID, local.cookies.cfid ); // i.e. not the url cfid
				assertNotEquals( local.status.sessionCFID, local.urlCFID ); 
				
			});
		});
	}

	function afterAll() {
		admin
			action="updateScope"
			type="server"
			password="#server.SERVERADMINPASSWORD#"
			attributeCollection=#variables.defaultScopeSettings#;
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}


}