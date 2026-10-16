component extends="org.lucee.cfml.test.LuceeTestCase" labels="log" {

	function run( testResults, testBox ) {
		describe( "LDEV-6530 classic log layout: escaping, date/time format, timezone", function() {

			it( title="double quotes are escaped CSV style", body=function( currentSpec ) {
				var line = logAndReadLine( 'say "hi", then ""bye""' );
				expect( line ).toInclude( '"say ""hi"", then """"bye"""""' );
			});

			it( title="date and time use MM/dd/yyyy and HH:mm:ss", body=function( currentSpec ) {
				var line = logAndReadLine( "format check" );
				// "Severity","ThreadID","Date","Time",...
				expect( reFind( '^"INFO","[^"]*","\d{2}/\d{2}/\d{4}","\d{2}:\d{2}:\d{2}",', line ) ).toBe( 1, line );
			});

			it( title="the log timestamp follows the request/application timezone, not the JVM default", body=function( currentSpec ) {
				var TimeZone = createObject( "java", "java.util.TimeZone" );
				var orig = getTimeZone();
				var name = "ldev6530-" & lCase( createUUID() );
				try {
					// both far apart from each other and from any JVM default, so the date or the hour differs
					for ( var id in [ "Pacific/Kiritimati", "Etc/GMT+12" ] ) {
						setTimeZone( id );
						var tz = TimeZone.getTimeZone( id );
						var before = fmt( tz );
						cflog( text="tz #id#", file=name );
						var after = fmt( tz );
						var line = findLine( readLog( name ), "tz #id#" );
						var dt = reMatch( '"\d{2}/\d{2}/\d{4}","\d{2}:\d{2}', line );
						expect( dt.len() ).toBe( 1, line );
						expect( [ before, after ] ).toInclude( dt[ 1 ], id & ": " & line );
					}
				}
				finally {
					setTimeZone( orig );
				}
			});

		});
	}

	private string function fmt( required tz ) {
		var sdf = createObject( "java", "java.text.SimpleDateFormat" ).init( '"MM/dd/yyyy","HH:mm' );
		sdf.setTimeZone( arguments.tz );
		return sdf.format( createObject( "java", "java.util.Date" ).init() );
	}

	private string function logAndReadLine( required string text ) {
		var name = "ldev6530-" & lCase( createUUID() );
		cflog( text=arguments.text, file=name );
		return findLine( readLog( name ), '"' );
	}

	private string function findLine( required string content, required string needle ) {
		var lines = listToArray( arguments.content, chr( 10 ) );
		// skip the header line
		for ( var i = 2; i <= lines.len(); i++ ) {
			if ( find( arguments.needle, lines[ i ] ) ) return trim( lines[ i ] );
		}
		throw( message="no line with [#arguments.needle#] in: " & arguments.content );
	}

	private string function readLog( required string name ) {
		var paths = [ expandPath( "{lucee-web}/logs/#arguments.name#.log" ), expandPath( "{lucee-server}/logs/#arguments.name#.log" ) ];
		for ( var path in paths ) {
			if ( fileExists( path ) ) return fileRead( path );
		}
		throw( message="log file [#arguments.name#.log] not found in " & serializeJSON( paths ) );
	}
}
