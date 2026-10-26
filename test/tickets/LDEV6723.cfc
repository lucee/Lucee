component extends="org.lucee.cfml.test.LuceeTestCase" labels="config" {

	function beforeAll() {
		variables.tempRoot = getTempDirectory() & "ldev6723-" & createUUID() & "/";
		directoryCreate( variables.tempRoot );
		variables.factory = createObject( "java", "lucee.runtime.config.ConfigFactoryImpl" );
	}

	function afterAll() {
		if ( directoryExists( variables.tempRoot ) ) directoryDelete( variables.tempRoot, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6723 cfclasses are purged when the engine version changes", function() {

			it( "the exeLog marker written at startup contains the current engine version", function() {
				var marker = getPageContext().getConfig().getConfigDir().getRealResource( "exeLog" );
				expect( marker.exists() ).toBeTrue( "exeLog marker is missing" );
				var val = fileRead( marker.getAbsolutePath() );
				expect( listLen( val, ":", true ) ).toBe( 3, "unexpected marker format [#val#]" );
				expect( listLast( val, ":", true ) ).toBe( server.lucee.version );
			});

			it( "purges cfclasses when the marker has a different engine version", function() {
				var dirs = createDirs( "old-version" );
				fileWrite( dirs.configDir & "exeLog", "false:false:7.1.1.0" );

				var purged = callPurge( dirs, "false:false:8.0.1.0" );

				expect( purged ).toBeTrue();
				expect( fileExists( dirs.classFile ) ).toBeFalse( "stale class file was not purged" );
				expect( fileRead( dirs.configDir & "exeLog" ) ).toBe( "false:false:8.0.1.0" );
			});

			it( "purges cfclasses when the marker uses the old format without engine version", function() {
				var dirs = createDirs( "old-format" );
				fileWrite( dirs.configDir & "exeLog", "false:false" );

				var purged = callPurge( dirs, "false:false:8.0.1.0" );

				expect( purged ).toBeTrue();
				expect( fileExists( dirs.classFile ) ).toBeFalse( "stale class file was not purged" );
				expect( fileRead( dirs.configDir & "exeLog" ) ).toBe( "false:false:8.0.1.0" );
			});

			it( "purges cfclasses and writes the marker when no marker exists", function() {
				var dirs = createDirs( "no-marker" );

				var purged = callPurge( dirs, "false:false:8.0.1.0" );

				expect( purged ).toBeTrue();
				expect( fileExists( dirs.classFile ) ).toBeFalse( "stale class file was not purged" );
				expect( fileRead( dirs.configDir & "exeLog" ) ).toBe( "false:false:8.0.1.0" );
			});

			it( "keeps cfclasses when the marker matches", function() {
				var dirs = createDirs( "same-version" );
				fileWrite( dirs.configDir & "exeLog", "false:false:8.0.1.0" );

				var purged = callPurge( dirs, "false:false:8.0.1.0" );

				expect( purged ).toBeFalse();
				expect( fileExists( dirs.classFile ) ).toBeTrue( "class file was purged although the marker matches" );
			});

		});
	}

	private struct function createDirs( required string name ) {
		var base = variables.tempRoot & arguments.name & "/";
		var dirs = {
			configDir: base & "config/",
			classDir: base & "cfclasses/"
		};
		directoryCreate( dirs.configDir, true );
		directoryCreate( dirs.classDir & "sub/", true );
		dirs.classFile = dirs.classDir & "sub/test_cfm$cf.class";
		fileWrite( dirs.classFile, "stale" );
		return dirs;
	}

	private boolean function callPurge( required struct dirs, required string val ) {
		var config = getPageContext().getConfig();
		return variables.factory.purgeClassDirectoryIfStale(
			config,
			config.getResource( arguments.dirs.configDir ),
			config.getResource( arguments.dirs.classDir ),
			arguments.val
		);
	}

}
