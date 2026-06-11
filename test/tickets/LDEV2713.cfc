component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.testDir = getTempDirectory() & "LDEV2713/";
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
		describe( "LDEV-2713 fileCopy permission handling", function() {

			it( title="copied file should be writable by owner (Windows)", skip=isNotWindows(), body=function() {
				var srcFile = variables.testDir & "source-win.txt";
				var destFile = variables.testDir & "dest-win.txt";

				fileWrite( srcFile, "test content" );
				fileCopy( srcFile, destFile );

				// should be able to write to the copied file
				fileWrite( destFile, "modified content" );
				expect( fileRead( destFile ) ).toBe( "modified content" );
			});

			it( title="copied file should preserve mode (Linux)", skip=isWindows(), body=function() {
				var srcFile = variables.testDir & "source-mode.txt";
				var destFile = variables.testDir & "dest-mode.txt";

				fileWrite( srcFile, "test content" );
				fileSetAccessMode( srcFile, "640" );

				fileCopy( srcFile, destFile );

				var destInfo = fileInfo( destFile );
				expect( destInfo.mode ).toBe( "640" );
			});

			it( title="copied file should preserve executable bit (Linux)", skip=isWindows(), body=function() {
				var srcFile = variables.testDir & "source-exec.txt";
				var destFile = variables.testDir & "dest-exec.txt";

				fileWrite( srcFile, "##!/bin/bash" );
				fileSetAccessMode( srcFile, "755" );

				fileCopy( srcFile, destFile );

				var destInfo = fileInfo( destFile );
				expect( destInfo.mode ).toBe( "755" );
			});

			it( title="copied file should be writable when source is writable (Linux)", skip=isWindows(), body=function() {
				var srcFile = variables.testDir & "source-rw.txt";
				var destFile = variables.testDir & "dest-rw.txt";

				fileWrite( srcFile, "test content" );
				fileSetAccessMode( srcFile, "644" );

				fileCopy( srcFile, destFile );

				// should be able to write to the copied file
				fileWrite( destFile, "modified content" );
				expect( fileRead( destFile ) ).toBe( "modified content" );
			});

		});
	}

	private function isWindows() {
		return server.os.name contains "windows";
	}

	private function isNotWindows() {
		return !isWindows();
	}

}
