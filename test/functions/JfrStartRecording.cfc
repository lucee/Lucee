component extends="org.lucee.cfml.test.LuceeTestCase" labels="jfr" {

	function run( testResults, testBox ) {
		describe( title="Test suite for jfrStartRecording()", body=function() {
			it( title="checking jfrStartRecording() returns recording ID", skip=!jfrAvailable(), body=function( currentSpec ) {

				var recordingId = "";
				try {
					recordingId = jfrStartRecording();
					expect( recordingId ).toBeTypeOf( "string" );
					expect( len( recordingId ) ).toBeGT( 0 );
				}
				finally {
					if( len( recordingId ) > 0 ) {
						jfrStopRecording( recordingId );
					}
				}
			} );

			it( title="checking jfrStartRecording() with options", skip=!jfrAvailable(), body=function( currentSpec ) {

				var recordingId = "";
				try {
					recordingId = jfrStartRecording( {
						name: "Test Recording",
						maxAge: 60,
						dumpOnExit: false
					} );
					expect( recordingId ).toBeTypeOf( "string" );
					expect( len( recordingId ) ).toBeGT( 0 );
				}
				finally {
					if( len( recordingId ) > 0 ) {
						jfrStopRecording( recordingId );
					}
				}
			} );
		} );

		describe( title="Test suite for jfrStartRecording() with named arguments", body=function() {
			it( title="checking jfrStartRecording() with named options struct", skip=!jfrAvailable(), body=function( currentSpec ) {

				var recordingId = "";
				try {
					recordingId = jfrStartRecording( options={
						name: "Named Test Recording",
						maxAge: 60,
						dumpOnExit: false
					} );
					expect( recordingId ).toBeTypeOf( "string" );
					expect( len( recordingId ) ).toBeGT( 0 );
				}
				finally {
					if( len( recordingId ) > 0 ) {
						jfrStopRecording( recordingId );
					}
				}
			} );
		} );
	}
}
