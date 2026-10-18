component extends="org.lucee.cfml.test.LuceeTestCase" labels="directory" {

	function beforeAll() {
		variables.base = getTempDirectory() & "LDEV2152-" & createUUID() & "/";
		directoryCreate( base & "b/d", true, true );
		directoryCreate( base & "n" );
		loop list="a.txt,c.txt,j.txt,b/e.txt,b/d/g.txt,b/d/p.txt,n/h.txt,n/o.txt" item="local.fn" {
			fileWrite( base & fn, "" );
		}
	}

	function afterAll() {
		if ( directoryExists( base ) ) directoryDelete( base, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-2152 recursive directoryList() sorted by directory", function() {

			it( title="listInfo=query, sort='directory asc, name asc'", body=function( currentSpec ) {
				var q = directoryList( base, true, "query", "*.txt", "directory asc, name asc" );
				expect( queryColumnData( q, "name" ) ).toBe( [ "a.txt", "c.txt", "j.txt", "e.txt", "g.txt", "p.txt", "h.txt", "o.txt" ] );
			});

			it( title="listInfo=query, sort='directory desc, name asc'", body=function( currentSpec ) {
				var q = directoryList( base, true, "query", "*.txt", "directory desc, name asc" );
				expect( queryColumnData( q, "name" ) ).toBe( [ "h.txt", "o.txt", "g.txt", "p.txt", "e.txt", "a.txt", "c.txt", "j.txt" ] );
			});

			it( title="listInfo=path, sort='directory asc, name asc'", body=function( currentSpec ) {
				var arr = directoryList( base, true, "path", "*.txt", "directory asc, name asc" );
				expect( relative( arr ) ).toBe( [ "/a.txt", "/c.txt", "/j.txt", "/b/e.txt", "/b/d/g.txt", "/b/d/p.txt", "/n/h.txt", "/n/o.txt" ] );
			});

			it( title="listInfo=path, sort='directory desc, name asc'", body=function( currentSpec ) {
				var arr = directoryList( base, true, "path", "*.txt", "directory desc, name asc" );
				expect( relative( arr ) ).toBe( [ "/n/h.txt", "/n/o.txt", "/b/d/g.txt", "/b/d/p.txt", "/b/e.txt", "/a.txt", "/c.txt", "/j.txt" ] );
			});

			it( title="listInfo=name, sort='directory asc, name asc'", body=function( currentSpec ) {
				var arr = directoryList( base, true, "name", "*.txt", "directory asc, name asc" );
				expect( arr ).toBe( [ "a.txt", "c.txt", "j.txt", "e.txt", "g.txt", "p.txt", "h.txt", "o.txt" ] );
			});

			it( title="listInfo=name, sort='directory desc, name asc'", body=function( currentSpec ) {
				var arr = directoryList( base, true, "name", "*.txt", "directory desc, name asc" );
				expect( arr ).toBe( [ "h.txt", "o.txt", "g.txt", "p.txt", "e.txt", "a.txt", "c.txt", "j.txt" ] );
			});
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
