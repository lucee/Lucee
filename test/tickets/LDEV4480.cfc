component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, textbox ) {

		describe(title="Testcase for LDEV-4480 and LDEV-4448", body=function() {

			it(title="checking treating . as a number", skip=true, body=function( currentSpec ) {
				var dot = ".";
				expect( isNumeric( dot ) ).toBeFalse();
				expect ( function(){
					var x = dot * 1;
				}).toThrow();
			});
			it(title="dot should not be equal to 0", skip=true, body=function( currentSpec ) {
				var dot = ".";
				var zero = "0";
				expect( zero == dot ).toBeFalse();
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
