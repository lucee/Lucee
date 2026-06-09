component extends="org.lucee.cfml.test.LuceeTestCase" {

	function skipAimock() {
		return structCount( server.getTestService( "aimock" ) ) == 0;
	}

	function run( testResults, testBox ) {
		describe( title="Test suite for AIHas()", skip=skipAimock, body=function() {

			it( title="returns true for the registered mock connection", body=function() {
				expect( AIHas( "aimock" ) ).toBeTrue();
			});

			it( title="returns false for an unknown connection", body=function() {
				expect( AIHas( "missing-connection" ) ).toBeFalse();
			});

		});
	}

}
