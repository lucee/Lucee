component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe(title="Testcase for randRange() function", body=function() {
			it(title="Checking the randRange() function", body=function( currentSpec ) {
				expect(randRange(50, 51)).toBeBetween(50, 51);
				expect(randRange(25, 125, 'CFMX_COMPAT')).toBeBetween(25, 125);
				expect(randRange(100, 500, 'SHA1PRNG')).toBeBetween(100, 500);
			});

			it(title="Checking the randRange() unknown exception reports supported algpection", body=function( currentSpec ) {
				var e = "";
				try {
					rand("UNSUPPORTED");
				} catch ( err ){
					e = err;
				}
				expect( e ).notToBe("", "rand function should throw an exception with unsupported algo" );
				var mess = listToArray( e.message, "[]" );
				expect( mess ).toHaveLength( 8 );
				expect( ListLen( mess[ 8 ] ) ).toBeGT( 3, mess[ 8 ] ); // should produce a list of supported algos
				
			});
		});
	}
}