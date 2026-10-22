component extends="org.lucee.cfml.test.LuceeTestCase" labels="log" {

	// far apart from each other (26 hours), so the date and the hour differ between them
	variables.configTZ = "Etc/GMT+12";
	variables.requestTZ = "Pacific/Kiritimati";

	function beforeAll() {
		variables.uri = createURI( "LDEV6598" );
		variables.adminCfc = new org.lucee.cfml.Administrator( "server", request.ServerAdminPassword );
		variables.regional = adminCfc.getRegional();
		adminCfc.updateRegional( variables.configTZ, variables.regional.locale );
	}

	function afterAll() {
		adminCfc.updateRegional( variables.regional.timezone, variables.regional.locale );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6598 log timestamps use the server config timezone, not the request timezone or the JVM default", function() {

			it( title="pattern layout (default pattern)", body=function( currentSpec ) {
				// dd.MM.yyyy HH:mm:ss,SSS
				checkLog( "pattern", "dd.MM.yyyy HH", "^\d{2}\.\d{2}\.\d{4} \d{2}" );
			});

		});
	}

	private function checkLog( required string layout, required string mask, required string regex ) {
		var TimeZone = createObject( "java", "java.util.TimeZone" );
		expect( getPageContext().getConfig().getTimeZone().getID() ).toBe( variables.configTZ );
		// the JVM default has to differ from both, otherwise the test proves nothing
		expect( [ variables.configTZ, variables.requestTZ ] ).notToInclude( TimeZone.getDefault().getID() );

		var id = lCase( createUUID() );
		var token = createUUID();
		var logName = "ldev6598-#arguments.layout#-#id#";
		var logFile = expandPath( "{lucee-config}/logs/#logName#.log" );
		try {
			var before = fmt( arguments.mask, variables.configTZ );
			var result = _InternalRequest(
				template: "#variables.uri#/ldev6598.cfm",
				url: { id: id, tz: variables.requestTZ, log: logName, token: token }
			);
			var after = fmt( arguments.mask, variables.configTZ );
			// the request really ran with the request timezone
			expect( trim( result.filecontent ) ).toBe( variables.requestTZ );

			var line = findLine( fileRead( logFile ), token );
			var ts = reMatch( arguments.regex, line );
			expect( ts.len() ).toBe( 1, line );
			expect( [ before, after ] ).toInclude( ts[ 1 ],
				"#arguments.layout#: expected the config timezone [#variables.configTZ#: #before#], request timezone [#variables.requestTZ#: #fmt( arguments.mask, variables.requestTZ )#], JVM default [#TimeZone.getDefault().getID()#: #fmt( arguments.mask, TimeZone.getDefault().getID() )#], log line: #left( line, 150 )#" );
		}
		finally {
			if ( fileExists( logFile ) ) fileDelete( logFile );
		}
	}

	private string function fmt( required string mask, required string tz ) {
		var sdf = createObject( "java", "java.text.SimpleDateFormat" ).init( arguments.mask );
		sdf.setTimeZone( createObject( "java", "java.util.TimeZone" ).getTimeZone( arguments.tz ) );
		return sdf.format( createObject( "java", "java.util.Date" ).init() );
	}

	private string function findLine( required string content, required string needle ) {
		var lines = listToArray( arguments.content, chr( 10 ) );
		for ( var line in lines ) {
			if ( find( arguments.needle, line ) ) return trim( line );
		}
		throw( message="no line with [#arguments.needle#] in: " & arguments.content );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}
}
