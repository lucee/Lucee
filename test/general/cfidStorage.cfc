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
		admin action="getScope"
			type="server"
			password="#server.SERVERADMINPASSWORD#"
			returnVariable="local.scope";
		variables.defaultScopeSettings = scope;
	}

	function run( testResults , testBox ) {
		variables.defaultScopeSettings = "";
		
		describe(title="Testing cfid Storage options", asyncAll=false, body=function(){
			local.uri = createURI("cfidStorage/index.cfm");

			it("test cookieOnly", function(){
				// lucee should completely ignore the url.cfid
				changeCfidStorage( "cookieOnly" );

				local.urlCFID = createUUID();
				local.result = _internalRequest(
					template: local.uri,
					url: {
						cfid: local.urlCFID,
						cftoken: 0
					}
				); // this returns json

				local.status = deserializeJson( local.result.fileContext );

				assertEquals( local.status.cfidStorage, "cookieOnly" );
				assertNotEquals( local.status.sessionCFID, local.urlCFID );
				assertNotEquals( local.status.sessionCFID, local.status.urlCFID );

			});

			it ( "test cookieIfNotUrl", function(){
				// lucee should use the url.cfid
				changeCfidStorage( "cookieIfNotUrl" );

				local.urlCFID = createUUID();
				local.result = _internalRequest(
					template: local.uri,
					url: {
						cfid: local.urlCFID,
						cftoken: 0
					}
				); // this returns json

				local.status = deserializeJson( local.result.fileContext );

				assertEquals( local.status.cfidStorage, "cookieIfNotUrl" );
				assertEquals( local.status.sessionCFID, local.urlCFID );
				assertEquals( local.status.sessionCFID, local.status.urlCFID );

				// now test with both a url cfid and a cookie cfid, cookie takes precendence 
				local.cookies = {
					cfid: createUUID(),
					cftoken: 0
				};

				local.urlCFID = createUUID();
				local.result = _internalRequest(
					template: local.uri,
					cookies: local.cookies,
					url: {
						cfid: local.urlCFID,
						cftoken: 0
					}
				); // this returns json

				local.status = deserializeJson( local.result.fileContext );

				assertEquals( local.status.cfidStorage, "cookieIfNotUrl" );
				assertEquals( local.status.sessionCFID, local.cookies.cfid ); // i.e. not the url cfid
				assertNotEquals( local.status.sessionCFID, local.urlCFID ); 
				
			});

			it("test urlIfNotCookie", function(){
				// lucee should use the url.cfid
				changeCfidStorage( "urlIfNotCookie" );

				local.urlCFID = createUUID();
				local.result = _internalRequest(
					template: local.uri,
					url: {
						cfid: local.urlCFID,
						cftoken: 0
				}); // this returns json

				local.status = deserializeJson( local.result.fileContext );

				assertEquals( local.status.cfidStorage, "urlIfNotCookie" );
				assertEquals( local.status.sessionCFID, local.urlCFID );
				assertEquals( local.status.sessionCFID, local.status.urlCFID );

				// now test with both a url cfid and a cookie cfid, url takes precendence 
				local.cookies = {
					cfid: createUUID(),
					cftoken: 0
				};

				local.urlCFID = createUUID();
				local.result = _internalRequest(
					template: local.uri,
					cookies: local.cookies,
					url: {
						cfid: local.urlCFID,
						cftoken: 0
					}
				); // this returns json

				local.status = deserializeJson( local.result.fileContext );

				assertEquals( local.status.cfidStorage, "urlIfNotCookie" );
				assertEquals( local.status.sessionCFID, local.urlCFID ); // i.e. not the cookie cfid
				assertNotEquals( local.status.sessionCFID, local.cookies.cfid ); 
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

	private function changeCfidStorage( opt ){
		action="getScope"
			type="server"
			password="#server.SERVERADMINPASSWORD#"
			returnVariable="local.scope";

		scope.cfidStorage = arguments.opt;

		admin
			action="updateScope"
			type="server"
			password="#server.SERVERADMINPASSWORD#"
			attributeCollection=#scope#;
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}


}