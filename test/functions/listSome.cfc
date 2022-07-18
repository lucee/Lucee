component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("testcase for listSome()", function() {
			variables.list = "a,b,c,d,e,f";
			it(title="checking listSome() function", body=function( currentSpec ) {
				assertEquals( true, listSome( list, function(e) { return e == "a"; } )); 
				assertEquals( false, listSome( list, function(e) { return e  == "p"; } ));
			});
			it(title="checking list.listSome() member function", body=function( currentSpec ) {
				assertEquals( true, list.listSome( function(e) { return e  != "g"; } )); 
				assertEquals( false, list.listSome( function(e) { return e  == "j"; } ));
			});
		});
	}
}