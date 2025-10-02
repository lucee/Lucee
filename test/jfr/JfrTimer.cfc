component extends="org.lucee.cfml.test.LuceeTestCase" labels="jfr" {

	function testCftimerWithJfr() labels="jfr" {
		expect( function() {
			cftimer( label="JFR Timer Test", jfr=true ) {
				sleep( 10 );
			}
		} ).notToThrow();
	}

	function testCftimerWithJfrAndException() labels="jfr" {
		expect( function() {
			try {
				cftimer( label="Failing Timer", jfr=true ) {
					throw( message="Test error in timer", type="TestException" );
				}
			}
			catch( any e ) {
				// Exception should be caught, but JFR event should still be committed
				expect( e.message ).toBe( "Test error in timer" );
			}
		} ).notToThrow();
	}

	function testCftimerWithJfrVariable() labels="jfr" {
		var duration = 0;
		expect( function() {
			cftimer( label="Timed Operation", variable="duration", jfr=true ) {
				sleep( 15 );
			}
			expect( duration ).toBeGTE( 15 );
		} ).notToThrow();
	}

	function testCftimerWithoutJfr() labels="jfr" {
		// Should work normally without jfr attribute
		expect( function() {
			cftimer( label="Normal Timer" ) {
				sleep( 5 );
			}
		} ).notToThrow();
	}
}
