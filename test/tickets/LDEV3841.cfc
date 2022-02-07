component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {


    function beforeAll() {
        systemOutput("CGIReadOnly:" & getApplicationSettings().CGIReadOnly, true);
    }

    function afterAll() {
        systemOutput("CGIReadOnly:" & getApplicationSettings().CGIReadOnly, true);
    }

    public function run( testResults, testBox ) {
        describe("Testcase for LDEV-3841", function() {
            it( title="cfapplication cgiReadOnly=false", body=function( currentSpec ) {
                expect( cgiReadOnlyTest( 1 ) ).toBe( "writable:1" );
                systemOutput("CGIReadOnly:" & getApplicationSettings().CGIReadOnly, true);
            });
            it( title="cfapplication without setting cgiReadOnly", body=function( currentSpec ) {
                expect( cgiReadOnlyTest( 2 ) ).toBe( "writable:2" );
                systemOutput("CGIReadOnly:" & getApplicationSettings().CGIReadOnly, true);
            });
            it( title="cfapplication cgiReadOnly=true", body=function( currentSpec ) {
                expect( cgiReadOnlyTest( 3 ) ).toBe( "cgiReadOnly:3" );
                systemOutput("CGIReadOnly:" & getApplicationSettings().CGIReadOnly, true);
            });
        });
    }

    private string function cgiReadOnlyTest( required numeric scene ) {
        try {
            if (scene == 1) cfapplication(name="LDEV-3841", action='update', cgiReadOnly="false");
            if (scene == 2) cfapplication(action='update');
            if (scene == 3) cfapplication(action='update', cgiReadOnly="true");
            CGI.foo = "writable:#scene#";
            return CGI.foo;
        }
        catch(any e) {
            systemOutput(e.message, true);
            return "cgiReadOnly:#scene#";
        }
    }
}
