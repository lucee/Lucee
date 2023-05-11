component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("testcase for ArraySome()", function() {
			variables.arr = [1,2,3,4];
			it(title="checking ArraySome() function", body=function( currentSpec ) {
				assertEquals( false, ArraySome( arr, function(e) { return e == 5 } )); 
			});
			it(title="checking Array.Some() member function", body=function( currentSpec ) {	
				assertEquals( true, arr.Some(function(e) { return e > 3} )); 
			});
		});
	}
}