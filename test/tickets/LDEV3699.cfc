component extends="org.lucee.cfml.test.LuceeTestCase" labels="directory" {

	// LDEV-3699 directoryList( path, recurse, "query" ) reads the attributes of a local file with a single
	// file system call, the query returned must stay exactly the same

	variables.isWindows = server.os.name contains "windows";

	// relative path => content, every file has a different size
	variables.files = [
		{ name: "a.txt",         content: "a" },
		{ name: "empty.txt",     content: "" },
		{ name: "b.log",         content: "bbbbbbbb" },
		{ name: ".dotfile",      content: "ddd" },
		{ name: "sub/c.txt",     content: "cccccc" },
		{ name: "sub/d.cfm",     content: "dddddddddd" },
		{ name: "sub/deep/e.txt", content: "eeeeeeeeeeeeee" }
	];
	variables.dirs = [ "sub", "sub/deep", "emptydir" ];

	function beforeAll() {
		variables.base = getTempDirectory() & "LDEV3699-" & createUUID();
		directoryCreate( base );
		loop array=dirs item="local.d" {
			directoryCreate( base & "/" & d );
		}
		var start = dateAdd( "h", -5, now() );
		loop array=files item="local.f" index="local.i" {
			fileWrite( base & "/" & f.name, f.content );
			// different last modified for every file, including milliseconds
			fileSetLastModified( base & "/" & f.name, dateAdd( "l", i * 7919, dateAdd( "n", i * 17, start ) ) );
		}
	}

	function afterAll() {
		if ( directoryExists( base ) ) directoryDelete( base, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-3699 directoryList() query output stays the same", function() {

			it( title="non recursive listing has the same columns and column types as before", body=function( currentSpec ) {
				var q = directoryList( base, false, "query" );
				expect( listSort( q.columnList, "textnocase" ) ).toBe( "attributes,dateLastModified,directory,mode,name,size,type" );
				var meta = getMetaData( q );
				var types = {};
				loop array=meta item="local.col" {
					types[ col.name ] = col.typeName;
				}
				expect( types.name ).toBe( "VARCHAR" );
				expect( types.size ).toBe( "DOUBLE" );
				expect( types.type ).toBe( "VARCHAR" );
				expect( types.dateLastModified ).toBe( "DATE" );
				expect( types.attributes ).toBe( "VARCHAR" );
				expect( types.mode ).toBe( "VARCHAR" );
				expect( types.directory ).toBe( "VARCHAR" );
			});

			it( title="non recursive listing returns name, size, type, date, directory of files and directories", body=function( currentSpec ) {
				var q = directoryList( base, false, "query" );
				var expected = [ ".dotfile", "a.txt", "b.log", "empty.txt", "emptydir", "sub" ];
				expect( sortedNames( q ) ).toBe( expected );
				assertRows( q, "" );
			});

			it( title="recursive listing returns all files and directories with the correct values", body=function( currentSpec ) {
				var q = directoryList( base, true, "query" );
				expect( q.recordCount ).toBe( arrayLen( files ) + arrayLen( dirs ) );
				expect( sortedNames( q ) ).toBe( [ ".dotfile", "a.txt", "b.log", "c.txt", "d.cfm", "deep", "e.txt", "empty.txt", "emptydir", "sub" ] );
				assertRows( q, "" );
			});

			it( title="recursive listing with a filter string returns the correct rows", body=function( currentSpec ) {
				var q = directoryList( path=base, recurse=true, listInfo="query", filter="*.txt" );
				expect( sortedNames( q ) ).toBe( [ "a.txt", "c.txt", "e.txt", "empty.txt" ] );
				assertRows( q, "" );
			});

			it( title="non recursive listing with a filter string returns the correct rows", body=function( currentSpec ) {
				var q = directoryList( path=base & "/sub", recurse=false, listInfo="query", filter="*.cfm" );
				expect( sortedNames( q ) ).toBe( [ "d.cfm" ] );
				assertRows( q, "sub" );
			});

			it( title="listing with a closure filter returns the correct rows", body=function( currentSpec ) {
				var q = directoryList( path=base, recurse=true, listInfo="query", filter=function( path ) {
					return right( path, 4 ) == ".txt";
				} );
				expect( sortedNames( q ) ).toBe( [ "a.txt", "c.txt", "e.txt", "empty.txt" ] );
				assertRows( q, "" );
			});

			it( title="listing with type=file and type=dir returns the correct rows", body=function( currentSpec ) {
				var q = directoryList( path=base, recurse=true, listInfo="query", type="file" );
				expect( q.recordCount ).toBe( arrayLen( files ) );
				expect( valueList( q.type ) ).toBe( repeatString( "File,", arrayLen( files ) - 1 ) & "File" );
				assertRows( q, "" );

				q = directoryList( path=base, recurse=true, listInfo="query", type="dir" );
				expect( sortedNames( q ) ).toBe( [ "deep", "emptydir", "sub" ] );
				assertRows( q, "" );
			});

			it( title="cfdirectory action=list returns the same rows as directoryList()", body=function( currentSpec ) {
				directory action="list" directory=base recurse=true name="local.tagList";
				var q = directoryList( base, true, "query" );
				expect( tagList.recordCount ).toBe( q.recordCount );
				assertRows( tagList, "" );
			});

			it( title="sorted listing returns the correct values", body=function( currentSpec ) {
				var q = directoryList( path=base, recurse=true, listInfo="query", sort="size desc" );
				expect( q.name[ 1 ] ).toBe( "e.txt" );
				expect( q.size[ 1 ] ).toBe( 14 );
				assertRows( q, "" );
			});

			it( title="an empty directory returns an empty query with all the columns", body=function( currentSpec ) {
				var q = directoryList( base & "/emptydir", true, "query" );
				expect( q.recordCount ).toBe( 0 );
				expect( listSort( q.columnList, "textnocase" ) ).toBe( "attributes,dateLastModified,directory,mode,name,size,type" );
			});

			it( title="a file and a directory that does not exist return an empty query", body=function( currentSpec ) {
				expect( directoryList( base & "/missing", true, "query" ).recordCount ).toBe( 0 );
				expect( directoryList( base & "/a.txt", true, "query" ).recordCount ).toBe( 0 );
			});
		});

		if ( !isWindows ) {
			describe( "LDEV-3699 directoryList() query output stays the same for symbolic links", function() {

				it( title="a link to a file and a link to a directory list like the target", body=function( currentSpec ) {
					var linkDir = base & "/links";
					directoryCreate( linkDir );
					try {
						var Files = createObject( "java", "java.nio.file.Files" );
						var Paths = createObject( "java", "java.nio.file.Paths" );
						Files.createSymbolicLink( Paths.get( linkDir & "/tofile.txt", [] ), Paths.get( base & "/b.log", [] ), [] );
						Files.createSymbolicLink( Paths.get( linkDir & "/todir", [] ), Paths.get( base & "/sub", [] ), [] );

						var q = directoryList( linkDir, false, "query" );
						expect( sortedNames( q ) ).toBe( [ "todir", "tofile.txt" ] );
						var rows = {};
						loop query=q {
							rows[ q.name ] = { size: q.size, type: q.type, date: q.dateLastModified };
						}
						expect( rows[ "tofile.txt" ].type ).toBe( "File" );
						expect( rows[ "tofile.txt" ].size ).toBe( 8 );
						expect( rows[ "tofile.txt" ].date ).toBe( getFileInfo( base & "/b.log" ).lastModified );
						expect( rows[ "todir" ].type ).toBe( "Dir" );
						expect( rows[ "todir" ].size ).toBe( 0 );

						// the directory is followed when listing recursive
						q = directoryList( linkDir, true, "query" );
						expect( sortedNames( q ) ).toBe( [ "c.txt", "d.cfm", "deep", "e.txt", "todir", "tofile.txt" ] );
					}
					finally {
						directoryDelete( linkDir, true );
					}
				});

				it( title="a file without write access lists with the same values", body=function( currentSpec ) {
					var dir = base & "/ro";
					directoryCreate( dir );
					var f = dir & "/readonly.txt";
					fileWrite( f, "readonly" );
					fileSetAccessMode( f, "444" );
					try {
						var q = directoryList( path=dir, recurse=false, listInfo="query" );
						expect( q.recordCount ).toBe( 1 );
						expect( q.size ).toBe( 8 );
						expect( q.type ).toBe( "File" );
						expect( q.attributes ).toBe( "" );
						expect( q.mode ).toBe( "444" );
					}
					finally {
						fileSetAccessMode( f, "644" );
						directoryDelete( dir, true );
					}
				});

				it( title="a broken link is listed as a file of size 0", body=function( currentSpec ) {
					var linkDir = base & "/brokenlinks";
					directoryCreate( linkDir );
					try {
						var Files = createObject( "java", "java.nio.file.Files" );
						var Paths = createObject( "java", "java.nio.file.Paths" );
						Files.createSymbolicLink( Paths.get( linkDir & "/broken", [] ), Paths.get( base & "/does-not-exist", [] ), [] );
						var q = directoryList( linkDir, false, "query" );
						expect( q.recordCount ).toBe( 1 );
						expect( q.name ).toBe( "broken" );
						expect( q.type ).toBe( "File" );
						expect( q.size ).toBe( 0 );
					}
					finally {
						directoryDelete( linkDir, true );
					}
				});
			});
		}
		else {
			describe( "LDEV-3699 directoryList() query output stays the same for the Windows attributes", function() {

				it( title="the attributes column contains R for read only and H for hidden", body=function( currentSpec ) {
					var dir = base & "/winattr";
					directoryCreate( dir );
					try {
						fileWrite( dir & "/plain.txt", "p" );
						fileWrite( dir & "/hidden.txt", "hh" );
						fileWrite( dir & "/readonly.txt", "rrr" );
						fileWrite( dir & "/both.txt", "bbbb" );
						fileSetAttribute( dir & "/hidden.txt", "hidden" );
						fileSetAttribute( dir & "/readonly.txt", "readOnly" );
						fileSetAttribute( dir & "/both.txt", "hidden" );
						fileSetAttribute( dir & "/both.txt", "readOnly" );
						var q = directoryList( dir, false, "query" );
						var attrs = {};
						var sizes = {};
						loop query=q {
							attrs[ q.name ] = q.attributes;
							sizes[ q.name ] = q.size;
						}
						expect( attrs[ "plain.txt" ] ).toBe( "" );
						expect( attrs[ "hidden.txt" ] ).toBe( "H" );
						expect( attrs[ "readonly.txt" ] ).toBe( "R" );
						expect( attrs[ "both.txt" ] ).toBe( "RH" );
						expect( sizes[ "both.txt" ] ).toBe( 4 );
					}
					finally {
						fileSetAttribute( dir & "/hidden.txt", "normal" );
						fileSetAttribute( dir & "/readonly.txt", "normal" );
						fileSetAttribute( dir & "/both.txt", "normal" );
						directoryDelete( dir, true );
					}
				});
			});
		}
	}

	private array function sortedNames( required query q ) {
		var names = listToArray( valueList( q.name ) );
		arraySort( names, "textnocase" );
		return names;
	}

	// check every row of the query against what is known from the files created in beforeAll and against
	// the values the individual file functions return
	private void function assertRows( required query q, required string subDir ) {
		var expectedRows = {};
		loop array=files item="local.f" {
			expectedRows[ f.name ] = { type: "File", size: len( f.content ) };
		}
		loop array=dirs item="local.d" {
			expectedRows[ d ] = { type: "Dir", size: 0 };
		}

		loop query=q {
			var found = "";
			var expectedDir = "";
			// find the entry by its name and directory
			loop struct=expectedRows index="local.rel" {
				var relName = listLast( rel, "/" );
				var relParent = len( rel ) > len( relName ) ? left( rel, len( rel ) - len( relName ) - 1 ) : "";
				var dir = normalizePath( base & ( len( relParent ) ? "/" & relParent : "" ) );
				if ( relName == q.name && normalizePath( q.directory ) == dir ) {
					found = rel;
					expectedDir = dir;
					break;
				}
			}
			expect( found ).notToBe( "", "unexpected row [#q.name#] in directory [#q.directory#]" );
			var expectedRow = expectedRows[ found ];
			var info = getFileInfo( base & "/" & found );

			expect( q.type ).toBe( expectedRow.type, found );
			expect( q.size ).toBe( expectedRow.size, found );
			expect( isNumeric( q.size ) ).toBeTrue( found );
			if ( expectedRow.type == "File" ) expect( q.size ).toBe( info.size, found );
			expect( isDate( q.dateLastModified ) ).toBeTrue( found );
			// same date, down to the millisecond, as the one from the individual file function
			expect( q.dateLastModified ).toBe( info.lastModified, found );
			expect( normalizePath( q.directory ) ).toBe( expectedDir, found );
			if ( isWindows ) {
				var attr = ( info.canWrite ? "" : "R" ) & ( info.isHidden ? "H" : "" );
				expect( q.attributes ).toBe( attr, found );
			}
			else {
				expect( q.attributes ).toBe( "", found );
				expect( len( q.mode ) ).toBeGT( 0, found );
			}
		}
	}

	private string function normalizePath( required string path ) {
		return replace( replace( path, "\", "/", "all" ), "//", "/", "all" );
	}
}
