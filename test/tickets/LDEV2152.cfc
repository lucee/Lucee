component extends="org.lucee.cfml.test.LuceeTestCase" {

  function beforeAll(){
		variables.base = GetDirectoryFromPath(getcurrentTemplatepath()) & "LDEV2152\";
		if( directoryExists( base ) ){
			directoryDelete (base, true );
		}
 		directoryCreate( base );

		var dirList = "b,n";
		dirlist.each(function( index ){
			directorycreate( base & index );
			if( index is "b" ){
				directoryCreate (base & 'b\d' );
			}
		})
		var fileList = "a.txt,c.txt,j.txt";
		var fileList.listEach( function( index ){
			fileWrite( base & index, "" );
			fileWrite( base & 'b\e.txt', "" );
			filewrite( base & 'b\d\g.txt', "" );
			filewrite( base & 'b\d\p.txt', "" );
			filewrite( base & 'n\h.txt', "" );
			filewrite( base & 'n\o.txt', "" );
		});
		/*
		systemOutput("----testdata -----", true );
		var dirList = directorylist( base, true, 'path', '*.txt', 'directory ASC');
		loop array=dirList item="local.dir" index="local.i" {
			systemOutput( dir, true );
		}
		systemOutput("---------", true );
		*/
	}

	function run( testResults , testBox ) {
		describe( "test suite for LDEV-2152", function() {
			it(title = "recursive directorylist() with attribute listinfo = 'query'", skip=true, body = function( currentSpec ) {
				var dirList = directorylist( base, true, 'query', '*.txt', 'directory ASC');
				var names = queryColumnData( dirList, "name" );
				expect( names ).toBe ( [ "a.txt", "c.txt", "j.txt", "e.txt", "g.txt", "p.txt", "h.txt", "o.txt" ] );
			});

			it(title = "recursive directorylist() with attribute listinfo = 'query',sort = 'desc'", skip=true, body = function( currentSpec ) {
				var dirList = directorylist( base, true, 'query', '*.txt', 'directory DESC');
				var names = queryColumnData( dirList, "name" );
				expect( names ).toBe(['h.txt','o.txt','g.txt','p.txt','e.txt','a.txt','c.txt','j.txt']);
			});

			it(title = "recursive directorylist() with attribute listinfo = 'path', sort directory ASC", skip=true, body = function( currentSpec ) {
				var dirList = directorylist( base, true, 'path', '*.txt', 'directory ASC');
				loop array=dirList item="local.dir" index="local.i" {
					dirList[ local.i ] =  replace( listlast( dir, "LDEV2152" ), "\", "/", "all" );
				}
				expect( dirList ).toBe( [ '/a.txt', '/c.txt','/j.txt','/b/e.txt','/b/d/g.txt','/b/d/p.txt','/n/h.txt','/n/o.txt' ] );
			});

			it(title = "recursive directorylist() with attribute listinfo = 'path',sort = 'directory desc'", skip=true, body = function( currentSpec ) {
				var dirList = directorylist( base, true, 'path', '*.txt', 'directory DESC');
				loop array=dirList item="local.dir" index="local.i" {
					dirList[ local.i ] =  replace( listlast( dir, "LDEV2152" ), "\", "/", "all" );
				}
				expect ( dirList ).toBe( ['/n/h.txt','/n/o.txt','/b/d/g.txt', '/b/d/p.txt', '/b/e.txt', '/a.txt', '/c.txt', '/j.txt'] );
			});

			// fails 5.3
			it(title = "recursive directorylist() with attribute listinfo = 'name', sort directory ASC", skip=true, body = function( currentSpec ) {
				var dirList = directorylist( base, true, 'name', '*.txt', 'directory ASC');
				expect( dirList ).toBe( ['a.txt', 'c.txt', 'j.txt', 'e.txt', 'g.txt', 'p.txt', 'h.txt', 'o.txt'] );
			});

			// fails 5.3
			it(title = "recursive directorylist() with attribute listinfo = 'name',sort = 'directory desc'", skip=true, body = function( currentSpec ) {
				var dirList = directorylist( base, true, 'name', '*.txt', 'directory DESC');
				expect( dirList ).toBe( [ 'h.txt', 'o.txt', 'g.txt', 'p.txt', 'e.txt', 'a.txt', 'c.txt', 'j.txt' ] );
			});
		});

		describe( "test suite for LDEV-3188", function() {
			//  directorylist() - sort order doesn't work properly for listinfo - name.

			it(title = "recursive directorylist() with attribute listinfo = 'name', sort='name'", body = function( currentSpec ) {
				var dirList = directorylist( base, true, 'name', '*.txt', 'name');
				expect( dirList ).toBe( ['a.txt', 'c.txt', 'e.txt', 'g.txt', 'h.txt', 'j.txt', 'o.txt', 'p.txt'] );
			});

			it(title = "recursive directorylist() with attribute listinfo = 'name', sort='name asc'", body = function( currentSpec ) {
				var dirList = directorylist( base, true, 'name', '*.txt', 'name asc');
				expect( dirList ).toBe( ['a.txt', 'c.txt', 'e.txt', 'g.txt', 'h.txt', 'j.txt', 'o.txt', 'p.txt'] );
			});

			it(title = "recursive directorylist() with attribute listinfo = 'name', sort='name DESC'", body = function( currentSpec ) {
				var dirList = directorylist( base, true, 'name', '*.txt', 'name desc');
				expect( dirList ).toBe( ['p.txt', 'o.txt', 'j.txt', 'h.txt', 'g.txt', 'e.txt', 'c.txt', 'a.txt'] );
			});

			it(title = "recursive directorylist() with attribute listinfo = 'path', sort='path DESC'", body = function( currentSpec ) {
				var dirList = directorylist( base, true, 'name', '*.txt', 'name desc');
				loop array=dirList item="local.dir" index="local.i" {
					dirList[ local.i ] =  listlast( dir, "LDEV2152" );
				}
				expect( dirList ).toBe( ['p.txt', 'o.txt', 'j.txt', 'h.txt', 'g.txt', 'e.txt', 'c.txt', 'a.txt'] );
			});
		});
	}

	function afterAll(){
		if ( directoryExists( base ) ){
			directoryDelete( base, true) ;
		}
	}
}