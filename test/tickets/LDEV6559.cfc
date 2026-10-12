component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {

		describe( "for-in over Java objects which already work", function() {

			it( title="ArrayList gives values", body=function() {
				expect( forIn( newList( [ "a", "b" ] ) ) ).toBe( [ "a", "b" ] );
			});

			it( title="ArrayList without init is empty", body=function() {
				expect( forIn( createObject( "java", "java.util.ArrayList" ) ) ).toBe( [] );
			});

			it( title="HashSet gives values", body=function() {
				var res = forIn( createObject( "java", "java.util.HashSet" ).init( [ "foo", "bar" ] ) );
				arraySort( res, "text" );
				expect( res ).toBe( [ "bar", "foo" ] );
			});

			it( title="TreeSet gives values in order", body=function() {
				expect( forIn( createObject( "java", "java.util.TreeSet" ).init( [ "b", "a" ] ) ) ).toBe( [ "a", "b" ] );
			});

			it( title="LinkedHashMap gives keys", body=function() {
				expect( forIn( newMap() ) ).toBe( [ "k1", "k2" ] );
			});

			it( title="Iterator gives values", body=function() {
				expect( forIn( newList( [ "a", "b" ] ).iterator() ) ).toBe( [ "a", "b" ] );
			});

			it( title="Enumeration gives values", body=function() {
				var en = createObject( "java", "java.util.Collections" ).enumeration( newList( [ "a", "b" ] ) );
				expect( forIn( en ) ).toBe( [ "a", "b" ] );
			});

			it( title="native String[] gives values", body=function() {
				var arr = createObject( "java", "java.lang.String" ).init( "x,y" ).split( "," );
				expect( forIn( arr ) ).toBe( [ "x", "y" ] );
			});

			it( title="class ref without init gives public field names", body=function() {
				var res = forIn( createObject( "java", "java.awt.Point" ) );
				arraySort( res, "text" );
				expect( res ).toBe( [ "x", "y" ] );
			});

			it( title="non iterable instance throws", body=function() {
				var pt = createObject( "java", "java.awt.Point" ).init( 1, 2 );
				expect( function() {
					forIn( pt );
				}).toThrow();
			});
		});

		describe( "loop collection= over Java objects which already work", function() {

			it( title="ArrayList item gives indexes", body=function() {
				expect( loopItem( newList( [ "a", "b" ] ) ) ).toBe( [ "1", "2" ] );
			});

			it( title="ArrayList index + item", body=function() {
				expect( loopKV( newList( [ "a", "b" ] ) ) ).toBe( [ "1=a", "2=b" ] );
			});

			it( title="LinkedHashMap item gives keys", body=function() {
				expect( loopItem( newMap() ) ).toBe( [ "k1", "k2" ] );
			});

			it( title="LinkedHashMap index + item", body=function() {
				expect( loopKV( newMap() ) ).toBe( [ "k1=v1", "k2=v2" ] );
			});

			it( title="Iterator item gives values", body=function() {
				expect( loopItem( newList( [ "a", "b" ] ).iterator() ) ).toBe( [ "a", "b" ] );
			});

			it( title="ArrayDeque index + item gives value=value", body=function() {
				expect( loopKV( newDeque( [ "a", "b" ] ) ) ).toBe( [ "a=a", "b=b" ] );
			});

			it( title="Iterator index + item gives value=value", body=function() {
				expect( loopKV( newList( [ "a", "b" ] ).iterator() ) ).toBe( [ "a=a", "b=b" ] );
			});
		});

		describe( "loop collection= index + item over an Enumeration", function() {

			it( title="Enumeration index + item gives value=value", body=function() {
				var en = createObject( "java", "java.util.Collections" ).enumeration( newList( [ "a", "b" ] ) );
				expect( loopKV( en ) ).toBe( [ "a=a", "b=b" ] );
			});
		});

		describe( "looping over something which isn't loopable", function() {

			it( title="for-in gives a loop error", body=function() {
				var e = catchError( function( pt ) {
					forIn( arguments.pt );
				});
				expectNotLoopable( e );
			});

			it( title="loop collection item gives a loop error", body=function() {
				var e = catchError( function( pt ) {
					loopItem( arguments.pt );
				});
				expectNotLoopable( e );
			});

			it( title="loop collection index + item gives the same loop error", body=function() {
				var e = catchError( function( pt ) {
					loopKV( arguments.pt );
				});
				expectNotLoopable( e );
			});

			it( title="for-in keeps the original cast error as the cause", body=function() {
				var e = catchError( function( pt ) {
					forIn( arguments.pt );
				});
				expect( e.cause.message ).toInclude( "to a value of type [collection]" );
			});
		});

		describe( "for-in over a Java Collection which isn't a List or Set", function() {

			it( title="ArrayDeque gives values", body=function() {
				expect( forIn( newDeque( [ "a", "b" ] ) ) ).toBe( [ "a", "b" ] );
			});

			it( title="ConcurrentLinkedQueue gives values", body=function() {
				var q = createObject( "java", "java.util.concurrent.ConcurrentLinkedQueue" ).init( [ "a", "b" ] );
				expect( forIn( q ) ).toBe( [ "a", "b" ] );
			});

			it( title="PriorityQueue gives values", body=function() {
				var q = createObject( "java", "java.util.PriorityQueue" ).init( [ "a" ] );
				expect( forIn( q ) ).toBe( [ "a" ] );
			});

			it( title="ArrayDeque loop collection item gives values", body=function() {
				expect( loopItem( newDeque( [ "a", "b" ] ) ) ).toBe( [ "a", "b" ] );
			});
		});

		describe( "for-in over a plain Java Iterable", function() {

			it( title="Path gives its segments", body=function() {
				expect( forIn( newPath( "a/b/c" ) ) ).toBe( [ "a", "b", "c" ] );
			});

			it( title="Path loop collection item gives its segments", body=function() {
				expect( loopItem( newPath( "a/b/c" ) ) ).toBe( [ "a", "b", "c" ] );
			});

			it( title="Path loop collection index + item gives value=value", body=function() {
				expect( loopKV( newPath( "a/b" ) ) ).toBe( [ "a=a", "b=b" ] );
			});

			it( title="SQLException walks the exception chain", body=function() {
				var ex = createObject( "java", "java.sql.SQLException" ).init( "outer" );
				ex.setNextException( createObject( "java", "java.sql.SQLException" ).init( "next" ) );
				var res = [];
				for ( var e in ex ) arrayAppend( res, e.getMessage() );
				expect( res ).toBe( [ "outer", "next" ] );
			});

			it( title="SQLException class ref without init iterates the instance, not field names", body=function() {
				var res = [];
				for ( var e in createObject( "java", "java.sql.SQLException" ) ) arrayAppend( res, e.getClass().getName() );
				expect( res ).toBe( [ "java.sql.SQLException" ] );
			});

			it( title="Iterable passed through a function argument", body=function() {
				var fn = function( it ) {
					return forIn( arguments.it );
				};
				expect( fn( newPath( "a/b" ) ) ).toBe( [ "a", "b" ] );
			});

			it( title="break then loop again starts from the beginning", body=function() {
				var p = newPath( "a/b/c" );
				for ( var x in p ) {
					break;
				}
				expect( forIn( p ) ).toBe( [ "a", "b", "c" ] );
			});

			it( title="nested loops over the same Iterable are independent", body=function() {
				var p = newPath( "a/b" );
				var res = [];
				for ( var x in p ) {
					for ( var y in p ) {
						arrayAppend( res, toString( x ) & toString( y ) );
					}
				}
				expect( res ).toBe( [ "aa", "ab", "ba", "bb" ] );
			});

			it( title="DirectoryStream, a single use Iterable", body=function() {
				var dir = getTempDirectory() & "ForInJava/";
				if ( directoryExists( dir ) ) directoryDelete( dir, true );
				directoryCreate( dir );
				fileWrite( dir & "one.txt", "1" );
				fileWrite( dir & "two.txt", "2" );
				var ds = createObject( "java", "java.nio.file.Files" ).newDirectoryStream( newPath( dir ) );
				try {
					var res = [];
					for ( var f in ds ) arrayAppend( res, toString( f.getFileName() ) );
				}
				finally {
					ds.close();
				}
				arraySort( res, "text" );
				expect( res ).toBe( [ "one.txt", "two.txt" ] );
			});

			it( title="empty DirectoryStream loops zero times", body=function() {
				var dir = getTempDirectory() & "ForInJava-empty/";
				if ( directoryExists( dir ) ) directoryDelete( dir, true );
				directoryCreate( dir );
				var ds = createObject( "java", "java.nio.file.Files" ).newDirectoryStream( newPath( dir ) );
				try {
					var res = forIn( ds );
				}
				finally {
					ds.close();
				}
				expect( res ).toBe( [] );
			});
		});
	}

	private array function forIn( required obj ) {
		var res = [];
		for ( var x in arguments.obj ) arrayAppend( res, toString( x ) );
		return res;
	}

	private array function loopItem( required obj ) {
		var res = [];
		loop collection=arguments.obj item="local.x" {
			arrayAppend( res, toString( x ) );
		}
		return res;
	}

	private array function loopKV( required obj ) {
		var res = [];
		loop collection=arguments.obj index="local.k" item="local.v" {
			arrayAppend( res, toString( k ) & "=" & toString( v ) );
		}
		return res;
	}

	private function catchError( required function fn ) {
		try {
			arguments.fn( createObject( "java", "java.awt.Point" ).init( 1, 2 ) );
		}
		catch ( any e ) {
			return e;
		}
		throw( message="expected an error, none was thrown" );
	}

	private function expectNotLoopable( required e ) {
		expect( arguments.e.type ).toBe( "expression" );
		expect( arguments.e.message ).toBe( "Can't loop over an object of type [java.awt.Point]" );
		expect( arguments.e.detail ).toInclude( "Java type of the object is java.awt.Point" );
		expect( arguments.e.detail ).toInclude( "Iterables" );
	}

	private function newList( required array values ) {
		var list = createObject( "java", "java.util.ArrayList" ).init();
		for ( var v in arguments.values ) list.add( v );
		return list;
	}

	private function newDeque( required array values ) {
		var dq = createObject( "java", "java.util.ArrayDeque" ).init();
		for ( var v in arguments.values ) dq.add( v );
		return dq;
	}

	private function newMap() {
		var map = createObject( "java", "java.util.LinkedHashMap" ).init();
		map.put( "k1", "v1" );
		map.put( "k2", "v2" );
		return map;
	}

	private function newPath( required string p ) {
		return createObject( "java", "java.io.File" ).init( arguments.p ).toPath();
	}
}
