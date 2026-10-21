component extends="org.lucee.cfml.test.LuceeTestCase" labels="java,maven,log" {

	function beforeAll() {
		variables.uri = createURI( "LDEV6664" );
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-6664 SLF4J 2.x from javaSettings.maven is bridged into Lucee logging", body=function() {

			it( title="SLF4J finds the Lucee provider instead of the NOP fallback, and its output reaches the application log", body=function() {
				var id = lCase( createUUID() );
				var token = createUUID();
				var logFile = expandPath( "{lucee-config}/logs/ldev6664-#id#.log" );
				try {
					var result = _InternalRequest(
						template: "#variables.uri#/ldev6664.cfm",
						url: { id: id, token: token }
					);
					// without a provider SLF4J uses org.slf4j.helpers.NOPLoggerFactory
					expect( trim( result.filecontent ) ).toBe( "lucee.commons.io.log.slf4j.LuceeSLF4JLoggerFactory" );
					expect( fileExists( logFile ) ).toBeTrue( "log file [#logFile#] missing" );
					var content = fileRead( logFile );
					expect( content ).toInclude( "ldev6664 #token#" );
					expect( content ).toInclude( "ERROR" );
				}
				finally {
					if ( fileExists( logFile ) ) fileDelete( logFile );
				}
			});
		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}
}
