component extends="org.lucee.cfml.test.LuceeTestCase" labels="regex" {

	function run( testResults, testBox ) {

		describe( "LDEV-6632 regex engines: java is built in, perl is provided by the Perl Regex extension", function() {

			beforeEach( function() {
				variables.origEngine = getApplicationSettings().regex;
			} );

			afterEach( function() {
				application action="update" regex=variables.origEngine;
			} );

			it( title="java is the default engine", body=function( currentSpec ) {
				expect( variables.origEngine.type ).toBe( "java" );
				expect( reReplace( "abc", "(b)", "[\1]" ) ).toBeString();
			} );

			it( title="java and compat are built in", body=function( currentSpec ) {
				application action="update" regex={ type: "java" };
				expect( reFind( "b+", "abbbc" ) ).toBe( 2 );
				application action="update" regex={ type: "compat" };
				expect( reFind( "b+", "abbbc" ) ).toBe( 2 );
				expect( reReplace( "abc", "(b)", "[\1]" ) ).toBe( "a[b]c" );
			} );

			it( title="an unknown engine name is rejected", body=function( currentSpec ) {
				expect( function() {
					application action="update" regex={ type: "doesnotexist" };
				} ).toThrow( message="invalid regex name" );
			} );

			it( title="perl is accepted as a setting, without the extension using it fails with a clear message", body=function( currentSpec ) {
				application action="update" regex={ type: "perl" };
				expect( getApplicationSettings().regex.type ).toBe( "perl" );

				var available = true;
				var msg = "";
				try {
					reFind( "b+", "abbbc" );
				}
				catch ( any e ) {
					available = false;
					msg = e.message;
				}
				if ( available ) {
					expect( reFind( "b+", "abbbc" ) ).toBe( 2 );
				}
				else {
					expect( msg ).toInclude( "[perl]" );
					expect( msg ).toInclude( "not available" );
				}
			} );

		} );

	}

}
