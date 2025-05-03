component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( 'LDEV-28' , function() {
			beforeEach( function(){
				variables.startingTZ=getTimeZone();
				setTimeZone("UTC");
			});
			afterEach( function(){
				setTimeZone(variables.startingTZ?:"UTC");
			});
			it( 'Create date object from member function.' , function() {
				var dateAsString = "2011-03-24";
				var actual = dateAsString.parseDateTime();
				expect( actual ).toBe( '{ts ''2011-03-24 00:00:00''}' );
			});
		});
	}
}