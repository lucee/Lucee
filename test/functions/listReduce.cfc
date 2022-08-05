component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		variables.list="there,lucee"; 
		describe("testcase for listReduce()", function() {
			it(title="checking listReduce() function", body=function( currentSpec ) {
				assertEquals( "hello there lucee", listReduce(list, function(value1,value2) { return (value1 & " " & value2)}, "hello"));
			});
			it(title="checking list.listReduce() member function", body=function( currentSpec ) {
				assertEquals( "hello there lucee", list.listReduce(function(value1,value2) { return (value1 & " " & value2)}, "hello"));
			});
		});
	}
}