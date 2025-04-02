component {

	function analyze( temp ){
		systemOutput("", true);
		systemOutput("-- test individual archives size and performance --", true);
		
		var archiveDir = getTempDirectory( "archiveDir" );
		var stored = getTempDirectory( "storedDir" );

		// copy the lar archives into a unique directory
		var files = directoryList(path=arguments.temp, listinfo="query",filter="*.lar");
		for (var f in files){
			if (f contains "testbox") continue; // testbox isn't deployed, therefore, not interesting
			fileCopy( arguments.temp & "/" & f.name, archiveDir & f.name );
		}
		files = directoryList(path=archiveDir, listinfo="query",filter="*.lar");

		reportFiles( files, stored, archiveDir );

		systemOutput("-- DEFLATE combined archives size and extract performance --", true);
		benchArchives( archiveDir, "*.lar", "compressed" );
		systemOutput("-- STORED combined archives size and extract performance --", true);
		benchArchives( stored, "*.lar", "store" );
	}

	function reportFiles( files, workDir, srcDir ){
		for (var f in arguments.files){
			var store = rezipWith( workDir, srcDir & "/" & f.name, "STORE" );
			var storeInfo = fileInfo( store );
			systemOutput( LJustify( f.name, 20 ) & chr(9)
					& "#LJustify( numberFormat( f.size / 1024 ), 6)# Kb using DEFLATE ( extracts in #benchmarkUnzip( srcDir & "/" & f.name )# ms ), "
					& "#LJustify( numberFormat( storeInfo.size / 1024 ), 6)# Kb using STORE, ( extracts in #benchmarkUnzip( store )# ms )",
				true);
		}
	}

	function benchArchives( dir, filter, label){
		arrayEach( [ "deflate", "deflateFastest", "store" ], function( level ){
			var zip = getTempFile("", "zip", "zip");
			zipDir( zip, dir, filter, arguments.level );
			var info = fileInfo( zip );
			var extract = benchmarkUnzip( zip );
			var deploy = benchmarkUnzip( zip, true );
			var total = numberFormat( parseNumber( trim( extract ) ) + parseNumber( trim( deploy ) ) );
			systemOutput( LJustify( level, 20 ) & chr(9)
				& "#LJustify( numberFormat( info.size / 1024 ), 4 )# Kb, "
				& "extracts in #extract# ms, "
				& "deploys in #deploy# ms, "
				& "total #total# ms ",
				true);
		});
	}
	
	function rezipWith( destDir, zipPath, compressionLevel, recurse=true ) {
		if (!fileExists(zipPath)) {
			throw "The specified ZIP file does not exist: " & zipPath;
		}

		var tempDir = getTempDirectory( "unzipped" );
		cfzip( action="unzip", file=zipPath, destination=tempDir );
		var newZipPath = destDir & getFileFromPath(zipPath);

		cfzip(
			action="zip",
			source=tempDir,
			file=newZipPath,
			compressionMethod=arguments.compressionLevel,
			recurse=arguments.recurse
		);

		// Clean up temporary directory
		directoryDelete( tempDir, true );

		return newZipPath;
	}

	function zipDir( destZip, sourceDir, filter, compressionLevel) {
		cfzip(
			action="zip",
			source=arguments.sourceDir,
			filter=arguments.filter,
			file=arguments.destZip,
			compressionMethod=arguments.compressionLevel
		);
	}

	function benchmarkUnzip( zipFile, recurse=false ){
		var tempDir = getTempDirectory( "stored" );
		var s = getTickCount();
		extract( "zip", zipFile, tempdir );
		if ( arguments.recurse ){
			var files = directoryList(path=tempdir, listinfo="path",filter="*.lar");
			arrayEach( files, function( file ){
				benchmarkUnzip(file, false);
			});

		}
		return LJustify(numberformat( getTickCount() -s ), 4);
	}

}