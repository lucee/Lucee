component extends = "org.lucee.cfml.test.LuceeTestCase" labels="array" skip=true {
	function run( testResults, textbox ) {
		describe("testcase for LDEV-3873", function() {
			it(title="Checking arrayEvery() method with empty array", body=function( currentSpec ) {
				var testArray  = [];
				var result = arrayEvery( testArray , function( value ) { true; } );
				expect( result ).toBeTrue();
			});
		});
	}
}
