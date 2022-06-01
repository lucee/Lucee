component extends="org.lucee.cfml.test.LuceeTestCase"{
    function run( testResults, testBox ) {
        describe("Testcase for FormatBaseN()", function() {
            it( title="Format to base binary(2)", body=function( currentSpec ){
                assertEquals('1010',formatBaseN(10,2));
                assertEquals('1010',formatBaseN(10.55,2));
                assertEquals('10011010010',formatBaseN(1234,2));
                assertEquals('11111111111111111111101100101110',formatBaseN(-1234,2));
            });
            it( title="Format to base octal(8)", body=function( currentSpec ){
                assertEquals('12',formatBaseN(10,8));
                assertEquals('12',formatBaseN(10.55,8));
                assertEquals('2322',formatBaseN(1234,8));
                assertEquals('-2322',formatBaseN(-1234,8));
            });
            it( title="Format to base decimal(10)", body=function( currentSpec ){
                assertEquals('10',formatBaseN(10,10));
                assertEquals('10',formatBaseN(10.55,10));
                assertEquals('1234',formatBaseN(1234,10));
                assertEquals('-1234',formatBaseN(-1234,10));
            });
            it( title="Format to base hexadecimal(16)", body=function( currentSpec ){
                assertEquals('a',formatBaseN(10,16));
                assertEquals('a',formatBaseN(10.55,16));
                assertEquals('4d2',formatBaseN(1234,16));
                assertEquals('fffffb2e',formatBaseN(-1234,16));
            });
        });
    }
}