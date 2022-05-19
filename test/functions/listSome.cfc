component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("testcase for listSome()", function() {
			variables.list = "a,b,c,d,e,f";
			it(title="checking listSome() function", body=function( currentSpec ) {throw "stop the build";
				assertEquals( true, listSome( list, function(e) { return e == "a"; } )); 
			});
			it(title="checking list.Some() member function", body=function( currentSpec ) {
				assertEquals( false, list.Some( function(e) { return e  == "j"; } )); 
			});
		});
	}
}