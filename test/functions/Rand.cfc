component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe(title="Testcase for rand() function", body=function() {
			it(title="Checking the rand() function", body=function( currentSpec ) {
				expect(rand()).toBeBetween(0, 1);
				expect(rand("SHA1PRNG")).toBeBetween(0, 1);
				expect(rand("CFMX_COMPAT")).toBeBetween(0, 1);
			});

			it(title="Checking the rand() unknown exception reports supported algpection", body=function( currentSpec ) {
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