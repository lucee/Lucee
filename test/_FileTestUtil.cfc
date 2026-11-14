component {

	// Shared file system helpers for tests.
	//
	// On Windows a delete can still hit a handle that a log appender, the archive mapper or
	// another thread holds open. That is expected, does not mean the test failed, and a
	// leftover file in the temp or logs directory is harmless (LDEV-5145).
	//
	// A concurrent recursive delete is a different case: the losing thread gets a
	// FileSystemException (access denied, the entry is delete pending, message often only the
	// path). LDEV-6819 covers the NoSuchFileException half of that race, the Windows half is
	// not covered yet, see isExpectedDeleteRaceError().

	public boolean function isWindows() {
		return server.os.name contains "windows";
	}

	// deletes a file, tolerating a Windows lock: retries briefly, then gives up without failing
	public void function deleteFileQuietly( required string file ) {
		loop from=1 to=5 index="local.i" {
			try {
				if ( fileExists( arguments.file ) ) fileDelete( arguments.file );
				return;
			}
			catch ( any e ) {
				if ( !isLockedDelete( e ) ) rethrow;
				sleep( 50 );
			}
		}
		systemOutput( "WARNING: could not delete [#arguments.file#], still locked", true );
	}

	// deletes a directory tree, tolerating a Windows lock: retries briefly, then gives up without failing
	public void function deleteDirectoryQuietly( required string dir ) {
		loop from=1 to=5 index="local.i" {
			try {
				if ( directoryExists( arguments.dir ) ) directoryDelete( arguments.dir, true );
				return;
			}
			catch ( any e ) {
				if ( !isLockedDelete( e ) ) rethrow;
				sleep( 50 );
			}
		}
		systemOutput( "WARNING: could not delete [#arguments.dir#], still locked", true );
	}

	// true when a thread lost a delete race, which is expected; the caller still has to assert
	// that the entry is gone, this only excuses the exception the losing thread saw
	public boolean function isExpectedDeleteRaceError( required string err, required string target ) {
		if ( arguments.err == "" ) return true;
		if ( reFindNoCase( "doesn't exist|does not exist", arguments.err ) > 0 ) return true;
		if ( !isWindows() ) return false;
		if ( reFindNoCase( "being used by another process|Can't delete", arguments.err ) > 0 ) return true;
		// the message can be the path of the entry that lost the race, expandPath normalises the
		// separators of both sides (and still works for paths that are already gone)
		var e = expandPath( arguments.err );
		var d = expandPath( arguments.target );
		return e == d || left( e, len( d ) + 1 ) == d & server.separator.file;
	}

	private boolean function isLockedDelete( required any e ) {
		if ( !isWindows() ) return false;
		var message = structKeyExists( arguments.e, "message" ) ? arguments.e.message : "";
		return message contains "another process" || message contains "delete file";
	}

}
