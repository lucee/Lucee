component extends="org.lucee.cfml.test.LuceeTestCase" {
    function run( testResults , testBox ) {
        describe( "test case for LDEV-2969", function() {
            it( title = "deserializeJSON with large precision", body=function( currentSpec ) {
                var myJSON = '{"lat":20.12283319000001}';
                var decoded = deserializeJSON( myJSON );
                var res = '{"a":1.000000001,"b":1.0000000001,"c":1.00000000001,"d":1.000000000001,"e":1.0000000000001,"f":1.00000000000001,"g":1.000000000000001}';
                var des = deserializeJSON(res);
                expect( toString( decoded.lat ) ).toBe( numberFormat( 20.12283319000001, "99.99999999999999" ) );
                expect( serializeJSON( decoded ) ).toBe( myJSON );
                expect( serializeJSON(des) ).toBe( res );
            });
        });
    }
}