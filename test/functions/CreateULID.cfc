component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( title="Test suite for CreateULID()", body=function() {
			it(title="checking CreateULID() function", body = function( currentSpec ) {
				systemOutput( "", true );
				systemOutput( "sample output", true );
				loop times=10 {
					systemOutput( createULID(), true );
				}
			});

			it(title="checking CreateULID('Monotonic') function", body = function( currentSpec ) {
				systemOutput( "", true );
				systemOutput( "sample Monotonic output", true );
				loop times=10 {
					systemOutput( createULID("Monotonic"), true );
				}
			});

			it(title="checking CreateULID('hash', number, string ) function", body = function( currentSpec ) {
				var once = createULID( "hash", 1, "b" );
				var again = createULID( "hash", 1, "b" );

				expect( once ).toBe( again );
			});
		});
	}
}
