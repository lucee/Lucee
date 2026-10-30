component extends="org.lucee.cfml.test.LuceeTestCase" labels="interface" {

	function run( testResults, testBox ) {
		describe( title="LDEV-170 implementing function may add optional arguments to an interface function", body=function() {

			it( title="same signature is accepted", body=function( currentSpec ) {
				expect( new LDEV170.ImplSame().f( 4 ) ).toBe( 4 );
			});

			it( title="additional optional argument is accepted", body=function( currentSpec ) {
				var o = new LDEV170.ImplAdditionalOptional();
				expect( o.f( 4 ) ).toBe( 8 );
				expect( o.f( 4, 3 ) ).toBe( 12 );
			});

			it( title="additional required argument is rejected", body=function( currentSpec ) {
				expect( function() {
					new LDEV170.ImplAdditionalRequired();
				}).toThrow( type="application" );
			});

			it( title="missing argument is rejected", body=function( currentSpec ) {
				expect( function() {
					new LDEV170.ImplMissingArgument();
				}).toThrow( type="application" );
			});

		});
	}

}
