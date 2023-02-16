component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( "check that hash function", function() {

			it(title = "str.hash() returns the same value as hash(str)", body = function( currentSpec ) {
				var testString = "MEANINGLESS_VALUE";
				expect( hash( testString ) ) .toBe( testString.hash() );
			});

			it(title = "Checking with hash() quick", body = function( currentSpec ) {
				var testString = "MEANINGLESS_VALUE";
				expect( hash( testString, "quick" ) ).toBe( testString.hash( "quick" ) );
			});

			it(title = "Checking with hash() quick with numIterations > 1 to throw", body = function( currentSpec ) {
				var testString = "MEANINGLESS_VALUE";
				expect( function(){
					hash( input=testString, algorithm="quick", numIterations=2 );
				}).toThrow();  //for algorithm [quick], argument [numIterations] makes no sense, because this algorithm has no security in mind
			});

			it(title = "Checking with hash() CFMX_COMPAT", body = function( currentSpec ) {
				var testString = "MEANINGLESS_VALUE";
				expect( hash( testString, "CFMX_COMPAT" ) ).toBe( testString.hash( "CFMX_COMPAT" ) );
			});

			it(title = "Checking with hash() MD5", body = function( currentSpec ) {
				var testString = "MEANINGLESS_VALUE";
				expect( hash( testString, "MD5" ) ).toBe( testString.hash( "MD5" ) );
			});

			it(title = "Checking with hash() SHA", body = function( currentSpec ) {
				var testString = "MEANINGLESS_VALUE";
				expect( hash( testString, "SHA" ) ).toBe( testString.hash( "SHA" ) );
			});

			it(title = "Checking with hash() SHA-256", body = function( currentSpec ) {
				var testString = "MEANINGLESS_VALUE";
				expect( hash( testString, "SHA-256" ) ).toBe( testString.hash( "SHA-256" ) );
			});

			it(title = "Checking with hash() SHA-384", body = function( currentSpec ) {
				var testString = "MEANINGLESS_VALUE";
				expect( hash( testString, "SHA-384" ) ).toBe( testString.hash( "SHA-384" ) );
			});

			it(title = "Checking with hash() SHA-512", body = function( currentSpec ) {
				var testString = "MEANINGLESS_VALUE";
				expect( hash( testString, "SHA-512" ) ).toBe( testString.hash( "SHA-512" ) );
			});
			
		});	
	}
}
