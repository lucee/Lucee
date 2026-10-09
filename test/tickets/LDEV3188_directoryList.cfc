component extends="org.lucee.cfml.test.LuceeTestCase" labels="directory" {

	// LDEV-3188 directoryList() sort must also apply to listInfo="name" and listInfo="path"

	// written out of alphabetical order, each file with a distinct size and lastModified
	variables.files = [
		{ name: "j.txt",     content: "jjjjjjj" },
		{ name: "a.txt",     content: "a" },
		{ name: "n/o.txt",   content: "oooo" },
		{ name: "c.txt",     content: "ccc" },
		{ name: "b/e.txt",   content: "eeeee" },
		{ name: "b/d/p.txt", content: "pp" },
		{ name: "n/h.txt",   content: "hhhhhhhh" },
		{ name: "b/d/g.txt", content: "gggggg" }
	];

	// expected order of the file names (recursive, filter *.txt) per sort
	variables.expected = [
		"name":                     [ "a.txt", "c.txt", "e.txt", "g.txt", "h.txt", "j.txt", "o.txt", "p.txt" ],
		"name asc":                 [ "a.txt", "c.txt", "e.txt", "g.txt", "h.txt", "j.txt", "o.txt", "p.txt" ],
		"name desc":                [ "p.txt", "o.txt", "j.txt", "h.txt", "g.txt", "e.txt", "c.txt", "a.txt" ],
		"size":                     [ "a.txt", "p.txt", "c.txt", "o.txt", "e.txt", "g.txt", "j.txt", "h.txt" ],
		"size desc":                [ "h.txt", "j.txt", "g.txt", "e.txt", "o.txt", "c.txt", "p.txt", "a.txt" ],
		"datelastmodified":         [ "j.txt", "a.txt", "o.txt", "c.txt", "e.txt", "p.txt", "h.txt", "g.txt" ],
		"datelastmodified desc":    [ "g.txt", "h.txt", "p.txt", "e.txt", "c.txt", "o.txt", "a.txt", "j.txt" ],
		"directory asc, name desc": [ "j.txt", "c.txt", "a.txt", "e.txt", "p.txt", "g.txt", "o.txt", "h.txt" ]
	];
	// relative path of each file name
	variables.relPath = {};
	variables.files.each( function( f ) {
		variables.relPath[ listLast( f.name, "/" ) ] = "/" & f.name;
	});

	function beforeAll() {
		variables.base = getTempDirectory() & "LDEV3188-" & createUUID() & "/";
		directoryCreate( base & "b/d", true, true );
		directoryCreate( base & "n" );
		var start = dateAdd( "h", -2, now() );
		loop array=files item="local.f" index="local.i" {
			fileWrite( base & f.name, f.content );
			fileSetLastModified( base & f.name, dateAdd( "n", i * 5, start ) );
		}
	}

	function afterAll() {
		if ( directoryExists( base ) ) directoryDelete( base, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-3188 directoryList() without sort (fast path unchanged)", function() {

			it( title="directoryList(dir) returns absolute paths of the direct children", body=function( currentSpec ) {
				var arr = directoryList( base );
				arraySort( arr, "textnocase" );
				expect( relative( arr ) ).toBe( [ "/a.txt", "/b", "/c.txt", "/j.txt", "/n" ] );
				loop array=arr item="local.p" {
					expect( fileExists( p ) || directoryExists( p ) ).toBeTrue( p );
				}
			});

			it( title="directoryList(dir, true) returns absolute paths of all entries", body=function( currentSpec ) {
				var arr = directoryList( base, true );
				arraySort( arr, "textnocase" );
				expect( relative( arr ) ).toBe( [ "/a.txt", "/b", "/b/d", "/b/d/g.txt", "/b/d/p.txt", "/b/e.txt", "/c.txt", "/j.txt", "/n", "/n/h.txt", "/n/o.txt" ] );
				loop array=arr item="local.p" {
					expect( fileExists( p ) || directoryExists( p ) ).toBeTrue( p );
				}
			});

			it( title="directoryList(dir, true, 'path', '*.txt') returns absolute paths", body=function( currentSpec ) {
				var arr = directoryList( base, true, "path", "*.txt" );
				arraySort( arr, "textnocase" );
				expect( relative( arr ) ).toBe( [ "/a.txt", "/b/d/g.txt", "/b/d/p.txt", "/b/e.txt", "/c.txt", "/j.txt", "/n/h.txt", "/n/o.txt" ] );
			});

			it( title="directoryList(dir, true, 'name', '*.txt') returns names", body=function( currentSpec ) {
				var arr = directoryList( base, true, "name", "*.txt" );
				arraySort( arr, "textnocase" );
				expect( arr ).toBe( [ "a.txt", "c.txt", "e.txt", "g.txt", "h.txt", "j.txt", "o.txt", "p.txt" ] );
			});

			it( title="empty sort is treated as no sort", body=function( currentSpec ) {
				var arr = directoryList( base, true, "path", "*.txt", "" );
				arraySort( arr, "textnocase" );
				expect( relative( arr ) ).toBe( [ "/a.txt", "/b/d/g.txt", "/b/d/p.txt", "/b/e.txt", "/c.txt", "/j.txt", "/n/h.txt", "/n/o.txt" ] );
			});
		});

		describe( "LDEV-3188 directoryList() with sort", function() {
			loop collection=variables.expected key="local.sortBy" value="local.names" {
				_sortSpecs( sortBy, names );
			}

			it( title="sorted path array contains the same paths as the unsorted one", body=function( currentSpec ) {
				var unsorted = directoryList( base, true, "path" );
				var sorted = directoryList( base, true, "path", "", "name desc" );
				arraySort( unsorted, "textnocase" );
				arraySort( sorted, "textnocase" );
				expect( sorted ).toBe( unsorted );
			});

			it( title="non recursive with filter, listInfo=path, sort='size desc'", body=function( currentSpec ) {
				var arr = directoryList( base, false, "path", "*.txt", "size desc" );
				expect( relative( arr ) ).toBe( [ "/j.txt", "/c.txt", "/a.txt" ] );
			});

			it( title="non recursive without filter, listInfo=name, sort='name desc'", body=function( currentSpec ) {
				var arr = directoryList( base, false, "name", "", "name desc" );
				expect( arr ).toBe( [ "n", "j.txt", "c.txt", "b", "a.txt" ] );
			});

			it( title="type=file, listInfo=name, sort='name desc'", body=function( currentSpec ) {
				var arr = directoryList( path=base, recurse=true, listInfo="name", sort="name desc", type="file" );
				expect( arr ).toBe( expected[ "name desc" ] );
			});

			it( title="type=dir, listInfo=path, sort='name desc'", body=function( currentSpec ) {
				var arr = directoryList( path=base, recurse=true, listInfo="path", sort="name desc", type="dir" );
				expect( relative( arr ) ).toBe( [ "/n", "/b/d", "/b" ] );
			});

			it( title="sort column names are case insensitive", body=function( currentSpec ) {
				var arr = directoryList( base, true, "name", "*.txt", "Size DESC" );
				expect( arr ).toBe( expected[ "size desc" ] );
			});
		});

		describe( "LDEV-3188 cfdirectory action=list with sort", function() {

			it( title="listInfo=all, sort='name desc'", body=function( currentSpec ) {
				directory action="list" directory=base recurse=true filter="*.txt" sort="name desc" name="local.q";
				expect( queryColumnData( q, "name" ) ).toBe( expected[ "name desc" ] );
			});

			it( title="listInfo=all, sort='size desc'", body=function( currentSpec ) {
				directory action="list" directory=base recurse=true filter="*.txt" sort="size desc" name="local.q";
				expect( queryColumnData( q, "name" ) ).toBe( expected[ "size desc" ] );
			});

			it( title="listInfo=all, sort='datelastmodified'", body=function( currentSpec ) {
				directory action="list" directory=base recurse=true filter="*.txt" sort="datelastmodified" name="local.q";
				expect( queryColumnData( q, "name" ) ).toBe( expected[ "datelastmodified" ] );
			});

			it( title="listInfo=all, sort='directory asc, name desc'", body=function( currentSpec ) {
				directory action="list" directory=base recurse=true filter="*.txt" sort="directory asc, name desc" name="local.q";
				expect( queryColumnData( q, "name" ) ).toBe( expected[ "directory asc, name desc" ] );
			});

			it( title="listInfo=name, sort='name desc'", body=function( currentSpec ) {
				directory action="list" directory=base recurse=true listinfo="name" filter="*.txt" sort="name desc" name="local.q";
				expect( queryColumnData( q, "name" ) ).toBe( [ "n/o.txt", "n/h.txt", "j.txt", "c.txt", "b/e.txt", "b/d/p.txt", "b/d/g.txt", "a.txt" ] );
			});

			it( title="listInfo=name, sort='name'", body=function( currentSpec ) {
				directory action="list" directory=base recurse=true listinfo="name" filter="*.txt" sort="name" name="local.q";
				expect( queryColumnData( q, "name" ) ).toBe( [ "a.txt", "b/d/g.txt", "b/d/p.txt", "b/e.txt", "c.txt", "j.txt", "n/h.txt", "n/o.txt" ] );
			});
		});
	}

	private function _sortSpecs( required string sortBy, required array names ) {
		var sortBy = arguments.sortBy;
		var names = arguments.names;

		it( title="listInfo=query, sort='#sortBy#'", data={ sortBy: sortBy, names: names }, body=function( data ) {
			var q = directoryList( base, true, "query", "*.txt", data.sortBy );
			expect( queryColumnData( q, "name" ) ).toBe( data.names );
		});

		it( title="listInfo=name, sort='#sortBy#'", data={ sortBy: sortBy, names: names }, body=function( data ) {
			var arr = directoryList( base, true, "name", "*.txt", data.sortBy );
			expect( arr ).toBe( data.names );
		});

		it( title="listInfo=path, sort='#sortBy#'", data={ sortBy: sortBy, names: names }, body=function( data ) {
			var arr = directoryList( base, true, "path", "*.txt", data.sortBy );
			expect( relative( arr ) ).toBe( data.names.map( function( n ) { return relPath[ n ]; } ) );
		});
	}

	private array function relative( required array paths ) {
		var root = replace( base, "\", "/", "all" );
		if ( right( root, 1 ) == "/" ) root = left( root, len( root ) - 1 );
		return arguments.paths.map( function( p ) {
			return replace( replace( p, "\", "/", "all" ), root, "" );
		});
	}
}
