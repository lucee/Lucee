component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.testDir = getTempDirectory() & "LDEV5437" & server.separator.file;
		variables.assetsDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV5437" & server.separator.file;
		// cleanup at start, leave artifacts for inspection after test
		if ( directoryExists( variables.testDir ) )
			directoryDelete( variables.testDir, true );
		setupTestFiles();
		variables.originalMappings = getApplicationSettings().mappings;
	}

	function afterAll() {
		// restore original mappings
		application action="update" mappings=variables.originalMappings;
	}

	private function setupTestFiles() {
		// Create structure:
		// testDir/
		//   outside-file.cfm            <- file OUTSIDE the mapped directory
		//   mapped/                     <- this directory will be mapped to /ldev5437mapped
		//     subdir/
		//       index.cfm               <- this file will try to include ../../outside-file.cfm

		var mappedDir = variables.testDir & "mapped" & server.separator.file;
		var subdir = mappedDir & "subdir" & server.separator.file;

		directoryCreate( subdir, true );

		// Copy test files from assets directory
		fileCopy( variables.assetsDir & "outside-file.cfm", variables.testDir & "outside-file.cfm" );
		fileCopy( variables.assetsDir & "index.cfm", subdir & "index.cfm" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-5437 path outside mapping resolution", function() {

			it( title="should resolve ../ path to file outside virtual mapping", body=function( currentSpec ) {
				// Set up a mapping that points to the "mapped" directory
				var mappings = duplicate( variables.originalMappings );
				mappings[ "/ldev5437mapped" ] = variables.testDir & "mapped";
				application action="update" mappings=mappings;

				// Now when we include /ldev5437mapped/subdir/index.cfm
				// and that file does cfinclude template="../../outside-file.cfm"
				// the ../ should resolve relative to the PHYSICAL path (testDir/mapped/subdir/)
				// NOT relative to the virtual mapping path (/ldev5437mapped/subdir/)
				//
				// Physical resolution: mapped/subdir/ -> mapped/ -> testDir/ -> outside-file.cfm (CORRECT)
				// Virtual resolution: /ldev5437mapped/subdir/ -> /ldev5437mapped/ -> / -> outside-file.cfm (WRONG - looks in webroot)

				cfinclude( template="/ldev5437mapped/subdir/index.cfm" );

				expect( result ).toBe( "outside-file-loaded" );
			});

			it( title="astFromPath should work with absolute path outside any mapping", body=function( currentSpec ) {
				// Create a file in temp directory - completely outside any mapping
				var testFile = variables.testDir & "ast-test-file.cfm";
				fileWrite( testFile, '<cfset x = 1>' );

				// astFromPath should be able to parse this file even though it's not under any mapping
				var ast = astFromPath( testFile );

				expect( ast ).toBeStruct();
				expect( ast ).toHaveKey( "body" );
			});

		});
	}

}
