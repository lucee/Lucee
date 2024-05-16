component extends="org.lucee.cfml.test.LuceeTestCase"  {

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV825", function() {
			it( title='test millisecond fraction', body=function( currentSpec ) {
				expect( millisecond(parseDateTime("2024-04-04 00:00:00.6")) ).toBe(600);
				expect( millisecond(parseDateTime("2024-04-04 00:00:00.60")) ).toBe(600);
				expect( millisecond(parseDateTime("2024-04-04 00:00:00.600")) ).toBe(600);
				expect( millisecond(parseDateTime("2024-04-04 00:00:00.6000")) ).toBe(600);
			});
		});
	}
}
