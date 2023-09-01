component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, textbox ) {

		describe(title="Testcase for LDEV-4480 and LDEV-4448", body=function() {

			it(title="checking treating . as a number", body=function( currentSpec ) {
				var dot = ".";
				expect( isNumeric( dot ) ).toBeTrue();
				expect( dot * 1 ).toBe( 0 );
			});

			it(title="checking treating 0. as a number", body=function( currentSpec ) {
				var dot = "0.";
				expect( isNumeric( dot ) ).toBeTrue();
				expect( dot * 1 ).toBe( 0 );
			});

			it(title="checking treating .0 as a number", body=function( currentSpec ) {
				var dot = ".0";
				expect( isNumeric( dot ) ).toBeTrue();
				expect( dot * 1 ).toBe( 0 );
			});

		});
	}
}
