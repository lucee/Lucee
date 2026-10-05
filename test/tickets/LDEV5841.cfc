component extends="org.lucee.cfml.test.LuceeTestCase" labels="expandPath" {

	function run( testResults, testbox ) {

		describe( title="LDEV-5841 expandPath behaviour differs between script-runner and Tomcat", body=function() {

			it( title="expandPath with dot should match getDirectoryFromPath", body=function( currentSpec ) {
				// expandPath(".") should resolve to current template directory
				var currentDir = getDirectoryFromPath( getCurrentTemplatePath() );
				var expandedDot = expandPath( "." );

				// Normalize both by adding trailing slash if missing (trailing slash difference is cosmetic)
				if ( !currentDir.endsWith( server.separator.file ) ) {
					currentDir &= server.separator.file;
				}
				if ( !expandedDot.endsWith( server.separator.file ) ) {
					expandedDot &= server.separator.file;
				}

				expect( expandedDot ).toBe( currentDir, "expandPath('.') should resolve to current template directory" );
			});

			it( title="expandPath with relative path should resolve from current template", body=function( currentSpec ) {
				var currentDir = getDirectoryFromPath( getCurrentTemplatePath() );
				var testFile = "test.txt";
				var expanded = expandPath( testFile );

				expect( expanded ).toBe( currentDir & testFile );
			});

			it( title="expandPath with ./ should return current template directory", body=function( currentSpec ) {
				var currentDir = getDirectoryFromPath( getCurrentTemplatePath() );
				var expanded = expandPath( "./" );

				expect( expanded ).toBe( currentDir );
			});

			it( title="expandPath with relative subdir path", body=function( currentSpec ) {
				var currentDir = getDirectoryFromPath( getCurrentTemplatePath() );
				var expanded = expandPath( "./subdir/file.txt" );

				// Normalize for comparison (remove trailing slashes)
				var normalizedDir = replace( currentDir, "\", "/", "all" );
				if ( right( normalizedDir, 1 ) == "/" ) {
					normalizedDir = left( normalizedDir, len( normalizedDir ) - 1 );
				}
				var normalizedExpanded = replace( expanded, "\", "/", "all" );

				expect( normalizedExpanded ).toInclude( normalizedDir );
				expect( normalizedExpanded ).toInclude( "subdir" );
				expect( normalizedExpanded ).toInclude( "file.txt" );
			});

		});

	}

}
