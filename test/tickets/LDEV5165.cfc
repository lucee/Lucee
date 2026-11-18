component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-5165 script components are only parsed into lines once", function() {

			it( "compiles a script component", function() {
				var cfc = new LDEV5165.Modern();
				expect( cfc.hello() ).toBe( "hi" );
			});

			it( "reports the correct line number for errors in a script component", function() {
				var cfc = new LDEV5165.Modern();
				try {
					cfc.fail();
					fail( "expected an exception" );
				}
				catch ( any e ) {
					expect( e.message ).toBe( "LDEV5165" );
					expect( e.tagContext[ 1 ].template ).toInclude( "Modern.cfc" );
					expect( e.tagContext[ 1 ].line ).toBe( 7 );
				}
			});

			it( "compiles a script component containing a tag in a comment", function() {
				var cfc = new LDEV5165.ModernWithTagInComment();
				expect( cfc.hello() ).toBe( "<cfoutput>" );
			});

			it( "compiles a tag based component", function() {
				var cfc = new LDEV5165.Tag();
				expect( cfc.hello() ).toBe( "hi" );
			});

		});
	}
}
