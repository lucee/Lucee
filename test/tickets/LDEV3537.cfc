component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults, testBox ){
		describe( "Test case for LDEV-3537", function() {
            variables.path = "http://"&cgi.SERVER_NAME & getDirectoryFromPath(cgi.SCRIPT_NAME)
            it( title="cfhttp url with single slash", body=function( currentSpec ){
                cfhttp( method="get", url="#path#LDEV3537/test.cfm");
                expect(trim(cfhttp.filecontent)).toBe("#path#LDEV3537/test.cfm");
            });
            it( title="cfhttp url with double slashes", body=function( currentSpec ){
                cfhttp( method="get", url="#path#LDEV3537//test.cfm");
                expect(trim(cfhttp.filecontent)).toBe("#path#LDEV3537//test.cfm");
            });
            it( title="cfhttp url with multiple slashes", body=function( currentSpec ){
                cfhttp( method="get", url="#path#LDEV3537//////test.cfm");
                expect(trim(cfhttp.filecontent)).toBe("#path#LDEV3537//////test.cfm");
            });
        });
    }
}