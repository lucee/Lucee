component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.testDir = getTempDirectory() & "LDEV6095/";
		// cleanup at start, leave artifacts for inspection after test
		if ( directoryExists( variables.testDir ) )
			directoryDelete( variables.testDir, true );
		directoryCreate( variables.testDir );
	}

	function afterAll() {
		if ( directoryExists( variables.testDir ) )
			directoryDelete( variables.testDir, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6095 fileCopy should preserve owner write permission", function() {

			it( title="copied file should be writable when source is read-only", skip=isWindows(), body=function() {
				var srcFile = variables.testDir & "source-readonly.txt";
				var destFile = variables.testDir & "dest-readonly.txt";

				// create source file and make it read-only
				fileWrite( srcFile, "test content" );
				fileSetAccessMode( srcFile, "444" );

				// verify source is read-only
				var srcInfo = fileInfo( srcFile );
				expect( srcInfo.mode ).toBe( "444" );

				// copy the file
				fileCopy( srcFile, destFile );

				// destination should be writable by owner
				// the owner just created this file, they should be able to write to it
				var destInfo = fileInfo( destFile );
				var destMode = destInfo.mode;
				var ownerWrite = mid( destMode, 1, 1 );

				// owner permission should include write (6 or 7, i.e., rw or rwx)
				expect( listFind( "2,3,6,7", ownerWrite ) ).toBeGT( 0,
					"Destination file owner should have write permission, got mode: #destMode#" );
			});

			it( title="copied file should be writable when source has group read-only", skip=isWindows(), body=function() {
				var srcFile = variables.testDir & "source-groupro.txt";
				var destFile = variables.testDir & "dest-groupro.txt";

				// create source file with owner rw, group read-only, other read-only
				fileWrite( srcFile, "test content" );
				fileSetAccessMode( srcFile, "644" );

				// copy the file
				fileCopy( srcFile, destFile );

				// destination owner should have write permission
				var destInfo = fileInfo( destFile );
				var destMode = destInfo.mode;
				var ownerWrite = mid( destMode, 1, 1 );

				expect( listFind( "2,3,6,7", ownerWrite ) ).toBeGT( 0,
					"Destination file owner should have write permission, got mode: #destMode#" );
			});

			it( title="copied file should preserve executable bit", skip=isWindows(), body=function() {
				var srcFile = variables.testDir & "source-exec.txt";
				var destFile = variables.testDir & "dest-exec.txt";

				// create source file with executable permission
				fileWrite( srcFile, "##!/bin/bash" );
				fileSetAccessMode( srcFile, "755" );

				// verify source is executable
				var srcInfo = fileInfo( srcFile );
				expect( srcInfo.mode ).toBe( "755" );

				// copy the file
				fileCopy( srcFile, destFile );

				// destination should also be executable
				var destInfo = fileInfo( destFile );
				var ownerPerm = mid( destInfo.mode, 1, 1 );

				// owner permission should include execute (1, 3, 5, or 7)
				expect( listFind( "1,3,5,7", ownerPerm ) ).toBeGT( 0,
					"Destination file should preserve executable bit, got mode: #destInfo.mode#" );
			});

		});
	}

	private function isWindows() {
		return server.os.name contains "windows";
	}

}
