component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("testcase for listEvery()", function() {
			variables.list = "a,b,c,d,e,f";
			it(title="checking listEvery() function", body=function( currentSpec ) {
				assertEquals( true, listEvery( list, function(e) { return e != "p";  } ));
				assertEquals( false, listEvery( list, function(e) { return e == "a"; } )); 
			});
			it(title="checking list.listEvery() member function", body=function( currentSpec ) {
				assertEquals( true, list.listEvery( function(e) { return e != "g";  } )); 
				assertEquals( false, list.listEvery( function(e) { return e == "e";  } ));
			});
		});
	}
}