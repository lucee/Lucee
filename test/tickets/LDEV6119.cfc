component extends="org.lucee.cfml.test.LuceeTestCase" labels="log" {

	function run( testResults, testBox ) {
		describe( "LDEV-6119 / LDEV-6214 classic log layout: message and stack trace", function() {

			it( title="the message is separated from the stack trace", body=function( currentSpec ) {
				var msg = "An error occurred processing the request";
				try {
					throw( message=msg, type="LDEV6119" );
				}
				catch ( any e ) {
					var content = logAndRead( msg, e );
				}
				expect( content ).toInclude( msg );
				expect( content ).toInclude( "lucee.runtime.exp.CustomTypeException: " & msg );
				// before the fix the stack trace was glued to the message: "...the requestlucee.runtime.exp.CustomTypeException"
				expect( content ).notToInclude( msg & "lucee.runtime.exp.CustomTypeException" );
			});

			it( title="an exception without a message does not repeat its class name (LDEV-6214)", body=function( currentSpec ) {
				try {
					throw( message="", type="LDEV6214" );
				}
				catch ( any e ) {
					var content = logAndRead( "", e );
				}
				expect( content ).toInclude( "lucee.runtime.exp.CustomTypeException" );
				// before the fix: "lucee.runtime.exp.CustomTypeExceptionlucee.runtime.exp.CustomTypeException: "
				expect( content ).notToInclude( "lucee.runtime.exp.CustomTypeExceptionlucee.runtime.exp.CustomTypeException" );
			});

			it( title="a Java exception without a message is separated from the stack trace", body=function( currentSpec ) {
				var content = logAndRead( "", createObject( "java", "java.lang.IllegalStateException" ).init() );
				expect( content ).toInclude( "Caused by: java.lang.IllegalStateException" );
				expect( content ).notToInclude( "java.lang.IllegalStateExceptionlucee.runtime.exp" );
			});

		});
	}

	private string function logAndRead( required string text, required any exception ) {
		var name = "ldev6119-" & lCase( createUUID() );
		cflog( text=arguments.text, file=name, exception=arguments.exception );
		var paths = [ expandPath( "{lucee-web}/logs/#name#.log" ), expandPath( "{lucee-server}/logs/#name#.log" ) ];
		for ( var path in paths ) {
			if ( fileExists( path ) ) {
				var content = fileRead( path );
				return content;
			}
		}
		throw( message="log file [#name#.log] not found in " & serializeJSON( paths ) );
	}
}
