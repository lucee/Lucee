component extends="org.lucee.cfml.test.LuceeTestCase" labels="log" {

	variables.LogFactory = createObject( "java", "lucee.commons.io.log.LogFactory" );
	variables.LoggerAndSourceData = createObject( "java", "lucee.commons.io.log.LoggerAndSourceData" );

	function beforeAll() {
		variables.dir = getTempDirectory() & "ldev6792_" & replace( lCase( createUUID() ), "-", "", "all" ) & "/";
		directoryCreate( variables.dir, true, true );
	}

	function afterAll() {
		if ( directoryExists( variables.dir ) ) directoryDelete( variables.dir, true );
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-6792 multiple appenders per logger", body=function() {

			it( title="a logger with a single appender has no appender list and the same id as before", body=function( currentSpec ) {
				var las = load( "single", { appender: "resource", appenderArguments: { path: file( "single.log" ) }, layout: "classic", level: "info" } );
				try {
					expect( las.getAppenders() ).toHaveLength( 1 );
					expect( las.getAppenderClassDefinition().getClassName() ).toInclude( "Resource" );

					var ad = las.getAppenders()[ 1 ];
					var legacyId = variables.LoggerAndSourceData.id( "single", ad.appender, ad.appenderArgs, ad.layout, ad.layoutArgs, las.getLevel(), las.getReadOnly() );
					expect( las.id() ).toBe( legacyId );

					las.getLog( false ).info( "app", "single-message" );
					expect( waitForContent( file( "single.log" ), "single-message" ) ).toBeTrue( "the message was not written" );
				}
				finally {
					las.close();
				}
			} );

			it( title="all appenders of a logger receive the message", body=function( currentSpec ) {
				var las = load( "multi", {
					level: "info",
					layout: "classic",
					appenders: [
						{ appender: "resource", appenderArguments: { path: file( "multi1.log" ) } },
						{ appender: "resource", appenderArguments: { path: file( "multi2.log" ) } }
					]
				} );
				try {
					expect( las.getAppenders() ).toHaveLength( 2 );
					las.getLog( false ).info( "app", "multi-message" );
					expect( waitForContent( file( "multi1.log" ), "multi-message" ) ).toBeTrue( "the first appender did not get the message" );
					expect( waitForContent( file( "multi2.log" ), "multi-message" ) ).toBeTrue( "the second appender did not get the message" );
				}
				finally {
					las.close();
				}
			} );

			it( title="every appender can have its own layout, without one it gets the layout of the logger", body=function( currentSpec ) {
				var las = load( "layouts", {
					level: "info",
					layout: "classic",
					appenders: [
						{ appender: "resource", appenderArguments: { path: file( "layout-pattern.log" ) }, layout: "pattern", layoutArguments: { pattern: "PATTERN6792 %m%n" } },
						{ appender: "resource", appenderArguments: { path: file( "layout-default.log" ) } }
					]
				} );
				try {
					las.getLog( false ).info( "app", "layout-message" );
					expect( waitForContent( file( "layout-pattern.log" ), "layout-message" ) ).toBeTrue();
					expect( waitForContent( file( "layout-default.log" ), "layout-message" ) ).toBeTrue();

					var pattern = fileRead( file( "layout-pattern.log" ) );
					expect( pattern ).toInclude( "PATTERN6792 layout-message" );

					// the classic layout of the logger quotes the values
					var classic = fileRead( file( "layout-default.log" ) );
					expect( classic ).toInclude( '"INFO"' );
					expect( classic ).notToInclude( "PATTERN6792" );
				}
				finally {
					las.close();
				}
			} );

			it( title="the level of the logger applies to all appenders", body=function( currentSpec ) {
				var las = load( "levels", {
					level: "error",
					layout: "classic",
					appenders: [
						{ appender: "resource", appenderArguments: { path: file( "level1.log" ) } },
						{ appender: "resource", appenderArguments: { path: file( "level2.log" ) } }
					]
				} );
				try {
					var log = las.getLog( false );
					log.info( "app", "level-info-message" );
					log.error( "app", "level-error-message" );
					expect( waitForContent( file( "level1.log" ), "level-error-message" ) ).toBeTrue();
					expect( waitForContent( file( "level2.log" ), "level-error-message" ) ).toBeTrue();
					expect( fileRead( file( "level1.log" ) ) ).notToInclude( "level-info-message" );
					expect( fileRead( file( "level2.log" ) ) ).notToInclude( "level-info-message" );
				}
				finally {
					las.close();
				}
			} );

			it( title="the id changes with every appender", body=function( currentSpec ) {
				var base = function( path2 ) {
					return {
						level: "info",
						layout: "classic",
						appenders: [
							{ appender: "resource", appenderArguments: { path: file( "id1.log" ) } },
							{ appender: "resource", appenderArguments: { path: file( path2 ) } }
						]
					};
				};
				var a = load( "ids", base( "id2.log" ) );
				var b = load( "ids", base( "id2.log" ) );
				var c = load( "ids", base( "id3.log" ) );
				var single = load( "ids", { level: "info", layout: "classic", appender: "resource", appenderArguments: { path: file( "id1.log" ) } } );
				try {
					expect( a.id() ).toBe( b.id() );
					expect( c.id() ).notToBe( a.id() );
					expect( single.id() ).notToBe( a.id() );
				}
				finally {
					a.close();
					b.close();
					c.close();
					single.close();
				}
			} );

			it( title="a logger with an empty appenders array uses the single appender", body=function( currentSpec ) {
				var las = load( "emptylist", { level: "info", layout: "classic", appender: "resource", appenderArguments: { path: file( "empty.log" ) }, appenders: [] } );
				try {
					expect( las.getAppenders() ).toHaveLength( 1 );
					las.getLog( false ).info( "app", "empty-list-message" );
					expect( waitForContent( file( "empty.log" ), "empty-list-message" ) ).toBeTrue();
				}
				finally {
					las.close();
				}
			} );

			it( title="a logger defined with the admin with one appender is stored in the old format", body=function( currentSpec ) {
				var name = createName();
				try {
					admin
						action="updateLogSettings" type="server" password="#request.ServerAdminPassword#"
						name="#name#" level="info"
						appenderClass="resource" appenderArgs={ path: file( "admin-single.log" ) }
						layoutClass="classic" layoutArgs={};

					var logger = readConfig().loggers[ name ];
					expect( logger ).notToHaveKey( "appenders" );
					expect( logger ).toHaveKey( "appenderClass" );
					expect( logger ).toHaveKey( "layoutClass" );

					var row = getLogSetting( name );
					expect( row.appenders ).toHaveLength( 1 );
					expect( row.appenders[ 1 ].appenderClass ).toBe( row.appenderClass );
					expect( row.appenders[ 1 ].appenderArgs.path ).toBe( file( "admin-single.log" ) );

					writeLog( text="admin-single-message", type="information", log=name );
					expect( waitForContent( file( "admin-single.log" ), "admin-single-message" ) ).toBeTrue();
				}
				finally {
					admin action="removeLogSetting" type="server" password="#request.ServerAdminPassword#" name="#name#";
				}
			} );

			it( title="appenders defined with the admin are stored, returned and used, and can be changed back to a single appender", body=function( currentSpec ) {
				var name = createName();
				var pw = request.ServerAdminPassword;
				try {
					admin
						action="updateLogSettings" type="server" password="#pw#"
						name="#name#" level="info"
						appenders=[
							{ appenderClass: "resource", appenderArgs: { path: file( "admin1.log" ) }, layoutClass: "pattern", layoutArgs: { pattern: "ADMIN6792 %m%n" } },
							{ appenderClass: "resource", appenderArgs: { path: file( "admin2.log" ) }, layoutClass: "classic", layoutArgs: {} }
						];

					// config
					var logger = readConfig().loggers[ name ];
					expect( logger.appenders ).toHaveLength( 2 );
					expect( logger ).notToHaveKey( "appenderClass" );
					expect( logger ).notToHaveKey( "layoutClass" );
					expect( logger.appenders[ 1 ].appenderArguments.path ).toBe( file( "admin1.log" ) );
					expect( logger.appenders[ 2 ].appenderArguments.path ).toBe( file( "admin2.log" ) );
					expect( logger.appenders[ 1 ].layoutArguments.pattern ).toBe( "ADMIN6792 %m%n" );

					// API, the first appender is still in the old fields
					var row = getLogSetting( name );
					expect( row.appenders ).toHaveLength( 2 );
					expect( row.appenderArgs.path ).toBe( file( "admin1.log" ) );
					expect( row.layoutArgs.pattern ).toBe( "ADMIN6792 %m%n" );
					expect( row.appenders[ 1 ].appenderArgs.path ).toBe( file( "admin1.log" ) );
					expect( row.appenders[ 2 ].appenderArgs.path ).toBe( file( "admin2.log" ) );
					expect( row.appenders[ 2 ].layoutClass ).toInclude( "Classic" );

					// both get the message, each with its layout
					writeLog( text="admin-multi-message", type="information", log=name );
					expect( waitForContent( file( "admin1.log" ), "admin-multi-message" ) ).toBeTrue( "first appender" );
					expect( waitForContent( file( "admin2.log" ), "admin-multi-message" ) ).toBeTrue( "second appender" );
					expect( fileRead( file( "admin1.log" ) ) ).toInclude( "ADMIN6792 admin-multi-message" );
					expect( fileRead( file( "admin2.log" ) ) ).toInclude( '"INFO"' );

					// the layout of the call is the layout of an appender without layout
					admin
						action="updateLogSettings" type="server" password="#pw#"
						name="#name#" level="info"
						layoutClass="pattern" layoutArgs={ pattern: "DEFAULT6792 %m%n" }
						appenders=[
							{ appenderClass: "resource", appenderArgs: { path: file( "admin3.log" ) } },
							{ appenderClass: "resource", appenderArgs: { path: file( "admin4.log" ) }, layoutClass: "classic", layoutArgs: {} }
						];
					writeLog( text="admin-default-layout-message", type="information", log=name );
					expect( waitForContent( file( "admin3.log" ), "admin-default-layout-message" ) ).toBeTrue();
					expect( waitForContent( file( "admin4.log" ), "admin-default-layout-message" ) ).toBeTrue();
					expect( fileRead( file( "admin3.log" ) ) ).toInclude( "DEFAULT6792 admin-default-layout-message" );
					expect( fileRead( file( "admin4.log" ) ) ).notToInclude( "DEFAULT6792" );

					// back to one appender, back to the old format
					admin
						action="updateLogSettings" type="server" password="#pw#"
						name="#name#" level="info"
						appenderClass="resource" appenderArgs={ path: file( "admin5.log" ) }
						layoutClass="classic" layoutArgs={};
					logger = readConfig().loggers[ name ];
					expect( logger ).notToHaveKey( "appenders" );
					expect( logger ).toHaveKey( "appenderClass" );
					expect( getLogSetting( name ).appenders ).toHaveLength( 1 );

					// and the same with a list of one appender
					admin
						action="updateLogSettings" type="server" password="#pw#"
						name="#name#" level="info"
						appenders=[ { appenderClass: "resource", appenderArgs: { path: file( "admin6.log" ) }, layoutClass: "classic", layoutArgs: {} } ];
					logger = readConfig().loggers[ name ];
					expect( logger ).notToHaveKey( "appenders" );
					expect( logger.appenderArguments.path ).toBe( file( "admin6.log" ) );
				}
				finally {
					admin action="removeLogSetting" type="server" password="#pw#" name="#name#";
				}
			} );

			it( title="an appender without layout is not accepted by the admin when the call has no layout either", body=function( currentSpec ) {
				var name = createName();
				try {
					expect( function() {
						admin
							action="updateLogSettings" type="server" password="#request.ServerAdminPassword#"
							name="#name#" level="info"
							appenders=[
								{ appenderClass: "resource", appenderArgs: { path: file( "invalid1.log" ) }, layoutClass: "classic", layoutArgs: {} },
								{ appenderClass: "resource", appenderArgs: { path: file( "invalid2.log" ) } }
							];
					} ).toThrow( message="Layout class is required" );
				}
				finally {
					admin action="removeLogSetting" type="server" password="#request.ServerAdminPassword#" name="#name#";
				}
			} );

		} );
	}

	private string function file( required string name ) {
		return variables.dir & arguments.name;
	}

	private string function createName() {
		return "ldev6792_" & replace( lCase( createUUID() ), "-", "", "all" );
	}

	private struct function readConfig() {
		return deserializeJSON( fileRead( expandPath( "{lucee-config}/.CFConfig.json" ) ) );
	}

	private struct function getLogSetting( required string name ) {
		admin action="getLogSettings" type="server" password="#request.ServerAdminPassword#" returnVariable="local.loggers";
		var result = queryExecute( "SELECT * FROM loggers WHERE name = :name", { name: arguments.name }, { dbtype: "query" } );
		expect( result.recordCount ).toBe( 1, "logger [#arguments.name#] not found in getLogSettings" );
		return queryGetRow( result, 1 );
	}

	// loads a logger the same way as the config does
	private function load( required string name, required struct data ) {
		var config = getPageContext().getConfig();
		var las = variables.LogFactory.getInstance().evaluate( config, arguments.name, arguments.data, 0 );
		expect( isNull( las ) ).toBeFalse( "the logger [#arguments.name#] was not created" );
		return las;
	}

	// the log files are written in the background
	private boolean function waitForContent( required string path, required string text ) {
		loop times=100 {
			if ( fileExists( arguments.path ) && find( arguments.text, fileRead( arguments.path ) ) ) return true;
			sleep( 100 );
		}
		return false;
	}

}
