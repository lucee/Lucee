component extends="org.lucee.cfml.test.LuceeTestCase" labels="directory,resources" {

	function beforeAll() {
		variables.base = getTempDirectory() & "LDEV4003_" & createUniqueId() & "/";
		directoryCreate( variables.base );
	}

	function afterAll() {
		if ( directoryExists( variables.base ) ) directoryDelete( variables.base, true );
	}

	private function createTree( required string root ) {
		directoryCreate( arguments.root & "/sub/deeper", true );
		fileWrite( arguments.root & "/a.txt", "a" );
		fileWrite( arguments.root & "/b.log", "b" );
		fileWrite( arguments.root & "/sub/c.txt", "c" );
		fileWrite( arguments.root & "/sub/deeper/d.txt", "d" );
	}

	private array function listAll( required string root ) {
		var files = directoryList( path=arguments.root, recurse=true, listInfo="path", type="all" );
		var rel = [];
		loop array=files item="local.f" {
			arrayAppend( rel, replace( replace( f, "\", "/", "all" ), replace( arguments.root, "\", "/", "all" ), "" ) );
		}
		arraySort( rel, "text" );
		return rel;
	}

	function run( testResults, testBox ) {

		// the resources used here (local file system, ram) don't implement ResourceCopyMove,
		// so this makes sure the default behaviour is unchanged
		loop array=[ "local", "ram" ] item="local.type" {
			var type = local.type;
			describe( title="LDEV-4003 directory copy and rename on #type#", body=function() {

				it( title="directoryCopy copies the whole tree", body=function( currentSpec ) {
					var root = ( type == "ram" ? "ram:///LDEV4003_" & createUniqueId() : variables.base & "copy1" );
					createTree( root & "/src" );
					directoryCopy( root & "/src", root & "/trg", true );
					expect( listAll( root & "/trg" ) ).toBe( [ "/a.txt", "/b.log", "/sub", "/sub/c.txt", "/sub/deeper", "/sub/deeper/d.txt" ] );
					expect( fileRead( root & "/trg/sub/deeper/d.txt" ) ).toBe( "d" );
					expect( directoryExists( root & "/src" ) ).toBeTrue( "source must still exist" );
				});

				it( title="directoryCopy with filter and recurse", body=function( currentSpec ) {
					var root = ( type == "ram" ? "ram:///LDEV4003_" & createUniqueId() : variables.base & "copy2" );
					createTree( root & "/src" );
					directoryCopy( root & "/src", root & "/trg", true, function( path ) { return right( arguments.path, 4 ) == ".txt" || directoryExists( arguments.path ); } );
					expect( listAll( root & "/trg" ) ).toBe( [ "/a.txt", "/sub", "/sub/c.txt", "/sub/deeper", "/sub/deeper/d.txt" ] );
				});

				it( title="directoryCopy without recurse copies only the files of the first level", body=function( currentSpec ) {
					var root = ( type == "ram" ? "ram:///LDEV4003_" & createUniqueId() : variables.base & "copy3" );
					createTree( root & "/src" );
					directoryCopy( root & "/src", root & "/trg", false );
					expect( listAll( root & "/trg" ) ).toBe( [ "/a.txt", "/b.log" ] );
				});

				it( title="cfdirectory action copy merges into an existing directory and overwrites files", body=function( currentSpec ) {
					var root = ( type == "ram" ? "ram:///LDEV4003_" & createUniqueId() : variables.base & "copy4" );
					createTree( root & "/src" );
					directoryCreate( root & "/trg" );
					fileWrite( root & "/trg/a.txt", "old" );
					fileWrite( root & "/trg/keep.txt", "keep" );
					directory action="copy" directory=root & "/src" destination=root & "/trg" recurse=true nameconflict="overwrite";
					expect( fileRead( root & "/trg/a.txt" ) ).toBe( "a" );
					expect( fileRead( root & "/trg/keep.txt" ) ).toBe( "keep" );
					expect( fileRead( root & "/trg/sub/c.txt" ) ).toBe( "c" );
				});

				it( title="cfdirectory action copy with nameconflict error throws when the destination exists", body=function( currentSpec ) {
					var root = ( type == "ram" ? "ram:///LDEV4003_" & createUniqueId() : variables.base & "copy5" );
					createTree( root & "/src" );
					directoryCreate( root & "/trg" );
					expect( function() {
						directory action="copy" directory=root & "/src" destination=root & "/trg" recurse=true nameconflict="error";
					}).toThrow( type="application", regex="already exists" );
				});

				it( title="directoryCopy throws when the source doesn't exist", body=function( currentSpec ) {
					var root = ( type == "ram" ? "ram:///LDEV4003_" & createUniqueId() : variables.base & "copy6" );
					expect( function() {
						directoryCopy( root & "/nope", root & "/trg", true );
					}).toThrow( type="application", regex="doesn't exist" );
				});

				it( title="directoryRename moves the whole tree", body=function( currentSpec ) {
					var root = ( type == "ram" ? "ram:///LDEV4003_" & createUniqueId() : variables.base & "ren1" );
					createTree( root & "/src" );
					directoryRename( root & "/src", root & "/trg" );
					expect( directoryExists( root & "/src" ) ).toBeFalse( "source must be gone" );
					expect( listAll( root & "/trg" ) ).toBe( [ "/a.txt", "/b.log", "/sub", "/sub/c.txt", "/sub/deeper", "/sub/deeper/d.txt" ] );
					expect( fileRead( root & "/trg/sub/c.txt" ) ).toBe( "c" );
				});

				it( title="directoryRename creates the missing parent path", body=function( currentSpec ) {
					var root = ( type == "ram" ? "ram:///LDEV4003_" & createUniqueId() : variables.base & "ren2" );
					createTree( root & "/src" );
					directoryRename( root & "/src", root & "/x/y/trg" );
					expect( fileRead( root & "/x/y/trg/sub/deeper/d.txt" ) ).toBe( "d" );
				});

				it( title="directoryRename throws when the new directory already exists", body=function( currentSpec ) {
					var root = ( type == "ram" ? "ram:///LDEV4003_" & createUniqueId() : variables.base & "ren3" );
					createTree( root & "/src" );
					directoryCreate( root & "/trg" );
					expect( function() {
						directoryRename( root & "/src", root & "/trg" );
					}).toThrow( type="application", regex="already exists" );
					expect( directoryExists( root & "/src" ) ).toBeTrue( "source must be untouched" );
				});

				it( title="directoryRename throws when the source doesn't exist", body=function( currentSpec ) {
					var root = ( type == "ram" ? "ram:///LDEV4003_" & createUniqueId() : variables.base & "ren4" );
					expect( function() {
						directoryRename( root & "/nope", root & "/trg" );
					}).toThrow( type="application", regex="doesn't exist" );
				});

			});
		}

		describe( title="LDEV-4003 copy and rename between different resource types", body=function() {

			it( title="directoryCopy from ram to the local file system", body=function( currentSpec ) {
				var src = "ram:///LDEV4003_" & createUniqueId();
				createTree( src );
				var trg = variables.base & "cross1";
				directoryCopy( src, trg, true );
				expect( listAll( trg ) ).toBe( [ "/a.txt", "/b.log", "/sub", "/sub/c.txt", "/sub/deeper", "/sub/deeper/d.txt" ] );
			});

			it( title="directoryRename from ram to the local file system", body=function( currentSpec ) {
				var src = "ram:///LDEV4003_" & createUniqueId();
				createTree( src );
				var trg = variables.base & "cross2";
				directoryRename( src, trg );
				expect( directoryExists( src ) ).toBeFalse();
				expect( fileRead( trg & "/sub/deeper/d.txt" ) ).toBe( "d" );
			});

		});
	}
}
