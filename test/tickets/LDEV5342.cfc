component extends="org.lucee.cfml.test.LuceeTestCase" skip="true" {

    function run( testResults , testBox ) {
        describe( title="Test case for LDEV-5342", body=function() {
            it( title = "should execute hash() successfully with the 'outputEncoding' attribute", body = function( currentSpec ) {
                var passValue ="password"
                var hashPass = "";
                var success = true;
                try{
                   hashPass = evaluate('hash(input = passValue, algorithm = "QUICK", encoding = "UTF-8", outputEncoding = "hex")');
                }
                catch( any e ) {
                    success = false;
                } 
                expect( success ).toBe( true );
                expect( len(hashPass) ).toBeGT( 0 );
            });
        });
    }

}