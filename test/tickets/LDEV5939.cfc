component extends="org.lucee.cfml.test.LuceeTestCase" labels="directory" {

	function run( testResults, testBox ) {
		describe( "test case for LDEV-5939 - empty/blank directory path behaviour", function() {

			// Destructive operations - MUST throw to prevent accidental deletion of current directory
			describe( "destructive operations should throw with empty/blank path", function() {

				it( title="cfdirectory action=delete with empty directory should throw", body=function( currentSpec ) {
					expect( function() {
						directory action="delete" directory="" recurse="yes";
					}).toThrow();
				});

				it( title="cfdirectory action=delete with blank directory should throw", body=function( currentSpec ) {
					expect( function() {
						directory action="delete" directory="   " recurse="yes";
					}).toThrow();
				});

				it( title="cfdirectory action=rename with empty directory should throw", body=function( currentSpec ) {
					expect( function() {
						directory action="rename" directory="" newdirectory="foo";
					}).toThrow();
				});

				it( title="DirectoryDelete() with empty path should throw", body=function( currentSpec ) {
					expect( function() {
						DirectoryDelete( "" );
					}).toThrow();
				});

				it( title="DirectoryDelete() with blank path should throw", body=function( currentSpec ) {
					expect( function() {
						DirectoryDelete( "   " );
					}).toThrow();
				});

				it( title="DirectoryRename() with empty source path should throw", body=function( currentSpec ) {
					expect( function() {
						DirectoryRename( "", "newname" );
					}).toThrow();
				});

				it( title="DirectoryRename() with blank source path should throw", body=function( currentSpec ) {
					expect( function() {
						DirectoryRename( "   ", "newname" );
					}).toThrow();
				});

			});

			// Non-destructive operations - empty path defaults to current directory (sensible behaviour)
			describe( "non-destructive operations should default to current directory", function() {

				it( title="cfdirectory action=list with empty directory lists current dir", body=function( currentSpec ) {
					directory action="list" directory="" name="local.qry";
					expect( local.qry ).toBeQuery();
					expect( local.qry.recordCount ).toBeGT( 0 );
				});

				it( title="DirectoryList() with empty path lists current directory", body=function( currentSpec ) {
					var result = DirectoryList( "" );
					expect( result ).toBeArray();
					expect( result.len() ).toBeGT( 0 );
				});

				it( title="DirectoryExists() with empty path returns false", body=function( currentSpec ) {
					// DirectoryExists explicitly returns false for empty/blank paths
					expect( DirectoryExists( "" ) ).toBeFalse();
				});

			});

			// DirectoryCreate - throws because current directory already exists
			describe( "DirectoryCreate with empty path throws (already exists)", function() {

				it( title="DirectoryCreate() with empty path throws (current dir already exists)", body=function( currentSpec ) {
					expect( function() {
						DirectoryCreate( "" );
					}).toThrow();
				});

			});

		});
	}

}
