component extends="org.lucee.cfml.test.LuceeTestCase" labels="jfr" {

	function run( testResults, testBox ) {
		describe( title="Test suite for jfrStopRecording()", body=function() {
			it( title="checking jfrStopRecording() stops and dumps recording", skip=!jfrAvailable(), body=function( currentSpec ) {

				var tempFile = getTempFile( getTempDirectory(), "test", ".jfr" );
				var recordingId = "";

				try {
					recordingId = jfrStartRecording( {
						name: "Test Recording",
						maxAge: 60,
						dumpOnExit: false
					} );

					jfrEmit( "test", "Test Event 1" );
					jfrEmit( "test", "Test Event 2", { foo: "bar" } );
					sleep( 100 );

					var result = jfrStopRecording( recordingId, tempFile );
					expect( result ).toInclude( tempFile );
					expect( fileExists( tempFile ) ).toBeTrue();
				}
				finally {
					if( fileExists( tempFile ) ) {
						fileDelete( tempFile );
					}
				}
			} );
		} );

		describe( title="Test suite for jfrStopRecording() with named arguments", body=function() {
			it( title="checking jfrStopRecording() with named recordingId and filePath", skip=!jfrAvailable(), body=function( currentSpec ) {

				var tempFile = getTempFile( getTempDirectory(), "test-named", ".jfr" );
				var recordingId = "";

				try {
					recordingId = jfrStartRecording( {
						name: "Named Test Recording",
						maxAge: 60,
						dumpOnExit: false
					} );

					jfrEmit( "test", "Test Event 1" );
					jfrEmit( "test", "Test Event 2", { foo: "bar" } );
					sleep( 100 );

					var result = jfrStopRecording( recordingId=recordingId, destination=tempFile );
					expect( result ).toInclude( tempFile );
					expect( fileExists( tempFile ) ).toBeTrue();
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
