component extends="org.lucee.cfml.test.LuceeTestCase" {
    function run( testResults, testBox ) {
        describe("Testcase for LDEV-3841", function() {
            it( title="cfapplication cgiReadOnly=false", body=function( currentSpec ) {    
                expect(cgiReadOnlyTest(1)).toBe("writable");
            });
            it( title="cfapplication wihtout setting cgiReadOnly", body=function( currentSpec ) {     
                expect(cgiReadOnlyTest(2)).toBe("writable");
            });
            it( title="cfapplication cgiReadOnly=true", body=function( currentSpec ) {      
                expect(cgiReadOnlyTest(3)).toBe("cgiReadOnly");
            });
        });
    }

    private string function cgiReadOnlyTest(required numeric scene) {
        try {
            if (scene == 1) cfapplication(name="LDEV-3841",action='update', cgiReadOnly="false");
            if (scene == 2) cfapplication(action='update');
            if (scene == 3) cfapplication(action='update', cgiReadOnly="true");
            CGI.foo = "writable";
            return CGI.foo;
        }
        catch(any e) {
            return "cgiReadOnly";
        }
    }
}