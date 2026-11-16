component extends="org.lucee.cfml.test.LuceeTestCase" labels="function" {

	function run( testResults, testBox ) {

		describe( "LDEV-6947 argumentCollection into a (...rest) signature", function() {

			it( "direct positional call gathers the surplus into rest", function() {
				function f( a, b, ...rest ) {
					return rest;
				}
				expect( f( 1, 2, 3, 4 ) ).toBe( [ 3, 4 ] );
				expect( f( 1, 2 ) ).toBe( [] );
			});

			it( "array argumentCollection gathers the surplus into rest", function() {
				function f( a, b, ...rest ) {
					return rest;
				}
				expect( f( argumentCollection=[ 1, 2, 3, 4 ] ) ).toBe( [ 3, 4 ] );
			});

			it( "forwarded scope gathers the surplus into rest", function() {
				function target( a, b, ...rest ) {
					return rest;
				}
				function wrapper( p1, p2 ) {
					return target( argumentCollection=arguments );
				}
				expect( wrapper( 1, 2, 3, 4 ) ).toBe( [ 3, 4 ] );
			});

			it( "forwarded scope after removing a named argument still gathers the surplus", function() {
				function target( a, b, ...rest ) {
					return rest;
				}
				function wrapper( p1, p2 ) {
					structDelete( arguments, "p2" );
					return target( argumentCollection=arguments );
				}
				expect( wrapper( 1, 2, 3, 4 ) ).toBe( [ 3, 4 ] );
			});

			it( "forwarded scope leaves no untranslated integer keys behind", function() {
				function target( a, b, ...rest ) {
					return arguments;
				}
				function wrapper( p1, p2 ) {
					return target( argumentCollection=arguments );
				}
				var args = wrapper( 1, 2, 3, 4 );
				expect( args.rest ).toBe( [ 3, 4 ] );
				expect( structKeyArray( args ).filter( function( k ) {
					return isNumeric( k );
				} ) ).toBe( [] );
			});
		});
	}
}
