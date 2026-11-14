component extends="org.lucee.cfml.test.LuceeTestCase" labels="dump" {

	function beforeAll() {
		variables.dir = getTempDirectory() & "LDEV6936/";
		if (!directoryExists(variables.dir)) directoryCreate(variables.dir);
		var webroot = expandPath("/");
		if (right(webroot, 1) != "/" && right(webroot, 1) != "\\") webroot &= "/";
		variables.webDir = webroot & "LDEV6936-web/";
		if (!directoryExists(variables.webDir)) directoryCreate(variables.webDir);
		variables.created = [];
	}

	function afterAll() {
		for (var path in variables.created) {
			if (fileExists(path)) fileDelete(path);
		}
		if (directoryExists(variables.dir)) {
			directoryDelete(variables.dir, true);
		}
		if (directoryExists(variables.webDir)) {
			directoryDelete(variables.webDir, true);
		}
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6936 writeDump/cfdump output= file path resolution", function() {

			it( "writes a new file to an absolute path outside the webroot when the parent directory exists", function() {
				var dest = variables.dir & "dump-abs-#createUUID()#.txt";
				var shadow = expandPath( dest );
				// On Unix, expandPath() rewrites a non-existing /... path under the webroot.
				// The bug is that writeDump then created the file there instead of at dest.
				var token = "LDEV-6936-abs-#createUUID()#";

				writeDump( var=token, output=dest, format="text" );
				arrayAppend( variables.created, dest );
				if ( shadow != dest ) arrayAppend( variables.created, shadow );

				expect( fileExists( dest ) ).toBeTrue( "dump should be written to the absolute filesystem path [#dest#]" );
				expect( fileRead( dest ) ).toInclude( token );
				if ( shadow != dest ) {
					expect( fileExists( shadow ) ).toBeFalse( "dump must not be written to the expandPath() webroot location [#shadow#]" );
				}
			});

			it( "still resolves a webroot-relative output path with expandPath (LDEV-5586)", function() {
				// A leading-slash path whose parent already exists under the webroot
				// must keep using expandPath(), not the raw filesystem path.
				var dest = variables.webDir & "rel-#createUUID()#.txt";
				var virtual = "/LDEV6936-web/" & getFileFromPath( dest );
				var token = "LDEV-6936-rel-#createUUID()#";

				writeDump( var=token, output=virtual, format="text" );
				arrayAppend( variables.created, dest );

				expect( fileExists( dest ) ).toBeTrue( "webroot-relative output [#virtual#] should be resolved via expandPath() to [#dest#]" );
				expect( fileRead( dest ) ).toInclude( token );
			});

			it( "output=false still skips writing and is not treated as a filename (LDEV-5586)", function() {
				var falseAsFile = expandPath( "false" );
				var existed = fileExists( falseAsFile );
				var before = existed ? fileRead( falseAsFile ) : "";

				writeDump( var="LDEV-6936-false-#createUUID()#", output="false", format="text" );

				if ( !existed ) {
					expect( fileExists( falseAsFile ) ).toBeFalse();
				}
				else {
					expect( fileRead( falseAsFile ) ).toBe( before );
				}
			});

			it( "writes to a mapped virtual path when the expanded parent exists", function() {
				var phys = variables.dir & "mapped/";
				if ( !directoryExists( phys ) ) directoryCreate( phys );
				var settings = getApplicationSettings();
				var origMappings = structKeyExists( settings, "mappings" ) && isStruct( settings.mappings ) ? duplicate( settings.mappings ) : {};
				var mappings = duplicate( origMappings );
				mappings[ "/ldev6936out" ] = phys;
				try {
					application action="update" mappings="#mappings#";

					var dest = phys & "dump-map-#createUUID()#.txt";
					var virtual = "/ldev6936out/" & getFileFromPath( dest );
					var token = "LDEV-6936-map-#createUUID()#";

					writeDump( var=token, output=virtual, format="text" );
					arrayAppend( variables.created, dest );

					expect( fileExists( dest ) ).toBeTrue( "mapped output [#virtual#] should land in [#dest#]" );
					expect( fileRead( dest ) ).toInclude( token );
				}
				finally {
					application action="update" mappings="#origMappings#";
				}
			});

		});
	}
}
