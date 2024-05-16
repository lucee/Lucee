component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("testcase for ArrayReduce()", function() {
			it(title="checking ArrayReduce() function", body=function( currentSpec ) {
				arr=["there","lucee"]; 
				assertEquals("hello there lucee", ArrayReduce(arr, function(value1,value2) { return (value1 & " " & value2)}, "hello"));
			});
			it(title="checking Array.Reduce() member function", body=function( currentSpec ) {
				arr=["there","lucee"]; 
				assertEquals("hello there lucee", arr.Reduce(function(value1,value2) { return (value1 & " " & value2)}, "hello"));
			});
		});
	}
}