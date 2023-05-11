component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("testcase for ArrayEvery()", function() {
			it(title="checking ArrayEvery() function", body=function( currentSpec ) {
				var arr = [1,2,3,4,5,6,7,8,9];
				assertEquals( false, ArrayEvery( arr, function(e) { return e > 5 } )); 
			});
			it(title="checking Array.Every() member function", body=function( currentSpec ) {
				var arr = [6,7,8,9];
				assertEquals( true, arr.Every( function(e) { return e > 5 } )); 
			});
		});
	}
}