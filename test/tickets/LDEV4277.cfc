component extends="org.lucee.cfml.test.LuceeTestCase" labels="string" {

	function run( testResults, testBox ) {
		describe( "Testcase for LDEV-4277", function() {

			it( title="repeatString() with a result that is too long for a string throws a clear expression exception", body=function( currentSpec ) {
				// 10 characters * 300 million = 3 billion characters, more than a string can hold (the length calculation overflowed an int)
				var err = tryRepeat( "abcdefghij", 300000000 );
				expect( err.type ).toBe( "expression" );
				expect( err.message ).toInclude( "too large" );
			});

			it( title="repeatString() member function with a result that is too long for a string throws a clear expression exception", body=function( currentSpec ) {
				var err = {};
				try {
					"abcdefghij".repeatString( 300000000 );
				}
				catch ( any e ) {
					err = e;
				}
				expect( err ).notToBeEmpty( "no exception was thrown" );
				expect( err.type ).toBe( "expression" );
				expect( err.message ).toInclude( "too large" );
			});

			it( title="a count of a few million characters in total still works", body=function( currentSpec ) {
				var res = repeatString( "abcdefghij", 100000 );
				expect( len( res ) ).toBe( 1000000 );
				expect( left( res, 12 ) ).toBe( "abcdefghijab" );
			});

			it( title="repeatString() with count 0 returns an empty string and a negative count still throws", body=function( currentSpec ) {
				expect( repeatString( "abc", 0 ) ).toBe( "" );
				var err = tryRepeat( "abc", -1 );
				expect( err.type ).toBe( "expression" );
				expect( err.message ).toInclude( "non-negative" );
			});
		});
	}

	private struct function tryRepeat( required string str, required numeric count ) {
		try {
			repeatString( arguments.str, arguments.count );
		}
		catch ( any e ) {
			return e;
		}
		return { type: "none", message: "no exception was thrown" };
	}
}
