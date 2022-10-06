component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("testcase for listEvery()", function() {
			variables.list = "a,b,c,d,e,f";
			it(title="checking listEvery() function", body=function( currentSpec ) {
				assertEquals( false, listEvery( list, function(e) { return e == "a"; } )); 
			});
			it(title="checking list.Every() member function", body=function( currentSpec ) {
				assertEquals( true, list.Every( function(e) { return e != "g";  } )); 
			});
		});
	}
}

