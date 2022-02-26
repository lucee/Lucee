component extends = "org.lucee.cfml.test.LuceeTestCase" labels="array" {
	function run( testResults, textbox ) {
		describe("testcase for LDEV-3873", function() {
			it(title="Checking arrayEvery() method with empty array", skip=true, body=function( currentSpec ) {
				var testArray  = [];
				var result = arrayEvery( testArray , function( value ) { true; } );
				expect( result ).toBeTrue();
			});
		});
	}
}
