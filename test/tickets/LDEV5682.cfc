component extends="org.lucee.cfml.test.LuceeTestCase" labels="log" {

	function run( testResults, testBox ) {
		describe( "LDEV-5682 logger config created via the admin must not pin the Lucee core bundle version", function() {

			it( title="console appender / classic layout", body=function( currentSpec ) {
				checkLogger( "console", "classic" );
			});

			it( title="resource appender / pattern layout", body=function( currentSpec ) {
				checkLogger( "resource", "pattern" );
			});

			it( title="a logger with a stale lucee.core bundle version still loads its appender", body=function( currentSpec ) {
				var cd = createObject( "java", "lucee.transformer.library.ClassDefinitionImpl" ).init(
					"lucee.commons.io.log.log4j2.appender.ConsoleAppender", "lucee.core", "6.0.0.1", getPageContext().getConfig().getIdentification()
				);
				expect( cd.getClazz().getName() ).toBe( "lucee.commons.io.log.log4j2.appender.ConsoleAppender" );
			});

		});
	}

	private function checkLogger( required string appender, required string layout ) {
		var name = "ldev5682_" & arguments.appender & "_" & replace( lCase( createUUID() ), "-", "", "all" );
		var pw = request.ServerAdminPassword;
		try {
			admin
				action="updateLogSettings" type="server" password="#pw#"
				name="#name#" level="info" appenderClass="#arguments.appender#" appenderArgs={}
				layoutClass="#arguments.layout#" layoutArgs={};

			var cfg = deserializeJSON( fileRead( expandPath( "{lucee-config}/.CFConfig.json" ) ) );
			var loggers = cfg.loggers ?: {};
			expect( loggers ).toHaveKey( name );
			var logger = loggers[ name ];
			var json = serializeJSON( logger );
			expect( logger ).notToHaveKey( "appenderBundleVersion", json );
			expect( logger ).notToHaveKey( "layoutBundleVersion", json );
			expect( logger ).notToHaveKey( "appenderBundleName", json );
			expect( logger ).notToHaveKey( "layoutBundleName", json );
			expect( logger.appenderClass ?: "" ).toInclude( "lucee.commons.io.log", json );

			// the logger still works
			writeLog( text="ldev5682 test", log=name, type="information" );
		}
		finally {
			try {
				admin action="removeLogSetting" type="server" password="#pw#" name="#name#";
			}
			catch ( any e ) {}
		}
	}
}
