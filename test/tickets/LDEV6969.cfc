component extends="org.lucee.cfml.test.LuceeTestCase" labels="jfr" {

	// The gate must not latch a false before a recording is armed, and must not
	// latch a true after one ends. Deliberately not gated on jfrEnabled(), since
	// that is the value under test.
	function testArmingAfterFirstIsEnabledCall() labels="jfr" {
		if( !jfrAvailable() ) {
			return;
		}

		var recordingId = "";
		var tempFile = getTempFile( getTempDirectory(), "ldev6969", ".jfr" );

		try {
			expect( jfrEnabled() ).toBeFalse();

			recordingId = jfrStartRecording( {
				name: "LDEV-6969 arming",
				maxAge: 60,
				dumpOnExit: false
			} );

			expect( jfrEnabled() ).toBeTrue();
		}
		finally {
			if( recordingId != "" ) {
				jfrStopRecording( recordingId, tempFile );
			}

			if( fileExists( tempFile ) ) {
				fileDelete( tempFile );
			}
		}

		expect( jfrEnabled() ).toBeFalse();
	}

	function testEnabledIsStableWithoutRecording() labels="jfr" {
		if( !jfrAvailable() ) {
			return;
		}

		var first = jfrEnabled();

		expect( jfrEnabled() ).toBe( first );
		expect( jfrEnabled() ).toBe( first );
	}

}
