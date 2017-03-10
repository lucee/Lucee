component extends="org.lucee.cfml.test.LuceeTestCase" {
    function run( testResults , testBox ) {
        describe( 'LDEV-28' , function() {
            it( 'Create date object from member function.' , function() {
                dateAsString = "2011-03-24";
  				actual = dateAsString.parseDateTime();
                expect( actual ).toBe( '{ts ''2011-03-24 00:00:00''}' );
            });
        });
    }
}