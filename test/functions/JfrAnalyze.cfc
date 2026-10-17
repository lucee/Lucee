component extends="org.lucee.cfml.test.LuceeTestCase" labels="jfr" {

	function run( testResults, testBox ) {
		describe( title="Test suite for jfrAnalyze()", body=function() {
			it( title="checking jfrAnalyze() returns array by default", skip=!jfrAvailable(), body=function( currentSpec ) {
				var tempFile = getTempFile( getTempDirectory(), "analyze", ".jfr" );
				try {
					var recordingId = jfrStartRecording();
					jfrEmit( "test", "Event 1", { type: "test", count: 1 } );
					jfrEmit( "test", "Event 2", { type: "test", count: 2 } );
					jfrStopRecording( recordingId, tempFile );

					var events = jfrAnalyze( tempFile );
					expect( events ).toBeTypeOf( "array" );
					expect( arrayLen( events ) ).toBeGT( 0 );

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
			} );

			it( title="checking jfrAnalyze() returns query when specified", skip=!jfrAvailable(), body=function( currentSpec ) {

				var tempFile = getTempFile( getTempDirectory(), "analyze-qry", ".jfr" );
				try {
					var recordingId = jfrStartRecording();
					jfrEmit( "database", "Query Event", { sql: "SELECT 1", rows: 1 } );
					jfrStopRecording( recordingId, tempFile );

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
			} );
		} );

		describe( title="Test suite for jfrAnalyze() with named arguments", body=function() {
			it( title="checking jfrAnalyze() with named filePath returns array", skip=!jfrAvailable(), body=function( currentSpec ) {

				var tempFile = getTempFile( getTempDirectory(), "analyze-named", ".jfr" );
				try {
					var recordingId = jfrStartRecording();
					jfrEmit( "test", "Event 1", { type: "test", count: 1 } );
					jfrEmit( "test", "Event 2", { type: "test", count: 2 } );
					jfrStopRecording( recordingId, tempFile );

					var events = jfrAnalyze( jfrFile=tempFile );
					expect( events ).toBeTypeOf( "array" );
					expect( arrayLen( events ) ).toBeGT( 0 );
				}
				finally {
					if( fileExists( tempFile ) ) {
						fileDelete( tempFile );
					}
				}
			} );

			it( title="checking jfrAnalyze() with named filePath and returnType", skip=!jfrAvailable(), body=function( currentSpec ) {

				var tempFile = getTempFile( getTempDirectory(), "analyze-named-qry", ".jfr" );
				try {
					var recordingId = jfrStartRecording();
					jfrEmit( "database", "Query Event", { sql: "SELECT 1", rows: 1 } );
					jfrStopRecording( recordingId, tempFile );

					var qry = jfrAnalyze( jfrFile=tempFile, returnType="query" );
					expect( qry ).toBeTypeOf( "query" );
					expect( qry.recordCount ).toBeGT( 0 );
				}
				finally {
					if( fileExists( tempFile ) ) {
						fileDelete( tempFile );
					}
				}
			} );
		} );

		describe( title="Test suite for jfrAnalyze() with options", body=function() {
			it( title="checking jfrAnalyze() with eventTypes filter", skip=!jfrAvailable(), body=function( currentSpec ) {

				var tempFile = getTempFile( getTempDirectory(), "analyze-eventTypes", ".jfr" );
				try {
					var recordingId = jfrStartRecording();
					jfrEmit( "test", "Test Event 1" );
					jfrEmit( "database", "DB Event" );
					jfrEmit( "test", "Test Event 2" );
					jfrStopRecording( recordingId, tempFile );

					var events = jfrAnalyze( tempFile, "array", { eventTypes: "lucee.runtime.jfr.CustomEvent" } );
					expect( events ).toBeTypeOf( "array" );
					expect( arrayLen( events ) ).toBeGT( 0 );
				}
				finally {
					if( fileExists( tempFile ) ) {
						fileDelete( tempFile );
					}
				}
			} );

			it( title="checking jfrAnalyze() with minDuration filter", skip=!jfrAvailable(), body=function( currentSpec ) {

				var tempFile = getTempFile( getTempDirectory(), "analyze-duration", ".jfr" );
				try {
					var recordingId = jfrStartRecording();
					var eventId1 = jfrBegin( "test", "Quick Event" );
					sleep( 5 );
					jfrCommit( eventId1 );

					var eventId2 = jfrBegin( "test", "Slow Event" );
					sleep( 50 );
					jfrCommit( eventId2 );

					jfrStopRecording( recordingId, tempFile );

					var events = jfrAnalyze( tempFile, "array", { minDuration: 30 } );
					expect( events ).toBeTypeOf( "array" );
				}
				finally {
					if( fileExists( tempFile ) ) {
						fileDelete( tempFile );
					}
				}
			} );

			it( title="checking jfrAnalyze() with sortBy and sortOrder", skip=!jfrAvailable(), body=function( currentSpec ) {

				var tempFile = getTempFile( getTempDirectory(), "analyze-sort", ".jfr" );
				try {
					var recordingId = jfrStartRecording();
					var eventId1 = jfrBegin( "test", "Event 1" );
					sleep( 10 );
					jfrCommit( eventId1 );

					var eventId2 = jfrBegin( "test", "Event 2" );
					sleep( 50 );
					jfrCommit( eventId2 );

					var eventId3 = jfrBegin( "test", "Event 3" );
					sleep( 30 );
					jfrCommit( eventId3 );

					jfrStopRecording( recordingId, tempFile );

					var events = jfrAnalyze( tempFile, "array", {
						eventTypes: "lucee.runtime.jfr.CustomEvent",
						sortBy: "duration",
						sortOrder: "desc"
					} );
					expect( events ).toBeTypeOf( "array" );
					if( arrayLen( events ) >= 2 ) {
						expect( events[1].duration ).toBeGTE( events[2].duration );
					}
				}
				finally {
					if( fileExists( tempFile ) ) {
						fileDelete( tempFile );
					}
				}
			} );

			it( title="checking jfrAnalyze() with maxEvents limit", skip=!jfrAvailable(), body=function( currentSpec ) {

				var tempFile = getTempFile( getTempDirectory(), "analyze-maxEvents", ".jfr" );
				try {
					var recordingId = jfrStartRecording();
					for( var i = 1; i <= 10; i++ ) {
						jfrEmit( "test", "Event #i#" );
					}
					jfrStopRecording( recordingId, tempFile );

					var events = jfrAnalyze( tempFile, "array", {
						eventTypes: "lucee.runtime.jfr.CustomEvent",
						maxEvents: 5
					} );
					expect( events ).toBeTypeOf( "array" );
					expect( arrayLen( events ) ).toBeLTE( 5 );
				}
				finally {
					if( fileExists( tempFile ) ) {
						fileDelete( tempFile );
					}
				}
			} );

			it( title="checking jfrAnalyze() with includeStackTraces option", skip=!jfrAvailable(), body=function( currentSpec ) {

				var tempFile = getTempFile( getTempDirectory(), "analyze-stack", ".jfr" );
				try {
					var recordingId = jfrStartRecording();
					jfrEmit( "test", "Test Event" );
					jfrStopRecording( recordingId, tempFile );

					var events = jfrAnalyze( tempFile, "array", { includeStackTraces: true } );
					expect( events ).toBeTypeOf( "array" );
				}
				finally {
					if( fileExists( tempFile ) ) {
						fileDelete( tempFile );
					}
				}
			} );
		} );
	}
}
