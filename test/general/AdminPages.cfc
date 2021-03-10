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

    function run( testResults , testBox ) {

        setup();

        function setup(){
            variables.adminRoot = "/lucee/admin/"; // doesn't work, throws .lex error???
            /*
             [/lucee/admin/web.cfm] [zip://C:\work\lucee\temp\archive\base\lucee-server\context\context\lucee-admin.lar!/web.cfm] not found
            [java]    [script]                 /Users/mic/Projects/Lucee5/source/cfml/context/admin/server.cfm:2
            */

            //variables.adminRoot = "/admin/";
            variables.adminPage = "index.cfm"; // server.cfm or web.cfm don't work???
            variables.adminPage = "server.cfm"; // server.cfm or web.cfm don't work???
        }
       
        describe(title="Testing Lucee Admin pages", asyncAll=false, body=function(){
            setup();
            
            it("Login to admin", function(){
                //systemOutput("------------- login to admin", true);
                local.loginResult = _internalRequest(
                    template: adminRoot & adminPage, 
                    forms: {
                        login_passwordserver: request.SERVERADMINPASSWORD,
                        lang: "en",
                        rememberMe: "s",
                        submit: "submit"
                    }
                );
                //systemOutput(loginResult);
                expect( loginResult.status ).toBe( 200, "Status code" );
            });

            it("Fetch and test admin pages", function(){
                //systemOutput("------------- get admin urls", true);
                // adminPage = "server.cfm";
                local._adminUrls = _internalRequest( 
                    template: adminRoot & adminPage, 
                    urls : { testUrls: true }
                );
                
                expect( _adminUrls.status ).toBe( 200, "Status Code" );
                //expect(_adminUrls).toBeJson();
                expect( isJson( _adminUrls.fileContent ) ).toBeTrue();
                local.adminUrls = deserializeJson( _adminUrls.fileContent );
                expect(adminUrls ).toBeArray();
        
                // systemOutput( adminUrls, true );

                loop array="#adminUrls#" item="local.testUrl" {
                    local.page = local.testUrl; // i.e "server.cfm?action=plugin&plugin=PerformanceAnalyzer"
                    //systemOutput( page, true );
                    
                    local.params = listRest( page, "?" );
                    local.result = _internalRequest( 
                        template: adminRoot & listFirst( page, "?" ), 
                        urls : queryStringToStruct( local.params )
                    );  // BUT this always returns the login page with index.cfm???
                    
                    //fileWrite("c:\tmp\#queryStringToStruct( local.params ).action#.html", local.result.fileContent );
                    // systemOutput( page & " " & local.result.status, true );
                    // this expect() maybe isn't even needed as _internalRequest throws the stack trace anyway??
                    expect( local.result.status ).toBeBetween( 200, 399, adminRoot & page & " returned status code: " & local.result.status);
                    
                }
            });
        });
    }

    private function queryStringToStruct( qs ){
        local.tmp = listToArray( arguments.qs, "&" );
        local.st = {};
        loop array="#tmp#" item="local.p"{
            st[ listFirst( p , "=" ) ] = listRest( p, "=" );
        }
        return st;
    }
}