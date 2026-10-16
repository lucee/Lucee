component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "Compress/Extract functions", function() {

			// core formats (always available)
			it( "can compress and extract zip", function() {
				doCompressExtract( "zip", "m" );
			});

			it( "can compress and extract gzip", function() {
				doCompressExtract( "gzip", "s" );
			});

			// extension formats (require compress extension)
			it( title="can compress and extract tgz", skip=!hasCompressExtension(), body=function() {
				doCompressExtract( "tgz", "m" );
			});

			it( title="can compress and extract tar", skip=!hasCompressExtension(), body=function() {
				doCompressExtract( "tar", "m" );
			});

			it( title="can compress and extract tbz", skip=!hasCompressExtension(), body=function() {
				doCompressExtract( "tbz", "m" );
			});

			it( title="can compress and extract bzip", skip=!hasCompressExtension(), body=function() {
				doCompressExtract( "bzip", "s" );
			});

		});
	}

	private boolean function hasCompressExtension() {
		return extensionExists( "8D7FB0DF-08BB-1589-FE3975678F07DB17" );
	}

	private void function doCompressExtract( required string format, required string type ) {
		var tmp = getTempDirectory() & "compress-test-" & createUUID() & "/";
		var srcDir = tmp & "src";
		var trgDir = tmp & "trg";
		var trg2Dir = tmp & "trg2";

		if ( directoryExists( tmp ) ) directoryDelete( tmp, true );
		directoryCreate( tmp );

		try {
			// source
			var src = srcDir & "/susi.txt";
			directoryCreate( srcDir );
			fileWrite( src, "Susi Sorglos foehnte Ihr Haar..." );

			// target
			var trg = trgDir & "/susi." & format;
			directoryCreate( trgDir );

			compress( format: format, source: type == "m" ? srcDir : src, target: trg, includeBaseFolder: true );
			assertTrue( fileExists( trg ), "compressed file should exist for format #format#" );

			// target 2 - extract
			var trg2 = trg2Dir & "/susi." & format & ".txt";
			directoryCreate( trg2Dir );

			extract( format, trg, type == "m" ? trg2Dir : trg2 );
			if ( type == "m" ) {
				assertTrue( fileExists( trg2Dir & "/src/susi.txt" ), "extracted file should exist for format #format#" );
			}
			else {
				assertTrue( fileExists( trg2 ), "extracted file should exist for format #format#" );
			}
		}
		finally {
			if ( directoryExists( tmp ) ) directoryDelete( tmp, true );
		}
	}

}
