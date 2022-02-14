component extends = "org.lucee.cfml.test.LuceeTestCase" skip = true{
	function run( testResults, textbox ) {
		describe("testcase for LDEV-3873", function() {
			it(title="Checking arrayEvery() method with empty array", body=function( currentSpec ) {
				testArray  = [];
				arrayEvery = arrayEvery(testArray , function(value) { true; }); 
				expect(arrayEvery).toBeTrue();
			});
		});
	}
}
