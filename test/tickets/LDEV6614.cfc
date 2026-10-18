component extends="org.lucee.cfml.test.LuceeTestCase" labels="java" {

	function run( testResults, testBox ) {
		describe( "LDEV-6614 createObject/new for Java classes, fast path and caches", function() {

			it( title="new java.lang.StringBuilder() without arguments", body=function( currentSpec ) {
				loop times=3 {
					var sb = new java.lang.StringBuilder();
					sb.append( "a" );
					expect( sb.toString() ).toBe( "a" );
				}
			});

			it( title="new with a String argument picks the CharSequence/String constructor", body=function( currentSpec ) {
				loop times=3 {
					var sb = new java.lang.StringBuilder( "abc" );
					expect( sb.toString() ).toBe( "abc" );
				}
			});

			it( title="new with an int argument picks the capacity constructor", body=function( currentSpec ) {
				loop times=3 {
					var sb = new java.lang.StringBuilder( javaCast( "int", 33 ) );
					expect( sb.toString() ).toBe( "" );
					expect( sb.capacity() ).toBe( 33 );
				}
			});

			it( title="new with a CFML number picks the same constructor as before", body=function( currentSpec ) {
				loop times=3 {
					var sb = new java.lang.StringBuilder( 33 );
					expect( sb.toString() ).toBe( "" );
					expect( sb.capacity() ).toBe( 33 );
				}
			});

			it( title="alternating argument types keep selecting the right overload", body=function( currentSpec ) {
				loop times=3 {
					expect( new java.lang.StringBuilder( "16" ).toString() ).toBe( "16" );
					expect( new java.lang.StringBuilder( javaCast( "int", 16 ) ).capacity() ).toBe( 16 );
					expect( new java.lang.StringBuilder( javaCast( "int", 16 ) ).toString() ).toBe( "" );
					expect( new java.lang.StringBuilder( "16" ).capacity() ).toBe( 18 );
				}
			});

			it( title="BigDecimal String and int constructors", body=function( currentSpec ) {
				loop times=3 {
					expect( new java.math.BigDecimal( "1.5" ).toPlainString() ).toBe( "1.5" );
					expect( new java.math.BigDecimal( javaCast( "int", 3 ) ).toPlainString() ).toBe( "3" );
				}
			});

			it( title="closure as FunctionalInterface constructor argument", body=function( currentSpec ) {
				loop times=3 {
					var task = new java.util.concurrent.FutureTask( function() {
						return "ok";
					} );
					task.run();
					expect( task.get() ).toBe( "ok" );
				}
			});

			it( title="createObject java still works", body=function( currentSpec ) {
				loop times=3 {
					expect( createObject( "java", "java.lang.StringBuilder" ).init().append( "x" ).toString() ).toBe( "x" );
					expect( createObject( "java", "java.lang.StringBuilder" ).init( "y" ).toString() ).toBe( "y" );
					expect( createObject( "java", "java.lang.StringBuilder" ).init( javaCast( "int", 7 ) ).capacity() ).toBe( 7 );
					expect( createObject( "java", "StringBuilder" ).init( "z" ).toString() ).toBe( "z" );
					var list = createObject( "java", "java.util.ArrayList" ).init( javaCast( "int", 5 ) );
					list.add( "a" );
					expect( list.size() ).toBe( 1 );
				}
			});

			it( title="createObject java static access without init", body=function( currentSpec ) {
				loop times=3 {
					expect( createObject( "java", "java.lang.Integer" ).MAX_VALUE ).toBe( 2147483647 );
					expect( createObject( "java", "java.lang.Integer" ).parseInt( "42" ) ).toBe( 42 );
				}
			});

			it( title="unknown classes still throw", body=function( currentSpec ) {
				loop times=2 {
					expect( function() {
						createObject( "java", "java.lang.DoesNotExistLDEV6614" );
					} ).toThrow();
					expect( function() {
						var x = new java.lang.DoesNotExistLDEV6614();
					} ).toThrow();
				}
			});

			it( title="a constructor without match still throws", body=function( currentSpec ) {
				expect( function() {
					var x = new java.lang.StringBuilder( [ 1, 2 ], [ 3 ] );
				} ).toThrow();
			});

			it( title="class lookup cache is cleared with the RPC classloaders", body=function( currentSpec ) {
				var cache = createObject( "java", "lucee.commons.lang.ClassLookupCache" );
				createObject( "java", "java.lang.StringBuilder" );
				expect( cache.size() ).toBeGTE( 1 );
				getPageContext().getConfig().clearRPCClassLoader();
				expect( cache.size() ).toBe( 0 );
				// still works after clearing
				expect( createObject( "java", "java.lang.StringBuilder" ).init( "a" ).toString() ).toBe( "a" );
				expect( new java.lang.StringBuilder( "b" ).toString() ).toBe( "b" );
			});

		});
	}

}
