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

            variables.adminRoot = "/admin/";
            //variables.adminPage = "server.cfm"; 
            variables.adminPage = "index.cfm"; // server.cfm or web.cfm don't work???
            
            variables.cookies = {}; // need to be authenticated to the admin, for subsequent requests after login
        }
       
        describe(title="Testing Lucee Admin pages", asyncAll=false, body=function(){
            setup();
            
            it( title="Login to admin", body=function(){
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
                variables.cookies = {
                     cfid: loginResult.session.cfid,
                     cftoken: loginResult.session.cftoken
                };
            });

            it( title="Fetch and test admin pages", body=function(){
                //systemOutput("------------- get admin urls", true);
                // adminPage = "server.cfm";
                local._adminUrls = _internalRequest(
                    template: adminRoot & adminPage,
                    urls : { testUrls: true },
                    cookies: variables.cookies
                );
                
                expect( _adminUrls.status ).toBe( 200, "Status Code" );
                //expect(_adminUrls.fileContent).toBeJson();
                expect( isJson( _adminUrls.fileContent ) ).toBeTrue();
                local.adminUrls = deserializeJson( _adminUrls.fileContent );
                expect( adminUrls ).toBeArray();
                // systemOutput( adminUrls, true );
                systemOutput( "", true );
                loop array="#adminUrls#" item="local.testUrl" {
                    checkUrl( adminRoot, local.testUrl, 200 );
                }
            });

            it( title="check admin extension pages", body=function(){
                local.extUrls = [];
                local.exts = ExtensionList();
                loop query=exts {
                    arrayAppend( extUrls, 
                        "index.cfm?action=ext.applications&action2=detail&id=#exts.id#&name=#URLEncodedFormat(exts.name)#"
                    );
                }
                loop array="#extUrls#" item="local.testUrl" {
                    checkUrl( adminRoot, local.testUrl, 200 );
                }
            });

            it( title="check missing admin extension page", skip=true, body=function() {
                local.missingExtUrl = "index.cfm?action=ext.applications&action2=detail&id=missing&name=missing";
                checkUrl( adminRoot, missingExtUrl, 404 );
            });

            it( title="check admin 302", body=function(){
                // redirect (not logged in)
                checkUrl( adminRoot, "server.cfm?action=ext.applications", 302 );
            });

            it( title="check admin 404", body=function(){
                // not found (page doens't exist )
                checkUrl( adminRoot, "index.cfm?action=i.dont.exist", 404 );
            });

            it( title="check admin 500", body=function(){
                // 500 (mappng doesn't exist)
                checkUrl( adminRoot, "index.cfm?action=resources.mappings&action2=create&virtual=/lucee/adminMissing", 500 );
            });
        });
    }

    private function checkUrl( required string adminRoot, required string testUrl, required numeric statusCode ){
        local.page =arguments.testUrl; // i.e "server.cfm?action=plugin&plugin=PerformanceAnalyzer"
        // systemOutput("", true );
        
        local.start = getTickCount();
        try {
            local.result = _internalRequest(
                template: arguments.adminRoot & listFirst( page, "?" ),
                urls : listRest( page, "?" ) & "&rawError=true",
                cookies: variables.cookies
            );
        } catch(e) {
            if ( arguments.statusCode neq 500 ){
                rethrow;
            } else {
                local.result = {
                    status: 500,
                    fileContent: e.message & " " & e.detail & e.stacktrace
                };
            }
        }
        local.TAB = chr(9);
        if (structCount(local.result)){
            systemOutput( TAB & TAB & adminRoot & page & " " & TAB & NumberFormat( getTickCount()-local.start ) & " ms", true );
        }
        // this expect() maybe isn't even needed as _internalRequest throws the stack trace anyway??
        // systemOutput( local.result.headers, true );
        //expect( local.result.status ).toBeBetween( 200, 399, adminRoot & page & " returned status code: " & local.result.status);
        if ( local.result.status neq arguments.statusCode )
            systemOutput( trim(local.result.filecontent), true );
        expect( local.result.status ).toBe( arguments.statusCode, 
            arguments.adminRoot & page & " returned status code: " & local.result.status );
    }
}