component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("testcase for ArrayMap()", function() {
			it(title="checking ArrayMap() function", body=function( currentSpec ) {
				var arr = [1,2,3];
				assertEquals(serializeJSON([5, 10, 15]), serializeJSON(ArrayMap( arr, function(e)  { return e * 5 } ))); 
			});
			it(title="checking Array.Map() member function", body=function( currentSpec ) {
				var arr = ["Hello","World"];
				assertEquals(serializeJSON(["Helloa", "Worlda"]), serializeJSON(arr.Map( function(e) { return e & 'a' } ))); 
			});
		});
	}
}