component extends = "org.lucee.cfml.test.LuceeTestCase" labels="array" {
    function run( testResults, textbox ) {
        describe("Testcase for LDEV-4371", function() {
            it(title="checking arrayNew() with type argument", body=function( currentSpec ) {
                var myArray = arrayNew( dimension=1, type="String" );
                expect(function() {
                    myArray.append( javaCast( "int", 1 ) );
                }).toThrow();
                expect(function() {
                    myArray.append( javaCast( "string", "1" ) );
                }).toThrow();
            });
        });
    }
}