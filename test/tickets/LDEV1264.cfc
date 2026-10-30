component extends="org.lucee.cfml.test.LuceeTestCase" labels="null" {
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1264", function() {
			it(title="Checking the array literal collection with bracket notation", body = function( currentSpec ) {
				var result = [ 'a', 'b', 'c' ][ 2 ];
				assertEquals("b", result );
			});

			it(title="Checking the struct literal collection with bracket notation", body = function( currentSpec ) {
				var result = { "one"="tahi", "two"="rua" }[ "one" ];
				assertEquals("tahi", result );
			});

			it(title="Checking the isNull() using array literal collection with bracket notation", body = function( currentSpec ) {
				var result = isNull( [ 'a', 'b', 'c' ][ 6 ] );
				expect(	result ).toBeTrue();
			});

			it(title="Checking the ternary operator using array literal collection with bracket notation", body = function( currentSpec ) {
				var result = [ 'a', 'b', 'c' ][ 6 ] ?: 'default';
				assertEquals("default", result );
			});

			it(title="Checking the isNull() using struct literal collection with bracket notation", body = function( currentSpec ) {
				var result = isNull( { "one"="tahi", "two"="rua" }[ "three" ] );
				expect(	result ).toBeTrue();
			});

			it(title="Checking the ternary operator using struct literal collection with bracket notation", body = function( currentSpec ) {
				var result = { "one"="tahi", "two"="rua" }[ "three" ] ?: 'default';
				assertEquals("default", result );
			});

			it(title="Checking the isNull() on an existing element of a literal collection with bracket notation", body = function( currentSpec ) {
				expect( isNull( [ 'a', 'b', 'c' ][ 2 ] ) ).toBeFalse();
				expect( isNull( { "one"="tahi", "two"="rua" }[ "one" ] ) ).toBeFalse();
			});

			it(title="Checking the isNull() using nested keys on a literal collection with bracket notation", body = function( currentSpec ) {
				expect( isNull( { "one"={ "a"=1 } }[ "one" ][ "b" ] ) ).toBeTrue();
				expect( isNull( { "one"={ "a"=1 } }[ "one" ][ "a" ] ) ).toBeFalse();
				expect( isNull( { "one"={ "a"=1 } }[ "two" ][ "a" ] ) ).toBeTrue();
			});
		});
	}
}