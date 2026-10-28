component extends="org.lucee.cfml.test.LuceeTestCase" labels="execute" {

	function isWindows() {
		return server.os.name contains "windows";
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-5525 cfexecute when the request thread gets interrupted (request timeout)", skip=isWindows(), body=function() {

			it( "an interrupt is not swallowed into a generic 'Error invoking external process' and the process gets terminated", function() {
				var duration = "7.25";
				var start = getTickCount();
				var err = "";
				createObject( "java", "java.lang.Thread" ).currentThread().interrupt();
				try {
					cfexecute( name="sleep", arguments=duration, timeout="10", variable="local.out" );
				}
				catch ( any e ) {
					err = e;
				}
				finally {
					// make sure the flag does not leak into the next spec
					createObject( "java", "java.lang.Thread" ).interrupted();
				}
				expect( isSimpleValue( err ) ).toBeFalse( "cfexecute should throw when the thread is interrupted" );
				expect( err.message ).notToInclude( "Error invoking external process" );
				expect( err.type ).toInclude( "InterruptedException" );
				expect( getTickCount() - start ).toBeLT( 5000 );
				sleep( 300 );
				expect( isRunning( duration ) ).toBeFalse( "spawned process [sleep #duration#] is still running" );
			});

			it( "a request timeout interrupt ends the request instead of being caught by try/catch", function() {
				var duration = "7.35";
				var res = _internalRequest(
					template: createURI( "LDEV5525/requestTimeout.cfm" ),
					url: { duration: duration },
					throwonerror: false
				);
				var body = trim( res.filecontent );
				// markers are built with ucase() so they don't match the source excerpt of the error page
				expect( body ).notToInclude( "CAUGHT_MARKER" );
				expect( body ).notToInclude( "AFTER_MARKER" );
				expect( body ).toInclude( "has run into a timeout" );
				sleep( 300 );
				expect( isRunning( duration ) ).toBeFalse( "spawned process [sleep #duration#] is still running" );
			});

		});
	}

	private boolean function isRunning( required string duration ) {
		var out = "";
		cfexecute( name="ps", arguments="-eo args", variable="out", timeout="5" );
		return reFind( "(^|\n)sleep #replace( arguments.duration, ".", "\.", "all" )#(\r?\n|$)", out ) > 0;
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatepath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}
}
