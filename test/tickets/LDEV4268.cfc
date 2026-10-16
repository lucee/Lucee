component extends="org.lucee.cfml.test.LuceeTestCase" labels="syntax" {

    function beforeAll() {
        variables.uri = createURI("LDEV4268");
    }

    function run( testResults , testBox ) {
        describe( "Testcase for LDEV-4268", function() {
            it( title="continue without a semicolon inside the script", body=function() {
                var res = _internalRequest(
                        template: "#variables.uri#/LDEV4268.cfm"
                    ).filecontent.trim();
                

                expect(res).toBe(9);
            });

            it( title="break without a semicolon inside the script", body=function() {
                expect( _internalRequest( template: "#variables.uri#/break.cfm" ).filecontent.trim() ).toBe( 4 );
            });

            it( title="continue and break without a semicolon before the closing curly bracket on the same line", body=function() {
                expect( _internalRequest( template: "#variables.uri#/sameLine.cfm" ).filecontent.trim() ).toBe( 6 );
            });

            it( title="continue without a semicolon followed by a statement on the next line", body=function() {
                expect( _internalRequest( template: "#variables.uri#/nextLine.cfm" ).filecontent.trim() ).toBe( 9 );
            });

            it( title="continue and break with a label without a semicolon", body=function() {
                expect( _internalRequest( template: "#variables.uri#/label.cfm" ).filecontent.trim() ).toBe( 12 );
            });
        });
    }

    private string function createURI(string calledName) {
        var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }

}
