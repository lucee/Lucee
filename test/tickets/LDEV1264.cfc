component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1262", function() {
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
		});
	}
}