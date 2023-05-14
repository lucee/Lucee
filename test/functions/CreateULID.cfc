component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( title="Test suite for CreateULID()", body=function() {
			it(title="checking CreateULID() function", body = function( currentSpec ) {
				systemOutput( "", true );
				systemOutput( "sample output", true );
				loop times=100 {
					systemOutput( createULID(), true );
				}
			});

			it(title="checking CreateULID('Monotonic') function", body = function( currentSpec ) {
				// TBD
			});

			it(title="checking CreateULID('hash', input ) function", body = function( currentSpec ) {
				// TBD
			});
		});
	}
}
