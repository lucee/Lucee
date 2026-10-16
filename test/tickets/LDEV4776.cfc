component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, testBox ) {
		describe( title="Testcase for LDEV-4776", body=function() {
			it( title="Checking decimalFormat()", body=function( currentSpec ) {

				expect( decimalFormat(511.925) ).toBe( 511.93 );
				expect( decimalFormat(512.925) ).toBe( 512.93 );
				expect( decimalFormat(654.925) ).toBe( 654.93 );
				expect( decimalFormat(655.925) ).toBe( 655.93 );

			});
			it( title="decimalFormat() rounds like round() and numberFormat()", body=function( currentSpec ) {
				loop array=[ 1.005, 0.125, 2.675, 1234.565, 512.925, 0.004, 0.005, 10, 1.1 ] item="local.n" {
					expect( decimalFormat( n ) ).toBe( numberFormat( n, ",.00" ), "decimalFormat(#n#)" );
				}
				expect( decimalFormat( 1234567.125 ) ).toBe( "1,234,567.13" );
				expect( decimalFormat( "" ) ).toBe( "0.00" );
				expect( decimalFormat( -512.92 ) ).toBe( "-512.92" );
			});
		});
	}
}
