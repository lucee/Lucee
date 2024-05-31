component extends = "org.lucee.cfml.test.LuceeTestCase" labels="struct" {

    function run( testResults, testBox ) {
        describe( "Testcase for LDEV-4725", function() {
            it( title="Checking structKeyList() function, unordered struct", skip="true", body=function( currentSpec ) {
                var numbers = { D:1, B:1, C:1 }; // ACF sorts alphabetically
                expect( structKeyList(numbers) ).toBe("D,B,C");
                expect( numbers.KeyList() ).toBe("D,B,C");
            });

            it( title="Checking structKeyList() function, ordered struct", body=function( currentSpec ) {
                var numbers = [ D=1, B=1, C=1 ];
                expect( structKeyList(numbers) ).toBe("D,B,C");
                expect( numbers.KeyList() ).toBe("D,B,C");
            });

            it( title="Checking structKeyArray() function, unordered struct", skip="true", body=function( currentSpec ) {
                var numbers = { D:1, B:1, C:1 }; // ACF sorts alphabetically
                expect( structKeyArray(numbers) ).toBe(["D","B","C"]);
                expect( numbers.KeyArray() ).toBe(["D","B","C"]);
            });

            it( title="Checking structKeyArray() function, ordered struct", body=function( currentSpec ) {
                var numbers = [ D=1, B=1, C=1 ];
                expect( structKeyArray(numbers) ).toBe( [ "D","B","C" ] );
                expect( numbers.KeyArray() ).toBe( [ "D","B","C" ]);
            });
        });
    }
}
