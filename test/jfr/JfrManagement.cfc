component extends="org.lucee.cfml.test.LuceeTestCase" labels="jfr" {

	function testJfrStartStopRecording() labels="jfr" {
		if( !jfrEnabled() ) {
			// Skip if JFR not available
			return;
		}

		var recordingId = "";
		var tempFile = getTempFile( getTempDirectory(), "test", ".jfr" );

		try {
			// Start a recording
			recordingId = jfrStartRecording( {
				name: "Test Recording",
				maxAge: 60,
				dumpOnExit: false
			} );

			expect( recordingId ).toBeTypeOf( "string" );
			expect( len( recordingId ) ).toBeGT( 0 );

			// Generate some events
			jfrEmit( "test", "Test Event 1" );
			jfrEmit( "test", "Test Event 2", { foo: "bar" } );

			sleep( 100 );

			// Stop and dump the recording
			var result = jfrStopRecording( recordingId, tempFile );
			expect( result ).toInclude( tempFile );

			// Verify file was created
			expect( fileExists( tempFile ) ).toBeTrue();
		}
		finally {
			// Cleanup
			if( fileExists( tempFile ) ) {
				fileDelete( tempFile );
			}
		}
	}

	function testJfrAnalyzeArray() labels="jfr" {
		if( !jfrEnabled() ) {
			return;
		}

		var tempFile = getTempFile( getTempDirectory(), "analyze", ".jfr" );

		try {
			// Create a recording with events
			var recordingId = jfrStartRecording();

			jfrEmit( "test", "Event 1", { type: "test", count: 1 } );
			jfrEmit( "test", "Event 2", { type: "test", count: 2 } );

			var eventId = jfrBegin( "test", "Duration Event" );
			sleep( 50 );
			jfrCommit( eventId, { success: true } );

			jfrStopRecording( recordingId, tempFile );

			// Analyze the recording as array
			var events = jfrAnalyze( tempFile );

			expect( events ).toBeTypeOf( "array" );
			expect( arrayLen( events ) ).toBeGT( 0 );

			// Check event structure
			if( arrayLen( events ) > 0 ) {
				var firstEvent = events[1];
				expect( firstEvent ).toHaveKey( "eventType" );
				expect( firstEvent ).toHaveKey( "startTime" );
				expect( firstEvent ).toHaveKey( "duration" );
				expect( firstEvent ).toHaveKey( "fields" );
			}
		}
		finally {
			if( fileExists( tempFile ) ) {
				fileDelete( tempFile );
			}
		}
	}

	function testJfrAnalyzeQuery() labels="jfr" {
		if( !jfrEnabled() ) {
			return;
		}

		var tempFile = getTempFile( getTempDirectory(), "analyze-qry", ".jfr" );

		try {
			var recordingId = jfrStartRecording();

			jfrEmit( "database", "Query Event", { sql: "SELECT 1", rows: 1 } );

			jfrStopRecording( recordingId, tempFile );

			// Analyze as query
			var qry = jfrAnalyze( tempFile, "query" );

			expect( qry ).toBeTypeOf( "query" );
			expect( qry.recordCount ).toBeGT( 0 );
			expect( qry.columnList ).toInclude( "eventType" );
			expect( qry.columnList ).toInclude( "startTime" );
			expect( qry.columnList ).toInclude( "duration" );
		}
		finally {
			if( fileExists( tempFile ) ) {
				fileDelete( tempFile );
			}
		}
	}
}
