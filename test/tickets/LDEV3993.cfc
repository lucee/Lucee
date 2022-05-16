component extends = "org.lucee.cfml.test.LuceeTestCase"{
    function run( testResults , testBox ) {
        describe( "Test case for LDEV-3993", function() {
            it(title="checking toString( CreateODBCTime() )", body=function( currentSpec ) {
                expect(toString( CreateODBCTime("10:10:10") )).toBe("{t '10:10:10'}");
            });
            it(title="checking CreateODBCTime().toString()", body=function( currentSpec ) {
                expect(CreateODBCTime("10:10:10").toString()).toBe("{t '10:10:10'}");
            });
        });
    }
}