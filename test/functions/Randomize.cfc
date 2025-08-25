component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe(title="Testcase for Randomize() function", body=function() {
			it(title="Checking the Randomize() function", body=function( currentSpec ) {
				var algo = 'SHA1PRNG';
				Randomize( 8, algo );
				var r1 = rand( algo );

				Randomize( 12, algo );
				var r2 = rand( algo );

				Randomize( 8, algo );
				var r3 = rand( algo );

				expect( r1 ).toBe( r3 );
				expect( r2 ).notToBe( r3 );

			});

			it(title="Checking the randomize() unknown exception reports supported algpection", body=function( currentSpec ) {
				var e = "";
				try {
					randomize( 8, "UNSUPPORTED" );
				} catch ( err ){
					e = err;
				}
				expect( e ).notToBe("", "randomize function should throw an exception with unsupported algo" );
				var mess = listToArray( e.message, "[]" );
				expect( mess ).toHaveLength( 8 , e.message );
				expect( ListLen( mess[ 8 ] ) ).toBeGT( 3, mess[ 8 ] ); // should produce a list of supported algos
			});
		});
	}
}