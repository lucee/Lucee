component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {
	function run( testResults, testBox ){
		
        // TODO this does not work locally, when running the test cases via JSR 223 (via build process), becazse there is not web server!
        
        describe( "Test case for LDEV-3349", function() {
            it( title="cfhttp with queryString %20", body=function( currentSpec ){
                cfhttp( method="get", url="#cgi.SERVER_NAME & getDirectoryFromPath(cgi.SCRIPT_NAME)#LDEV3349/test.cfm?%20");
                expect(cfhttp.filecontent).toBe("%20");
            });
            it( title="cfhttp with queryString space", body=function( currentSpec ){
                cfhttp( method="get", url="#cgi.SERVER_NAME & getDirectoryFromPath(cgi.SCRIPT_NAME)#LDEV3349/test.cfm? ");
                expect(cfhttp.filecontent).toBe("%20");
            });
            it( title="cfhttp with queryString +", body=function( currentSpec ){
                cfhttp( method="get", url="#cgi.SERVER_NAME & getDirectoryFromPath(cgi.SCRIPT_NAME)#LDEV3349/test.cfm?+");
                expect(cfhttp.filecontent).toBe("%20");
            });
            it( title="cfhttp with queryString +%20", body=function( currentSpec ){
                cfhttp( method="get", url="#cgi.SERVER_NAME & getDirectoryFromPath(cgi.SCRIPT_NAME)#LDEV3349/test.cfm?+%20");
                expect(cfhttp.filecontent).toBe("%20%20");
            });
            it( title="cfhttp with queryString %2B+%2B", body=function( currentSpec ){
                cfhttp( method="get", url="#cgi.SERVER_NAME & getDirectoryFromPath(cgi.SCRIPT_NAME)#LDEV3349/test.cfm?%2B+%2B");
                expect(cfhttp.filecontent).toBe("%2B%20%2B");
            });
            it( title="cfhttp with queryString space1space", body=function( currentSpec ){
                cfhttp( method="get", url="#cgi.SERVER_NAME & getDirectoryFromPath(cgi.SCRIPT_NAME)#LDEV3349/test.cfm? 1 ");
                expect(cfhttp.filecontent).toBe("%201%20");
            });
        });
    }
}