component extends="org.lucee.cfml.test.LuceeTestCase" {
    function run( testResults , testBox ) {
        describe( 'LDEV-27' , function() {
            it( 'Parse date to datetime object' , function() {
                setLocale("english (uk)");
                actual = lsParseDateTime("2011-03-24");
                expect( actual ).toBe( '{ts ''2011-03-24 00:00:00''}' );
            });
        });
    }
}