component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true {

    function run( testResults, testBox ) {
        describe( "Testcase for LDEV-4725", function() {
            it( title="Checking strucyKeyList() function", body=function( currentSpec ) {
                var numbers = { A:1, B:1, C:1 };
                expect( structKeyList(numbers) ).toBe("A,B,C");
                expect( numbers.KeyList() ).toBe("A,B,C");
            });
        });
    }
}
