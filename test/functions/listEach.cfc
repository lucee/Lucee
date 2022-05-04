component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("testcase for listEach()", function() {
			variables.list = "a,b,c,d,e,f";
			it(title="checking listEach() function", body=function( currentSpec ) {
				var result = "";
				listEach(list, function(value) { 
					result &= value;
				});
				assertEquals("abcdef", result);
			});
			it(title="checking list.listEach() member function", body=function( currentSpec ) {
				var result = "";

				list.listEach(function(value) { 
					result &= value;
				});
				assertEquals("abcdef", result);
			});
		});
	}
}