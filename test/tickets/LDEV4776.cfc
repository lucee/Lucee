component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, testBox ) {
		describe( title="Testcase for LDEV-4776", body=function() {
			it( title="Checking decimalFormat()", body=function( currentSpec ) {

				expect( decimalFormat(511.925) ).toBe( 511.93 );
				expect( decimalFormat(512.925) ).toBe( 512.93 );
				expect( decimalFormat(654.925) ).toBe( 654.93 );
				expect( decimalFormat(655.925) ).toBe( 655.93 );

			});
		});
	}
}