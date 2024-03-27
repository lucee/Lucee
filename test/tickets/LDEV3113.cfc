component extends="org.lucee.cfml.test.LuceeTestCase" {

    function beforeAll() {
        variables.uri=createURI("LDEV3113");
    }

    function run( testResults , testBox ) {
        describe( "test case for LDEV-3113", function() {
            it( title="checking with form scope", body=function( currentSpec ) {
                var result=_InternalRequest(
                    template : "#uri#\LDEV3113.cfm",
                    forms : {
                        Scene=1,
                        formstruct="true",
                        formstruct.name="test"
                    }
                );
                expect( listLen(result.filecontent) ).toBe(2);
            });
            it( title="checking with url scope", skip=true, body=function( currentSpec ) {
                http url="http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#LDEV3113/LDEV3113.cfm?urlstruct=true&urlstruct.name=test" result="result";
                expect( result.filecontent ).toBeTrue();
            });
        });
    }

    private string function createURI(string calledName) {
        var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }

}