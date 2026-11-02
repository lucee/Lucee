component extends="org.lucee.cfml.test.LuceeTestCase" labels="log,maven" {

	variables.mvn = "lucee.test.ldev6820:logging:1.0";
	variables.dir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6820/";
	variables.logDir = variables.dir & "logs/";

	function beforeAll() {
		// install the test artifact (a log4j2 appender and a log4j2 layout, see artifact/src) into the maven directory of Lucee
		var target = getPageContext().getConfig().getMavenDir().getRealResource( "lucee/test/ldev6820/logging/1.0" );
		var targetDir = target.getAbsolutePath();
		directoryCreate( targetDir, true, true );
		fileCopy( variables.dir & "artifact/logging-1.0.jar", targetDir & "/logging-1.0.jar" );
		fileCopy( variables.dir & "artifact/logging-1.0.pom", targetDir & "/logging-1.0.pom" );
		directoryCreate( variables.logDir, true, true );
	}

	function afterAll() {
		if ( directoryExists( variables.logDir ) ) directoryDelete( variables.logDir, true );
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-6820 maven coordinates for log appenders and layouts", body=function() {

			it( title="the maven coordinates of an appender defined via the admin are kept", body=function( currentSpec ) {
				var name = "ldev6820_" & replace( lCase( createUUID() ), "-", "", "all" );
				var pw = request.ServerAdminPassword;
				try {
					admin
						action="updateLogSettings" type="server" password="#pw#"
						name="#name#" level="info"
						appenderClass="lucee.test.Plain6820" appenderMaven="#variables.mvn#" appenderArgs={}
						layoutClass="classic" layoutArgs={};

					// stored with the maven coordinates
					var cfg = deserializeJSON( fileRead( expandPath( "{lucee-config}/.CFConfig.json" ) ) );
					expect( cfg.loggers ).toHaveKey( name );
					var logger = cfg.loggers[ name ];
					expect( logger.appenderClass ).toBe( "lucee.test.Plain6820" );
					expect( logger.appenderMaven ?: "" ).toBe( variables.mvn, serializeJSON( logger ) );

					// and returned by getLogSettings
					admin action="getLogSettings" type="server" password="#pw#" returnVariable="local.loggers";
					var found = false;
					loop query="local.loggers" {
						if ( local.loggers.name != name ) continue;
						found = true;
						expect( local.loggers.appenderClass ).toBe( "lucee.test.Plain6820" );
						expect( local.loggers.appenderMaven ).toBe( variables.mvn );
					}
					expect( found ).toBeTrue( "logger [#name#] not found in getLogSettings" );
				}
				finally {
					admin action="removeLogSetting" type="server" password="#pw#" name="#name#";
				}
			} );

		} );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/LDEV6820/";
		return baseURI & calledName;
	}

}
